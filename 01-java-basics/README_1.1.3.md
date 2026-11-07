# 1.1.3 Type Casting and Conversion

In one week the file service stored a 4 GiB upload as a 100-byte file, paid a 4.35 refund as 4.34,
answered "no such account" for an account that exists, and couldn't read back a price file that
its Frankfurt servers had written. Each bug is a conversion that compiled, and none of them threw
where the value went wrong.

## A Narrowing Integer Cast Keeps the Low Bits and Never Fails

The upload's length arrives as a `long`, and the legacy storage API takes an `int`:

```java
long uploaded = 4_294_967_396L;              // 4 GiB and 100 bytes
int length = (int) uploaded;
System.out.println("length = " + length);
System.out.println("(int) 3_000_000_000L = " + (int) 3_000_000_000L);
```

```text
length = 100
(int) 3_000_000_000L = -1294967296
```

A `long`-to-`int` cast keeps the low 32 bits. Nothing is checked, so nothing throws. A negative
result is more likely to fail somewhere later; 100 is a plausible length, and the service stored the first 100 bytes as the whole file.

A compound assignment hides the same cast. `total += x` means `total = (int) (total + x)`, so
this compiles and wraps:

```java
int total = 0;
total += 3_000_000_000L;                     // compiles
System.out.println("total = " + total);
```

```text
total = -1294967296
```

`total = total + 3_000_000_000L` would not compile. Where a value must fit, convert it with
`Math.toIntExact(uploaded)`, which throws instead:

```text
java.lang.ArithmeticException: integer overflow
```

<!-- practice 1 -->

## Casting a double to int or long Truncates Toward Zero, Clamps at the Range, and Turns NaN Into 0

The refund service converts euros to cents:

```java
double refund = 4.35;
System.out.println("refund * 100             = " + refund * 100);
System.out.println("(int) (refund * 100)     = " + (int) (refund * 100));
System.out.println("Math.round(refund * 100) = " + Math.round(refund * 100));
```

```text
refund * 100             = 434.99999999999994
(int) (refund * 100)     = 434
Math.round(refund * 100) = 435
```

4.35 is not exact in binary (unit 1.1.1), and the product lands just below 435. The cast drops
the fraction, so the customer gets 4.34. `Math.round` returns the nearest whole number, as a
`long` for a `double`.

This cast doesn't wrap the way integer casts do. It truncates toward zero, clamps values outside
the range to `MIN_VALUE` or `MAX_VALUE`, and turns `NaN` into 0:

```text
(int) -2.7 = -2, (int) 1e10 = 2147483647, (int) Double.NaN = 0
Math.round(-2.5) = -2, (long) (-4.35 * 100 + 0.5) = -434
```

A feed that sends `NaN` for an amount produces a refund of 0 cents, with no error, and
`Math.round(Double.NaN)` is 0 as well.

Rounding by adding 0.5 and casting works only for positive values: a reversal of -4.35 becomes -434 cents. `Math.round` breaks ties toward positive infinity,
so -2.5 rounds to -2.

<!-- practice 2 -->

## Widening to float or double Is Implicit and Rounds Large Values

Java converts `int` and `long` to `float` and `double` without a cast, because the range fits.
The precision may not: a `float` has 24 significant bits, so an `int` above 2^24 can round, and a
`double` has 53, so a `long` above 2^53 can.

```java
long orderId = 9_007_199_254_740_993L;
double asDouble = orderId;                   // no cast needed
float asFloat = 16_777_217;
System.out.println(orderId + " -> " + (long) asDouble);
System.out.println("16_777_217 as float = " + asFloat);
Object shown = true ? 1 : 2.0;
System.out.println("true ? 1 : 2.0 = " + shown);
```

```text
9007199254740993 -> 9007199254740992
16_777_217 as float = 1.6777216E7
true ? 1 : 2.0 = 1.0
```

An order id above 2^53 that passes through a `double`, in a metrics library or a JavaScript
client, can come back as a different order. The last line widens implicitly: when one branch of
`?:` is an `int` and the other a `double`, the result is a `double`, on either branch.

## A Wrapper Converts Only to Its Own Primitive: an Integer Is Never a Long

`int` widens to `long`, but `Integer` and `Long` are unrelated classes. Accounts are kept by a
`Long` id, and the id arrives as an `int`:

```java
Map<Long, String> owners = new HashMap<>();
owners.put(42L, "Jane Doe");
System.out.println("get(42) = " + owners.get(42) + ", get(42L) = " + owners.get(42L));
Object id = 42;                              // a JSON decoder returns Integer for a small number
Long asLong = (Long) id;
```

```text
get(42) = null, get(42L) = Jane Doe
java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.Long (java.lang.Integer and java.lang.Long are in module java.base of loader 'bootstrap')
```

