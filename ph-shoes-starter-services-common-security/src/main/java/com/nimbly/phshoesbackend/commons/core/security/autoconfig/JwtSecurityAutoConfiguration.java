package com.nimbly.phshoesbackend.commons.core.security.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.nimbly.phshoesbackend.commons.core.security.jwt.JwtAuthenticationFilter;
import com.nimbly.phshoesbackend.commons.core.security.jwt.JwtSecurityProperties;
import com.nimbly.phshoesbackend.commons.core.security.jwt.JwtTokenService;

@AutoConfiguration
@EnableConfigurationProperties(JwtSecurityProperties.class)
public class JwtSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "phshoes.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtTokenService jwtTokenService(JwtSecurityProperties properties) {
        return new JwtTokenService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "phshoes.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                                           JwtSecurityProperties properties) {
        return new JwtAuthenticationFilter(jwtTokenService, properties);
    }
}
