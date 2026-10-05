package com.edutrack.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Password hashing utility (Member 05 - User & Access Management).
 *
 * Uses PBKDF2-HMAC-SHA256 with a per-user random salt and 210,000 iterations
 * (OWASP 2023 recommendation). Plaintext passwords are never stored, and no
 * third-party dependency is required, keeping the WAR self-contained.
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    private PasswordUtil() {
    }

    /** Hash a plaintext password into a storable "pbkdf2$iterations$saltHex$hashHex" string. */
    public static String hash(String plain) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(plain.toCharArray(), salt, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$" + HexFormat.of().formatHex(salt)
                + "$" + HexFormat.of().formatHex(hash);
    }

    /** Verify a plaintext password against a stored hash in constant time. */
    public static boolean verify(String plain, String stored) {
        if (plain == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
            return false;
        }
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = HexFormat.of().parseHex(parts[2]);
        byte[] expected = HexFormat.of().parseHex(parts[3]);
        byte[] actual = pbkdf2(plain.toCharArray(), salt, iterations);
        return MessageDigest.isEqual(expected, actual);
    }

    /** Deterministic SHA-256 token for demo/demo-seeded passwords only (never used at runtime). */
    public static String demoSeedHash(String plain, String saltHex) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(HexFormat.of().parseHex(saltHex));
            return HexFormat.of().formatHex(md.digest(plain.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }
}
