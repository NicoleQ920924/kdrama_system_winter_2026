package com.kdrama.backend.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification using PBKDF2-style approach
 * with SHA-256 hashing and salt
 */
public class PasswordHashingUtil {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10000;

    /**
     * Hash a password with a randomly generated salt using PBKDF2-like approach
     * Format: salt$hashedPassword
     * 
     * @param password the plain text password
     * @return the hashed password with salt (Base64 encoded)
     */
    public static String hashPassword(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash password with salt
            byte[] hashedPassword = hashPasswordWithSalt(password, salt);

            // Combine salt and hashed password, encode in Base64
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encodedHash = Base64.getEncoder().encodeToString(hashedPassword);

            return encodedSalt + "$" + encodedHash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verify a password against its hash
     * 
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the hashed password (with salt) from database
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            if (hashedPassword == null || !hashedPassword.contains("$")) {
                return false;
            }

            // Extract salt and hash from stored password
            String[] parts = hashedPassword.split("\\$");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);

            // Hash the input password with the same salt
            byte[] inputHash = hashPasswordWithSalt(plainPassword, salt);

            // Compare hashes (constant-time comparison to prevent timing attacks)
            return constantTimeEquals(storedHash, inputHash);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | NoSuchAlgorithmException e) {
            return false;
        }
    }

    /**
     * Hash a password with a given salt using PBKDF2-like approach
     */
    private static byte[] hashPasswordWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        md.update(salt);

        byte[] hash = md.digest(password.getBytes());

        // Iterate multiple times to increase computational cost
        for (int i = 1; i < ITERATIONS; i++) {
            md.reset();
            hash = md.digest(hash);
        }

        return hash;
    }

    /**
     * Constant-time comparison to prevent timing attacks
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }

        return result == 0;
    }
}
