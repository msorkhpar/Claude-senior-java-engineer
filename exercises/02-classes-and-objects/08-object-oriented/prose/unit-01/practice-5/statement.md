Java classes have one superclass, so the diamond problem cannot arise for
state. Interfaces bring it back for **behaviour**: since Java 8 a class that
inherits the same default method from two interfaces **must override it**, and
the override may call either one with `InterfaceName.super.method()`.

In `Birds`, write:

- `Swimmer.move()`, a default method: `"<name> swims"`.
- `Flyer.move()`, a default method: `"<name> flies"`.
- `Duck.move()`: a duck is both, so it answers
  `"<Swimmer's move> and <Flyer's move>"`, built from the two defaults.

`Penguin` (a `Swimmer`) and `Bat` (a `Flyer`) are given, and neither
overrides `move()`. Each interface's `name()` is implemented by the records.

## Examples

```
new Duck("Donald").move()           -> "Donald swims and Donald flies"
new Penguin("Pingu").move()         -> "Pingu swims"
new Bat("Bruce").move()             -> "Bruce flies"
```
