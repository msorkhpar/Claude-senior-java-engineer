# 4.1.1. Introduction to Lambda Expressions

## Concept Explanation

Lambda expressions are one of the most significant features introduced in Java 8, marking Java's entry into functional
programming. A lambda expression is essentially an anonymous function - a method without a name that can be passed
around as if it were an object.

Before lambda expressions, Java developers had to use verbose anonymous inner classes to pass behavior as a parameter.
Lambda expressions provide a clear and concise way to represent a single-method interface (functional interface) using
an expression.

**Real-world analogy**: Think of a lambda expression like a recipe card you hand to someone. Instead of calling a
professional chef (creating a named class), you simply write down the steps (the lambda) on a card and hand it over. The
receiver can execute those steps whenever needed.

## Key Points to Remember

1. Lambda expressions enable treating functionality as a method argument (behavior parameterization).
2. They can only be used with functional interfaces (interfaces with a single abstract method).
3. Lambda expressions reduce boilerplate code significantly compared to anonymous inner classes.
4. They enable a more functional programming style in Java.
5. Lambda expressions can access effectively final variables from the enclosing scope (closure).
6. The compiler infers the type of lambda parameters from the context (target typing).
7. Lambda expressions don't have their own scope; they use the enclosing scope.

## Relevant Java 21 Features

While lambda expressions were introduced in Java 8, Java 21 continues to enhance the functional programming experience:

- **Better type inference**: Java 21 improves type inference for lambda expressions in complex scenarios.
- **Pattern matching integration**: Lambda expressions work seamlessly with pattern matching features.
- **Virtual threads**: Lambda expressions are commonly used with virtual threads for concurrent programming.
- **Sequenced collections**: Lambda expressions integrate well with the new sequenced collections API.

## Common Pitfalls and How to Avoid Them

1. **Modifying non-final variables**: Lambda expressions can only access effectively final variables from the enclosing
   scope.
   ```java
   int count = 0;
   list.forEach(item -> count++); // Compilation error!
   ```
   **Solution**: Use wrapper objects like AtomicInteger or redesign to avoid mutation.

2. **Overusing lambdas**: Not every anonymous class should become a lambda. If the logic is complex, a named method is
   more readable.

3. **Ignoring exceptions**: Lambda expressions can't throw checked exceptions directly if the functional interface
   doesn't declare them.
   ```java
   list.forEach(item -> {
       Thread.sleep(100); // Compilation error!
   });
   ```
   **Solution**: Wrap checked exceptions in runtime exceptions or use a functional interface that declares the
   exception.

4. **Confusing `this` reference**: In lambda expressions, `this` refers to the enclosing class, not the lambda itself (
   unlike anonymous classes).

## Best Practices and Optimization Techniques

1. **Keep lambdas short**: If a lambda exceeds 3-5 lines, consider extracting it to a method and using a method
   reference instead.

2. **Use method references when possible**: They are more readable than equivalent lambdas.
   ```java
   list.forEach(System.out::println); // Better than: list.forEach(s -> System.out.println(s))
   ```

3. **Avoid side effects**: Lambda expressions should ideally be pure functions without side effects for better
   maintainability and parallelization.

4. **Type explicitly when it improves readability**: While type inference is powerful, explicit types can sometimes make
   code clearer.
   ```java
   (String s, Integer i) -> s.length() + i // Explicit types for clarity
   ```

5. **Consider performance**: Lambda expressions have minimal overhead, but repeated lambda creation in tight loops might
   have performance implications.

## Edge Cases and Their Handling

1. **Null handling**: Always consider null inputs in lambda expressions.
   ```java
   list.stream()
       .filter(Objects::nonNull)
       .forEach(System.out::println);
   ```

2. **Empty collections**: Ensure lambdas handle empty collections gracefully.

3. **Exception handling**: Use try-catch blocks within lambdas or create exception-handling wrapper functions.

4. **Recursive lambdas**: While possible, recursive lambdas require careful design and are often better implemented as
   regular methods.

## Interview-specific Insights

Interviewers often focus on:

- Understanding the difference between lambda expressions and anonymous inner classes
- Knowledge of functional interfaces and their relationship with lambdas
- Ability to refactor anonymous inner classes to lambda expressions
- Understanding of variable capture and effectively final concept
- Performance implications and best practices
- Integration with Stream API and modern Java features

Common tricky questions:

- "Why can't a lambda modify local variables from the enclosing scope?"
- "What's the difference between `this` in a lambda vs. an anonymous inner class?"
- "Can you explain target typing in the context of lambda expressions?"

## Interview Q&A Section

**Q1: What problem do lambda expressions solve in Java?**

