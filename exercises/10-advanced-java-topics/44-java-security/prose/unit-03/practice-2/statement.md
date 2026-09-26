The page's Q4: a plain SHA-256 hash proves only that data was not changed,
since anyone can compute it. **HMAC-SHA256 mixes in a secret key**, so a
valid tag also proves the sender holds the key. Webhooks are signed this
way. The receiver recomputes the tag and compares it with
`MessageDigest.isEqual`, in constant time; **any signature that is not
exactly the tag fails**, a shortened or malformed one included, and never
with an exception.

Write `Webhook`:

- `sign(byte[] key, String body)`: HMAC-SHA256 of `body`'s UTF-8 bytes under
  `key`, as 64 lower-case hex digits;
- `verify(byte[] key, String body, String signature)`: `true` only when
  `signature` is exactly `sign(key, body)`.

| key | body | result |
|---|---|---|
| `Jefe` | `what do ya want for nothing?` | `sign` = `5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843` (RFC 4231) |
| `Jefe` | the same body, that signature | `verify` = `true` |
| `Jefe` | the same body, the first 32 digits of it | `false` |
| `Jefe` | the same body, that signature in upper case | `false` |
| `Jefe` | the same body, `"not-hex!"` | `false` |
| another key | the same body, the `Jefe` signature | `false` |
