Pattern matching for `instanceof` lets an `if` test a type and bind a variable in one step,
and the variable can be used in the rest of the condition:

```java
if (obj instanceof String s && s.length() > 5) {
    System.out.println(s.toUpperCase());
}
```

Write `shout(Object obj)` in `Shout`. It returns:

- the text in upper case, when `obj` is a `String` longer than 5 characters;
- the text unchanged, when `obj` is a `String` of 5 characters or fewer;
- `""` for anything else, including `null`.

Examples: `shout("welcome")` is `"WELCOME"`, `shout("hello")` is `"hello"`,
`shout(42)` is `""`, and `shout(null)` is `""`.
