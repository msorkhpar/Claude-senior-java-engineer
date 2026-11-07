# 1.5.3 Record Patterns in Java (Java 19+)

A payments service replaced its getter chains with record patterns. Since the deploy, payments
from some payers are rejected:

```text
Exception in thread "main" java.lang.IllegalArgumentException: not a payment: Payment[payer=Payer[id=p-17, address=null], cents=5000]
```

The message says the value is not a payment, then prints one. Nothing failed inside the pattern;
it didn't match.

Record patterns were previews in Java 19 and 20 and are final in Java 21; every output here is
from Java 21. From records we need one fact: each component has an accessor
method, `iban()` for `iban` (unit 2.4.1). Type patterns and binding variables are unit 1.5.2;
`switch`, guards and exhaustiveness are unit 1.5.4.

## A Record Pattern Tests the Type, Then Binds Each Component by Position

Two accounts and a transfer between them:

```java
record Account(String iban, String currency) {}
record Transfer(Account from, Account to, long cents) {}
```

```java
Object event = new Transfer(new Account("DE-ACC-1", "EUR"), new Account("GB-ACC-2", "GBP"), 12_500);

if (event instanceof Transfer(Account(var iban, var currency), Account to, long cents)) {
    System.out.println("debit " + iban + " " + cents + " " + currency + ", credit " + to.iban());
}
if (event instanceof Transfer(Account to, Account from, long cents)) {
    System.out.println("debit " + from.iban() + ", credit " + to.iban());
}
```

```text
debit DE-ACC-1 12500 EUR, credit GB-ACC-2
debit GB-ACC-2, credit DE-ACC-1
```

The first pattern tests `event instanceof Transfer`, calls `from()`, `to()` and `cents()`, and
matches each result against the pattern in the same position. `Account(var iban, var currency)`
repeats the test one level down, so one pattern takes apart a record and the records inside it.
`var` gives a binding its component's type: `iban` is a `String`.

The second pattern reads as well as the first and debits the wrong account. Components bind by
position: the first pattern in the list gets `from()`, whatever you name it. The compiler checks
the count (`incorrect number of nested patterns`), that each pattern fits its component's type,
and that the type is a record. A class with the same accessors gets `deconstruction patterns can
only be applied to records` (javac's name for record patterns). Here both components are `Account`s, so
it had nothing to object to. Where a record has two components of one type (from and to, billing
and shipping, min and max), copy the order from the record's declaration and name each binding
after its component.

> **Where Java 21 allows them.** A record pattern follows `instanceof` or is a `case` label
> (unit 1.5.4). It can't declare variables (`Point(int x, int y) = p;` doesn't compile), and
> `for (Point(var x, var y) : points)`, a Java 20 preview, was dropped from Java 21. The unnamed
> pattern `_`, for a component you don't need, is a preview in Java 21 and final in Java 22; in
> 21 without `--enable-preview` every component gets a name.

## A Record Pattern Never Matches null, at Any Depth; var and the Component's Declared Type Do

The payment service's records, and its method after the refactor:

```java
record Address(String country, String postcode) {}
record Payer(String id, Address address) {}          // address is null when none is on file
record Payment(Payer payer, long cents) {}
```

```java
static String book(Object event) {
    if (event instanceof Payment(Payer(var id, Address(var country, var postcode)), long cents)) {
        return "booked " + id + " " + country + " " + cents;
    }
    throw new IllegalArgumentException("not a payment: " + event);
}
```

Payer `p-17` has no address on file. Before reading the output, decide which of these four tests
are `true`:

```java
Object event = new Payment(new Payer("p-17", null), 5_000);
System.out.println("Payer(var id, Address(var c, var pc)): "
        + (event instanceof Payment(Payer(var id, Address(var c, var pc)), var cents)));
System.out.println("Payer(var id, var address):            "
        + (event instanceof Payment(Payer(var id, var address), var cents)));
System.out.println("Payer(var id, Address address):        "
        + (event instanceof Payment(Payer(var id, Address address), var cents)));
Object none = null;
System.out.println("null, Payment(var payer, var cents):   "
        + (none instanceof Payment(var payer, var cents)));
```

```text
Payer(var id, Address(var c, var pc)): false
Payer(var id, var address):            true
Payer(var id, Address address):        true
null, Payment(var payer, var cents):   false
```

