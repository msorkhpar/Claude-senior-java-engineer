"""One corpus root in, one `Plan` out — from the declarations and nothing else.

**What it does.** Reads `corpus.json`, every container map beneath the
archive and the narration record when there is one, places each declared
container and unit under the corpus's own profile, and collects the result.

**How you use it.** `plan_for(root)`, or `plan_for(root, bytes_per_unit=N)` to
project the media footprint at a rate as well as measure it.

**Depends on.** `corpus.manifest`, `corpus.container`, `corpus.placement` (which
also says where the archive is: the plan reads maps from the root it prints),
`corpus.media` for the reading of the disk, `cli.plan.report`,
`cli.plan.recorded` for the narration record, and `narrate.speakable` for a
unit's token. ⛔ It opens three kinds of file and no others, and no unit document.

## ⛔ The narration record is a plan input

⭐ `cli.plan.recorded` reads it and says which clips the record locates in each
declared unit's audio directory; this module names one copy line per clip, at
that directory — asked of placement — and names each superseded clip beside
the paths, never among them.

## ⛔ The footprint is MEASURED, never guessed

⭐ For a policy that weighs its media, the declared units' media directories and
every clip the narration record locates are weighed by `corpus.media.measure`,
the one measurement a commit decision rests on, which stats files and opens
none. ⛔ A reading that is refused is said, with its reason, rather than printed
as zero. ⛔ **A limit the reading crosses is a refusal** (§5 *stops and
says so*), asked of `corpus.media.verdict_for` through `MediaProjection`.

## ⛔ A path on disk is never a `create`

⭐ Each named path is asked whether it is already at the corpus root, by
`os.path.lexists` and never by opening it, so the plan still writes and reads
nothing more (R3). Who writes a path the build does not is carried as data:
the archive's adapter, `studyforge serve` for the discovery cache, and
`studyforge narrate` for a clip beside the material.

## ⛔ Nothing raises

Everything that goes wrong becomes a `Refusal`. A plan that stopped at the
first defect would make an integrator fix one problem per run against 166
units, which is the same argument `validate.Report.of` makes about draining
every check.

## ⛔ Two artifacts claiming one path are a REFUSAL

⚠️ Printed as two `create` lines beside `0 refusal(s)`, the pair would let a
build replace one page with the other in the same run, because a plan that
exits `0` is the go signal for a build. ⭐ So each
path claimed twice is a refusal naming both claimants, and it is asked of
`validate`'s own `duplicate-path` enumeration, never of a second copy.
"""

from __future__ import annotations

import os
from dataclasses import replace
from pathlib import Path

from studyforge.cli.plan.media import MediaProjection
from studyforge.cli.plan.recorded import Recorded, read_record
from studyforge.cli.plan.report import Creation, Plan, Refusal
from studyforge.corpus.container import CONTAINER_FILENAME, Container
from studyforge.corpus.container import RAISES as CONTAINER_RAISES
from studyforge.corpus.container import parse as parse_container
from studyforge.corpus.manifest import MANIFEST_FILENAME, Manifest
from studyforge.corpus.manifest import RAISES as MANIFEST_RAISES
from studyforge.corpus.manifest import parse as parse_manifest
from studyforge.corpus.media import MediaError, MediaFootprint, measure
from studyforge.corpus.placement import (
    AUDIO_DIRNAME,
    UNIT_MEDIA_DIRNAMES,
    IgnoreFile,
    PlacementError,
    Profile,
    UnitLocations,
    profile_for,
)
from studyforge.narrate.speakable import SpeakableError
from studyforge.narrate.speakable.naming import unit_token
from studyforge.validate.corpus import Held, Walk
from studyforge.validate.paths import RULE_DUPLICATE_PATH, check_placement
from studyforge.validate.report import Finding

#: What a clip's line says about when a build writes it.
CLIP_COPY = "a build into any other output copies it there"

#: ⭐ Who writes the paths a build into the corpus root does not.
ADAPTER = "an adapter"
SERVE = "`studyforge serve`"
NARRATE = "`studyforge narrate`"


