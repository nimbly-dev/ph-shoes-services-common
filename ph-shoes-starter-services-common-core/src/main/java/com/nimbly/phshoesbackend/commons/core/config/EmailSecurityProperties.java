package com.nimbly.phshoesbackend.commons.core.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "phshoes.security.email")
public class EmailSecurityProperties {

    /**
     * Enables the shared email crypto utilities.
     */
    private boolean enabled = true;

    /**
     * Base64-encoded HMAC pepper used to hash normalized email addresses.
     */
    private String hmacPepperB64;

    /**
     * Optional list of legacy peppers used when decoding legacy tokens.
     */
    private List<String> legacyPeppersB64 = new ArrayList<>();

    /**
     * Base64-encoded AES key (256-bit recommended) used to encrypt stored emails.
     */
    private String aesKeyB64;
}

