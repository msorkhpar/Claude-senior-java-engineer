`String.equals` returns at the first character that differs, so the time it
takes tells an attacker **where** the first mismatch is, and a token can be
guessed one character at a time. The page's constant-time comparison **never
returns at the first differing character**: it combines the differences of
all characters (`result |= a ^ b`) and decides only at the end. Returning
early on different **lengths** is acceptable, since token lengths are public.

Write `Tokens.matches(CharSequence expected, CharSequence given)`:

- `true` exactly when both hold the same characters;
- when the lengths are equal, it reads **every** character of `given`,
  wherever the first mismatch is;
- a `null` `given` never matches (`expected` is never `null`).

| expected | given | result |
|---|---|---|
| `tok-4f9a2c` | `tok-4f9a2c` | `true` |
| `tok-4f9a2c` | `xok-4f9a2c` | `false`, after reading all 10 characters of `given` |
| `tok-4f9a2c` | `tok-4f9a2` | `false` |
| `tok-4f9a2c` | `null` | `false` |
