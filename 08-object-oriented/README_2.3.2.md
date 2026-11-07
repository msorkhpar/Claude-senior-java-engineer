# 2.3.2 Polymorphism in Java

The release that added fragile parcels and pallets to the shipping service brought three tickets.
Fragile parcels are billed as ordinary parcels, and pallets go out free. Since a cleanup that
removed "duplicate code", the customs printer prints `skipped` for every shipment. And a helper
that totals weights won't accept a `List<Parcel>`, so its callers copy their lists first. All of it
compiled.

How a call reaches an override is unit 1.3.1, and why an overload is chosen at compile time is
unit 1.3.4.

## An instanceof Chain Fails Silently on a Subclass or a New Type; an Abstract Method Makes the Compiler Ask

The billing code priced each kind of shipment in one method. `FragileParcel extends Parcel`, and
`Pallet`, another direct subclass of `Shipment`, arrived a year after the method was written:

```java
static int feeCents(Shipment s) {
    if (s instanceof Letter) return 85;
    if (s instanceof Parcel p) return 450 + p.grams / 100 * 20;
    if (s instanceof FragileParcel f) return 450 + f.grams / 100 * 20 + 300;
    return 0;                                    // "can't happen"
}
```

Predict the fees for a letter, a 1,200 g parcel, a 1,200 g fragile parcel and a pallet:

```text
Letter            85
Parcel           690
FragileParcel    690
Pallet             0
```

A fragile parcel is a `Parcel`, so the second test catches it and the third never runs. javac
compiles this without a word, even with `-Xlint:all`: it rejects a dominated `case` in a switch
(unit 1.5.4), but it doesn't judge the order of `if` tests. The pallet falls through to a default
written for a case nobody expected. Both mistakes produce a plausible number, so nothing fails until
finance finds the missing surcharges and the free pallets a month later.

Moving the decision into the types removes both mistakes:

```java
abstract class Shipment {
    final int grams;
    Shipment(int grams) { this.grams = grams; }
    abstract int feeCents();
}
class Parcel extends Shipment {
    Parcel(int grams) { super(grams); }
    @Override int feeCents() { return 450 + grams / 100 * 20; }
}
class FragileParcel extends Parcel {
    FragileParcel(int grams) { super(grams); }
    @Override int feeCents() { return super.feeCents() + 300; }
}
```

```text
Letter            85
Parcel           690
FragileParcel    990
```

Callers write `s.feeCents()` and get the most specific implementation the object's class has, so
there is no order to get wrong. A `Pallet` without a price no longer compiles:

```text
Fees.java:22: error: Pallet is not abstract and does not override abstract method feeCents() in Shipment
```

A chain repeats the decision wherever types are tested, and each copy must be found when a type is
added. When the types are sealed and the operations keep growing, a switch over the sealed type is
the better trade, for the reasons in unit 1.5.4's Design Note.

## Java Chooses the Method by the Receiver Alone, So Dispatching on a Second Object Takes Two Calls

Customs needs a form per kind of shipment, and other handlers (labels, insurance) need their own
method per kind. Each handler implements one overload per type:

```java
interface Handler {
    default String visit(Shipment s) { return "skipped"; }   // so a new type doesn't break every handler
    String visit(Letter l);
    String visit(Parcel p);
    String visit(FragileParcel f);
}
```

The obvious call, `customs.visit(s)` with `s` declared as `Shipment`, runs `visit(Shipment)` for
every shipment. The overload is chosen from the argument's declared type (unit 1.3.4), and the run-time
choice looks only at the object left of the dot, `customs`. To dispatch on the shipment as well,
the call goes through the shipment first, and each class declares the same one-line method:

```java
abstract class Shipment {
    abstract String accept(Handler h);
}
class Letter extends Shipment {
    @Override String accept(Handler h) { return h.visit(this); }
}
class Parcel extends Shipment {
    @Override String accept(Handler h) { return h.visit(this); }
}
class FragileParcel extends Parcel {
    @Override String accept(Handler h) { return h.visit(this); }
}
```

```text
Letter         visit(s): skipped   s.accept(customs): no form
Parcel         visit(s): skipped   s.accept(customs): CN22
FragileParcel  visit(s): skipped   s.accept(customs): CN22 + handling note
```

`s.accept(customs)` is dispatched on the shipment, which picks the class. Inside that class `this`
has the class's own type, so the compiler picks the matching overload, and that call is dispatched
on the handler. This is double dispatch, and the Visitor pattern is built on it.

### Spot the Problem: the accept Methods Look Duplicated

The cleanup from the opening deleted the three `accept` methods and wrote one in the base class:

