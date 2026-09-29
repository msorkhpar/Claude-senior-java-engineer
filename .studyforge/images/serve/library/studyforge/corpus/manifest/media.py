"""Whether a corpus commits its generated media, and what it does when that stops fitting.

**What it does.** Models `corpus.json`'s optional `media` key: a commit mode
and, for the mode that has them, the limits that decide when a corpus has
outgrown the default.

**How you use it.** `parse_media(value)`; an absent key yields the default
policy rather than `None`, so no caller ever asks "did they declare one?".

**Depends on.** `errors`.

⭐ **The default is `auto`, and `auto` commits.** *Regenerable is not the same
as available*: a clone that carries its own audio speaks with no synthesis
service, no GPU and no network, and that is what R8 is for. A corpus that
ignores its media asks every reader to stand up a service before they can hear
anything.

⛔ **The default has a ceiling, and crossing it is a decision rather than an
accident.** The extraction source reached **11.42 GiB of pack against a ~5 GB
soft limit, with one file at 150.9 MiB against a hard 100 MiB per-file block**
— and found out when the push became *impossible*, after the history already
held the blob. So the policy is manifest data, the footprint is measured,
and a corpus that crosses its limits **stops and says so**, naming the
number and the limit. ⛔ It never silently switches to ignoring media, which
would produce clones that are silent with no error, and it never silently
keeps committing.

⛔ **The two byte-limit field names are CONFIRMED and FROZEN, and the rename
once offered is CLOSED** (R9). ⚠️ The
paragraph that stood here still described that window as open long after it
had shut, which is how a stale offer gets taken up by somebody moving fast.

## ⭐ Three limits, and only two of them have a default

⚠️ **`max_files` bounds the file COUNT, and it is the limit narration actually
needs.** A corpus's clips are many and small: 20 000 of them at 20 KB each is
400 MB — under a 5 GB total and under a 100 MiB per-file block, so **both byte
limits say yes** while the clone is a repository whose every `status`, `clone`
and `checkout` pays for 20 000 paths. ⛔ That is the case neither byte ceiling
can state, and it is why this is a third limit rather than a lonely one.

⛔ **It has NO default, deliberately.** The two byte defaults trace to §5's
measured numbers; there is no measured count in this project, and a number
invented here would make every existing corpus's build depend on a ceiling
nobody chose. ⭐ **Unstated means unbounded** — the count is measured and
reported either way, and it refuses only where a corpus asked it to.

⛔ **`max_files` is `corpus_api: 3`'s key** (`document.py`'s `KEY_VERSIONS`).
An older build refuses an unknown key by name and blames the corpus for the
framework's age, which is exactly what R9 versions — so a new field and its
version bump go together, as `content.not_material` did at `2`.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.corpus.manifest.errors import ManifestError
from studyforge.describe import describe, describe_keys

#: `always` — commit media whatever the size. `never` — a corpus that has
#: made the decision to hold its media elsewhere. `auto` — commit while it
#: fits, and stop loudly when it does not.
COMMIT_MODES = ("always", "never", "auto")

DEFAULT_COMMIT = "auto"

#: §5's measured numbers: a ~5 GB soft limit on a repository, and a hard
#: 100 MiB per-file block. Defaults rather than laws — a corpus may declare
#: its own, and its host may differ.
DEFAULT_MAX_TOTAL_BYTES = 5_000_000_000
DEFAULT_MAX_FILE_BYTES = 100 * 1024 * 1024

#: ⛔ **The count limit has no default and `None` is that statement.** §5
#: measured the two byte numbers; nobody measured a count, and a ceiling
#: invented here would refuse a build on a number no round chose. ⭐ A corpus
#: that wants one declares it.
DEFAULT_MAX_FILES: int | None = None


@dataclass(frozen=True, slots=True)
class MediaPolicy:
    """What this corpus does with the media it generates."""

    commit: str = DEFAULT_COMMIT
    max_total_bytes: int = DEFAULT_MAX_TOTAL_BYTES
    max_file_bytes: int = DEFAULT_MAX_FILE_BYTES
    #: ⚠️ **`None` is *unbounded*, never *zero*.** A reader that treated an
    #: unstated ceiling as `0` would refuse the first clip every corpus
    #: generates, which is the direction this field must never fail in.
    max_files: int | None = DEFAULT_MAX_FILES

    @property
    def commits(self) -> bool:
        """Whether generated media is committed under this policy.

        ⚠️ `auto` answers **yes**. It is not "decide later": it is "commit,
        and stop loudly when the limits say to". A reader of this property
        that treated `auto` as undecided would produce the silent clone the
        whole policy exists to prevent.
        """
        return self.commit in ("always", "auto")

    @property
    def has_limits(self) -> bool:
        """Whether the limits are consulted at all — only `auto` consults them."""
        return self.commit == "auto"


#: ⭐ Returned when `media` is absent. Stated as a value rather than left
#: implicit, because the manifest's contract is that the default is **asserted
#: rather than assumed**: a test compares against this object.
DEFAULT_MEDIA = MediaPolicy()


def parse_media(value: object) -> MediaPolicy:
    """Build a `MediaPolicy`; an absent `media` key yields `DEFAULT_MEDIA`."""
    if value is None:
        return DEFAULT_MEDIA
    if not isinstance(value, dict):
        raise ManifestError(f"'media' must be an object, got {describe(value)}")
    known = {"commit", "max_total_bytes", "max_file_bytes", "max_files"}
    unknown = sorted(set(value) - known)
    if unknown:
        raise ManifestError(
            f"'media' has unknown key(s), {describe_keys(unknown)}; expected {sorted(known)}"
        )
    commit = value.get("commit", DEFAULT_COMMIT)
    if commit not in COMMIT_MODES:
        raise ManifestError(
            f"'media.commit' must be one of {list(COMMIT_MODES)}, got {describe(commit)}"
        )
    return MediaPolicy(
        commit=commit,
        max_total_bytes=_limit(value, "max_total_bytes", DEFAULT_MAX_TOTAL_BYTES),
        max_file_bytes=_limit(value, "max_file_bytes", DEFAULT_MAX_FILE_BYTES),
        max_files=_count(value),
    )


def _limit(value: dict, field: str, default: int | None = None, unit: str = "bytes") -> int:
    """Return one limit: a positive int, or the default when unstated.

    ⚠️ **The unit is named because the message is read by a person editing
    `corpus.json`.** *"must be a positive int of bytes"* against `max_files`
    would send them to convert a count into a size.

    ⛔ **A `None` default means the key is not optional here** and an absent
    one falls into the same refusal as a bad one — which is what `_count`
    relies on, having already answered the absent case itself.
    """
    limit = value.get(field, default)
    if not isinstance(limit, int) or isinstance(limit, bool) or limit < 1:
        raise ManifestError(
            f"'media.{field}' must be a positive int of {unit}, got {describe(limit)}"
        )
    return limit


def _count(value: dict) -> int | None:
    """Return the optional file-count ceiling, or `None` when it is unstated.

    ⛔ **Absent and `null` are different answers.** An absent key is *no
    ceiling*; an explicit `null` is a corpus that tried to state one and wrote
    something that is not a count, and it is refused like any other bad value.
    """
    if "max_files" not in value:
        return DEFAULT_MAX_FILES
    return _limit(value, "max_files", unit="files")
