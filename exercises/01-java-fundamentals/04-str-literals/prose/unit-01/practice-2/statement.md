A String can be built from a byte array: `new String(bytes)` decodes the bytes into
characters. Bytes are not characters: in UTF-8 an ASCII letter takes one byte, but `é`
takes two (`0xC3 0xA9`) and `€` takes three. Name the charset explicitly, so the result does
not depend on the machine's default.

Write `decode(byte[] utf8)` in `Decoder`. It returns the text the UTF-8 bytes encode:

- `decode(new byte[]{72, 101, 108, 108, 111})` is `"Hello"`, the page's own example;
- `decode(new byte[]{104, (byte) 0xC3, (byte) 0xA9})` is `"hé"`;
- `decode(new byte[]{})` is `""`.
