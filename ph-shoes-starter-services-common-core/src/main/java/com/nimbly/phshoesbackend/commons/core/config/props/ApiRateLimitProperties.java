package com.nimbly.phshoesbackend.commons.core.config.props;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "phshoes.api.rate-limit")
public class ApiRateLimitProperties {

    /**
     * Enables the API rate limiting interceptor when true.
     */
    private boolean enabled;

    /**
     * Default rolling window applied when not overridden.
     */
    private Duration defaultWindow = Duration.ofHours(24);

    private LimitConfig global = new LimitConfig();

    private LimitConfig perIp = new LimitConfig();

    private LimitConfig perUser = new LimitConfig();

    /**
     * Per-route overrides.
     */
    private List<Route> routes = new ArrayList<>();

    @Getter
    @Setter
    public static class LimitConfig {

        /**
         * Maximum number of requests allowed in the window.
         */
        private long limit;

        /**
         * Duration for this limit. When null, {@link ApiRateLimitProperties#defaultWindow} is used.
         */
        private Duration window;
    }

    @Getter
    @Setter
    public static class Route {

        /**
         * Logical name of the route, e.g. "signup".
         */
        private String name;

        /**
         * Ant-style pattern to match, e.g. /api/v1/user-accounts/verify.
         */
        private String pattern;

        /**
         * Optional per-user override for this route.
         */
        private LimitConfig perUser = new LimitConfig();
    }
}

