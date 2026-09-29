/* The quiz: what a reader chose, whether it was right, and why — graded in the page.

   ⭐ **The register ruling (2026-09-25): a quiz's answers live in the page, in a
   script local to that page, and nothing about a quiz is a server function.**
   This reverses the ruling that kept the key on the local study server. ⭐ So
   each quiz section carries its OWN key, as a JSON data block
   (`<script type="application/json" data-practice-part="key">`, written by
   `render/page/quiz.py`), and this file — shared by every page, and holding no
   answer — reads it and grades in the browser. ⛔ **No request is made to
   answer a quiz**, so the reading is the same over `file://` and served (R8).

   ⭐ **The rule, in full**: a question is right when the chosen option is its
   key; the chosen option's sentence is shown, right or wrong; and the quiz is
   complete only when EVERY question is answered right — no pass mark, no
   partial credit. ⚠️ A wrong answer shows its own sentence and never names
   the key: a reader told why their choice fails can try again.

   ⭐ **A complete quiz is recorded as passed in the reader's own browser store**
   (`study-progress.js`, where "Mark as read" keeps its marks), so its card reads
   *passed* after a reload. ⛔ Never on the server, and never as a run's pass.
   ⚠️ The answers themselves are not kept: a reload clears the choices, and the
   pass stays.

   ⛔ **A quiz has no file, no command and no grader to submit to, so it renders
   no Run and no Submit — and not disabled ones.** With no script at all, the
   Check control stays hidden and the page says why.

   ⭐ **Every word this file says is read off the markup**, where Python put it —
   the same two-sided spelling every hook on this page has. */

(function () {
  'use strict';

  var QUIZ = 'section[data-practice-quiz]';
  var PART = 'data-practice-part';
  var QUESTION = 'data-practice-question';
  var VERDICT = 'data-practice-verdict';

  /* What a quiz says when it is graded, raised on its section, so the card over
     it reads the reader's record again (`practice-workspace.js`). */
  var SETTLED = 'studyforge:practice-settled';

  /* Where each of this file's own sentences is kept. ⚠️ `right` is read off two
     different elements and means two different things — the question's *Right.*
     and the section's counting line. */
  var RIGHT = 'data-practice-right';
  var WRONG = 'data-practice-wrong';
  var COMPLETE = 'data-practice-complete';
  var BLANK = 'data-practice-blank';

  function part(root, name) {
    return root.querySelector('[' + PART + '="' + name + '"]');
  }

  function words(element, name) {
    return (element && element.getAttribute(name)) || '';
  }

  /* This quiz's key, or null where the block is missing or is not one. ⛔ A
     quiz with no key cannot be graded, and says nothing rather than guess. */
  function keyOf(quiz) {
    var block = part(quiz, 'key');
    if (!block) { return null; }
    try {
      var key = JSON.parse(block.textContent);
      return key && typeof key === 'object' && !Array.isArray(key) ? key : null;
    } catch (error) {
      return null;
    }
  }

  /* What the reader chose, as `{question id: option id}`. ⚠️ Read off the DOM:
     the radios ARE the state. */
  function chosen(quiz) {
    var answers = {};
    [].slice.call(quiz.querySelectorAll('[' + QUESTION + ']')).forEach(function (question) {
      var picked = question.querySelector('input[type="radio"]:checked');
      if (picked) { answers[question.getAttribute(QUESTION)] = picked.value; }
    });
    return answers;
  }

  /* One question graded and drawn; answers whether it is right. ⭐ The sentence
     is the one for whatever the reader CHOSE, right or wrong. */
  function draw(question, entry, answer) {
    var says = part(question, 'says');
    var hasSaid = entry && entry.says && Object.prototype.hasOwnProperty.call(entry.says, answer);
    if (answer === undefined || !hasSaid) {
      question.removeAttribute(VERDICT);
      if (says) { says.textContent = ''; says.hidden = true; }
      return false;
    }
    var right = answer === entry.key;
    question.setAttribute(VERDICT, right ? 'correct' : 'wrong');
    if (says) {
      /* ⚠️ The sentence is markup the BUILD rendered (`markup.inline`: escaped,
         its inline code as code); the verdict's own word stays text. */
      says.textContent = words(says, right ? RIGHT : WRONG) + ' ';
      says.insertAdjacentHTML('beforeend', entry.says[answer]);
      says.hidden = false;
    }
    return right;
  }

  function grade(quiz, key) {
    var answers = chosen(quiz);
    var status = part(quiz, 'status');
    var questions = [].slice.call(quiz.querySelectorAll('[' + QUESTION + ']'));
    var right = 0;
    questions.forEach(function (question) {
      var id = question.getAttribute(QUESTION);
      if (draw(question, key[id], answers[id])) { right += 1; }
    });
    /* ⛔ Complete is EVERY question answered right, and nothing less. */
    var complete = questions.length > 0 && right === questions.length;
    if (status) {
      status.textContent = !Object.keys(answers).length ? words(status, BLANK)
        : complete ? words(status, COMPLETE)
          : words(status, RIGHT).replace('{right}', right).replace('{asked}', questions.length);
    }
    var store = window.studyforge && window.studyforge.progress;
    if (complete && store) { store.passQuiz(quiz.getAttribute('data-practice-quiz')); }
    quiz.dispatchEvent(new CustomEvent(SETTLED, { bubbles: true }));
  }

  function wire(quiz) {
    var key = keyOf(quiz);
    var check = part(quiz, 'check');
    var controls = part(quiz, 'controls');
    var offline = part(quiz, 'offline');
    if (!key || !check || !controls) { return; }
    if (offline) { offline.hidden = true; }
    controls.hidden = false;
    var graded = false;
    /* ⭐ Re-graded as soon as a reader changes an answer, once they have asked
       once, so the sentence under a question can never describe an option that
       is no longer chosen. */
    check.addEventListener('click', function () { graded = true; grade(quiz, key); });
    quiz.addEventListener('change', function () { if (graded) { grade(quiz, key); } });
  }

  [].slice.call(document.querySelectorAll(QUIZ)).forEach(wire);
}());
