/*
 * What a practice is ALLOWED to do, and everything else derived from it.
 *
 * ⛔ This file is an ALLOW-LIST and that is the whole design. A deny-list of
 * the surfaces a reader was seen reaching -- the command palette, Go to File,
 * Search for Text -- is wrong the next time the workbench gains a command,
 * and nothing would say so. An allow-list is wrong in the other direction:
 * a command a practice turns out to need is MISSING, the reader notices at
 * once, and the answer is one line here. Silent over-permission is the
 * failure this must not have; silent under-permission is visible and cheap.
 *
 * ⭐ So this file names what a reader editing ONE file needs, and
 * `removals()` computes the rest AGAINST THE WORKBENCH'S OWN default
 * keybindings, read at run time out of `vscode://defaultsettings/keybindings.json`.
 * Nothing here enumerates what is forbidden, and nothing has to be edited
 * when the workbench gains a command: the new command arrives in that
 * document, is not allowed, and is removed.
 *
 * ⛔ A keybinding removal is `{ "key": <the default's key>, "command": "-<id>" }`
 * and carries NO `when`. Measured, and it is not a shortcut: VS Code's
 * resolver treats a removal without a `when` clause as matching EVERY
 * `when` the default carries, so one entry per key+command removes all of
 * them. A removal that copies the default's `when` would only remove the
 * binding in the state that clause describes.
 *
 * ⛔ THE SURFACE THIS DOES NOT COVER. Removing a keybinding does not
 * unregister a command: `executeCommand` still runs it, and so does a context
 * menu entry. That half is the tab guard in `extension.js`, which is an
 * allow-list of exactly one thing -- the file this window's URL opened -- and
 * closes every other tab whatever opened it.
 */

/** Command families a practice needs, matched as an id PREFIX.
 *
 *  ⚠️ Three, and each is a whole namespace on purpose:
 *
 *   - `cursor`      every caret move and selection (`cursorLeft`, `cursorTop`,
 *                   `cursorWordEndRightSelect`, `cursorUndo` ...). There are
 *                   over fifty and they are the arrow keys.
 *   - `editor.action.`  every action that acts INSIDE the open editor: find,
 *                   replace, comment, format, fold, multi-cursor, select all.
 *                   Some of them navigate (`revealDefinition`) -- that lands
 *                   as another TAB, and the tab guard closes it.
 *   - `notifications.`  dismissing a toast. A reader who cannot dismiss a
 *                   notification is reading their practice around it.
 */
const FAMILIES = ['cursor', 'editor.action.', 'notifications.'];

/** The individual commands a practice needs, which no family covers.
 *
 *  ⭐ Grouped by what they are for. They are the editor's own verbs, the
 *  widgets that sit inside it (suggest, parameter hints, find, code actions,
 *  rename, snippets), and ONE workbench command: save.
 */
const COMMANDS = [
    /* typing, deleting and history */
    'undo', 'redo', 'tab', 'outdent',
    'deleteLeft', 'deleteRight', 'deleteWordLeft', 'deleteWordRight',
    'expandLineSelection', 'cancelSelection', 'removeSecondaryCursors',

    /* scrolling the one file */
    'scrollLineDown', 'scrollLineUp', 'scrollPageDown', 'scrollPageUp',

    /* the find widget inside the editor -- NOT `workbench.action.findInFiles` */
    'actions.find', 'closeFindWidget', 'toggleFindCaseSensitive',
    'toggleFindInSelection', 'toggleFindRegex', 'toggleFindWholeWord',
    'togglePreserveCase',

    /* completion */
    'acceptSelectedSuggestion', 'acceptAlternativeSelectedSuggestion',
    'focusSuggestion', 'hideSuggestWidget', 'selectNextSuggestion',
    'selectPrevSuggestion', 'selectNextPageSuggestion', 'selectPrevPageSuggestion',
    'toggleSuggestionDetails', 'toggleSuggestionFocus', 'suggestWidgetCopy',
    'insertBestCompletion', 'insertNextSuggestion', 'insertPrevSuggestion',
    'toggleExplainMode',

    /* parameter hints */
    'closeParameterHints', 'showNextParameterHint', 'showPrevParameterHint',

    /* code actions (the lightbulb), on the one file */
    'acceptSelectedCodeAction', 'selectNextCodeAction', 'selectPrevCodeAction',
    'hideCodeActionWidget', 'previewSelectedCodeAction', 'toggleSectionCodeAction',
    'expandSectionCodeAction', 'collapseSectionCodeAction', 'clearFilterCodeActionWidget',

    /* rename and linked editing, which the language server offers in-place */
    'acceptRenameInput', 'acceptRenameInputWithPreview', 'cancelRenameInput',
    'focusNextRenameSuggestion', 'focusPreviousRenameSuggestion',
    'cancelLinkedEditingInput',

    /* snippets */
    'insertSnippet', 'jumpToNextSnippetPlaceholder', 'jumpToPrevSnippetPlaceholder',
    'leaveSnippet',

    /* ⭐ THE ONE workbench command. `files.autoSave` already writes the file,
       so this is muscle memory rather than correctness -- but a reader who
       presses Ctrl+S and watches nothing happen does not trust the editor. */
    'workbench.action.files.save',
];

const ALLOWED = new Set(COMMANDS);

/** Whether a practice may invoke `command`. */
function allows(command) {
    if (typeof command !== 'string' || !command) { return false; }
    if (ALLOWED.has(command)) { return true; }
    return FAMILIES.some(function (family) { return command.startsWith(family); });
}

/** The keybinding removals `defaults` implies, sorted and deduplicated.
 *
 *  `defaults` is the workbench's own default keybinding list --
 *  `[{ key, command, when? }, ...]` -- and the answer is what belongs in the
 *  user keybindings file so that none of the commands this file does not
 *  allow can be reached by a key.
 *
 *  ⛔ Deduplicated on key AND command, because one command is bound to one
 *  key several times with different `when` clauses, and one removal without
 *  a `when` covers all of them.
 */
function removals(defaults) {
    const seen = new Set();
    const out = [];
    for (const entry of defaults || []) {
        if (!entry || !entry.key || !entry.command || allows(entry.command)) { continue; }
        const mark = `${entry.key}\u0000${entry.command}`;
        if (seen.has(mark)) { continue; }
        seen.add(mark);
        out.push({ key: entry.key, command: `-${entry.command}` });
    }
    out.sort(function (left, right) {
        if (left.key !== right.key) { return left.key < right.key ? -1 : 1; }
        return left.command < right.command ? -1 : left.command > right.command ? 1 : 0;
    });
    return out;
}

/** Parse the workbench's default keybindings document, which is JSON with comments.
 *
 *  ⚠️ `//` lines only: the document VS Code generates has no block comments
 *  and no trailing commas, and this is the one reader of it. A parse that
 *  fails raises, and the caller records that rather than writing an empty
 *  removal list -- an empty one would read as "nothing to confine".
 */
function parse(text) {
    const stripped = String(text).replace(/^[ \t]*\/\/.*$/gm, '');
    const entries = JSON.parse(stripped);
    if (!Array.isArray(entries)) {
        throw new Error('the default keybindings document is not a list');
    }
    return entries;
}

module.exports = { FAMILIES, COMMANDS, allows, removals, parse };
