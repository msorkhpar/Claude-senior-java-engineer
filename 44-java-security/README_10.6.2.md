# 10.6.2. Common Vulnerabilities

## Concept Explanation

Understanding common vulnerabilities is essential for any senior Java engineer. The OWASP Top 10 provides the industry
standard catalog of the most critical web application security risks. This section covers the three most relevant
categories for Java developers: injection flaws, authentication and access control flaws, and sensitive data exposure.

**Real-world analogy**: Imagine a fortress with multiple entry points. Each gate (input point) has its own set of
vulnerabilities: the front gate might be susceptible to battering rams (SQL injection), the side entrance might have a
weak lock (broken authentication), and the treasury room might have windows left open (sensitive data exposure). A
security-conscious architect addresses every entry point, not just the main gate.

### Injection Flaws

Injection occurs when untrusted data is sent to an interpreter as part of a command or query. The attacker's hostile data
tricks the interpreter into executing unintended commands or accessing unauthorized data.

- **SQL Injection**: Malicious SQL statements inserted through user input
- **XSS (Cross-Site Scripting)**: Malicious scripts injected into web pages viewed by other users
- **Command Injection**: OS commands injected through application input
- **LDAP Injection**: Manipulating LDAP queries through user input

### Authentication and Access Control Flaws

Broken authentication allows attackers to assume other users' identities. Broken access control allows unauthorized users
to access restricted resources.

- **Brute force attacks**: Automated password guessing
- **Session fixation**: Forcing a known session ID on a victim
- **Privilege escalation**: Accessing resources beyond authorized level
- **Insecure direct object references (IDOR)**: Accessing objects by manipulating identifiers

### Sensitive Data Exposure

Applications frequently fail to adequately protect sensitive data such as credentials, credit card numbers, and personal
information. Data can be exposed through:

- Insufficient encryption (or no encryption at all)
- Information leakage in error messages
- Logging sensitive data
- Insecure deserialization
- Timing attacks that reveal information through response times

## Key Points to Remember

- SQL injection is the single most dangerous vulnerability; always use parameterized queries (`PreparedStatement`)
- XSS prevention requires **output encoding** -- encode data for the context it is rendered in (HTML, JavaScript, URL)
- Never reveal whether a username or password was incorrect -- always use a generic message
- Implement account lockout or rate limiting to prevent brute force attacks
- Use **constant-time comparison** for security tokens to prevent timing attacks
- Java serialization is inherently insecure; prefer JSON/records for data transfer
- Role-based access control (RBAC) should be enforced at the service layer, not just the UI
- Error messages to clients should be generic; log detailed errors server-side only
- Store passwords as salted hashes (bcrypt, scrypt, Argon2) -- never in plain text

## Relevant Java 21 Features

- **Records** (JEP 395): Use records as secure DTOs with built-in validation via compact constructors, replacing
  insecure Java serialization
- **Pattern matching for switch** (JEP 441): Write cleaner access control logic with exhaustive matching on sealed
  permission hierarchies
- **Sealed classes** (JEP 409): Restrict role/permission hierarchies to prevent unauthorized extension
- **Virtual threads** (JEP 444): Handle many concurrent login/authentication requests efficiently without complex thread
  pool tuning
- **Text blocks** (JEP 378): Write SQL queries as text blocks to reduce escape-character bugs (still use
  PreparedStatement for parameterization)

### Evolution of Java security vulnerabilities

| Era           | Common Issue                        | Modern Mitigation                      |
|---------------|-------------------------------------|----------------------------------------|
| Java 1.x      | Applet sandbox escapes             | Applets removed (Java 11)              |
| Java 5-6      | Serialization gadget chains        | Records, JSON libraries                |
| Java 7-8      | XML External Entity (XXE)          | Secure parser defaults in Java 17+     |
| Java 9+       | Illegal reflective access          | Module system (JPMS)                   |
| Java 17+      | Security Manager bypass            | Deprecated; use JPMS instead           |

## Common Pitfalls and How to Avoid Them

### 1. Using string concatenation for SQL queries

