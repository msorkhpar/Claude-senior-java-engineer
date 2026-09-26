`ObjectInputStream.readObject()` rebuilds whatever object graph the bytes
describe, and it runs each class's own `readObject()` **while** it does so.
Checking the class of the result afterwards is too late: a gadget has already
run. The page's mitigation, if Java serialization cannot be avoided, is a
**serialization filter** (JEP 290) set on the stream before reading. The
filter is asked about **every class in the graph**, nested ones too, and
refuses any class that is not on the allow-list.

Write `SafeReader.read(byte[] data, Set<Class<?>> allowed)`: it returns the
deserialized object when every class in its graph is in `allowed`, and
otherwise throws `InvalidClassException` **without running** the refused
class's code.

| bytes of | allowed | result |
|---|---|---|
| `Note("hello")` | `{Note}` | `Note("hello")` |
| `Gadget` (its `readObject()` has a side effect) | `{Note}` | `InvalidClassException`; the side effect never happens |
| `Envelope(Gadget)` | `{Envelope}` | `InvalidClassException` |