def plan_for(root: Path | str, *, bytes_per_unit: int | None = None) -> Plan:
    """Build the plan for the corpus rooted at `root`.

    `bytes_per_unit` is the media projection's rate. ⭐ **A parameter rather
    than a constant**, so a person asking *"would 200 MB a unit still fit?"* can
    ask it. ⛔ Absent or present, the media on disk is measured.
    """
    root = Path(root)
    manifest, refusals = _manifest(root)
    if manifest is None:
        return _unplannable(refusals)
    profile = profile_for(manifest.placement)
    held, unreadable = _containers(root, manifest, profile)
    refusals += unreadable
    refusals += _claimed_twice(root, manifest, held)
    # ⭐ A corpus that is not voiced reads no record, exactly as a build
    # of it does (`generate.narration.narrated`), so the plan names no clip copy.
    record = read_record(root) if manifest.narration else Recorded()
    refusals += record.refusals
    creations = _corpus_creations(root, profile)
    placed: list[UnitLocations] = []
    for where, container in held:
        made, failed = _container_creations(container, profile, where, record, placed)
        creations += [_on(root, creation) for creation in made]
        refusals += failed
    measured, unmeasured = _measured(root, manifest, placed)
    units = sum(len(container.units) for _, container in held)
    media = MediaProjection(manifest.media, units, bytes_per_unit, measured, unmeasured)
    refusals += [Refusal(MANIFEST_FILENAME, said) for said in media.crossed()]
    named = {creation.path for creation in creations}
    ignore = _ignore_file(profile, media, refusals)
    return Plan(
        source=manifest.source,
        title=manifest.title,
        profile=profile.name,
        describes=profile.describes,
        read_files=(MANIFEST_FILENAME, *(where for where, _ in held), *record.read),
        creations=tuple(sorted(creations, key=lambda creation: creation.path)),
        edits=manifest.permitted_edits,
        ignore=() if ignore is None else ignore.lines,
        media=media,
        refusals=tuple(refusals),
        ignore_home=None if ignore is None else ignore.home.as_posix(),
        superseded=tuple(clip for clip in record.superseded if clip.path not in named),
    )


def _measured(
    root: Path, manifest: Manifest, placed: list[UnitLocations]
) -> tuple[MediaFootprint | None, str]:
    """Weigh the corpus's generated media on disk, or say why not. ⛔ Nothing raises.

    ⭐ Only for a policy that weighs its media: `always` and `never` are decisions
    already taken, and `corpus.media.verdict_for` walks no disk for them either.
    """
    if not manifest.media.has_limits:
        return None, ""
    try:
        return measure(root, placed), ""
    except MediaError as error:
        return None, str(error)


def _claimed_twice(
    root: Path, manifest: Manifest, held: list[tuple[str, Container]]
) -> list[Refusal]:
    """Return one refusal per path two artifacts claim, from `validate`'s one check."""
    walk = Walk(
        root=root,
        manifest=manifest,
        containers=[Held(where, (root / where).parent, container) for where, container in held],
    )
    return [
        Refusal(item.where, item.message, rule=item.rule)
        for item in check_placement(walk)
        if isinstance(item, Finding) and item.rule == RULE_DUPLICATE_PATH
    ]


def _ignore_file(
    profile: Profile, media: MediaProjection, refusals: list[Refusal]
) -> IgnoreFile | None:
    """Return the ignore file the media policy requires, or record why none may hold it.

    ⛔ **Asked of the profile, and its home is printed with every line**:
    a rule with no named file is a rule somebody pastes into the
    root ignore file, which R3 forbids however declared.
    """
    try:
        return profile.ignore_file(media=media.ignored)
    except PlacementError as error:
        refusals.append(Refusal(MANIFEST_FILENAME, str(error)))
        return None


def _unplannable(refusals: list[Refusal]) -> Plan:
    """Return the plan for a corpus whose manifest will not read: refusals, nothing else.

    ⛔ **Not an empty plan.** An empty `create` list means *"this build writes
    nothing into your repository"*, which is a claim, and it is one nothing
    here is entitled to make. The summary counts the refusal and the exit code
    is 1.
    """
    return Plan(
        source="?",
        title="?",
        profile="?",
        describes="?",
        read_files=(MANIFEST_FILENAME,),
        creations=(),
        edits=(),
        ignore=(),
        media=None,
        refusals=tuple(refusals),
    )


def _manifest(root: Path) -> tuple[Manifest | None, list[Refusal]]:
    """Read `corpus.json`, or say why not.

    ⛔ `exc.strerror`, never `exc`: an `OSError` formats itself with the
    filename it was given, and this one is absolute (R7).
    """
    try:
        text = (root / MANIFEST_FILENAME).read_text(encoding="utf-8")
    except OSError as exc:
        reason = exc.strerror or exc.__class__.__name__
        return None, [Refusal(MANIFEST_FILENAME, f"cannot be read: {reason}")]
    except UnicodeDecodeError:
        return None, [Refusal(MANIFEST_FILENAME, "is not UTF-8 text")]
    try:
        return parse_manifest(text, MANIFEST_FILENAME), []
    except MANIFEST_RAISES as error:
        # ⛔ **The reader's own tuple**, which was a retyped pair here.
        # It includes `PersonalDataLeak`, untranslated: its message
        # says it is a leak, so a plan does not report it as a parse error.
        return None, [Refusal(MANIFEST_FILENAME, str(error))]


