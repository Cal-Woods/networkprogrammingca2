package interfaces;

import model.Email;

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
     * @param email {@link Email} to send
     * @return True if email sent
     */
    boolean saveEmail(Email email);

    /**
     * Receives email from client.
     * @return {@link Email} object with obtained data from network
     */
    Email receiveEmail();


    void sendEmailsForRecipient(String recipient);
}