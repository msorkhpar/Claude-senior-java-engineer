/* The practice workspace's panes: the description resized and closed, and the
   report resized and collapsed.

   ⭐ **Register ruling (2026-09-26).** In a code practice's workspace:
   - the divider between the description and the editor is dragged — mouse,
     touch or pen — to widen or narrow the description, and moved from the
     keyboard with Left and Right (Home and End to the limits);
   - *Hide description* closes the description so the editor takes the whole
     width, and a slim edge stays at the left to open it again; Enter on the
     divider does the same;
   - the divider above the report is dragged, or moved with Up and Down, to
     give the code more height;
   - the report collapses to a slim bar at the bottom that still shows the
     last verdict, and a new Run or Submit opens it again.
   Every one of the four is kept per reader, in the display record of
   `study-progress.js` — the one file that touches the store — so a page with
   no storage still resizes and closes, for as long as it is open.

   ⛔ **Nothing is moved, copied or created.** Every control is markup, shipped
   `hidden` (`practice-workspace.html`, `practice-panel.html`); this file shows
   them, and says the reader's choices to `practice-panes.css` through two
   custom properties and three attributes on the root. An `iframe` moved to
   another parent reloads (`practice-workspace.js`), and nothing here moves one.

   ⛔ **A drag is the divider's until it ends**: the pointer is captured, and
   the root carries `data-workspace-dragging` so every frame ignores the
   pointer meanwhile — or a drag over the editor would stop dead there.

   ⚠️ **The description's side is a desktop's.** At phone width the workspace
   stacks, and a quiz is one column at every width: there the divider, the
   toggle and the edge stay hidden, and a closed description is shown. The
   report's controls work at every width, and only where a run can report —
   over `file://` there is nothing to report and they stay hidden. */

