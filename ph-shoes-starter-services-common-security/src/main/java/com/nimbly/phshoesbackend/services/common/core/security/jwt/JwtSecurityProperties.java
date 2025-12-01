package com.nimbly.phshoesbackend.services.common.core.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "phshoes.security.jwt")
public class JwtSecurityProperties {

    private boolean enabled = true;
    private String issuer = "ph-shoes";
    private String audience;
    private String secret;
    private long accessTtlSeconds = 3600;
    private String headerName = "Authorization";
    private String headerPrefix = "Bearer ";
}

