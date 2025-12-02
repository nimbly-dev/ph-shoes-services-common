package com.nimbly.phshoesbackend.commons.core.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.nimbly.phshoesbackend.commons.core.config.EmailSecurityProperties;
import com.nimbly.phshoesbackend.commons.core.security.EmailCrypto;

@AutoConfiguration
@ConditionalOnProperty(prefix = "phshoes.security.email", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmailCryptoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EmailCrypto emailCrypto(EmailSecurityProperties properties) {
        return new EmailCrypto(properties);
    }
}

