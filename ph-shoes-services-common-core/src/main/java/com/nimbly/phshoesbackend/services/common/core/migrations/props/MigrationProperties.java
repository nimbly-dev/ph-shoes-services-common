package com.nimbly.phshoesbackend.services.common.core.migrations.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "schema.migrations")
@Data
public class MigrationProperties {
    /** Enable/disable migrations at startup. */
    private boolean enabled = true;

    /** If a step throws and supportsRevert() == true, call revert(). */
    private boolean revertOnError = true;

    /** Create the service row at `initial` if missing. We’ll do this automatically using serviceName. */
    private boolean bootstrapIfMissing = true;

    /** Starting version inserted when bootstrapping. */
    private String initial = "0.0.0";

    /** Logical service name for this application (used for versions row & step filtering). */
    private String serviceName = "default_service";

    /** Optional table prefix applied to all tables, including migration_versions. */
    private String tablePrefix = "";
}
