"""Narration: what a page says out loud, and the synthesis that produces it.

**What it does.** Derives a speakable script from a unit document — the
contract for what is spoken, what is skipped, and how a spoken segment maps
back to the section it belongs to — and drives synthesis to produce the clips a
page plays.

**How you use it.** Ask for a unit's speakable segments; hand them to
synthesis. The player and its highlight sync live in `render/assets/`, because
they are page behaviour rather than pipeline behaviour.

**Depends on.** `unit`, `address`, `archive`, and `render.markup` for the one
inline-marker parser — `speakable/voice.py`'s contract is why that one is taken
deliberately rather than by habit. ⛔ Not on `serve`, and not on the page renderer.

⚠️ **Synthesis is the one place R10 bends.** Audio is content-addressed and
cached: the same text with the same voice parameters is not re-synthesised, and
a clip filename carries a digest of what was said. That digest is deterministic
and therefore reproducible by construction — it is not the churn R10 bans,
because the clip is linked by one page regenerated in the same run.

⭐ **Regenerable is not the same as available.** Generated media is committed by
default: a clone that carries its own audio speaks with no synthesis service,
no GPU and no network, which is what R8 is for. The default has a ceiling, and
crossing it is a corpus's declared decision rather than an accident.

`speakable/` is a **package**: the
extraction source's one module is 626 lines, R11's ceiling is 400, and a
port pays that during extraction rather than
after. Synthesis is `synth/`'s; the service it talks to is narrate-service (§8.2).
"""

from studyforge.narrate.enabled import narration_on

#: ⭐ The one "narration is on" predicate, exported so no stage reaches past it.
__all__ = ["narration_on"]
