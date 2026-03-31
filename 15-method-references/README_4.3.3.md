# 4.3.3. Using Method References with Functional Interfaces

## Concept Explanation

Method references reach their full potential when combined with Java's built-in functional interfaces in
`java.util.function` and the collections/stream API. Each standard functional interface defines a contract that
method references must satisfy, and choosing the right combination unlocks highly readable, reusable code.

This section covers how to use each method reference type with the five core functional interfaces (`Consumer`,
`Supplier`, `Function`, `Predicate`, `BiFunction`) and two special-purpose interfaces (`Comparator`, `Optional`),
showing both the method reference syntax and its equivalent lambda.

### `Consumer<T>` — `void accept(T t)`

A `Consumer` consumes a value and returns nothing. Static, bound, and unbound method references all map onto
`Consumer` when the referenced method accepts one argument and returns void.

```java
// Static method reference → Consumer
Consumer<String> log = Logger::log;                  // static Logger.log(String)

// Bound instance method reference → Consumer
PrintStream ps = System.out;
Consumer<String> print = ps::println;                // bound to ps

// Unbound instance method reference → Consumer (method returns void, takes no args)
Consumer<List<String>> clearList = List::clear;      // unbound: receiver is the list
```

### `Supplier<T>` — `T get()`

A `Supplier` takes no arguments and returns a value. Bound instance method references and constructor references
map to `Supplier` when they take no additional arguments.

```java
// Bound instance method reference → Supplier
String str = "Hello, World!";
Supplier<String> upper = str::toUpperCase;           // bound: no args, returns String

// Constructor reference → Supplier (no-arg constructor)
Supplier<ArrayList<String>> listFactory = ArrayList::new;
```

### `Function<T, R>` — `R apply(T t)`

A `Function` transforms one value into another. All four reference types can produce a `Function`:

```java
// Static method reference → Function
Function<String, Integer> parse = Integer::parseInt;

// Bound instance method reference → Function (method takes one arg)
String delimiter = ",";
Function<String, String[]> split = delimiter::split; // ??? — wait, this is reversed
// Better: the bound object receives; the Function's arg is the method's parameter
// "delimiter" would be the receiver... actually this example needs correction:
// String::split is an unbound instance method ref
Function<String, String[]> splitByComma = s -> s.split(","); // lambda when arg needed

// Unbound instance method reference → Function (method takes no args; receiver is the arg)
Function<String, Integer> length = String::length;     // str -> str.length()
Function<String, String>  lower  = String::toLowerCase; // str -> str.toLowerCase()

// Constructor reference → Function (single-arg constructor)
Function<String, StringBuilder> sbFactory = StringBuilder::new;
```

### `Predicate<T>` — `boolean test(T t)`

A `Predicate` tests a value and returns a boolean. Static and unbound instance method references returning boolean
map naturally.

```java
// Static method reference → Predicate
Predicate<String> nonNull = Objects::nonNull;          // Objects.nonNull(String)
Predicate<String> isNull  = Objects::isNull;

// Bound instance method reference → Predicate
String keyword = "Java";
Predicate<String> contains = keyword::contains;        // bound: checks if "Java" contains arg
// Wait — this tests if "Java" contains each element. Usually want the other way:
Predicate<String> contains2 = s -> s.contains("Java"); // lambda for this common case

// Unbound instance method reference → Predicate (boolean return, no extra args)
Predicate<String> isEmpty  = String::isEmpty;          // str -> str.isEmpty()
Predicate<String> isBlank  = String::isBlank;
Predicate<List<?>> listEmpty = List::isEmpty;
```

### `BiFunction<T, U, R>` — `R apply(T t, U u)`

A `BiFunction` transforms two values into one. Unbound instance method references with one parameter map to
`BiFunction`.

```java
// Unbound instance method reference with one argument → BiFunction
BiFunction<String, String, String> concat = String::concat;   // (s1, s2) -> s1.concat(s2)
BiFunction<String, String, Boolean> contains = String::contains; // compile error — CharSequence!
// Correct:
BiFunction<String, CharSequence, Boolean> containsBi = String::contains;

// Static method → BiFunction
BiFunction<Integer, Integer, Integer> max = Math::max;         // Math.max(int, int)

// Constructor with one arg → Function, two args → BiFunction
BiFunction<String, Integer, String> repeated = (s, n) -> s.repeat(n); // lambda (no direct ref)
```

