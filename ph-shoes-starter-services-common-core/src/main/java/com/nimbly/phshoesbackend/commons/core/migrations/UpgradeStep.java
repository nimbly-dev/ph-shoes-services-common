package com.nimbly.phshoesbackend.commons.core.migrations;

/**
 * Contract for describing and applying schema upgrade steps against DynamoDB.
 * Implementations should be idempotent.
 */
public interface UpgradeStep {

    String service();

    String fromVersion();

    String toVersion();

    String description();

    /**
     * Steps may override to control ordering; defaults to natural order.
     */
    default int order() {
        return 0;
    }

    void apply(UpgradeContext context);
}

