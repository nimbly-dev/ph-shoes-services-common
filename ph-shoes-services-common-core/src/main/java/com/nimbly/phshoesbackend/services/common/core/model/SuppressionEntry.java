package com.nimbly.phshoesbackend.services.common.core.model;

import com.nimbly.phshoesbackend.services.common.core.model.dynamo.SuppressionAttrs;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@NoArgsConstructor
@DynamoDbBean
public class SuppressionEntry {

    // one-item-per-email design
    @Getter(onMethod_ = {
            @DynamoDbPartitionKey,
            @DynamoDbAttribute(SuppressionAttrs.PK_EMAIL_HASH)
    })
    @Setter
    private String emailHash;

    @Getter(onMethod_ = {
            @DynamoDbAttribute(SuppressionAttrs.REASON)
    })
    @Setter
    private SuppressionReason reason;

    @Getter(onMethod_ = {
            @DynamoDbAttribute(SuppressionAttrs.CREATED_AT)
    })
    @Setter
    private Instant createdAt;

    // optional TTL
    @Getter(onMethod_ = {
            @DynamoDbAttribute(SuppressionAttrs.EXPIRES_AT)
    })
    @Setter
    private Long expiresAt;

    @Getter(onMethod_ = {
            @DynamoDbAttribute(SuppressionAttrs.SOURCE)
    })
    @Setter
    private String source;

    @Getter(onMethod_ = {
            @DynamoDbAttribute(SuppressionAttrs.NOTES)
    })
    @Setter
    private String notes;
}
