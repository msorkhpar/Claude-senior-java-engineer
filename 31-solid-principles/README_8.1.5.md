# 8.1.5. Dependency Inversion Principle (DIP)

The refund rule is three lines: refund a purchase made within 30 days, and tell the customer. Its
unit test fails on every CI run with `IllegalStateException: SMTP_HOST is not set`. On a laptop
where the variable is set, the same test sends a real email. Nothing is wrong with the rule: the
policy builds its own mailer.

The Dependency Inversion Principle says the policy declares the interface it needs, in its own
terms, and the mail code implements it.

## A Policy That Builds Its Own Mailer Can't Run Without a Mail Server

```java
/** The production detail: talks to a mail server named in the environment. */
final class SmtpMailer {
    private final String host;

    SmtpMailer() {
        host = System.getenv("SMTP_HOST");
        if (host == null) throw new IllegalStateException("SMTP_HOST is not set");
    }

    void send(String to, String subject) {
        System.out.println("  SMTP " + host + ": " + subject + " -> " + to);
    }
}
```

```java
/** The policy: refunds within 30 days of purchase, and tells the customer. */
final class RefundPolicy {
    private final SmtpMailer mailer = new SmtpMailer();

    boolean refund(String customer, int daysSincePurchase) {
        if (daysSincePurchase > 30) return false;
        mailer.send(customer, "Your refund is approved");
        return true;
    }
}
```

The test of the rule checks a refusal on day 31 and an approval on day 30. On CI, and then with
`SMTP_HOST=mail.example.com`:

```text
test failed: java.lang.IllegalStateException: SMTP_HOST is not set
```

```text
day 31 refused: true
  SMTP mail.example.com: Your refund is approved -> jane@example.com
day 30 approved: true
```

The day-31 check never sends anything and fails anyway: the field initializer runs in the
constructor, before any rule. A class that calls `new` on its detail can't be built without that
detail, so every test of the class needs a mail server and sends real mail.

The fix gives the policy an interface and takes the implementation as a constructor parameter:

```java
/** What the refund policy needs from the outside world, in its own words. */
public interface RefundNotifier {
    void refundApproved(String customer);
}
```

```java
import java.util.Objects;

final class RefundPolicy {
    private final RefundNotifier notifier;

    RefundPolicy(RefundNotifier notifier) {
        this.notifier = Objects.requireNonNull(notifier, "notifier");
    }

    boolean refund(String customer, int daysSincePurchase) {
        if (daysSincePurchase > 30) return false;
        notifier.refundApproved(customer);
        return true;
    }
}
```

The test passes `told::add`, the `add` method of a list named `told`, as a fake that remembers
whom it told. It runs without `SMTP_HOST`:

```text
day 31 refused: true
day 30 approved: true
told: [jane@example.com]
```

The current time is a dependency of the same kind; unit 7.3.2 passes a `Clock` the same way.

## The Interface Belongs to the Policy, So the Policy Compiles Without the Mail Code

A constructor parameter of an interface type is dependency injection. It is not yet inversion.
Here the mail team published the interface, in their own package and their own terms:

```java
package smtp;

public interface MailTransport {
    void send(Envelope envelope);

    record Envelope(String from, String to, String subject) {}
}
```

```java
package billing;

import smtp.MailTransport;

public final class RefundPolicy {
    private final MailTransport mail;

    public RefundPolicy(MailTransport mail) {
        this.mail = mail;
    }

    public boolean refund(String customer, int daysSincePurchase) {
        if (daysSincePurchase > 30) return false;
        mail.send(new MailTransport.Envelope("refunds@example.com", customer, "Your refund is approved"));
        return true;
    }
}
```

The billing and mail code build as separate jars. Compiling billing on its own:

```text
$ javac -Xmaxerrs 1 -d ../out/policy billing/*.java
billing/RefundPolicy.java:3: error: package smtp does not exist
import smtp.MailTransport;
           ^
1 error
only showing the first 1 errors, of 4 total; use -Xmaxerrs if you would like to see more
```

