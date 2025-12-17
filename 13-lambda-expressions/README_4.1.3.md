# 4.1.3. Using Lambda Expressions with Functional Interfaces

## Concept Explanation

Functional interfaces are the foundation of lambda expressions in Java. A functional interface is an interface with
exactly one abstract method (SAM - Single Abstract Method). Lambda expressions can only be used where a functional
interface is expected, making functional interfaces the "target type" for lambdas.

Java provides many built-in functional interfaces in the `java.util.function` package, and you can create custom
functional interfaces for specific needs. The `@FunctionalInterface` annotation helps document this intent and prevents
accidentally adding additional abstract methods.

**Real-world analogy**: Think of a functional interface as a job description with one main responsibility. A lambda
expression is like hiring a contractor to do that specific job. The interface defines what needs to be done (method
signature), and the lambda provides how to do it (implementation).

## Key Points to Remember

1. Functional interfaces have exactly one abstract method.
2. They can have multiple default methods and static methods.
3. Methods inherited from Object class don't count toward the abstract method count.
4. The `@FunctionalInterface` annotation is optional but recommended.
5. Lambda expressions provide implementation for the functional interface's abstract method.
6. Java provides many standard functional interfaces in `java.util.function` package.
7. Custom functional interfaces can be created for domain-specific needs.

## Relevant Java 21 Features

Java 21 continues to enhance functional programming capabilities:

- **Improved type inference**: Better handling of complex functional interface scenarios.
- **Pattern matching**: Functional interfaces work seamlessly with modern pattern matching.
- **Sequenced collections**: New functional interfaces for sequenced collection operations.
- **Virtual threads**: Functional interfaces integrate well with Project Loom's virtual threads.

## Common Pitfalls and How to Avoid Them

1. **Using wrong functional interface type**:
   ```java
   // Wrong: Using Function when you need Consumer
   Function<String, Void> wrong = s -> {
       System.out.println(s);
       return null;  // Awkward!
   };

   // Correct: Use Consumer for side effects
   Consumer<String> correct = s -> System.out.println(s);
   ```

2. **Forgetting @FunctionalInterface annotation**:
   ```java
   // Without annotation, accidentally adding methods goes unnoticed
   interface MyInterface {  // No annotation - risky!
       void doSomething();
       void doSomethingElse();  // Oops! Not functional anymore
   }

   // With annotation, compiler catches the error
   @FunctionalInterface
   interface MyInterface {
       void doSomething();
       // void doSomethingElse();  // Compilation error!
   }
   ```

3. **Not understanding generic type parameters**:
   ```java
   // Confusing input/output types
   Function<String, Integer> length = s -> s.length();  // String -> Integer
   Function<Integer, String> format = i -> String.valueOf(i);  // Integer -> String
   ```

4. **Using functional interfaces when method references would be clearer**:
   ```java
   // Less clear
   list.forEach(item -> System.out.println(item));

   // More clear
   list.forEach(System.out::println);
   ```

## Best Practices and Optimization Techniques

1. **Choose the right functional interface**:
    - `Consumer<T>`: Takes input, returns nothing
    - `Supplier<T>`: Takes nothing, returns output
    - `Function<T, R>`: Takes input, returns output
    - `Predicate<T>`: Takes input, returns boolean

2. **Use standard functional interfaces when possible**:
   ```java
   // Don't create custom interface for common cases
   // Instead of:
   @FunctionalInterface
   interface StringProcessor {
       String process(String s);
   }

   // Use standard:
   Function<String, String> processor = s -> s.toUpperCase();
   ```

3. **Create custom functional interfaces for domain-specific operations**:
   ```java
   @FunctionalInterface
   interface OrderValidator {
       boolean validate(Order order);

       default OrderValidator and(OrderValidator other) {
           return order -> this.validate(order) && other.validate(order);
       }
   }
   ```

