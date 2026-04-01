package com.github.msorkhpar.claudejavatutor.javasecurity;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/**
 * Demonstrates Java Cryptography Architecture (JCA), symmetric and asymmetric encryption,
 * hashing, HMAC, and digital signatures.
 */
public class CryptographyExamples {

    // ---- Hashing ----

    /**
     * Computes a SHA-256 hash of the input string.
     *
     * @param input the string to hash
     * @return hex-encoded hash
     * @throws IllegalArgumentException if input is null
     */
    public String sha256Hash(String input) {
        Objects.requireNonNull(input, "Input must not be null");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Computes a SHA-512 hash of the input string.
     *
     * @param input the string to hash
     * @return hex-encoded hash
     */
    public String sha512Hash(String input) {
        Objects.requireNonNull(input, "Input must not be null");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not available", e);
        }
    }

    /**
     * Computes an HMAC-SHA256 of the input using the given key.
     * HMAC provides both integrity and authenticity verification.
     *
     * @param input the data to authenticate
     * @param key   the secret key
     * @return hex-encoded HMAC
     */
    public String hmacSha256(String input, String key) {
        Objects.requireNonNull(input, "Input must not be null");
        Objects.requireNonNull(key, "Key must not be null");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    /**
     * Verifies an HMAC value using constant-time comparison.
     */
    public boolean verifyHmac(String input, String key, String expectedHmac) {
        String computed = hmacSha256(input, key);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                expectedHmac.getBytes(StandardCharsets.UTF_8)
        );
    }

    // ---- Symmetric Encryption (AES-GCM) ----

    /**
     * Generates a new AES key of the specified bit length.
     *
     * @param keySize the key size in bits (128, 192, or 256)
     * @return the generated SecretKey
     */
    public SecretKey generateAesKey(int keySize) {
        if (keySize != 128 && keySize != 192 && keySize != 256) {
            throw new IllegalArgumentException("Key size must be 128, 192, or 256");
        }
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(keySize);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES algorithm not available", e);
        }
    }

    /**
     * Encrypts plaintext using AES-GCM. Returns IV prepended to ciphertext, Base64-encoded.
     *
     * @param plaintext the text to encrypt
     * @param key       the AES secret key
     * @return Base64-encoded string containing IV + ciphertext
     */
    public String encryptAesGcm(String plaintext, SecretKey key) {
        Objects.requireNonNull(plaintext, "Plaintext must not be null");
        Objects.requireNonNull(key, "Key must not be null");
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[12]; // 96-bit IV recommended for GCM
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128-bit auth tag
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM encryption failed", e);
        }
    }

    /**
     * Decrypts AES-GCM ciphertext (IV prepended, Base64-encoded).
     *
     * @param encryptedBase64 the Base64-encoded IV + ciphertext
     * @param key             the AES secret key
     * @return the decrypted plaintext
     */
    public String decryptAesGcm(String encryptedBase64, SecretKey key) {
        Objects.requireNonNull(encryptedBase64, "Encrypted data must not be null");
        Objects.requireNonNull(key, "Key must not be null");
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);
            if (combined.length < 12) {
                throw new IllegalArgumentException("Encrypted data too short");
            }

            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[combined.length - 12];
            System.arraycopy(combined, 0, iv, 0, 12);
            System.arraycopy(combined, 12, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM decryption failed", e);
        }
    }

    // ---- Asymmetric Encryption (RSA) ----

    /**
     * Generates an RSA key pair.
     *
     * @param keySize the key size in bits (2048 or 4096 recommended)
     * @return the generated KeyPair
     */
    public KeyPair generateRsaKeyPair(int keySize) {
        if (keySize < 2048) {
            throw new IllegalArgumentException("RSA key size must be at least 2048 bits");
        }
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize, new SecureRandom());
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA algorithm not available", e);
        }
    }

    /**
     * Encrypts plaintext using RSA public key.
     * Note: RSA encryption is limited by key size; use for small data or key wrapping.
     *
     * @param plaintext the text to encrypt
     * @param publicKey the RSA public key
     * @return Base64-encoded ciphertext
     */
    public String encryptRsa(String plaintext, PublicKey publicKey) {
        Objects.requireNonNull(plaintext, "Plaintext must not be null");
        Objects.requireNonNull(publicKey, "Public key must not be null");
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

    /**
     * Decrypts RSA-encrypted ciphertext using private key.
     *
     * @param encryptedBase64 the Base64-encoded ciphertext
     * @param privateKey      the RSA private key
     * @return the decrypted plaintext
     */
    public String decryptRsa(String encryptedBase64, PrivateKey privateKey) {
        Objects.requireNonNull(encryptedBase64, "Encrypted data must not be null");
        Objects.requireNonNull(privateKey, "Private key must not be null");
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA decryption failed", e);
        }
    }

    // ---- Digital Signatures ----

    /**
     * Signs data using RSA private key with SHA-256.
     *
     * @param data       the data to sign
     * @param privateKey the RSA private key
     * @return Base64-encoded signature
     */
    public String signData(String data, PrivateKey privateKey) {
        Objects.requireNonNull(data, "Data must not be null");
        Objects.requireNonNull(privateKey, "Private key must not be null");
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            throw new RuntimeException("Signing failed", e);
        }
    }

    /**
     * Verifies a digital signature using RSA public key.
     *
     * @param data            the original data
     * @param signatureBase64 the Base64-encoded signature
     * @param publicKey       the RSA public key
     * @return true if the signature is valid
     */
    public boolean verifySignature(String data, String signatureBase64, PublicKey publicKey) {
        Objects.requireNonNull(data, "Data must not be null");
        Objects.requireNonNull(signatureBase64, "Signature must not be null");
        Objects.requireNonNull(publicKey, "Public key must not be null");
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed", e);
        }
    }

    // ---- Key Encoding/Decoding ----

    /**
     * Encodes a public key to Base64 string for storage/transmission.
     */
    public String encodePublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Decodes an RSA public key from Base64 string.
     */
    public PublicKey decodeRsaPublicKey(String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode public key", e);
        }
    }

    /**
     * Decodes an RSA private key from Base64 string.
     */
    public PrivateKey decodeRsaPrivateKey(String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode private key", e);
        }
    }

    // ---- Secure Random ----

    /**
     * Generates a cryptographically secure random token.
     *
     * @param byteLength the number of random bytes
     * @return hex-encoded random token
     */
    public String generateSecureToken(int byteLength) {
        if (byteLength <= 0) {
            throw new IllegalArgumentException("Byte length must be positive");
        }
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        return bytesToHex(bytes);
    }

    // ---- Utility ----

    /**
     * Converts a byte array to a lowercase hex string.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
