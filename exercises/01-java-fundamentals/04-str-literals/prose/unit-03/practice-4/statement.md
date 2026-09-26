`substring(0, n)` needs a String of at least `n` characters, so a method that shortens text
first checks whether there is anything to shorten.

Write `abbreviate(String text, int max)` in `Abbreviation`. It returns `text` unchanged when
it has at most `max` characters, and otherwise its first `max - 3` characters followed by
`"..."`, so the result is exactly `max` long:

- `abbreviate("Hello, World", 8)` is `"Hello..."`;
- `abbreviate("Hi", 8)` is `"Hi"`, and `abbreviate("", 5)` is `""`;
- a text of exactly `max` characters is kept: `abbreviate("Exactly8", 8)` is `"Exactly8"`;
- `max` below `3` leaves no room for the dots: throw `IllegalArgumentException`.
