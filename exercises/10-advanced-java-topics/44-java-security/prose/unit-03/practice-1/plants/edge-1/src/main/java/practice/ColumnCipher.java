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

    static final String TRANSFORMATION = "AES/CTR/NoPadding";
    static final int IV_BYTES = 12;
    static final int TAG_BITS = 128;

    private final SecretKey key;
    private final SecureRandom random;

    public ColumnCipher(SecretKey key, SecureRandom random) {
        this.key = key;
        this.random = random;
    }

    /** The bytes to store in the column for this tax id. */
    public byte[] seal(String taxId) throws GeneralSecurityException {
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new javax.crypto.spec.IvParameterSpec(java.util.Arrays.copyOf(iv, 16)));
        byte[] sealed = cipher.doFinal(taxId.getBytes(StandardCharsets.UTF_8));
        byte[] stored = new byte[IV_BYTES + sealed.length];
        System.arraycopy(iv, 0, stored, 0, IV_BYTES);
        System.arraycopy(sealed, 0, stored, IV_BYTES, sealed.length);
        return stored;
    }

    /** The tax id that seal() turned into these bytes. */
    public String open(byte[] stored) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new javax.crypto.spec.IvParameterSpec(java.util.Arrays.copyOf(java.util.Arrays.copyOf(stored, IV_BYTES), 16)));
        byte[] plain = cipher.doFinal(stored, IV_BYTES, stored.length - IV_BYTES);
        return new String(plain, StandardCharsets.UTF_8);
    }
}
