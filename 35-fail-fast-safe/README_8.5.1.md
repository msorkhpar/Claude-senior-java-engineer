# 8.5.1. Definition and Purpose of Fail-Fast and Fail-Safe Iterators

At 02:00 the renewal-reminder job stops with a `NullPointerException`. The one reminder it sent
before stopping quoted Jane Doe a price of minus 560 dollars. The stack trace names the reminder
job's methods and `main`. Both customers came in at 14:05 from a partner's CSV file, and nothing in
the trace says so.

Unit 6.5.3 showed fail-fast in an `ArrayList` iterator, which usually throws at the next step after
the list changes under it. More generally, a component handed something it can't work with throws
at once, while the caller and the bad value are both on the stack. Fail-safe is the opposite
choice: carry on with a substitute. Units 8.5.2 and 8.5.3 return to iterators, the weakly
consistent kind and how to choose one. Every output is from Java 21.

## A Null Stored Now Fails Later, in a Trace That Doesn't Name Who Stored It

The customer is a plain record, and the importer copies whatever the file holds:

```java
public record Customer(String name, String email, int discountPercent) {}
```

```java
static Customer importRow(Map<String, String> row) {          // Importer, 14:05
    return new Customer(row.get("name"), row.get("email"), Integer.parseInt(row.get("discount")));
}
```

```java
static String mailbox(Customer c) {                            // Reminders, 02:00
    return c.email().strip().toLowerCase(Locale.ROOT);
}

static long price(long cents, Customer c) {
    return cents * (100 - c.discountPercent()) / 100;
}
```

The partner's file had a row for Jane Doe with discount `1500` (the partner meant 15.00 % and sent
basis points), and a row for John Roe with no email column. The program imports both rows, then
runs the job, where `sendAll` loops over the customers and `send` prints one reminder:

```text
imported 2 customers
to jane@example.com: your plan renews at -56000 cents
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "String.strip()" because the return value of "Customer.email()" is null
	at Reminders.mailbox(Reminders.java:15)
	at Reminders.send(Reminders.java:11)
	at Reminders.sendAll(Reminders.java:7)
	at Main.main(Main.java:13)
```

Before reading on: which line would you fix?

Java 21's message names the null exactly, the return value of `Customer.email()`. It can't name
where the null came from. A stack trace holds the frames that are on the stack when the exception
is created, and the constructor that stored the null returned twelve hours earlier. To find it you
search for every place that builds a `Customer`, and to find the row you need the partner's file,
which by now may have been replaced by the next one. The loop has no catch, so the first bad
customer also ends the run for every customer after it.

A discount of 1500 is a valid `int` and the arithmetic is valid, so for Jane's row nothing
threw, and the reminder went out with a negative price. No later null check would have seen it:
the value is present and wrong.

## A Check in the Constructor Puts the Caller in the Trace

Every `Customer` is made by its constructor, so a check there runs while the code holding the bad
value is still on the stack:

```java
import java.util.Objects;

/** A customer as the import stores it. */
public record Customer(String name, String email, int discountPercent) {
    public Customer {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(email, "email");
        if (email.isBlank())
            throw new IllegalArgumentException("email is blank");
        if (discountPercent < 0 || discountPercent > 100)
            throw new IllegalArgumentException("discountPercent " + discountPercent + " is not in 0..100");
    }
}
```

The same importer, run once with John's row and once with Jane's:

```text
Exception in thread "main" java.lang.NullPointerException: email
	at java.base/java.util.Objects.requireNonNull(Objects.java:259)
	at Customer.<init>(Customer.java:7)
	at Importer.importRow(Importer.java:6)
	at Main.main(Main.java:10)
```

```text
Exception in thread "main" java.lang.IllegalArgumentException: discountPercent 1500 is not in 0..100
	at Customer.<init>(Customer.java:11)
	at Importer.importRow(Importer.java:6)
	at Main.main(Main.java:10)
```

The frame under the constructor is now the importer, at 14:05, with the row in a local variable.
The importer can catch the exception per row, report "row 2: email" to the partner while the file
is still at hand, and store nothing the 02:00 job can trip on. A `Customer` with a discount of 1500
can no longer exist, so neither can the negative price.

`requireNonNull` accepts `""` and `" "`, which is what a file that has the column but leaves the
cell empty gives you. A row whose email cell holds a space stops the same import with
`IllegalArgumentException: email is blank`.

