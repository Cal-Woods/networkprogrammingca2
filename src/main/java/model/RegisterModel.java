package model;

import exceptions.InvalidEmailFormatException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import utils.Validators;

/**
 * Model for holding registration details.
 *
 * @author Cal Woods
 */
@Slf4j
@Getter
@Setter
public class RegisterModel {
    private String email;
    private String password;
    private String confirmPassword;
    private Long phoneNumber;
    private String firstName;
    private String lastName;

    /**
     * All arguments constructor for {@link RegisterModel}
     * @param password Given password
     * @param confirmPassword Given confirmation password
     * @param email Given email
     * @param phoneNumber Given phone number(Can be null)
     * @param firstName Given first name
     * @param lastName Given last name
     */
    public RegisterModel(String password, String confirmPassword, String email, String phoneNumber, String firstName, String lastName) {
        validatePasswords(password, confirmPassword);
        validateEmail(email);
        validatePhoneNumber(phoneNumber);
        validateFirstLastName(firstName, lastName);
    }

    /**
     * No arguments constructor for setting values post-registration using setters.
     */
    public RegisterModel() {}

    /**
     * Validate and sets incoming email.
     * @throws InvalidEmailFormatException If email is in an invalid format {something@provider.domain, johndoe@gmail.com}
     */
    public void validateEmail(String email) throws InvalidEmailFormatException {
        if (email == null) {
            log.error("Invalid username was null in RegisterModel!");
            throw new IllegalArgumentException("Invalid username was null!");
        }
        if(email.isBlank()) {
            log.error("Invalid email was empty or contained only whitespace in RegisterModel!");
            throw new IllegalArgumentException("Invalid email was empty or contained only whitespace!");
        }
        if (email.length() < 3) {
            log.error("Invalid username too short in RegisterModel, must be at least three characters!");
            throw new  IllegalArgumentException("Invalid username was too short at " + email.length() + " characters, must be at least 3 characters!");
        }

        try {
            Validators.validateEmail(email);
        }
        catch(InvalidEmailFormatException e) {
            log.error("Invalid email was provided in RegisterModel, {} not in valid format!", email);
            throw new InvalidEmailFormatException("Invalid email was provided in RegisterModel, " + email);
        }

        this.email = email;
    }

    /**
     * Validates given password is valid and matches given confirmation password.
     * @param password Given password
     * @param confirmPassword Given confirmation password
     */
    public void validatePasswords(String password, String confirmPassword) {
        if (password == null) {
            log.error("Invalid password was null in RegisterModel!");
            throw new IllegalArgumentException("Invalid password was null!");
        }
        if(password.isBlank()) {
            log.error("Invalid password was empty or contained only whitespace in RegisterModel!");
            throw new IllegalArgumentException("Invalid password was empty or contained only whitespace!");
        }
        if(password.length() < 8) {
            log.error("Invalid password in RegisterModel, must be at least 8 characters!");
            throw new IllegalArgumentException("Invalid password was too short at " + password.length() + " characters, must be at least 8 characters!");
        }
        if(confirmPassword == null) {
            log.error("Invalid confirm password was null in RegisterModel!");
            throw new IllegalArgumentException("Invalid confirmation password was null!");
        }
        if(confirmPassword.isBlank()) {
            log.error("Invalid confirmPassword in RegisterModel was empty or contained only whitespace!");
            throw new IllegalArgumentException("Invalid confirmation password was empty or contained only whitespace!");
        }
        if(confirmPassword.length() < 8) {
            log.error("Invalid confirmPassword in RegisterModel, was less than at least 8 characters!");
            throw new IllegalArgumentException("Invalid confirmation password, must be at least 8 characters!");
        }

        if(!confirmPassword.equals(password)) {
            log.error("Invalid password was provided in RegisterModel, does not match confirmation password!");
            throw new IllegalArgumentException("Invalid password was provided, does not match confirmation password!");
        }

        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    /**
     * Validates phone number is all digits and sets instance phone number to null
     * if given phone number is null.
     * @param number Given phone number
     */
    public void validatePhoneNumber(String number) {
        if (number == null) {
            this.phoneNumber = null;
            return;
        }
        if(number.isBlank()) {
            log.error("Invalid phone number in RegisterModel was empty or contained only whitespace!");
            throw new IllegalArgumentException("Invalid phone number was empty or contained only whitespace!");
        }
        if (number.length() < 12) {
            log.error("Invalid phone number in RegisterModel, was less than 12 digit!");
            throw new IllegalArgumentException("Invalid phone number was less than 12 digits, must be at least 12!, " + number);
        }
        if(!number.matches("^\\d+$")) {
            log.error("Invalid phone number was not all numerical digits, " + number);
            throw new IllegalArgumentException("Invalid phone number was not all numerical digits, " + number);
        }

        this.phoneNumber = Long.parseLong(number);
    }

    /**
     * Validates and sets first and last names.
     * @param firstName First name
     * @param lastName Last name
     */
    public void validateFirstLastName(String firstName,  String lastName) {
        if (firstName == null || firstName.isBlank()) {
            log.error("Invalid first name in RegisterModel was null or blank!");
            throw new IllegalArgumentException("Invalid first name was empty or contained only whitespace!");
        }
        if(firstName.length() < 3) {
            log.error("Invalid first name in RegisterModel, was less than three characters!");
            throw new IllegalArgumentException("Invalid first name, must be at least three characters!");
        }
        if(lastName == null || lastName.isBlank()) {
            log.error("Invalid last name in RegisterModel, was null or blank!");
            throw new IllegalArgumentException("Invalid last name was empty or contained only whitespace!");
        }
        if(lastName.length() < 3) {
            log.error("Invalid last name in RegisterModel, was less than 3 characters!");
            throw new IllegalArgumentException("Invalid last name, must be at least 3 characters!");
        }

        this.firstName = firstName;
        this.lastName = lastName;
    }
}
