package client;

import exceptions.InvalidEmailFormatException;
import lombok.extern.slf4j.Slf4j;
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
                if(!loginToken.isBlank()) {
                    System.out.println("You are logged in as " + loginClaim != null ? loginClaim +"!" : "Not logged in!");
                }
                System.out.println("\nCommands:\n  LOGIN##username##password\n  SEND##to##sub##body(Must be logged in)\n  VIEW-EMAILS(Must be logged in)\n  LOGOUT\n  QUIT");
                System.out.print("> ");

                input = console.nextLine();

                //Validate single-part commands
                if(input.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    break;
                }
                if(input.equalsIgnoreCase("LOGOUT")) {
                    if(loginToken.equals("")) {
                        System.out.println("You are not logged in so you do not need to logout!");
                        loginClaim = "";
                        continue;
                    }

                    out.println(input);

                    loginToken = "";
                    loginClaim = "";
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
                        break;
                        //TODO: Finish validating command structure
//                    case "":
//
//                        break;
                }

                String response = in.readLine();

                if(response.matches("^TOKEN##[a-zA-Z0-9-]+$")) {
                    loginToken = response;
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