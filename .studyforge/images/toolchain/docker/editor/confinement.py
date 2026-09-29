"""Prove the workbench ALLOWS nothing outside the practice, before the image is tagged.

**What it does.** `prove(image)` starts a container from `image` with the
image's own command line, opens a workbench in a headless browser at the URL a
study page builds, and then **is the reader**: it presses `Ctrl+Shift+P`,
`Ctrl+P`, `Ctrl+,`, `F5` and the rest at a real session and looks at the page
for what appeared. It then presses the keys a practice NEEDS -- find, type,
save -- and checks those still work. `Confinement.ok` is true only when every
confined chord opened nothing AND the practice still edits and saves.

**Why it is keys and pixels rather than a file.** The lockdown confines by
keybinding; the keybindings live in a file; and a check that reads that file
cannot see what the workbench ALLOWS. ⛔ That is the exact shape of the Restricted
Mode defect, where a check that read INSTALLATION could not see
ACTIVATION and passed for five rounds against a broken product. ⚠️ It would
have been especially wrong here: measured, the workbench reads the user
keybindings file when a session STARTS and ignores a write made while one is
open, so a correct file on disk and a confined session are different facts.

**The other half.** This one is BEHAVIOURAL and samples -- the chords
`CONFINED` names, not all four hundred. The EXHAUSTIVE half is the extension's
own report, which derives the removals the allow-list implies from the
workbench's own keybinding document and says how many the session did not
load; this refuses on `missing=`. ⭐ Together: *does the seed still cover this
workbench*, and *does this workbench actually refuse*.

**How you use it.** `build.py` runs it after `activation.py`, tagging only if both pass; standalone:

    python3 docker/editor/confinement.py [--write] <image reference>...

⭐ `--write` is the GENERATOR: it opens the same session in EACH image named and
writes the union of what the extension derived into the seed. ⛔ **Name one
image per set in `SEED_SETS`**: one seed serves every set, each set's
extensions bind keys of their own, and a seed derived from one set's workbench
leaves another's unremoved, which this gate refuses. ⛔ The seed is never
hand-edited -- `lockdown/allowed.js` is the allow-list; the rest is computed. ⚠️ It UNIONS rather than replaces, for the measurement
`lockdown/keybindings.js` carries: the workbench's default keybinding document
is not the same in two sessions of one image, so a regeneration that replaced
would oscillate and drop what another session found.

**Depends on.** The standard library, `activation.py` (container, browser),
`probe.py` (the session both halves open), `cdp.py`, a Docker CLI and a
Chromium-family browser. ⛔ A missing browser REFUSES rather than skips; no
Docker socket is mounted: it runs `docker` from outside.
"""

from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
import time
from dataclasses import dataclass, field
from pathlib import Path

HERE = Path(__file__).resolve().parent
COMPONENT = HERE.parents[1]
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(COMPONENT / "lockdown"))
import activation  # noqa: E402
import cdp  # noqa: E402
import lockdown as lockdown_extension  # noqa: E402
import probe  # noqa: E402

# ⭐ The session this gate and its generator open lives in `probe.py`; these
# names are re-exported so every reader of `confinement.<name>` keeps working.
from probe import MAIN, MAIN_TEXT, OTHER, OTHER_TEXT, free_port, session  # noqa: E402, F401

Refused = activation.Refused

#: The generated seed the image bakes in, relative to the component root.
SEED = Path("docker/editor/seed/keybindings.json")
#: The runtime sets the seed is derived from, one image each (`--write`).
SEED_SETS = (("java", "maven"), ("python",), ("gradle", "java", "kotlin", "node", "python"))

#: The chords this presses, and what each of them is. ⛔ The first of them are
#: the ones the reader's own screenshot named; the rest are the surfaces around
#: the editor, which the Restricted Mode defect could only close after the fact. ⚠️ `Run Task` has
#: NO default keybinding at all -- it is reachable only THROUGH the palette, so
#: closing the palette closes it and there is no chord to press for it.
CONFINED = (
    ("ctrl+p", "Go to File"),
    ("ctrl+e", "Go to File, the other chord"),
    ("ctrl+shift+p", "Show and Run Commands"),
    ("f1", "Show and Run Commands, the other chord"),
    ("ctrl+shift+f", "Search for Text"),
    ("ctrl+shift+alt+l", "Open Quick Chat"),
    ("ctrl+shift+o", "Go to Symbol in Editor"),
    ("f5", "Start Debugging"),
    ("ctrl+,", "Preferences: Open Settings"),
    ("ctrl+g", "Go to Line"),
    ("ctrl+b", "the primary side bar"),
    ("ctrl+shift+e", "the Explorer"),
    ("ctrl+shift+d", "Run and Debug"),
    ("ctrl+shift+x", "Extensions"),
    ("ctrl+`", "the integrated terminal"),
    ("ctrl+j", "the panel"),
    ("ctrl+shift+u", "the Output panel"),
)

