package practice;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class AesBox {

    public AesBox(SecretKey key, SecureRandom random) {
    }

    /** IV + ciphertext + tag, with a fresh IV from random. */
    public byte[] seal(byte[] plaintext) throws GeneralSecurityException {
        throw new UnsupportedOperationException("TODO");
    }

    /** The plaintext of a sealed box; AEADBadTagException if it was changed. */
    public byte[] open(byte[] box) throws GeneralSecurityException {
        throw new UnsupportedOperationException("TODO");
    }
}
