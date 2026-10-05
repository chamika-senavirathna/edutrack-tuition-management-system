package com.edutrack.exception;

/**
 * Thrown when an authenticated user lacks the role/permission/ownership needed
 * for an operation. Enforced in every service so authorization is not JSP-only.
 */
public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) {
        super(message);
    }
}
