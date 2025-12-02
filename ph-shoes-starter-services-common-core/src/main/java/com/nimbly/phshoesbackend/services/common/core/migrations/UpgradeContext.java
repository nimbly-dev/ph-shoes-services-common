package com.nimbly.phshoesbackend.services.common.core.migrations;

import org.springframework.util.StringUtils;

import com.nimbly.phshoesbackend.commons.core.config.props.DynamoMigrationProperties;

/**
 * Context passed to schema upgrade steps. Provides helpers such as prefixing
 * table names so services can run against different environments without
 * hardcoding physical names.
 */
public class UpgradeContext {

    private final DynamoMigrationProperties properties;

    public UpgradeContext(DynamoMigrationProperties properties) {
        this.properties = properties;
    }

    /**
     * Resolves the physical table name by prepending the configured prefix if present.
     */
    public String tbl(String logicalName) {
        String prefix = properties.getTablePrefix();
        if (!StringUtils.hasText(prefix)) {
            return logicalName;
        }
        return prefix + logicalName;
    }
}

