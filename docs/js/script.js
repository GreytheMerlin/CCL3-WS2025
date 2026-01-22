/* js/script.js */

/**
 * ✅ Data pasted from your CSVs (User data + Questionares + SUS).
 */
const participants = [
  { pid: "P1", gender: "Male", age: 24, appStructure: 4, puzzleAppropriate: "no",  firstLook: "adding an alarm",         susScore: 87.5 },
  { pid: "P2", gender: "Male", age: 22, appStructure: 3, puzzleAppropriate: "yes", firstLook: "Alarm",                    susScore: 100.0 },
  { pid: "P3", gender: "Male", age: 23, appStructure: 3, puzzleAppropriate: "yes", firstLook: "Alarm clock",              susScore: 100.0 },
  { pid: "P4", gender: "Male", age: 22, appStructure: 1, puzzleAppropriate: "yes", firstLook: "navigation",               susScore: 100.0 },
  { pid: "P5", gender: "Male", age: 21, appStructure: 1, puzzleAppropriate: "yes", firstLook: "How to set an alarm",      susScore: 82.5 },
  { pid: "P6", gender: "Male", age: 36, appStructure: 1, puzzleAppropriate: "yes", firstLook: "alarm how it looks like",  susScore: 97.5 }
];

// ---------- utilities ----------
const clamp = (v, a, b) => Math.max(a, Math.min(b, v));
const fmt = (n, d=1) => Number(n).toFixed(d);

function median(sorted) {
  const n = sorted.length;
  if (!n) return NaN;
  const mid = Math.floor(n / 2);
  return (n % 2 === 0) ? (sorted[mid - 1] + sorted[mid]) / 2 : sorted[mid];
}

function quartiles(values) {
  const v = values.slice().sort((a,b)=>a-b);
  if (!v.length) return null;

  const med = median(v);
  const mid = Math.floor(v.length / 2);
  const lower = v.slice(0, mid);
  const upper = (v.length % 2 === 0) ? v.slice(mid) : v.slice(mid + 1);

  const q1 = median(lower.length ? lower : v);
  const q3 = median(upper.length ? upper : v);

  const iqr = q3 - q1;
  const lowFence = q1 - 1.5 * iqr;
  const highFence = q3 + 1.5 * iqr;

  let wLow = v[0], wHigh = v[v.length - 1];
  for (let i = 0; i < v.length; i++) { if (v[i] >= lowFence) { wLow = v[i]; break; } }
  for (let i = v.length - 1; i >= 0; i--) { if (v[i] <= highFence) { wHigh = v[i]; break; } }

  const outliers = v.filter(x => x < wLow || x > wHigh);
  return { q1, med, q3, iqr, wLow, wHigh, outliers, sorted: v, min: v[0], max: v[v.length-1] };
}

function stats(values) {
  const v = values.map(Number).filter(x => !Number.isNaN(x));
  if (!v.length) return null;
  const min = Math.min(...v);
  const max = Math.max(...v);
  const mean = v.reduce((a,b)=>a+b,0) / v.length;
  return { n: v.length, min, max, mean };
}

// ---------- SVG helpers ----------
function svgEl(tag, attrs = {}, children = []) {
  const el = document.createElementNS("http://www.w3.org/2000/svg", tag);
  for (const [k, v] of Object.entries(attrs)) el.setAttribute(k, String(v));
  for (const c of children) el.appendChild(c);
  return el;
}

function clearSvg(svg) {
  while (svg.firstChild) svg.removeChild(svg.firstChild);
}

function textEl(x, y, str, opts = {}) {
  return svgEl("text", {
    x, y,
    fill: opts.fill ?? "#cbd5e1",
    "font-size": opts.size ?? 12,
    "font-weight": opts.weight ?? "400",
    "text-anchor": opts.anchor ?? "start",
    "dominant-baseline": opts.baseline ?? "alphabetic",
  }, [document.createTextNode(str)]);
}

