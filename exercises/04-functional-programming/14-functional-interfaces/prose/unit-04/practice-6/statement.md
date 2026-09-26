`BiPredicate<T, U>` tests two values together: `boolean test(T t, U u)`. It composes with `and()`, `or()` and
`negate()`, like `Predicate`.

Write two methods in `Matcher`:

1. `startsWithAndLongerThan(int minLength)` returns a `BiPredicate<String, String>` that, for `(word, prefix)`, is
   true when the word is longer than `minLength` **and** starts with `prefix`. Two BiPredicates joined with `and()` do
   this neatly.
2. `matching(List<String> words, String prefix, BiPredicate<String, String> rule)` returns the words for which
   `rule.test(word, prefix)` is true, in order, repeats included. The prefix test is case-sensitive.

| words | prefix | rule | result |
|---|---|---|---|
| `["hi", "hello", "java", "javafx"]` | `"ja"` | `startsWithAndLongerThan(3)` | `["java", "javafx"]` |
| `["hi", "hello"]` | `"x"` | `startsWithAndLongerThan(3)` | `[]` |

Check the page's example with prefix `"he"` against the words `hi, hello, hey, java, javafx`. A list may contain
`null`: it is skipped before any rule sees it, so no rule is ever handed `null` and a `null` is never in the result.
