r"""Where this container page sits, and how it reaches the shared assets.

**What it does.** Holds the placement decision one container page is rendered
against, and turns it into the two hrefs the page writes for itself — its
stylesheet and its script.

**How you use it.**

    from studyforge.corpus import placement
    from studyforge.render.container import Placement

    profile = placement.profile_for(manifest.placement)
    where = Placement(
        corpus=manifest.source,
        container=profile.container(address, titles, origin=origin),
        shared=profile.corpus(),
    )
    where.stylesheet()      # '../../.studyforge/assets/page.css'

**Depends on.** `corpus.placement` for what a location is and for the one
relative-href computation, and `render.pageassets` for the two shared
filenames. ⛔ No filesystem: whether anything is there is discovery's question.

## ⛔ Three fields and no profile, exactly as the unit page holds it

⭐ The profile has already answered by the time a page is rendered, so the
renderer holds **locations** rather than the engine that computed them — which
is what keeps this package free of any branch on a profile name (R1). A third
profile is registered and nothing here changes.

## ⛔ Every href is relative to the page, and none of them is ever absolute

⚠️ **The floor is a page opened by double-clicking it** (R8). A rooted href
works under a server and breaks the moment the file is opened from disk, and an
absolute one carries a home directory (R7). ⭐ Both are unrepresentable here:
`relative_href` refuses an absolute path on either side, and this module never
joins one.

## ⭐ The container page's asset href is NOT the unit page's

⚠️ Under `sibling` a container page sits one directory above its units, and
under `tree` it sits two. ⛔ **So the number of `../` steps differs**, and a
container page that reused a unit page's stylesheet href would load nothing,
render every word, and be unstyled with no error anywhere — which is exactly
the failure `render.page`'s own contract says the class-name seam exists to
prevent, arriving instead through a path.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.corpus.placement import (
    ContainerLocations,
    CorpusLocations,
    relative_href,
)
from studyforge.render.pageassets import SCRIPT_NAME, STYLESHEET_NAME


@dataclass(frozen=True, slots=True)
class Placement:
    """Where one container page sits, and where the files it links sit."""

    corpus: str
    container: ContainerLocations
    shared: CorpusLocations

    def stylesheet(self) -> str:
        """How this page addresses the shared stylesheet."""
        return self._shared_asset(STYLESHEET_NAME)

    def script(self) -> str:
        """How this page addresses the shared script."""
        return self._shared_asset(SCRIPT_NAME)

    def _shared_asset(self, name: str) -> str:
        """One file from the corpus's shared asset directory, relative to this page."""
        return relative_href(self.container.page, self.shared.assets / name)
