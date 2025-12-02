package com.nimbly.phshoesbackend.services.common.core.security.jwt;

public class JwtVerificationException extends RuntimeException {
    public JwtVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

