/* The practice panel's two editor windows: where each one is, and the tablist over them.

   ⛔ **Split out of `practice.js` at its seam**, and the split is
   its own act rather than a passenger: that file stood at `399` of
   R11's `400` and the Submit breakdown had behaviour to add to the panel. ⭐ The seam is the
   SUBJECT — `practice.js` is *the controls, the run and what the run reported*,
   and this is *the two windows of the editor* — and the two share nothing but
   the markup, which is why neither has to reach into the other.

   ⛔ **This file draws; it never talks to the API.** Everything it asks goes
   through `window.studyforge.run.practice(corpus, key)`, which the SERVING
   PROCESS adds to the page it answers. ⭐ A built text that named the
   API, the serving origin or the client file is a defect R8's floor reads
   (`tests/studyforge/cli/serving.py`), and there is no such name below.

   ⛔ **The editor is NOT started from here, and that is deliberate.** It is a
   development environment with a shell, and opening a reading page is not
   consent to run one. The slot carries the sentence saying it is not running
   and how to start it, so a reader sees a statement rather than a blank frame —
   ⭐ and when the server answers where this practice's two windows are, one
   frame replaces that sentence, and each tab points it at its own window. ⛔ **Every URL is the SERVER's
   answer, never a name in this file**: a built page may name no origin and no
   port (R8), the editor's host port is per-project, and the absolute path a
   window opens is a path inside somebody else's container.

   ⛔ **This panel does not make anything read-only and never says it is.** The
   editor enforces that itself, out of the workspace settings the server writes
   — a guard here would be a second, weaker copy of a rule the editor keeps.

   ## ⛔ ONE reload, and only a genuinely COLD instance can ever need it

   ⭐ **What a served page may frame is composed from the editor origins the
   SERVING INSTANCE has discovered**, and a cold instance has discovered none
   until a reader's own client asks — which this panel does, exactly one
   document too late: the policy governing THIS document was sent before the
   ask. ⛔ **The serving process may not ask earlier.** Discovering an editor
   forks `docker`, and putting that on the path of an ordinary page response is
   refused outright (spec §8.3), so the remaining move is the client's.

   ⭐ **The browser is ASKED rather than guessed at.** A
   `securitypolicyviolation` naming `frame-src` and this editor's own host is
   the browser stating that the frame was blocked; the reload then gets a
   document composed from the record that ask has just filled. ⛔ **Nothing is
   reloaded on a hunch** — no violation, no reload.

   ⛔ **Three guards, and each one closes a real loop.** The blocked URI must be
   the editor's host, so an unrelated violation reloads nothing. The editor's
   host must be the host THIS page was reached at, because a server withholds an
   editor from another spelling of the same machine on purpose and no number of
   reloads would change that. And this navigation must not itself be a reload,
   which caps the whole remedy at one. ⚠️ **A browser with no navigation timing
   is not reloaded at all**: failing closed is a frame that does not load, and
   failing open is a page that reloads for ever. */

