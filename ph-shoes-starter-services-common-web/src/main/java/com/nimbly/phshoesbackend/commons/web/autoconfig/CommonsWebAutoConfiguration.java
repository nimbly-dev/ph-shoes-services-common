package com.nimbly.phshoesbackend.commons.web.autoconfig;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nimbly.phshoesbackend.commons.core.config.props.ServiceStatusProperties;
import com.nimbly.phshoesbackend.commons.core.ratelimit.ApiRateLimiter;
import com.nimbly.phshoesbackend.commons.core.status.ServiceStatusContributor;
import com.nimbly.phshoesbackend.commons.web.ratelimit.ApiRateLimitInterceptor;
import com.nimbly.phshoesbackend.commons.web.ratelimit.ApiRateLimitingWebMvcConfigurer;
import com.nimbly.phshoesbackend.commons.web.status.ServiceStatusController;

@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer.class)
public class CommonsWebAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "phshoes.api.rate-limit", name = "enabled", havingValue = "true")
    @ConditionalOnBean(ApiRateLimiter.class)
    @ConditionalOnMissingBean
    public ApiRateLimitInterceptor apiRateLimitInterceptor(ApiRateLimiter apiRateLimiter) {
        return new ApiRateLimitInterceptor(apiRateLimiter);
    }

    @Bean
    @ConditionalOnBean(ApiRateLimitInterceptor.class)
    @ConditionalOnProperty(prefix = "phshoes.api.rate-limit", name = "enabled", havingValue = "true")
    public WebMvcConfigurer apiRateLimitingWebMvcConfigurer(ApiRateLimitInterceptor interceptor) {
        return new ApiRateLimitingWebMvcConfigurer(interceptor);
    }

    @Bean
    @ConditionalOnProperty(prefix = "phshoes.status", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public ServiceStatusController serviceStatusController(ServiceStatusProperties properties,
                                                           ObjectProvider<ServiceStatusContributor> contributors) {
        return new ServiceStatusController(properties, contributors);
    }
}
