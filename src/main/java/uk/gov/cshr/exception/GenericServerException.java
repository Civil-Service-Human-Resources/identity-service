package uk.gov.cshr.exception;

public class GenericServerException extends RuntimeException {
    public GenericServerException(String message) {
        super(message);
    }
}
