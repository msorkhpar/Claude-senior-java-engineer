Java has four signed integer types, and each has a fixed size and a fixed range:
`byte` (8 bits), `short` (16), `int` (32) and `long` (64).

Write `NarrowestType.narrowestType(long value)`. It returns the name of the
**narrowest** of those four types that can hold `value` exactly: `"byte"`,
`"short"`, `"int"` or `"long"`.

| value | answer |
|---|---|
| `100` | `"byte"` |
| `1000` | `"short"` |
| `100000` | `"int"` |
| `10000000000L` | `"long"` |

Mind the ends of every range: both bounds belong to the type, and the negative
end of a two's-complement range reaches one further than the positive end.
The wrapper classes (`Byte`, `Short`, `Integer`, `Long`) name each bound.
