# 9.1.3. Builder Pattern

A customer is refunded 45.00 EUR for order 7781. The next morning reconciliation finds the shop's
account 45.00 richer and the customer's 45.00 poorer. The refund code compiled, its tests passed,
and every field of the transfer it created held a valid value. Two arguments were in the wrong
order.

A builder is for objects like that one: many parts, several of the same type, some optional. Every
output below is from Java 21.

## Same-Typed Constructor Arguments Swap Without an Error; a Builder Names Each One

The transfer class grew one constructor per optional part, the telescoping style:

```java
/** Money moved between two accounts, with constructors for each optional part. */
public final class Transfer {
    private final String fromAccount;
    private final String toAccount;
    private final long cents;
    private final String currency;
    private final String memo;

    public Transfer(String fromAccount, String toAccount, long cents) {
        this(fromAccount, toAccount, cents, "EUR");
    }

    public Transfer(String fromAccount, String toAccount, long cents, String currency) {
        this(fromAccount, toAccount, cents, currency, "");
    }

    public Transfer(String fromAccount, String toAccount, long cents, String currency, String memo) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.cents = cents;
        this.currency = currency;
        this.memo = memo;
    }

    public String fromAccount() { return fromAccount; }
    public String toAccount() { return toAccount; }
    public long cents() { return cents; }

    @Override public String toString() {
        return "Transfer[from=" + fromAccount + ", to=" + toAccount + ", cents=" + cents
                + ", currency=" + currency + ", memo=" + memo + "]";
    }
}
```

Before reading the output, decide which balance goes up:

```java
Map<String, Long> balances = new TreeMap<>(Map.of("ACC-1001 (shop)", 50_000L, "ACC-2002 (customer)", 0L));
String shop = "ACC-1001 (shop)", customer = "ACC-2002 (customer)";

// refund order 7781: the shop pays the customer back 45.00 EUR
Transfer refund = new Transfer(customer, shop, 4_500, "EUR", "refund order 7781");

balances.merge(refund.fromAccount(), -refund.cents(), Long::sum);
balances.merge(refund.toAccount(), refund.cents(), Long::sum);
```

```text
Transfer[from=ACC-2002 (customer), to=ACC-1001 (shop), cents=4500, currency=EUR, memo=refund order 7781]
{ACC-1001 (shop)=54500, ACC-2002 (customer)=-4500}
```

`customer` and `shop` are both `String`, so the swapped call matches the constructor exactly. The
compiler checks types. A test that builds a transfer from its own literals and
reads them back passes in either order. Nothing fails until the money is in the wrong account.

The telescope has a second cost. A memo without a currency would need
`Transfer(String, String, long, String memo)`, and that signature is taken:

```text
clash/Transfer.java:25: error: constructor Transfer(String,String,long,String) is already defined in class Transfer
```

So a caller who wants a memo passes every value before it, which is how a five-argument call with
adjacent `String`s comes about. Here is the same class with a builder: a private constructor that
takes the builder, and one method per part.

```java
/** Money moved between two accounts; built by naming each part. */
public final class Transfer {
    private final String fromAccount;
    private final String toAccount;
    private final long cents;
    private final String currency;
    private final String memo;

    private Transfer(Builder b) {
        this.fromAccount = b.fromAccount;
        this.toAccount = b.toAccount;
        this.cents = b.cents;
        this.currency = b.currency;
        this.memo = b.memo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String fromAccount() { return fromAccount; }
    public String toAccount() { return toAccount; }
    public long cents() { return cents; }

    @Override public String toString() {
        return "Transfer[from=" + fromAccount + ", to=" + toAccount + ", cents=" + cents
                + ", currency=" + currency + ", memo=" + memo + "]";
    }

    public static final class Builder {
        private String fromAccount;
        private String toAccount;
        private long cents;
        private String currency = "EUR";
        private String memo = "";

        private Builder() {}

        public Builder from(String account) { this.fromAccount = account; return this; }
        public Builder to(String account) { this.toAccount = account; return this; }
        public Builder cents(long cents) { this.cents = cents; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder memo(String memo) { this.memo = memo; return this; }

        public Transfer build() {
            return new Transfer(this);
        }
    }
}
```

