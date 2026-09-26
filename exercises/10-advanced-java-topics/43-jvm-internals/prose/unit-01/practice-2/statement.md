Class loading has three phases: **loading** finds the bytes and creates the
`Class`, **linking** verifies and prepares it, and **initialization** runs the
static initializers. The failures look alike but mean different things:

- `ClassNotFoundException` (checked): a dynamic lookup such as
  `Class.forName` found no such class.
- `ExceptionInInitializerError`: the class's static initializer threw, the
  first time the class was initialised.
- `NoClassDefFoundError`: every later use of that class, which the JVM has
  marked unusable.

Write `ClassProbe` with the nested enum `Outcome { LOADED, NOT_FOUND,
INIT_FAILED, UNUSABLE }`:

- `initialise(name, loader)` loads **and initialises** the class through
  `loader` and returns the outcome; it never throws for these three failures.
- `isPresent(name, loader)` says whether the class can be loaded, **without
  initialising it**.

| call | result |
|---|---|
| `initialise("java.lang.String", loader)` | `LOADED` |
| `initialise("com.example.DoesNotExist", loader)` | `NOT_FOUND` |
| `initialise(broken, loader)` (static block throws) | `INIT_FAILED` |
| `initialise(broken, loader)` again | `UNUSABLE` |
| `isPresent(broken, loader)` on a fresh broken class | `true`, and its static block has not run |
