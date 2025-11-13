package com.nimbly.phshoesbackend.services.common.core.api.status;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(RestController.class)
@ConditionalOnProperty(prefix = "phshoes.status", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ServiceStatusProperties.class)
public class ServiceStatusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ServiceStatusReporter.class)
    public ServiceStatusReporter serviceStatusReporter(ServiceStatusProperties properties,
                                                       ObjectProvider<ServiceStatusContributor> contributors,
                                                       Environment environment,
                                                       ObjectProvider<Clock> clockProvider) {
        Clock clock = clockProvider.getIfAvailable(Clock::systemUTC);
        List<ServiceStatusContributor> ordered = contributors.orderedStream().toList();
        return new DefaultServiceStatusReporter(properties, ordered, environment, clock);
    }

    @Bean
    @ConditionalOnBean(ServiceStatusReporter.class)
    @ConditionalOnMissingBean
    public ServiceStatusController serviceStatusController(ServiceStatusReporter reporter) {
        return new ServiceStatusController(reporter);
    }
}
