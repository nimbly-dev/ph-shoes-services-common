package com.nimbly.phshoesbackend.services.common.core.api.status;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@AutoConfiguration
@ConditionalOnClass(GroupedOpenApi.class)
@ConditionalOnProperty(prefix = "phshoes.status.openapi", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceStatusOpenApiAutoConfiguration {

    @Bean(name = "phShoesServiceStatusGroupedOpenApi")
    @ConditionalOnMissingBean(name = "phShoesServiceStatusGroupedOpenApi")
    public GroupedOpenApi phShoesServiceStatusGroupedOpenApi(ServiceStatusProperties props) {
        String path = props.getPath();
        String normalizedPath = StringUtils.hasText(path) ? path : "/system/status";
        ServiceStatusProperties.OpenApiProperties openapi = props.getOpenapi();
        String groupName = openapi != null && StringUtils.hasText(openapi.getGroupName())
                ? openapi.getGroupName()
                : "service-status";

        return GroupedOpenApi.builder()
                .group(groupName)
                .pathsToMatch(normalizedPath)
                .build();
    }
}
