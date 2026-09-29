r"""An instance: one served root, its corpora discovered, every namespace wired.

**What it does.** `make_instance(root)` runs `discovery.discover`, hands
discovery's report to `log` line by line, and returns `instance_of` the result:
`app`'s bound, not-yet-serving server with the content namespace's source over
every corpus found and the state and run namespaces registered, whose static
mount is the served root itself.

⭐ **Both forms of the verb register their namespaces HERE, in `namespaces_of`,
and nowhere else**: `instance_of` calls it over
every corpus a discovery found, and `cli/serve.py`'s `--site` form calls it over
the one corpus it serves, so a served site answers `state` and `run` as a
served root does. `site_discovery` is the one-corpus discovery the `--site` form
hands it. `run` is the writer namespace — a run's starts and its stop are
`POST` — and `WRITERS` names it, so no caller spells the writers itself. ⛔ No
quiz namespace: a quiz is graded in its own page (register ruling, 2026-09-25).
⚠️ The `serve` verb calls `discover` and `instance_of` and never `make_instance`,
so a namespace registered only in `make_instance` would never be served.
⛔ Every corpus's progress store is refused by `routes.assets` on any path.

**How you use it.**

    server = make_instance(root, port=DEFAULT_PORT, log=print)   # catch `serve.RAISES`
    server.serve_forever()

    discovered = discover(root)                                  # the same, in two halves
    server = instance_of(discovered, port=DEFAULT_PORT, log=print)

    discovered = site_discovery(corpus, root, site)              # one corpus, built elsewhere
    namespaces = namespaces_of(discovered, sources)              # the namespaces alone
    server = make_server(site, content, namespaces=namespaces, writers=WRITERS)

## ⛔ A site built elsewhere is SCANNED where it is built

⭐ **`state` reads the pages a scan finds, and the record's claims are believed
only for a unit whose page is present.** A corpus served from its root is
scanned there; a site `build --out` wrote somewhere else is scanned in that
directory, which is what the static mount serves — so a practice recorded by a
run is believed, and its pages are named, exactly as the root form names them.
⛔ Scanning the corpus root instead would report every page absent and every
record a disagreement. The progress store and the unit documents stay the
corpus root's: `site_discovery` gives its one `ServedCorpus` the corpus root as
`root` and the site as `scan_root`, so no subclass
overrides a scan. ⛔ The startup scan judges no cache, so nothing is written
into the site or the corpus root.

## ⭐ The frame policy is wired here too

⭐ **A published site frames its configured editor from the FIRST response**: the
compose's `STUDYFORGE_EDITOR_ORIGIN` seeds the record `Runs.origins` reads, with no
probe on the request path. The development form keeps discovery (nothing configures
its editor; asking would fork `docker` per request, spec §8.3).

⛔ **A page may embed only the editors THIS instance discovered**, and `frame-src`
is therefore composed at serve time rather than built into a page (R8). The
origins come off the run namespace's own `Runs` — `frames_for(namespaces)`, beside
`client_for(namespaces)` and for the same reason — so one probe answers both the
index and the policy, and a seam registering no run namespace gets `'none'`.

**Depends on.** `serve.app`, `serve.discovery`, `serve.addressing`,
`serve.routes.content`, `serve.routes.state`, `serve.routes.run` and `.runs`, and
`archive.scrub` for the report, and `corpus.discovery`'s startup scan for a site
built elsewhere.

⭐ **This is the seam `studyforge serve` calls**: a root and a
port, and nothing else — no configured paths, no corpus named. ⚠️ The verb takes
the two halves, so it can refuse a corpus that declares pages nobody built before
any socket exists. ⛔ What a namespace answers is decided in its own module; this
one only binds them together.
"""

from __future__ import annotations

from collections.abc import Callable, Mapping
from functools import partial
from pathlib import Path, PurePosixPath

from studyforge.archive.scrub import scrub
from studyforge.corpus.discovery import assemble
from studyforge.corpus.discovery import scan_sha256 as digest_of
from studyforge.generate import Corpus
from studyforge.serve import published as serve_published
from studyforge.serve.addressing import CorporaContent
from studyforge.serve.app import DEFAULT_PORT, Frames, ServingServer, make_server
from studyforge.serve.discovery import Discovered, ServedCorpus, discover
from studyforge.serve.response import Request, Response
from studyforge.serve.routes import run, runs, state
from studyforge.serve.routes.content import CorpusContent

#: The namespaces that also answer `POST`: `run`, whose starts and stop are acts
#: a prefetch must never take. ⛔ A quiz is not among them: it is graded in its
#: own page, and nothing about it is a server's (register ruling, 2026-09-25).
WRITERS = (run.NAMESPACE,)

