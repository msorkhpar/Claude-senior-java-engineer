# 10.6.1. Security Best Practices in Java

## Concept Explanation

Security in Java is not a feature you bolt on at the end -- it is a discipline woven into every line of code from the
very beginning. Security best practices encompass secure coding guidelines, input validation and sanitization, and secure
configuration and deployment.

**Real-world analogy**: Think of building a bank vault. You do not add the steel doors and alarm systems after the
building is complete. Instead, the entire structure -- from the foundation to the walls to the locking mechanisms -- is
designed with security in mind from day one. Similarly, secure Java code starts with how you validate inputs, handle
errors, manage resources, and protect sensitive data.

Java provides a rich set of tools and patterns for writing secure code:

- **Input validation** prevents attackers from injecting malicious data
- **Defensive copying** ensures mutable objects cannot be modified externally
- **Path traversal prevention** blocks unauthorized filesystem access
- **Integer overflow protection** avoids silent arithmetic bugs
- **Sensitive data masking** prevents accidental exposure of PII
- **Try-with-resources** guarantees proper resource cleanup

The Java platform itself includes the Java Security Architecture (JSA), which provides cryptographic services,
authentication, authorization, and secure communication. Modern Java versions (17+, 21+) have deprecated the Security
Manager in favor of stronger, more targeted security mechanisms.

## Key Points to Remember

- **Never trust user input** -- always validate, sanitize, and constrain
- Use `Objects.requireNonNull()` for fail-fast null checking
- Prefer **immutable objects** (records, `List.copyOf()`, `Map.copyOf()`) to prevent unintended state modification
- Use `Math.addExact()` and `Math.multiplyExact()` to detect integer overflow
- Always use **try-with-resources** for I/O operations to prevent resource leaks
- Mask sensitive data (credit cards, emails, SSNs) before logging or displaying
- Validate file paths against a base directory to prevent path traversal
- Limit file sizes when reading user-supplied files
- Use **regex patterns** compiled as static constants for input validation (avoid recompilation)
- Return **generic error messages** to users -- never expose stack traces or internal details

## Relevant Java 21 Features

- **Records** (JEP 395): Immutable data carriers with built-in validation in compact constructors, ideal for secure DTOs
- **Sealed classes** (JEP 409): Restrict which classes can extend a type, limiting attack surface
- **Pattern matching for switch** (JEP 441): Write cleaner validation logic with exhaustive pattern matching
- **Text blocks** (JEP 378): Avoid escape-sequence errors in SQL queries and HTML templates
- **Deprecation of Security Manager** (JEP 411): Java 17 deprecated the Security Manager; Java 21 continues this path.
  Use module system (`java.lang.module`) and other targeted mechanisms instead

### Evolution across Java versions

| Version  | Security Enhancement                                    |
|----------|---------------------------------------------------------|
| Java 1.0 | Basic sandbox model for applets                        |
| Java 1.2 | Fine-grained access control with Security Manager      |
| Java 9   | Module system (JPMS) for strong encapsulation           |
| Java 14  | Records (preview) for immutable data                   |
| Java 17  | Security Manager deprecated (JEP 411)                  |
| Java 21  | Virtual threads (thread-per-request simplifies auth)   |

## Common Pitfalls and How to Avoid Them

### 1. String concatenation in SQL queries

```java
// BAD - vulnerable to SQL injection
String query = "SELECT * FROM users WHERE name = '" + userInput + "'";

// GOOD - use PreparedStatement
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
stmt.setString(1, userInput);
```

### 2. Returning mutable internal collections

```java
// BAD - caller can modify internal state
public List<String> getItems() {
    return this.items;
}

// GOOD - return defensive copy or unmodifiable view
public List<String> getItems() {
    return List.copyOf(this.items);
}
```

### 3. Ignoring integer overflow

```java
// BAD - silently wraps around
int total = Integer.MAX_VALUE + 1; // becomes Integer.MIN_VALUE

// GOOD - throws ArithmeticException on overflow
int total = Math.addExact(Integer.MAX_VALUE, 1);
```

### 4. Logging sensitive data

```java
// BAD - exposes password in logs
logger.info("Login attempt for user {} with password {}", user, password);

// GOOD - never log sensitive data
logger.info("Login attempt for user {}", user);
```

