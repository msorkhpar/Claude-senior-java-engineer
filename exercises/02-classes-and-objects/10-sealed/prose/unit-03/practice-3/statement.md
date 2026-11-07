The monitoring service stores each alert as a tree of rules: `Above(metric, threshold)`, `And`,
`Or`, `Not`, and `Const(true)` or `Const(false)`. The rule editor produces constants whenever
someone switches a condition off, so stored rules are full of `And(Const(true), …)` and
`Not(Not(…))`, and the on-call engineers can no longer read them. `fires` already evaluates a rule.

Finish `simplify(Rule)` in `src/main/java/practice/AlertRules.java`. The result must fire for
exactly the readings the input fires for, with no `Const` left beside an `And` or an `Or`, no `Not`
of a `Const` and no `Not` directly inside a `Not`, at any depth of the tree. A rule that ends up
always or never firing becomes a single `Const`. About fifteen lines in total.

The tests build their own rules over their own metrics.
