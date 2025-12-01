package com.nimbly.phshoesbackend.commons.web.ratelimit;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class ApiRateLimitingWebMvcConfigurer implements WebMvcConfigurer {

    private final ApiRateLimitInterceptor interceptor;

    public ApiRateLimitingWebMvcConfigurer(ApiRateLimitInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}

