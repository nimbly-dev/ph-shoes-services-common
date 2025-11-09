package com.nimbly.phshoesbackend.services.common.core.api.config;

import com.nimbly.phshoesbackend.services.common.core.api.props.ApiRateLimitProperties;
import com.nimbly.phshoesbackend.services.common.core.api.rate.ApiRateLimitInterceptor;
import com.nimbly.phshoesbackend.services.common.core.api.rate.ApiRateLimiter;
import com.nimbly.phshoesbackend.services.common.core.api.rate.DefaultApiRateLimiter;
import com.nimbly.phshoesbackend.services.common.core.api.rate.identity.RequestIdentityResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@AutoConfiguration
@EnableConfigurationProperties(ApiRateLimitProperties.class)
@ConditionalOnClass({HandlerInterceptor.class, WebMvcConfigurer.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "phshoes.api.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApiRateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RequestIdentityResolver requestIdentityResolver() {
        return new RequestIdentityResolver();
    }

    @Bean
    @ConditionalOnMissingBean(ApiRateLimiter.class)
    public ApiRateLimiter apiRateLimiter(ApiRateLimitProperties props, ObjectProvider<Clock> clockProvider) {
        Clock clock = clockProvider.getIfAvailable(Clock::systemUTC);
        return new DefaultApiRateLimiter(props, clock);
    }

    @Bean("phShoesApiRateLimitInterceptor")
    @ConditionalOnMissingBean(name = "phShoesApiRateLimitInterceptor")
    public HandlerInterceptor phShoesApiRateLimitInterceptor(ApiRateLimiter limiter,
                                                             ApiRateLimitProperties props,
                                                             RequestIdentityResolver identityResolver) {
        return new ApiRateLimitInterceptor(limiter, props, identityResolver);
    }

    @Bean
    public WebMvcConfigurer phShoesApiRateLimitWebMvcConfigurer(HandlerInterceptor phShoesApiRateLimitInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(phShoesApiRateLimitInterceptor);
            }
        };
    }
}
