The support team's `/debug/dump` endpoint shows the instance fields of whatever object an engineer
points it at. Since the service moved to Java 17, pointing it at a JDK object, such as the
`ArrayBlockingQueue` behind the job scheduler, makes the endpoint answer 500, and the
engineer gets nothing at all.

`FieldDump.dump(...)` already walks the fields. Finish the `TODO` in
`src/main/java/practice/FieldDump.java`: `read(field, target)` returns the field's value as text,
or `FieldDump.HIDDEN` when the JVM won't let the dump read it.

When you're done, the service's own objects show every field, private ones included; a JDK object
shows whatever the JVM lets it read and `<hidden>` for the rest; and no object makes the dump
fail. The tests run without any `--add-opens` option. About five lines.
