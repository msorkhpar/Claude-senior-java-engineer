# 2.1.4 Inheritance Basics in Java

`PremiumAccount extends Account` and redeclares `overdraftLimit` as 5,000, yet premium customers
are refused once they are 1,000 overdrawn. Next to it, a `TreeSet` subclass that counts additions
for a dashboard counts six when three cities are added from a `List`, and three when they come as
a `TreeSet`. Neither subclass has a line that looks wrong. Each assumes something about its
superclass that isn't so.

Dispatch, static hiding and private methods are unit 1.3.1; covariant returns are unit 1.3.2.

## A Field Is Hidden, Not Overridden: the Declared Type Picks Which One Is Read

```java
class Account {
    long overdraftLimit = 1_000;
    long balance;

    boolean canWithdraw(long amount) {
        return balance - amount >= -overdraftLimit;
    }
}

class PremiumAccount extends Account {
    long overdraftLimit = 5_000;
}
```

With `Account asAccount = premium`, predict the three lines, then read them:

```text
premium.overdraftLimit:   5000
asAccount.overdraftLimit: 1000
premium.canWithdraw(3_000): false
```

A premium account has two fields named `overdraftLimit`. Only overridable instance methods are
chosen by the object at run time. The compiler binds a field access to the field of the declared type, and inside
`Account` that type is `Account`, so `canWithdraw` reads 1,000 whatever the object is. A test that
checks `premium.overdraftLimit` passes, and customers are refused.

Put the varying value behind a method the subclass overrides:

```java
class Account {
    long balance;

    long overdraftLimit() { return 1_000; }

    boolean canWithdraw(long amount) {
        return balance - amount >= -overdraftLimit();
    }
}

class PremiumAccount extends Account {
    @Override long overdraftLimit() { return 5_000; }
}
```

Now `canWithdraw(3_000)` on a premium account returns `true`.

<!-- practice 1 -->

## super.m() Calls the Superclass's Version, and Nothing Reaches Past It

An audit logger extends a JSON logger and wants `Logger`'s plain text. Calling
`format("card blocked")` on it:

```java
class Logger {
    String format(String msg) { return msg; }
}

class JsonLogger extends Logger {
    @Override String format(String msg) { return "{\"msg\":\"" + msg + "\"}"; }
}

class AuditLogger extends JsonLogger {
    @Override String format(String msg) {
        return "AUDIT " + ((Logger) this).format(msg);   // wants Logger's plain text
    }
}
```

```text
AuditLogger: java.lang.StackOverflowError
```

A cast changes the declared type, not the object, so the call dispatches to `AuditLogger.format`
again, which calls itself. `super.format(msg)` would print `AUDIT {"msg":"card blocked"}`: a
`super` call is not dispatched on the object. It runs the version the direct superclass has,
declared there or inherited, here `JsonLogger`'s. `super.super.format(msg)` doesn't compile
(`<identifier> expected`).

Once `JsonLogger` overrides `format`, no subclass of `JsonLogger` can reach `Logger`'s version. If the plain
version is needed, `JsonLogger` must offer it under another name.

## Code Called Through super Still Calls Your Overrides, So a Subclass Depends on Its Superclass's Self-Calls

The dashboard's set counts in both methods:

```java
import java.util.Collection;
import java.util.TreeSet;

class CountingSet extends TreeSet<String> {
    int added;

    @Override public boolean add(String s) {
        added++;
        return super.add(s);
    }

    @Override public boolean addAll(Collection<? extends String> c) {
        added += c.size();
        return super.addAll(c);
    }
}
```

```text
addAll(a List) on an empty set:       6
addAll(a TreeSet) on an empty set:    3
then, on that set, addAll(2 more):    7
```

`super.addAll(c)` runs `TreeSet`'s code, but on this object, and the calls that code makes to
`add` are ordinary virtual calls that reach `CountingSet.add`. For a `List`, `TreeSet.addAll`
adds the elements one by one through `add`. For a sorted set with the same ordering, added to an
empty set, it builds the tree directly and calls no `add` at all. Removing the `addAll` override
("it calls `add` anyway") gives:

```text
addAll(a List) on an empty set:       3
addAll(a TreeSet) on an empty set:    0
```

None of this is in `TreeSet`'s javadoc, so a later JDK may change which method calls which, and the counts change without a
compile error. This is the fragile base class problem: a subclass that overrides a method its
superclass calls on itself depends on the superclass's implementation, not its contract.

