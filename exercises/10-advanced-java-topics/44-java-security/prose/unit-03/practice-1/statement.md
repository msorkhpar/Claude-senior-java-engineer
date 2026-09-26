The page expects you to write a SHA-256 hash "from scratch using
`MessageDigest`" at a whiteboard. A SHA-256 digest is 32 bytes, shown as 64
lower-case hex digits: **every byte is two digits**, `0x01` is `01`, and **a
leading zero byte is kept**. Text is turned into bytes with
`StandardCharsets.UTF_8`. For large content the page says to call
`MessageDigest.update()` incrementally rather than loading it all: **the
whole stream is hashed**, however the stream hands out its bytes.

Write `Digests`:

- `sha256Hex(String text)`: the digest of `text`'s UTF-8 bytes, in hex;
- `sha256Hex(InputStream in)`: the digest of every byte of `in`, read in
  chunks until the end.

| input | `sha256Hex` |
|---|---|
| `"Contract: Alice pays Bob $100"` | `3f1474663ea161aa9567e07884bba2edb564c2bd63a44ab38fd0e959bdbcc2f5` |
| `"abc"` | `ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad` |
| `"file-417"` | `004fe204e259e7d7a7c69613839d6ce32f03b6743bbcf99e939e606472c51411` |
| a stream of `"abc"` that returns one byte per `read` | the same digest as `"abc"` |
