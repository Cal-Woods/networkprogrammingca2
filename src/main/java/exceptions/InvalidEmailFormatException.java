package exceptions;

public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException() {
        super();
    }
    public InvalidEmailFormatException(String message) {
        super(message);
    }
}
