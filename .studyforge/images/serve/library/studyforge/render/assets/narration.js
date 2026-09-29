/* The narration transport: play, advance, and the highlight that tracks what is spoken.

   ⛔ **One clip per speech unit, and the granularity is the whole design.**
   Spec §8.2 chose the speech unit precisely so a highlight can track playback with
   no word-level timing data anywhere: the audio element already knows which clip
   it is playing, so *which passage is lit* is the same question as *which clip is
   loaded*, and no timing table has to be kept in step with anything.

   ⛔ **Nothing here composes a clip path.** A passage carries its own source,
   emitted by the renderer from `corpus.placement`'s answer (R4) — `audio/x.mp3`
   under the `tree` profile and `audio/<stem>/x.mp3` under `sibling`. ⚠️ A script
   that spelled either would be correct under one profile and silently wrong under
   the other, and the page would render both ways.

   ⛔ **Nothing here types a word a reader sees** (R13). Every sentence is in
   `templates/player.html`, carried as a hidden `[data-state]` span this file
   unhides — the same shape `read-mark.js` uses, and for the same reason: a
   sentence in a script is a sentence no template check reads and no translator
   finds. ⭐ The two things written into the page are a **count** and a **heading
   copied off the page itself**; neither is prose this file authored.

   ⛔ **Progressive enhancement, and the transport ships HIDDEN.** With scripting
   off, a reader is shown nothing rather than a Play button that cannot play —
   a control that does nothing is a dead control, and none is shown. ⭐ The same
   holds when the clips are not here: the page's FIRST clip is asked once
   (`narration-probe.js`), and only a clip that loads unhides the transport.

   ⭐ **It degrades honestly, in three named states** (R6). A passage whose clip
   is not on disk says so and stops rather than pretending; a unit with no usable
   clip at all says so and the controls are disabled; a browser refusing to start
   audio is a known state with a stated remedy — press play once — and is neither
   an error nor hidden.

   ⛔ **No network** (R8). The only thing this file assigns to `src` is a value it
   read off the page, which is relative to the page by construction. */

