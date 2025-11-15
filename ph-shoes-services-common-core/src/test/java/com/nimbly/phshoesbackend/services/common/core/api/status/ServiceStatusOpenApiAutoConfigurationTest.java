package com.nimbly.phshoesbackend.services.common.core.api.status;

import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

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
}
