package com.nimbly.phshoesbackend.services.common.core.api.status;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "phshoes.status")
public class ServiceStatusProperties {

    /** Toggle for the /system/status controller. */
    private boolean enabled = true;

    /** Request path for the controller (will be prefixed by the service context-path). */
    private String path = "/system/status";

    /** Internal identifier for the service (defaults to spring.application.name). */
    private String serviceId;

    /** Friendly name rendered in the UI (defaults to serviceId). */
    private String displayName;

    /** Deployment environment label (e.g. local, staging, prod). */
    private String environment;

    /** Optional semantic version or git SHA. */
    private String version;

    /** Optional short description of the service responsibilities. */
    private String description;

    /** Optional region/edge identifier if the service runs in multiple regions. */
    private String region;

    /** Additional free-form metadata bubbled up to the response. */
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /** Optional OpenAPI exposure customisation. */
    private OpenApiProperties openapi = new OpenApiProperties();

    @Data
    public static class OpenApiProperties {
        private boolean enabled = true;
        private String groupName = "default";
        private String[] pathsToMatch = new String[]{"/**"};
        private boolean removeSecurity = true;
    }
}
