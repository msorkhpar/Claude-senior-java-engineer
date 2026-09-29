"""Where the reader is in a course: the progress line, the strip and the Up next slip.

**What it does.** Returns the markup the root index and a container page carry
above their lists (the plan's §6): a progress line with its numbers at
zero, an optional segmented strip sized by each group's units, the Up next
slip, and, on the index, the filter with its two expand controls.

**How you use it.** `line(total, strip)`, `strip(segments)`, `up_next(title,
href)`, `finder()`. The index and container renderers put them in the body.

**Depends on.** `render.markup` for escaping and the href gate.

## ⛔ Every word is here, and the page script writes only numbers

The marks live in the reader's browser, so what is read can only be known at
read time. ⭐ So every sentence is written HERE with its numbers at zero, its
alternatives already present and hidden, and `progress-view.js` fills numbers,
moves an href and shows or hides what exists. A script that composed sentences
would be words the renderer's tests never see. ⛔ **And the page is identical
whoever opens it** (R10): no mark reaches the markup.

## ⛔ With no script, nothing here lies

The progress region and the filter ship `hidden`, because neither can be true
without the script. The slip ships visible and names the course's first unit,
which is the right answer for a reader with no marks.
"""

from __future__ import annotations

from collections.abc import Sequence

from studyforge.render.markup import anchor, escape, escape_attribute, inline, safe_href


def _units(number: int) -> str:
    """`1 unit` or `38 units`."""
    return "1 unit" if number == 1 else f"{number} units"


def line(total: int, strip_markup: str = "") -> str:
    """Return the progress region: `0 of N units read`, `N to go` right-aligned."""
    return (
        f'<section aria-label="Progress" hidden><p><strong>0</strong> of {_units(total)} '
        f"read <span><b>{total}</b> to go</span></p>{strip_markup}</section>"
    )


def strip(segments: Sequence[tuple[str, str, int]]) -> str:
    """Return the segmented strip: one link per `(key, title, units)`, sized by units.

    ⭐ Each segment is a link to its group's own disclosure (the key is that
    element's id), so the picture of progress is also the way to a group — the
    reference page's strip, sized by real work. ⚠️ The size is a custom
    property in the markup, `--units`, written from counts the build already
    knows; no colour and no measure is typed here.

    ⛔ **The strip never overflows its column.** `--segments` is how many
    segments share it, so the stylesheet lets a segment's floor give way to an
    equal share once the segments outnumber the room for their floors.
    """
    if len(segments) < 2:
        return ""
    items = "".join(
        f'<li style="--units: {units}"><a href="{escape_attribute(anchor(key))}">'
        f"{inline(title)}</a></li>"
        for key, title, units in segments
    )
    return f'<ol aria-label="Progress by group" style="--segments: {len(segments)}">{items}</ol>'


def up_next(title: str, href: str | None) -> str:
    """Return the Up next slip naming `title`, or `''` when there is nothing to open.

    ⛔ The all-read state is present and hidden, so the script never types
    it — and it is a paragraph, not a link: there is nowhere left to go, and a
    link into the page's own content would be chrome pointing inward.
    """
    target = safe_href(href) if href else None
    if target is None:
        return ""
    return (
        f'<nav aria-label="Up next"><a href="{escape_attribute(target)}"><span>Up next</span> '
        f"{inline(title)}</a>"
        "<p hidden><span>Up next</span> You have read every unit</p></nav>"
    )


def finder() -> str:
    """Return the filter and its two controls , shipped hidden."""
    return (
        '<form role="search" aria-label="Filter units" hidden>'
        '<input type="search" name="filter" placeholder="Filter units…" '
        'aria-label="Filter units" autocomplete="off" spellcheck="false">'
        '<button type="button" value="expand">Expand all</button>'
        '<button type="button" value="collapse">Collapse all</button>'
        '<p role="status" aria-live="polite" hidden><b>0</b> matching units</p>'
        "</form>"
    )


def units_phrase(number: int) -> str:
    """Return the masthead's `11 units`, published for the container page's meta."""
    return escape(_units(number))
