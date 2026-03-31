# 4.3.2. Types of Method References (Static, Instance, Constructor)

## Concept Explanation

Java defines four distinct categories of method references, each resolving the receiver and arguments differently.
Understanding these categories is essential because each one corresponds to a different relationship between the
method being referenced and the functional interface being used.

### Type 1: Static Method Reference — `ClassName::staticMethod`

A static method reference refers to a static method of a class. There is no receiver object — the method is called
directly on the class.

**Signature mapping**: The parameters of the functional interface's abstract method become the arguments to the
static method. If the static method takes one parameter, the functional interface must take one parameter.

```java
// Integer.parseInt(String s) — static, one argument
Function<String, Integer> parser = Integer::parseInt;
int n = parser.apply("42"); // calls Integer.parseInt("42")

// Math.abs(int a) — static, one argument
IntUnaryOperator abs = Math::abs;
int result = abs.applyAsInt(-5); // calls Math.abs(-5)
```

### Type 2: Bound Instance Method Reference — `instance::instanceMethod`

A bound instance method reference captures a specific instance at the time of creation. The receiver is fixed; when
the functional interface is invoked, the referenced method is always called on that captured instance.

**Signature mapping**: The functional interface's abstract method parameters become the arguments to the instance
method. The receiver is already determined.

```java
// A specific String object is captured
String prefix = "Hello";
Predicate<String> startsWith = prefix::startsWith;  // bound: 'prefix' is fixed
boolean b = startsWith.test(" World"); // calls "Hello".startsWith(" World")

// A specific PrintStream (System.out) is captured
Consumer<String> printer = System.out::println;  // bound: System.out is fixed
printer.accept("test"); // calls System.out.println("test")
```

### Type 3: Unbound Instance Method Reference — `ClassName::instanceMethod`

An unbound instance method reference refers to an instance method, but the receiver is NOT fixed. Instead, the
receiver is supplied as the first argument when the functional interface is invoked. This is why the functional
interface needs one extra parameter compared to the method's declared parameters.

**Signature mapping**: The first parameter of the functional interface's abstract method becomes the receiver; the
remaining parameters become the method's arguments.

```java
// String::toUpperCase — no parameters; but Function<String,String> takes one: the receiver
Function<String, String> toUpper = String::toUpperCase;
String up = toUpper.apply("hello"); // calls "hello".toUpperCase()

// String::compareTo — one parameter; Comparator<String> takes two: receiver + argument
Comparator<String> cmp = String::compareTo;
int r = cmp.compare("apple", "banana"); // calls "apple".compareTo("banana")
```

### Type 4: Constructor Reference — `ClassName::new`

A constructor reference refers to a constructor of a class. Like a factory method, it creates a new instance when
invoked via the functional interface.

**Signature mapping**: The parameters of the functional interface's abstract method become the constructor arguments.
The return type is the class being constructed.

```java
// No-argument constructor
Supplier<ArrayList<String>> listMaker = ArrayList::new;
ArrayList<String> list = listMaker.get(); // calls new ArrayList<>()

// One-argument constructor
Function<String, StringBuilder> sbMaker = StringBuilder::new;
StringBuilder sb = sbMaker.apply("initial"); // calls new StringBuilder("initial")

// Two-argument constructor (requires BiFunction or custom interface)
BiFunction<String, Integer, String> repeat = String::new; // hypothetical
// In practice: PointRecord record = biFunction.apply(x, y);
```

### Summary Table

| Type                     | Syntax                         | Receiver         | Example                         |
|--------------------------|--------------------------------|------------------|---------------------------------|
| Static                   | `ClassName::staticMethod`      | None (static)    | `Integer::parseInt`             |
| Bound Instance           | `instance::instanceMethod`     | Fixed at capture | `System.out::println`           |
| Unbound Instance         | `ClassName::instanceMethod`    | First argument   | `String::toUpperCase`           |
| Constructor              | `ClassName::new`               | N/A (creates)    | `ArrayList::new`                |

## Key Points to Remember

