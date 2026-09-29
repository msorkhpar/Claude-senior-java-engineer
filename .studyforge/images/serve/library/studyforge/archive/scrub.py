r"""R7's enforcement: scrub what this build wrote, refuse what the source wrote.

**What it does.** Recognises the personal-data shapes that reach a document
*because of whose machine and whose account ran the build* — an absolute home
path, an email address, a bearer token — and offers two responses to them:
`scrub` rewrites, `assert_clean` refuses.

**How you use it.** `assert_clean(value, where)` at every disk and wire
boundary; it takes a decoded string or a whole decoded document and raises
`PersonalDataLeak`. `scrub(text)` on a line this framework is about to emit.
`leaks(value, where)` when a caller wants every finding rather than the first
— `studyforge validate` reports, it does not raise.

**Depends on.** `re` and `archive.samples`, and nothing else. ⛔ Not on `os`, `pwd`,
`socket`, `getpass` or `subprocess`: this gate must hold no value and read no
environment, and `test_scrub.py` asserts that of this module's imports.

## Which of the two, and why it is not a contradiction

⭐ **Scrub our words; refuse the source's.** A log line, a subprocess's output,
a path in a progress report — this framework wrote those, so rewriting one
loses nothing. An archive document is the verbatim record of what a source
said (§6): rewriting it would corrupt the record *and* break the
`content_sha256` that covers it, and it would leave nobody knowing personal
data had ever been there. So the same module scrubs on the way in, upstream,
and refuses at the boundary — belt and braces, and a match at the inner gate
is not redundancy but the news that an upstream stage failed (R6).

## The pattern set is ruled, and it is short

⭐ **The gate's question is not "is this string identifying?" but "did this
build put it there?"** Four shapes pass
that test and they are all that ship:

| shape | why it is environmental |
|---|---|
| home path | `/home/<name>` carries the account name of the machine that ran |
| local hostname | `<host>.local` **is** the name of the machine that ran |
| email address | an address in captured material came from a signed-in session |
| bearer token | a credential minted for whoever authenticated |

⚠️ **The fourth arrived late and it was drift, not a new idea.** The
repository hygiene check has always swept `<host>.local` and this gate
never did — ⛔ and a machine name is personal data in an archive document
exactly as much as in a source file. ⭐ *One shape vocabulary, two policies*:
the two checks may differ in what they **do** and may not differ in what they
**recognise** without saying why. The shared shape table is data both sides
assert against, and every divergence left in it carries a reason.

⛔ **No payment-card pattern.** One designed shape's material carries 96 card-shaped
digit strings because a test PAN is the *subject of the lesson*; a gate that
refuses rather than rewrites would refuse the whole corpus with a diagnosis
that looks exactly like a leak. The gate is not a content classifier.

⛔ **No username pattern, ever.** To match "this is the user's account name"
the gate must *hold* the account name, which is the exact datum R7 forbids it
to hold; the unanchored alternative matches every symbol in every codebase. A
gate that must contain the secret to detect the secret is self-defeating. The
account name is caught where it has a structural anchor — the segment after
`/home` — and nowhere else.

⛔ **And the gate matches shape only, never a value derived at run time.**
Deriving this machine's account name and comparing would be a gate whose
verdict differs by machine, and `assert_clean` is half of what `studyforge
validate` promises an adapter (R2). A contract that says "valid here, invalid
there" is not a contract. ⚠️ This is where this gate and the repository
hygiene check (`tests.floor.personal_data`) part company hardest: that one
is a local development check, so a machine-specific verdict is exactly what it
wants. **The two gates have different subjects and neither imports the
other's patterns.**

## ⛔ This gate is an open set and cannot be closed, so it is never the only layer

⭐ **Where a legal set can be written down, write it down.** Keys, versions,
profiles, contract fields, slugs, **and a path inside the source** all have an
end, and each of those is enforced by enumerating what is permitted. ⛔ **This
gate has no such set.** Its permitted set is *"all text that is not personal
data"*, which nobody can write down, so its forbidden list is forced and is
**known-incomplete by construction**. Two unforeseen entries were found
in it, which is simply what an open set does — and the answer to that is never
a longer list.

The answer is that three layers stand between a home directory and disk, and
**each is asserted on its own** (`tests/test_gate_layers.py`), because no one
of them is sufficient:

| layer | subject | its set | on a doubtful value |
|---|---|---|---|
| `studyforge.sourcepath` | every field a reader **types as a path** | enumerable | refuse |
| `assert_clean` (`SHAPES`) | the **source's** free text | open, narrow | let through |
| `scrub` (`SCRUBBED`) | text **this framework wrote** | open, wide | rewrite |

⚠️ **Read the third column before adding anything to the second.** Of seven
home-path spellings, the path rule refuses all seven, this gate names three,
and `scrub` rewrites six. ⛔ The seventh — a `home` segment
under a longer prefix, inside free text a source wrote — is **not closable
here**, because `/export/home/<name>/x` and `/var/lib/home/cache/x` are the
same shape. It is closed for every path field and for our own output, and the
residual is stated here.

## Sample data passes: reserved domains and one placeholder home

⭐ **A lesson's sample is not a leak when it is unreachable by construction.**
An address on a reserved domain and the placeholder home path pass, and nothing
else does; `studyforge.archive.samples` states both forms and why the gate
still holds no value and reads no environment.

## The residual class is specified, not built

A second source could legitimately carry a shape this gate owns — a lesson
about HTTP quoting a real support address. When that happens the exemption is
**manifest data read by the gate**, never a pattern hardcoded for one corpus
(R1). Three conditions, so it cannot become an off switch: home paths and
credentials are never declarable; every string that passes only because of a
declaration is counted and named in the build report (*pass loud*); and the
gate still refuses, it just refuses less. ⛔ v1 builds none of it.

## Every gate reads decoded strings

⛔ **`assert_clean` and `scrub_document` ride the same walker**, and the
walker is handed values that have already been decoded. A gate reading a
*serialised* form is not a second check on the first — it is a check on a
different document, inventing the matches that escaping created. Measured: the
extraction source gated rendered JSON, where a newline is the two characters
`\` and `n`, so a decorator on its own line serialised as `...\n@router.get(`
— and `n@router.get` is address-shaped. Three clean lessons were refused.
⚠️ Teaching the pattern that shape would be far worse than the bug.
"""

