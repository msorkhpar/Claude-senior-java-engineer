r"""A unit's words about ANOTHER unit: its outline number and its source link, served as the unit.

**What it does.** Rewrites the prose of one served unit so that a reference to
another unit of the same corpus reads as that unit and leads to its page:

- a dotted number in the text that is a unit's recorded label (`see 3.2.4`)
  becomes that unit's title, set in emphasis;
- a link whose label is such a number (`[3.2.4](README_3.2.4.md)`) keeps its
  link and shows the title instead of the number;
- a link to the source file a unit was read from (`README_3.2.4.md`,
  `../11-try-catch/README_3.1.3.md`) links that unit's generated page, at the
  heading its fragment names (`README_3.2.4.md#rules`);
- a link label that opens with its target's number (`[7.3.2.1. LocalDate](…)`)
  loses the number;
- a link to any other file of the corpus (`src/main/java/…/Types.java`) is
  addressed from the page, which sits somewhere else than the source did;
- an in-page link to the source's own anchor (`#introduction`) links the id the
  page gives that heading (`unit.headings`);
- `section 2.2`, where `2.2` is the outline number of one of the unit's own
  headings, or of exactly one heading of its container, is served as that
  heading's words in double quotes, linking the heading (`Section "Bitmaps"`),
  the quotes outside the link;
- a quiz's stem, options and per-option sentences follow the same rules, as
  plain words (`Mentions.words`): the page escapes them as text, and the
  grading route answers a sentence as it is served.

**How you use it.** `generate.declarations` builds one `Mentions` per unit
(`Mentions.of(...)`, `Mentions.numbered(...)`) and hands it to
`build_unit(mentions=...)`, and the builder serves
`mentions.sections(sections, found)`, `found` being the headings it read before
they lost their numbers. ⭐ Every consumer of a served unit (the page, the
narration, validate's narration check, the server's content route and its
quiz grading) builds it through that one call, so the page, its clips and a
quiz's answer say the same words. `mentions.unreached(sections)` counts the
corpus-file links a page outside the corpus root cannot keep.

**Depends on.** `re`, `unit.prose` for every run of words a unit shows,
`unit.headings` for what a unit's headings are called, and
`corpus.placement.relative_href` for the one way a page addresses another file.
⛔ Not on `render`: the inline markers read here are the archive's own
(`[label](href)` and a backtick span), and this module only decides which words
and which href the served document carries.

## ⛔ Only what names a unit of THIS corpus is touched

⭐ A number is replaced only when it is EXACTLY a label the corpus's container
maps record, and a link only when its href resolves, from the unit's own
recorded origin, to another unit's recorded origin. So `JLS §17.4.5`,
`Java 1.4`, `HTTP/1.1`, a version, a quantity and a number that names nothing
the corpus declares keep every character.

⚠️ **A bare number must have at least three dotted parts** (`3.2.4`). A
two-part label (`3.2`) is replaced only inside a link's label, because
`Java 1.4` or `JDBC 4.2` sitting in a sentence is far likelier to be a version
than a reference, and a wrong title in a sentence is worse than a number.

⚠️ **A link's fragment is kept only when it names a heading of the target
unit.** It names an anchor of the SOURCE file, and the generated page keys its
headings itself, so the fragment is looked up among the target's headings as
its source's host spells them (`unit.headings.by_slug`) and served as the id
that page gives the heading. A fragment that names none is dropped, and the
link lands at the top of the page.

## ⭐ A file the author links is linked where it is

⛔ **Nothing is copied and nothing is dropped.** The file is the author's, and
it stays where the author put it. A relative link is read from the unit's
recorded origin, and when that names a regular file under the corpus root it is
addressed from the page, on every placement profile, so it resolves over
`file://` and when served. ⚠️ Only a page in a site written into the corpus
root reaches the file (`beside`). Anywhere else the link keeps the author's
href, and `unreached` counts it so the build can say so. ⚠️ A link that climbs
out of the corpus, or names a file that is not there, keeps its href, and
`validate`'s `link-unresolved` names the unit that carries it: it is never
served broken without a word.

## ⛔ A heading number is taken only after the word `section`

⚠️ A version (`2.1.7`) and a standard's year (`8583:1987`) sit in the sentences a heading's
number, so a bare number is never a heading. `section 2.2` is, when `2.2` is
one of the unit's own headings, or else exactly one heading of the unit's
container, whose outline the source numbered as one. ⭐ A number that is a
unit's label is that unit's, as above, whatever else it could be.

⭐ **The heading's words are served in double quotes**, and the author's word
`section` keeps its case, so a title such as `Part 2: Encoding` reads as a
name inside the sentence: `Section "Part 2: Encoding" lists …`. On the page the quotes
sit outside the link; in a quiz's plain words they are the only mark. ⚠️
Narration speaks the served words with the quotes in them: the engine reads a
quote as punctuation and voices no word for it.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field, replace
from pathlib import Path, PurePosixPath
from urllib.parse import unquote

from studyforge.corpus.placement import relative_href
from studyforge.unit.headings import Heading, by_number, by_slug
from studyforge.unit.outline import without_outline_number
from studyforge.unit.prose import quiz, walk


@dataclass(frozen=True, slots=True)
class Target:
    """One unit another unit may name: its served title, its page and its headings' anchors.

    ⭐ `anchors` maps each anchor its source file's host renders to the link
    its page gives that heading (`unit.headings.by_slug`), so a link to the
    unit's file with a fragment lands on the heading it names.
    """

    title: str
    page: PurePosixPath
    anchors: dict[str, str] = field(default_factory=dict)


#: The two markers a reference can sit in or next to, as the archive writes them.
#: ⛔ The render's own shapes for a code span and a link (`render.markup.text`):
#: nothing inside a code span is ever rewritten.
_MARKERS = re.compile(r"`[^`]+`|\[(?P<label>[^\]\n]+)\]\((?P<href>[^)\s]*)\)")

#: A bare dotted number standing on its own: three parts or more.
_BARE = re.compile(r"(?<![\w.§/-])\d{1,3}(?:\.\d{1,3}){2,}(?![\w]|\.\d)")

#: A heading's outline number, after the word `section` and one space.
_SECTION = re.compile(r"(?<=\b[Ss]ection[ \t\n])\d{1,3}(?:\.\d{1,3})+(?![\w]|\.\d)")

#: A link label's opening number, with its stop, then a space.
_LEADING = re.compile(r"^(?P<number>\d{1,3}(?:\.\d{1,3})+)\.?[ \t]+(?=\S)")


@dataclass(frozen=True, slots=True, eq=False)
class Mentions:
    """The corpus's units as ONE unit may name them, and that unit's own place.

    ⭐ `labels`, `origins` and `root` are shared by every unit of a corpus,
    `numbers` by every unit of a container; `origin` and `page` are this unit's.
    `own` and `anchors` are its headings, which `sections` reads. `beside` says
    the page sits in a site written into the corpus root, the one place a
    corpus file is reached from. The default names nothing of the corpus, so a
    unit built with no mentions serves only its references to itself.
    """

    labels: dict[str, Target] = field(default_factory=dict)
    origins: dict[str, Target] = field(default_factory=dict)
    origin: str | None = None
    page: PurePosixPath | None = None
    numbers: dict[str, tuple[Heading, PurePosixPath]] = field(default_factory=dict)
    root: Path | None = None
    own: dict[str, Heading] = field(default_factory=dict)
    anchors: dict[str, str] = field(default_factory=dict)
    beside: bool = True

    @staticmethod
    def of(
        units: tuple[tuple[str | None, str | None, str, PurePosixPath, tuple[Heading, ...]], ...],
    ) -> tuple[dict[str, Target], dict[str, Target]]:
        """Index `(label, origin, title, page, headings)` rows by label and by origin.

        ⛔ A label two units share names neither of them: a number that could be
        either is left as the source wrote it.
        """
        labels: dict[str, Target] = {}
        shared: set[str] = set()
        origins: dict[str, Target] = {}
        for label, origin, title, page, held in units:
            target = Target(without_outline_number(title), page, by_slug(held))
            if isinstance(label, str) and label:
                key = label.rstrip(".")
                if key in labels:
                    shared.add(key)
                labels[key] = target
            if isinstance(origin, str) and origin:
                origins[_normal(origin)] = target
        return {key: value for key, value in labels.items() if key not in shared}, origins

    @staticmethod
    def numbered(
        units: tuple[tuple[PurePosixPath, tuple[Heading, ...]], ...],
    ) -> dict[str, tuple[Heading, PurePosixPath]]:
        """Index one container's `(page, headings)` rows by outline number.

        ⛔ A number two headings of the container share names neither of them.
        """
        found = tuple((heading, page) for page, held in units for heading in held)
        unique = by_number(tuple(heading for heading, _ in found))
        return {
            number: (heading, page)
            for heading, page in found
            if (number := heading.number) is not None and unique.get(number) is heading
        }

    def sections(self, sections: list, found: tuple[Heading, ...]) -> list:
        """Return `sections` with every reference served, `found` being their headings.

        ⚠️ `found` is read from the sections BEFORE their outline numbers left
        (`unit.headings.headings`), and `sections` are served after: a mention
        is served in the words a reader is served.
        """
        served = replace(self, own=by_number(found), anchors=by_slug(found))
        return [served._section(section) for section in sections]

    def unreached(self, sections: list) -> int:
        """Count the links to a corpus file in `sections` that this page does not link.

        ⭐ Read from the sections as the archive wrote them, so it counts exactly
        the links `served` would address from a page beside the files. ⛔ `0` for
        a page that sits in a site written into the corpus root: it keeps them.
        """
        if self.beside:
            return 0
        found: list[str] = []

        def links(value: object) -> object:
            if isinstance(value, str):
                found.extend(
                    match["href"]
                    for match in _MARKERS.finditer(value)
                    if match.group("label") is not None and self._corpus_file(match["href"])
                )
            return value

        for section in sections:
            if isinstance(section, dict):
                walk(section.get("blocks"), links)
        return len(found)

    def _section(self, section: object) -> object:
        """One section with its prose served, and its quiz's words, if it carries one."""
        if not isinstance(section, dict):
            return section
        served = {**section, "blocks": self.served(section.get("blocks"))}
        if "workspace" in section:
            served["workspace"] = quiz(section["workspace"], self.words)
        return served

    def words(self, value: object) -> object:
        """Return one run of plain text with its mentions served as words; a non-string as it came.

        ⭐ The rules `text` keeps, for text the page escapes rather than reads as
        markup (a quiz's stem, options and sentences): a unit's label is its
        title, and `section 2.2` is the heading's words, with no emphasis and no
        link, because neither would be one there.
        """
        if not isinstance(value, str):
            return value
        return self._bare(value, plain=True)

    def served(self, blocks: object) -> object:
        """Return `blocks` with every reference served as what it names; a copy.

        ⭐ A value that names nothing returns `blocks` itself.
        """
        names = (self.labels, self.origins, self.numbers, self.own, self.anchors)
        if not isinstance(blocks, list) or not (any(names) or self.root is not None):
            return blocks
        return walk(blocks, self.text)

    def text(self, value: object) -> object:
        """Return one run of prose with its mentions served; a non-string as it came."""
        if not isinstance(value, str):
            return value
        out: list[str] = []
        position = 0
        for match in _MARKERS.finditer(value):
            out.append(self._bare(value[position : match.start()]))
            label = match.group("label")
            out.append(match.group(0) if label is None else self._link(label, match["href"]))
            position = match.end()
        out.append(self._bare(value[position:]))
        return "".join(out)

    def _bare(self, text: str, *, plain: bool = False) -> str:
        def named(match: re.Match[str]) -> str:
            target = self.labels.get(match.group(0))
            if target is None:
                return match.group(0)
            return target.title if plain else f"*{target.title}*"

        out: list[str] = []
        position = 0
        for match in _SECTION.finditer(text):
            heading = self._heading(match.group(0), plain=plain)
            if heading is not None:
                out.append(_BARE.sub(named, text[position : match.start()]))
                out.append(heading)
                position = match.end()
        out.append(_BARE.sub(named, text[position:]))
        return "".join(out)

    def _heading(self, number: str, *, plain: bool = False) -> str | None:
        """Return the link a heading's number is served as, its words when `plain`, or `None`."""
        if number in self.labels:
            return None
        heading, page = self.own.get(number), self.page
        if heading is None:
            heading, page = self.numbers.get(number, (None, None))
            if page is None or self.page is None or page == self.page:
                return None
        if heading is None or not _labelled(heading.text):
            return None
        if plain:
            return f'"{heading.text}"'
        where = "" if page == self.page else relative_href(self.page, page)
        return f'"[{heading.text}]({where}{heading.reference})"'

    def _link(self, label: str, href: str) -> str:
        if href.startswith("#"):
            named = unquote(href[1:])
            reference = self.anchors.get(named) or self.anchors.get(named.lower())
            return f"[{label}]({reference or href})"
        target = self._linked(href)
        named = self.labels.get(label.strip().rstrip("."))
        if named is not None and (target is None or named is target):
            label = named.title
        elif target is not None:
            leading = _LEADING.match(label)
            if leading is not None and self.labels.get(leading.group("number")) is target:
                label = label[leading.end() :]
        if target is None or self.page is None:
            return f"[{label}]({self._file(href) or href})"
        return f"[{label}]({relative_href(self.page, target.page)}{_fragment(href, target)})"

    def _file(self, href: str) -> str | None:
        """Return how the page addresses the corpus file `href` names, or `None` when none.

        ⛔ `None` too for a page that is not `beside` the files: it keeps the href.
        """
        return self._corpus_file(href) if self.beside else None

    def _corpus_file(self, href: str) -> str | None:
        """Return how a page beside the files addresses the corpus file `href` names."""
        if href.startswith("#") or self._linked(href) is not None:
            return None
        path = re.split(r"[?#]", href, maxsplit=1)[0]
        if self.origin is None or self.page is None or self.root is None:
            return None
        if not path or _SCHEME.match(href) or href.startswith("/"):
            return None
        walked = _normal(f"{PurePosixPath(self.origin).parent}/{path}")
        if not walked or walked.split("/", 1)[0] == "..":
            return None
        root = self.root.resolve()
        found = (root / unquote(walked)).resolve()
        if not found.is_relative_to(root) or not found.is_file():
            return None
        return relative_href(self.page, PurePosixPath(walked)) + href[len(path) :]

    def _linked(self, href: str) -> Target | None:
        """Return the unit whose source file `href` names, read from this unit's own origin."""
        path = re.split(r"[?#]", href, maxsplit=1)[0]
        if self.origin is None or not path or _SCHEME.match(href) or path.startswith("/"):
            return None
        return self.origins.get(_normal(f"{PurePosixPath(self.origin).parent}/{path}"))


#: An href that carries a scheme, and so names no file of the corpus.
_SCHEME = re.compile(r"^[A-Za-z][A-Za-z0-9+.-]*:")


def _fragment(href: str, target: Target) -> str:
    """Return the link to the heading of `target` that `href`'s fragment names, or `""`."""
    _, mark, fragment = href.partition("#")
    if not mark:
        return ""
    named = unquote(fragment)
    return target.anchors.get(named) or target.anchors.get(named.lower()) or ""


def _labelled(text: str) -> bool:
    """Whether a heading's words can stand as a link's label, as the archive writes one."""
    return bool(text) and not any(mark in text for mark in "[]\n")


def _normal(path: str) -> str:
    """Return a relative path with its `.` and `..` segments walked, as a source file names it."""
    out: list[str] = []
    for part in path.split("/"):
        if part in ("", "."):
            continue
        if part == ".." and out and out[-1] != "..":
            out.pop()
        else:
            out.append(part)
    return "/".join(out)
