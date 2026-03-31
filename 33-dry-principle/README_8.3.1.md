# 8.3.1. Definition and Purpose of DRY Principle

## Concept Explanation

The DRY (Don't Repeat Yourself) principle was introduced by Andy Hunt and Dave Thomas in their book *The Pragmatic Programmer* (1999). The principle states:

> "Every piece of knowledge must have a single, unambiguous, authoritative representation within a system."

DRY is fundamentally about **knowledge duplication**, not code duplication. Two code fragments that look identical may represent different domain concepts and therefore are not true DRY violations. Conversely, two fragments that look different may encode the same business rule in two places -- which *is* a violation.

**Real-world analogy**: Think of a company phone directory. If the receptionist maintains a printed list, the HR department maintains a spreadsheet, and the IT department maintains a database, any phone number change must be applied in three places. When one is missed, the data becomes inconsistent. DRY says there should be one authoritative source, and all other views derive from it.

DRY applies to:
- **Code** -- duplicated logic, copy-pasted methods
- **Data** -- the same fact stored in multiple locations
- **Knowledge** -- business rules, validation logic, constants, configuration

## Key Points to Remember

- DRY is about **knowledge**, not syntax. Identical-looking code serving different domains is not necessarily a violation.
- The opposite of DRY is WET: "Write Everything Twice" or "We Enjoy Typing."
- Violating DRY leads to **shotgun surgery** -- a single change requires edits in many places.
- DRY encourages single-source-of-truth for constants, validation rules, and business logic.
- Over-applying DRY (premature abstraction) can be worse than the duplication it eliminates.
- The **Rule of Three** heuristic says: wait until you see three instances of duplication before abstracting.

## Relevant Java 21 Features

- **Records**: Immutable data carriers that eliminate boilerplate getters, equals, hashCode, and toString -- a form of DRY at the language level.
- **Sealed classes**: Reduce duplication in type-checking logic by constraining permitted subtypes and enabling exhaustive pattern matching in switch.
- **Pattern matching for switch (JEP 441)**: Eliminates repetitive instanceof-cast chains.
- **Text blocks**: Reduce duplication of escape sequences and formatting in multi-line strings.
- **Default methods in interfaces**: Allow shared behavior without duplicating it across implementors.

## Common Pitfalls and How to Avoid Them

1. **Confusing structural duplication with knowledge duplication**
   ```java
   // These LOOK the same but represent DIFFERENT domain rules
   double calculateTax(double amount) { return amount * 0.10; }
   double calculateCommission(double amount) { return amount * 0.10; }
   // They may diverge independently -- merging them violates SRP
   ```
   **Solution**: Only extract when the duplication represents the same business rule.

2. **Magic numbers repeated across files**
   ```java
   // In ServiceA.java
   if (retries > 3) { throw new RuntimeException("Too many retries"); }
   // In ServiceB.java
   if (retries > 3) { throw new RuntimeException("Too many retries"); }
   ```
   **Solution**: Extract into a named constant: `public static final int MAX_RETRIES = 3;`

3. **Copy-pasting validation logic**
   ```java
   // In RegisterController
   if (email == null || !email.contains("@")) { throw ... }
   // In ProfileController
   if (email == null || !email.contains("@")) { throw ... }
   ```
   **Solution**: Centralize into a `ValidationUtils.isValidEmail(email)` method.

4. **Premature abstraction**
   ```java
   // Don't create a "GenericProcessor<T,R,S,U>" after seeing two similar methods.
   // Wait for the Rule of Three.
   ```
   **Solution**: Tolerate small duplication until the pattern is clear.

## Best Practices and Optimization Techniques

1. **Extract Method**: The most common DRY refactoring -- pull duplicated logic into a private method.
2. **Template Method Pattern**: Define the skeleton of an algorithm and let subclasses fill in the details.
3. **Higher-order functions**: Pass behavior as lambda expressions to eliminate structural duplication.
4. **Constants and enums**: Replace magic numbers and strings with named constants.
5. **Utility classes**: Centralize cross-cutting concerns (validation, formatting, logging).
6. **Code generation**: For truly mechanical duplication (DTOs, mappers), use annotation processors or tools like MapStruct.

## Edge Cases and Their Handling

1. **Null inputs**: Shared validation methods should handle null gracefully rather than forcing callers to null-check.
2. **Empty collections**: Reusable pipelines (filter-map-collect) naturally handle empty inputs.
3. **Boundary values**: Centralized constants ensure boundary checks are consistent (e.g., `MAX_RETRIES` used everywhere).
4. **Configuration drift**: When DRY is applied to configuration, ensure all consumers read from the same source.

## Interview-specific Insights

Interviewers focus on:
- Whether you understand DRY as a *knowledge* principle, not just code deduplication
- Your ability to identify when NOT to apply DRY (incidental vs. intentional duplication)
- How you balance DRY with SRP and KISS
- Real refactoring examples from your experience

Tricky questions:
- "Is it always good to eliminate duplicated code?" (No -- incidental duplication should be left alone.)
- "What is the difference between DRY and code reuse?" (DRY is broader -- it covers data, knowledge, and process.)

## Interview Q&A Section

**Q1: What is the DRY principle and why is it important?**

```text
A1: DRY stands for "Don't Repeat Yourself." It states that every piece of knowledge
should have a single, unambiguous, authoritative representation within a system.

Importance:
1. Reduces maintenance cost -- changing a rule requires editing only one place.
2. Prevents inconsistencies -- eliminates the risk of updating one copy but missing another.
3. Improves readability -- named abstractions communicate intent better than duplicated code.
4. Enables easier testing -- one implementation means one set of tests.

DRY applies to code, data, documentation, and configuration. It is about knowledge
duplication, not syntactic similarity.
```

```java
// VIOLATION: Same email validation in multiple places
public class ViolationExample {
    public boolean validateForRegistration(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
    public boolean validateForPasswordReset(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}

// DRY: Single source of truth
public class DryExample {
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
    public boolean validateForRegistration(String email) { return isValidEmail(email); }
    public boolean validateForPasswordReset(String email) { return isValidEmail(email); }
}
```

**Q2: What is the difference between structural duplication and knowledge duplication?**

```text
A2: Structural duplication occurs when two code fragments look syntactically similar
but represent DIFFERENT business concepts. Knowledge duplication occurs when the SAME
business rule or fact is encoded in multiple places.

Structural duplication example: A tax calculation and a commission calculation both
multiply an amount by a rate. They look identical but represent different domain rules
that may evolve independently. Merging them would create harmful coupling.

Knowledge duplication example: An email validation regex appears in both the
registration service and the password reset service. This IS the same business rule
and should be extracted to a single place.

The key question to ask: "If one changes, must the other change too?" If yes, it's
knowledge duplication and violates DRY. If no, it's incidental and should be left alone.
```

```java
// Structural duplication (NOT a DRY violation -- keep separate)
double calculateTax(double amount, double rate) { return amount * rate; }
double calculateCommission(double amount, double rate) { return amount * rate; }

// Knowledge duplication (IS a DRY violation -- extract)
// Before:
double shippingCostA(double weight) { return weight * 0.15; }
double shippingCostB(double weight) { return weight * 0.15 + 5.0; }

// After:
static final double COST_PER_KG = 0.15;
double shippingCostA(double weight) { return weight * COST_PER_KG; }
double shippingCostB(double weight) { return weight * COST_PER_KG + 5.0; }
```

**Q3: How does the Template Method pattern help enforce DRY?**

```text
A3: The Template Method pattern defines the skeleton of an algorithm in a base class
and lets subclasses override specific steps without duplicating the overall structure.

Benefits for DRY:
1. The algorithm structure (filter -> transform -> collect) is defined once.
2. Subclasses only specify what varies (the filter condition and transformation).
3. If the overall pipeline needs to change (e.g., add logging), only the base class
   changes.

This is particularly useful when multiple data processing pipelines share the same
structural pattern but differ in their specific operations.
```

```java
public abstract class DataProcessor<T, R> {
    // Template method -- defines the algorithm once
    public final List<R> process(List<T> items) {
        return items.stream()
                .filter(this::isValid)
                .map(this::transform)
                .collect(Collectors.toList());
    }
    protected abstract boolean isValid(T item);
    protected abstract R transform(T item);
}

// Concrete implementation -- only defines the varying parts
public class StringUpperCaseProcessor extends DataProcessor<String, String> {
    @Override protected boolean isValid(String item) { return item != null && !item.isBlank(); }
    @Override protected String transform(String item) { return item.toUpperCase(); }
}
```

**Q4: What is the Rule of Three and how does it relate to DRY?**

```text
A4: The Rule of Three is a heuristic that says you should not abstract duplication
until you see it at least three times. The reasoning is:

1. First time: Just write it.
2. Second time: Note the similarity but tolerate it.
3. Third time: Now you have enough evidence that it's a real pattern -- refactor.

This prevents premature abstraction, which is worse than small-scale duplication.
Premature abstraction creates:
- Unnecessary indirection that hurts readability
- Coupling between unrelated modules through a shared abstraction
- Abstractions that don't fit future use cases

The Rule of Three balances DRY with KISS (Keep It Simple, Stupid) and YAGNI
(You Ain't Gonna Need It).
```

```java
// First time -- order summary
String formatOrderSummary(String id, String desc, double total) {
    return String.format("[%s] %s - $%.2f", id, desc, total);
}
// Second time -- invoice summary (looks similar, keep separate for now)
String formatInvoiceSummary(String num, String client, double amount) {
    return String.format("[%s] %s - $%.2f", num, client, amount);
}
// Third time -- NOW extract!
String formatSummary(String identifier, String description, double amount) {
    return String.format("[%s] %s - $%.2f", identifier, description, amount);
}
```

**Q5: How do higher-order functions help achieve DRY in Java?**

```text
A5: Higher-order functions accept or return functions, allowing you to parameterize
behavior. Instead of writing multiple methods that differ only in their filter or
transformation logic, you write ONE generic method and pass the varying behavior
as lambda expressions.

This eliminates the repeated stream().filter().map().collect() boilerplate that
appears in many service methods. The pipeline structure is defined once, and callers
supply only the parts that vary.

In Java 21, this works seamlessly with functional interfaces (Predicate, Function,
Consumer, Supplier) and method references.
```

```java
public class FunctionalDry {
    // One reusable pipeline -- no need to duplicate stream logic
    public <T, R> List<R> filterAndTransform(
            List<T> items, Predicate<T> filter, Function<T, R> transformer) {
        return items.stream()
                .filter(filter)
                .map(transformer)
                .collect(Collectors.toList());
    }
}

// Usage -- different behaviors, same pipeline
List<String> shortNames = dry.filterAndTransform(names, n -> n.length() < 5, String::toUpperCase);
List<Integer> evenDoubled = dry.filterAndTransform(numbers, n -> n % 2 == 0, n -> n * 2);
```

**Q6: What are the consequences of violating DRY?**

```text
A6: Violating DRY leads to several problems:

1. Shotgun surgery: A single business rule change requires edits in multiple files.
2. Inconsistency bugs: One copy is updated but another is missed.
3. Increased test surface: Each duplicate needs its own tests.
4. Knowledge fragmentation: New developers can't find the "real" source of truth.
5. Higher maintenance cost: More code to read, understand, and maintain.
6. Regression risk: Fixing a bug in one copy but not the others.

In concurrent systems, DRY violations are especially dangerous because duplicated
synchronization logic can easily become inconsistent, leading to race conditions
or deadlocks that are hard to reproduce and debug.
```

```java
// VIOLATION: Bug fix applied to one copy but not the other
class ServiceA {
    void process(String data) {
        if (data != null && !data.isEmpty()) { /* fixed: was data.isBlank() */ }
    }
}
class ServiceB {
    void process(String data) {
        if (data != null && !data.isBlank()) { /* BUG: still uses old check */ }
    }
}
```

## Code Examples

- Test: [DryDefinitionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryDefinitionTest.java)
- Source: [DryDefinition.java](src/main/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryDefinition.java)
