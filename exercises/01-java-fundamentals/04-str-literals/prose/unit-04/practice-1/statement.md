`==` asks whether two variables refer to the same object; `equals()` asks whether two
Strings hold the same characters. Two equal literals happen to share one object, so `==`
can look right in a quick test, and then fail for a String read or built at run time.

Write `accepts(String expected, String typed)` in `Login`. It says whether the typed text is
exactly the expected one. Neither argument is `null`.

- `accepts("s3cret", "s3cret")` is `true`, and `accepts("s3cret", "secret")` is `false`;
- `accepts("s3cret", new String("s3cret"))` is `true`, and so is a typed text assembled
  character by character.
