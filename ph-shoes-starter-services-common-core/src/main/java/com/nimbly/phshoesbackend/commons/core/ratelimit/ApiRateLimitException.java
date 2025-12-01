package com.nimbly.phshoesbackend.commons.core.ratelimit;

public class ApiRateLimitException extends RuntimeException {

    public ApiRateLimitException(String message) {
        super(message);
    }
}

