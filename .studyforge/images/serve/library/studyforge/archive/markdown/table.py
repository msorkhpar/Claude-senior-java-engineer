"""The pipe-table reader.

**What it does.** Turns a GFM pipe table into a `table` block —
`{"headers": [...], "rows": [[...]]}`.

**How you use it.** `read_table(lines, start)` where `lines[start]` is the
header row and `lines[start + 1]` is already known to be its separator; the
dispatcher checks that, because a header row is only a header row if the line
under it is a rule.

**Depends on.** `scan` for cell splitting. Nothing else.

⚠️ **A borderless table is still a table.** GFM makes the leading and trailing
`|` optional and authors omit both, so a reader that required them turned
every comparison table into a paragraph — silently, because prose is the
catch-all and nothing downstream can tell a lost table from prose that never
was one.
"""

from __future__ import annotations

from studyforge.archive.markdown import patterns, scan


def read_table(lines: list[str], start: int):
    """Return `(block, next_index)` for the pipe table opening at `start`.

    Every following line that still carries an unescaped `|` is a data row —
    a row is recognised by the separator it splits on rather than by a leading
    character, so a line with no pipe at all ends the table, which is what the
    blank line after one does.

    ⭐ A header row whose cells are **all blank** is a table with no headers,
    and comes back as `headers: []`. GFM has no other way to write a headerless
    table, and keeping the blanks would render a row of empty header cells and
    make a narrator read every cell as "«nothing»: value".
    """
    headers = scan.cells(lines[start])
    if all(not header for header in headers):
        headers = []
    index = start + 2
    rows = []
    while index < len(lines) and patterns.UNESCAPED_PIPE.search(lines[index]):
        rows.append(scan.cells(lines[index]))
        index += 1
    return {"type": "table", "headers": headers, "rows": rows}, index
