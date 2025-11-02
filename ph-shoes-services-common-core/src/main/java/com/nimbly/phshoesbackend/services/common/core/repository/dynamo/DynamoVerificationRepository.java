package com.nimbly.phshoesbackend.services.common.core.repository.dynamo;

import com.nimbly.phshoesbackend.services.common.core.model.VerificationEntry;
import com.nimbly.phshoesbackend.services.common.core.model.VerificationStatus;
import com.nimbly.phshoesbackend.services.common.core.model.dynamo.VerificationAttrs;
import com.nimbly.phshoesbackend.services.common.core.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DynamoVerificationRepository implements VerificationRepository {

    private final DynamoDbEnhancedClient enhanced;

    private DynamoDbTable<VerificationEntry> table() {
        return enhanced.table(VerificationAttrs.TABLE, TableSchema.fromBean(VerificationEntry.class));
    }

    @Override
    public void put(VerificationEntry e) {
        table().putItem(e);
    }

    @Override
    public Optional<VerificationEntry> getById(String verificationId, boolean consistentRead) {
        Key key = Key.builder().partitionValue(verificationId).build();
        GetItemEnhancedRequest req = GetItemEnhancedRequest.builder()
                .key(key)
                .consistentRead(consistentRead)
                .build();
        VerificationEntry out = table().getItem(req);
        return Optional.ofNullable(out);
    }

    @Override
    public void markUsedIfPendingAndNotExpired(String verificationId, long nowEpochSeconds) {
        VerificationEntry partial = new VerificationEntry();
        partial.setVerificationId(verificationId);
        partial.setStatus(VerificationStatus.VERIFIED);
        partial.setVerifiedAt(Instant.ofEpochSecond(nowEpochSeconds).toString());

        Expression cond = Expression.builder()
                .expression("#s = :pending AND #exp > :nowNum")
                .putExpressionName("#s", VerificationAttrs.STATUS)
                .putExpressionName("#exp", VerificationAttrs.EXPIRES_AT)
                .putExpressionValue(":pending", AttributeValue.builder().s(VerificationStatus.PENDING.name()).build())
                .putExpressionValue(":nowNum",  AttributeValue.builder().n(Long.toString(nowEpochSeconds)).build())
                .build();

        table().updateItem(UpdateItemEnhancedRequest.builder(VerificationEntry.class)
                .item(partial)
                .ignoreNulls(true)
                .conditionExpression(cond)
                .build());
    }

    @Override
    public void markStatusIfPending(String verificationId, VerificationStatus newStatus) {
        VerificationEntry partial = new VerificationEntry();
        partial.setVerificationId(verificationId);
        partial.setStatus(newStatus);

        Expression cond = Expression.builder()
                .expression("#s = :pending")
                .putExpressionName("#s", VerificationAttrs.STATUS)
                .putExpressionValue(":pending", AttributeValue.builder().s(VerificationStatus.PENDING.name()).build())
                .build();

        table().updateItem(UpdateItemEnhancedRequest.builder(VerificationEntry.class)
                .item(partial)
                .ignoreNulls(true)
                .conditionExpression(cond)
                .build());
    }
}
