A String can be built from a char array, `new String(chars)`, or from part of one:
`new String(chars, offset, count)` takes `count` characters starting at `offset`. The third
argument is a count, not an end index.

Write `word(char[] buffer, int start, int end)` in `Buffer`. It returns the characters from
index `start` up to, but not including, `end`:

- with `buffer = {'H', 'e', 'l', 'l', 'o', ' ', 'y', 'o', 'u'}`, `word(buffer, 0, 5)` is
  `"Hello"` and `word(buffer, 6, 9)` is `"you"`;
- `word(buffer, 3, 3)` is `""`;
- the String is a copy: changing `buffer` afterwards does not change a word already taken.
