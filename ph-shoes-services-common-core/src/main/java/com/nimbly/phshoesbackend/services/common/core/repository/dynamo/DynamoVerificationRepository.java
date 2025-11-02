package com.nimbly.phshoesbackend.services.common.core.repository.dynamo;

import com.nimbly.phshoesbackend.services.common.core.model.VerificationEntry;
import com.nimbly.phshoesbackend.services.common.core.model.VerificationStatus;
import com.nimbly.phshoesbackend.services.common.core.model.dynamo.AccountAttrs;
import com.nimbly.phshoesbackend.services.common.core.model.dynamo.VerificationAttrs;
import com.nimbly.phshoesbackend.services.common.core.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DynamoVerificationRepository implements VerificationRepository {

    private final DynamoDbClient ddb;

    @Override
    public void put(VerificationEntry e) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(VerificationAttrs.PK_VERIFICATION_ID, AttributeValue.builder().s(e.getVerificationId()).build());
        item.put(VerificationAttrs.USER_ID,            AttributeValue.builder().s(e.getUserId()).build());
        item.put(VerificationAttrs.EMAIL_HASH,         AttributeValue.builder().s(e.getEmailHash()).build());
        if (e.getEmailPlain() != null) {
            item.put(VerificationAttrs.EMAIL_PLAIN,    AttributeValue.builder().s(e.getEmailPlain()).build());
        }
        if (e.getCodeHash() != null) {
            item.put(VerificationAttrs.CODE_HASH,      AttributeValue.builder().s(e.getCodeHash()).build());
        }
        // WRITE enum as its name()
        item.put(VerificationAttrs.STATUS,             AttributeValue.builder().s(
                e.getStatus() == null ? VerificationStatus.UNKNOWN.name() : e.getStatus().name()
        ).build());
        item.put(VerificationAttrs.EXPIRES_AT,         AttributeValue.builder().n(Long.toString(e.getExpiresAt())).build());
        item.put(VerificationAttrs.CREATED_AT,         AttributeValue.builder().s(e.getCreatedAt()).build());
        if (e.getVerifiedAt() != null) {
            item.put(VerificationAttrs.VERIFIED_AT,    AttributeValue.builder().s(e.getVerifiedAt()).build());
        }

        ddb.putItem(PutItemRequest.builder()
                .tableName(VerificationAttrs.TABLE)
                .item(item)
                .build());
    }

    @Override
    public Optional<VerificationEntry> getById(String verificationId, boolean consistentRead) {
        var res = ddb.getItem(GetItemRequest.builder()
                .tableName(VerificationAttrs.TABLE)
                .key(Map.of(VerificationAttrs.PK_VERIFICATION_ID, AttributeValue.builder().s(verificationId).build()))
                .consistentRead(consistentRead)
                .build());

        if (!res.hasItem()) return Optional.empty();
        var m = res.item();

        VerificationEntry e = new VerificationEntry();
        e.setVerificationId(m.get(VerificationAttrs.PK_VERIFICATION_ID).s());
        e.setUserId(m.get(VerificationAttrs.USER_ID).s());
        e.setEmailHash(m.get(VerificationAttrs.EMAIL_HASH).s());
        e.setEmailPlain(m.containsKey(VerificationAttrs.EMAIL_PLAIN) ? m.get(VerificationAttrs.EMAIL_PLAIN).s() : null);
        // READ enum safely
        var statusStr = m.get(VerificationAttrs.STATUS).s();
        VerificationStatus status;
        try {
            status = VerificationStatus.valueOf(statusStr);
        } catch (Exception ignore) {
            status = VerificationStatus.UNKNOWN;
        }
        e.setStatus(status);
        e.setCodeHash(m.containsKey(VerificationAttrs.CODE_HASH) ? m.get(VerificationAttrs.CODE_HASH).s() : null);
        e.setExpiresAt(Long.parseLong(m.get(VerificationAttrs.EXPIRES_AT).n()));
        e.setCreatedAt(m.get(VerificationAttrs.CREATED_AT).s());
        e.setVerifiedAt(m.containsKey(VerificationAttrs.VERIFIED_AT) ? m.get(VerificationAttrs.VERIFIED_AT).s() : null);

        return Optional.of(e);
    }

    @Override
    public void markUsedIfPendingAndNotExpired(String verificationId, long nowEpochSeconds) {
        // Flip PENDING -> VERIFIED and stamp VERIFIED_AT if not expired
        Map<String, String> names = Map.of(
                "#s",   VerificationAttrs.STATUS,
                "#va",  VerificationAttrs.VERIFIED_AT,
                "#exp", VerificationAttrs.EXPIRES_AT
        );

        Map<String, AttributeValue> values = Map.of(
                ":pending", AttributeValue.builder().s(VerificationStatus.PENDING.name()).build(),
                ":verified", AttributeValue.builder().s(VerificationStatus.VERIFIED.name()).build(),
                ":now",     AttributeValue.builder().s(Instant.ofEpochSecond(nowEpochSeconds).toString()).build(),
                ":nowNum",  AttributeValue.builder().n(Long.toString(nowEpochSeconds)).build()
        );

        ddb.updateItem(UpdateItemRequest.builder()
                .tableName(VerificationAttrs.TABLE)
                .key(Map.of(VerificationAttrs.PK_VERIFICATION_ID, AttributeValue.builder().s(verificationId).build()))
                .conditionExpression("#s = :pending AND #exp > :nowNum")
                .updateExpression("SET #s = :verified, #va = :now")
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .build());
    }
}