```java
// VULNERABLE: SQL injection
String sql = "SELECT * FROM users WHERE name = '" + name + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);

// SECURE: Parameterized query
String sql = "SELECT * FROM users WHERE name = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, name);
ResultSet rs = stmt.executeQuery();
```

### 2. Rendering user input directly in HTML

```java
// VULNERABLE: XSS
String html = "<div>" + userInput + "</div>";

// SECURE: Encode output
String html = "<div>" + encodeHtml(userInput) + "</div>";
// Where encodeHtml replaces <, >, &, ", ' with HTML entities
```

### 3. Revealing authentication details in error messages

```java
// INSECURE: tells attacker the username exists
if (!userExists) return "User not found";
if (!passwordMatch) return "Incorrect password";

// SECURE: generic message
return "Invalid username or password";
```

### 4. Not implementing account lockout

```java
// INSECURE: unlimited login attempts
public boolean login(String user, String pass) {
    return checkCredentials(user, pass);
}

// SECURE: rate limiting
public boolean login(String user, String pass) {
    if (loginTracker.isLockedOut(user)) {
        throw new SecurityException("Account temporarily locked");
    }
    boolean success = checkCredentials(user, pass);
    if (!success) {
        loginTracker.recordFailedAttempt(user);
    } else {
        loginTracker.resetAttempts(user);
    }
    return success;
}
```

### 5. Using Java serialization for untrusted data

```java
// INSECURE: deserialization of untrusted data
ObjectInputStream ois = new ObjectInputStream(untrustedStream);
Object obj = ois.readObject(); // Can execute arbitrary code!

// SECURE: use JSON/records instead
record UserDTO(String name, String email) {}
UserDTO user = objectMapper.readValue(jsonString, UserDTO.class);
```

## Best Practices and Optimization Techniques

1. **Use parameterized queries everywhere**: Not just for WHERE clauses, but also for ORDER BY, LIMIT, table names (use
   allowlists for dynamic table/column names)
2. **Implement Content Security Policy (CSP)**: HTTP header that restricts which scripts can execute in the browser
3. **Use HttpOnly and Secure flags on cookies**: Prevents JavaScript access and ensures transmission only over HTTPS
4. **Implement CSRF tokens**: Prevent cross-site request forgery by validating tokens on state-changing operations
5. **Log security events**: Failed logins, permission denials, and input validation failures should be logged for
   monitoring and incident response
6. **Use a Web Application Firewall (WAF)**: Additional layer of defense that can catch common attack patterns
7. **Keep dependencies updated**: Vulnerable libraries (e.g., Log4Shell in log4j) are a major attack vector. Use tools
   like OWASP Dependency-Check or Snyk
8. **Principle of fail-secure**: When access control fails, deny access rather than allowing it

## Edge Cases and Their Handling

1. **Empty vs. null credentials**: Both should be rejected immediately; avoid passing them to authentication backends
2. **Concurrent login attempts**: Use thread-safe data structures (`ConcurrentHashMap`, `AtomicInteger`) for rate
   limiting
3. **Case sensitivity in usernames**: Normalize usernames (e.g., lowercase) before comparison to prevent bypass via
   capitalization
4. **Unicode normalization**: Attackers may use different Unicode representations of the same character; normalize before
   validation
5. **Very long input strings**: Set maximum lengths to prevent ReDoS (Regular Expression Denial of Service) and buffer
   overflow-like issues
6. **Null bytes in strings**: Some systems treat null bytes as string terminators; strip them from input

## Interview-specific Insights

Interviewers test for:

- Understanding of the OWASP Top 10 and how to mitigate each vulnerability in Java
- Ability to identify SQL injection, XSS, and other vulnerabilities in code snippets
- Knowledge of proper password storage (hashing with salt, bcrypt/Argon2)
- Understanding of why generic error messages are important for security
- Knowledge of timing attacks and constant-time comparison
- Awareness of Java deserialization vulnerabilities and modern alternatives

Tricky areas:

- "Show me a piece of code with a SQL injection vulnerability and fix it" -- be ready to do this on a whiteboard
- "What is a timing attack and how do you prevent it?"
- "Why is Java serialization considered insecure?"
- "How would you implement role-based access control?"

