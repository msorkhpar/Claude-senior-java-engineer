r"""A lesson's links to its own code: marked, and its examples drawn to open in place.

**What it does.** Marks every link on a unit page that leads to a code file of
the corpus with that file's corpus-relative path (`data-code-path`), and turns
a list of such links — a page's code examples — into one collapsed entry per
example, a source and its paired test, named for its source. Expanded, served,
an entry opens its example in the course's editor right there; as built, it
says why each file opens as plain text.

**How you use it.** `page.document` calls `body = examples(*mark(body,
placement), placement)` over the page's rendered sections. A page that links no
code, and a corpus with no pairing, render byte for byte as they did.

**Depends on.** `corpus.placement` for the generated directory's name,
`render.templates` and `render.markup`. ⛔ Not on `serve` and not on `execute`:
a page renders over `file://`, and this names no API, no origin and no port
(R8). ⭐ Which source a test stands beside is the build's answer
(`Placement.pairing`), read from the corpus's files before the page is drawn.

## ⭐ Which link is code is read from the page's own geometry

⭐ **A link to a corpus file is written relative to the page by
`unit.mentions`**, and the page knows where it sits, so the file is the link resolved
from the page. It is code when it stays inside the corpus, stays outside the
generated directory — a page, a clip or an asset is never code — and ends in a
suffix the corpus's declared runtimes write (`Placement.code`, which is `()` for
a page that does not sit beside the corpus's files, and for a corpus that
declares no runtime). ⛔ **The link is left exactly as it is**: its href is
the file's plain view, which is what it opens whenever the editor does not.

⚠️ **The anchor is found by the one shape `render.markup.text` writes**,
`<a href="…" rel="noopener noreferrer">`, over text in which every literal
character the material carried is escaped — so a `<a` inside a code block is
`&lt;a` and is never read as a link.

## ⭐ A list of code links is the page's examples, one entry per pair

⚠️ **Measured on a real course:** a click on a code example opened a panel at
the foot of the page, under every practice, and the page scrolled there. ⭐ So
a list whose every item is one code link and a short label (`Test: …`) is drawn
as the examples it is: each item joins the entry of the pair its file stands
in, the entries follow the order their first item has, and each is a
`<details>` named for its source — for its test where no source was found.
⭐ **Each item keeps its own element and words**, inside its entry. ⚠️ Items of
one pair that the list separates are drawn together, which moves the later one
up.

⛔ **A panel is never narrated** (register ruling, 2026-09-26: a code example is
never narrated). `narrate.speakable` gives such a list no speech unit, so its
items have no clip to name; and the panel drops any audio attribute an item
carries all the same, so no record, however old, puts one inside
`div[data-code-examples]`.

⭐ **An entry's tabs are its pair's**: `Source` and `Test`, or the one of them
it has. ⛔ **Nothing is loaded as built**: the editor's frames are empty slots
`code-links.js` fills when the entry is expanded, served, with the editor up.

## ⛔ An entry says WHY as built, and the served page says the rest

⭐ **Built, an entry's note is one sentence**: each file opens as plain text,
because the course's editor is not running here — and how to start it. ⭐
**Served with the editor up**, `code-links.js` shows the sentence saying the
editor opens a COPY of the code instead; ⭐ with the editor up and the
course's declared runner down, the editor still opens the files and the entry
shows the RUNNER's sentence in place of Run tests, so each note names the one
service that is missing. ⛔ **The words are framework
structure** (R1): this framework's, in `EXAMPLE_TEMPLATE`, never the material's.
"""

from __future__ import annotations

import re
from html import unescape
from pathlib import PurePosixPath
from urllib.parse import unquote

from studyforge.corpus.placement.profile import GENERATED_ROOT
from studyforge.render import templates
from studyforge.render.markup import escape_attribute
from studyforge.render.page.assets import AUDIO_ATTRIBUTE, Placement

#: One example: a collapsed entry, its lines, and the slots its editor opens in.
EXAMPLE_TEMPLATE = "code-example.html"

#: The examples of one list, and the corpus they open in.
EXAMPLES_TEMPLATE = "code-examples.html"

#: The most words an example's line carries beside its link — a label, not prose.
MAX_LABEL = 40

#: The attribute a code link carries: the file's path, relative to the corpus.
PATH_ATTRIBUTE = "data-code-path"

#: The audio attribute a narrated item carries, which no line of a panel keeps.
AUDIO = re.compile(rf' {AUDIO_ATTRIBUTE}="[^"]*"')

