package com.nimbly.phshoesbackend.commons.security.autoconfig;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.nimbly.phshoesbackend.commons.core.config.props.SecurityProperties;
import com.nimbly.phshoesbackend.commons.security.filter.JwtAuthenticationFilter;
import com.nimbly.phshoesbackend.commons.security.jwt.JwtTokenService;

@AutoConfiguration
@ConditionalOnClass(SecurityFilterChain.class)
public class CommonsSecurityAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "phshoes.security", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public JwtTokenService jwtTokenService(SecurityProperties properties) {
        return new JwtTokenService(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "phshoes.security", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService tokenService) {
        return new JwtAuthenticationFilter(tokenService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "phshoes.security", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain phShoesSecurityFilterChain(HttpSecurity http,
                                                          JwtAuthenticationFilter jwtFilter,
                                                          SecurityProperties properties) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(auth -> {
            List<String> permitAll = properties.getPermitAll();
            if (permitAll != null && !permitAll.isEmpty()) {
                auth.requestMatchers(permitAll.toArray(new String[0])).permitAll();
            }
            auth.anyRequest().authenticated();
        });
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }
}

