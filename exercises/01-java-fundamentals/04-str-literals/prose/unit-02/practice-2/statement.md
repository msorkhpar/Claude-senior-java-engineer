`+` never fails on `null`: it inserts the four letters `"null"`, so `"Hello" + null` is
`"Hellonull"`. `"Hello".concat(null)` throws `NullPointerException` instead. Neither is
what a reader wants to see, so a value that may be `null` is checked before it is joined.

Write `address(String title, String name)` in `Greeting`. It returns the title and the name
separated by one space, leaving out whichever is `null`:

- `address("Dr", "Ada")` is `"Dr Ada"`;
- `address(null, "Ada")` is `"Ada"`, and `address("Dr", null)` is `"Dr"`;
- `address(null, null)` is `""`.
