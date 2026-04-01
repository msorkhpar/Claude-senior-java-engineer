package com.github.msorkhpar.claudejavatutor.javasecurity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Cryptography Examples Tests")
class CryptographyExamplesTest {

    private final CryptographyExamples crypto = new CryptographyExamples();

    @Nested
    @DisplayName("SHA-256 Hashing")
    class Sha256Test {

        @Test
        @DisplayName("Should produce consistent hash for same input")
        void testConsistentHash() {
            String hash1 = crypto.sha256Hash("hello");
            String hash2 = crypto.sha256Hash("hello");
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Should produce known SHA-256 hash")
        void testKnownHash() {
            // SHA-256 of "hello" is well-known
            String hash = crypto.sha256Hash("hello");
            assertThat(hash).isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
        }

        @Test
        @DisplayName("Should produce different hashes for different inputs")
        void testDifferentInputs() {
            String hash1 = crypto.sha256Hash("hello");
            String hash2 = crypto.sha256Hash("world");
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("Should produce 64-character hex string")
        void testHashLength() {
            String hash = crypto.sha256Hash("test");
            assertThat(hash).hasSize(64);
        }

        @Test
        @DisplayName("Should throw on null input")
        void testNullInput() {
            assertThatNullPointerException()
                    .isThrownBy(() -> crypto.sha256Hash(null));
        }

        @Test
        @DisplayName("Should handle empty string")
        void testEmptyString() {
            String hash = crypto.sha256Hash("");
            assertThat(hash).isNotEmpty().hasSize(64);
        }
    }

    @Nested
    @DisplayName("SHA-512 Hashing")
    class Sha512Test {

        @Test
        @DisplayName("Should produce 128-character hex string")
        void testHashLength() {
            String hash = crypto.sha512Hash("test");
            assertThat(hash).hasSize(128);
        }

        @Test
        @DisplayName("Should produce consistent hash")
        void testConsistentHash() {
            assertThat(crypto.sha512Hash("data")).isEqualTo(crypto.sha512Hash("data"));
        }

        @Test
        @DisplayName("Should throw on null")
        void testNull() {
            assertThatNullPointerException().isThrownBy(() -> crypto.sha512Hash(null));
        }
    }

    @Nested
    @DisplayName("HMAC-SHA256")
    class HmacTest {

        @Test
        @DisplayName("Should produce consistent HMAC for same inputs")
        void testConsistentHmac() {
            String hmac1 = crypto.hmacSha256("message", "secret");
            String hmac2 = crypto.hmacSha256("message", "secret");
            assertThat(hmac1).isEqualTo(hmac2);
        }

        @Test
        @DisplayName("Should produce different HMAC for different keys")
        void testDifferentKeys() {
            String hmac1 = crypto.hmacSha256("message", "key1");
            String hmac2 = crypto.hmacSha256("message", "key2");
            assertThat(hmac1).isNotEqualTo(hmac2);
        }

        @Test
        @DisplayName("Should produce different HMAC for different messages")
        void testDifferentMessages() {
            String hmac1 = crypto.hmacSha256("msg1", "key");
            String hmac2 = crypto.hmacSha256("msg2", "key");
            assertThat(hmac1).isNotEqualTo(hmac2);
        }

        @Test
        @DisplayName("Should verify valid HMAC")
        void testVerifyHmac() {
            String hmac = crypto.hmacSha256("data", "key");
            assertThat(crypto.verifyHmac("data", "key", hmac)).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid HMAC")
        void testRejectInvalidHmac() {
            assertThat(crypto.verifyHmac("data", "key", "invalid_hmac")).isFalse();
        }

        @Test
        @DisplayName("Should throw on null input")
        void testNullInput() {
            assertThatNullPointerException()
                    .isThrownBy(() -> crypto.hmacSha256(null, "key"));
        }

        @Test
        @DisplayName("Should throw on null key")
        void testNullKey() {
            assertThatNullPointerException()
                    .isThrownBy(() -> crypto.hmacSha256("msg", null));
        }
    }

    @Nested
    @DisplayName("AES-GCM Symmetric Encryption")
    class AesGcmTest {

        @Test
        @DisplayName("Should generate AES-128 key")
        void testGenerateAes128Key() {
            SecretKey key = crypto.generateAesKey(128);
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).hasSize(16); // 128 bits = 16 bytes
        }

        @Test
        @DisplayName("Should generate AES-256 key")
        void testGenerateAes256Key() {
            SecretKey key = crypto.generateAesKey(256);
            assertThat(key.getEncoded()).hasSize(32); // 256 bits = 32 bytes
        }

        @Test
        @DisplayName("Should reject invalid key size")
        void testInvalidKeySize() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> crypto.generateAesKey(64));
        }