Wrap the set and forward to it instead, so its internal calls never reach your code (unit 8.4.1
develops this). Or, for a class you write to be extended, document which methods call which
and make the rest `final`.

<!-- practice 2 -->

## An Override Can Widen Access but Not Narrow It, So a Subclass Can't Take a Method Away

An escrow account must not be closed by hand, so its author tries to hide `close()`:

```java
public class Narrow {
    public static class Account {
        public void close() { System.out.println("closed"); }
    }

    public static class EscrowAccount extends Account {
        @Override void close() { throw new UnsupportedOperationException(); }
    }
}
```

```text
Narrow.java:7: error: close() in EscrowAccount cannot override close() in Account
        @Override void close() { throw new UnsupportedOperationException(); }
                       ^
  attempting to assign weaker access privileges; was public
1 error
```

A caller holding an `Account` compiled against a public `close()` and gets the escrow override at
run time. Access is checked against the declared type, so a narrower override couldn't
keep anyone out. Leaving out `@Override` changes nothing: the method still overrides, and the
same error follows. Widening is allowed: `Object.clone()` is `protected`, `ArrayList.clone()` is
`public`.

That leaves an override that throws. It compiles, and the failure moves to run time: a loop that
closes three accounts closes the first, throws at the escrow one, and never reaches the third.
Code written against `Account` breaks: the Liskov principle, unit 8.1.3.

## protected Reaches a Subclass in Another Package Only Through Its Own Type

```java
package bank;

public class Account {
    protected long balance;
}
```

```java
package premium;

import bank.Account;

public class PremiumAccount extends Account {
    void absorb(PremiumAccount other) { balance += other.balance; }

    void absorb(Account other) { balance += other.balance; }
}
```

```text
premium/PremiumAccount.java:8: error: balance has protected access in Account
    void absorb(Account other) { balance += other.balance; }
                                                 ^
1 error
```

Outside `bank`, `PremiumAccount` may use `balance` only through references of type
`PremiumAccount` or its subclasses. Otherwise any class could read any account's balance by
declaring a subclass. Inside `bank`, every class reads `balance` freely: `protected` includes
package access, so it exposes more than package-private does.

<!-- practice 3 -->

## Summary

- **Fields are hidden, not overridden.** Code in the superclass reads its own field; vary a value
  through an overridable method.
- **`super.m()` runs the direct superclass's version.** A cast to a superclass still dispatches
  to the override, and `super.super` doesn't exist.
- **Code run through `super` calls your overrides.** A subclass that overrides a self-called
  method depends on the superclass's implementation, and can break when it changes.
- **Overrides can't narrow access.** A subclass can only refuse an inherited method at run time,
  which breaks callers of the superclass.
- **`protected` works across packages only through the subclass's own type**, and opens the
  member to the whole package.

## Reference

| Expression | Dispatched on the object? | Reaches |
|---|---|---|
| `ref.field` | no | the field of `ref`'s declared type |
| `ref.method()` | yes | the override in the object's class |
| `((Base) this).method()` | yes | the override in the object's class |
| `super.method()` | no | the direct superclass's version, own or inherited |

| Member is | Same package | Subclass in another package | Other code |
|---|---|---|---|
| `public` | yes | yes | yes |
| `protected` | yes | through its own type only | no |
| package-private | yes | no, and not inherited | no |
| `private` | no, and not inherited | no | no |

## Challenges

1. `AbstractCollection` documents that its `addAll` goes through `add`; `TreeSet` says nothing. Which
   other JDK classes document their self-calls, and would you subclass one that doesn't?
2. You maintain a library class that users already subclass, and you want `addAll` to stop calling
   `add` for speed. How do you find out whom you would break, and what do you ship instead?
3. A framework makes your entity extend its base class, and you need a count like the
   dashboard's. Where does the counter go so a framework upgrade can't change it?

## Design Note: Why Fields Don't Dispatch

Java dispatches overridable instance methods and binds the rest without looking at the object:
fields, static and private methods, `super` calls. A field read stays one instruction with no lookup, and a superclass's fields keep
their meaning when a subclass adds one with the same name. The price is this page's opening bug.
Keep fields `private` and expose behaviour through methods, which a subclass overrides on purpose.

## Code Examples

- Test: [InheritanceExampleTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/classobj/InheritanceExampleTest.java)
- Source: [InheritanceExample.java](src/main/java/com/github/msorkhpar/claudejavatutor/classobj/InheritanceExample.java)
