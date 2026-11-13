r"""A course's read-only preview: its learner tree as static pages for GitHub Pages.

**What it does.** From the learner tree `release` wrote, writes into an empty directory a
tree of static pages a plain web server can serve: the index, every page reachable
from it, the shipped stylesheets and scripts, the search's index, and the preview's own two
files. Reading, the search, the quizzes and mock exams (which grade inside the page), the
reading marks, the language modes and the index filter work as they do served. ⭐ Every
place that needs the local server (Run and Submit, the editor, a code example opened in the
editor or run beside the lesson, narration) is REMOVED and a note stands in its place,
saying it is available when the course runs locally with Docker.

**How you use it.**

    previewed = preview(tree, out)            # out: a new or empty directory
    previewed.pages, previewed.files          # what was written, relative to `out`

    python -m studyforge.skills.execution.standalone.preview TREE OUT
    python3 .github/preview/preview.py TREE OUT       # the copy in a learner repository

The first half of the module is the preview's words, script and stylesheet and the edits
of one built page (`remove_server_parts`, then `finish`), the second is the walk over the tree.

**Depends on.** `re`, `html`, `posixpath`, `urllib` and `pathlib` only. ⛔ It starts no
process, reads nothing outside `tree`, writes only into `out` and names no account, host
or repository.

## ⛔ The Python standard library and nothing else, in ONE file

⭐ This file is copied, byte for byte, into a learner repository's `.github/preview/`,
where a GitHub-hosted runner builds the preview on every push with plain `python3`. So it
imports nothing of this project, and it needs no sibling file: what `tour` and the README
need of it (the anchor of the README's section, below) is defined HERE. ⛔ There is ONE
source; the copy is proved identical.

Size exception: this file is one file by design, since a learner repository's runner runs it
with plain `python3` and no sibling; split, the copy would be several files and no longer one.

## ⛔ The preview is made from the built page, and the repository is never written down

⭐ Every edit is a deterministic text edit of a page the site already built; the shipped
`page.js` and `page.css` are copied as they are. ⛔ A server-only part is REMOVED, not hidden,
and a note stands in its place. ⛔ No page holds an owner or a repository name: `PREVIEW_JS`
builds the links at run time from `location` on a `<owner>.github.io/<repo>/` page, and
builds nothing anywhere else.

## ⛔ No directory starts with a dot

⭐ The learner tree keeps its pages and assets under `.studyforge/`, and the action that
packs the Pages artifact leaves out every path that starts with a dot. ⭐ So that
directory is written as `course/` and EVERY reference to it is recomputed from where
the page and its target now sit (never a text replacement); a path with any other
dot-component is refused. No `.nojekyll` is written: a deployment from an Actions
artifact does not run Jekyll.

## ⛔ Only what the index reaches is written

⭐ Pages are found by following links from `index.html`, so nothing no page shows is copied.
⛔ A link to a file that is not in the tree, or that leaves it, is refused by name.

⭐ The search is the one exception, because `page.js` loads its files by script, not by a
link: where a copied `page.js` has a `search-index.js` beside it, that index, the ranking
library and every piece the index names are copied too, a page the index names that the walk
did not reach is copied as a page when the tree holds it, and refused by name when it does not.
"""

from __future__ import annotations

import ast
import posixpath
import re
import sys
from collections.abc import Sequence
from dataclasses import dataclass
from html import escape
from pathlib import Path
from urllib.parse import quote, unquote, urlsplit, urlunsplit

#: ⭐ The README section every note points to, as GitHub spells its anchor. `tour` states the
#: heading it comes from, and its test proves the two agree.
RUN_ANCHOR = "run-it-locally-with-docker"

#: The attribute that marks a link the script points at the README's section.
RUN_LINK = "data-preview-run"

#: The attribute that marks a source link the script points at the source viewer.
SOURCE_LINK = "data-code-path"

#: ⭐ An empty icon, so a browser does not ask a static host for a `favicon.ico` it does not have.
NO_ICON = "data:,"

