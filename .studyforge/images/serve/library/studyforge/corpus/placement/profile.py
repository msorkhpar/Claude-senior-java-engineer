"""The profile: one address in, one location set out — and the registry of them.

**What it does.** Defines what a placement profile must answer, and holds the
registry a manifest's `placement` name is looked up in.

**How you use it.** `profile_for("sibling").unit(address, 7, "java", origin=…)`.

**Depends on.** `names`, `locations`, `errors`.

## A third profile is added without changing any consumer

⭐ That is placement's acceptance, and it is what the registry is for. A profile is
a `Profile` subclass with a `name`, registered with `register()`; every
consumer asks `profile_for(manifest.placement)` and none of them names a
profile. ⛔ There is no `if placement == "tree"` anywhere in this package or
downstream of it, and `test_profile` asserts that of the whole of `src/`.

## The seam with `corpus.manifest`, drawn deliberately

⚠️ `corpus.manifest.PLACEMENT_PROFILES` lists **the names a manifest may
declare**; this registry holds **what each one does**. They are different
questions, and a manifest must be able
to refuse an unknown name without importing a placement engine. ⛔ But they
must not drift, so `test_profile` asserts the two sets are equal and fails a
profile registered here that a manifest may not name.

## The generated root

⚠️ Both profiles put the *shared* artifacts in one place — assets, archive and
the discovery cache — and differ only in where **pages** land. That is the
honest difference: `sibling` exists so the reader's own directories gain a page
beside the file they already know, not so a corpus's archive is scattered
through it.

⛔ **The archive is the one shared artifact NOT under the generated root**.
It is the adapter's output (R2), and `validate`, the adapter's
definition of done, reads it at `names.ARCHIVE_DIRNAME` beside `corpus.json`.
Composing `.studyforge/archive` from that name would make `plan` print a root
nothing reads, where a malformed map validates clean.

## Ignore lines are a profile's answer, not a caller's guess

⛔ **`studyforge plan` must print the ignore lines its
profile requires**, and R1 forbids the caller reaching that by asking which
profile it has. So the profile answers with `ignore_file`, and everything else it
places (`unit`, `container`, `corpus`) is committed. `cli.plan` and the onboarding
skill are the callers.

⛔ **A build's output is committed, and the only generated ignore rules about
the CORPUS are the media policy's**. §5's
*regenerable is not the same as available* holds for the page that plays a clip
as much as for the clip: a clone that ignores its pages has no reading floor,
and one that ignores only the bundle has pages that render unstyled with no
error. ⚠️ Under `sibling`, rules ignoring the pages and the root index would
have no home R3 allows and would match `*.html` and `*.json`.

## ⛔ The discovery cache is the one build output that IS ignored

⚠️ **`site.json` is not among the committed output, and the reason is structural
rather than a preference.** Every other artifact in that list is read by
somebody: a reader opens the pages and the index, and a page loads the bundle. ⛔
**Nothing reads the cache.** `corpus.discovery.assemble` scans on every call and
returns the scan — there is no branch on which a cached `Site` is handed back —
so a clone that carries the cache gains exactly nothing, while every reader who
serves the corpus gets a modified file for doing the one thing the tool is for.
⭐ **A workspace pin check refuses the workspace within a minute of a serve**
if the cache is tracked, on `.studyforge/site.json` alone.

⛔ **The home is a `.gitignore` inside the generated directory the rules are
about, never the repository root** (R3). A profile whose media **no one**
generated directory encloses has no home — whether nothing encloses it or one
directory per source directory does — and `ignore_file` raises rather
than hand a caller rules nothing may hold.

⭐ **What is committed is recognised instead, by `validate` asking the plan**
for this corpus's own placed paths — exact paths from its declarations, never a
glob a manifest would have to carry.
"""

from __future__ import annotations

from pathlib import PurePosixPath

from studyforge.address import Address
from studyforge.corpus.placement.errors import PlacementError
from studyforge.corpus.placement.locations import (
    ContainerLocations,
    CorpusLocations,
    IgnoreFile,
    UnitLocations,
)
from studyforge.corpus.placement.names import (
    ARCHIVE_DIRNAME,
    ASSETS_DIRNAME,
    IGNORE_FILENAME,
    ROOT_INDEX_FILENAME,
    SITE_CACHE_FILENAME,
    STAGING_SUFFIX,
)
from studyforge.describe import describe
from studyforge.sourcepath import SOURCE_PATH_DESCRIBED, source_path_fault

#: Where everything a reader does not browse lives. ⚠️ Dot-prefixed so it sorts
#: out of the way in a repository whose directories are the material — which is
#: the whole point of `sibling`.
GENERATED_ROOT = ".studyforge"