```java
Transfer refund = Transfer.builder()
        .from(shop)
        .to(customer)
        .cents(4_500)
        .memo("refund order 7781")
        .build();
```

```text
Transfer[from=ACC-1001 (shop), to=ACC-2002 (customer), cents=4500, currency=EUR, memo=refund order 7781]
{ACC-1001 (shop)=45500, ACC-2002 (customer)=4500}
```

The caller set a memo without passing a currency, which took its default. The builder doesn't make the
compiler reject `.from(customer)`; it puts the word `from` next to the value, where a reviewer
reads it. Only distinct types, such as a `PayerAccount` and a `PayeeAccount`, turn the swap into a
compile error. A record's canonical constructor is positional too (unit 2.4.3 shows the same
swap), and a static factory (unit 9.1.2) names one way to create an object, not each part.

## A Builder Checks the Whole Object in build(), Where Every Part Is Set

A transfer to its own account is a mistake, and someone adds the check to the `to()` setter so it
fails as early as possible:

```java
public Builder to(String account) {
    if (account.equals(fromAccount)) {
        throw new IllegalArgumentException("transfer to its own account " + account);
    }
    this.toAccount = account;
    return this;
}
```

Three callers: two orders, and one forgotten part. `attempt` prints the transfer, or the
exception.

```java
attempt("from, then to: ", () -> Transfer.builder().from("ACC-1001").to("ACC-1001").cents(4_500).build());
attempt("to, then from: ", () -> Transfer.builder().to("ACC-1001").from("ACC-1001").cents(4_500).build());
attempt("no to:         ", () -> Transfer.builder().from("ACC-1001").cents(4_500).build());
```

```text
from, then to: java.lang.IllegalArgumentException: transfer to its own account ACC-1001
to, then from: Transfer[from=ACC-1001, to=ACC-1001, cents=4500, currency=EUR, memo=]
no to:         Transfer[from=ACC-1001, to=null, cents=4500, currency=EUR, memo=]
```

When `to()` runs first, `fromAccount` is still `null`, and the check compares against nothing. A setter sees one part. A rule that relates two parts, or requires one, belongs in `build()`, the
call that says the caller is done. The same checks, moved there:

```java
public Transfer build() {
    if (fromAccount == null || toAccount == null) {
        throw new IllegalStateException("from and to are required");
    }
    if (fromAccount.equals(toAccount)) {
        throw new IllegalStateException("transfer to its own account " + toAccount);
    }
    if (cents <= 0) {
        throw new IllegalStateException("cents must be positive: " + cents);
    }
    return new Transfer(this);
}
```

```text
from, then to: java.lang.IllegalStateException: transfer to its own account ACC-1001
to, then from: java.lang.IllegalStateException: transfer to its own account ACC-1001
no to:         java.lang.IllegalStateException: from and to are required
```

The product's constructor is private and only `build()` calls it, so no path to a `Transfer` skips
the checks. A setter may still reject a value that is wrong on its own, such as a negative amount,
at once.

For a type with three required parts, a builder is more code than it saves. A record's compact
constructor runs its checks on every path to an instance, including a `with` method that calls
`new` (unit 2.4.1):

```java
import java.util.Objects;

/** A fee charged to one account: three parts, all required. */
public record Fee(String account, long cents, String currency) {
    public Fee {
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(currency, "currency");
        if (cents <= 0) throw new IllegalArgumentException("cents must be positive: " + cents);
    }

    public Fee withCents(long newCents) {
        return new Fee(account, newCents, currency);
    }
}
```

With `monthly = new Fee("ACC-1001", 990, "EUR")`, `monthly.withCents(1_490)`, then
`monthly.withCents(0)`:

```text
Fee[account=ACC-1001, cents=1490, currency=EUR]
java.lang.IllegalArgumentException: cents must be positive: 0
```

