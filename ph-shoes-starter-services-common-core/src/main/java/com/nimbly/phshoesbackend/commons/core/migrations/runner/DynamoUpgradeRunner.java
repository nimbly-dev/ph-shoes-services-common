package com.nimbly.phshoesbackend.commons.core.migrations.runner;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.CollectionUtils;

import com.nimbly.phshoesbackend.commons.core.migrations.UpgradeContext;
import com.nimbly.phshoesbackend.commons.core.migrations.UpgradeStep;

public class DynamoUpgradeRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DynamoUpgradeRunner.class);

    private final List<UpgradeStep> steps;
    private final UpgradeContext context;

    public DynamoUpgradeRunner(List<UpgradeStep> steps, UpgradeContext context) {
        this.steps = steps;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (CollectionUtils.isEmpty(steps)) {
            log.info("No DynamoDB upgrade steps registered.");
            return;
        }

        steps.stream()
                .sorted(Comparator.comparingInt(UpgradeStep::order))
                .forEach(step -> {
                    log.info("Executing Dynamo upgrade {} -> {} ({})", step.fromVersion(), step.toVersion(), step.description());
                    step.apply(context);
                });
    }
}