#: The ignore file inside the generated root, for a profile whose media lives
#: below it. ⛔ Inside the directory the rules are about and never the
#: repository root: that is the one mechanism R3 leaves.
GENERATED_IGNORE_HOME = PurePosixPath(GENERATED_ROOT, IGNORE_FILENAME)

#: ⛔ **The rules covering what this framework writes into the generated root
#: for ONE MACHINE and never for a commit** — the discovery cache, and the name
#: it is staged under while it is written. ⭐ Minted here because two
#: writers put them in one file: a corpus's generated ignore file, below, and
#: `corpus.discovery.cache`, which ensures them the moment it writes the cache
#: into a corpus onboarded before they existed.
CACHE_IGNORE_LINES = (SITE_CACHE_FILENAME, SITE_CACHE_FILENAME + STAGING_SUFFIX)

#: ⛔ **The line by which an ignore file hides ITSELF, and it is written into
#: exactly one shape of file**: one carrying nothing but
#: `CACHE_IGNORE_LINES`. ⭐ Such a file is this machine's own — the same
#: judgement `progress.store` makes about the directory it records into — so it
#: never enters a commit, and a corpus that has no such file yet goes clean the
#: first time it is served rather than trading one untracked file for another.
#: ⚠️ **A file that also carries the corpus's media policy is the opposite and
#: must NOT hide itself**: a clone has to read those rules or the
#: media it regenerates reads as dirt, so that file is committed.
SELF_IGNORE_LINE = IGNORE_FILENAME


class Profile:
    """The map from a logical address to physical locations.

    ⛔ Subclasses answer `unit` and `container`; everything else is shared,
    because a profile that could move the archive would make two corpora on one
    disk unfindable by one scan.
    """

    #: The name a manifest declares. Set by every subclass.
    name = ""

    #: One line, for `studyforge plan` and for a person choosing between them.
    describes = ""

    def unit(
        self,
        address: Address,
        ordinal: int,
        title: str,
        *,
        origin: str | None = None,
        label: str | None = None,
    ) -> UnitLocations:
        """Where one unit's artifacts go."""
        raise NotImplementedError

    def container(
        self,
        address: Address,
        titles: tuple[str, ...],
        *,
        origin: str | None = None,
    ) -> ContainerLocations:
        """Where one container's own page goes."""
        raise NotImplementedError

    def media_ignore_lines(self) -> tuple[str, ...]:
        """Return the lines covering the per-unit clip directories this profile mints.

        ⛔ Only `names.UNCOMMITTED_DIRNAMES`: a policy that does not commit media
        keeps its narration clips out of git, and every other kind stays committed.

        ⛔ Answered by every subclass, like `unit` and `container`, and for the
        same reason: a profile that puts media somewhere new and inherited a
        stale glob would report ignore rules that ignore nothing, which is the
        one failure mode a dry-run exists to prevent. Written relative to
        `ignore_home`'s directory, which is how git reads a nested ignore file.
        """
        raise NotImplementedError

    def ignore_home(self) -> PurePosixPath | None:
        """Return the ignore file this profile's media rules live in, or None.

        ⛔ Answered by every subclass. A home is inside a directory this
        framework generates, and it is ONE file — so a profile whose media is
        enclosed by one generated directory per source directory has none,
        as much as one whose media is enclosed by nothing at all.
        """
        raise NotImplementedError

    def media_ignore_files(self, audio_directories) -> tuple[IgnoreFile, ...]:
        """Return the ignore files a profile with NO single home writes: none, by default.

        ⭐ A profile whose media sits under one generated directory answers with
        `ignore_file` and needs nothing here. ⛔ A profile whose media is enclosed
        by one generated directory per source directory (`sibling`) answers
        with one file inside each, from the audio directories a corpus holds
        (a unit's `audio`, or where a narration record put a clip). Never the
        repository's root ignore file (R3).
        """
        del audio_directories
        return ()

    def ignore_lines(self, *, media: bool) -> tuple[str, ...]:
        """Return the ignore lines a build under this profile requires.

        `media` says whether the generated media is to be ignored — it is the
        corpus's `media` policy inverted, and the caller passes it rather than
        this method reading a manifest.

        ⛔ **Only the corpus's OWN clips are ever ignored here, and they are
        committed by default**, so the default answer is empty. Pages, the root
        index and the bundle are what a clone reads (§5). ⚠️ **The
        discovery cache is no longer among them** — see `ignore_file`.
        """
        return self.media_ignore_lines() if media else ()

    def ignore_file(self, *, media: bool) -> IgnoreFile:
        """Return the ignore file a corpus requires inside the generated root.

        ⛔ **There is always one**, and that is the change: this
        framework writes its own discovery cache into every corpus it serves,
        so every corpus needs the rule covering it, and a rule a corpus would
        have to add by hand is a hole in the skills (R19).

        ⭐ **Two rule sets, one file, because one directory has one ignore
        file.** The machine-local rules are always there; the media policy's
        are there when the policy does not commit media, and only then is the
        file a *committed* artifact — which is why `SELF_IGNORE_LINE` goes in
        only when it is absent.

        ⛔ **Raises `PlacementError` when the media policy's rules are required
        and no file may hold them**, and refuses a home at the repository root
        (R3). A caller handed rules with nowhere to put them pastes them into
        the root file.
        """
        lines = self.ignore_lines(media=media)
        if not lines:
            return IgnoreFile(home=GENERATED_IGNORE_HOME, lines=cache_ignore_lines())
        home = self.ignore_home()
        if home is None or len(home.parts) < 2:
            raise PlacementError(
                f"the corpus's media policy does not commit generated media, and placement "
                f"{self.name!r} has no ignore file that may hold the rules: no single "
                f"directory this framework generates encloses its media, and the repository's "
                f"root ignore file is never edited. Commit the media, or choose a "
                f"placement whose media lives under {GENERATED_ROOT}/"
            )
        return IgnoreFile(home=home, lines=(*CACHE_IGNORE_LINES, *lines))

    def corpus(self) -> CorpusLocations:
        """Return the paths that exist once per corpus. ⛔ The same under every profile."""
        generated = PurePosixPath(GENERATED_ROOT)
        return CorpusLocations(
            root_index=PurePosixPath(ROOT_INDEX_FILENAME),
            assets=generated / ASSETS_DIRNAME,
            archive=PurePosixPath(ARCHIVE_DIRNAME),
            site_cache=generated / SITE_CACHE_FILENAME,
        )

    def __repr__(self) -> str:
        """Return the profile's declared name, which is what a plan prints."""
        return f"<placement {self.name!r}>"


