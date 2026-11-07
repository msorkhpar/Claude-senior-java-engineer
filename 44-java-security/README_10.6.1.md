# 10.6.1. Security Best Practices in Java

An incident review of an account service lists four findings. A support log holds a line with a
user's password in it. Two users received the same password-reset link. An attachment named
`%2e%2e%2freport.pdf` passed the name check. A heap dump attached to a vendor's support ticket holds
a password that the code wipes after every login.

Each finding has a rule, one per section below. Injection, path traversal, XML entities and
deserialization are unit 10.6.2; encryption and password hashing are unit 10.6.3. The
`SecurityManager` is no part of the answer: JEP 411 deprecated it for removal in Java 17, and on
Java 21 `System.setSecurityManager` throws `UnsupportedOperationException` unless the JVM starts
with `-Djava.security.manager=allow`.

## Validate the Value the Next Layer Will Use, Against an Allow-List

The upload endpoint checks attachment names with a deny-list: refuse anything containing `../`, `/`
or `\`. The storage service behind it decodes the name with `URLDecoder` (form decoding, which also turns
`+` into a space) and normalises it with NFKC. An allow-list instead matches the whole input
against a pattern for a good name:

```java
static boolean denyList(String name) {
    return !name.contains("../") && !name.contains("/") && !name.contains("\\");
}

static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-]{1,64}\\.(pdf|png|txt)");

static boolean allowList(String name) {
    return NAME.matcher(name).matches();
}

// Decode and normalise first, then allow-list the value the later layers will use.
static String canonical(String raw) {
    return Normalizer.normalize(URLDecoder.decode(raw, StandardCharsets.UTF_8), Normalizer.Form.NFKC);
}
```

Six names, each check's verdict, and what the storage layer receives:

```text
input                deny      later layer     allow     canonical, then allow
report.pdf           accepted  report.pdf      accepted  accepted: report.pdf
../report.pdf        refused   ../report.pdf   refused   refused
%2e%2e%2freport.pdf  accepted  ../report.pdf   refused   refused
full-width ../       accepted  ../report.pdf   refused   refused
full-width report    accepted  report.pdf      refused   accepted: report.pdf
Cyrillic \u0440eport accepted  \u0440eport.pdf refused   refused
```

`String.contains` compares characters as they are, and neither `%2e%2e%2f` nor the full-width
`．．／` (U+FF0E, U+FF0E, U+FF0F) contains the characters the deny-list looks for. Both became `../`
one layer later, after the check had passed them. A deny-list has to name every spelling of every
bad input, and each decoder or normaliser downstream adds more.

The allow-list refused the encoded and full-width names, because `%`, `．` and `／` are not in its
character class, so a spelling nobody thought of is refused. Decoding and normalising first make the check
judge the string the storage layer will use, so the full-width `ｒｅｐｏｒｔ.pdf` is accepted and
stored as `report.pdf`, not under a second spelling of the same name.

NFKC doesn't help with the last row. The Cyrillic `р` (U+0440) is a different letter from the
Latin `p`, and normalisation leaves it alone; only the allow-list's ASCII range refuses it. A field
that must accept other scripts needs a rule about which ones, or two names that look the same can
belong to two people.

`URLDecoder.decode` throws `IllegalArgumentException` on a stray `%`, as in `100%.pdf`; treat that
as a refusal too.

<!-- practice 1 -->

## A Token from Random Is a Function of Its Seed; Use SecureRandom

The reset service made each token like this:

```java
static String seededToken() {
    Random random = new Random(System.currentTimeMillis());
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
}

