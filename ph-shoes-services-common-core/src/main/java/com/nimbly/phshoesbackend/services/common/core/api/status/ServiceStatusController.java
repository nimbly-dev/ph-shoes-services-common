package com.nimbly.phshoesbackend.services.common.core.api.status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${phshoes.status.path:/system/status}")
@ConditionalOnBean(ServiceStatusReporter.class)
@ConditionalOnProperty(prefix = "phshoes.status", name = "enabled", havingValue = "true", matchIfMissing = true)
@Tag(name = "status", description = "Lightweight service status/health endpoint")
public class ServiceStatusController {

    private final ServiceStatusReporter reporter;

    public ServiceStatusController(ServiceStatusReporter reporter) {
        this.reporter = reporter;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns the current service status payload")
    public ServiceStatusResponse serviceStatus() {
        return reporter.snapshot();
    }
}