<!-- practice 1 -->

## A Builder Used Again After build() Changes What It Built, Unless the Product Copies

Transfers now carry tags, which reconciliation matches on. The monthly payroll run sets the payer
and a batch tag once, then reuses the builder for each employee. The builder collects tags in a list,
and `Transfer` gets a `tags()` accessor:

```java
private final List<String> tags = new ArrayList<>();      // in Transfer.Builder

public Builder tag(String tag) { this.tags.add(tag); return this; }
```

```java
Transfer.Builder payroll = Transfer.builder().from("ACC-1001").tag("payroll-2026-10");

Transfer first = payroll.to("ACC-3017").cents(310_000).tag("E-17").build();
System.out.println("first, when built:     " + first.tags());

Transfer second = payroll.to("ACC-3018").cents(295_000).tag("E-18").build();
System.out.println("first, after second:   " + first.tags());
System.out.println("second:                " + second.tags());
```

We ran it three times, each with another version of the line in `Transfer`'s constructor that
keeps the tags, labelled in the output:

```java
this.tags = b.tags;                                   // shared
this.tags = Collections.unmodifiableList(b.tags);     // view
this.tags = List.copyOf(b.tags);                      // copy
```

```text
shared:
first, when built:     [payroll-2026-10, E-17]
first, after second:   [payroll-2026-10, E-17, E-18]
second:                [payroll-2026-10, E-17, E-18]
view:
first, when built:     [payroll-2026-10, E-17]
first, after second:   [payroll-2026-10, E-17, E-18]
second:                [payroll-2026-10, E-17, E-18]
copy:
first, when built:     [payroll-2026-10, E-17]
first, after second:   [payroll-2026-10, E-17]
second:                [payroll-2026-10, E-17, E-18]
```

With the first two, the transfer to employee 17, built and perhaps already sent, changed when the
builder was used for employee 18, and reconciliation now matches it to both. The builder's list keeps
changing while the builder is used, and a product that holds a reference to it changes with it.
`Collections.unmodifiableList` stops the product's readers from writing, but it is a view of the
same list, so the builder's writes still show through. `List.copyOf` takes an unmodifiable copy
of the builder's `ArrayList`. Unit 2.4.2 shows the same leak through a record's list
component.

The copy fixes the first transfer, and the second still carries `E-17`, because the builder keeps
every tag it was given. To use a builder as a template, give it a `copy()` method and build each
product from a copy, or create a new builder per product. A builder kept in a static field, a
singleton in the sense of unit 9.1.1, is reused by every thread that calls it.

<!-- practice 2 -->

## HttpRequest.Builder Copies on build(), but a Reused Builder Keeps Adding Headers

`java.net.http.HttpRequest` is created through `HttpRequest.newBuilder()`, and building a request
sends nothing, so it runs without a network. A payout client reuses one builder for two payouts,
each with its own idempotency key:

```java
HttpRequest.Builder payouts = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com/v1/payouts"))
        .timeout(Duration.ofSeconds(5))
        .header("Accept", "application/json");

HttpRequest first = payouts.header("Idempotency-Key", "po-17")
        .POST(BodyPublishers.ofString("{\"cents\":310000}")).build();
HttpRequest second = payouts.header("Idempotency-Key", "po-18")
        .POST(BodyPublishers.ofString("{\"cents\":295000}")).build();
```

The same two payouts, each built from `copy()` of `template`, a builder set up exactly like
`payouts`:

```java
HttpRequest a = template.copy().header("Idempotency-Key", "po-17")
        .POST(BodyPublishers.ofString("{\"cents\":310000}")).build();
HttpRequest b = template.copy().header("Idempotency-Key", "po-18")
        .POST(BodyPublishers.ofString("{\"cents\":295000}")).build();
```

The keys of each pair, then the method, URI, timeout and body length of request `b`:

```text
reused:  first [po-17], second [po-17, po-18]
copy():  first [po-17], second [po-18]
POST https://api.example.com/v1/payouts PT5S 16 bytes
```