(function () {
  'use strict';

  /* The region and the transport, both from `templates/player.html`. ⚠️ Spelled
     here and in the template, which is the two-sided spelling every hook on this
     page has: markup and script cannot import one another. */
  var PLAYER = 'player';
  var NARRATOR = 'narrator';

  /* What a narrated passage carries. ⛔ `data-audio` is `render/page/assets.py`'s
     `AUDIO_ATTRIBUTE`, named there one milestone before its writer so the two
     sides could not spell it differently — and it holds the page-relative HREF of
     the clip, which is what a module whose whole subject is *where this page
     reaches* declares.

     ⚠️ **`data-speech-id` is deliberately NOT read here.** The positional id is
     what a *structure* edit must not renumber (spec §8.2), and the player has no
     question it answers: which clip is loaded already says which passage is lit.
     ⛔ Reading it to key something the DOM order already keys would be a second
     ordering, agreeing today and disagreeing the day one of them is wrong. */
  var SOURCE = 'data-audio';

  /* The highlight. ⭐ NOT published in `pageassets.SURFACE_HOOKS`, and that is a
     decision rather than an omission: `data-marked` is published because
     `chrome.css` paints a state `read-mark.js` writes, so two parts hold the
     two ends. Here `narration.css` and this file are one feature's, the spelling
     has one owner, and publishing it would oblige a stylesheet nobody else writes. */
  var SPEAKING = 'data-speaking';

  /* Which sentence, and which face of the play button, is showing. */
  var STATE = 'data-state';
  var MISSING = 'missing';
  var BLOCKED = 'blocked';
  var NONE = 'none';
  var PAUSED = 'paused';
  var PLAYING = 'playing';

  /* How the fill draws itself. ⭐ A custom property rather than a width, so
     `narration.css` owns the drawing and this file only ever states a number. */
  var PROGRESS = '--progress';

  var player = document.getElementById(PLAYER);
  var audio = document.getElementById(NARRATOR);
  if (!player || !audio) { return; }

  /* ⛔ THE WHOLE DOCUMENT, NOT `#content`. A unit page is headed by its
     material's own opening heading, and that heading sits in the `<header>`
     above the content — it is a narrated passage like every other one. Scoped to
     `#content` the transport would skip the first passage of every page while
     `speakable` still made its clip: a clip on disk that nothing could play. ⭐ `querySelectorAll` answers in
     document order, so the heading is still passage one. ⚠️ Nothing outside the
     heading and the content carries `data-audio` — `render/page/document.py` is
     the one composer of this skeleton and fills the attribute in exactly those
     two places. */
  var passages = [].slice.call(document.querySelectorAll('[' + SOURCE + ']'));
  if (!passages.length) { return; }

  var track = document.getElementById('track');
  var fill = document.getElementById('fill');
  var previous = document.getElementById('previous');
  var play = document.getElementById('play');
  var next = document.getElementById('next');
  var speed = document.getElementById('speed');
  var where = document.getElementById('where');
  var counter = document.getElementById('counter');
  var status = document.getElementById('status');

  /* `state name -> the span carrying that sentence`. ⛔ Read out of the markup,
     never listed here: a state this file knows and the template does not is a
     state that announces nothing, silently. */
  var sentences = faces(status);
  var faceOfPlay = faces(play);

  /* Whether each passage can be played at all. ⚠️ A passage may arrive with an
     empty source — the renderer emitted the attribute and synthesis has not run —
     and a passage may fail to load when its clip is not on disk. Both are the
     same answer to the reader and are kept in one place. */
  var playable = passages.map(function (passage) {
    return !!(passage.getAttribute(SOURCE) || '').trim();
  });

  var at = firstPlayable();
  var reduced = quiet();

  /* ⛔ Nothing plays, and no key or click is answered, until the first clip
     was heard (`narration-probe.js`): a page whose clips are not here asks for
     exactly one, and Space still scrolls it. */
  var heard = false;

  function faces(holder) {
    var found = {};
    if (!holder) { return found; }
    [].slice.call(holder.querySelectorAll('[' + STATE + ']')).forEach(function (face) {
      found[face.getAttribute(STATE)] = face;
    });
    return found;
  }

  /* ⛔ Shown by name, and every other face hidden — so two sentences can never be
     on screen at once and `null` is a legal argument meaning *say nothing*. */
  function say(name) {
    Object.keys(sentences).forEach(function (key) {
      sentences[key].hidden = key !== name;
    });
    /* ⚠️ The region too, not only its sentences: an empty live region left in
       the flow reserves a line that reads as a message which failed to arrive,
       and CSS cannot ask "are all my children hidden" without `:has`. */
    if (status) { status.hidden = !name; }
  }

  function showFace(name) {
    Object.keys(faceOfPlay).forEach(function (key) {
      faceOfPlay[key].hidden = key !== name;
    });
  }

  function quiet() {
    if (!window.matchMedia) { return false; }
    var query = window.matchMedia('(prefers-reduced-motion: reduce)');
    return !!(query && query.matches);
  }

  function firstPlayable() {
    for (var index = 0; index < passages.length; index += 1) {
      if (playable[index]) { return index; }
    }
    return -1;
  }

  function nextPlayable(from, step) {
    for (var index = from + step; index >= 0 && index < passages.length; index += step) {
      if (playable[index]) { return index; }
    }
    return -1;
  }

  function anyPlayable() {
    return firstPlayable() !== -1;
  }

  /* The count, and the heading this passage sits under. ⛔ Both are numbers or
     text already on the page — this file authors neither. */
  function label() {
    if (counter) {
      counter.textContent = at === -1 ? '' : (at + 1) + ' / ' + passages.length;
    }
    if (!where) { return; }
    var heading = at === -1 ? null : headingAbove(passages[at]);
    where.textContent = heading ? heading.textContent : '';
  }

  function headingAbove(passage) {
    var section = passage.closest ? passage.closest('section') : null;
    return section ? section.querySelector('h1, h2, h3, h4, h5, h6') : null;
  }

  /* Overall progress through the unit, not through one clip: the aria-label on
     `#track` says *"Progress through this unit"*, and a bar that reset at every
     passage would be answering a question nobody asked. */
  function progress() {
    var whole = passages.length;
    var done = at === -1 ? 0 : at;
    var within = 0;
    if (audio.duration && isFinite(audio.duration) && audio.duration > 0) {
      within = Math.min(1, (audio.currentTime || 0) / audio.duration);
    }
    var fraction = whole ? Math.min(1, (done + within) / whole) : 0;
    var percent = Math.round(fraction * 1000) / 10;
    if (fill && fill.style && fill.style.setProperty) {
      fill.style.setProperty(PROGRESS, percent + '%');
    }
    if (track) {
      track.setAttribute('aria-valuemax', '100');
      track.setAttribute('aria-valuenow', String(percent));
    }
  }

  /* ⭐ A hidden passage is marked, and scrolled to, as its stand-in (`narration-stand-in.js`). */
  var standIn = window.studyforge.standIn, standing = null;

  function highlight() {
    passages.forEach(function (passage, index) {
      if (index === at) { passage.setAttribute(SPEAKING, 'true'); } else { passage.removeAttribute(SPEAKING); }
    });
    if (standing) { standing.removeAttribute(SPEAKING); }
    standing = at === -1 ? null : standIn(passages[at]);
    if (standing) { standing.setAttribute(SPEAKING, 'true'); }
  }

  function reveal(passage) {
    var target = passage && (standIn(passage) || passage);
    if (!target || !target.scrollIntoView) { return; }
    target.scrollIntoView({ block: 'center', behavior: reduced ? 'auto' : 'smooth' });
  }

  /* ⛔ The one place `src` is assigned, and the value is the page's own. */
  function load(index) {
    at = index;
    audio.src = passages[index].getAttribute(SOURCE);
    audio.playbackRate = rate();
    highlight();
    label();
    progress();
  }

  function rate() {
    var chosen = speed ? parseFloat(speed.value) : 1;
    return chosen > 0 ? chosen : 1;
  }

  /* ⛔ ONLY the rejection is handled, and that is not laziness — it is a defect
     this part's own runtime test caught. A success handler that cleared the
     status would run on the microtask queue, AFTER a synchronous `error` from a
     clip that is not on disk had already put the reason on screen, and would
     wipe it: the reader would be shown silence with no explanation, which is the
     exact failure R6 is here to prevent. `begin` clears the status before it
     starts, so there is nothing left for a success to clear.

     ⛔ **A clip that is not on disk fires `error` AND rejects `play()`, in
     either order.** The rejection stands down when the passage it started is no
     longer playable, so an `error` that arrived first keeps its sentence; one that
     arrives second overwrites the blocked one on its own. */
  function start() {
    var started = audio.play();
    var index = at;
    if (started && started.catch) {
      started.catch(function () {
        if (index !== -1 && !playable[index]) { return; }
        /* ⚠️ A known state with a stated remedy, not an error: the browser
           refused to start audio without a gesture it recognised. */
        say(BLOCKED);
        showFace(PAUSED);
      });
    }
  }

  function begin(index, scroll) {
    if (!heard || index === -1 || !playable[index]) { return; }
    load(index);
    say(null);
    showFace(PLAYING);
    if (scroll) { reveal(passages[index]); }
    start();
  }

  function toggle() {
    if (!anyPlayable()) { return; }
    if (audio.paused) {
      if (at === -1) { at = firstPlayable(); }
      begin(at, false);
    } else {
      audio.pause();
      showFace(PAUSED);
    }
  }

  function step(direction, scroll) {
    if (!anyPlayable()) { return; }
    var target = at === -1 ? firstPlayable() : nextPlayable(at, direction);
    if (target === -1) { return; }
    begin(target, scroll);
  }

  /* ⛔ A clip the page names and the disk does not have. It stops here rather
     than skipping on: a cascade of silent skips is the failure that reports
     nothing, and this passage's own state is what the reader needs. */
  audio.addEventListener('error', function () {
    if (at === -1) { return; }
    playable[at] = false;
    showFace(PAUSED);
    say(anyPlayable() ? MISSING : NONE);
    if (!anyPlayable()) { disable(); }
  });

  audio.addEventListener('ended', function () {
    var target = nextPlayable(at, 1);
    if (target === -1) {
      showFace(PAUSED);
      at = passages.length - 1;
      progress();
      return;
    }
    begin(target, true);
  });

  audio.addEventListener('timeupdate', progress);
  audio.addEventListener('play', function () { showFace(PLAYING); });
  audio.addEventListener('pause', function () { showFace(PAUSED); });

  if (play) { play.addEventListener('click', toggle); }
  if (next) { next.addEventListener('click', function () { step(1, true); }); }
  if (previous) { previous.addEventListener('click', function () { step(-1, true); }); }
  if (speed) {
    speed.addEventListener('change', function () { audio.playbackRate = rate(); });
  }

  /* Click any passage to read from there — the template says so, so it holds.
     ⚠️ A click on a link or a button inside a passage is that control's, never
     the narrator's. */
  passages.forEach(function (passage, index) {
    passage.addEventListener('click', function (event) {
      if (!playable[index]) { return; }
      var target = event.target;
      if (target && target.closest && target.closest('a, button, select, summary')) { return; }
      begin(index, false);
    });
  });

  /* ⛔ The keyboard contract is the template's own sentence and this implements
     exactly it. ⚠️ Space is left alone on a focused control, where it activates
     that control, and in a field, where it is a space — hijacking either is how a
     page-wide shortcut becomes a bug nobody can type around. */
  var TYPING = { INPUT: true, TEXTAREA: true, SELECT: true, BUTTON: true, OPTION: true };

  document.addEventListener('keydown', function (event) {
    if (!heard || event.defaultPrevented || event.altKey || event.ctrlKey || event.metaKey) { return; }
    var target = event.target;
    if (target && (TYPING[target.tagName] || target.isContentEditable)) { return; }
    if (event.key === ' ' || event.key === 'Spacebar') {
      event.preventDefault();
      toggle();
    } else if (event.key === 'ArrowRight') {
      event.preventDefault();
      step(1, true);
    } else if (event.key === 'ArrowLeft') {
      event.preventDefault();
      step(-1, true);
    }
  });

  function disable() {
    [previous, play, next, speed].forEach(function (control) {
      if (control) { control.disabled = true; }
    });
  }

  /* ⛔ The transport is unhidden only once its first clip was heard. A unit
     with no clip to ask for asks nothing and shows nothing. ⭐ The first
     passage is where narration WILL start, and the transport's own line says
     so; nothing on the page is lit until the reader starts it. */
  showFace(PAUSED);
  at = firstPlayable();
  if (at === -1) { return; }
  label();
  progress();
  say(null);
  window.studyforge.probeClip(passages[at].getAttribute(SOURCE), function () {
    heard = true;
    player.hidden = false;
  });
}());