### `Comparator<T>` — `int compare(T o1, T o2)`

`Comparator` is a two-argument functional interface. Unbound instance method references with one additional
parameter (the argument to compare against) map cleanly via `Comparator.comparing()`.

```java
// Unbound instance method ref → Comparator (String::compareTo = (s1,s2) -> s1.compareTo(s2))
Comparator<String> alphaCmp = String::compareTo;
Comparator<String> ignoreCaseCmp = String::compareToIgnoreCase;

// Comparator.comparing with method reference (most idiomatic form)
Comparator<Person> byName = Comparator.comparing(Person::getName);
Comparator<Person> byAge  = Comparator.comparingInt(Person::getAge);
```

### `Optional<T>` — `map`, `filter`, `ifPresent`

Optional's methods accept functional interfaces, making them natural targets for method references.

```java
Optional<String> opt = Optional.of("hello");
Optional<Integer> len  = opt.map(String::length);      // Function<String, Integer>
Optional<String> upper = opt.map(String::toUpperCase); // Function<String, String>
opt.ifPresent(System.out::println);                    // Consumer<String>
Optional<String> filtered = opt.filter(String::isBlank); // Predicate<String>
```

## Key Points to Remember

1. Match the method reference type to the functional interface based on the number and types of arguments.
2. `Consumer` — use when the method returns void and consumes a value (print, log, store).
3. `Supplier` — use with bound instance refs or no-arg constructor refs that return a value.
4. `Function` — use with unbound instance refs (no extra args) or single-arg static/constructor refs.
5. `Predicate` — use with boolean-returning methods, static or unbound instance.
6. `BiFunction`/`Comparator` — use with unbound instance refs that take one argument, or two-arg static methods.
7. `Comparator.comparing(MethodRef)` is one of the most powerful combinations in Java.
8. `Optional.map(MethodRef)` and `Optional.ifPresent(MethodRef)` are common in modern code.
9. Stream's `filter`, `map`, `forEach`, `sorted` all accept method references via their functional interface parameters.
10. Compose multiple method references using `andThen`, `compose`, `and`, `or`, `negate` on functional interfaces.

## Relevant Java 21 Features

- **Java 8**: All standard functional interfaces and method reference types co-introduced.
- **Java 9**: `Optional.ifPresentOrElse(Consumer, Runnable)` — can use method references for both branches.
- **Java 9**: `Optional.stream()` — converts `Optional` to a stream, enabling further method reference chaining.
- **Java 11**: `Predicate.not(Predicate)` — `Predicate.not(String::isBlank)` is idiomatic for negation.
- **Java 16**: `Stream.toList()` — eliminates the need for `Collectors.toList()` in terminal operations.
- **Java 17**: Pattern matching improves the readability of switches that feed into method references.
- **Java 21**: `SequencedCollection` methods (`getFirst()`, `getLast()`) can be used as bound instance method
  references when the collection is the receiver.

## Common Pitfalls and How to Avoid Them

1. **Using a method reference where argument adaptation is needed**:

   ```java
   // Problem: need to transform the argument before calling the method
   // String::parseInt doesn't exist; we need Integer::parseInt but want to also trim
   List<String> rawNumbers = List.of("  42  ", " 7 ", "  100  ");
   // rawNumbers.stream().map(Integer::parseInt).toList(); // NumberFormatException!
   ```

   ```java
   // Fix: use a lambda when you need to adapt/transform arguments
   List<Integer> numbers = rawNumbers.stream()
           .map(String::trim)             // method reference for trimming
           .map(Integer::parseInt)        // method reference for parsing
           .toList();                     // 2-step pipeline
   ```

2. **Composing Predicates: negate a method reference**:

   ```java
   // Problem: cannot write !String::isBlank as a method reference
   // List<String> nonBlanks = list.stream().filter(!String::isBlank).toList(); // compile error
   ```

   ```java
   // Fix 1: use Predicate.not() (Java 11+) — idiomatic
   List<String> nonBlanks = list.stream().filter(Predicate.not(String::isBlank)).toList();
   // Fix 2: use lambda
   List<String> nonBlanks2 = list.stream().filter(s -> !s.isBlank()).toList();
   ```

