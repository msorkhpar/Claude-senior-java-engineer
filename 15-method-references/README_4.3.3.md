# 4.3.3. Using Method References with Functional Interfaces

A teammate adds one method to `Customer`, beside the one that was already there:

```java
String displayName() { return name; }
String displayName(Locale locale) { return name.toUpperCase(locale); }
```

Their branch compiles. The next build fails in a file they never opened, on a line that has
sorted customers for two years:

```java
customers.sort(Comparator.comparing(Customer::displayName).reversed());
```

```text
Customers.java:12: error: incompatible types: cannot infer type-variable(s) T,U
        customers.sort(Comparator.comparing(Customer::displayName).reversed());
                                           ^
    (argument mismatch; invalid method reference
      incompatible types: Object cannot be converted to Locale)
```

Nothing on that line passes a `Locale`. javac picks which `displayName` the reference names from
the parameter types the functional interface supplies, and here it supplied none. Five rules
follow, one per heading. Every output is from javac and Java 21.

## The Interface's Parameter Types Choose the Overload; the Value at Run Time Doesn't

`String.valueOf` has nine overloads, among them `valueOf(char[])` and `valueOf(Object)`. Before
reading the output, decide what each reference prints for the same array:

```java
char[] code = {'A', '-', '7'};
Function<char[], String> fromChars = String::valueOf;    // valueOf(char[])
Function<Object, String> fromObject = String::valueOf;   // valueOf(Object)
System.out.println(fromChars.apply(code) + " | " + fromObject.apply(code));
System.out.println(fromObject.apply(null));
try {
    fromChars.apply(null);
} catch (NullPointerException e) {
    System.out.println(e.getMessage());
}
```

```text
A-7 | [C@7f31245a
null
Cannot read the array length because "value" is null
```

javac resolves `String::valueOf` like the call `String.valueOf(x)`, with `x` typed `char[]` in one
and `Object` in the other. That happens once, at compile time: `javap -v` shows one reference
pointing at `String.valueOf:([C)Ljava/lang/String;` and the other at
`String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;`. From then on `fromObject` calls `valueOf(Object)` whatever
it is given, and for an array that means `toString()`, the class name and a hash. A `null` goes
the other way: `valueOf(Object)` writes `"null"`, and `valueOf(char[])` throws.

An export that keeps its column formatters as `Function<Object, String>` therefore writes
`[C@7f31245a` into a token column, and a test suite whose tokens are all `String`s never sees it.

javac searches in the same phases as for a call: first without boxing, then with it, then
varargs. `Math::max` has `int`, `long`, `float` and `double` overloads:

```java
IntBinaryOperator ints = Math::max;          // max(int, int)
BinaryOperator<Integer> boxed = Math::max;   // max(int, int), after unboxing
BinaryOperator<Long> longs = Math::max;      // max(long, long)
System.out.println(ints.applyAsInt(3, 9) + " " + boxed.apply(3, 9) + " " + longs.apply(3L, 9L));
try {
    boxed.apply(null, 9);
} catch (NullPointerException e) {
    System.out.println("boxed.apply(null, 9): " + e);
}

IntUnaryOperator absInt = Math::abs;         // abs(int)
LongUnaryOperator absLong = Math::abs;       // abs(long)
System.out.println(absInt.applyAsInt(Integer.MIN_VALUE) + " " + absLong.applyAsLong(Integer.MIN_VALUE));
```

```text
9 9 9
boxed.apply(null, 9): java.lang.NullPointerException
-2147483648 2147483648
```

No `max(Integer, Integer)` exists, so for `BinaryOperator<Integer>` javac allows unboxing, finds
all four overloads applicable, and takes the most specific, `max(int, int)`. The reference unboxes
each argument before the call, so a `null` element throws a `NullPointerException` with no
message. With `abs` the overload changes the answer: `Integer.MIN_VALUE` has no positive `int`,
so `abs(int)` returns it unchanged, while `abs(long)` returns 2147483648.

