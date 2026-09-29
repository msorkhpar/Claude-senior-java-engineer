r"""One served section, and the three fields on it that are never an author's.

**What it does.** Builds a section record in key order, deriving the `video`,
the `workspace` and the `attachments` from the archive rather than accepting
them from anywhere.

**How you use it.** `section(key=..., kind=..., heading=..., document=...)`.

**Depends on.** `archive.document` for what a video record and a media entry
are, `exercise` for what a workspace is.

## ⛔ The derived fields are refused in an authored overlay, and this is why

⚠️ `unit.content.DERIVED_FIELDS` names them and refuses them there; this is
where they come from instead.

- **`workspace`** — the backend runs those commands, so the only hand that may
  write them is the one that also wrote the file they address.
- **`video`** — the archive is the only record of what was downloaded and where
  it was filed, and it is *carried* byte for byte rather than re-derived.
- **`attachments`** — the same record, for the files spec C4 says the page links
  rather than shows. ⛔ A hand-written list here would name files no ingest ever
  fetched, and the build would copy nothing to meet the link.

## ⭐ `attachments` is the page's half of C4, and it is carried, never invented (§6)

⚠️ **The archive declared these files and nothing read them**: they were
archived, digested and then mentioned by no page and copied by no build. ⭐ The
served section carries the archive's own entries, `render.page.section` links
them, and `generate.media` copies exactly what the page links — the order spec
§5 and R3 require, because a copy no page addresses is bytes a reader
can never reach.

⛔ **`assets` is deliberately NOT carried.** An asset is a file a **block**
already names, so the page reaches it through that block's `src`; listing them
too would offer the reader the diagram they are already looking at. See
`archive.document.MEDIA_ENTRY_KEYS`, which is where the two lists' one
vocabulary and their two purposes are stated.

## ⭐ `video` is always written, `null` when there is none

⛔ **A key that disappears when it is empty cannot be told from one nobody
wrote**, and the two answers are different: *the archive had no video* and
*this build could not see it* must not render the same. ⚠️ The same argument
`unit.trust` makes for a defaulted `trust`, one document along. ⭐ `attachments`
is written the same way and is `[]` when the archive declared none, so a unit
with no companion files is told from one whose files this build could not read.
"""

from __future__ import annotations

from studyforge.archive.document import MEDIA_ENTRY_KEYS, VIDEO_KEYS
from studyforge.exercise import of as exercise_of
from studyforge.exercise import to_document as exercise_document
from studyforge.unit.outline import without_outline_number

#: A served section's keys, in the order they are written (R10).
SECTION_KEYS = ("key", "kind", "heading", "blocks", "video", "workspace", "attachments")


def section(*, key: str, kind: str, heading: str, blocks: list, document: dict) -> dict:
    """One served section: somebody's heading and blocks, and the archive's two fields.

    ⚠️ **`blocks` is passed in rather than read from `document`**, and the
    distinction is the two shapes: a derived section's blocks *are* the
    archive's, and an authored section's are the **author's**. ⛔ The only
    things this build adds either way are `video` and `workspace`, which is the
    exact promise `unit.content.DERIVED_FIELDS` makes from the other side.

    ⛔ **The heading is served without the source's outline number**
    (`unit.outline`): the site lists and orders the units itself. ⚠️ **The
    blocks keep theirs here**, because a sentence that names a heading names it
    by that number (`unit.headings`); `builder.document` takes them off every
    heading block once the references are served, so the page and its narration
    still read the same words.
    """
    return {
        "key": key,
        "kind": kind,
        "heading": without_outline_number(heading),
        "blocks": list(blocks or []),
        "video": video_of(document),
        "workspace": workspace_of(document, key),
        "attachments": attachments_of(document),
    }


def video_of(document: dict) -> dict | None:
    """Carry the archive's own video record through — ⛔ never re-derive it."""
    record = document.get("video")
    if record is None:
        return None
    return {name: record.get(name) for name in VIDEO_KEYS if name in record}


def attachments_of(document: dict) -> list[dict]:
    """Carry the archive's own attachment entries through — ⛔ never re-derive one.

    ⭐ **`local` is what the page addresses and `remote` is provenance**, and
    both are carried for the reason a `video` record's provenance is: a re-fetch
    must be possible from the document alone. ⚠️ Only the entry's declared keys
    survive, in the archive's own order (R10), so an adapter that wrote a
    sixteenth field does not change what this build serves.

    ⛔ Anything that is not a list of objects is `[]`: a malformed declaration is
    the archive's defect and `studyforge validate` names it, and a page that
    raised here would lose the whole lesson over a companion file.
    """
    declared = document.get("attachments")
    if not isinstance(declared, list):
        return []
    return [
        {name: entry.get(name) for name in MEDIA_ENTRY_KEYS if name in entry}
        for entry in declared
        if isinstance(entry, dict)
    ]


def workspace_of(document: dict, where: str) -> dict | None:
    """Return the exercise record a practice carries, or `None` for anything else.

    ⚠️ Read through `exercise.of`, which owns R5's rule by way of `unit.trust`.
    ⛔ No provenance or trust logic is spelled here; a second spelling of that
    rule is the defect this package refuses.
    """
    exercise = exercise_of(document, where)
    return None if exercise is None else exercise_document(exercise)