4. **Compose functional interfaces**:
   ```java
   Predicate<String> startsWithA = s -> s.startsWith("A");
   Predicate<String> longerThan5 = s -> s.length() > 5;
   Predicate<String> combined = startsWithA.and(longerThan5);
   ```

5. **Cache lambda instances when reusable**:
   ```java
   // Don't recreate lambdas unnecessarily
   private static final Comparator<String> LENGTH_COMPARATOR =
       (s1, s2) -> Integer.compare(s1.length(), s2.length());

   public void sort(List<String> list) {
       list.sort(LENGTH_COMPARATOR);  // Reuse
   }
   ```

## Edge Cases and Their Handling

1. **Functional interfaces with generic wildcards**:
   ```java
   Function<? super String, ? extends Number> complex;
   // Understanding when to use bounded wildcards
   ```

2. **Functional interfaces with primitive specializations**:
   ```java
   // Use IntFunction instead of Function<Integer, R> for better performance
   IntFunction<String> formatter = i -> "Number: " + i;
   ```

3. **Functional interfaces throwing checked exceptions**:
   ```java
   @FunctionalInterface
   interface ThrowingFunction<T, R> {
       R apply(T t) throws Exception;
   }
   ```

4. **Multiple abstract methods from different interfaces**:
   ```java
   // This is still functional if only one method is abstract
   interface MyInterface extends Runnable, Cloneable {
       // Only run() is abstract (clone() has default impl from Object)
   }
   ```

## Interview-specific Insights

Interviewers often focus on:

- Understanding the definition and purpose of functional interfaces
- Knowledge of standard functional interfaces in `java.util.function`
- Ability to choose the right functional interface for a given scenario
- Understanding the relationship between lambda expressions and functional interfaces
- Knowledge of functional interface composition and chaining
- Understanding primitive specializations (IntPredicate, LongFunction, etc.)

Common tricky questions:

- "Can an interface with multiple methods be a functional interface?"
- "What's the difference between Consumer and Function?"
- "Why would you create a custom functional interface instead of using standard ones?"
- "How do default methods affect functional interfaces?"

## Interview Q&A Section

**Q1: What is a functional interface and how does it relate to lambda expressions?**

```text
A1: A functional interface is an interface that contains exactly one abstract method (SAM - Single Abstract Method). It serves as the contract that lambda expressions implement.

Key characteristics:
1. **Exactly one abstract method**: This is the method the lambda will implement.
2. **Can have default methods**: These don't count as abstract methods.
3. **Can have static methods**: These also don't count.
4. **Inherits methods from Object**: equals(), hashCode(), toString() don't count.

Relationship with lambda expressions:
- Lambda expressions can only be used where a functional interface is expected
- The functional interface's method signature determines the lambda's parameters and return type
- The compiler uses the functional interface as the "target type" to infer lambda parameter types
- Each lambda expression is an implementation of the functional interface's abstract method
```

```java
// Example demonstrating the relationship
public class FunctionalInterfaceExample {

    // 1. Defining a functional interface
    @FunctionalInterface
    interface Calculator {
        int calculate(int a, int b);  // Single abstract method

        // Can have default methods
        default int square(int n) {
            return calculate(n, n);  // Uses the abstract method
        }

        // Can have static methods
        static int negate(int n) {
            return -n;
        }
    }

    public void demonstrateRelationship() {
        // Lambda provides implementation for calculate method
        Calculator addition = (a, b) -> a + b;
        Calculator multiplication = (a, b) -> a * b;
        Calculator power = (a, b) -> (int) Math.pow(a, b);

        // Using the lambdas
        System.out.println(addition.calculate(5, 3));      // 8
        System.out.println(multiplication.calculate(5, 3)); // 15
        System.out.println(power.calculate(2, 3));          // 8

        // Using default method
        System.out.println(addition.square(5));             // 25 (5+5=10? No, square uses calculate(n,n))

        // Using static method
        System.out.println(Calculator.negate(10));          // -10
    }

    // 2. Using standard functional interfaces
    public void standardInterfaces() {
        // Function<T, R>: Takes T, returns R
        Function<String, Integer> length = s -> s.length();

        // Consumer<T>: Takes T, returns nothing
        Consumer<String> printer = s -> System.out.println(s);

        // Supplier<T>: Takes nothing, returns T
        Supplier<Double> random = () -> Math.random();

        // Predicate<T>: Takes T, returns boolean
        Predicate<Integer> isEven = n -> n % 2 == 0;

        // BiFunction<T, U, R>: Takes T and U, returns R
        BiFunction<String, String, String> concat = (s1, s2) -> s1 + s2;
    }
}
```

