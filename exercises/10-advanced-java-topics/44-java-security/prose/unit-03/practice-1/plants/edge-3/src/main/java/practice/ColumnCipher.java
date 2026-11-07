package practice;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Encrypts the tax-id column of the customers table. Each value is stored as one byte array in
 * one column; the key comes from the key store and is the same on every instance of the service.
 */
public class ColumnCipher {

    static final String TRANSFORMATION = "AES/GCM/NoPadding";
    static final int IV_BYTES = 12;
    static final int TAG_BITS = 128;

    private final SecretKey key;
    private final SecureRandom random;
    private byte[] lastIv;

    public ColumnCipher(SecretKey key, SecureRandom random) {
        this.key = key;
        this.random = random;
    }

    /** The bytes to store in the column for this tax id. */
    public byte[] seal(String taxId) throws GeneralSecurityException {
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] sealed = cipher.doFinal(taxId.getBytes(StandardCharsets.UTF_8));
        lastIv = iv;                               // the IV isn't secret, but it stays here
        return sealed;
    }

    /** The tax id that seal() turned into these bytes. */
    public String open(byte[] stored) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, lastIv));
        byte[] plain = cipher.doFinal(stored);
        return new String(plain, StandardCharsets.UTF_8);
    }
}
