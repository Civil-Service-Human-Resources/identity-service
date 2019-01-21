package uk.gov.cshr.exception;

public class NotificationException extends RuntimeException {
    public NotificationException(Throwable e) {
        super(e);
    }

    public NotificationException(String message) {
        super(message);
    }
}
