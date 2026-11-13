/* The exam form's panels: the navigator, the start panel, the results and the review.
   Written beside `mock-form.js`, which binds them to one exam and calls them. */

(function () {
  'use strict';

  var F = window.studyforge && window.studyforge.mockForm;
  if (!F) { return; }
  var part = F.part;
  var fill = F.fill;
  var make = F.make;
  var percent = F.percent;
  var QUESTION = 'data-practice-question';
  var VERDICT = 'data-practice-verdict';

  var SETTLED = 'studyforge:practice-settled';

  function sentenceCase(text) { return text.charAt(0).toUpperCase() + text.slice(1); }

  function bind(c) {
    var exam = c.exam;
    var plan = c.plan;
    var words = c.words;
    var key = c.key;
    var passMark = c.passMark;
    var name = c.name;
    var list = c.list;
    var items = c.items;
    var byId = c.byId;
    var metas = c.metas;
    var metaById = c.metaById;
    var sittings = c.sittings;
    var view = c.view;
    var examLayout = c.examLayout;
    var startPanel = part(exam, 'start');
    var bar = part(exam, 'bar');
    var navigator = part(exam, 'navigator');
    var again = part(exam, 'again');
    var missing = part(exam, 'missing');
    var result = part(exam, 'result');
    var submit = part(exam, 'submit');
    var controls = part(exam, 'controls');
    var pager = part(exam, 'pager');
    function S() { return c.state(); }
    function minutesFor(sitting, drawn) {
      return F.minutesFor(plan, sittings, sitting, drawn, metas.length);
    }

    function matches(index) {
      var id = S().order[index];
      if (view.filterDomain && metaById[id].domain !== view.filterDomain) { return false; }
      if (view.filterFlagged && !S().flags[id]) { return false; }
      return true;
    }
    function walkable() {
      var found = [];
      S().order.forEach(function (id, index) { if (matches(index) || index === S().current) { found.push(index); } });
      return found;
    }

    function buildNavigator() {
      navigator.textContent = '';
      if (c.quiz) {
        var only = make('div', { 'data-form-part': 'numbers' });
        S().order.forEach(function (id, index) {
          var one = make('button', { type: 'button', 'data-form-part': 'number', 'data-index': String(index) }, String(index + 1));
          one.addEventListener('click', function () { c.go(index); });
          only.appendChild(one);
        });
        navigator.appendChild(only);
        return;
      }
      var filters = make('div', { 'data-form-part': 'filters' });
      var label = make('label', {}, words.domainFilter + ' ');
      var select = make('select', { 'data-form-part': 'filter-domain' });
      select.appendChild(make('option', { value: '' }, words.allDomains));
      (plan.domains || []).forEach(function (domain) {
        select.appendChild(make('option', { value: domain.id }, domain.title));
      });
      label.appendChild(select);
      var flagLabel = make('label', {});
      var box = make('input', { type: 'checkbox', 'data-form-part': 'filter-flagged' });
      flagLabel.appendChild(box);
      flagLabel.appendChild(document.createTextNode(' ' + words.flaggedOnly));
      filters.appendChild(label);
      filters.appendChild(flagLabel);
      var grid = make('div', { 'data-form-part': 'numbers' });
      S().order.forEach(function (id, index) {
        var button = make('button', { type: 'button', 'data-form-part': 'number', 'data-index': String(index) }, String(index + 1));
        button.addEventListener('click', function () { c.go(index); });
        grid.appendChild(button);
      });
      navigator.appendChild(filters);
      navigator.appendChild(grid);
      select.addEventListener('change', function () { view.filterDomain = select.value; c.refresh(); });
      box.addEventListener('change', function () { view.filterFlagged = box.checked; c.refresh(); });
    }

    function drawNavigator() {
      navigator.hidden = !(examLayout && !S().submitted);
      if (navigator.hidden) { return; }
      [].slice.call(navigator.querySelectorAll('[data-form-part="number"]')).forEach(function (button) {
        var index = Number(button.getAttribute('data-index'));
        var id = S().order[index];
        var states = [S().answers[id] === undefined ? words.open : words.answered];
        button.setAttribute('data-form-state', S().answers[id] === undefined ? 'open' : 'answered');
        if (S().flags[id]) { states.push(words.flaggedState); button.setAttribute('data-flagged', 'true'); }
        else { button.removeAttribute('data-flagged'); }
        button.setAttribute('aria-label', fill(words.navQuestion, { n: index + 1, state: states.join(', ') }));
        if (index === S().current) { button.setAttribute('aria-current', 'true'); } else { button.removeAttribute('aria-current'); }
        button.hidden = !(matches(index) || index === S().current);
      });
    }

    function showStart() {
      c.stopTimer();
      c.clear();
      bar.hidden = true; navigator.hidden = true; pager.hidden = true; controls.hidden = true;
      result.hidden = true; missing.hidden = true;
      list.hidden = true;
      exam.removeAttribute('data-mock-passed');
      startPanel.textContent = '';
      startPanel.hidden = false;
      var group = make('fieldset', {});
      group.appendChild(make('legend', {}, sittings.length ? words.choose : words.beginOnly));
      sittings.forEach(function (sitting, index) {
        var count = typeof sitting.questions === 'number'
          ? fill(words.questions, { n: sitting.questions })
          : typeof sitting.scenarios === 'number'
            ? fill(words.scenarios, { n: sitting.scenarios })
            : fill(words.allQuestions, { n: metas.length });
        var drawn = typeof sitting.questions === 'number' ? sitting.questions : metas.length;
        var inScenarios = metas.filter(function (m) { return m.scenario; }).length;
        var scenarioCount = F.unitsOf(metaById, metas.map(function (m) { return m.id; }))
          .filter(function (unit) { return unit.scenario; }).length;
        var span = minutesFor(sitting, typeof sitting.scenarios === 'number'
          ? Math.round(inScenarios * sitting.scenarios / Math.max(1, scenarioCount)) : drawn);
        var text = sitting.title + ': ' + count + (span === null ? '' : ', ' + fill(words.minutes, { n: span }));
        var row = make('p', {});
        var label = make('label', {});
        var radio = make('input', { type: 'radio', name: name + ':sitting', value: sitting.id });
        if (index === 0) { radio.checked = true; }
        label.appendChild(radio);
        label.appendChild(document.createTextNode(' ' + text));
        row.appendChild(label);
        group.appendChild(row);
      });
      if (!sittings.length) {
        group.appendChild(make('p', {}, typeof plan.minutes === 'number' ? fill(words.minutes, { n: plan.minutes }) : words.noTime));
      }
      var go_ = make('button', { type: 'button', 'data-form-part': 'begin' }, words.begin);
      go_.addEventListener('click', function () {
        var chosen = startPanel.querySelector('input[type="radio"]:checked');
        startPanel.hidden = true;
        c.begin(chosen ? chosen.value : null);
      });
      startPanel.appendChild(group);
      startPanel.appendChild(go_);
      go_.focus();
    }


    function row(table, title, value) {
      var tr = document.createElement('tr');
      var th = make('th', { scope: 'row' }, title);
      var td = make('td', {}, value);
      tr.appendChild(th); tr.appendChild(td);
      table.querySelector('tbody').appendChild(tr);
    }

    /* A plain quiz explains an answer as it is given: the verdict and the sentence for the option
       CHOSEN, never the key, so a reader told why a choice fails can try again. */
    function explain(id) {
      var item = byId[id];
      var box = item.querySelector('[data-form-part="review"]');
      var entry = key[id];
      var answer = S().answers[id];
      var chosen = Array.isArray(answer) ? answer : (answer === undefined ? [] : [answer]);
      box.textContent = '';
      [].slice.call(item.querySelectorAll('[data-practice-part="mark"]')).forEach(function (old) { old.remove(); });
      if (!chosen.length || !entry || !Object.prototype.hasOwnProperty.call(entry.says, chosen[0])) {
        box.hidden = true;
        item.removeAttribute(VERDICT);
        return;
      }
      var right = c.isRight(id, answer);
      var said = right ? words.right : words.wrong;
      /* The 2026-11 redesign: each chosen option carries the verdict's icon and word too. */
      [].slice.call(item.querySelectorAll('input:checked')).forEach(function (input) {
        input.closest('label').appendChild(make('span', { 'data-practice-part': 'mark' }, said.replace(/\.$/, '')));
      });
      var line = make('p', { 'data-form-part': 'verdict' }, '');
      line.appendChild(make('strong', {}, said));
      line.appendChild(document.createTextNode(' '));
      line.insertAdjacentHTML('beforeend', entry.says[chosen[0]]);
      box.appendChild(line);
      chosen.slice(1).forEach(function (more) {
        if (!Object.prototype.hasOwnProperty.call(entry.says, more)) { return; }
        var extra = make('p', { 'data-form-part': 'verdict-more' });
        extra.insertAdjacentHTML('beforeend', entry.says[more]);
        box.appendChild(extra);
      });
      box.hidden = false;
      item.setAttribute(VERDICT, right ? 'correct' : 'wrong');
    }

    /* A quiz is complete the moment every question is right, as the all-on-one-page quiz is, so
       the card over it reads passed without the reader having to finish. */
    function answered(id) {
      explain(id);
      var progress = window.studyforge && window.studyforge.progress;
      var every = S().order.every(function (one) { return c.isRight(one, S().answers[one]); });
      if (every && progress) { progress.passQuiz(c.name); }
      exam.dispatchEvent(new CustomEvent(SETTLED, { bubbles: true }));
    }

    /* The progress line: the place and what is answered, or just what is answered. */
    function countText(done) {
      var total = S().order.length;
      return c.quiz && !S().submitted
        ? fill(words.positionCount, { n: S().current + 1, total: total, answered: done })
        : fill(words.count, { answered: done, asked: total });
    }

    function explainAll() {
      if (!c.quiz) { return; }
      S().order.forEach(function (id) { explain(id); });
    }

    /* The end of a quiz: every question's verdict, each a way back to that question. */
    function summarise() {
      var list = part(exam, 'summary');
      if (!list) {
        list = make('ol', { 'data-form-part': 'summary', 'aria-label': words.summary });
        result.insertBefore(list, part(exam, 'review-filter'));
      }
      list.textContent = '';
      list.hidden = false;
      S().order.forEach(function (id, index) {
        var answer = S().answers[id];
        var verdict = answer === undefined ? words.unanswered : c.isRight(id, answer) ? words.right : words.wrong;
        var li = make('li', { 'data-form-verdict': answer === undefined ? 'open' : c.isRight(id, answer) ? 'right' : 'wrong' });
        var jump = make('button', { type: 'button', 'aria-label': fill(words.summaryJump, { n: index + 1 }) },
          fill(words.summaryItem, { n: index + 1, verdict: verdict }));
        jump.addEventListener('click', function () {
          var item = byId[id];
          item.hidden = false;
          if (item.scrollIntoView) { item.scrollIntoView(); }
          var legend = item.querySelector('legend');
          if (legend) { legend.focus({ preventScroll: true }); }
        });
        li.appendChild(jump);
        list.appendChild(li);
      });
    }

    function review(id, right) {
      var item = byId[id];
      var box = item.querySelector('[data-form-part="review"]');
      var entry = key[id];
      var answer = S().answers[id];
      var chosen = Array.isArray(answer) ? answer : (answer === undefined ? [] : [answer]);
      box.textContent = '';
      var verdict = make('p', { 'data-form-part': 'verdict' },
        chosen.length === 0 ? words.unanswered : right ? words.right : words.wrong);
      box.appendChild(verdict);
      var ul = make('ul', { 'data-form-part': 'explanations' });
      [].slice.call(item.querySelectorAll('[data-practice-option]')).forEach(function (option) {
        var optionId = option.getAttribute('data-practice-option');
        var isKey = entry.keys.indexOf(optionId) >= 0;
        var li = make('li', { 'data-form-key': isKey ? 'true' : 'false', 'data-form-chosen': chosen.indexOf(optionId) >= 0 ? 'true' : 'false' });
        var marks = [];
        if (isKey) { marks.push(words.key); }
        if (chosen.indexOf(optionId) >= 0) { marks.push(words.yours.toLowerCase()); }
        var label = option.querySelector('label');
        var text = [].slice.call(label.childNodes).filter(function (n) { return !(n.matches && n.matches('[data-practice-part="mark"]')); });
        li.appendChild(make('strong', {}, (marks.length ? sentenceCase(marks.join(', ')) + ': ' : '') + text.map(function (n) { return n.textContent; }).join('').trim() + ' '));
        var says = make('span', {});
        says.innerHTML = entry.says[optionId] || '';
        li.appendChild(says);
        ul.appendChild(li);
      });
      box.appendChild(ul);
      box.hidden = false;
      item.setAttribute(VERDICT, right ? 'correct' : 'wrong');
    }

    /* A multiple-response question with some but not its n options chosen blocks the submit. */
    function refuseShort(state) {
      var partial = state.order.filter(function (id) {
        var select = metaById[id].select;
        var count = byId[id].querySelectorAll('input:checked').length;
        return select && count > 0 && count !== select;
      });
      if (!partial.length) { return false; }
      var places = partial.map(function (id) { return state.order.indexOf(id) + 1; }).join(', ');
      missing.textContent = fill(words.wrongCount || 'Choose exactly the number of options each '
        + 'question asks for. Not complete: {list}.', { list: places });
      missing.hidden = false;
      return true;
    }

    function show() {
      c.stopTimer();
      var asked = S().order.length;
      var right = 0;
      var byDomain = {};
      var byDifficulty = {};
      S().order.forEach(function (id) {
        var ok = c.isRight(id, S().answers[id]);
        if (ok) { right += 1; }
        var one = metaById[id];
        byDomain[one.domain] = byDomain[one.domain] || { right: 0, asked: 0 };
        byDomain[one.domain].asked += 1;
        if (ok) { byDomain[one.domain].right += 1; }
        if (one.difficulty) {
          byDifficulty[one.difficulty] = byDifficulty[one.difficulty] || { right: 0, asked: 0 };
          byDifficulty[one.difficulty].asked += 1;
          if (ok) { byDifficulty[one.difficulty].right += 1; }
        }
        review(id, ok);
      });
      var reached = asked > 0 && percent(right, asked) >= passMark;
      part(exam, 'overall').textContent = fill(reached ? words.reached : words.short,
        { right: right, asked: asked, percent: percent(right, asked), pass: passMark });
      var scaledLine = part(exam, 'scaled');
      if (plan.scale) {
        var span = plan.scale.max - plan.scale.min;
        var score = plan.scale.min + Math.floor((span * right * 2 + asked) / (2 * Math.max(1, asked)));
        scaledLine.textContent = fill(words.scaled, { score: score, min: plan.scale.min, max: plan.scale.max, line: plan.scale.pass });
        scaledLine.hidden = false;
        exam.setAttribute('data-mock-scaled', String(score));
      } else { scaledLine.hidden = true; }
      var domains = part(exam, 'domains');
      domains.hidden = !!c.quiz;
      if (c.quiz) { summarise(); }
      domains.querySelector('tbody').textContent = '';
      (plan.domains || []).forEach(function (domain) {
        var s = byDomain[domain.id] || { right: 0, asked: 0 };
        row(domains, domain.title, fill(words.scoreWords, { right: s.right, asked: s.asked, percent: percent(s.right, s.asked) }));
      });
      var difficulties = part(exam, 'difficulties');
      difficulties.querySelector('tbody').textContent = '';
      difficulties.hidden = !(plan.difficulties && plan.difficulties.length);
      (plan.difficulties || []).forEach(function (difficulty) {
        var s = byDifficulty[difficulty.id] || { right: 0, asked: 0 };
        row(difficulties, difficulty.title, fill(words.scoreWords, { right: s.right, asked: s.asked, percent: percent(s.right, s.asked) }));
      });
      buildReviewFilter();
      bar.hidden = true;
      exam.setAttribute('data-mock-passed', reached ? 'true' : 'false');
      list.hidden = false;
      result.hidden = false;
      missing.hidden = true;
      submit.hidden = true;
      again.hidden = false;
      controls.hidden = false;
      c.lock(true);
      c.refresh();
      var limited = S().sitting && sittings.some(function (s) {
        return s.id === S().sitting && (typeof s.questions === 'number' || typeof s.scenarios === 'number') && asked < metas.length;
      });
      return reached && !limited;
    }

    function buildReviewFilter() {
      var box = part(exam, 'review-filter');
      box.textContent = '';
      [['all', words.reviewAll], ['missed', words.reviewMissed], ['flagged', words.reviewFlagged]].filter(function (pair) {
        return !(c.quiz && pair[0] === 'flagged');
      }).forEach(function (pair) {
        var button = make('button', { type: 'button', 'data-review': pair[0], 'aria-pressed': view.review === pair[0] ? 'true' : 'false' }, pair[1]);
        button.addEventListener('click', function () {
          view.review = pair[0];
          [].slice.call(box.querySelectorAll('button')).forEach(function (other) {
            other.setAttribute('aria-pressed', other === button ? 'true' : 'false');
          });
          c.refresh();
          var none = part(exam, 'review-none');
          if (none) { none.hidden = visibleIds().length > 0; }
        });
        box.appendChild(button);
      });
      var none = make('p', { 'data-form-part': 'review-none', hidden: '' }, words.reviewNone);
      box.appendChild(none);
    }


    return {
      matches: matches, walkable: walkable, buildNavigator: buildNavigator,
      drawNavigator: drawNavigator, showStart: showStart, show: show, explain: explain,
      explainAll: explainAll, answered: answered, countText: countText, refuseShort: refuseShort
    };
  }

  window.studyforge.mockForm.panels = bind;
}());