## Interview Q&A Section

**Q1: What is SQL injection and how do you prevent it in Java?**

```text
A1: SQL injection is an attack where malicious SQL code is inserted into application queries through user input.
When the application concatenates user input directly into SQL strings, the attacker can modify the query's logic
to bypass authentication, extract data, modify data, or even execute administrative operations.

Example attack: If a login query is built as:
  "SELECT * FROM users WHERE username = '" + input + "' AND password = '" + pass + "'"
An attacker can input: admin' --
This comments out the password check, granting access without a valid password.

Prevention in Java:
1. PreparedStatement (parameterized queries): The primary defense. Parameters are sent separately from the SQL
   command, so they can never be interpreted as SQL code.
2. Stored procedures: When appropriate, encapsulate SQL logic server-side.
3. Input validation: Whitelist acceptable characters (e.g., alphanumeric only for usernames).
4. ORM frameworks (JPA/Hibernate): Use JPQL/HQL with named parameters, which are parameterized by default.
5. Least privilege: Database account used by the application should not have DROP, CREATE, or ALTER permissions.

The key insight: SQL injection is NOT an input validation problem -- it is a query construction problem.
Parameterized queries solve it at the root cause level.
```

```java
// VULNERABLE
public boolean loginInsecure(Connection conn, String user, String pass) throws SQLException {
    String sql = "SELECT * FROM users WHERE username = '" + user + "' AND password = '" + pass + "'";
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(sql); // Attacker can inject SQL
    return rs.next();
}

// SECURE
public boolean loginSecure(Connection conn, String user, String pass) throws SQLException {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, user);
        stmt.setString(2, pass); // In practice, compare against hashed password
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        }
    }
}
```

**Q2: What is Cross-Site Scripting (XSS) and how do you prevent it?**

```text
A2: XSS occurs when an attacker injects malicious scripts into web pages viewed by other users. There are three
types:

1. Stored XSS: The malicious script is permanently stored on the target server (e.g., in a database). When
   other users view the affected page, the script executes in their browser.

2. Reflected XSS: The script is reflected off the web server, typically via a URL parameter or form submission,
   and immediately returned in the response.

3. DOM-based XSS: The vulnerability exists in client-side code that processes data from untrusted sources (e.g.,
   document.location) and writes it to the DOM.

Prevention in Java:
1. Output encoding: Encode all user-supplied data before rendering it in HTML. Replace <, >, &, ", ' with their
   HTML entity equivalents (&lt;, &gt;, &amp;, &quot;, &#x27;).
2. Content Security Policy (CSP): HTTP header that restricts script sources.
3. HttpOnly cookies: Prevents JavaScript from accessing session cookies.
4. Use templating engines: Thymeleaf, Freemarker, and JSP with JSTL auto-escape by default.
5. Validate input: Reject input that contains unexpected HTML/script content.

The key principle: "Encode output, not input." Store the original data and encode it at the point of rendering,
because different contexts (HTML body, HTML attribute, JavaScript, URL) require different encoding.
```

```java
public class XssPrevention {
    // HTML entity encoding for safe rendering
    public String encodeForHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }

    // VULNERABLE: directly embedding user input
    public String renderCommentInsecure(String userComment) {
        return "<div class='comment'>" + userComment + "</div>";
    }

    // SECURE: encoding before rendering
    public String renderCommentSecure(String userComment) {
        return "<div class='comment'>" + encodeForHtml(userComment) + "</div>";
    }
}
```

**Q3: How do you prevent brute force attacks on login endpoints?**