For an unbound reference the interface's first parameter is the receiver, and it may be any
subtype of the class before `::`: `Function<String, String> show = Object::toString` compiles.
The other way round fails, since an `Object` is not a `String`: `Function<Object, Integer> length =
String::length` gives `method length in class String cannot be applied to given types`.

<!-- practice 1 -->

## An Exact Reference Lets javac Pick `submit(Callable)`; Overloading the Method Makes the Call Ambiguous

`ExecutorService.submit` is overloaded for `Runnable` and `Callable<T>`. A method that returns a
value fits both, since a `Runnable` can drop the result. Which does each line choose?

```java
int refresh() { prices = 120; return prices; }   // returns how many prices it loaded
void clear() { prices = 0; }

Future<?> a = pool.submit(this::refresh);
Future<?> b = pool.submit(this::clear);
System.out.println("submit(this::refresh).get() = " + a.get());
System.out.println("submit(this::clear).get()   = " + b.get());
```

```text
submit(this::refresh).get() = 120
submit(this::clear).get()   = null
```

`refresh` has one declaration, is not generic and takes no varargs, so javac knows the call
`this::refresh` makes before it chooses between the `submit`s. Such a reference is *exact*. Both fit, and the one whose method returns a value is more specific: the
same preference unit 4.1.2 showed for lambdas. `clear` returns `void` and fits only `Runnable`.

Now the price cache gets a second `refresh`:

```java
int refresh() { return 120; }
int refresh(boolean force) { return force ? 120 : 0; }

Future<?> schedule(ExecutorService pool) {
    return pool.submit(this::refresh);
}
```

```text
PriceCache.java:8: error: reference to submit is ambiguous
        return pool.submit(this::refresh);
                   ^
  both method <T>submit(Callable<T>) in ExecutorService and method submit(Runnable) in ExecutorService match
```

Which `refresh` the reference names now depends on the target. The reference is *inexact*, and
javac doesn't use it to choose the overload of `submit`: it only checks that some `refresh` takes
as many arguments as each candidate's interface supplies. Both pass and neither is more specific,
so the call is ambiguous. A single varargs method (`int count(String... names)`) or generic method
(`static <T> T none()`) gave javac 21 the same error. Naming the target fixes one call site:

```java
System.out.println("cast:   " + pool.submit((Callable<Integer>) cache::refresh).get());
System.out.println("lambda: " + pool.submit(() -> cache.refresh()).get());
```

```text
cast:   120
lambda: 120
```

The lambda has no parameters, so its body can be checked against both overloads, and the
value-returning one wins.

## An Inexact Method Reference Leaves Inference Without the Types It Needs

Back to the opening. `comparing` is generic, `<T, U> Comparator<T> comparing(Function<? super T,
? extends U>)`. Assigned straight to a `Comparator<Customer>`, it gets `T` from the target. With
`.reversed()` called on its result it gets no target (unit 4.1.3), and `T` must come from the
argument. While `displayName` had one declaration, the reference told inference its types: the
receiver is a `Customer`, the key a `String`. Once overloaded, it tells inference nothing. `T`
becomes `Object`, and javac then reports the closest candidate it tried, `displayName(Locale)`
with an `Object` argument.

`String::toLowerCase`, overloaded with `toLowerCase(Locale)`, fails the same way:

```text
Lower.java:4: error: incompatible types: cannot infer type-variable(s) T,U
    Comparator<String> zToA = Comparator.comparing(String::toLowerCase).reversed();
                                                  ^
    (argument mismatch; invalid method reference
      incompatible types: Object cannot be converted to Locale)
```

`comparing(String::length).reversed()` compiles, because `length` has one declaration. Unit
4.1.3's advice, use a method reference where a lambda can't infer its parameter, holds only for
exact references. Each of these sorts `Mei`, `Ade` and `Zora`:

```java
customers.sort(Comparator.comparing(Customer::displayName, Comparator.reverseOrder()));
customers.sort(Comparator.<Customer, String>comparing(Customer::displayName).reversed());
Comparator<Customer> byName = Comparator.comparing(Customer::displayName);
customers.sort(byName.reversed());
```

```text
[Zora, Mei, Ade]
[Zora, Mei, Ade]
[Zora, Mei, Ade]
```

