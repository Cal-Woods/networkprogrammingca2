import concurrency.ServerThread;
import interfaces.EmailManager;
import services.InMemoryEmailManager; // You need this concrete class
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class TcpMultiThreadServer {
    static int SERVER_PORT = 5555;
    static String separator = "##";
    // Initialize your storage (Step 1 implementation)
    static EmailManager emailManager = new InMemoryEmailManager();

    public static void main(String[] args) {
        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(SERVER_PORT)) {
            log.info("Server is listening on port " + SERVER_PORT);

            while (true) {
                java.net.Socket clientSocket = serverSocket.accept();
                log.info("New client connected!");

                ServerThread worker = new ServerThread(clientSocket, emailManager, separator);

                new Thread(worker).start();
            }
        } catch (java.io.IOException e) {
            log.error("Server error: " + e.getMessage());
        }
    }
}