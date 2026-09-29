r"""Where a recorded clip is: a directory relative to the corpus root, read back without placement.

**What it does.** `root_of(state)` recovers the corpus root from the record's
own path, `where_of(into, root)` spells an audio directory the way the record
carries it, `checked_where(value)` refuses a recorded one that is absolute or
climbs out of the root, and `located(root, where, filename)` is the path a
recorded clip has. `audio_dir(root, locations)` is where one unit's clips go:
its placement answer, rooted. `Superseded` is one clip an earlier wording wrote.

**How you use it.** The pass records `where_of(into, root_of(state))` beside
every filename it writes; a prune asks `located(root, clip.where, clip.filename)`
and never re-derives placement.

**Depends on.** `corpus.placement` for the generated root and a unit's media
directory, and the standard library. ⛔ Never `record` or `incremental`: both import this, one way.

## ⛔ Why the record carries a directory

⚠️ **A unit's audio directory is placement's answer to declarations that can
change** — ordinal, title, origin, label. A record that kept only a filename
could find a clip only while its unit is still declared the same way, so the
clips of a removed or renumbered unit were beyond every prune. ⭐ The directory
the pass actually wrote into is recorded instead, so a clip is locatable from
the record alone.

⛔ **Relative to the corpus root, and never absolute** (R7): an absolute path
carries a home directory, and `checked_where` refuses one on the way in as well
as on the way out.
"""

from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path, PurePosixPath

from studyforge.corpus.placement import AUDIO_DIRNAME, GENERATED_ROOT, UnitLocations


@dataclass(frozen=True, slots=True)
class Superseded:
    """One clip a record entry wrote before a newer one replaced it, and where it is."""

    filename: str
    #: ⚠️ `None` only for a clip a version-1 record wrote and no run could place.
    where: str | None = None

    def document(self) -> dict[str, object]:
        """Return the object the record carries for this clip."""
        return {"filename": self.filename, "where": self.where}


def order(item: Superseded) -> tuple[str, str]:
    """Sort key for superseded clips, so two runs render one order (R10)."""
    return (item.where or "", item.filename)


def root_of(state: Path | str) -> Path:
    """Return the corpus root a record at `state` belongs to.

    ⛔ Refuses a path that is not `<root>/<generated root>/<name>`, because a
    directory relative to the wrong root locates nothing.
    """
    file = Path(state)
    depth = len(PurePosixPath(GENERATED_ROOT).parts)
    if len(file.parents) <= depth:
        raise ValueError("the record is not inside a corpus's generated root")
    root = file.parents[depth]
    if root / GENERATED_ROOT / file.name != file:
        raise ValueError("the record is not inside a corpus's generated root")
    return root


def checked_where(value: object) -> str | None:
    """Return a recorded directory if it stays under the corpus root, else `None`."""
    if not isinstance(value, str) or not value or "\\" in value:
        return None
    path = PurePosixPath(value)
    if path.is_absolute() or ".." in path.parts:
        return None
    return path.as_posix()


def where_of(into: Path | str, root: Path | str) -> str:
    """Spell `into` relative to `root`, refusing a directory outside it.

    ⛔ Raises `ValueError` naming neither path (R7).
    """
    relative = os.path.relpath(Path(into).resolve(), Path(root).resolve())
    spelled = checked_where(PurePosixPath(Path(relative).as_posix()).as_posix())
    if spelled is None:
        raise ValueError("the audio directory is not under the corpus root")
    return spelled


def located(root: Path | str, where: object, filename: str) -> Path | None:
    """Return where a recorded clip is, from the record alone, or `None` if it cannot say."""
    checked = checked_where(where)
    if checked is None:
        return None
    return Path(root) / checked / filename


def audio_dir(root: Path | str, locations: UnitLocations) -> Path:
    """Return where one unit's clips go: its placement answer, rooted — ⛔ never composed here.

    ⛔ **`locations` is the ONE derivation**, the unit's
    `generate.declarations.unit_location`, which takes what `unit_stem` takes,
    the label included. `narrate`, the build's read and the page hold that one
    answer, so no stem is spelled here and no argument of it can be dropped here.

    ⭐ The *"through the placement policy"* half of synthesis: a caller that
    composed the media directory itself would be the second layout authority R4
    removes, and that mistake is invisible under one of the two profiles, whose
    media directory happens to sit where a hand-composed path would put it.
    """
    return Path(root) / Path(str(locations.media_dir(AUDIO_DIRNAME)))
