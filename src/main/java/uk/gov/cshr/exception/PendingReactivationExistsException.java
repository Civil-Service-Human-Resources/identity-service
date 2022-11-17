package uk.gov.cshr.exception;

public class PendingReactivationExistsException extends RuntimeException{
    public PendingReactivationExistsException(String message){
        super(message);
    }
}
