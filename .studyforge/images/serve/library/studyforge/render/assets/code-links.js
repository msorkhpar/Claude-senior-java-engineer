/* A lesson's code examples: each one opens in the course's editor, in place.

   ⭐ **An example is an entry the build drew** (`details[data-code-example]`,
   `render/page/code.py`): a source and its paired test, named for the source,
   with the page's own links to each file inside it. Served, with the corpus's
   editor up, expanding the entry opens the example RIGHT THERE — the source
   and the test in two windows of ONE editor, a tab for each, and a Run that
   runs the test. ⛔ **Anything short of that leaves the entry as built**: no
   server, no editor, a file the server will not open — each link is the file's
   plain view, which is never broken, and the entry's sentence says why.

   ⛔ **Nothing loads until an entry is expanded, and one entry at most is
   live.** Expanding another closes the first, and a closed entry's frames are
   removed, so a page never carries an example's editor it is not showing.

   ⛔ **The page never moves.** No entry is scrolled to, and each window is
   built by `window.studyforge.frames.frame`, which `practice-editor.js`
   publishes — the focus guard that keeps a workbench from taking the reader's
   focus or the page's position, and the one reload when a cold instance's
   frame policy blocked the frame. ⛔ Neither is copied here.

   ⛔ **This file draws; it never talks to the API.** Everything it asks goes
   through `window.studyforge.run` — `editor`, `code`, `codeTest`, `stop` —
   which the SERVING PROCESS adds to the page it answers, so a built text names
   no API, no origin and no client file (R8).

   ⭐ **The copy is said, not hidden**: shown only once the editor answered,
   each entry's second sentence tells the reader the editor opens a COPY of the
   code, where their changes and a run's output stay. */

