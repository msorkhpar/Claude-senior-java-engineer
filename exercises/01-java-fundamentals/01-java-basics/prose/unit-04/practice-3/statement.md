A compound assignment hides a narrowing cast: `byte b = 10; b += 300;` compiles
and leaves `b == 54`, because `b += 300` means `b = (byte) (b + 300)`. The
arithmetic itself happens in `int`: `a + b` on two `byte`s is an `int`.

Write `SaturatingBytes.addSaturating(byte a, byte b)`. It returns `a + b` when the
sum fits in a `byte`, and otherwise holds it at the nearest limit: `127` above,
`-128` below (a *saturating* add, as audio and image code use).

| a | b | answer |
|---|---|---|
| `10` | `20` | `30` |
| `100` | `100` | `127` |
| `-100` | `-100` | `-128` |
