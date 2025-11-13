package com.nimbly.phshoesbackend.services.common.core.api.status;

/**
 * Services can provide beans implementing this interface to enrich the status payload
 * with downstream health checks or additional metadata.
 */
@FunctionalInterface
public interface ServiceStatusContributor {

    void contribute(ServiceStatusResponseBuilder builder);
}
