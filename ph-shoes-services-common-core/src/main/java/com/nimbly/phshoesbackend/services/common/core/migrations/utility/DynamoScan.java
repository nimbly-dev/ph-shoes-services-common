package com.nimbly.phshoesbackend.services.common.core.migrations.utility;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.*;

@Component
public class DynamoScan {
    public Iterable<Map<String, AttributeValue>> scanAll(DynamoDbClient ddb, String table, String projection, int pageSize, Logger log) {
        return () -> new Iterator<>() {
            private Map<String, AttributeValue> lastKey = null;
            private Iterator<Map<String, AttributeValue>> it = Collections.emptyIterator();
            @Override public boolean hasNext() {
                while (!it.hasNext()) {
                    var req = ScanRequest.builder()
                            .tableName(table).limit(pageSize)
                            .exclusiveStartKey(lastKey)
                            .projectionExpression(projection)
                            .build();
                    var resp = ddb.scan(req);
                    lastKey = resp.lastEvaluatedKey();
                    it = resp.items().iterator();
                    if (!it.hasNext() && (lastKey == null || lastKey.isEmpty())) return false;
                    if (!it.hasNext()) continue;
                }
                return true;
            }
            @Override public Map<String, AttributeValue> next() { return it.next(); }
        };
    }
}