from __future__ import annotations

import re
from collections.abc import Callable, Iterator

from studyforge.archive.samples import admitted

#: What `scrub` writes in place of each shape. ⭐ Every one is a documented
#: placeholder (`example.invalid`, `Jane Doe`) or an obvious redaction, so a scrubbed line
#: reads as *deliberately* anonymous rather than as a plausible other value.
#:
#: ⭐ **Two of the three are chosen not to match the shape they replace**, so
#: they need no exemption and this module stays clean under the repository
#: hygiene check that sweeps it. ⚠️ `Bearer <redacted>` diverges from the
#: extraction source, which writes `Bearer` followed by the bare word
#: `REDACTED`: eight characters of `[A-Za-z0-9]` after `Bearer` *is* the
#: shape, so that placeholder trips its own pattern — measured, on this
#: sentence, which had to be rewritten to say so. The email placeholder
#: cannot be chosen that way — every well-formed placeholder address is
#: address-shaped — so it sits on a reserved domain, which the gate admits.
HOME_PATH_PLACEHOLDER = "/path/to/project"
EMAIL_PLACEHOLDER = "contact@example.com"
TOKEN_PLACEHOLDER = "Bearer <redacted>"
HOSTNAME_PLACEHOLDER = "host.invalid"

#: `(name, pattern, placeholder)`. ⛔ Every entry is a *shape*: this module
#: contains no real identifier of any kind, which is the property that lets it
#: be committed at all. Order matters — the home-path rule runs first, so
#: `/home/<name>` is consumed before the email rule can see an address hiding
#: further along the same path.
#:
#: ⚠️ **The email local part is one character or more**, unlike the repository
#: hygiene check's, which requires two. That is not drift. This gate reads
#: decoded strings, so `\n@router.get` never reaches it as `n@router.get`; the
#: structural fix is available here and it is the better one. The other gate
#: reads whole files with no decoding stage, has no decoded form to prefer,
#: and had to harden the pattern instead. ⛔ Do not "align" them.
SHAPES: tuple[tuple[str, re.Pattern[str], str], ...] = (
    (
        "home path",
        # The segment after `/home` or `/Users` is an account name, and so is
        # the one a bare `~` is followed by — ⭐ **two spellings of one shape,
        # not two shapes.** The tilde branch requires a following `/` and a
        # leading letter so that `~5/6` and `~50 lines` are not account names,
        # and both branches share the lookbehind, which stops the same letters
        # mid-path: `/var/lib/home/cache` and `foo~bar/` name nobody. ⭐ The
        # prose above writes the shape as `/home/<name>` with angle brackets,
        # outside the character class, so a document explaining this rule
        # stays both swept and clean.
        re.compile(
            r"(?<![\w.~])(?:/(?:home|Users)/[A-Za-z0-9._\-]+|~[A-Za-z][A-Za-z0-9._\-]*(?=/))"
        ),
        HOME_PATH_PLACEHOLDER,
    ),
    (
        "local hostname",
        # ⛔ The mDNS suffix, and the trailing guard keeps `settings.local.json`
        # — a filename, not a host — out of it. Word for word the repository
        # hygiene check's pattern, because the two gates must
        # share a vocabulary; the placeholder is this module's own.
        re.compile(r"(?<![\w.])[A-Za-z0-9-]+\.local(?![\w.])"),
        HOSTNAME_PLACEHOLDER,
    ),
    (
        "email address",
        re.compile(r"\b[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}\b"),
        EMAIL_PLACEHOLDER,
    ),
    (
        "bearer token",
        re.compile(r"\bBearer\s+[A-Za-z0-9._\-]{8,}"),
        TOKEN_PLACEHOLDER,
    ),
)

