/*
 * One practice, and nothing else.
 *
 * A study page embeds this editor in an iframe beside the lesson, and the
 * reader's whole job in it is: read the test, edit the file, press Run on the
 * page. Everything else the workbench offers -- the explorer, the terminal,
 * the panel, tabs, other files -- is a way to end up somewhere the lesson did
 * not send them, so the layout is closed on startup and re-closed whenever the
 * practice changes.
 *
 * What this file does NOT do, on purpose:
 *
 *   - it is not a security boundary. code-server is an IDE with a shell, and
 *     an iframe of one is exactly as powerful as the process behind it. This
 *     removes the ways *in*, not the possibility. The security boundary is
 *     the container, the loopback bind and the one exact `--embed-origin`.
 *   - it does not make files read-only. That is `files.readonlyInclude` /
 *     `files.readonlyExclude` in the workspace settings the study server
 *     writes, which the editor enforces itself. ⛔ But it DOES keep that
 *     lock standing, and this is why: `files.readonlyExclude` is an object
 *     setting, VS Code MERGES object settings across scopes, and a USER-scope
 *     entry naming the test file is merged INTO the workspace lock and
 *     re-opens it. Measured, in a real session:
 *     `{"Main.java": true}` became `{"MainTest.java": true, "Main.java": true}`
 *     after one `ConfigurationTarget.Global` write. ⚠️ So a reader who can
 *     reach the settings editor can make the test that judges them writable,
 *     and confining the command surface is what stops that -- not tidiness.
 *
 * Which is the second job this file grew: the workbench's command surface is
 * confined to what a practice NEEDS, by keybinding and by tab, from the
 * allow-list in `allowed.js`. ⛔ Hiding a surface does not disable a command:
 * a closed Explorer is one `Ctrl+P` from being irrelevant.
 *   - it does not open any file. A page shows a practice in TWO iframes of
 *     this one code-server -- the file to edit in one, the test that judges
 *     it in the other -- and which file a window shows is decided by that
 *     window's own URL, through code-server's
 *     `payload=[["openFile", "vscode-remote://<host:port><abs path>"]]`.
 *     Nothing else can tell the two windows apart: an extension cannot read
 *     its own window's query string, and both windows share one workspace
 *     settings file, so anything this extension opened it would open in both.
 *
 * Which leaves it one job, and no knowledge of the practice at all: close
 * everything around the editor, and close every editor but the active one.
 *
 * ⛔ This extension belongs to no consumer and names none. Its identifier and
 * the configuration section below are this framework's, and both are derived
 * from ONE place -- `package.json` beside this file, which `lockdown.py`
 * packages and the image's build checks against its installed list.
 */

const vscode = require('vscode');
const fs = require('fs');
const path = require('path');

const keybindings = require('./keybindings');

/** This extension's own manifest, for its identifier and nothing else.
 *
 *  ⛔ The id is written in ONE place (`package.json` beside this file) and
 *  derived everywhere else, here included: the banner below carries it, and
 *  the image's activation gate greps for it. */
const manifest = require('./package.json');

/** The ONE line that says this extension RAN.
 *
 *  ⚠️ An installed extension and a running one are different facts, and
 *  this extension's own history is the proof: it was installed, listed by
 *  `code-server --list-extensions` and present in `extensions.json` for five
 *  rounds while the extension host activated it in NO session -- the
 *  workbench's Restricted Mode had disabled it, silently and with no error.
 *  ⛔ A check that reads the installed list cannot see that. So the extension
 *  announces itself on the extension host's own console, which lands in
 *  `remoteexthost.log`, and the image's build greps a REAL session's log for
 *  this banner before the image is tagged. ⭐ It is printed only after the
 *  first pass has actually run its commands, so an extension that activates
 *  and then throws writes nothing.
 *
 *  ⛔ It is written to the extension's OWN log directory (`context.logUri`,
 *  which the workbench gives every extension and which VS Code already fills
 *  with `vscode.git/Git.log` and the Java server's log), and NOT with
 *  `console.log`: measured on code-server 4.137.0, an extension's console goes
 *  to the BROWSER's devtools and reaches no file on the server, so a gate
 *  could never read it. */
const BANNER = `${manifest.publisher}.${manifest.name}: confined`;

/** The file the banner is written to, inside the directory the workbench gave us. */
const RECORD = `${manifest.name}.log`;

/** Record that this extension ran, and what it managed to close.
 *
 *  Best-effort and never thrown from: a window whose log directory cannot be
 *  written is still a window this extension should confine. */
function record(context, lines) {
    try {
        const directory = (context.logUri && context.logUri.fsPath) || context.logPath;
        if (!directory) { return; }
        fs.mkdirSync(directory, { recursive: true });
        fs.appendFileSync(path.join(directory, RECORD), `${lines.join('\n')}\n`);
    } catch (error) {
        /* not writable in this window; the confining above already happened */
    }
}

/** The configuration section whose rewrite means "the practice changed".
 *
 *  It is the namespace of the two keys `package.json` contributes, and
 *  nothing here reads their values: see the note above. */
const SECTION = 'studyforge.practice';

/** Everything that is not the one editor the URL asked for.
 *
 *  `closeOtherEditors` is first and it is the whole trick. The workbench
 *  handles the URL's `openFile` payload while it starts up, well before
 *  extensions activate on `onStartupFinished` -- so by the time this runs,
 *  the payload's file already *is* the active editor. Closing the others
 *  leaves exactly the file that window was addressed with, and this
 *  extension never has to know which file that was. It runs first so that
 *  it acts before any of the closes below can move focus. */