A record pattern starts with an `instanceof` test, and `null` is an instance of nothing (unit
1.5.1). That holds at every level. The address is `null`, so `Address(var c, var pc)` fails, the
whole pattern fails, and `book` calls a payment "not a payment". Its author read the
nested pattern as "take the address apart if there is one". It means "a payment whose payer has
an address".

A component pattern that tests nothing matches `null` and binds it: `var address`, or a type
pattern on the component's declared type or a supertype, such as `Address address`. So a match
doesn't promise non-null bindings: after `Payer(var id, var address)` matched, a call to
`address.country()` threw `NullPointerException`.

Choose per component: nest a record pattern only where a `null` component means "not this case".
Where `null` is a legal value, bind it with `var` and check it, or give it a branch of its own.
`book` needs the country only as a label, so `var address` and a null check are enough.

<!-- practice 1 -->

## A Narrower Type on a Component Is a Test, So a Payload Whose Type Changes Stops Matching

```java
record Event(String type, Object payload) {}
record Envelope<T>(String id, T body) {}
```

A producer moves its event payload from JSON text to UTF-8 bytes:

```java
Object before = new Event("order.created", "{\"id\":42}");
Object after = new Event("order.created", "{\"id\":42}".getBytes(StandardCharsets.UTF_8));
System.out.println("String payload: " + (before instanceof Event(var type, String json)));
System.out.println("byte[] payload: " + (after instanceof Event(var type, String json)));
```

```text
String payload: true
byte[] payload: false
```

`payload` is declared `Object`, so `String json` asks a question: is this component's value a
`String`? It is not a cast, and a `byte[]` makes the pattern fail without a `ClassCastException`.
A consumer that dispatches on `Event(var type, String json)` stops handling the producer's events
and reports nothing. Wherever a chain of record patterns ends, in an `else` or a `default`, make
the leftover case throw or count, not return quietly.

The same test reaches inside generic records. Type arguments are erased (unit 1.5.1), but the
components hold real objects:

```java
Object env = new Envelope<>("m-1", 42);
System.out.println("Envelope(var id, String body):  " + (env instanceof Envelope(var id, String body)));
System.out.println("Envelope(var id, Integer body): " + (env instanceof Envelope(var id, Integer body)));
Envelope<String> typed = new Envelope<>("m-2", "hello");
if (typed instanceof Envelope(var id, var body)) {
    System.out.println("inferred: body.length() = " + body.length());   // body is a String
}
```

```text
Envelope(var id, String body):  false
Envelope(var id, Integer body): true
inferred: body.length() = 5
```

Leave the type arguments out of the pattern: the compiler infers them, so for an
`Envelope<String>`, `body` is a `String`. Against an `Object`,
`Envelope<String>(var id, var body)` is refused (`Object cannot be safely cast to
Envelope<String>`), since nothing at run time could check the `String`.

Primitive components get no such test in Java 21. A pattern on an `int` component is `int` or
`var`: `long x` and `Integer x` are compile errors.

> **Later versions.** javac 25 accepts `long x` on an `int` component with `--enable-preview`;
> without it, it reports `primitive patterns are a preview feature`.

<!-- practice 3 -->

## Matching Calls the Accessors, and an Exception From One Arrives as MatchException

A record can override an accessor. This one masks the card number:

```java
record CardToken(String pan) {
    @Override
    public String pan() {
        return "**** " + pan.substring(pan.length() - 4);
    }
}
```

```java
Object ok = new CardToken("4111111111111111");
if (ok instanceof CardToken(var pan)) System.out.println("bound: " + pan);

Object shortPan = new CardToken("42");                 // bad data from an old import
try {
    if (shortPan instanceof CardToken(var pan)) System.out.println("bound: " + pan);
} catch (StringIndexOutOfBoundsException e) {
    System.out.println("caught " + e);
}
```

```text
bound: **** 1111
Exception in thread "main" java.lang.MatchException: java.lang.StringIndexOutOfBoundsException: Range [-2, 2) out of bounds for length 2
```

The binding is what `pan()` returned, not the field. A record pattern gets each component through
its accessor, the record's public API, so it sees the same masked, copied or validated value as
any other caller. A `var` component is read the same way.

