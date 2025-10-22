package com.nimbly.phshoesbackend.services.common.core.migrations;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class MigrationVersionDao {
    public static final String DEFAULT_TABLE = "migration_versions";
    private final DynamoDbClient ddb;
    private final String table;
    public MigrationVersionDao(DynamoDbClient ddb, String tablePrefix) {
        this.ddb = ddb;
        this.table = (tablePrefix == null ? "" : tablePrefix) + DEFAULT_TABLE;
    }
    public Optional<String> getCurrentVersion(String service) {
        var out = ddb.getItem(b -> b.tableName(table).key(Map.of("service", AttributeValue.fromS(service))).consistentRead(true));
        if (out.item()==null || out.item().isEmpty()) return Optional.empty();
        var v = out.item().get("currentVersion");
        return v == null ? Optional.empty() : Optional.ofNullable(v.s());
    }
    public void ensureRowIfMissing(String service, String initial) {
        ddb.putItem(b -> b.tableName(table)
                .item(Map.of("service", AttributeValue.fromS(service),
                        "currentVersion", AttributeValue.fromS(initial),
                        "updatedAt", AttributeValue.fromS(Instant.EPOCH.toString())))
                .conditionExpression("attribute_not_exists(service)"));
    }
    public void advance(String service, String from, String to) {
        ddb.updateItem(b -> b.tableName(table)
                .key(Map.of("service", AttributeValue.fromS(service)))
                .updateExpression("SET currentVersion=:to, updatedAt=:now")
                .conditionExpression("currentVersion=:from")
                .expressionAttributeValues(Map.of(
                        ":from", AttributeValue.fromS(from),
                        ":to",   AttributeValue.fromS(to),
                        ":now",  AttributeValue.fromS(Instant.now().toString())
                )));
    }
}