def _containers(
    root: Path, manifest: Manifest, profile: Profile
) -> tuple[list[tuple[str, Container]], list[Refusal]]:
    """Every container map under the archive, in sorted path order (R10)."""
    held: list[tuple[str, Container]] = []
    refusals: list[Refusal] = []
    for path in sorted((root / profile.corpus().archive).rglob(CONTAINER_FILENAME)):
        where = path.relative_to(root).as_posix()
        try:
            text = path.read_text(encoding="utf-8")
        except OSError, UnicodeDecodeError:
            refusals.append(Refusal(where, "cannot be read as UTF-8 text"))
            continue
        try:
            held.append((where, parse_container(text, where, manifest)))
        except CONTAINER_RAISES as error:
            # ⛔ **The reader's own tuple, never a list retyped here**.
            # This site caught `ContainerError` and `PersonalDataLeak` and
            # missed `AddressError`, which `container`'s contract argues for in
            # the same paragraph — so a container map whose address was the
            # wrong depth CRASHED the one command whose whole contract is that
            # nothing raises. ⭐ A member added to the reader now arrives here.
            refusals.append(Refusal(where, str(error)))
    return held, refusals


def _token(container: Container, n: int) -> str | None:
    """Return the unit token a declared unit's speech ids begin with, or None if it has none."""
    try:
        return unit_token(container.address.unit_key(n))
    except SpeakableError:
        return None


def _on(root: Path, creation: Creation) -> Creation:
    """Return `creation` saying whether its path is at `root` now, asked without opening it."""
    return replace(creation, present=os.path.lexists(root / creation.path.rstrip("/")))


def _corpus_creations(root: Path, profile: Profile) -> list[Creation]:
    """Return the four paths every corpus gets, whatever its addresses are.

    ⚠️ **The archive is one of them and it is not this build's output.** It is
    listed because a plan answers *"what will be in my repository afterwards"*,
    and its line says who writes it.
    """
    where = profile.corpus()
    made = [
        Creation(where.root_index.as_posix(), "the root index"),
        Creation(f"{where.assets.as_posix()}/", "the shared stylesheets, scripts and player"),
        Creation(
            f"{where.archive.as_posix()}/",
            "the archive root, which every build reads",
            writer=ADAPTER,
        ),
        Creation(
            where.site_cache.as_posix(), "the discovery cache, never the authority", writer=SERVE
        ),
    ]
    return [_on(root, creation) for creation in made]


def _container_creations(
    container: Container,
    profile: Profile,
    where: str,
    record: Recorded,
    placed: list[UnitLocations],
) -> tuple[list[Creation], list[Refusal]]:
    """Every path one container and its declared units occupy; each unit placed is kept."""
    key = container.address.key
    made: list[Creation] = []
    failed: list[Refusal] = []
    try:
        page = profile.container(container.address, container.titles, origin=container.origin)
        made.append(Creation(page.page.as_posix(), f"the page for container {key}"))
    except PlacementError as error:
        failed.append(Refusal(where, str(error)))
    for unit in container.units:
        try:
            at = profile.unit(
                container.address, unit.n, unit.title, origin=unit.origin, label=unit.label
            )
        except PlacementError as error:
            failed.append(Refusal(where, f"unit {unit.n}: {error}"))
            continue
        placed.append(at)
        made.append(Creation(at.page.as_posix(), f"{key} unit {unit.n}'s page"))
        # ⛔ Asked by kind, never read back off the minted directory name.
        # `tree` calls it `audio` and `sibling` calls it `audio/<stem>`, so a
        # description taken from the name would say something different under
        # each profile — `UnitLocations.href`'s *ask, never compose* rule
        # arriving one layer up.
        made += [
            Creation(
                f"{at.media_dir(kind).as_posix()}/",
                f"{key} unit {unit.n}'s {kind}",
                narration=kind == AUDIO_DIRNAME,
                when_filled=True,
            )
            for kind in UNIT_MEDIA_DIRNAMES
        ]
        audio = at.media_dir(AUDIO_DIRNAME).as_posix()
        made += [
            Creation(
                f"{audio}/{name}",
                f"{key} unit {unit.n}'s clip, which {CLIP_COPY}",
                narration=True,
                writer=NARRATE,
            )
            for name in record.copies(_token(container, unit.n), audio)
        ]
    return made, failed