A check in the constructor holds only while the object can't change afterwards. `String` and `int`
fields can't, but a `List` argument is still held by the caller, and
`Collections.unmodifiableList` is a read-only view of the caller's list, not a copy:

```java
List<String> tags = new ArrayList<>(List.of("vip"));
List<String> view = Collections.unmodifiableList(tags);
List<String> copy = List.copyOf(tags);
tags.add(null);                                     // the caller, after the constructor returned
System.out.println("view: " + view + ", copy: " + copy);
```

```text
view: [vip, null], copy: [vip]
```

`List.copyOf` returns an unmodifiable list that doesn't follow the caller's, and it throws
`NullPointerException` if the list holds a null, so one call checks the elements and keeps them
checked. A record with a `List<String> tags` component stores `List.copyOf(tags)`.

### An Assertion Checks Nothing Unless the JVM Runs with -ea

The range check written as an assertion:

```java
record Customer(String name, String email, int discountPercent) {
    Customer {
        assert discountPercent >= 0 && discountPercent <= 100 : "discountPercent " + discountPercent;
    }
}
```

```text
$ java Asserts
Customer[name=Jane Doe, email=jane@example.com, discountPercent=1500]
$ java -ea Asserts
Exception in thread "main" java.lang.AssertionError: discountPercent 1500
	at Asserts$Customer.<init>(Asserts.java:4)
	at Asserts.main(Asserts.java:9)
```

Maven's Surefire plugin enables assertions for tests by default, so this check fails the right
test and is absent from a production JVM started without `-ea`. A value that
comes from outside the class gets an `if` and an exception.

<!-- practice 1 -->

## A Default Belongs Where Refusing to Start Costs More Than the Default

Fail-fast applied to configuration says a service with a setting missing should not start. The
payment client reads three settings at startup. `gateway.url` has no safe value; `http.timeout.ms`
and `retries` do, and `retries` is new in this release. Two loaders, where `required(file, key)`
throws when the key is absent:

```java
/** Every key must be present: no defaults. */
static Settings strict(Properties file) {
    return new Settings(required(file, "gateway.url"),
            Integer.parseInt(required(file, "http.timeout.ms")),
            Integer.parseInt(required(file, "retries")));
}

/** A default for every optional key, and nothing else checked. */
static Settings lenient(Properties file) {
    return new Settings(required(file, "gateway.url"),
            Integer.parseInt(file.getProperty("http.timeout.ms", "5000")),
            Integer.parseInt(file.getProperty("retries", "2")));
}
```

Both read the config file written for the last release, which has no `retries` line:

```properties
gateway.url=https://pay.example.com
http.timeout.ms=2000
```

```text
strict  last release: refused to start: java.lang.IllegalArgumentException: missing setting retries
lenient last release: Settings[gatewayUrl=https://pay.example.com, timeoutMs=2000, retries=2]
```

With the strict loader, the upgrade is also a config migration: every environment's file must gain
the new line before the new version starts, and each instance whose file was missed stays down.
Running with two retries, a value nobody had to choose, costs less than an instance that doesn't
come up.

`gateway.url` stays required in both loaders. Any URL the code could fall back to would send
payments somewhere they shouldn't go, so a missing one must stop the start.

## A Default Also Hides a Misspelt Key, Unless Unknown Keys Stop the Start

During an incident the gateway answers slowly, and the on-call engineer edits the file so callers
give up after 800 ms instead of 2 s, then restarts:

```properties
gateway.url=https://pay.example.com
http.timout.ms=800
```

```text
lenient incident:     Settings[gatewayUrl=https://pay.example.com, timeoutMs=5000, retries=2]
```

Spot the problem before reading on.

`timout` is missing an `e`. `getProperty("http.timeout.ms", "5000")` finds no such key and returns
the default, so the service restarts cleanly and now waits up to 5 s per attempt, longer than
before the edit. Request threads pile up behind the gateway while the engineer believes the fix is
live.

This loader keeps its defaults and refuses every key it doesn't know:

```java
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/** The payment client's settings, read once when the service starts. */
public record Settings(String gatewayUrl, int timeoutMs, int retries) {
    static final Map<String, String> DEFAULTS = Map.of("http.timeout.ms", "5000", "retries", "2");
    static final String REQUIRED = "gateway.url";

    static Settings load(Properties file) {
        Set<String> unknown = new TreeSet<>(file.stringPropertyNames());
        unknown.removeAll(DEFAULTS.keySet());
        unknown.remove(REQUIRED);
        if (!unknown.isEmpty())
            throw new IllegalArgumentException("unknown settings " + unknown);
        String url = file.getProperty(REQUIRED);
        if (url == null)
            throw new IllegalArgumentException("missing setting " + REQUIRED);
        return new Settings(url,
                Integer.parseInt(file.getProperty("http.timeout.ms", DEFAULTS.get("http.timeout.ms"))),
                Integer.parseInt(file.getProperty("retries", DEFAULTS.get("retries"))));
    }
}
```

