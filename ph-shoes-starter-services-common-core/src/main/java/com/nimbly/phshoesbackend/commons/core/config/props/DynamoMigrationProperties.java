package com.nimbly.phshoesbackend.commons.core.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "phshoes.dynamo.migrations")
public class DynamoMigrationProperties {

    /**
     * Enables DynamoDB schema migrations when true.
     */
    private boolean enabled;

    /**
     * Optional logical prefix used when resolving table names.
     */
    private String tablePrefix;
}

