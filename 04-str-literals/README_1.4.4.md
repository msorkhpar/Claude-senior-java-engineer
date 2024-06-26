# 1.4.4 String Comparison in Java: equals() vs. ==

## Concept Explanation

In Java, comparing strings is a common operation, but it's crucial to understand the difference between using the
`equals()` method and the `==` operator. This distinction is fundamental to avoiding subtle bugs and ensuring 
correct string comparisons.

### equals() Method

The `equals()` method compares the content of strings. It checks if two strings have the same sequence of characters.

### == Operator

The `==` operator compares object references. For strings, it checks if two string variables refer to the exact same 
object in memory.

## Key Points to Remember

1. Use `equals()` to compare string content.
2. Use `==` only when you want to check if two variables refer to the same object.
3. String literals with the same content share the same memory location due to string interning.
4. Strings created with `new` keyword always create a new object, even if the content is identical.

## Relevant Java 21 Features

While string comparison hasn't changed significantly in Java 21, it's worth noting that Java continues to optimize
string handling internally. The principles of `equals()` vs. `==` remain consistent across versions.

## Common Pitfalls and How to Avoid Them

1. Using `==` for string content comparison:
    - Pitfall: `if (str1 == str2)` might give unexpected results.
    - Solution: Always use `if (str1.equals(str2))` for content comparison.

2. Forgetting to handle null values:
    - Pitfall: `str1.equals(str2)` throws NullPointerException if `str1` is null.
    - Solution: Use `Objects.equals(str1, str2)` or null checks before comparison.

3. Case sensitivity issues:
    - Pitfall: `"hello".equals("Hello")` returns false.
    - Solution: Use `equalsIgnoreCase()` for case-insensitive comparison.

## Best Practices and Optimization Techniques

1. Use `equals()` for content comparison.
2. Consider using `equalsIgnoreCase()` for case-insensitive comparisons.
3. For frequent comparisons, consider normalizing strings (e.g., converting to lowercase) once and then comparing.
4. Use `Objects.equals()` for null-safe comparisons.

## Edge Cases and Their Handling

1. Null strings:
   ```java
   String str1 = null;
   String str2 = "hello";
   boolean result = Objects.equals(str1, str2); // Safely handles null
   ```

2. Empty strings:
   ```java
   String str1 = "";
   String str2 = new String("");
   boolean result = str1.equals(str2); // true
   boolean sameObject = (str1 == str2); // false
   ```

## Interview-specific Insights

- Interviewers often ask about the difference between `equals()` and `==` for strings.
- Be prepared to explain string interning and its impact on `==` comparisons.
- Understand the performance implications of different comparison methods.

# Interview Q&A Section

Q1: What's the difference between `equals()` and `==` when comparing strings in Java?

```java
String str1 = "Hello";
String str2 = new String("Hello");
String str3 = "Hello";

System.out.println(str1.equals(str2)); // true
System.out.println(str1 == str2);      // false
System.out.println(str1 == str3);      // true
```

A1: The `equals()` method compares the content of the strings, while `==` compares the object references. In the example:
- `str1.equals(str2)` returns `true` because both strings have the same content.
- `str1 == str2` returns `false` because `str2` is created using `new`, so it's a different object.
- `str1 == str3` returns `true` because string literals with the same content are interned and share the same memory location.

Q2: How does string interning affect string comparison with `==`?

A2: String interning is a method of storing only one copy of each distinct string value in memory. In Java, string l
iterals are automatically interned. This means that when you create string literals with the same content, they will 
reference the same object in memory. As a result, comparing these strings with `==` will return `true`. However, strings
created with `new` or by string manipulation methods are not automatically interned, so `==` comparison with these 
strings may return `false` even if the content is the same.

Q3: How would you safely compare two strings that might be null?

```java
public boolean safeStringCompare(String str1, String str2) {
    return Objects.equals(str1, str2);
}
```

A3: The safest way to compare two potentially null strings is to use `Objects.equals(str1, str2)`. This method handles 
null values gracefully:
- If both strings are null, it returns true.
- If one is null and the other isn't, it returns false.
- If neither is null, it calls `str1.equals(str2)`.

This approach avoids NullPointerExceptions and provides a concise, null-safe comparison.

Q4: What's the performance difference between `equals()` and `==` for string comparison?

A4: The `==` operator is generally faster than `equals()` because it only compares memory addresses, which is a simple 
operation. However, `equals()` compares the actual content of the strings, which can be slower, especially for long 
strings. Despite this, `equals()` should be preferred for string comparison because it's more reliable for comparing 
string content. The performance difference is usually negligible in most applications, and correct behavior should be 
prioritized over minor performance gains.

Q5: How would you perform a case-insensitive string comparison in Java?

```java
String str1 = "Hello";
String str2 = "hello";
boolean result = str1.equalsIgnoreCase(str2);
System.out.println(result); // true
```

A5: For case-insensitive string comparison, use the `equalsIgnoreCase()` method. This method compares two strings, 
ignoring case considerations. It's more efficient than converting both strings to lowercase (or uppercase) and then 
comparing, as it avoids creating new String objects.

## Code Examples

- Test: [StringComparisonTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/literals/StringComparisonTest.java)
- Source: [StringComparison.java](src/main/java/com/github/msorkhpar/claudejavatutor/literals/StringComparison.java)
