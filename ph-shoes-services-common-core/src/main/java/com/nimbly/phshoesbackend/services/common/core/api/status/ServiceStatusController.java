package com.nimbly.phshoesbackend.services.common.core.api.status;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${phshoes.status.path:/system/status}")
class ServiceStatusController {

    private final ServiceStatusReporter reporter;

    ServiceStatusController(ServiceStatusReporter reporter) {
        this.reporter = reporter;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceStatusResponse serviceStatus() {
        return reporter.snapshot();
    }
}
