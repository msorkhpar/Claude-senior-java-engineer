`Constructor.newInstance(args...)` can call any constructor, private ones too
after `setAccessible(true)`, where the deprecated `Class.newInstance()` only
calls the no-arg one. `getConstructors()` lists public constructors only;
**`getDeclaredConstructors()`** lists them all. `newInstance` wraps whatever
the constructor throws in `InvocationTargetException`.

Write `Maker.make(type, args...)`. It picks the constructor of `type` whose
parameters fit `args`, at any access level, and calls it:

- A constructor fits when it has as many parameters as there are arguments
  and each argument is an instance of its parameter type. A **primitive
  parameter** takes its wrapper: an `Integer` fits `int`.
- When the constructor throws, the caller gets **that exception** if it is
  unchecked, and an `IllegalStateException` with it as the cause otherwise.
- No fitting constructor is a `NoSuchMethodException`.

With `Member() / Member(String) / Member(int) / Member(Collection<String>) /
private Member(String, int)`:

| call | `name` of the result |
|---|---|
| `make(Member.class)` | `"default"` |
| `make(Member.class, "Alice")` | `"Alice"` |
| `make(Member.class, 7)` | `"level-7"` |
| `make(Member.class, new ArrayList<>(List.of("a", "b")))` | `"tags-2"` |
| `make(Member.class, "Bob", 42)` | `"Bob#42"` |
| `make(Member.class, " ")` | `IllegalArgumentException` from the constructor |