### 5. Not limiting resource consumption

```java
// BAD - reads entire file regardless of size
String content = Files.readString(path);

// GOOD - check file size first
long size = Files.size(path);
if (size > MAX_ALLOWED_SIZE) {
    throw new SecurityException("File too large");
}
String content = Files.readString(path);
```

## Best Practices and Optimization Techniques

1. **Compile regex patterns once**: Store `Pattern` objects as `static final` fields rather than recompiling in each
   method call
2. **Use the principle of least privilege**: Grant code only the minimum permissions it needs
3. **Prefer allow-lists over deny-lists**: Validate that input matches an expected pattern rather than trying to block
   all bad patterns
4. **Clear sensitive data from memory**: Overwrite char arrays (used for passwords) when done; avoid `String` for secrets
   since strings are immutable and may linger in the string pool
5. **Centralize validation logic**: Create reusable validation utilities to ensure consistent enforcement
6. **Use Java records for DTOs**: Records are immutable by default and support compact constructors for validation
7. **Configure secure defaults**: Set restrictive defaults for timeouts, max sizes, and allowed characters
8. **Apply defense in depth**: Do not rely on a single layer of validation; validate at every boundary (API layer,
   service layer, persistence layer)

## Edge Cases and Their Handling

1. **Null inputs**: Use `Objects.requireNonNull()` with descriptive messages; decide whether null means "no value" or
   "error"
2. **Empty strings vs blank strings**: An empty string `""` passes non-null checks but may still be invalid; use
   `isBlank()` for whitespace-only strings
3. **Unicode and encoding attacks**: Normalize Unicode input before validation to prevent homoglyph attacks (e.g., using
   Cyrillic "a" instead of Latin "a")
4. **Path traversal with symbolic links**: Always call `normalize()` on resolved paths and check with `startsWith()`
5. **Integer boundary values**: `Integer.MIN_VALUE` and `Integer.MAX_VALUE` behave unexpectedly with negation and
   absolute value
6. **Concurrent modification of shared state**: Use immutable objects or concurrent collections for data shared across
   threads

## Interview-specific Insights

Interviewers frequently test:

- Awareness of OWASP Top 10 vulnerabilities and how Java mitigates them
- Understanding of input validation vs. sanitization (validation rejects bad input; sanitization cleans it)
- Knowledge of defensive programming techniques (defensive copies, immutable objects)
- Ability to identify security flaws in code snippets
- Understanding of Java's Security Manager deprecation and modern alternatives
- Practical experience with secure resource handling (try-with-resources)

Common tricky questions:

- "Why should passwords be stored in char[] rather than String?"
- "What is the difference between validation and sanitization?"
- "How does path traversal work and how do you prevent it?"
- "Why is `Math.addExact()` preferable to the `+` operator for security-sensitive calculations?"

## Interview Q&A Section

**Q1: What is the difference between input validation and input sanitization?**

```text
A1: Input validation and input sanitization are complementary but distinct techniques:

Input Validation:
- Checks whether the input conforms to an expected format, type, length, or range
- Rejects invalid input entirely (fail-fast approach)
- Examples: checking email format with regex, ensuring age is between 0-150, verifying username is alphanumeric
- Preferred for security because it is stricter: if input does not match the whitelist, it is rejected

Input Sanitization:
- Transforms input to remove or neutralize dangerous content
- Allows the input through after cleaning it
- Examples: HTML-encoding special characters (<, >, &), stripping SQL keywords, removing null bytes
- Used when you must accept a wide range of input (e.g., user comments that may contain HTML-like text)

Best Practice: Validate first, then sanitize. If validation fails, reject the input outright. If the input passes
validation, sanitize it as a defense-in-depth measure before using it in HTML output, SQL queries, etc.
```

```java
public class ValidationVsSanitization {
    // Validation: rejects invalid input
    public boolean validateAge(String input) {
        try {
            int age = Integer.parseInt(input);
            return age >= 0 && age <= 150;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Sanitization: cleans input for safe HTML rendering
    public String sanitizeForHtml(String input) {
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
```

**Q2: Why should passwords be stored in char[] rather than String?**