(function () {
  'use strict';

  /* ⚠️ Spelled here and in `render/page/code.py`, `code-examples.html` and
     `code-example.html` — the two-sided spelling every hook on this page has. */
  var EXAMPLES = 'div[data-code-examples]';
  var ENTRY = 'details[data-code-example]';
  var OPEN = 'data-code-open';
  var CORPUS = 'data-corpus';
  var PATH = 'data-code-path';
  var PART = 'data-code-part';
  var TAB = 'data-code-tab';
  var FRAME = 'data-code-frame';
  var ACT = 'data-code-act';
  var WINDOWS = ['main', 'test'];
  var TITLES = { main: 'Source', test: 'Test' };

  var lists = [].slice.call(document.querySelectorAll(EXAMPLES));
  if (!lists.length) { return; }
  var run = window.studyforge && window.studyforge.run;
  var frames = window.studyforge && window.studyforge.frames;
  if (!run || !run.available() || !run.code || !run.editor || !frames) { return; }

  var entries = [];
  lists.forEach(function (list) {
    [].slice.call(list.querySelectorAll(ENTRY)).forEach(function (details) {
      entries.push({ details: details, corpus: list.getAttribute(CORPUS), where: null, built: {} });
    });
  });

  function part(entry, name) { return entry.details.querySelector('[' + PART + '="' + name + '"]'); }
  function show(element, visible) { if (element) { element.hidden = !visible; } }
  function tabs(entry) { return [].slice.call(entry.details.querySelectorAll('[' + TAB + ']')); }
  function slot(entry, name) { return entry.details.querySelector('[' + FRAME + '="' + name + '"]'); }

  /* ⭐ The one live example, and the one answer it is waiting for. */
  var live = null;
  var asked = 0;

  function select(entry, name) {
    tabs(entry).forEach(function (button) {
      var mine = button.getAttribute(TAB) === name;
      button.setAttribute('aria-selected', mine ? 'true' : 'false');
      button.tabIndex = mine ? 0 : -1;
    });
    WINDOWS.forEach(function (one) { show(slot(entry, one), one === name); });
    var where = entry.where;
    if (where && !entry.built[name] && where[name] && where[name].url) {
      entry.built[name] = true;
      var named = tabs(entry).filter(function (button) {
        return button.getAttribute(TAB) === name;
      })[0];
      frames.frame(slot(entry, name), where[name].url, named ? named.textContent : TITLES[name]);
    }
  }

  /* ⛔ Closing empties the windows: a closed entry holds no editor. */
  function unload(entry) {
    WINDOWS.forEach(function (name) { slot(entry, name).textContent = ''; });
    entry.where = null;
    entry.built = {};
    show(part(entry, 'editor'), false);
    show(part(entry, 'controls'), false);
    part(entry, 'status').textContent = '';
    show(part(entry, 'output'), false);
    if (live === entry) { live = null; }
  }

  function draw(entry, where) {
    entry.where = where;
    entry.built = {};
    frames.reloadWhenBlocked(where.main.url);
    show(part(entry, 'editor'), true);
    show(part(entry, 'controls'), !!where.runs && runs);
    select(entry, 'main');
    setTimeout(function () { remember(null); }, 5000);
  }

  function load(entry) {
    if (live === entry) { return; }
    if (live) { var before = live; before.details.open = false; unload(before); }
    live = entry;
    var mine = ++asked;
    remember(entries.indexOf(entry));
    run.code(entry.corpus, entry.details.getAttribute(OPEN)).then(function (where) {
      if (mine !== asked || live !== entry || !entry.details.open) { return; }
      if (where) { draw(entry, where); } else { remember(null); }
    }, function () { remember(null); });
  }

  /* ⚠️ A COLD instance's page is reloaded once, by `frames.reloadWhenBlocked`,
     when its frame policy blocked the first frame — and a reload forgets the
     expanded entry. ⭐ So the entry rides on this history entry's own state,
     which a reload keeps and nothing else reads, until it is drawn; a page that
     was just reloaded opens it again. ⛔ Not the browser's store: that is
     `study-progress.js`'s alone. */
  var REOPEN = 'studyforgeCode';

  function remember(index) {
    try {
      var state = {};
      state[REOPEN] = typeof index === 'number' && index >= 0
        ? { entry: index, x: window.scrollX, y: window.scrollY } : null;
      history.replaceState(state, '');
    } catch (ignored) { return; }
  }

  function reopened() {
    try {
      var timing = performance.getEntriesByType('navigation');
      var kept = history.state && history.state[REOPEN];
      remember(null);
      return timing.length && timing[0].type === 'reload' && kept && typeof kept.entry === 'number'
        ? kept : null;
    } catch (ignored) { return null; }
  }

  /* ⭐ The reload put the page wherever the browser restored it, before the
     entry reopened; the reader is put back exactly where they clicked, once.
     ⛔ This is the one place the page is moved, and only to where it WAS. */
  function putBack(kept) {
    window.scrollTo({ left: kept.x, top: kept.y, behavior: 'instant' });
  }

  function wire(entry) {
    tabs(entry).forEach(function (button) {
      button.addEventListener('click', function () {
        if (entry.where) { select(entry, button.getAttribute(TAB)); }
      });
    });
    var act = entry.details.querySelector('[' + ACT + '="test"]');
    var stop = entry.details.querySelector('[' + ACT + '="stop"]');
    act.addEventListener('click', function () {
      var where = entry.where;
      if (!where || !where.runs || !runs) { return; }
      var status = part(entry, 'status');
      var output = part(entry, 'output');
      act.disabled = true;
      show(stop, true);
      output.textContent = '';
      show(output, true);
      status.textContent = 'Running the test…';
      run.codeTest(entry.corpus, where.runs, function (line) {
        output.textContent += line + '\n';
      }).then(function (verdict) {
        status.textContent = verdict === 0 ? 'Passed.' : verdict === 'stopped' ? 'Stopped.'
          : verdict === 'timeout' ? 'Timed out.' : 'Failed.';
      }, function () {
        status.textContent = 'The test could not be run.';
      }).then(function () {
        act.disabled = false;
        show(stop, false);
      });
    });
    stop.addEventListener('click', function () { run.stop(); });
    entry.details.addEventListener('toggle', function () {
      if (entry.details.open) { load(entry); } else if (live === entry) { unload(entry); }
    });
    /* ⭐ A link inside an open entry shows its own file's window, in place. */
    entry.details.addEventListener('click', function (event) {
      if (event.defaultPrevented || event.button !== 0) { return; }
      if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) { return; }
      var anchor = event.target.closest ? event.target.closest('a[' + PATH + ']') : null;
      var where = entry.where;
      if (!anchor || !where) { return; }
      var path = anchor.getAttribute(PATH);
      var name = where.test && where.test.file === path ? 'test' : 'main';
      if (where[name] && where[name].file === path) {
        event.preventDefault();
        select(entry, name);
      }
    });
  }

  /* ⭐ Only an editor that is UP makes an entry open the editor; until then
     each entry is its lines and the editor's sentence, and every link is the
     file's plain view. ⭐ The runner is its own service: with the editor up and
     a declared runner down, the editor still opens the files, and only Run
     tests is withheld, with the runner's sentence — each panel names the
     service that is missing, from the run index's `editor` and `runnable`. */
  var runs = false;
  var runnable = run.runnable ? run.runnable(entries[0].corpus) : Promise.resolve(true);
  Promise.all([run.editor(entries[0].corpus), runnable]).then(function (both) {
    var found = both[0];
    if (!found) { return; }
    runs = !!both[1];
    entries.forEach(function (entry) {
      show(part(entry, 'plain'), false);
      show(part(entry, 'copy'), true);
      show(part(entry, 'no-runner'), !runs);
      wire(entry);
    });
    var kept = reopened();
    var again = kept ? entries[kept.entry] || null : null;
    if (again) {
      again.details.open = true;
      requestAnimationFrame(function () { putBack(kept); });
    }
    entries.forEach(function (entry) {
      if (entry.details.open && !live) { load(entry); } else if (entry.details.open) {
        entry.details.open = false;
      }
    });
  }, function () { return null; });
}());