#: The one shape `render.markup.text` writes an inline link in.
ANCHOR = re.compile(r'<a href="(?P<href>[^"]*)" rel="noopener noreferrer">')

#: An href that carries a scheme, and so names no file of the corpus.
SCHEME = re.compile(r"^[A-Za-z][A-Za-z0-9+.-]*:")

#: One flat list, as `blocks.prose` writes it. ⚠️ Non-greedy: a nested list
#: ends the match early, and its items then fail `ITEM`, so it is left alone.
LIST = re.compile(r'<(?P<tag>ul|ol) class="items"(?P<start>[^>]*)>(?P<items>.*?)</(?P=tag)>', re.S)

#: One item that is a marked code link and a short label, and nothing else.
ITEM = re.compile(
    r'<li(?P<attributes>[^>]*)>(?P<before>[^<]*)<a href="[^"]*" rel="noopener noreferrer" '
    rf'{PATH_ATTRIBUTE}="(?P<path>[^"]*)">[^<]*</a>(?P<after>[^<]*)</li>'
)


def mark(body: str, placement: Placement) -> tuple[str, tuple[str, ...]]:
    """Return `body` with each code link marked, and the files marked, in order, once each."""
    if not placement.code:
        return body, ()
    found: list[str] = []

    def marked(match: re.Match[str]) -> str:
        path = code_file(unescape(match["href"]), placement)
        if path is None:
            return match.group(0)
        if path not in found:
            found.append(path)
        opening = match.group(0)
        return f'{opening[:-1]} {PATH_ATTRIBUTE}="{escape_attribute(path)}">'

    return ANCHOR.sub(marked, body), tuple(found)


def code_file(href: str, placement: Placement) -> str | None:
    """Return the corpus-relative code file `href` leads to from this page, or `None`."""
    path = re.split(r"[?#]", href, maxsplit=1)[0]
    if not path or SCHEME.match(href) or href.startswith("/") or href.startswith("#"):
        return None
    walked: list[str] = []
    for part in [*PurePosixPath(placement.unit.page).parent.parts, *unquote(path).split("/")]:
        if part in ("", "."):
            continue
        if part == "..":
            if not walked:
                return None
            walked.pop()
        else:
            walked.append(part)
    if not walked or walked[0] == GENERATED_ROOT or not walked[-1].endswith(placement.code):
        return None
    return "/".join(walked)


def examples(body: str, paths: tuple[str, ...], placement: Placement) -> str:
    """Return `body` with each list of code links drawn as its examples, one per pair."""
    if not paths or placement.pairing is None:
        return body
    pairing = placement.pairing
    return LIST.sub(lambda found: _examples(found, pairing, placement) or found.group(0), body)


def _examples(found: re.Match[str], pairing, placement: Placement) -> str | None:
    """Draw one list as its examples, or `None` when an item is anything but a code link."""
    items = list(ITEM.finditer(found["items"]))
    if not items or "".join(item.group(0) for item in items) != found["items"]:
        return None
    if any(len(unescape(item["before"] + item["after"]).strip()) > MAX_LABEL for item in items):
        return None
    entries: dict[tuple[str | None, str | None], list[re.Match[str]]] = {}
    for item in items:
        source, test = pairing(unescape(item["path"]))
        entries.setdefault((source, test), []).append(item)
    tag, start = found["tag"], found["start"]
    drawn = "".join(
        _entry(pair, lines, f'<{tag} class="items"{start}>', f"</{tag}>")
        for pair, lines in entries.items()
    )
    return templates.fill(
        EXAMPLES_TEMPLATE, corpus=escape_attribute(placement.corpus), entries=drawn
    )


def _entry(pair: tuple[str | None, str | None], lines, opening: str, closing: str) -> str:
    """One example: named for its source, its lines, and a tab for each file it has."""
    source, test = pair
    first = unescape(lines[0]["path"])
    named = PurePosixPath(source or test or first).name
    windows = (
        (("main", "Source"), ("test", "Test"))
        if source and test
        else ((("main", "Source" if source else "Test"),))
    )
    tabs = "".join(
        f'<button type="button" role="tab" data-code-tab="{window}" '
        f'aria-selected="{"true" if index == 0 else "false"}">{words}</button>'
        for index, (window, words) in enumerate(windows)
    )
    return templates.fill(
        EXAMPLE_TEMPLATE,
        path=escape_attribute(first),
        name=escape_attribute(named),
        lines=opening + "".join(AUDIO.sub("", line.group(0)) for line in lines) + closing,
        tabs=tabs,
    )
