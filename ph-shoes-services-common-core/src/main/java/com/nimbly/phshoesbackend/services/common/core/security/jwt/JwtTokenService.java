package com.nimbly.phshoesbackend.services.common.core.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * Shared helper that issues + validates JWT access tokens using the configured {@link JwtSecurityProperties}.
 */
public class JwtTokenService {

    private final JwtSecurityProperties properties;
    private final Algorithm algorithm;

    public JwtTokenService(JwtSecurityProperties properties) {
        this.properties = properties;
        if (!StringUtils.hasText(properties.getSecret())) {
            throw new IllegalStateException("phshoes.security.jwt.secret must be configured");
        }
        this.algorithm = Algorithm.HMAC256(properties.getSecret());
    }

    public String issueAccessToken(String userId, String email) {
        return issueAccessToken(userId, email, Collections.emptyList());
    }

    public String issueAccessToken(String userId, String email, Collection<String> roles) {
        Instant now = Instant.now();
        long ttlSeconds = Math.max(properties.getAccessTtlSeconds(), 1);
        Instant expiresAt = now.plusSeconds(ttlSeconds);

        var builder = JWT.create()
                .withIssuer(properties.getIssuer())
                .withSubject(userId)
                .withClaim("email", email)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .withJWTId(UUID.randomUUID().toString());

        if (roles != null && !roles.isEmpty()) {
            builder.withArrayClaim("roles", roles.toArray(String[]::new));
        }

        return builder.sign(algorithm);
    }

    public DecodedJWT parseAccess(String token) {
        try {
            var verifier = JWT.require(algorithm);
            if (StringUtils.hasText(properties.getIssuer())) {
                verifier = verifier.withIssuer(properties.getIssuer());
            }
            return verifier.build().verify(token);
        } catch (JWTVerificationException e) {
            throw new JwtVerificationException("Invalid or expired token", e);
        }
    }

    public String userIdFromAuthorizationHeader(String header) {
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            throw new JwtVerificationException("Missing or invalid Authorization header");
        }
        String token = header.substring(7).trim();
        return parseAccess(token).getSubject();
    }

    public int getAccessTtlSeconds() {
        return properties.getAccessTtlSeconds();
    }

    public String getIssuer() {
        return properties.getIssuer();
    }

    public JwtSecurityProperties getProperties() {
        return properties;
    }

    /**
     * Lightweight runtime exception so callers can treat verification failures uniformly.
     */
    public static class JwtVerificationException extends RuntimeException {
        public JwtVerificationException(String message) {
            super(message);
        }

        public JwtVerificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
