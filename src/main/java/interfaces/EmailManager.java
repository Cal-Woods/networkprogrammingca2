package interfaces;

import model.Email;

import java.util.List;

public interface EmailManager {
    boolean storeEmail(Email email);
    List<Email> getEmailsByRecipient(String recipient);
}
