package com.nimbly.phshoesbackend.services.common.core.api.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Wire response returned to clients asking for the service status.
 */
public record ServiceStatusResponse(
        String serviceId,
        String displayName,
        String environment,
        String version,
        String description,
        String region,
        ServiceState state,
        Instant checkedAt,
        long uptimeSeconds,
        Map<String, Object> metadata,
        List<DependencyStatus> dependencies) {
}
