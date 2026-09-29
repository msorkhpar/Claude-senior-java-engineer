/* The practice panel: Run, Submit, the result, and what the Submit reported.

   ⛔ **This file draws; it never talks to the API.** Everything it sends goes
   through `window.studyforge.run` — `available()`, `start(corpus, practice,
   mode, onLine)`, `stop()` — which the SERVING PROCESS adds to the page it
   answers (`serve.routes.assets` says how). ⭐ That is the whole
   reason this part can live in a built site at all: a built text that named the
   API, the serving origin or the client file is a defect R8's floor reads
   (`tests/studyforge/cli/serving.py`), and there is no such name below.

   ⛔ **Over `file://` there is no origin to ask, so the controls stay HIDDEN**
   and the panel shows the sentence that says why. ⚠️ Nothing is disabled: a
   dead button is a promise the page cannot keep, which is this panel's own
   rule about the editor and about Submit.

   ⛔ **The practice key and the two mode words are read off the markup,
   verbatim.** `data-practice` carries the string `progress.practice_key` minted
   in Python and `data-practice-act` carries one of `exercise.COMMANDS`. Nothing
   here composes a key, splits one, encodes one or invents a mode — the client
   refuses a malformed key before any request, and a key spelled twice would
   simply never match anything with nothing failing anywhere.

   ⛔ **Nothing is written to browser storage.** A run's outcome is the SERVER's
   record (spec §8.5), written where it was established; a page that also
   remembered would be a second answer to *did this pass?*. ⭐ So nothing here
   has to be namespaced against the one storage origin every `file://` page
   shares.

   ## ⭐ THE TWO EDITOR WINDOWS ARE `practice-editor.js`'s

   ⚠️ **This file stood at `399` of R11's `400`** and the breakdown below had to
   go somewhere. ⛔ **Neither a size exception nor a trim of four other rows'
   prose was an honest answer**, so the split was taken
   at the seam by subject: the frames, their tablist and the one reload a cold
   instance needs are *the editor*, and this file is *the controls, the run and
   what the run reported*. ⭐ The two share nothing but the markup.

   ## ⛔ THE BREAKDOWN IS READ OFF THIS RUN'S OWN STREAM, NOT FETCHED

   ⛔ **A built page may name no API and no origin** (R8), so there is no
   asking the state namespace where the recorded breakdown lives. ⭐ **The run's
   response body is the one thing the server already hands this page**, and the
   verdicts are said on it — one framed `--- case <id>: passed|failed ---` per
   declared case, just before the exit line, by `serve.routes.breakdown`.

   ⭐ **The counts are DERIVED here and were never recorded**: Python renders
   every declared case with its id, its kind and the corpus's own sentence, and
   this joins the run's verdicts onto them. ⛔ **A population that does not match
   is shown as NOTHING rather than as a partial count** — a breakdown whose case
   set differs from the panel's is a breakdown of a different practice, which is
   what a corpus regenerated under a reader looks like.

   ⛔ **A reader shown *edge cases 2/3* is looking at an INCOMPLETE practice and
   never at a new kind of verdict.** `progress.is_pass` is untouched, this file
   never decides what passed, and the status line beside the breakdown is still
   the run's own word. */

