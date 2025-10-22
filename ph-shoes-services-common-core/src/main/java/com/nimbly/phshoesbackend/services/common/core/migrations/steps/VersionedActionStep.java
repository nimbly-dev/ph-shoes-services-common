package com.nimbly.phshoesbackend.services.common.core.migrations.steps;

import com.nimbly.phshoesbackend.services.common.core.migrations.StepAction;
import com.nimbly.phshoesbackend.services.common.core.migrations.UpgradeCheck;
import com.nimbly.phshoesbackend.services.common.core.migrations.UpgradeContext;
import com.nimbly.phshoesbackend.services.common.core.migrations.UpgradeStep;

public class VersionedActionStep implements UpgradeStep {
    private final String service;
    private final String from;
    private final String to;
    private final StepAction action;

    public VersionedActionStep(String service, String from, String to, StepAction action) {
        this.service = service; this.from = from; this.to = to; this.action = action;
    }

    @Override public String service()     { return service; }
    @Override public String fromVersion() { return from; }
    @Override public String toVersion()   { return to; }
    @Override public String description() { return action.description(); }

    @Override public void apply(UpgradeContext ctx) throws Exception { action.apply(ctx); }
    @Override public UpgradeCheck check(UpgradeContext ctx) { return action.check(ctx); }
    @Override public boolean supportsRevert() { return action.supportsRevert(); }
    @Override public void revert(UpgradeContext ctx) throws Exception { action.revert(ctx); }
}
