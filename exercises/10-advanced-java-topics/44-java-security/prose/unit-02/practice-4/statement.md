The shop keeps each visitor's cart in a cookie: the `Cart`, written with Java serialization by
`CartCookie.write`. The browser sends the cookie back with every request, so its bytes are
whatever the visitor, or anyone who can change their traffic, chooses to send. The shop's class
path holds hundreds of `Serializable` classes from its own code and its libraries, and some of
them start work when they are rebuilt.

Write `read(cookie)` in `src/main/java/practice/CartCookie.java`. A cookie that holds a cart
comes back as that cart. A cookie that holds anything else, at the top or anywhere inside the
cart, is refused with an `IOException`, and no code of any other class runs while it is read.
Serializing a cart writes a `Cart`, the `ArrayList` of its lines, that list's `Object[]`, and
each `CartLine`.

About six lines in total.
