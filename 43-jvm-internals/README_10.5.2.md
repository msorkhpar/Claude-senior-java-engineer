# 10.5.2. Bytecode Execution and Interpretation

After a load test of 1,000 requests, the dashboard's request counter still shows 0. The line that
counts is `hits = hits++;`, and two reviewers read it as "add one". javac compiled it without a
warning, even with `-Xlint:all`.

javac produces bytecode, instructions for a stack machine. The JVM loads and verifies it
(unit 10.5.1), interprets it, and compiles hot methods to machine code (unit 10.5.3). `javap -c`
prints it.

## Bytecode Works on an Operand Stack, So `i = i++` Stores the Old Value Back

Before reading on, predict what these three methods return for 5:

```java
public class Increments {
    static int post(int i) {
        i = i++;
        return i;
    }

    static int pre(int i) {
        i = ++i;
        return i;
    }

    static int both(int i) {
        return i++ - ++i;
    }

    public static void main(String[] args) {
        int hits = 0;
        for (int request = 0; request < 1_000; request++) {
            hits = hits++;                       // meant to count requests
        }
        System.out.println("post(5) = " + post(5) + ", pre(5) = " + pre(5) + ", both(5) = " + both(5));
        System.out.println("hits after 1000 requests: " + hits);
    }
}
```

```text
post(5) = 5, pre(5) = 6, both(5) = -2
hits after 1000 requests: 0
```

`javac Increments.java && javap -c Increments` shows why:

```text
  static int post(int);
    Code:
       0: iload_0
       1: iinc          0, 1
       4: istore_0
       5: iload_0
       6: ireturn

  static int pre(int);
    Code:
       0: iinc          0, 1
       3: iload_0
       4: istore_0
       5: iload_0
       6: ireturn

  static int both(int);
    Code:
       0: iload_0
       1: iinc          0, 1
       4: iinc          0, 1
       7: iload_0
       8: isub
       9: ireturn
```

Each method call has an array of local variables (the parameter `i` is local 0 in a static method)
and an operand stack. `iload_0` pushes a copy of local 0, `istore_0` pops the top into local 0, and
`iadd`, `isub` and the other binary arithmetic instructions pop two values and push the result. `iinc 0, 1`
works differently: it adds 1 to local 0 in place and leaves the stack alone.

In `post`, `iload_0` pushes 5, `iinc` makes the local 6, and `istore_0` writes the 5 from the stack
back over it. The JLS defines it this way: the value of `i++` is the variable's value before the
increment, and the assignment stores that value after the increment has happened. `++i` increments
first, then loads, so `pre` stores 6. In `both`, the left operand is loaded first (5), two
increments make the local 7, the right operand loads 7, and `isub` subtracts the top of the stack
from the value under it: 5 − 7 = −2.

So `hits = hits++` stores the old value back on each of the 1,000 iterations, and the counter
stays at 0. As a loop's update, `for (...; i < n; i = i++)` runs forever. Write `hits++` on its own, and don't read a
variable again in the expression that increments it.

<!-- practice 1 -->

## invokedynamic Leaves the Choice of Code to the First Run

String concatenation (unit 1.4.2) and lambdas (unit 4.1.1) both compile to `invokedynamic`. This
program times the first three calls of one of each:

```java
import java.util.function.IntSupplier;

public class Linking {
    static String label(String host, int port) {
        return host + ":" + port;
    }

    static IntSupplier fixed() {
        return () -> 42;
    }

    static long timed(Runnable r) {
        long t0 = System.nanoTime();
        r.run();
        return (System.nanoTime() - t0) / 1_000;
    }

    public static void main(String[] args) {
        for (int call = 1; call <= 3; call++) {
            long concat = timed(() -> label("example.com", 8080));
            long lambda = timed(() -> fixed());
            System.out.printf("call %d: label %5d us, fixed() %5d us%n", call, concat, lambda);
        }
    }
}
```

```text
call 1: label  8564 us, fixed()   583 us
call 2: label    24 us, fixed()     1 us
call 3: label    15 us, fixed()     0 us
```

Each method centres on one `invokedynamic`:

```text
  static java.lang.String label(java.lang.String, int);
    Code:
       0: aload_0
       1: iload_1
       2: invokedynamic #7,  0              // InvokeDynamic #0:makeConcatWithConstants:(Ljava/lang/String;I)Ljava/lang/String;
       7: areturn
  static java.util.function.IntSupplier fixed();
    Code:
       0: invokedynamic #11,  0             // InvokeDynamic #1:getAsInt:()Ljava/util/function/IntSupplier;
       5: areturn
```

