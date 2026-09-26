The JVM is a **stack machine**: loads push values onto the operand stack, an
arithmetic instruction pops its operands and pushes the result, and a store
pops a value into a local variable. For `isub` and `idiv` the **right operand
is on top**, so it is popped first.

Write `StackMachine.run(program, args)`. `program` is a list of instructions
as `javap -c` prints them; `args` fill local variables `0, 1, 2, ...` (there
are 16 locals, all starting at 0). Support:

| instruction | effect |
|---|---|
| `iload_<n>`, `iload <n>` | push local `n` |
| `istore_<n>`, `istore <n>` | pop into local `n` |
| `iconst_<n>` (`n` 0 to 5), `bipush <v>` | push the constant |
| `iadd`, `isub`, `imul`, `idiv` | pop two, push the result as Java `int` arithmetic does |
| `iinc <n> <d>` | add `d` to local `n`; the stack is untouched |
| `ireturn` | pop and return the top value |

`int` arithmetic **wraps** in 32 bits, and `idiv` **truncates toward zero**
(dividing by zero throws `ArithmeticException`). An instruction that finds
**too few values** on the stack throws `IllegalStateException`, as the
verifier would reject it.

| program | args | result |
|---|---|---|
| `iload_0, iload_1, iadd, ireturn` | `5, 3` | `8` |
| `iload_0, iload_0, imul, iconst_2, iload_0, imul, iadd, iconst_1, iadd, ireturn` | `3` | `16` |
| `iload_0, iload_1, isub, ireturn` | `10, 3` | `7` |
| `iload_0, iconst_1, iadd, ireturn` | `Integer.MAX_VALUE` | `Integer.MIN_VALUE` |
| `iload_0, iinc 0 1, istore_0, iload_0, ireturn` (`i = i++`) | `5` | `5` |
| `iload_0, iadd, ireturn` | `5` | `IllegalStateException` |
