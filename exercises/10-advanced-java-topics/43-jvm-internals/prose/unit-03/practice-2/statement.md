Inlining is the JIT's most important optimisation, and HotSpot decides it with
simple rules the page lists:

- a method of **at most `MaxInlineSize`** bytes of bytecode (35 by default)
  is inlined at any call site;
- at a **hot** call site the limit is **`FreqInlineSize`** (325 by default);
- a call site that has seen **three or more receiver types** is megamorphic:
  the JIT gives up devirtualization, so the call is not inlined. One type
  (monomorphic) or two (bimorphic) are fine.

Write `InlinePolicy` with `new InlinePolicy(maxInlineSize, freqInlineSize)`,
`InlinePolicy.defaults()` (35 and 325), and
`decide(bytecodeSize, hot, receiverTypes)` returning the nested enum
`Decision { INLINE, TOO_BIG, MEGAMORPHIC }`. Check the receiver types first.

| call site | bytes | hot | receiver types | decision |
|---|---|---|---|---|
| `smallMethod` (`x + 1`) | 4 | no | 1 | `INLINE` |
| the page's `largeMethod` | 36 | yes | 1 | `INLINE` |
| the page's `largeMethod` | 36 | no | 1 | `TOO_BIG` |
| a 500-line method | 2000 | yes | 1 | `TOO_BIG` |
| `Shape s = getRandomShape(); s.draw()` | 20 | yes | 3 | `MEGAMORPHIC` |
