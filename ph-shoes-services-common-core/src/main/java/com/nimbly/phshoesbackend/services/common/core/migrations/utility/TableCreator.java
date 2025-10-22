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
    public void ensureTable(String tableName,
                            List<AttributeDefinition> attrs,
                            List<KeySchemaElement> keySchema) {
        if (exists(tableName)) {
            log.info("[migrations] table exists: {}", tableName);
            return;
        }
        log.info("[migrations] creating table: {}", tableName);
        ddb.createTable(CreateTableRequest.builder()
                .tableName(tableName)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(attrs)
                .keySchema(keySchema)
                .build());
        try (DynamoDbWaiter waiter = ddb.waiter()) {
            waiter.waitUntilTableExists(DescribeTableRequest.builder().tableName(tableName).build());
        }
        log.info("[migrations] ACTIVE: {}", tableName);
    }

    private boolean exists(String tableName) {
        try {
            ddb.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
            return true;
        } catch (ResourceNotFoundException rnfe) {
            return false;
        }
    }
}
