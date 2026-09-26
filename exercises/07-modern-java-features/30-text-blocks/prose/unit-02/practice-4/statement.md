The page says text blocks are interned in the string pool, like literals: a
literal and a text block with the same characters are the **same object**. A
string built while the program runs is an equal but separate object, and the
course's own `textBlockIsString()` shows how to reach the pooled one:
`regular.intern()`.

Write `Canonical.of(String text)`. It returns the one instance that stands for
`text`'s characters: for text equal to a compile-time constant such as the
starter's `GREETING` text block, that constant itself; for any other text, the
pooled instance, the one a literal with those characters would be.

| call | answer |
|---|---|
| `of("Bye")` (a literal) | that same literal object |
| `of(new StringBuilder("Hello").append("\nWorld").toString())` | the very object `GREETING` |
| `of(a)` and `of(b)`, with `a` and `b` built at run time equal to `"order-40002"` | the pooled object, the one the literal `"order-40002"` is, both times |