static final SecureRandom SECURE = new SecureRandom();   // secureToken() is the same with SECURE
```

Two requests in the same millisecond, then 10,000 tokens from each generator, issued as fast as a
loop runs:

```text
same millisecond, same token: true
Random(currentTimeMillis()): 5 distinct of 10000 (4 ms)
SecureRandom:                10000 distinct of 10000
SecureRandom algorithm: NativePRNG
```

The javadoc of `java.util.Random` specifies a 48-bit seed advanced by a linear
congruential step, and requires that two instances with the same seed return the same sequence.
The seed here is the clock, so requests in the same millisecond share a token, and one second of
possible request times is only 1,000 seeds. Seeding from `nanoTime()`, or leaving the seed out,
removes the collisions but not the formula: the javadoc says instances of `Random` are not
cryptographically secure, and `Math.random()` uses one.

`SecureRandom` on Linux defaults to `NativePRNG`, which reads the operating system's generator. A
`SHA1PRNG` is different when seeded before its first use: the javadoc says `setSeed` supplements
the existing seed, but a `SHA1PRNG` that hasn't produced anything yet has none to supplement:

```text
SHA1PRNG seeded with the same value, same output: true
NativePRNG seeded with the same value, same output: false
```

Create one `new SecureRandom()` (its javadoc says it is safe for concurrent threads), don't seed it,
and take 16 bytes or more per token.

<!-- practice 4 -->

## Wiping a char[] Removes Your Copy of a Secret; a String Copy Stays Until GC

`Console.readPassword()` returns a `char[]` so that the caller can overwrite it, which a `String`
doesn't allow. This program dumps its own heap at each step of a login. `dump(label, liveOnly)`
calls `HotSpotDiagnosticMXBean.dumpHeap`, which with `liveOnly` set runs a full GC first and writes
only reachable objects. A second JVM then counts the copies of the placeholder password
`hunter2-example` in each dump, so the search adds no copies of its own:

```java
dump("before login", false);

char[] p1 = readPassword();
verify(p1);
dump("char[] in use", false);
Arrays.fill(p1, '\0');
dump("char[] wiped", false);

char[] p2 = readPassword();
verify(new String(p2));                          // an API that takes a String
Arrays.fill(p2, '\0');
dump("char[] wiped, String copy unreachable", false);
dump("after a full GC, live objects only", true);
dump("after a full GC, whole heap", false);
```

```text
heap-1.hprof: 0 as String bytes, 0 as char[]
heap-2.hprof: 0 as String bytes, 1 as char[]
heap-3.hprof: 0 as String bytes, 0 as char[]
heap-4.hprof: 1 as String bytes, 0 as char[]
heap-5.hprof: 0 as String bytes, 0 as char[]
heap-6.hprof: 0 as String bytes, 0 as char[]
```

`Arrays.fill` removed the array's copy at once (dump 3). The second login wiped its array too, but
`new String(p2)` had copied the characters into the `String`'s own byte array, which no public API
can overwrite. Unreachable, it stayed in the heap, and in a whole-heap dump, until a full GC reclaimed
it (dumps 4 to 6).

An API on the way that takes a `String`, such as `DriverManager.getConnection(url, user,
password)`, makes a copy you can't wipe. The program wipes inline; real code wipes in a `finally`,
so a failed login wipes too, and wipes every array it copied the secret into.

> **What the run can't show.** A heap dump lists objects. Whether a reclaimed object's bytes stay in
> the process's memory until the space is reused depends on the collector; a collector that moves
> objects, as G1 does, may leave an earlier copy of the array where it used to be. Swap, core files
> and the operating system's buffers are out of Java's reach. Wiping shortens the time the
> characters sit in a live object; it doesn't remove every copy.

<!-- practice 2 -->

## A Record's toString Prints Every Component, Password Included

Nobody wrote `log.info(password)`. The login handler logged the request:

```java
record LoginRequest(String username, String password) {}

record Session(String id, LoginRequest origin) {}
```

```java
var request = new LoginRequest("jane", "hunter2-example");
log.info("login attempt: " + request);                  // java.util.logging
try {
    if (request.username().length() < 5) throw new IllegalArgumentException("username too short: " + request);
} catch (IllegalArgumentException e) {
    System.out.println(e);
}
System.out.println(new Session("s-41", request));
```

```text
Nov 07, 2026 1:10:31 AM Leak main
INFO: login attempt: LoginRequest[username=jane, password=hunter2-example]
java.lang.IllegalArgumentException: username too short: LoginRequest[username=jane, password=hunter2-example]
Session[id=s-41, origin=LoginRequest[username=jane, password=hunter2-example]]
```

A record's generated `toString` prints every component by name and value, and a record that
contains it prints it too, because the generated `toString` of `Session` calls the one of
`LoginRequest`.

Two fixes, run in the same program: override `toString` in the record, or give the secret a type
whose `toString` hides it.

```java
record SafeLoginRequest(String username, String password) {
    @Override
    public String toString() {
        return "SafeLoginRequest[username=" + username + ", password=***]";
    }
}

