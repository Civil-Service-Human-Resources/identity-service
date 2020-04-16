package uk.gov.cshr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)  // 409
public class NotEnoughSpaceAvailableException extends RuntimeException {
    public NotEnoughSpaceAvailableException(String s) {
    }
}