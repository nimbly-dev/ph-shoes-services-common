package com.nimbly.phshoesbackend.commons.core.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.nimbly.phshoesbackend.commons.core.config.props.ApiRateLimitProperties;
import com.nimbly.phshoesbackend.commons.core.ratelimit.ApiRateLimiter;
import com.nimbly.phshoesbackend.commons.core.ratelimit.InMemoryApiRateLimiter;

@AutoConfiguration
@ConditionalOnProperty(prefix = "phshoes.api.rate-limit", name = "enabled", havingValue = "true")
public class ApiRateLimitingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApiRateLimiter apiRateLimiter(ApiRateLimitProperties properties) {
        return new InMemoryApiRateLimiter(properties);
    }
}