`get` takes an `Object`, as do `containsKey`, `remove` and a set's `contains`, so `get(42)`
compiles. It boxes 42 to an `Integer`, which never equals a `Long`. A reference cast converts
nothing: it checks the object's class and throws `ClassCastException` on a mismatch. JSON decoders
that read into a `Map<String, Object>` often return an `Integer` for a small number and a `Long`
for a large one, so `(Long) id` passes tests that use large ids and fails in production on small
ones. `((Number) id).longValue()` accepts both.

<!-- practice 3 -->

## Text Conversions: parseInt Rejects Spaces, parseDouble Accepts NaN, and String.format Uses the Default Locale

The price export writes `String.format("%.2f", price)`, and the import reads the file back with
`Double.parseDouble`. On the Frankfurt servers:

```java
Locale.setDefault(Locale.GERMANY);           // the Frankfurt servers
String line = String.format("%.2f", 1.5);
System.out.println(line);
Double.parseDouble(line);
```

```text
1,50
java.lang.NumberFormatException: For input string: "1,50"
```

`String.format` without a `Locale` uses the JVM's default format locale, taken from the operating
system at startup unless `-Duser.language` and `-Duser.country` override it.
`parseDouble` and `String.valueOf` never do. `String.format(Locale.ROOT, "%.2f", 1.5)` prints
`1.50` on every machine.

`Integer.parseInt(" 42")` throws, while `Double.parseDouble`
trims spaces and accepts `"NaN"`, `"Infinity"` and `"1e3"`. An import that validates prices with
`parseDouble` lets `NaN` in, and every later `<`, `>` or `==` with it is `false` (unit 1.1.1). Check
`Double.isFinite` after parsing.

<!-- practice 4 -->

## Summary

- **A narrowing integer cast keeps the low bits:** an out-of-range value turns into another
  valid-looking number, and `+=` casts for you. Use `Math.toIntExact` where it must fit.
- **A cast from `double` truncates toward zero, clamps, and maps `NaN` to 0:** round with
  `Math.round`, and reject `NaN` yourself.
- **Widening to `float` or `double` needs no cast and can round** above 2^24 (`float`) or 2^53
  (`double`).
- **Wrappers don't widen:** an `Integer` key never finds a `Long` one, and `(Long)` on an
  `Integer` throws. Go through `Number`.
- **Text conversions differ:** `String.format` follows the default locale, `parseDouble`
  accepts `NaN`, and `parseInt` rejects spaces.

## Reference

**What a cast does to a value out of range (Java 21, JLS 5.1.2, 5.1.3 and 15.26.2):**

| Conversion | Result |
|---|---|
| `long` to `int`, `int` to `short`/`byte`/`char` | low-order bits kept: `(byte) 200` is -56, `(char) -1` is 65535 |
| `double`/`float` to `int` or `long` | truncated toward zero; clamped to `MIN_VALUE`/`MAX_VALUE`; `NaN` is 0 |
| `double` to `byte`/`short`/`char` | to `int` first, then low bits: `(byte) 300.0` is 44 |
| `int`/`long` to `float`, `long` to `double` | implicit, rounded to nearest: `(float) 123456789` is 1.2345679E8 |
| compound assignment `a op= b` | `a = (type of a) (a op b)`: `int x = 10; x += 3.7;` gives 13 |

**What the parse methods accept (Java 21):**

| Input | `Integer.parseInt` | `Double.parseDouble` |
|---|---|---|
| `" 42"` | `NumberFormatException` | 42.0 |
| `"+42"` | 42 | 42.0 |
| `"1_000"` | `NumberFormatException` | `NumberFormatException` |
| `"2147483648"` | `NumberFormatException` | 2.147483648E9 |
| `"NaN"`, `"Infinity"` | `NumberFormatException` | `NaN`, `Infinity` |
| `"12.5f"` | `NumberFormatException` | 12.5 |
| `"1,50"` | `NumberFormatException` | `NumberFormatException` |

## Challenges

1. Find a `(int)` cast on a `long` in your code base. What is the largest value that can reach it,
   and what happens one above it?
2. A payment API sends amounts as JSON numbers. Decide whether to read them as `double`,
   `BigDecimal` or `long` minor units, and what each choice costs at the edges.
3. Your services run in containers whose default locale nobody set. Which of your outputs change
   if one of them starts with `de_DE`?

## Design Note: Why a Cast Never Throws

The JLS defines the result of every narrowing primitive conversion, down to `NaN` and values out
of range, and states that it never causes a run-time exception. A check would cost a branch on
every cast, so checking is opt-in: `Math.toIntExact` and the other `*Exact` methods. The cost
falls on review: once the cast is written, the compiler no longer reports the lossy conversion,
and a compound assignment never reports it at all.

## Code Examples:

- Test: [TypeCastingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javabasics/TypeCastingTest.java)
- Source: [TypeCasting.java](src/main/java/com/github/msorkhpar/claudejavatutor/javabasics/TypeCasting.java)