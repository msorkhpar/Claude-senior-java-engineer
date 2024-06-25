# Switch Expressions in Java (Java 14+)

## Concept Explanation

Switch expressions, introduced as a standard feature in Java 14, are an enhancement to the traditional switch statement. They provide a more concise and expressive way to write multi-way conditionals, allowing the switch construct to be used as an expression that returns a value.

Key features of switch expressions include:
1. Arrow syntax (`->`) for concise case labels
2. Multiple case labels per branch
3. No fall-through between cases (unless explicitly stated)
4. Ability to return a value directly
5. Exhaustiveness checking by the compiler

## Key Points to Remember

1. Switch expressions can return a value, unlike traditional switch statements.
2. The arrow syntax (`->`) allows for more concise case definitions.
3. Multiple case labels can be combined using commas.
4. There's no fall-through between cases when using the arrow syntax.
5. The `yield` keyword is used to return a value from a block in a switch expression.
6. Switch expressions must be exhaustive (cover all possible cases).

## Java 14+ Features

- Introduction of the `->` syntax for concise case labels
- Ability to use switch as an expression
- Multiple case labels per branch
- The `yield` keyword for returning values from blocks

## Common Pitfalls and How to Avoid Them

1. Forgetting to cover all possible cases: Ensure all possible values are handled or include a default case.
2. Mistakenly allowing fall-through: When using the arrow syntax, fall-through doesn't occur automatically.
3. Forgetting to use `yield` in block cases: Use `yield` to return a value from a multi-statement case block.

## Best Practices and Optimization Techniques

1. Use switch expressions for concise, value-returning switches.
2. Combine case labels when the same action applies to multiple values.
3. Prefer the arrow syntax for single-expression cases.
4. Use blocks with `yield` for complex case logic.
5. Leverage pattern matching in switch (preview feature in Java 21) for more powerful switches.

## Edge Cases and Their Handling

1. Null values: Switch expressions don't handle null by default. Use a preceding null check if necessary.
2. Enum switches: Ensure all enum constants are covered or include a default case.
3. Exhaustiveness with sealed classes: When switching on sealed classes, cover all permitted subclasses.

## Interview-specific Insights

- Be prepared to compare switch expressions with traditional switch statements.
- Understand the benefits of switch expressions in terms of readability and safety.
- Know how to refactor a traditional switch statement into a switch expression.
- Be aware of the latest developments, such as pattern matching in switch (preview in Java 21).

## Interview Q&A

Q1: What are the main advantages of switch expressions over traditional switch statements?

A1: Switch expressions offer several advantages:
1. Conciseness: They allow for more compact code, especially with the arrow syntax.
2. Safety: They eliminate accidental fall-through between cases.
3. Expressiveness: They can be used as expressions, returning values directly.
4. Exhaustiveness: The compiler checks if all possible cases are covered.
5. Multiple case labels: You can combine multiple case labels in a single branch.

Here's a comparison:

```java
// Traditional switch statement
String result;
switch (day) {
    case MONDAY:
    case FRIDAY:
    case SUNDAY:
        result = "Relax";
        break;
    case TUESDAY:
        result = "Work";
        break;
    case THURSDAY:
    case SATURDAY:
        result = "Party";
        break;
    case WEDNESDAY:
        result = "Study";
        break;
    default:
        result = "Unknown";
}

// Switch expression
String result = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> "Relax";
    case TUESDAY -> "Work";
    case THURSDAY, SATURDAY -> "Party";
    case WEDNESDAY -> "Study";
    default -> "Unknown";
};
```

Q2: How do you handle multi-statement logic in a switch expression case?

A2: For cases that require multiple statements, you can use a block with the `yield` keyword to return a value. Here's an example:

```java
int result = switch (status) {
    case SUCCESS -> {
        logger.info("Operation successful");
        yield 1;
    }
    case ERROR -> {
        logger.error("Operation failed");
        sendAlert();
        yield -1;
    }
    default -> 0;
};
```

Q3: How does exhaustiveness checking work in switch expressions?

A3: The compiler performs exhaustiveness checking for switch expressions, ensuring that all possible values of the switched variable are handled. This is particularly useful for enum types and sealed classes. If not all cases are covered, you'll get a compilation error. You can use a default case to catch any unhandled values:

```java
enum Color { RED, GREEN, BLUE }

String shade = switch (color) {
    case RED -> "Warm";
    case GREEN -> "Cool";
    case BLUE -> "Cool";
    // No default needed, all enum values are covered
};

// If a new color is added to the enum, this switch will cause a compilation error
// unless updated or a default case is added
```

Q4: Can you use switch expressions with null values?

A4: Switch expressions don't handle null values by default. Attempting to switch on a null value will result in a NullPointerException. To handle potential null values, you should perform a null check before the switch expression:

```java
String result = (obj == null) ? "Null input" : switch (obj) {
    case String s -> "String: " + s;
    case Integer i -> "Integer: " + i;
    default -> "Unknown type";
};
```

Q5: How do switch expressions interact with pattern matching in Java 21 (preview feature)?

A5: Pattern matching in switch, introduced as a preview feature in Java 21, enhances switch expressions by allowing you to match against patterns, including type patterns and guarded patterns. This makes switch expressions even more powerful:

```java
Object obj = // some object
String result = switch (obj) {
    case String s -> "String of length " + s.length();
    case Integer i when i > 0 -> "Positive integer";
    case Integer i -> "Non-positive integer";
    case List<?> list -> "List with " + list.size() + " elements";
    default -> "Something else";
};
```

This feature combines the conciseness of switch expressions with the power of pattern matching, allowing for more expressive and type-safe code.