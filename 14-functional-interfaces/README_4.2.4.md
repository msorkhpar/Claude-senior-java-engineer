# 4.2.4. Predicate — Testing Values and Returning Boolean Results

## Concept Explanation

`Predicate<T>` is the functional interface for **boolean-valued tests**. It accepts a value of type `T` and returns
`true` or `false`. A Predicate models a condition — a property or constraint that something either satisfies or does
not.

**Real-world analogy**: Think of a Predicate like an entry checkpoint at an event. Each person approaching the
entrance is tested against a set of rules (age requirement, dress code, ticket possession). The checkpoint returns
`true` (you may enter) or `false` (you may not). Multiple checkpoints can be combined: "has ticket AND is over 18 OR
is on the VIP list". This is exactly how `and()`, `or()`, and `negate()` work.

The interface definition:

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);

    default Predicate<T> and(Predicate<? super T> other) { ... }      // logical AND (short-circuit)
    default Predicate<T> or(Predicate<? super T> other) { ... }       // logical OR  (short-circuit)
    default Predicate<T> negate() { ... }                             // logical NOT
    static <T> Predicate<T> not(Predicate<? super T> target) { ... } // static NOT (Java 11+)
    static <T> Predicate<T> isEqual(Object targetRef) { ... }        // null-safe equality
}
```

**Composition rules:**
- `p1.and(p2)` — short-circuit AND: if `p1.test(t)` is `false`, `p2` is never called.
- `p1.or(p2)` — short-circuit OR: if `p1.test(t)` is `true`, `p2` is never called.
- `p.negate()` — logical NOT: flips the boolean result.
- `Predicate.not(p)` (Java 11+) — static factory for negation; more readable with method references.
- `Predicate.isEqual(value)` — tests `Objects.equals(t, value)`, safe for null targets.

**BiPredicate<T, U>** accepts two arguments of types `T` and `U` and returns `boolean`. It has `and()`, `or()`, and
`negate()` methods, but no `not()` or `isEqual()` static factories.

**Primitive specializations** (avoid boxing):
- `IntPredicate` — `boolean test(int value)`
- `LongPredicate` — `boolean test(long value)`
- `DoublePredicate` — `boolean test(double value)`

**Key use sites in the JDK:**
- `Stream.filter(Predicate<T>)` — the primary use site: keep only elements matching the predicate
- `Collection.removeIf(Predicate<E>)` — remove elements matching the predicate
- `Optional.filter(Predicate<T>)` — keep the Optional's value only if it satisfies the predicate
- `Files.find(path, depth, BiPredicate)` — file system traversal with attribute-based filtering

## Key Points to Remember

1. `Predicate<T>` has one abstract method: `boolean test(T t)` — it tests a condition on an input.
2. `and()`, `or()`, and `negate()` are instance methods for composing predicates; all maintain short-circuit evaluation.
3. `Predicate.not(p)` (Java 11+) is a static method — particularly useful for negating method references: `Predicate.not(String::isEmpty)`.
4. `Predicate.isEqual(value)` uses `Objects.equals()` — it is null-safe on both the predicate argument and the target value.
5. Short-circuit evaluation in `and()` and `or()` is important for null safety: put null-checks first in `and()` chains.
6. `BiPredicate<T,U>` is used by `Files.find()` and other JDK methods that need to test two correlated values.
7. Primitive specializations (`IntPredicate`, `LongPredicate`, `DoublePredicate`) have only `test()`, `and()`, `or()`, and `negate()` — no static factories.
8. Predicates should be pure functions (no side effects); a Predicate that modifies state is a code smell.

## Relevant Java 21 Features

- **Java 8**: `Predicate<T>`, `BiPredicate<T,U>`, and primitive specializations introduced.
- **Java 8**: `Stream.filter(Predicate)`, `Collection.removeIf(Predicate)`, and `Optional.filter(Predicate)` added.
- **Java 11**: `Predicate.not(Predicate)` static factory method added — enables clean negation of method references like `Predicate.not(String::isBlank)`.
- **Java 16+**: Records can be tested with Predicates without getters — `p -> p instanceof PersonRecord(var n, var a) && a > 18` (pattern matching).
- **Java 21**: Pattern matching for switch can be used inside a Predicate to test complex type hierarchies. `Predicate<Shape> isLargeCircle = s -> s instanceof Circle c && c.radius() > 100`.
- **Java 21**: Sequenced collections' `getFirst()`/`getLast()` can be used inside Predicate conditions.

## Common Pitfalls and How to Avoid Them

1. **Null input causing `NullPointerException` in a non-null-safe Predicate** — a Predicate that calls instance methods will throw NPE on null input if a null check is not the first condition.

   ```java
   // Broken: s.isEmpty() throws NPE when s is null
   Predicate<String> notEmpty = s -> !s.isEmpty();
   notEmpty.test(null); // NullPointerException!
   ```

   ```java
   // Fix: put null check FIRST in the and() chain (short-circuit protects the second)
   Predicate<String> notNull = s -> s != null;
   Predicate<String> notEmpty = s -> !s.isEmpty();
   Predicate<String> validInput = notNull.and(notEmpty);
   validInput.test(null); // returns false, no NPE
   ```

2. **Using `negate()` method instead of the more readable `Predicate.not()` with method references** — `negate()` on an inline lambda is readable, but `Predicate.not()` on a method reference is far more expressive.

   ```java
   // Verbose: manually wrap in lambda then negate
   Predicate<String> notBlank = ((Predicate<String>) String::isBlank).negate();
   ```

   ```java
   // Clean (Java 11+): static Predicate.not() with method reference
   Predicate<String> notBlank = Predicate.not(String::isBlank);

   // Usage in stream filter
   List<String> nonBlank = strings.stream()
       .filter(Predicate.not(String::isBlank))
       .collect(Collectors.toList());
   ```

3. **Side effects inside a Predicate** — Predicates that modify external state (increment a counter, write to a list) are impure. In parallel streams, this causes race conditions. Use `Consumer` for side effects.

   ```java
   // Broken: side effect in Predicate, NOT safe in parallel streams
   List<String> seen = new ArrayList<>();
   list.parallelStream()
       .filter(s -> { seen.add(s); return !s.isEmpty(); }) // race condition!
       .collect(toList());
   ```

   ```java
   // Fix: keep Predicate pure, handle side effects separately
   List<String> result = list.stream()
       .filter(s -> !s.isEmpty()) // pure predicate
       .peek(seen::add)           // side effect with peek (only for debugging)
       .collect(toList());
   ```

4. **Calling `and()` / `or()` with null** — passing null to `and()` or `or()` throws `NullPointerException` when the composed predicate is tested (not at composition time).

   ```java
   Predicate<String> p = s -> true;
   Predicate<String> bad = p.and(null); // NPE thrown when test() is called
   ```

   ```java
   // Fix: guard against null predicates
   Predicate<String> safe = p.and(Objects.requireNonNull(other, "predicate must not be null"));
   ```

5. **Order of `and()` predicates for performance** — Put the cheapest and most selective predicate first to maximize short-circuiting benefits.

   ```java
   // Suboptimal: expensive check runs even when cheap check would have failed
   Predicate<User> valid = expensiveDatabaseCheck.and(simpleNullCheck);
   ```

   ```java
   // Optimal: cheap check first — short-circuits before expensive check
   Predicate<User> valid = simpleNullCheck.and(expensiveDatabaseCheck);
   ```

## Best Practices and Optimization Techniques

1. **Use `Predicate.not()` (Java 11+) instead of manual lambda negation** for method references — it is more readable and works without an intermediate variable.

2. **Order predicates in `and()` chains from cheapest to most expensive** to maximize short-circuit benefit.

3. **Extract and name complex predicates** as constants or static fields — they become self-documenting and reusable.

   ```java
   private static final Predicate<String> NOT_BLANK = Predicate.not(String::isBlank);
   private static final Predicate<String> VALID_EMAIL = email ->
       email != null && email.contains("@") && email.contains(".");
   ```

4. **Use `Collection.removeIf(Predicate)` instead of iterator-based removal** — it is concise and avoids `ConcurrentModificationException` in the loop.

   ```java
   List<String> names = new ArrayList<>(List.of("Alice", "", "Bob", null, ""));
   names.removeIf(s -> s == null || s.isBlank()); // in-place removal
   // names = ["Alice", "Bob"]
   ```

5. **Use primitive specializations for numeric filtering** in performance-sensitive code — `IntPredicate` avoids boxing each element.

6. **Compose predicates for validation** rather than writing nested if-statements — it is more expressive and each condition is independently testable.

## Edge Cases and Their Handling

1. **Empty stream with `filter(Predicate)`** — `filter` on an empty stream returns an empty stream; no NPE or exception.

2. **`Predicate.isEqual(null)`** — returns a Predicate that tests if the input is `null` (uses `Objects.equals(null, t)`, which returns true only for null input). This is useful for null-element detection.

   ```java
   Predicate<String> isNull = Predicate.isEqual(null);
   isNull.test(null);   // true
   isNull.test("hello"); // false
   ```

3. **Combining `IntPredicate` with `and()` / `or()`** — primitive predicates support composition but do NOT have `not()` or `isEqual()` static factories. Use instance `negate()` instead.

   ```java
   IntPredicate positive = n -> n > 0;
   IntPredicate even = n -> n % 2 == 0;
   IntPredicate positiveAndEven = positive.and(even);
   IntPredicate positiveOrEven = positive.or(even);
   IntPredicate notPositive = positive.negate();
   ```

4. **`BiPredicate.negate()` with `Files.find()`** — `Files.find()` accepts a `BiPredicate<Path, BasicFileAttributes>`. Negating it with `negate()` effectively inverts the search criteria.

5. **Predicate in a parallel stream with short-circuit terminal** — In `Stream.anyMatch(Predicate)`, once a match is found in any thread, the remaining threads are cancelled. The Predicate may be called from multiple threads concurrently — ensure it is stateless.

6. **All-true and all-false sentinels:**

   ```java
   Predicate<Object> alwaysTrue  = x -> true;
   Predicate<Object> alwaysFalse = x -> false;
   // Equivalent:
   Predicate<Object> alwaysTrue2  = Predicate.not(x -> false);
   ```

## Interview-specific Insights

Interviewers focus on:

- The three composition methods (`and`, `or`, `negate`) and their short-circuit behavior.
- `Predicate.not()` (Java 11+) as the clean way to negate method references.
- `Predicate.isEqual()` and its null-safety behavior.
- Understanding that `and()` / `or()` short-circuit just like `&&` and `||`.
- The null-safety ordering pattern: put null checks first in `and()` chains.
- `Collection.removeIf(Predicate)` as the idiomatic way to conditionally remove elements.
- Primitive specializations and why they exist.

**Common tricky questions:**
- "If `p1.test(t)` is `false` in `p1.and(p2)`, is `p2.test(t)` called?" (No — `and()` short-circuits just like `&&`.)
- "What does `Predicate.isEqual(null)` return when tested with `null`?" (Returns `true` — it uses `Objects.equals(null, null)`.)
- "Can you negate a method reference with `Predicate.not()`?" (Yes — `Predicate.not(String::isBlank)` works cleanly.)
- "What is the difference between `Predicate.negate()` and `Predicate.not()`?" (`negate()` is an instance method; `not()` is a static factory. `not()` is preferred for method references because it avoids the explicit cast needed to call `negate()` on a method reference.)

## Interview Q&A Section

**Q1: How do `Predicate.and()`, `or()`, and `negate()` work, and do they short-circuit?**

```text
A1: All three Predicate composition methods maintain short-circuit semantics, mirroring
Java's && and || operators:

    p1.and(p2)  — if p1.test(t) is FALSE, p2 is never called (short-circuit AND)
    p1.or(p2)   — if p1.test(t) is TRUE,  p2 is never called (short-circuit OR)
    p.negate()  — always calls p.test(t) and inverts the boolean result

