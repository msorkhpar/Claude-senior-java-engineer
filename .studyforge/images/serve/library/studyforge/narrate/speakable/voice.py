r"""How one run of prose becomes one string a speech engine can read.

**What it does.** Turns displayed text into spoken text: inline markers resolve to
their content, identifier-shaped tokens are pronounced as words, a literal URL
becomes a fixed phrase, emoji are dropped and whitespace is collapsed.

**How you use it.** `spoken_text(value)` is the whole transform.
`looks_like_code`, `split_identifier` and `elide_urls` are published because each
is separately testable and each encodes a measured decision.

**Depends on.** `re` and `studyforge.render.markup` for the inline split.
⛔ Nothing else — no filesystem, no archive, no corpus.

## ⛔ Display and speech legitimately differ, and that is the design

⭐ A URL is dropped, an identifier is respaced, a fenced method is not spoken
at all. Because alignment is at **speech-unit granularity** rather than word
level, that difference never has to be reconciled word by word — which is the
decision that makes narration tractable at all, and the reason word-level
highlighting is out of scope rather than merely deferred.

## ⛔ The inline split is IMPORTED, never written a second time

⚠️ **`studyforge.render.markup.segments` is the one interpreter of inline markers**,
and this module consumes it. ⭐ Display and speech must never disagree about where
a marker begins: joining the segment **bodies** is what keeps the marker characters
out of the audio, so `**Kotlin**` speaks as "Kotlin" with no separate stripping
pass that somebody could forget to run, and no clip can read an asterisk aloud.

⚠️ **This is a dependency on `render`, and `narrate`'s own contract said it would
have none.** ⛔ It is taken deliberately and the alternative is worse: a second
parser. ⭐ It is also a **leaf** — `studyforge.render.__init__` is a docstring and
nothing else, and `render.markup.text` imports only `re`, so nothing of the page
renderer is reached. ⚠️ That the parser's home is under `render/` at all is
a known inversion, and moving it to a neutral home is a separate change
rather than an edit made from inside this module.

## ⛔ Order is load-bearing

⭐ Markers resolve **first**, so a link's `href` is gone before `elide_urls` could
turn it into a phrase — the listener wants the link's *words*, not an announcement
that a link was there. What `elide_urls` then catches is a URL that appeared as
visible prose.

⚠️ **`URL_PHRASE` is a listening device and never a personal-data control.** Its
pattern is not the gate's pattern and the two disagree at the edges; `assert_clean`
is the gate, and it runs on the finished string in `script.py` (R7).
"""

from __future__ import annotations

import re

from studyforge.render.markup import segments

#: What a literal URL in visible prose is spoken as. ⛔ Never spelled out: a
#: spelled URL is the longest and least useful thing a clip can say.
URL_PHRASE = "this link"

#: Split points inside one identifier, applied in this order.
_CAMEL_LOWER_UPPER = re.compile(r"(?<=[a-z0-9])(?=[A-Z])")  # fooBar     -> foo|Bar
_CAMEL_ACRONYM = re.compile(r"(?<=[A-Z])(?=[A-Z][a-z])")  # HTTPServer -> HTTP|Server
_LETTER_DIGIT = re.compile(r"(?<=[A-Za-z])(?=[0-9])")  # item2      -> item|2
_DIGIT_LETTER = re.compile(r"(?<=[0-9])(?=[A-Za-z])")  # 2Count     -> 2|Count

#: The four shapes that say "identifier, not English word". ⭐ Shape is the signal,
#: not markup: `getAllUsers` reads the same whether or not the page wrapped it in
#: `<code>`, and the gate is narrow so an ordinary capitalised word is never mangled.
_HAS_CAMEL = re.compile(r"[a-z][A-Z]")
_HAS_SNAKE = re.compile(r"[A-Za-z0-9]_[A-Za-z0-9]")
_SCREAMING = re.compile(r"^[A-Z0-9]+(_[A-Z0-9]+)+$")
_DOTTED = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)+(\(\))?$")

#: Punctuation peeled off a token before its shape is read, and re-attached after.
#: ⚠️ A trailing `.` is deliberately **absent** from the trailing set: stripping it
#: would make `_DOTTED` fire on "e.g." and speak it as "e g". It is removed
#: separately, and only when some other code signal is already present.
_LEAD_PUNCT = "([{\"'“‘"
_TRAIL_PUNCT = ",;:!?)]}\"'”’"

_URL = re.compile(r"\b(?:[a-z][a-z0-9+.\-]*://\S+|www\.\S+\.[a-z]{2,}\S*)", re.IGNORECASE)

