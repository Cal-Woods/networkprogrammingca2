package concurrency;

import interfaces.EmailManager;
import lombok.extern.slf4j.Slf4j;
import model.Email;
import utils.Validators; // Connects your Validators
import exceptions.InvalidEmailFormatException; // Connects your Custom Exception
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

@Slf4j
public class ServerThread implements Runnable {
    private final Socket socket;
    private final EmailManager emailManager;
    private final String separator;
    private String currentUser = null;

    public ServerThread(Socket socket, EmailManager manager, String separator) {
        this.socket = socket;
        this.emailManager = manager;
        this.separator = separator;
    }

    @Override
    public void run() {
        try (Scanner in = new Scanner(socket.getInputStream());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Handshake greeting
            out.println("220 Welcome to the Email Server");

            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split(separator);
                String command = tokens[0].toUpperCase().trim();

                switch (command) {
                    case "LOGIN":
                        if (tokens.length < 2) {
                            out.println("400 ERROR##Usage: LOGIN##username");
                        } else {
                            this.currentUser = tokens[1];
                            out.println("200 OK##Welcome " + currentUser);
                        }
                        break;

                    case "SEND":
                        handleSend(tokens, out);
                        break;

                    case "QUIT":
                        out.println("221 Goodbye");
                        return; // Exit the thread

                    default:
                        out.println("500 ERROR##Unknown Command");
                        break;
                }
            }
        } catch (Exception e) {
            log.error("Client error: {}", e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private void handleSend(String[] tokens, PrintWriter out) {
        try {
            // 1. Check if logged in
            if (currentUser == null) {
                out.println("401 ERROR##Please LOGIN first");
                return;
            }

            // 2. Check if we have all parts (SEND##to##subject##body)
            if (tokens.length < 4) {
                out.println("400 ERROR##Missing data. Use SEND##to##sub##body");
                return;
            }

            // 3. VALIDATION: This triggers  Validators.java code!
            Validators.validateEmail(tokens[1]);

            // 4. LOGIC: Create and save the email
            Email email = new Email(
                    (int)(Math.random() * 1000),
                    currentUser,
                    tokens[1],
                    tokens[2],
                    tokens[3]
            );

            emailManager.storeEmail(email);
            out.println("200 OK##Email sent successfully to " + tokens[1]);

        } catch (InvalidEmailFormatException e) {
            // Catches the specific error from your Validator
            out.println("400 ERROR##" + e.getMessage());
        } catch (Exception e) {
            out.println("500 ERROR##Server processing error");
        }
    }
}