The interface lives in `smtp`, so billing still imports the mail code. Billing can't be built, tested or released without the smtp jar, and a push or SMS
channel added later has to fill in a `from` address and a subject to fit an envelope.

Move the interface to `billing`, named for what the policy needs (the `RefundNotifier` above), and
make `smtp.SmtpNotifier implements billing.RefundNotifier`. Now billing compiles alone, and the
mail code is the side that needs the other on its class path:

```text
$ javac -Xmaxerrs 1 -d ../out/policy billing/*.java
$ javac -Xmaxerrs 1 -d ../out/mail smtp/*.java
smtp/SmtpNotifier.java:3: error: package billing does not exist
import billing.RefundNotifier;
              ^
1 error
only showing the first 1 errors, of 2 total; use -Xmaxerrs if you would like to see more
$ javac -Xmaxerrs 1 -cp ../out/policy -d ../out/mail smtp/*.java
$
```

That reversal is the inversion: the detail depends on the policy's abstraction, and the
policy depends on nothing below it.

## A Constructor Parameter Turns a Forgotten Dependency Into a Compile Error

A service locator also keeps `new SmtpMailer()` out of the policy: a global map from type to
implementation, filled in at start-up.

```java
final class RefundPolicy {
    boolean refund(String customer, int daysSincePurchase) {
        if (daysSincePurchase > 30) return false;
        Services.get(RefundNotifier.class).refundApproved(customer);
        return true;
    }
}
```

The registration line in `main` was lost in a merge. The code compiles, the service starts, and
the smoke test of a late refund passes:

```text
started
day 45: false
Exception in thread "main" java.lang.IllegalStateException: no RefundNotifier registered
	at Services.get(Services.java:14)
	at RefundPolicy.refund(RefundPolicy.java:4)
	at Main.main(Main.java:7)
```

The exception comes at the first approved refund. Nothing in `RefundPolicy`'s constructor or method
signatures says it needs a notifier; only its body does. With the constructor version from the
first section, the same lost line is a build failure:

```text
Main.java:3: error: constructor RefundPolicy in class RefundPolicy cannot be applied to given types;
        RefundPolicy policy = new RefundPolicy();
                              ^
  required: RefundNotifier
  found:    no arguments
  reason: actual and formal argument lists differ in length
1 error
```

The wiring is plain code in `main`, `new RefundPolicy(new SmtpNotifier())`, and needs no
framework. A container such as Spring does the same wiring by reflection at start-up; there a
missing dependency fails when the container starts, before any request but not at compile time.
The compiler can't see a `null`
argument, which is what `requireNonNull` in the constructor is for. A constructor that replaces
`null` with `new SmtpNotifier()` as a default brings back the problem of the first section.

<!-- practice 1 -->

## A Fake Checks the Outcome; a Mock Checks the Calls

The policy now also refuses a second refund of the same order, through a `RefundLog` it owns:
its constructor takes `(notifier, log)` and `refund` takes the order id first. It called
`log.contains(orderId)`, then `log.add(orderId)`. Two requests for the same order could both
pass `contains` before either added (the check-then-act race of unit 6.2.1), so the two calls
became one, which is safe when `add` is atomic, as in a concurrent set. `contains` stays for the
support screen:

```java
/** Which orders have been refunded. add() returns false if the order was already there. */
public interface RefundLog {
    boolean contains(String orderId);

    boolean add(String orderId);
}
```

```java
        if (!log.add(orderId)) return false;           // one step: no window between check and add
```

Two tests guard the rule. One uses a Mockito mock and verifies the calls; the other uses a fake,
a working in-memory `RefundLog`, and checks what happened:

