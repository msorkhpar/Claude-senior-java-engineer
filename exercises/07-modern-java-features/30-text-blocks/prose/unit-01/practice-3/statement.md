The page's first example is a small JSON object built from traditional
literals joined with `+`. Each source line needs its own `\n`, and the page's
pitfall shows what a missing one does: `"SELECT *" + "FROM users"` becomes
`SELECT *FROM users`.

Write `UserJson.of(String name, int age)`. It returns exactly these four lines,
joined by `\n`, with no newline after the last one:

```text
{
    "name": "<name>",
    "age": <age>
}
```

The inner lines are indented by four spaces. `name` is written as a JSON
string, so a double quote inside it is written `\"` and a backslash `\\`.

| name, age | answer (as it prints) |
|---|---|
| `Alice`, `30` | `{` / `    "name": "Alice",` / `    "age": 30` / `}` |
| `She said "Hi"`, `7` | the name line is `    "name": "She said \"Hi\"",` |
| `C:\temp`, `1` | the name line is `    "name": "C:\\temp",` |

(`/` separates the lines here; the answer joins them with `\n`.)
