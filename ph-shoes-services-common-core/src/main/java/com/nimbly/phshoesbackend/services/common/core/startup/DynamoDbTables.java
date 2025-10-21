package com.nimbly.phshoesbackend.services.common.core.startup;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamoDbTables {

    private final DynamoDbClient ddb;
    private final Environment env;

    public void ensureMigrationVersionsTableDev() {
        // only in 'dev' profile
        boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");
        if (!isDev) {
            log.info("Skipping migration_versions creation (profile != dev)");
            return;
        }

        final String tableName = "migration_versions";

        if (tableExists(tableName)) {
            log.info("migration_versions already exists: {}", tableName);
            return;
        }

        try {
            log.info("Creating migration_versions (dev) as {}", tableName);
            CreateTableRequest req = CreateTableRequest.builder()
                    .tableName(tableName)
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("service").attributeType("S").build()
                    )
                    .keySchema(
                            KeySchemaElement.builder().attributeName("service").keyType(KeyType.HASH).build()
                    )
                    .build();

            ddb.createTable(req);

            // wait until ACTIVE
            try (DynamoDbWaiter waiter = ddb.waiter()) {
                waiter.waitUntilTableExists(DescribeTableRequest.builder().tableName(tableName).build());
            }
            log.info("Created and ACTIVE: {}", tableName);
        } catch (ResourceInUseException e) {
            log.info("Table already exists (race): {}", tableName);
        } catch (SdkServiceException e) {
            log.error("Failed creating {}: {}", tableName, e.getMessage(), e);
            throw e;
        }
    }

    private boolean tableExists(String tableName) {
        try {
            ddb.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
}
