package com.nimbly.phshoesbackend.commons.core.status;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ServiceDependencyStatus {

    String name;

    ServiceState state;

    String description;
}

