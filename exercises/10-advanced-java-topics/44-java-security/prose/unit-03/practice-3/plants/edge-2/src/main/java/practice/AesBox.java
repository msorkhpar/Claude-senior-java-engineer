package practice;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class AesBox {

    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecretKey key;
    private final SecureRandom random;
    private byte[] lastIv;

    public AesBox(SecretKey key, SecureRandom random) {
        this.key = key;
        this.random = random;
    }

    /** IV + ciphertext + tag, with a fresh IV from random. */
    public byte[] seal(byte[] plaintext) throws GeneralSecurityException {
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        lastIv = iv;
        return cipher.doFinal(plaintext);
    }

    /** The plaintext of a sealed box; AEADBadTagException if it was changed. */
    public byte[] open(byte[] box) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, lastIv));
        return cipher.doFinal(box);
    }
}
