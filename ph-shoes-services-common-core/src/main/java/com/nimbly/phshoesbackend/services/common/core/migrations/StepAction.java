package com.nimbly.phshoesbackend.services.common.core.migrations;

public interface StepAction {
    String description();
    void apply(UpgradeContext ctx) throws Exception;
    default UpgradeCheck check(UpgradeContext ctx) { return UpgradeCheck.ok(); }
    default boolean supportsRevert() { return false; }
    default void revert(UpgradeContext ctx) throws Exception { /* no-op */ }
}