Short-circuiting is critical for null safety: by placing a null-check predicate first in an
and() chain, you ensure that method calls on the value only happen if it is non-null.

Ordering also matters for performance: place the cheapest and most discriminating predicate
first to short-circuit the expensive ones as often as possible.
```

```java
// Short-circuit and() for null safety
Predicate<String> notNull = s -> s != null;
Predicate<String> longEnough = s -> s.length() >= 5; // would NPE on null
Predicate<String> startsWithA = s -> s.startsWith("A"); // would NPE on null

// Safe: notNull is tested first; if null, the rest are skipped
Predicate<String> valid = notNull.and(longEnough).and(startsWithA);

System.out.println(valid.test(null));       // false (short-circuited at notNull)
System.out.println(valid.test("Al"));       // false (short-circuited at longEnough)
System.out.println(valid.test("Alice"));    // true (all three pass)
System.out.println(valid.test("Robert"));   // false (startsWithA fails)

// Short-circuit or()
Predicate<Integer> isZero = n -> n == 0;
Predicate<Integer> isNegative = n -> n < 0; // only called if isZero is false
Predicate<Integer> nonPositive = isZero.or(isNegative);

System.out.println(nonPositive.test(0));  // true  (isZero is true, isNegative not called)
System.out.println(nonPositive.test(-5)); // true  (isZero false, isNegative true)
System.out.println(nonPositive.test(3));  // false (both false)

