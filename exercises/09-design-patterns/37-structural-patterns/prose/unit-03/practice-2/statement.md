The page's protection proxy guards a document service by role, and the
client cannot tell it from the real service: it has the same
`DocumentService` interface.

| role | read | write | delete |
|---|---|---|---|
| `GUEST` | yes | no | no |
| `USER` | yes | yes | no |
| `ADMIN` | yes | yes | yes |

Given: `DocumentService` (`read`, `write`, `delete`, each returning a `String`),
`Role` and `User(name, role)`. Write `ProtectionDocumentProxy(real, user)`:

- An allowed call delegates to the real service and returns its answer.
- A call the role may not make throws `SecurityException`, and **a refused call
  never reaches the real service**. **Only `ADMIN` may delete.**
- The proxy decides access, nothing else: **the real service's own exceptions
  pass through unchanged** (the page: let `NoSuchElementException` propagate
  rather than hide it).
- `null` for either constructor argument throws `NullPointerException`.

| user | call | result |
|---|---|---|
| `guest`, `GUEST` | `read("doc1")` | the real service's text |
| `guest`, `GUEST` | `write("doc1", "x")` | `SecurityException`, real service untouched |
| `bob`, `USER` | `write("doc1", "x")` | the real service's answer |
| `bob`, `USER` | `delete("doc1")` | `SecurityException` |
| `root`, `ADMIN` | `delete("doc1")` | the real service's answer |
| anyone | `read("nope")` | `NoSuchElementException` from the real service |
