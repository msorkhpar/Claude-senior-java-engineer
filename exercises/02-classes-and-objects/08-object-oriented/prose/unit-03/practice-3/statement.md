An abstract class can provide a **common base implementation** that related
classes complete. In the *template method* pattern, the abstract class owns
the algorithm in one `final` method and leaves its **steps** abstract, so each
subclass supplies only what differs. A *hook* is a step with a default that a
subclass may override.

`Exporters.Exporter` is abstract. Write its final method

```
public final String export(List<String> items)
```

which builds, each line ending in `"\n"`: the `header()` line (only when the
hook `includeHeader()` answers `true`, its default), then one line per item,
`formatItem(position, item)` with the 0-based position, then the `footer()`
line. Then write the three exporters:

| exporter | header | item line | footer | header shown |
|---|---|---|---|---|
| `Markdown` | `# Items` | `<position+1>. <item>` | `(<n> items)` | yes |
| `Csv` | `index,item` | `<position>,<item>` | `end` | yes |
| `Plain` | `ITEMS` | `<item>` | `--` | no |

`<n>` is the number of items exported: `footer` is given it.

## Examples

```
new Markdown().export(["apple", "pear"]) -> "# Items\n1. apple\n2. pear\n(2 items)\n"
new Csv().export(["apple"])              -> "index,item\n0,apple\nend\n"
new Plain().export(["apple"])            -> "apple\n--\n"
new Markdown().export([])                -> "# Items\n(0 items)\n"
```
