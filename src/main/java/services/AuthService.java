package services;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;
import exceptions.InvalidEmailFormatException;
import lombok.extern.slf4j.Slf4j;
import model.RegisterModel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Authentication services for server with Argon2 hashing.
 *
 * @author Amena Zahidi
 * @Co-Author Cal Woods
 */
@Slf4j
public class AuthService {
    private final Map<String, String> credentials = new HashMap<>();
    private final String pepper;

    public AuthService() {
        this.pepper = this.getClass().getName().toUpperCase() + "-" + UUID.randomUUID().toString();
    }
    public AuthService(String pepper) {
        this.pepper = pepper + "-" + UUID.randomUUID().toString();
    }

    public boolean authenticate(String username, String password) {
        return password.equals(credentials.get(username));
    }


    public boolean register(RegisterModel registerModel) throws IllegalArgumentException, InvalidEmailFormatException {
        //Call registerModel instance validation methods
        try {
            registerModel.validateEmail(registerModel.getEmail());
            registerModel.validatePasswords(registerModel.getPassword(), registerModel.getConfirmPassword());
            registerModel.validatePhoneNumber(String.valueOf(registerModel.getPhoneNumber()));
            registerModel.validateFirstLastName(registerModel.getFirstName(), registerModel.getLastName());

            //Check user does not already exist
            if(credentials.containsKey(registerModel.getEmail())) {
                return false;
            }

            //Hash password
            Argon2Function hasher = Argon2Function.getInstance(16, 4, 2, 60, Argon2.ID);
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
            log.error("Cannot perform register action as invalid data received from RegisterModel!");
            throw new IllegalArgumentException("Invalid data received from RegisterModel!");
        }
    }
}