The first is the one to prefer: `comparing` gets its target from `sort`, and the order to reverse
is the key's.

### A Generic Method Needs Type Arguments When Nothing Else Gives Them

`Collections::emptyList` names one method, but its `T` is open until a target fixes it. In a
chain, nothing does:

```java
CompletableFuture<List<String>> names =
        CompletableFuture.supplyAsync(Collections::emptyList).thenApply(list -> list);
```

```text
EmptyChain.java:6: error: incompatible types: inference variable U has incompatible bounds
            CompletableFuture.supplyAsync(Collections::emptyList).thenApply(list -> list);
                                                                           ^
    equality constraints: List<String>
    lower bounds: List<Object>
```

`supplyAsync` inferred a `List<Object>`, and `thenApply` can't turn it into a `List<String>`.
`Collections::<String>emptyList` supplies the type argument, and the same chain compiles and
yields `[]`.

> **javac and the specification.** As we read JLS 15.13.1, a generic method reference with
> explicit type arguments is exact. javac 21 still reports
> `pool.submit(Collections::<String>emptyList)` as ambiguous, like `this::refresh`; a cast to
> `Callable<List<String>>` compiles.

## A Varargs Method Packs the Interface's Arguments, So an Array Becomes One Element

`String.format(String, Object...)` fits a `BiFunction<String, Object, String>`. No
`format(String, Object)` exists, so javac reaches the varargs phase and compiles the reference to
a synthetic `lambda$` method (unit 4.3.1) that calls `String.format(t, x)` with `x` typed `Object`,
so `x` is packed into a new one-element array. A routing service renders shard names this way:

```java
BiFunction<String, Object, String> render = String::format;   // format(String, Object...)
Object[] route = {"eu-west", 7};
System.out.println("direct:    " + String.format("%s/%s", route));
System.out.println("reference: " + render.apply("shard %s", "eu-west"));
try {
    System.out.println(render.apply("%s/%s", route));
} catch (MissingFormatArgumentException e) {
    System.out.println("reference: " + e);
}
System.out.println("reference: " + render.apply("%s", route).startsWith("[Ljava.lang.Object;@"));

BiFunction<String, Object[], String> spread = String::format;
System.out.println("spread:    " + spread.apply("%s/%s", route));
```

```text
direct:    eu-west/7
reference: shard eu-west
reference: java.util.MissingFormatArgumentException: Format specifier '%s'
reference: true
spread:    eu-west/7
```

The direct call passes its `Object[]` as the varargs array. Through the
reference the array arrives typed `Object` and is packed: the first `%s` gets the whole array,
printed as `[Ljava.lang.Object;@…`, and the second finds nothing. Single values work, so the
failure appears with the first message that carries two. With `Object[]` as the parameter type,
the reference passes the array through.

A primitive array is never spread into `T...`, since an `int[]` is not an `Object[]`:

```java
Function<String[], List<String>> words = Arrays::asList;
Function<int[], List<int[]>> numbers = Arrays::asList;
System.out.println(words.apply(new String[] {"a", "b", "c"}).size() + " "
        + numbers.apply(new int[] {1, 2, 3}).size());
```

```text
3 1
```

<!-- practice 2 -->

## A Method That Throws a Checked Exception Doesn't Fit `Function`

`Files.readString(Path)` declares `throws IOException`, and `Function.apply` declares nothing:

```text
ReadAll.java:5: error: incompatible thrown types IOException in functional expression
    Function<Path, String> read = Files::readString;
                                  ^
```

The rule is the one for a lambda's body, but a reference has no body to put a `try` in. Unit
3.2.2 gives the two ways out: a functional interface whose method declares the exception, which
takes `Files::readString` as it is, or a lambda that wraps the exception and code at your boundary
that unwraps it.

<!-- practice 3 -->

## Summary

- **The interface's parameter types choose the overload, at compile time.** `String::valueOf` as
  a `Function<Object, String>` prints `[C@…` for a `char[]`; `Math::abs` as an `IntUnaryOperator`
  leaves `Integer.MIN_VALUE` negative; a boxed target unboxes and throws on `null`.
