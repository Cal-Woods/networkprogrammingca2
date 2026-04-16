package utils;

import exceptions.InvalidEmailFormatException;

/**
 * Contains global validator methods.
 */
public class Validators {
    /**
     * Global validate email method that checks given email address for an email pattern.
     * @param email Given email address
     * @throws InvalidEmailFormatException If email address fails validation
     */
    public static void validateEmail(String email) {
        String input = validateStringData(email);

        if(!input.matches("^.+@.+[.][a-zA-Z]{2,}")) {
            throw new InvalidEmailFormatException("Given email data is an invalid address, check sender and recipient!");
        }
    }

    public static String validateStringData(String inputData) {
        if(inputData == null) {
            throw new InvalidEmailFormatException("Could not validate email data as given input data was null!");
        }
        if(inputData.isBlank()) {
            throw new InvalidEmailFormatException("Could not validate given input data as it was empty!");
        }

        String trimmedData = inputData.trim();

        return trimmedData;
    }
}
