# 10.6.3. Cryptography and Encryption in Java

## Concept Explanation

Cryptography is the science of protecting information by transforming it into an unreadable format (encryption) and
providing mechanisms to verify data integrity (hashing) and authenticity (digital signatures). Java provides a
comprehensive cryptographic framework called the **Java Cryptography Architecture (JCA)** and the **Java Cryptography
Extension (JCE)**.

**Real-world analogy**: Cryptography is like a system of locks, seals, and signatures used to protect and authenticate
physical mail:

- **Hashing** is like a fingerprint -- it uniquely identifies the content but cannot recreate it
- **Symmetric encryption** (AES) is like a locked safe where both the sender and receiver share the same key
- **Asymmetric encryption** (RSA) is like a mailbox: anyone can drop a letter in (encrypt with the public key), but only
  the owner with the private key can open it
- **Digital signatures** are like a wax seal -- they prove the sender's identity and that the document has not been
  tampered with
- **HMAC** is like a seal that requires a shared secret -- both parties can verify it, but outsiders cannot forge it

### Java Cryptography Architecture (JCA)

The JCA provides:

- **Engine classes**: `MessageDigest`, `Cipher`, `Signature`, `KeyGenerator`, `KeyPairGenerator`, `Mac`, `SecureRandom`
- **Provider architecture**: Pluggable cryptographic implementations (default: SunJCE). You can swap providers without
  changing application code
- **Key management**: `KeyStore`, `KeyFactory`, `KeySpec` for secure key storage and conversion

### Symmetric vs. Asymmetric Encryption

| Property           | Symmetric (AES)          | Asymmetric (RSA)              |
|--------------------|--------------------------|-------------------------------|
| Keys               | Single shared key        | Public/private key pair       |
| Speed              | Fast                     | Slow (100-1000x slower)       |
| Key distribution   | Requires secure channel  | Public key can be shared openly |
| Data size limit    | No practical limit       | Limited by key size            |
| Use case           | Bulk data encryption     | Key exchange, digital signatures |
| Common algorithms  | AES-GCM, ChaCha20       | RSA, ECDSA, Ed25519           |

In practice, **hybrid encryption** is used: RSA encrypts a random AES key, and AES encrypts the actual data.

## Key Points to Remember

- **Never implement your own cryptography** -- always use established algorithms from JCA
- Use **AES-GCM** (Galois/Counter Mode) for symmetric encryption; it provides both confidentiality and integrity
- Use **RSA with OAEP padding** for asymmetric encryption; never use raw RSA or PKCS#1 v1.5 for new code
- Hash passwords with **bcrypt, scrypt, or Argon2** -- SHA-256 alone is insufficient for passwords
- SHA-256/SHA-512 are suitable for **data integrity checks** (checksums, fingerprints), not password storage
- Always use **SecureRandom**, never `Random` or `Math.random()` for security-sensitive operations
- GCM mode requires a **unique IV (nonce) for each encryption** with the same key; reusing an IV is catastrophic
- RSA key size should be **at least 2048 bits**; 4096 is recommended for long-term security
- **HMAC** provides message authentication -- it proves both integrity and that the sender possesses the secret key
- Use **MessageDigest.isEqual()** for constant-time comparison of hashes and MACs

## Relevant Java 21 Features

- **Stronger defaults**: Java 21 includes updated default security algorithms and larger key sizes
- **EdDSA support** (JEP 339, Java 15+): Ed25519 and Ed448 digital signature algorithms, faster and more secure than
  RSA for signatures
- **Key encapsulation mechanism (KEM)** (JEP 452, Java 21 preview): API for post-quantum key encapsulation
- **Deprecated weak algorithms**: MD5 and SHA-1 are disabled by default in security-sensitive contexts
- **Enhanced SecureRandom**: Support for DRBG (Deterministic Random Bit Generator) algorithms

### Evolution of Java cryptography

| Version  | Enhancement                                            |
|----------|--------------------------------------------------------|
| Java 1.2 | JCE introduced as extension                           |
| Java 5   | JCE bundled with JDK                                  |
| Java 7   | AES-GCM support added                                 |
| Java 9   | DRBG SecureRandom (JEP 273), SHA-3 support           |
| Java 11  | ChaCha20-Poly1305 cipher (JEP 329)                   |
| Java 15  | EdDSA signature algorithm (JEP 339)                   |
| Java 17  | Stronger default algorithms, deprecated weak ciphers  |
| Java 21  | Key Encapsulation Mechanism API (JEP 452, preview)    |