```text
A1: Lambda expressions solve several problems in Java:

1. **Verbosity of anonymous inner classes**: Before Java 8, passing behavior as a parameter required creating verbose anonymous inner classes. Lambda expressions reduce this boilerplate significantly.

2. **Readability**: Lambda expressions make code more concise and readable by focusing on what the code does rather than how it's structured.

3. **Functional programming**: They enable a functional programming style in Java, making it easier to write code that treats functions as first-class citizens.

4. **Better abstractions**: They allow better abstractions for common patterns like filtering, mapping, and reducing data.

5. **Parallel processing**: Lambda expressions work seamlessly with the Stream API, making it easier to write parallel processing code.

Example comparison:
// Before Java 8 (anonymous inner class)
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
});

// With lambda expression
Collections.sort(names, (s1, s2) -> s1.compareTo(s2));

// Or even better with method reference
Collections.sort(names, String::compareTo);
```

**Q2: How are lambda expressions different from anonymous inner classes?**

```text
A2: While lambda expressions can replace anonymous inner classes in some cases, they have important differences:

1. **Scope**: Lambda expressions don't create a new scope. They use the enclosing scope. Anonymous inner classes have their own scope.
   - In a lambda, 'this' refers to the enclosing class
   - In an anonymous inner class, 'this' refers to the anonymous class itself

2. **Compilation**: Lambda expressions are compiled using invokedynamic instruction (introduced in Java 7), making them more efficient. Anonymous inner classes generate a new .class file for each instance.

3. **Usage**: Lambda expressions can only be used with functional interfaces (single abstract method). Anonymous inner classes can implement interfaces with any number of methods.

4. **Variable capture**: Both can access effectively final variables from the enclosing scope, but lambda expressions enforce this more strictly.

5. **Performance**: Lambda expressions generally have better performance characteristics due to how they're implemented by the JVM.
```

**Q3: Can you explain what "effectively final" means in the context of lambda expressions?**

```java
// Example demonstrating effectively final
public void demonstrateEffectivelyFinal() {
    int multiplier = 3; // Effectively final (not modified after initialization)

    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
    numbers.forEach(n -> System.out.println(n * multiplier)); // Works fine

    // multiplier = 5; // If we uncomment this, the lambda above won't compile
}

// What doesn't work
public void demonstrateNonFinal() {
    int count = 0;
    List<String> items = Arrays.asList("a", "b", "c");

    // This won't compile because count is modified
    // items.forEach(item -> count++);

    // Solution 1: Use AtomicInteger
    AtomicInteger counter = new AtomicInteger(0);
    items.forEach(item -> counter.incrementAndGet()); // Works

    // Solution 2: Use streams
    long finalCount = items.stream().count(); // Better approach
}
```

```text
A3 (continued): "Effectively final" means a variable that isn't explicitly declared as final but whose value doesn't change after initialization. Lambda expressions can only access local variables from the enclosing scope if they are final or effectively final.

Why this restriction?
1. **Thread safety**: Lambda expressions might be executed in different threads, and mutable shared state would cause race conditions.
2. **Semantic clarity**: It makes the code's intent clearer and prevents confusing bugs.
3. **Implementation efficiency**: The JVM can optimize lambda expressions better when variables are immutable.
```

**Q4: What is a functional interface and how does it relate to lambda expressions?**

```text
A4: A functional interface is an interface that contains exactly one abstract method (SAM - Single Abstract Method). It may contain default methods, static methods, and methods inherited from Object class, but only one abstract method.

Relationship with lambda expressions:
- Lambda expressions can only be used to instantiate functional interfaces
- The lambda expression provides the implementation of the abstract method
- The compiler uses the functional interface's method signature to infer the lambda's parameter types and return type

The @FunctionalInterface annotation:
- Optional but recommended
- Causes a compilation error if the interface doesn't meet the functional interface criteria
- Documents the intent clearly
```

```java
// Example of functional interface
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b); // Single abstract method

    // Can have default methods
    default int square(int n) {
        return n * n;
    }

    // Can have static methods
    static int add(int a, int b) {
        return a + b;
    }
}

// Using lambda expression with this functional interface
public class FunctionalInterfaceExample {
    public static void main(String[] args) {
        // Lambda expression implementing Calculator
        Calculator addition = (a, b) -> a + b;
        Calculator multiplication = (a, b) -> a * b;
        Calculator subtraction = (a, b) -> a - b;

        System.out.println("5 + 3 = " + addition.calculate(5, 3));  // 8
        System.out.println("5 * 3 = " + multiplication.calculate(5, 3));  // 15
        System.out.println("5 - 3 = " + subtraction.calculate(5, 3));  // 2

        // Using default method
        System.out.println("Square of 5 = " + addition.square(5));  // 25

        // Using static method
        System.out.println("Static add: " + Calculator.add(10, 20));  // 30
    }
}
```

**Q5: Can you explain the concept of "closure" in lambda expressions?**

```text
A5: A closure is a lambda expression that captures variables from its surrounding scope. In Java, lambda expressions can capture local variables and instance variables from the enclosing scope.

Key characteristics of closures in Java:
1. Lambda expressions can access variables from the enclosing scope
2. These variables must be final or effectively final
3. Instance variables can be accessed and modified (they're part of the object's state)
4. The lambda "closes over" these variables, capturing their values

This is different from some other languages where closures can capture and modify local variables. Java's approach prioritizes thread safety and predictability.
```