**Q2: What are the standard functional interfaces in Java and when should you use each?**

```java
public class StandardFunctionalInterfaces {

    // 1. Consumer<T> - Takes input, performs action, returns nothing
    public void demonstrateConsumer() {
        Consumer<String> print = s -> System.out.println(s);
        Consumer<String> logAndPrint = s -> {
            System.err.println("Logging: " + s);
            System.out.println(s);
        };

        List<String> items = List.of("A", "B", "C");
        items.forEach(print);  // Common use with forEach

        // Chaining consumers
        Consumer<String> combined = print.andThen(logAndPrint);
    }

    // 2. Supplier<T> - Takes no input, returns output
    public void demonstrateSupplier() {
        Supplier<String> uuidGenerator = () -> UUID.randomUUID().toString();
        Supplier<LocalDateTime> now = LocalDateTime::now;
        Supplier<List<String>> listSupplier = ArrayList::new;

        // Common use: lazy initialization
        String id = uuidGenerator.get();

        // Common use: factory methods
        List<String> list = listSupplier.get();
    }

    // 3. Function<T, R> - Takes input, returns output (transformation)
    public void demonstrateFunction() {
        Function<String, Integer> length = s -> s.length();
        Function<Integer, String> format = i -> "Number: " + i;
        Function<String, String> upper = String::toUpperCase;

        // Chaining functions
        Function<String, String> lengthMessage =
                length.andThen(format);  // String -> Integer -> String

        // Composing functions
        Function<String, Integer> upperThenLength =
                upper.andThen(String::length);  // First upper, then length

        System.out.println(lengthMessage.apply("hello"));  // "Number: 5"
    }

    // 4. Predicate<T> - Takes input, returns boolean (testing)
    public void demonstratePredicate() {
        Predicate<String> isEmpty = String::isEmpty;
        Predicate<String> startsWithA = s -> s.startsWith("A");
        Predicate<Integer> isPositive = n -> n > 0;

        // Combining predicates
        Predicate<String> notEmpty = isEmpty.negate();
        Predicate<String> startsWithAAndNotEmpty =
                startsWithA.and(notEmpty);

        List<String> names = List.of("Alice", "Bob", "", "Alex");
        names.stream()
                .filter(startsWithAAndNotEmpty)
                .forEach(System.out::println);  // Alice, Alex
    }

    // 5. BiFunction<T, U, R> - Takes two inputs, returns output
    public void demonstrateBiFunction() {
        BiFunction<String, String, String> concat = (s1, s2) -> s1 + s2;
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);

        String result = repeat.apply("Hello", 3);  // "HelloHelloHello"

        // andThen for further transformation
        BiFunction<Integer, Integer, String> addAndFormat =
                add.andThen(i -> "Result: " + i);
    }

    // 6. UnaryOperator<T> - Special case of Function where input and output are same type
    public void demonstrateUnaryOperator() {
        UnaryOperator<String> toUpper = s -> s.toUpperCase();
        UnaryOperator<Integer> square = n -> n * n;
        UnaryOperator<String> trim = String::trim;

        // Chaining unary operators
        UnaryOperator<String> trimAndUpper = trim.andThen(toUpper);
        String result = trimAndUpper.apply("  hello  ");  // "HELLO"
    }

    // 7. BinaryOperator<T> - Special case of BiFunction where both inputs and output are same type
    public void demonstrateBinaryOperator() {
        BinaryOperator<Integer> max = (a, b) -> a > b ? a : b;
        BinaryOperator<String> concat = (s1, s2) -> s1 + s2;
        BinaryOperator<Integer> multiply = (a, b) -> a * b;

        // Common use: reduce operations
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        int product = numbers.stream()
                .reduce(1, multiply);  // 120

        // Using method reference
        int maximum = numbers.stream()
                .reduce(Integer::max)
                .orElse(0);
    }

    // 8. Primitive specializations (better performance, avoid boxing)
    public void demonstratePrimitiveSpecializations() {
        // IntPredicate instead of Predicate<Integer>
        IntPredicate isEven = n -> n % 2 == 0;

        // IntFunction<R> instead of Function<Integer, R>
        IntFunction<String> formatter = n -> "Number: " + n;

        // ToIntFunction<T> instead of Function<T, Integer>
        ToIntFunction<String> length = String::length;

        // IntConsumer instead of Consumer<Integer>
        IntConsumer printer = n -> System.out.println(n);

        // IntSupplier instead of Supplier<Integer>
        IntSupplier random = () -> ThreadLocalRandom.current().nextInt(100);

        // IntBinaryOperator instead of BinaryOperator<Integer>
        IntBinaryOperator adder = (a, b) -> a + b;

        // Similar specializations exist for long and double
        LongPredicate isPositiveLong = n -> n > 0;
        DoublePredicate isPositiveDouble = d -> d > 0.0;
    }
}
```

