package interfaces;

import model.Email;

import java.util.List;
import java.util.Optional;

public interface EmailManager {
    boolean storeEmail(Email email);

    List<Email> getReceivedEmails(String recipient);

    List<Email> searchReceivedEmails(String recipient, String query);

    List<Email> getSentEmails(String sender);

    List<Email> searchSentEmails(String sender, String query);

    Optional<Email> getEmailById(String username, int emailId);

    default List<Email> getEmailsByRecipient(String recipient) {
        return getReceivedEmails(recipient);
    }

    default List<Email> searchEmails(String recipient, String query) {
        return searchReceivedEmails(recipient, query);
    }

    default List<Email> getEmailsForUser(String receiver) {
        return getReceivedEmails(receiver);
    }
}
