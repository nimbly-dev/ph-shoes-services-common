package com.nimbly.phshoesbackend.commons.core.ratelimit;

public interface ApiRateLimiter {

    /**
     * Verifies whether the incoming request is allowed to proceed. Implementations should throw
     * {@link ApiRateLimitException} when the request exceeds configured limits.
     */
    void verifyRequest(RateLimitRequestContext context);
}

