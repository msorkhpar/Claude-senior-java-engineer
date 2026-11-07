# 1.1.2 Reference Data Types

The order service shipped an "amend order" screen, and three tickets came in the same week.
Customers saw a placed order change while support was still editing the draft. The duplicate-order
check, green in every unit test, let a retried API call through, and the customer paid twice. And
the heap after each full collection grew daily until the pod died with `OutOfMemoryError`.

All three come from what a variable of a reference type holds. Classes, interfaces, arrays, enums
and records are reference types, and a variable of one holds a reference to an object, or `null`,
never the object itself.

## Assignment Copies the Reference, Not the Object

In the examples an `Order` holds an id and a mutable `List<Line>`, which `lines()` returns as it
is, and a `Line` holds a SKU and a quantity that `setQty` changes. Support opens a placed order and adds a juice to the draft. What
does the placed order print?

```java
Order placed = new Order("A-1001", new ArrayList<>(List.of(new Line("espresso", 2), new Line("croissant", 1))));
Order draft = placed;
draft.lines().add(new Line("juice", 1));
System.out.println("placed: " + placed);
System.out.println("draft == placed: " + (draft == placed));
```

```text
placed: A-1001 [2x espresso, 1x croissant, 1x juice]
draft == placed: true
```

`new` created one `Order`. `draft = placed` copied the reference into a second variable, so both
names reach the same object. The juice is on the customer's order before anyone presses Save.

Passing an argument works the same way: the method receives a copy of the reference, so it can
change the caller's object but can't make the caller's variable point elsewhere (unit 1.3.3). A
report helper that sorts the list it was given reorders the caller's list.

## A Copy Is Only as Deep as the Code That Made It

The fix that went out gave the draft a list of its own:

```java
Order draft = new Order(placed.id(), new ArrayList<>(placed.lines()));
draft.lines().add(new Line("juice", 1));
draft.lines().get(0).setQty(5);
```

Before reading on: which of the two changes reaches the placed order?

```text
placed: A-1001 [5x espresso, 1x croissant]
draft:  A-1001 [5x espresso, 1x croissant, 1x juice]
same list: false, same first line: true
```

`new ArrayList<>(...)` made a new list and filled it with the references the old list held. The
juice went into the new list only. The quantity change went into a `Line` that both lists refer
to, so the customer is charged for five espressos while the amendment screen shows the juice as
the only change.

That is a shallow copy: a new container that shares everything below it. `List.copyOf` is no
deeper. On the same lines:

```text
same first line: true
add: java.lang.UnsupportedOperationException
```

It returns an unmodifiable list of the same elements: nobody can add a line to it, and anyone can
still call `setQty` on a line in it. A copy that must be independent copies every mutable level:

```java
List<Line> copied = new ArrayList<>();
for (Line l : placed.lines()) copied.add(new Line(l.sku(), l.qty()));
Order draft = new Order(placed.id(), copied);
draft.lines().get(0).setQty(5);
```

```text
placed: A-1001 [2x espresso, 1x croissant]
draft:  A-1001 [5x espresso, 1x croissant]
```

Deep copies go wrong at the level someone forgot: a `Line` with a mutable list of notes needs that
list copied too, or the two lines share it. The other way out is to make the lower
levels immutable: a `Line` that no one can change (a record of immutable values, unit 2.4.1) can
be shared by any number of lists. An array's `clone()` and `Arrays.copyOf` are shallow as well.

<!-- practice 1 -->

## == Compares References; equals Compares What the Class Defines

The duplicate check compared each incoming order id with the ids it had seen, using `==`. Its
tests used string literals:

```java
String typed = "ORD-7";
String parsed = "id=ORD-7".split("=")[1];
int n = 7;
String built = "ORD-" + n;
System.out.println("typed == \"ORD-7\":   " + (typed == "ORD-7"));
System.out.println("typed == \"ORD-\" + \"7\": " + (typed == "ORD-" + "7"));
System.out.println("typed == parsed:    " + (typed == parsed));
System.out.println("typed == built:     " + (typed == built));
System.out.println("typed.equals(parsed): " + typed.equals(parsed));
```

