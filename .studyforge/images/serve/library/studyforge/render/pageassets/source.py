r"""The asset directory: the page's CSS and JS as files, read exactly.

**What it does.** Finds the parts on disk and returns their exact text.

**How you use it.** `text("palette.css")`, `names()`; `data(name)` and
`font_names()` for the vendored faces.

**Depends on.** `errors` and `pathlib`.

⛔ **Read as bytes and decoded, never `read_text`.** Universal-newline
translation rewrites a `\\r\\n` into `\\n` on the way in and silently changes
what the page ships. These files are composed into output compared byte for
byte (R10), so the read has to be exact.

⚠️ **`render/assets/` is a data directory, not a package.** It holds no
`__init__.py` and never will; `[tool.setuptools.package-data]` is what ships
it, and that declaration already exists — a new asset is a file, not build
configuration.

⛔ **Not to be confused with a corpus's generated media.** This directory is
the *source* of the reading surface; what a build writes beside a page is the
*product*, and editing the product is editing a build artefact.
"""

from __future__ import annotations

from pathlib import Path

from studyforge.describe import describe
from studyforge.render.pageassets.errors import AssetError

#: Where the parts live: beside the code that composes them, so an editor
#: opening `render/` sees the page's CSS next to the module that reads it.
ASSET_DIR = Path(__file__).resolve().parent.parent / "assets"

#: What counts as a part — a source file composed into, or linked by, a page.
#: The icon sprite is one: it is substituted into `video-player.js` rather
#: than fetched, so `.svg` belongs here.
PART_SUFFIXES = (".css", ".js", ".svg")

#: A vendored face. ⛔ Never a part: it is binary, it is never
#: composed as text, and `faces` is the one reader of it — it arrives in the
#: page stylesheet as base64, so no page ever requests a font file (R8).
FONT_SUFFIX = ".woff2"

#: A vendored dependency's licence sits in the same directory, because it
#: belongs beside the code it covers — but nothing composes it into a page,
#: so it is not a part.
LICENCE_SUFFIX = ".LICENSE"


def text(name: str) -> str:
    """Return the named part's exact text — `text("palette.css")`.

    ⛔ Raises `AssetError` rather than returning an empty string. A page
    rendered without its stylesheet would be well-formed, unreadable, and
    would fail nothing.
    """
    path = ASSET_DIR / name
    if "/" in name or "\\" in name or name in ("", ".", ".."):
        raise AssetError(
            f"asset name must be one filename in the asset directory, got {describe(name)}"
        )
    try:
        return path.read_bytes().decode("utf-8")
    except OSError as error:
        # ⛔ `error.strerror`, never `error`: an OSError formats itself with
        # the absolute path it was given, and a refusal is read in a log (R7).
        raise AssetError(f"cannot read the page asset {name!r}: {error.strerror}") from None
    except UnicodeDecodeError:
        raise AssetError(f"the page asset {name!r} is not valid UTF-8") from None


def data(name: str) -> bytes:
    """Return the named vendored face's exact bytes — `data("Charis-Regular.woff2")`.

    ⛔ Only a `FONT_SUFFIX` file: a part is text and is read with `text`, and a
    reader that took any name would be a second way into the directory.
    """
    if "/" in name or "\\" in name or not name.endswith(FONT_SUFFIX):
        raise AssetError(f"a face must be one {FONT_SUFFIX} file in the asset directory")
    try:
        return (ASSET_DIR / name).read_bytes()
    except OSError as error:
        # ⛔ `error.strerror`, never `error`: it would carry the absolute path (R7).
        raise AssetError(f"cannot read a vendored face: {error.strerror}") from None


def font_names() -> tuple[str, ...]:
    """Every vendored face on disk, sorted (R10: never directory order)."""
    return tuple(
        sorted(
            path.name
            for path in ASSET_DIR.iterdir()
            if path.is_file() and path.suffix == FONT_SUFFIX
        )
    )


def names() -> tuple[str, ...]:
    """Every part on disk, sorted.

    ⚠️ Sorted rather than in directory order. R10 forbids depending on
    filesystem enumeration, which differs between machines — and this is the
    only place in the package that enumerates at all. ⛔ **Nothing composes a
    bundle from this**: what goes into a page, and in what order, is stated in
    `bundle`, because CSS order is meaning.
    """
    return tuple(
        sorted(
            path.name
            for path in ASSET_DIR.iterdir()
            if path.is_file() and path.suffix in PART_SUFFIXES
        )
    )


def licence_names() -> tuple[str, ...]:
    """Every licence file on disk, sorted."""
    return tuple(
        sorted(
            path.name
            for path in ASSET_DIR.iterdir()
            if path.is_file() and path.name.endswith(LICENCE_SUFFIX)
        )
    )