(function () {
  'use strict';

  /* The panel, and the attributes it carries. ⚠️ Spelled here and in
     `render/page/practice.py`, which is the same two-sided spelling every hook
     on this page has: markup and script cannot import one another, and the
     Python side is the single source for what is EMITTED. */
  var PANEL = 'section[data-practice]';
  var KEY = 'data-practice';
  var CORPUS = 'data-corpus';
  var PART = 'data-practice-part';
  var ACT = 'data-practice-act';
  var STOP = 'stop';
  var TEST = 'test';

  /* ⭐ What a finished run tells the rest of the page, raised on the panel: the
     workspace reads the reader's record again after it (`practice-workspace.js`),
     so a card says *passed* once the server has recorded a pass. */
  var SETTLED = 'studyforge:practice-settled';

  /* One declared case, its kind, and the mark a verdict leaves on it. ⚠️ The
     verdict is an attribute AND a word: a breakdown told apart only by colour
     is a breakdown a screen reader cannot read. */
  var CASE = 'data-practice-case';
  var CASE_KIND = 'data-practice-case-kind';
  var VERDICT = 'data-practice-verdict';
  var MAIN = 'main';
  var EDGE = 'edge';
  var PASSED = 'passed';
  var FAILED = 'failed';

  /* Every word the breakdown says, kept where the markup is: a label spelled
     in the script too would be a second place for it to drift. */
  var SAYS = {
    done: 'data-practice-ask-done',
    missed: 'data-practice-ask-missed',
    edges: 'data-practice-edges',
    passed: 'data-practice-passed',
    failed: 'data-practice-failed'
  };

  /* What one case's verdict looks like on the stream. ⛔ Anchored whole, and the
     id must be one this panel DECLARES before anything is drawn — so a line of
     this shape out of a grader's own output reaches a case set that does not
     match and is shown as nothing. ⚠️ The record is unaffected either way: it is
     folded from the grader's report on the server, never from this text. */
  var CASE_LINE = /^--- case (.+): (passed|failed) ---$/;

  function part(panel, name) {
    return panel.querySelector('[' + PART + '="' + name + '"]');
  }

  function show(element, visible) {
    if (element) { element.hidden = !visible; }
  }

  function words(element, name) {
    return element.getAttribute(SAYS[name]) || '';
  }

  /* One line of output, appended as it arrives. ⚠️ `textContent`, never
     `innerHTML`: a program's own output is not markup, and a grader that
     printed a tag would otherwise be parsed as one. */
  function append(output, line) {
    output.appendChild(document.createTextNode(line + '\n'));
    output.scrollTop = output.scrollHeight;
  }

  /* What a finished run is called, from the verdict the client resolves with:
     a status number, 'timeout' or 'stopped'. ⛔ The server decides what a run
     MEANT and records it; this only says what the reader just watched. */
  function verdict(answer, mode) {
    if (answer === 'stopped') { return 'Stopped.'; }
    if (answer === 'timeout') { return 'Timed out.'; }
    if (answer !== 0) { return 'Finished with errors.'; }
    return mode === 'run' ? 'Finished.' : 'Passed.';
  }

  function refusal(answer) {
    if (answer && answer.refused === 409) {
      return 'Something is already running. Stop it first.';
    }
    return 'That could not be started.';
  }

  /* ⭐ **Every declared case, marked with what THIS run said about it.** ⛔ Drawn
     only when the two populations are the same set, and cleared to nothing
     whenever they are not — the argument is at the top of this file. */
  function breakdown(panel) {
    var region = part(panel, 'breakdown');
    if (!region) { return null; }
    var rows = [].slice.call(region.querySelectorAll('[' + CASE + ']'));
    var summary = part(region, 'summary');

    function clear() {
      show(region, false);
      rows.forEach(function (row) {
        row.removeAttribute(VERDICT);
        var mark = part(row, 'verdict');
        if (mark) { mark.textContent = ''; }
      });
      if (summary) { summary.textContent = ''; }
    }

    /* ⛔ Both directions, because either alone lets a partial count through: a
       case the run never named, and a name this panel never declared. */
    function whole(said) {
      var known = 0;
      rows.forEach(function (row) {
        if (Object.prototype.hasOwnProperty.call(said, row.getAttribute(CASE))) { known += 1; }
      });
      return !!rows.length && known === rows.length && known === Object.keys(said).length;
    }

    function draw(said) {
      if (!whole(said)) { clear(); return; }
      var edges = 0;
      var passed = 0;
      var ask = true;
      rows.forEach(function (row) {
        var right = said[row.getAttribute(CASE)];
        var edge = row.getAttribute(CASE_KIND) === EDGE;
        var mark = part(row, 'verdict');
        row.setAttribute(VERDICT, right ? PASSED : FAILED);
        if (mark) { mark.textContent = words(region, right ? 'passed' : 'failed'); }
        if (edge) { edges += 1; }
        if (edge && right) { passed += 1; }
        if (row.getAttribute(CASE_KIND) === MAIN && !right) { ask = false; }
      });
      if (summary) {
        summary.textContent = words(region, ask ? 'done' : 'missed') + ' ' +
          words(region, 'edges').replace('{passed}', passed).replace('{total}', edges);
      }
      show(region, true);
    }

    return { clear: clear, draw: draw };
  }

  function wire(panel, run) {
    var key = panel.getAttribute(KEY);
    var corpus = panel.getAttribute(CORPUS);
    var controls = part(panel, 'controls');
    var status = part(panel, 'status');
    var output = part(panel, 'output');
    var acts = [].slice.call(panel.querySelectorAll('[' + ACT + ']'));
    if (!key || !corpus || !controls || !status || !output || !acts.length) { return; }
    var cases = breakdown(panel);

    /* ⭐ The editor slot is shown, and what it shows is the sentence saying the
       editor is not running. Hiding it instead would be the blank panel this
       row exists to refuse. ⚠️ What FILLS it is `practice-editor.js`'s. */
    show(part(panel, 'offline'), false);
    show(part(panel, 'editor'), true);
    /* ⛔ The controls are shown only where the index says this corpus can run
       code now: a corpus whose runner is declared and down runs nothing, so it
       gets the sentence saying why instead of a button that would be refused. */
    var asking = run.runnable ? run.runnable(corpus) : Promise.resolve(true);
    asking.then(function (ok) {
      show(controls, ok);
      show(part(panel, 'no-runner'), !ok);
    }, function () { show(controls, true); });

    var stop = null;
    var starters = [];
    acts.forEach(function (button) {
      if (button.getAttribute(ACT) === STOP) { stop = button; } else { starters.push(button); }
    });

    /* ⛔ **Focus follows the control that goes away, and that is keyboard
       correctness rather than polish.** A button that is disabled or hidden
       while it holds focus drops focus to the document, and a keyboard reader
       is returned to the top of the page mid-run. So the start moves focus to
       Stop and the end gives it back to the button that was pressed — and only
       ever when this panel already had it. */
    var pressed = null;

    /* ⛔ **Set when STOP takes focus away from this panel itself**, which is the
       one hand-back `holdsFocus()` cannot answer for. Pressing Stop disables
       Stop, a disabled element drops focus to the document AT ONCE, and the
       run then settles a moment later with focus already on `<body>` — so the
       question *did the panel have focus?* answers no and the keyboard reader
       is left at the top of the page. ⚠️ The ordinary end-of-run path does
       not have this problem; only Stop does. */
    var handedBack = false;

    /* ⚠️ Asked BEFORE the control is disabled or hidden, never after: a
       disabled element drops focus to the document immediately, so a check
       taken afterwards always answers no and the reader is left at the top of
       the page. */
    function holdsFocus() {
      return panel.contains(document.activeElement);
    }

    function live(running) {
      starters.forEach(function (button) { button.disabled = running; });
      show(stop, running);
    }

    function settle(text, said) {
      var keyboard = holdsFocus() || handedBack;
      handedBack = false;
      status.textContent = text;
      if (cases && said) { cases.draw(said); }
      live(false);
      /* ⛔ AFTER `live(false)`: the button that was pressed is disabled while
         the run is live, and focusing a disabled control does nothing at all. */
      if (keyboard && pressed) { pressed.focus(); }
      panel.dispatchEvent(new CustomEvent(SETTLED, { bubbles: true }));
    }

    starters.forEach(function (button) {
      button.addEventListener('click', function () {
        var mode = button.getAttribute(ACT);
        var keyboard = holdsFocus();
        var said = mode === TEST ? {} : null;
        pressed = button;
        output.textContent = '';
        show(output, true);
        status.textContent = 'Running…';
        if (cases) { cases.clear(); }
        live(true);
        if (keyboard && stop) { stop.focus(); }
        run.start(corpus, key, mode, function (line) {
          /* ⛔ A case line is the SERVER talking about this run rather than the
             program's own output, so it is taken OFF the stream instead of
             standing raw beside the breakdown it feeds — the same way the client
             already takes the exit line. ⚠️ Every other line, the breakdown's
             own refusal included, reaches the reader unchanged. */
          var found = said ? CASE_LINE.exec(line) : null;
          if (found) { said[found[1]] = found[2] === PASSED; } else { append(output, line); }
        }).then(
          function (answer) { settle(verdict(answer, mode), said); },
          function (answer) { settle(refusal(answer), null); }
        );
      });
    });

    if (stop) {
      stop.addEventListener('click', function () {
        /* ⛔ Asked BEFORE the line below, for the reason `holdsFocus` states:
           this IS the disable that drops focus to the document. */
        handedBack = holdsFocus();
        stop.disabled = true;
        run.stop().then(
          function () { stop.disabled = false; },
          function () { stop.disabled = false; }
        );
      });
    }
  }

  var panels = [].slice.call(document.querySelectorAll(PANEL));
  if (!panels.length) { return; }
  var run = window.studyforge && window.studyforge.run;
  if (!run || !run.available()) { return; }
  panels.forEach(function (panel) { wire(panel, run); });
}());
