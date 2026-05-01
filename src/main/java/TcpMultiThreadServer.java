import concurrency.ServerThread;
import interfaces.EmailManager;
import services.InMemoryEmailManager; // You need this concrete class
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

@Slf4j
public class TcpMultiThreadServer {
    static int SERVER_PORT = 50000;
    static String separator = "##";
    static LinkedList<Thread> threads = new LinkedList();
    static int currentRunningThreads = 0;
    // Initialize your storage (Step 1 implementation)
    static EmailManager emailManager = new InMemoryEmailManager();

    public static void main(String[] args) {
        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(SERVER_PORT)) {
            log.info("Server is listening on port " + SERVER_PORT);

            while (true) {
                java.net.Socket clientSocket = serverSocket.accept();
                log.info("New client connected at {}:{}!",  clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

                ServerThread worker = new ServerThread(clientSocket, emailManager, separator);
                threads.add(new Thread(worker));
                threads.get(currentRunningThreads++).start();
            }
        } catch (java.io.IOException e) {
            log.error("Server error: " + e.getMessage());
        }
    }
}