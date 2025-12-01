package com.nimbly.phshoesbackend.commons.core.ratelimit;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RateLimitRequestContext {

    /**
     * Request path (e.g. /api/v1/user-accounts/verify).
     */
    String path;

    /**
     * Caller IP address, if available.
     */
    String ipAddress;

    /**
     * Authenticated user identifier, if available.
     */
    String userId;
}

