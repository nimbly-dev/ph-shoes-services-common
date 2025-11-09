package com.nimbly.phshoesbackend.services.common.core.api.rate;

import com.nimbly.phshoesbackend.services.common.core.api.props.ApiRateLimitProperties;
import com.nimbly.phshoesbackend.services.common.core.api.props.ApiRateLimitProperties.LimitProperties;
import com.nimbly.phshoesbackend.services.common.core.api.props.ApiRateLimitProperties.RouteProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultApiRateLimiterTest {

    private ApiRateLimitProperties properties;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        properties = new ApiRateLimitProperties();
        properties.getGlobal().setLimit(5);
        properties.getPerIp().setLimit(3);
        properties.getPerUser().setLimit(3);
        fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    }

    @Test
    void allowsRequestsWithinDefaultLimits() {
        ApiRateLimiter limiter = new DefaultApiRateLimiter(properties, fixedClock);
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 3; i++) {
                limiter.assertWithinLimit(request("/signup", "10.0.0.1", "user-1"));
            }
        });
    }

    @Test
    void blocksWhenGlobalLimitExceeded() {
        properties.getGlobal().setLimit(2);
        ApiRateLimiter limiter = new DefaultApiRateLimiter(properties, fixedClock);
        limiter.assertWithinLimit(request("/signup", "10.0.0.1", "user-1"));
        limiter.assertWithinLimit(request("/signup", "10.0.0.2", "user-2"));
        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                () -> limiter.assertWithinLimit(request("/signup", "10.0.0.3", "user-3")));
        assertEquals("default:global", ex.getScope());
    }

    @Test
    void enforcesPerIpLimit() {
        properties.getPerIp().setLimit(1);
        ApiRateLimiter limiter = new DefaultApiRateLimiter(properties, fixedClock);
        limiter.assertWithinLimit(request("/signup", "10.0.0.1", "user-1"));
        assertThrows(RateLimitExceededException.class,
                () -> limiter.assertWithinLimit(request("/signup", "10.0.0.1", "user-2")));
        assertDoesNotThrow(() -> limiter.assertWithinLimit(request("/signup", "10.0.0.2", "user-2")));
    }

    @Test
    void routeOverridesTakePrecedence() {
        RouteProperties signup = new RouteProperties();
        signup.setName("signup");
        signup.setPattern("/signup");
        LimitProperties perUser = new LimitProperties();
        perUser.setEnabled(true);
        perUser.setLimit(1);
        signup.setPerUser(perUser);
        properties.getRoutes().add(signup);
        ApiRateLimiter limiter = new DefaultApiRateLimiter(properties, fixedClock);

        limiter.assertWithinLimit(request("/signup", "10.0.0.1", "user-override"));
        assertThrows(RateLimitExceededException.class,
                () -> limiter.assertWithinLimit(request("/signup", "10.0.0.2", "user-override")));

        // Other routes still honor the default limits
        assertDoesNotThrow(() -> limiter.assertWithinLimit(request("/health", "10.0.0.1", "user-override")));
    }

    private static ApiRateLimitRequest request(String path, String ip, String userId) {
        return new ApiRateLimitRequest(path, HttpMethod.POST, ip, userId, 1);
    }
}