`#0` and `#1` index the class's `BootstrapMethods` table (`javap -v`, first two entries,
descriptors replaced by `...`):

```text
BootstrapMethods:
  0: #94 REF_invokeStatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(...)
    Method arguments:
      #81 \u0001:\u0001
  1: #100 REF_invokeStatic java/lang/invoke/LambdaMetafactory.metafactory:(...)
    Method arguments:
      #83 ()I
      #84 REF_invokeStatic Linking.lambda$fixed$0:()I
      #83 ()I
```

The class file holds no concatenation code and no `IntSupplier` class. It holds a description (the
recipe `\u0001:\u0001`, the method `lambda$fixed$0`) and the JDK method that builds the code. The first time an `invokedynamic` runs, the JVM calls that bootstrap
method, which generates the code and returns a call site. The JVM links the instruction to it, and
every later execution goes straight to the linked code. Each `Runnable` in `main` is linked before `timed`
starts its clock, so the 8.6 ms of the first `label` call is `StringConcatFactory` building its
code: with `-Xlog:class+load`, that call alone loaded 15 generated `LambdaForm$MH` classes.

javac can still emit the old form. With `-XDstringConcat=inline` (an internal javac option) or
`--release 8`, `label` compiles to a `StringBuilder` chain:

```text
       0: new           #7                  // class java/lang/StringBuilder
       3: dup
       4: invokespecial #9                  // Method java/lang/StringBuilder."<init>":()V
       7: aload_0
       8: invokevirtual #10                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      11: ldc           #14                 // String :
      13: invokevirtual #10                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      16: iload_1
      17: invokevirtual #16                 // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
      20: invokevirtual #19                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      23: areturn
```

```text
call 1: label    31 us, fixed()   734 us
call 2: label     5 us, fixed()     0 us
call 3: label     3 us, fixed()     0 us
```

The first concatenation is now cheap. The first `fixed()` call still pays for its own bootstrap,
which spins one hidden class. Over five runs of each on one busy machine, the two first calls
together took 8.9–15.7 ms with `invokedynamic` and 0.5–2.1 ms with `inline`. From the second call
on, both were in microseconds.

The cost is paid once per call site, at startup: a service with thousands of distinct
concatenations and lambdas spends part of its start in bootstraps. The code that concatenates
belongs to the JDK that runs the class (see the Design Note); a class compiled for Java 8 keeps
its `StringBuilder` chain on every JDK. The lambda body `lambda$fixed$0` is a synthetic method
(unit 10.3.1), and the linked target is a `MethodHandle` (unit 10.3.3).

## A switch on Strings Is a hashCode lookupswitch Followed by equals

`"Aa"` and `"BB"` have the same `hashCode()`, 2112, and so does `"C#"`. Which case does each reach?

```java
public class Routes {
    static int route(String region) {
        switch (region) {
            case "Aa": return 1;
            case "BB": return 2;
            case "eu": return 3;
            default:   return 0;
        }
    }

    public static void main(String[] args) {
        System.out.println("\"Aa\".hashCode() = " + "Aa".hashCode() + ", \"BB\".hashCode() = " + "BB".hashCode());
        for (String r : new String[] {"Aa", "BB", "eu", "C#"}) {
            System.out.println(r + " -> " + route(new String(r)) + " (hash " + r.hashCode() + ")");
        }
        try {
            route(null);
        } catch (NullPointerException e) {
            System.out.println("route(null): " + e);
        }
    }
}
```

```text
"Aa".hashCode() = 2112, "BB".hashCode() = 2112
Aa -> 1 (hash 2112)
BB -> 2 (hash 2112)
eu -> 3 (hash 3248)
C# -> 0 (hash 2112)
route(null): java.lang.NullPointerException: Cannot invoke "String.hashCode()" because "<local1>" is null
```

Every string reached the right case. The bytecode is two switches:

```text
       0: aload_0
       1: astore_1
       2: iconst_m1
       3: istore_2
       4: aload_1
       5: invokevirtual #7                  // Method java/lang/String.hashCode:()I
       8: lookupswitch  { // 2
                  2112: 36
                  3248: 64
               default: 75
          }
      36: aload_1
      37: ldc           #13                 // String BB
      39: invokevirtual #15                 // Method java/lang/String.equals:(Ljava/lang/Object;)Z
      42: ifeq          50
      45: iconst_1
      46: istore_2
      47: goto          75
      50: aload_1
      51: ldc           #19                 // String Aa
      53: invokevirtual #15                 // Method java/lang/String.equals:(Ljava/lang/Object;)Z
      56: ifeq          75
      59: iconst_0
      60: istore_2
      61: goto          75
      64: aload_1
      65: ldc           #21                 // String eu
      67: invokevirtual #15                 // Method java/lang/String.equals:(Ljava/lang/Object;)Z
      70: ifeq          75
      73: iconst_2
      74: istore_2
      75: iload_2
      76: tableswitch   { // 0 to 2
```

