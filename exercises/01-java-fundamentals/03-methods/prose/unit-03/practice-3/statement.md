A `StringBuilder` is a mutable object: a method that calls `append` or `insert` on a builder
it was passed changes the caller's builder. A method that assigns a new builder to its
parameter changes nothing the caller can see.

Write `wrap(StringBuilder sb, String tag)` in `Tags`. It wraps the caller's builder's
content in an opening and a closing tag, in place, and returns that same builder:

- if `sb` holds `"hi"`, `wrap(sb, "b")` returns `sb`, which now holds `"<b>hi</b>"`;
- wrapping twice nests: `wrap(wrap(sb, "b"), "i")` leaves `"<i><b>hi</b></i>"`;
- an empty builder becomes `"<p></p>"`.