The first request still has one key after the second was built: `build()` copies the builder's
state into an immutable request, the fix of the previous section. The second request has two,
because `header()` adds a value, and which one the payout API reads is up to that API.
`setHeader()` replaces the value, but it still changes the shared builder. `copy()`
returns a new builder with the template's state, and nothing done to the copy reaches the template.

The JDK's builder checks a part in its setter when the part alone can be wrong, and leaves only the
missing URI to `build()`. Each of these four ran on its own, and only the first calls `build()`:

```java
() -> HttpRequest.newBuilder().header("Accept", "application/json").build(),
() -> HttpRequest.newBuilder().uri(URI.create("ftp://files.example.com/payouts.csv")),
() -> HttpRequest.newBuilder().header("Content-Length", "10"),
() -> HttpRequest.newBuilder().timeout(Duration.ZERO)
```

```text
java.lang.IllegalStateException: uri is null
java.lang.IllegalArgumentException: invalid URI scheme ftp
java.lang.IllegalArgumentException: restricted header name: "Content-Length"
java.lang.IllegalArgumentException: Invalid duration: PT0S
```

`StringBuilder` is a growing character buffer with nothing to validate, despite the name
(unit 1.4.5).

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **Same-typed constructor arguments swap without an error.** A builder names each part at the
  call site and lets optional parts take defaults; only distinct types make a swap fail to compile.
- **A builder checks the whole object in `build()`.** A setter that checks one part against another
  depends on call order, and a missing part gets through as `null`. For a small type, a record's
  compact constructor does the same job.
- **A builder used again after `build()` changes what it built, unless the product copies.** A
  read-only view still shows the builder's writes; `List.copyOf` doesn't. Reuse also carries every
  earlier part into the next product.
- **`HttpRequest.Builder` copies on `build()`, and `header()` adds.** Start each request from
  `copy()` of a template.

## Reference

**`HttpRequest.Builder` (Java 21):**

| Call | Rejects, and when |
|---|---|
| `uri(URI)` | a scheme other than `http` or `https`, or no host, at the call (`IllegalArgumentException`) |
| `header`, `setHeader` | a restricted name such as `Content-Length`, an invalid name or value, at the call (`IllegalArgumentException`) |
| `timeout(Duration)` | zero or negative, at the call (`IllegalArgumentException`) |
| `build()` | a builder with no URI (`IllegalStateException`) |
| `copy()` | nothing; returns a new builder with the same state |

`header(name, value)` adds a value to the name; `setHeader(name, value)` replaces its values.

**Keeping a builder's list in the product:**

| Code | Builder's later writes show | Product's readers can write |
|---|---|---|
| `this.tags = b.tags` | yes | yes |
| `Collections.unmodifiableList(b.tags)` | yes | no |
| `new ArrayList<>(b.tags)` | no | yes, if the getter returns it |
| `List.copyOf(b.tags)` | no | no; it also rejects `null` elements |

## Challenges

1. Your `Order` has nine parts, four of them required. Compare a builder that takes the required
   ones in its constructor with one that checks them in `build()`. Which mistakes does each catch,
   and when?
2. Sketch a builder whose `build()` can only be called after `from()` and `to()`, so the compiler
   rejects a missing part. What does each added required part cost?
3. Find a builder in your codebase that is reused in a loop. Write the test that fails if the
   second product carries a part of the first.

## Design Note: Why Java Has Builders and Not Named Arguments

Languages with named and default arguments let the caller name each argument and leave out the
optional ones, which covers most of what builders and telescoping constructors are for. Java has neither, so the builder supplies both, at the price of a second
class that mirrors the product's fields and must be kept in step with it: add a field to `Transfer`
and forget the builder method, and nothing can set it. The GoF form adds a director that drives one
sequence of steps over different builders to produce different representations. It is rare in Java code.

## Code Examples

- Source: [BuilderPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/BuilderPattern.java)
- Test: [BuilderPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/BuilderPatternTest.java)
