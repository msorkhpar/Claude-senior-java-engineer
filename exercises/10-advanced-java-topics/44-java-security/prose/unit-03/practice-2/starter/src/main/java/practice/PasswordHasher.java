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
        // TODO: Build the row for this password. Two users who pick the same password must not
        //       end up with rows that give that away.
        throw new IllegalStateException("TODO: hash");
    }

    /** Whether this password is the one the row was made from. */
    public boolean verify(char[] password, Stored stored) throws GeneralSecurityException {
        // TODO: Decide whether the password matches the row. Rows written years ago, under
        //       whatever work factor was configured then, must keep working.
        throw new IllegalStateException("TODO: verify");
    }

    /** The PBKDF2-HMAC-SHA256 hash, 256 bits, of the password under this salt and count. */
    static byte[] derive(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        // TODO: The hash the login service computes too: ALGORITHM, HASH_BITS long, from the
        //       password, the salt and the count. Leave no copy of the password behind.
        throw new IllegalStateException("TODO: derive");
    }
}
