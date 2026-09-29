r"""The page's stylesheet and script: source files on disk, composed by code.

**What it does.** Owns `render/assets/` — the reading surface's CSS and JS as
real files — and composes them into the two artefacts every generated page
links.

**How you use it.**

    from studyforge.render.pageassets import written_files

    for name, body in written_files().items():
        (out / name).write_text(body, encoding="utf-8", newline="\n")

**Depends on.** The standard library and `render/assets/`. ⛔ Nothing else in
`studyforge`: assets know nothing about corpora, addresses or documents, and a
stylesheet that had to be told which corpus it was for would be the source
knowledge R1 forbids.

## The four rulings this package exists to hold

⛔ **R13 — markup, styling and scripts are source files, never code strings.**
The inherited cost is on record: 80 KB of triple-quoted Python meant that
changing a colour was editing Python, in a file no editor would highlight,
lint or fold. Here a colour is a line in `palette.css`. ⚠️ The boundary is
stated too — loop bodies and inline wrappers stay in code, because a file for
a closing tag removes no duplication and adds a hop.

⛔ **R8 — the site works over `file://`, with no network and no server.** Every
asset is local, every face is vendored and embedded, and the icon sprite is
substituted rather than fetched. A served origin adds the API, progress and
Run/Submit; it is never a prerequisite for *reading*.

⛔ **R10 — what is emitted is byte-for-byte reproducible.** No clock, no
directory order, no set iteration: the bundle order is a stated tuple, and the
one function that enumerates the directory sorts and is used by nothing that
composes.

⛔ **A shared asset carries a plain name, never a content digest** (§8.2). A
digest renames the file and rewrites every page that links it whenever a
colour changes.

## What is in the package

| Module | Owns |
|---|---|
| `source` | the directory, and reading one part exactly |
| `bundle` | what goes into a page, in what order, under what name |
| `vendored` | the two bundles this project did not write, and their licences |
| `grammars` | which fence languages the vendored highlighter covers, and the plain fallback |
| `surface` | the class names and `data-*` hooks the stylesheet targets |
| `errors` | `AssetError`, the only exception any of it raises |

⭐ **Assets are shared and linked, never inlined.** They were 79% of each 60 KB
page in the extraction source and byte-identical across all of them; at 1,290
units that is roughly 60 MB of duplication for no reader-visible gain.

⛔ **Highlighting happens in the browser, never at build time.** Building spans
into every page inflates all of them, puts a lexer's output permanently on
disk, and would feed markup to the narration extractor. ⚠️ A language with no
grammar renders as the DECLARED plain fallback, and its caption says so. The
framework knows no corpus's languages (R1): what is highlightable is what the
vendored bundle declares in its header, and a test checks that line against
the grammars the bundle carries (`grammars`).
"""

from __future__ import annotations

from studyforge.render.pageassets.bundle import (
    JOIN,
    SCRIPT_NAME,
    SCRIPT_PARTS,
    SPRITE_PART,
    SPRITE_PLACEHOLDER,
    STYLE_PARTS,
    STYLESHEET_NAME,
    compose,
    script,
    stylesheet,
    written_files,
)
from studyforge.render.pageassets.errors import AssetError
from studyforge.render.pageassets.grammars import (
    PLAIN,
    falls_back,
    grammar_for,
    highlighted_languages,
)
from studyforge.render.pageassets.source import (
    ASSET_DIR,
    LICENCE_SUFFIX,
    PART_SUFFIXES,
    licence_names,
    names,
    text,
)
from studyforge.render.pageassets.surface import (
    HOOK_CLASSES,
    SURFACE_CLASSES,
    SURFACE_HOOKS,
    class_for,
)
from studyforge.render.pageassets.vendored import (
    HEADER_CHARS,
    HEADER_MARKERS,
    VENDORED,
    header_of,
    is_vendored,
    licence_for,
)

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.render.pageassets.bundle` directly is a consumer this contract
#: failed.
__all__ = [
    "ASSET_DIR",
    "HEADER_CHARS",
    "HEADER_MARKERS",
    "HOOK_CLASSES",
    "JOIN",
    "LICENCE_SUFFIX",
    "PART_SUFFIXES",
    "PLAIN",
    "SCRIPT_NAME",
    "SCRIPT_PARTS",
    "SPRITE_PART",
    "SPRITE_PLACEHOLDER",
    "STYLESHEET_NAME",
    "STYLE_PARTS",
    "SURFACE_CLASSES",
    "SURFACE_HOOKS",
    "VENDORED",
    "AssetError",
    "class_for",
    "compose",
    "falls_back",
    "grammar_for",
    "header_of",
    "highlighted_languages",
    "is_vendored",
    "licence_for",
    "licence_names",
    "names",
    "script",
    "stylesheet",
    "text",
    "written_files",
]
