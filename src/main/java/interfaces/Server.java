package interfaces;

public interface Server {
    void startServer();
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
