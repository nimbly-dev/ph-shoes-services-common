package com.nimbly.phshoesbackend.services.common.core.api.status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility builder made public so services can add contributors without worrying about
 * the wire format details.
 */
public class ServiceStatusResponseBuilder {

    private final Instant checkedAt;
    private final long uptimeSeconds;
    private final List<DependencyStatus> dependencies = new ArrayList<>();
    private final Map<String, Object> metadata = new LinkedHashMap<>();
    private ServiceState state = ServiceState.UP;
    private String serviceId;
    private String displayName;
    private String environment;
    private String version;
    private String description;
    private String region;

    public ServiceStatusResponseBuilder(Instant checkedAt, long uptimeSeconds) {
        this.checkedAt = Objects.requireNonNull(checkedAt, "checkedAt is required");
        this.uptimeSeconds = Math.max(0, uptimeSeconds);
    }

    public ServiceStatusResponseBuilder serviceId(String value) {
        this.serviceId = value;
        return this;
    }

    public ServiceStatusResponseBuilder displayName(String value) {
        this.displayName = value;
        return this;
    }

    public ServiceStatusResponseBuilder environment(String environment) {
        this.environment = environment;
        return this;
    }

    public ServiceStatusResponseBuilder version(String version) {
        this.version = version;
        return this;
    }

    public ServiceStatusResponseBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ServiceStatusResponseBuilder region(String region) {
        this.region = region;
        return this;
    }

    public ServiceStatusResponseBuilder metadata(String key, Object value) {
        if (key != null && value != null) {
            metadata.put(key, value);
        }
        return this;
    }

    public ServiceStatusResponseBuilder metadata(Map<String, Object> extras) {
        if (extras != null && !extras.isEmpty()) {
            extras.forEach(this::metadata);
        }
        return this;
    }

    public ServiceStatusResponseBuilder dependency(DependencyStatus dependency) {
        if (dependency != null) {
            dependencies.add(dependency);
            state(dependency.state());
        }
        return this;
    }

    public ServiceStatusResponseBuilder dependency(String name, ServiceState state, String message) {
        return dependency(DependencyStatus.builder(name, state).message(message).build());
    }

    /**
     * Allows contributors to mark the service as degraded/down explicitly.
     */
    public ServiceStatusResponseBuilder state(ServiceState candidate) {
        if (candidate == null) return this;
        if (candidate.ordinal() > state.ordinal()) {
            state = candidate;
        }
        return this;
    }

    public ServiceStatusResponse build() {
        ServiceStatusResponse response = new ServiceStatusResponse(
                serviceId,
                displayName,
                environment,
                version,
                description,
                region,
                state,
                checkedAt,
                uptimeSeconds,
                Collections.unmodifiableMap(new LinkedHashMap<>(metadata)),
                Collections.unmodifiableList(new ArrayList<>(dependencies))
        );
        return response;
    }
}