```java
    static void mockTest() {
        RefundLog log = mock(RefundLog.class);
        when(log.contains("B-77")).thenReturn(false);
        when(log.add("B-77")).thenReturn(true);
        RefundPolicy policy = new RefundPolicy(customer -> { }, log);

        policy.refund("B-77", "jane@example.com", 3);

        verify(log).contains("B-77");
        verify(log).add("B-77");
    }

    static void fakeTest() {
        Set<String> refunded = ConcurrentHashMap.newKeySet();
        RefundLog log = new RefundLog() {
            public boolean contains(String orderId) { return refunded.contains(orderId); }
            public boolean add(String orderId) { return refunded.add(orderId); }
        };
        List<String> told = new ArrayList<>();
        RefundPolicy policy = new RefundPolicy(told::add, log);

        boolean first = policy.refund("B-77", "jane@example.com", 3);
        boolean second = policy.refund("B-77", "jane@example.com", 4);

        if (!first || second || told.size() != 1)
            throw new AssertionError("first=" + first + " second=" + second + " told=" + told);
    }
```

Before and after the change:

```text
== v1
mock test: passed
fake test: passed
== v2
mock test: FAILED WantedButNotInvoked
    Wanted but not invoked:
    refundLog.contains("B-77");
    However, there was exactly 1 interaction with this mock:
    refundLog.add("B-77");
fake test: passed
```

The mock test pinned the old call sequence, so the race fix failed the build
although every refund decision stayed the same. The fake test only knows that the second refund
of B-77 is refused, which is the rule. A two-method fake is cheap because the policy owns a small
interface; a fake of `java.sql.Connection` would be a project of its own. When the call is the
outcome, as with the notifier, check the call: the `told` list records it.

## ServiceLoader Picks the Implementation From the Class Path

The composition root, the `main` that builds the objects and wires them, can choose the
implementation without naming it. `java.util.ServiceLoader` reads, from every jar or directory on
the class path, a file `META-INF/services/<interface name>` that lists
implementation classes, one per line. The mail jar holds its class and that file:

```text
-- jars/mail.jar
META-INF/MANIFEST.MF
smtp/SmtpNotifier.class
META-INF/services/billing.RefundNotifier
```

`META-INF/services/billing.RefundNotifier` contains the line `smtp.SmtpNotifier`. The
application asks for any `RefundNotifier`:

```java
ServiceLoader<RefundNotifier> loader = ServiceLoader.load(RefundNotifier.class);
// ...
            case "first" -> loader.findFirst()
                    .orElseThrow(() -> new IllegalStateException("no RefundNotifier on the class path"));
```

Run with no mail jar, with a mail jar built without the services file, and with the full one:

```text
$ java -cp jars/policy.jar:out/app Main first
Exception in thread "main" java.lang.IllegalStateException: no RefundNotifier on the class path
$ java -cp jars/policy.jar:jars/mail-no-services.jar:out/app Main first
Exception in thread "main" java.lang.IllegalStateException: no RefundNotifier on the class path
$ java -cp jars/policy.jar:jars/mail.jar:out/app Main first
using smtp.SmtpNotifier
```

In the second run `smtp.SmtpNotifier` is on the class path and still not found: on the class
path, `ServiceLoader` doesn't scan classes, it reads the file. A build that leaves resources out of
the jar ships an application that finds no notifier. Neither `billing` nor the application names
`smtp`.

### Iterating the Loader Builds Every Provider Before the One You Want

A second channel ships as `push.jar`. Its constructor needs a `PUSH_KEY` that this installation
doesn't have, and the configuration asks for `smtp.SmtpNotifier`. `Main` has three modes: `first`
calls `findFirst()`, `loop` compares each instance's class, and `stream` (the `default` branch)
compares each `ServiceLoader.Provider`'s type:

```java
            case "loop" -> {
                for (RefundNotifier candidate : loader) {
                    if (candidate.getClass().getName().equals(args[1])) yield candidate;
                }
                throw new IllegalStateException("no " + args[1]);
            }
            default -> loader.stream()
                    .filter(provider -> provider.type().getName().equals(args[1]))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("no " + args[1]))
                    .get();
```

