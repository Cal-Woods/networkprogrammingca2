import concurrency.ServerThread;
import interfaces.EmailManager;
import model.Email;
import model.RegisterModel;
import services.AuthService;
import services.InMemoryEmailManager; // You need this concrete class
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TcpMultiThreadServer {
    static int SERVER_PORT = 50000;
    static String separator = "##";
    // Initialize your storage (Step 1 implementation)
    static EmailManager emailManager = new InMemoryEmailManager();
    static AuthService authService = new AuthService();
    static AtomicInteger emailIdSequence = new AtomicInteger(1);

    public static void main(String[] args) {
        if (args.length > 0) {
            SERVER_PORT = Integer.parseInt(args[0]);
        }

        bootstrapDemoData();

        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(SERVER_PORT)) {
            log.info("Server is listening on port " + SERVER_PORT);

            while (true) {
                java.net.Socket clientSocket = serverSocket.accept();
                log.info("New client connected at {}:{}!",  clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

                ServerThread worker = new ServerThread(clientSocket, emailManager, separator, authService, emailIdSequence);
                new Thread(worker).start();
            }
        }
        catch (java.io.IOException e) {
            log.error("Server error: " + e.getMessage());
        }
    }

    private static void bootstrapDemoData() {
        RegisterModel user1 = new RegisterModel();
        user1.validateFirstLastName("John", "Doe");
        user1.validateEmail("john.doe@example.com");
        user1.validatePasswords("password123", "password123");
        user1.validatePhoneNumber("123456789012");
        authService.register(user1);

        RegisterModel user2 = new RegisterModel();
        user2.validateFirstLastName("Support", "Desk");
        user2.validateEmail("support@example.com");
        user2.validatePasswords("password123", "password123");
        authService.register(user2);

        emailManager.storeEmail(new Email(
                emailIdSequence.getAndIncrement(),
                "support@example.com",
                "john.doe@example.com",
                "Welcome",
                "This is a bootstrapped message so you can see the inbox right away.",
                Instant.parse("2026-05-08T12:00:00Z")
        ));

        log.info("Bootstrapped demo users and sample mail.");
    }
}
