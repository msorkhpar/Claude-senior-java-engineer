"""Pages: the unit renderer, the container renderer, the root index, templates and assets.

**What it does.** Turns unit documents and the table of contents into the files
a reader opens — a page per unit, a page per container, and a root index — and
owns the templates, stylesheets and scripts they load.

**How you use it.** Give the renderer a unit document and a placement decision;
it returns bytes. Every page it writes carries its own address inside it, so a
scanner can identify it wherever it ends up (R4), and every asset it references
is addressed **relative to the page**, so the result opens over `file://` with
no server (R8).

⛔ **What more than one renderer needs is a SIBLING package, never a name on one
of them**. `markup` holds the escaping routine and the permitted-href
gate; `pageassets` holds the class names and the two shared asset filenames.
⚠️ Both are reached through their own `__all__`, so `page`, `container` and
`index` share them without any of the three owning the others.

**Depends on.** `unit`, `contents`, `address`. ⛔ Not on `serve`: a page that
needs the server to render is a page that fails the `file://` floor, and that
floor is the product's baseline rather than a fallback.

⛔ **Markup, styling and scripts are source files, never code strings** (R13).
Templates live in `templates/*.html` and assets in `assets/*.{css,js}`, loaded
and composed by code — the inherited debt being 80 KB of triple-quoted strings
where changing a colour meant editing Python. Loop bodies and inline wrappers
stay in code; a template file for a closing tag removes no duplication.

⛔ **A template is used exactly, minus one trailing newline** — no reflow, no
re-indent, no whitespace collapse — because pages are compared byte for byte
(R10). Substitution **fails** on an unfilled placeholder; it never reaches the
page as a literal.

⛔ **A shared asset linked by every page carries a plain name**, never a
content digest: a digest there renames a file and rewrites every page that
links it whenever a colour changes.
"""
