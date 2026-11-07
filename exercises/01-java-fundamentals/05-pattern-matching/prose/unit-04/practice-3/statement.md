Each service reads its settings from a configuration file, and a parser turns the file into a map
from key to value. `Settings` keeps what the admin page shows:

| Parsed value | Shown as |
|---|---|
| a `String` | the string itself |
| an `Integer` | its digits |
| a `Boolean` | `yes` or `no` |
| a `List` | its elements, separated by `, ` |

Operators often leave a key in the file with no value, such as `retries:`, to say "use the
built-in default", and the parser gives that key the value `null`. Such keys belong in `unset()`,
not in `values()`. Any other type means the file is wrong, and the load must stop with an
`IllegalArgumentException` that names the key: last quarter a `ratio: 0.75` went unnoticed for a
week because the service treated it as missing.

Write `put(...)` in `src/main/java/practice/Settings.java`. About seven lines in total.