#: The words of the banner every page carries.
BANNER = (
    "<strong>Read-only preview.</strong> Reading, the quizzes, your reading marks and the "
    "index filter work here. Running and submitting practices, the editor, code examples "
    "that open in the editor and narration need the course running "
    f"<a {RUN_LINK}>locally with Docker</a>."
)

#: The note that stands where a practice's Run, Submit and editor were.
PRACTICE_NOTE = (
    "Run and Submit, and the editor to write your answer in, are available when the "
    f"course runs locally with Docker: see <a {RUN_LINK}>Run it locally with Docker</a>. "
    "This preview shows the statement only."
)

#: The note that stands where a code example's editor and Run were.
EXAMPLE_NOTE = (
    "Opening this example in the editor beside the lesson, and running its test, are "
    f"available when the course runs locally with Docker: see <a {RUN_LINK}>Run it locally "
    "with Docker</a>. The links above open the files in the repository's source viewer."
)

#: The note that stands where the Run beside a code example's tab was.
EXAMPLE_RUN_NOTE = (
    "Running this example's test is available when the course runs locally with Docker: "
    f"see <a {RUN_LINK}>Run it locally with Docker</a>."
)

#: The note that stands where the narration player was.
NARRATION_NOTE = (
    "Narration is available when the course runs locally with Docker: see "
    f"<a {RUN_LINK}>Run it locally with Docker</a>."
)

#: ⭐ The 2026-11 redesign: the banner is no longer a strip above every page. It is a
#: "Run locally" disclosure in the top bar, open on request, holding the same words and the
#: three commands. ⛔ A `<details>`, not a scripted dialog, so it opens with scripting off.
RUN_LOCALLY = (
    '<details data-preview-run-locally><summary>'
    '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" '
    'stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" '
    'focusable="false"><path d="M 4 17 l 6 -5 l -6 -5"></path><path d="M 12 19 h 8"></path></svg>'
    "<span>Run locally</span></summary>"
    '<div role="note"><p>{banner}</p>'
    "<pre data-preview-commands><code>git clone <span data-preview-repo>&lt;this course's "
    "repository&gt;</span>\ncd <span data-preview-dir>&lt;its folder&gt;</span>\n"
    "docker compose up</code></pre></div></details>"
)

#: Where the disclosure goes: in the top bar, just before search (after the brand and sections).
_SEARCH_OPEN = re.compile(r'<button type="button" data-section="search-open"')

PREVIEW_CSS = """\
/* The read-only preview: "Run locally" in the top bar and a note where a server-only part was.
   ⭐ The 2026-11 redesign moved the banner into the bar (`[data-preview-run-locally]`); the
   strip below is what a page from before the redesign, with no top bar, still gets. */
[data-preview-banner] {
  padding: 0.6rem var(--gutter, 1rem);
  background: var(--surface-2, #e8eef7);
  color: var(--fg, #14213d);
  border-bottom: 1px solid var(--rule-strong, #94a3b8);
  font: 0.9rem/1.45 var(--font-ui, system-ui, sans-serif);
  text-align: center;
}
[data-preview-banner] p { margin: 0; }
[data-preview-note] {
  margin: 0.75rem 0;
  padding: 0.6rem 0.8rem;
  border-left: 4px solid var(--accent, #2563eb);
  background: var(--surface-2, #e8eef7);
  color: var(--fg, #14213d);
  font: 0.95rem/1.5 var(--font-ui, system-ui, sans-serif);
}
[data-preview-banner] a, [data-preview-note] a { color: inherit; text-decoration: underline; }
[data-preview-note] { border-left: 0; border: 1px solid var(--rule, #dbe3ec); border-radius: 12px;
  background: var(--surface, #ffffff); }
details[data-preview-run-locally] { position: relative; order: 0; margin-left: auto; font-family: var(--font-ui, sans-serif); }
details[data-preview-run-locally] + button[data-section="search-open"] { margin-left: .5rem; }
details[data-preview-run-locally] > summary {
  display: inline-flex; align-items: center; gap: .4rem; list-style: none; cursor: pointer;
  padding: .35rem .7rem; font-size: .85rem; font-weight: 600; white-space: nowrap;
  color: var(--accent, #a64d07); border: 1px solid var(--rule, #dbe3ec); border-radius: 8px;
  background: var(--surface, #ffffff);
}
details[data-preview-run-locally] > summary::-webkit-details-marker { display: none; }
details[data-preview-run-locally] > div {
  position: absolute; right: 0; top: calc(100% + .5rem); z-index: 5; width: min(28rem, 90vw);
  box-sizing: border-box; padding: 1rem 1.1rem; font-size: .9rem; line-height: 1.5;
  color: var(--fg, #22304a); background: var(--surface, #ffffff);
  border: 1px solid var(--rule, #dbe3ec); border-radius: 12px; box-shadow: 0 8px 24px rgba(15, 23, 42, .14);
}
details[data-preview-run-locally] > div p { margin: 0 0 .75rem; }
details[data-preview-run-locally] a { color: inherit; text-decoration: underline; }
details[data-preview-run-locally] pre {
  margin: 0; padding: .7rem .85rem; overflow-x: auto; border-radius: 8px;
  font: .8rem/1.6 var(--font-mono, monospace); font-variant-ligatures: none;
  color: var(--term-fg, #e2e8f0); background: var(--term-bg, #0f172a);
}
@media (max-width: 40rem) { details[data-preview-run-locally] > summary span { display: none; } }
"""

