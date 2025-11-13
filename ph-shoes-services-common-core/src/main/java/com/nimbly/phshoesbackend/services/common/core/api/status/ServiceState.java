package com.nimbly.phshoesbackend.services.common.core.api.status;

/**
 * Shared status levels so contributors can downgrade a service consistently.
 */
public enum ServiceState {
    UP,
    DEGRADED,
    DOWN
}
