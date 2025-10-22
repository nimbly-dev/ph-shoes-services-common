package com.nimbly.phshoesbackend.services.common.core.migrations.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTimeToLiveRequest;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveStatus;
import software.amazon.awssdk.services.dynamodb.model.UpdateTimeToLiveRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class TtlOps {
    private final DynamoDbClient ddb;

    /** Idempotently enable TTL on a table for the given attribute (epoch seconds). */
    public void ensureTtl(String table, String ttlAttr) {
        var cur = ddb.describeTimeToLive(DescribeTimeToLiveRequest.builder().tableName(table).build());
        var status = cur.timeToLiveDescription().timeToLiveStatus();
        var curAttr = cur.timeToLiveDescription().attributeName();

        if (TimeToLiveStatus.ENABLED.equals(status) && ttlAttr.equals(curAttr)) {
            log.info("[migrations] TTL already enabled on {} attr={}", table, ttlAttr);
            return;
        }
        ddb.updateTimeToLive(UpdateTimeToLiveRequest.builder()
                .tableName(table)
                .timeToLiveSpecification(TimeToLiveSpecification.builder()
                        .attributeName(ttlAttr)
                        .enabled(true)
                        .build())
                .build());
        log.info("[migrations] TTL enable requested on {} attr={}", table, ttlAttr);
    }
}