#: Shapes a line **this framework wrote** is rewritten for and a corpus is
#: **not refused** for — `scrub` reads these, `assert_clean` never does.
#:
#: ⭐ **The asymmetry is this module's oldest ruling, applied to the pattern
#: set for the first time.** "Scrub our words; refuse the source's" was about
#: which *response* each subject gets; it decides the *width* too, because the
#: two responses have opposite costs. A false positive in `scrub` rewrites one
#: of our own log lines slightly too eagerly and loses nothing. A false
#: positive in `assert_clean` refuses a legitimate corpus with a diagnosis
#: that looks exactly like a leak — the same argument that keeps a card
#: pattern out of the gate entirely.
#:
#: ⛔ **Every entry here is a shape that cannot be told from a benign one.**
#: `/export/home/<name>/x` is a home directory and `/var/lib/home/cache/x` is
#: not, and **nothing about their shape separates them** — which is why the
#: gate must not be the layer that protects a path. The first of those is
#: a live hole; it is closed for every field a reader types as a path
#: (`studyforge.sourcepath`), and it is closed for text this framework emits
#: (here). It stays open for free text a source authored, and that residual is
#: stated rather than left to be discovered.
ALSO_SCRUBBED: tuple[tuple[str, re.Pattern[str], str], ...] = (
    (
        "home path under a longer prefix",
        # ⚠️ No lookbehind, and that is the whole difference from the gate's
        # entry: `/export/home/<name>` matches, and so does the benign
        # `/var/lib/home/cache`. Acceptable here, not acceptable there.
        re.compile(r"/(?:home|Users)/[A-Za-z0-9._\-]+"),
        HOME_PATH_PLACEHOLDER,
    ),
    (
        "home path with Windows separators",
        # ⛔ `\\host\home\<name>` is a home directory that no POSIX
        # predicate calls absolute and no `/`-anchored pattern sees, so it is
        # named here in its own spelling.
        re.compile(r"\\(?:home|Users)\\[A-Za-z0-9._\-]+"),
        HOME_PATH_PLACEHOLDER,
    ),
    (
        "a home directory the shell would expand",
        # A bare `~/` names *a* home, never *whose*, so it carries no identity
        # and the gate has no business refusing a lesson that says `cd ~/src`.
        # In our own output it is still a build-machine path and is rewritten.
        re.compile(r"(?<![\w.~])~(?=/)"),
        HOME_PATH_PLACEHOLDER,
    ),
)

#: What `scrub` reads: the gate's shapes first, then the ambiguous ones. ⭐ A
#: superset by construction rather than by assertion, so the two sets can
#: never drift into disagreeing about a shape they share.
SCRUBBED: tuple[tuple[str, re.Pattern[str], str], ...] = SHAPES + ALSO_SCRUBBED


class PersonalDataLeak(Exception):
    """Personal data reached a boundary that refuses it (R7).

    ⛔ The message names the *shape* and the *location*, never the matched
    text. A refusal that quotes the leak has only relocated it into a
    traceback, a CI log, or an issue somebody pasted it into.
    """


