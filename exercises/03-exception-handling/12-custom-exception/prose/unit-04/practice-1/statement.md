Customers build their own services against the payments SDK. Last quarter a patch release made
one customer's build fail at forty call sites, and the team only heard about it from the
customer. Now CI compares every public method of the release candidate with the same method in
the last release, and fails the release when the new `throws` clause would stop code that
compiled against the last release from compiling.

`ThrowsDiff.breaking(last, next)` already walks the candidate's throws clause and collects what
it finds. Finish the two `TODO`s in `src/main/java/practice/ThrowsDiff.java`:

- `isChecked(type)`: whether a caller must catch or declare an exception of that type;
- `handledBy(type, declared)`: whether code written against a method that declared `declared`
  already copes with that type.

When you're done, CI fails a release exactly when a customer's build would, and never for a
change their compiler accepts. About six lines in total.

The tests describe two versions of an SDK interface and compare their methods through
reflection; `Class.isAssignableFrom` answers "is this the same type, or a supertype of that one?".
