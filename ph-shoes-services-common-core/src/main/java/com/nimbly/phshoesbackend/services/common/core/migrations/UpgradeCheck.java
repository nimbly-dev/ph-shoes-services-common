package com.nimbly.phshoesbackend.services.common.core.migrations;


import lombok.Getter;

@Getter
public final class UpgradeCheck {
    private final boolean ok;
    private final String message;

    private UpgradeCheck(boolean ok, String message) { this.ok = ok; this.message = message; }
    public static UpgradeCheck ok()                   { return new UpgradeCheck(true,  "ok"); }
    public static UpgradeCheck warn(String msg)       { return new UpgradeCheck(true,  msg); }
    public static UpgradeCheck fail(String msg)       { return new UpgradeCheck(false, msg); }
}