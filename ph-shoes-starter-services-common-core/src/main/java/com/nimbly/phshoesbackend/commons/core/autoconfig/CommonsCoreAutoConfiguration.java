package com.nimbly.phshoesbackend.commons.core.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.nimbly.phshoesbackend.commons.core.config.props.ApiProperties;
import com.nimbly.phshoesbackend.commons.core.config.props.ApiRateLimitProperties;
import com.nimbly.phshoesbackend.commons.core.config.props.DynamoMigrationProperties;
import com.nimbly.phshoesbackend.commons.core.config.props.ServiceStatusProperties;
import com.nimbly.phshoesbackend.services.common.core.config.EmailSecurityProperties;
import com.nimbly.phshoesbackend.services.common.core.config.SuppressionProperties;

@AutoConfiguration
@EnableConfigurationProperties({
        ApiProperties.class,
        ApiRateLimitProperties.class,
        ServiceStatusProperties.class,
        DynamoMigrationProperties.class,
        EmailSecurityProperties.class,
        SuppressionProperties.class
})
public class CommonsCoreAutoConfiguration {
}
