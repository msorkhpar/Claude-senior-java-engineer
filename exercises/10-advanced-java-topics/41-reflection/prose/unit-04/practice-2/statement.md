The checkout service prices every order line through rules named in configuration: a rule is any
object with a public `int apply(int cents)`, registered at startup with `add(name, rule)`. Since
the rules were added, the allocation profile of a busy checkout node shows `java.lang.Integer` as
its top allocated type, with the stacks ending in the rule table, and young-generation GCs have
doubled.

Finish the `TODO` in `src/main/java/practice/RuleTable.java`: `prepare(rule)` turns one rule
object into the `IntUnaryOperator` that `apply(name, cents)` calls for it.

When you're done, every name applies its own rule object, an object without `int apply(int)` is
refused when it is added, and applying a rule allocates nothing. About ten lines in total.

The tests measure allocation with the thread's allocation counter, over amounts well outside the
`Integer` cache.
