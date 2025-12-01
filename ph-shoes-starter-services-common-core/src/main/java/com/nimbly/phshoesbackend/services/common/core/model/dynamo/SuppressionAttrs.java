package com.nimbly.phshoesbackend.services.common.core.model.dynamo;

public final class SuppressionAttrs {

    private SuppressionAttrs() {
    }

    public static final String TABLE = "email_suppressions";
    public static final String PK_EMAIL_HASH = "email_hash";
    public static final String REASON = "reason";
    public static final String SOURCE = "source";
    public static final String NOTES = "notes";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String EXPIRES_AT = "expires_at";
}

