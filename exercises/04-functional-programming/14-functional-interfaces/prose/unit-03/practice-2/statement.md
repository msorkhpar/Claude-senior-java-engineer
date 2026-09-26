A data-cleaning pipeline is a chain of small, named `Function<String, String>` steps joined with `andThen`; each
step can be tested alone and the whole thing can be handed to `Stream.map`.

Write `Slugs.slugifier()`. It returns a `Function<String, String>` that turns a title into a URL slug, built from
these steps: lower-case the text, remove every character that is not `a-z`, `0-9`, whitespace or `-`, trim, turn
each run of whitespace into one space, and replace each space with `-`. Choose an order that gives the answers
below.

| title | slug |
|---|---|
| `"  Hello, World!  "` | `"hello-world"` |
| `"Java 21 Features!!!"` | `"java-21-features"` |
| `"  My   Blog   Post  "` | `"my-blog-post"` |

Look closely at a title like `"Hello, World !"`. A `null` title has no slug: return `""`. Lower-casing is
`toLowerCase(Locale.ROOT)`, and the kept characters are exactly the ASCII letters `a-z` and digits `0-9` (plus
whitespace and `-` before the last steps): `é` and `_` are removed. Hyphens already in a title are kept as they are;
the tests use no title with a hyphen next to whitespace or at either end.
