package com.nimbly.phshoesbackend.commons.core.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "phshoes.api")
public class ApiProperties {

    /**
     * Base REST path shared across controllers, defaults to /api/v1.
     */
    private String basePath = "/api/v1";
}