3. **Chaining method references in `Function.andThen()`**:

   ```java
   // Problem: trying to chain without intermediate variables is verbose
   // Goal: trim, then toUpperCase, then get length
   Function<String, Integer> transform =
           ((Function<String, String>) String::trim)
               .andThen(String::toUpperCase)
               .andThen(String::length);  // works but the cast is ugly
   ```

   ```java
   // Fix: assign to a variable first for cleaner composition
   Function<String, String> prepare = ((Function<String, String>) String::trim).andThen(String::toUpperCase);
   Function<String, Integer> full = prepare.andThen(String::length);
   // Or: just use a lambda for the multi-step transformation
   Function<String, Integer> clean = s -> s.trim().toUpperCase().length();
   ```

4. **Using `Consumer.andThen()` with method references**:

   ```java
   List<String> auditLog = new ArrayList<>();
   Consumer<String> logger = auditLog::add;       // bound: adds to auditLog
   Consumer<String> printer = System.out::println; // bound: prints to stdout

   // This works correctly
   Consumer<String> both = logger.andThen(printer);
   both.accept("event");  // adds to log AND prints
   ```

5. **Ambiguous overload in `Comparator.comparing`**:

   ```java
   // Problem: if a record has a getter that conflicts with a standard Object method
   record Box(int size, String label) {}
   // Comparator.comparing(Box::size) might conflict if 'size' is also a method name
   // in a parent class — prefer method references to clearly-named methods
   Comparator<Box> bySize = Comparator.comparingInt(Box::size); // clear and unambiguous
   ```

## Best Practices and Optimization Techniques

1. **Use `Comparator.comparing(MethodRef)` for readable sort keys**: This is the single most impactful use of
   method references. It makes sort logic self-documenting.

2. **Pipeline composition**: Build complex transformations from simple method references using `Function.andThen()`,
   `Predicate.and()`, `Predicate.or()`, and `Consumer.andThen()`.

3. **Use `Predicate.not()` (Java 11+) with method references for negation**: Avoids `s -> !s.isBlank()` in favor
   of `Predicate.not(String::isBlank)`.

4. **Pass method references as parameters to promote flexibility**: Accept `Function<T,R>` or `Predicate<T>`
   parameters so callers can provide method references. This is the Strategy pattern realized via functional interfaces.

5. **Use `Stream.toArray(Type[]::new)` to collect to typed arrays**: This is idiomatic and avoids unchecked casts.

6. **Create named `Comparator` constants with method references**:

   ```java
   // In a domain class
   public static final Comparator<Employee> BY_NAME = Comparator.comparing(Employee::getName);
   public static final Comparator<Employee> BY_SALARY_DESC =
           Comparator.comparingDouble(Employee::getSalary).reversed();
   ```

## Edge Cases and Their Handling

1. **Method reference in `Optional.map()` returning `Optional`** — this creates `Optional<Optional<T>>` unless
   you use `flatMap`:

   ```java
   Optional<String> name = Optional.of("Alice");
   // map with method that returns Optional creates Optional<Optional<String>>:
   Optional<Optional<String>> doubled = name.map(Optional::of); // nested Optional — usually wrong
   // Fix: use flatMap for methods that return Optional
   Optional<String> same = name.flatMap(Optional::of); // flat — correct
   ```

2. **`BiConsumer` with `Map.forEach`**: This is the standard way to iterate a map:

   ```java
   Map<String, Integer> scores = Map.of("Alice", 95, "Bob", 87);
   // BiConsumer applied via Map.forEach
   scores.forEach((k, v) -> System.out.printf("%s: %d%n", k, v));
   // If you have a two-argument method:
   // scores.forEach(MyLogger::logEntry); // if static void logEntry(String, Integer) exists
   ```

3. **Using method references with `Collectors`**: Many `Collectors` factory methods accept functional interfaces:

   ```java
   // Collectors.groupingBy with method reference as classifier
   Map<String, List<Employee>> byDept =
           employees.stream().collect(Collectors.groupingBy(Employee::getDepartment));
   // Collectors.joining is a terminal collector, not a method reference scenario
   ```

