// Verify the installed extensions against the pins, inside the image, at build time.
//
// Usage (the Dockerfile only; run by the node code-server bundles, so it needs
// no runtime to be declared):
//   code-server --list-extensions --show-versions \
//     | node verify_extensions.js <extensions dir> <id@version ...>
//
// It fails, naming each one, when:
//   * a pinned extension is not installed, or an installed one is not pinned —
//     the set is compared EXACTLY, so a marketplace that stopped serving an id,
//     or a dependency that arrived some other way, both stop the build;
//   * an installed extension's `extensionDependencies` names one that is not
//     installed. ⛔ Measured on code-server 4.137.0: offline, an extension whose
//     dependency is missing installs with exit 0 and no warning, so without this
//     the image would ship an extension that cannot activate.
"use strict";
const fs = require("fs");
const path = require("path");

const [dir, ...expected] = process.argv.slice(2);
const listed = fs.readFileSync(0, "utf8").split("\n").map((l) => l.trim().toLowerCase()).filter(Boolean);
const want = new Set(expected.map((e) => e.toLowerCase()));
const have = new Set(listed);
const problems = [];
for (const e of want) if (!have.has(e)) problems.push(`${e} is pinned but not installed`);
for (const e of have) if (!want.has(e)) problems.push(`${e} is installed but not pinned`);

const ids = new Set(listed.map((e) => e.split("@")[0]));
for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
  const manifest = path.join(dir, entry.name, "package.json");
  if (!entry.isDirectory() || !fs.existsSync(manifest)) continue;
  const pkg = JSON.parse(fs.readFileSync(manifest, "utf8"));
  const id = `${pkg.publisher}.${pkg.name}`.toLowerCase();
  for (const need of pkg.extensionDependencies || []) {
    if (!ids.has(need.toLowerCase())) problems.push(`${id} depends on ${need}, which is not installed`);
  }
}
for (const p of problems) console.error(`extension check: ${p}`);
if (problems.length) process.exit(1);
console.log(`extensions: ${[...want].sort().join(" ")}`);