1. **Static**: `ClassName::staticMethod` — no receiver; arguments match the functional interface's parameters exactly.
2. **Bound instance**: `object::instanceMethod` — receiver is the captured object; arguments match the method's parameters.
3. **Unbound instance**: `ClassName::instanceMethod` — receiver is the first functional interface argument; remaining arguments match the method's parameters.
4. **Constructor**: `ClassName::new` — creates a new instance; arguments match the constructor's parameters.
5. The functional interface determines how the compiler interprets the method reference type.
6. Unbound instance method references are the trickiest — the receiver consumes one extra parameter from the interface.
7. Constructor references always return an instance of the referenced class.
8. Static method references work exactly like function pointers in other languages.
9. You can use constructor references with generic types; type inference usually resolves the type parameter.
10. Interface default methods can be referenced as unbound instance method references through implementing types.

## Relevant Java 21 Features

- **Java 8**: All four types introduced simultaneously.
- **Java 9+**: Factory methods like `Map.of()`, `List.of()` are static and can serve as method references
  `Map::of`, `List::of` when the arity matches.
- **Java 16+**: Record components generate getter methods that are perfect for unbound instance method references.
  `record Person(String name, int age){}` — use `Person::name` or `Person::age` as `Function<Person, String>` and
  `Function<Person, Integer>` respectively.
- **Java 21**: `String` template processors (preview) and `SequencedCollection` methods are designed with method
  reference usage in mind.

## Common Pitfalls and How to Avoid Them

1. **Confusing static and unbound instance method references visually**: Both use `ClassName::method` syntax. The
   difference is whether the method is static or an instance method.

   ```java
   // Static method reference: Integer.parseInt is static
   Function<String, Integer> staticRef = Integer::parseInt; // (String) -> Integer

   // Unbound instance method reference: String.length is an instance method
   Function<String, Integer> unboundRef = String::length; // receiver is the String arg

   // Key question: is the referenced method static or an instance method?
   // Check the method declaration in the class's API.
   ```

2. **Wrong arity in the functional interface for unbound instance methods**: An unbound instance method reference
   requires the interface to have one extra parameter for the receiver.

   ```java
   // String::contains takes one argument AND needs a receiver
   // So the functional interface needs TWO parameters: receiver + argument
   BiPredicate<String, CharSequence> contains = String::contains; // correct
   // Predicate<String> contains2 = String::contains; // compile error: too few params
   ```

   ```java
   // Fix: match the functional interface arity to receiver + args
   // String::compareTo has one param + one receiver = BiFunction or Comparator
   Comparator<String> cmp = String::compareTo; // correct: takes two Strings
   ```

3. **Using bound when you mean unbound or vice versa**:

   ```java
   String target = "searchTerm";
   List<String> sources = List.of("find searchTerm here", "nothing", "also searchTerm");

   // BOUND: always checks if target contains "se" — probably not intent
   Predicate<String> bound = target::contains; // checks if "searchTerm" contains each source
   // sources.stream().filter(bound).toList(); // filters sources that "searchTerm" contains??

   // UNBOUND: checks if each source contains "searchTerm" — likely the intent
   Predicate<String> unbound = s -> s.contains(target); // lambda needed here
   List<String> found = sources.stream().filter(unbound).toList(); // correct
   ```

4. **Constructor reference with wrong type argument**:

   ```java
   // Attempting to create a LinkedList with constructor reference as Supplier<List<String>>
   Supplier<List<String>> supplier = LinkedList::new; // OK — no-arg constructor
   Supplier<List<String>> arrayList = ArrayList::new; // Also OK

   // Problem: using a constructor reference to a non-existent constructor
   // If the class has no matching constructor, it's a compile error
   // Function<Integer, MyClass> fn = MyClass::new; // only if MyClass(int) constructor exists
   ```

5. **Static method reference in an instance context**: Static method references do not use `this` and do not have
   access to instance state. If you need instance state, you must use a bound instance method reference or a lambda.

   ```java
   class Validator {
       private final int minLength;
       Validator(int minLength) { this.minLength = minLength; }

       // WRONG: cannot make this static and use as a bound ref that accesses minLength
       // public static boolean isValid(String s) { return s.length() >= minLength; } // compile error

       // CORRECT: instance method, use as bound reference
       public boolean isValid(String s) { return s.length() >= minLength; }
   }

   Validator v = new Validator(5);
   Predicate<String> validCheck = v::isValid; // bound instance ref — captures v
   ```

## Best Practices and Optimization Techniques

1. **Use static method references for pure utility logic**: Static methods in classes like `Integer`, `Math`,
   `Objects`, `String` are excellent candidates. `Objects::nonNull`, `Integer::parseInt`, `Math::max`.

