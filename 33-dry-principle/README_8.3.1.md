# 8.3.1. Definition and Purpose of DRY Principle

Users of a payments app send each other money by username. In March, `alice` and `Alice` received
each other's payments. The fix makes sign-up refuse a name that differs from a taken one only in
case, and its test passes. In May, support finds a third account, `ALICE`, with the fix still in
place.

DRY ("Don't Repeat Yourself") comes from *The Pragmatic Programmer* (Hunt and Thomas, 1999): "Every
piece of knowledge must have a single, unambiguous, authoritative representation within a system."
It is about knowledge, not text.

## A Rule Kept in Two Places Gets Fixed in One of Them

The account class after the March fix:

```java
import java.util.HashMap;
import java.util.Map;

/** Who owns which username. Users send each other money by username. */
public class Accounts {
    private final Map<String, String> owners = new HashMap<>();     // username -> account id

    public void signUp(String username, String accountId) {
        // the March fix: was owners.containsKey(username)
        if (owners.keySet().stream().anyMatch(n -> n.equalsIgnoreCase(username)))
            throw new IllegalArgumentException("username taken: " + username);
        owners.put(username, accountId);
    }

    public void rename(String accountId, String from, String to) {
        if (owners.containsKey(to))
            throw new IllegalArgumentException("username taken: " + to);
        owners.remove(from);
        owners.put(to, accountId);
    }

    public String ownerOf(String username) {
        return owners.get(username);
    }
}
```

Each check starts from a fresh `Accounts` where `alice` (acct-1) and `bob` (acct-2) have signed up;
`attempt` runs an action on it and reports whether it was refused. The first check is March's test:

```java
check("sign up as Alice", "refused", attempt(a -> a.signUp("Alice", "acct-3")));
check("bob renames to ALICE", "refused", attempt(a -> a.rename("acct-2", "bob", "ALICE")));
check("pay Alice", "acct-1", seeded().ownerOf("Alice"));
```

Before reading on: which of them fail?

```text
ok      sign up as Alice
FAILED  bob renames to ALICE: expected refused, got accepted
FAILED  pay Alice: expected acct-1, got null
```

"Two usernames that differ only in case are the same name" is one piece of knowledge. Before March,
`signUp` and `rename` each held a copy, the same `containsKey` check. The bug report named sign-up, so
the fix and its test went there. `rename` still lets anyone take a look-alike name, and money sent
to the name a user typed reaches whoever holds that spelling.

## A Copy of a Rule Doesn't Have to Look Like One

The third check failed although no line of `Accounts` mentions case. `owners.get(username)` asks the
`HashMap`, which compares keys with `String.equals`. That is a third copy of the rule, and it says
the opposite: names are the same only when every character is.

Searching for `equalsIgnoreCase` finds only the fixed copy. After the fix, `signUp` and `rename`
share no line, so a duplicate-code detector, which looks for code that looks alike, finds nothing
either.
The copies turn up when you ask which code decides whether two usernames are the same: here, every
lookup in `owners`.

## One Method Decides When Two Names Are the Same, and Every Path Calls It

An `isTaken(name)` helper called from `rename` would fix the second check but not the third, because
`ownerOf` would still decide with `String.equals`. The rule belongs where every path passes, the
map's key:

```java
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Who owns which username. Users send each other money by username. */
public class Accounts {
    private final Map<String, String> owners = new HashMap<>();     // key(username) -> account id

    /** When two usernames are the same name. Nothing else in the class decides it. */
    static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }

    public void signUp(String username, String accountId) {
        if (owners.putIfAbsent(key(username), accountId) != null)
            throw new IllegalArgumentException("username taken: " + username);
    }

    public void rename(String accountId, String from, String to) {
        if (owners.putIfAbsent(key(to), accountId) != null)
            throw new IllegalArgumentException("username taken: " + to);
        owners.remove(key(from));
    }

    public String ownerOf(String username) {
        return owners.get(key(username));
    }
}
```

The same three checks:

```text
ok      sign up as Alice
ok      bob renames to ALICE
ok      pay Alice
```

A method added later that reads `owners` with a raw name, skipping `key`, finds only names already
in lower case, so the first check with a name like `Alice` exposes it. The next change to what counts as the same name, such as ignoring a trailing space, is one edit
to `key`, and sign-up, rename and payment all get it. `Locale.ROOT` keeps the key the same on a
server whose default locale is Turkish, where `"I".toLowerCase()` is a dotless `ı`.

<!-- practice 1 -->