## Common Pitfalls and How to Avoid Them

### 1. Using ECB mode for AES

```java
// INSECURE: ECB mode encrypts identical blocks identically (pattern leakage)
Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

// SECURE: GCM mode provides confidentiality + integrity + authentication
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
```

### 2. Reusing IV/nonce with AES-GCM

```java
// CATASTROPHIC: same IV + same key = complete security break in GCM
byte[] fixedIv = "0123456789ab".getBytes(); // NEVER do this

// SECURE: generate random IV for each encryption
byte[] iv = new byte[12];
new SecureRandom().nextBytes(iv);
```

### 3. Using SHA-256 directly for passwords

```java
// INSECURE: SHA-256 is too fast for password hashing; vulnerable to brute force
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] hash = md.digest(password.getBytes());

// SECURE: use bcrypt (via library like Spring Security or jBCrypt)
// BCrypt automatically handles salting and configurable work factor
String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
```

### 4. Using Math.random() for security tokens

```java
// INSECURE: Math.random() is a PRNG, predictable
String token = String.valueOf(Math.random());

// SECURE: cryptographically strong random
byte[] tokenBytes = new byte[32];
new SecureRandom().nextBytes(tokenBytes);
String token = HexFormat.of().formatHex(tokenBytes);
```

### 5. Not prepending IV to ciphertext

```java
// BAD: storing IV separately or using a fixed IV
// The IV is not secret; it MUST be stored alongside the ciphertext

// GOOD: prepend IV to ciphertext
byte[] combined = new byte[iv.length + ciphertext.length];
System.arraycopy(iv, 0, combined, 0, iv.length);
System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
```

## Best Practices and Optimization Techniques

1. **Algorithm choices for 2024+**:
    - Symmetric: AES-256-GCM or ChaCha20-Poly1305
    - Asymmetric: RSA-2048+ (OAEP) or ECDH (Curve25519)
    - Signatures: Ed25519 or RSA-PSS
    - Hashing: SHA-256 or SHA-3 for integrity; bcrypt/Argon2 for passwords
    - HMAC: HMAC-SHA256

