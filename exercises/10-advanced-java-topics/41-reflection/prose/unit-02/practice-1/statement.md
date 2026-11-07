The shipping service has a debug endpoint that prints whatever object a request handler holds, for
support engineers chasing a bad order. It writes the service's own objects field by field, private
and inherited fields included, which is what support needs. Since the service moved to Java 21,
the endpoint answers 500 for most orders: the objects hold JDK collections, charsets and other
library objects, and the service runs without any `--add-opens`.

`Snapshot.render(...)` already writes text, numbers and enum constants as they are, sorts the fields
and recurses. Finish the `TODO` in `src/main/java/practice/Snapshot.java`:

- `openFields(type)`: the instance fields of `type` and of all its superclasses, each made readable,
  or empty when the object should be written through its own `toString()` instead.

When you're done, an object of the service's own classes is still written field by field, and
any object whose fields can't all be read is written whole through its `toString()`, without an
exception and without leaving fields out. About eight lines in total.