```text
A2 (continued): Guidelines for choosing:

Consumer<T>: Use when you need side effects (printing, logging, modifying external state)
- forEach operations
- Logging and monitoring
- Database updates

Supplier<T>: Use for lazy initialization and factory methods
- Generating values on demand
- Factory patterns
- Lazy loading

Function<T, R>: Use for transformations and mapping
- Stream map operations
- Data transformation pipelines
- Type conversions

Predicate<T>: Use for filtering and testing conditions
- Stream filter operations
- Validation logic
- Conditional processing

BiFunction<T, U, R>: Use when you need two inputs
- Combining two values
- Map merge operations
- Two-argument transformations

UnaryOperator<T>: Use for in-place transformations
- Modifying values of the same type
- replaceAll operations

BinaryOperator<T>: Use for combining two values of the same type
- Reduce operations
- Accumulation
- Finding min/max

Primitive specializations: Use for performance with primitives
- Avoiding boxing/unboxing overhead
- High-performance computations
- Stream operations on primitive streams
```

**Q3: How do you create and use custom functional interfaces?**

```java
public class CustomFunctionalInterfaces {

    // 1. Simple custom functional interface
    @FunctionalInterface
    interface StringTransformer {
        String transform(String input);

        // Can have default methods
        default StringTransformer andThen(StringTransformer after) {
            return input -> after.transform(this.transform(input));
        }
    }

    // 2. Custom functional interface with domain-specific semantics
    @FunctionalInterface
    interface OrderValidator {
        boolean validate(Order order);

        // Combining validators
        default OrderValidator and(OrderValidator other) {
            return order -> this.validate(order) && other.validate(order);
        }

        default OrderValidator or(OrderValidator other) {
            return order -> this.validate(order) || other.validate(order);
        }

        default OrderValidator negate() {
            return order -> !this.validate(order);
        }
    }

    // 3. Custom functional interface with checked exception
    @FunctionalInterface
    interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;

        // Helper method to wrap in unchecked exception
        static <T, R> Function<T, R> wrap(CheckedFunction<T, R> checked) {
            return t -> {
                try {
                    return checked.apply(t);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    // 4. Custom functional interface for specific business logic
    @FunctionalInterface
    interface PriceCalculator {
        double calculate(double basePrice, int quantity, Customer customer);

        // Default implementations for common scenarios
        static PriceCalculator standard() {
            return (basePrice, quantity, customer) -> basePrice * quantity;
        }

        static PriceCalculator withDiscount(double discountPercent) {
            return (basePrice, quantity, customer) ->
                    basePrice * quantity * (1 - discountPercent / 100);
        }

        static PriceCalculator withCustomerTier() {
            return (basePrice, quantity, customer) -> {
                double tierDiscount = customer.getTierDiscount();
                return basePrice * quantity * (1 - tierDiscount);
            };
        }
    }

    // Using custom functional interfaces
    public void useCustomInterfaces() {
        // 1. StringTransformer
        StringTransformer toUpper = s -> s.toUpperCase();
        StringTransformer addPrefix = s -> "PREFIX_" + s;
        StringTransformer combined = toUpper.andThen(addPrefix);

        String result = combined.transform("hello");  // "PREFIX_HELLO"

        // 2. OrderValidator
        OrderValidator hasItems = order -> !order.getItems().isEmpty();
        OrderValidator hasValidTotal = order -> order.getTotal() > 0;
        OrderValidator hasCustomer = order -> order.getCustomer() != null;

        OrderValidator fullValidator = hasItems
                .and(hasValidTotal)
                .and(hasCustomer);

        Order order = new Order();
        boolean isValid = fullValidator.validate(order);

        // 3. CheckedFunction
        CheckedFunction<String, Integer> parser = Integer::parseInt;
        Function<String, Integer> safeParser = CheckedFunction.wrap(parser);

        List<String> numbers = List.of("1", "2", "abc", "4");
        numbers.stream()
                .map(safeParser)  // Wrapped checked exception
                .forEach(System.out::println);

        // 4. PriceCalculator
        PriceCalculator standard = PriceCalculator.standard();
        PriceCalculator withDiscount = PriceCalculator.withDiscount(10);
        PriceCalculator tierBased = PriceCalculator.withCustomerTier();

        Customer vipCustomer = new Customer("VIP");
        double price = tierBased.calculate(100.0, 5, vipCustomer);
    }

    // 5. Functional interface with multiple type parameters
    @FunctionalInterface
    interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);

        default <W> TriFunction<T, U, V, W> andThen(Function<R, W> after) {
            return (t, u, v) -> after.apply(this.apply(t, u, v));
        }
    }

    public void useTriFunction() {
        TriFunction<String, Integer, Boolean, String> formatter =
                (text, count, upper) -> {
                    String repeated = text.repeat(count);
                    return upper ? repeated.toUpperCase() : repeated;
                };

        String result = formatter.apply("hi", 3, true);  // "HIHIHI"

        // Chaining with andThen
        TriFunction<String, Integer, Boolean, Integer> repeaterThenLength =
                formatter.andThen(String::length);

        int length = repeaterThenLength.apply("hi", 3, true);  // 6
    }

    // Supporting classes
    static class Order {
        private List<String> items = new ArrayList<>();
        private double total;
        private Customer customer;

        List<String> getItems() {
            return items;
        }

        double getTotal() {
            return total;
        }

        Customer getCustomer() {
            return customer;
        }
    }

    static class Customer {
        private String tier;

        Customer(String tier) {
            this.tier = tier;
        }

        double getTierDiscount() {
            return switch (tier) {
                case "VIP" -> 0.20;
                case "Gold" -> 0.10;
                default -> 0.0;
            };
        }
    }
}
```

