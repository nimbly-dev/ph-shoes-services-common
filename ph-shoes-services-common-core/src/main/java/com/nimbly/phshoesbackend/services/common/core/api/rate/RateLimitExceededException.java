package com.nimbly.phshoesbackend.services.common.core.api.rate;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised whenever a caller exceeds one of the configured API limits.
 */
@Getter
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    private final String scope;
    private final int limit;
    private final int attemptedTotal;
    private final int requested;

    public RateLimitExceededException(String scope, int limit, int attemptedTotal, int requested) {
        super(buildMessage(scope, limit, attemptedTotal, requested));
        this.scope = scope;
        this.limit = limit;
        this.attemptedTotal = attemptedTotal;
        this.requested = requested;
    }

    private static String buildMessage(String scope, int limit, int attemptedTotal, int requested) {
        return "API rate limit exceeded for %s: limit=%d, attempted=%d (+%d)".formatted(
                scope, limit, attemptedTotal, requested);
    }
}