// negate()
Predicate<String> isEmpty = String::isEmpty;
Predicate<String> isNotEmpty = isEmpty.negate();
System.out.println(isNotEmpty.test("hello")); // true
System.out.println(isNotEmpty.test(""));      // false
```

---

**Q2: What is `Predicate.not()` and why was it added in Java 11?**

```text
A2: Predicate.not(Predicate target) is a static factory method that returns a Predicate
representing the logical negation of the argument.

It was added in Java 11 specifically to enable clean negation of method references.
Before Java 11, to negate a method reference you had to:
1. Assign it to an explicit typed variable, then call .negate()
2. Or write a manual lambda

Both workarounds are verbose. Predicate.not() solves this elegantly:

    // Java 8-10 workaround
    Predicate<String> notBlank = ((Predicate<String>) String::isBlank).negate();

    // Java 11+
    Predicate<String> notBlank = Predicate.not(String::isBlank);

The most common use case is stream filtering: filter out elements that match a condition
expressed as a method reference.
```

```java
List<String> strings = Arrays.asList("hello", "", "  ", "world", null, "java");

// Java 11+: Predicate.not with method references — clean and readable
List<String> nonEmpty = strings.stream()
    .filter(Objects::nonNull)                    // first remove nulls
    .filter(Predicate.not(String::isEmpty))       // then remove empty strings
    .filter(Predicate.not(String::isBlank))       // then remove blank strings
    .collect(Collectors.toList());