When an accessor throws during a match, the match throws `MatchException` with the original
exception as its cause (see its javadoc), and no later `case` or `default` is tried. The
`catch` for `StringIndexOutOfBoundsException` never ran, and in a batch job one bad record from
an old import ends the batch. Catch `MatchException` where one record may fail without the rest,
and decide by `getCause()`.

How often an accessor runs is the compiler's choice. javac reads components left to right and
stops at the first that fails; in a `switch` it read each accessor once across three cases, and
sometimes again after a failed `when` guard (unit 1.5.4; Reference). Keep accessors free of side effects.

<!-- practice 2 -->

## Summary

- **A record pattern tests the type, then binds each component by position.** Names are free,
  so two components of one type can be bound the wrong way round without a compile error.
- **A record pattern never matches `null`, at any depth.** A nested pattern on a component that
  may be `null` rejects the whole value; `var` or the declared type binds the `null` instead.
- **A narrower type on a component is a test, not a cast.** A payload whose type changes falls
  through to the leftover branch, so that branch must not be silent.
- **Matching calls the accessors.** It sees what they return, and their exceptions arrive as
  `MatchException` with the original as the cause.

## Reference

**What a component pattern matches (Java 21):**

| Component pattern | Value is `null` | Value not of the pattern's type |
|---|---|---|
| `var x` | matches, `x` is `null` | (no test) matches |
| `T x`, `T` the component's declared type or a supertype | matches, `x` is `null` | (no test) matches |
| `S x`, `S` narrower than the declared type | doesn't match | doesn't match, no exception |
| a record pattern `R(...)` | doesn't match | doesn't match |

**Refused by javac 21:**

| Pattern | Error |
|---|---|
| fewer components than the record | `incorrect number of nested patterns` |
| a class that is not a record | `deconstruction patterns can only be applied to records` |
| `long x` or `Integer x` on an `int` component | `pattern of type long is not applicable at int` (`Integer` for the second) |
| `Envelope<String>(var id, var body)` on an `Object` | `Object cannot be safely cast to Envelope<String>` |
| `Point(int x, _)` | `unnamed variables are a preview feature and are disabled by default` (javac 25 with `--release 22` accepts it) |
| `for (Point(int x, int y) : points)` | a syntax error (`'.class' expected`) |

**Accessor calls (javac 21; javac 25 with `--release 21` the same), for `Pair(Object left, Object right)` holding `(42, "x")`:**

| Code | Accessors called |
|---|---|
| `p instanceof Pair(String l, var r)` | `left()` only: the first component failed |
| a `switch` with cases `Pair(String l, String r)`, `Pair(Integer l, Integer r)`, `Pair(Integer l, String r)` | `left()` once, `right()` once |
| `case Pair(var l, var r) when l == null`, then `case Pair(var l, var r)` | `left()` and `right()` twice each |
| the same with `Pair(Integer l, String r)` and a false guard `l > 100` | `left()` once, `right()` once |
| any accessor throwing during a match | `MatchException`, cause = the accessor's exception |

## Challenges

1. A consumer matches events with a chain of record patterns, and some event kinds are ignored
   on purpose. How do you make a value that nothing matched visible, without paging anyone for the
   ones you mean to ignore?
2. A record holds a mutable list of 10,000 items, and its accessor returns `List.copyOf(items)`.
   A hot loop matches it with three guarded `case`s on every iteration. Count the copies, measure
   what they cost, and decide what you would change: the accessor, the record or the switch.
3. Clients of your library destructure its records with record patterns. You add a component to
   one of them. What fails in the clients' sources, and what happens to their already compiled
   classes? Try it.

## Design Note: Why a Pattern Calls the Accessors

A record pattern could have read the fields. Java has it call the accessors, so a pattern can't
see more of a record than its public API shows. The pattern is the inverse of the canonical
constructor: one builds the record from its components, the other gets them back.

The price is that matching runs code. An accessor can be slow, have side effects, or throw, and
the language needs a way to report a throw from inside a test that otherwise only answers yes or
no. It reports it as `MatchException`, a type that few `catch` blocks name.

## Code Examples

- Test: [RecordPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/patternmatching/RecordPatternTest.java)
- Source: [RecordPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/patternmatching/RecordPattern.java)
