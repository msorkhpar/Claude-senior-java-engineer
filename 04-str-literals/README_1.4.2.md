# 1.4.2 String Concatenation in Java

The nightly stock export writes one CSV line per SKU:

```java
String csv = "";
for (Item it : items) {
    csv += it.sku() + "," + it.qty() + "\n";
}
```

With 25,000 SKUs it finished in 0.4 seconds. The catalogue has grown to 100,000, and the same
loop now takes about 4 seconds and allocates 69 GB to produce a 1.4 MB file. In review, someone
proposes replacing every `+` in the codebase with a `StringBuilder`. Only the `+=` on that line is
the problem.

## One `+` Expression Is One Concatenation, Compiled to a Single Call

A method that builds one CSV line, and what `javap -c` shows for it:

```java
static final String SEP = ",";

static String line(String sku, int qty, double price) {
    return sku + SEP + qty + SEP + price + "\n";
}
```

```text
  static java.lang.String line(java.lang.String, int, double);
    Code:
       0: aload_0
       1: iload_1
       2: dload_2
       3: invokedynamic #7,  0              // InvokeDynamic #0:makeConcatWithConstants:(Ljava/lang/String;ID)Ljava/lang/String;
       8: areturn
```

Since Java 9 (JEP 280), `javac` compiles a whole concatenation expression that is not a constant
to a single `invokedynamic` whose bootstrap method is `StringConcatFactory.makeConcatWithConstants`.
The constants travel in a recipe, here `\u0001,\u0001,\u0001\n`, where each character U+0001
stands for an argument; `SEP` is a `static final` constant, so its `,` was folded into the recipe.
The first time the instruction runs, the JDK links a method for that recipe. In JDK 21 that method
turns `float`, `double` and object operands into strings, computes the exact length, and copies
each part into the result once.

Rewriting one expression by hand gains nothing. We built a 40-character log line from a `String`,
an `int` and a `long`, ten million times each way:

```java
return "user=" + user + " order=" + orderId + " total=" + cents;

return new StringBuilder().append("user=").append(user).append(" order=").append(orderId)
        .append(" total=").append(cents).toString();
```

```text
+         87 bytes,  24 ns per call
builder  263 bytes,  44 ns per call
```

The builder starts with room for 16 characters, grows twice on the way to 40, then copies its
content into a new `String`. The bytes come from `com.sun.management.ThreadMXBean`'s
`getThreadAllocatedBytes` and were the same on every run; the times vary by machine and JDK.

> **Old bytecode differs.** Classes compiled with `--release 8` still contain the Java 8
> translation: `new StringBuilder()` and one `append` per operand. A library built for Java 8
> keeps it on any JVM.

## `+=` in a Loop Copies Everything Built So Far, on Every Pass

The export's loop body compiles to one call per row:

```text
      29: aload_1
      30: aload_3
      31: invokevirtual #27                 // Method Export$Item.sku:()Ljava/lang/String;
      34: aload_3
      35: invokevirtual #31                 // Method Export$Item.qty:()I
      38: invokedynamic #35,  0             // InvokeDynamic #0:makeConcatWithConstants:(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;
      43: astore_1
```

`csv += ...` is `csv = csv + (...)`, so the whole line is one expression, and its first argument
(`aload_1`) is `csv`: the file so far. Each pass creates a new `String` and copies every earlier
row into it. For n rows of about 14 Latin-1 characters, one byte each, that is 14 × n² / 2 bytes. We ran the export three
ways, after warm-up:

```text
+=          25,000 rows    345,000 chars     408 ms    4,313 MB allocated
builder     25,000 rows    345,000 chars       4 ms        1 MB allocated
joining     25,000 rows    345,000 chars       3 ms        2 MB allocated
+=          50,000 rows    690,000 chars   1,026 ms   17,252 MB allocated
builder     50,000 rows    690,000 chars       5 ms        3 MB allocated
joining     50,000 rows    690,000 chars       2 ms        4 MB allocated
+=         100,000 rows  1,380,000 chars   4,069 ms   69,004 MB allocated
builder    100,000 rows  1,380,000 chars       3 ms        6 MB allocated
joining    100,000 rows  1,380,000 chars       2 ms        9 MB allocated
```