// ["hello", "world", "java"]

// Contrast with verbose Java 8 workaround
List<String> nonEmpty8 = strings.stream()
    .filter(s -> s != null)
    .filter(s -> !s.isEmpty())
    .filter(s -> !s.isBlank())
    .collect(Collectors.toList());
// Same result, but less idiomatic

// Predicate.not works with any method reference that matches the signature
Predicate<Integer> notNegative = Predicate.not(n -> n < 0);
Predicate<List<?>> notEmptyList = Predicate.not(List::isEmpty);
```

---

**Q3: What is `Predicate.isEqual()` and when should you use it over `Objects::equals`?**

```text
A3: Predicate.isEqual(Object targetRef) returns a Predicate<T> that tests whether the
input object is equal to targetRef, using Objects.equals(targetRef, t).

Key properties:
- Null-safe: Predicate.isEqual(null) tests whether the input IS null (returns true for null)
- Predicate.isEqual("hello") tests whether the input is equal to "hello"
- Both targetRef and the tested value can be null without NPE

Compared to Objects::equals:
- Predicate.isEqual("x") is a Predicate<Object> that tests equality with "x"
- Objects::equals is a BiPredicate<Object,Object> — takes two args
- They serve different purposes; isEqual is convenient for stream filtering against a constant

Use Predicate.isEqual when you need a Predicate for a specific constant value in stream
pipelines or optional filtering.
```

```java
// Filter a list for a specific value
List<String> names = List.of("Alice", "Bob", "Alice", "Charlie");

// Using Predicate.isEqual
Predicate<String> isAlice = Predicate.isEqual("Alice");
long aliceCount = names.stream().filter(isAlice).count();  // 2

// Null-safe: isEqual(null) tests for null
List<String> withNulls = Arrays.asList("Alice", null, "Bob", null);
Predicate<String> isNull = Predicate.isEqual(null);
List<String> nulls = withNulls.stream().filter(isNull).collect(Collectors.toList());
// [null, null]

// Negated: find elements NOT equal to a target
List<String> notAlice = names.stream()
    .filter(Predicate.not(Predicate.isEqual("Alice")))
    .collect(Collectors.toList());
// ["Bob", "Charlie"]

