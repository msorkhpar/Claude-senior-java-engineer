RSA is slow and **can only encrypt data shorter than its key**; AES is fast
but needs a shared key. The page's hybrid encryption uses both: a fresh AES
session key encrypts the message with AES-GCM, and the recipient's RSA public
key encrypts only that session key, with **OAEP padding**
(`RSA/ECB/OAEPWithSHA-256AndMGF1Padding`). **Every message gets its own
session key.**

Write `Hybrid`:

- `seal(plaintext, recipient, random)` returns `Sealed(encryptedKey, data)`:
  `encryptedKey` is a new 256-bit AES key encrypted with RSA-OAEP under
  `recipient`; `data` is `IV (12 bytes) + ciphertext + tag` of `plaintext`
  under that key, with `AES/GCM/NoPadding` and a 128-bit tag;
- `open(sealed, privateKey)` reverses it.

| message | result |
|---|---|
| `"Contract: Alice pays Bob $100"` | `open(seal(m))` = `m` |
| 10 000 bytes | `open(seal(m))` = `m` |
| any, sealed twice | two different session keys |
