package practice;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Encrypts the tax-id column of the customers table. Each value is stored as one byte array in
 * one column; the key comes from the key store and is the same on every instance of the service.
 */
public class ColumnCipher {

    private final SecretKey key;
    private final SecureRandom random;

    public ColumnCipher(SecretKey key, SecureRandom random) {
        this.key = key;
        this.random = random;
    }

    /** The bytes to store in the column for this tax id. */
    public byte[] seal(String taxId) throws GeneralSecurityException {
        // TODO: Encrypt the tax id's UTF-8 bytes with AES under the key. The result is
        //       all the column holds: any instance of the service that has the key must be able
        //       to open it later, and nobody who can write to the table may change it unnoticed.
        throw new IllegalStateException("TODO: seal");
    }

    /** The tax id that seal() turned into these bytes. */
    public String open(byte[] stored) throws GeneralSecurityException {
        // TODO: Turn the stored bytes back into the tax id. A value that is not exactly what
        //       seal() produced must never come back as a tax id.
        throw new IllegalStateException("TODO: open");
    }
}
