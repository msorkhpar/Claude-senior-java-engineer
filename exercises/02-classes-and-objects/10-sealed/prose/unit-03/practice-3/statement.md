The page's answer on algebraic data types: a sealed interface is a **sum
type** (a value is exactly one of its variants), and each variant as a record
is a **product type** (it is made of its fields). `Calc` models arithmetic
expressions that way:

```text
sealed interface Expr permits Num, Add, Mul, Neg
record Num(long value)          record Add(Expr left, Expr right)
record Mul(Expr left, Expr right)   record Neg(Expr operand)
```

Write two functions over `Expr`, each one `switch` over the variants (record
patterns such as `case Add(Expr l, Expr r)` fit well):

1. `static long eval(Expr e)`: the expression's value.
2. `static String show(Expr e)`: the expression as text, with only the
   parentheses its meaning needs:
   - a `Num` prints its value;
   - an `Add` prints `left + right`, and its operands never need parentheses;
   - a `Mul` prints `left * right`, and an operand that is an `Add` is put in parentheses;
   - a `Neg` prints `-` then its operand, and an operand that is an `Add` or a
     `Mul` is put in parentheses.

**Examples**

- `Add(Num(1), Mul(Num(2), Num(3)))`: `eval` -> `7`, `show` -> `"1 + 2 * 3"`
- `Mul(Add(Num(1), Num(2)), Num(3))`: `eval` -> `9`, `show` -> `"(1 + 2) * 3"`
- `Neg(Num(4))`: `eval` -> `-4`, `show` -> `"-4"`
