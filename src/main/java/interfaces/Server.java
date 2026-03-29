package interfaces;

import model.Email;

import java.time.LocalDateTime;

public interface Server {
    /**
     * Starts server on configured port.
     */
    void startServer();

    /**
     * Stops server.
     */
    void stopServer();

    /**
     * Sends an email over network connection.
     * @return True if email sent
     */
    boolean sendEmail(String recipient, String subject, String body, LocalDateTime timestamp);

    /**
     * Receives email from client.
     */
    Email receiveEmail();
}