```text
typed == "ORD-7":   true
typed == "ORD-" + "7": true
typed == parsed:    false
typed == built:     false
typed.equals(parsed): true
```

On references, `==` is true only when both point to the same object. The JVM keeps one `String`
object for each distinct literal, and the compiler turns `"ORD-" + "7"` into the literal
`"ORD-7"`, so every `"ORD-7"` in the program is one object and the tests pass. `"ORD-" + n` is computed
at run time, because `n` is a variable, not a constant. An id that arrives in a request is parsed
at run time into a new `String` with the same characters. `==` says `false`, the check sees no
duplicate, and the retried request charges again. `equals` compares the characters.

A class that doesn't override `equals` inherits `Object.equals`, which is `==`. Two ids with the
same field:

```java
final class OrderId {
    private final String value;
    OrderId(String value) { this.value = value; }
}

record OrderKey(String value) { }
```

Each is added to a `HashSet`, which is then asked for a new object with the same value, and
`equals` is called on two new objects:

```java
Set<OrderId> seen = new HashSet<>();
seen.add(new OrderId("ORD-7"));
System.out.println("class:  " + seen.contains(new OrderId("ORD-7"))
        + ", equals: " + new OrderId("ORD-7").equals(new OrderId("ORD-7")));
```

```text
class:  false, equals: false
record: true, equals: true
```

A set of `OrderId`s never finds an equal id that arrives as a new object. A record's generated
`equals` and `hashCode` use its components (unit 2.4.1). Unit 1.1.1 showed the same trap with `Integer`, where `==` works only
while the values are in the cache.

## A Reference Field or Array Element Starts as null, and the NPE Comes Where It Is Used

Fields and array elements of a reference type start as `null`: `new Customer[3]` creates an array
of three `null`s and no `Customer`. A local variable gets no default, and reading one before it is
assigned doesn't compile (javac: `variable owner might not have been initialized`).

A `Customer` gets its name in the constructor, and the signup flow sets its `email` afterwards.
One path through signup forgot. Hours later the nightly mail job reaches that customer:

```java
Customer c = new Customer("Jane Doe");
System.out.println(c.email.toLowerCase());

Customer[] batch = new Customer[3];
System.out.println(batch[1].name);
```

```text
Cannot invoke "String.toLowerCase()" because "c.email" is null
Cannot read field "name" because "batch[1]" is null
```

The `NullPointerException`'s message names the `null` expression, but the trace is the mail
job's, and nothing in it leads back to the signup request that made the mistake. A check where
the reference enters the object moves the failure to the mistake:

```java
CheckedCustomer(String name, String email) {
    this.name = Objects.requireNonNull(name, "name");
    this.email = Objects.requireNonNull(email, "email");
}
```

```text
java.lang.NullPointerException: email
	at java.base/java.util.Objects.requireNonNull(Objects.java:259)
	at Nulls$CheckedCustomer.<init>(Nulls.java:17)
	at Nulls.main(Nulls.java:37)
```

Below `requireNonNull`, the trace names the constructor and the code that called it with the
`null`: in the service, the signup path.

> **Local names need debug information.** The messages above name `c` and `batch` because the
> class was compiled with `javac -g`. Without it the same lines say `"<local1>.email"` and
> `"<local2>[1]"`. Field names appear either way.

<!-- practice 2 -->

## An Object Lives While Something Reachable Refers to It

The collector doesn't count references: it starts from the GC roots (the local variables of
running threads, static fields) and keeps every object it can reach. The rest is garbage, whatever
refers to it. Two cycles, watched through weak references (a
`WeakReference` doesn't keep its object alive, and its `get()` returns `null` once the object is
collected):

```java
Node a = new Node("a"), b = new Node("b");
a.next = b; b.next = a;
WeakReference<Node> cycle = new WeakReference<>(a);

Node c = new Node("c"), d = new Node("d");
c.next = d; d.next = c;
REGISTRY.add(c);                                   // a static List<Node>
WeakReference<Node> registered = new WeakReference<>(c);

a = null; b = null; c = null; d = null;
System.gc();
```

```text
unreachable cycle collected:  true
registered cycle collected:   false
```

