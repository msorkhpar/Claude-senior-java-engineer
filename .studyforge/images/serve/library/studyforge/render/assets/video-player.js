/* The video block's controls, and the one place Plyr is stopped from
   reaching the network.

   Plyr (vendored, `plyr.js`) enhances the `<video>` the page already shipped,
   so NO generated page changes and the renderer's markup is untouched — the
   same bargain the syntax highlighting strikes. With JS off the element keeps
   its native `controls` attribute and still plays.

   ⛔ NOTHING HERE MAY REACH THE NETWORK (R8). The site is read over `file://`
   as well as from a server, and a clone is meant to play its material with no
   connection at all. Plyr would otherwise fetch its icon sprite from a CDN on
   every single init, so the sprite is vendored, injected into the document
   once, and `loadSprite` is off with `iconUrl` empty — which makes Plyr emit
   `<use href="#plyr-play">` against the inline copy. `blankVideo` is emptied
   for the same reason. ⚠️ The vendored bundle still CONTAINS remote URLs, for
   the streaming providers this site never uses; what matters is that no code
   path here can reach one, and `test_no_network` asserts each disarming
   option is set rather than that the bundle is free of strings. */

(function () {
  var videos = [].slice.call(document.querySelectorAll('figure.video video'));
  if (!videos.length || typeof Plyr === 'undefined') { return; }

  /* Substituted from the vendored `plyr.svg` when the script bundle is
     composed, so the sprite has exactly one source on disk and no derived
     file is committed beside it. */
  var SPRITE = '__PLYR_SPRITE__';

  var holder = document.createElement('div');
  holder.hidden = true;
  holder.setAttribute('aria-hidden', 'true');
  holder.innerHTML = SPRITE;
  document.body.insertBefore(holder, document.body.firstChild);

  var players = videos.map(function (video) {
    return new Plyr(video, {
      /* Plyr's own shortcuts, only once the player has focus. The page-wide
         ones below are ours, and are deliberately narrower. */
      keyboard: { focused: true, global: false },
      loadSprite: false,
      iconUrl: '',
      blankVideo: '',
      speed: { selected: 1, options: [0.5, 0.75, 1, 1.25, 1.5, 1.75, 2] },
      seekTime: 5,
      controls: [
        'play-large', 'restart', 'rewind', 'play', 'fast-forward', 'progress',
        'current-time', 'duration', 'mute', 'volume', 'settings', 'pip',
        'fullscreen'
      ],
      settings: ['speed'],
      tooltips: { controls: true, seek: true }
    });
  });

  /* Two videos on one page must not speak over each other. Wired from the
     elements' own events rather than from either player's internals, so
     neither has to know the other exists — and so narration can join
     the same convention without either side being edited. */
  videos.forEach(function (video) {
    video.addEventListener('play', function () {
      videos.forEach(function (other) { if (other !== video) { other.pause(); } });
    });
  });

  function playing() {
    for (var i = 0; i < videos.length; i += 1) {
      if (!videos[i].paused && !videos[i].ended) { return videos[i]; }
    }
    return null;
  }

  /* While a video is PLAYING it owns space and the arrows, wherever focus
     sits; when it stops they go back untouched.

     ⛔ Registered in the CAPTURE phase on `document`, which is what lets this
     stay purely additive: a later bubble-phase listener on the same node
     never runs while a video is playing, so no other script needs editing and
     none needs to know this file exists. */
  document.addEventListener('keydown', function (event) {
    if (event.metaKey || event.ctrlKey || event.altKey) { return; }
    var video = playing();
    if (!video) { return; }
    var tag = (event.target && event.target.tagName || '').toLowerCase();
    if (tag === 'input' || tag === 'textarea' || tag === 'select') { return; }

    var handled = true;
    if (event.key === ' ' || event.key === 'k') { video.pause(); }
    else if (event.key === 'ArrowRight' || event.key === 'l') {
      video.currentTime = Math.min(video.duration || Infinity, video.currentTime + 5);
    } else if (event.key === 'ArrowLeft' || event.key === 'j') {
      video.currentTime = Math.max(0, video.currentTime - 5);
    } else if (event.key === 'ArrowUp') {
      video.volume = Math.min(1, video.volume + 0.1);
    } else if (event.key === 'ArrowDown') {
      video.volume = Math.max(0, video.volume - 0.1);
    } else { handled = false; }

    if (handled) {
      event.preventDefault();
      event.stopPropagation();
    }
  }, true);

  window.__studyforgeVideoPlayers = players;
}());