The JVM can only switch on an `int`. javac switches on the hash first, with a `lookupswitch`
because the hashes are sparse. Each hash leads to an `equals` for every label with that hash: 2112
tries `"BB"`, then `"Aa"`. A match stores the case's position in the source (0 for `"Aa"`, 1 for `"BB"`, 2 for `"eu"`;
local 2 starts at −1), and a
`tableswitch` on that dense index jumps to the case body. The hash only narrows the search and
`equals` decides, which is why `"C#"` falls to `default`. The `NullPointerException` comes from the
`hashCode()` call on javac's copy of the selector, `<local1>`. The program passes `new String(r)`
so that no selector is the same object as a label: `equals`, not identity, decides.

A hand-written dispatch keyed by `hashCode()` alone sends `"BB"` to `"Aa"`'s handler. If you bucket by hash, compare with `equals` inside the
bucket, as javac does.

> **`case null` compiles differently.** A Java 21 `switch` on a `String` with a `case null` label
> compiles to `invokedynamic` `SwitchBootstraps.typeSwitch` followed by a `tableswitch`, with no
> `hashCode()` call in the method, and `route(null)` returns the `case null` value.

<!-- practice 2 -->

## finally Is Copied Onto Every Exit, and a return There Replaces the athrow

There is no `finally` instruction. Unit 3.1.3 showed how `finally` behaves; javac builds it from
copies and an exception table:

```java
public class Finally {
    static int calls;

    static int parse(String s) {
        try {
            return Integer.parseInt(s);
        } finally {
            calls++;
        }
    }

    @SuppressWarnings("finally")
    static int parseOrSwallow(String s) {
        try {
            return Integer.parseInt(s);
        } finally {
            calls++;
            return -1;
        }
    }

    public static void main(String[] args) {
        System.out.println("parseOrSwallow(\"12\") = " + parseOrSwallow("12"));
        System.out.println("parseOrSwallow(\"x\") = " + parseOrSwallow("x"));
        try {
            parse("x");
        } catch (NumberFormatException e) {
            System.out.println("parse(\"x\") threw " + e);
        }
        System.out.println("calls = " + calls);
    }
}
```

```text
parseOrSwallow("12") = -1
parseOrSwallow("x") = -1
parse("x") threw java.lang.NumberFormatException: For input string: "x"
calls = 3
```

`parseOrSwallow` is listed from offset 13; its first lines are the same as `parse`'s.

```text
  static int parse(java.lang.String);
    Code:
       0: aload_0
       1: invokestatic  #7                  // Method java/lang/Integer.parseInt:(Ljava/lang/String;)I
       4: istore_1
       5: getstatic     #13                 // Field calls:I
       8: iconst_1
       9: iadd
      10: putstatic     #13                 // Field calls:I
      13: iload_1
      14: ireturn
      15: astore_2
      16: getstatic     #13                 // Field calls:I
      19: iconst_1
      20: iadd
      21: putstatic     #13                 // Field calls:I
      24: aload_2
      25: athrow
    Exception table:
       from    to  target type
           0     5    15   any

  static int parseOrSwallow(java.lang.String);
      13: iconst_m1
      14: ireturn
      15: astore_2
      16: getstatic     #13                 // Field calls:I
      19: iconst_1
      20: iadd
      21: putstatic     #13                 // Field calls:I
      24: iconst_m1
      25: ireturn
    Exception table:
       from    to  target type
           0     5    15   any
```

`calls++` appears twice in `parse`. The normal path (0–14) saves the result in local 1, runs its
copy of the `finally` code, and returns the saved value. The exception table sends any throwable
raised between offsets 0 and 5 to offset 15, a handler of type `any`: it saves the exception in
local 2, runs the second copy, and rethrows it with `athrow`. The range stops at 5, so an exception
thrown by a copy of the `finally` code isn't caught by the same handler and doesn't run it twice.

In `parseOrSwallow` the handler ends with `iconst_m1, ireturn`. The exception sits in local 2 and
no instruction reads it again. That is how a `return` in `finally` swallows an exception: javac
copied the `return` onto the exception path, and a path that returns has no `athrow`.

Copying also multiplies code. Counting the `audit` calls in `javap -c` for these two methods:

