package com.nimbly.phshoesbackend.commons.core.model;

import java.time.Instant;

import lombok.Data;

@Data
public class SuppressionEntry {

    private String emailHash;
    private SuppressionReason reason;
    private String source;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    /**
     * Optional epoch seconds when this entry should expire (TTL).
     */
    private Long expiresAt;
}

