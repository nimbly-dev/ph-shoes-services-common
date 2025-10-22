package com.nimbly.phshoesbackend.services.common.core.migrations.utility;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

@Component
public class MigrationMarkers {
    public void mark(DynamoDbClient ddb, String table, Map<String,AttributeValue> key, String attr) {
        ddb.updateItem(b -> b.tableName(table)
                .key(key).updateExpression("SET #m = :one")
                .expressionAttributeNames(Map.of("#m", attr))
                .expressionAttributeValues(Map.of(":one", AttributeValue.fromN("1"))));
    }
    public void unmark(DynamoDbClient ddb, String table, Map<String,AttributeValue> key, String attr) {
        ddb.updateItem(b -> b.tableName(table)
                .key(key).updateExpression("REMOVE #m")
                .expressionAttributeNames(Map.of("#m", attr)));
    }
}
