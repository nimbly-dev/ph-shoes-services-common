package com.nimbly.phshoesbackend.services.common.core.model.dynamo;

public final class SuppressionAttrs {
    private SuppressionAttrs() {}

    public static final String TABLE       = "email_suppressions";
    public static final String PK_EMAIL_HASH = "email";      // HMAC(email) as the PK
    public static final String REASON      = "reason";
    public static final String SOURCE      = "source";
    public static final String NOTES       = "notes";        // use NOTES (not DETAILS)
    public static final String CREATED_AT  = "created_at";   // snake_case kept
    public static final String EXPIRES_AT  = "expires_at";   // epoch seconds (TTL)
}