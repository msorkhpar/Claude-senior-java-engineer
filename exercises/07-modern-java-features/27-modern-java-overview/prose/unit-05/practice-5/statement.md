A chat service shows a warning when a nickname contains a picture (a face, a heart, a rocket), and
prints each nickname over a dashed underline in its plain-text logs. Support has two reports:
`agent_007` gets the warning, while a nickname ending in a rocket doesn't, and that nickname's
underline comes out one dash too long.

Finish `hasPicture(nickname)` and `underline(nickname)` in
`src/main/java/practice/Nicknames.java`. Letters of any script, digits, `#`, `*`, `_` and `-` are
not pictures. Count a picture as one character, as the screen shows it; the tests use only
pictures that are a single Unicode character. About two lines in total.
