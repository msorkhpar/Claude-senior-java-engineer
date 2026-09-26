When the same Strings are compared again and again, normalise each one once, for example
trimmed and lower-cased, and compare the normalised forms. A `HashSet` of normalised words
then counts the different words for you.

Write `count(List<String> words)` in `Distinct`. It returns how many different words the
list holds, where two words are the same when they differ only in case or in spaces around
them:

- `count(List.of("apple", "pear", "apple"))` is `2`;
- `count(List.of("Apple", "APPLE", "apple"))` is `1`;
- `count(List.of(" pear", "pear "))` is `1`;
- `count(List.of())` is `0`.
