package com.nimbly.phshoesbackend.services.common.core.api.status;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultServiceStatusReporterTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void snapshotUsesPropertiesAndContributors() {
        ServiceStatusProperties props = new ServiceStatusProperties();
        props.setServiceId("accounts");
        props.setDisplayName("User Accounts");
        props.setEnvironment("local");
        props.setVersion("1.2.3");
        props.getMetadata().put("foo", "bar");

        ServiceStatusContributor contributor = builder -> builder
                .dependency("dynamodb", ServiceState.UP, "ok")
                .metadata("contributed", true);

        DefaultServiceStatusReporter reporter = new DefaultServiceStatusReporter(
                props,
                List.of(contributor),
                new MockEnvironment(),
                FIXED_CLOCK
        );

        ServiceStatusResponse snapshot = reporter.snapshot();

        assertThat(snapshot.serviceId()).isEqualTo("accounts");
        assertThat(snapshot.displayName()).isEqualTo("User Accounts");
        assertThat(snapshot.environment()).isEqualTo("local");
        assertThat(snapshot.version()).isEqualTo("1.2.3");
        assertThat(snapshot.metadata()).containsEntry("foo", "bar").containsEntry("contributed", true);
        assertThat(snapshot.dependencies()).hasSize(1);
        assertThat(snapshot.state()).isEqualTo(ServiceState.UP);
    }

    @Test
    void snapshotFallsBackToApplicationName() {
        ServiceStatusProperties props = new ServiceStatusProperties();
        MockEnvironment env = new MockEnvironment().withProperty("spring.application.name", "user-accounts-service");

        DefaultServiceStatusReporter reporter = new DefaultServiceStatusReporter(
                props,
                List.of(),
                env,
                FIXED_CLOCK
        );

        ServiceStatusResponse snapshot = reporter.snapshot();

        assertThat(snapshot.serviceId()).isEqualTo("user-accounts-service");
        assertThat(snapshot.displayName()).isEqualTo("user-accounts-service");
    }
}
