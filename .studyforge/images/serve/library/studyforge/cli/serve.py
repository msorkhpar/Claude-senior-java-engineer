r"""The `serve` verb: the CLI stage that starts what `studyforge.serve` built.

**What it does.** Parses the arguments `studyforge serve` takes, finds every
corpus under the root it is given (`serve.discovery`), checks that each holds
every page it declares, binds `serve.instance` on loopback and serves until it is
stopped. Given `--site`, it serves that one built directory for the one corpus
at the root instead — with state, Run and Submit, as the root form has them.
Serving the bytes is `studyforge.serve`'s; ⛔ this module is its caller and never
a second author of it.

**How you use it.**

    studyforge build <corpus-root> --out <corpus-root>     # built in place, per corpus
    studyforge serve <root> [--port N]                     # every corpus under <root>

    studyforge build <corpus-root> --out <directory>
    studyforge serve <corpus-root> --site <directory> [--port N]

    studyforge serve <root> --no-narration                 # no voice
    studyforge serve /corpus --published --port N          # inside the course's compose

`main(argv) -> int` is the dispatcher's callable. Ctrl-C (or `SIGTERM`) exits `0`.
⚠️ `started=` hands the bound server to a caller, so a test can stop the verb.

**Depends on.** `serve` (discovery, the instance, the app, content),
`generate.declarations`, `progress`, `validate` for the exit codes, and
`argparse`. ⛔ Nothing here knows any source (R1).

## ⭐ `--no-narration` serves the reading floor and edits no page

⭐ Narration is optional: off with `--no-narration` or `narration: false` in
`corpus.json`; `--narration` voices it over that answer. ⛔ The player is in a
built page's bytes, so a site built WITH narration is refused, each page named,
exactly as an unbuilt page is (exit `1`), and every clip under the served root is
refused by path. `cli.unvoiced` holds all three answers and argues the choice.

## ⛔ With no `--site`, NO CONFIGURED PATH

⭐ **The root is the only input**: every `corpus.json` under it is a corpus,
served where it sits by one instance. ⛔ The manifests on disk are the list of
mounts. Tested in `tests/studyforge/cli/test_serve_root.py`.

⚠️ **`--site` stays, as an override for ONE corpus built somewhere else** (`build`
takes `--out` with no default); it adds no mount beside the root's.

## ⛔ `--site` answers state, Run and Submit

⭐ **Both forms take their namespaces from ONE constructor,
`serve.instance.namespaces_of`**, and their writers from its `WRITERS`: a site
that registered `run` but not `state` recorded progress it could not read back.
The site is scanned where it is built (`serve.instance.site_discovery`); ⛔
nothing is written into it. Tested in `tests/studyforge/cli/test_serve_site_run.py`
and `test_serve_site_state.py`. ⭐ `site_namespaces` is the one name a test
replaces to serve a site that offers no execution.

## ⛔ The site is BUILT first, and it never needs this command

⭐ **R8: a built site opens over `file://` with no server**, so this verb serves
what `studyforge build` wrote and writes nothing into it, and `--site` has no
default, as `build`'s `--out` has none. `tests/studyforge/cli/test_serve_floor.py`
asserts the floor rather than assuming it.

⛔ **Exit codes are `build`'s**: `0` stopped cleanly, `1` a declared page nobody
built (each named; nothing bound), `2` the tool could not run — a missing
directory, no servable or readable corpus, a port it cannot listen on.

## ⛔ The Docker socket is never mounted into, or reachable from, this process

Spec §8.3. Not behind a flag, not "only locally": the parser offers no option
naming one, this module imports no Docker client and starts no process, and a
socket the environment points at is never connected to — each asserted, in
`tests/studyforge/cli/test_serve.py` and `test_serve_process.py`. ⭐ `--published`,
the compose's form, reaches the runner over its internal network and is refused
outside a container; ⛔ both forms refuse an `instance.env` value that cannot work.

⚠️ **`private=` names the reader's progress store** by its resolved path, so a
`--site` over the corpus's generated root still cannot serve the record, which
the static mount's prefix check, relative to the site root, would not see.
"""

from __future__ import annotations

import argparse
import os
import signal
import threading
from collections.abc import Callable
from pathlib import Path

from studyforge.archive.scrub import scrub
from studyforge.cli.unvoiced import (
    BUILT_VOICED,
    SERVED_SILENT,
    unvoiced_clips,
    voiced_pages,
)
from studyforge.generate import RAISES
from studyforge.generate.declarations import read_corpus
from studyforge.narrate import narration_on
from studyforge.progress import store_dir
from studyforge.serve import (
    LOOPBACK,
    PUBLISH_REFUSED,
    WRITERS,
    Discovered,
    client_for,
    frames_for,
    instance_refusal,
    namespaces_of,
    prepared,
    site_discovery,
)
from studyforge.serve import RAISES as REFUSED
from studyforge.serve.app import DEFAULT_PORT, ServingServer, make_server
from studyforge.serve.discovery import discover
from studyforge.serve.instance import instance_of
from studyforge.serve.routes.content import CorpusContent
from studyforge.validate.cli import UNUSABLE
from studyforge.validate.report import INVALID, OK