```text
A3: Guidelines for creating custom functional interfaces:

When to create custom interfaces:
1. **Domain-specific semantics**: When standard interfaces don't convey meaning
   - OrderValidator is clearer than Predicate<Order>
   - PriceCalculator is clearer than TriFunction

2. **Checked exceptions**: When you need to throw checked exceptions
   - Create interfaces that declare throws clause
   - Provide wrapper methods for standard functional interfaces

3. **Multiple parameters**: When you need more than two parameters
   - Standard interfaces only go up to BiFunction
   - Create TriFunction, QuadFunction, etc. as needed

4. **Complex composition**: When you want specific default methods
   - Custom chaining methods (and, or, negate)
   - Domain-specific transformations

Best practices:
1. Always use @FunctionalInterface annotation
2. Provide meaningful names that convey intent
3. Add useful default methods for composition
4. Provide static factory methods for common cases
5. Consider if a standard interface would suffice first
6. Document the interface's purpose and contracts

When NOT to create custom interfaces:
- Simple transformations (use Function)
- Simple conditions (use Predicate)
- Simple side effects (use Consumer)
- Value generation (use Supplier)

The key is balancing expressiveness with simplicity!
```

**Q4: How do functional interfaces compose and chain together?**

```java
public class FunctionalInterfaceComposition {

    // 1. Function composition with andThen and compose
    public void demonstrateFunctionComposition() {
        Function<String, String> trim = String::trim;
        Function<String, String> toLowerCase = String::toLowerCase;
        Function<String, Integer> length = String::length;
        Function<Integer, String> format = n -> "Length: " + n;

        // andThen: execute this function, then the next
        Function<String, String> trimThenLower = trim.andThen(toLowerCase);
        String result1 = trimThenLower.apply("  HELLO  ");  // "hello"

        // Chaining multiple functions
        Function<String, String> pipeline = trim
                .andThen(toLowerCase)
                .andThen(length)
                .andThen(format);

        String result2 = pipeline.apply("  HELLO  ");  // "Length: 5"

        // compose: execute the parameter first, then this function
        Function<String, Integer> lowerThenLength =
                length.compose(toLowerCase);  // First lower, then length

        // Difference between andThen and compose:
        // f.andThen(g): f(x) then g(result) = g(f(x))
        // f.compose(g): g(x) then f(result) = f(g(x))
    }

    // 2. Predicate composition with and, or, negate
    public void demonstratePredicateComposition() {
        Predicate<String> notNull = Objects::nonNull;
        Predicate<String> notEmpty = s -> !s.isEmpty();
        Predicate<String> startsWithA = s -> s.startsWith("A");
        Predicate<String> longerThan5 = s -> s.length() > 5;

        // and: both must be true
        Predicate<String> valid = notNull.and(notEmpty);

        // or: at least one must be true
        Predicate<String> startsWithAOrLong = startsWithA.or(longerThan5);

        // negate: invert the result
        Predicate<String> isEmpty = notEmpty.negate();

        // Complex combinations
        Predicate<String> complex = notNull
                .and(notEmpty)
                .and(startsWithA.or(longerThan5));

        List<String> names = List.of("Alice", "Bob", "Alexander", null, "", "Amy");
        List<String> filtered = names.stream()
                .filter(complex)
                .collect(Collectors.toList());
        // Result: ["Alice", "Alexander", "Amy"]
    }

    // 3. Consumer composition with andThen
    public void demonstrateConsumerComposition() {
        Consumer<String> print = System.out::println;
        Consumer<String> log = s -> System.err.println("LOG: " + s);
        Consumer<String> writeToFile = s -> {
            // Simulate file writing
            System.out.println("Writing to file: " + s);
        };

        // Execute consumers in sequence
        Consumer<String> pipeline = print
                .andThen(log)
                .andThen(writeToFile);

        pipeline.accept("Hello");
        // Output:
        // Hello
        // LOG: Hello
        // Writing to file: Hello
    }

    // 4. Comparator composition
    public void demonstrateComparatorComposition() {
        List<Person> people = List.of(
                new Person("Alice", 30, "Engineering"),
                new Person("Bob", 25, "Sales"),
                new Person("Charlie", 30, "Engineering"),
                new Person("David", 25, "Engineering")
        );

        // Simple comparators
        Comparator<Person> byAge = Comparator.comparingInt(Person::getAge);
        Comparator<Person> byName = Comparator.comparing(Person::getName);
        Comparator<Person> byDepartment = Comparator.comparing(Person::getDepartment);

        // Chaining: thenComparing
        Comparator<Person> ageT henName = byAge.thenComparing(byName);
        people.sort(ageThenName);
        // Sorted by age, then by name for same age

        // Complex sorting
        Comparator<Person> complex = Comparator
                .comparing(Person::getDepartment)
                .thenComparingInt(Person::getAge)
                .thenComparing(Person::getName);

        people.sort(complex);
        // Sorted by department, then age, then name

        // Reversed comparator
        Comparator<Person> oldestFirst = byAge.reversed();

        // Null handling
        Comparator<Person> nullSafe = Comparator
                .nullsFirst(byName);
    }

    // 5. Custom composition patterns
    public void customCompositionPatterns() {
        // Building a validation pipeline
        Validator<Order> validator = Validator
                .of((Order o) -> o.getTotal() > 0, "Order must have positive total")
                .and(o -> !o.getItems().isEmpty(), "Order must have items")
                .and(o -> o.getCustomer() != null, "Order must have customer");

        Order order = new Order();
        ValidationResult result = validator.validate(order);

        // Building a transformation pipeline
        Transformer<String> transformer = Transformer
                .of(String::trim)
                .then(String::toLowerCase)
                .then(s -> s.replaceAll("\\s+", "_"))
                .then(s -> s.substring(0, Math.min(s.length(), 50)));

        String cleaned = transformer.apply("  Hello World  ");  // "hello_world"
    }

    // Helper classes for custom composition
    @FunctionalInterface
    interface Validator<T> {
        ValidationResult validate(T value);

        static <T> Validator<T> of(Predicate<T> predicate, String message) {
            return value -> predicate.test(value)
                    ? ValidationResult.success()
                    : ValidationResult.failure(message);
        }

        default Validator<T> and(Predicate<T> predicate, String message) {
            return value -> {
                ValidationResult first = this.validate(value);
                if (!first.isValid()) return first;

                return predicate.test(value)
                        ? ValidationResult.success()
                        : ValidationResult.failure(message);
            };
        }
    }

    @FunctionalInterface
    interface Transformer<T> {
        T apply(T value);

        static <T> Transformer<T> of(Function<T, T> function) {
            return function::apply;
        }

        default Transformer<T> then(Function<T, T> next) {
            return value -> next.apply(this.apply(value));
        }
    }

    // Supporting classes
    record Person(String name, int age, String department) {
        String getName() {
            return name;
        }

        int getAge() {
            return age;
        }

        String getDepartment() {
            return department;
        }
    }

    static class Order {
        private List<String> items = new ArrayList<>();
        private double total;
        private Object customer;

        List<String> getItems() {
            return items;
        }

        double getTotal() {
            return total;
        }

        Object getCustomer() {
            return customer;
        }
    }

    record ValidationResult(boolean isValid, String message) {
        static ValidationResult success() {
            return new ValidationResult(true, "");
        }

        static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
    }
}
```

```text
A4: Key composition patterns:

Function:
- andThen(g): f.andThen(g) = g(f(x))
- compose(g): f.compose(g) = f(g(x))
- Use for building transformation pipelines

Predicate:
- and(p): both conditions must be true
- or(p): at least one condition must be true
- negate(): inverts the result
- Use for building complex conditions

Consumer:
- andThen(c): execute both consumers in sequence
- Use for building effect pipelines

Comparator:
- thenComparing: secondary sort criteria
- reversed: reverse the ordering
- Use for multi-field sorting

Benefits of composition:
1. **Readability**: Intent is clear and declarative
2. **Reusability**: Build complex logic from simple pieces
3. **Maintainability**: Easy to modify individual pieces
4. **Testability**: Test simple pieces independently
5. **Type safety**: Compiler ensures type compatibility

Best practices:
1. Build simple, focused functions
2. Compose them into complex behavior
3. Name intermediate compositions meaningfully
4. Extract common compositions to reusable constants
5. Use method references when possible for clarity
```

## Code Examples

-
Test: [LambdaFunctionalInterfacesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaFunctionalInterfacesTest.java)
-
Source: [LambdaFunctionalInterfaces.java](src/main/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaFunctionalInterfaces.java)