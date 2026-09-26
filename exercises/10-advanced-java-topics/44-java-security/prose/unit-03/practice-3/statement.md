The page's `AesGcmExample` encrypts with `AES/GCM/NoPadding`: a 12-byte IV,
a 128-bit authentication tag, and three rules. **Each encryption draws a
fresh random IV**: reusing an IV with the same key is catastrophic. The IV is
not secret, and **it travels in front of the ciphertext**, so any holder of
the key can decrypt later. And GCM is authenticated: **a tampered box is
refused** with an exception, never decrypted into garbage.

Write `AesBox(SecretKey key, SecureRandom random)`:

- `seal(plaintext)` returns `IV (12 bytes, from random) + ciphertext + tag`;
- `open(box)` returns the plaintext, and throws `AEADBadTagException` when
  the box was changed.

| call | result |
|---|---|
| `open(seal("Contract: Alice pays Bob $100"))` | the same bytes |
| `seal(m)` twice | two different boxes |
| `seal(m)` with an `m` of 29 bytes | 12 + 29 + 16 = 57 bytes; the first 12 are the IV |
| `open(box)` after one byte of `box` is flipped | `AEADBadTagException` |