// Optional.filter with isEqual
Optional<String> opt = Optional.of("hello");
Optional<String> onlyHello = opt.filter(Predicate.isEqual("hello")); // present
Optional<String> onlyWorld = opt.filter(Predicate.isEqual("world")); // empty
```

---

**Q4: How do you build a complex multi-condition validation using `Predicate` composition?**

```text
A4: Predicate composition is ideal for validation rules because:
1. Each rule is expressed as a named Predicate — independently readable and testable
2. Rules are combined with and()/or() to express AND and OR logic
3. The combined predicate can be applied to streams, collections, and Optional
4. New rules can be added by chaining without modifying existing ones (Open/Closed Principle)

This pattern is commonly seen in:
- Email / password validation
- Business rule engines
- Data quality checks
- Form input validation
```

```java
// Email validation with composed Predicates
public static Predicate<String> emailValidator() {
    Predicate<String> notNull     = s -> s != null;
    Predicate<String> notEmpty    = s -> !s.isEmpty();
    Predicate<String> hasAtSign   = s -> s.contains("@");
    Predicate<String> hasDomain   = s -> {
        int at = s.indexOf('@');
        return at > 0 && s.lastIndexOf('.') > at + 1;
    };
    return notNull.and(notEmpty).and(hasAtSign).and(hasDomain);
}

// Password strength validation
public static Predicate<String> strongPasswordValidator() {
    Predicate<String> minLength   = p -> p != null && p.length() >= 8;
    Predicate<String> hasUpper    = p -> p.chars().anyMatch(Character::isUpperCase);
    Predicate<String> hasLower    = p -> p.chars().anyMatch(Character::isLowerCase);
    Predicate<String> hasDigit    = p -> p.chars().anyMatch(Character::isDigit);
    Predicate<String> hasSpecial  = p -> p.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    return minLength.and(hasUpper).and(hasLower).and(hasDigit).and(hasSpecial);
}

// Usage
Predicate<String> validEmail = emailValidator();
System.out.println(validEmail.test("user@example.com")); // true
System.out.println(validEmail.test("not-an-email"));     // false
System.out.println(validEmail.test(null));               // false

Predicate<String> strongPwd = strongPasswordValidator();
System.out.println(strongPwd.test("Passw0rd!"));    // true
System.out.println(strongPwd.test("password"));     // false (no upper, no digit, no special)

// Apply to a list
List<String> emails = List.of("a@b.com", "invalid", "c@d.org", "bad");
List<String> validEmails = emails.stream()
    .filter(validEmail)
    .collect(Collectors.toList());
// ["a@b.com", "c@d.org"]
```

---

**Q5: What is `BiPredicate<T,U>` and how is it used in practice?**

```text
A5: BiPredicate<T,U> tests a condition on two arguments of types T and U and returns boolean.
Its abstract method is boolean test(T t, U u).

Like Predicate, it supports and(), or(), and negate() composition methods, but does not
have static factories (not(), isEqual()).

Key use sites in the JDK:
- Files.find(Path, int, BiPredicate<Path, BasicFileAttributes>) — file system filtering
- Testing a pair of values (e.g., key-value pair, parent-child, index-element)

BiPredicate is also useful when you need to test a relationship between two objects
(containment, comparison, membership) without capturing one via closure.
```

```java
// Basic BiPredicate: tests if a String contains a substring
BiPredicate<String, String> contains = String::contains;
System.out.println(contains.test("Hello World", "World")); // true
System.out.println(contains.test("Hello World", "Java"));  // false

// BiPredicate composition
BiPredicate<String, String> longerThan3 = (s, prefix) -> s.length() > 3;
BiPredicate<String, String> startsWith  = (s, prefix) -> s.startsWith(prefix);
BiPredicate<String, String> combined    = longerThan3.and(startsWith);

List<String> words = List.of("hi", "hello", "hey", "java", "javafx");
String prefix = "he";
List<String> matching = words.stream()
    .filter(w -> combined.test(w, prefix))
    .collect(Collectors.toList());
// ["hello", "hey"] — "hey" is 3 chars so fails longerThan3? No: "hey".length() = 3 > 3 is false
// Actually: ["hello"] — only "hello" passes both: length > 3 AND starts with "he"

