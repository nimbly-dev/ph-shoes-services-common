package com.nimbly.phshoesbackend.commons.core.status;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ServiceStatus {

    String serviceId;

    String displayName;

    ServiceState state;

    Instant checkedAt;

    long uptimeSeconds;

    String environment;

    String version;

    String description;

    @Singular("metadataEntry")
    Map<String, String> metadata;

    @Singular("dependency")
    Map<String, ServiceDependencyStatus> dependencies;

    public static ServiceStatusBuilder builderFrom(ServiceStatus template) {
        if (template == null) {
            return builder();
        }
        ServiceStatusBuilder builder = builder()
                .serviceId(template.getServiceId())
                .displayName(template.getDisplayName())
                .state(template.getState())
                .checkedAt(template.getCheckedAt())
                .uptimeSeconds(template.getUptimeSeconds())
                .environment(template.getEnvironment())
                .version(template.getVersion())
                .description(template.getDescription());

        if (template.getMetadata() != null) {
            template.getMetadata().forEach(builder::metadataEntry);
        }
        if (template.getDependencies() != null) {
            template.getDependencies().forEach(builder::dependency);
        }
        return builder;
    }

    public Map<String, String> getMetadata() {
        return metadata == null ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
    }

    public Map<String, ServiceDependencyStatus> getDependencies() {
        return dependencies == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(dependencies));
    }
}