4. **Method reference to a method with a generic return type**: Type inference usually handles this, but may
   require an explicit type parameter if the inference fails.

   ```java
   // Collections.emptyList() is generic; inference works when target type is clear
   Supplier<List<String>> empty = Collections::emptyList; // inferred as List<String>
   ```

5. **Combining method references with `flatMap`**:

   ```java
   List<List<String>> nested = List.of(List.of("a","b"), List.of("c","d"));
   // Flatten using method reference — Collection::stream is an unbound instance method ref
   List<String> flat = nested.stream()
           .flatMap(Collection::stream)     // each List -> Stream
           .collect(Collectors.toList());
   ```

## Interview-specific Insights

Interviewers focus on:

- Writing a complete stream pipeline using method references exclusively (no lambdas) for a given requirement.
- Explaining `Comparator.comparing(Person::getName)` — what is the method reference type and how does `comparing` work?
- Using `Predicate.not()` and `Predicate.and()`/`or()` with method references.
- Recognizing when a method reference cannot replace a lambda (multi-step logic, argument adaptation).
- The `Stream.toArray(String[]::new)` idiom.
- Composing `Consumer` pipelines with `andThen` and method references for audit/logging patterns.

**Common tricky questions:**

- "Write a stream pipeline that reads a list of strings, filters out nulls and blanks, converts to integers,
  removes duplicates, sorts descending, and collects to a list — use method references as much as possible."
- "How do you negate a method reference used as a `Predicate`?"
- "How does `Comparator.comparing(Person::getName).reversed()` work? What does `reversed()` return?"

## Interview Q&A Section

**Q1: How do you use method references with `Consumer` in a stream pipeline? Show a chained example.**

```text
A1: Consumer<T> is the functional interface for stream's terminal forEach operation and for
andThen-chained side effects. You can use any method reference that takes one argument and
returns void as a Consumer.

Common patterns:
- System.out::println — bound instance ref to println, used as Consumer<Object>
- list::add — bound instance ref to add, used as Consumer<T> for collecting side effects
- Logger::log — static method ref if log(String) is static
- MyService::processItem — bound or unbound depending on context

andThen chains multiple Consumers, all receiving the same input.
```

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

record Order(String id, double amount, String customer) {}

public class ConsumerMethodRefDemo {

    static List<String> auditLog = new ArrayList<>();
    static List<Order> processedOrders = new ArrayList<>();

    static void auditOrder(Order o) {
        auditLog.add("PROCESSED: " + o.id() + " for " + o.customer());
    }

    public static void main(String[] args) {
        List<Order> orders = List.of(
                new Order("O1", 150.0, "Alice"),
                new Order("O2", 75.0, "Bob"),
                new Order("O3", 200.0, "Charlie")
        );

        // Chain: audit → store → print
        Consumer<Order> pipeline = ConsumerMethodRefDemo::auditOrder  // static method ref
                .andThen(processedOrders::add)                        // bound instance ref
                .andThen(o -> System.out.println("Done: " + o.id())); // lambda for complex action

        orders.stream()
                .filter(o -> o.amount() > 100)
                .forEach(pipeline);

        System.out.println("Audit: " + auditLog);
        System.out.println("Processed: " + processedOrders.size());
    }
}
```

---

**Q2: How do you use method references with `Comparator.comparing()`? Show a multi-level sort.**

```text
A2: Comparator.comparing(MethodRef) is one of the most powerful and idiomatic Java idioms.
It creates a Comparator that extracts a key using the method reference and compares by that key.

For multi-level sort: chain .thenComparing(MethodRef) after the primary Comparator.
For reverse sort: call .reversed() on the Comparator.
For numeric keys: use comparingInt, comparingLong, comparingDouble to avoid boxing.

The method reference provided to comparing() acts as a key extractor:
it maps the element type T to a Comparable R, and the Comparator compares by R.
```

```java
import java.util.*;
import java.util.stream.*;

record Employee(String name, String department, double salary, int yearsOfService) {}

