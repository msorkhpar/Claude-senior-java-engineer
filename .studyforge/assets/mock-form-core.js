/* The exam form's shared parts: reading the page's blocks, the reader's own store, the seeded
   shuffle and the draw of a sitting. Written beside `mock-form.js`, which uses it. */

(function () {
  'use strict';

  var PART = 'data-form-part';
  var STORE_KEY = 'studyforge.mockform.v1';
  var VERSION = 1;

  function part(root, name) { return root.querySelector('[' + PART + '="' + name + '"]'); }
  function fill(template, values) {
    return String(template).replace(/\{(\w+)\}/g, function (all, name) {
      return Object.prototype.hasOwnProperty.call(values, name) ? String(values[name]) : all;
    });
  }
  function json(root, name) {
    var block = part(root, name);
    if (!block) { return null; }
    try {
      var read = JSON.parse(block.textContent);
      return read && typeof read === 'object' && !Array.isArray(read) ? read : null;
    } catch (error) { return null; }
  }
  function make(tag, attributes, text) {
    var element = document.createElement(tag);
    Object.keys(attributes || {}).forEach(function (name) { element.setAttribute(name, attributes[name]); });
    if (text !== undefined) { element.textContent = text; }
    return element;
  }

  /* The seeded generator and the shuffle that uses it. */
  function generator(seed) {
    var a = seed >>> 0;
    return function () {
      a = (a + 0x6D2B79F5) >>> 0;
      var t = a;
      t = Math.imul(t ^ (t >>> 15), t | 1);
      t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }
  function hash(text) {
    var h = 2166136261;
    for (var i = 0; i < text.length; i += 1) { h = Math.imul(h ^ text.charCodeAt(i), 16777619); }
    return h >>> 0;
  }
  function shuffled(list, random) {
    var a = list.slice();
    for (var i = a.length - 1; i > 0; i -= 1) {
      var j = Math.floor(random() * (i + 1));
      var t = a[i]; a[i] = a[j]; a[j] = t;
    }
    return a;
  }
  function newSeed() {
    try {
      var buffer = new Uint32Array(1);
      window.crypto.getRandomValues(buffer);
      return buffer[0];
    } catch (error) { return Math.floor(Math.random() * 4294967296); }
  }

  /* The reader's own store, or null where the browser refuses it. */
  function backing() {
    try {
      var held = window.localStorage;
      held.setItem(STORE_KEY + '.probe', '1');
      held.removeItem(STORE_KEY + '.probe');
      return held;
    } catch (error) { return null; }
  }
  function everything(store) {
    try {
      var held = JSON.parse(store.getItem(STORE_KEY) || 'null');
      if (held && held.version === VERSION && held.exams && typeof held.exams === 'object') {
        if (!held.seen || typeof held.seen !== 'object') { held.seen = {}; }
        return held;
      }
    } catch (error) { /* an unreadable record is an empty one */ }
    return { version: VERSION, exams: {}, seen: {} };
  }
  function recall(store, name) {
    if (!store) { return null; }
    var kept = everything(store).exams[name];
    return kept && typeof kept === 'object' ? kept : null;
  }
  function seenOf(store, name) {
    if (!store) { return []; }
    var list = everything(store).seen[name];
    return Array.isArray(list) ? list : [];
  }
  function keep(store, name, state, seen) {
    if (!store) { return; }
    try {
      var held = everything(store);
      if (state === null) { delete held.exams[name]; } else { held.exams[name] = state; }
      if (seen) { held.seen[name] = seen; }
      store.setItem(STORE_KEY, JSON.stringify(held));
    } catch (error) { /* a full or refused store loses the draft, not the exam */ }
  }

  function percent(right, asked) { return asked === 0 ? 0 : Math.floor((right * 100) / asked); }

  function clock(ms) {
    var total = Math.max(0, Math.ceil(ms / 1000));
    var h = Math.floor(total / 3600);
    var m = Math.floor((total % 3600) / 60);
    var s = total % 60;
    function two(n) { return (n < 10 ? '0' : '') + n; }
    return (h ? h + ':' + two(m) : String(m)) + ':' + two(s);
  }

  /* ---- the draw: pure, so what a page draws is what a reading of it asks for ---- */

  function unitsOf(metaById, ids) {
    var units = [];
    var at = {};
    ids.forEach(function (id) {
      var scenario = metaById[id].scenario;
      var unitKey = scenario ? 'S:' + scenario : 'Q:' + id;
      if (at[unitKey] === undefined) {
        at[unitKey] = units.length;
        units.push({ key: unitKey, scenario: scenario, ids: [] });
      }
      units[at[unitKey]].ids.push(id);
    });
    return units;
  }

  function quotas(plan, metas, n) {
    var counts = {};
    var weights = {};
    metas.forEach(function (one) { counts[one.domain] = (counts[one.domain] || 0) + 1; });
    var total = 0;
    (plan.domains || []).forEach(function (domain) {
      weights[domain.id] = typeof domain.weight === 'number' ? domain.weight : (counts[domain.id] || 0);
      total += weights[domain.id];
    });
    total = total || 1;
    var base = {};
    var sum = 0;
    var order = (plan.domains || []).map(function (domain) { return domain.id; });
    order.forEach(function (id) { base[id] = Math.floor((n * weights[id]) / total); sum += base[id]; });
    var ranked = order.slice().sort(function (a, b) {
      var ra = (n * weights[a]) % total;
      var rb = (n * weights[b]) % total;
      return rb - ra || order.indexOf(a) - order.indexOf(b);
    });
    for (var i = 0; i < n - sum; i += 1) { base[ranked[i % ranked.length]] += 1; }
    return base;
  }

  function draw(plan, metas, metaById, sitting, random, seen) {
    var all = metas.map(function (one) { return one.id; });
    var seenSet = {};
    seen.forEach(function (id) { seenSet[id] = true; });
    if (all.every(function (id) { return seenSet[id]; })) { seenSet = {}; }
    var units = unitsOf(metaById, all);
    function unseenRatio(unit) {
      return unit.ids.filter(function (id) { return seenSet[id]; }).length / unit.ids.length;
    }
    var ranked = shuffled(units, random).sort(function (a, b) { return unseenRatio(a) - unseenRatio(b); });
    var chosen = [];
    if (sitting && typeof sitting.scenarios === 'number') {
      chosen = ranked.filter(function (unit) { return unit.scenario; }).slice(0, sitting.scenarios);
    } else if (sitting && sitting.per_domain && typeof sitting.questions === 'number') {
      /* An exact count per domain, whole scenarios kept together: try shuffles until the units
         fit every domain's count exactly (a count no whole scenarios can meet draws the best try). */
      var best = null;
      for (var attempt = 0; attempt < 200; attempt += 1) {
        var trial = attempt === 0 ? ranked
          : shuffled(units, random).sort(function (a, b) { return unseenRatio(a) - unseenRatio(b); });
        var held = {};
        var total = 0;
        var tryChosen = [];
        trial.forEach(function (unit) {
          var counts = {};
          unit.ids.forEach(function (id) { counts[metaById[id].domain] = (counts[metaById[id].domain] || 0) + 1; });
          var fits = Object.keys(counts).every(function (d) {
            return (held[d] || 0) + counts[d] <= (sitting.per_domain[d] || 0);
          });
          if (!fits) { return; }
          Object.keys(counts).forEach(function (d) { held[d] = (held[d] || 0) + counts[d]; });
          total += unit.ids.length;
          tryChosen.push(unit);
        });
        if (!best || total > best.total) { best = { total: total, chosen: tryChosen }; }
        if (total === sitting.questions) { break; }
      }
      chosen = best.chosen;
    } else if (sitting && typeof sitting.questions === 'number' && sitting.questions < all.length) {
      var n = sitting.questions;
      var quota = quotas(plan, metas, n);
      var held = {};
      var total = 0;
      var picked = {};
      var take = function (unit) {
        picked[unit.key] = true;
        chosen.push(unit);
        total += unit.ids.length;
        unit.ids.forEach(function (id) {
          held[metaById[id].domain] = (held[metaById[id].domain] || 0) + 1;
        });
      };
      (plan.domains || []).forEach(function (domain) {
        ranked.forEach(function (unit) {
          if (picked[unit.key] || total + unit.ids.length > n) { return; }
          var here = unit.ids.filter(function (id) { return metaById[id].domain === domain.id; }).length;
          if (here && (held[domain.id] || 0) < quota[domain.id]) { take(unit); }
        });
      });
      ranked.forEach(function (unit) {
        if (!picked[unit.key] && total + unit.ids.length <= n) { take(unit); }
      });
    } else {
      chosen = units;
    }
    var order = [];
    shuffled(chosen, random).forEach(function (unit) { order = order.concat(unit.ids); });
    return order;
  }

  /* The sitting the mock's time is for: the largest number of questions any sitting draws. */
  function fullCount(sittings, total) {
    var most = 0;
    sittings.forEach(function (one) {
      most = Math.max(most, typeof one.questions === 'number' ? one.questions
        : typeof one.scenarios === 'number' ? 0 : total);
    });
    return most || total;
  }

  function minutesFor(plan, sittings, sitting, drawn, total) {
    if (sitting && typeof sitting.minutes === 'number') { return sitting.minutes; }
    if (typeof plan.minutes !== 'number') { return null; }
    return Math.max(1, Math.min(plan.minutes, Math.round((plan.minutes * drawn) / fullCount(sittings, total))));
  }


  var root = (window.studyforge = window.studyforge || {});
  root.mockForm = {
    part: part, fill: fill, json: json, make: make, generator: generator, hash: hash,
    shuffled: shuffled, newSeed: newSeed, backing: backing, recall: recall, seenOf: seenOf,
    keep: keep, percent: percent, clock: clock, unitsOf: unitsOf, quotas: quotas, draw: draw,
    fullCount: fullCount, minutesFor: minutesFor
  };
}());
