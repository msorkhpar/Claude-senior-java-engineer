The page's Q5 stacks an encryption layer and a compression layer on one data
source. Each layer changes the data on `write` and undoes the change on
`read`, and the outermost layer is applied first on the way in, so **the order
of the layers decides what is stored**.

Given: `DataSource` (`read()`, `write(data)`) and `InMemoryDataSource`. Write
two decorators:

- `EncryptionDecorator(wrappee, shift)`: writes each character shifted up by
  `shift`, and shifts back down on read.
- `CompressionDecorator(wrappee)`: run-length encoding. A run of `n > 1` equal
  characters is written as the character followed by `n` in decimal, and
  **a run of one is written without a count**. On read, **a run length of
  several digits is read whole** (`a12` is twelve a's). Because counts are
  digits, **data containing digits is refused** with `IllegalArgumentException`
  and nothing is written.

Both layers keep no copy of the data: **`read` asks the wrapped source every
time**.

| stack (shift 3) | `write("aaabbcc")` stores | `read()` |
|---|---|---|
| `Compression(Encryption(store))` | `d6e5f5` | `aaabbcc` |
| `Encryption(Compression(store))` | `d3e2f2` | `aaabbcc` |
| `Compression(store)`, `write("abc")` | `abc` | `abc` |
| `Compression(store)`, `write("a2")` | nothing, `IllegalArgumentException` | - |