2. **Use bound instance method references for object method delegation**: When a specific object's method should
   handle events, callbacks, or processing. E.g., `handler::processEvent` where `handler` is a specific service.

3. **Use unbound instance method references as property extractors in streams**:
   `Comparator.comparing(Person::getName)`, `stream.map(Employee::getDepartment)` — this pattern is idiomatic.

4. **Use constructor references in factory patterns and mapping**: When transforming a stream of raw data into
   domain objects: `dtoList.stream().map(MyEntity::new).toList()`.

5. **Document unusual unbound instance method references**: When using `ClassName::method` for an instance method
   with complex argument mapping, add a comment explaining the receiver/argument binding.

6. **Prefer `Comparator.comparing()` with method references over manual comparators**:

   ```java
   // Preferred: self-documenting
   list.sort(Comparator.comparing(Employee::getName).thenComparing(Employee::getDepartment));
   // Avoid: verbose lambda equivalent
   list.sort((e1, e2) -> { int c = e1.getName().compareTo(e2.getName()); return c != 0 ? c : e1.getDepartment().compareTo(e2.getDepartment()); });
   ```

## Edge Cases and Their Handling

1. **Varargs constructor reference**: If the constructor accepts varargs, the functional interface must accept an
   array of the vararg type.

   ```java
   // String has no varargs constructor in standard API; hypothetical:
   // Function<Object[], MyClass> fn = MyClass::new; // if MyClass(Object... args) exists
   ```

2. **Generic class constructor reference with type wildcards**: Type inference usually handles this, but sometimes
   an explicit type witness is needed.

   ```java
   // Inferred as Supplier<ArrayList<String>> from target type
   Supplier<ArrayList<String>> supplier = ArrayList::new; // fine

   // If inference fails, use explicit type
   Supplier<ArrayList<String>> explicit = ArrayList<String>::new; // Java 8 diamond in ref
   ```

3. **Inner class constructor reference**: Non-static inner classes require an enclosing instance, making their
   constructor references more complex.

   ```java
   class Outer {
       class Inner { Inner(int x) {} }

       void demo() {
           // Non-static inner class: constructor takes enclosing instance implicitly
           // IntFunction<Inner> fn = Inner::new; // only works inside Outer context
           IntFunction<Inner> fn = Inner::new; // valid inside Outer
       }
   }
   ```

4. **Static method reference resolving to the most specific overload**: When multiple static overloads exist, the
   one matching the functional interface parameter types is chosen.

   ```java
   // Math.max(int,int) vs Math.max(double,double) — resolved by target type
   IntBinaryOperator intMax = Math::max;      // resolves to max(int, int)
   DoubleBinaryOperator doubleMax = Math::max; // resolves to max(double, double)
   ```

5. **Method reference vs lambda for static methods with side effects**: Static method references that have side
   effects (like logging) work the same as lambdas but can make side effects less visible.

## Interview-specific Insights

Interviewers focus on:

- Can you enumerate all four types and give an example of each without prompting?
- Do you understand WHY unbound instance method references consume an extra functional interface parameter?
- Can you identify which type a given method reference is just by looking at the syntax?
- How do you choose the right functional interface arity for each type?
- What is the relationship between constructor references and the factory method design pattern?

**Common tricky interview questions:**

- "What is the functional interface type for `String::compareTo`? Why does it take two strings?"
  (`Comparator<String>` or `BiFunction<String, String, Integer>` — because the receiver is the first arg)
- "How is `new ArrayList<>()` expressed as a constructor reference? What functional interface does it match?"
  (`ArrayList::new` as `Supplier<ArrayList>`)
- "If a class has a private constructor, can you use a constructor reference to it?"
  (No — the same access rules as regular invocations apply)

## Interview Q&A Section

**Q1: Enumerate all four types of method references with an example of each.**

```text
A1: The four types of method references in Java are:

1. Static method reference: ClassName::staticMethod
   - Refers to a static method. No receiver. Parameters of the functional interface
     map directly to the method's parameters.

2. Bound instance method reference: instance::instanceMethod
   - Refers to an instance method of a specific captured object. The receiver is fixed.
     Parameters of the functional interface map to the method's parameters.

3. Unbound instance method reference: ClassName::instanceMethod
   - Refers to an instance method, but the receiver is the first functional interface
     parameter (supplied at invocation time). The remaining interface parameters map
     to the method's parameters.

4. Constructor reference: ClassName::new
   - Refers to a constructor. Parameters of the functional interface map to the
     constructor's parameters. Return type is the constructed class.
```

