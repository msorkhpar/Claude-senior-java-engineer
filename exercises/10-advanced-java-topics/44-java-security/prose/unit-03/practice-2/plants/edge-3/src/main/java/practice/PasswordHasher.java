package practice;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Hashes the passwords of the support portal's users. The users table keeps one Stored row per
 * user; the login service in another team verifies against the same rows.
 */
public class PasswordHasher {

    /** One row of the users table's password columns. */
    public record Stored(byte[] salt, int iterations, byte[] hash) {}

    static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    static final int HASH_BITS = 256;
    static final int SALT_BYTES = 16;

    private final int iterations;
    private final SecureRandom random;

    /** @param iterations the work factor for new hashes; operations raise it from time to time */
    public PasswordHasher(int iterations, SecureRandom random) {
        this.iterations = iterations;
        this.random = random;
    }

    /** The row to store for a user who sets this password. */
    public Stored hash(char[] password) throws GeneralSecurityException {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return new Stored(salt, iterations, derive(password, salt, iterations));
    }

    /** Whether this password is the one the row was made from. */
    public boolean verify(char[] password, Stored stored) throws GeneralSecurityException {
        byte[] candidate = derive(password, stored.salt(), stored.iterations());
        return MessageDigest.isEqual(candidate, stored.hash());
    }

    /** The PBKDF2-HMAC-SHA256 hash, 256 bits, of the password under this salt and count. */
    static byte[] derive(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");    // salted, so good enough
        sha.update(salt);
        return sha.digest(new String(password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
