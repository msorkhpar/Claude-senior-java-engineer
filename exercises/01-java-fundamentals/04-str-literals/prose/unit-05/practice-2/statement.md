A String cannot change, so replacing characters one by one would build a new String each
time. A `StringBuilder` changes a character in place with `setCharAt(index, c)`.

Write `mask(String card)` in `CardMask`. It replaces every digit with `*` except the last
four digits, and leaves every other character, such as a space or a dash, where it is:

- `mask("1234567812345678")` is `"************5678"`;
- `mask("1234 5678 9012 3456")` is `"**** **** **** 3456"`;
- a number of four digits or fewer is not masked: `mask("123")` is `"123"`.
