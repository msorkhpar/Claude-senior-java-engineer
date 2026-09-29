/* The page's execution client: Run and Submit, over the served run namespace.

   ⭐ **Served by the run namespace itself, at `/api/v1/run/client.js`, and never
   written into a built site.** A built page opens over `file://` naming no server
   (R8, and the floor `tests/studyforge/cli/serving.py` reads); this file names
   the API on every line that matters, so it exists exactly where the API does —
   an origin that can answer it.

   ⭐ **A client and nothing else.** It publishes `studyforge.run` — `available`,
   `start`, `stop`, `editor`, `runnable`, `practice`, `code`, `codeTest`, `practices` — and
   draws nothing: the practice panel that puts Run and
   Submit in front of a reader is the page's own. ⛔ A control this file
   drew where no panel exists would be a dead button, and a dead button is
   a promise the page cannot keep. ⛔ **Nothing here grades a quiz**: a quiz is
   graded in its own page, with no request (register ruling, 2026-09-25).

   ⛔ **Nothing this sends becomes a command** (spec §8.3, rule 3). A start is
   a `POST` to `/api/v1/run/<corpus>/<mode>/<practice>` with NO body: the path
   SELECTS a practice and one of its two acts, and the server reads the command
   from the unit's generated document. There is no argument here that could
   carry a command, and none is sent.

   ⛔ **The practice is named by its key, verbatim**: the string
   `progress.practice_key` minted in Python, which the page carries. Nothing
   here composes a key, splits one or encodes one; a key that is not segments
   of lowercase letters, digits and hyphens is refused before any request —
   a `.` or `..` segment would otherwise be resolved by the browser into a
   different endpoint.

   ⭐ **The two modes are the data's own words**: `run` (Run: the reader's
   program) and `test` (Submit: the grader). Only a `test` that exits 0 ever
   completes a practice, and that is decided and recorded by the server.

   ⭐ **Output arrives line by line** and each line is handed to `onLine` as it
   arrives; the promise resolves with the verdict the last line spells — a
   status, `timeout` or `stopped`. ⛔ Over `file://` there is no origin to ask
   (R8), so `available()` is false and nothing is sent — should a copy of this
   file ever be loaded there. */

