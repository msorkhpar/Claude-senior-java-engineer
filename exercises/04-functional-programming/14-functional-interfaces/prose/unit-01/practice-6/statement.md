`Consumer.accept` declares no checked exceptions, so `list.forEach(path -> Files.delete(path))` does not compile.
The fix is to catch the checked exception inside the lambda and rethrow it unchecked.

`Unchecked.IoAction<T>` is given: it is a consumer whose `accept` may throw `IOException`. Write
`Unchecked.unchecked(IoAction<T> action)`, which returns a plain `Consumer<T>` that runs `action`. If `action`
throws an `IOException`, the consumer throws an `UncheckedIOException` wrapping it.

| action on `"a"` | `unchecked(action).accept("a")` |
|---|---|
| records `a` | records `a` |
| throws `IOException("disk")` | throws `UncheckedIOException` |

Keep the original exception reachable, leave exceptions that are already unchecked alone, and do not hide a failure
from the caller.
