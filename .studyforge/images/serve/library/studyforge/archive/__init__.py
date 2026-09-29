"""The adapter seam: the archive document, the block vocabulary, and the personal-data gate.

**What it does.** Defines what an adapter writes and what the framework will
read — a versioned document per unit, whose body is a sequence of typed blocks
— plus the Markdown reader that produces those blocks and the gate every string
passes on the way in.

**How you use it.** An adapter writes archive documents to disk and runs
`studyforge validate`; that is its entire obligation and its definition of done
(R2). ⛔ The seam is **on disk, not in Python**: there are no callbacks, no
plugin registry and no framework API for an adapter to call, which is what lets
adapters be written in any language and assigned to different agents in
parallel.

**Depends on.** `address`. ⛔ Not on `render` or `serve`: the archive is the
input to a page, and a vocabulary that knows how it will be displayed has
already stopped being a vocabulary.

⛔ **The gate refuses rather than rewrites** (R7). No absolute home path,
account id, name or email reaches the archive. A scrubber that silently
rewrote would leave nobody knowing personal data had been there.

⚠️ **The vocabulary must cover what real material contains**, not what a clean
corpus contains: raw HTML appears in 6 of 19 files of one surveyed tutorial, and a
parser that raises on anything it does not recognise stops an ingest dead (C3).
Attachments — a dataset a lesson loads, a notebook — are a class of their own,
neither block nor rendered media (C4).

**The modules.** `blocks` is the vocabulary — what a block may be, once.
`document` is the format: `build` assembles and gates, `render` writes the
exact bytes, `parse` and `load` read one back. `markdown` is the strict reader
(§6), `scrub` is the gate (R7), `samples` names the sample data the gate
admits, and `errors` holds the one exception
both halves of the format raise.

⭐ **`blocks` is the single block-type list, and it is a contract rather than
a convenience.** The reader, the counter and the fixture checker all import
it; adding a type is a `raw_api` change; and
`tests/studyforge/archive/test_blocks.py` fails if a second list appears
anywhere in the tree, because a vocabulary that disagrees with its own checker
means the gate and the parser have different ideas of what a document may
contain.
"""
