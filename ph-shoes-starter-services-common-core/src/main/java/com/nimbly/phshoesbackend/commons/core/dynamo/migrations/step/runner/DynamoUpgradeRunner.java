package com.nimbly.phshoesbackend.commons.core.dynamo.migrations.step.runner;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.CollectionUtils;

import com.nimbly.phshoesbackend.commons.core.config.props.DynamoMigrationProperties;
import com.nimbly.phshoesbackend.commons.core.dynamo.migrations.step.UpgradeStep;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoUpgradeRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DynamoUpgradeRunner.class);

    private final List<UpgradeStep> steps;

    private final DynamoDbClient dynamoDbClient;

    private final DynamoMigrationProperties properties;

    public DynamoUpgradeRunner(List<UpgradeStep> steps,
                               DynamoDbClient dynamoDbClient,
                               DynamoMigrationProperties properties) {
        this.steps = steps;
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (CollectionUtils.isEmpty(steps)) {
            log.info("No DynamoDB upgrade steps registered.");
            return;
        }

        log.info("Running {} DynamoDB upgrade steps", steps.size());
        steps.stream()
                .sorted(Comparator.comparingInt(UpgradeStep::getOrder))
                .forEach(step -> runStep(step));
    }

    private void runStep(UpgradeStep step) {
        String id = step.getId();
        log.info("Executing Dynamo upgrade step {}", id);
        step.upgrade(dynamoDbClient);
        log.info("Completed Dynamo upgrade step {}", id);
    }
}

