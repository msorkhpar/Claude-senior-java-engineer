/* Whether a page's narration can be heard: its FIRST clip, asked once.

   ⛔ **Split out of `narration.js` at its seam** (R11): that file is *the
   transport and the highlight*; this is *whether there is anything to
   transport*. It defines one function and draws nothing.

   ⭐ **The page asks the clip itself, and nothing written beside it.** A file
   that said whether the clips were on disk went stale whenever anything but
   its writer moved them: a site packed for release said *released* on the
   author's own disk, with every clip there. So the page loads the metadata of
   its first clip, in an element of its own that plays nothing:

   - it loads: the clips are here, and `heard` is called — once;
   - it fails — not found, a network error, or a clip the browser cannot
     decode — and nothing more happens: no other clip is asked for, and the
     transport stays hidden.

   ⚠️ **A failed probe is one error in the console**, `404` served or
   `ERR_FILE_NOT_FOUND` over `file://`, and that one line is the accepted
   price of asking. ⛔ A probe that never settles is never heard: the
   transport stays hidden rather than guessing.

   ⛔ **No network of its own** (R8): the one source it is given is the page's
   own relative href, read off the passage by `narration.js`. */

(function () {
  'use strict';

  window.studyforge = window.studyforge || {};

  window.studyforge.probeClip = function (source, heard) {
    var probe = new Audio();
    /* ⛔ Answered once: both listeners go at the first answer, so a later
       event from the same element can never call `heard`. */
    function settle(loaded) {
      probe.removeEventListener('loadedmetadata', found);
      probe.removeEventListener('error', lost);
      if (loaded) { heard(); }
    }
    function found() { settle(true); }
    function lost() { settle(false); }
    probe.addEventListener('loadedmetadata', found);
    probe.addEventListener('error', lost);
    probe.preload = 'metadata';
    probe.src = source;
  };
}());
