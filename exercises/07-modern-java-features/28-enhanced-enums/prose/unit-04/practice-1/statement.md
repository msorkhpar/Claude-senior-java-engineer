The order-search endpoint reads its query string into a map, and every handler gets its
parameters through `QueryParam`, as in `QueryParam.PAGE.get(query, Integer.class)`. The binding
layer that fills a handler's arguments is generic code that passes the result on as an `Object`.
Last week a handler declared its `limit` argument as a `String`. The request passed the binding
layer, and the error turned up two services away, in a report job that read the stored value
back.

Parsing already works. Finish the two `TODO`s in `src/main/java/practice/QueryParam.java`:

- `get(...)`: hand the parsed value back as the type the caller asked for;
- `boxed(...)`: the class whose instances a caller asking for `type` can be given.

When you're done, a caller that asks for a type the parameter doesn't hold gets a
`ClassCastException` from `get` itself, while callers that ask for `int`, `boolean`, or a wider
type such as `Number` get their value. About four lines in total.