PREVIEW_JS = """\
/* The read-only preview: the links that need the repository, built from where this page is served.

   A project page is served at https://<owner>.github.io/<repo>/, so the owner is the host's first
   label and the repository is the path's first segment. Anywhere else nothing is built and the
   text stays as it is, without a link. Nothing here makes a request. */
(function () {
  'use strict';

  var LABEL = /^[A-Za-z0-9][A-Za-z0-9._-]*$/;
  var HASH = String.fromCharCode(35);
  var ANCHOR = '@ANCHOR@';
  var RUN = 'a[data-preview-run]';
  var SOURCE = 'a[data-code-path]';

  function repository(hostname, pathname) {
    var labels = String(hostname || '').toLowerCase().split('.');
    if (labels.length !== 3 || labels[1] !== 'github' || labels[2] !== 'io') { return null; }
    var first = String(pathname || '').split('/').filter(Boolean)[0];
    if (!labels[0] || !first || /\\.html?$/i.test(first)) { return null; }
    if (!LABEL.test(labels[0]) || !LABEL.test(first)) { return null; }
    return 'https://github.com/' + labels[0] + '/' + first;
  }

  function plain(link) {
    link.replaceWith(document.createTextNode(link.textContent));
  }

  window.studyforgePreview = { repository: repository };
  var base = repository(window.location.hostname, window.location.pathname);
  if (base) {
    [].slice.call(document.querySelectorAll('[data-preview-repo]')).forEach(function (span) {
      span.textContent = base + '.git';
    });
    [].slice.call(document.querySelectorAll('[data-preview-dir]')).forEach(function (span) {
      span.textContent = base.split('/').pop();
    });
  }
  [].slice.call(document.querySelectorAll(RUN)).forEach(function (link) {
    if (!base) { plain(link); return; }
    link.href = base + '/blob/main/README.md' + HASH + ANCHOR;
  });
  [].slice.call(document.querySelectorAll(SOURCE)).forEach(function (link) {
    var path = link.getAttribute('data-code-path') || '';
    if (!base || !path) { plain(link); return; }
    link.href = base + '/blob/main/' + path.split('/').map(encodeURIComponent).join('/');
  });
}());
""".replace("@ANCHOR@", RUN_ANCHOR)

