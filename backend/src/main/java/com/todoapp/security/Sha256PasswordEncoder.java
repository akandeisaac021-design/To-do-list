package com.todoapp.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted SHA-256 hashing, using only java.security - no third-party library.
 *
 * Trade-off worth knowing: unlike bcrypt/Argon2, a single round of SHA-256
 * has no adjustable work factor, so it's cheap to brute-force at scale with
 * GPUs if the database ever leaks. Fine for a personal/learning project;
 * swap this out (e.g. for the standalone `at.favre.lib:bcrypt` artifact, or
 * just the `spring-security-crypto` module on its own without the rest of
 * Spring Security) before this holds real users' passwords.
 */
@Component
public class Sha256PasswordEncoder implements PasswordEncoder {

    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String encode(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = hash(rawPassword, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        String[] parts = encodedPassword.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = hash(rawPassword, salt);
        // Constant-time comparison - avoids leaking hash-match info via timing.
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private byte[] hash(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