```text
A2: This is a classic Java security interview question. The reasons are:

1. Strings are immutable: Once a String is created, it cannot be modified. A password stored in a String remains in
   memory until the garbage collector reclaims it. You have no control over when that happens.

2. String pooling: String literals and some dynamically created strings may be interned in the String pool, keeping
   them in memory even longer than a regular heap object.

3. Memory dumps: If a memory dump or heap dump is taken (e.g., during debugging, crash analysis, or a security
   incident), any password stored as a String will be visible in plain text.

4. char[] can be explicitly cleared: After authentication, you can overwrite the char[] with zeros or random
   characters, immediately removing the password from memory.

This is why Java's Console.readPassword() returns char[] instead of String, and why the JPasswordField in Swing
provides getPassword() returning char[].

Note: This is a defense-in-depth measure, not a complete solution. A determined attacker with memory access can still
capture the password before it is cleared. However, it significantly reduces the window of exposure.
```

```java
public class PasswordHandling {
    public void authenticateUser() {
        char[] password = getPasswordFromUser(); // e.g., Console.readPassword()
        try {
            boolean authenticated = authenticate(password);
            if (authenticated) {
                // proceed
            }
        } finally {
            // Immediately clear the password from memory
            java.util.Arrays.fill(password, '\0');
        }
    }

    private char[] getPasswordFromUser() {
        // In real code: return System.console().readPassword("Password: ");
        return new char[]{'s', 'e', 'c', 'r', 'e', 't'};
    }

    private boolean authenticate(char[] password) {
        // Compare against hashed password from database
        return true; // simplified
    }
}
```

**Q3: How do you prevent path traversal attacks in Java?**

```text
A3: Path traversal (also called directory traversal) occurs when an attacker manipulates file paths to access files
outside the intended directory. For example, a user might supply "../../etc/passwd" as a filename.

Prevention strategy:
1. Resolve the user-supplied path against a known safe base directory
2. Normalize the result (resolve ".." and "." components)
3. Verify the normalized path still starts with the base directory
4. Optionally, check that the filename does not contain path separators

This approach is essential in any application that serves files based on user input (file upload/download, document
management, etc.).

Additional defenses:
- Use allowlists for permitted file extensions
- Limit filename length
- Reject filenames with null bytes (which can truncate paths in some systems)
- Run the application with minimal filesystem permissions
```

```java
import java.nio.file.Path;

public class PathTraversalPrevention {
    private final Path baseDirectory;

    public PathTraversalPrevention(Path baseDirectory) {
        this.baseDirectory = baseDirectory.toAbsolutePath().normalize();
    }

    public Path resolveSafely(String userSupplied) {
        // Resolve against base and normalize
        Path resolved = baseDirectory.resolve(userSupplied).normalize();

        // Verify the resolved path is still within the base directory
        if (!resolved.startsWith(baseDirectory)) {
            throw new SecurityException("Path traversal attempt detected: " + userSupplied);
        }

        return resolved;
    }
}
```

**Q4: What is defensive copying and when should you use it?**

```text
A4: Defensive copying is the practice of creating copies of mutable objects when they cross trust boundaries -
specifically when receiving them as constructor/method arguments or returning them from getters.

When to use:
1. In constructors: Copy mutable arguments so external code cannot modify internal state after construction
2. In getters: Return copies of mutable fields so external code cannot modify internal state through the returned
   reference
3. In setters: Copy the incoming value before storing it

Why it matters for security:
- Without defensive copies, an attacker (or buggy code) can modify an object's internal state after validation
   has occurred (a "time-of-check to time-of-use" or TOCTOU vulnerability)
- Example: If a constructor validates a Date parameter but stores the original reference, the caller can modify
   the Date after construction, bypassing the validation

Modern Java alternative:
- Java records are immutable by default - no defensive copying needed for the record itself
- List.copyOf(), Map.copyOf(), Set.copyOf() create unmodifiable copies
- Use these in constructors to ensure collections cannot be modified externally
```

