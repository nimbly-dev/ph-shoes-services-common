package com.nimbly.phshoesbackend.services.common.core.migrations.utility;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Component
public class IndexOps {
    public void addGsiAll(DynamoDbClient ddb, String table, String indexName,
                          String hashAttr, ScalarAttributeType hashType,
                          long rcu, long wcu, Logger log) {
        ddb.updateTable(b -> b.tableName(table)
                .attributeDefinitions(a -> a.attributeName(hashAttr).attributeType(hashType))
                .globalSecondaryIndexUpdates(u -> u.create(c -> c
                        .indexName(indexName)
                        .keySchema(k -> k.attributeName(hashAttr).keyType(KeyType.HASH))
                        .projection(p -> p.projectionType(ProjectionType.ALL))
                        .provisionedThroughput(t -> t.readCapacityUnits(rcu).writeCapacityUnits(wcu)))));
        log.info("Requested creation of GSI {} on {}", indexName, table);
    }
}
