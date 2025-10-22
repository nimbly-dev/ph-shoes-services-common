package com.nimbly.phshoesbackend.services.common.core.migrations.config;

import com.nimbly.phshoesbackend.services.common.core.migrations.MigrationRunner;
import com.nimbly.phshoesbackend.services.common.core.migrations.MigrationVersionDao;
import com.nimbly.phshoesbackend.services.common.core.migrations.UpgradeContext;
import com.nimbly.phshoesbackend.services.common.core.migrations.UpgradeStep;
import com.nimbly.phshoesbackend.services.common.core.migrations.props.MigrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(MigrationProperties.class)
public class MigrationAutoConfiguration {

    @Bean
    public MigrationVersionDao migrationVersionDao(DynamoDbClient ddb, MigrationProperties props) {
        return new MigrationVersionDao(ddb, props.getTablePrefix());
    }

    @Bean
    public UpgradeContext upgradeContext(DynamoDbClient ddb, MigrationProperties props) {
        return new UpgradeContext(ddb, props.getTablePrefix(),
                LoggerFactory.getLogger("SchemaMigrations"), Clock.systemUTC());
    }

    @Bean
    public MigrationRunner migrationRunner(MigrationVersionDao dao, MigrationProperties props) {
        return new MigrationRunner(dao, LoggerFactory.getLogger("SchemaMigrations"), props.isRevertOnError());
    }

    /**
     * Simple flow:
     * 1) Ensure <serviceName> exists in migration_versions (if bootstrap enabled).
     * 2) Collect all UpgradeStep beans, filter to serviceName, sort by fromVersion, and run.
     */
    @Bean
    public ApplicationRunner runMigrations(MigrationRunner runner,
                                           UpgradeContext ctx,
                                           MigrationProperties props,
                                           ObjectProvider<List<UpgradeStep>> allStepsOpt,
                                           MigrationVersionDao dao) {
        return args -> {
            if (!props.isEnabled()) return;

            final String service = props.getServiceName();

            // 1) Bootstrap versions row if missing (simple and explicit).
            if (props.isBootstrapIfMissing()) {
                dao.getCurrentVersion(service).orElseGet(() -> {
                    log.info("Bootstrapping service '{}' at {}", service, props.getInitial());
                    dao.ensureRowIfMissing(service, props.getInitial());
                    return props.getInitial();
                });
            }

            // 2) Gather and run steps for this service.
            List<UpgradeStep> all = allStepsOpt.getIfAvailable(List::of);
            List<UpgradeStep> stepsForService = all.stream()
                    .filter(s -> service.equals(s.service()))
                    .sorted(Comparator.comparing(UpgradeStep::fromVersion))
                    .collect(Collectors.toList());

            runner.run(service, stepsForService, ctx, props.getInitial(), /*bootstrapIfMissing*/ false);
        };
    }
}