(function () {
  'use strict';

  /* The panel and its parts. ⚠️ Spelled here, in `practice.js` and in
     `render/page/practice.py` — the same two-sided spelling every hook on this
     page has: markup and script cannot import one another, and the Python side
     is the single source for what is EMITTED. */
  var PANEL = 'section[data-practice]';
  var KEY = 'data-practice';
  var CORPUS = 'data-corpus';
  var PART = 'data-practice-part';
  var TAB = 'data-practice-tab';
  var FRAME = 'data-practice-frame';

  /* ⭐ What the workspace says when it opens and closes a practice, raised on
     the practice's panel, and the mark an open panel carries
     (`practice-workspace.js`). */
  var OPENED = 'studyforge:practice-opened';
  var CLOSED = 'studyforge:practice-closed';
  var OPEN = 'data-workspace-open';

  /* The two windows, and what the frame is called to a screen reader. ⚠️ These
     are the framework's own words for its own controls, not the material's
     (R1) — the same status the panel's 'Running…' and 'Passed.' already have. */
  var TITLES = { main: 'Your code', test: 'Tests' };

  /* The directive a blocked editor frame is refused by, in the browser's own
     spelling. ⚠️ Compared as a PREFIX rather than for equality, because the
     older `violatedDirective` reports the whole directive — `frame-src 'none'`
     — where `effectiveDirective` reports only its name. */
  var FRAME_SRC = 'frame-src';

  function part(panel, name) {
    return panel.querySelector('[' + PART + '="' + name + '"]');
  }

  function show(element, visible) {
    if (element) { element.hidden = !visible; }
  }

  /* ⛔ **A FRAME NEVER TAKES FOCUS THE READER DID NOT GIVE IT, AND THE PAGE
     NEVER MOVES ON ITS OWN**.

     ⚠️ **The mechanism, as a real browser behaves.** A
     workbench focuses its editor as it starts — `restoreParts()` calls
     `activeGroup.focus()`, then the editor that opens the window's file calls
     `focus()` on its input, neither with `preventScroll` — and the browser lets
     a frame of another origin on the same site take focus from the page with
     no user activation at all. ⛔ **Focusing an element scrolls every ancestor
     frame to it**, so a reader who opened the page at its top was carried to
     the editor seconds later, and `document.activeElement` would become the frame.
     ⚠️ Nothing on the frame refuses it: `inert` does not reach the framed
     document, and `allow="focus-without-user-activation 'none'"` is not
     honoured.

     ⭐ **So the page gives focus back, and it can because of an order the
     browser keeps.** The page's `blur` is dispatched INSIDE the frame's
     `focus()` call, before the scroll it starts has moved anything; one task
     later focus goes back to where the reader left it and the page is put back
     where it was, which also cancels the glide `scroll-behavior: smooth` had
     queued. ⛔ **The reader never sees the page move**: the page rests where
     it was opened, and at most one 3px step is painted before it is put back.

     ⭐ **What counts as GIVEN is the reader's hand, never a timer:** the
     pointer over THAT frame together with the page's own user activation —
     which a click inside a frame propagates to every ancestor — or a Tab
     pressed on the page just before focus arrived. ⚠️ **Two steals raise NO
     `blur`**: a frame taking focus while the reader types in ANOTHER frame, and
     any frame taking it while the browser window itself is not focused (the
     page still scrolls). So the page also reads `activeElement`
     every `WATCH_EVERY` ms for as long as it carries a frame, and answers
     those the same way. ⭐ Nothing is installed until the first frame is
     built, so a page with no editor carries none of it. */
  var held = (function () {
    var TAB_GRACE = 500;
    var WATCH_EVERY = 100;
    var frames = [];
    var pointed = null;
    var tabbed = -Infinity;
    var trusted = null;
    var previous = null;
    var resting = { x: 0, y: 0 };

    function ours(element) {
      return frames.indexOf(element) >= 0 ? element : null;
    }

    function here() {
      return { x: window.scrollX, y: window.scrollY };
    }

    function given(built) {
      var state = navigator.userActivation;
      var active = state ? state.isActive : true;
      return (pointed === built && active) || Date.now() - tabbed < TAB_GRACE;
    }

    /* ⚠️ Put the page back, and CANCEL the glide the frame queued. A scroll to
       where the page already is does nothing, and a glide not yet begun
       survives it (the page creeps 3px and stops there), so the
       page is moved one pixel and back — both instant, within one task, so no
       frame is ever painted between them. ⚠️ A glide already under way can
       still land one step after that, so the next two
       frames look again. */
    function stay(at, again) {
      if (window.scrollX !== at.x || window.scrollY !== at.y || again === undefined) {
        var nudge = at.y > 0 ? at.y - 1 : at.y + 1;
        window.scrollTo({ left: at.x, top: nudge, behavior: 'instant' });
        window.scrollTo({ left: at.x, top: at.y, behavior: 'instant' });
      }
      var left = again === undefined ? 2 : again;
      if (left > 0) { requestAnimationFrame(function () { stay(at, left - 1); }); }
    }

    /* ⚠️ One task later, and not inside the `blur`: a focus moved while the
       browser is still dispatching the frame's own focus change is ignored,
       and a MICROTASK is still inside it (`activeElement`
       stays the frame and the page glides to it). ⛔ Whichever frame holds
       focus by THEN is the one answered: the second practice's workbench can
       take it from the first in between, and raises no event here. */
    function refuse(at) {
      setTimeout(function () {
        var built = ours(document.activeElement);
        if (!built || built === trusted) { return; }
        var back = !!previous && previous !== built && previous !== document.body &&
          document.contains(previous);
        if (back) { previous.focus({ preventScroll: true }); } else { built.blur(); }
        stay(at);
      }, 0);
    }

    function arrived(at) {
      var built = ours(document.activeElement);
      if (!built || built === trusted) { return; }
      if (given(built)) {
        trusted = built;
        previous = built;
      } else {
        refuse(at);
      }
    }

    /* The only way to see the two steals that raise no `blur`. ⚠️ `resting` is
       where the page was one tick ago, which is before a steal it now sees. */
    function tick() {
      arrived(resting);
      resting = here();
    }

    function install() {
      setInterval(tick, WATCH_EVERY);
      document.addEventListener('focusin', function (event) { previous = event.target; }, true);
      /* Focus back on the page itself, which raises no `focusin` when it lands
         on the body: the page, not the frame it left, is where it now is. */
      window.addEventListener('focus', function () {
        trusted = null;
        previous = document.activeElement;
      });
      document.addEventListener('keydown', function (event) {
        if (event.key === 'Tab') { tabbed = Date.now(); }
      }, true);
      window.addEventListener('blur', function () {
        resting = here();
        arrived(resting);
      });
    }

    return function (built) {
      if (!frames.length) { install(); }
      frames.push(built);
      built.addEventListener('pointerenter', function () { pointed = built; });
      built.addEventListener('pointerleave', function () {
        if (pointed === built) { pointed = null; }
      });
    };
  }());

  function frame(slot, url, title) {
    var built = document.createElement('iframe');
    built.src = url;
    built.title = title;
    /* ⛔ BEFORE it is added: the workbench may take focus as soon as it loads. */
    held(built);
    slot.appendChild(built);
  }

  /* A URL's host, without its port and without its scheme. ⚠️ An IPv6 literal
     keeps its brackets, which is the spelling `location.hostname` uses too. */
  function hostOf(url) {
    var found = /^[a-z]+:\/\/([^/?#]*)/i.exec(String(url || ''));
    return found ? found[1].replace(/:\d+$/, '').toLowerCase() : '';
  }

  /* Reload once if, and only if, the browser says this document's policy
     blocked the editor's frame. ⛔ The three guards are argued at the top of
     this file, and each of them closes a loop rather than tidying one. */
  function reloadWhenBlocked(url) {
    var editor = hostOf(url);
    var timing = window.performance && window.performance.getEntriesByType
      ? window.performance.getEntriesByType('navigation')
      : [];
    if (!editor || editor !== String(location.hostname || '').toLowerCase()) { return; }
    if (!timing.length || timing[0].type === 'reload') { return; }
    var reloaded = false;
    document.addEventListener('securitypolicyviolation', function (event) {
      var directive = event.effectiveDirective || event.violatedDirective || '';
      if (reloaded || directive.indexOf(FRAME_SRC) !== 0) { return; }
      if (hostOf(event.blockedURI) !== editor) { return; }
      reloaded = true;
      location.reload();
    });
  }

  /* ⭐ **Two windows of ONE editor, in ONE frame — never a split pane, and never
     two frames.** The file a reader may type in and the file that judges it are
     two different acts of reading; a tab picks which one the frame shows.

     ⛔ **One frame, because a page holds one editor at most** (register
     ruling): a second workbench is a second language server, so the Tests tab
     points the same frame at the other window's URL rather than building one.

     ⛔ **Each window's URL is the SERVER's answer and is never built here**: the
     window's own URL is the only thing that can point two windows of one editor
     at two different files, because an extension cannot read its own window's
     query string and both windows share one workspace settings file. */
  function windows(panel, where) {
    var slot = panel.querySelector('[' + FRAME + '="main"]');
    if (!slot) { return; }
    var tested = !!(where.test && where.test.url);
    var buttons = [].slice.call(panel.querySelectorAll('[' + TAB + ']')).filter(
      function (button) {
        var keep = tested || button.getAttribute(TAB) !== 'test';
        if (!keep) { button.hidden = true; }
        return keep;
      }
    );
    var built = null;

    function select(name) {
      buttons.forEach(function (button) {
        var mine = button.getAttribute(TAB) === name;
        button.setAttribute('aria-selected', mine ? 'true' : 'false');
        button.tabIndex = mine ? 0 : -1;
      });
      var url = where[name].url;
      if (!built) {
        frame(slot, url, TITLES[name]);
        built = slot.lastElementChild;
      } else if (built.src !== url) {
        built.title = TITLES[name];
        built.src = url;
      }
      show(slot, true);
    }

    /* ⛔ BEFORE the frame is added, because the violation it listens for is
       raised by adding it. */
    reloadWhenBlocked(where.main.url);
    buttons.forEach(function (button) {
      button.addEventListener('click', function () { select(button.getAttribute(TAB)); });
      /* ⚠️ Arrow keys move between tabs, which is what a tablist is announced
         as promising. Without them the roles say one thing and the keyboard
         does another. */
      button.addEventListener('keydown', function (event) {
        var step = event.key === 'ArrowRight' ? 1 : event.key === 'ArrowLeft' ? -1 : 0;
        var next = buttons.indexOf(button) + step;
        if (!step || next < 0 || next >= buttons.length) { return; }
        event.preventDefault();
        buttons[next].focus();
        select(buttons[next].getAttribute(TAB));
      });
    });
    select('main');
    /* ⭐ One tab is no choice, so the tablist stays hidden where the material
       names no test — the same honesty as offering no Submit. */
    show(part(panel, 'tabs'), tested);
    show(part(panel, 'no-editor'), false);
  }

  /* ⭐ **Nothing is asked until the reader opens this practice** (register
     ruling: no editor loads until a practice is opened), and the frame goes
     when it is closed or another is opened — so the page holds one at most.
     ⛔ Frames are added only for an editor that is already up over this
     corpus's own files and that actually holds this practice's file — the
     server decides both, this asks.

     ⚠️ **Asked for, never assumed.** A site BUILT by one version of this
     framework may be SERVED by another, and the client is the serving process's;
     a panel that called a function an older client does not publish would take
     the whole editor slot down with it. */
  var asking = 0;

  function open(panel, run) {
    var key = panel.getAttribute(KEY);
    var corpus = panel.getAttribute(CORPUS);
    if (!key || !corpus || !run.practice) { return; }
    var mine = asking += 1;
    run.practice(corpus, key).then(function (where) {
      /* ⛔ A reader who moved on before the answer came gets no frame here. */
      if (mine !== asking || !panel.hasAttribute(OPEN)) { return; }
      if (where && where.main && where.main.url) { windows(panel, where); }
    }, function () { return null; });
  }

  /* ⭐ The panel goes back to what it shipped as: no frame, the sentence
     standing, the tablist hidden. ⚠️ A clone of each tab replaces it, which is
     the one way to drop the listeners the last opening added. */
  function close(panel) {
    asking += 1;
    var slot = panel.querySelector('[' + FRAME + '="main"]');
    if (slot) { while (slot.firstChild) { slot.removeChild(slot.firstChild); } show(slot, false); }
    [].slice.call(panel.querySelectorAll('[' + TAB + ']')).forEach(function (button) {
      var fresh = button.cloneNode(true);
      fresh.hidden = false;
      button.parentNode.replaceChild(fresh, button);
    });
    show(part(panel, 'tabs'), false);
    show(part(panel, 'no-editor'), true);
  }

  /* ⭐ The frame and its one reload are PUBLISHED, so a lesson's code examples
     (`code-links.js`) builds its windows with the same focus guard and the
     same remedy for a cold instance, rather than a second copy of either. */
  window.studyforge = window.studyforge || {};
  window.studyforge.frames = { frame: frame, reloadWhenBlocked: reloadWhenBlocked };

  var run = window.studyforge && window.studyforge.run;
  if (!run || !run.available()) { return; }
  document.addEventListener(OPENED, function (event) {
    if (event.target.matches && event.target.matches(PANEL)) { open(event.target, run); }
  });
  document.addEventListener(CLOSED, function (event) {
    if (event.target.matches && event.target.matches(PANEL)) { close(event.target); }
  });
}());
