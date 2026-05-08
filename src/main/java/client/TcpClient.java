package client;

import exceptions.InvalidEmailFormatException;
import lombok.extern.slf4j.Slf4j;
import model.RegisterModel;
import utils.Validators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

@Slf4j
public class TcpClient {
    private static boolean loggedIn = false;
    private static String currentUser;
    private static String host = "localhost";
    private static int port = 50000;

    public static void main(String[] args) {
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner console = new Scanner(System.in)) {

            String greeting = in.readLine();
            if (greeting != null) {
                System.out.println("Server: " + greeting);
            }

            while (true) {
                printPrompt();
                String input = console.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

                String command = extractCommand(input);

                if ("HELP".equals(command)) {
                    printHelp();
                    continue;
                }

                if ("QUIT".equals(command)) {
                    out.println("QUIT");
                    printUntilTerminal(in);
                    break;
                }

                if ("LOGOUT".equals(command)) {
                    out.println("LOGOUT");
                    String response = in.readLine();
                    handleSingleLineResponse(response);
                    loggedIn = false;
                    currentUser = null;
                    continue;
                }

                if (!validateAndSend(input, command, out)) {
                    continue;
                }

                if (isMultiLineCommand(command)) {
                    printResponseBlock(in);
                } else {
                    String response = in.readLine();
                    handleSingleLineResponse(response);
                    if (response != null && response.startsWith("200##LOGIN##OK##")) {
                        loggedIn = true;
                        currentUser = response.split("##", 4)[3];
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private static boolean validateAndSend(String input, String command, PrintWriter out) {
        try {
            switch (command) {
                case "LOGIN" -> {
                    if (loggedIn) {
                        System.out.println("You are already logged in.");
                        return false;
                    }
                    String[] tokens = input.split("##", 3);
                    if (tokens.length != 3) {
                        System.out.println("Invalid LOGIN command. Use LOGIN##email##password");
                        return false;
                    }
                    Validators.validateEmail(tokens[1]);
                    if (tokens[2].length() < 8) {
                        System.out.println("Invalid password, must be at least 8 characters.");
                        return false;
                    }
                    out.println(input);
                }
                case "REGISTER" -> {
                    String[] tokens = input.split("##", 7);
                    if (tokens.length < 6 || tokens.length > 7) {
                        System.out.println("Invalid REGISTER command. Use REGISTER##first##last##email##password##confirm##phone?");
                        return false;
                    }

                    RegisterModel registerModel = new RegisterModel();
                    registerModel.validateFirstLastName(tokens[1], tokens[2]);
                    registerModel.validateEmail(tokens[3]);
                    registerModel.validatePasswords(tokens[4], tokens[5]);
                    if (tokens.length == 7) {
                        registerModel.validatePhoneNumber(tokens[6]);
                    }
                    out.println(input);
                }
                case "SEND" -> {
                    if (!loggedIn) {
                        System.out.println("You must log in before sending mail.");
                        return false;
                    }
                    String[] tokens = input.split("##", 4);
                    if (tokens.length != 4) {
                        System.out.println("Invalid SEND command. Use SEND##recipient##subject##body");
                        return false;
                    }
                    Validators.validateEmail(tokens[1]);
                    out.println(input);
                }
                case "INBOX", "LIST-INBOX", "SENT", "LIST-SENT" -> {
                    if (!loggedIn) {
                        System.out.println("You must log in first.");
                        return false;
                    }
                    out.println(input);
                }
                case "SEARCH-INBOX", "SEARCH-SENT" -> {
                    if (!loggedIn) {
                        System.out.println("You must log in first.");
                        return false;
                    }
                    String[] tokens = input.split("##", 2);
                    if (tokens.length != 2 || tokens[1].isBlank()) {
                        System.out.println("Invalid search command. Use " + command + "##query");
                        return false;
                    }
                    out.println(input);
                }
                case "READ" -> {
                    if (!loggedIn) {
                        System.out.println("You must log in first.");
                        return false;
                    }
                    String[] tokens = input.split("##", 2);
                    if (tokens.length != 2) {
                        System.out.println("Invalid READ command. Use READ##emailId");
                        return false;
                    }
                    Integer.parseInt(tokens[1].trim());
                    out.println(input);
                }
                default -> {
                    out.println(input);
                }
            }
        } catch (InvalidEmailFormatException e) {
            System.out.println("Invalid email format: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            System.out.println("READ expects a numeric email id.");
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid data: " + e.getMessage());
            return false;
        }

        return true;
    }

    private static boolean isMultiLineCommand(String command) {
        return "INBOX".equals(command)
                || "LIST-INBOX".equals(command)
                || "SENT".equals(command)
                || "LIST-SENT".equals(command)
                || "SEARCH-INBOX".equals(command)
                || "SEARCH-SENT".equals(command);
    }

    private static void printResponseBlock(BufferedReader in) throws IOException {
        String response = in.readLine();
        if (response == null) {
            System.out.println("Server closed the connection.");
            return;
        }

        System.out.println(response);
        while (true) {
            String line = in.readLine();
            if (line == null || "END".equals(line)) {
                break;
            }
            printEmailLine(line);
        }
    }

    private static void printUntilTerminal(BufferedReader in) throws IOException {
        String response;
        while ((response = in.readLine()) != null) {
            System.out.println(response);
            if (response.startsWith("221##BYE")) {
                break;
            }
        }
    }

    private static void handleSingleLineResponse(String response) {
        if (response == null) {
            System.out.println("Server closed the connection.");
            return;
        }

        if (response.startsWith("200##LOGIN##OK##")) {
            currentUser = response.split("##", 4)[3];
            loggedIn = true;
        } else if (response.startsWith("200##READ##OK##")) {
            String[] parts = response.split("##", 9);
            if (parts.length >= 9) {
                System.out.println("Email ID: " + parts[3]);
                System.out.println("From: " + parts[4]);
                System.out.println("To: " + parts[5]);
                System.out.println("Subject: " + parts[6]);
                System.out.println("Timestamp: " + parts[7]);
                System.out.println("Body: " + parts[8]);
                return;
            }
        }

        System.out.println(response);
    }

    private static void printEmailLine(String response) {
        if (!response.startsWith("EMAIL##")) {
            System.out.println(response);
            return;
        }

        String[] parts = response.split("##", 6);
        if (parts.length < 5) {
            System.out.println(response);
            return;
        }

        String id = parts[1];
        String party = parts[2];
        String subject = parts[3];
        String timestamp = parts[4];

        System.out.println("ID: " + id + " | " + party + " | Subject: " + subject + " | " + timestamp);
    }

    private static void printPrompt() {
        String userLabel = currentUser == null ? "guest" : currentUser;
        System.out.println();
        System.out.println("Current user: " + userLabel);
        System.out.println("Commands: REGISTER##first##last##email##password##confirm##phone?, LOGIN##email##password, SEND##recipient##subject##body, INBOX, SENT, SEARCH-INBOX##query, SEARCH-SENT##query, READ##id, LOGOUT, QUIT, HELP");
        System.out.print("> ");
    }

    private static void printHelp() {
        System.out.println("REGISTER##first##last##email##password##confirm##phone?");
        System.out.println("LOGIN##email##password");
        System.out.println("SEND##recipient##subject##body");
        System.out.println("INBOX");
        System.out.println("SENT");
        System.out.println("SEARCH-INBOX##query");
        System.out.println("SEARCH-SENT##query");
        System.out.println("READ##id");
        System.out.println("LOGOUT");
        System.out.println("QUIT");
    }

    private static String extractCommand(String input) {
        int separatorIndex = input.indexOf("##");
        if (separatorIndex < 0) {
            return input.toUpperCase();
        }
        return input.substring(0, separatorIndex).trim().toUpperCase();
    }
}
