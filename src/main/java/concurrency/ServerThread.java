package concurrency;

import exceptions.InvalidEmailFormatException;
import interfaces.EmailManager;
import lombok.extern.slf4j.Slf4j;
import model.Email;
import model.RegisterModel;
import services.AuthService;
import utils.Validators;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ServerThread implements Runnable {
    private final Socket socket;
    private final EmailManager emailManager;
    private final AuthService authService;
    private final AtomicInteger emailIdSequence;
    private final String separator;
    private String currentUser;
    private String currentToken;

    public ServerThread(Socket socket, EmailManager manager, String separator, AuthService authService, AtomicInteger emailIdSequence) {
        this.socket = socket;
        this.emailManager = manager;
        this.authService = authService;
        this.emailIdSequence = emailIdSequence;
        this.separator = separator;
    }

    @Override
    public void run() {
        try (Scanner in = new Scanner(socket.getInputStream());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("220##WELCOME##Email Server ready");

            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String command = extractCommand(line);

                switch (command) {
                    case "REGISTER" -> handleRegister(line, out);
                    case "LOGIN" -> handleLogin(line, out);
                    case "SEND" -> handleSend(line, out);
                    case "LIST-INBOX", "INBOX" -> handleListInbox(out);
                    case "SEARCH-INBOX" -> handleSearchInbox(line, out);
                    case "LIST-SENT", "SENT" -> handleListSent(out);
                    case "SEARCH-SENT" -> handleSearchSent(line, out);
                    case "READ" -> handleRead(line, out);
                    case "LOGOUT" -> handleLogout(out);
                    case "QUIT" -> {
                        handleLogout(out);
                        out.println("221##BYE");
                        return;
                    }
                    default -> out.println("400##ERROR##UNKNOWN_COMMAND");
                }
            }
        } catch (Exception e) {
            log.error("Client error: {}", e.getMessage());
        } finally {
            handleDisconnect();
        }
    }

    private void handleRegister(String line, PrintWriter out) {
        String[] tokens = line.split(separator, 7);
        if (tokens.length < 6 || tokens.length > 7) {
            out.println("400##REGISTER##USAGE##REGISTER##first##last##email##password##confirm##phone?");
            return;
        }

        RegisterModel registerModel = new RegisterModel();

        try {
            registerModel.validateFirstLastName(tokens[1], tokens[2]);
            registerModel.validateEmail(tokens[3]);
            registerModel.validatePasswords(tokens[4], tokens[5]);
            if (tokens.length == 7) {
                registerModel.validatePhoneNumber(tokens[6]);
            }

            boolean registered = authService.register(registerModel);
            if (registered) {
                out.println("201##REGISTER##OK##" + registerModel.getEmail());
            } else {
                out.println("409##REGISTER##EXISTS");
            }
        } catch (InvalidEmailFormatException e) {
            out.println("400##REGISTER##INVALID_EMAIL##" + e.getMessage());
        } catch (IllegalArgumentException e) {
            out.println("400##REGISTER##INVALID_DATA##" + e.getMessage());
        }
    }

    private void handleLogin(String line, PrintWriter out) {
        if (isAuthenticated()) {
            out.println("409##LOGIN##ALREADY_AUTHENTICATED");
            return;
        }

        String[] tokens = line.split(separator, 3);
        if (tokens.length != 3) {
            out.println("400##LOGIN##USAGE##LOGIN##email##password");
            return;
        }

        try {
            String token = authService.authenticate(tokens[1], tokens[2]);
            if (token == null) {
                out.println("401##LOGIN##FAILED");
                return;
            }

            this.currentUser = tokens[1];
            this.currentToken = token;
            out.println("200##LOGIN##OK##" + currentUser);
        } catch (InvalidEmailFormatException e) {
            out.println("400##LOGIN##INVALID_EMAIL##" + e.getMessage());
        }
    }

    private void handleSend(String line, PrintWriter out) {
        if (!isAuthenticated()) {
            out.println("401##AUTH_REQUIRED");
            return;
        }

        String[] tokens = line.split(separator, 4);
        if (tokens.length != 4) {
            out.println("400##SEND##USAGE##SEND##recipient##subject##body");
            return;
        }

        try {
            Validators.validateEmail(tokens[1]);
            if (!authService.isRegistered(tokens[1])) {
                out.println("404##SEND##RECIPIENT_NOT_REGISTERED");
                return;
            }

            Email email = new Email(
                    emailIdSequence.getAndIncrement(),
                    currentUser,
                    tokens[1],
                    tokens[2],
                    tokens[3]
            );

            emailManager.storeEmail(email);
            out.println("201##SEND##OK##" + email.getEmailId());
        } catch (InvalidEmailFormatException e) {
            out.println("400##SEND##INVALID_RECIPIENT##" + e.getMessage());
        } catch (IllegalArgumentException e) {
            out.println("400##SEND##INVALID_DATA##" + e.getMessage());
        }
    }

    private void handleListInbox(PrintWriter out) {
        if (!isAuthenticated()) {
            out.println("401##AUTH_REQUIRED");
            return;
        }

        List<Email> emails = emailManager.getReceivedEmails(currentUser);
        writeEmailList(out, "INBOX", emails, true);
    }

    private void handleSearchInbox(String line, PrintWriter out) {
        if (!isAuthenticated()) {
            out.println("401##AUTH_REQUIRED");
            return;
        }

        String[] tokens = line.split(separator, 2);
        if (tokens.length != 2) {
            out.println("400##SEARCH-INBOX##USAGE##SEARCH-INBOX##query");
            return;
        }

        List<Email> emails = emailManager.searchReceivedEmails(currentUser, tokens[1]);
        writeEmailList(out, "SEARCH-INBOX", emails, true);
    }

    private void handleListSent(PrintWriter out) {
        if (!isAuthenticated()) {
            out.println("401##AUTH_REQUIRED");
            return;
        }

        List<Email> emails = emailManager.getSentEmails(currentUser);
        writeEmailList(out, "SENT", emails, false);
    }

    private void handleSearchSent(String line, PrintWriter out) {
        if (!isAuthenticated()) {
            out.println("401##AUTH_REQUIRED");
            return;
        }

        String[] tokens = line.split(separator, 2);
        if (tokens.length != 2) {
            out.println("400##SEARCH-SENT##USAGE##SEARCH-SENT##query");
            return;
        }

        List<Email> emails = emailManager.searchSentEmails(currentUser, tokens[1]);
        writeEmailList(out, "SEARCH-SENT", emails, false);
    }

    private void handleRead(String line, PrintWriter out) {
        if (!isAuthenticated()) {
            out.println("401##AUTH_REQUIRED");
            return;
        }

        String[] tokens = line.split(separator, 2);
        if (tokens.length != 2) {
            out.println("400##READ##USAGE##READ##emailId");
            return;
        }

        try {
            int emailId = Integer.parseInt(tokens[1].trim());
            Optional<Email> email = emailManager.getEmailById(currentUser, emailId);
            if (email.isPresent()) {
                out.println(email.get().toReadResponseLine());
            } else {
                out.println("404##READ##NOT_FOUND##" + emailId);
            }
        } catch (NumberFormatException e) {
            out.println("400##READ##INVALID_ID");
        }
    }

    private void handleLogout(PrintWriter out) {
        if (currentToken != null) {
            authService.logout(currentToken);
        }

        currentUser = null;
        currentToken = null;
        out.println("200##LOGOUT##OK");
    }

    private void handleDisconnect() {
        if (currentToken != null) {
            authService.logout(currentToken);
        }

        try {
            socket.close();
        } catch (Exception ignored) {
        }
    }

    private boolean isAuthenticated() {
        return currentUser != null && !currentUser.isBlank();
    }

    private String extractCommand(String line) {
        int separatorIndex = line.indexOf(separator);
        if (separatorIndex < 0) {
            return line.toUpperCase();
        }
        return line.substring(0, separatorIndex).trim().toUpperCase();
    }

    private void writeEmailList(PrintWriter out, String label, List<Email> emails, boolean inbox) {
        if (emails == null || emails.isEmpty()) {
            out.println("204##" + label + "##EMPTY");
            out.println("END");
            return;
        }

        out.println("200##" + label + "##" + emails.size());
        for (Email email : emails) {
            out.println(inbox ? email.toInboxMetadataLine() : email.toSentMetadataLine());
        }
        out.println("END");
    }
}
