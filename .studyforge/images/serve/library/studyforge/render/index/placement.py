r"""Where the root index sits, and how it reaches everything it links.

**What it does.** Holds the placement decision the one index page is rendered
against, and turns it into the hrefs the page writes for itself — its
stylesheet, its script, and one per unit page it lists.

**How you use it.**

    from studyforge.corpus import placement
    from studyforge.render.index import Placement

    where = Placement(shared=placement.profile_for(manifest.placement).corpus())
    where.stylesheet()          # '.studyforge/assets/page.css'
    where.unit(entry.page)      # 'basics/01-intro/one.unit.html'

**Depends on.** `corpus.placement` for what a location is and for the one
relative-href computation, and `render.pageassets` for the two shared
filenames. ⛔ No filesystem: whether anything is there is discovery's question.

## ⭐ One field, because the root index is the page that needs no address

⚠️ The unit page and the container page each hold their **own** location as
well as the corpus's, because there are many of them and each sits somewhere
different. ⛔ **There is exactly one root index and `Profile.corpus()` puts it at
the top under every registered profile**, so its location is already in
`CorpusLocations` and a second field would be a second copy of it.

⭐ **The consequence, and it is worth stating:** the index needs no profile at
all. `corpus()` answers identically for `tree` and for `sibling`, so a corpus
that changed profile would render the *same* index chrome — and different unit
hrefs, because those come out of the contents document, which is where the
profile's answer was already recorded.

## ⛔ Every href is relative to the page, and none of them is ever absolute

⚠️ **The floor is a page opened by double-clicking it** (R8). A rooted href works
under a server and breaks the moment the file is opened from disk, and an
absolute one carries a home directory (R7). ⭐ Both are unrepresentable here:
`relative_href` refuses an absolute path on either side, and this module never
joins one.

⚠️ **The index sits at the root, so almost every href it writes has no `../` in
it at all** — which makes it the one page where an off-by-one in the arithmetic
would look most like a plain filename. ⛔ That is why the tests resolve every
href against the page's own directory rather than spelling the answer.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import PurePosixPath

from studyforge.corpus.placement import CorpusLocations, relative_href
from studyforge.render.pageassets import SCRIPT_NAME, STYLESHEET_NAME


@dataclass(frozen=True, slots=True)
class Placement:
    """Where the one root index sits, and where the files it links sit."""

    shared: CorpusLocations

    def stylesheet(self) -> str:
        """How this page addresses the shared stylesheet."""
        return self._shared_asset(STYLESHEET_NAME)

    def script(self) -> str:
        """How this page addresses the shared script."""
        return self._shared_asset(SCRIPT_NAME)

    def unit(self, page: PurePosixPath) -> str:
        """How this page addresses one unit's page, given where the contents put it.

        ⛔ **Ask, never compose.** `Entry.page` is recorded by the corpus's own
        profile, so this is the one arithmetic that turns it into a link and a
        caller that spelled the answer would be right under one profile.
        """
        return relative_href(self.shared.root_index, page)

    def _shared_asset(self, name: str) -> str:
        """One file from the corpus's shared asset directory, relative to this page."""
        return relative_href(self.shared.root_index, self.shared.assets / name)