- **An exact reference (one declaration, not generic, not varargs) lets javac pick
  `submit(Callable)`.** Adding an overload makes every `submit(this::m)` ambiguous; a cast or a
  lambda fixes the call site.
- **An inexact reference leaves inference without the types it needs.** `comparing(m).reversed()`
  breaks when `m` gains an overload; pass `reverseOrder()` to `comparing`, or give type arguments.
- **A varargs method packs the interface's arguments.** An `Object[]` passed as `Object` becomes
  one element, and an `int[]` is never spread.
- **A checked exception doesn't fit `Function`.** Use an interface that declares it, or wrap it in
  a lambda (unit 3.2.2).

## Reference

**Which method a reference names (javac 21, handles read with `javap -v`):**

| Reference | Target | Method chosen |
|---|---|---|
| `String::valueOf` | `Function<char[], String>` | `valueOf(char[])`; `null` throws `NullPointerException` |
| `String::valueOf` | `Function<Object, String>` | `valueOf(Object)`; a `char[]` gives `[C@…`, `null` gives `"null"` |
| `Math::max` | `IntBinaryOperator`, `BinaryOperator<Integer>` | `max(int, int)`; the boxed target throws on `null` |
| `Math::max` | `BinaryOperator<Long>` | `max(long, long)` |
| `Math::abs` | `IntUnaryOperator` / `LongUnaryOperator` | `abs(int)` / `abs(long)` |
| `String::format` | `BiFunction<String, Object, String>` | `format(String, Object...)`, argument packed into a new array |
| `String::format` | `BiFunction<String, Object[], String>` | `format(String, Object...)`, array passed as is |
| `Arrays::asList` | `Function<int[], List<int[]>>` | `asList(T...)` with `T = int[]`: a list of one |

**What javac 21 reported:**

| Code | Result |
|---|---|
| `pool.submit(this::refresh)`, `refresh` overloaded | `reference to submit is ambiguous` |
| `pool.submit(Collections::<String>emptyList)` | `reference to submit is ambiguous` |
| `pool.submit((Callable<List<String>>) Collections::emptyList)` | compiles |
| `comparing(Customer::displayName).reversed()`, `displayName` overloaded | `cannot infer type-variable(s) T,U` |
| `supplyAsync(Collections::emptyList).thenApply(list -> list)` as `CompletableFuture<List<String>>` | `inference variable U has incompatible bounds` |
| `Function<Path, String> read = Files::readString` | `incompatible thrown types IOException in functional expression` |
| `Function<String, String> show = Object::toString` | compiles |
| `Function<Object, Integer> length = String::length` | `method length in class String cannot be applied to given types` |

## Challenges

1. Pick a class in your codebase that other modules use through method references. Which of its
   methods could gain an overload without breaking a caller's build, and how would you find out
   before merging?
2. Your API takes a `Function<Object, String>` formatter, and callers pass `String::valueOf`.
   Would you change the parameter type, add overloads for common types, or document it? What does
   each cost a caller?
3. `submit(this::refresh)` and `submit(() -> refresh())` both choose `Callable` today. Which one
   keeps compiling when `refresh` gains an overload, and which one would you write in a code base
   you don't control?

## Design Note: Why javac Ignores an Overloaded Reference While It Picks an Overload

To choose an overload of `submit`, javac checks every argument against every candidate. For a
reference to an overloaded method, that would mean resolving the reference again for each
candidate's parameter types, and each resolution could pick a different method, while the
candidate's own type variables would depend on that pick. javac leaves such a reference out of the
choice and resolves it once, against the method chosen. The cost lands on callers: adding an
overload doesn't break compiled ones (a `Scheduler` compiled against the old `PriceCache` still
printed `get() = 120` against the new one), but breaks them at their next compile.

## Code Examples

- Source: [MethodReferencesWithFunctionalInterfaces.java](src/main/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferencesWithFunctionalInterfaces.java)
- Test: [MethodReferencesWithFunctionalInterfacesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferencesWithFunctionalInterfacesTest.java)
