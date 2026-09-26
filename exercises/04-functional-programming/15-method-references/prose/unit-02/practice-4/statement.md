A bound reference fixes the **receiver**, and each tested value becomes the
**argument**. So `target::contains` asks "does `target` contain this value?". The page
warns that the question you usually want, "does this value contain `target`?", has the
receiver and argument the other way round, and needs a lambda.

Write three methods in `Mentions`, each keeping the order of its input:

1. `mentioning(String term, List<String> sources)` keeps the sources that contain `term`,
   matching case exactly.
2. `prefixesOf(String text, List<String> candidates)` keeps the candidates that `text`
   starts with, as `String.startsWith` decides (so `""` is a prefix of every text).
3. `allowed(List<String> requested, List<String> allowList)` keeps the requested values
   that are in `allowList`, repeats included.

| call | result |
|---|---|
| `mentioning("searchTerm", ["find searchTerm here", "nothing", "also searchTerm"])` | `["find searchTerm here", "also searchTerm"]` |
| `prefixesOf("javascript", ["java", "script", "j"])` | `["java", "j"]` |
| `allowed(["a", "x"], ["a", "b"])` | `["a"]` |

Two of these fit a bound reference; decide for each which object is the receiver.
