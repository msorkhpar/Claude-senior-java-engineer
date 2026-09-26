The page lists which classes may extend which:

- a `sealed` class permits exactly the subclasses it names in `permits`, or,
  with no `permits` clause, exactly the subclasses declared in its own file;
  any other class trying to extend it fails with
  `class is not allowed to extend sealed class: <Name>`;
- a `final` class may not be extended at all (`cannot inherit from final <Name>`);
- a `non-sealed` class, like any plain class, may be extended by anyone.

Write `static String verdict(Class<?> parent, Class<?> candidate)` that
answers, from `parent`'s declaration read by reflection, whether the compiler
would accept `candidate` as a **direct** subclass of `parent`:

- return `null` when it would be accepted;
- return `"cannot inherit from final " + parent's simple name` when `parent` is final;
- return `"class is not allowed to extend sealed class: " + parent's simple name`
  when `parent` is sealed and does not permit `candidate`.

Only `parent`'s own declaration decides. `Class.isSealed()` and
`Class.getPermittedSubclasses()` see both explicit and implicit permits.

**Examples** (with `sealed class Shape permits Circle`, `final class Circle extends Shape`)

- `verdict(Shape.class, Circle.class)` -> `null`
- `verdict(Shape.class, Square.class)` -> `"class is not allowed to extend sealed class: Shape"`
- `verdict(Circle.class, Square.class)` -> `"cannot inherit from final Circle"`