#: What a surface looks like in the page, and what to call it in a refusal. ⭐
#: Read by COMPUTED STYLE and size, never by presence: the workbench keeps the
#: quick input's element in the document and hides it, so `querySelector` alone
#: would report the palette open in every session.
SURFACES = {
    ".quick-input-widget": "the command palette / quick open",
    ".settings-editor": "the settings editor",
    ".part.sidebar": "the primary side bar",
    ".part.panel": "the panel",
    ".part.auxiliarybar": "the secondary side bar",
}

#: The one surface a practice DOES need, and the chord that opens it. ⛔ Not a
#: nicety: a check that only ever sees nothing cannot tell a confined
#: workbench from a browser that never delivered a keystroke.
ALLOWED_CHORD = ("ctrl+f", ".editor-widget.find-widget", "the editor's own find widget")

#: What is typed into the file the URL opens, which must reach the disk.
TYPED = "probe-edit"

SETTLE = 1.4


@dataclass(frozen=True)
class Confinement:
    """What a real session did when the keys were pressed. ⭐ `ok` is the conjunction."""

    image: str
    #: chord -> the surfaces that appeared, empty when the chord did nothing.
    opened: dict = field(default_factory=dict)
    #: Whether the practice's own find widget still opens.
    find: bool = False
    #: Whether typing reached the file ON DISK through the editor's save.
    edited: bool = False
    #: The extension's own line, which carries `missing=` and `extra=`.
    report: str = ""

    def _counted(self, name: str) -> int:
        found = re.search(rf"{name}=(\d+)", self.report)
        return int(found.group(1)) if found else -1

    @property
    def missing(self) -> int:
        """Removals THIS session's workbench needs that it did not load. ⛔ The refusal."""
        return self._counted("missing")

    @property
    def extra(self) -> int:
        """Removals it loaded whose command the allow-list now permits -- the practice, over-confined."""
        return self._counted("extra")

    @property
    def reached(self) -> list:
        return sorted(chord for chord, surfaces in self.opened.items() if surfaces)

    @property
    def ok(self) -> bool:
        return (not self.reached and self.find and self.edited
                and self.missing == 0 and self.extra == 0)

    def complaint(self) -> str:
        wrong = []
        for chord in self.reached:
            wrong.append(f"{chord} opened {', '.join(self.opened[chord])}")
        if not self.find:
            wrong.append(f"{ALLOWED_CHORD[0]} did not open {ALLOWED_CHORD[2]}, so the practice is broken "
                         "and this check cannot tell a confined session from a dead one")
        if not self.edited:
            wrong.append("what the probe typed never reached the file on disk, so the practice cannot be done")
        if self.missing != 0:
            wrong.append(f"the extension reports {self.missing} keybinding removals the session did not load "
                         f"-- the seed no longer covers this workbench ({self.report or 'no report at all'})")
        elif self.extra != 0:
            wrong.append(f"the extension reports {self.extra} loaded removals whose command the allow-list "
                         f"now permits -- the seed takes something away from the practice ({self.report})")
        return f"{self.image}: the practice frame is not confined -- {'; '.join(wrong)}"


def prove(image: str, lock=None) -> Confinement:
    """Open a real session in `image` and press the keys a reader would."""
    lock = lock or lockdown_extension.identity(COMPONENT / "lockdown")
    with session(image, "confinement") as (name, port, debug):
        return _drive(image, name, port, lock, debug)


def derived(image: str, lock=None) -> list:
    """What the extension derives from THIS image's workbench, for `--write` to save.

    ⭐ The derivation is the extension's, never re-implemented here: the
    allow-list is JavaScript, it runs inside the workbench that owns the
    default keybindings, and this only carries the answer out. ⚠️ What it
    carries is already the UNION of this session's derivation and the seed the
    image booted with, so writing it over the seed can only add.
    """
    lock = lock or lockdown_extension.identity(COMPONENT / "lockdown")
    with session(image, "keybindings") as (name, _, _debug):
        return _wait_for_derived(name, lock)


#: The page probe: which surfaces are visibly open right now. ⚠️ `width > 2`
#: rather than `> 0`, because a closed part keeps a one-pixel sash.
_VISIBLE = """(() => {
  const open = [];
  for (const [selector, name] of %s) {
    for (const element of document.querySelectorAll(selector)) {
      const style = getComputedStyle(element), box = element.getBoundingClientRect();
      if (style.display !== 'none' && style.visibility !== 'hidden' && box.width > 2 && box.height > 2) {
        open.push(name); break;
      }
    }
  }
  return open;
})()"""


