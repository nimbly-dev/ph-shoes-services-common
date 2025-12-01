package com.nimbly.phshoesbackend.commons.security.jwt;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.util.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.nimbly.phshoesbackend.commons.core.config.props.SecurityProperties;

public class JwtTokenService {

    private final JWTVerifier verifier;

    private final SecurityProperties.Jwt jwtProperties;

    public JwtTokenService(SecurityProperties properties) {
        this.jwtProperties = properties.getJwt();
        if (this.jwtProperties == null || !StringUtils.hasText(this.jwtProperties.getSecret())) {
            throw new IllegalStateException("phshoes.security.jwt.secret must be configured");
        }

        Algorithm algorithm = Algorithm.HMAC256(this.jwtProperties.getSecret());
        Verification verification = JWT.require(algorithm);

        if (StringUtils.hasText(this.jwtProperties.getIssuer())) {
            verification.withIssuer(this.jwtProperties.getIssuer());
        }

        if (StringUtils.hasText(this.jwtProperties.getAudience())) {
            String[] audiences = Arrays.stream(this.jwtProperties.getAudience().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toArray(String[]::new);
            if (audiences.length > 0) {
                verification.withAudience(audiences);
            }
        }

        Duration clockSkew = this.jwtProperties.getClockSkew();
        if (clockSkew != null && !clockSkew.isZero()) {
            verification.acceptLeeway(clockSkew.toSeconds());
        }

        this.verifier = verification.build();
    }

    public DecodedJWT verify(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

    public SecurityProperties.Jwt getProperties() {
        return jwtProperties;
    }
}

