/* Math: draws a page's LaTeX with the vendored KaTeX, in place, after the page has loaded.
   Two forms, both as the archive keeps them (GitHub's own math syntax):
   - a display formula is a ```math fence, drawn as a code figure whose caption names `math`;
   - an inline formula is a code span between two dollar signs, $`x_{i,t}`$.
   A formula KaTeX cannot parse is left as the code it was, so nothing a reader can read is lost.
   Linked only by a page whose body carries math (render/page/math.py). */
(function () {
  "use strict";
  var OPTIONS = { throwOnError: true, output: "htmlAndMathml" };

  function display() {
    var figures = document.querySelectorAll("figure.code");
    for (var i = 0; i < figures.length; i++) {
      var figure = figures[i];
      var what = figure.querySelector("figcaption .what");
      var code = figure.querySelector("pre code");
      if (!what || !code || what.textContent.trim() !== "math") continue;
      var drawn = document.createElement("div");
      drawn.className = "math-display";
      try {
        katex.render(code.textContent, drawn, { displayMode: true, throwOnError: true, output: "htmlAndMathml" });
      } catch (refused) {
        continue;
      }
      figure.parentNode.replaceChild(drawn, figure);
    }
  }

  function inline() {
    var codes = Array.prototype.slice.call(document.querySelectorAll("code"));
    for (var i = 0; i < codes.length; i++) {
      var code = codes[i];
      if (code.closest("pre")) continue;
      var before = code.previousSibling;
      var after = code.nextSibling;
      if (!before || !after || before.nodeType !== 3 || after.nodeType !== 3) continue;
      if (before.data.slice(-1) !== "$" || after.data.charAt(0) !== "$") continue;
      var drawn = document.createElement("span");
      drawn.className = "math-inline";
      try {
        katex.render(code.textContent, drawn, OPTIONS);
      } catch (refused) {
        continue;
      }
      before.data = before.data.slice(0, -1);
      after.data = after.data.slice(1);
      code.parentNode.replaceChild(drawn, code);
    }
  }

  function draw() {
    if (typeof katex === "undefined") return;
    display();
    inline();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", draw);
  } else {
    draw();
  }
})();
