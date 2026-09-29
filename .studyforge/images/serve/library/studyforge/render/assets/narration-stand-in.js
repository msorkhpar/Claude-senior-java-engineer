/* Where a narrated passage the page is not showing is shown instead.

   ⛔ **Split out of `narration.js` at its seam** (R11): that file is *the
   transport and the highlight*; this is *where a passage stands when it is
   hidden*. It defines one function and draws nothing.

   ⭐ **A passage the page is not showing is spoken where the reader can see
   it chosen.** An item inside a CLOSED entry — a code example, a disclosure —
   stands in as that entry's summary, and one inside a practice the page keeps
   under its *Practice (n)* list (`practice-workspace.js`) stands in as that
   practice's card. `narration.js` marks the stand-in `data-speaking` beside
   the passage, and scrolls to it rather than to a passage with no box.

   ⛔ **The entry is NOT opened.** Opening a code example loads a full editor,
   which only a reader's own hand may start (`code-links.js`), and a second,
   quieter kind of opening would be a state that file has to tell apart from
   the reader's. The summary names what is being read, and the reader opens it
   if they want to see it. ⚠️ A passage the page shows answers `null`: it is
   its own place. */

(function () {
  'use strict';

  var CLOSED = 'details:not([open])';
  var PRACTICE = 'section[data-kind="practice"][hidden]';
  var CARD = 'data-practice-card';

  /* ⚠️ Bounded: a stand-in may itself be hidden (an entry inside a closed
     entry), and each step climbs one level, so a page can never loop here. */
  var LEVELS = 8;

  /* ⚠️ `checkVisibility` where the browser has it: the inside of a closed
     `details` keeps its boxes (it is hidden by `content-visibility`), so a
     count of boxes alone calls it shown. */
  function shown(element) {
    if (!element) { return false; }
    return element.checkVisibility ? element.checkVisibility() : element.getClientRects().length > 0;
  }

  function standIn(passage) {
    var target = passage;
    for (var level = 0; target && !shown(target) && level < LEVELS; level += 1) {
      var closed = target.parentElement && target.parentElement.closest(CLOSED);
      var summary = closed && closed.querySelector('summary');
      if (summary && !summary.contains(target)) { target = summary; continue; }
      var practice = target.closest(PRACTICE);
      target = practice ? document.querySelector('li[' + CARD + '="' + practice.id + '"]') : null;
    }
    return target && target !== passage ? target : null;
  }

  window.studyforge = window.studyforge || {};
  window.studyforge.standIn = standIn;
}());
