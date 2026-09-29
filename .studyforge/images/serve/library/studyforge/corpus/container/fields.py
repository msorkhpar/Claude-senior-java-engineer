"""Reading one declared field, and refusing it without reproducing it.

**What it does.** Reads the small values a container map declares — required
text, optional text, an optional source path, an optional slug — and refuses
each of them in a message that names the field and the fault.

**How you use it.** `required_text`, `optional_text`, `optional_path`,
`optional_origin`, `optional_slug`, `optional_label`, and
`is_filename_component` for the one permitted-set rule a filename component
obeys. ⭐ A value is described by `studyforge.describe.describe`, which names
**an empty string as one**.

**Depends on.** `studyforge.address` for what a slug is,
`studyforge.sourcepath` for what a source path is, `studyforge.describe` for
how a value is named, and this package's `errors`.

⛔ **A refusal never quotes the value.** This is not general caution: the field
this module refuses most often is `origin`, and **the one shape being refused
there is precisely the shape that carries a home directory** — so a message
quoting it would copy personal data into a log *from the check that exists to
catch it* (R7).

⭐ **The rule has one home and this module keeps no copy**: copies of it
disagree about integers, booleans and the empty string, and nobody decides
which.

⚠️ **The same holds for paths.** `optional_path` and
`placement.profile.origin_directory` both ask `studyforge.sourcepath`, because
two forbidden lists disagree and the gap between them is reachable; it states
the permitted set
instead. ⭐ Each package keeps its own error type and its own sentence — the
rule is `sourcepath`'s, the document is this contract's.
"""

from __future__ import annotations

from studyforge.address import is_slug
from studyforge.address.slug import SLUG_PERMITTED
from studyforge.corpus.container.errors import ContainerError
from studyforge.describe import describe as said
from studyforge.sourcepath import SOURCE_PATH_DESCRIBED, source_path_fault

#: The characters a generated filename component may carry — **a permitted
#: set, and deliberately not a forbidden one**.
#:
#: ⛔ A forbidden list is an **open set and cannot be finished**: every
#: character nobody thought of is permitted by default, so it is wrong the
#: moment it is written and stays wrong silently. A list such as
#: `"/\\ \t\n\r"` passes **seven shapes into a filename**: a vertical tab, a
#: form feed, a non-breaking space, U+2028, `"`, `:` and `*`. Two of those —
#: `:` and `"` — break the `file://` floor (R8), so an open set is not a
#: tidiness question.
#:
#: ⭐ **Derived from `is_slug`, never re-typed.** The permitted class is *what a
#: slug accepts*, plus `.` so `4.4.1` passes — and it is computed by asking, so
#: a second spelling of the slug rule cannot exist here to drift from the
#: first. ⛔ A constant exported can be re-typed; a constant *derived* cannot
#: be re-typed at all. `tests/.../test_fields.py`
#: pins the resulting set literally, so a change to `is_slug` is a decision
#: somebody makes rather than one that arrives.
#:
#: ⚠️ **The derivation itself lives once, in `address.slug`**, and this is
#: `SLUG_PERMITTED | {"."}`. A refusal needs the same set to describe a slug fault
#: without reproducing the value; two copies of one *computation* is the same
#: defect as two copies of one constant, one step earlier.
#:
#: ⚠️ **Lowercase, and that is the point, not an oversight.** `A` and `a` are
#: one filename on a case-insensitive filesystem, and `sibling` places twenty
#: units in a single directory — so an uppercase label is a collision this
#: framework would generate and never detect on the machine that generated it.
FILENAME_PERMITTED = SLUG_PERMITTED | {"."}

#: ⛔ A filename component must *begin* with one of these. A leading `.` is a
#: hidden file, and a leading `-` is read as an option by half the tools that
#: will ever list the directory; both are inside `FILENAME_PERMITTED` because
#: they are wanted in the middle (`4.4.1`, `part-two`), and neither is wanted
#: first. ⚠️ This is also what refuses a label of `.` or `..` — **a path
#: traversal every character of which is permitted**, and the thing a permitted
#: set alone does not give you.
FILENAME_MUST_START_WITH = frozenset(
    character for character in FILENAME_PERMITTED if is_slug(character)
)

#: How a refusal says the rule. ⛔ Stated once, so the two callers cannot
#: describe one class two ways — which is the failure this constant replaces.
FILENAME_PERMITTED_DESCRIBED = (
    "lowercase ASCII letters, digits, and . or -, beginning with a letter or digit"
)

#: The two keys an object `origin` carries. ⛔ Exactly these, both
#: required: a `path` alone is the string form written the long way, and a
#: `section` alone is a region of nothing. ⚠️ Neither is a *unit* key — the
#: object is the value of `origin`, so `UNIT_KEYS` is unchanged.
ORIGIN_KEYS = ("path", "section")


def is_filename_component(value: object) -> bool:
    """Return whether `value` may be used, unchanged, as one component of a filename.

    ⭐ **The one predicate, and it lives here** — the container map refuses a
    label where it enters, and `placement.names.label_of` refuses one where a
    filename is minted, and both ask *this*. Two spellings of one rule is the
    defect; the missing character was only how it showed.

    ⚠️ ASCII and lowercase by construction, not by oversight. A label reaches a
    `file://` URL, a directory listing, a shell completion, a case-insensitive
    filesystem and a discovery scan, and the set that survives all five
    unchanged is small. A corpus whose display numbering is not in it records a
    label that is, and keeps the original in its **title**, which is under no
    filename constraint at all.
    """
    return (
        isinstance(value, str)
        and value != ""
        and value[0] in FILENAME_MUST_START_WITH
        and set(value) <= FILENAME_PERMITTED
    )


