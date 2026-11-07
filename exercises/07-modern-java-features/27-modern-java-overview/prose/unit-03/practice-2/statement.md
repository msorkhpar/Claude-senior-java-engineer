The shop keeps its discount rules as small scripts, such as `total > 480`, so that the marketing
team can change them without a release. The rules used to run on the JDK's own JavaScript engine.
After the move to a new Java release, the service started cleanly and then failed on the first
order that had a discount, with a `NullPointerException`.

The team has added a script engine as a dependency, and the configuration now lists the engine
names to try, in order of preference. Finish `pickEngine(...)` in
`src/main/java/practice/DiscountRules.java` so that rules run on the first configured engine that
is installed, and so that a service with none of them installed fails while it starts, with
`DiscountRules.NoScriptEngine` and a message that names the engines it looked for. About five
lines.

The tests register their own small engines under names such as `graal.js` and `rhino`.