_PLAYER = re.compile(r'<footer id="player"[^>]*>.*?</footer>\s*', re.DOTALL)
_NARRATOR = re.compile(r'<audio id="narrator"[^>]*></audio>\s*')
_AUDIO = re.compile(r' data-audio="[^"]*"')
_PRACTICE = re.compile(r'(<section data-practice="[^"]*"[^>]*>)(.*?)(</section>)', re.DOTALL)
_EXAMPLE = re.compile(
    r"(<details data-code-example[^>]*>)(.*?)(</details>)",
    re.DOTALL,
)
_EXAMPLE_RUN = re.compile(
    r'<p data-example-run="[^"]*"[^>]*>.*?</p>(\s*<pre data-example-part="output"[^>]*></pre>)?',
    re.DOTALL,
)
#: ⭐ The stylesheet and script of a Run beside a code example: with the strips gone they
#: serve nothing.
_EXAMPLE_RUN_ASSET = re.compile(
    r'<link\b[^>]*\bhref="(?:[^"]*/)?example-run\.css"[^>]*>\s*'
    r'|<script\b[^>]*\bsrc="(?:[^"]*/)?example-run\.js"[^>]*></script>\s*'
)
_SUMMARY = re.compile(r"<summary>.*?</summary>", re.DOTALL)
_LIST = re.compile(r'<ul class="items">.*?</ul>', re.DOTALL)
_SOURCE_TAG = re.compile(r"<a\b[^>]*\bdata-code-path=[^>]*>")
_HREF = re.compile(r' href="[^"]*"')
#: ⭐ READS the skip link the built pages open with; the character class spells its fragment mark
#: because the composer of anchors is not importable here.
_BODY = re.compile(r'<body[^>]*>(\s*<a href="[#]content">[^<]*</a>)?')
_HEAD_END = "</head>"
_BODY_END = "</body>"
_HEADER_END = "</header>"


class PageRefused(ValueError):
    """A page whose shape this module will not edit, and why."""


def _note(kind: str, words: str) -> str:
    return f'<p data-preview-note="{kind}">{words}</p>'


def _practice(found: re.Match[str]) -> str:
    if "<section" in found.group(2):
        raise PageRefused("a practice panel holds a section, so it cannot be replaced whole")
    return found.group(1) + "\n" + _note("practice", PRACTICE_NOTE) + "\n" + found.group(3)


def _example(found: re.Match[str]) -> str:
    inside = found.group(2)
    summary, items = _SUMMARY.search(inside), _LIST.search(inside)
    if not summary or not items:
        raise PageRefused("a code example lacks its summary or its list of files")
    return (
        found.group(1)
        + summary.group(0)
        + items.group(0)
        + _note("example", EXAMPLE_NOTE)
        + found.group(3)
    )


def remove_server_parts(html: str) -> tuple[str, bool]:
    """Return the page with every server-only part removed, and whether it had narration."""
    narrated = bool(_PLAYER.search(html) or _AUDIO.search(html))
    html = _PLAYER.sub("", html)
    html = _NARRATOR.sub("", html)
    html = _AUDIO.sub("", html)
    html = _PRACTICE.sub(_practice, html)
    html = _EXAMPLE.sub(_example, html)
    html = _EXAMPLE_RUN.sub(lambda _: _note("example", EXAMPLE_RUN_NOTE), html)
    html = _EXAMPLE_RUN_ASSET.sub("", html)
    html = _SOURCE_TAG.sub(lambda tag: _HREF.sub("", tag.group(0)), html)
    if narrated and _HEADER_END in html:
        html = html.replace(_HEADER_END, _HEADER_END + "\n" + _note("narration", NARRATION_NOTE), 1)
    return html, narrated


def finish(html: str, *, assets: str) -> str:
    """Return the page with the banner, the stylesheet and the script added.

    `assets` is the directory of `preview.css` and `preview.js` as this page reaches
    it: a relative path with no trailing slash.
    """
    banner = f'<div data-preview-banner role="note"><p>{BANNER}</p></div>'
    if _HEAD_END not in html or _BODY_END not in html or not _BODY.search(html):
        raise PageRefused("a page without a head and a body cannot carry the banner")
    icon = "" if 'rel="icon"' in html else f'<link rel="icon" href="{NO_ICON}">\n'
    css = f'{icon}<link rel="stylesheet" href="{escape(assets)}/preview.css">\n'
    js = f'<script src="{escape(assets)}/preview.js" defer></script>\n'
    html = html.replace(_HEAD_END, css + _HEAD_END, 1)
    # ⚠️ Searched only now, after the head gained its links: an offset taken earlier is stale.
    topbar = _SEARCH_OPEN.search(html)
    if topbar:
        # ⭐ The redesign's top bar is there: the banner becomes its "Run locally".
        at = topbar.start()
        html = html[:at] + RUN_LOCALLY.format(banner=BANNER) + html[at:]
    else:
        html = _BODY.sub(lambda body: body.group(0) + "\n" + banner, html, count=1)
    before, end, after = html.rpartition(_BODY_END)
    return before + js + end + after


