The reporting service reads its exporters from a config file: `html=...HtmlExporter`,
`md=...MarkdownExporter`, one class name per format. Two incidents last quarter: a misspelt
class name went live on a Friday and was found by the Monday morning report, and an exporter
nobody had used for months failed its own template setup and took the whole service down at
startup.

`ExporterRegistry` already creates exporters from a working config. Finish the `TODO` in
`src/main/java/practice/ExporterRegistry.java`:

- `resolve(key, className)`: what the registry keeps for one config entry, worked out when the
  service starts.

When you're done, a config entry the registry can't use stops it from being built, with a
`ConfigException` that names the entry's key, and building the registry runs none of the
exporters' own setup: each exporter's class is set up when its first instance is created. About
seven lines in total.

The tests use exporter classes nested in the test class, and pass you the class loader to use.
