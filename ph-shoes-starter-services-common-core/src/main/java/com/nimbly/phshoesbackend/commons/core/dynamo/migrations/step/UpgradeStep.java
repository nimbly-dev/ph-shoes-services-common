package com.nimbly.phshoesbackend.commons.core.dynamo.migrations.step;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Base contract for DynamoDB schema upgrade steps.
 *
 * Implementations are expected to be idempotent and safe to run multiple times.
 */
public interface UpgradeStep {

    /**
     * A stable identifier for this upgrade step.
     */
    String getId();

    /**
     * Order in which this step should be executed.
     */
    int getOrder();

    /**
     * Perform the upgrade using the provided DynamoDB client.
     */
    void upgrade(DynamoDbClient dynamoDbClient);
}

