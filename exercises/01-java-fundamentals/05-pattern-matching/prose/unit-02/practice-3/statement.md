A pattern variable is in scope wherever the match is certain. After a negated test that
leaves the method, that is the whole rest of the method:

```java
if (!(obj instanceof String s)) {
    return ...;
}
// s is in scope here
```

Write `of(Object obj)` in `Initial`. It returns the first character of a String, in upper
case, and `'?'` when there is no initial to give:

- `of("hello")` is `'H'`, and `of("Ada")` is `'A'`;
- `of("")` is `'?'`: an empty String has no first character;
- `of(42)` and `of(null)` are `'?'`.
