"""The two bundles this project did not write, and the rules that keep them honest.

**What it does.** Names every vendored file, pairs each bundle with its
licence, and states the header contract that makes "unedited" checkable.

**How you use it.** `is_vendored(name)`, `licence_for(name)`, `VENDORED`.

**Depends on.** `errors` and `source`.

⛔ **Do not edit vendored code.** Not to fix a warning, not to drop a feature
this site never uses. An edited bundle cannot be re-vendored without losing
the edit, and nothing records that it was made. Each file carries a header
saying its version, its licence and the exact command that reproduces it, and
that header is the only permitted modification.

⛔ **A vendored bundle ships with its licence beside it**, in the same
directory, because a licence in a different tree is one that goes missing when
the directory is copied. `test_vendored` fails a bundle with no licence and a
licence with no bundle.

⚠️ **The bundles contain URLs this site never fetches.** `plyr.js` carries
endpoints for streaming providers the reading floor does not use. R8 is about
what a *page requests*, not about what strings a minified bundle contains, so
the check is that every network path is disarmed by configuration —
`loadSprite: false`, `iconUrl: ""`, `blankVideo: ""` — and that no **authored**
asset names a remote host at all.
"""

from __future__ import annotations

from studyforge.describe import describe
from studyforge.render.pageassets.errors import AssetError
from studyforge.render.pageassets.source import text

#: `bundle file -> its licence file`. ⭐ Stated, not derived from a naming
#: convention: a convention would silently pair a bundle with the wrong
#: licence the first time somebody vendored `foo.min.js` beside `foo.LICENSE`.
VENDORED = {
    "prism.js": "prism.LICENSE",
    "plyr.js": "plyr.LICENSE",
    "plyr.css": "plyr.LICENSE",
    "plyr.svg": "plyr.LICENSE",
}

#: Every vendored header must say all three, or "unedited" is unverifiable:
#: what it is, what licence it is under, and how to reproduce it.
HEADER_MARKERS = ("MIT", "VENDORED, UNMODIFIED", "re-vendor")

#: How many leading characters of a bundle count as its header. ⚠️ A minified
#: bundle is one enormous line, so this is a character count and not a line
#: count — reading "the first five lines" of `plyr.js` reads the whole file.
#:
#: ⛔ **900 rather than 600.** A bundle declares every name its grammars carry on one
#: `Languages:` line, and at eleven Prism components that line alone is 170
#: characters: inside a 600-character window the header had **2** characters of
#: margin left, which strands the next grammar and buys the reproduce recipe
#: nothing. ⭐ It is safe because the invariant was never the window — it is
#: `grammars.declared_languages`, which REFUSES a header carrying two
#: `Languages:` lines — and no Prism 1.30.0 component vendored here carries
#: that marker at all.
HEADER_CHARS = 900


def is_vendored(name: str) -> bool:
    """Whether `name` is a file this project did not write."""
    return name in VENDORED


def licence_for(name: str) -> str:
    """Return the licence filename covering `name`, or raise naming it."""
    try:
        return VENDORED[name]
    except KeyError:
        raise AssetError(
            f"that is not a vendored bundle and has no licence of its own; "
            f"this build vendors {sorted(VENDORED)}, and was given {describe(name)}"
        ) from None


def header_of(name: str) -> str:
    """Return the vendoring header of `name` — what says where it came from."""
    return text(name)[:HEADER_CHARS]
