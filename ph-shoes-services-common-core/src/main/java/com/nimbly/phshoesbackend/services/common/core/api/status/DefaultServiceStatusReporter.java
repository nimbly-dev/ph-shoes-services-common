package com.nimbly.phshoesbackend.services.common.core.api.status;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class DefaultServiceStatusReporter implements ServiceStatusReporter {

    private final ServiceStatusProperties properties;
    private final List<ServiceStatusContributor> contributors;
    private final Environment environment;
    private final Clock clock;
    private final Instant startedAt;

    DefaultServiceStatusReporter(ServiceStatusProperties properties,
                                 List<ServiceStatusContributor> contributors,
                                 Environment environment,
                                 Clock clock) {
        this.properties = Objects.requireNonNull(properties, "properties is required");
        this.contributors = contributors == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(contributors));
        this.environment = environment;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.startedAt = Instant.now(this.clock);
    }

    @Override
    public ServiceStatusResponse snapshot() {
        Instant now = Instant.now(clock);
        long uptimeSeconds = Duration.between(startedAt, now).toSeconds();
        ServiceStatusResponseBuilder builder = new ServiceStatusResponseBuilder(now, uptimeSeconds)
                .serviceId(resolveServiceId())
                .displayName(resolveDisplayName())
                .environment(properties.getEnvironment())
                .version(resolveVersion())
                .description(properties.getDescription())
                .region(properties.getRegion())
                .metadata(properties.getMetadata());

        for (ServiceStatusContributor contributor : contributors) {
            try {
                contributor.contribute(builder);
            } catch (Exception ex) {
                builder.dependency("status-contributor:" + contributor.getClass().getSimpleName(),
                        ServiceState.DEGRADED, "Contributor threw exception: " + ex.getClass().getSimpleName());
            }
        }

        return builder.build();
    }

    private String resolveServiceId() {
        if (StringUtils.hasText(properties.getServiceId())) {
            return properties.getServiceId();
        }
        String appName = environment == null ? null : environment.getProperty("spring.application.name");
        if (StringUtils.hasText(appName)) {
            return appName;
        }
        return "ph-shoes-service";
    }

    private String resolveDisplayName() {
        if (StringUtils.hasText(properties.getDisplayName())) {
            return properties.getDisplayName();
        }
        return resolveServiceId();
    }

    private String resolveVersion() {
        if (StringUtils.hasText(properties.getVersion())) {
            return properties.getVersion();
        }
        return environment == null ? null : environment.getProperty("info.build.version");
    }
}
