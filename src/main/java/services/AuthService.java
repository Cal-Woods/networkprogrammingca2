package services;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.HashingFunction;
import com.password4j.Password;
import com.password4j.types.Argon2;
import exceptions.InvalidEmailFormatException;
import lombok.extern.slf4j.Slf4j;
import model.RegisterModel;
import utils.Validators;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication services for server with Argon2 hashing.
 *
 * @author Amena Zahidi
 * @Co-Author Cal Woods
 */
@Slf4j
public class AuthService {
    private final HashingFunction hasher = Argon2Function.getInstance(16, 4, 2, 60, Argon2.ID);
    private final Map<String, String> credentials = new ConcurrentHashMap<>();
    private final Map<String, String> tokens = new ConcurrentHashMap<>();
    private final String pepper;

    public AuthService() {
        this.pepper = this.getClass().getName().toUpperCase() + "-" + UUID.randomUUID().toString();
    }
    public AuthService(String pepper) {
        this.pepper = pepper + "-" + UUID.randomUUID().toString();
    }

    /**
     * Authenticates a user with given login credentials.
     * @param username Given username
     * @param password Given password
     * @throws InvalidEmailFormatException If given username is not a valid email address
     * @return A {@link String} token representing login session
     */
    public String authenticate(String username, String password, String ipAddress) throws InvalidEmailFormatException {
        String validatedIp = Validators.validateStringData(ipAddress);
        String validatedUsername = Validators.validateStringData(username);
        Validators.validateEmail(validatedUsername);

        if (!credentials.containsKey(validatedUsername)) {
            return null;
        }

        String validatedPassword = Validators.validateStringData(password);
        if (validatedPassword.length() < 8) {
            return null;
        }

        if (!hasher.check(validatedPassword, credentials.get(validatedUsername))) {
            return null;
        }

        String token = UUID.fromString(validatedIp).toString();
        tokens.put(token, validatedUsername);

        return token;
    }

    /**
     * Validates register data as a {@link RegisterModel} object.
     * @param registerModel Given {@link RegisterModel} object
     * @return True if registration successful
     * @throws IllegalArgumentException If given data was invalid
     * @throws InvalidEmailFormatException If given email was invalid
     *
     * @author Cal Woods
     */
    public boolean register(RegisterModel registerModel) throws IllegalArgumentException, InvalidEmailFormatException {
        //Call registerModel instance validation methods
        try {
            registerModel.validateEmail(registerModel.getEmail());
            registerModel.validatePasswords(registerModel.getPassword(), registerModel.getConfirmPassword());
            if(registerModel.getPhoneNumber() != null) {
                registerModel.validatePhoneNumber(String.valueOf(registerModel.getPhoneNumber()));
            }
            registerModel.validateFirstLastName(registerModel.getFirstName(), registerModel.getLastName());

            //Check user does not already exist
            if(credentials.containsKey(registerModel.getEmail())) {
                return false;
            }

            //Hash password
            Hash hashed = Password.hash(registerModel.getPassword()).addRandomSalt(12).addPepper(pepper).with(hasher);

            //Put new credentials
            credentials.put(registerModel.getEmail(), hashed.getResult());
            return true;
        }
        catch(IllegalArgumentException e) {
            log.error("Cannot perform register action as invalid data received from RegisterModel!");
            throw new IllegalArgumentException("Invalid data received from RegisterModel!");
        }
        catch(InvalidEmailFormatException e) {
            log.error("Cannot perform register action as invalid email received from RegisterModel!");
            throw new IllegalArgumentException("Invalid email received from RegisterModel!");
        }
    }
}