```text
A3: Brute force prevention requires multiple layers of defense:

1. Account lockout: After N failed attempts (typically 5-10), temporarily lock the account for a period (e.g.,
   15-30 minutes). Use an increasing lockout duration for repeated lockouts.

2. Rate limiting: Limit the number of login attempts per IP address per time window. Use a sliding window
   algorithm for accuracy.

3. CAPTCHA: After a threshold of failed attempts, require CAPTCHA completion. This prevents automated attacks
   while allowing legitimate users to continue.

4. Progressive delays: Introduce increasing delays between login attempts (e.g., 1s after 3rd failure, 2s after
   4th, 4s after 5th). This is called "tarpitting."

5. Multi-factor authentication (MFA): Even if the password is compromised, the attacker needs the second factor.

6. Password complexity requirements: Enforce minimum length, character diversity, and check against known
   breached passwords (e.g., using the HaveIBeenPwned API).

7. Monitoring and alerting: Log all failed login attempts and alert security teams on anomalous patterns.

Implementation considerations:
- Use ConcurrentHashMap for thread-safe tracking of attempts
- Store attempt counts with timestamps to implement sliding windows
- Be careful not to create a denial-of-service vector (attacker locks out legitimate users). Mitigate by also
  tracking by IP and using CAPTCHA as an alternative to hard lockout.
```

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BruteForceProtection {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MS = 15 * 60 * 1000L; // 15 minutes

    record AttemptRecord(AtomicInteger count, long lockoutUntil) {}

    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String username) {
        AttemptRecord record = attempts.get(username);
        if (record == null) return true;
        if (record.lockoutUntil > 0 && System.currentTimeMillis() < record.lockoutUntil) {
            return false; // Still locked out
        }
        return record.count.get() < MAX_ATTEMPTS;
    }

    public void recordFailure(String username) {
        attempts.compute(username, (key, existing) -> {
            if (existing == null) {
                return new AttemptRecord(new AtomicInteger(1), 0);
            }
            int newCount = existing.count.incrementAndGet();
            if (newCount >= MAX_ATTEMPTS) {
                return new AttemptRecord(existing.count, System.currentTimeMillis() + LOCKOUT_MS);
            }
            return existing;
        });
    }

    public void recordSuccess(String username) {
        attempts.remove(username);
    }
}
```

**Q4: What is a timing attack and how do you prevent it?**

```text
A4: A timing attack is a side-channel attack where an attacker measures the time taken to perform operations
to infer information about secret values. In the context of authentication:

How it works:
- Standard string comparison (String.equals()) returns false as soon as it finds a mismatching character
- If the first character is wrong, the comparison is faster than if only the last character is wrong
- By measuring response times across many requests, an attacker can guess the correct value one character
  at a time

Where it applies:
- API key/token comparison
- Password hash comparison (though hashing already provides some protection)
- HMAC verification
- CSRF token validation
- Session ID comparison

Prevention: Use constant-time comparison that always checks all characters regardless of where the mismatch
occurs.

Java provides:
- MessageDigest.isEqual(byte[], byte[]): Constant-time byte array comparison
- Custom implementation: XOR all characters and check the result at the end

