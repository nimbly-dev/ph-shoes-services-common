package com.nimbly.phshoesbackend.services.common.core.api.rate;

import org.springframework.http.HttpMethod;

/**
 * Context passed to the {@link ApiRateLimiter} so limits can be enforced consistently.
 */
public record ApiRateLimitRequest(String path,
                                  HttpMethod method,
                                  String clientIp,
                                  String userId,
                                  int permits) {

    public ApiRateLimitRequest {
        path = (path == null || path.isBlank()) ? "/" : path;
        method = (method == null) ? HttpMethod.GET : method;
        permits = permits <= 0 ? 1 : permits;
    }
}
