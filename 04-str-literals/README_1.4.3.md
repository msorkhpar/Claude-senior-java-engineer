# 1.4.3 String methods (e.g., length(), charAt(), substring())

String methods are essential tools for manipulating and extracting information from strings in Java. This section covers
three fundamental string methods: `length()`, `charAt()`, and `substring()`.

## Key Points to Remember

1. Strings in Java are immutable, so these methods return new String objects or primitive values.
2. Method calls can be chained for complex operations.
3. These methods are highly optimized and widely used in Java programming.

## Relevant Java 21 Features

While these methods have been part of Java since its early versions, Java 21 introduces no significant changes to their 
functionality. However, it's worth noting that Java 21 continues to optimize string handling under the hood.

## Common Pitfalls and How to Avoid Them

1. **IndexOutOfBoundsException**: Always check string length before using `charAt()` or `substring()`.
2. **Off-by-one errors**: Remember that string indices start at 0, not 1.
3. **Unnecessary string creation**: Avoid creating new strings when simple checks can suffice (e.g., use `isEmpty()` instead of `length() == 0`).

## Best Practices and Optimization Techniques

1. Use `isEmpty()` instead of `length() == 0` for better readability and potential performance benefits.
2. When possible, use `charAt()` instead of `substring()` for single character extraction.
3. For repeated concatenations or manipulations, consider using StringBuilder.

## Edge Cases and Their Handling

1. Empty strings: `length()` returns 0, `charAt(0)` throws an exception, `substring(0,0)` returns an empty string.
2. Null strings: All these methods will throw a NullPointerException if called on a null string.

## Interview-specific Insights

Interviewers often ask candidates to implement string manipulation algorithms using these basic methods. Understanding their behavior and efficiency is crucial.

## Detailed Explanation

### length()

The `length()` method returns the number of characters in the string. It's a constant-time operation (O(1)) because the length is stored as part of the String object.

### charAt(int index)

This method returns the character at the specified index in the string. It's also a constant-time operation (O(1)). The index is zero-based, meaning the first character is at index 0.

### substring(int beginIndex, int endIndex)

The `substring()` method extracts a portion of the string. It returns a new string that is a substring of the original string. The substring begins at `beginIndex` and extends to the character at index `endIndex - 1`. Thus, the length of the substring is `endIndex - beginIndex`.

There's also an overloaded version `substring(int beginIndex)` that returns a substring from the specified index to the end of the string.

## Interview Q&A Section

Q1: What's the time complexity of the `length()`, `charAt()`, and `substring()` methods?

A1:
```text
- length(): O(1) - constant time, as the length is stored in the String object.
- charAt(int index): O(1) - constant time, as it directly accesses the character array.
- substring(int beginIndex, int endIndex): O(n) where n is the length of the substring. While the operation itself is quick, it creates a new String object, which involves copying characters.
```

Q2: How would you efficiently check if a string is empty?

A2:
```java
// Preferred way
if (str.isEmpty()) {
    // String is empty
}

// Less preferred, but equivalent
if (str.length() == 0) {
    // String is empty
}
```

Q3: What happens if you call `substring()` with `beginIndex` equal to `endIndex`?

A3:
```text
If beginIndex equals endIndex, the result is an empty string. This is a valid operation and doesn't throw an exception. For example:

String str = "Hello";
String empty = str.substring(2, 2); // empty is ""
```

Q4: How would you extract the last character of a string without knowing its length beforehand?

A4:
```java
String str = "Hello";
char lastChar = str.charAt(str.length() - 1); // lastChar is 'o'
```

## Code Examples

- Test: [StringMethodsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/literals/StringMethodsTest.java)
- Source: [StringMethods.java](src/main/java/com/github/msorkhpar/claudejavatutor/literals/StringMethods.java)
