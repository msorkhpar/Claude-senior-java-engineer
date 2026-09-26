Records under a sealed interface, taken apart by record patterns, make an exhaustive switch:
when every permitted record has a case, no `default` is needed, and the compiler refuses the
switch if a new kind of expression is added and not handled. A `default` arm would hide
that missing case instead.

`Calc` holds `sealed interface Expr permits Num, Add, Sub, Mul, Neg` and its five records:
`Num(int value)`, `Add(Expr left, Expr right)`, `Sub(Expr left, Expr right)`,
`Mul(Expr left, Expr right)` and `Neg(Expr operand)`.

Write `eval(Expr expr)` in `Calc` as one switch over record patterns, with no `default`:

- `eval(new Num(7))` is `7`;
- `eval(new Add(new Num(2), new Mul(new Num(3), new Num(4))))` is `14`;
- `eval(new Sub(new Num(10), new Num(4)))` is `6`: left minus right;
- `eval(new Neg(new Num(5)))` is `-5`.
