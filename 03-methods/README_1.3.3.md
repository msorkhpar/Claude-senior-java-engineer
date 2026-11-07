# 1.3.3 Pass by Value and Pass by Reference in Java

One sprint of the shop's checkout service brought four tickets. Coupons never lower the total.
"Empty cart" leaves the cart full. The leaderboard helper reorders the admin's list of scores, and
for the mobile API it throws `UnsupportedOperationException`. Every invoice in the nightly batch
shows the last customer's lines. In each one, a method and its caller disagree about what the
method can change.

A variable of a reference type holds a reference, and assignment copies it (unit 1.1.2). Passing
an argument is one more such copy.

## Java Copies Every Argument, So Assigning to a Parameter Never Reaches the Caller's Variable

Predict the three lines:

```java
static void swap(List<String> a, List<String> b) {
    List<String> t = a;
    a = b;
    b = t;
}

static void emptyByAssigning(List<String> cart) {
    cart = new ArrayList<>();
}

static void emptyByClearing(List<String> cart) {
    cart.clear();
}
```

```text
after swap:     mine=[tea] yours=[cake]
after assigning: cart=[tea, cake]
after clearing:  cart=[]
```

When a method is called, each parameter is a new local variable initialised with the argument's
value (JLS §8.4.1). For a primitive that value is the value itself; for a reference type it is
the reference, or `null`.
`swap` exchanged its own two copies and returned. `emptyByAssigning` pointed its copy at a new
list, which nothing referred to once it returned. `clear()` followed the copy to the one list both
variables refer to.