#: The hidden directory the learner tree keeps its pages in, and what it is written as.
HIDDEN = ".studyforge"
VISIBLE = "course"

#: The page every visitor lands on.
INDEX = "index.html"

_REFERENCE = re.compile(r'\b(?:href|src)="([^"]*)"')

#: ⭐ The search's files beside `page.js`: the index, which names its pieces and the pages it
#: ranks, the ranking library, and the builder an index of records needs (only where there is one).
SEARCH_BUNDLE = "page.js"
SEARCH_INDEX = "search-index.js"
SEARCH_LIBRARY = "minisearch.js"
SEARCH_BUILDER = "search-build.js"
_SEARCH_LIST = re.compile(r'"(pages|parts)":(\[[^\]]*\])')


class PreviewRefused(ValueError):
    """A tree this module will not write a preview of, and why."""


@dataclass(frozen=True, slots=True)
class Previewed:
    """What one preview wrote, each path relative to the output."""

    pages: tuple[str, ...]
    files: tuple[str, ...]
    narrated: tuple[str, ...]


def mapped(path: str) -> str:
    """Return where the learner tree's `path` sits in the preview."""
    parts = path.split("/")
    if parts[0] == HIDDEN:
        parts[0] = VISIBLE
    for part in parts:
        if part.startswith("."):
            raise PreviewRefused(
                f"{path} has a dot-directory or dot-file, which Pages would not serve"
            )
    return "/".join(parts)


def _target(page: str, url: str) -> str | None:
    """Return the tree path `url` names from `page`, or None where it names no file."""
    split = urlsplit(url)
    if split.scheme or split.netloc or not split.path:
        return None
    joined = posixpath.normpath(posixpath.join(posixpath.dirname(page), unquote(split.path)))
    if joined == ".." or joined.startswith("../") or joined.startswith("/"):
        raise PreviewRefused(f"{page} links {url}, which leaves the tree")
    return joined


def _reference(page: str, url: str, sub: dict[str, str]) -> str:
    target = _target(page, url)
    if target is None:
        return url
    split = urlsplit(url)
    here = posixpath.dirname(sub[page]) or "."
    new = quote(posixpath.relpath(sub[target], here), safe="/")
    return urlunsplit(("", "", new, split.query, split.fragment))


def _named(tree: Path, index: str) -> dict[str, list[str]]:
    """The page addresses and piece names the search index at `index` lists, read as literals."""
    text = (tree / index).read_text(encoding="utf-8")
    return {kind: ast.literal_eval(names) for kind, names in _SEARCH_LIST.findall(text)}


def _searched_pages(tree: Path, files: set[str], pages: dict[str, str]) -> list[str]:
    """Pages a search index names that the walk has not reached but the tree holds."""
    more: list[str] = []
    for bundle in sorted(files):
        index = posixpath.join(posixpath.dirname(bundle), SEARCH_INDEX)
        if posixpath.basename(bundle) != SEARCH_BUNDLE or not (tree / index).is_file():
            continue
        for page in _named(tree, index).get("pages", []):
            target = _target(index, page)
            if target and target not in pages and (tree / target).is_file():
                more.append(target)
    return more


