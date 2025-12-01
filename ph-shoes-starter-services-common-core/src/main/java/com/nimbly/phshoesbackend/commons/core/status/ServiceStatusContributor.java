package com.nimbly.phshoesbackend.commons.core.status;

@FunctionalInterface
public interface ServiceStatusContributor {

    void contribute(ServiceStatus.ServiceStatusBuilder builder);
}

