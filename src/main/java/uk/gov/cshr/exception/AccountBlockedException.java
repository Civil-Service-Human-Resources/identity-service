package uk.gov.cshr.exception;

import org.springframework.security.authentication.AccountStatusException;

/**
 * Thrown if an authentication request is rejected because the account is blocked.
 * This means the username is neither whitelisted, nor part of an agency domain, nor invited via LPG identity-management.
 * Makes no assertion as to whether or not the credentials were valid.
 */
public class AccountBlockedException extends AccountStatusException {
    // ~ Constructors
    // ===================================================================================================

    /**
     * Constructs a <code>AccountBlockedException</code> with the specified message.
     *
     * @param msg the detail message.
     */
    public AccountBlockedException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <code>AccountBlockedException</code> with the specified message and root
     * cause.
     *
     * @param msg the detail message.
     * @param t   root cause
     */
    public AccountBlockedException(String msg, Throwable t) {
        super(msg, t);
    }
}