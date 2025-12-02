package com.nimbly.phshoesbackend.commons.core.migrations.utility;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateGlobalSecondaryIndexAction;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DescribeTimeToLiveRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexUpdate;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveStatus;
import software.amazon.awssdk.services.dynamodb.model.UpdateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateTimeToLiveRequest;

/**
 * Helper for creating DynamoDB tables/indexes in a migration-safe way.
 */
public class TableCreator {

    private static final Logger log = LoggerFactory.getLogger(TableCreator.class);

    private final DynamoDbClient dynamoDbClient;

    public TableCreator(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void createTableIfNotExists(String tableName,
                                       List<AttributeDefinition> attributeDefinitions,
                                       List<KeySchemaElement> keySchema,
                                       BillingMode billingMode,
                                       long readCapacityUnits,
                                       long writeCapacityUnits) {
        if (tableExists(tableName)) {
            return;
        }

        CreateTableRequest.Builder builder = CreateTableRequest.builder()
                .tableName(tableName)
                .attributeDefinitions(attributeDefinitions)
                .keySchema(keySchema);
        if (billingMode == BillingMode.PAY_PER_REQUEST) {
            builder.billingMode(BillingMode.PAY_PER_REQUEST);
        } else {
            builder.billingMode(BillingMode.PROVISIONED)
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(readCapacityUnits)
                            .writeCapacityUnits(writeCapacityUnits)
                            .build());
        }
        dynamoDbClient.createTable(builder.build());
        log.info("Created DynamoDB table {}", tableName);
    }

    public void createGsiIfNotExists(String tableName,
                                     String indexName,
                                     String attributeName,
                                     ScalarAttributeType attributeType,
                                     BillingMode billingMode,
                                     long readCapacityUnits,
                                     long writeCapacityUnits) {
        DescribeTableResponse describe = dynamoDbClient.describeTable(
                DescribeTableRequest.builder().tableName(tableName).build());
        TableDescription table = describe.table();
        if (table.globalSecondaryIndexes() != null) {
            for (GlobalSecondaryIndexDescription gsi : table.globalSecondaryIndexes()) {
                if (indexName.equals(gsi.indexName())) {
                    return;
                }
            }
        }

        CreateGlobalSecondaryIndexAction.Builder create = CreateGlobalSecondaryIndexAction.builder()
                .indexName(indexName)
                .keySchema(KeySchemaElement.builder()
                        .attributeName(attributeName)
                        .keyType(KeyType.HASH)
                        .build())
                .projection(Projection.builder().projectionType(ProjectionType.ALL).build());
        if (billingMode == BillingMode.PROVISIONED) {
            create.provisionedThroughput(ProvisionedThroughput.builder()
                    .readCapacityUnits(readCapacityUnits)
                    .writeCapacityUnits(writeCapacityUnits)
                    .build());
        }

        GlobalSecondaryIndexUpdate update = GlobalSecondaryIndexUpdate.builder()
                .create(create.build())
                .build();

        UpdateTableRequest request = UpdateTableRequest.builder()
                .tableName(tableName)
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName(attributeName)
                        .attributeType(attributeType)
                        .build())
                .globalSecondaryIndexUpdates(update)
                .build();
        dynamoDbClient.updateTable(request);
        log.info("Created GSI {} on table {}", indexName, tableName);
    }

    public void enableTtlIfDisabled(String tableName, String attributeName) {
        try {
            var ttl = dynamoDbClient.describeTimeToLive(
                    DescribeTimeToLiveRequest.builder().tableName(tableName).build())
                    .timeToLiveDescription();
            if (ttl != null && ttl.timeToLiveStatus() == TimeToLiveStatus.ENABLED) {
                return;
            }
        } catch (ResourceNotFoundException ex) {
            log.warn("Table {} not found while enabling TTL", tableName);
            return;
        }

        dynamoDbClient.updateTimeToLive(UpdateTimeToLiveRequest.builder()
                .tableName(tableName)
                .timeToLiveSpecification(TimeToLiveSpecification.builder()
                        .attributeName(attributeName)
                        .enabled(true)
                        .build())
                .build());
        log.info("Enabled TTL on table {} using attribute {}", tableName, attributeName);
    }

    private boolean tableExists(String tableName) {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
            return true;
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }
}