```java
public class Copies {
    static void audit(String what) { }

    static int status(int code) {
        try {
            if (code < 0) return -1;
            if (code == 0) return 0;
            if (code < 100) return 1;
            return 2;
        } finally {
            audit("status " + code);
        }
    }

    static int nested(int code) {
        try {
            try {
                if (code < 0) return -1;
                return 1;
            } finally {
                audit("inner " + code);
            }
        } finally {
            audit("outer " + code);
        }
    }
}
```

```text
status: audit calls 5, last offset       77: athrow
nested: audit calls 6, last offset       71: athrow
```

`status` has five copies: one per `return` and one in the `any` handler. In `nested`, each `finally`
is copied onto the two `return`s and into its own handler. A long `finally` in a method with many exits makes the method
several times longer, and that counts against HotSpot's inlining limits (`MaxInlineSize` 35 bytes,
`FreqInlineSize` 325 on this JDK; unit 10.5.3). Move a long cleanup into a method of its own, and
each copy becomes one call.

<!-- practice 3 -->

## Summary

- **Bytecode runs on an operand stack.** `i++` pushes the old value before `iinc` changes the
  local, so `i = i++` stores the old value back, and a counter written that way never moves.
- **`invokedynamic` defers the code to its first execution.** The bootstrap method
  (`StringConcatFactory`, `LambdaMetafactory`) runs once per call site, costing milliseconds at
  startup, and the JDK that runs the class, not the compiler, decides the concatenation code.
- **A `switch` on a `String` switches on `hashCode()`, then confirms with `equals`.** Colliding
  strings still reach their own case, and a `null` selector throws from `hashCode()` unless there
  is a `case null`.
- **`finally` is copied onto every exit, and a `return` in it replaces the rethrow.** The exception
  is stored in a local that nothing reads again, and the copies grow the method.

## Reference

**Instructions on this page (JVMS chapter 6):**

| Instruction | Effect |
|---|---|
| `iload_n`, `aload_n` | push local `n` (an `int`, a reference) |
| `istore_n`, `astore_n` | pop the top into local `n` |
| `iinc n, d` | add `d` to local `n`; the operand stack is unchanged |
| `iconst_m1` … `iconst_5` | push the constant −1 … 5 |
| `iadd`, `isub` | pop two `int`s, push the result; `isub` subtracts the top from the one below |
| `invokedynamic` | call the bootstrap method on first execution, then the linked call site |
| `lookupswitch` / `tableswitch` | jump on an `int` key: sparse keys matched / a dense range indexed |
| `athrow` | throw the reference on top of the stack |
| `ireturn`, `areturn` | return an `int`, a reference |

**What javac 21 emits:**

| Source | Bytecode |
|---|---|
| `x + ":" + y` (`--release` 9 or later) | `invokedynamic` `makeConcatWithConstants`, recipe as a static argument |
| the same with `--release 8` or `-XDstringConcat=inline` | `new StringBuilder`, `append` per operand, `toString` |
| a lambda | private synthetic `lambda$m$n` plus `invokedynamic` `LambdaMetafactory.metafactory` |
| `switch` on a `String` | `hashCode`, `lookupswitch`, `equals` per label in the bucket, `tableswitch` on an index |
| the same with `case null` | `invokedynamic` `SwitchBootstraps.typeSwitch`, `tableswitch` |
| `try`-`finally` | the `finally` code copied onto each exit, plus an `any` handler ending in `athrow` |

## Challenges

1. A service starts in 4 s and you suspect bootstraps. How would you count the `invokedynamic`
   call sites linked during startup, and what would you change if they turned out to matter?
2. Run `javap -c` on a `for` loop over an `int[]` and an enhanced `for` over the same array.
   Predict the difference first.
3. A code generator emits a method with 300 `return` statements inside one `try`-`finally`.
   Estimate the bytecode size, and decide where the cleanup should go instead.

## Design Note: Why the Bytecode Names a Bootstrap Instead of the Code

Before Java 9, javac decided how `+` concatenated, and every class it compiled carried that
decision until it was recompiled. A better strategy in a new JDK reached no existing library. With
`invokedynamic`, javac records only what is concatenated and leaves the how to a method in the
running JDK, so a JDK upgrade changes the concatenation code of every class at once. Lambdas got
the same freedom: their classes are spun at run time instead of shipped in the jar.

The cost is the one measured above: work javac used to do once at build time now happens, by default, at
every start, once per call site. `javap -c` no longer shows everything a method does either. For a
concatenation or a lambda, the code that runs exists only after the first call.

## Code Examples

- Test: [BytecodeExecutionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/jvminternals/BytecodeExecutionTest.java)
- Source: [BytecodeExecution.java](src/main/java/com/github/msorkhpar/claudejavatutor/jvminternals/BytecodeExecution.java)
