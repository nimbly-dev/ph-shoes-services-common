package com.nimbly.phshoesbackend.services.common.core.api.status;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the state of a downstream system that the service relies on.
 */
public final class DependencyStatus {

    private final String name;
    private final ServiceState state;
    private final String message;
    private final Map<String, Object> metadata;

    private DependencyStatus(Builder builder) {
        this.name = builder.name;
        this.state = builder.state == null ? ServiceState.DOWN : builder.state;
        this.message = builder.message;
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
    }

    public String name() {
        return name;
    }

    public ServiceState state() {
        return state;
    }

    public String message() {
        return message;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    public static Builder builder(String name, ServiceState state) {
        Builder builder = new Builder();
        builder.name = Objects.requireNonNull(name, "name is required");
        builder.state = Objects.requireNonNull(state, "state is required");
        return builder;
    }

    public static final class Builder {
        private String name;
        private ServiceState state = ServiceState.UP;
        private String message;
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder state(ServiceState state) {
            this.state = state == null ? ServiceState.DOWN : state;
            return this;
        }

        public Builder metadata(String key, Object value) {
            if (key != null && value != null) {
                this.metadata.put(key, value);
            }
            return this;
        }

        public Builder metadata(Map<String, Object> extras) {
            if (extras != null && !extras.isEmpty()) {
                extras.forEach((k, v) -> {
                    if (k != null && v != null) {
                        metadata.put(k, v);
                    }
                });
            }
            return this;
        }

        public DependencyStatus build() {
            return new DependencyStatus(this);
        }
    }
}
