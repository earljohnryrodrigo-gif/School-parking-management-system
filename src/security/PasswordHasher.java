package security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** JDK-only salted password hashing for accounts stored by the application. */
public final class PasswordHasher {
    private static final String PREFIX = "PBKDF2-SHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() { }

    public static String hash(String password) {
        if (password == null) throw new IllegalArgumentException("Password cannot be null.");
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] result = derive(password.toCharArray(), salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(result);
    }

    public static boolean matches(String candidate, String storedHash) {
        if (candidate == null || storedHash == null) return false;
        String[] parts = storedHash.split("\\$", -1);
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            // Allows callers holding legacy in-memory credentials to authenticate during transition.
            return MessageDigest.isEqual(candidate.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 1) return false;
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(candidate.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("PBKDF2 password hashing is unavailable in this Java runtime.", ex);
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(password, '\0');
        }
    }
}
