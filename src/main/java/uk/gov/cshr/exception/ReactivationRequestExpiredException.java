package uk.gov.cshr.exception;

public class ReactivationRequestExpiredException extends RuntimeException{
    public ReactivationRequestExpiredException(String message){
        super(message);
    }
}