#: Emoji are dropped from speech and left untouched in display. ⛔ Explicit ranges
#: rather than a Unicode category: category `So` also holds the degree sign and the
#: arrows, which carry meaning in teaching prose and must survive.
_EMOJI_RANGES = (
    (0x2600, 0x27BF),  # misc symbols and dingbats
    (0x2B00, 0x2BFF),  # stars, misc symbols and arrows
    (0xFE00, 0xFE0F),  # variation selectors
    (0x1F000, 0x1FAFF),  # pictographs, emoticons, transport, supplemental
)
_ZERO_WIDTH_JOINER = 0x200D

_WHITESPACE = re.compile(r"\s+")


def looks_like_code(token: str) -> bool:
    """Return whether a token's shape says identifier rather than English word."""
    return bool(
        _HAS_CAMEL.search(token)
        or _HAS_SNAKE.search(token)
        or _SCREAMING.match(token)
        or _DOTTED.match(token)
    )


def split_identifier(token: str) -> str:
    """Return `token` respaced for pronunciation: `getAllUsers` -> "get all users".

    ⚠️ An all-uppercase run longer than one character is left alone — that is the
    acronym signal (API, HTTP, SQL, JSON, URL), and lower-casing it destroys the
    only evidence an engine has. Trailing empty parens are dropped silently;
    "parentheses" is never announced.
    """
    words: list[str] = []
    for part in re.split(r"[_.]", token.rstrip("()")):
        if not part:
            continue
        spaced = _DIGIT_LETTER.sub(
            " ",
            _LETTER_DIGIT.sub(" ", _CAMEL_ACRONYM.sub(" ", _CAMEL_LOWER_UPPER.sub(" ", part))),
        )
        words += [
            word if word.isupper() and len(word) > 1 else word.lower() for word in spaced.split()
        ]
    return " ".join(words)


def elide_urls(text: str) -> str:
    """Return `text` with every literal URL replaced by `URL_PHRASE`."""
    return _URL.sub(URL_PHRASE, text or "")


def strip_emoji(text: str) -> str:
    """Return `text` without decorative pictographs; display keeps them."""
    kept = [
        character
        for character in text or ""
        if ord(character) != _ZERO_WIDTH_JOINER
        and not any(low <= ord(character) <= high for low, high in _EMOJI_RANGES)
    ]
    return "".join(kept)


def spoken_identifiers(text: str) -> str:
    """Return `text` with every identifier-shaped token respaced for pronunciation.

    ⚠️ Whitespace is normalised to single spaces on the way through, which is
    harmless: speech has no layout and the caller collapses it anyway.
    """
    return " ".join(_spoken_token(token) for token in (text or "").split())


def spoken_text(value: object) -> str:
    """Return the whole inline transform of one run of prose, ready to be gated.

    ⛔ Not gated here. The string this returns is derived text and re-enters the
    personal-data gate in `script.py`, which is the module that knows the id the
    refusal would have to name (R7).
    """
    joined = "".join(body for _kind, body, _href in segments(value))
    return _WHITESPACE.sub(" ", strip_emoji(elide_urls(spoken_identifiers(joined)))).strip()


def _spoken_token(token: str) -> str:
    """Return one whitespace-delimited token respaced, its punctuation intact."""
    lead = ""
    trail = ""
    while token and token[0] in _LEAD_PUNCT:
        lead, token = lead + token[0], token[1:]
    while token and token[-1] in _TRAIL_PUNCT:
        trail, token = token[-1] + trail, token[:-1]
    token, dot = _strip_sentence_dot(token)
    trail = dot + trail
    if not token or not looks_like_code(token):
        return lead + token + trail
    return lead + split_identifier(token) + trail


def _strip_sentence_dot(token: str) -> tuple[str, str]:
    """Return `(token, trailing dots)`, peeling a full stop only off clearly code-shaped text.

    ⚠️ `e.g.` and `3.14.` keep their dot deliberately: without it the dotted-path
    detector fires and speaks "e g". A dotted path is only recognised past a full
    stop when **two or more** dots remain, which no ordinary abbreviation has. The
    accepted cost is that a two-segment path ending a sentence stays verbatim —
    mildly clumsy aloud, and far cheaper than mangling every "e.g." in the material.
    """
    trail = ""
    while token.endswith("."):
        core = token[:-1]
        code_shaped = (
            _HAS_CAMEL.search(core)
            or _HAS_SNAKE.search(core)
            or _SCREAMING.match(core)
            or (core.count(".") >= 2 and _DOTTED.match(core))
        )
        if not code_shaped:
            break
        trail = "." + trail
        token = core
    return token, trail