```text
load    last release: Settings[gatewayUrl=https://pay.example.com, timeoutMs=2000, retries=2]
load    incident:     refused to start: java.lang.IllegalArgumentException: unknown settings [http.timout.ms]
```

The last release's file still loads with the default, and the typo stops the restart with the
key's name in the message. In a rolling restart that halts on a failed start, the first instance
fails and the others keep serving. Logging a warning and carrying on would still run with 5,000 ms.

A value that doesn't parse is already a failure: `http.timeout.ms=800ms` stops `lenient` and `load` with
`NumberFormatException: For input string: "800ms"`. A loader that catches it and uses the default
would turn that failure back into the silent 5 s.

The rule has a cost too. Once a release refuses unknown keys, rolling back to it with a newer
release's file fails: a loader that never heard of a key the newer file sets refuses to start.
Old versions must either know the keys newer ones may write or tolerate them.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **A null stored now fails later, in a trace that doesn't name who stored it.** Java 21's message
  names the null, not the code that stored it, and an out-of-range value doesn't fail at all.
- **A check in the constructor puts the caller in the trace.** Check null, blank and range with an
  `if`, not `assert`, and copy what the caller could still change.
- **A default belongs where refusing to start costs more than the default.** A new optional key
  gets one; a key with no safe value stays required.
- **A default also hides a misspelt key.** Refusing unknown keys keeps the default for absent keys
  and turns a typo into a failed start.

## Reference

| Call (Java 21) | With a bad value |
|---|---|
| `Objects.requireNonNull(x, "email")` | `NullPointerException: email`; lets `""` and `" "` through |
| `Objects.requireNonNull(x, () -> "email of row " + row)` | builds the message only when `x` is null |
| `Objects.requireNonNullElse(x, fallback)` | returns `fallback` for a null `x` (a fail-safe default); throws if `fallback` is null too |
| `Objects.checkIndex(1500, 101)` | `IndexOutOfBoundsException: Index 1500 out of bounds for length 101` |
| `List.copyOf(list)` | an unmodifiable list that doesn't follow the argument; `NullPointerException` on a null element |
| `Collections.unmodifiableList(list)` | a read-only view; the caller's later changes show through it |
| `assert cond : msg` | nothing, unless the JVM runs with `-ea`; then `AssertionError` |
| `Properties.getProperty(key, fallback)` | `fallback` when the key is absent, misspelt keys included; `""` when it is present and empty |
| `Integer.parseInt("800ms")` | `NumberFormatException: For input string: "800ms"` |
| `Properties.load` of the line `workers=16 per node` | stores the whole value `"16 per node"`, which `Integer.parseInt` refuses the same way |

## Challenges

1. The partner sends 40,000 rows a night, and a dozen have no email. Do you refuse the file, refuse
   the rows, or store them flagged? What must the reminder job know in each case?
2. Settings come from environment variables instead of a file. How do you catch a misspelt variable
   name when the environment holds hundreds of names your service doesn't own?
3. Two services share one config file, each knowing half its keys. What becomes of refusing unknown
   keys, and what would you change?
4. Find a `requireNonNullElse` or `getOrDefault` in code you know. Is it a fail-safe default, or
   does it hide a bug?

## Design Note: Why the Check Goes Where the Object Is Made

A check could sit in each method that uses a field, or once in the constructor. In the methods,
every new method has to remember it, and the failure still comes late. In the constructor, the
type itself carries the guarantee: every `Customer` that exists has an email and a discount between
0 and 100, and the code that uses one checks nothing. The cost is that every way of building one
pays the check, including a test that wants a half-filled object. Data that was accepted under
older rules, such as an old row with no email, can't be loaded at all until someone decides what
it means, and the constructor forces that decision at import time rather than at 02:00.

## Code Examples

- Test: [FailFastIteratorDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailFastIteratorDemoTest.java)
- Source: [FailFastIteratorDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailFastIteratorDemo.java)
- Test: [FailSafeIteratorDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailSafeIteratorDemoTest.java)
- Source: [FailSafeIteratorDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailSafeIteratorDemo.java)