```java
import java.util.*;
import java.util.function.*;

public class FourTypesDemo {

    // Helper class for demonstration
    static class StringWrapper {
        private final String value;
        StringWrapper(String value) { this.value = value; }
        String getValue() { return value; }
        String appendTo(String other) { return value + other; }
        static StringWrapper of(String s) { return new StringWrapper(s); }
    }

    public static void main(String[] args) {
        // 1. Static method reference: ClassName::staticMethod
        Function<String, StringWrapper> staticRef = StringWrapper::of;
        // Equivalent lambda: s -> StringWrapper.of(s)
        StringWrapper w1 = staticRef.apply("hello");

        // 2. Bound instance method reference: instance::instanceMethod
        StringWrapper instance = new StringWrapper("prefix");
        Function<String, String> boundRef = instance::appendTo;
        // Equivalent lambda: other -> instance.appendTo(other)
        String s2 = boundRef.apply("_suffix"); // "prefix_suffix"

        // 3. Unbound instance method reference: ClassName::instanceMethod
        Function<StringWrapper, String> unboundRef = StringWrapper::getValue;
        // Equivalent lambda: sw -> sw.getValue()
        String s3 = unboundRef.apply(w1); // "hello"

        // 4. Constructor reference: ClassName::new
        Function<String, StringWrapper> ctorRef = StringWrapper::new;
        // Equivalent lambda: s -> new StringWrapper(s)
        StringWrapper w2 = ctorRef.apply("world");

        System.out.println(w1.getValue()); // "hello"
        System.out.println(s2);            // "prefix_suffix"
        System.out.println(s3);            // "hello"
        System.out.println(w2.getValue()); // "world"
    }
}
```

---

**Q2: Why does `String::compareTo` used as a `Comparator<String>` work, even though it looks like a static reference?**

```text
A2: String::compareTo is an UNBOUND instance method reference, not a static method reference.
Even though the syntax looks like ClassName::method, the method 'compareTo' is an instance
method of String, not a static method.

When used as a Comparator<String> (whose compare(T a, T b) method takes two Strings):
- The first String parameter of compare() becomes the RECEIVER of compareTo()
- The second String parameter of compare() becomes the ARGUMENT to compareTo()

So: cmp.compare(s1, s2) translates to: s1.compareTo(s2)

This is exactly why unbound instance method references "consume" one extra parameter from
the functional interface — to provide the receiver object.

This is a very common interview question because candidates often assume ClassName::method
means a static reference, but the compiler determines the type based on the method declaration.
```

```java
import java.util.*;

// String::compareTo as Comparator<String>
Comparator<String> cmp = String::compareTo;
// Equivalent lambda: (s1, s2) -> s1.compareTo(s2)

List<String> words = new ArrayList<>(Arrays.asList("banana", "apple", "cherry"));
words.sort(cmp);
System.out.println(words); // [apple, banana, cherry]

// String::compareToIgnoreCase as Comparator<String>
Comparator<String> ignoreCaseCmp = String::compareToIgnoreCase;
List<String> mixed = new ArrayList<>(Arrays.asList("Banana", "apple", "CHERRY"));
mixed.sort(ignoreCaseCmp);
System.out.println(mixed); // [apple, Banana, CHERRY] — sorted case-insensitively

// Explanation: compareToIgnoreCase(String other) is an instance method on String
// So ClassName::instanceMethod => first functional-interface param is the receiver
```

---

**Q3: How does a constructor reference work with the factory pattern?**

```text
A3: A constructor reference (ClassName::new) acts as a factory: when the functional interface's
abstract method is called, it creates and returns a new instance of the class.

This is equivalent to the Factory Method design pattern — the caller doesn't know or care which
concrete class is being instantiated; they just call the functional interface, and the
constructor reference handles instantiation.

Benefits of constructor references as factories:
1. The factory can be passed as a parameter (dependency injection without reflection)
2. The instantiation logic is decoupled from the caller
3. Can switch implementations by changing the reference, not the consuming code
4. Works naturally with streams: stream.map(SomeClass::new) creates a stream of new instances
```

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

record Point(double x, double y) {}
record Colour(int r, int g, int b) {}

public class ConstructorRefFactoryDemo {

    // Generic factory method that accepts a constructor reference
    public static <T> List<T> createList(int count, Supplier<T> factory) {
        return Stream.generate(factory).limit(count).collect(Collectors.toList());
    }

