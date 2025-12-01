package com.nimbly.phshoesbackend.commons.core.autoconfig;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.nimbly.phshoesbackend.commons.core.config.props.DynamoMigrationProperties;
import com.nimbly.phshoesbackend.commons.core.dynamo.migrations.step.UpgradeStep;
import com.nimbly.phshoesbackend.commons.core.dynamo.migrations.step.runner.DynamoUpgradeRunner;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@AutoConfiguration
@ConditionalOnClass(DynamoDbClient.class)
@ConditionalOnBean(DynamoDbClient.class)
@ConditionalOnProperty(prefix = "phshoes.dynamo.migrations", name = "enabled", havingValue = "true")
public class DynamoMigrationsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ApplicationRunner dynamoUpgradeRunner(List<UpgradeStep> steps,
                                          DynamoDbClient client,
                                          DynamoMigrationProperties properties) {
        return new DynamoUpgradeRunner(steps, client, properties);
    }
}

