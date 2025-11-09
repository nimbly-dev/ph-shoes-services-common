package com.nimbly.phshoesbackend.services.common.core.api.rate;

/**
 * Contract used by the shared API rate limiting guardrail.
 */
public interface ApiRateLimiter {

    /**
     * Ensure the request is still within the configured limits.
     *
     * @throws RateLimitExceededException when any of the active limits would be violated
     */
    void assertWithinLimit(ApiRateLimitRequest request);
}
