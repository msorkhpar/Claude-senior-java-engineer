A constructor is the one place every object passes through, so it is where
invalid input is stopped: check the arguments and throw an
`IllegalArgumentException` before any field is set. An object that exists is then
always valid.

Write `Member`:

- `Member(String name, int age)`, and the getters `getName()` and `getAge()`.
- The name must not be `null`, empty, or only whitespace.
- The age must be between `0` and `150`, both included.
- Any other input is refused with `IllegalArgumentException`, whose message says
  what was wrong (for example `Name cannot be null or empty`, or
  `Age must be between 0 and 150`).

**Examples**

```
new Member("Grace", 85)  -> Grace, 85
new Member("   ", 30)    -> IllegalArgumentException
new Member(null, 30)     -> IllegalArgumentException
new Member("Tim", 150)   -> Tim, 150
new Member("Tim", 151)   -> IllegalArgumentException
```