function lineEl(x1,y1,x2,y2, opts={}) {
  return svgEl("line", {
    x1, y1, x2, y2,
    stroke: opts.stroke ?? "#1e293b",
    "stroke-width": opts.w ?? 2,
    "stroke-linecap": "round"
  });
}

function rectEl(x,y,w,h, opts={}) {
  return svgEl("rect", {
    x, y, width: w, height: h,
    rx: opts.rx ?? 10, ry: opts.ry ?? 10,
    fill: opts.fill ?? "rgba(79,70,229,0.22)",
    stroke: opts.stroke ?? "#4f46e5",
    "stroke-width": opts.sw ?? 2
  });
}

function circleEl(cx,cy,r, opts={}) {
  return svgEl("circle", {
    cx, cy, r,
    fill: opts.fill ?? "rgba(6,182,212,0.85)",
    stroke: opts.stroke ?? "#06b6d4",
    "stroke-width": opts.sw ?? 1
  });
}

// ---------- Chart Logic ----------
function drawSingleBoxplot(svgId, values, valueLabel, noteId, domainOverride=null) {
  const svg = document.getElementById(svgId);
  if (!svg) return; // Safely exit if chart doesn't exist on this page
  clearSvg(svg);

  const W = 760, H = 540;
  const pad = { l: 60, r: 20, t: 20, b: 44 };
  const plotW = W - pad.l - pad.r;

  const vals = values.map(Number).filter(x => !Number.isNaN(x));
  const q = quartiles(vals);
  if (!q) return;

  let minV = q.min, maxV = q.max;
  if (domainOverride) { minV = domainOverride[0]; maxV = domainOverride[1]; }
  const range = (maxV - minV) || 1;
  const minA = minV - range * 0.05;
  const maxA = maxV + range * 0.05;

  const xScale = (v) => pad.l + ((v - minA) / (maxA - minA)) * plotW;
  const y = 120;

  // grid + ticks
  const ticks = 6;
  for (let i = 0; i <= ticks; i++) {
    const v = minA + (i / ticks) * (maxA - minA);
    const x = xScale(v);
    svg.appendChild(lineEl(x, pad.t, x, H - pad.b, { stroke: "#111827", w: 1 }));
    svg.appendChild(textEl(x, H - 18, fmt(v, valueLabel === "App Structure (1–5)" ? 1 : 0), {
      anchor: "middle", size: 11, fill: "#94a3b8"
    }));
  }

  svg.appendChild(textEl(pad.l, 14, valueLabel, { size: 12, fill: "#94a3b8" }));

  // whisker
  svg.appendChild(lineEl(xScale(q.wLow), y, xScale(q.wHigh), y, { stroke: "#cbd5e1", w: 2 }));
  svg.appendChild(lineEl(xScale(q.wLow), y - 12, xScale(q.wLow), y + 12, { stroke: "#cbd5e1", w: 2 }));
  svg.appendChild(lineEl(xScale(q.wHigh), y - 12, xScale(q.wHigh), y + 12, { stroke: "#cbd5e1", w: 2 }));

  // box
  const boxH = 44;
  const x1 = xScale(q.q1);
  const x3 = xScale(q.q3);
  svg.appendChild(rectEl(x1, y - boxH/2, Math.max(2, x3 - x1), boxH, {
    fill: "rgba(79,70,229,0.22)",
    stroke: "#4f46e5",
    sw: 2,
    rx: 12,
    ry: 12
  }));

  // median
  svg.appendChild(lineEl(xScale(q.med), y - boxH/2, xScale(q.med), y + boxH/2, { stroke: "#06b6d4", w: 3 }));

  // points (jitter)
  vals.forEach((v, i) => {
    const jy = y + (Math.random() - 0.5) * 26;
    svg.appendChild(circleEl(xScale(v), jy, 5));
  });

  // outliers
  q.outliers.forEach(v => svg.appendChild(circleEl(xScale(v), y, 6, { fill: "rgba(229,231,235,0.9)", stroke: "#e5e7eb", sw: 1 })));

  // note
  const st = stats(vals);
  const noteEl = document.getElementById(noteId);
  if (noteEl && st) {
    noteEl.textContent = `n=${st.n} · mean=${fmt(st.mean, 1)} · median=${fmt(q.med, 1)} · min=${fmt(st.min, 1)} · max=${fmt(st.max, 1)}.`;
  }
}

