package com.nimbly.phshoesbackend.services.common.core.api.status;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceStatusOpenApiAutoConfigurationTest {

    @Test
    void createsGroupedOpenApiUsingProperties() {
        ServiceStatusProperties props = new ServiceStatusProperties();
        props.setPath("/custom/status");
        props.getOpenapi().setGroupName("custom-status");
        props.getOpenapi().setPathsToMatch(new String[]{"/api/**"});

        GroupedOpenApi api = new ServiceStatusOpenApiAutoConfiguration()
                .phShoesServiceStatusGroupedOpenApi(props);

        assertThat(api.getGroup()).isEqualTo("custom-status");
        assertThat(api.getPathsToMatch()).contains("/api/**", "/custom/status");
    }

    @Test
    void securityCustomizerClearsStatusSecurity() {
        ServiceStatusProperties props = new ServiceStatusProperties();
        props.setPath("/system/status");

        Operation op = new Operation();
        op.setSecurity(Collections.emptyList());

        PathItem item = new PathItem();
        item.setGet(op);

        OpenAPI openAPI = new OpenAPI();
        openAPI.paths(new Paths().addPathItem("/system/status", item));

        new ServiceStatusOpenApiAutoConfiguration()
                .phShoesServiceStatusSecurityCustomizer(props)
                .customise(openAPI);

        assertThat(openAPI.getPaths().get("/system/status").getGet().getSecurity()).isNull();
    }

    @Test
    void securityCustomizerCanBeDisabled() {
        ServiceStatusProperties props = new ServiceStatusProperties();
        props.setPath("/system/status");
        props.getOpenapi().setRemoveSecurity(false);

        Operation op = new Operation();
        op.setSecurity(Collections.emptyList());

        PathItem item = new PathItem();
        item.setGet(op);

        OpenAPI openAPI = new OpenAPI();
        openAPI.paths(new Paths().addPathItem("/system/status", item));

        new ServiceStatusOpenApiAutoConfiguration()
                .phShoesServiceStatusSecurityCustomizer(props)
                .customise(openAPI);

        assertThat(openAPI.getPaths().get("/system/status").getGet().getSecurity()).isNotNull();
    }
}
