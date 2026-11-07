The shop's web sessions live in a session store: after each request the store serializes the
`Session`, and the next request, possibly on another node after a rolling restart, reads it back.
The session's attributes used to be an enum. They are now `Session.Attribute<T>`, so that
`session.get(USER_ID)` returns a `Long` and `session.get(LOCALE)` a `Locale` without a cast.

Since that change shipped, every deploy logs users out and empties their carts, and two older
modules that compare attributes with `==` and look them up with `valueOf` misbehave.

Finish the three `TODO`s in `src/main/java/practice/Session.java`:

- `values()`: every attribute, in declaration order;
- `valueOf(String)`: the attribute with a given name;
- `readResolve()`: what a deserialized attribute becomes.

When you're done, a session read back after a restart holds everything it held before, an
attribute read back from a stream is the declared attribute itself, `valueOf` treats an unknown
name the way the old enum's did, and no caller can change which attributes exist through the list
`values()` hands out. About eight lines in total.
