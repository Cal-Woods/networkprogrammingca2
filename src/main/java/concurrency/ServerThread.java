package concurrency;

import interfaces.EmailManager;
import lombok.extern.slf4j.Slf4j;
import model.Email;
import model.RegisterModel;
import services.AuthService;
import utils.Validators; // Connects your Validators
import exceptions.InvalidEmailFormatException; // Connects your Custom Exception

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

@Slf4j
public class ServerThread implements Runnable {
    private final Socket socket;
    private final EmailManager emailManager;
    private final String separator;
    private String currentUser = null;
    private final AuthService authService;

    public ServerThread(Socket socket, EmailManager manager, String separator, AuthService authService) {
        this.socket = socket;
        this.emailManager = manager;
        this.separator = separator;
        this.authService = authService;
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
                if(!line.contains(separator)) {
                    log.error("Invalid command received from client!##" + line);
                    out.println("Invalid command!");
                    continue;
                }
                String[] tokens = line.split(separator);
                String command = tokens[0].toUpperCase().trim();

                switch (command) {
                    case "LOGIN":
                        if (tokens.length != 3) {
                            out.println("400 ERROR##Usage: LOGIN##username##password");
                        }

                        String loginToken = authService.authenticate(tokens[1], tokens[2], socket.getInetAddress().getHostAddress());

                        if (loginToken == null) {
                            out.println("400 ERROR##Incorrect username or password!");
                            break;
                        }

                        out.println("TOKEN##" + loginToken);
                        break;

                    case "SEND":
                        handleSend(tokens, out);
                        break;

                    case "QUIT":
                        out.println("221 Goodbye");
                        socket.close();
                        return; // Exit the thread

                    default:
                        out.println("500 ERROR##Unknown Command");
                        break;
                }
            }
        }
        catch (IOException e) {
            log.error("Server error, cannot start: " + e.getMessage());
            return;
        }
        catch (Exception e) {
            log.error("Client error: {}", e.getMessage());
            return;
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