/* The reader's own record of what they have read — and the ONE file that
   touches the store.

   ⛔ **No server, no origin, no network** (R8). A reader who never starts
   `studyforge serve` still keeps their place, because a read mark is the
   reader's own assertion and needs nobody's agreement to be true. ⚠️ The
   served half is a different fact: a PASS is established by a grader run and
   is written where it was established (spec §8.5). ⛔ **A read mark is
   never a pass**, and nothing here can produce a run's pass — this file knows
   no grader and no run, and a quiz's pass is only kept here, never judged.

   ⛔ **An explicit act, never inferred.** Nothing here observes scrolling, the
   narration reaching the end, or the page having been opened. The store is
   written when the reader presses the control and at no other time. A record
   the reader cannot trust is worse than none.

   ⭐ **A third record holds the quizzes the reader has answered wholly right**:
   a quiz is graded in its own page, and nothing about it is a server's, so
   its pass is the reader's own, like a mark. ⛔ It is never a run's pass.

   ⛔ **Two records, never one.** The marks and the reader's display
   preferences have different shapes and different lifetimes, so they are two
   storage keys, read and written independently, each carrying its own version
   — and a preference that fails to parse cannot take every mark with it.
   ⚠️ The floor declares no display preference yet; the record exists because
   the INDEPENDENCE is the ruling, and the first consumer is expected to be
   the narration transport (a speed, a volume), which has no business sharing a
   key with the reading record.

   ⛔ **No clock.** A timestamp is a second fact nobody asked for, and it turns
   the personal archive's merge from a set union into an ordering problem. Nothing here
   reads `Date`, and the marks are kept sorted so the stored text is stable
   under re-marking rather than ordered by when somebody pressed a button.

   ⛔ **A stored value this cannot display is discarded, not applied.** A
   record of the wrong shape, an unknown version, an entry that is not a key:
   dropped. ⚠️ A half-applied record is worse than an empty one — the reader
   would be shown marks nobody can account for, under names nothing matches.

   ⚠️ **This part comes BEFORE anything that uses it in `bundle.SCRIPT_PARTS`,
   and the order is asserted against the real composition.** The extraction
   source placed its store AFTER the page script that read it at startup: the
   guard skipped, the setting silently never came back, the suite stayed
   green, and it was found only by loading a page in a browser.

   ⛔ **The key a mark is filed under is minted by `Address.unit_key` in
   Python and carried to this page as data.** Nothing here composes one: a
   second spelling that differed by one character would simply never match
   anything, with nothing failing anywhere. */

