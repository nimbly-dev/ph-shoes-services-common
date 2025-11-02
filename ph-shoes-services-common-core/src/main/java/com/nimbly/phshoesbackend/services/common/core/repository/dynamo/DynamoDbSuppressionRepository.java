package com.nimbly.phshoesbackend.services.common.core.repository.dynamo;

import com.nimbly.phshoesbackend.services.common.core.model.SuppressionEntry;
import com.nimbly.phshoesbackend.services.common.core.model.SuppressionReason;
import com.nimbly.phshoesbackend.services.common.core.model.dynamo.SuppressionAttrs;
import com.nimbly.phshoesbackend.services.common.core.repository.SuppressionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DynamoDbSuppressionRepository implements SuppressionRepository {

    private final DynamoDbClient dynamo;

    private static Map<String, AttributeValue> key(String emailHash) {
        return Map.of(SuppressionAttrs.PK_EMAIL_HASH, AttributeValue.fromS(emailHash));
    }

    @Override
    public boolean isSuppressed(String emailHash) {
        var response = dynamo.getItem(GetItemRequest.builder()
                .tableName(SuppressionAttrs.TABLE)
                .key(key(emailHash))
                .consistentRead(true)
                .build());

        var item = response.item();
        if (item == null || item.isEmpty()) return false;

        // If no TTL → permanent suppression
        var ttl = item.get(SuppressionAttrs.EXPIRES_AT);
        if (ttl == null || ttl.n() == null || ttl.n().isBlank()) return true;

        long now = Instant.now().getEpochSecond();
        long expires = Long.parseLong(ttl.n());
        return expires > now;
    }

    @Override
    public void put(SuppressionEntry entry) {
        var item = new LinkedHashMap<String, AttributeValue>(8);

        item.put(SuppressionAttrs.PK_EMAIL_HASH, AttributeValue.fromS(entry.getEmailHash()));

        if (entry.getExpiresAt() != null)
            item.put(SuppressionAttrs.EXPIRES_AT, AttributeValue.fromN(Long.toString(entry.getExpiresAt())));

        if (entry.getCreatedAt() != null)
            item.put(SuppressionAttrs.CREATED_AT, AttributeValue.fromS(entry.getCreatedAt().toString()));

        if (entry.getReason() != null)
            item.put(SuppressionAttrs.REASON, AttributeValue.fromS(entry.getReason().name()));

        if (entry.getSource() != null && !entry.getSource().isBlank())
            item.put(SuppressionAttrs.SOURCE, AttributeValue.fromS(entry.getSource()));

        if (entry.getNotes() != null && !entry.getNotes().isBlank())
            item.put(SuppressionAttrs.NOTES, AttributeValue.fromS(entry.getNotes()));

        dynamo.putItem(PutItemRequest.builder()
                .tableName(SuppressionAttrs.TABLE)
                .item(item)
                .build());
    }

    @Override
    public void remove(String emailHash) {
        dynamo.deleteItem(DeleteItemRequest.builder()
                .tableName(SuppressionAttrs.TABLE)
                .key(key(emailHash))
                .build());
    }

    @Override
    public SuppressionEntry get(String emailHash) {
        var response = dynamo.getItem(GetItemRequest.builder()
                .tableName(SuppressionAttrs.TABLE)
                .key(key(emailHash))
                .consistentRead(true)
                .build());

        var item = response.item();
        if (item == null || item.isEmpty()) return null;

        var entry = new SuppressionEntry();
        entry.setEmailHash(emailHash);

        var ttl = item.get(SuppressionAttrs.EXPIRES_AT);
        if (ttl != null && ttl.n() != null && !ttl.n().isBlank())
            entry.setExpiresAt(Long.parseLong(ttl.n()));

        var created = item.get(SuppressionAttrs.CREATED_AT);
        if (created != null && created.s() != null && !created.s().isBlank()) {
            try { entry.setCreatedAt(Instant.parse(created.s())); } catch (Exception ignore) {}
        }

        var reason = item.get(SuppressionAttrs.REASON);
        if (reason != null && reason.s() != null && !reason.s().isBlank()) {
            try { entry.setReason(SuppressionReason.valueOf(reason.s())); } catch (Exception ignore) {}
        }

        var source = item.get(SuppressionAttrs.SOURCE);
        if (source != null && source.s() != null && !source.s().isBlank())
            entry.setSource(source.s());

        var notes = item.get(SuppressionAttrs.NOTES);
        if (notes != null && notes.s() != null && !notes.s().isBlank())
            entry.setNotes(notes.s());

        return entry;
    }
}