Important: Even with constant-time comparison, ensure the comparison always happens (don't short-circuit with
an early return on different lengths without performing the comparison).
```

```java
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class TimingAttackPrevention {
    // VULNERABLE: early-return string comparison
    public boolean insecureCompare(String a, String b) {
        return a.equals(b); // Returns as soon as mismatch found
    }

    // SECURE: constant-time comparison
    public boolean constantTimeCompare(String a, String b) {
        if (a == null || b == null) return a == b;
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }

    // Alternative manual implementation
    public boolean constantTimeCompareManual(String a, String b) {
        if (a == null || b == null) return a == b;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
```

**Q5: Why is Java serialization considered a security risk?**

```text
A5: Java serialization (ObjectInputStream/ObjectOutputStream) has been called "a gift that keeps on giving" to
attackers. Here is why:

1. Arbitrary code execution: When an object is deserialized, its readObject() method is called automatically.
   If the classpath contains classes with dangerous readObject() implementations (called "gadget classes"),
   an attacker can craft a serialized payload that triggers arbitrary code execution.

2. Gadget chains: Libraries like Apache Commons Collections, Spring, and others contain classes that, when
   combined in specific ways during deserialization, allow Remote Code Execution (RCE). The attacker does not
   need to upload code -- they just need to craft the right byte sequence.

3. Widely exploited: Real-world attacks include the 2015 Apache Commons Collections vulnerability and the 2021
   Log4Shell (which used a similar trust-untrusted-data pattern).

4. Hard to fix: Simply validating the class being deserialized is difficult because the gadget chain may involve
   many intermediate classes.

Mitigations:
- Do NOT deserialize untrusted data using ObjectInputStream
- Use serialization filters (JEP 290, Java 9+) if you must use Java serialization
- Prefer JSON (Jackson, Gson) or Protocol Buffers for data exchange
- Use Java records as DTOs -- they are not Serializable by default and have built-in validation
- The Java team has proposed eventually removing serialization or making it opt-in

Senior engineer insight: If you see ObjectInputStream in a codebase, treat it as a red flag and investigate
whether it processes untrusted data.
```

```java
import java.io.*;
import java.util.Map;

public class DeserializationSecurity {
    // INSECURE: deserializing untrusted data
    public Object deserializeInsecure(byte[] data) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject(); // DANGER: can execute arbitrary code
        }
    }

    // SECURE: use records and JSON instead
    public record UserDTO(String username, String email, String role) {
        public UserDTO {
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Username required");
            }
        }
    }

    // MITIGATION: serialization filter (Java 9+)
    public Object deserializeWithFilter(byte[] data) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            ois.setObjectInputFilter(info -> {
                if (info.serialClass() != null) {
                    String className = info.serialClass().getName();
                    // Only allow specific classes
                    if (className.startsWith("com.myapp.dto.")) {
                        return ObjectInputFilter.Status.ALLOWED;
                    }
                    return ObjectInputFilter.Status.REJECTED;
                }
                return ObjectInputFilter.Status.UNDECIDED;
            });
            return ois.readObject();
        }
    }
}
```

**Q6: How do you implement role-based access control (RBAC) in Java?**

```text
A6: Role-based access control maps users to roles and roles to permissions. This provides a scalable and
maintainable authorization model.

Core components:
1. Roles: Named groups of permissions (e.g., GUEST, USER, ADMIN, SUPER_ADMIN)
2. Permissions: Specific actions that can be performed (e.g., READ, WRITE, DELETE, MANAGE_USERS)
3. Role-Permission mapping: A static or configurable mapping from roles to their granted permissions
4. Enforcement point: Code that checks permissions before allowing operations

Implementation approaches:
1. Annotation-based: Use custom annotations (@RequiresPermission(WRITE)) on methods
2. Programmatic: Check permissions explicitly in service methods
3. Framework-based: Spring Security, Apache Shiro provide mature RBAC implementations

Best practices:
- Define permissions granularly (not just "admin" or "user")
- Enforce at the service layer, not just the controller/UI layer
- Use sealed interfaces/enums for roles and permissions to prevent unauthorized extension
- Cache permission lookups for performance
- Log all permission denials for audit purposes
- Default to deny: if no explicit permission is granted, deny access
```

```java
import java.util.*;

public class RbacExample {
    enum Role { GUEST, USER, ADMIN, SUPER_ADMIN }
    enum Permission { READ, WRITE, DELETE, MANAGE_USERS }

    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = Map.of(
        Role.GUEST,       Set.of(Permission.READ),
        Role.USER,        Set.of(Permission.READ, Permission.WRITE),
        Role.ADMIN,       Set.of(Permission.READ, Permission.WRITE, Permission.DELETE),
        Role.SUPER_ADMIN, Set.of(Permission.values())
    );

    public void enforcePermission(Role role, Permission required) {
        Set<Permission> granted = ROLE_PERMISSIONS.getOrDefault(role, Set.of());
        if (!granted.contains(required)) {
            throw new SecurityException(
                "Role %s lacks permission %s".formatted(role, required));
        }
    }

    // Usage in a service method
    public void deleteDocument(Role callerRole, String documentId) {
        enforcePermission(callerRole, Permission.DELETE);
        // proceed with deletion
    }
}
```

## Code Examples

- Source: [CommonVulnerabilities.java](src/main/java/com/github/msorkhpar/claudejavatutor/javasecurity/CommonVulnerabilities.java)
- Test: [CommonVulnerabilitiesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javasecurity/CommonVulnerabilitiesTest.java)
