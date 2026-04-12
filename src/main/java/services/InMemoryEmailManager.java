package services;

import interfaces.EmailManager;
import model.Email;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEmailManager implements EmailManager {
    private final Map<String, List<Email>> database = new ConcurrentHashMap<>();

    @Override
    public boolean storeEmail(Email email) {
        database.computeIfAbsent(email.getRecipient(),
                k -> Collections.synchronizedList(new ArrayList<>())).add(email);
        return true;
    }

    @Override
    public List<Email> getEmailsByRecipient(String recipient) {
        return database.getOrDefault(recipient, Collections.emptyList());
    }

    @Override
    public List<Email> searchEmails(String recipient, String query) {
        String lowerQuery = query.toLowerCase();
        return getEmailsByRecipient(recipient).stream()
                .filter(e -> e.getSubject().toLowerCase().contains(lowerQuery))
                .toList();
    }
}