def required_text(value: object, what: str, where: str) -> str:
    """Return a field that must be present and must be non-empty text."""
    if not isinstance(value, str) or not value.strip():
        raise ContainerError(f"{where} has no {what}; it is required and must be text")
    return value


def optional_text(value: object, what: str, where: str) -> str | None:
    """Return a field that may be absent, but must be non-empty text if present.

    ⚠️ Absent and empty are different: absent says "nothing to say", empty says
    somebody meant to write something here. Only one of those is a decision.
    """
    if value is None:
        return None
    if not isinstance(value, str) or not value.strip():
        raise ContainerError(
            f"{where} declares {what} as {said(value)}; it must be text, or absent"
        )
    return value


def optional_path(value: object, what: str, where: str) -> str | None:
    """Read an `origin`, refusing an absolute or escaping path — **never echoing it**.

    ⛔ The one shape being refused here is precisely the shape that carries a
    home directory, so a refusal quoting the value would copy personal data
    into a log from the check that exists to catch it (R7).
    The message describes the fault and names the field.
    """
    if value is None:
        return None
    if not isinstance(value, str) or not value.strip():
        raise ContainerError(
            f"{where} declares {what} as {said(value)}; it must be a path, or absent"
        )
    fault = source_path_fault(value)
    if fault is not None:
        raise ContainerError(
            f"{where} declares {what} as {fault}. It must be {SOURCE_PATH_DESCRIBED}, "
            f"and it is not quoted here because that shape is where a home directory "
            f"lives."
        )
    return value


def optional_origin(value: object, what: str, where: str) -> tuple[str | None, str | None]:
    """Read an `origin`: a whole file, a **region** of one, or absent.

    Returns `(path, section)`. ⭐ **The section is the second half of one
    declared field and never a second key**: `origin` is one thing a corpus
    declares, and every reader that wants a *path* — placement, media, the
    `.parent` `origin_directory` takes — goes on getting a plain string out of
    the first half whichever shape was written.

    ⛔ **A region is bounded by a heading, so `section` is the exact text of
    one**. A line range would couple the manifest to a file's byte
    layout and an anchor would couple it to a renderer's slug rules; the
    heading is the only bound `validate` can find without the Markdown reader,
    which is the independence `check_completeness` is built on.
    """
    if value is None:
        return None, None
    if isinstance(value, dict):
        return _region(value, what, where)
    return optional_path(value, what, where), None


def _region(value: dict, what: str, where: str) -> tuple[str, str]:
    """Read the object form of an `origin` — ⛔ both keys, both required."""
    if set(value) != set(ORIGIN_KEYS):
        raise ContainerError(
            f"{where} declares {what} as an object that is not a region; a region is "
            f"{list(ORIGIN_KEYS)}, both required. A path alone is the string form "
            f"written the long way, and a section alone is a region of nothing. "
            f"The keys are not reproduced here, since a refusal never quotes a value that may be "
            f"personal."
        )
    path = optional_path(value["path"], f"{what} path", where)
    if path is None:
        raise ContainerError(
            f"{where} declares {what} with no path; a region is a region of a file"
        )
    return path, required_text(value["section"], f"{what} section", where)


def optional_slug(value: object, what: str, where: str) -> str | None:
    """Read a `url_slug`: the source's own addressing for a unit, or absent.

    ⚠️ Required to be a slug, and that is measured rather than assumed: of the
    extraction source's **1290** unit entries, **0** carry a `url_slug` that is
    not one. ⛔ Converted to a `ContainerError` rather than allowed to escape as
    `studyforge.address`'s `AddressError`, following the manifest's precedent exactly — the
    rule is `studyforge.address`'s, the document is this contract's.
    """
    if value is None:
        return None
    if not isinstance(value, str) or not is_slug(value):
        raise ContainerError(
            f"{where} declares {what} as {said(value)}; it must be a slug, or absent"
        )
    return value


def optional_label(value: object, what: str, where: str) -> str | None:
    """Read a unit's own display numbering, or absent.

    ⛔ Refused unless it is a usable filename component, because it becomes
    one downstream (`placement.names.label_of`). ⛔ The refusal names the **permitted**
    class and never reproduces the value: a label is read straight out of a
    file somebody else wrote, and describing rather than echoing is this
    module's whole job.

    ⭐ A map that accepted `a/b` would produce a corpus that **validates and
    then fails at render** — a milestone later, in another package, with
    nothing between the two saying why. Refusing it where it enters means the
    failure arrives next to the file that caused it.
    """
    text = optional_text(value, what, where)
    if text is None:
        return None
    if not is_filename_component(text):
        raise ContainerError(
            f"{where} declares {what} as text that cannot become part of a filename. "
            f"A label may carry only {FILENAME_PERMITTED_DESCRIBED}. Accepted here, "
            f"it would validate and then fail at render. It is not reproduced, "
            f"because a declared field is read out of a file somebody else wrote."
        )
    return text
