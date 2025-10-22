package com.nimbly.phshoesbackend.services.common.core.migrations;

import org.slf4j.Logger;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Clock;
import java.util.Objects;
import java.util.function.Supplier;


/**
 * @param tablePrefix e.g. "phshoes_"
 */
public record UpgradeContext(DynamoDbClient ddb, String tablePrefix, Logger log, Clock clock) {
    public UpgradeContext(DynamoDbClient ddb, String tablePrefix, Logger log, Clock clock) {
        this.ddb = Objects.requireNonNull(ddb);
        this.tablePrefix = tablePrefix == null ? "" : tablePrefix;
        this.log = Objects.requireNonNull(log);
        this.clock = Objects.requireNonNull(clock);
    }

    public String tbl(String logical) {
        return tablePrefix + logical;
    }

    /**
     * Simple retry helper.
     */
    public <T> T withRetry(String label, int maxAttempts, Supplier<T> body) {
        RuntimeException last = null;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                return body.get();
            } catch (RuntimeException ex) {
                last = ex;
                long sleep = Math.min(1000L << (i - 1), 8000L);
                log.warn("{} attempt {}/{} failed: {} (sleep {}ms)", label, i, maxAttempts, ex.toString(), sleep);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw last;
    }
}