```text
$ java -cp jars/policy.jar:jars/push.jar:jars/mail.jar:out/app Main first
Exception in thread "main" java.util.ServiceConfigurationError: billing.RefundNotifier: Provider push.PushNotifier could not be instantiated
Caused by: java.lang.IllegalStateException: PUSH_KEY is not set
$ java -cp jars/policy.jar:jars/push.jar:jars/mail.jar:out/app Main loop smtp.SmtpNotifier
Exception in thread "main" java.util.ServiceConfigurationError: billing.RefundNotifier: Provider push.PushNotifier could not be instantiated
Caused by: java.lang.IllegalStateException: PUSH_KEY is not set
$ java -cp jars/policy.jar:jars/push.jar:jars/mail.jar:out/app Main stream smtp.SmtpNotifier
using smtp.SmtpNotifier
$ java -cp jars/policy.jar:jars/mail.jar:jars/push.jar:out/app Main loop smtp.SmtpNotifier
using smtp.SmtpNotifier
```

The iterator constructs each provider as it reaches it, so the push channel that nobody asked for
failed the lookup. The last run is the loop again with the jars in the other order, and it works:
the result depended on class path order. `Provider.type()` returns the class without building it,
and only `get()` calls the constructor, so the stream builds just the chosen channel.

`ServiceLoader.load(type)` looks through the current thread's context class loader. Code that
keeps plug-ins in a class loader of its own passes it: `ServiceLoader.load(type, loader)`.

<!-- practice 2 -->

## Summary

- **A policy that calls `new` on its detail can't run without it.** Its tests need the mail
  server, and on a machine that has one they send real mail.
- **The policy owns the interface, in its own terms.** Injecting an interface that the detail
  publishes still leaves billing unable to compile without the mail jar.
- **A constructor parameter makes a forgotten dependency a compile error.** A service locator
  hides it until the first request that needs it.
- **A fake checks the outcome, a mock the calls.** A test that verifies calls can fail a
  refactor that changes no decision.
- **`ServiceLoader` finds implementations through `META-INF/services`.** Without the file the
  class is never found; iterating builds each provider it passes, and `Provider.type()` builds
  none.

## Reference

**`ServiceLoader` with providers on the class path (Java 21):**

| Situation | What happens |
|---|---|
| provider class in a jar without its `META-INF/services` file | not found |
| file lists a class that doesn't exist (`smtp.SmtpNotifer`) | `ServiceConfigurationError: ... Provider smtp.SmtpNotifer not found` |
| provider without a public no-argument constructor | `ServiceConfigurationError: ... Unable to get public no-arg constructor`, from `stream()` too |
| provider constructor throws | `ServiceConfigurationError: ... could not be instantiated`, from `findFirst()`, the iterator and `Provider.get()` |
| order of providers | the order in which the class loader finds the files, then the order of the lines in each |
| `#` in the file | starts a comment; blank lines and surrounding spaces are ignored |
| `ServiceLoader.load(type)` | uses `Thread.currentThread().getContextClassLoader()` |
| one `ServiceLoader` used from several threads | not safe, per its javadoc |

## Challenges

1. The billing team wants to ship `RefundNotifier` in a jar of its own so that two policies can
   share it. Who owns that jar, and what happens to it when one policy needs a new method?
2. A module's test suite uses Mockito for every collaborator. Pick one interface it mocks and
   write the fake. Which tests get shorter, and which can no longer be written?
3. `ServiceLoader` finds two `RefundNotifier`s in production. Design how the composition root
   chooses, and what it does when the configured one is missing.

## Design Note: ServiceLoader Is a Service Locator

`ServiceLoader.load(RefundNotifier.class)` is a global lookup by type, the thing the third section
warned about. It is safe in one place: the composition root, the `main` that builds the objects
and passes each one its dependencies through constructors. There, a missing provider fails at
start-up, before any request, and the policies still declare everything they need in their
constructors. Called from inside a policy, it would hide the dependency again, and a test would
need a services file on its class path to run the rule. The JDK finds JDBC drivers and
`java.nio.file` file-system providers through `ServiceLoader`, and `DriverManager` shows the cost
of leaving the lookup open: it is a global registry that any code can call with a URL, and a
missing driver shows up as `SQLException: No suitable driver found for <url>` at that call.

## Code Examples

- Source: [DependencyInversion.java](src/main/java/com/github/msorkhpar/claudejavatutor/solidprinciples/DependencyInversion.java)
- Test: [DependencyInversionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/solidprinciples/DependencyInversionTest.java)
