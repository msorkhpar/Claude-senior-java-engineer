HotSpot's JIT is steered by `-XX` flags: `-XX:+Name` turns a boolean on,
**`-XX:-Name` turns it off**, and `-XX:Name=value` sets a number. When a flag
appears more than once, **the last one wins**.

Write `JitFlags.parse(args)` for these flags, ignoring every other argument:

| flag | kind | default |
|---|---|---|
| `TieredCompilation` | boolean | on |
| `TieredStopAtLevel` | number, **0 to 4** | 4 |
| `MaxInlineSize` | number | 35 |
| `FreqInlineSize` | number | 325 |
| `PrintCompilation` | boolean | off |
| `PrintInlining` | boolean, diagnostic | off |
| `UnlockDiagnosticVMOptions` | boolean | off |

The result answers `tiered()`, `topTier()`, `maxInlineSize()`,
`freqInlineSize()`, `printCompilation()` and `printInlining()`. `topTier()` is
the stop level when tiered compilation is on, and 4 (C2 alone) when it is off.

A stop level outside 0 to 4 is refused with `IllegalArgumentException`, and so
is `PrintInlining` turned on **without `-XX:+UnlockDiagnosticVMOptions`**.

| args | result |
|---|---|
| `-Xmx4g` | tiered, top tier 4, inline sizes 35 and 325 |
| `-XX:TieredStopAtLevel=1` | tiered, top tier 1 (C1 only) |
| `-XX:-TieredCompilation` | not tiered, top tier 4 |
| `-XX:TieredStopAtLevel=1 -XX:-TieredCompilation` | not tiered, top tier 4 |
| `-XX:MaxInlineSize=50 -XX:MaxInlineSize=20` | max inline size 20 |
| `-XX:TieredStopAtLevel=5` | `IllegalArgumentException` |
| `-XX:+PrintInlining` | `IllegalArgumentException` |
| `-XX:-UnlockDiagnosticVMOptions -XX:+PrintInlining` | `IllegalArgumentException` |
| `-XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining` | prints inlining |
