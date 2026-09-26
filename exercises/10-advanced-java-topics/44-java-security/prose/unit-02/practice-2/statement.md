XSS happens when user text reaches a page as markup. The page's fix is
**output encoding**: when a comment is rendered, replace `&`, `<`, `>`, `"`
and `'` with `&amp;`, `&lt;`, `&gt;`, `&quot;` and `&#x27;`. It stores the
original text and encodes at the point of rendering, so **every `&` is
encoded**, even one the user typed as part of something that looks like an
entity: the reader must see exactly what was typed.

Write `CommentRenderer`:

- `encode(text)`: the encoded text; `null` gives `""`.
- `render(comment)`: `"<div class='comment'>" + encode(comment) + "</div>"`.
  The attribute is single-quoted, so **a single quote must be encoded too**.

| call | result |
|---|---|
| `encode("<script>alert(\"x\")</script>")` | `&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;` |
| `encode("Tom & Jerry")` | `Tom &amp; Jerry` |
| `encode("use &lt; for <")` | `use &amp;lt; for &lt;` |
| `render("it's")` | `<div class='comment'>it&#x27;s</div>` |
| `encode(null)` | `""` |
