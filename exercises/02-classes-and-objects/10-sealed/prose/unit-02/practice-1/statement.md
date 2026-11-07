The event bus writes a type id into every message it sends and reads it back to pick the class
to decode into. A message whose class has no id fails, and it fails in production, at the first
message of that class. So at startup `TypeRegistry.of(Event.class)` registers every class a
message can have at run time, by walking the sealed hierarchy under `Event`.

Last month a team added a class to the hierarchy that the registry never saw, and the first
message of that class was lost. The registry should never start in a state where that can
happen.

Finish `collect(Class<?> type)` in `src/main/java/practice/TypeRegistry.java`. When you're done,
`of(root)` registers exactly the classes a value of `root` can have, and it throws
`IllegalArgumentException`, naming the class, when the hierarchy lets in classes that no list
names. About eight lines in total.

The tests build their own hierarchies, with records, classes and interfaces at more than one
level.
