r"""The narration record, and where a recorded clip is: what a build and a server read.

**What it does.** Holds the two modules that answer *"what were these clips made
under"* (`record`) and *"where is a recorded clip, and where does a unit's
clips go"* (`location`), without the pass that makes clips.

**How you use it.**

    from studyforge.narrate.recorded import audio_dir, read_state, state_file

    state = read_state(state_file(root))
    directory = audio_dir(root, locations)

**Depends on.** `archive.scrub`, `describe`, `version`, `narrate.answers`,
`narrate.speakable.naming` and `corpus.placement`. ⛔ Never `narrate.synth`,
`narrate.client` or `narrate.wire`: synthesis imports this package, one way.

## ⛔ Why the record is not inside `narrate.synth`

⭐ **Reading the record is not synthesising.** A build asks it which clips a
page may play, `plan` asks it which clips a build copies, and the study server
asks it through both. ⛔ Kept inside `synth`, every one of those imported the
synthesis pass with it, and a learner's copy of a course, which carries the
server and never synthesises (`studyforge.release`), would have carried the
pass too. ⭐ `narrate.synth` re-exports every name here unchanged, so a caller
that synthesises still imports one package.
"""

from studyforge.narrate.recorded.location import (
    Superseded,
    audio_dir,
    checked_where,
    located,
    order,
    root_of,
    where_of,
)
from studyforge.narrate.recorded.record import (
    CLIP_KEYS,
    KNOWN_NARRATION_API,
    NARRATION_API,
    NARRATION_STATE_FILENAME,
    STATE_KEYS,
    SUPERSEDED_KEY,
    WRITING_SUFFIX,
    Clip,
    Conditions,
    State,
    StateError,
    forget,
    forget_superseded,
    read_state,
    render_state,
    state_file,
    the_one_file,
    write_state,
)

#: ⛔ The package's whole public surface.
__all__ = [
    "CLIP_KEYS",
    "KNOWN_NARRATION_API",
    "NARRATION_API",
    "NARRATION_STATE_FILENAME",
    "STATE_KEYS",
    "SUPERSEDED_KEY",
    "WRITING_SUFFIX",
    "Clip",
    "Conditions",
    "State",
    "StateError",
    "Superseded",
    "audio_dir",
    "checked_where",
    "forget",
    "forget_superseded",
    "located",
    "order",
    "read_state",
    "render_state",
    "root_of",
    "state_file",
    "the_one_file",
    "where_of",
    "write_state",
]
