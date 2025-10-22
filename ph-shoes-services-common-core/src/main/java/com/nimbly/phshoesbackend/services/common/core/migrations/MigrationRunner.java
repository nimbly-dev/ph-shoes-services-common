package com.nimbly.phshoesbackend.services.common.core.migrations;

import org.slf4j.Logger;
import java.util.*;
import java.util.stream.Collectors;

public class MigrationRunner {
    private final MigrationVersionDao dao;
    private final Logger log;
    private final boolean revertOnError;

    public MigrationRunner(MigrationVersionDao dao, Logger log, boolean revertOnError) {
        this.dao = dao; this.log = log; this.revertOnError = revertOnError;
    }

    public void run(String service, List<UpgradeStep> stepsAnyOrder, UpgradeContext ctx,
                    String initial, boolean bootstrapIfMissing) {
        Map<String, UpgradeStep> chain = stepsAnyOrder.stream()
                .collect(Collectors.toMap(
                        UpgradeStep::fromVersion, s -> s,
                        (a,b) -> { throw new IllegalStateException("Duplicate fromVersion for "+service+": "+a.fromVersion()); },
                        LinkedHashMap::new));

        var currentOpt = dao.getCurrentVersion(service);
        if (currentOpt.isEmpty()) {
            if (!bootstrapIfMissing)
                throw new IllegalStateException("No migration_versions row for service "+service+"; enable bootstrap or seed manually.");
            dao.ensureRowIfMissing(service, initial);
        }
        String current = dao.getCurrentVersion(service).orElse(initial);

        while (true) {
            var step = chain.get(current);
            if (step == null) { log.info("Service {} is up-to-date at {}", service, current); return; }

            var pre = step.check(ctx);
            if (!pre.isOk()) throw new IllegalStateException("Pre-check failed: " + pre.getMessage());

            try {
                log.info("Applying {} {} -> {} :: {}", service, step.fromVersion(), step.toVersion(), step.description());
                step.apply(ctx);
                dao.advance(service, step.fromVersion(), step.toVersion());
                current = step.toVersion();
                log.info("✓ {} advanced to {}", service, current);
            } catch (Exception ex) {
                log.error("✗ Migration failed at {} {} -> {}: {}", service, step.fromVersion(), step.toVersion(), ex.toString(), ex);
                if (revertOnError && step.supportsRevert()) {
                    try {
                        log.warn("Attempting revert for {} {} -> {}", service, step.fromVersion(), step.toVersion());
                        step.revert(ctx);
                        log.warn("Revert completed for {} {} -> {}", service, step.fromVersion(), step.toVersion());
                    } catch (Exception rex) {
                        log.error("Revert failed: {}", rex.toString(), rex);
                    }
                }
                throw new RuntimeException("Migration aborted at " + service + "@" + step.toVersion(), ex);
            }
        }
    }
}