// Take the workbench's own AI out of the product, inside the image, at build time.
//
// Usage (the Dockerfile only; run by the node code-server bundles):
//   node no_ai.js <workbench bundle> ...
//
// ⚠️ WHY THIS EXISTS AFTER THE CHAT EXTENSION WAS DELETED. Code 1.137's workbench
// carries a chat of its OWN: the "Chat" view container ("Build with Agent", a
// chat input, "Generate Agent Instructions"), a title-bar chat button, a
// Copilot status item, agent sessions, MCP, voice -- about 470 registered
// commands. None of it is an extension, so no `--list-extensions` names it.
// Measured on code-server 4.137.0 with the extension and the Copilot modules
// gone: the title bar still offered "Toggle Chat" and the chat view opened.
//
// ⛔ A USER SETTING IS NOT THE INSTRUMENT. The reader's settings.json lives on a
// volume that outlives the image, and the entrypoint never overwrites it, so a
// seeded setting would miss every existing volume. So the PRODUCT changes, in
// every bundle given, with four edits:
//  1. `chat.disableAIFeatures` DEFAULTS to true. That is the workbench's own
//     switch for every AI surface (title bar, status bar, layout, welcome),
//     and a default is the product's, so it holds on a fresh volume and on an
//     existing one that never named the key.
//  2. The chat entitlement is always HIDDEN, whatever the setting says. The
//     chat view, its setup and its welcome are gated on that, so a
//     settings.json that sets the switch back to false still shows no chat.
//  3. The command registry REFUSES every AI command id (`AI` below): it is
//     never registered, so nothing -- a menu, a keybinding, an extension's
//     `executeCommand` -- can run it. It answers "command not found".
//  4. The command palette's list refuses the same ids.
//
// ⛔ Each edit must find EXACTLY ONE anchor per bundle and leave exactly one
// marker, or this exits 1 naming the bundle and the edit: a code-server that
// renamed a symbol must not ship a workbench whose chat silently came back.
// `tests/test_editor_no_ai.py` reads the EFFECT in a real browser, on a fresh
// and on an existing user-data volume, and plants the edits back out.
"use strict";
const fs = require("fs");

// The AI command ids, by the words the workbench spells them with. ⭐ Matched
// case-insensitively anywhere in the id. `KEPT` is the one id that matches a
// word and is not AI: the server's own "remote agent" log.
const AI = /chat|copilot|codex|agent|mcp|languagemodel|^lm\.|aiedits|aicustomization|aisearch|withai|voice|prompt|skill|instructions|toolset/i;
const KEPT = /remoteagent/i;

const refuse = (id) => `if(${AI}.test(${id}.id)&&!${KEPT}.test(${id}.id))return{dispose(){}};`;
const escape = (text) => text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

function edits(text) {
  const keys = [...text.matchAll(/([\w$]+)="chat\.disableAIFeatures"/g)].map((m) => m[1]);
  if (keys.length !== 1) return { error: `names the AI switch ${keys.length} times; this patches exactly one` };
  const key = escape(keys[0]);
  return {
    list: [
      ["the AI switch default",
        new RegExp(`\\[${key}\\]:\\{type:"boolean",description:([\\w$]+\\(\\d+,null\\)),default:!1,`, "g"),
        (_, description) => `[${keys[0]}]:{type:"boolean",description:${description},default:!0,`],
      ["the chat entitlement gate",
        /withConfiguration\(([\w$]+)\)\{return this\._forceHidden\|\|/g,
        (_, e) => `withConfiguration(${e}){return!0||this._forceHidden||`],
      ["the command registry",
        /if\(typeof ([\w$]+)=="string"\)\{if\(![\w$]+\)throw new Error\("invalid command"\);return this\.registerCommand\(\{id:[\w$]+,handler:[\w$]+\}\)\}/g,
        (whole, command) => whole + refuse(command)],
      ["the command palette",
        /addCommand\(([\w$]+)\)\{return this\._commands\.set\(/g,
        (_, command) => `addCommand(${command}){${refuse(command)}return this._commands.set(`],
    ],
  };
}

function main(bundles) {
  const problems = [];
  for (const bundle of bundles) {
    let text = fs.readFileSync(bundle, "utf8");
    const planned = edits(text);
    if (planned.error) {
      problems.push(`${bundle} ${planned.error}`);
      continue;
    }
    const before = problems.length;
    if (text.includes(`${AI}.test(`)) problems.push(`${bundle} already refuses the AI commands; it is patched once`);
    for (const [name, anchor, replace] of planned.list) {
      const found = (text.match(anchor) || []).length;
      if (found !== 1) {
        problems.push(`${bundle} holds ${found} of ${name}; this patches exactly one`);
        continue;
      }
      text = text.replace(anchor, replace);
    }
    const refusals = text.split(`${AI}.test(`).length - 1;
    if (refusals !== 2) problems.push(`${bundle}: ${refusals} AI command refusals after the edits, not 2`);
    if (problems.length > before) continue;
    fs.writeFileSync(bundle, text);
    console.log(`no-ai: ${bundle}: the switch on, the chat hidden, the AI commands refused`);
  }
  for (const p of problems) console.error(`no-ai: ${p}`);
  return problems.length || !bundles.length ? 1 : 0;
}

// ⭐ Run by the Dockerfile; required by the tests, which read `AI` and `KEPT`.
module.exports = { AI, KEPT, refuse, edits };
if (require.main === module) process.exitCode = main(process.argv.slice(2));