Doubling the rows multiplies the `+=` allocation by four and the time by 2.5 to 4; the JIT does
not turn the loop into a buffer. The 69 GB is garbage the collector
clears while the rest of the service runs, so other requests slow down too. With a hundred rows in
the tests the loop allocates about 70 KB, which is why it ships.

## Repeated Text Goes into One Buffer, and a Joiner Places the Separators

The `builder` and `joining` rows above ran these, each with one buffer for the whole loop:

```java
StringBuilder csv = new StringBuilder();
for (Item it : items) {
    csv.append(it.sku()).append(',').append(it.qty()).append('\n');
}
return csv.toString();
```

```java
return items.stream()
        .map(it -> it.sku() + "," + it.qty() + "\n")
        .collect(Collectors.joining());
```

The `+` inside `map` is fine: one expression per row, with no accumulated text in it. Sizing a
`StringBuilder`, and when `StringBuffer` is the right choice, is unit 1.4.5.

A loop that appends a separator after every item leaves one too many; a joiner puts them only
between items:

```java
StringBuilder sb = new StringBuilder();
for (String sku : skus) sb.append(sku).append(", ");
System.out.println("[" + sb + "]");
System.out.println("[" + String.join(", ", skus) + "]");
System.out.println(skus.stream().collect(Collectors.joining(", ", "Low stock: ", ".")));
System.out.println(none.stream().collect(Collectors.joining(", ", "Low stock: ", ".")));
StringJoiner j = new StringJoiner(", ", "Low stock: ", ".").setEmptyValue("Stock is fine.");
none.forEach(j::add);
System.out.println(j);
```

```text
[A-1, B-7, C-3, ]
[A-1, B-7, C-3]
Low stock: A-1, B-7, C-3.
Low stock: .
Stock is fine.
```

The extra separator reaches whoever reads the text: a header value ending in `", "`, or, for an
importer that reads `\n` as a separator rather than a terminator, one more record, empty, at the
end of the file. A prefix and suffix are
written even when there are no items, so an empty list posts `Low stock: .` to the ops channel;
`StringJoiner.setEmptyValue` gives the empty case its own text.

<!-- practice 1 -->

## `+` Turns Every Operand into Text, and null Becomes the Word "null"

```java
String userId = null;
String[] roles = {"admin", "ops"};
char aisle = 'C', side = 'L';
System.out.println("cart:" + userId);
System.out.println("roles " + roles);
System.out.println(aisle + side + "-07");
System.out.println("" + aisle + side + "-07");
System.out.println("odd " + new Odd());          // Odd.toString() returns null
System.out.println("cart:".concat(userId));
```

```text
cart:null
roles [Ljava.lang.String;@677327b6
143-07
CL-07
odd null
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "String.isEmpty()" because "str" is null
	at java.base/java.lang.String.concat(String.java:2885)
	at ConversionPage.main(ConversionPage.java:14)
```

Once one operand is a `String`, `+` applies string conversion to each other operand. A `null`
reference, or a `toString()` that returns `null`, becomes the four letters `null`; any other
object becomes its `toString()`. Arrays don't override `toString()`, so they print their type and
a hash code. Use `Arrays.toString` or `String.join` for the elements. `+` never
throws on `null`, which is the risk: the cache key `"cart:" + userId` is `cart:null` for every
anonymous visitor, and they all share one cart.

`aisle + side` has no `String` in it. Two `char`s are promoted to `int` and added: `'C' + 'L'` is
67 + 76 = 143, and only then does `"-07"` turn the sum into text. `sb.append(aisle + side)`
appends `143` the same way. Starting with `""`, or `String.valueOf(aisle)`, keeps every part text.
Unit 1.1.4 covers the left-to-right grouping behind this.

