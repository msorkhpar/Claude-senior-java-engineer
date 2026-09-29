/* The copy button on a code block.

   ⛔ Progressive enhancement, and it is the reason the button is created HERE
   rather than rendered into the page. A button written into the markup would
   sit there doing nothing with scripting off, and a control that does nothing
   is worse than no control. The page ships the figure; this adds the button
   when there is something to add it to.

   ⚠️ `navigator.clipboard` needs a secure context, which `file://` is not in
   every browser. The fallback is the old selection-and-`execCommand` route,
   and when neither works the button says so instead of silently doing
   nothing. */

(function () {
  /* ⛔ The fallback names the key THIS machine uses: it said
     "Press ⌘C" to every reader, which is wrong everywhere but a Mac. */
  function copyKey() {
    var platform = (navigator.userAgentData && navigator.userAgentData.platform) ||
      navigator.platform || '';
    return /mac|iphone|ipad/i.test(platform) ? '\u2318C' : 'Ctrl+C';
  }

  var figures = [].slice.call(document.querySelectorAll('figure.code'));
  if (!figures.length) { return; }

  function copy(text) {
    if (navigator.clipboard && window.isSecureContext) {
      return navigator.clipboard.writeText(text);
    }
    return new Promise(function (resolve, reject) {
      var area = document.createElement('textarea');
      area.value = text;
      area.setAttribute('readonly', '');
      area.style.position = 'fixed';
      area.style.left = '-9999px';
      document.body.appendChild(area);
      area.select();
      var ok = false;
      try { ok = document.execCommand('copy'); } catch (error) { ok = false; }
      document.body.removeChild(area);
      if (ok) { resolve(); } else { reject(new Error('copy refused')); }
    });
  }

  figures.forEach(function (figure) {
    var code = figure.querySelector('pre code');
    var caption = figure.querySelector('figcaption');
    if (!code || !caption) { return; }

    var button = document.createElement('button');
    button.type = 'button';
    button.className = 'copy';
    button.textContent = 'Copy code';
    caption.appendChild(button);

    button.addEventListener('click', function () {
      copy(code.textContent).then(function () {
        button.textContent = 'Copied';
      }, function () {
        button.textContent = 'Select it and press ' + copyKey();
      });
      window.setTimeout(function () { button.textContent = 'Copy code'; }, 2000);
    });
  });
}());