    // Two-argument factory
    public static <T> List<T> createList(List<String> names, Function<String, T> factory) {
        return names.stream().map(factory).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        // Using no-arg constructor reference as Supplier
        Supplier<ArrayList<String>> listFactory = ArrayList::new;
        List<List<String>> buckets = createList(3, listFactory); // 3 empty ArrayLists

        // Using single-arg constructor reference as Function
        List<String> names = List.of("Alice", "Bob", "Charlie");
        List<StringBuilder> builders = createList(names, StringBuilder::new);
        builders.forEach(sb -> System.out.println(sb.length())); // 5, 3, 7

        // Stream transformation using constructor reference
        List<String> pointStrings = List.of("1.0,2.0", "3.0,4.0");
        List<Point> points = pointStrings.stream()
                .map(s -> s.split(","))
                .map(parts -> new Point(Double.parseDouble(parts[0]),
                                        Double.parseDouble(parts[1])))
                .collect(Collectors.toList()); // constructor called via lambda (no ref possible here)

        // Switch factory at call site — no code change in consumer
        Function<String, StringBuilder> factory1 = StringBuilder::new;
        Function<String, StringBuffer> factory2 = StringBuffer::new;
    }
}
```

---

**Q4: When does the compiler use a static vs. an unbound instance method reference interpretation?**

```text
A4: When the compiler sees ClassName::method, it first checks whether 'method' is:
  (a) A static method of ClassName → static method reference interpretation
  (b) An instance method of ClassName → unbound instance method reference interpretation

If both exist (overloading), the compiler uses the target type (functional interface) to
disambiguate. If 'method' is only static, interpretation (a) is used. If 'method' is only
an instance method, interpretation (b) is used.

The key indicator:
- Static method reference: the functional interface parameter count == method parameter count
- Unbound instance ref: the functional interface parameter count == method param count + 1
  (the extra parameter is the receiver)

If the target type is ambiguous (multiple candidates match), the compiler reports an error
and requires an explicit cast.
```

```java
import java.util.function.*;

class Disambiguate {
    static String staticMethod(String s) { return "static:" + s; }
    String instanceMethod(String s) { return "instance:" + s; }

    void demo() {
        // Unambiguous: staticMethod is static, used as Function<String,String>
        Function<String, String> staticRef = Disambiguate::staticMethod;

        // Unambiguous: instanceMethod is an instance method, used as BiFunction
        // because unbound instance refs consume one extra param for the receiver
        BiFunction<Disambiguate, String, String> unboundRef = Disambiguate::instanceMethod;

        // For the instance method, if used as Function<String,String>,
        // it would try static interpretation (1 param in + 1 param out) — but
        // staticMethod shadows here. In general: the compiler picks based on method type.

        // Creating an unbound ref for just one class with one instance method:
        // Function<String, Integer> len = String::length; // unbound: String is receiver
    }
}
```

---

**Q5: How do constructor references work with arrays?**

```text
A5: Array constructor references use a special syntax: TypeName[]::new
This creates a functional interface that, when invoked with an int argument, allocates
a new array of the specified length.

This is commonly used with Stream.toArray(IntFunction<T[]>) to collect a stream into
a typed array. Without array constructor references, you'd need to write i -> new String[i].

Array constructor references work for any type:
- Primitive: int[]::new, double[]::new, etc.
- Object: String[]::new, MyClass[]::new, etc.
```

```java
import java.util.stream.*;

// Array constructor reference
java.util.function.IntFunction<String[]> arrayFactory = String[]::new;
String[] arr = arrayFactory.apply(5); // new String[5]

// Most common use: Stream.toArray()
String[] words = Stream.of("hello", "world", "foo")
        .map(String::toUpperCase)
        .toArray(String[]::new); // instead of: .toArray(i -> new String[i])

// Equivalent lambda
String[] words2 = Stream.of("hello", "world", "foo")
        .map(String::toUpperCase)
        .toArray(i -> new String[i]); // same result, less idiomatic

// Primitive arrays work too
int[] squares = IntStream.rangeClosed(1, 5)
        .map(n -> n * n)
        .toArray(); // IntStream.toArray() doesn't need a factory
```

## Code Examples

- Source: [MethodReferenceTypes.java](src/main/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferenceTypes.java)
- Test: [MethodReferenceTypesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferenceTypesTest.java)
