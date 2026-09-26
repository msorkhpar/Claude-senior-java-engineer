`@Inherited` lets a subclass "see" its superclass's annotation, with limits
the page spells out: it works **only through superclasses, never from an
interface**; **only the nearest** annotated superclass counts; a class that
declares the annotation itself shadows its parents'; and an annotation that is
not `@Inherited` is **never inherited**.

`Auditable` (`@Inherited`, `level()` default `"INFO"`) and `Author` (not
inherited) are given. Write, following exactly the rule the JVM applies:

- `describe(Class<?> type)`: `"<level> declared"` when `type` itself carries
  `@Auditable`, `"<level> from <SimpleName>"` when it inherits it from a
  superclass, and `"none"` otherwise.
- `authorOf(Class<?> type)`: the `@Author` value as seen on `type`, or empty.

| class | `describe` | `authorOf` |
|---|---|---|
| `@Auditable(level = "DEBUG") @Author("Jane") ParentService` | `DEBUG declared` | `Optional[Jane]` |
| `ChildService extends ParentService` | `DEBUG from ParentService` | `Optional.empty` |
| `@Auditable(level = "TRACE") LoudChild extends ParentService` | `TRACE declared` | `Optional.empty` |
| `Grandchild extends ChildService` | `DEBUG from ParentService` | `Optional.empty` |
| `Impl implements Tracked`, `Tracked` is `@Auditable` | `none` | `Optional.empty` |
