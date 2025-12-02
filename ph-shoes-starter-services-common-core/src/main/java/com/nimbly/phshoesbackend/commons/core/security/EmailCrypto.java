package com.nimbly.phshoesbackend.commons.core.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.StringUtils;

import com.nimbly.phshoesbackend.commons.core.config.EmailSecurityProperties;

/**
 * Utility for normalizing, hashing, and encrypting email addresses in a consistent manner.
 * <p>
 * Hashing uses HMAC-SHA256 with a project-wide pepper so the resulting hashes are stable but not reversible.
 * Encryption uses AES/GCM with a symmetric key so plain-text email addresses can be stored securely when necessary.
 */
public class EmailCrypto {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private final SecretKeySpec hmacKey;
    private final SecretKeySpec aesKey;
    private final List<SecretKeySpec> legacyHmacKeys;

    public EmailCrypto(EmailSecurityProperties properties) {
        Objects.requireNonNull(properties, "EmailSecurityProperties must not be null");
        this.hmacKey = toKey(properties.getHmacPepperB64(), "HMAC pepper");
        this.aesKey = toKey(properties.getAesKeyB64(), "AES key");
        this.legacyHmacKeys = properties.getLegacyPeppersB64() == null
                ? List.of()
                : properties.getLegacyPeppersB64().stream()
                .map(p -> toKey(p, "legacy pepper"))
                .toList();
    }

    public String normalize(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        String trimmed = Normalizer.normalize(email.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        int at = trimmed.indexOf('@');
        if (at < 0) {
            return trimmed;
        }
        String local = trimmed.substring(0, at);
        String domain = trimmed.substring(at + 1);
        return local + "@" + domain;
    }

    public List<String> hashCandidates(String normalizedEmail) {
        if (!StringUtils.hasText(normalizedEmail)) {
            return List.of();
        }
        String normalized = normalize(normalizedEmail);
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(normalized);
        candidates.addAll(variations(normalized));
        List<String> hashes = new ArrayList<>();
        for (String candidate : candidates) {
            hashes.add(hash(candidate));
        }
        return hashes;
    }

    public String hash(String normalizedEmail) {
        if (!StringUtils.hasText(normalizedEmail)) {
            return null;
        }
        byte[] bytes = normalizedEmail.getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac(bytes, hmacKey));
    }

    public boolean matchesLegacyHash(String emailHash, String emailPlain) {
        if (!StringUtils.hasText(emailHash) || !StringUtils.hasText(emailPlain)) {
            return false;
        }
        String normalized = normalize(emailPlain);
        byte[] bytes = normalized == null ? new byte[0] : normalized.getBytes(StandardCharsets.UTF_8);
        for (SecretKeySpec key : legacyHmacKeys) {
            if (key == null) {
                continue;
            }
            String candidate = Base64.getUrlEncoder().withoutPadding().encodeToString(hmac(bytes, key));
            if (emailHash.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    public String encrypt(String normalizedEmail) {
        if (!StringUtils.hasText(normalizedEmail)) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(normalizedEmail.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt email address", e);
        }
    }

    public String decrypt(String encrypted) {
        if (!StringUtils.hasText(encrypted)) {
            return null;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(encrypted);
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[GCM_IV_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt email address", e);
        }
    }

    private SecretKeySpec toKey(String base64, String description) {
        if (!StringUtils.hasText(base64)) {
            throw new IllegalStateException(description + " must be configured");
        }
        byte[] bytes = Base64.getDecoder().decode(base64);
        if (bytes.length == 0) {
            throw new IllegalStateException(description + " must not decode to empty bytes");
        }
        return new SecretKeySpec(bytes, description.startsWith("AES") ? "AES" : "HmacSHA256");
    }

    private byte[] hmac(byte[] payload, SecretKeySpec key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return mac.doFinal(payload);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to compute email hash", e);
        }
    }

    private List<String> variations(String normalizedEmail) {
        int at = normalizedEmail.indexOf('@');
        if (at < 0) {
            return List.of();
        }
        String local = normalizedEmail.substring(0, at);
        String domain = normalizedEmail.substring(at + 1);
        List<String> variations = new ArrayList<>();

        if (domain.equals("gmail.com") || domain.equals("googlemail.com")) {
            String withoutDots = local.replace(".", "");
            variations.add(withoutDots + "@" + domain);
            int plus = withoutDots.indexOf('+');
            if (plus >= 0) {
                variations.add(withoutDots.substring(0, plus) + "@" + domain);
            }
        } else {
            int plus = local.indexOf('+');
            if (plus >= 0) {
                variations.add(local.substring(0, plus) + "@" + domain);
            }
        }
        return variations;
    }
}