def _surfaces(session, pairs=None) -> list:
    return cdp.evaluate(session, _VISIBLE % json.dumps(list((pairs or SURFACES).items())))


def _drive(image: str, name: str, port: int, lock, debug: int) -> Confinement:
    """Press every confined chord, then the practice's own, then type and save."""
    page = cdp.wait_for_target(debug, f":{port}/")
    with cdp.Session(page["webSocketDebuggerUrl"]) as session:
        session.call("Runtime.enable")
        box = probe.wait_for_editor(session)
        opened = {}
        for chord, _ in CONFINED:
            cdp.click(session, box["x"], box["y"])
            cdp.press(session, chord)
            time.sleep(SETTLE)
            opened[chord] = _surfaces(session)
            cdp.press(session, "escape")
            time.sleep(0.4)
        chord, selector, label = ALLOWED_CHORD
        cdp.click(session, box["x"], box["y"])
        cdp.press(session, chord)
        time.sleep(SETTLE)
        find = bool(_surfaces(session, {selector: label}))
        cdp.press(session, "escape")
        time.sleep(0.4)
        edited = _edit_and_save(session, name, box)
    return Confinement(image=image, opened=opened, find=find, edited=edited,
                       report=_report(name, lock))


def _edit_and_save(session, name: str, box: dict) -> bool:
    """Type into the one file and save it, and answer whether the disk changed.

    ⛔ Read back through `docker exec`, from the container's own copy of the
    file: the point is that the reader's edit reaches the disk a Run and a
    Submit execute against, and only the container can say that.
    """
    cdp.click(session, box["x"], box["y"])
    cdp.press(session, "ctrl+end")
    time.sleep(0.4)
    cdp.type_text(session, f"\n{TYPED}\n")
    time.sleep(0.6)
    cdp.press(session, "ctrl+s")
    deadline = time.monotonic() + 30.0
    while time.monotonic() < deadline:
        read = subprocess.run(["docker", "exec", name, "cat", f"{activation.SOURCES}/{MAIN}"],
                              stdin=subprocess.DEVNULL, capture_output=True, text=True)
        if TYPED in read.stdout:
            return True
        time.sleep(1.5)
    return False


def _report(name: str, lock) -> str:
    """The extension's own keybinding line out of the session's log."""
    for line in activation.exthost_log(name, lock).splitlines():
        if line.startswith(f"{lock.banner} keybindings:"):
            return line.strip()
    return ""


def _wait_for_derived(name: str, lock, timeout: float = activation.ACTIVATION_TIMEOUT) -> list:
    """What the extension wrote beside its log, once the session has activated it."""
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        read = subprocess.run(
            ["docker", "exec", name, "sh", "-c",
             f"cat {activation.LOGS}/*/exthost*/{lock.id}/{lockdown_extension.DERIVED_KEYBINDINGS} 2>/dev/null"],
            stdin=subprocess.DEVNULL, capture_output=True, text=True)
        if read.stdout.strip():
            entries = json.loads(read.stdout)
            if entries:
                return entries
        time.sleep(activation.POLL)
    raise Refused(f"the lockdown derived no keybinding removals in {timeout:.0f}s; "
                  "either it did not activate, or the workbench's default list could not be read")


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("images", nargs="+", metavar="image", help="editor images or ids, one per runtime set")
    parser.add_argument("--write", action="store_true", help=f"regenerate {SEED} from them instead of checking")
    parser.add_argument("--root", default=str(COMPONENT))
    args = parser.parse_args(argv)
    try:
        if args.write:
            target = Path(args.root) / SEED
            seen = {(entry["key"], entry["command"]) for image in args.images for entry in derived(image)}
            entries = [{"key": key, "command": command} for key, command in sorted(seen)]
            target.write_text(json.dumps(entries, indent=1) + "\n", encoding="utf-8")
            print(f"{target}: {len(entries)} removals, derived from {' '.join(args.images)}")
            return 0
        proofs = [prove(image) for image in args.images]
    except (Refused, cdp.CdpError) as refusal:
        print(f"refused: {refusal}", file=sys.stderr)
        return 2
    for proof in proofs:
        print(f"{proof.image}: {len(CONFINED)} confined chords opened nothing; {ALLOWED_CHORD[2]} still "
              f"opens and the file still saves\n{proof.report}" if proof.ok else f"refused: {proof.complaint()}",
              file=sys.stdout if proof.ok else sys.stderr)
    return 0 if all(proof.ok for proof in proofs) else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