public class ComparatorMethodRefDemo {

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice", "Engineering", 95000, 5),
                new Employee("Bob", "Marketing", 75000, 3),
                new Employee("Charlie", "Engineering", 88000, 8),
                new Employee("Diana", "Marketing", 82000, 5),
                new Employee("Eve", "Engineering", 95000, 3)
        );

        // Single-key sort by name (ascending)
        Comparator<Employee> byName = Comparator.comparing(Employee::name);

        // Multi-level: department asc, salary desc, name asc
        Comparator<Employee> multiLevel =
                Comparator.comparing(Employee::department)           // primary: dept asc
                        .thenComparingDouble(Employee::salary)       // secondary: salary asc
                        .reversed()                                  // whole chain reversed
                        .thenComparing(Employee::name);              // tertiary: name asc (not reversed)

        List<Employee> sorted = employees.stream()
                .sorted(Comparator.comparing(Employee::department)
                        .thenComparingDouble(Employee::salary).reversed()
                        .thenComparing(Employee::name))
                .collect(Collectors.toList());

        // Easiest pattern: step by step
        List<Employee> byDeptThenSalary = employees.stream()
                .sorted(Comparator.comparing(Employee::department)
                        .thenComparingDouble(Employee::salary))
                .collect(Collectors.toList());

        byDeptThenSalary.forEach(e ->
                System.out.printf("%-10s %-12s %.0f%n",
                        e.name(), e.department(), e.salary()));
    }
}
```

---

**Q3: How do you use method references with `Predicate` for filtering, including negation and composition?**

```text
A3: Predicate<T> is the functional interface used by Stream.filter(). Any method reference
returning a boolean that takes one argument of the stream's element type works as a Predicate.

For negation: use Predicate.not(methodRef) (Java 11+) or s -> !method(s) lambda.
For AND composition: predicate1.and(predicate2) — both must be true.
For OR composition: predicate1.or(predicate2) — at least one must be true.
These can all use method references.
```

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

record User(String username, String email, boolean verified, int age) {}

public class PredicateMethodRefDemo {

    static boolean isAdult(User u) { return u.age() >= 18; }
    static boolean hasEmail(User u) { return u.email() != null && !u.email().isBlank(); }

    public static void main(String[] args) {
        List<User> users = List.of(
                new User("alice", "alice@x.com", true, 25),
                new User("bob", "", false, 30),
                new User("charlie", "c@x.com", false, 16),
                new User("diana", "d@x.com", true, 22)
        );

        // Unbound instance method ref as Predicate
        Predicate<User> isVerified = User::verified;

        // Static method ref as Predicate
        Predicate<User> isAdult = PredicateMethodRefDemo::isAdult;
        Predicate<User> hasEmail = PredicateMethodRefDemo::hasEmail;

        // Negation with Predicate.not (Java 11+)
        Predicate<User> isUnverified = Predicate.not(User::verified);

        // Composition: adult AND verified AND has email
        Predicate<User> eligible = isAdult.and(isVerified).and(hasEmail);

        List<User> eligibleUsers = users.stream()
                .filter(eligible)
                .collect(Collectors.toList());
        System.out.println("Eligible: " + eligibleUsers.stream().map(User::username).toList());
        // [alice, diana]

        // Filtering with Objects::nonNull
        List<String> words = Arrays.asList("hello", null, "", "world", null, "  ");
        List<String> cleanWords = words.stream()
                .filter(Objects::nonNull)             // static method ref for null check
                .filter(Predicate.not(String::isBlank)) // negated unbound instance method ref
                .collect(Collectors.toList());
        System.out.println("Clean: " + cleanWords); // [hello, world]
    }
}
```

---

**Q4: How do you use method references with `Function` for transformation, including `andThen` and `compose`?**

```text
A4: Function<T,R> is the core transformation interface used by Stream.map(). Method references
map to Function when the referenced method takes one argument and returns a value.

andThen(after): applies this function first, then passes the result to 'after'.
  result = after.apply(this.apply(input))

compose(before): applies 'before' first, then this function.
  result = this.apply(before.apply(input))

andThen reads left-to-right (pipeline order), while compose reads right-to-left (mathematical).
Most developers prefer andThen because it matches the mental model of a pipeline.
```

