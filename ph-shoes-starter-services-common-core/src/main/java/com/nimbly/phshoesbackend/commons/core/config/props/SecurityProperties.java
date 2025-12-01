package com.nimbly.phshoesbackend.commons.core.config.props;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "phshoes.security")
public class SecurityProperties {

    /**
     * Enables the shared security configuration when true.
     */
    private boolean enabled;

    /**
     * Paths that should always be permitted without authentication.
     */
    private List<String> permitAll = new ArrayList<>(Arrays.asList("/system/status", "/actuator/**"));

    /**
     * JWT settings used by the shared authentication filter.
     */
    private Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Jwt {

        /**
         * Shared secret for symmetric verification (HMAC).
         */
        private String secret;

        /**
         * Expected issuer claim.
         */
        private String issuer;

        /**
         * Expected audience claim.
         */
        private String audience;

        /**
         * Allowed clock skew for token verification.
         */
        private Duration clockSkew = Duration.ofSeconds(30);

        /**
         * Header name that carries the token.
         */
        private String headerName = "Authorization";

        /**
         * Optional prefix (e.g. "Bearer ") stripped from the header value.
         */
        private String headerPrefix = "Bearer ";
    }
}