function drawCountBars(svgId, countsObj, titleLabel, noteId) {
  const svg = document.getElementById(svgId);
  if (!svg) return;
  clearSvg(svg);

  const W = 760, H = 520;
  const pad = { l: 60, r: 20, t: 20, b: 54 };
  const plotW = W - pad.l - pad.r;
  const plotH = H - pad.t - pad.b;

  const labels = Object.keys(countsObj);
  const values = labels.map(k => countsObj[k]);
  const maxV = Math.max(...values, 1);

  // grid lines
  const ticks = 4;
  for (let i = 0; i <= ticks; i++) {
    const v = (i / ticks) * maxV;
    const y = pad.t + (1 - v / maxV) * plotH;
    svg.appendChild(lineEl(pad.l, y, pad.l + plotW, y, { stroke: "#111827", w: 1 }));
    svg.appendChild(textEl(12, y + 4, `${Math.round(v)}`, { size: 11, fill: "#94a3b8" }));
  }

  svg.appendChild(textEl(pad.l, 14, titleLabel, { size: 12, fill: "#94a3b8" }));

  const step = plotW / labels.length;
  const barW = Math.min(140, step * 0.6);

  labels.forEach((lab, i) => {
    const v = countsObj[lab];
    const x = pad.l + step * i + step / 2;
    const y0 = pad.t + plotH;
    const yV = pad.t + (1 - v / maxV) * plotH;

    svg.appendChild(rectEl(x - barW/2, yV, barW, Math.max(2, y0 - yV), {
      fill: "rgba(79,70,229,0.22)",
      stroke: "#4f46e5",
      sw: 2,
      rx: 12,
      ry: 12
    }));

    svg.appendChild(textEl(x, yV - 8, `${v}`, { anchor: "middle", size: 12, fill: "#e5e7eb", weight: "600" }));
    svg.appendChild(textEl(x, H - 22, lab, { anchor: "middle", size: 12, fill: "#cbd5e1" }));
  });

  const noteEl = document.getElementById(noteId);
  if (noteEl) noteEl.textContent = `Total responses: ${values.reduce((a,b)=>a+b,0)}.`;
}

function drawParticipantBars(svgId, participants, valueKey, labelKey, titleLabel, noteId) {
  const svg = document.getElementById(svgId);
  if (!svg) return;
  clearSvg(svg);

  const W = 760, H = 540;
  const pad = { l: 60, r: 20, t: 20, b: 54 };
  const plotW = W - pad.l - pad.r;
  const plotH = H - pad.t - pad.b;

  const labels = participants.map(p => p[labelKey]);
  const values = participants.map(p => Number(p[valueKey])).filter(x => !Number.isNaN(x));
  const maxV = Math.max(...values, 1);
  const minV = Math.min(...values, 0);
  const range = (maxV - minV) || 1;

  const minA = minV - range * 0.05;
  const maxA = maxV + range * 0.05;

  const yScale = (v) => pad.t + (1 - (v - minA) / (maxA - minA)) * plotH;

  // grid
  const ticks = 5;
  for (let i = 0; i <= ticks; i++) {
    const v = minA + (i / ticks) * (maxA - minA);
    const y = yScale(v);
    svg.appendChild(lineEl(pad.l, y, pad.l + plotW, y, { stroke: "#111827", w: 1 }));
    svg.appendChild(textEl(10, y + 4, fmt(v, 0), { size: 11, fill: "#94a3b8" }));
  }

  svg.appendChild(textEl(pad.l, 14, titleLabel, { size: 12, fill: "#94a3b8" }));

  const step = plotW / labels.length;
  const barW = Math.min(90, step * 0.6);

  participants.forEach((p, i) => {
    const v = Number(p[valueKey]);
    const x = pad.l + step * i + step / 2;
    const y0 = yScale(minA);
    const yV = yScale(v);

    svg.appendChild(rectEl(x - barW/2, yV, barW, Math.max(2, y0 - yV), {
      fill: "rgba(79,70,229,0.22)",
      stroke: "#4f46e5",
      sw: 2,
      rx: 12,
      ry: 12
    }));

    svg.appendChild(textEl(x, yV - 8, `${v}`, { anchor: "middle", size: 12, fill: "#e5e7eb", weight: "600" }));
    svg.appendChild(textEl(x, H - 22, p[labelKey], { anchor: "middle", size: 12, fill: "#cbd5e1" }));
  });

  const noteEl = document.getElementById(noteId);
  if (noteEl) {
    const st = stats(participants.map(p => p[valueKey]));
    noteEl.textContent = st ? `mean=${fmt(st.mean, 1)} · min=${fmt(st.min, 0)} · max=${fmt(st.max, 0)}.` : "";
  }
}

