package com.nimbly.phshoesbackend.services.common.core.migrations.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TableCreator {
    private final DynamoDbClient ddb;

    /** Create table if missing; wait ACTIVE; idempotent. */
    public void ensureTable(String table,
                            List<AttributeDefinition> attrs,
                            List<KeySchemaElement> key,
                            List<GlobalSecondaryIndex> gsis) {
        if (tableExists(table)) { log.info("[migrations] table exists: {}", table); return; }

        log.info("[migrations] creating table: {}", table);
        var req = CreateTableRequest.builder()
                .tableName(table)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(attrs)
                .keySchema(key)
                .globalSecondaryIndexes(gsis == null ? List.of() : gsis)
                .build();
        ddb.createTable(req);
        try (DynamoDbWaiter w = ddb.waiter()) {
            w.waitUntilTableExists(b -> b.tableName(table));
        }
        log.info("[migrations] ACTIVE: {}", table);
    }

    public boolean tableExists(String table) {
        try { ddb.describeTable(b -> b.tableName(table)); return true; }
        catch (ResourceNotFoundException rnfe) { return false; }
    }
}
