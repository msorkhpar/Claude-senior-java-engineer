/* Where the reader is: the Up next slip, the progress line and strip, the tick
   of the unit up next, the filter and the two expand controls, and the rail's
   fold on a narrow screen.

   ⛔ **Every word a reader sees is markup.** The index and container renderers
   write each sentence with its numbers at zero and its alternatives hidden;
   this file only writes numbers into `<b>`/`<strong>`, moves an href, and
   hides or shows what is already there. Nothing here composes a sentence.

   ⛔ **Joined by the unit key and nothing else**, as `read-mark.js` is: a row
   on the index or a container page carries its key as its `id`, a rail row
   carries it as `data-unit`, and the store holds keys. Nothing here
   derives a key from an href or a position.

   ⭐ **Progressive enhancement.** With no script the slip names the first unit
   (the renderer wrote that), the progress region and the filter stay hidden,
   and the rail stays open. With no working store the filter still works and
   the progress stays hidden, because the marks are the store's.

   ⚠️ Composed before `read-mark.js`, which stays LAST, and it reads the store
   through the same published name, guarded here because this file has work to
   do without it. */

(function () {
  'use strict';

  var WIDE = '(min-width: 72rem)';
  var STEP = 'aria-current';
  var LISTS = 'nav[aria-label="Contents"] li[id], nav[aria-label="Units"] li[id]';
  var RAIL_UNITS = 'nav[aria-label="Containers"] li[data-unit]';
  var MARKED = 'data-marked';
  /* The hidden words a read row speaks: markup, shown or hidden here
     from the store's answer, so a screen reader hears what the tick shows. */
  var SAID = 'span[data-kind="read-state"]';

  function say(row, read) {
    var words = row.querySelector(SAID);
    if (words) { words.hidden = !read; }
  }

  var store = window.studyforge && window.studyforge.progress;
  var usable = !!(store && store.supported());

  /* --- the rail folds on a narrow screen ---------------------------------- */

  var fold = document.querySelector('nav[aria-label="Containers"] > details');
  if (fold && window.matchMedia) {
    var wide = window.matchMedia(WIDE);
    var place = function () { fold.open = wide.matches; };
    place();
    if (wide.addEventListener) { wide.addEventListener('change', place); }
  }

  /* --- moving the reader on (the brief's §3) ------------------------------ */

  /* ⭐ Marking a unit read brings its Up next slip into view. Focus stays on
     the button: moving it would take the reader somewhere they did not ask to
     go. ⚠️ Here and not in `read-mark.js`, which must never reach for scrolling
     (a mark is an explicit act and nothing about scrolling may infer one). */
  /* --- the rail shows what the reader marked ------------------------------ */

  /* ⭐ On every page that carries a rail, a row whose key the store holds is
     marked, and one it does not hold is cleared — so an unmark shows too.
     ⛔ `data-marked` is set here at read time and emitted by no renderer (R10);
     with no working store the rail shows no marks rather than wrong ones. */
  var railed = [].slice.call(document.querySelectorAll(RAIL_UNITS));
  function paintRail() {
    if (!usable) { return; }
    var held = store.marks();
    railed.forEach(function (row) {
      var holds = held.indexOf(row.getAttribute('data-unit')) !== -1;
      if (holds) { row.setAttribute(MARKED, 'true'); } else { row.removeAttribute(MARKED); }
      say(row, holds);
    });
  }
  paintRail();

  var control = document.querySelector('section[data-section="read-mark"] button');
  var onward = document.querySelector('nav[aria-label="Between units"] a[rel="next"]');
  if (control && railed.length) {
    /* ⚠️ After the turn, for the reason given below: the store's answer is
       written by `read-mark.js`'s listener, which runs after this one. */
    control.addEventListener('click', function () { window.setTimeout(paintRail, 0); });
  }
  if (control && onward && onward.scrollIntoView) {
    /* ⚠️ Read after the turn: `read-mark.js` is composed after this file, so
       its own listener — the one that asks the store and sets `aria-pressed` —
       runs after this one. The state is the store's answer, never the click. */
    control.addEventListener('click', function () {
      window.setTimeout(function () {
        if (control.getAttribute('aria-pressed') !== 'true') { return; }
        var still = window.matchMedia &&
          window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        onward.scrollIntoView({ block: 'nearest', behavior: still ? 'auto' : 'smooth' });
      }, 0);
    });
  }

  var rows = [].slice.call(document.querySelectorAll(LISTS));
  if (!rows.length) { return; }

  var marks = usable ? store.marks() : [];
  rows.forEach(function (row) { say(row, read(row)); });

  function readable(row) { return row.getAttribute('data-readable') === 'true'; }
  function read(row) { return marks.indexOf(row.id) !== -1; }
  function within(root) {
    return rows.filter(function (row) { return root.contains(row) && readable(row); });
  }
  function count(list) { return list.filter(read).length; }
  function fill(holder, value) {
    var slot = holder && holder.querySelector('b, strong');
    if (slot) { slot.textContent = String(value); }
  }

  var units = rows.filter(readable);
  var next = usable ? units.filter(function (row) { return !read(row); })[0] || null : null;
  if (next) { next.setAttribute(STEP, 'step'); }

  /* --- the slip ------------------------------------------------------------ */

  var slip = document.querySelector('nav[aria-label="Up next"]');
  if (slip && usable) {
    var lead = slip.querySelector('a');
    var finished = slip.querySelector('p');
    if (next && lead) {
      var source = next.querySelector('a');
      var title = source ? source.cloneNode(true) : null;
      if (title) {
        [].slice.call(title.querySelectorAll('span')).forEach(function (span) {
          span.parentNode.removeChild(span);
        });
        lead.setAttribute('href', source.getAttribute('href'));
        lead.lastChild.textContent = ' ' + title.textContent.trim();
      }
    } else if (!next && finished && lead && units.length) {
      lead.hidden = true;
      finished.hidden = false;
    }
  }

  /* --- the progress line, the strip, and each group's own count ----------- */

  var region = document.querySelector('section[aria-label="Progress"]');
  if (region && usable) {
    var line = region.querySelector('p');
    var done = count(units);
    fill(line, done);
    fill(line && line.querySelector('span'), units.length - done);
    [].slice.call(region.querySelectorAll('li > a[href^="#"]')).forEach(function (link) {
      var group = document.getElementById(link.getAttribute('href').slice(1));
      if (!group) { return; }
      var members = within(group);
      var share = members.length ? Math.round((100 * count(members)) / members.length) : 0;
      link.style.setProperty('--read', share + '%');
      if (next && group.contains(next)) { link.parentNode.setAttribute(STEP, 'step'); }
      link.addEventListener('click', function () { reveal(group); });
    });
    region.hidden = false;
  }

  [].slice.call(document.querySelectorAll('nav[aria-label="Contents"] summary > small')).forEach(
    function (tally) {
      if (!usable) { return; }
      var members = within(tally.parentNode.parentNode);
      fill(tally, count(members));
      tally.hidden = false;
    }
  );

  /* --- open the group the reader is in ------------------------------------ */

  var groups = [].slice.call(document.querySelectorAll('nav[aria-label="Contents"] details'));

  function reveal(target) {
    for (var node = target; node; node = node.parentNode) {
      if (node.tagName === 'DETAILS') { node.open = true; }
    }
  }

  if (next && groups.length && !window.location.hash) {
    groups.forEach(function (group) { group.open = group.contains(next); });
  }

  /* --- the filter, and expand all / collapse all -------------------------- */

  var search = document.querySelector('form[role="search"]');
  if (!search) { return; }
  var field = search.querySelector('input');
  var status = search.querySelector('[role="status"]');
  var before = null;

  function remember() {
    if (before === null) { before = groups.map(function (group) { return group.open; }); }
  }

  function restore() {
    rows.forEach(function (row) { row.hidden = false; });
    groups.forEach(function (group, at) {
      group.parentNode.hidden = false;
      if (before !== null) { group.open = before[at]; }
    });
    before = null;
    if (status) { status.hidden = true; }
  }

  /* ⚠️ A row's text without its read words: filtering for "read" must not
     match every row the reader finished. */
  function searchable(row) {
    var copy = row.cloneNode(true);
    [].slice.call(copy.querySelectorAll(SAID)).forEach(function (words) {
      words.parentNode.removeChild(words);
    });
    return copy.textContent;
  }

  function narrow(query) {
    var wanted = query.trim().toLowerCase();
    if (!wanted) { restore(); return; }
    remember();
    var shown = 0;
    rows.forEach(function (row) {
      var hit = searchable(row).toLowerCase().indexOf(wanted) !== -1;
      row.hidden = !hit;
      if (hit) { shown += 1; }
    });
    groups.forEach(function (group) {
      var any = rows.some(function (row) { return !row.hidden && group.contains(row); });
      group.parentNode.hidden = !any;
      group.open = any;
    });
    if (status) { fill(status, shown); status.hidden = false; }
  }

  search.addEventListener('submit', function (event) { event.preventDefault(); });
  if (field) {
    field.addEventListener('input', function () { narrow(field.value); });
    field.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') { field.value = ''; restore(); }
    });
  }

  [].slice.call(search.querySelectorAll('button[value]')).forEach(function (button) {
    button.addEventListener('click', function () {
      var opening = button.value === 'expand';
      groups.forEach(function (group) { group.open = opening; });
    });
  });

  search.hidden = false;
}());
