# 1.1.1 Primitive Data Types

In one week the usage service had three bugs, and none failed where the mistake was. The 30-day
retention window came out as -1,702,967,296 ms. The deploy gate passed a canary that had served
no requests. A nightly job died with a `NullPointerException` on a line with no object in sight.
All three come from the edges of the primitive types and their wrappers.

## Integer Arithmetic Wraps Without an Error, Even When the Result Goes Into a long

What does this print?

```java
int days = 30;
long windowMs = days * 24 * 60 * 60 * 1000;
long fixedMs = days * 24L * 60 * 60 * 1000;
System.out.println("windowMs = " + windowMs);
System.out.println("fixedMs  = " + fixedMs);
```

```text
windowMs = -1702967296
fixedMs  = 2592000000
```

An expression's type comes from its operands, not from the variable that receives it. Every
operand of the first product is an `int`, so it is computed in 32 bits: 2,592,000,000 is above
`Integer.MAX_VALUE`, the high bits are dropped, and the `long` receives the wrapped result. In the
second line `24L` makes every multiplication after it a `long` one. Widening after the overflow is
too late: in `days * 24 * 3600L`, `days * 24` is still an `int`.

Where a wrong number must not pass, `Math.multiplyExact` and `Math.addExact` throw instead. With
the same values:

```text
multiplyExact: java.lang.ArithmeticException: integer overflow
```

The range is asymmetric too: `int` reaches -2,147,483,648 but only +2,147,483,647, so
`Math.abs(Integer.MIN_VALUE)` is still negative. A router that picks a shard with
`Math.abs(key.hashCode()) % 10` works for every key except those whose hash is `MIN_VALUE`:

```text
Math.abs(hash) % 10     = -8
Math.floorMod(hash, 10) = 2
```

Index -8 throws `ArrayIndexOutOfBoundsException`, for those keys only. `Math.floorMod` takes the
divisor's sign, so the result stays in 0 to 9.

<!-- practice 1 -->

## A double Holds a Binary Fraction, So Compare Computed Values With a Tolerance That Fits Their Size

`0.1` has no exact binary form. The literal rounds to the nearest `double`, and each addition
rounds again, so the errors build up:

```java
double total = 0;
for (int i = 0; i < 10; i++) total += 0.1;
System.out.println("total = " + total + ", total == 1.0: " + (total == 1.0));
System.out.println("|total - 1.0| < 1e-9: " + (Math.abs(total - 1.0) < 1e-9));
```

```text
total = 0.9999999999999999, total == 1.0: false
|total - 1.0| < 1e-9: true
```

A fixed tolerance stops working as values grow. Near 1e9, adjacent doubles are 1.19e-7 apart
(`Math.ulp(1e9)`), so a tolerance of 1e-9 is just `==`. Scale the tolerance to the values, or keep
money exact in `long` cents or `BigDecimal`.

## Integer Division by Zero Throws; Floating-Point Division Returns Infinity or NaN, and NaN Equals Nothing

The deploy gate blocked a canary whose error rate was above 5%. This one served no requests:

```java
long failed = 0, requests = 0;
double rate = (double) failed / requests;
System.out.println("rate > 0.05: " + (rate > 0.05) + ", rate <= 0.05: " + (rate <= 0.05)
        + ", rate == rate: " + (rate == rate));
```

```text
rate > 0.05: false, rate <= 0.05: false, rate == rate: false
```

`failed / requests` on the two `long`s would have thrown `ArithmeticException: / by zero`. In
floating point, IEEE 754 defines a result instead: `1.0 / 0` is `Infinity` and `0.0 / 0` is `NaN`.
Every `<`, `>`, `<=`, `>=` and `==` with `NaN` is `false`, even `rate == rate` (only `!=` is
`true`), so
`if (rate > 0.05) block()` lets the empty canary through, and `!(rate > limit)` is not the same
test as `rate <= limit`. Detect it with `Double.isNaN`; `x == Double.NaN` is always `false`.

<!-- practice 2 -->

## A Wrapper Is an Object: It Can Be null, and == Compares References

A wrapper such as `Integer` holds a primitive in an object, and the compiler boxes and unboxes
for you. Unboxing `null` throws:

```java
Map<String, Integer> retries = new HashMap<>();
retries.put("invoice-run", 3);
int n = retries.get("payout-run");
```

```text
Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.Map.get(Object)" is null
```

That was the nightly job; `getOrDefault("payout-run", 0)` returns `0`.

`==` on two wrappers compares references:

```text
127 == 127: true, 128 == 128: false, 128 equals 128: true
```

`Integer.valueOf`, which autoboxing calls, always caches -128 to 127 and may cache more. Equal
counts compare `true` until they pass 127. Compare with `equals`, or unbox first.

<!-- practice 3 -->

## Summary

- **Integer arithmetic wraps silently, in the operands' type:** widen before multiplying, and
  use `Math.*Exact` or `Math.floorMod` where a wrong number must not pass.
- **A `double` is a binary fraction:** compare with a tolerance scaled to the values; keep money
  exact.
- **Floating-point division by zero gives `Infinity` or `NaN`**, and `NaN` is neither equal to
  nor ordered with anything, itself included.
- **A wrapper is an object:** unboxing `null` throws, and `==` compares references.

## Reference

| Type | Bits | Range |
|---|---|---|
| `byte` | 8 | -128 to 127 |
| `short` | 16 | -32,768 to 32,767 |
| `int` | 32 | -2^31 to 2^31-1 |
| `long` | 64 | -2^63 to 2^63-1 |
| `char` | 16 | `'\u0000'` to `'\uffff'` (unsigned) |
| `float` | 32 | ±3.4028235E38 |
| `double` | 64 | ±1.7976931348623157E308 |
| `boolean` | not specified | `true`, `false` |

## Challenges

1. An `int` counter grows by 10,000 a second. When does it wrap, and what breaks first?
2. Choose a tolerance for prices up to one million euros, or argue for `long` cents.
3. In a `Map<String, Integer>` of your code base, which `get` could unbox a `null`?

## Design Note: Why Overflow Wraps Instead of Throwing

The Java Language Specification defines overflow: the result is the low-order bits of the true
result, the same on every machine. Java chose not to pay for a check on every addition, so the
check is yours: `Math.addExact`, a wider type, or `BigInteger`.

## Code Examples

- Test: [PrimitiveTypesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javabasics/PrimitiveTypesTest.java)
- Source: [PrimitiveTypes.java](src/main/java/com/github/msorkhpar/claudejavatutor/javabasics/PrimitiveTypes.java)
- Test: [AutoboxingPerformanceTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javabasics/AutoboxingPerformanceTest.java)
- Source: [WrapperVsPrimitive.java](src/main/java/com/github/msorkhpar/claudejavatutor/javabasics/WrapperVsPrimitive.java)