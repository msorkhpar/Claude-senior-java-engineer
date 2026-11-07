A code-review bot explains suspicious arithmetic by running the bytecode javac produced for a
method and printing what it returns, so a reviewer can see why `total = total++ + bonus` doesn't
do what it looks like. Its interpreter, `IntMachine`, reads instructions as `javap -c` prints them.
Loads, stores and constants already work; two pieces don't.

Finish the two `TODO`s in `src/main/java/practice/IntMachine.java`:

- `increment(local, delta)`: what the `iinc` instruction does;
- `arithmetic(op)`: what `iadd`, `isub`, `imul` and `idiv` do.

When you're done, every method the tests feed it returns exactly what the JVM returned when it ran
the same method. About eight lines in total.

The tests hold each program next to the Java method javac compiled it from, and compare the
two results.
