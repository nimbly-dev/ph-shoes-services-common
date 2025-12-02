package com.nimbly.phshoesbackend.commons.core.security.jwt;

public class JwtVerificationException extends RuntimeException {
    public JwtVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

