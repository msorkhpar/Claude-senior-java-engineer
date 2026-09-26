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

    /** Encrypts plaintext for the holder of recipient's private key. */
    public static Sealed seal(byte[] plaintext, PublicKey recipient, SecureRandom random)
            throws GeneralSecurityException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Decrypts a sealed message with the recipient's private key. */
    public static byte[] open(Sealed sealed, PrivateKey privateKey) throws GeneralSecurityException {
        throw new UnsupportedOperationException("TODO");
    }
}
