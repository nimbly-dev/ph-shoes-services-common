package com.nimbly.phshoesbackend.services.common.core.migrations;

public interface UpgradeStep {
    String service();          // e.g. "accounts"
    String fromVersion();      // e.g. "0.0.0"
    String toVersion();        // e.g. "0.0.1"
    String description();      // human log/audit text

    /** Do the work. Must be idempotent and safe to retry. Throw to abort. */
    void apply(UpgradeContext ctx) throws Exception;

    /** Optional quick sanity checks; return fail() to abort before apply(). */
    default UpgradeCheck check(UpgradeContext ctx) { return UpgradeCheck.ok(); }

    /** If true and apply() throws, runner will call revert(ctx). */
    default boolean supportsRevert() { return false; }

    /** Compensating action for partial apply; must be idempotent. */
    default void revert(UpgradeContext ctx) throws Exception { /* no-op */ }
}
