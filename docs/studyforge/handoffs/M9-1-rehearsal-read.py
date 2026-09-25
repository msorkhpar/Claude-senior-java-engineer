"""Read this source. THE ONE MODULE IN THIS PACKAGE WRITTEN BY HAND.

**What it does.** Files the course from its curriculum record (`README.md`)
into sections and modules, and reads each lesson file's Markdown into the
archive's block vocabulary.

**How the record is read.** A section is a bare numbered line (`1. Java
Fundamentals`) inside the `## Curriculum` region. A module is a top-level list
entry linking `<module-dir>/README.md`; its address is the section's recorded
address and the module directory's own name. Every linked `README_*.md`
nested under a module, at any depth, is one unit, numbered in the record's
order. The module `README.md` is a contents page and is the container's origin.

**Depends on.** `studyforge.corpus.container`, `studyforge.address`.
"""

from __future__ import annotations

import re
from pathlib import Path

from studyforge.address import Address
from studyforge.corpus.container import Container, Unit

RECORD = "README.md"
VARIANT = "prose"

#: The section addresses. A recommendation pending the author: the record
#: writes no address for a section, and an address is recorded, never derived.
SECTION_ADDRESS = {
    1: "01-java-fundamentals",
    2: "02-classes-and-objects",
    3: "03-exception-handling",
    4: "04-functional-programming",
    5: "05-java-memory-model",
    6: "06-multithreading-and-concurrency",
    7: "07-modern-java-features",
    8: "08-principles",
    9: "09-design-patterns",
    10: "10-advanced-java-topics",
}

_SECTION = re.compile(r"^(\d+)\. (.+?)\s*$")
_MODULE = re.compile(
    r"^- (?:\[)?(\d+\.\d+)\.\s*\[?([^\]]+?)\]?\((\d\d-[a-z0-9-]+)/README\.md\)\s*$"
)
_UNIT = re.compile(r"^( +)- \[([\d.]+?)\.? (.+)\]\(([^)]+)\)\s*$")


def _record_lines(root: Path) -> list[str]:
    lines = (Path(root) / RECORD).read_text("utf-8").splitlines()
    start = next(i for i, line in enumerate(lines) if line.strip() == "## Curriculum")
    end = next(
        (i for i in range(start + 1, len(lines)) if lines[i].startswith("## ")), len(lines)
    )
    return lines[start + 1 : end]


def _filed(root: Path) -> list[tuple[tuple[str, str], tuple[str, str], str, list[Unit]]]:
    filed: list = []
    section = None
    for line in _record_lines(root):
        if m := _SECTION.match(line):
            section = (int(m.group(1)), m.group(2))
            continue
        if m := _MODULE.match(line):
            if section is None:
                raise ValueError(f"{RECORD}: module {m.group(3)} precedes any section")
            number, title, directory = m.groups()
            if not number.startswith(f"{section[0]}."):
                raise ValueError(f"{RECORD}: module {number} is filed under section {section[0]}")
            filed.append(
                (
                    (SECTION_ADDRESS[section[0]], directory),
                    (section[1], title.strip()),
                    f"{directory}/README.md",
                    [],
                )
            )
            continue
        if m := _UNIT.match(line):
            if not filed:
                raise ValueError(f"{RECORD}: a unit precedes any module: {line.strip()}")
            _, label, title, target = m.groups()
            module_dir = filed[-1][0][1]
            if not target.startswith(f"{module_dir}/"):
                raise ValueError(f"{RECORD}: {target} is listed under module {module_dir}")
            units = filed[-1][3]
            units.append(
                Unit(
                    n=len(units) + 1,
                    title=title.strip(),
                    practices=0,
                    origin=target,
                    label=label,
                )
            )
    return filed


def containers(root: Path) -> list[Container]:
    """One `Container` per module, filed under its section by the record."""
    return [
        Container(
            address=Address(address),
            titles=titles,
            variant=VARIANT,
            ingested="1970-01-01",
            units=tuple(units),
            origin=origin,
        )
        for address, titles, origin, units in _filed(Path(root))
    ]


def documents(root: Path, container: Container) -> list[dict]:
    """One lesson document per unit: the unit's file, read into blocks."""
    out = []
    for unit in container.units:
        text = (Path(root) / unit.origin).read_text("utf-8")
        out.append(
            {
                "address": container.address,
                "variant": VARIANT,
                "unit": unit.n,
                "kind": "lesson",
                "ordinal": 1,
                "title": unit.title,
                "blocks": markdown_blocks(text, unit.origin),
            }
        )
    return out