Pass by reference (C++ `&`, C# `ref`) makes the parameter another name for the caller's
variable, so `swap` works there. Java has no such parameter, for objects either, which is why
"empty cart" changed nothing without an error.

Declaring the parameter `final` turns the assignment into a compile error (`final parameter cart
may not be assigned`). It does nothing about `cart.clear()`, which still compiles.

### Spot the Problem: a Default That Never Arrives

The import job gives every post an `imported` tag, and an untagged post `general` as well:

```java
static void addDefaults(List<String> tags) {
    if (tags.isEmpty()) tags = new ArrayList<>(List.of("general"));   // (1)
    tags.add("imported");                                             // (2)
}
```

```text
tagged:   [java, imported]
untagged: []
```

Line (1) points `tags` at a new list, so line (2) adds to that list, and the caller's post keeps
no tags at all. A method that changes the caller's object never assigns to the parameter that
refers to it: `tags.add("general")` instead. Reassigning a parameter is harmless when the method
never meant to change the caller's object (`s = s.strip()`).

<!-- practice 1 -->

## An Immutable Argument Can't Be Changed, So a Method That Computes a New Value Returns It

Two versions of the coupon code, and two other helpers written the same way:

```java
static void applyCoupon(BigDecimal total, BigDecimal off) {
    total.subtract(off);                      // the result is thrown away
}

static void applyCouponAssigning(BigDecimal total, BigDecimal off) {
    total = total.subtract(off);              // only the parameter changes
}

static void countVisit(Integer visits) {
    visits++;
}

static void addSuffix(String sku) {
    sku += "-EU";
}
```

Called on `80.00` with `15.00` off, a count of `1000` and `"TEA-1"`:

```text
total=80.00 visits=1000 sku=TEA-1
```

`BigDecimal`, `String` and the wrappers have no method that changes the object. `subtract` returns
a new `BigDecimal`; `visits++` unboxes, adds and assigns the boxed result to the parameter; `+=`
builds a new `String` into the parameter. The only "change" these methods can make is an
assignment to the parameter, which the caller never sees. `javac -Xlint:all` compiles them
without a warning. The shop charged full price until someone compared receipts.

Return the new value, and let the caller assign it: `total = withCoupon(total, off)` printed
`65.00`. A method with two results returns an object that holds both, such as a record, not an
`int[]` or `AtomicInteger` out-argument the caller has to know to read.

<!-- practice 4 -->

## A Method That Changes Its Argument Makes That Change Part of Its Contract

The leaderboard helper returns the three best scores:

```java
static List<Integer> top3(List<Integer> scores) {
    scores.sort(Comparator.reverseOrder());
    return new ArrayList<>(scores.subList(0, 3));
}
```

The admin page passes its `ArrayList` of scores in the order they were entered; the mobile API
passes `List.of(...)`:

```text
top3: [95, 88, 70], caller's list now [95, 88, 70, 40, 12]
top3 of List.of: java.lang.UnsupportedOperationException
```

The admin got the right result and lost its entry order. The mobile call failed: every method of
a `List.of` list that could change it throws, even when the call would change nothing (`sort` on
one element, `removeIf` that matches nothing). Nothing in the signature says the argument is
changed. The admin page's tests passed, and the failure came with the second caller.

A method that only reads its input builds its result without touching it:

```java
static List<Integer> top3(List<Integer> scores) {
    return scores.stream().sorted(Comparator.reverseOrder()).limit(3).toList();
}
```

It returned `[95, 88, 70]` for both callers and left the admin's list as it was. Its result is
unmodifiable now, which callers of the old version may not expect. When changing
the argument is the point, as with `Collections.sort(list)`, the name and the documentation say
so, and the caller decides what to pass.

<!-- practice 2 -->

## A Method That Keeps Its Argument Keeps the Caller's Object

The nightly batch reuses one buffer list for each customer's lines, and the invoice stores what
it is given:

```java
import java.util.List;

final class Invoice {
    private final String customer;
    private final List<String> lines;

    Invoice(String customer, List<String> lines) {
        this.customer = customer;
        this.lines = lines;
    }

    @Override public String toString() { return customer + " " + lines; }
}
```

```java
for (int i = 0; i < names.size(); i++) {
    buffer.clear();
    buffer.addAll(orders.get(i));
    invoices.add(new Invoice(names.get(i), buffer));
}
```

```text
class:  [Ann [juice, bagel], Bo [juice, bagel], Cy [juice, bagel]]
record: [Ann [juice, bagel], Bo [juice, bagel], Cy [juice, bagel]]
view:   [Ann [juice, bagel], Bo [juice, bagel], Cy [juice, bagel]]
```

The constructor stored the caller's reference, so the three invoices and the buffer share one
list. Each `clear()` rewrote every invoice built before it. `final` stops the field from being
reassigned, not the list from changing. The second line is the same batch with
`record InvoiceRecord(String customer, List<String> lines)`: a record's implicit constructor
stores its components as given. The third wraps the argument in
`Collections.unmodifiableList(lines)` before storing it, which is a read-only view of the caller's
list, not a copy.

An object that keeps an argument copies it on the way in:

```java
import java.util.List;

record CopiedInvoice(String customer, List<String> lines) {
    CopiedInvoice {
        lines = List.copyOf(lines);
    }
}
```

```text
copied: [Ann [tea, cake], Bo [coffee], Cy [juice, bagel]]
```

`List.copyOf` copies only the list, not the elements (unit 1.1.2), and throws
`NullPointerException` if the list holds a `null`. Unit 2.2.2 covers the other direction, a getter
that hands out its field.

<!-- practice 3 -->

## Summary

- **Java copies every argument:** assigning to a parameter, primitive or reference, never changes
  the caller's variable, and code after that assignment works on the new object.
- **An immutable argument can't be changed:** `subtract`, `++` and `+=` produce a new value instead
  of changing the object, so a method that computes a new value returns it.
- **Changing an argument is part of a method's contract:** callers lose state they relied on, and
  callers that pass an unmodifiable list get an exception. A method that only reads leaves its input alone.
- **Keeping an argument keeps the caller's object:** later changes by the caller show through a
  `final` field, a record component or an unmodifiable view; copy on the way in.

## Reference

**What a method can do with its argument (Java 21):**

| Argument | Assign to the parameter | Change the object through it |
|---|---|---|
| primitive | changes the local copy only | n/a |
| mutable object (`ArrayList`, `StringBuilder`, array) | changes the local copy only | the caller sees the change |
| immutable object (`String`, `Integer`, `BigDecimal`) | changes the local copy only | impossible: no method changes it |
| unmodifiable list (`List.of`, `List.copyOf`) | changes the local copy only | throws `UnsupportedOperationException`, even if nothing would change |

**Keeping a list argument:**

| Stored as | Caller's later changes show | Holder can change it |
|---|---|---|
| `lines` | yes | yes |
| `Collections.unmodifiableList(lines)` | yes | no |
| `new ArrayList<>(lines)` | no | yes |
| `List.copyOf(lines)` | no | no; rejects `null` elements |

## Challenges

1. Find a method in your codebase that takes a collection and returns `void`. Does it change its
   argument, and does anything but its body say so?
2. An API takes a 50 MB `byte[]` and must keep it. Copy it, or document that the caller must not
   change it after the call? What does each choice cost, and who pays?
3. A parser has to return the parsed value and the position where it stopped. Compare a record, an
   `int[]` position argument and a mutable parser object that remembers its position.

## Design Note: Why Java Has No Pass by Reference

Because every parameter is a copy, a reader of a call site knows that `process(order)` leaves
the variable `order` pointing where it pointed, whatever `process` does, without opening
`process`. In C++, where a `&` parameter looks the same at the call site, the reader has to open
it.

A method with several results needs an object to return them, which records made cheap to write.
A signature never says whether the method changes the object it
is given or keeps it, so that part of the contract lives in names, documentation and immutable
types.

## Code Examples

- Test: [PassByValueReferenceReferenceTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methods/PassByValueReferenceReferenceTest.java)
- Source: [PassByValueReference.java](src/main/java/com/github/msorkhpar/claudejavatutor/methods/PassByValueReference.java)

This example demonstrates that:

1. Assigning to the parameter `value` inside `modifyPrimitive` doesn't affect the caller's `x`.
2. Changes to the object's state (`sb.append()` in `modifyObject`) are visible to the caller.
3. Reassigning `sb` inside `reassignObject` doesn't affect the caller's `sb`.

These tests demonstrate:

1. Primitives are passed by value, and modifications inside methods don't affect the original value.
2. Objects (like StringBuilder) are passed by value of the reference, allowing modifications to the object's state.
3. Reassigning object references inside methods doesn't affect the original reference.
4. Arrays, being objects, behave similarly to other objects.
5. Strings and wrapper classes (like Integer) are passed exactly like any other reference; because they are immutable,
   the method cannot change the object, and `s += " World"` or `i = 20` only reassigns the local parameter.

This implementation and test suite cover the core concepts of pass by value in Java, including common edge cases and
potential misconceptions. It provides a solid foundation for understanding and explaining this important Java concept
in an interview setting.
