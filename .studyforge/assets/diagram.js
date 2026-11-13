/* Diagrams: draws each Mermaid figure of a page in place, with the vendored Mermaid, after load.
   A diagram is a ```mermaid fence, which the renderer frames as figure[data-frame="diagram"] with
   its source in <pre><code> (render/page/blocks/figure.py). The drawing goes into a box inside the
   figure and the source stays where it was, hidden once drawn: with scripting off, or a diagram
   Mermaid refuses, the reader keeps the source, and a re-draw reads it again.
   ⭐ Themed from the palette: every colour is a token read off the page with getComputedStyle and
   handed to Mermaid's `base` theme, so a diagram looks native in light and in dark, and it is drawn
   again when the reader changes the theme (data-theme on <html>) or the system does.
   ⛔ securityLevel 'strict': a diagram's labels are text, never markup or a script.
   Linked only by a page whose body carries a diagram (render/page/diagram.py). */
(function () {
  "use strict";
  var FIGURES = 'figure.code[data-frame="diagram"]';
  var DRAWING = "data-diagram-drawing";
  var DRAWN = "data-drawn";
  /* A wide diagram is scaled down to the column only this far; past it, its labels are too
     small to read, so it keeps this share of its own width and scrolls inside its card. */
  var READABLE = 0.75;
  var generation = 0;

  function token(style, name) {
    return style.getPropertyValue(name).trim();
  }

  function dark() {
    var chosen = document.documentElement.getAttribute("data-theme");
    if (chosen === "dark") return true;
    if (chosen === "light") return false;
    return !!(window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches);
  }

  /* Mermaid's names on the left, the palette's tokens on the right. A token the page does not
     define is left to Mermaid's own default rather than guessed. */
  var THEMED = {
    background: "--surface",
    mainBkg: "--bg",
    primaryColor: "--bg",
    primaryTextColor: "--fg",
    primaryBorderColor: "--rule-strong",
    secondaryColor: "--surface-2",
    secondaryTextColor: "--fg",
    secondaryBorderColor: "--rule-strong",
    tertiaryColor: "--surface",
    tertiaryTextColor: "--fg",
    tertiaryBorderColor: "--rule",
    nodeBorder: "--rule-strong",
    nodeTextColor: "--fg",
    textColor: "--fg",
    titleColor: "--fg",
    lineColor: "--muted",
    defaultLinkColor: "--muted",
    edgeLabelBackground: "--surface",
    clusterBkg: "--surface",
    clusterBorder: "--rule-strong",
    actorBkg: "--bg",
    actorBorder: "--rule-strong",
    actorTextColor: "--fg",
    actorLineColor: "--rule-strong",
    signalColor: "--fg-soft",
    signalTextColor: "--fg",
    labelBoxBkgColor: "--surface",
    labelBoxBorderColor: "--rule-strong",
    labelTextColor: "--fg",
    loopTextColor: "--fg",
    noteBkgColor: "--surface",
    noteTextColor: "--fg",
    noteBorderColor: "--accent",
    activationBkgColor: "--surface-2",
    activationBorderColor: "--rule-strong",
    sequenceNumberColor: "--surface",
    labelColor: "--fg",
    stateLabelColor: "--fg",
    stateBkg: "--bg",
    compositeBackground: "--surface",
    altBackground: "--surface-2",
    classText: "--fg",
    errorBkgColor: "--bad-soft",
    errorTextColor: "--bad"
  };

  function theme() {
    var style = getComputedStyle(document.documentElement);
    var variables = { darkMode: dark(), fontSize: "15px" };
    var face = token(style, "--font-ui");
    if (face) variables.fontFamily = face;
    for (var name in THEMED) {
      var value = token(style, THEMED[name]);
      if (value) variables[name] = value;
    }
    return {
      startOnLoad: false,
      securityLevel: "strict",
      suppressErrorRendering: true,
      theme: "base",
      fontFamily: variables.fontFamily,
      /* ⚠️ A sequence diagram measures its actors, messages and notes in faces of its own
         configuration, not the theme's: left at Mermaid's, a note's box is sized for a face the
         page never draws it in. ⭐ And a note wraps to the actors it spans rather than running past
         its own box. */
      sequence: {
        wrap: true,
        actorFontFamily: variables.fontFamily,
        messageFontFamily: variables.fontFamily,
        noteFontFamily: variables.fontFamily
      },
      themeVariables: variables
    };
  }

  function box(figure) {
    var drawing = figure.querySelector("[" + DRAWING + "]");
    if (!drawing) {
      drawing = document.createElement("div");
      drawing.setAttribute(DRAWING, "");
      var pre = figure.querySelector("pre");
      figure.insertBefore(drawing, pre);
    }
    return drawing;
  }

  function refused(figure, id) {
    figure.removeAttribute(DRAWN);
    var drawing = figure.querySelector("[" + DRAWING + "]");
    if (drawing) drawing.parentNode.removeChild(drawing);
    /* Mermaid may leave its scratch element behind when it gives up. */
    var scratch = document.getElementById("d" + id);
    if (scratch) scratch.parentNode.removeChild(scratch);
  }

  function readable(drawing) {
    var svg = drawing.querySelector("svg");
    if (!svg || !svg.viewBox || !svg.viewBox.baseVal) return;
    var width = svg.viewBox.baseVal.width;
    if (width > 0) svg.style.minWidth = Math.round(width * READABLE) + "px";
  }

  function drawOne(figure, index, mine) {
    var code = figure.querySelector("pre code");
    if (!code) return Promise.resolve();
    var id = "sf-diagram-" + mine + "-" + index;
    return mermaid.render(id, code.textContent).then(
      function (drawn) {
        if (mine !== generation) return;
        var drawing = box(figure);
        drawing.innerHTML = drawn.svg;
        if (drawn.bindFunctions) drawn.bindFunctions(drawing);
        readable(drawing);
        figure.setAttribute(DRAWN, "");
      },
      function () {
        if (mine === generation) refused(figure, id);
      }
    );
  }

  function faces(family) {
    if (!family || !document.fonts || !document.fonts.load) return Promise.resolve();
    var loads = ["400", "600"].map(function (weight) {
      return document.fonts.load(weight + " 15px " + family).catch(function () {});
    });
    return Promise.all(loads).then(function () {});
  }

  function draw() {
    if (typeof mermaid === "undefined") return;
    var figures = document.querySelectorAll(FIGURES);
    if (!figures.length) return;
    generation += 1;
    var mine = generation;
    var settings = theme();
    mermaid.initialize(settings);
    /* ⚠️ Mermaid measures each label before it draws the box around it, so the interface face
       must be loaded first: measured in a fallback face, a note's box is narrower than its words. */
    var chain = faces(settings.fontFamily);
    Array.prototype.forEach.call(figures, function (figure, index) {
      chain = chain.then(function () {
        if (mine === generation) return drawOne(figure, index, mine);
      });
    });
  }

  function watch() {
    if (window.MutationObserver) {
      new MutationObserver(draw).observe(document.documentElement, {
        attributes: true,
        attributeFilter: ["data-theme"]
      });
    }
    if (window.matchMedia) {
      var query = window.matchMedia("(prefers-color-scheme: dark)");
      if (query.addEventListener) query.addEventListener("change", draw);
      else if (query.addListener) query.addListener(draw);
    }
  }

  function start() {
    draw();
    watch();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", start);
  } else {
    start();
  }
})();
