Java is always pass-by-value. For an object the value passed is the
**reference**, so a method and its caller point at the same heap object: the
method can change that object, but **reassigning the parameter does not reach
the caller**, it only changes the method's own copy of the reference.

Write two methods on `Names`:

- `trimAll(names)` trims every name **in the caller's list** (it returns
  nothing, so the change must happen in the list itself).
- `trimmedCopy(names)` returns a new list of the trimmed names; **the copy
  leaves the caller's list unchanged**.

| call | caller's list afterwards | returns |
|---|---|---|
| `trimAll(["  ada ", "bob"])` | `[ada, bob]` | |
| `trimmedCopy(["  ada ", "bob"])` | `[  ada , bob]` | `[ada, bob]` |