`concat` is the strict alternative: `"cart:".concat(null)` throws `NullPointerException`, and so
does calling `concat` on `null`. Use it where a missing part is a bug that should fail at once,
and handle `null` explicitly where it is a valid value.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **One `+` expression is one concatenation.** Since Java 9 it compiles to a single
  `invokedynamic` that sizes the result exactly; the same line written as a `StringBuilder` chain
  allocated three times as much.
- **`+=` in a loop copies everything built so far on every pass.** Allocation grows with the
  square of the row count: 4.3 GB for 25,000 rows, 69 GB for 100,000.
- **Repeated text goes into one buffer, and a joiner places the separators.** `StringBuilder`,
  `String.join`, `Collectors.joining` and `StringJoiner` are linear, and a joiner puts nothing
  after the last item.
- **`+` converts every other operand to text.** `null` becomes `"null"`, an array its type and
  hash, `char + char` is still arithmetic, and `concat(null)` throws.

## Reference

**Results (Java 21):**

| Expression | Result |
|---|---|
| `"cart:" + s`, `String s = null` | `cart:null` |
| `s + s`, `String s = null` | `nullnull` |
| `"cart:".concat(null)` | `NullPointerException` |
| `'C' + 'L' + "-07"` | `143-07` |
| `"" + 'C' + 'L' + "-07"` | `CL-07` |
| `"pin " + pin`, `char[] pin` | `pin [C@…`; `println(pin)` prints the characters |
| `s += 1 + 2`, `s = "x"` | `x3`: the right side is evaluated first |
| `s = s + 1 + 2`, `s = "x"` | `x12` |
| `(a + "7") == "ORD-7"`, `final String a = "ORD-"` | `true`: `a` is a constant, so the sum is folded (unit 1.1.2) |
| the same with `a` not `final` | `false` |
| `s + "" == s` | `false`: a `+` that is not a constant expression creates a new `String` |
| `s.concat("") == s` | `true`: `concat` returns `this` for an empty argument |

**Building text:**

| Tool | Use it for | Empty input gives |
|---|---|---|
| `a + b + c` | one expression | n/a |
| `StringBuilder` | a loop that appends | `""` |
| `String.join(sep, items)` | items already in an array or `Iterable` | `""` |
| `Collectors.joining(sep, prefix, suffix)` | the end of a stream | `prefix + suffix` |
| `StringJoiner` | a loop that adds items | `setEmptyValue`, else `prefix + suffix` |
| `"%s/%05d".formatted("B-7", 42)` | fixed layouts and padding: `B-7/00042` | n/a |

## Challenges

1. Find a `+=` on a `String` inside a loop in a codebase you know. How many iterations does it see
   in production, and how much does it allocate at that count?
2. `log.debug("order " + order)` builds its text even when debug is off. Measure the cost per call
   when `Order.toString()` prints twenty fields, and compare it with your logging library's `{}`
   placeholder form.
3. A cache key is `tenant + ":" + userId`, and either may be `null`. Which keys can collide, and how
   would you make a missing value impossible to confuse with the text `"null"`?

## Design Note: Why `+` Compiles to a Call the JVM Links at Run Time

Before Java 9, `javac` fixed the strategy in every class file: a `StringBuilder` chain, which only
the JIT could improve by recognising its shape. Anything better meant changing the compiler and
recompiling every library. With `invokedynamic`, the class file records only the recipe and the argument types, and
the JDK supplies the code when the call site is linked, so a later JDK can bring a faster strategy
to class files compiled years earlier.

The price is paid once per call site: the first execution of each concatenation links a method
for its recipe, which adds a little to startup.

## Code Examples

- Test: [StringConcatenationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/literals/StringConcatenationTest.java)
- Source: [StringConcatenation.java](src/main/java/com/github/msorkhpar/claudejavatutor/literals/StringConcatenation.java)