```java
abstract class Shipment {
    String accept(Handler h) { return h.visit(this); }      // written once, inherited by all
}
```

```text
Letter         visit(s): skipped   s.accept(customs): skipped
Parcel         visit(s): skipped   s.accept(customs): skipped
FragileParcel  visit(s): skipped   s.accept(customs): skipped
```

The three bodies were identical as text, not as compiled code. `javap -c` shows the overload each
one calls, in `Letter` and `Parcel` before the cleanup, then in `Shipment` after it:

```text
       2: invokeinterface #7,  2            // InterfaceMethod Handler.visit:(LLetter;)Ljava/lang/String;
       2: invokeinterface #7,  2            // InterfaceMethod Handler.visit:(LParcel;)Ljava/lang/String;
       2: invokeinterface #7,  2            // InterfaceMethod Handler.visit:(LShipment;)Ljava/lang/String;
```

In `Shipment`, `this` is a `Shipment` to the compiler, whatever the object is at run time. A
subclass that inherits `accept` instead of declaring its own has the same problem: it is handled as
its parent. Every class with its own overload needs its own `accept`. The `visit(Shipment)` overload
is what let the cleanup compile; without it, `visit(this)` in `Shipment` would have been an error.

<!-- practice 1 -->

## A List of Parcels Is Not a List of Shipments; a Wildcard Says Whether the Method Reads or Writes

A helper that totals weights takes a `List<Shipment>`. Passing it a `List<Parcel>`:

```text
Invariant.java:17: error: incompatible types: List<Parcel> cannot be converted to List<Shipment>
        System.out.println(totalGrams(parcels));
                                      ^
```

If it compiled, the helper could call `items.add(new Letter())`, and the caller's list of parcels
would hold a letter. The `ClassCastException` would come later, in whatever code read a `Parcel`
from it. Arrays allow the conversion and check every store at run time instead, with unit 1.5.1's
`ArrayStoreException`. Generics refuse at compile time: neither of `List<Parcel>` and
`List<Shipment>` is a subtype of the other.

A wildcard tells the compiler whether a method reads from the list or writes to it:

```java
static int totalGrams(List<? extends Shipment> items) {      // reads Shipments
    int sum = 0;
    for (Shipment s : items) sum += s.grams;
    return sum;
}

static void addReturns(List<? super Parcel> out) {           // writes Parcels
    out.add(new Parcel(450));
}
```

Both methods now take a `List<Parcel>` (1,200 g and 800 g) and a `List<Shipment>` (a truck holding
one letter), and the program adds a return to each. A `Comparator<Shipment>` sorts a
`List<Parcel>` for the same reason, since `List.sort` takes a `Comparator<? super E>`:

```java
Comparator<Shipment> byWeight = Comparator.comparingInt(s -> s.grams);
parcels.sort(byWeight);                                  // List<Parcel>.sort(Comparator<? super Parcel>)
```

```text
parcels: 2000 g, truck: 20 g
truck: [Letter 20 g, Parcel 450 g]
parcels: [Parcel 450 g, Parcel 800 g, Parcel 1200 g]
```

Each wildcard allows only one of the two. A `List<? extends Shipment>` could be a list of pallets,
so the compiler won't add a letter to it. A `List<? super Parcel>` could be a `List<Object>`, so
what comes out is only an `Object`. `CAP#1` is javac's name for the unknown element type:

```text
ReadOnly.java:10: error: incompatible types: Letter cannot be converted to CAP#1
        items.add(new Letter());
ReadOnly.java:14: error: incompatible types: CAP#1 cannot be converted to Parcel
        Parcel first = out.get(0);
```

A parameter the method reads from is `? extends T`, one it writes to is `? super T`, and one it does
both with is plain `T`. The JDK's own signatures follow this, as in
`Collections.copy(List<? super T> dest, List<? extends T> src)`. An API without wildcards passes the
cost to its callers: they copy lists, or cast to a raw type and give up the check.

## A Call Site That Has Seen Three Classes Isn't Inlined, Which Matters Only in a Hot Loop

Is every virtual call a lookup in a method table? This loop calls `feeCents()` from one call site
over 1,000 shipments, drawn in equal shares from one to four small final classes of the benchmark's
own, with one JVM run per number of classes:

```java
static int total(Shipment[] items) {
    int sum = 0;
    for (Shipment s : items) sum += s.feeCents();            // the call site
    return sum;
}
```

`-XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining` shows what the C2 compiler did with the call:

```text
== 1 class(es)
@ 25  CallSite$Letter::feeCents (3 bytes)  inline (hot)
== 2 class(es)
@ 25  CallSite$Letter::feeCents (3 bytes)  inline (hot)
@ 25  CallSite$Parcel::feeCents (4 bytes)  inline (hot)
== 3 class(es)
@ 25  CallSite$Shipment::feeCents (0 bytes)  virtual call
== 4 class(es)
@ 25  CallSite$Shipment::feeCents (0 bytes)  virtual call
```

The interpreter and the first compiler tier record which classes reach each call site, and C2
reads that profile. It keeps two classes (`TypeProfileWidth=2` on Java 21), so a third class leaves
C2 with a virtual call, unless one class has at least 90 % of the calls
(`TypeProfileMajorReceiverPercent`): with three classes and 95 % letters, the same call site
printed `CallSiteSkew$Letter::feeCents (3 bytes)  inline (hot)`. Best of ten timed rounds, on one
machine, with `sink` there to keep the loop's result in use:

```text
1 class(es): 0.14 ns per call (sink 3)
2 class(es): 0.27 ns per call (sink 4)
3 class(es): 1.91 ns per call (sink 6)
4 class(es): 6.54 ns per call (sink 4)
```

> **Measure your own workload.** These numbers come from a hand-timed loop whose methods return a
> constant, on Java 21; once a call is inlined, the time is per element more than per call, and the
> gap between three and four classes is not explained by dispatch alone. Java 25 on the same machine gave 1.70 ns for both three and four classes,
> and prints the inlining lines in another format. A few nanoseconds matter only when the method
> does as little as the call costs, in a loop that runs millions of times. Use JMH and a profiler
> before replacing an override with an `if`.

## Summary

- **An `instanceof` chain fails silently**: an early test hides a subclass's branch, and a new type
  falls into the default. An abstract method puts the decision in each type, and the compiler names
  every class that lacks it.
- **Java dispatches on the receiver only.** An overload is fixed by the argument's declared type,
  so dispatching on a second object takes a second call: `accept` in every class, calling
  `visit(this)`. One `accept` in the base class sends everything to the default.
- **A `List<Parcel>` is not a `List<Shipment>`.** Read through `? extends T`, write through
  `? super T`; arrays allow the conversion and fail at run time instead.
- **HotSpot doesn't inline a call site that has seen three or more classes**, unless one of them
  dominates. That costs
  nanoseconds per call, which matters only in hot loops with tiny methods; measure before designing
  around it.

## Reference

**What chooses the method (Java 21):**

| Call | Chosen from | When |
|---|---|---|
| `obj.m(arg)`, `m` overridable: which implementation | the class of `obj` | run time |
| `obj.m(arg)`: which overload | the declared types of the arguments | compile time |
| `T.m()` or `expr.m()`, `m` static | the declared type left of the dot | compile time |
| `m()`, `m` private | the class that declares it | compile time |
| `super.m()` | the direct superclass, then upward | compile time |

**Wildcards, for a parameter `list`:**

| Declared as | Accepts | `list.get(0)` is | `list.add(x)` takes |
|---|---|---|---|
| `List<T>` | `List<T>` only | `T` | `T` |
| `List<? extends T>` | `List<T>`, `List<S>` for any subtype `S` | `T` | only `null` |
| `List<? super T>` | `List<T>`, `List<U>` for any supertype `U` | `Object` | `T` |

## Challenges

1. A rules engine applies twelve kinds of rule to every order through one interface, and a profile
   shows the dispatching call near the top. What would you measure before changing the design, and
   what would you change it to?
2. Your shipment types live in a library you can't edit, and you need a new operation on them.
   Compare a Visitor the library would have to add, a switch over a sealed type, and an
   `instanceof` chain in your own code.
3. `merge(List<Shipment> a, List<Shipment> b)` returns a new list. Write its signature with
   wildcards, and decide what element type the result should have.

## Design Note: Why Java Dispatches on the Receiver Only

Java chooses the implementation at run time from one object, the receiver, and the overload at
compile time from the declared types of everything else. That keeps every call checkable before it
runs: the compiler knows the one method signature each call will use, and only the class that
supplies it is left to run time. Languages with multiple dispatch choose from the run-time classes
of all the arguments, so a two-type decision needs no Visitor, but an ambiguity between two
methods can then appear only at run time.

The cost in Java is boilerplate: an `accept` in every class, and a handler interface that changes
when a type is added. Switches over sealed types (units 1.5.3 and 1.5.4) now cover many of the
cases the Visitor was written for, and the compiler checks that they handle every type.

## Code Examples

- Test: [PolymorphismDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/oo/PolymorphismDemoTest.java)
- Source: [Shape2.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Shape2.java)
- Source: [Circle2.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Circle2.java)
- Source: [Rectangle2.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Rectangle2.java)
