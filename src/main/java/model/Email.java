package model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import utils.Validators;

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
    @NonNull private String sender;
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
    public Email(int emailId, String sender, String recipient, String subject, String body) {
        if (emailId < 1) {
            log.error("Could not create new Email object as given email id was < 1!");
            throw new IllegalArgumentException("Email was < 1, must be > 0!");
        }
        this.emailId = emailId;

        this.sender = Validators.validateStringData(sender);
        Validators.validateEmail(this.sender);

        this.recipient = Validators.validateStringData(recipient);
        Validators.validateEmail(this.recipient);

        this.subject = Validators.validateStringData(subject);

        this.body = Validators.validateStringData(body);
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