def expected_units(root: Path) -> dict[str, int] | None:
    """The second reading: lesson files on disk per module, without the record."""
    counts: dict[str, int] = {}
    for address, _titles, origin, _units in _filed(Path(root)):
        directory = Path(root) / Path(origin).parent
        counts[Address(address).key] = len(list(directory.glob("README_*.md")))
    return counts


# --- Markdown into the block vocabulary ------------------------------------

_FENCE = re.compile(r"^(\s*)(```+|~~~+)\s*([\w+-]*)\s*$")
_HEADING = re.compile(r"^(#{1,6})\s+(.*?)\s*#*\s*$")
_ITEM = re.compile(r"^(\s*)([-*+]|\d+[.)])\s+(.*)$")
_RULE = re.compile(r"^\s{0,3}([-*_])(\s*\1){2,}\s*$")
_TABLE_SEP = re.compile(r"^\s*\|?\s*:?-{2,}:?\s*(\|\s*:?-{2,}:?\s*)*\|?\s*$")


class Unreadable(ValueError):
    """A construct the block vocabulary cannot hold: reported, never dropped."""


def _cells(line: str) -> list[str]:
    body = line.strip()
    if body.startswith("|"):
        body = body[1:]
    if body.endswith("|") and not body.endswith("\\|"):
        body = body[:-1]
    cells, cur, i = [], "", 0
    while i < len(body):
        if body[i] == "\\" and i + 1 < len(body) and body[i + 1] == "|":
            cur += "|"
            i += 2
            continue
        if body[i] == "|":
            cells.append(cur.strip())
            cur = ""
        else:
            cur += body[i]
        i += 1
    cells.append(cur.strip())
    return cells


def markdown_blocks(text: str, where: str) -> list[dict]:
    lines = text.replace("\t", "    ").splitlines()
    return _blocks(lines, where)


def _blocks(lines: list[str], where: str) -> list[dict]:
    out: list[dict] = []
    i = 0
    n = len(lines)
    while i < n:
        line = lines[i]
        if not line.strip():
            i += 1
            continue
        if m := _FENCE.match(line):
            i, block = _fence(lines, i, m, where)
            out.append(block)
            continue
        if m := _HEADING.match(line):
            out.append({"type": "heading", "level": len(m.group(1)), "text": m.group(2)})
            i += 1
            continue
        if _RULE.match(line):
            out.append({"type": "rule"})
            i += 1
            continue
        if line.lstrip().startswith("|") and i + 1 < n and _TABLE_SEP.match(lines[i + 1]):
            headers = _cells(line)
            rows = []
            i += 2
            while i < n and lines[i].strip().startswith("|"):
                row = _cells(lines[i])
                row = (row + [""] * len(headers))[: len(headers)]
                rows.append(row)
                i += 1
            out.append({"type": "table", "headers": headers, "rows": rows})
            continue
        if line.lstrip().startswith(">"):
            inner = []
            while i < n and lines[i].lstrip().startswith(">"):
                inner.append(re.sub(r"^\s*>\s?", "", lines[i]))
                i += 1
            out.append({"type": "quote", "blocks": _blocks(inner, where)})
            continue
        if line.lstrip().startswith("<") and not line.lstrip().startswith("<http"):
            chunk = []
            while i < n and lines[i].strip():
                chunk.append(lines[i])
                i += 1
            out.append({"type": "html", "text": "\n".join(chunk)})
            continue
        if _ITEM.match(line):
            i = _list(lines, i, out, where)
            continue
        para = []
        while i < n and lines[i].strip() and not (
            _FENCE.match(lines[i])
            or _HEADING.match(lines[i])
            or _ITEM.match(lines[i])
            or lines[i].lstrip().startswith(">")
            or (lines[i].lstrip().startswith("|") and i + 1 < n and _TABLE_SEP.match(lines[i + 1]))
        ):
            para.append(lines[i].strip())
            i += 1
        out.append({"type": "para", "text": " ".join(para)})
    return out


