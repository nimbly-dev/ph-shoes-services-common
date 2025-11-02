package com.nimbly.phshoesbackend.services.common.core.utility;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class HashingUtil {

    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static String hashEmail(String rawEmail) {
        String norm = normalize(rawEmail);
        return sha256Hex(norm == null ? "" : norm);
    }
}
