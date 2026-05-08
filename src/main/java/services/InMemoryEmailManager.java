package services;

import interfaces.EmailManager;
import model.Email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryEmailManager implements EmailManager {
    private final Map<String, List<Email>> inboxDatabase = new ConcurrentHashMap<>();
    private final Map<String, List<Email>> sentDatabase = new ConcurrentHashMap<>();

    @Override
    public boolean storeEmail(Email email) {
        inboxDatabase.computeIfAbsent(email.getRecipient(),
                k -> Collections.synchronizedList(new ArrayList<>())).add(email);
        sentDatabase.computeIfAbsent(email.getSender(),
                k -> Collections.synchronizedList(new ArrayList<>())).add(email);
        return true;
    }

    @Override
    public List<Email> getReceivedEmails(String recipient) {
        return new ArrayList<>(inboxDatabase.getOrDefault(recipient, Collections.emptyList()));
    }

    @Override
    public List<Email> searchReceivedEmails(String recipient, String query) {
        String lowerQuery = query == null ? "" : query.toLowerCase();
        return getReceivedEmails(recipient).stream()
                .filter(e -> matchesQuery(e, lowerQuery))
                .collect(Collectors.toList());
    }

    @Override
    public List<Email> getSentEmails(String sender) {
        return new ArrayList<>(sentDatabase.getOrDefault(sender, Collections.emptyList()));
    }

    @Override
    public List<Email> searchSentEmails(String sender, String query) {
        String lowerQuery = query == null ? "" : query.toLowerCase();
        return getSentEmails(sender).stream()
                .filter(e -> matchesQuery(e, lowerQuery))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Email> getEmailById(String username, int emailId) {
        return getReceivedEmails(username).stream()
                .filter(email -> email.getEmailId() == emailId)
                .findFirst()
                .or(() -> getSentEmails(username).stream()
                        .filter(email -> email.getEmailId() == emailId)
                        .findFirst());
    }

    private boolean matchesQuery(Email email, String lowerQuery) {
        if (lowerQuery.isBlank()) {
            return true;
        }

        return email.getSender().toLowerCase().contains(lowerQuery)
                || email.getRecipient().toLowerCase().contains(lowerQuery)
                || email.getSubject().toLowerCase().contains(lowerQuery)
                || email.getBody().toLowerCase().contains(lowerQuery);
    }
}