(function () {
  'use strict';

  var shell = document.querySelector('div[data-workspace]');
  if (!shell) { return; }
  var root = document.documentElement;
  var store = window.studyforge && window.studyforge.progress;
  var run = window.studyforge && window.studyforge.run;
  var served = !!(run && run.available && run.available());

  /* What the stylesheet reads, spelled here and in `practice-panes.css`. */
  var LEFT = '--workspace-left';
  var REPORT = '--workspace-report';
  var STATEMENT = 'data-workspace-statement';
  var SIZED = 'data-workspace-report-sized';
  var DRAGGING = 'data-workspace-dragging';
  var QUIZ = 'data-workspace-quiz';
  var SHOWN = 'data-practice-report';
  var NARROW = '(max-width: 40rem)';
  var OPENED = 'studyforge:practice-opened';
  var SETTLED = 'studyforge:practice-settled';

  /* The reader's four choices, by name in the display record. */
  var KEPT_LEFT = 'workspace-left';
  var KEPT_STATEMENT = 'workspace-statement';
  var KEPT_REPORT = 'workspace-report';
  var KEPT_REPORT_SHOWN = 'workspace-report-shown';
  var OPEN = 'open';
  var CLOSED = 'closed';

  /* The clamps, in pixels, and a key press's step, in percent. ⭐ The
     description keeps a readable column, and the editor the width of a line. */
  var LEAST_LEFT = 240;
  var LEAST_RIGHT = 360;
  var LEAST_REPORT = 72;
  var STEP = 2;
  var DEFAULT_LEFT = 42;

  function recall(name) {
    try { return store ? store.preference(name) : null; } catch (ignored) { return null; }
  }

  function keep(name, value) {
    try { if (store) { store.prefer(name, String(value)); } } catch (ignored) { return; }
  }

  function number(said) {
    var value = said === null ? NaN : parseFloat(said);
    return isFinite(value) ? value : null;
  }

  function within(value, low, high) { return Math.min(high, Math.max(low, value)); }

  function tenth(value) { return Math.round(value * 10) / 10; }

  function range(element, low, high, now) {
    element.setAttribute('aria-valuemin', String(Math.round(low)));
    element.setAttribute('aria-valuemax', String(Math.round(high)));
    element.setAttribute('aria-valuenow', String(Math.round(now)));
  }

  function words(element, attribute, open) {
    [].slice.call(element.querySelectorAll('[' + attribute + ']')).forEach(function (word) {
      word.hidden = word.getAttribute(attribute) !== (open ? 'hide' : 'show');
    });
  }

  /* --- a drag, the same for both dividers ------------------------------- */
  function draggable(handle, axis, move, done) {
    handle.addEventListener('pointerdown', function (event) {
      if (event.button !== 0) { return; }
      event.preventDefault();
      try { handle.setPointerCapture(event.pointerId); } catch (ignored) { /* moved on */ }
      root.setAttribute(DRAGGING, axis);
      var ended = false;
      function moved(later) { move(later); }
      function end() {
        if (ended) { return; }
        ended = true;
        handle.removeEventListener('pointermove', moved);
        ['pointerup', 'pointercancel', 'lostpointercapture'].forEach(function (name) {
          handle.removeEventListener(name, end);
        });
        root.removeAttribute(DRAGGING);
        done();
      }
      handle.addEventListener('pointermove', moved);
      ['pointerup', 'pointercancel', 'lostpointercapture'].forEach(function (name) {
        handle.addEventListener(name, end);
      });
    });
  }

  /* --- the description's side ------------------------------------------ */
  var divider = shell.querySelector('[data-workspace-part="divider"]');
  var toggle = shell.querySelector('[data-workspace-act="statement"]');
  var edge = shell.querySelector('[data-workspace-act="reopen"]');
  var left = number(recall(KEPT_LEFT));
  var closed = recall(KEPT_STATEMENT) === CLOSED;

  function statement() { return document.querySelector('section[data-kind][data-workspace-open]'); }

  function leftBounds() {
    var width = window.innerWidth || 1;
    var high = Math.min(80, 100 - LEAST_RIGHT / width * 100);
    return { low: Math.min(high, Math.max(15, LEAST_LEFT / width * 100)), high: high };
  }

  function beside() {
    return !!statement() && !root.hasAttribute(QUIZ) && !window.matchMedia(NARROW).matches;
  }

  function paintLeft() {
    if (!divider || !toggle || !edge) { return; }
    var bounds = leftBounds();
    var now = tenth(within(left === null ? DEFAULT_LEFT : left, bounds.low, bounds.high));
    root.style.setProperty(LEFT, now + '%');
    range(divider, bounds.low, bounds.high, now);
    if (closed) { root.setAttribute(STATEMENT, CLOSED); } else { root.removeAttribute(STATEMENT); }
    var able = beside();
    toggle.hidden = !able;
    divider.hidden = !able || closed;
    edge.hidden = !able || !closed;
    [toggle, edge].forEach(function (one) { one.setAttribute('aria-expanded', String(!closed)); });
    words(toggle, 'data-workspace-word', !closed);
    var one = statement();
    if (one && one.id) {
      [toggle, edge, divider].forEach(function (control) {
        control.setAttribute('aria-controls', one.id);
      });
    }
  }

  function setLeft(value) {
    var bounds = leftBounds();
    left = tenth(within(value, bounds.low, bounds.high));
    paintLeft();
  }

  function flipStatement(focusing) {
    closed = !closed;
    keep(KEPT_STATEMENT, closed ? CLOSED : OPEN);
    paintLeft();
    /* ⛔ Focus follows the control that went away, so a keyboard reader is
       never dropped to the top of the document. */
    if (focusing) { (closed ? edge : divider).focus(); }
  }

  if (divider && toggle && edge) {
    draggable(divider, 'x', function (event) {
      var gutter = divider.getBoundingClientRect().width;
      setLeft((event.clientX - gutter / 2) / (window.innerWidth || 1) * 100);
    }, function () { keep(KEPT_LEFT, left); });

    divider.addEventListener('keydown', function (event) {
      var bounds = leftBounds();
      var now = left === null ? DEFAULT_LEFT : left;
      var to = {
        ArrowLeft: now - STEP, ArrowRight: now + STEP, Home: bounds.low, End: bounds.high
      }[event.key];
      if (event.key === 'Enter') {
        event.preventDefault();
        flipStatement(true);
        return;
      }
      if (to === undefined) { return; }
      event.preventDefault();
      setLeft(to);
      keep(KEPT_LEFT, left);
    });

    toggle.addEventListener('click', function () { flipStatement(false); });
    edge.addEventListener('click', function () { flipStatement(true); });
  }

  /* --- the report under the editor -------------------------------------- */
  var report = number(recall(KEPT_REPORT));
  var reportShown = recall(KEPT_REPORT_SHOWN) !== CLOSED;

  function panel() { return document.querySelector('section[data-practice][data-workspace-open]'); }

  function parts(one) {
    function part(name) { return one.querySelector('[data-practice-part="' + name + '"]'); }
    return {
      divider: part('report-divider'), report: part('report'), bar: part('report-bar'),
      body: part('report-body'), glance: part('glance'), editor: part('editor')
    };
  }

  /* The panel's inner height, and what the report may take of it: at least its
     bar and a line or two, at most what leaves the editor its floor. */
  function reportBounds(one, had) {
    var style = window.getComputedStyle(one);
    var box = one.getBoundingClientRect();
    var top = box.top + parseFloat(style.paddingTop);
    var inner = Math.max(1, one.clientHeight - parseFloat(style.paddingTop) -
      parseFloat(style.paddingBottom));
    var floor = had.editor ? parseFloat(window.getComputedStyle(had.editor).minHeight) || 160 : 0;
    var above = had.report.getBoundingClientRect().top - top -
      (had.editor ? had.editor.getBoundingClientRect().height : 0);
    var high = Math.max(1, (inner - above - floor) / inner * 100);
    var low = Math.min(high, LEAST_REPORT / inner * 100);
    return { low: low, high: high, inner: inner, bottom: top + inner };
  }

  function paintReport() {
    var one = panel();
    if (!one) { return; }
    var had = parts(one);
    if (!had.divider || !had.report || !had.bar || !had.body) { return; }
    had.bar.hidden = !served;
    had.divider.hidden = !served || !reportShown;
    had.body.hidden = served && !reportShown;
    one.setAttribute(SHOWN, served && !reportShown ? CLOSED : OPEN);
    had.bar.setAttribute('aria-expanded', String(!served || reportShown));
    words(had.bar, 'data-practice-report-word', !served || reportShown);
    if (!served) { return; }
    var bounds = reportBounds(one, had);
    if (report === null) {
      root.removeAttribute(SIZED);
      root.style.removeProperty(REPORT);
    } else {
      report = tenth(within(report, bounds.low, bounds.high));
      root.setAttribute(SIZED, '');
      root.style.setProperty(REPORT, report + '%');
    }
    var now = report === null
      ? had.report.getBoundingClientRect().height / bounds.inner * 100 : report;
    range(had.divider, bounds.low, bounds.high, now);
  }

  function setReport(value) {
    report = value;
    paintReport();
  }

  function flipReport(open, focusing) {
    reportShown = open;
    keep(KEPT_REPORT_SHOWN, open ? OPEN : CLOSED);
    paintReport();
    var one = panel();
    if (focusing && one && !open) { parts(one).bar.focus(); }
  }

  /* ⭐ The last verdict at a glance: the cases passed out of those declared
     where a Submit reported them, and otherwise the status line's own words.
     ⛔ The words are the markup's (`data-practice-glance`). */
  function glance(one) {
    var had = parts(one);
    if (!had.glance || !had.bar) { return; }
    var rows = [].slice.call(one.querySelectorAll('[data-practice-case]'));
    var said = rows.filter(function (row) { return row.hasAttribute('data-practice-verdict'); });
    var status = one.querySelector('[data-practice-part="status"]');
    if (rows.length && said.length === rows.length) {
      var passed = said.filter(function (row) {
        return row.getAttribute('data-practice-verdict') === 'passed';
      }).length;
      had.glance.textContent = (had.bar.getAttribute('data-practice-glance') || '')
        .replace('{passed}', passed).replace('{total}', rows.length);
    } else {
      had.glance.textContent = status ? status.textContent : '';
    }
  }

  /* Each panel's divider and bar are wired once, whichever practice is open. */
  [].slice.call(document.querySelectorAll('section[data-practice]')).forEach(function (one) {
    var had = parts(one);
    if (!had.divider || !had.bar) { return; }
    draggable(had.divider, 'y', function (event) {
      var bounds = reportBounds(one, had);
      var gutter = had.divider.getBoundingClientRect().height;
      setReport((bounds.bottom - event.clientY - gutter / 2) / bounds.inner * 100);
    }, function () { if (report !== null) { keep(KEPT_REPORT, report); } });

    had.divider.addEventListener('keydown', function (event) {
      var bounds = reportBounds(one, had);
      var now = report === null
        ? had.report.getBoundingClientRect().height / bounds.inner * 100 : report;
      var to = {
        ArrowUp: now + STEP, ArrowDown: now - STEP, Home: bounds.low, End: bounds.high
      }[event.key];
      if (event.key === 'Enter') {
        event.preventDefault();
        flipReport(false, true);
        return;
      }
      if (to === undefined) { return; }
      event.preventDefault();
      setReport(to);
      keep(KEPT_REPORT, report);
    });

    had.bar.addEventListener('click', function () { flipReport(!reportShown, false); });

    /* ⭐ A new Run or Submit opens the report again, so its answer is seen. */
    one.addEventListener('click', function (event) {
      var act = event.target.closest && event.target.closest('button[data-practice-act]');
      if (!act || act.getAttribute('data-practice-act') === 'stop') { return; }
      if (!reportShown) { flipReport(true, false); }
      window.setTimeout(function () { glance(one); }, 0);
    });
  });

  document.addEventListener(SETTLED, function (event) {
    if (event.target && event.target.matches && event.target.matches('section[data-practice]')) {
      glance(event.target);
    }
  });

  function paint() {
    paintLeft();
    paintReport();
  }

  /* ⭐ Painted whenever a practice opens — the section's own mark changing —
     and once now, for a practice the address opened before this part ran. */
  document.addEventListener(OPENED, paint);
  if (window.MutationObserver) {
    new MutationObserver(paint).observe(root, {
      attributes: true, subtree: true, attributeFilter: ['data-workspace-open', QUIZ]
    });
  }
  window.addEventListener('resize', paint);
  paint();
}());
