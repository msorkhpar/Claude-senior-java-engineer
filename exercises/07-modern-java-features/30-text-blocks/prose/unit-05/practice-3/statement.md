HTML templates are one of the page's main use cases, and its security answer
shows the catch: `formatted()` puts text in as it is, so `<script>` from a
user becomes a real script tag. Its safe version escapes the input first.

Write `Html.card(String title, String body)`. It returns these four lines
joined by `\n`, with no newline after the last:

```text
<div class="card">
    <h1><title></h1>
    <p><body></p>
</div>
```

Both values are escaped before they go in: `&` becomes `&amp;`, `<` becomes
`&lt;`, `>` becomes `&gt;` and `"` becomes `&quot;`. Escape `&` **first**, or
the `&` of an escape made earlier would be escaped again. Every `&` is escaped, even one that already
starts something like `&lt;`: the input is text, not HTML. A single quote is
left as it is.

| title, body | answer |
|---|---|
| `Welcome`, `Hello!` | `<div class="card">` / `    <h1>Welcome</h1>` / `    <p>Hello!</p>` / `</div>` |
| `<b>News</b>`, `ok` | the title line is `    <h1>&lt;b&gt;News&lt;/b&gt;</h1>` |
| `Hi`, `Tom & Jerry <3` | the body line is `    <p>Tom &amp; Jerry &lt;3</p>` |
| `Hi`, `&lt;` | the body line is `    <p>&amp;lt;</p>` |
| `Hi`, `Say "hi"` | the body line is `    <p>Say &quot;hi&quot;</p>` |

(`/` separates the lines here.)