#: The registry. ⛔ Mutated only through `register`, and read only through
#: `profile_for`, so adding a profile is one call and touches no consumer.
_PROFILES: dict[str, Profile] = {}


def register(profile: Profile) -> Profile:
    """Add a profile to the registry, refusing a name already taken."""
    if not profile.name:
        raise PlacementError(f"{type(profile).__name__} declares no name")
    if profile.name in _PROFILES:
        raise PlacementError(f"a placement profile named {profile.name!r} is already registered")
    _PROFILES[profile.name] = profile
    return profile


def profile_for(name: object) -> Profile:
    """Return the registered profile `name`, or raise listing the ones there are."""
    if name in _PROFILES:
        return _PROFILES[str(name)]
    raise PlacementError(
        f"no placement profile by that name; this build registers {registered()}, "
        f"and was given {describe(name)}"
    )


def registered() -> tuple[str, ...]:
    """Every registered profile name, sorted.

    ⛔ Sorted, not insertion-ordered: `studyforge plan` prints this and R10
    forbids an output that depends on import order.
    """
    return tuple(sorted(_PROFILES))


def origin_directory(origin: object, address: Address, what: str = "artifact") -> PurePosixPath:
    """Return the source directory an artifact is placed beside.

    `what` names the thing being placed, so a container's refusal does not
    call it a unit — the two records that carry an `origin` are different
    records, and an integrator sent to the wrong one looks in the wrong place.

    ⛔ Refuses an origin that escapes the source root — a generated artifact
    written outside the repository is R3's prohibition reached by accident.
    """
    if not isinstance(origin, str) or not origin.strip():
        raise PlacementError(
            f"the 'sibling' profile places an artifact beside its source file, and the "
            f"{what} at {address.key!r} records no usable 'origin' — the container map "
            f"must carry one for every {what} placed under this profile"
        )
    # ⛔ The offending origin is DESCRIBED, never echoed (R7). It is corpus
    # data, and the shapes being refused here are exactly the shapes that
    # carry a home directory — so a refusal that quoted it would copy personal
    # data into a build log, from the check that exists to catch it. Name the
    # fault and the record, and let the integrator look at the one named.
    #
    # ⚠️ The predicate is `sourcepath`'s and not this module's: a local
    # `is_absolute() or ".." in parts` and a different forbidden list elsewhere
    # leave a gap, such as `C:/Users/<name>/x`, that neither refuses.
    fault = source_path_fault(origin)
    if fault is not None:
        raise PlacementError(
            f"the 'origin' recorded for the {what} at {address.key!r} is {fault}; an "
            f"origin is {SOURCE_PATH_DESCRIBED} and stays inside it"
        )
    return PurePosixPath(origin).parent


def cache_ignore_lines() -> tuple[str, ...]:
    """Return the rules for a file that holds nothing but this framework's own caches.

    ⛔ **The self-ignore line is part of the answer and not an afterthought.**
    A file carrying only machine-local rules is itself machine-local: written
    without it, the first serve of an already-onboarded corpus would trade one
    untracked file for another and a workspace pin check would still refuse
    the workspace.
    """
    return (*CACHE_IGNORE_LINES, SELF_IGNORE_LINE)
