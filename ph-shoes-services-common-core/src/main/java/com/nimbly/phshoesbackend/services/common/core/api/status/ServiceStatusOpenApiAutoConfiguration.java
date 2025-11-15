package com.nimbly.phshoesbackend.services.common.core.api.status;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
        ServiceStatusProperties.OpenApiProperties openapi = props.getOpenapi();
        String groupName = openapi != null && StringUtils.hasText(openapi.getGroupName())
                ? openapi.getGroupName()
                : "default";

        String[] pathsToMatch = openapi != null && openapi.getPathsToMatch() != null && openapi.getPathsToMatch().length > 0
                ? openapi.getPathsToMatch()
                : new String[]{"/**"};

        // Always ensure the explicit status path is covered.
        String normalizedPath = StringUtils.hasText(path) ? path : "/system/status";
        boolean statusPathIncluded = false;
        for (String candidate : pathsToMatch) {
            if (StringUtils.hasText(candidate) && candidate.equals(normalizedPath)) {
                statusPathIncluded = true;
                break;
            }
        }
        if (!statusPathIncluded) {
            String[] merged = new String[pathsToMatch.length + 1];
            System.arraycopy(pathsToMatch, 0, merged, 0, pathsToMatch.length);
            merged[pathsToMatch.length] = normalizedPath;
            pathsToMatch = merged;
        }

        return GroupedOpenApi.builder()
                .group(groupName)
                .pathsToMatch(pathsToMatch)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "phShoesServiceStatusSecurityCustomizer")
    public OpenApiCustomiser phShoesServiceStatusSecurityCustomizer(ServiceStatusProperties props) {
        boolean removeSecurity = props.getOpenapi() == null || props.getOpenapi().isRemoveSecurity();
        if (!removeSecurity) {
            return openApi -> {
            };
        }
        return openApi -> {
            if (openApi.getPaths() == null || openApi.getPaths().isEmpty()) {
                return;
            }
            String match = StringUtils.hasText(props.getPath()) ? props.getPath() : "/system/status";
            openApi.getPaths().forEach((path, pathItem) -> {
                if (match.equals(path)) {
                    pathItem.readOperations().forEach(operation -> operation.setSecurity(null));
                }
            });
        };
    }
}
