package com.nimbly.phshoesbackend.commons.web.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.nimbly.phshoesbackend.commons.core.ratelimit.ApiRateLimitException;
import com.nimbly.phshoesbackend.commons.core.ratelimit.RateLimitRequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbly.phshoesbackend.commons.core.ratelimit.ApiRateLimiter;

public class ApiRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiRateLimitInterceptor.class);

    private final ApiRateLimiter apiRateLimiter;

    public ApiRateLimitInterceptor(ApiRateLimiter apiRateLimiter) {
        this.apiRateLimiter = apiRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RateLimitRequestContext context = RateLimitRequestContext.builder()
                .path(request.getRequestURI())
                .ipAddress(resolveClientIp(request))
                .userId(resolveUserId(request))
                .build();
        try {
            apiRateLimiter.verifyRequest(context);
            return true;
        } catch (ApiRateLimitException ex) {
            log.warn("Request blocked by rate limiter: {}", ex.getMessage());
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
            return false;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(header)) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveUserId(HttpServletRequest request) {
        if (request.getUserPrincipal() != null && StringUtils.hasText(request.getUserPrincipal().getName())) {
            return request.getUserPrincipal().getName();
        }
        String header = request.getHeader("X-User-Id");
        if (StringUtils.hasText(header)) {
            return header;
        }
        return null;
    }
}