// negate()
BiPredicate<String, String> doesNotContain = contains.negate();
System.out.println(doesNotContain.test("Hello World", "Java")); // true

// Files.find — real JDK use of BiPredicate
Path root = Path.of("/tmp");
try (Stream<Path> found = Files.find(root, 3,
        (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".java"))) {
    found.forEach(System.out::println);
}
```

---

**Q6: How do primitive `Predicate` specializations differ from generic `Predicate<T>`, and when should you use them?**

```text
A6: IntPredicate, LongPredicate, and DoublePredicate test conditions on primitive types
directly, without boxing:

    IntPredicate:    boolean test(int value)
    LongPredicate:   boolean test(long value)
    DoublePredicate: boolean test(double value)

All three support and(), or(), and negate() instance methods. They do NOT have the static
factory methods not() or isEqual() that generic Predicate has.

Use primitive specializations when:
- Filtering IntStream, LongStream, or DoubleStream (these APIs require the primitive forms)
- Testing elements in tight loops where boxing overhead is measurable
- Working with large arrays of numbers

The performance benefit is most visible in hot code paths that process millions of elements.
```

```java
// Generic Predicate with boxing
Predicate<Integer> isEvenBoxed = n -> n % 2 == 0;
List<Integer> evens = List.of(1, 2, 3, 4, 5, 6).stream()
    .filter(isEvenBoxed)  // each int is boxed to Integer
    .collect(Collectors.toList());

// IntPredicate — no boxing
IntPredicate isEvenPrimitive = n -> n % 2 == 0;
int[] evensArray = IntStream.rangeClosed(1, 6)
    .filter(isEvenPrimitive)  // no boxing at all
    .toArray(); // [2, 4, 6]

// Composition with IntPredicate
IntPredicate positive = n -> n > 0;
IntPredicate even     = n -> n % 2 == 0;
IntPredicate negated  = even.negate();

IntPredicate positiveAndEven = positive.and(even);
IntPredicate positiveOrNeg   = positive.or(negated);

int[] result = IntStream.rangeClosed(-5, 5)
    .filter(positiveAndEven)
    .toArray(); // [2, 4]

// LongPredicate for large number checking
LongPredicate isBillion = n -> n >= 1_000_000_000L;
boolean result2 = isBillion.test(5_000_000_000L); // true — no boxing needed
```

---

**Q7: How would you use `Collection.removeIf(Predicate)` and when is it better than an iterator?**

```text
A7: Collection.removeIf(Predicate<E> filter) removes all elements from the collection
for which the Predicate returns true. It is a default method on Collection (Java 8+).

Advantages over iterator-based removal:
1. Conciseness: one line vs. 5-6 lines of iterator code
2. Safety: no ConcurrentModificationException — the method handles iteration internally
3. Performance: ArrayList's implementation uses a BitSet for efficient bulk removal (O(n))
4. Readability: the condition is expressed as a named Predicate

Use removeIf when you want to modify a collection in-place based on a condition.
Use stream.filter() when you want to produce a NEW collection without modifying the original.
```

```java
// Iterator-based removal (verbose, error-prone)
List<String> names = new ArrayList<>(List.of("Alice", "", "Bob", null, "  ", "Charlie"));
Iterator<String> it = names.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s == null || s.isBlank()) {
        it.remove();
    }
}
// names = ["Alice", "Bob", "Charlie"]

// removeIf — concise and safe
List<String> names2 = new ArrayList<>(List.of("Alice", "", "Bob", null, "  ", "Charlie"));
names2.removeIf(s -> s == null || s.isBlank());
// names2 = ["Alice", "Bob", "Charlie"]

// With named Predicate for reusability
Predicate<String> nullOrBlank = s -> s == null || s.isBlank();
names2.removeIf(nullOrBlank);

// Complex business condition
List<User> users = new ArrayList<>(loadUsers());
Predicate<User> inactive = u -> u.getLastLogin().isBefore(LocalDate.now().minusYears(1));
users.removeIf(inactive); // remove users inactive for over a year

// Note: removeIf works on mutable collections only
// List.of() is immutable — removeIf throws UnsupportedOperationException
// List.of("a", "b").removeIf(s -> true); // UnsupportedOperationException!
```

## Code Examples

- Source: [PredicateDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/PredicateDemo.java)
- Test: [PredicateDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/PredicateDemoTest.java)