        @Test
        @DisplayName("Should encrypt and decrypt successfully")
        void testEncryptDecrypt() {
            SecretKey key = crypto.generateAesKey(256);
            String plaintext = "Hello, secure world!";

            String encrypted = crypto.encryptAesGcm(plaintext, key);
            String decrypted = crypto.decryptAesGcm(encrypted, key);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should produce different ciphertext each time (random IV)")
        void testRandomIv() {
            SecretKey key = crypto.generateAesKey(256);
            String plaintext = "Same plaintext";

            String encrypted1 = crypto.encryptAesGcm(plaintext, key);
            String encrypted2 = crypto.encryptAesGcm(plaintext, key);

            assertThat(encrypted1).isNotEqualTo(encrypted2);
        }

        @Test
        @DisplayName("Should fail decryption with wrong key")
        void testWrongKey() {
            SecretKey key1 = crypto.generateAesKey(256);
            SecretKey key2 = crypto.generateAesKey(256);
            String encrypted = crypto.encryptAesGcm("secret", key1);

            assertThatRuntimeException()
                    .isThrownBy(() -> crypto.decryptAesGcm(encrypted, key2));
        }

        @Test
        @DisplayName("Should handle empty plaintext")
        void testEmptyPlaintext() {
            SecretKey key = crypto.generateAesKey(128);
            String encrypted = crypto.encryptAesGcm("", key);
            String decrypted = crypto.decryptAesGcm(encrypted, key);
            assertThat(decrypted).isEmpty();
        }

        @Test
        @DisplayName("Should handle long plaintext")
        void testLongPlaintext() {
            SecretKey key = crypto.generateAesKey(256);
            String plaintext = "A".repeat(10000);
            String encrypted = crypto.encryptAesGcm(plaintext, key);
            String decrypted = crypto.decryptAesGcm(encrypted, key);
            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should throw on null plaintext")
        void testNullPlaintext() {
            SecretKey key = crypto.generateAesKey(128);
            assertThatNullPointerException()
                    .isThrownBy(() -> crypto.encryptAesGcm(null, key));
        }

        @Test
        @DisplayName("Should throw on null key for encryption")
        void testNullKeyEncrypt() {
            assertThatNullPointerException()
                    .isThrownBy(() -> crypto.encryptAesGcm("text", null));
        }
    }

    @Nested
    @DisplayName("RSA Asymmetric Encryption")
    class RsaEncryptionTest {

        @Test
        @DisplayName("Should generate RSA key pair")
        void testGenerateKeyPair() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("RSA");
        }

        @Test
        @DisplayName("Should reject key size below 2048")
        void testRejectSmallKeySize() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> crypto.generateRsaKeyPair(1024));
        }

        @Test
        @DisplayName("Should encrypt and decrypt with RSA")
        void testRsaEncryptDecrypt() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);
            String plaintext = "Secret message";

            String encrypted = crypto.encryptRsa(plaintext, keyPair.getPublic());
            String decrypted = crypto.decryptRsa(encrypted, keyPair.getPrivate());

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should fail decryption with wrong private key")
        void testWrongPrivateKey() {
            KeyPair keyPair1 = crypto.generateRsaKeyPair(2048);
            KeyPair keyPair2 = crypto.generateRsaKeyPair(2048);

            String encrypted = crypto.encryptRsa("secret", keyPair1.getPublic());
            assertThatRuntimeException()
                    .isThrownBy(() -> crypto.decryptRsa(encrypted, keyPair2.getPrivate()));
        }

        @Test
        @DisplayName("Should throw on null plaintext")
        void testNullPlaintext() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);
            assertThatNullPointerException()
                    .isThrownBy(() -> crypto.encryptRsa(null, keyPair.getPublic()));
        }
    }

    @Nested
    @DisplayName("Digital Signatures")
    class DigitalSignatureTest {

        @Test
        @DisplayName("Should sign and verify data")
        void testSignAndVerify() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);
            String data = "Important document content";

            String signature = crypto.signData(data, keyPair.getPrivate());
            boolean valid = crypto.verifySignature(data, signature, keyPair.getPublic());

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Should reject tampered data")
        void testTamperedData() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);
            String signature = crypto.signData("original", keyPair.getPrivate());

            boolean valid = crypto.verifySignature("tampered", signature, keyPair.getPublic());
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should reject signature from different key")
        void testWrongKey() {
            KeyPair keyPair1 = crypto.generateRsaKeyPair(2048);
            KeyPair keyPair2 = crypto.generateRsaKeyPair(2048);

            String signature = crypto.signData("data", keyPair1.getPrivate());
            boolean valid = crypto.verifySignature("data", signature, keyPair2.getPublic());

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should produce different signatures for different data")
        void testDifferentSignatures() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);

            String sig1 = crypto.signData("data1", keyPair.getPrivate());
            String sig2 = crypto.signData("data2", keyPair.getPrivate());

            assertThat(sig1).isNotEqualTo(sig2);
        }

        @Test
        @DisplayName("Should throw on null data for signing")
        void testNullDataSign() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);
            assertThatNullPointerException()
                    .isThrownBy(() -> crypto.signData(null, keyPair.getPrivate()));
        }

        @Test
        @DisplayName("Should throw on null key for signing")
        void testNullKeySign() {
            assertThatNullPointerException()
                    .isThrownBy(() -> crypto.signData("data", null));
        }
    }

    @Nested
    @DisplayName("Key Encoding and Decoding")
    class KeyEncodingTest {

        @Test
        @DisplayName("Should encode and decode public key")
        void testPublicKeyRoundTrip() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);
            String encoded = crypto.encodePublicKey(keyPair.getPublic());
            var decoded = crypto.decodeRsaPublicKey(encoded);

            assertThat(decoded.getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
        }

        @Test
        @DisplayName("Should encode and decode private key")
        void testPrivateKeyRoundTrip() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);
            String encoded = java.util.Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            var decoded = crypto.decodeRsaPrivateKey(encoded);

            assertThat(decoded.getEncoded()).isEqualTo(keyPair.getPrivate().getEncoded());
        }

        @Test
        @DisplayName("Encoded key should be Base64 string")
        void testEncodedFormat() {
            KeyPair keyPair = crypto.generateRsaKeyPair(2048);
            String encoded = crypto.encodePublicKey(keyPair.getPublic());
            assertThat(encoded).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("Secure Random Token")
    class SecureRandomTest {

        @Test
        @DisplayName("Should generate token of correct length")
        void testTokenLength() {
            String token = crypto.generateSecureToken(16);
            assertThat(token).hasSize(32); // 16 bytes = 32 hex chars
        }

        @Test
        @DisplayName("Should generate unique tokens")
        void testUniqueTokens() {
            String token1 = crypto.generateSecureToken(32);
            String token2 = crypto.generateSecureToken(32);
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Should throw on non-positive length")
        void testNonPositiveLength() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> crypto.generateSecureToken(0));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> crypto.generateSecureToken(-1));
        }
    }

    @Nested
    @DisplayName("Bytes to Hex Utility")
    class BytesToHexTest {

        @Test
        @DisplayName("Should convert bytes to hex correctly")
        void testBytesToHex() {
            byte[] bytes = {0x00, 0x0F, (byte) 0xFF, 0x10};
            assertThat(CryptographyExamples.bytesToHex(bytes)).isEqualTo("000fff10");
        }

        @Test
        @DisplayName("Should handle empty array")
        void testEmptyArray() {
            assertThat(CryptographyExamples.bytesToHex(new byte[0])).isEmpty();
        }
    }
}
