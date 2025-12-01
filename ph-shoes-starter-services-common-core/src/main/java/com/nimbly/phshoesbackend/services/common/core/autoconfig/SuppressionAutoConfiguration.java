package com.nimbly.phshoesbackend.services.common.core.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.nimbly.phshoesbackend.services.common.core.config.SuppressionProperties;
import com.nimbly.phshoesbackend.services.common.core.repository.SuppressionRepository;
import com.nimbly.phshoesbackend.services.common.core.repository.impl.DynamoSuppressionRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@AutoConfiguration
@ConditionalOnClass(DynamoDbClient.class)
@ConditionalOnBean(DynamoDbClient.class)
@ConditionalOnProperty(prefix = "phshoes.suppression", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SuppressionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SuppressionRepository suppressionRepository(DynamoDbClient dynamoDbClient,
                                                       SuppressionProperties properties) {
        return new DynamoSuppressionRepository(dynamoDbClient, properties);
    }
}

