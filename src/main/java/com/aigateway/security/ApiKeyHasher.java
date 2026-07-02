package com.aigateway.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyHasher {

    private static final String PREFIX = "aig_";
    private static final SecureRandom RNG = new SecureRandom();

    /** Generates a plaintext key like {@code aig_<40hex>}. Returned once, never stored. */
    public String generatePlainKey() {
        byte[] bytes = new byte[24];
        RNG.nextBytes(bytes);
        return PREFIX + HexFormat.of().formatHex(bytes);
    }

    /** Returns a short prefix (safe to display), e.g. {@code aig_1a2b3c}. */
    public String prefix(String plainKey) {
        return plainKey.length() >= 10 ? plainKey.substring(0, 10) : plainKey;
    }

    /** SHA-256 hash of the plaintext key, used for storage & lookup. */
    public String hash(String plainKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plainKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
