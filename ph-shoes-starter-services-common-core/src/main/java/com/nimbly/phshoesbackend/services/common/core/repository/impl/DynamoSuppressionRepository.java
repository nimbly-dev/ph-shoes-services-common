package com.nimbly.phshoesbackend.services.common.core.repository.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.nimbly.phshoesbackend.services.common.core.config.SuppressionProperties;
import com.nimbly.phshoesbackend.services.common.core.model.SuppressionEntry;
import com.nimbly.phshoesbackend.services.common.core.repository.SuppressionRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class DynamoSuppressionRepository implements SuppressionRepository {

    private final DynamoDbClient dynamoDbClient;
    private final SuppressionProperties properties;

    public DynamoSuppressionRepository(DynamoDbClient dynamoDbClient,
                                       SuppressionProperties properties) {
        this.dynamoDbClient = Objects.requireNonNull(dynamoDbClient, "dynamoDbClient");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public void put(SuppressionEntry entry) {
        if (entry == null || entry.getEmailHash() == null || entry.getEmailHash().isBlank()) {
            return;
        }
        Map<String, AttributeValue> item = new HashMap<>();
        put(item, properties.getEmailHashAttribute(), entry.getEmailHash());
        if (entry.getReason() != null) {
            put(item, properties.getReasonAttribute(), entry.getReason().name());
        }
        put(item, properties.getSourceAttribute(), entry.getSource());
        put(item, properties.getNotesAttribute(), entry.getNotes());

        Instant createdAt = entry.getCreatedAt() != null ? entry.getCreatedAt() : Instant.now();
        put(item, properties.getCreatedAtAttribute(), createdAt.getEpochSecond());
        if (entry.getUpdatedAt() != null) {
            put(item, properties.getUpdatedAtAttribute(), entry.getUpdatedAt().getEpochSecond());
        }
        if (entry.getExpiresAt() != null) {
            item.put(properties.getExpiresAtAttribute(), AttributeValue.fromN(entry.getExpiresAt().toString()));
        }
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(properties.getTableName())
                .item(item)
                .build());
    }

    @Override
    public boolean isSuppressed(String emailHash) {
        if (emailHash == null || emailHash.isBlank()) {
            return false;
        }
        Map<String, AttributeValue> key = Map.of(properties.getEmailHashAttribute(), AttributeValue.fromS(emailHash));
        GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(properties.getTableName())
                .key(key)
                .projectionExpression(String.join(",",
                        properties.getEmailHashAttribute(),
                        properties.getExpiresAtAttribute()))
                .build());
        if (!response.hasItem() || response.item().isEmpty()) {
            return false;
        }
        AttributeValue ttl = response.item().get(properties.getExpiresAtAttribute());
        if (ttl == null || ttl.n() == null || ttl.n().isBlank()) {
            return true;
        }
        long expiresAt = Long.parseLong(ttl.n());
        return expiresAt <= 0 || expiresAt > Instant.now().getEpochSecond();
    }

    @Override
    public void remove(String emailHash) {
        if (emailHash == null || emailHash.isBlank()) {
            return;
        }
        Map<String, AttributeValue> key = Map.of(properties.getEmailHashAttribute(), AttributeValue.fromS(emailHash));
        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(properties.getTableName())
                .key(key)
                .build());
    }

    private static void put(Map<String, AttributeValue> item, String attribute, String value) {
        if (attribute == null || attribute.isBlank() || value == null || value.isBlank()) {
            return;
        }
        item.put(attribute, AttributeValue.fromS(value));
    }

    private static void put(Map<String, AttributeValue> item, String attribute, long epochSeconds) {
        if (attribute == null || attribute.isBlank()) {
            return;
        }
        item.put(attribute, AttributeValue.fromN(Long.toString(epochSeconds)));
    }
}

