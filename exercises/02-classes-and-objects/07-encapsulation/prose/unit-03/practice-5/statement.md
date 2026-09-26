The page's edge case: **reflection can bypass access control.** A `private` field
stops the compiler, but `java.lang.reflect` can still read it. That is why the page
calls `private` protection against mistakes, not a security wall, and why ordinary
code should not reach in this way.

Write `Peek.read(Object target, String fieldName)`. It returns the current value of
the field called `fieldName` in `target`, whatever the field's access modifier.

- `Class.getDeclaredField` finds only the fields a class **itself** declares; a
  field may belong to a superclass, so look up the whole class hierarchy.
- When no class in the hierarchy declares the field, throw
  `IllegalArgumentException`.

Examples:

```
class Account { private double balance = 1000.0; }
read(new Account(), "balance")         -> 1000.0

class Savings extends Account { private double rate = 0.02; }
read(new Savings(), "balance")         -> 1000.0
read(new Savings(), "owner")           -> IllegalArgumentException
```