record ArrayLoginRequest(String username, char[] password) {}

record Secret(String value) {
    @Override
    public String toString() {
        return "***";
    }
}

record TypedLoginRequest(String username, Secret password) {}
```

```text
SafeLoginRequest[username=jane, password=***]
ArrayLoginRequest[username=jane, password=[C@728938a9]
TypedLoginRequest[username=jane, password=***]
equal with a copy of the same array: false
```

The override protects one record; every other record holding the password as a `String` needs its
own. The `Secret` type makes every `toString` that reaches it print `***`. A `char[]` component
doesn't print its characters, because arrays inherit `Object.toString`, but the generated `equals`
then compares the arrays by identity, as the last line shows.

<!-- practice 3 -->

## Summary

- **Validate the value the next layer will use, against an allow-list.** A deny-list checks one
  spelling; `%2e%2e%2f` and full-width `．．／` passed it and became `../` downstream. Decode,
  normalise with NFKC, then match the whole string; normalisation doesn't touch look-alike letters
  from other scripts.
- **A token from `Random` is a function of its seed.** Seeded from the clock, 10,000 tokens came out
  as 5 distinct values. Use one unseeded `SecureRandom`; a `SHA1PRNG` seeded before use repeats
  too.
- **Wiping a `char[]` removes your copy; a `String` copy stays until GC.** A heap dump showed the
  password after the array was wiped, because an API took a `String`.
- **A record's `toString` prints every component.** Log calls, exception messages and enclosing
  records all print the password; override `toString`, or wrap the secret in a type that prints
  `***`.

## Reference

| Call or type (Java 21) | Behaviour | Source |
|---|---|---|
| `String.contains`, `String.equals` | compare the characters as given; no decoding or normalisation | run |
| `Normalizer.normalize(s, Form.NFKC)` | full-width `．／ｒ` become `./r`; Cyrillic `р` stays | run |
| `Matcher.matches()` / `Matcher.find()` | whole input / any part of it | javadoc |
| `new Random(seed)` | same seed, same sequence, on every implementation | javadoc |
| `new Random()`, `Math.random()` | not cryptographically secure | javadoc |
| `new SecureRandom()` on Linux | `NativePRNG`; thread-safe | run, javadoc |
| `SecureRandom.getInstance("SHA1PRNG")` + `setSeed` before use | same seed, same output | run |
| `Arrays.fill(chars, '\0')` | the array's copy is gone from the next heap dump | run |
| `new String(chars)` | a copy that no API can overwrite; in a whole-heap dump until GC | run |
| record `toString` | every component; an array component prints as `[C@…` | run |
| `System.setSecurityManager` | `UnsupportedOperationException` unless `-Djava.security.manager=allow` | run |

## Challenges

1. Your API gateway URL-decodes paths, and your service decodes them again. Which string should your
   validation see, and what does a double-encoded `%252e` turn into at each layer?
2. A username field must accept Latin, Greek and Cyrillic names. Which rule stops `аdmin` (with a
   Cyrillic `а`) from passing for `admin`, and what does it cost legitimate users?
3. Find every place in a service where a password exists as a `String`, starting from the HTTP
   request body. Which of them can you remove, and which belong to libraries you can't change?

## Design Note: Hide the Secret in Its Type, Not at Each Call Site

A rule such as "never log the password" depends on every developer, at every log call, knowing which
objects contain one. The record run shows how it fails: the code logged a request and a
session. A `Secret` type makes printing safe wherever the value travels.

Every reader of the value then calls `value()`, and the type changes only `toString`: any code that
calls `value()`, a serializer that reads record components included, still sees the text, and the
value is still a `String` that a heap dump can show.

## Code Examples

- Source: [SecureCodingPractices.java](src/main/java/com/github/msorkhpar/claudejavatutor/javasecurity/SecureCodingPractices.java)
- Test: [SecureCodingPracticesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javasecurity/SecureCodingPracticesTest.java)
