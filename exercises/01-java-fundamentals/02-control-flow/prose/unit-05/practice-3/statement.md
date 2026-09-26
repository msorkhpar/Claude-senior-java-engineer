When an arm of a switch expression needs several statements, it becomes a block, and the
block gives the arm's value with `yield`:

```java
case ERROR -> {
    sendAlert();
    yield -1;
}
```

Write `code(String status, List<String> alerts)` in `Status` as one switch expression. The
status is matched in any case:

- `"success"` gives `1`;
- `"error"` adds one alert, `"alert: "` followed by the status as given, to `alerts`, and
  gives `-1`;
- any other status gives `0` and raises no alert.

Examples: `code("SUCCESS", alerts)` is `1`; `code("Error", alerts)` is `-1` and leaves
`alerts` holding `"alert: Error"`; `code("pending", alerts)` is `0`.