Each node of the first cycle is still referred to by the other, and the cycle was collected,
because nothing reachable leads to it. The second cycle is reachable through a static field, and stays
for as long as the list holds it.

A Java memory leak is an object that stays reachable after nothing needs it. The duplicate check
kept every order id it had seen in a static list, and no code removed one. Every entry stayed
reachable, so each full collection kept all of them. The heap after collection grew with traffic
until the pod hit `OutOfMemoryError`. A bound fixes it: drop ids older than the retry window, or
cap the list's size. The JVM runs those collections on its own as the heap fills. How the collector finds what is reachable is unit 10.2.2.

> **`System.gc()` is a request.** On Java 21 with default settings the unreachable cycle was
> collected in 10 runs out of 10 of this program. With `-XX:+DisableExplicitGC` the same program
> printed `false` on both lines: nothing was collected. A test that relies on it passes or fails
> with the JVM's flags.

<!-- practice 3 -->

## Summary

- **Assignment copies the reference, not the object:** two variables, or a caller and a method,
  can change one object.
- **A copy is only as deep as the code that made it:** `new ArrayList<>(list)`, `List.copyOf`
  and `clone()` share the elements; copy every mutable level, or make the elements immutable.
- **`==` compares references:** it passes for string literals and fails for strings built at run
  time. `equals` compares what the class defines, which is identity unless the class overrides it.
- **A reference field or array element starts as `null`:** the NPE comes where it is used;
  `Objects.requireNonNull` where the reference enters moves the failure to the mistake.
- **An object lives while something reachable refers to it:** unreachable cycles are collected,
  and anything a static collection holds is not.

## Reference

**What a copy of a list or array shares (Java 21):**

| Copy of `a` | New container | Same elements | Can be edited | Sees later adds to `a` |
|---|---|---|---|---|
| `b = a` | no | yes | yes | yes |
| `new ArrayList<>(a)` | yes | yes | yes | no |
| `List.copyOf(a)` | yes, unless `a` is already unmodifiable | yes | no | no |
| `a.stream().toList()` | yes | yes | no | no |
| `Collections.unmodifiableList(a)` | a view of `a` | yes | no | yes |
| `array.clone()`, `Arrays.copyOf(array, n)` | yes | yes | yes | n/a |

A two-dimensional array's `clone()` copies only the outer array: after `copy[0][0] = 99`,
`original[0][0]` is 99 too.

**Defaults and lifetime:**

| Situation | What happens |
|---|---|
| reference field, array element | starts as `null` |
| local variable | no default: reading it before assignment is a compile error |
| object reachable only through a `WeakReference` | may be collected; `get()` then returns `null` |
| `Object.finalize()` | deprecated for removal since Java 18 (JEP 421), may never run; use try-with-resources or `java.lang.ref.Cleaner` |

## Challenges

1. A service caches `Customer` objects and hands them to its callers. Which callers can change the
   cached copy? Compare returning a deep copy, an unmodifiable view and an immutable type: what does
   each cost per request, and what does each still let a caller change?
2. An `Order` whose `equals` and `hashCode` use its lines is put in a `HashSet`, and then a line is
   added. Does the set still find it? Try it, and decide what a key's fields must be.
3. Find a static collection in a code base you know that only grows. What bounds it, and how would
   you show from a heap dump that it is what holds the memory?

## Design Note: Why Java Copies References and Never Objects

Every object in Java is reached through a reference, and assignment, argument passing and
collections copy only the reference. Passing a ten-thousand-line order costs the same as passing an
`int`, and because a program can't see or compute addresses, the collector is free to move objects
and update every reference to them.

The price: an object reachable through two references can change under
either holder. Java's answer is immutability rather than copying. A `String`, an `Integer`, a
`LocalDate` or a record of immutable values can be shared by any number of holders, which is what
lets the JVM hand one `"ORD-7"` object to every literal in the program. A mutable object that
crosses a boundary needs an owner who decides who may change it, and a copy or a read-only view
for everyone else.

## Code Examples

- Test: [ReferenceTypesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javabasics/ReferenceTypesTest.java)
- Source: [ReferenceTypes.java](src/main/java/com/github/msorkhpar/claudejavatutor/javabasics/ReferenceTypes.java)