(function () {
  'use strict';

  /* The two records. ⛔ The version is in the NAME, not only in the body: a
     record this build cannot read is one it must not overwrite in place
     either, so the next shape is the next key and the old one is left where
     the reader's browser put it. */
  var MARKS_KEY = 'studyforge.read.v1';
  var DISPLAY_KEY = 'studyforge.display.v1';
  /* ⭐ The third record: the quizzes this reader has answered wholly right. */
  var QUIZZES_KEY = 'studyforge.quizzes.v1';

  /* ⛔ THE BOOT CACHE, AND IT IS A DIFFERENT STORAGE AREA ON PURPOSE.
     ⚠️ `page.html` carries a synchronous boot in the `<head>` so a
     reader who chose a theme is not shown the other one for a frame. A boot is
     the EARLIEST a document can touch a storage area, so it must not read
     `localStorage`: a document that binds it before the previous page's write has
     been committed keeps a snapshot WITHOUT that write, for its whole life.
     ⛔ Under load a mark written on one page is then missing on the next in a
     large share of runs. ⛔ And it is not cosmetic — the reader then marks the page they
     are on, `writeMarks` composes the record from the stale set, and the
     earlier mark is gone.

     ⭐ So what the boot reads is a CACHE in `sessionStorage`, whose area is
     separate: touching it binds nothing the marks live in. ⛔ It is never an
     authority — the display record above is — and nothing here reads it back.
     ⚠️ The key is composed rather than written whole, so a caller names a
     preference and never a key. */
  var BOOT_PREFIX = 'studyforge.boot.';
  var BOOT_SUFFIX = '.v1';

  /* The shape inside a record. ⚠️ Versioned in the body as well, because a
     browser can hold a key this build wrote and a key a later build wrote,
     and a reader whose two machines disagree is the normal case. */
  var RECORD_VERSION = 1;
  var MARKS_FIELD = 'read';
  var PASSED_FIELD = 'passed';
  var DISPLAY_FIELD = 'display';

  /* How long a thing this will keep. ⚠️ A bound rather than a grammar: the
     unit key's grammar is `Address`'s and re-spelling it here would be a
     second definition of what a key is, wrong the day one of them changes.
     What this needs to know is only whether it can display the value. */
  var LONGEST = 200;

  /* Whether the browser will let us keep anything at all. ⚠️ Probed rather
     than assumed: a `file://` page in a private window, or one whose site
     data is blocked, THROWS on the property access itself — not on the
     write — so there is no answer to be had without a try. */
  function backing() {
    try {
      var store = window.localStorage;
      var probe = MARKS_KEY + '.probe';
      store.setItem(probe, '1');
      store.removeItem(probe);
      return store;
    } catch (error) {
      return null;
    }
  }

  var backed = backing();

  /* The same probe against the session area. ⚠️ Probed separately: a browser
     can refuse one and allow the other, and a refused cache costs a frame of
     flash while a refused store costs the reader their marks. */
  function sessioned() {
    try {
      var store = window.sessionStorage;
      var probe = BOOT_PREFIX + 'probe';
      store.setItem(probe, '1');
      store.removeItem(probe);
      return store;
    } catch (error) {
      return null;
    }
  }

  var cached = sessioned();

  /* ⛔ What the head boot may act on, kept for one preference. A value of
     `null` REMOVES it, because an absent cache and a cached word must not be
     two answers to one question: the boot acts on what it finds or on nothing.
     ⭐ Returns whether it took, the way `keep` does. */
  function cache(name, value) {
    if (!cached) { return false; }
    try {
      if (value === null) {
        cached.removeItem(BOOT_PREFIX + name + BOOT_SUFFIX);
      } else {
        cached.setItem(BOOT_PREFIX + name + BOOT_SUFFIX, value);
      }
      return true;
    } catch (error) {
      return false;
    }
  }

  /* One record, or null when there is nothing this can use. ⛔ Every way of
     being unusable lands here and returns the same thing, so a caller never
     sees a half-parsed record: no store, no entry, not JSON, not an object,
     a version this build does not know. */
  function record(name) {
    if (!backed) { return null; }
    var raw;
    try { raw = backed.getItem(name); } catch (error) { return null; }
    if (raw === null) { return null; }
    var parsed;
    try { parsed = JSON.parse(raw); } catch (error) { return null; }
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) { return null; }
    if (parsed.version !== RECORD_VERSION) { return null; }
    return parsed;
  }

  function keep(name, body) {
    if (!backed) { return false; }
    try {
      backed.setItem(name, JSON.stringify(body));
      return true;
    } catch (error) {
      /* A quota refusal, or site data blocked between the probe and now. ⛔
         Reported as a failed write rather than swallowed, so the control
         reads the store back and shows what is actually there. */
      return false;
    }
  }

  /* Whether this is something the control could show a reader, and a bound
     rather than a grammar. ⚠️ Written as a loop over code points rather than
     as a character class, because a HYPHEN inside one is a range operator or a literal depending
     on where it sits — and every slug this framework mints is hyphenated,
     so a class that swallowed `-` would discard every key there is.
     ⛔ Refused: anything at or below a space (every ASCII control and every
     space), and DEL. A key this cannot display is discarded, not applied. */
  function usable(value) {
    if (typeof value !== 'string' || value.length === 0 || value.length > LONGEST) {
      return false;
    }
    for (var at = 0; at < value.length; at += 1) {
      var code = value.charCodeAt(at);
      if (code <= 0x20 || code === 0x7f) { return false; }
    }
    return true;
  }

  /* The marks, sorted, with anything unusable dropped. ⛔ Dropped and not
     repaired: there is no shape a bad entry could be corrected INTO that the
     reader ever asserted. */
  function marks() {
    var held = record(MARKS_KEY);
    var listed = held && Array.isArray(held[MARKS_FIELD]) ? held[MARKS_FIELD] : [];
    var kept = [];
    listed.forEach(function (entry) {
      if (usable(entry) && kept.indexOf(entry) === -1) { kept.push(entry); }
    });
    return kept.sort();
  }

  function writeMarks(kept) {
    var body = { version: RECORD_VERSION };
    body[MARKS_FIELD] = kept.slice().sort();
    return keep(MARKS_KEY, body);
  }

  function marked(key) {
    return usable(key) && marks().indexOf(key) !== -1;
  }

  function mark(key) {
    if (!usable(key)) { return false; }
    var kept = marks();
    if (kept.indexOf(key) === -1) { kept.push(key); }
    return writeMarks(kept);
  }

  function unmark(key) {
    if (!usable(key)) { return false; }
    return writeMarks(marks().filter(function (held) { return held !== key; }));
  }

  /* ⭐ **A quiz passed, by its practice key** — the same machinery as the
     marks, over its own key. ⛔ A quiz is graded in its page and the server
     records nothing about it (register ruling), so its pass is the reader's
     own record, kept where the read marks are; it is never a RUN's pass. */
  function passes() {
    var held = record(QUIZZES_KEY);
    var listed = held && Array.isArray(held[PASSED_FIELD]) ? held[PASSED_FIELD] : [];
    return listed.filter(function (entry, at) {
      return usable(entry) && listed.indexOf(entry) === at;
    }).sort();
  }

  function passedQuiz(key) {
    return usable(key) && passes().indexOf(key) !== -1;
  }

  function passQuiz(key) {
    if (!usable(key)) { return false; }
    var kept = passes();
    if (kept.indexOf(key) === -1) { kept.push(key); }
    var body = { version: RECORD_VERSION };
    body[PASSED_FIELD] = kept.sort();
    return keep(QUIZZES_KEY, body);
  }

  /* The second record, and it is deliberately the same machinery over a
     different key — never the same record with a second field in it. */
  function preferences() {
    var held = record(DISPLAY_KEY);
    var kept = held && held[DISPLAY_FIELD] && typeof held[DISPLAY_FIELD] === 'object'
      ? held[DISPLAY_FIELD] : {};
    var answer = {};
    Object.keys(kept).sort().forEach(function (name) {
      if (usable(name) && usable(kept[name])) { answer[name] = kept[name]; }
    });
    return answer;
  }

  function preference(name) {
    var held = preferences();
    return Object.prototype.hasOwnProperty.call(held, name) ? held[name] : null;
  }

  function prefer(name, value) {
    if (!usable(name) || !usable(value)) { return false; }
    var held = preferences();
    held[name] = value;
    var body = { version: RECORD_VERSION };
    body[DISPLAY_FIELD] = held;
    return keep(DISPLAY_KEY, body);
  }

  /* ⛔ Published under one name, so the unit page, the container page and the
     root index share one implementation. Two would be a mark written under
     one name and read back under another, with no symptom but a badge that
     never lights. */
  window.studyforge = window.studyforge || {};
  window.studyforge.progress = {
    MARKS_KEY: MARKS_KEY,
    DISPLAY_KEY: DISPLAY_KEY,
    QUIZZES_KEY: QUIZZES_KEY,
    passedQuiz: passedQuiz,
    passQuiz: passQuiz,
    supported: function () { return backed !== null; },
    marks: marks,
    marked: marked,
    mark: mark,
    unmark: unmark,
    preferences: preferences,
    preference: preference,
    prefer: prefer,
    cache: cache
  };
}());