```java
import java.util.function.*;
import java.util.stream.*;

public class FunctionMethodRefDemo {

    static String removeWhitespace(String s) { return s.replaceAll("\\s+", ""); }
    static int parseOrDefault(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    public static void main(String[] args) {
        // Basic Function method references
        Function<String, String> trim = String::trim;          // unbound instance ref
        Function<String, String> clean = FunctionMethodRefDemo::removeWhitespace; // static ref
        Function<String, Integer> parse = FunctionMethodRefDemo::parseOrDefault;  // static ref
        Function<String, Integer> length = String::length;    // unbound instance ref

        // Composing with andThen (left-to-right pipeline)
        Function<String, Integer> pipeline = trim.andThen(clean).andThen(parse);
        System.out.println(pipeline.apply("  42  ")); // 42

        // andThen with constructor reference
        Function<String, StringBuilder> sbPipeline =
                ((Function<String, String>) String::trim).andThen(StringBuilder::new);
        StringBuilder sb = sbPipeline.apply("  hello  ");
        System.out.println(sb.toString()); // "hello"

        // In a stream
        var result = java.util.List.of("  1  ", " 2 ", "  3  ").stream()
                .map(String::trim)          // method ref step 1
                .map(Integer::parseInt)     // method ref step 2
                .map(n -> n * n)            // lambda for computation
                .collect(Collectors.toList());
        System.out.println(result); // [1, 4, 9]
    }
}
```

---

**Q5: How do you use `BiFunction` and `Comparator` method references for complex operations?**

```text
A5: BiFunction<T,U,R> takes two arguments and returns a value. Unbound instance method references
with one additional parameter naturally map to BiFunction. Two-argument static methods also map
to BiFunction.

Comparator<T> is a special case of BiFunction<T,T,Integer> with additional utilities. The
Comparator interface integrates deeply with method references via comparing(), thenComparing(),
comparingInt(), comparingLong(), comparingDouble() — all of which accept method references
as key extractors.

BiFunction.andThen() chains the transformation:
  BiFunction<T,U,R> biFunc;
  Function<R,V> after;
  BiFunction<T,U,V> result = biFunc.andThen(after);
```

```java
import java.util.function.*;
import java.util.*;
import java.util.stream.*;

record Point(double x, double y) {
    double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

public class BiFunctionMethodRefDemo {

    // Two-argument static method → BiFunction
    static String formatEntry(String key, Integer value) {
        return key + "=" + value;
    }

    public static void main(String[] args) {
        // Static two-arg method → BiFunction
        BiFunction<String, Integer, String> format = BiFunctionMethodRefDemo::formatEntry;
        System.out.println(format.apply("score", 95)); // "score=95"

        // Unbound instance method with one arg → BiFunction
        // String::concat takes (String receiver, String arg) → String
        BiFunction<String, String, String> concat = String::concat;
        System.out.println(concat.apply("Hello, ", "World!")); // "Hello, World!"

        // BiFunction.andThen: chain a Function after a BiFunction
        BiFunction<String, String, Integer> combinedLength =
                ((BiFunction<String, String, String>) String::concat)
                        .andThen(String::length);
        System.out.println(combinedLength.apply("Hello", "World")); // 10

        // Unbound instance method as BiFunction: Point::distanceTo
        BiFunction<Point, Point, Double> distance = Point::distanceTo;
        Point p1 = new Point(0, 0), p2 = new Point(3, 4);
        System.out.println(distance.apply(p1, p2)); // 5.0

        // Comparator with method refs for multi-key sorting
        List<Point> points = List.of(new Point(3, 4), new Point(1, 2), new Point(3, 1));
        List<Point> sorted = points.stream()
                .sorted(Comparator.comparingDouble(Point::x).thenComparingDouble(Point::y))
                .collect(Collectors.toList());
        sorted.forEach(p -> System.out.printf("(%.0f, %.0f)%n", p.x(), p.y()));
        // (1, 2), (3, 1), (3, 4)
    }
}
```

---

**Q6: How do you build a completely method-reference-based data processing pipeline?**

