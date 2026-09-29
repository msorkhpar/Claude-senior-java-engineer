"""The serving API's namespaces, one module each.

**What it does.** Holds the routes `app` dispatches to by the first path segment
after `/api/v1/`: `content` (what a unit *is*), `assets` (the bytes), `state` (what this machine
has, never cached) and `run` (Run and Submit, streamed and recorded). ⛔ No
`quiz`: a quiz is graded in its own page, and nothing about it is a server
function (register ruling, 2026-09-25).

**How you use it.** Each module exposes `route(..., request, rest)` returning a
`serve.response.Response`; `app` binds the first arguments and registers it under
the namespace's name.

**Depends on.** `serve.response`, `serve.caching`, and whatever each namespace
reads. ⛔ A state namespace and a run namespace join here as their
own modules and are registered through `app.make_server(namespaces=...)` — never
folded into content, whose caching rule is the opposite of theirs.
"""
