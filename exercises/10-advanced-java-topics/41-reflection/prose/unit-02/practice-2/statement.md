The billing team's tests use a small helper, `Overrides.apply(target, values)`, to replace fields
of the object under test before they run it. A flaky test led to a bad discovery: when one of
the fields a test names can't be written, the helper fails half-way, after it has already
replaced the fields before it, and the next test in the class runs against an object that is
half overridden.

`apply` already looks the fields up, checks each one before writing any, and then writes them.
Finish the `TODO` in `src/main/java/practice/Overrides.java`:

- `writable(field)`: whether `Field.set` will write this field once it has been made accessible.

When you're done, a call that names a field `Field.set` would refuse fails before anything is
written, and every field it would write is still accepted. About three lines in total.