#: What an unbuilt page says. ⭐ It names the command that fixes it.
NOT_BUILT = "the corpus declares this page and the site holds no file there; build it first"

#: What a clean stop says.
STOPPED = "stopped"


def build_parser() -> argparse.ArgumentParser:
    """Return the argument parser, so a test can read the interface."""
    parser = argparse.ArgumentParser(
        prog="studyforge serve",
        description=(
            "Serve every built corpus under a root on loopback, adding the API. "
            "The site still opens without this command; stop it with Ctrl-C."
        ),
    )
    parser.add_argument(
        "root",
        help=(
            "a directory; every corpus.json under it is served where it sits. "
            "With --site, the corpus root — the directory holding corpus.json"
        ),
    )
    parser.add_argument(
        "--site",
        default=None,
        metavar="DIR",
        help=(
            "serve only this corpus, from the directory `studyforge build --out` wrote. "
            "No default: where generated output belongs is the corpus owner's decision"
        ),
    )
    parser.add_argument(
        "--narration",
        action=argparse.BooleanOptionalAction,
        default=None,
        help=(
            "serve the site's narration, or leave it out (--no-narration): no clip is "
            "served and a page built with narration is refused. Default: each "
            "corpus.json's `narration`, which is on when it says nothing"
        ),
    )
    parser.add_argument(
        "--published",
        action="store_true",
        help=(
            "serve from inside the course's compose: every address of this container, "
            "the editor and the run service as the compose declared them. Refused elsewhere"
        ),
    )
    parser.add_argument(
        "--port",
        type=_port,
        default=DEFAULT_PORT,
        help=f"the loopback port to listen on (default {DEFAULT_PORT}; 0 picks a free one)",
    )
    return parser


def main(
    argv: list[str] | None = None,
    out=None,
    *,
    started: Callable[[ServingServer], None] | None = None,
) -> int:
    """Serve one built site until stopped, and return an exit code."""
    import sys

    stream = sys.stdout if out is None else out

    def say(line: str) -> None:
        print(line, file=stream, flush=True)

    arguments = build_parser().parse_args(argv)
    if arguments.published and arguments.site is not None:
        say("--published serves a root, never --site")
        return UNUSABLE
    if arguments.site is None:
        return _serve_root(arguments, say, started)
    root, site = Path(arguments.root), Path(arguments.site)
    for given, path in ((arguments.root, root), (arguments.site, site)):
        if not path.is_dir():
            # ⛔ What was asked for, never the absolute path it resolved to (R7).
            say(f"{given}: not a directory")
            return UNUSABLE
    try:
        corpus = read_corpus(root)
    except RAISES as refusal:
        say(str(refusal))
        return UNUSABLE
    if refused := instance_refusal(root):  # ⭐ the publisher's instance.env, by key
        say(refused)
        return UNUSABLE
    unbuilt = _unbuilt(corpus, site)
    if unbuilt:
        for page in unbuilt:
            say(f"unbuilt {page}  {NOT_BUILT}")
        return INVALID
    silent = not narration_on(root, asked=arguments.narration)
    loud = voiced_pages(corpus, site) if silent else []
    if loud:
        for page in loud:
            say(f"narrated {page}  {BUILT_VOICED}")
        return INVALID
    store = _inside(store_dir(root))
    clips = unvoiced_clips([site] if silent else [])
    content = CorpusContent(corpus)
    namespaces = site_namespaces(site_discovery(corpus, root, site), content)
    try:
        server = make_server(
            site,
            content,
            port=arguments.port,
            namespaces=namespaces,
            private=lambda path: store(path) or clips(path),
            log=say,
            writers=tuple(name for name in WRITERS if name in namespaces),
            client=client_for(namespaces),
            frames=frames_for(namespaces),
        )
    except OSError as refusal:
        return _could_not_listen(arguments.port, refusal, say)
    host, port = server.server_address[:2]
    say(f"serve http://{host}:{port}/  site {arguments.site}  corpus {arguments.root}")
    if silent:
        say(f"narration off  {SERVED_SILENT}")
    return _serve(server, say, started)