#: ⭐ Where a served page's execution client is fetched from —
#: `serve.routes.run`'s own spelling, taken and never re-composed. ⛔ Handed to
#: `make_server` by both forms, so the static mount adds ONE script tag to each
#: HTML page it answers and a BUILT page still names no API and no origin (R8).
#: ⚠️ It is passed only where the run namespace is actually registered: a page
#: told to fetch a client from a namespace that is not there would fetch a `404`.
CLIENT = run.CLIENT_PATH


def make_instance(
    root: Path | str,
    port: int = DEFAULT_PORT,
    log: Callable[[str], None] | None = None,
) -> ServingServer:
    """Discover every corpus under `root` and return a server wired to serve them all."""
    discovered = discover(root)
    if log is not None:
        for line in discovered.report:
            log(scrub(line))
    return instance_of(discovered, port=port, log=log)


def instance_of(
    discovered: Discovered,
    port: int = DEFAULT_PORT,
    log: Callable[[str], None] | None = None,
    private: Callable[[Path], bool] | None = None,
    published: serve_published.Published | None = None,
) -> ServingServer:
    """Return a server wired to serve every corpus one discovery found, from its root.

    ⭐ `private` is the static mount's refusal by path, for what the verb decided is
    not served — narration clips a run left out; `None` refuses nothing
    beyond what the mount already refuses. ⭐ `published` is what a published
    compose declared (`serve.published`): the server then binds inside its own
    container and reaches the runner and the editor as declared.
    """
    sources = {served.source: CorpusContent(served.corpus) for served in discovered.corpora}
    namespaces = namespaces_of(discovered, sources, published)
    return make_server(
        discovered.root,
        CorporaContent(sources, discovered.depths),
        port=port,
        namespaces=namespaces,
        log=log,
        writers=WRITERS,
        client=client_for(namespaces),
        frames=frames_for(namespaces),
        published=published is not None,
        **({} if private is None else {"private": private}),
    )


class RunNamespace:
    """The run namespace as a route, carrying where this instance's editors are.

    ⭐ **The frame policy:** its origins must come from the very `Runs` the
    route answers from — one probe, one cache, and a `frame-src` that admits
    exactly the editors the run index published. ⛔ Carrying them ON the
    registered route is what lets `frames_for` read them back out of a mapping a
    REPLACED seam returned (`cli.serve.site_namespaces`), exactly as
    `client_for` reads the client out of the same mapping: a seam that registers
    no run namespace gets no frames, and the policy stays `'none'`.
    """

    def __init__(self, live: runs.Runs) -> None:
        """Hold the instance's runs, and offer their origins as this route's frames."""
        self.live = live
        self.frames: Frames = live.origins

    def __call__(self, request: Request, rest: str) -> Response:
        """Answer one request under the run namespace."""
        return run.route(self.live, request, rest)


def namespaces_of(
    discovered: Discovered,
    sources: dict[str, CorpusContent],
    published: serve_published.Published | None = None,
) -> dict:
    """Return the `state` and `run` namespaces over `discovered`, for either form.

    ⭐ `sources` maps each served corpus's `source` to the content its unit
    documents are read from, which is where a run reads its command.
    """
    seams = (
        {}
        if published is None
        else {
            "runner": serve_published.runner_for(published),
            "editor": serve_published.editor_for(published),
        }
    )
    declared = () if published is None or published.origin is None else (published.origin,)
    return {
        state.NAMESPACE: partial(state.route, discovered),
        run.NAMESPACE: RunNamespace(runs.Runs(discovered, sources, declared=declared, **seams)),
    }


def frames_for(namespaces: Mapping[str, object]) -> Frames | None:
    """Return where this instance's editors are, for the frame policy, or `None`.

    ⛔ **`None` where nothing registered offers them**, which is `frame-src 'none'`:
    a `--site` serve whose seam was replaced to serve a site with NO execution
    has no editor to frame, and a policy that named one anyway would be
    a widening nobody asked for.
    """
    return getattr(namespaces.get(run.NAMESPACE), "frames", None)


def client_for(namespaces: Mapping[str, object]) -> str | None:
    """Return where a served page fetches the execution client, or `None`.

    ⛔ **`None` where the run namespace is not registered**, and that is not
    defensiveness: the `--site` form's namespaces are a named seam a caller may
    replace to serve a site with NO execution, and a page told to fetch
    a client from a namespace that is not there would fetch a `404` on every
    load and offer Run and Submit that answer nothing.
    """
    return CLIENT if run.NAMESPACE in namespaces else None


def site_discovery(corpus: Corpus, root: Path, site: Path) -> Discovered:
    """Return the one-corpus discovery of `corpus` at `root`, served from `site`.

    ⭐ It reports nothing, and its startup scan judges no cache, so nothing is written.
    """
    startup = assemble(site, {corpus.manifest.source: corpus.manifest.depth})
    relative, digest = PurePosixPath("."), digest_of(startup.site)
    served = ServedCorpus(corpus, root, relative, startup, digest, scan_root=site)
    return Discovered(root, (served,), ())