```text
A6: A fully method-reference-based pipeline is achievable when each transformation step
corresponds to a named method. The key pattern is:

1. Filter with Predicate method references (Objects::nonNull, ClassName::isValid)
2. Transform with Function method references (ClassName::extractField, Type::convert)
3. Sort with Comparator.comparing(MethodRef)
4. Collect with standard terminal operations

When computation or multi-step logic is needed, extract to a named static or instance method
and reference it. This keeps the pipeline composed entirely of named operations.

This approach has benefits:
- Each method reference is independently testable
- The pipeline reads like a business description
- New requirements can be added by inserting new method references
```

```java
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

record Product(String name, String category, double price, int stock, boolean active) {}

public class FullPipelineDemo {

    // Named predicates as static methods → used as method references
    static boolean isActive(Product p) { return p.active(); }
    static boolean inStock(Product p) { return p.stock() > 0; }
    static boolean isPremium(Product p) { return p.price() > 100.0; }

    // Named transformer
    static String formatProduct(Product p) {
        return String.format("%s ($%.2f, stock: %d)", p.name(), p.price(), p.stock());
    }

    public static List<String> getPremiumInStockByPrice(List<Product> products) {
        return products.stream()
                .filter(Objects::nonNull)                  // null guard: static ref
                .filter(FullPipelineDemo::isActive)        // static method ref: isActive
                .filter(FullPipelineDemo::inStock)         // static method ref: inStock
                .filter(FullPipelineDemo::isPremium)       // static method ref: isPremium
                .sorted(Comparator.comparingDouble(Product::price))   // sort by price
                .map(FullPipelineDemo::formatProduct)      // transform: static method ref
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Product> catalog = List.of(
                new Product("Widget A", "Hardware", 150.0, 10, true),
                new Product("Gadget B", "Electronics", 80.0, 5, true),
                new Product("Tool C", "Hardware", 200.0, 0, true),  // out of stock
                new Product("Device D", "Electronics", 120.0, 3, false), // inactive
                new Product("Part E", "Hardware", 175.0, 7, true)
        );

        List<String> premium = getPremiumInStockByPrice(catalog);
        premium.forEach(System.out::println);
        // Widget A ($150.00, stock: 10)
        // Part E ($175.00, stock: 7)
    }
}
```

---

**Q7: How do you use array constructor references with `Stream.toArray()`?**

```text
A7: Stream.toArray(IntFunction<T[]>) collects stream elements into a typed array.
The argument is an IntFunction that takes the array size (int) and returns a new array of
that size.

Array constructor references (Type[]::new) are the idiomatic way to supply this factory.
They avoid unchecked casts (unlike .toArray() with a raw Object[] return) and are more
readable than the equivalent lambda i -> new Type[i].

This works for any reference type. For primitive streams (IntStream, LongStream, DoubleStream),
the no-argument .toArray() already returns a primitive array.
```

```java
import java.util.stream.*;
import java.util.function.*;

public class ArrayConstructorRefDemo {

    record Employee(String name, double salary) {}

    public static void main(String[] args) {
        // Standard usage: collect to typed String array
        String[] names = Stream.of("Alice", "Bob", "Charlie")
                .map(String::toUpperCase)
                .toArray(String[]::new);     // array constructor reference
        System.out.println(Arrays.toString(names)); // [ALICE, BOB, CHARLIE]

        // Without constructor reference (less idiomatic):
        String[] names2 = Stream.of("Alice", "Bob", "Charlie")
                .map(String::toUpperCase)
                .toArray(i -> new String[i]);   // equivalent lambda
        System.out.println(Arrays.toString(names2)); // same result

        // With domain objects
        Employee[] employees = Stream.of(
                new Employee("Alice", 90000),
                new Employee("Bob", 75000))
                .sorted(Comparator.comparing(Employee::name))
                .toArray(Employee[]::new);     // Employee constructor ref for array
        System.out.println(employees[0].name()); // Alice

        // IntFunction stands alone for array creation
        IntFunction<double[]> doubleArrayFactory = double[]::new;
        double[] arr = doubleArrayFactory.apply(5); // new double[5]
        System.out.println(arr.length); // 5
    }
}
```

## Code Examples

- Source: [MethodReferencesWithFunctionalInterfaces.java](src/main/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferencesWithFunctionalInterfaces.java)
- Test: [MethodReferencesWithFunctionalInterfacesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferencesWithFunctionalInterfacesTest.java)
