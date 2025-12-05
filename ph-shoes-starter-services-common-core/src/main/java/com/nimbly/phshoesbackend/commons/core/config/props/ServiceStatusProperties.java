package com.nimbly.phshoesbackend.commons.core.config.props;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.nimbly.phshoesbackend.commons.core.status.ServiceState;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "phshoes.status")
public class ServiceStatusProperties {

    /**
     * Enable the shared /system/status endpoint.
     */
    private boolean enabled;

    /**
     * Path for the status endpoint.
     */
    private String path = "/system/status";

    private String serviceId;

    private String displayName;

    private String environment;

    private String version;

    private String description;

    /**
     * Default state reported by the endpoint (contributors can override).
     */
    private ServiceState state = ServiceState.UP;

    /**
     * Arbitrary key/value metadata for this service.
     */
    private Map<String, String> metadata = new LinkedHashMap<>();
}

