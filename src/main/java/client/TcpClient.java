package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TcpClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5555);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner console = new Scanner(System.in)) {

            System.out.println("Connected to Server: " + in.readLine());

            while (true) {
                System.out.println("\nCommands: LOGIN##user, SEND##to##sub##body, QUIT");
                System.out.print("> ");
                String input = console.nextLine();
                out.println(input);

                if (input.equalsIgnoreCase("QUIT")) break;
                System.out.println("Response: " + in.readLine());
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}