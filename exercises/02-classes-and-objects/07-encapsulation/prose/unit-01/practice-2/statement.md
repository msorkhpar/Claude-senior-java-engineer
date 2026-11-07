The support team's diagnostics page shows the state of an order, a session or a cart: every
field next to its value. `FieldReader.read(object)` builds that table and already works out the
names; it needs two helpers in `src/main/java/practice/FieldReader.java`:

- `instanceFields(type)`: the fields an object of that type carries. Support wants the whole
  state, not only the parts a class chose to expose, and static fields are not state;
- `value(field, object)`: the field's value in that object.

Support also opens objects whose class comes from the JDK, such as the plain `ArrayList` behind a
cart. For those the page must still render, with `FieldReader.HIDDEN` in place of each value the
JVM won't let the reader see. About ten lines in total.
