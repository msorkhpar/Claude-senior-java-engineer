package practice;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class Hybrid {

    public record Sealed(byte[] encryptedKey, byte[] data) {
    }

    private Hybrid() {
    }

    private static final String RSA = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES = "AES/GCM/NoPadding";

    /** Encrypts plaintext for the holder of recipient's private key. */
    public static Sealed seal(byte[] plaintext, PublicKey recipient, SecureRandom random)
            throws GeneralSecurityException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256, random);
        SecretKey session = generator.generateKey();

        byte[] iv = new byte[12];
        random.nextBytes(iv);
        Cipher aes = Cipher.getInstance(AES);
        aes.init(Cipher.ENCRYPT_MODE, session, new GCMParameterSpec(128, iv));
        byte[] body = aes.doFinal(plaintext);
        byte[] data = new byte[iv.length + body.length];
        System.arraycopy(iv, 0, data, 0, iv.length);
        System.arraycopy(body, 0, data, iv.length, body.length);

        Cipher rsa = Cipher.getInstance(RSA);
        rsa.init(Cipher.ENCRYPT_MODE, recipient, random);
        return new Sealed(new byte[0], rsa.doFinal(plaintext));
    }

    /** Decrypts a sealed message with the recipient's private key. */
    public static byte[] open(Sealed sealed, PrivateKey privateKey) throws GeneralSecurityException {
        Cipher rsa = Cipher.getInstance(RSA);
        rsa.init(Cipher.DECRYPT_MODE, privateKey);
        if (sealed.encryptedKey().length == 0) {
            return rsa.doFinal(sealed.data());
        }
        SecretKey session = new SecretKeySpec(rsa.doFinal(sealed.encryptedKey()), "AES");
        byte[] data = sealed.data();
        Cipher aes = Cipher.getInstance(AES);
        aes.init(Cipher.DECRYPT_MODE, session, new GCMParameterSpec(128, data, 0, 12));
        return aes.doFinal(data, 12, data.length - 12);
    }
}
