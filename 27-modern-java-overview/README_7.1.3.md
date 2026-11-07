# 7.1.3. Java 15

An order service moves from Java 11 to Java 21 in one step. The first pod on 21 never comes up:

```text
Unrecognized VM option 'UseBiasedLocking'
Error: Could not create the Java Virtual Machine.
Error: A fatal exception has occurred. Program will exit.
```

Someone deletes the option from the start script. The pod starts, and the first order that needs a
discount rule fails (the trace is from the reproduction in the Nashorn section):

```text
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "javax.script.ScriptEngine.eval(String)" because "engine" is null
	at Rules.main(Rules.java:7)
```

Both failures go back to Java 15 (September 2020), a release between two LTS releases that many
teams skipped, meeting its changes all at once. Most of what 15 made standard has its own unit: text blocks (JEP 378, units 7.4.1 to 7.4.5), helpful `NullPointerException` messages
switched on by default (unit 3.2.2), hidden classes, which lambdas have used since then (JEP 371,
unit 4.1.1), and ZGC and Shenandoah as production collectors (JEPs 377 and 379, modules 40 and
43). Records and sealed classes were still previews in 15 (units 2.4.1 and 2.5.1).

Outputs are from Temurin 21.0.12 unless a line says Java 25 (Temurin 25.0.4). Java 15 itself was
not run; where a claim is about 15, the JEP is named.

## Preview Features Must Be Switched On Twice, at Compile Time and at Run Time

A preview feature is a complete language or VM feature that ships switched off, so that a later
release can still change it (JEP 12). On 21, string templates (JEP 430) were one:

```java
public class Greeting {
    public static void main(String[] args) {
        String user = "Jane Doe";
        int open = 3;
        System.out.println(STR."\{user} has \{open} open orders");
    }
}
```

`javac` needs the flag and a release; `java` needs the flag:

```text
$ javac Greeting.java
Greeting.java:5: error: string templates are a preview feature and are disabled by default.
        System.out.println(STR."\{user} has \{open} open orders");
                               ^
  (use --enable-preview to enable string templates)
1 error
$ javac --enable-preview Greeting.java
error: --enable-preview must be used with either -source or --release
$ javac --enable-preview --release 21 Greeting.java
Note: Greeting.java uses preview features of Java SE 21.
Note: Recompile with -Xlint:preview for details.
$ java Greeting
Error: LinkageError occurred while loading main class Greeting
	java.lang.UnsupportedClassVersionError: Preview features are not enabled for Greeting (class file version 65.65535). Try running with '--enable-preview'
$ java --enable-preview Greeting
Jane Doe has 3 open orders
```

The compiler marks the class file: major version 65 is Java 21 (the release number plus 44), and
minor version 65535 means "uses preview features". The JVM checks that mark when it loads the
class, so the `java` command needs the flag too.

The compiler plugin and the test runner get `--enable-preview`, and the start script doesn't. The build is green, and the service
fails on its first class load.

## A Preview Class File Runs Only on the Release That Compiled It

The same `Greeting.class`, on Java 25 with the flag:

```text
$ java --enable-preview Greeting        # Java 25
Error: LinkageError occurred while loading main class Greeting
	java.lang.UnsupportedClassVersionError: Greeting (class file version 65.65535) was compiled with preview features that are unsupported. This version of the Java Runtime only recognizes preview features for class file version 69.65535
```

An ordinary class compiled for 21 loads on every later JVM. A preview class doesn't, because the
next release may have changed the feature. String templates were withdrawn after Java 22, and Java
25's `javac` reads `\{` as a bad escape:

```text
$ javac --enable-preview --release 25 Greeting.java      # Java 25
Greeting.java:5: error: illegal escape character
        System.out.println(STR."\{user} has \{open} open orders");
                                 ^
```

The compiler applies the same rule. Java 21's `javac` compiles for older releases, but previews
only for its own. `Point` is a record and `Shape` a sealed interface:

```text
$ javac --release 15 --enable-preview Point.java
error: invalid source release 15 with --enable-preview
  (preview language features are only supported for release 21)
$ javac --release 15 Point.java Shape.java
Point.java:1: error: records are not supported in -source 15
  (use -source 16 or higher to enable records)
Shape.java:1: error: sealed classes are not supported in -source 15
  (use -source 17 or higher to enable sealed classes)
```

So code that used records on 15 (JEP 384, their second preview) had to be rebuilt on 16, where
records became standard, and its sealed types (JEP 360) were rebuilt as a preview again on 16
(JEP 397) and once more on 17. The cost lands on whoever shares the code: one internal library
built with `--enable-preview` pins every service that uses it to that release, until the library
is rebuilt without previews. Keep previews out of anything another team deploys.

<!-- practice 1 -->

## Nashorn Is Gone, and the Engine Lookup Returns null Instead of Failing

The discount rules from the opening are JavaScript, evaluated through the scripting API:

```java
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Rules {
    public static void main(String[] args) throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        System.out.println(engine.eval("order.total > 100"));
    }
}
```

Java 15 removed the Nashorn JavaScript engine (JEP 372), which 11 had deprecated for removal. On
21 the JDK ships no script engine. Looking up `"nashorn"` and `"js"`, and counting
`getEngineFactories()`, prints:

```text
nashorn: null
js:      null
engines: 0
```

`getEngineByName` returns `null` for a name it doesn't know; it throws only when the name itself
is `null`. In a service that looks the engine up at startup, the failure moves to the first use:
the first order with a rule. The NPE message names `engine` because the class was
compiled with `-g`; without it the message says `"<local1>" is null` (unit 3.2.2).

Code that used Nashorn's own API fails differently, and the build can hide it. Compiled with
`--release 14`, it only warns:

```java
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

public class NashornApi {
    public static void main(String[] args) throws Exception {
        var engine = new NashornScriptEngineFactory().getScriptEngine();
        System.out.println(engine.eval("6 * 7"));
    }
}
```

```text
$ javac --release 14 NashornApi.java
NashornApi.java:5: warning: [removal] NashornScriptEngineFactory in jdk.nashorn.api.scripting has been deprecated and marked for removal
$ java NashornApi
Exception in thread "main" java.lang.NoClassDefFoundError: jdk/nashorn/api/scripting/NashornScriptEngineFactory
	at NashornApi.main(NashornApi.java:5)
```

`--release 14` checks the code against 14's API, not against the JVM that will run it. With
`--release 15` the same source doesn't compile (`package jdk.nashorn.api.scripting does not
exist`). Build with the `--release` you run on.

The fix has two parts. Put an engine on the class path as a dependency: the standalone OpenJDK
Nashorn and GraalJS both register through `javax.script` (not run here). Code that imported
`jdk.nashorn.api.scripting` also has to change its imports, since the standalone engine has its own
package. Then check the lookup's result when the service starts, so that a missing engine stops
the deploy instead of the first order.

<!-- practice 2 -->

## A Removed JVM Option Is Ignored for a While, Then Stops the JVM From Starting

Java 15 switched biased locking off by default and deprecated its options (JEP 374). Biased
locking made a lock that only one thread ever takes nearly free; JEP 374 argues that modern code
rarely gains from it and that it complicated the JVM. The cost falls on old code that locks
without contention, such as `Vector`, `Hashtable` and `StringBuffer`: each uncontended
`synchronized` pays an atomic instruction again. JDK 18 removed the code.

A HotSpot option leaves in three stages, and Java 21 shows all three:

```text
$ java -XX:+FlightRecorder -version
OpenJDK 64-Bit Server VM warning: Option FlightRecorder was deprecated in version 13.0 and will likely be removed in a future release.
$ java -XX:G1ConcRSHotCardLimit=4 -version
OpenJDK 64-Bit Server VM warning: Ignoring option G1ConcRSHotCardLimit; support was removed in 21.0
$ java -XX:+UseBiasedLocking -version
Unrecognized VM option 'UseBiasedLocking'
Error: Could not create the Java Virtual Machine.
```

A **deprecated** option still works and warns. An **obsolete** one is accepted and ignored, with a
warning, and the JVM starts. An **expired** one is unknown, and the JVM refuses to start. The
option that 21 ignores, 25 refuses:

```text
$ java -XX:G1ConcRSHotCardLimit=4 -version       # Java 25
Unrecognized VM option 'G1ConcRSHotCardLimit=4'
Error: Could not create the Java Virtual Machine.
```

Upgrading one LTS at a time leaves a release of warnings to clean up; jumping from 11 to 21 can
skip them, and the first sign is a crash loop. Before an upgrade, start the new JVM with the
production options and `-version`: it fails the way the pod would, in a second.

<!-- practice 3 -->

## Summary

- **A preview feature must be enabled twice.** `javac --enable-preview --release N` marks the
  class file (minor version 65535), and `java --enable-preview` must accept it.
- **A preview class file runs only on the release that compiled it.** One shared library built
  with previews pins every service that uses it to that release.
- **Nashorn is gone since 15, and `getEngineByName` returns `null`.** Check the engine at
  startup, and build with the `--release` you run on.
- **A removed JVM option is deprecated, then ignored, then refused.** Jumping several releases can
  skip the warning and go straight to a JVM that won't start.

## Reference

**Java 15 (September 2020), by JEP.** Not run here; the unit named covers each.

| JEP | Change | Status in 15 | Unit |
|---|---|---|---|
| 378 | text blocks | standard | 7.4.1 to 7.4.5 |
| 371 | hidden classes | standard | 4.1.1 |
| 372 | Nashorn JavaScript engine | removed | this unit |
| 374 | biased locking | off by default, deprecated | this unit |
| 377, 379 | ZGC, Shenandoah | production collectors | modules 40, 43 |
| 339 | EdDSA signatures | standard | module 44 |
| 375 | pattern matching for `instanceof` | second preview | 1.5.2 |
| 384 | records | second preview | 2.4.1 |
| 360 | sealed classes | first preview | 2.5.1 |

**Class file versions** (ran: `javap -v` and the errors above):

| | Value |
|---|---|
| major version | feature release + 44 (65 is Java 21, 69 is Java 25) |
| minor version | 0, or 65535 when the class uses preview features |
| ordinary class | loads on its release and every later one |
| preview class | loads only on its own release, and only with `--enable-preview` |

## Challenges

1. A team wants a preview feature in one service that only it deploys, and will drop it if the
   feature changes. Which build and run settings would you require, and what would make you say
   no?
2. Write a CI step that fails when a jar meant for other teams contains a preview class file.
   Where in the pipeline does it belong, and what does it scan?
3. An application synchronizes heavily on `StringBuffer` and `Hashtable` and is moving from 11 to
   21. How would you find out whether losing biased locking matters to it, and what would you
   change if it does?

## Design Note: Why a Preview Is Tied to One Release

Java promises that a class file compiled today loads on every later JVM. Previews are the one
exception, and the exception is what lets Java ship features early: if preview class files ran on
later releases, the code built against a preview would freeze its first design. String templates
were withdrawn, and no code built without previews broke.

The cost falls on teams that let a preview leak into shared code. The JVM's refusal is immediate
and clear, but it comes at upgrade time, from a jar nobody remembers building with the flag.

## Code Examples

- Implementation: [Java15Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java15Features.java)
- Tests: [Java15FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java15FeaturesTest.java)
