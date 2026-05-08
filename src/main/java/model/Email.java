package model;

import exceptions.InvalidEmailFormatException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import utils.Validators;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Structure for holding Email data.
 *
 * @implNote Comparable is for sorting emails
 * @author Cal Woods
 */
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Getter
public class Email implements Comparable<Email> {
    @EqualsAndHashCode.Include @Setter(AccessLevel.NONE) private int emailId;
    @NonNull private String sender;
    @NonNull private String recipient;
    @NonNull private String subject;
    @NonNull private String body;
    @NonNull private Instant timestamp;

    /**
     * Constructor for creating new {@link Email} object with all parameters.
     * @param emailId Given email id
     * @param recipient Given email recipient
     * @param subject Given email subject
     * @param body Given email body
     */
    public Email(int emailId, String sender, String recipient, String subject, String body) {
        this(emailId, sender, recipient, subject, body, Instant.now());
    }

    public Email(int emailId, String sender, String recipient, String subject, String body, Instant timestamp) {
        if (emailId < 1) {
            log.error("Could not create new Email object as given email id was < 1!");
            throw new IllegalArgumentException("Email was < 1, must be > 0!");
        }
        this.emailId = emailId;

        this.sender = Validators.validateStringData(sender);
        try {
            Validators.validateEmail(this.sender);
        }
        catch (InvalidEmailFormatException e) {
            log.error("Could not create new Email object as given email address of sender was invalid! {}", sender);
            throw new InvalidEmailFormatException("Given email address of sender was not a valid email address!");
        }

        this.recipient = Validators.validateStringData(recipient);
        try {
            Validators.validateEmail(this.recipient);
        }
        catch (InvalidEmailFormatException e) {
            log.error("Could not create new Email object as given email address of recipient was invalid! {}", sender);
            throw new InvalidEmailFormatException("Given email address of recipient was not a valid email address!");
        }

        this.subject = Validators.validateStringData(subject);

        this.body = Validators.validateStringData(body);
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    @Override
    public int compareTo(Email o) {
        if (o == null) {
            log.error("Could not compare Email objects as given Email o was null!!");
            throw new IllegalArgumentException("Email o was null!");
        }

        if(this.emailId < o.emailId) {
            return -1;
        }
        else if(this.emailId > o.emailId) {
            return 1;
        }
        else {
            return 0;
        }
    }

    public String format() {
        return "ID: " + this.emailId
                + "\nSender: " + this.sender
                + "\nRecipient: " + this.recipient
                + "\nSubject: " + this.subject
                + "\nTimestamp: " + DateTimeFormatter.ISO_INSTANT.format(this.timestamp)
                + "\nBody: " + this.body;
    }

    public String toMetadataLine() {
        return "EMAIL##" + emailId
                + "##" + sender
                + "##" + recipient
                + "##" + subject
                + "##" + DateTimeFormatter.ISO_INSTANT.format(timestamp);
    }

    public String toInboxMetadataLine() {
        return "EMAIL##" + emailId
                + "##" + sender
                + "##" + subject
                + "##" + DateTimeFormatter.ISO_INSTANT.format(timestamp);
    }

    public String toSentMetadataLine() {
        return "EMAIL##" + emailId
                + "##" + recipient
                + "##" + subject
                + "##" + DateTimeFormatter.ISO_INSTANT.format(timestamp);
    }

    public String toReadLine() {
        return "READ##" + emailId
                + "##" + sender
                + "##" + recipient
                + "##" + subject
                + "##" + DateTimeFormatter.ISO_INSTANT.format(timestamp)
                + "##" + body;
    }

    public String toReadResponseLine() {
        return "200##READ##OK##" + emailId
                + "##" + sender
                + "##" + recipient
                + "##" + subject
                + "##" + DateTimeFormatter.ISO_INSTANT.format(timestamp)
                + "##" + body;
    }
}
