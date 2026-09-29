r"""The page's markup, as files — and the strict substitution that fills them.

**What it does.** Owns `render/templates/*.html`, reads one exactly, and fills
its placeholders, **refusing** any substitution that is not exact in either
direction.

**How you use it.**

    from studyforge.render import templates

    markup = templates.fill("image.html", src=…, alt=…, width=…, caption=…,
                            attributes=…)

`templates.template(name)` is the `string.Template` itself for a caller that
wants to inspect it; `templates.names()` is every template on disk, sorted.

**Depends on.** `pathlib` and `string`. ⛔ Nothing else in `studyforge`: markup
knows nothing about corpora, documents or addresses, and a loader that had to be
told which corpus it was for would be the source knowledge R1 forbids.

## ⛔ The loader lives in the directory it loads (R21, and it was unowned)

⚠️ **This function once had no owner**: the port of `pageassets` carried its
asset half and not its template half. Putting it in
`render/pageassets/templates.py` was considered; ⛔ **that would publish three
new names from a package the page renderer does not own**, which is the
escalation R21 asks for rather than a placement.

⭐ **So it is here, and here is better than beside it for three reasons that are
not about ownership:**

1. **It is a sibling of `page/`, not a child.** The root index needs
   templates and must not import the unit renderer to get them — which was the
   actual requirement, and this satisfies it.
2. **The directory is `Path(__file__).parent`.** There is no path arithmetic
   from another package to drift, so the loader and the files it loads cannot
   come apart.
3. **`pageassets`' own test asserts templates and asset parts are never
   confused.** Two packages is a stronger form of that than two functions.

## ⛔ A template is used exactly, minus one trailing newline

⚠️ **No reflow, no re-indent, no whitespace collapse**, because pages are
compared byte for byte (R10) and every one of those would change them. ⭐ That
forces the split the tests enforce: **markup emitted on one line is authored on
one line, however long**, and a template whose newlines are real output — the
page skeleton, the section wrapper, the player — reads as a page.

⚠️ **Read as bytes and decoded, never `read_text`.** Universal-newline
translation would rewrite a `\r\n` on the way in and silently change what the
page ships.

## ⛔ Substitution fails in **both** directions

⭐ The rendering design asks that *an unfilled placeholder must fail, never reach the page as a
literal*, and `Template.substitute` already does that half. ⚠️ **The other half
is silent and is the one that outlives an edit:** a value passed for a
placeholder the template no longer has is ignored, so a slot deleted from the
markup takes its content off the page and nothing anywhere raises. ⛔ So `fill`
compares the two sets and refuses either mismatch by name.
"""

from __future__ import annotations

from pathlib import Path
from string import Template

#: Where the markup lives: this package's own directory. ⛔ Never composed from
#: another package's `__file__` — see the module docstring.
TEMPLATE_DIR = Path(__file__).resolve().parent

#: What counts as a template. ⚠️ The loader's own `__init__.py` sits in this
#: directory and is not one; the suffix is what separates them, so a `.py` file
#: can never be read as markup and a template can never be imported.
TEMPLATE_SUFFIX = ".html"

__all__ = [
    "TEMPLATE_DIR",
    "TEMPLATE_SUFFIX",
    "TemplateError",
    "fill",
    "names",
    "placeholders",
    "template",
]


class TemplateError(Exception):
    """A page template this build cannot read or cannot fill exactly.

    ⚠️ `Exception`, not `ValueError`, following `AssetError`'s split rather than
    inventing a second one: every failure here is *"this markup is not something
    I can use"*, not *"you handed me an unacceptable value"*.

    ⛔ The message names the template and the placeholders, and **never formats
    an exception object into itself**: an `OSError` renders with the absolute
    path it was given, which is a home directory in a build log (R7).
    """


def template(name: str) -> Template:
    """Return the named template, exactly as it is on disk minus one trailing newline.

    `string.Template` rather than `str.format` or an f-string: markup is full of
    braces the moment anything is styled inline, and `${name}` needs none of them
    escaped. ⚠️ A literal dollar in a template is written `$$`.
    """
    text = _read(name)
    return Template(text[:-1] if text.endswith("\n") else text)


def fill(name: str, /, **values: str) -> str:
    """Return the named template with every placeholder filled, or raise saying which.

    ⛔ **Exact in both directions.** A placeholder with no value and a value with
    no placeholder are both refused, by name — see the module docstring for why
    the second one is the dangerous half.
    """
    filled = template(name)
    wanted = frozenset(filled.get_identifiers())
    given = frozenset(values)
    _refuse_mismatch(name, "has no value", wanted - given)
    _refuse_mismatch(name, "has no placeholder", given - wanted)
    return filled.substitute(values)


def placeholders(name: str) -> frozenset[str]:
    """Every placeholder the named template carries.

    ⭐ Published so a test can compare a renderer's call against the markup it
    fills without parsing either — which is how a slot that quietly lost its
    content is caught by something other than a reader.
    """
    return frozenset(template(name).get_identifiers())


def names() -> tuple[str, ...]:
    """Every template on disk, sorted.

    ⛔ Sorted rather than in directory order: two machines enumerating a
    directory can disagree, and anything derived from that order would make a
    page differ between them (R10).
    """
    return tuple(
        sorted(
            path.name
            for path in TEMPLATE_DIR.iterdir()
            if path.is_file() and path.suffix == TEMPLATE_SUFFIX
        )
    )


def _read(name: str) -> str:
    """Return one template's exact text, or refuse naming it and not its path."""
    if not isinstance(name, str) or not name.endswith(TEMPLATE_SUFFIX) or "/" in name:
        raise TemplateError(
            f"a template is named by its filename, which ends in {TEMPLATE_SUFFIX!r} "
            f"and holds no directory separator"
        )
    try:
        return (TEMPLATE_DIR / name).read_bytes().decode("utf-8")
    except OSError:
        # ⛔ The path is never in the message (R7): it is absolute, and this
        # refusal is the one most likely to reach a build log.
        raise TemplateError(
            f"there is no page template called {name!r}; this build ships {list(names())}"
        ) from None


def _refuse_mismatch(name: str, fault: str, offending: frozenset[str]) -> None:
    """Refuse a substitution that is not exact, naming the placeholders."""
    if offending:
        raise TemplateError(
            f"{name}: {', '.join(sorted(offending))} {fault}. A template is filled "
            f"exactly — a placeholder left unfilled would reach the page as a literal, "
            f"and a value with nowhere to go is a slot whose content silently vanished"
        )