def _article(noun: str) -> str:
    """Return `"a"` or `"an"`, so a refusal reads as a sentence."""
    return "an" if noun[:1].lower() in "aeiou" else "a"


def scrub(text: str) -> str:
    """Replace every personal-data shape in one string with its placeholder.

    For text **this framework wrote** — a log line, a subprocess's output, a
    path in a report. ⛔ Not for archive content: see the module contract.
    Idempotent, so a line may pass through more than one stage.
    """
    out = text or ""
    for _name, pattern, placeholder in SCRUBBED:
        out = pattern.sub(placeholder, out)
    return out


def shape_in(text: str) -> str | None:
    """Name the first personal-data shape in `text`, or `None`.

    ⛔ Returns the *name* of what matched and never the matched text, so the
    value has nowhere to escape to. A match equal to that shape's own
    placeholder, or sample data `samples.admitted` names, is not a leak.
    """
    for name, pattern, placeholder in SHAPES:
        for match in pattern.finditer(text or ""):
            if match.group(0) == placeholder or admitted(name, match.group(0)):
                continue
            return name
    return None


def _walk(value: object, visit: Callable[[str, str], str], where: str) -> object:
    """Rebuild `value`, replacing every string it reaches with `visit(where, text)`.

    ⛔ **The one walker.** `scrub_document` uses the rebuilt result and
    `leaks` throws it away, so the two can never come to disagree about which
    strings a document has — which is what "both layers ride the same walker"
    means. Dict **keys** are visited too: an asset map keyed by a filesystem
    path is as much a leak as one valued by it.
    """
    if isinstance(value, str):
        return visit(where, value)
    if isinstance(value, dict):
        out: dict[object, object] = {}
        for index, (key, item) in enumerate(value.items()):
            # ⛔ The key's own location is positional, not `f"...{key}"`: a
            # key that *is* the leak would otherwise be echoed by the message
            # reporting it.
            named = visit(f"{where} key {index}", key) if isinstance(key, str) else key
            out[named] = _walk(item, visit, f"{where}.{named}")
        return out
    if isinstance(value, (list, tuple)):
        items = [_walk(item, visit, f"{where}[{index}]") for index, item in enumerate(value)]
        return items if isinstance(value, list) else tuple(items)
    if value is None or isinstance(value, (bool, int, float)):
        return value
    # R6: a type the walker does not understand may be hiding strings, so it
    # is refused rather than skipped. ⛔ Names the type and never `repr(value)`
    # — the repr of an unknown object is exactly the thing this gate exists to
    # keep out of a traceback.
    raise TypeError(f"{where} is a {type(value).__name__}; the gate reads decoded JSON only")


def scrub_document(value: object, where: str = "document") -> object:
    """Return `value` with every string scrubbed, structure preserved.

    For a document **this framework generated**. ⛔ Never for archive content,
    which is refused rather than rewritten.
    """
    return _walk(value, lambda _at, text: scrub(text), where)


def leaks(value: object, where: str) -> Iterator[tuple[str, str]]:
    """`(location, shape name)` for every personal-data shape in `value`.

    Non-raising, for callers that report rather than refuse — `studyforge
    validate` lists findings. ⛔ Yields the shape's name, never the value.
    """
    found: list[tuple[str, str]] = []

    def visit(at: str, text: str) -> str:
        name = shape_in(text)
        if name is not None:
            found.append((at, name))
        return text

    _walk(value, visit, where)
    yield from found


def assert_clean(value: object, where: str) -> None:
    """Raise `PersonalDataLeak` if `value` carries personal data (R7).

    The gate. Takes a decoded string or a whole decoded document — one name,
    so no call site can reach for the weaker of two. ⛔ `where` has no
    default: the refusal is forbidden to carry the matched text, so its
    location is the only actionable thing it may say, and a call site that
    omitted it would leave a refusal nobody can act on.

    ⭐ `where` is itself scrubbed before it is formatted in. A caller that
    passes an absolute path would otherwise make this gate emit the leak it
    exists to prevent — which is the general form of the rule that an
    exception object is never formatted into a message.
    """
    for at, name in leaks(value, where):
        raise PersonalDataLeak(
            f"{scrub(at)} carries {_article(name)} {name}, which is personal data; "
            f"refusing to write it rather than rewriting the record"
        )
