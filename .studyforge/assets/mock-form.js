/* The mock exam's exam form: a timer, a navigator, flags, scenarios, multiple response, sittings.

   Graded here, in the page, from the key block the page carries: no request, no container, no
   model, the same over `file://` and served. Written beside `page.js` for a corpus with a mock
   exam that opts in or a plain quiz (`render.page.mockform`); `mock-form-core.js` holds the shared
   parts and the draw, `mock-form-panels.js` the navigator, start panel and results.

   The rule. A question with one key is right when the chosen option is the key; one that asks the
   reader to choose n is right only when exactly its n keyed options are chosen, and cannot be
   submitted with some other number chosen. A percent is rounded DOWN; a scaled score is linear.

   State. Kept in the reader's own browser under `studyforge.mockform.v1`, per exam: the sitting,
   the seed, the start time, the drawn questions in order, the answers, the flags and whether it
   was submitted; the ids a sitting drew are kept beside, so the next prefers questions not yet
   seen. Where storage is refused the exam still works and forgets on reload. */

(function () {
  'use strict';

  var F = window.studyforge && window.studyforge.mockForm;
  var wirePanels = F && F.panels;
  if (!F || !wirePanels) { return; }
  var EXAM = 'section[data-mock-form]';
  var QUESTION = 'data-practice-question';
  var VERDICT = 'data-practice-verdict';
  var SETTLED = 'studyforge:practice-settled';

  var part = F.part, fill = F.fill, json = F.json, make = F.make, generator = F.generator;
  var hash = F.hash, shuffled = F.shuffled, newSeed = F.newSeed, backing = F.backing;
  var recall = F.recall, seenOf = F.seenOf, keep = F.keep, percent = F.percent, clock = F.clock;

  function wire(exam) {
    var key = json(exam, 'key');
    var plan = json(exam, 'plan');
    var words = json(exam, 'words');
    var submit = part(exam, 'submit');
    var controls = part(exam, 'controls');
    if (!key || !plan || !words || !submit || !controls) { return; }
    var name = exam.getAttribute('data-practice-quiz');
    var passMark = Number(exam.getAttribute('data-practice-mock'));
    var examLayout = exam.getAttribute('data-mock-form') === 'exam';
    var quizFlavor = exam.getAttribute('data-form-kind') === 'quiz';
    var list = exam.querySelector('[data-practice-part="questions"]');
    var items = [].slice.call(list.querySelectorAll('[' + QUESTION + ']'));
    var byId = {};
    items.forEach(function (item) { byId[item.getAttribute(QUESTION)] = item; });
    var sittings = plan.sittings || [];
    var timed = typeof plan.minutes === 'number' || sittings.some(function (s) { return typeof s.minutes === 'number'; });
    var needsStart = timed || sittings.length > 0;
    var store = backing();
    var state = recall(store, name);
    var timer = null, announced = {}, confirming = false;
    var startPanel = part(exam, 'start');
    var bar = part(exam, 'bar');
    var navigator = part(exam, 'navigator');
    var pager = part(exam, 'pager');
    var again = part(exam, 'again');
    var missing = part(exam, 'missing');
    var result = part(exam, 'result');
    var announce = part(exam, 'announce');
    var offline = exam.querySelector('[data-practice-part="offline"]');
    if (offline) { offline.hidden = true; }
    exam.setAttribute('data-mock-ready', 'true');

    function meta(item) {
      return {
        id: item.getAttribute(QUESTION),
        domain: item.getAttribute('data-mock-domain'),
        scenario: item.getAttribute('data-mock-scenario'),
        difficulty: item.getAttribute('data-mock-difficulty'),
        select: Number(item.getAttribute('data-mock-select')) || 0
      };
    }
    var metas = items.map(meta);
    var metaById = {};
    metas.forEach(function (one) { metaById[one.id] = one; });

    var view = { filterDomain: '', filterFlagged: false, review: 'all' };
    var c = {
      exam: exam, plan: plan, words: words, key: key, passMark: passMark, name: name, list: list,
      items: items, byId: byId, metas: metas, metaById: metaById, sittings: sittings, view: view,
      examLayout: examLayout, quiz: quizFlavor, state: function () { return state; }, clear: function () { state = null; },
      stopTimer: stopTimer, lock: lock, refresh: refresh, go: go, isRight: isRight, begin: begin
    };
    var P = wirePanels(c);

    function say(text) { if (announce) { announce.textContent = text; } }

    function save() { keep(store, name, state); }

    function begin(sittingId) {
      var sitting = null;
      sittings.forEach(function (one) { if (one.id === sittingId) { sitting = one; } });
      var seed = newSeed();
      var random = generator(seed);
      var seen = seenOf(store, name);
      var order = sittings.length ? F.draw(plan, metas, metaById, sitting, random, seen) : metas.map(function (one) { return one.id; });
      var allSeen = metas.every(function (one) { return seen.indexOf(one.id) >= 0; });
      var nowSeen = allSeen ? order.slice()
        : order.concat(seen.filter(function (id) { return order.indexOf(id) < 0; }));
      state = {
        sitting: sitting ? sitting.id : null, seed: seed, started: Date.now(),
        minutes: F.minutesFor(plan, sittings, sitting, order.length, metas.length), order: order, answers: {}, flags: {},
        current: 0, submitted: false
      };
      keep(store, name, state, sittings.length ? nowSeen : null);
      open();
    }

    /* ---- the page of a sitting ---- */

    function arrange() {
      var inOrder = {};
      state.order.forEach(function (id) { inOrder[id] = true; });
      state.order.forEach(function (id) { list.appendChild(byId[id]); });
      items.forEach(function (item) {
        if (!inOrder[item.getAttribute(QUESTION)]) { list.appendChild(item); item.setAttribute('data-form-out', 'true'); }
        else { item.removeAttribute('data-form-out'); }
      });
      if (sittings.length) {
        state.order.forEach(function (id) {
          var item = byId[id];
          if (item.getAttribute('data-mock-shuffle') === 'false') { return; }
          var options = item.querySelector('[data-practice-part="options"]');
          var rows = [].slice.call(options.children);
          shuffled(rows, generator(state.seed ^ hash(id))).forEach(function (row) { options.appendChild(row); });
        });
      }
      state.order.forEach(function (id, index) {
        byId[id].querySelector('legend').setAttribute('data-n', String(index + 1));
      });
    }

    function pick(item) {
      var boxes = [].slice.call(item.querySelectorAll('input:checked'));
      if (!boxes.length) { return null; }
      if (!meta(item).select) { return boxes[0].value; }
      return boxes.length === meta(item).select ? boxes.map(function (box) { return box.value; }) : null;
    }
    function answersNow() {
      var made = {};
      state.order.forEach(function (id) {
        var one = pick(byId[id]);
        if (one !== null) { made[id] = one; }
      });
      return made;
    }
    function restore() {
      state.order.forEach(function (id) {
        var wanted = state.answers[id];
        var chosen = Array.isArray(wanted) ? wanted : (wanted === undefined ? [] : [wanted]);
        [].slice.call(byId[id].querySelectorAll('input')).forEach(function (input) {
          input.checked = chosen.indexOf(input.value) >= 0;
        });
        limit(byId[id]);
      });
    }
    function limit(item) {
      var select = meta(item).select;
      if (!select) { return; }
      var boxes = [].slice.call(item.querySelectorAll('input'));
      var count = boxes.filter(function (box) { return box.checked; }).length;
      boxes.forEach(function (box) { box.disabled = state.submitted || (count >= select && !box.checked); });
    }
    function isRight(id, answer) {
      var entry = key[id];
      if (!entry || answer === undefined || answer === null) { return false; }
      var chosen = Array.isArray(answer) ? answer : [answer];
      if (chosen.length !== entry.keys.length) { return false; }
      return entry.keys.every(function (k) { return chosen.indexOf(k) >= 0; });
    }
    function unansweredIds() {
      return state.order.filter(function (id) { return state.answers[id] === undefined; });
    }

    /* ---- what is on view ---- */

    function visibleIds() {
      if (state.submitted) {
        return state.order.filter(function (id) {
          if (view.review === 'missed') { return !isRight(id, state.answers[id]); }
          if (view.review === 'flagged') { return !!state.flags[id]; }
          return true;
        });
      }
      if (examLayout) { return [state.order[state.current]]; }
      return state.order;
    }

    function refresh() {
      var shown = visibleIds();
      var previousScenario = null;
      state.order.forEach(function (id) {
        var item = byId[id];
        var on = shown.indexOf(id) >= 0;
        item.hidden = !on;
        var card = item.querySelector('[data-form-part="scenario"]');
        if (card) {
          var repeat = on && !examLayout && !state.submitted ? previousScenario === meta(item).scenario : false;
          if (state.submitted && on) { repeat = previousScenario === meta(item).scenario; }
          card.hidden = repeat;
        }
        if (on) { previousScenario = meta(item).scenario; }
        var flag = item.querySelector('[data-form-part="flag"]');
        var line = item.querySelector('[data-form-part="flag-line"]');
        if (flag) {
          flag.setAttribute('aria-pressed', state.flags[id] ? 'true' : 'false');
          flag.textContent = state.flags[id] ? words.flagged : words.flag;
          flag.disabled = state.submitted;
        }
        if (line) { line.hidden = state.submitted || quizFlavor; }
      });
      items.forEach(function (item) {
        if (item.getAttribute('data-form-out')) { item.hidden = true; }
      });
      var answered = Object.keys(state.answers).length;
      var meter = part(exam, 'meter');
      if (meter) { meter.max = state.order.length; meter.value = answered; }
      part(exam, 'count').textContent = P.countText(answered);
      P.drawNavigator();
      if (examLayout && !state.submitted) {
        pager.hidden = false;
        var walk = P.walkable();
        part(exam, 'previous').disabled = !walk.some(function (i) { return i < state.current; });
        part(exam, 'next').disabled = !walk.some(function (i) { return i > state.current; });
      } else { pager.hidden = true; }
    }

    function go(index) {
      if (index < 0 || index >= state.order.length) { return; }
      state.current = index;
      save();
      refresh();
      var legend = byId[state.order[index]].querySelector('legend');
      say(fill(words.position, { n: index + 1, total: state.order.length }));
      if (legend) { legend.focus(); }
    }

    /* ---- the timer ---- */

    function stopTimer() { if (timer) { window.clearInterval(timer); timer = null; } }
    function tick() {
      var left = state.started + state.minutes * 60000 - Date.now();
      part(exam, 'timer').textContent = clock(left);
      [10, 5, 1].forEach(function (minute) {
        if (left <= minute * 60000 && !announced[minute] && left > 0) {
          announced[minute] = true;
          say(minute === 1 ? words.oneMinuteLeft : fill(words.minutesLeft, { n: minute }));
        }
      });
      if (left <= 0) {
        stopTimer();
        finish(true);
      }
    }
    function startTimer() {
      stopTimer();
      var line = part(exam, 'timer-line');
      if (typeof state.minutes !== 'number' || state.submitted) { line.hidden = true; return; }
      line.hidden = false;
      tick();
      if (!state.submitted) { timer = window.setInterval(tick, 1000); }
    }

    /* ---- the start, the sitting, the result ---- */

    function lock(on) {
      items.forEach(function (item) {
        [].slice.call(item.querySelectorAll('input')).forEach(function (input) { input.disabled = on; });
        if (!on) { limit(item); }
      });
    }

    function settle(reached) {
      var progress = window.studyforge && window.studyforge.progress;
      if (reached && progress) { progress.passQuiz(name); }
      exam.dispatchEvent(new CustomEvent(SETTLED, { bubbles: true }));
    }

    function finish(automatic) {
      if (state.submitted) { return; }
      state.answers = answersNow();
      state.submitted = true;
      state.finished = Date.now();
      save();
      var reached = P.show();
      if (automatic) { say(words.timeUp); }
      if (result.scrollIntoView) { result.scrollIntoView(); }
      settle(reached);
    }

    function open() {
      startPanel.hidden = true;
      list.hidden = false;
      bar.hidden = false;
      controls.hidden = false;
      submit.hidden = state.submitted;
      again.hidden = !state.submitted;
      arrange();
      P.buildNavigator();
      restore();
      if (state.current >= state.order.length) { state.current = 0; }
      if (state.submitted) { P.show(); return; }
      lock(false);
      items.forEach(function (item) {
        item.removeAttribute(VERDICT);
        var box = item.querySelector('[data-form-part="review"]');
        if (box) { box.hidden = true; box.textContent = ''; }
        var line = item.querySelector('[data-form-part="flag-line"]');
        if (line) { line.hidden = quizFlavor; }
      });
      P.explainAll();
      result.hidden = true;
      exam.removeAttribute('data-mock-passed');
      refresh();
      startTimer();
    }

    /* ---- the controls ---- */

    exam.addEventListener('change', function (event) {
      if (!state || state.submitted) { return; }
      var item = event.target.closest && event.target.closest('[' + QUESTION + ']');
      if (!item) { return; }
      limit(item);
      state.answers = answersNow();
      confirming = false; missing.hidden = true;
      save(); if (quizFlavor) { P.answered(item.getAttribute(QUESTION)); }
      refresh();
    });
    exam.addEventListener('click', function (event) {
      var flag = event.target.closest && event.target.closest('[data-form-part="flag"]');
      if (!flag || !state || state.submitted) { return; }
      var id = flag.closest('[' + QUESTION + ']').getAttribute(QUESTION);
      if (state.flags[id]) { delete state.flags[id]; } else { state.flags[id] = true; }
      save();
      refresh();
    });
    part(exam, 'previous').addEventListener('click', function () {
      var before = P.walkable().filter(function (i) { return i < state.current; });
      if (before.length) { go(before[before.length - 1]); }
    });
    part(exam, 'next').addEventListener('click', function () {
      var after = P.walkable().filter(function (i) { return i > state.current; });
      if (after.length) { go(after[0]); }
    });
    submit.addEventListener('click', function () {
      if (!state || state.submitted) { return; }
      state.answers = answersNow();
      if (P.refuseShort(state)) { confirming = false; return; }
      var left = unansweredIds();
      if (left.length && !confirming) {
        confirming = true;
        var numbers = left.map(function (id) { return state.order.indexOf(id) + 1; });
        var text = numbers.length > 1
          ? numbers.slice(0, -1).join(', ') + ' and ' + numbers[numbers.length - 1]
          : String(numbers[0]);
        missing.textContent = fill(words.confirm, { list: text });
        missing.hidden = false;
        return;
      }
      finish(false);
    });
    again.addEventListener('click', function () {
      keep(store, name, null);
      confirming = false;
      view.filterDomain = ''; view.filterFlagged = false; view.review = 'all';
      items.forEach(function (item) {
        item.removeAttribute(VERDICT);
        [].slice.call(item.querySelectorAll('input')).forEach(function (input) { input.checked = false; input.disabled = false; });
      });
      exam.dispatchEvent(new CustomEvent(SETTLED, { bubbles: true }));
      if (needsStart) { P.showStart(); }
      else { begin(null); }
      exam.scrollIntoView();
    });

    /* ---- opening ---- */

    function valid(kept) {
      return kept && Array.isArray(kept.order) && kept.order.length > 0 && typeof kept.seed === 'number'
        && kept.order.every(function (id) { return !!byId[id]; })
        && kept.answers && typeof kept.answers === 'object' && kept.flags && typeof kept.flags === 'object';
    }
    if (valid(state)) {
      if (typeof state.current !== 'number') { state.current = 0; }
      open();
      if (!state.submitted && typeof state.minutes === 'number' && state.started + state.minutes * 60000 <= Date.now()) {
        stopTimer();
        finish(true);
      }
    } else if (needsStart) {
      P.showStart();
    } else {
      begin(null);
    }
  }

  [].slice.call(document.querySelectorAll(EXAM)).forEach(wire);
}());
