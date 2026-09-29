"""The reading order, and what one unit points at when the reader finishes it.

**What it does.** Flattens a `Contents` into the order a reader reads it in,
and answers — for any one unit — which unit comes before it, which comes
after, and how the two are addressed from its own page.

**How you use it.**

    from studyforge import contents as toc

    toc.order(built)                 # every entry, in reading order
    toc.neighbours(built, key)       # the previous and next entries, or None
    toc.links(built, key)            # {'previous': {'href', 'label'}, ...}

**Depends on.** `studyforge.corpus.placement` for `relative_href` and for the
root index's one spelling, and this package's `entries` and `errors`.
⛔ **Not on `render`.** `links` returns a mapping of plain strings and no
markup: a renderer turns it into `navigation.Links`, and R13 keeps the markup
in the renderer's templates rather than in this module's strings.

## ⛔ The order is DERIVED, never stored

⚠️ **This is the first thing in the project that computes a reading order**, so
it is also the first chance to store one twice. ⭐ The tree already states the
order — siblings by segment, units by their contiguous ordinals — and a flat
list written into the document beside it would be a second copy that the day's
first edit puts out of step, with nothing comparing them. So the walk lives
here, once, and `tests` assert that this is the module it lives in.

## ⭐ An href is computed per asking page, which is why none is stored

⛔ **There is no single correct href for a unit.** `relative_href` answers
*"how does the page at A address B"*, and under the `sibling` profile two
units of one container sit in different directories, so the same target has a
different href from each of its neighbours. ⚠️ Storing one would be correct
under `tree`, wrong under `sibling`, and wrong silently — which is exactly the
failure `UnitLocations.href`'s *ask, never compose* rule already names one
layer down.

## ⛔ The index link is labelled with the CORPUS's own title

⚠️ Any word this module chose — *"Contents"*, *"Index"*, *"Up"* — would be a
sentence of this framework's own in every corpus's chrome (R1), and the
`meta` line of the unit page refuses the same temptation for the same reason.
⭐ The corpus already says what it is called, in its manifest, and that name is
carried into the contents for exactly this.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import PurePosixPath

from studyforge.contents.entries import Contents, Entry, Group
from studyforge.contents.errors import ContentsError
from studyforge.corpus.placement import ROOT_INDEX_FILENAME, relative_href
from studyforge.describe import describe

#: The slots of the between-units bar, in the order they are emitted. ⛔ A
#: tuple, so nothing downstream depends on a dict's iteration order (R10), and
#: the same three names `render.page.navigation.LINK_SLOTS` already uses — the
#: renderer's `Links` fields are these words.
LINK_FIELDS = ("previous", "next", "index")

#: What a link is written as: where it goes and what it is called. ⛔ Two plain
#: strings and no markup (R13).
LINK_KEYS = ("href", "label")


@dataclass(frozen=True, slots=True)
class Neighbours:
    """Where one unit sits in the reading order, and what is either side of it.

    ⭐ `position` is 1-based and `total` is the whole corpus, so a renderer can
    say *"3 of 38"* without walking the tree again. ⛔ Both are counted over
    what the corpus **declares**, never over what exists on this machine.
    """

    entry: Entry
    previous: Entry | None
    next: Entry | None
    position: int
    total: int


def order(contents: Contents) -> tuple[Entry, ...]:
    """Every unit, in the order a reader reads them.

    ⛔ Depth-first over the groups in the order the tree holds them, which
    `tree.build` has already made a function of the addresses. Nothing here
    sorts anything: a second orderer is the defect this module exists to
    prevent.
    """
    found: list[Entry] = []
    for group in contents.groups:
        _walk(group, found)
    return tuple(found)


def _walk(group: Group, found: list[Entry]) -> None:
    """Append one group's units, or recurse into the groups it holds."""
    for child in group.groups:
        _walk(child, found)
    found.extend(group.entries)


def positions(contents: Contents) -> dict[str, int]:
    """Return `{unit key: 1-based position}` for the whole corpus.

    ⚠️ Built once by a caller that asks about many units — `neighbours` is a
    linear scan, and a renderer walking 1,290 pages would otherwise make it a
    quadratic one.
    """
    return {entry.key: position for position, entry in enumerate(order(contents), start=1)}


def neighbours(contents: Contents, key: str) -> Neighbours:
    """Where the unit `key` names sits, and what is either side of it.

    ⛔ Raises rather than returning an empty answer for a key this corpus does
    not declare: an absent neighbour and an absent *unit* are different facts,
    and a bar rendered from the second is a page that quietly points nowhere.
    """
    walked = order(contents)
    for position, entry in enumerate(walked):
        if entry.key != key:
            continue
        return Neighbours(
            entry=entry,
            previous=walked[position - 1] if position > 0 else None,
            next=walked[position + 1] if position + 1 < len(walked) else None,
            position=position + 1,
            total=len(walked),
        )
    # ⛔ The count, never the list: a corpus has up to four figures of these
    # and a refusal nobody can read is a refusal nobody acts on (R6).
    #
    # ⛔ **And the key is DESCRIBED, never echoed** (R7). This
    # branch fires precisely because the value was not a unit key, which is the
    # branch an absolute path arrives at — so quoting it would take the one
    # input guaranteed to carry a home directory and put it in a log.
    raise ContentsError(
        f"corpus {contents.corpus} declares {len(walked)} unit(s) and was asked "
        f"about {describe(key)} that is not one of them; a unit key is minted by "
        f"Address.unit_key and matched by string equality, so a near miss never "
        f"matches anything"
    )


def links(contents: Contents, key: str) -> dict[str, dict[str, str]]:
    """How the page for `key` addresses its neighbours and the root index.

    Returns at most three entries, keyed by `LINK_FIELDS`; a slot with no
    destination is **absent** rather than present and empty. ⭐ The shape is
    what `render.page.navigation.Links` takes, field for field, and the
    renderer builds one from it — this module names no markup (R13) and
    imports nothing from `render` (the direction is one-way).
    """
    here = neighbours(contents, key)
    found: dict[str, dict[str, str]] = {}
    if here.previous is not None:
        found["previous"] = _link(here.entry.page, here.previous.page, here.previous.title)
    if here.next is not None:
        found["next"] = _link(here.entry.page, here.next.page, here.next.title)
    found["index"] = _link(here.entry.page, PurePosixPath(ROOT_INDEX_FILENAME), contents.title)
    return {field: found[field] for field in LINK_FIELDS if field in found}


def _link(from_page: PurePosixPath, to_page: PurePosixPath, label: str) -> dict[str, str]:
    """One destination as plain data: a relative href and what to call it."""
    return {"href": relative_href(from_page, to_page), "label": label}
