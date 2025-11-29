package com.nimbly.phshoesbackend.services.common.core.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "phshoes.security.jwt")
public class JwtSecurityProperties {

    /**
     * Master toggle that lets a service opt-in to the shared JWT module.
     */
    private boolean enabled = false;

    /**
     * JWT issuer claim that tokens must match. Defaults to the historical value.
     */
    private String issuer = "ph-shoes";

    /**
     * Shared HMAC secret. Dev defaults to a placeholder so local bootstrapping still works.
     */
    private String secret = "change-me-for-dev";

    /**
     * Access token TTL (seconds). Mirrors the previous AppAuthProps default (30 minutes).
     */
    private int accessTtlSeconds = 1800;
}
