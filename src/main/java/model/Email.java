package model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
@Setter
public class Email implements Comparable<Email> {
    @EqualsAndHashCode.Include @Setter(AccessLevel.NONE) private int emailId;
    @NonNull private String recipient;
    @NonNull private String subject;
    @NonNull private String body;

    /**
     * Constructor for creating new {@link Email} object with all parameters.
     * @param emailId Given email id
     * @param recipient Given email recipient
     * @param subject Given email subject
     * @param body Given email body
     */
    public Email(int emailId, String recipient, String subject, String body) {
        if (emailId < 1) {
            log.error("Could not create new Email object as given email id was < 0!");
            throw new IllegalArgumentException("Email was < 1, must be > 0!");
        }
        this.emailId = emailId;

        validateStringData(recipient);
        this.recipient = recipient;

        validateStringData(subject);
        this.subject = subject;

        validateStringData(body);
        this.body = body;
    }

    private static void validateStringData(String data) {
        if (data == null || data.isBlank()) {
            log.error("Could not create new Email object as given String data was blank!");
            throw new IllegalArgumentException("Given String data was blank, check all String data!" + data.toString());
        }
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
}