package com.nimbly.phshoesbackend.commons.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "phshoes.suppression")
public class SuppressionProperties {

    /**
     * Enables the shared suppression repository and migrations.
     */
    private boolean enabled = true;

    /**
     * DynamoDB table name that stores suppression entries.
     */
    private String tableName = "email_suppressions";

    /**
     * Attribute name used as the partition key / email hash.
     */
    private String emailHashAttribute = "email_hash";

    /**
     * Attribute storing the suppression reason (enum name).
     */
    private String reasonAttribute = "reason";

    /**
     * Attribute storing the originating system/source.
     */
    private String sourceAttribute = "source";

    /**
     * Attribute storing arbitrary notes about the suppression entry.
     */
    private String notesAttribute = "notes";

    /**
     * Attribute storing creation timestamp (epoch seconds).
     */
    private String createdAtAttribute = "created_at";

    /**
     * Attribute storing last update timestamp (epoch seconds).
     */
    private String updatedAtAttribute = "updated_at";

    /**
     * Attribute storing TTL/expiry timestamp (epoch seconds).
     */
    private String expiresAtAttribute = "expires_at";
}
