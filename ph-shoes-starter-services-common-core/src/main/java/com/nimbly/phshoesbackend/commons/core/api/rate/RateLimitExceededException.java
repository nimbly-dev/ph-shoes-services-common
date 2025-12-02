package com.nimbly.phshoesbackend.commons.core.api.rate;

import com.nimbly.phshoesbackend.commons.core.ratelimit.ApiRateLimitException;

public class RateLimitExceededException extends ApiRateLimitException {

    private final String scope;

    public RateLimitExceededException(String scope, String message) {
        super(message);
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }
}
