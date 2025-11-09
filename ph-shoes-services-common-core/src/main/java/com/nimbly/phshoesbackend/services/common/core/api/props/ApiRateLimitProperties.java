package com.nimbly.phshoesbackend.services.common.core.api.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "phshoes.api.rate-limit")
public class ApiRateLimitProperties {

    /** Toggle to turn the whole interceptor on/off. */
    private boolean enabled = true;

    /** When true we only log limit violations but still allow the request. */
    private boolean logOnly = false;

    /** Default window applied when a limit does not specify its own duration. */
    private Duration defaultWindow = Duration.ofHours(24);

    /** Global (system-wide) limit per window. */
    private LimitProperties global = LimitProperties.enabled(2000);

    /** Limit per unique client IP. */
    private LimitProperties perIp = LimitProperties.enabled(120);

    /** Limit per authenticated user id. */
    private LimitProperties perUser = LimitProperties.enabled(60);

    /** Optional route-specific overrides. Evaluated in the configured order. */
    private List<RouteProperties> routes = new ArrayList<>();

    @Data
    public static class LimitProperties {
        private boolean enabled = true;
        private int limit = 100;
        private Duration window;

        public static LimitProperties enabled(int limit) {
            LimitProperties props = new LimitProperties();
            props.setEnabled(true);
            props.setLimit(limit);
            return props;
        }
    }

    @Data
    public static class RouteProperties {
        /** Friendly name that will be used in log/exception messages. Defaults to the pattern. */
        private String name;
        /** Ant style pattern that should match the incoming request URI. */
        private String pattern = "/**";
        /** Restrict the route to specific HTTP methods (empty = all methods). */
        private Set<HttpMethod> methods = new HashSet<>();
        /** Allow disabling a specific route without removing it. */
        private Boolean enabled;
        /** Optional shared window override for this route. */
        private Duration window;
        /** Optional overrides per scope. */
        private LimitProperties global;
        private LimitProperties perIp;
        private LimitProperties perUser;
    }
}
