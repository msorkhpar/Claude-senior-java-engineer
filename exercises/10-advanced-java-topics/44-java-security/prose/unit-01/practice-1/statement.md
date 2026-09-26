The page's rule is **validate with an allow-list**: say exactly what is
allowed and refuse everything else, rather than hunting for bad patterns.
Unicode input is first **normalized with NFKC**, so that different encodings of
the same text (full-width letters, for example) are validated as one form.
Normalization does not remove homoglyphs, so the allow-list itself stays
**ASCII only**.

Write `SignupForm` with two methods; both refuse bad input (including `null`)
with `IllegalArgumentException`:

- `username(raw)`: normalizes `raw` with NFKC, then accepts it only if **the
  whole input** is 3 to 20 ASCII letters or digits, and returns the
  **normalized** username.
- `age(raw)`: accepts only ASCII digits whose value is 0 to 150, and returns
  the number.

| call | result |
|---|---|
| `username("alice99")` | `"alice99"` |
| `username("ａｌｉｃｅ")` (full-width) | `"alice"` |
| `username("bob!")`, `username("ab")` | `IllegalArgumentException` |
| `username("аdmin")` (Cyrillic `а`) | `IllegalArgumentException` |
| `age("42")`, `age("150")` | `42`, `150` |
| `age("151")`, `age("+42")`, `age("abc")` | `IllegalArgumentException` |
