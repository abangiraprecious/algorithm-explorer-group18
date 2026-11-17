/* Algorithm Explorer – front end.
 *
 * This file only DRAWS. Every algorithm and data structure runs in the Java back end;
 * the page sends the user's input to /run/<route> and replays the steps it gets back.
 * (No sorting or searching is done here – not even to lay out labels.)
 */
'use strict';

(function () {
  // ------------------------------------------------------------------ helpers
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => Array.prototype.slice.call(r.querySelectorAll(s));
  const esc = (s) => String(s).replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
  const fmt = (n) => (n === null || n === undefined ? '–' : Number(n).toLocaleString('en-US'));

  function fmtTime(ns) {
    if (ns === null || ns === undefined) return '–';
    if (ns < 1000) return ns + ' ns';
    if (ns < 1e6) return (ns / 1e3).toFixed(ns < 1e4 ? 2 : 1) + ' µs';
    if (ns < 1e9) return (ns / 1e6).toFixed(ns < 1e7 ? 2 : 1) + ' ms';
    return (ns / 1e9).toFixed(2) + ' s';
  }

  function fmtShort(v) {
    if (v >= 1e9) return (v / 1e9).toFixed(v >= 1e10 ? 0 : 1) + 'B';
    if (v >= 1e6) return (v / 1e6).toFixed(v >= 1e7 ? 0 : 1) + 'M';
    if (v >= 1e3) return (v / 1e3).toFixed(v >= 1e4 ? 0 : 1) + 'k';
    return String(Math.round(v * 100) / 100);
  }

  const COLORS = {
    compare: '#2a78d6', swap: '#d6336c', pivot: '#6250d6', final: '#1f8a4c',
    bar: '#8d9bb0', barIn: '#55657c', barOut: '#d3dae3', ink: '#000000', ink3: '#000000', axis: '#6f7a8a', grid: '#e4e9ef',
  };

  // Validated categorical palette, assigned to each algorithm by a FIXED map
  // so a line keeps its colour whatever else is selected.
  const SERIES_COLOR = {
    bubble: '#2a78d6', selection: '#eb6834', insertion: '#1baf7a', merge: '#eda100',
    quick: '#e87ba4', heap: '#008300', counting: '#6250d6', linear: '#e34948', binary: '#5b6b80',
    'fib-naive': '#2a78d6', 'fib-memo': '#eb6834', hanoi: '#1baf7a', factorial: '#eda100', power: '#e87ba4',
  };

  const state = { tab: 'sort', data: null, steps: [], idx: 0, timer: null, prep: null, byTab: {} };

  // ------------------------------------------------------------------ tabs
  $$('.tab').forEach((b) => b.addEventListener('click', () => showTab(b.dataset.tab)));

  function showTab(tab) {
    stop();
    state.tab = tab;
    $$('.tab').forEach((b) => b.classList.toggle('is-active', b.dataset.tab === tab));
    $$('.panel').forEach((p) => { p.hidden = p.id !== 'panel-' + tab; });
    const saved = state.byTab[tab];
    if (saved && tab !== 'analysis' && tab !== 'about') load(saved);
    else $('#results').hidden = true;
  }

  // ------------------------------------------------------------------ quick-fill buttons (they only generate INPUT)
  function rnd(lo, hi) { return lo + Math.floor(Math.random() * (hi - lo + 1)); }

  $$('[data-fill]').forEach((btn) => btn.addEventListener('click', () => {
    const n = Number(btn.dataset.n);
    const kind = btn.dataset.fill;
    const out = new Array(n);
    if (kind === 'random') {
      const hi = n <= 60 ? 99 : n * 3;
      for (let i = 0; i < n; i++) out[i] = rnd(1, hi);
    } else {
      // build an increasing sequence directly from random gaps (no sorting needed)
      let v = rnd(1, 6);
      for (let i = 0; i < n; i++) { out[i] = v; v += rnd(1, 7); }
      if (kind === 'reversed') {
        for (let i = 0, j = n - 1; i < j; i++, j--) { const t = out[i]; out[i] = out[j]; out[j] = t; }
      }
    }
    const ta = btn.closest('.field').querySelector('textarea');
    ta.value = out.join(', ');
    if (btn.closest('form').dataset.route === 'search' && kind === 'sorted') {
      $('#search-target').value = out[rnd(0, n - 1)];
    }
  }));

  // ------------------------------------------------------------------ recursion & data-structure presets
  const REC = {
    factorial: { label: 'n', value: '6', hint: 'A whole number from 0 to 20 (21! does not fit in 64 bits).' },
    'fib-naive': { label: 'n', value: '5', hint: '0 to 32. Try 5 to see the whole call tree, then 25 to feel the exponential cost.' },
    'fib-memo': { label: 'n', value: '6', hint: '0 to 92. Compare the number of calls with the naive version for the same n.' },
    'fib-iter': { label: 'n', value: '10', hint: '0 to 92. A plain loop, shown for comparison: no calls and no call stack.' },
    hanoi: { label: 'Number of disks', value: '3', hint: '1 to 20. Needs 2ⁿ − 1 moves; the replay keeps the first 2,500 steps.' },
    power: { label: 'Base and exponent', value: '3 13', hint: 'Two whole numbers, e.g. "3 13" for 3¹³. The result must fit in 64 bits.' },
    gcd: { label: 'Two numbers', value: '1071 462', hint: 'Two whole numbers from 0 to 1,000,000,000.' },
    permutations: { label: 'Word', value: 'abc', hint: 'Up to 7 characters. Three characters shows the whole recursion tree.' },
    palindrome: { label: 'Text', value: 'Never odd or even', hint: 'Spaces, punctuation and capital letters are ignored.' },
  };

  function syncRec() {
    const r = REC[$('#rec-algo').value];
    $('#rec-arg-label').textContent = r.label;
    $('#rec-arg').value = r.value;
    $('#rec-arg-hint').textContent = r.hint;
  }
  $('#rec-algo').addEventListener('change', syncRec);
  syncRec();

  const DS = {
    stack: { script: 'push 5 8 2\npeek\npop\npush 9 4 7\npop\npop\npop\npop\npop\npop',
      help: 'Commands: push <numbers>, pop, peek, size, isEmpty. The array starts with room for 4 and doubles when full. The last pop runs on an empty stack to show underflow.' },
    queue: { script: 'enqueue 10 20 30\ndequeue\nenqueue 40 50\ndequeue\nenqueue 60 70\npeek\ndequeue',
      help: 'Commands: enqueue <numbers>, dequeue, peek, size. Watch the front and rear wrap around the ring before it grows.' },
    list: { script: 'addLast 10 20 30\naddFirst 5\ninsert 2 15\nsearch 30\nget 3\nremove 20\nreverse',
      help: 'Commands: addFirst <n>, addLast <n>, insert <position> <n>, removeFirst, remove <n>, search <n>, get <position>, reverse, size.' },
    bst: { script: 'insert 50 30 70 20 40 60 80 35\nsearch 35\ninorder\npreorder\nheight\ndelete 30\ndelete 50\ninorder\nmin\nmax',
      help: 'Commands: insert <numbers>, search <n>, delete <n>, inorder, preorder, postorder, height, min, max. Try inserting 1 2 3 4 5 6 to see a tree degenerate into a list.' },
    heap: { script: 'insert 40 15 30 10 50 5\npeek\nextractMin\nextractMin\nbuild 9 4 7 1 8 2 6',
      help: 'Commands: insert <numbers>, extractMin, peek, build <numbers>, size. The tree and the array are the same data.' },
    hash: { script: 'put apple 3\nput banana 7\nput cherry 2\nput date 9\nput elder 4\nput fig 1\nput grape 8\nget cherry\nput apple 10\nremove banana\nget banana',
      help: 'Commands: put <key> <value>, get <key>, remove <key>. The table starts with 8 buckets and doubles when more than 6 keys are stored.' },
    brackets: { script: '{ [ (a + b) * c ] - (d / e) }', label: 'Text with brackets',
      help: 'Any text using ( ) [ ] { }. Try an unbalanced one such as ( [ ) ].' },
    expression: { script: '3 + 4 * (2 - 1) ^ 2 - 10 / 5', label: 'Expression',
      help: 'Numbers with + − * / % ^ and brackets. A leading or bracketed minus is treated as negation, e.g. -(2 + 3) * 2.' },
  };

  function syncDs() {
    const d = DS[$('#ds-kind').value];
    $('#ds-script').value = d.script;
    $('#ds-help').textContent = d.help;
    $('#ds-script-label').innerHTML = d.label ? esc(d.label)
      : 'Commands <span class="hint">one per line, or separated by ;</span>';
    $('#ds-script').rows = d.label ? 2 : 6;
  }
  $('#ds-kind').addEventListener('change', syncDs);
  syncDs();

  // ------------------------------------------------------------------ talking to the back end
  async function post(route, body) {
    let res;
    try {
      res = await fetch('/run/' + route, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body,
      });
    } catch (e) {
      throw new Error('Could not reach the Java back end. Start it with run.sh (macOS/Linux) or run.bat (Windows), then reload this page.');
    }
    let data;
    try { data = await res.json(); } catch (e) { throw new Error('The back end sent an unreadable reply (HTTP ' + res.status + ').'); }
    if (!res.ok || data.error) throw new Error(data.error || 'HTTP ' + res.status);
    return data;
  }

  $$('form.controls').forEach((form) => form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (form.dataset.route === 'analyze') { runAnalysis(form); return; }
    const btn = form.querySelector('.run');
    const label = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Running…';
    stop();
    try {
      const data = await post(form.dataset.route, new URLSearchParams(new FormData(form)));
      state.byTab[state.tab] = data;
      load(data);
    } catch (err) {
      showError(err.message);
    } finally {
      btn.disabled = false;
      btn.textContent = label;
    }
  }));

  function showError(msg) {
    $('#results').hidden = false;
    $('#error').hidden = false;
    $('#error').textContent = msg;
    $('#result-body').hidden = true;
  }

  // ------------------------------------------------------------------ loading a result
  function load(data) {
    state.data = data;
    state.steps = data.steps || [];
    state.idx = 0;
    $('#results').hidden = false;
    $('#error').hidden = true;
    $('#result-body').hidden = false;

    $('#result-line').textContent = data.result || '';
    const notes = [];
    if (data.note) notes.push(data.note);
    if (data.traceOn === false) notes.push('The step replay is switched off for inputs this large (each step stores the whole array). The counts and the running time below still cover the full run.');
    if (data.truncated) notes.push('The replay shows the first ' + fmt(state.steps.length) + ' steps. The counts and the running time cover the whole run.');
    if (data.category === 'recursion' && data.list) notes.push('Output: ' + data.list.slice(0, 60).join(', ') + (data.list.length > 60 ? ' …' : ''));
    if (data.category === 'sort' && data.output && data.output.length <= 200) notes.push('Output: ' + data.output.join(', '));
    $('#result-note').hidden = notes.length === 0;
    $('#result-note').textContent = notes.join(' ');

    renderMetrics(data);
    renderTheory(data);
    state.prep = data.category === 'recursion' || data.algo === 'binary-rec' ? prepRecursion(data) : null;

    const side = data.category === 'recursion' || data.algo === 'binary-rec';
    $('#side').hidden = !side;
    $('#side-title').textContent = 'Call stack';

    buildLog();
    $('#scrub').max = Math.max(0, state.steps.length - 1);
    $('#player').hidden = state.steps.length === 0;
    render(0);
  }

  function renderMetrics(d) {
    const c = d.counts || {};
    const items = [];
    items.push({ label: 'Running time', value: fmtTime(d.timeNs), lead: true,
      title: 'Median of ' + d.trials + ' runs with step recording switched off' });
    items.push({ label: 'Total operations', value: fmt(c.total), lead: true });
    if (d.n !== undefined) items.push({ label: 'Input size n', value: fmt(d.n) });
    if (d.category === 'ds') {
      items.push({ label: 'Steps', value: fmt(c.operations) });
    } else {
      const always = d.category === 'sort' || d.category === 'search' ? ['comparisons'] : ['calls', 'maxDepth'];
      const names = [['comparisons', d.category === 'recursion' ? 'Base-case checks' : 'Comparisons'], ['swaps', 'Swaps'],
        ['writes', d.algo === 'hanoi' ? 'Disk moves' : 'Writes'], ['arith', 'Arithmetic'], ['calls', 'Recursive calls'], ['maxDepth', 'Max depth']];
      names.forEach(([k, label]) => {
        if (always.indexOf(k) >= 0 || c[k] > 0) items.push({ label, value: fmt(c[k]) });
      });
    }
    $('#metrics').innerHTML = items.map((m) =>
      '<div class="metric' + (m.lead ? ' lead' : '') + '"' + (m.title ? ' title="' + esc(m.title) + '"' : '') + '><dt>' + esc(m.label) + '</dt><dd>' + esc(m.value) + '</dd></div>'
    ).join('');
  }

  function log2(n) { return Math.log(n) / Math.LN2; }

  function renderTheory(d) {
    const t = d.theory || {};
    const rows = [['Best case', t.best], ['Average case', t.avg], ['Worst case', t.worst], ['Extra space', t.space],
      ['Stable', t.stable], ['Recurrence', t.recurrence]].filter((r) => r[1] && r[1] !== '–');
    let html = '<h3>' + esc(t.name || d.algo) + '</h3>';
    if (t.idea) html += '<p>' + esc(t.idea) + '</p>';
    if (rows.length) html += '<table>' + rows.map((r) => '<tr><th>' + r[0] + '</th><td>' + esc(r[1]) + '</td></tr>').join('') + '</table>';

    const c = d.counts || {};
    if ((d.category === 'sort' || d.category === 'search') && d.n > 1) {
      const n = d.n;
      html += '<div class="yours"><p><strong>Your run, n = ' + fmt(n) + ':</strong> ' + fmt(c.comparisons) + ' comparisons, '
        + fmt(c.total) + ' operations in total.</p><table>'
        + '<tr><th>log₂ n</th><td>' + fmt(Math.round(log2(n) * 10) / 10) + '</td></tr>'
        + '<tr><th>n</th><td>' + fmt(n) + '</td></tr>'
        + '<tr><th>n log₂ n</th><td>' + fmt(Math.round(n * log2(n))) + '</td></tr>'
        + '<tr><th>n²</th><td>' + fmt(n * n) + '</td></tr>'
        + '<tr><th>n(n−1)/2</th><td>' + fmt(n * (n - 1) / 2) + '</td></tr></table>'
        + '<p>Compare the comparisons with these reference values to see which growth rate this run matches.</p></div>';
    } else if (d.category === 'recursion') {
      html += '<div class="yours"><p><strong>Your run:</strong> ' + fmt(c.calls) + ' recursive call(s), the call stack reached depth '
        + fmt(c.maxDepth) + '. Each active call keeps a frame on the stack, which is why recursion uses O(depth) extra memory.</p></div>';
    } else if (d.category === 'ds') {
      html += '<div class="yours"><p>Each step in the list shows what that one command cost in basic operations, next to its Big-O class.</p></div>';
    }
    $('#theory').innerHTML = html;
  }

  // ------------------------------------------------------------------ step log
  function buildLog() {
    const steps = state.steps;
    const parts = new Array(steps.length);
    const rec = state.data.category === 'recursion' || state.data.algo === 'binary-rec';
    for (let i = 0; i < steps.length; i++) {
      const s = steps[i];
      let msg = esc(s.m || '');
      if (rec && s.d) msg = '<span class="ind">' + '│ '.repeat(Math.max(0, Math.min(s.d - 1, 20))) + '</span>' + msg;
      const cost = s.cost !== undefined ? '<span class="cost">' + fmt(s.cost) + ' ops, ' + esc(s.big) + '</span>' : '<span></span>';
      parts[i] = '<li data-i="' + i + '" class="t-' + esc(s.t) + '"><span>' + msg + '</span>' + cost + '</li>';
    }
    $('#log').innerHTML = parts.join('');
    if (!steps.length) $('#log').innerHTML = '<li><span>No steps recorded for this input.</span><span></span></li>';
  }

  $('#log').addEventListener('click', (e) => {
    const li = e.target.closest('li[data-i]');
    if (li) { stop(); render(Number(li.dataset.i)); }
  });

  // ------------------------------------------------------------------ player
  function render(i) {
    const steps = state.steps;
    const d = state.data;
    if (!d) return;
    if (steps.length === 0) { drawStatic(d); $('#step-message').textContent = ''; return; }
    i = Math.max(0, Math.min(i, steps.length - 1));
    state.idx = i;
    const s = steps[i];
    $('#scrub').value = i;
    $('#counter').textContent = 'Step ' + fmt(i + 1) + ' of ' + fmt(steps.length);
    $('#step-message').innerHTML = '<span class="kind">' + esc(s.t) + '</span>' + esc(s.m || '');

    const log = $('#log');
    const prev = log.querySelector('li.current');
    if (prev) prev.classList.remove('current');
    const li = log.children[i];
    if (li) {
      li.classList.add('current');
      if (li.offsetTop < log.scrollTop || li.offsetTop + li.offsetHeight > log.scrollTop + log.clientHeight) {
        log.scrollTop = li.offsetTop - log.clientHeight / 2;
      }
    }

    switch (d.category) {
      case 'sort': drawBars(s); break;
      case 'search': drawSearch(s); break;
      case 'recursion': drawRecursion(i); break;
      case 'ds': drawDs(s.snap); break;
    }
    if ($('#side').hidden === false) drawStack(state.prep ? state.prep.stackAt[i] : (s.stack || []));
  }

  function play() {
    if (state.timer) { stop(); return; }
    if (state.idx >= state.steps.length - 1) render(0);
    const rate = Number($('#speed').value);
    $('#play').textContent = '❚❚ Pause';
    $('#play').setAttribute('aria-label', 'Pause');
    state.timer = setInterval(() => {
      if (state.idx >= state.steps.length - 1) { stop(); return; }
      render(state.idx + 1);
    }, 1000 / rate);
  }

  function stop() {
    if (state.timer) clearInterval(state.timer);
    state.timer = null;
    $('#play').textContent = '▶ Play';
    $('#play').setAttribute('aria-label', 'Play');
  }

  $('#player').addEventListener('click', (e) => {
    const b = e.target.closest('button[data-act]');
    if (!b) return;
    const act = b.dataset.act;
    if (act === 'play') { play(); return; }
    stop();
    if (act === 'first') render(0);
    if (act === 'prev') render(state.idx - 1);
    if (act === 'next') render(state.idx + 1);
    if (act === 'last') render(state.steps.length - 1);
  });
  $('#scrub').addEventListener('input', (e) => { stop(); render(Number(e.target.value)); });
  $('#speed').addEventListener('change', () => { if (state.timer) { stop(); play(); } });

  document.addEventListener('keydown', (e) => {
    if ($('#results').hidden || !state.steps.length) return;
    const tag = (e.target.tagName || '').toLowerCase();
    if (tag === 'input' || tag === 'textarea' || tag === 'select' || tag === 'button') return;
    if (e.key === 'ArrowRight') { stop(); render(state.idx + 1); e.preventDefault(); }
    else if (e.key === 'ArrowLeft') { stop(); render(state.idx - 1); e.preventDefault(); }
    else if (e.key === ' ') { play(); e.preventDefault(); }
    else if (e.key === 'Home') { stop(); render(0); e.preventDefault(); }
    else if (e.key === 'End') { stop(); render(state.steps.length - 1); e.preventDefault(); }
  });

  // ------------------------------------------------------------------ stage: key
  function key(items) {
    return '<div class="key">' + items.map((it) => '<span><i style="background:' + it[1] + '"></i>' + esc(it[0]) + '</span>').join('') + '</div>';
  }

  function drawStatic(d) {
    let html = '<p class="empty">No replay for this input. ';
    if (d.category === 'sort') html += 'Use 120 numbers or fewer to watch each step; the timing and counts above are for your full input.';
    html += '</p>';
    $('#stage').innerHTML = html;
    $('#counter').textContent = '';
  }

  // ------------------------------------------------------------------ stage: sorting bars
  function drawBars(s) {
    const a = s.a;
    if (!a) return;
    const n = a.length;
    const W = 1000;
    const labels = n <= 40;
    const top = 26, bottom = labels ? 28 : 10, H = 330;
    let lo = 0, hi = 0;
    for (let i = 0; i < n; i++) { if (a[i] < lo) lo = a[i]; if (a[i] > hi) hi = a[i]; }
    if (hi === lo) hi = lo + 1;
    const y = (v) => top + (hi - v) / (hi - lo) * (H - top - bottom);
    const y0 = y(0);
    const bw = W / Math.max(n, 1);
    const gap = bw > 8 ? 2 : (bw > 3 ? 1 : 0);
    const h = {}; (s.h || []).forEach((k) => { h[k] = true; });
    const f = {}; (s.f || []).forEach((k) => { f[k] = true; });
    const r = s.r;
    const moving = s.t === 'swap' || s.t === 'write';
    let out = '<svg viewBox="0 0 ' + W + ' ' + H + '" aria-hidden="true">';
    out += '<line x1="0" x2="' + W + '" y1="' + y0 + '" y2="' + y0 + '" stroke="' + COLORS.axis + '" stroke-width="1"/>';
    for (let i = 0; i < n; i++) {
      let col = COLORS.bar;
      if (s.pv === i) col = COLORS.pivot;
      else if (h[i]) col = moving ? COLORS.swap : COLORS.compare;
      else if (f[i]) col = COLORS.final;
      else if (r) col = (i < r[0] || i > r[1]) ? COLORS.barOut : COLORS.barIn;
      const v = a[i];
      const yt = Math.min(y(v), y0), hh = Math.max(Math.abs(y(v) - y0), 1.5);
      const x = i * bw + gap / 2, w = Math.max(bw - gap, 0.6);
      out += '<rect x="' + x.toFixed(2) + '" y="' + yt.toFixed(2) + '" width="' + w.toFixed(2) + '" height="' + hh.toFixed(2)
        + '" fill="' + col + '"><title>a[' + i + '] = ' + v + '</title></rect>';
      if (labels) {
        const ty = v >= 0 ? yt - 6 : yt + hh + 14;
        const fs = n <= 20 ? 15 : 12;
        out += '<text x="' + (x + w / 2).toFixed(2) + '" y="' + ty.toFixed(2) + '" text-anchor="middle" font-size="' + fs
          + '" font-weight="600" fill="' + COLORS.ink + '" font-family="system-ui, sans-serif">' + v + '</text>';
        out += '<text x="' + (x + w / 2).toFixed(2) + '" y="' + (H - 6) + '" text-anchor="middle" font-size="11" fill="'
          + COLORS.ink3 + '" font-family="system-ui, sans-serif">' + i + '</text>';
      }
    }
    out += '</svg>';
    const k = [['Being compared', COLORS.compare], ['Being moved', COLORS.swap], ['In final place', COLORS.final]];
    if (state.data.algo === 'quick') k.splice(2, 0, ['Pivot', COLORS.pivot]);
    if (r) k.push(['Active part', COLORS.barIn]);
    $('#stage').innerHTML = out + key(k);
  }

  // ------------------------------------------------------------------ stage: search cells
  function drawSearch(s) {
    const a = state.data.array || [];
    const r = s.r, h = s.h ? s.h[0] : -1;
    let html = '<div class="ds-row">';
    for (let i = 0; i < a.length; i++) {
      let cls = 'cell';
      if (i === h) cls += s.t === 'found' ? ' found' : ' hi';
      else if (r && (i < r[0] || i > r[1])) cls += ' dim';
      else if (!r && state.data.algo !== 'linear' && s.t !== 'compare') cls += ' dim';
      html += '<div class="' + cls + '">' + a[i] + '<small>' + i + '</small></div>';
    }
    html += '</div>';
    const k = [['Being checked', COLORS.compare], ['Found', COLORS.final]];
    if (state.data.algo !== 'linear') k.push(['Outside the search range (faded)', COLORS.barOut]);
    $('#stage').innerHTML = '<p class="caption">Looking for <strong>' + esc(state.data.target) + '</strong> in ' + fmt(a.length)
      + ' values (small numbers are indices)</p>' + html + key(k);
  }

  // ------------------------------------------------------------------ recursion: call tree + stack
  function prepRecursion(d) {
    const steps = d.steps;
    const byId = {};
    const order = [];
    const curAt = new Array(steps.length);
    const stackAt = new Array(steps.length);
    const pegsAt = new Array(steps.length);
    const live = [], liveLabels = [];
    let top = 0, pegs = null;
    for (let i = 0; i < steps.length; i++) {
      const s = steps[i];
      if (s.t === 'call') {
        const node = { id: s.id, p: s.p, label: s.label, d: s.d, call: i, ret: Infinity, v: null, kids: [] };
        byId[s.id] = node;
        order[order.length] = node;
        if (byId[s.p]) byId[s.p].kids[byId[s.p].kids.length] = node;
        liveLabels[top] = s.label;
        live[top++] = s.id;
        curAt[i] = s.id;
        stackAt[i] = liveLabels.slice(0, top);
      } else if (s.t === 'return') {
        if (byId[s.id]) { byId[s.id].ret = i; byId[s.id].v = s.v; }
        curAt[i] = s.id;
        stackAt[i] = liveLabels.slice(0, top);   // frame is still shown while it returns
        top--;
      } else {
        curAt[i] = top > 0 ? live[top - 1] : -1;
        stackAt[i] = liveLabels.slice(0, top);
      }
      if (s.pegs) pegs = s.pegs;
      pegsAt[i] = pegs;
    }
    // simple tidy layout: leaves left to right, parents centred over their children
    let leaf = 0, maxD = 1;
    function place(n) {
      if (n.d > maxD) maxD = n.d;
      if (!n.kids.length) { n.x = leaf++; return; }
      for (let k = 0; k < n.kids.length; k++) place(n.kids[k]);
      n.x = (n.kids[0].x + n.kids[n.kids.length - 1].x) / 2;
    }
    for (let i = 0; i < order.length; i++) if (!byId[order[i].p]) place(order[i]);
    return { byId, order, curAt, stackAt, pegsAt, leaves: leaf, maxD };
  }

  function drawRecursion(i) {
    const p = state.prep, d = state.data;
    if (d.algo === 'hanoi') { drawHanoi(p.pegsAt[i]); return; }
    if (!p.order.length) {
      $('#stage').innerHTML = '<p class="caption">This version uses a loop, so there are no recursive calls and the call stack stays empty.</p>'
        + '<p class="result-line">' + esc(state.steps[i].m) + '</p>';
      return;
    }
    if (p.order.length > 160) {
      $('#stage').innerHTML = '<p class="empty">The call tree has ' + fmt(p.order.length) + ' nodes in this replay, too many to draw clearly. '
        + 'The call stack on the right still shows every step. Try a smaller input to see the tree.</p>';
      return;
    }
    const labels = p.order.length <= 40;
    const colW = labels ? 96 : 26, rowH = labels ? 64 : 40;
    const W = Math.max(p.leaves * colW, 300), H = p.maxD * rowH + 30;
    const cx = (n) => n.x * colW + colW / 2, cy = (n) => (n.d - 1) * rowH + 24;
    const cur = p.curAt[i];
    let edges = '', nodes = '';
    for (let k = 0; k < p.order.length; k++) {
      const n = p.order[k];
      const visible = n.call <= i;
      const returned = n.ret <= i;
      const parent = p.byId[n.p];
      if (parent) {
        edges += '<line x1="' + cx(parent) + '" y1="' + (cy(parent) + 12) + '" x2="' + cx(n) + '" y2="' + (cy(n) - 12)
          + '" stroke="' + (visible ? '#9aa6b5' : '#e4e9ef') + '" stroke-width="1.5"/>';
      }
      let fill = '#fff', stroke = '#d3dae3', textCol = COLORS.ink;
      if (visible && !returned) { stroke = COLORS.compare; textCol = COLORS.ink; }
      if (returned) { fill = '#e2f3e9'; stroke = COLORS.final; textCol = COLORS.ink; }
      if (n.id === cur) { fill = '#efedfb'; stroke = COLORS.pivot; textCol = COLORS.ink; }
      const title = '<title>' + esc(n.label) + (n.v !== null && returned ? ' = ' + esc(n.v) : '') + '</title>';
      if (labels) {
        // a returned call shows its value inside the box: "fib(3)=2"
        const w = colW - 10;
        let text = n.label;
        if (returned && n.v !== null && n.v !== 'done') text = n.label + '=' + n.v;
        if (text.length > 13) text = text.slice(0, 12) + '…';
        nodes += '<g>' + title + '<rect x="' + (cx(n) - w / 2) + '" y="' + (cy(n) - 13) + '" width="' + w + '" height="26" fill="' + fill
          + '" stroke="' + stroke + '" stroke-width="' + (n.id === cur ? 2.5 : 1.5) + '"/>'
          + '<text x="' + cx(n) + '" y="' + (cy(n) + 4) + '" text-anchor="middle" font-size="11" font-family="ui-monospace, Menlo, monospace" fill="' + textCol + '">'
          + esc(text) + '</text></g>';
      } else {
        nodes += '<circle cx="' + cx(n) + '" cy="' + cy(n) + '" r="' + (n.id === cur ? 8 : 6) + '" fill="' + (visible ? (returned ? COLORS.final : fill) : '#fff')
          + '" stroke="' + stroke + '" stroke-width="2">' + title + '</circle>';
      }
    }
    const svg = '<svg viewBox="0 0 ' + W + ' ' + H + '" style="max-width:' + Math.max(W, 300) + 'px" aria-hidden="true">' + edges + nodes + '</svg>';
    $('#stage').innerHTML = '<p class="caption">Recursion tree: ' + fmt(p.order.length) + ' call(s) in this replay</p>' + svg
      + key([['Current call', COLORS.pivot], ['Waiting on the stack', COLORS.compare], ['Returned', COLORS.final]]);
  }

  function drawHanoi(pegs) {
    if (!pegs) return;
    let maxDisk = 1;
    pegs.forEach((pg) => pg.forEach((dk) => { if (dk > maxDisk) maxDisk = dk; }));
    const W = 900, H = 300, base = 270, pegW = W / 3, diskH = Math.min(24, 200 / maxDisk);
    let out = '<svg viewBox="0 0 ' + W + ' ' + H + '" aria-hidden="true">';
    for (let p = 0; p < 3; p++) {
      const cx = pegW * p + pegW / 2;
      out += '<rect x="' + (cx - 4) + '" y="40" width="8" height="' + (base - 40) + '" fill="#9aa6b5"/>';
      out += '<text x="' + cx + '" y="' + (base + 24) + '" text-anchor="middle" font-size="16" font-weight="600" fill="' + COLORS.ink
        + '" font-family="Palatino, Georgia, serif">' + 'ABC'[p] + '</text>';
      pegs[p].forEach((dk, level) => {
        const w = 30 + (pegW - 50) * dk / maxDisk;
        const yy = base - (level + 1) * diskH;
        const shade = Math.round(58 + 28 * dk / maxDisk);
        out += '<rect x="' + (cx - w / 2) + '" y="' + yy + '" width="' + w + '" height="' + (diskH - 2) + '" fill="hsl(214, 55%, ' + shade + '%)"/>';
        if (diskH >= 14) out += '<text x="' + cx + '" y="' + (yy + diskH / 2 + 3) + '" text-anchor="middle" font-size="11" font-weight="600" fill="' + COLORS.ink + '" font-family="system-ui">' + dk + '</text>';
      });
    }
    out += '<line x1="10" x2="' + (W - 10) + '" y1="' + base + '" y2="' + base + '" stroke="' + COLORS.axis + '" stroke-width="2"/></svg>';
    $('#stage').innerHTML = out;
  }

  function drawStack(frames) {
    if (!frames || !frames.length) { $('#side-body').innerHTML = '<p class="hint">The call stack is empty.</p>'; return; }
    let html = '<ol class="frames">';
    for (let i = frames.length - 1; i >= 0; i--) {
      html += '<li class="' + (i === frames.length - 1 ? 'top' : '') + '">' + esc(frames[i]) + '<span class="depth">' + (i + 1) + '</span></li>';
    }
    html += '</ol><p class="hint" style="margin-top:.5rem">Top frame is the call running now. The number is its depth.</p>';
    $('#side-body').innerHTML = html;
  }

  // ------------------------------------------------------------------ data structures
  function cell(v, cls, small) {
    return '<div class="cell' + (cls ? ' ' + cls : '') + '">' + esc(v) + (small !== undefined ? '<small>' + esc(small) + '</small>' : '') + '</div>';
  }

  function drawDs(sn) {
    if (!sn) return;
    let html = '';
    switch (sn.kind) {
      case 'stack': html = dsStack(sn); break;
      case 'queue': html = dsQueue(sn); break;
      case 'list': html = dsList(sn); break;
      case 'tree': html = dsTree(sn); break;
      case 'heap': html = dsHeap(sn); break;
      case 'hash': html = dsHash(sn); break;
      case 'expr': html = dsExpr(sn); break;
    }
    $('#stage').innerHTML = html;
  }

  function textTrack(text, pos) {
    let h = '<div class="text-track">';
    for (let i = 0; i < text.length; i++) {
      const c = text[i] === ' ' ? '&nbsp;' : esc(text[i]);
      h += i === pos ? '<span class="cur">' + c + '</span>' : (i < pos ? '<span class="done">' + c + '</span>' : c);
    }
    return h + '</div>';
  }

  function dsStack(sn) {
    let html = '';
    if (sn.extra) html += '<div class="ds-block"><p class="caption">Scanning the text</p>' + textTrack(sn.extra.text, sn.extra.pos) + '</div>';
    let col = '';
    for (let i = 0; i < sn.items.length; i++) col += cell(sn.items[i], i === sn.items.length - 1 ? 'top' : '', 'index ' + i + (i === sn.items.length - 1 ? ', top' : ''));
    for (let i = sn.items.length; i < sn.cap; i++) col += cell('free', 'empty-slot', 'index ' + i);
    html += '<div class="ds-block"><p class="caption">Array of capacity ' + sn.cap + ' holding ' + sn.items.length + ' item(s). The top is drawn highest.</p>'
      + '<div class="stack-col">' + col + '</div></div>';
    return html;
  }

  function dsQueue(sn) {
    let logical = '<span class="caption">front</span>';
    sn.items.forEach((v, i) => { logical += (i ? '<span class="arrow">←</span>' : '') + cell(v, i === 0 ? 'hi' : ''); });
    if (!sn.items.length) logical += '<span class="hint">(empty)</span>';
    logical += '<span class="caption">rear</span>';
    let ring = '';
    sn.slots.forEach((v, i) => {
      const marks = (i === sn.front && sn.size > 0 ? 'front ' : '') + (i === sn.rear ? 'rear' : '');
      ring += cell(v === null ? '–' : v, v === null ? 'empty-slot' : (i === sn.front ? 'hi' : ''), 'slot ' + i + (marks ? ': ' + marks.trim() : ''));
    });
    return '<div class="ds-block"><p class="caption">Queue order (dequeue takes from the front)</p><div class="ds-row">' + logical + '</div></div>'
      + '<div class="ds-block"><p class="caption">The ring buffer underneath: front = ' + sn.front + ', rear (next free slot) = ' + sn.rear
      + ', size ' + sn.size + ' of ' + sn.cap + '</p><div class="ds-row">' + ring + '</div></div>';
  }

  function dsList(sn) {
    const hi = sn.hi ? sn.hi[0] : -1;
    let row = '<span class="caption">head</span><span class="arrow">→</span>';
    sn.items.forEach((v, i) => { row += cell(v, i === hi ? 'hi' : '', 'pos ' + i) + '<span class="arrow">→</span>'; });
    row += '<span class="caption">null</span>';
    return '<div class="ds-block"><p class="caption">' + sn.items.length + ' node(s); each node stores a value and a pointer to the next node</p><div class="ds-row">' + row + '</div></div>';
  }

  // generic binary tree drawing; node = {label, l, r, path, last}
  function treeSvg(root, emptyText) {
    if (!root) return '<p class="empty">' + emptyText + '</p>';
    let count = 0, maxD = 0;
    function place(n, d) {
      if (!n) return;
      place(n.l, d + 1);
      n.x = count++; n.d = d;
      if (d > maxD) maxD = d;
      place(n.r, d + 1);
    }
    place(root, 0);
    const colW = 46, rowH = 62;
    const W = Math.max(count * colW, 200), H = (maxD + 1) * rowH + 10;
    let edges = '', nodes = '';
    function draw(n) {
      if (!n) return;
      const x = n.x * colW + colW / 2, y = n.d * rowH + 26;
      [n.l, n.r].forEach((c) => {
        if (!c) return;
        edges += '<line x1="' + x + '" y1="' + y + '" x2="' + (c.x * colW + colW / 2) + '" y2="' + (c.d * rowH + 26) + '" stroke="#9aa6b5" stroke-width="1.5"/>';
        draw(c);
      });
      let fill = '#fff', stroke = COLORS.barIn;
      if (n.path) { fill = '#e3eefb'; stroke = COLORS.compare; }
      if (n.last) { fill = '#efedfb'; stroke = COLORS.pivot; }
      nodes += '<circle cx="' + x + '" cy="' + y + '" r="19" fill="' + fill + '" stroke="' + stroke + '" stroke-width="2"/>'
        + '<text x="' + x + '" y="' + (y + 4.5) + '" text-anchor="middle" font-size="13" font-weight="600" fill="' + COLORS.ink + '" font-family="system-ui, sans-serif">' + esc(n.label) + '</text>';
      if (n.sub !== undefined) nodes += '<text x="' + x + '" y="' + (y + 32) + '" text-anchor="middle" font-size="10" fill="' + COLORS.ink3 + '" font-family="system-ui">' + n.sub + '</text>';
    }
    draw(root);
    return '<svg viewBox="0 0 ' + W + ' ' + H + '" style="max-width:' + W * 1.4 + 'px" aria-hidden="true">' + edges + nodes + '</svg>';
  }

  function dsTree(sn) {
    const path = {};
    (sn.path || []).forEach((k) => { path[k] = true; });
    const last = sn.path && sn.path.length ? sn.path[sn.path.length - 1] : null;
    function conv(n) { return n ? { label: n.k, l: conv(n.l), r: conv(n.r), path: !!path[n.k], last: n.k === last } : null; }
    let html = '<div class="ds-block">' + treeSvg(conv(sn.root), 'The tree is empty.')
      + key([['Visited by this operation', COLORS.compare], ['Where it ended', COLORS.pivot]]) + '</div>';
    if (sn.out) html += '<div class="ds-block"><p class="caption">Traversal output</p><div class="ds-row">' + sn.out.map((v) => cell(v)).join('') + '</div></div>';
    return html;
  }

  function dsHeap(sn) {
    const hi = {};
    (sn.hi || []).forEach((k) => { hi[k] = true; });
    const lastHi = sn.hi && sn.hi.length ? sn.hi[sn.hi.length - 1] : -1;
    function build(i) {
      if (i >= sn.items.length) return null;
      return { label: sn.items[i], l: build(2 * i + 1), r: build(2 * i + 2), path: !!hi[i], last: i === lastHi, sub: '[' + i + ']' };
    }
    const arr = sn.items.map((v, i) => cell(v, hi[i] ? 'hi' : '', i)).join('');
    return '<div class="ds-block">' + treeSvg(build(0), 'The heap is empty.') + '</div>'
      + '<div class="ds-block"><p class="caption">The same heap as an array: children of index i are 2i+1 and 2i+2</p><div class="ds-row">'
      + (arr || '<span class="hint">(empty)</span>') + '</div>'
      + key([['Moved by this operation', COLORS.compare], ['Final position', COLORS.pivot]]) + '</div>';
  }

  function dsHash(sn) {
    let html = '<div class="ds-block"><p class="caption">' + sn.size + ' key(s) in ' + sn.buckets.length + ' buckets, load factor '
      + (sn.size / sn.buckets.length).toFixed(2) + '</p>';
    sn.buckets.forEach((chain, i) => {
      let row = '';
      chain.forEach((e, j) => { row += (j ? '<span class="arrow">→</span>' : '') + cell(e.k + ': ' + e.v, i === sn.hi ? 'hi' : ''); });
      html += '<div class="bucket' + (i === sn.hi ? ' hi' : '') + '"><span class="idx">' + i + '</span><div class="ds-row">'
        + (row || '<span class="hint">empty</span>') + '</div></div>';
    });
    return html + '</div>';
  }

  function dsExpr(sn) {
    const stackName = sn.phase === 'convert' ? 'Operator stack' : 'Value stack';
    let st = sn.stack.map((v, i) => cell(v === 'u-' ? 'neg' : v, i === sn.stack.length - 1 ? 'top' : '')).join('');
    if (!st) st = '<span class="hint">(empty)</span>';
    const out = sn.output.map((v) => cell(v === 'u-' ? 'neg' : v)).join('') || '<span class="hint">(empty)</span>';
    return '<div class="ds-block"><p class="caption">' + (sn.phase === 'convert' ? 'Phase 1: infix to postfix (shunting-yard)' : 'Phase 2: evaluate the postfix expression') + '</p></div>'
      + '<div class="ds-block"><p class="caption">' + stackName + ' (bottom on the left, top on the right)</p><div class="ds-row">' + st + '</div></div>'
      + '<div class="ds-block"><p class="caption">Postfix output</p><div class="ds-row">' + out + '</div></div>';
  }

  // ------------------------------------------------------------------ complexity analysis
  const DEFAULT_SIZES = { arrays: '500, 1000, 2000, 4000, 8000', recursion: '5, 10, 15, 20, 25' };

  $$('input[name="mode"]').forEach((r) => r.addEventListener('change', () => {
    const mode = $('input[name="mode"]:checked').value;
    $$('.checks').forEach((c) => { c.hidden = c.dataset.mode !== mode; });
    $('#an-sizes').value = DEFAULT_SIZES[mode];
    $('#an-input-field').hidden = mode !== 'arrays';
  }));

  async function runAnalysis(form) {
    const mode = $('input[name="mode"]:checked').value;
    const picked = $$('.checks[data-mode="' + mode + '"] input:checked').map((c) => c.value);
    const out = $('#analysis-out');
    if (!picked.length) { out.innerHTML = '<div class="error">Pick at least one algorithm to measure.</div>'; return; }
    if (picked.length > 8) { out.innerHTML = '<div class="error">Pick at most 8 algorithms so every line stays distinguishable.</div>'; return; }
    const body = new URLSearchParams();
    body.set('algos', picked.join(' '));
    body.set('sizes', $('#an-sizes').value);
    body.set('input', mode === 'arrays' ? $('#an-input').value : 'random');
    body.set('trials', $('#an-trials').value);
    const btn = form.querySelector('.run');
    btn.disabled = true;
    btn.textContent = 'Measuring…';
    out.innerHTML = '<p class="loading">Running every algorithm at every size. Large inputs for O(n²) sorts can take several seconds.</p>';
    try {
      const data = await post('analyze', body);
      renderAnalysis(data, mode);
    } catch (err) {
      out.innerHTML = '<div class="error">' + esc(err.message) + '</div>';
    } finally {
      btn.disabled = false;
      btn.textContent = 'Run analysis';
    }
  }

  function matchFit(expected, fit) {
    if (!fit || fit === '?') return false;
    if (expected.indexOf('ⁿ') >= 0) return fit === '2ⁿ';
    if (expected === 'n + k') return fit === 'n' || fit === 'n log n';
    if (expected.indexOf(' to ') >= 0) return fit === 'n' || fit === 'n log n' || fit === 'n²';
    if (expected === 'n log n') return fit === 'n log n' || fit === 'n';
    return expected.indexOf(fit) === 0 && (expected.length === fit.length || expected[fit.length] === ' ');
  }

  function renderAnalysis(data, mode) {
    const out = $('#analysis-out');
    const series = data.series.map((s) => ({ key: s.key, name: s.name, color: SERIES_COLOR[s.key] || '#5b6b80', s }));
    const inputName = { random: 'random', sorted: 'already sorted', reversed: 'reversed', nearly: 'nearly sorted', few: 'few distinct values' }[data.input];
    let html = '<div class="chart-tools"><label><input type="checkbox" id="logaxes"> Logarithmic axes (straight lines; the slope is the exponent)</label>'
      + (mode === 'arrays' ? '<span>Input: ' + esc(inputName) + ', median of ' + data.trials + ' timed run(s)</span>' : '<span>Median of ' + data.trials + ' timed run(s)</span>') + '</div>';
    html += '<div class="charts"><div class="chart-card" id="chart-ops"></div><div class="chart-card" id="chart-time"></div></div>';

    // summary table
    html += '<div class="table-wrap"><table class="summary-table"><thead><tr><th>Algorithm</th><th>Theory for this input</th><th>Best fit to your data</th>'
      + '<th class="num">Growth exponent (operations)</th><th class="num">Growth exponent (time)</th><th class="num">Work ratio when n last grew</th><th>Not run</th></tr></thead><tbody>';
    series.forEach((x) => {
      const s = x.s, pts = s.points;
      let ratio = '–';
      if (pts.length >= 2) {
        const a = pts[pts.length - 2], b = pts[pts.length - 1];
        ratio = (b.ops / Math.max(a.ops, 1)).toFixed(2) + '× for n ×' + (b.n / a.n).toFixed(2);
      }
      const ok = matchFit(s.expected, s.fit);
      html += '<tr><td><span class="swatch" style="background:' + x.color + '"></span>' + esc(s.name) + '</td>'
        + '<td>O(' + esc(s.expected) + ')</td>'
        + '<td class="' + (ok ? 'verdict-ok' : 'verdict-diff') + '">O(' + esc(s.fit) + ')' + (ok ? ' matches' : ' differs') + '</td>'
        + '<td class="num">' + (s.slopeOps === null ? '–' : s.slopeOps.toFixed(2)) + '</td>'
        + '<td class="num">' + (s.slopeTime === null ? '–' : s.slopeTime.toFixed(2)) + '</td>'
        + '<td class="num">' + ratio + '</td>'
        + '<td>' + (s.skipped.length ? 'n = ' + s.skipped.map(fmt).join(', ') + ' (limit ' + fmt(s.limit) + ')' : '') + '</td></tr>';
    });
    html += '</tbody></table></div>';
    html += '<p class="analysis-note">The growth exponent is the slope of log(operations) against log(n): about 1 means linear, about 1.1 to 1.3 means n log n over these sizes, about 2 means quadratic. '
      + 'For exponential algorithms the exponent keeps rising as n grows, because no power of n fits them. Operation counts are exact and repeatable; times include noise from the JIT compiler and the rest of your computer, so judge growth by operations first.</p>';

    // data table
    let sizes = [];
    series.forEach((x) => x.s.points.forEach((p) => { if (sizes.indexOf(p.n) < 0) sizes[sizes.length] = p.n; }));
    sizes = insertionSorted(sizes);
    html += '<details class="data"><summary>Show every measurement as a table</summary><div class="table-wrap"><table class="data-table"><thead><tr><th>n</th>'
      + series.map((x) => '<th>' + esc(x.name) + '</th>').join('') + '</tr></thead><tbody>';
    sizes.forEach((n) => {
      html += '<tr><td class="num">' + fmt(n) + '</td>';
      series.forEach((x) => {
        const p = x.s.points.filter((q) => q.n === n)[0];
        html += '<td class="num">' + (p ? fmt(p.ops) + ' ops<br>' + fmtTime(p.ns) : 'not run') + '</td>';
      });
      html += '</tr>';
    });
    html += '</tbody></table></div></details>';
    out.innerHTML = html;

    const draw = () => {
      const log = $('#logaxes').checked;
      lineChart($('#chart-ops'), series, 'ops', { title: 'Operations performed', sub: 'Exact count of comparisons, swaps, writes, arithmetic and calls', log, fmtY: fmt });
      lineChart($('#chart-time'), series, 'ns', { title: 'Running time', sub: 'Median measured time', log, fmtY: fmtTime });
    };
    $('#logaxes').addEventListener('change', draw);
    draw();
  }

  // small insertion sort for chart layout (kept by hand, like everything else)
  function insertionSorted(arr, keyFn) {
    const a = arr.slice();
    const k = keyFn || ((v) => v);
    for (let i = 1; i < a.length; i++) {
      const cur = a[i];
      let j = i - 1;
      while (j >= 0 && k(a[j]) > k(cur)) { a[j + 1] = a[j]; j--; }
      a[j + 1] = cur;
    }
    return a;
  }

  function niceTicks(min, max, count) {
    if (max <= min) max = min + 1;
    const span = max - min;
    const step0 = Math.pow(10, Math.floor(Math.log10(span / count)));
    const err = span / count / step0;
    const step = step0 * (err >= 7.5 ? 10 : err >= 3.5 ? 5 : err >= 1.5 ? 2 : 1);
    // keep adding ticks until one reaches or passes max, so every point fits inside the plot
    const ticks = [];
    for (let v = Math.ceil(min / step) * step; ; v += step) {
      ticks[ticks.length] = v;
      if (v >= max - step * 0.001) break;
    }
    return ticks;
  }

  function lineChart(card, series, field, opt) {
    const direct = series.length <= 4;
    const W = 600, H = 340, m = { l: 66, r: direct ? 130 : 18, t: 26, b: 44 };
    const pw = W - m.l - m.r, ph = H - m.t - m.b;
    let xmin = Infinity, xmax = -Infinity, ymin = Infinity, ymax = 0;
    series.forEach((x) => x.s.points.forEach((p) => {
      const v = p[field];
      if (p.n < xmin) xmin = p.n;
      if (p.n > xmax) xmax = p.n;
      if (v > ymax) ymax = v;
      if (v > 0 && v < ymin) ymin = v;
    }));
    if (!isFinite(xmin)) { card.innerHTML = '<h3>' + esc(opt.title) + '</h3><p class="empty">No sizes were within the limits for these algorithms.</p>'; return; }
    if (xmax === xmin) xmax = xmin + 1;
    if (!isFinite(ymin)) ymin = 1;
    if (ymax <= 0) ymax = 1;

    let sx, sy, xt, yt;
    if (opt.log) {
      const lx0 = Math.log10(xmin), lx1 = Math.log10(xmax);
      const ly0 = Math.floor(Math.log10(ymin)), ly1 = Math.ceil(Math.log10(ymax)) || 1;
      sx = (v) => m.l + (Math.log10(v) - lx0) / ((lx1 - lx0) || 1) * pw;
      sy = (v) => m.t + ph - (Math.log10(Math.max(v, Math.pow(10, ly0))) - ly0) / ((ly1 - ly0) || 1) * ph;
      // x ticks: the measured sizes themselves
      const seen = [];
      series.forEach((x) => x.s.points.forEach((p) => { if (seen.indexOf(p.n) < 0) seen[seen.length] = p.n; }));
      xt = insertionSorted(seen);
      yt = [];
      for (let e = ly0; e <= ly1; e++) yt[yt.length] = Math.pow(10, e);
    } else {
      xt = niceTicks(0, xmax, 5);
      const x1 = xt[xt.length - 1];
      sx = (v) => m.l + v / x1 * pw;
      yt = niceTicks(0, ymax, 5);
      const y1 = yt[yt.length - 1];
      sy = (v) => m.t + ph - v / y1 * ph;
    }

    let svg = '<svg viewBox="0 0 ' + W + ' ' + H + '" role="img" aria-label="' + esc(opt.title) + ' by input size">';
    yt.forEach((v) => {
      const yy = sy(v);
      svg += '<line x1="' + m.l + '" x2="' + (m.l + pw) + '" y1="' + yy + '" y2="' + yy + '" stroke="' + COLORS.grid + '" stroke-width="1"/>'
        + '<text x="' + (m.l - 8) + '" y="' + (yy + 4) + '" text-anchor="end" font-size="11" fill="' + COLORS.ink3 + '" font-family="system-ui">'
        + esc(opt.fmtY === fmtTime ? fmtTime(v) : fmtShort(v)) + '</text>';
    });
    xt.forEach((v) => {
      const xx = sx(v);
      svg += '<line x1="' + xx + '" x2="' + xx + '" y1="' + (m.t + ph) + '" y2="' + (m.t + ph + 5) + '" stroke="' + COLORS.axis + '"/>'
        + '<text x="' + xx + '" y="' + (m.t + ph + 18) + '" text-anchor="middle" font-size="11" fill="' + COLORS.ink3 + '" font-family="system-ui">' + fmtShort(v) + '</text>';
    });
    svg += '<line x1="' + m.l + '" x2="' + (m.l + pw) + '" y1="' + (m.t + ph) + '" y2="' + (m.t + ph) + '" stroke="' + COLORS.axis + '"/>';
    svg += '<text x="' + (m.l + pw / 2) + '" y="' + (H - 6) + '" text-anchor="middle" font-size="12" fill="' + COLORS.ink + '" font-family="system-ui">input size n</text>';

    const ends = [];
    series.forEach((x) => {
      const pts = x.s.points.filter((p) => !opt.log || p[field] > 0);
      if (!pts.length) return;
      let d = '';
      pts.forEach((p, i) => { d += (i ? 'L' : 'M') + sx(p.n).toFixed(1) + ' ' + sy(p[field]).toFixed(1); });
      svg += '<path d="' + d + '" fill="none" stroke="' + x.color + '" stroke-width="2" stroke-linejoin="round" stroke-linecap="round"/>';
      pts.forEach((p) => {
        svg += '<circle cx="' + sx(p.n).toFixed(1) + '" cy="' + sy(p[field]).toFixed(1) + '" r="4" fill="' + x.color + '" stroke="#fff" stroke-width="2"/>';
      });
      const last = pts[pts.length - 1];
      ends[ends.length] = { name: x.name, color: x.color, x: sx(last.n), y: sy(last[field]) };
    });

    if (direct && ends.length) {
      // spread labels at least 15px apart, keeping them inside the plot area
      const sorted = insertionSorted(ends, (e) => e.y);
      const lowest = m.t + ph - 2;
      for (let i = 1; i < sorted.length; i++) if (sorted[i].y - sorted[i - 1].y < 15) sorted[i].y = sorted[i - 1].y + 15;
      if (sorted[sorted.length - 1].y > lowest) {
        sorted[sorted.length - 1].y = lowest;
        for (let i = sorted.length - 2; i >= 0; i--) if (sorted[i + 1].y - sorted[i].y < 15) sorted[i].y = sorted[i + 1].y - 15;
      }
      sorted.forEach((e) => {
        svg += '<line x1="' + (m.l + pw + 6) + '" x2="' + (m.l + pw + 16) + '" y1="' + e.y + '" y2="' + e.y + '" stroke="' + e.color + '" stroke-width="3" stroke-linecap="round"/>'
          + '<text x="' + (m.l + pw + 20) + '" y="' + (e.y + 4) + '" font-size="11.5" fill="' + COLORS.ink + '" font-family="system-ui">' + esc(shortName(e.name)) + '</text>';
      });
    }
    svg += '<line class="xhair" x1="0" x2="0" y1="' + m.t + '" y2="' + (m.t + ph) + '" stroke="' + COLORS.axis + '" stroke-dasharray="3 3" visibility="hidden"/>';
    svg += '<rect class="hit" x="' + m.l + '" y="' + m.t + '" width="' + pw + '" height="' + ph + '" fill="transparent"/>';
    svg += '</svg>';

    const legend = series.length >= 2 ? '<div class="legend">' + series.map((x) => '<span><i style="background:' + x.color + '"></i>' + esc(x.name) + '</span>').join('') + '</div>' : '';
    card.innerHTML = '<h3>' + esc(opt.title) + '</h3><p class="sub">' + esc(opt.sub) + (opt.log ? ', log–log axes' : '') + '</p>' + legend + svg
      + '<div class="tooltip" hidden></div>';

    // hover: snap to the nearest measured size and list every series at that n
    const svgEl = card.querySelector('svg'), tip = card.querySelector('.tooltip'), hair = card.querySelector('.xhair');
    const allN = [];
    series.forEach((x) => x.s.points.forEach((p) => { if (allN.indexOf(p.n) < 0) allN[allN.length] = p.n; }));
    card.querySelector('.hit').addEventListener('mousemove', (ev) => {
      const r = svgEl.getBoundingClientRect();
      const px = (ev.clientX - r.left) / r.width * W;
      let best = allN[0], bd = Infinity;
      allN.forEach((n) => { const dd = Math.abs(sx(n) - px); if (dd < bd) { bd = dd; best = n; } });
      const xx = sx(best);
      hair.setAttribute('x1', xx); hair.setAttribute('x2', xx); hair.setAttribute('visibility', 'visible');
      let rows = '';
      series.forEach((x) => {
        const p = x.s.points.filter((q) => q.n === best)[0];
        rows += '<div class="row"><i style="background:' + x.color + '"></i><span>' + esc(shortName(x.name)) + '</span><span>' + (p ? opt.fmtY(p[field]) : 'not run') + '</span></div>';
      });
      tip.innerHTML = '<b>n = ' + fmt(best) + '</b>' + rows;
      tip.hidden = false;
      const cr = card.getBoundingClientRect();
      let left = (xx / W) * r.width + (r.left - cr.left) + 14;
      if (left + 200 > cr.width) left -= 230;
      tip.style.left = left + 'px';
      tip.style.top = (r.top - cr.top + 20) + 'px';
    });
    card.querySelector('.hit').addEventListener('mouseleave', () => { tip.hidden = true; hair.setAttribute('visibility', 'hidden'); });
  }

  function shortName(name) {
    return name.replace(' (iterative)', '').replace('Fibonacci (naive recursion)', 'Fib naive').replace('Fibonacci (memoised recursion)', 'Fib memoised');
  }
})();