```java
// Example demonstrating closures
public class ClosureExample {
    private int instanceVar = 10;

    public void demonstrateClosure() {
        int localVar = 5;  // Effectively final

        // Lambda capturing both instance and local variables
        Runnable task = () -> {
            System.out.println("Instance var: " + instanceVar);
            System.out.println("Local var: " + localVar);

            // Can modify instance variable
            instanceVar++;

            // Cannot modify local variable
            // localVar++; // Compilation error!
        };

        task.run();
    }

    // Example with Stream API
    public List<String> filterStrings(List<String> strings, int minLength) {
        // minLength is captured by the lambda (closure)
        return strings.stream()
                .filter(s -> s.length() >= minLength)  // Closure over minLength
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        ClosureExample example = new ClosureExample();
        example.demonstrateClosure();

        List<String> words = Arrays.asList("hi", "hello", "hey", "goodbye");
        List<String> longWords = example.filterStrings(words, 5);
        System.out.println("Long words: " + longWords);  // [hello, goodbye]
    }
}
```

**Q6: What are the performance implications of using lambda expressions?**

```text
A6: Lambda expressions are generally very efficient in Java:

Advantages:
1. **invokedynamic**: Lambda expressions use the invokedynamic bytecode instruction, which allows the JVM to optimize their execution over time.
2. **No extra class files**: Unlike anonymous inner classes, lambda expressions don't generate separate .class files.
3. **Lazy evaluation**: When used with streams, lambda expressions enable lazy evaluation and optimization.
4. **JIT optimization**: The JIT compiler can inline lambda expressions effectively.

Potential concerns:
1. **Object creation**: Each lambda expression evaluation can create an object (though the JVM often optimizes this).
2. **Capturing variables**: Capturing variables from the enclosing scope has a small overhead.
3. **Stateless vs stateful**: Stateless lambdas (no captured variables) are more efficient than stateful ones.

Best practices for performance:
1. Prefer method references over lambdas when possible (slightly more efficient).
2. Avoid creating lambdas in tight loops if they capture many variables.
3. Use parallel streams judiciously - the overhead of parallelization isn't always worth it for small collections.
4. For very hot code paths, measure and profile to ensure lambda expressions don't become a bottleneck.

In practice, the readability and maintainability benefits of lambda expressions far outweigh any minimal performance costs in most applications.
```

**Q7: How do you handle exceptions in lambda expressions?**

```java
// Problem: Checked exceptions in lambdas
public class ExceptionHandlingInLambdas {

    // This won't compile because parseInt can throw NumberFormatException
    // and forEach doesn't declare throwing checked exceptions
    public void problematicExample() {
        List<String> numbers = Arrays.asList("1", "2", "abc", "4");
        // numbers.forEach(s -> System.out.println(Integer.parseInt(s)));
    }

    // Solution 1: Try-catch inside lambda
    public void solution1_TryCatchInside() {
        List<String> numbers = Arrays.asList("1", "2", "abc", "4");
        numbers.forEach(s -> {
            try {
                System.out.println(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number: " + s);
            }
        });
    }

    // Solution 2: Create a wrapper functional interface
    @FunctionalInterface
    interface CheckedConsumer<T> {
        void accept(T t) throws Exception;
    }

    public static <T> Consumer<T> wrapper(CheckedConsumer<T> consumer) {
        return item -> {
            try {
                consumer.accept(item);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public void solution2_Wrapper() {
        List<String> numbers = Arrays.asList("1", "2", "abc", "4");
        numbers.forEach(wrapper(s -> {
            System.out.println(Integer.parseInt(s));
        }));
    }

    // Solution 3: Use Stream API with better error handling
    public void solution3_StreamAPI() {
        List<String> numbers = Arrays.asList("1", "2", "abc", "4");
        numbers.stream()
                .map(s -> {
                    try {
                        return Optional.of(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        return Optional.<Integer>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(System.out::println);
    }

    // Solution 4: Extract to a method
    public void solution4_ExtractMethod() {
        List<String> numbers = Arrays.asList("1", "2", "abc", "4");
        numbers.forEach(this::printNumber);
    }

    private void printNumber(String s) {
        try {
            System.out.println(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            System.out.println("Invalid number: " + s);
        }
    }
}
```

```text
A7: Handling exceptions in lambda expressions can be challenging because:
1. Lambda expressions used with standard functional interfaces (like Consumer, Predicate, Function) can't throw checked exceptions.
2. The functional interface's method signature determines what exceptions can be thrown.

The solutions above demonstrate different approaches:
1. **Try-catch inside lambda**: Simple but can make lambdas verbose
2. **Wrapper method**: Reusable approach for handling checked exceptions
3. **Stream API with Optional**: Functional approach that handles errors gracefully
4. **Extract to method**: Best for complex logic - improves readability

Choose the approach based on:
- Complexity of error handling logic
- Whether you need to recover from errors or propagate them
- Readability and maintainability requirements
- Whether the exception is checked or unchecked
```

## Code Examples

-
Test: [LambdaIntroductionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaIntroductionTest.java)
-
Source: [LambdaIntroduction.java](src/main/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaIntroduction.java)