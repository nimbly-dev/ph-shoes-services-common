package com.nimbly.phshoesbackend.services.common.core.migrations.steps;

import com.nimbly.phshoesbackend.services.common.core.migrations.UpgradeContext;
import com.nimbly.phshoesbackend.services.common.core.migrations.UpgradeStep;

public final class DummyStep implements UpgradeStep {
    private final String service;
    private final String from;
    private final String to;
    private final String description;

    public DummyStep(String service, String from, String to) {
        this(service, from, to, "No-op migration");
    }

    public DummyStep(String service, String from, String to, String description) {
        this.service = service;
        this.from = from;
        this.to = to;
        this.description = (description == null || description.isBlank()) ? "No-op migration" : description;
    }

    @Override public String service()     { return service; }
    @Override public String fromVersion() { return from; }
    @Override public String toVersion()   { return to; }
    @Override public String description() { return description; }

    @Override public void apply(UpgradeContext ctx) { /* intentionally no-op */ }
}