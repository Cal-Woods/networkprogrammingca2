package client;

import exceptions.InvalidEmailFormatException;
import lombok.extern.slf4j.Slf4j;
import model.RegisterModel;
import utils.Validators;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

@Slf4j
public class TcpClient {
    private static String loginToken = "";
    private static String loginClaim;

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 50000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner console = new Scanner(System.in)) {

            System.out.println("Connected to Server: " + in.readLine());

            String input = "";

            while (true) {
                System.out.println("You are " + (loginClaim != null ? "logged in as: " + loginClaim + "!" : "not logged in!"));

                System.out.println("\nCommands:\n  LOGIN##username##password\n  SEND##to##sub##body(Must be logged in)\n  VIEW-EMAILS(Must be logged in)\n  LOGOUT\n  QUIT");
                System.out.print("> ");

                input = console.nextLine();

                //Validate single-part commands
                if(input.equalsIgnoreCase("QUIT")) {
                    out.println(input);
                    out.close();
                    in.close();
                    socket.close();
                    System.out.println("Goodbye!");
                    return;
                }
                if(input.equalsIgnoreCase("LOGOUT")) {
                    if(loginToken.isBlank()) {
                        System.out.println("You are not logged in so you do not need to logout!");
                        continue;
                    }

                    out.println(input + "##" + loginToken);

                    loginToken = "";
                    loginClaim = null;
                    System.out.println("Logged out successfully!");
                    continue;
                }

                String[] inputTokens = null;

                if(!input.contains("##")) {
                    System.out.println("Invalid Command!");
                    continue;
                }

                inputTokens = input.split("##");

                //Validate tokens
                try {
                    for (int i = 0; i < inputTokens.length; i++) {
                        //Set each token to validated
                        inputTokens[i] = Validators.validateStringData(inputTokens[i]);
                    }
                }
                catch(IllegalArgumentException e) {
                    System.out.println("Parts of the given command were invalid, check your command for" +
                            "errors!");
                    continue;
                }

                //Validate multi-part commands
                switch(inputTokens[0].toUpperCase()) {
                    case "LOGIN":
                        if(inputTokens.length != 3) {
                            log.info(("An invalid login command was entered into input!"));
                            System.out.println("Invalid command!");
                            continue;
                        }
                        try {
                            Validators.validateEmail(inputTokens[1]);
                            if(inputTokens[2].length() < 8) {
                                log.info(("An invalid password was given as login parameter!"));
                                System.out.println("Invalid password, must be at least 8 characters!");
                                continue;
                            }

                            out.println(input);
                        }
                        catch(InvalidEmailFormatException e) {
                            log.info("Could not complete login operation as given password was less than 8 characters!");
                            System.out.println("Invalid username, must be a valid email address!");
                            continue;
                        }
                        break;
                    case "SEND":
                        if(inputTokens.length != 4) {
                            System.out.println("Invalid command!");
                            continue;
                        }

                        out.println(input);
                        break;
                    case "REGISTER":
                        if(inputTokens.length != 6 && inputTokens.length != 7) {
                            log.error("Invalid REGISTER command was entered into input! {}", inputTokens.length);
                            System.out.println("Invalid REGISTER command, not enough arguments!");
                            continue;
                        }

                        RegisterModel registerModel = new RegisterModel();

                        try {
                            registerModel.validateFirstLastName(inputTokens[1],  inputTokens[2]);
                            registerModel.validateEmail(inputTokens[3]);
                            registerModel.validatePasswords(inputTokens[4], inputTokens[5]);
                            if(inputTokens.length == 7) {
                                registerModel.validatePhoneNumber(inputTokens[6]);
                            }

                            out.println(input);
                        }
                        catch(IllegalArgumentException e) {
                            log.error("Invalid register data was given by user! {}",  e.getMessage());
                            System.out.println("Invalid register data, " + e.getMessage());
                        }
                        catch(InvalidEmailFormatException e) {
                            log.error("Invalid email format! {}",  e.getMessage());
                            System.out.println("Invalid email format, " + e.getMessage());
                        }
                        break;
                }

                String response = in.readLine();

                if(response.matches("^TOKEN##[a-zA-Z0-9-]{36}$")) {
                    loginToken = response.substring(7);
                    loginClaim = inputTokens[1];
                    continue;
                }

                System.out.println(response);
            }
        }
        catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}