package com.nimbly.phshoesbackend.services.common.core.api.rate;

import com.nimbly.phshoesbackend.services.common.core.api.props.ApiRateLimitProperties;
import com.nimbly.phshoesbackend.services.common.core.api.rate.identity.RequestIdentityResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Simple MVC interceptor that delegates to {@link ApiRateLimiter} for every request.
 */
public class ApiRateLimitInterceptor implements HandlerInterceptor {

    private final ApiRateLimiter limiter;
    private final ApiRateLimitProperties properties;
    private final RequestIdentityResolver identityResolver;

    public ApiRateLimitInterceptor(ApiRateLimiter limiter,
                                   ApiRateLimitProperties properties,
                                   RequestIdentityResolver identityResolver) {
        this.limiter = limiter;
        this.properties = properties;
        this.identityResolver = identityResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.isEnabled()) {
            return true;
        }
        HttpMethod method = resolveMethod(request.getMethod());
        ApiRateLimitRequest context = new ApiRateLimitRequest(
                request.getRequestURI(),
                method,
                identityResolver.resolveClientIp(request),
                identityResolver.resolveUserId(request),
                1
        );
        limiter.assertWithinLimit(context);
        return true;
    }

    private static HttpMethod resolveMethod(String methodName) {
        if (methodName == null) {
            return null;
        }
        try {
            return HttpMethod.valueOf(methodName);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
