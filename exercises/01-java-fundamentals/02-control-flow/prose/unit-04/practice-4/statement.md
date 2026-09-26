A `switch` on a `String` compares with `equals()`, so the match is case-sensitive. A
`null` selector throws `NullPointerException`, so it is handled before the switch (or,
since Java 21, with a `case null` label).

A warehouse labels fruit with two-letter codes, in capitals. Write `name(String code)` in
`Codes`:

| code | name |
|---|---|
| `"AP"` | `"apple"` |
| `"BN"` | `"banana"` |
| `"CH"` | `"cherry"` |
| any other text | `"unknown"` |
| `null` | `"missing"` |

Codes are exact: `"ap"` is not a code, so `name("ap")` is `"unknown"`. A code read or built
at run time is a code too: `name(new String("AP"))` is `"apple"`.
