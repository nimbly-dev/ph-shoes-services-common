package com.nimbly.phshoesbackend.services.common.core.api.rate;

import com.nimbly.phshoesbackend.commons.core.ratelimit.ApiRateLimitException;

public class RateLimitExceededException extends ApiRateLimitException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}