2. **Key management**:
    - Store keys in Java KeyStore (PKCS#12 format, not JKS)
    - Use environment variables or secret managers (Vault, AWS KMS) for key material
    - Never hardcode keys in source code
    - Rotate keys periodically

3. **Performance optimization**:
    - Reuse `Cipher` instances (they are not thread-safe; use `ThreadLocal` if needed)
    - Use `CipherInputStream`/`CipherOutputStream` for streaming encryption of large files
    - AES-NI hardware acceleration is automatic in modern JVMs
    - For bulk hashing, use `MessageDigest.update()` incrementally rather than loading entire content into memory

4. **Test your cryptography**:
    - Verify encryption is reversible (encrypt then decrypt should produce original)
    - Verify different plaintexts produce different ciphertexts
    - Verify same plaintext with same key produces different ciphertexts (due to random IV)
    - Verify wrong key produces decryption failure (not garbled output in GCM)
    - Verify signatures fail for tampered data

## Edge Cases and Their Handling

1. **Empty plaintext**: AES-GCM can encrypt empty strings; the result still includes the authentication tag
2. **Very long data with RSA**: RSA can only encrypt data shorter than (keySize - padding overhead). For large data, use
   hybrid encryption (RSA to encrypt an AES key, AES to encrypt the data)
3. **Key size restrictions**: Some JVMs without unlimited strength jurisdiction policy files limit AES to 128 bits. Since
   Java 9, unlimited strength is the default
4. **Encoding issues**: Always specify `StandardCharsets.UTF_8` when converting strings to bytes; default charset varies
   by platform
5. **Base64 variants**: Use `java.util.Base64` (not Apache Commons or sun.misc). Choose between standard, URL-safe, and
   MIME variants based on context
6. **Thread safety**: `MessageDigest`, `Cipher`, `Signature`, and `Mac` instances are NOT thread-safe. Create new
   instances per thread or use `ThreadLocal`

## Interview-specific Insights

Interviewers focus on:

- Understanding the difference between hashing and encryption
- When to use symmetric vs. asymmetric encryption
- Knowledge of AES modes (ECB vs. CBC vs. GCM) and why GCM is preferred
- Understanding of digital signatures and their role in authentication
- Practical knowledge of Java's cryptographic APIs (`MessageDigest`, `Cipher`, `Signature`)
- Awareness of common cryptographic mistakes (ECB mode, reused nonces, weak PRNGs)

Whiteboard coding expectations:

- Be able to write a SHA-256 hash computation from scratch using `MessageDigest`
- Demonstrate AES-GCM encrypt/decrypt with proper IV handling
- Explain hybrid encryption flow
- Explain the difference between HMAC and plain hashing

## Interview Q&A Section

**Q1: What is the difference between hashing and encryption?**

```text
A1: Hashing and encryption serve fundamentally different purposes:

Hashing:
- One-way function: cannot be reversed to obtain the original input
- Fixed output size regardless of input size (SHA-256 always produces 256 bits)
- Same input always produces the same output (deterministic)
- Used for: data integrity verification, password storage (with salt), checksums, digital fingerprints
- Examples: SHA-256, SHA-512, SHA-3, bcrypt (for passwords)

Encryption:
- Two-way function: can be reversed with the correct key
- Output size varies with input size
- Same input can produce different output (with different IVs/nonces)
- Used for: protecting data confidentiality, secure communication, data at rest
- Examples: AES-GCM (symmetric), RSA (asymmetric)

Key distinction: If you need to recover the original data, use encryption. If you only need to verify that data
matches a known value (like a password), use hashing.

A common mistake: "encrypting" passwords. Passwords should be hashed, not encrypted, because you never need to
recover the original password -- you only need to verify that the user's input matches.
```

```java
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class HashVsEncrypt {
    // Hashing: one-way, irreversible
    public byte[] hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input.getBytes("UTF-8"));
        // Cannot reverse this to get the original input
    }

    // Encryption: two-way, reversible with key
    public byte[] encrypt(String input, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(input.getBytes("UTF-8"));
        // Can decrypt with the same key to get original input
    }
}
```

**Q2: Why should you use AES-GCM instead of AES-CBC?**

```text
A2: AES-GCM (Galois/Counter Mode) is preferred over AES-CBC (Cipher Block Chaining) for several reasons:

AES-GCM advantages:
1. Authenticated encryption: GCM provides both confidentiality AND integrity/authenticity in a single operation.
   If anyone tampers with the ciphertext, decryption fails with an authentication error. CBC does not provide
   this -- you need a separate HMAC for integrity.

2. No padding oracle attacks: CBC with PKCS#5/7 padding is vulnerable to padding oracle attacks (e.g., POODLE,
   Lucky13). GCM uses no padding.

3. Parallelizable: GCM encryption and decryption can be parallelized, making it faster on multi-core hardware.
   CBC encryption is sequential (each block depends on the previous one's ciphertext).

4. Simpler to use correctly: With GCM, you get integrity for free. With CBC, you must manually add an HMAC
   and verify it in the correct order (encrypt-then-MAC), which is error-prone.

GCM requirements:
- Never reuse an IV (nonce) with the same key. A 12-byte random IV is recommended.
- The authentication tag should be 128 bits (full length).
- GCM is limited to 2^39 - 256 bits (~64 GB) per single encryption. For larger data, use chunking.

When CBC might still be appropriate:
- Legacy system compatibility
- When hardware AES-GCM support is unavailable (rare in modern systems)
- Streaming encryption where the total size is unknown (though chunked GCM works)
```

```java
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

public class AesGcmExample {
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // bits

    public byte[] encryptGcm(byte[] plaintext, SecretKey key) throws Exception {
        // Generate random IV (MUST be unique per encryption)
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ciphertext = cipher.doFinal(plaintext);

        // Prepend IV to ciphertext for storage/transmission
        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
        return result;
    }

    public byte[] decryptGcm(byte[] combined, SecretKey key) throws Exception {
        // Extract IV and ciphertext
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return cipher.doFinal(ciphertext); // Throws if tampered
    }
}
```

**Q3: How do digital signatures work and when would you use them?**

```text
A3: Digital signatures provide three security properties:
1. Authentication: Proves the signer's identity (only the private key holder can create the signature)
2. Integrity: Detects any modification to the signed data
3. Non-repudiation: The signer cannot deny having signed the data

How they work:
1. The signer computes a hash of the data (e.g., SHA-256)
2. The hash is encrypted with the signer's private key, producing the signature
3. The verifier decrypts the signature with the signer's public key to recover the hash
4. The verifier independently computes the hash of the data
5. If both hashes match, the signature is valid

Use cases:
- Code signing: Ensuring software updates come from the legitimate publisher
- Document signing: Legal contracts, PDF signatures
- JWT (JSON Web Tokens): Authentication tokens signed by the server
- TLS/SSL certificates: Verifying website identity
- API request signing: Ensuring API requests are authentic (e.g., AWS Signature V4)
- Blockchain: Transaction authorization

Java implementation:
- Use java.security.Signature with algorithms like SHA256withRSA or Ed25519
- For JWT, use libraries like JJWT or Nimbus JOSE+JWT
- Store private keys securely in KeyStore; distribute public keys freely
```

```java
import java.security.*;
import java.util.Base64;

public class DigitalSignatureExample {
    public String sign(String data, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    public boolean verify(String data, String signatureB64, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes("UTF-8"));
        return sig.verify(Base64.getDecoder().decode(signatureB64));
    }

    // Example usage
    public void demo() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();

        String document = "Contract: Alice pays Bob $100";
        String signature = sign(document, keyPair.getPrivate());

        boolean valid = verify(document, signature, keyPair.getPublic());
        System.out.println("Signature valid: " + valid); // true

        boolean tampered = verify("Contract: Alice pays Bob $1000", signature, keyPair.getPublic());
        System.out.println("Tampered valid: " + tampered); // false
    }
}
```

**Q4: What is HMAC and how is it different from a plain hash?**

```text
A4: HMAC (Hash-based Message Authentication Code) combines a cryptographic hash function with a secret key to
provide both integrity and authentication.

Plain hash (SHA-256):
- Anyone can compute the hash of any data
- Verifies integrity: "Has the data been modified?"
- Does NOT verify authenticity: "Did the expected sender produce this?"
- Vulnerable to length extension attacks (for SHA-256, though not SHA-3)

HMAC-SHA256:
- Requires a secret key to compute
- Verifies both integrity AND authenticity
- Only someone with the secret key can produce a valid HMAC
- Not vulnerable to length extension attacks
- Formula: HMAC(K, m) = H((K' XOR opad) || H((K' XOR ipad) || m))

When to use each:
- Use plain hash for: checksums, file integrity, content deduplication, data fingerprinting
- Use HMAC for: API authentication, cookie signing, message authentication between parties who share a secret,
  verifying data authenticity in addition to integrity

Real-world examples:
- AWS API requests are authenticated with HMAC-SHA256
- JWT HS256 tokens use HMAC-SHA256 for signing
- Webhook payloads are often verified with HMAC
```

```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

public class HmacVsHash {
    // Plain hash: anyone can compute
    public String sha256(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes("UTF-8"));
        return bytesToHex(hash);
    }

    // HMAC: requires secret key
    public String hmacSha256(String data, String secretKey) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        mac.init(keySpec);
        byte[] hmac = mac.doFinal(data.getBytes("UTF-8"));
        return bytesToHex(hmac);
    }

    // Verification: constant-time comparison to prevent timing attacks
    public boolean verifyHmac(String data, String key, String expectedHmac) throws Exception {
        String computed = hmacSha256(data, key);
        return MessageDigest.isEqual(
            computed.getBytes("UTF-8"),
            expectedHmac.getBytes("UTF-8")
        );
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
```

**Q5: How do you securely generate random numbers in Java?**

```text
A5: Java provides several random number generators, but for security-sensitive operations, you MUST use
java.security.SecureRandom.

Why not java.util.Random or Math.random()?
- They use a PRNG (Pseudo-Random Number Generator) with a predictable seed
- If an attacker knows or guesses the seed, they can predict ALL future outputs
- Random uses a 48-bit seed with a linear congruential generator -- trivially breakable

SecureRandom guarantees:
- Cryptographically strong random numbers
- Seeded from the operating system's entropy source (/dev/urandom on Linux, CryptGenRandom on Windows)
- Output is computationally indistinguishable from true random numbers

When to use SecureRandom:
- Generating encryption keys and IVs/nonces
- Creating session IDs and authentication tokens
- CSRF tokens
- Password reset tokens
- Salt for password hashing
- Any value that an attacker must not be able to predict

Java 17+ recommendation:
- Use SecureRandom.getInstanceStrong() for highest security (may block until enough entropy is available)
- Use new SecureRandom() for general-purpose secure randomness (non-blocking)
- For specific algorithms: SecureRandom.getInstance("DRBG") (Java 9+)

Performance note: SecureRandom is slower than Random. For non-security contexts (e.g., shuffling a list for
display, generating test data), Random is fine. Only use SecureRandom when security matters.
```

```java
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

public class SecureRandomExamples {
    // INSECURE: predictable
    public String insecureToken() {
        return String.valueOf(Math.random()); // NEVER use for security
    }

    // SECURE: cryptographically strong
    public String secureToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    // Generate a secure session ID
    public String generateSessionId() {
        byte[] id = new byte[32]; // 256 bits of randomness
        new SecureRandom().nextBytes(id);
        return HexFormat.of().formatHex(id);
    }

    // Generate a secure AES key IV
    public byte[] generateIv(int lengthBytes) {
        byte[] iv = new byte[lengthBytes];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // Note: UUID.randomUUID() uses SecureRandom internally, so it's suitable for tokens
    // but produces only 122 bits of randomness (Type 4 UUID)
    public String uuidToken() {
        return UUID.randomUUID().toString();
    }
}
```

**Q6: Explain hybrid encryption and why it is used in practice.**

```text
A6: Hybrid encryption combines symmetric and asymmetric encryption to get the best of both worlds:
- Asymmetric encryption's key distribution advantage (no shared secret needed)
- Symmetric encryption's performance advantage (fast bulk encryption)

How it works:
1. The sender generates a random symmetric key (called a "session key" or "data encryption key")
2. The sender encrypts the actual data with the symmetric key (AES-GCM)
3. The sender encrypts the symmetric key with the recipient's RSA public key
4. Both the encrypted key and the encrypted data are sent to the recipient
5. The recipient decrypts the symmetric key with their RSA private key
6. The recipient decrypts the data with the recovered symmetric key

Why this approach:
- RSA can only encrypt small amounts of data (limited by key size minus padding)
- RSA is 100-1000x slower than AES for the same amount of data
- AES alone requires secure key exchange (chicken-and-egg problem)
- Hybrid encryption solves both issues: RSA handles key exchange, AES handles bulk data

Real-world usage:
- TLS/SSL: The TLS handshake uses asymmetric crypto to establish a shared symmetric key
- PGP/GPG email encryption
- SSH key exchange
- Encrypted messaging (Signal protocol)
```

```java
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.*;
import java.util.Base64;

public class HybridEncryption {
    public record EncryptedPackage(String encryptedKey, String encryptedData) {}

    // Sender: encrypt data for a recipient
    public EncryptedPackage encrypt(String plaintext, PublicKey recipientPublicKey) throws Exception {
        // 1. Generate random AES session key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey sessionKey = keyGen.generateKey();

        // 2. Encrypt data with AES-GCM
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey, new GCMParameterSpec(128, iv));
        byte[] encData = aesCipher.doFinal(plaintext.getBytes("UTF-8"));

        // Combine IV + ciphertext
        byte[] combined = new byte[iv.length + encData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encData, 0, combined, iv.length, encData.length);

        // 3. Encrypt session key with RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey);
        byte[] encKey = rsaCipher.doFinal(sessionKey.getEncoded());

        return new EncryptedPackage(
            Base64.getEncoder().encodeToString(encKey),
            Base64.getEncoder().encodeToString(combined)
        );
    }
}
```

## Code Examples

- Source: [CryptographyExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/javasecurity/CryptographyExamples.java)
- Test: [CryptographyExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javasecurity/CryptographyExamplesTest.java)