```java
import java.util.List;
import java.util.Objects;

public class DefensiveCopyExample {
    // INSECURE: stores reference to mutable list
    static class InsecureConfig {
        private final List<String> origins;
        public InsecureConfig(List<String> origins) {
            this.origins = origins; // Caller can still modify!
        }
        public List<String> getOrigins() {
            return origins; // Caller can modify internal state!
        }
    }

    // SECURE: defensive copying on input and output
    static class SecureConfig {
        private final List<String> origins;
        public SecureConfig(List<String> origins) {
            this.origins = List.copyOf(Objects.requireNonNull(origins)); // Defensive copy
        }
        public List<String> getOrigins() {
            return origins; // Already unmodifiable via List.copyOf
        }
    }
}
```

**Q5: How does Java's try-with-resources help with security?**

```text
A5: Try-with-resources (introduced in Java 7) ensures that resources implementing AutoCloseable are properly closed
after use, even if an exception occurs. This has several security implications:

1. Prevents resource leaks: Unclosed file handles, database connections, or network sockets can lead to denial of
   service. An attacker could exhaust the system's file descriptors or connection pool.

2. Ensures proper cleanup: Some resources hold sensitive data in memory (e.g., input streams reading encryption
   keys). Proper closing triggers cleanup of these internal buffers.

3. Prevents data corruption: Unclosed output streams may not flush their buffers, leading to partial writes that
   could compromise data integrity.

4. Eliminates complex finally blocks: Before try-with-resources, developers had to write error-prone finally blocks
   to close resources. Missing or incorrect finally blocks were a common source of resource leaks.

In Java 9+, you can use effectively final variables in try-with-resources:
    InputStream in = openStream();
    try (in) { ... }

Best practice: Always use try-with-resources for any AutoCloseable resource. This includes streams, connections,
readers, writers, and any custom resources that need cleanup.
```

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TryWithResourcesSecurity {
    // INSECURE: resource leak if exception occurs
    public String readFileInsecure(Path path) throws IOException {
        var reader = Files.newBufferedReader(path);
        return reader.readLine(); // reader never closed if readLine throws
    }

    // SECURE: resource automatically closed
    public String readFileSecure(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            return reader.readLine();
        } // reader.close() called automatically, even on exception
    }

    // SECURE with size limit: defense in depth
    public String readFileSafely(Path path, long maxBytes) throws IOException {
        long size = Files.size(path);
        if (size > maxBytes) {
            throw new SecurityException("File exceeds maximum allowed size");
        }
        try (var reader = Files.newBufferedReader(path)) {
            return reader.readLine();
        }
    }
}
```

**Q6: What is the principle of least privilege and how do you apply it in Java?**

```text
A6: The principle of least privilege states that every module, process, or user should have only the minimum
permissions necessary to perform its function. In Java:

1. Module system (JPMS): Use module-info.java to explicitly declare which packages are exported. Internal
   implementation packages remain inaccessible to other modules.

2. Access modifiers: Use the most restrictive access level possible:
   - private for internal implementation details
   - package-private (default) for module-internal collaboration
   - protected sparingly, only when subclass access is genuinely needed
   - public only for the intended API surface

3. Immutable objects: Make fields final, classes final (or sealed), and collections unmodifiable. This prevents
   code from modifying state it should only read.

4. Database permissions: Use database accounts with minimal privileges (e.g., SELECT-only for read operations).

5. File system permissions: Run applications with a dedicated user that has access only to necessary directories.

6. Container security: In Docker/Kubernetes, run as a non-root user and mount only required volumes as read-only.

This principle limits the blast radius of a security breach. If an attacker compromises one component, they can
only access what that component was permitted to access.
```

```java
// Applying least privilege through access modifiers and sealed types
public sealed interface PaymentProcessor permits CreditCardProcessor, BankTransferProcessor {
    void processPayment(double amount);
}

// Only these two classes can implement PaymentProcessor
public final class CreditCardProcessor implements PaymentProcessor {
    private final String merchantId; // private - least privilege

    public CreditCardProcessor(String merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public void processPayment(double amount) {
        // Only has access to credit card processing logic
    }
}

public final class BankTransferProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        // Only has access to bank transfer logic
    }
}
```

## Code Examples

- Source: [SecureCodingPractices.java](src/main/java/com/github/msorkhpar/claudejavatutor/javasecurity/SecureCodingPractices.java)
- Test: [SecureCodingPracticesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javasecurity/SecureCodingPracticesTest.java)