const CONFINE = [
    'workbench.action.closeOtherEditors',
    'workbench.action.closeSidebar',
    'workbench.action.closePanel',
    'workbench.action.closeAuxiliaryBar',
];

/** When to close again after the first attempt, in milliseconds.
 *
 *  ⭐ A BELT, NOT THE MECHANISM, and the side bar is no longer why. The image
 *  starts the primary side bar CLOSED -- the Dockerfile step
 *  "THE PRIMARY SIDE BAR STARTS CLOSED" patches the workbench's default --
 *  so there is no Explorer for a retry to close: measured on the image with
 *  that step, a two-practice session painted the side bar in 0 frames over
 *  30 s, the Java language server running. What the retries still cover is
 *  anything that opens AFTER activation -- a panel, the auxiliary bar or a
 *  stray editor raised by an extension that activates later than this one.
 *  ⚠️ An earlier image started with the Explorer open, a single close at
 *  activation did not hold, and these retries were what shut it; that is the
 *  history of the schedule, not its current reason.
 *
 *  A decaying schedule rather than a permanent interval, so this settles the
 *  workbench and then stops. Nothing here fights the reader forever. */
const RETRIES = [250, 750, 2000, 6000, 12000];

/** Close every surrounding surface, and every editor but the active one.
 *  Each command is best-effort: a workbench without one of these still has
 *  the others. */
async function confine() {
    const ran = [];
    for (const command of CONFINE) {
        try {
            await vscode.commands.executeCommand(command);
            ran.push(command);
        } catch (error) {
            /* not in this build; the rest still apply */
        }
    }
    return ran;
}

/** The one editor this window was addressed with, learned rather than told.
 *
 *  ⭐ `closeOtherEditors` has already run by the time this is read, so the
 *  active editor IS the file this window's URL opened -- the same trick the
 *  note above `CONFINE` describes, one step further. ⛔ It is learned ONCE:
 *  if a stray tab ever did become active before this ran, re-learning would
 *  adopt the stray as the thing to keep and close the practice. */
let mine = null;

/** Close every tab that is not the one file this window opened.
 *
 *  ⛔ THIS IS THE COMMAND HALF OF THE CONFINEMENT, and it is an allow-list of
 *  exactly one thing. Removing a keybinding does not unregister a command:
 *  the editor's own context menu still offers Go to Definition, an extension
 *  can still `executeCommand`, and either lands the reader in a file the
 *  lesson did not send them to. ⭐ Rather than enumerate what may open a tab
 *  -- which is the deny-list that is wrong the next time the workbench gains
 *  a command -- this closes everything that is not the practice, whatever
 *  opened it and whenever. The settings editor is closed by the same rule as
 *  a stray source file, and neither is named here.
 *
 *  ⚠️ A tab whose `input` is not a text document (the settings editor, a
 *  webview, a diff) has no `uri` to compare, so it is not the practice and it
 *  goes. Measured: the settings editor arrives as a tab labelled `Settings`
 *  with an input this extension host cannot type.
 *
 *  ⛔ Does nothing at all while `mine` is unknown. A guard that does not know
 *  what to keep must not start closing. */
async function closeStrangers() {
    if (!mine) { return []; }
    const closed = [];
    for (const group of vscode.window.tabGroups.all) {
        for (const tab of group.tabs) {
            const input = tab.input;
            if (input && input.uri && input.uri.toString() === mine) { continue; }
            try {
                await vscode.window.tabGroups.close(tab, true);
                closed.push(tab.label);
            } catch (error) {
                /* already gone, or this build will not close it */
            }
        }
    }
    return closed;
}

function learn() {
    const active = vscode.window.activeTextEditor;
    if (!mine && active && active.document) { mine = active.document.uri.toString(); }
    return mine;
}

function apply(context) {
    confine().then(function () { learn(); return closeStrangers(); });
    for (const delay of RETRIES) {
        const timer = setTimeout(confine, delay);
        context.subscriptions.push({ dispose: () => clearTimeout(timer) });
    }
}

function activate(context) {
    /* ⭐ The first pass announces itself, and only this one does: the retries
       and the practice-changed passes are the same work again, and a banner
       per pass would say nothing more while filling the log. */
    confine().then(async function (ran) {
        learn();
        const closed = await closeStrangers();
        record(context, [
            `${BANNER} ${ran.join(' ')}`,
            `${BANNER} kept=${mine ? 1 : 0} closed=${closed.length}`,
            await keybindings.report(context, BANNER),
        ]);
    });
    for (const delay of RETRIES) {
        const timer = setTimeout(confine, delay);
        context.subscriptions.push({ dispose: () => clearTimeout(timer) });
    }
    context.subscriptions.push(
        /* ⛔ Not on a schedule and not only at startup: a tab can open at any
           moment, so the guard runs whenever the workbench says the tabs
           changed. Closing one fires this again, finds nothing left to close
           and stops. */
        vscode.window.tabGroups.onDidChangeTabs(function () { closeStrangers(); }),
        vscode.workspace.onDidChangeConfiguration(function (event) {
            /* The reader moved to another practice and the study server
               rewrote the workspace settings. This extension no longer reads
               those two keys -- the URL names the file now -- but the rewrite
               is still the one signal in here that the practice changed, and
               a changed practice means a window that should be confined
               again: the workbench may have restored a surface, and the new
               URL's file wants the others closed around it. Idempotent, so
               an extra pass over an already-confined window costs nothing. */
            if (event.affectsConfiguration(SECTION)) { apply(context); }
        }),
    );
}

function deactivate() {}

module.exports = { activate, deactivate, SECTION, CONFINE, RETRIES, BANNER, RECORD, closeStrangers };
