"""The local HTTP surface: app wiring, content, state, assets and run routes, security, caching.

**What it does.** Serves a built corpus over a local origin, adding what
`file://` cannot do — the API, the reader's progress, and Run/Submit — to a site
that already works without it (R8).

**How you use it.** `studyforge serve <root> [--port N]`, which calls
`instance`, or `instance.make_instance(root)` — each given a root and nothing
else. The endpoint shapes are the contract, and the reading page is a consumer
of them: a second source that populates the same API gets the same site out.

**Depends on.** `contents`, `unit`, `corpus`, `generate` (its declarations reader only),
`progress`, `execute`. ⛔ Not on
`render` at request time — pages are built, not rendered per request.

⚠️ **Two namespaces with opposite caching rules.** Content is what a unit *is*:
cacheable, with strong validators and conditional requests. State is what the
reader has done: derived from the filesystem on every request, never cached,
because it changes underneath the page.

⛔ **The Docker socket is never mounted into this process** (spec §8.3). Not
behind a flag, not "only locally". Execution reaches the toolchain container
from outside; the web-facing process is never the thing holding
root-equivalent access to the host. This is the reading of R15 that R15 itself
rules out.

⚠️ **A package precisely because it replaces a 2,743-line file** (R11). The
extraction pays that debt during the port, not after: it arrives as focused
modules or it does not arrive.

**The static half**: `app` (the loopback server and its seams), `security`,
`caching`, `response`, `routes.content` and `routes.assets`. **Then**:
`discovery` (a root in, every corpus under it found, no configured paths),
`addressing` (N-segment unit addresses at each corpus's own depth), `routes.state`
(never cached, derived from the filesystem on every request) and `instance`
(`make_instance(root, port, log)`, the seam `studyforge serve` calls). **And**:
`routes.run` (Run and Submit), registered in `instance.instance_of` as the one
namespace `app` answers `POST` under, and `app`'s streamed response.

⛔ **Only the run namespace's modules and `published` import `execute`** — the
runner, which starts every process; no module of this package starts one or imports
a library that does (asserted in `tests/studyforge/serve/test_init.py`).
⭐ `published` is the form a course's compose runs: its runner reaches the runner
container's run service over the compose network, never the Docker socket.

⚠️ `discovery` reads a corpus through `generate`'s `read_corpus`, the one reader of
a corpus's declarations, which is why `generate` is named above.
"""

from __future__ import annotations

from studyforge.serve.discovery import RAISES, Discovered, ServedCorpus
from studyforge.serve.instance import (
    CLIENT,
    WRITERS,
    client_for,
    frames_for,
    namespaces_of,
    site_discovery,
)
from studyforge.serve.published import REFUSED as PUBLISH_REFUSED
from studyforge.serve.published import instance_refusal, prepared, start_published
from studyforge.serve.security import LOOPBACK

#: ⛔ What `discovery.discover` and `instance.make_instance` let out.
#: ⭐ `Discovered` and `ServedCorpus` are shared with `cli.serve`, whose `--site` form
#: serves one corpus with Run and Submit (one exported home, R21).
#: ⭐ `WRITERS`, `namespaces_of` and `site_discovery` are shared with it too: both forms
#: take their namespaces from the one constructor (the same remedy).
#: ⭐ `CLIENT` and `client_for` are shared for the same reason: both forms hand the static
#: mount the one path the execution client is served at, so a served page gets it
#: and a built page still names nothing (R8).
#: ⭐ `start_published`, `PUBLISH_REFUSED` and `LOOPBACK` are shared with its
#: `--published` form: the compose's config, its refusals, and the address it opens;
#: `prepared` and `instance_refusal` check the publisher's `instance.env` in every form.
__all__ = [
    "CLIENT",
    "LOOPBACK",
    "PUBLISH_REFUSED",
    "RAISES",
    "WRITERS",
    "Discovered",
    "ServedCorpus",
    "client_for",
    "frames_for",
    "namespaces_of",
    "instance_refusal",
    "prepared",
    "site_discovery",
    "start_published",
]
