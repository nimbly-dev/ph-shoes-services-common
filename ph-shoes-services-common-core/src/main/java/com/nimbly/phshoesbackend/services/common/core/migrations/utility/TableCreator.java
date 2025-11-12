package com.nimbly.phshoesbackend.services.common.core.migrations.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Small DynamoDB schema helper used by migration steps:
 * - create tables (idempotent)
 * - add GSIs (idempotent)
 * - enable TTL
 * Public methods are easy to mock in unit tests.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TableCreator {

    private final DynamoDbClient ddb;

    /** Create a table if missing; waits until ACTIVE. Billing defaults to PROVISIONED when null. */
    public void createTableIfNotExists(
            String table,
            List<AttributeDefinition> attributes,
            List<KeySchemaElement> keySchema,
            BillingMode billingMode,           // null ⇒ PROVISIONED
            Long readCapacityUnits,            // used only for PROVISIONED
            Long writeCapacityUnits            // used only for PROVISIONED
    ) {
        if (tableExists(table)) {
            waitTableActive(table);
            log.info("[migrations] table READY: {}", table);
            return;
        }

        BillingMode bm = (billingMode != null) ? billingMode : BillingMode.PROVISIONED;
        CreateTableRequest.Builder b = CreateTableRequest.builder()
                .tableName(table)
                .attributeDefinitions(attributes)
                .keySchema(keySchema)
                .billingMode(bm);

        if (bm == BillingMode.PROVISIONED) {
            long rcu = Optional.ofNullable(readCapacityUnits).orElse(1L);
            long wcu = Optional.ofNullable(writeCapacityUnits).orElse(1L);
            b = b.provisionedThroughput(ProvisionedThroughput.builder()
                    .readCapacityUnits(rcu).writeCapacityUnits(wcu).build());
        }

        log.info("[migrations] creating table: {} (billing={})", table, bm);
        ddb.createTable(b.build());
        waitTableActive(table);
        log.info("[migrations] table ACTIVE: {}", table);
    }

    /** Create a GSI if missing; waits until ACTIVE. Uses table billing mode; throughput set only when table is PROVISIONED. */
    public void createGsiIfNotExists(
            String table,
            String indexName,
            String hashAttr,
            ScalarAttributeType hashType,
            BillingMode billingModeHint,     // may be null; if non-null must match table’s mode
            Long indexReadCapacityUnits,     // used only when PROVISIONED
            Long indexWriteCapacityUnits     // used only when PROVISIONED
    ) {
        TableDescription td = ddb.describeTable(r -> r.tableName(table)).table();

        if (td.globalSecondaryIndexes() != null &&
                td.globalSecondaryIndexes().stream().anyMatch(g -> g.indexName().equals(indexName))) {
            waitGsiActive(table, indexName);
            log.info("[migrations] GSI READY: {} on {}", indexName, table);
            return;
        }

        BillingMode tableMode = tableBillingMode(td);
        if (billingModeHint != null && billingModeHint != tableMode) {
            throw SdkClientException.create("Billing hint " + billingModeHint + " != table mode " + tableMode);
        }

        // Ensure attribute definition exists for the GSI key
        List<AttributeDefinition> attrDefs = new ArrayList<>(Optional.ofNullable(td.attributeDefinitions()).orElse(List.of()));
        Set<String> names = new HashSet<>();
        for (var ad : attrDefs) names.add(ad.attributeName());
        if (!names.contains(hashAttr)) {
            attrDefs.add(AttributeDefinition.builder().attributeName(hashAttr).attributeType(hashType).build());
        }

        CreateGlobalSecondaryIndexAction.Builder gsi = CreateGlobalSecondaryIndexAction.builder()
                .indexName(indexName)
                .keySchema(KeySchemaElement.builder().attributeName(hashAttr).keyType(KeyType.HASH).build())
                .projection(Projection.builder().projectionType(ProjectionType.ALL).build());

        if (tableMode == BillingMode.PROVISIONED) {
            long rcu = Optional.ofNullable(indexReadCapacityUnits).orElse(1L);
            long wcu = Optional.ofNullable(indexWriteCapacityUnits).orElse(1L);
            gsi = gsi.provisionedThroughput(ProvisionedThroughput.builder()
                    .readCapacityUnits(rcu).writeCapacityUnits(wcu).build());
        }

        log.info("[migrations] creating GSI {} on {} (billing={})", indexName, table, tableMode);
        ddb.updateTable(UpdateTableRequest.builder()
                .tableName(table)
                .attributeDefinitions(attrDefs)
                .globalSecondaryIndexUpdates(GlobalSecondaryIndexUpdate.builder()
                        .create(gsi.build())
                        .build())
                .build());

        waitGsiActive(table, indexName);
        log.info("[migrations] GSI ACTIVE: {} on {}", indexName, table);
    }

    /** Enable TTL if disabled for a given attribute (epoch seconds). */
    public void enableTtlIfDisabled(String table, String ttlAttr) {
        var cur = ddb.describeTimeToLive(b -> b.tableName(table)).timeToLiveDescription();
        if (cur != null && TimeToLiveStatus.ENABLED.equals(cur.timeToLiveStatus())
                && ttlAttr.equals(cur.attributeName())) {
            log.info("[migrations] TTL already enabled on {} attr={}", table, ttlAttr);
            return;
        }
        log.info("[migrations] enabling TTL on {} attr={}", table, ttlAttr);
        ddb.updateTimeToLive(UpdateTimeToLiveRequest.builder()
                .tableName(table)
                .timeToLiveSpecification(TimeToLiveSpecification.builder()
                        .attributeName(ttlAttr).enabled(true).build())
                .build());
    }

    /** Wait until table is ACTIVE. */
    public void waitTableActive(String table) {
        try (DynamoDbWaiter waiter = ddb.waiter()) {
            waiter.waitUntilTableExists(r -> r.tableName(table));
        }
        for (int i = 0; i < 20; i++) {
            if (TableStatus.ACTIVE.equals(ddb.describeTable(r -> r.tableName(table)).table().tableStatus())) return;
            sleep(500);
        }
        throw SdkClientException.create("Timed out waiting for table ACTIVE: " + table);
    }

    /** Wait until GSI is ACTIVE. */
    public void waitGsiActive(String table, String indexName) {
        for (int i = 0; i < 600; i++) {
            var t = ddb.describeTable(b -> b.tableName(table)).table();
            var gsi = Optional.ofNullable(t.globalSecondaryIndexes()).orElse(List.of())
                    .stream().filter(x -> indexName.equals(x.indexName())).findFirst();
            if (gsi.isPresent() && IndexStatus.ACTIVE.equals(gsi.get().indexStatus())) return;
            sleep(500);
        }
        throw SdkClientException.create("Timed out waiting for GSI " + indexName + " on " + table);
    }

    private boolean tableExists(String table) {
        try { ddb.describeTable(r -> r.tableName(table)); return true; }
        catch (ResourceNotFoundException rnfe) { return false; }
    }

    private BillingMode tableBillingMode(TableDescription desc) {
        BillingModeSummary summary = desc.billingModeSummary();
        if (summary != null && summary.billingMode() != null) {
            return summary.billingMode();
        }
        return BillingMode.PROVISIONED;
    }

    private void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
