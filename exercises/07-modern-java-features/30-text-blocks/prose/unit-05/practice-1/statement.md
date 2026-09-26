The page's first use case is a JSON response body: a text block holds the
template, and `formatted()` fills it. The page also warns that `formatted()`
does no escaping of its own.

Write `ApiResponse.json(int status, String message, String data)`. It returns
these five lines joined by `\n`, with no newline after the last:

```text
{
    "status": <status>,
    "message": "<message>",
    "data": <data>
}
```

- `data` is already JSON (an array, an object, `null`) and goes in as it is.
- `message` is a JSON string: a backslash in it is written `\\` and a double
  quote `\"`. Escape the backslashes before the quotes.
- The values are **arguments** to `formatted()`, never part of the template,
  so a `%` in the message is printed as it is.

| status, message, data | the message line |
|---|---|
| `200`, `OK`, `[1, 2]` | `    "message": "OK",` |
| `400`, `She said "Hi"`, `null` | `    "message": "She said \"Hi\"",` |
| `500`, `C:\temp`, `null` | `    "message": "C:\\temp",` |
| `200`, `50% done`, `{}` | `    "message": "50% done",` |