## Identical Lines Are One Rule Only If One Must Change When the Other Does

The app also allows one account per email address, in a `Contacts` class whose `key(email)` has the
same body: `return email.toLowerCase(Locale.ROOT);`. Two methods with one body look like what DRY
forbids, so a developer extracts `Identifiers.canonical(s)`, and both `key` methods
become `return Identifiers.canonical(...);`.

Months later, product asks for an email rule. Nearly all the app's users are on Gmail, which ignores
dots before the `@`, and product decides to apply that to every address: `j.doe@example.com` and
`jdoe@example.com` count as one mailbox, and the second registration is refused. There is one place to change:

```java
import java.util.Locale;

/** Extracted from Accounts.key and Contacts.key, whose bodies were the same line. */
final class Identifiers {
    private Identifiers() {}

    static String canonical(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        int at = lower.indexOf('@');                    // new: dots before the @ don't count
        return lower.substring(0, at).replace(".", "") + lower.substring(at);
    }
}
```

```text
ok      register J.Doe@Example.com when jdoe@example.com exists
FAILED  sign up as alice: expected accepted, got java.lang.StringIndexOutOfBoundsException: Range [0, -1) out of bounds for length 5
```

The email test passes, and every username sign-up now throws. A developer who stops the exception with
`if (at < 0) at = lower.length();` makes it quieter: `j.doe` and `jdoe` become one username, which no
rule asked for.

The two lines were equal by coincidence. Ask of any two copies: if one changes, must the other?
For `signUp` and `rename` the answer is yes, every time, because a name one refuses the other must
refuse. For a username and an email address it is no. Each `key` keeps its own body, and the email
rule goes into `Contacts.key` alone:

```java
static String key(String email) {
    String lower = email.toLowerCase(Locale.ROOT);
    int at = lower.indexOf('@');
    return lower.substring(0, at).replace(".", "") + lower.substring(at);
}
```

```text
ok      register J.Doe@Example.com when jdoe@example.com exists
ok      sign up as alice
```

Unit 8.1.1 showed the same failure through two teams sharing a rounding helper, and unit 8.3.4
follows a merged method that grows a parameter for each caller.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **A rule kept in two places gets fixed in one of them.** The copies agree until the first fix,
  which reaches the copy the bug report named.
- **A copy of a rule doesn't have to look like one.** A map lookup by raw name decides equality as
  surely as an `if` does, and neither text search nor a duplicate-code detector finds it.
- **One method decides, and every path calls it.** Keying the data by that method's result makes a
  new path that skips it fail on the first name not already in canonical form.
- **Identical lines are one rule only if one must change when the other does.** Merging two rules
  that agree today hands the next change to one of them to both.

## Reference

What the runs printed, with `alice` and `bob` signed up:

| Shape | Rename `bob` to `ALICE` | Pay `Alice` |
|---|---|---|
| fix in `signUp` only | accepted | `null` |
| one `key(username)` for every path | refused | acct-1 |

With `jdoe@example.com` registered and no usernames taken:

| Shape | Register `J.Doe@Example.com` | Sign up `alice` |
|---|---|---|
| `key` shared with email, email rule added | refused | throws |
| separate `key` per kind of identifier | refused | accepted |

| Expression (Java 21) | Value |
|---|---|
| `"I".toLowerCase()` under a Turkish default locale | `"ı"` |
| `"I".toLowerCase(Locale.ROOT)` | `"i"` |
| `"ı".equalsIgnoreCase("I")` | `true` |

## Challenges

1. With the expression table in mind: if the March fix had stayed in `signUp` and
   `rename` had used `key`, which names would the two paths disagree on? Write the check that finds
   one.
2. The usernames are also unique in the database, through a `UNIQUE` index on the column. Which rule
   does that index encode, and how would you keep it and `key` from drifting apart?
3. Every entry in `owners` is stored under the result of `key`. What has to happen to the data when
   the rule in `key` changes?

## Design Note: Why the Knowledge Goes into the Data's Key

A shared validation method leaves every caller free not to call it, and the next path someone
writes is the one that doesn't. Keying the map by `key(username)` puts the rule where a lookup that
skips it misses on the first mixed-case name. The cost is the third challenge: the rule's output is now stored, so changing the rule
means migrating every stored key, and a team in a hurry will be tempted to add the change in a
second place instead.

## Code Examples

- Test: [DryDefinitionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryDefinitionTest.java)
- Source: [DryDefinition.java](src/main/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryDefinition.java)
