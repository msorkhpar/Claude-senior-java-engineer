The page's best practices: use **initialisation blocks** for logic common to all
constructors, and **chain** constructors with `this(...)`. In a chain of
constructors, an instance initialiser (like a field initialiser) runs **once**, just
before the body of the constructor that does not start with `this(...)`. It runs
after `super()` and before that constructor's own statements.

Write `Gadget`. Every gadget keeps a log of its construction, `steps()`, a
`List<String>`:

- `Gadget()`: name `unnamed`, colour `grey`;
- `Gadget(String name)`: colour `grey`;
- `Gadget(String name, String colour)`: records the step `named:<name>`;
- `getName()`, `getColour()`, `steps()`.

Whichever constructor is used, the step `setup` is recorded **exactly once**, and
**before** the `named:` step. Put the work shared by every constructor where it
runs once for any constructor.

**Examples**

```
new Gadget().steps()               -> ["setup", "named:unnamed"]
new Gadget("lamp").getColour()     -> "grey"
new Gadget("lamp", "red").steps()  -> ["setup", "named:lamp"]
```
