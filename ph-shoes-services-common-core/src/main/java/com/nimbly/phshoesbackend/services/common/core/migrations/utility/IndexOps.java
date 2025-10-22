package com.nimbly.phshoesbackend.services.common.core.migrations.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class IndexOps {
    private final DynamoDbClient ddb;

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

    public void ensureGsiAll(String table, String indexName,
                             String hashAttr, ScalarAttributeType hashType) {
        var desc = ddb.describeTable(b -> b.tableName(table)).table();
        boolean exists = desc.globalSecondaryIndexes() != null &&
                desc.globalSecondaryIndexes().stream().anyMatch(g -> g.indexName().equals(indexName));
        if (exists) { log.info("[migrations] GSI exists: {} on {}", indexName, table); return; }

        // add missing attribute definition if needed
        var defined = desc.attributeDefinitions() == null ? Set.<String>of() :
                desc.attributeDefinitions().stream().map(AttributeDefinition::attributeName).collect(java.util.stream.Collectors.toSet());
        var update = UpdateTableRequest.builder()
                .tableName(table)
                .attributeDefinitions(defined.contains(hashAttr)
                        ? desc.attributeDefinitions()
                        : java.util.stream.Stream.concat(
                        desc.attributeDefinitions().stream(),
                        java.util.stream.Stream.of(AttributeDefinition.builder().attributeName(hashAttr).attributeType(hashType).build())
                ).toList())
                .globalSecondaryIndexUpdates(GlobalSecondaryIndexUpdate.builder()
                        .create(CreateGlobalSecondaryIndexAction.builder()
                                .indexName(indexName)
                                .keySchema(KeySchemaElement.builder().attributeName(hashAttr).keyType(KeyType.HASH).build())
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build())
                        .build())
                .build();
        ddb.updateTable(update);
        log.info("[migrations] requested creation of GSI {} on {}", indexName, table);
    }
}