def _fence(lines, i, m, where):
    indent = len(m.group(1))
    marker = m.group(2)
    lang = m.group(3) or "text"
    body = []
    i += 1
    while i < len(lines):
        c = _FENCE.match(lines[i])
        if c and c.group(2).startswith(marker[0] * 3) and not c.group(3):
            i += 1
            break
        body.append(lines[i][indent:] if lines[i][:indent].strip() == "" else lines[i].lstrip())
        i += 1
    else:
        raise Unreadable(f"{where}: an unclosed code fence")
    return i, {"type": "code", "lang": lang, "text": "\n".join(body) + "\n"}


def _list(lines, i, out, where):
    """Read one list, and everything indented under its items.

    A fenced code block inside an item cannot be held by a list item (items
    are text and nested lists only), so the list is closed, the code is
    emitted as its own block in reading order, and text after it becomes a
    paragraph; the next item continues the list with its own number.
    """
    m = _ITEM.match(lines[i])
    base = len(m.group(1))
    ordered = m.group(2)[0].isdigit()
    items: list = []

    def flush():
        if items:
            block = {"type": "list", "ordered": ordered, "items": list(items)}
            if ordered:
                first = first_number[0]
                if first != 1:
                    block["start"] = first
            out.append(block)
            items.clear()

    first_number = [None]
    n = len(lines)
    while i < n:
        line = lines[i]
        m = _ITEM.match(line)
        if m and len(m.group(1)) == base and (m.group(2)[0].isdigit()) == ordered:
            if not items and first_number[0] is None:
                first_number[0] = int(re.match(r"\d+", m.group(2)).group()) if ordered else None
            i += 1
            text = [m.group(3).strip()]
            body: list[str] = []
            while i < n:
                nxt = lines[i]
                fm = _FENCE.match(nxt)
                if fm and len(fm.group(1)) > base:
                    # a fence opened inside the item runs to its close,
                    # whatever the indentation of the lines inside it
                    body.append(nxt)
                    i += 1
                    while i < n:
                        body.append(lines[i])
                        closing = _FENCE.match(lines[i])
                        i += 1
                        if closing and not closing.group(3):
                            break
                    continue
                if not nxt.strip():
                    # a blank line ends the item unless indented content follows
                    j = i
                    while j < n and not lines[j].strip():
                        j += 1
                    if j < n and (len(lines[j]) - len(lines[j].lstrip())) > base and not (
                        _ITEM.match(lines[j]) and len(_ITEM.match(lines[j]).group(1)) <= base
                    ):
                        body.append("")
                        i = j
                        continue
                    break
                ind = len(nxt) - len(nxt.lstrip())
                if ind <= base:
                    break
                body.append(nxt)
                i += 1
            parts = _item_parts(text, body, where)
            # split around code blocks and other non-list blocks
            current: list = []
            detached = False
            for part in parts:
                if not detached and (isinstance(part, str) or part.get("type") == "list"):
                    current.append(part)
                    continue
                if not detached:
                    if current:
                        items.append(_as_item(current))
                        current = []
                    flush()
                    first_number[0] = None
                    detached = True
                out.append({"type": "para", "text": part} if isinstance(part, str) else part)
            if current:
                items.append(_as_item(current))
            continue
        if not line.strip():
            j = i
            while j < n and not lines[j].strip():
                j += 1
            m2 = _ITEM.match(lines[j]) if j < n else None
            if m2 and len(m2.group(1)) == base and (m2.group(2)[0].isdigit()) == ordered:
                i = j
                continue
        break
    flush()
    return i


def _as_item(parts):
    if len(parts) == 1 and isinstance(parts[0], str):
        return parts[0]
    return parts


def _item_parts(text, body, where):
    """An item's own text, then its indented body read as blocks."""
    if not body:
        return [" ".join(text)]
    indents = [len(b) - len(b.lstrip()) for b in body if b.strip()]
    cut = min(indents) if indents else 0
    dedented = [b[cut:] if b.strip() else "" for b in body]
    # continuation lines directly after the item text join its text
    k = 0
    while k < len(dedented) and dedented[k].strip() and not (
        _FENCE.match(dedented[k]) or _ITEM.match(dedented[k]) or _HEADING.match(dedented[k])
    ):
        text.append(dedented[k].strip())
        k += 1
    parts: list = [" ".join(text)]
    for block in _blocks(dedented[k:], where):
        if block["type"] == "para":
            parts.append(block["text"])
        else:
            parts.append(block)
    return parts