(function () {
  'use strict';

  var BASE = '/api/v1/run/';
  var MODES = ['run', 'test'];
  var STOP = 'stop';
  var EDITOR = 'editor';
  var KEY = /^[a-z0-9]+(?:-[a-z0-9]+)*(?:\/[a-z0-9]+(?:-[a-z0-9]+)*)+$/;
  var CORPUS = /^[A-Za-z0-9][A-Za-z0-9._-]*$/;
  var EXIT = /^--- exit (.+) ---$/;

  function available() {
    return (location.protocol === 'http:' || location.protocol === 'https:') &&
      typeof fetch === 'function' && typeof TextDecoder === 'function';
  }

  function refused(reason) {
    return Promise.reject({ refused: reason });
  }

  /* Split what arrived into whole lines; the remainder waits for the next chunk. */
  function lines(buffer, onLine) {
    var parts = buffer.split('\n');
    var rest = parts.pop();
    parts.forEach(function (line) { onLine(line); });
    return rest;
  }

  function verdictOf(last) {
    var found = EXIT.exec(last || '');
    if (!found) { return null; }
    return /^\d+$/.test(found[1]) ? Number(found[1]) : found[1];
  }

  function start(corpus, practice, mode, onLine) {
    if (!available()) { return refused('no-origin'); }
    if (MODES.indexOf(mode) === -1) { return refused('mode'); }
    if (!CORPUS.test(corpus) || !KEY.test(practice)) { return refused('practice'); }
    return streamed(BASE + corpus + '/' + mode + '/' + practice, onLine);
  }

  /* POST to `url` and hand each line of the body to `onLine` as it arrives;
     resolve with the verdict the last line spells. */
  function streamed(url, onLine) {
    var last = null;
    var each = function (line) {
      last = line;
      if (onLine) { onLine(line); }
    };
    return fetch(url, {
      method: 'POST',
      cache: 'no-store',
      credentials: 'same-origin'
    }).then(function (response) {
      if (!response.ok) { return refused(response.status); }
      var reader = response.body.getReader();
      var decoder = new TextDecoder();
      var buffer = '';
      function pump() {
        return reader.read().then(function (chunk) {
          if (chunk.done) {
            buffer += decoder.decode();
            if (buffer) { each(buffer); }
            return verdictOf(last);
          }
          buffer = lines(buffer + decoder.decode(chunk.value, { stream: true }), each);
          return pump();
        });
      }
      return pump();
    });
  }

  /* The run index, asked once per page and remembered. ⭐ Where a running
     editor is can only be true at SERVE time: its host port is per-project and
     a built page may name no origin and no port (R8), so the page asks the
     origin it is being read at. ⛔ An index that cannot be read is no editor,
     never an error a reader sees. */
  var index = null;

  function asked() {
    if (!index) {
      index = fetch(BASE, { cache: 'no-store', credentials: 'same-origin' })
        .then(function (response) { return response.ok ? response.json() : {}; })
        .then(null, function () { return {}; });
    }
    return index;
  }

  /* Where this corpus's editor is — { origin, folder } — or null when there is
     not one to say. ⛔ Nothing here starts one, and nothing here builds a URL:
     a caller decides what to do with an origin and a folder. */
  function editor(corpus) {
    if (!available()) { return refused('no-origin'); }
    if (!CORPUS.test(corpus)) { return refused('corpus'); }
    return asked().then(function (answer) {
      var found = answer && answer.editor && answer.editor[corpus];
      return found && found.origin && found.folder ? found : null;
    });
  }

  /* Whether this corpus can run code now. ⛔ `false` only where the index says
     so: a corpus that declares its runner while that runner is down runs
     nothing, and the page then offers no Run, Submit or Run tests. ⭐ An index
     that cannot be read, or an older server that says nothing, is `true`: the
     page offers what it always offered, and a refused run says why. */
  function runnable(corpus) {
    if (!available()) { return refused('no-origin'); }
    if (!CORPUS.test(corpus)) { return refused('corpus'); }
    return asked().then(function (answer) {
      var said = answer && answer.runnable;
      return !(said && said[corpus] === false);
    });
  }

  /* One practice's two editor windows: where each is, and the workspace
     settings they are read under, prepared by the server on the way.

     ⭐ **Two windows of ONE editor, and each is addressed by its own URL** —
     which is the only thing that can tell them apart: an extension cannot read
     its own window's query string, and both windows share one workspace
     settings file, so anything an extension opened it would open in BOTH.

     ⛔ **The URLs are the SERVER's, never built here.** The `vscode-remote`
     authority is the editor's own host and port, which only the origin the
     reader is reading at can say (R8), and a path the editor does not hold is
     answered as nothing rather than as a URL that would open an empty, dirty
     buffer titled with the file's own name.

     ⛔ **`editor` stands where a mode stands and is not one of `MODES`**: it
     starts nothing. ⚠️ Anything but an answer — no editor, no such practice, a
     workspace that could not be prepared — is `null`, never an error a reader
     sees, and the page then shows the sentence it already ships.

     ⛔ **Asked only of an editor the index says is up.** With none, nothing is
     posted: a `404` for a practice no editor holds would be an error in the
     reader's console on every practice opened while the editor is stopped. */
  function practice(corpus, key) {
    if (!available()) { return refused('no-origin'); }
    if (!CORPUS.test(corpus) || !KEY.test(key)) { return refused('practice'); }
    return editor(corpus).then(function (up) {
      if (!up) { return null; }
      return fetch(BASE + corpus + '/' + EDITOR + '/' + key, {
        method: 'POST',
        cache: 'no-store',
        credentials: 'same-origin'
      }).then(function (response) {
        return response.ok ? response.json() : null;
      }).then(function (answer) {
        return answer && answer.main && answer.main.url ? answer : null;
      }, function () { return null; });
    });
  }

  /* ⭐ A LESSON'S CODE FILE: its two windows, and its test's run. The path is
     the corpus-relative one the page carries on the link, and it only SELECTS a
     file: the server pairs it, copies the code, and reads the command from the
     corpus's declared build tool (spec §8.3, rule 3). ⛔ A path that is not
     segments of letters, digits, `.`, `_` and `-` — none of them `.` or `..`,
     none opening with `-` — is refused here, before any request; one that is
     needs no encoding, and is sent as the page carries it. */
  var CODE = 'code';
  var CODE_TEST = 'code-test';
  var SEGMENT = /^(?!-)[A-Za-z0-9._-]+$/;

  function codePath(path) {
    var parts = String(path || '').split('/');
    for (var at = 0; at < parts.length; at += 1) {
      if (!SEGMENT.test(parts[at]) || parts[at] === '.' || parts[at] === '..') { return null; }
    }
    return parts.join('/');
  }

  /* Where one code file's windows are — `{ main, test, opened, runs }` — or
     null. ⚠️ Anything but an answer is null: the page then follows the link to
     the file's plain view, which is never broken. */
  function code(corpus, path) {
    if (!available()) { return refused('no-origin'); }
    var encoded = codePath(path);
    if (!CORPUS.test(corpus) || encoded === null) { return refused('code'); }
    return fetch(BASE + corpus + '/' + CODE + '/' + encoded, {
      method: 'POST',
      cache: 'no-store',
      credentials: 'same-origin'
    }).then(function (response) {
      return response.ok ? response.json() : null;
    }).then(function (answer) {
      return answer && answer.main && answer.main.url ? answer : null;
    }, function () { return null; });
  }

  /* Run the test `path` names, in the copy of the code, streamed like a run. */
  function codeTest(corpus, path, onLine) {
    if (!available()) { return refused('no-origin'); }
    var encoded = codePath(path);
    if (!CORPUS.test(corpus) || encoded === null) { return refused('code'); }
    return streamed(BASE + corpus + '/' + CODE_TEST + '/' + encoded, onLine);
  }

  function stop() {
    if (!available()) { return refused('no-origin'); }
    return fetch(BASE + STOP, { method: 'POST', cache: 'no-store', credentials: 'same-origin' })
      .then(function (response) { return response.ok ? response.json() : refused(response.status); })
      .then(function (answer) { return answer.stopped === true; });
  }

  /* ⭐ WHAT THE READER'S OWN RECORD SAYS OF A UNIT'S PRACTICES — `{ section key:
     passed }` — read from the state namespace, which reads the progress record
     on this machine (spec §8.5). ⛔ Asked, never cached, and never written: a
     pass is recorded by the server when a Submit exits 0, and nothing here can
     say one. ⚠️ Anything but an answer is `null`, and a card then says nothing
     rather than something wrong. The unit is named by its key, verbatim, and a
     key that is not one is refused before any request. */
  var STATE_BASE = '/api/v1/state/';
  var UNITS = '/units/';

  function practices(corpus, unit) {
    if (!available()) { return refused('no-origin'); }
    if (!CORPUS.test(corpus) || !KEY.test(unit)) { return refused('unit'); }
    return fetch(STATE_BASE + corpus + UNITS + unit, { cache: 'no-store', credentials: 'same-origin' })
      .then(function (response) { return response.ok ? response.json() : null; })
      .then(function (answer) {
        var held = answer && answer.practices;
        if (!held || typeof held !== 'object') { return null; }
        var passed = {};
        Object.keys(held).forEach(function (section) {
          passed[section] = !!held[section] && held[section].passed === true;
        });
        return passed;
      }, function () { return null; });
  }

  window.studyforge = window.studyforge || {};
  window.studyforge.run = {
    available: available,
    start: start,
    stop: stop,
    editor: editor,
    runnable: runnable,
    practice: practice,
    code: code,
    codeTest: codeTest,
    practices: practices,
    modes: MODES.slice()
  };
})();