def _search(tree: Path, files: set[str], pages: dict[str, str]) -> set[str]:
    """Return the search's files beside each copied `page.js` with an index, or refuse by name."""
    found: set[str] = set()
    for bundle in sorted(files):
        if posixpath.basename(bundle) != SEARCH_BUNDLE:
            continue
        here = posixpath.dirname(bundle)
        index = posixpath.join(here, SEARCH_INDEX)
        if not (tree / index).is_file():
            continue
        named = _named(tree, index)
        for page in named.get("pages", []):
            target = _target(index, page)
            if target not in pages:
                raise PreviewRefused(f"{index} names {page}, which no page of the preview reaches")
        wanted = [SEARCH_LIBRARY, *named.get("parts", [])]
        if (tree / here / SEARCH_BUILDER).is_file():
            wanted.append(SEARCH_BUILDER)
        for name in wanted:
            one = _target(index, name)
            if one is None or not (tree / one).is_file():
                raise PreviewRefused(f"{index} needs {name}, which is not in the tree")
            found.add(one)
        found.add(index)
    return found


def preview(tree: Path, out: Path) -> Previewed:
    """Write the read-only preview of the learner tree at `tree` into `out`, or refuse by name."""
    tree, out = Path(tree), Path(out)
    if out.exists() and any(out.iterdir()):
        raise PreviewRefused("the target directory is not empty; name a new or empty one")
    if not (tree / INDEX).is_file():
        raise PreviewRefused(f"the tree has no {INDEX}, so there is no page to land on")
    pages: dict[str, str] = {}
    narrated: list[str] = []
    files: set[str] = set()
    queue = [INDEX]
    while queue or (queue := _searched_pages(tree, files, pages)):
        page = queue.pop()
        if page in pages:
            continue
        try:
            text, voiced = remove_server_parts((tree / page).read_text(encoding="utf-8"))
        except PageRefused as error:
            raise PreviewRefused(f"{page}: {error}") from error
        pages[page] = text
        if voiced:
            narrated.append(page)
        for url in _REFERENCE.findall(text):
            target = _target(page, url)
            if target is None:
                continue
            if not (tree / target).is_file():
                raise PreviewRefused(f"{page} links {url}, which is not in the tree")
            if target.endswith(".html"):
                queue.append(target)
            else:
                files.add(target)
    files |= _search(tree, files, pages)
    sub = {one: mapped(one) for one in (*pages, *files)}
    reserved = {f"{VISIBLE}/preview.css", f"{VISIBLE}/preview.js"}
    if len(set(sub.values())) != len(sub) or reserved & set(sub.values()):
        raise PreviewRefused("two paths of the tree would be written to the same place")
    out.mkdir(parents=True, exist_ok=True)
    for page, text in pages.items():
        here = posixpath.dirname(sub[page]) or "."
        assets = posixpath.relpath(VISIBLE, here)
        done = _REFERENCE.sub(
            lambda found, page=page: found.group(0).replace(
                f'"{found.group(1)}"', f'"{_reference(page, found.group(1), sub)}"'
            ),
            text,
        )
        _write(out / sub[page], finish(done, assets=assets))
    for one in files:
        (out / sub[one]).parent.mkdir(parents=True, exist_ok=True)
        (out / sub[one]).write_bytes((tree / one).read_bytes())
    _write(out / VISIBLE / "preview.css", PREVIEW_CSS)
    _write(out / VISIBLE / "preview.js", PREVIEW_JS)
    return Previewed(
        tuple(sorted(sub[one] for one in pages)),
        tuple(
            sorted(
                [
                    *(sub[one] for one in files),
                    f"{VISIBLE}/preview.css",
                    f"{VISIBLE}/preview.js",
                ]
            )
        ),
        tuple(sorted(sub[one] for one in narrated)),
    )


def _write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="\n") as handle:
        handle.write(text)


def main(argv: Sequence[str] | None = None) -> int:
    """`python -m ...preview TREE OUT`: write the preview, say what it holds."""
    arguments = list(sys.argv[1:] if argv is None else argv)
    if len(arguments) != 2:
        print("usage: preview.py TREE OUT")
        return 2
    try:
        made = preview(Path(arguments[0]), Path(arguments[1]))
    except PreviewRefused as error:
        print(f"refused: {error}")
        return 1
    voiced = len(made.narrated)
    print(f"{len(made.pages)} pages, {len(made.files)} files, {voiced} pages had narration")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
