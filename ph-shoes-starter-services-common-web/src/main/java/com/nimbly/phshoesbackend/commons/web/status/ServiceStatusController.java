package com.nimbly.phshoesbackend.commons.web.status;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbly.phshoesbackend.commons.core.config.props.ServiceStatusProperties;
import com.nimbly.phshoesbackend.commons.core.status.ServiceStatus;
import com.nimbly.phshoesbackend.commons.core.status.ServiceStatusContributor;

@RestController
@ConditionalOnProperty(prefix = "phshoes.status", name = "enabled", havingValue = "true")
public class ServiceStatusController {

    private final ServiceStatusProperties properties;

    private final ObjectProvider<ServiceStatusContributor> contributors;

    private final Instant startedAt = Instant.now();

    public ServiceStatusController(ServiceStatusProperties properties,
                                   ObjectProvider<ServiceStatusContributor> contributors) {
        this.properties = properties;
        this.contributors = contributors;
    }

    @GetMapping("${phshoes.status.path:/system/status}")
    public ServiceStatus getStatus() {
        Instant now = Instant.now();
        ServiceStatus.ServiceStatusBuilder builder = ServiceStatus.builder()
                .serviceId(properties.getServiceId())
                .displayName(properties.getDisplayName())
                .state(properties.getState())
                .checkedAt(now)
                .uptimeSeconds(Duration.between(startedAt, now).getSeconds())
                .environment(properties.getEnvironment())
                .version(properties.getVersion())
                .description(properties.getDescription());

        Map<String, String> metadata = properties.getMetadata();
        if (metadata != null) {
            metadata.forEach(builder::metadataEntry);
        }

        contributors.orderedStream().forEach(contributor -> contributor.contribute(builder));
        return builder.build();
    }
}