// ---------- DOM Updates ----------
function renderSummaryTable() {
  const tbody = document.getElementById("summary-table-body");
  if (!tbody) return;
  tbody.innerHTML = "";
  participants.forEach(p => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${p.pid}</td>
      <td>${p.gender}</td>
      <td>${p.age}</td>
      <td>${p.appStructure}</td>
      <td>${p.puzzleAppropriate.toUpperCase()}</td>
      <td>${fmt(p.susScore, 1)}</td>
    `;
    tbody.appendChild(tr);
  });
}

function renderFeedbackExcerpts() {
  const ul = document.getElementById("feedback-excerpts");
  if (!ul) return;
  ul.innerHTML = "";

  const excerpts = participants.map(p => p.firstLook).filter(Boolean);
  const unique = [];
  excerpts.forEach(x => {
    const norm = x.trim().toLowerCase();
    if (!unique.some(u => u.trim().toLowerCase() === norm)) unique.push(x.trim());
  });

  unique.slice(0, 5).forEach(x => {
    const li = document.createElement("li");
    li.textContent = `“${x}”`;
    ul.appendChild(li);
  });
}

function setDemographicsSummary() {
  const countEl = document.getElementById("participants-count");
  const genderEl = document.getElementById("participants-gender");
  const ageEl = document.getElementById("participants-age");

  const n = participants.length;
  const genders = {};
  participants.forEach(p => { genders[p.gender] = (genders[p.gender] || 0) + 1; });

  const ages = participants.map(p => p.age);
  const ageStats = stats(ages);

  if (countEl) countEl.textContent = `${n}`;
  if (genderEl) genderEl.textContent = Object.entries(genders).map(([g,c]) => `${g}: ${c}`).join(", ");
  if (ageEl && ageStats) ageEl.textContent = `mean ${fmt(ageStats.mean,1)}, min ${ageStats.min}, max ${ageStats.max}`;
}

// ---------- init (Run on load) ----------
(function init() {
  setDemographicsSummary();
  renderSummaryTable();
  renderFeedbackExcerpts();

  drawSingleBoxplot("svg-box-sus", participants.map(p => p.susScore), "SUS (0–100)", "note-box-sus", [0, 100]);
  drawSingleBoxplot("svg-box-structure", participants.map(p => p.appStructure), "App Structure (1–5)", "note-box-structure", [1, 5]);

  const puzzleCounts = { Yes: 0, No: 0 };
  participants.forEach(p => {
    if ((p.puzzleAppropriate || "").toLowerCase().startsWith("y")) puzzleCounts.Yes++;
    else puzzleCounts.No++;
  });
  drawCountBars("svg-bar-puzzle", puzzleCounts, "Responses", "note-bar-puzzle");

  drawParticipantBars("svg-bar-age", participants, "age", "pid", "Age", "note-bar-age");
})();