def _serve_root(
    arguments: argparse.Namespace,
    say: Callable[[str], None],
    started: Callable[[ServingServer], None] | None,
) -> int:
    """Serve every corpus discovered under the root, with no configured path."""
    if not Path(arguments.root).is_dir():
        say(f"{arguments.root}: not a directory")
        return UNUSABLE
    try:
        discovered = discover(arguments.root)
    except REFUSED as refusal:
        say(str(refusal))
        return UNUSABLE
    report = [scrub(line) for line in discovered.report]
    unbuilt = sorted(
        (served.relative / page).as_posix()
        for served in discovered.corpora
        for page in _unbuilt(served.corpus, served.root)
    )
    silent = [
        served
        for served in discovered.corpora
        if not narration_on(served.root, asked=arguments.narration)
    ]
    loud = sorted(
        (served.relative / page).as_posix()
        for served in silent
        for page in voiced_pages(served.corpus, served.root)
    )
    if unbuilt or loud:
        for line in (
            *report,
            *(f"unbuilt {page}  {NOT_BUILT}" for page in unbuilt),
            *(f"narrated {page}  {BUILT_VOICED}" for page in loud),
        ):
            say(line)
        return INVALID
    clips = unvoiced_clips(served.root for served in silent)
    try:
        config = prepared(os.environ, discovered.corpora, arguments.published)
    except PUBLISH_REFUSED as refusal:
        say(str(refusal))
        return UNUSABLE
    try:
        server = instance_of(
            discovered, port=arguments.port, log=say, private=clips, published=config
        )
    except OSError as refusal:
        return _could_not_listen(arguments.port, refusal, say)
    host, port = server.server_address[:2]
    host = host if config is None else LOOPBACK  # ⭐ the compose publishes it there alone
    # ⭐ The listening line is FIRST, as in the `--site` form: a caller reads the port off it.
    say(f"serve http://{host}:{port}/  root {arguments.root}")
    for served in discovered.corpora:
        index = served.href(served.corpus.shared.root_index)
        say(f"corpus {served.source} http://{host}:{port}{index}")
    for served in silent:
        say(f"narration off  corpus {served.source}: {SERVED_SILENT}")
    for line in report:
        say(line)
    return _serve(server, say, started)


def _could_not_listen(port: int, refusal: OSError, say: Callable[[str], None]) -> int:
    """Report a port the server could not bind, and return `2`."""
    # ⚠️ `strerror` only: an `OSError`'s full text can carry a path (R7).
    reason = refusal.strerror or type(refusal).__name__
    say(f"port {port}: could not listen ({reason})")
    return UNUSABLE


def _port(text: str) -> int:
    """Parse a TCP port, refusing one out of range here rather than at `bind`."""
    try:
        value = int(text)
    except ValueError:
        raise argparse.ArgumentTypeError(f"not a port: {text}") from None
    if not 0 <= value <= 65535:
        raise argparse.ArgumentTypeError(f"not a port: {text}")
    return value


def _unbuilt(corpus: object, site: Path) -> list[str]:
    """Every page the corpus declares that the site holds no file for, sorted.

    ⭐ **The plan's own enumeration**: the footprint's `.html` files are exactly
    the pages a build writes (`generate/site.py`), and the root index is named
    beside them so a footprint that owns nothing still checks the one page a
    reader opens first.
    """
    pages = {corpus.shared.root_index} | {
        path for path in corpus.footprint.files if path.suffix == ".html"
    }
    return sorted(str(page) for page in pages if not (site / page).is_file())


def site_namespaces(discovered: Discovered, content: CorpusContent) -> dict:
    """Return the `--site` form's namespaces: the root form's constructor, over one corpus.

    ⭐ `serve.instance.namespaces_of` builds them in both forms — `state`, and
    `run` with the progress it records — and `WRITERS` names which of them write.
    ⭐ **This is the verb's one NAMED SEAM for what a `--site` serve registers**:
    the build-and-serve skill's test replaces it to serve
    a site with no execution, and the verb registers as writers only the members of
    `WRITERS` it returned.
    """
    return namespaces_of(discovered, {served.source: content for served in discovered.corpora})


def _inside(directory: Path) -> Callable[[Path], bool]:
    """Return a `private=` predicate: whether a resolved path is inside `directory`."""
    base = directory.resolve()
    return lambda path: Path(path).resolve().is_relative_to(base)


def _serve(
    server: ServingServer,
    say: Callable[[str], None],
    started: Callable[[ServingServer], None] | None,
) -> int:
    """Serve until shut down or interrupted; close the socket on every way out."""
    restore = _stop_on_terminate()
    try:
        if started is not None:
            started(server)
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        restore()
        server.server_close()
    say(STOPPED)
    return OK


def _stop_on_terminate() -> Callable[[], None]:
    """Make `SIGTERM` stop the server as Ctrl-C does; return the undo.

    ⚠️ Only the main thread may install a handler, so a verb run from a test's
    thread keeps the process's own and is stopped through `started=` instead.
    """
    if threading.current_thread() is not threading.main_thread():
        return lambda: None
    previous = signal.getsignal(signal.SIGTERM)

    def interrupt(signum: int, frame: object) -> None:
        raise KeyboardInterrupt

    signal.signal(signal.SIGTERM, interrupt)
    return lambda: signal.signal(signal.SIGTERM, previous)
