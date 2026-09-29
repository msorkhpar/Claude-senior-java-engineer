"""`studyforge` — turns any body of teaching material into a local, offline study site.

**What it does.** Reads an *archive* written by an adapter, and produces reading
pages, narration, a table of contents, navigation, a reader's own progress
record, and — where the material supports it — graded practices. The site works
over `file://` with no network and no server (R8); a served origin adds the API,
progress and Run/Submit but is never a prerequisite for reading.

**How you use it.** Through the command line, not through imports:

    studyforge validate <archive>   # the adapter's definition of done (R2)
    studyforge plan <corpus>        # where output would land, before it lands
    studyforge build <corpus>
    studyforge serve <corpus>

An adapter's entire obligation is to write a valid archive on disk. It gets no
callbacks and no framework API (R2), which is what lets adapters be written in
any language and tested in isolation. A consumer never imports these internals.

**Depends on.** The standard library, and nothing else (see `pyproject.toml`).
⛔ And on no source, ever: the framework must not import from, name, or branch
on any adapter, and every source-specific fact arrives as data through the
manifest or the archive (R1).

**Layout.** One package per responsibility, named by spec §3.2; each states its
own contract in its own docstring (R17).
"""
