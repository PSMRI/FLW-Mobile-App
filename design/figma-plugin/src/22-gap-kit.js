// ---------------------------------------------------------------------------
// Gap-fix kit (2026-11-05): shared patterns for functionality found in code but
// missing from the design (docs/DESIGN_FUNCTIONALITY_GAPS.md).
// Use: const G = GK(ctx);  (G includes everything from SK(ctx))
// ---------------------------------------------------------------------------
Object.assign(EXTRA_ICONS, {
  mic: 'M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z',
  delete: 'M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z',
  sort: 'M3 18h6v-2H3v2zM3 6v2h18V6H3zm0 7h12v-2H3v2z',
  visibility: 'M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z',
  download: 'M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z',
  history: 'M13 3a9 9 0 0 0-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42A8.954 8.954 0 0 0 13 21a9 9 0 0 0 0-18zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z',
  filter: 'M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z',
  logout: 'M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z',
  pdf: 'M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z',
  image: 'M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z',
  eye_surgery: 'M12 6.5c3.79 0 7.17 2.13 8.82 5.5-1.65 3.37-5.02 5.5-8.82 5.5S4.83 15.37 3.18 12C4.83 8.63 8.21 6.5 12 6.5m0-2C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zm0 5a2.5 2.5 0 0 1 0 5 2.5 2.5 0 0 1 0-5m0-2c-2.48 0-4.5 2.02-4.5 4.5s2.02 4.5 4.5 4.5 4.5-2.02 4.5-4.5-2.02-4.5-4.5-4.5z',
  pill: 'M4.22 11.29l7.07-7.07a5 5 0 0 1 7.07 7.07l-7.07 7.07a5 5 0 0 1-7.07-7.07zm1.42 1.42a3 3 0 0 0 4.24 4.24l3.54-3.54-4.24-4.24-3.54 3.54z',
  refresh: 'M17.65 6.35A7.958 7.958 0 0 0 12 4a8 8 0 1 0 7.73 10h-2.08A6 6 0 1 1 12 6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z',
});

// Adds a trailing mic (voice search) to every Search bar variant. Idempotent; prop "Show mic" (default true)
// so every existing search bar instance in the file gets the mic the code already has.
async function ensureSearchMic(ctx) {
  const set = ctx.N.searchBar; if (!set) return;
  const owner = set.type === 'COMPONENT_SET' ? set : set;
  const defs = owner.componentPropertyDefinitions || {};
  if (Object.keys(defs).some((k) => k.startsWith('Show mic'))) return;
  const variants = owner.type === 'COMPONENT_SET' ? owner.children : [owner];
  const ink = ctx.V['color/on-surface-variant'];
  const made = [];
  for (const v of variants) {
    const mic = ctx.IC.mic.createInstance(); mic.name = 'Mic'; mic.resize(22, 22);
    const g = mic.findOne((n) => n.name === 'glyph'); if (g) g.fills = [figma.variables.setBoundVariableForPaint({ type: 'SOLID', color: { r: 0, g: 0, b: 0 } }, 'color', ink)];
    const host = v.layoutMode ? v : (v.findOne((n) => n.type === 'FRAME' && n.layoutMode === 'HORIZONTAL') || v);
    host.appendChild(mic); made.push(mic);
  }
  const k = owner.addComponentProperty('Show mic', 'BOOLEAN', true);
  for (const m of made) m.componentPropertyReferences = { visible: k };
}

function GK(ctx) {
  const S = SK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, icon, inst, kinst, setP, fixW, scrim, sheet, pinBottom, overlayCenter, note, fitSection } = S;

  // ---- small pieces
  const row = (p, name = 'Row', g = 8) => { const r = AL('HORIZONTAL', name); r.itemSpacing = g; r.counterAxisAlignItems = 'CENTER'; if (p) add(p, r); return r; };
  const caption = async (p, s, color = 'color/on-surface-variant', style = 'Body/Small') => { const t = await txt(s, style, color); p.appendChild(t); wrapText(t); return t; };
  const tag = (p, tone, label) => { const t = inst('tag', `Tone=${tone}`, { Label: label }); p.appendChild(t); return t; };
  // try a button variant, fall back to Enabled + 40% opacity when the variant doesn't exist
  const btn = (p, style, label, disabled = false, fillW = false) => {
    let b;
    try { b = inst('button', `Style=${style}, State=${disabled ? 'Disabled' : 'Enabled'}`, { Label: label, 'Show icon': false }); }
    catch (e) { b = inst('button', `Style=${style}, State=Enabled`, { Label: label, 'Show icon': false }); if (disabled) b.opacity = 0.4; }
    p.appendChild(b); if (fillW) b.layoutSizingHorizontal = 'FILL'; return b;
  };
  // round icon button (call, delete, eye, ABHA ...)
  const iconBtn = (p, ic, tone = 'color/primary', label, disabled = false) => {
    const c = AL('VERTICAL', 'Icon action ' + ic); c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 2; p.appendChild(c);
    const b = AL('HORIZONTAL', 'Hit'); b.resize(40, 40); b.primaryAxisSizingMode = 'FIXED'; b.counterAxisSizingMode = 'FIXED'; b.primaryAxisAlignItems = 'CENTER'; b.counterAxisAlignItems = 'CENTER'; b.cornerRadius = 20; fill(b, 'color/surface-container-high'); c.appendChild(b);
    b.appendChild(icon(ic, tone, 22));
    if (disabled) c.opacity = 0.35;
    return { c, b, label: label ? txt(label, 'Label/Small', 'color/on-surface-variant').then((t) => { c.appendChild(t); return t; }) : null };
  };

  // ---- Generic card with everything the code cards have.
  // d = { name, meta, initials, tags:[[tone,label]], sync:'Synced'|'Pending'|'Failed', rows:[[label,value]],
  //       icons:[[icon, label, colorVar?, disabled?]], actions:[[style,label,disabled?]], state:'normal'|'deceased'|'deleted'|'readonly', stateNote }
  const actionCard = async (p, d) => {
    const c = AL('VERTICAL', 'Card · ' + d.name); pad(c, 'spacing/md', 'spacing/sm'); c.itemSpacing = 8; rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, d.state === 'deleted' || d.state === 'deceased' ? 'color/outline' : 'color/outline-variant'); add(p, c);
    const top = row(c, 'Header', 12);
    const initials = d.initials || d.name.split(' ').map((w) => w[0]).join('').slice(0, 2);
    top.appendChild(inst('avatar', null, { Initials: initials }));
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 2; top.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    col.appendChild(await txt(d.name, 'Title/Medium', 'color/on-surface'));
    if (d.meta) { const m = await txt(d.meta, 'Body/Small', 'color/on-surface-variant'); col.appendChild(m); wrapText(m); }
    if (d.sync) { try { top.appendChild(inst('syncBadge', null, { Status: d.sync })); } catch (e) { /* variant names differ: ignore */ } }
    const tags = (d.tags || []).slice();
    if (d.state === 'deceased') tags.unshift(['Neutral', d.stateNote || 'Deceased']);
    if (d.state === 'deleted') tags.unshift(['Neutral', d.stateNote || 'Deleted record']);
    if (tags.length) { const tr = AL('HORIZONTAL', 'Tags'); tr.itemSpacing = 6; tr.layoutWrap = 'WRAP'; tr.counterAxisSpacing = 6; add(c, tr); for (const [tone, l] of tags) tag(tr, tone, l); }
    if (d.rows && d.rows.length) {
      const g = AL('HORIZONTAL', 'Values'); g.layoutWrap = 'WRAP'; g.itemSpacing = 8; g.counterAxisSpacing = 6; add(c, g);
      for (const [l, v] of d.rows) { const cell = AL('VERTICAL', 'Value'); cell.itemSpacing = 0; g.appendChild(cell); fixW(cell, 140); const a = await txt(l, 'Label/Small', 'color/on-surface-variant'); cell.appendChild(a); wrapText(a); const b = await txt(v, 'Body/Medium', 'color/on-surface'); cell.appendChild(b); wrapText(b); }
    }
    const blocked = d.state === 'deceased' || d.state === 'deleted';
    if ((d.icons && d.icons.length) || (d.actions && d.actions.length)) {
      const ar = row(c, 'Actions', 8); ar.layoutWrap = 'WRAP'; ar.counterAxisSpacing = 8;
      for (const [ic, label, color, dis] of d.icons || []) { const r = iconBtn(ar, ic, color || 'color/primary', label, blocked || dis); if (r.label) await r.label; }
      for (const [st, label, dis] of d.actions || []) btn(ar, st, label, blocked || dis);
    }
    if (blocked) c.opacity = 0.6;
    if (d.footnote) await caption(c, d.footnote);
    return c;
  };

  // ---- Dialog over a fixed-height phone frame (scrim + centred dialog). danger = destructive main action.
  const dialog = async (s, { ic = 'warning', icColor = 'color/warning', title, body, cancel = 'Cancel', ok = 'OK', danger = false, extra } = {}) => {
    scrim(s);
    const c = AL('VERTICAL', 'Dialog · ' + title); fixW(c, 312); pad(c, 'spacing/xl', 'spacing/xl'); gap(c, 'spacing/md'); rad(c, 'radius/xl'); fill(c, 'color/surface-container');
    if (ic) c.appendChild(icon(ic, danger ? 'color/error' : icColor, 28));
    const t = await txt(title, 'Title/Large', 'color/on-surface'); c.appendChild(t); wrapText(t);
    if (body) { const b = await txt(body, 'Body/Medium', 'color/on-surface-variant'); c.appendChild(b); wrapText(b); }
    if (extra) await extra(c);
    const acts = AL('HORIZONTAL', 'Actions'); acts.primaryAxisAlignItems = 'MAX'; gap(acts, 'spacing/xs'); add(c, acts);
    if (cancel) btn(acts, 'Text', cancel);
    if (ok) { const b = btn(acts, 'Filled', ok); if (danger) { const bg = b.findOne((n) => n.name === 'Container' || n.name === 'Background'); if (bg && 'fills' in bg) bg.fills = [paint('color/error')]; else b.fills = [paint('color/error')]; } }
    overlayCenter(s, c);
    return c;
  };

  // ---- Bottom sheet listing visits: visits = [{label, date, status, tone, action}] ; add = {label, disabled, reason}
  const visitSheet = async (s, { title, subtitle, visits = [], add: addBtn, footnote } = {}) => {
    scrim(s);
    const sh = sheet(s, 'Sheet · ' + title);
    const hd = AL('VERTICAL', 'Head'); hd.itemSpacing = 2; add(sh, hd);
    hd.appendChild(await txt(title, 'Title/Large', 'color/on-surface'));
    if (subtitle) await caption(hd, subtitle);
    for (const v of visits) {
      const r = AL('HORIZONTAL', 'Visit · ' + v.label); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 12; pad(r, 'spacing/md', 'spacing/sm'); rad(r, 'radius/md'); fill(r, 'color/surface'); stroke(r, 'color/outline-variant'); add(sh, r);
      const col = AL('VERTICAL', 'Text'); col.itemSpacing = 2; r.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      col.appendChild(await txt(v.label, 'Title/Small', 'color/on-surface'));
      if (v.date) await caption(col, v.date);
      if (v.status) tag(r, v.tone || 'Neutral', v.status);
      if (v.action) btn(r, v.actionStyle || 'Text', v.action, !!v.disabled);
    }
    if (addBtn) {
      const b = btn(sh, 'Filled', addBtn.label, !!addBtn.disabled, true);
      if (addBtn.reason) { const r = row(sh, 'Blocked reason', 6); r.appendChild(icon('lock', 'color/on-surface-variant', 16)); const t = await txt(addBtn.reason, 'Body/Small', 'color/on-surface-variant'); r.appendChild(t); wrapText(t); }
    }
    if (footnote) await caption(sh, footnote);
    pinBottom(s, sh);
    return sh;
  };

  // ---- Option sheet (sort, source chooser, filters): options = [label] or [[icon,label]]
  const optionSheet = async (s, title, options, selIdx = -1, opts = {}) => {
    scrim(s);
    const sh = sheet(s, 'Sheet · ' + title);
    const t = await txt(title, 'Title/Large', 'color/on-surface'); add(sh, t);
    if (opts.subtitle) await caption(sh, opts.subtitle);
    for (let i = 0; i < options.length; i++) {
      const o = options[i];
      if (Array.isArray(o)) { const r = row(sh, 'Option', 16); pad(r, 'spacing/md', 'spacing/sm'); r.appendChild(icon(o[0], 'color/primary', 24)); const l = await txt(o[1], 'Body/Large', 'color/on-surface'); r.appendChild(l); wrapText(l); if (o[2]) { const h = await txt(o[2], 'Body/Small', 'color/on-surface-variant'); r.appendChild(h); } }
      else add(sh, kinst('Radio option', `Selected=${i === selIdx}`, { Label: o }));
    }
    if (opts.actions) { const ar = AL('HORIZONTAL', 'Actions'); ar.primaryAxisAlignItems = 'MAX'; ar.itemSpacing = 8; add(sh, ar); for (const [st, l] of opts.actions) btn(ar, st, l); }
    pinBottom(s, sh);
    return sh;
  };
  const sortSheet = (s, options = ['Name A–Z', 'Name Z–A', 'Newest first', 'Oldest first', 'Due date'], sel = 0) => optionSheet(s, 'Sort by', options, sel, { actions: [['Text', 'Reset'], ['Filled', 'Apply']] });
  const sourceSheet = (s, rules = 'Max 5 MB per file') => optionSheet(s, 'Add file', [['camera', 'Take photo'], ['image', 'Choose from gallery'], ['pdf', 'Choose PDF']], -1, { subtitle: rules });

  // ---- Search row: search bar (with mic) + optional sort / filter icons
  const searchRow = (p, placeholder, { sort = false, filter = false } = {}) => {
    const r = row(p, 'Search row', 8);
    const sb = inst('searchBar', null, { Placeholder: placeholder }); r.appendChild(sb); sb.layoutSizingHorizontal = 'FILL';
    if (sort) iconBtn(r, 'sort', 'color/on-surface-variant');
    if (filter) iconBtn(r, 'filter', 'color/on-surface-variant');
    return r;
  };

  // ---- View mode (record saved): info banner + Edit button or locked reason
  const viewMode = async (p, { saved = 'Saved on 12 Oct 2026', editable = true, reason } = {}) => {
    const c = AL('HORIZONTAL', 'View mode bar'); c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 8; pad(c, 'spacing/md', 'spacing/sm'); rad(c, 'radius/md'); fill(c, 'color/primary-subtle'); add(p, c);
    c.appendChild(icon(editable ? 'visibility' : 'lock', 'color/primary', 20));
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 0; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    col.appendChild(await txt('View only · ' + saved, 'Label/Large', 'color/on-surface'));
    if (reason) { const t = await txt(reason, 'Body/Small', 'color/on-surface-variant'); col.appendChild(t); wrapText(t); }
    if (editable) btn(c, 'Tonal', 'Edit');
    return c;
  };

  // ---- Disabled "Add" with the reason (frequency rules etc.)
  const blockedAdd = async (p, label, reason) => {
    const c = AL('VERTICAL', 'Blocked add'); c.itemSpacing = 6; add(p, c);
    btn(c, 'Filled', label, true, true);
    const r = row(c, 'Reason', 6); r.appendChild(icon('lock', 'color/on-surface-variant', 16)); const t = await txt(reason, 'Body/Small', 'color/on-surface-variant'); r.appendChild(t); wrapText(t);
    return c;
  };

  // ---- Upload block: files = [[name, state]] (state = Done|Uploading|Error if the File row variant exists)
  const uploads = async (p, title, files = [], rules = 'Camera, gallery or PDF · max 5 MB each') => {
    const c = AL('VERTICAL', 'Uploads · ' + title); c.itemSpacing = 8; add(p, c);
    const t = await txt(title, 'Label/Large', 'color/on-surface-variant'); c.appendChild(t); wrapText(t);
    for (const [n, st] of files) { let f; try { f = kinst('File row', `State=${st || 'Done'}`, { 'File name': n }); } catch (e) { f = kinst('File row', 'State=Done', { 'File name': n }); } add(c, f); }
    const r = row(c, 'Add file', 8); r.appendChild(icon('cloud_upload', 'color/primary', 20)); const a = await txt('Add file', 'Label/Large', 'color/primary'); r.appendChild(a);
    await caption(c, rules);
    return c;
  };

  // ---- Simple data table (history tables: IFA bottles, ANTRA doses, IRS rounds ...)
  const table = async (p, title, head, rows) => {
    const c = AL('VERTICAL', 'Table · ' + title); c.itemSpacing = 0; rad(c, 'radius/md'); stroke(c, 'color/outline-variant'); c.clipsContent = true; add(p, c);
    if (title) { const h = AL('HORIZONTAL', 'Title'); pad(h, 'spacing/sm', 'spacing/xs'); add(c, h); h.appendChild(await txt(title, 'Title/Small', 'color/on-surface')); }
    const mk = async (cells, header) => {
      const r = AL('HORIZONTAL', header ? 'Head' : 'Row'); pad(r, 'spacing/sm', 'spacing/xs'); r.itemSpacing = 6; add(c, r); if (header) fill(r, 'color/surface-container-high');
      for (const v of cells) { const t = await txt(String(v), header ? 'Label/Medium' : 'Body/Small', header ? 'color/on-surface-variant' : 'color/on-surface'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL'; t.textAutoResize = 'HEIGHT'; }
    };
    await mk(head, true); for (const r of rows) await mk(r, false);
    return c;
  };

  // ---- Rules: developer/PO notes attached under a frame. Collected; placed by finish().
  const RULES = [];
  const rules = (frame, lines) => { RULES.push([frame, Array.isArray(lines) ? lines : [lines]]); };

  // Layout frames row by row with their rule notes underneath, then fit the section.
  const finish = async (sec, frames, rowOf, top = 160) => {
    const notes = new Map();
    for (const [f, lines] of RULES) {
      if (!frames.includes(f)) continue;
      const n = await note(sec, f.x, f.y, 360, 'RULES (from code)\n' + lines.map((l) => '• ' + l).join('\n'));
      n.name = 'Rules · ' + f.name; notes.set(f, n);
    }
    const byRow = {};
    for (const f of frames) { const r = rowOf(f); (byRow[r] = byRow[r] || []).push(f); }
    let y = top;
    for (const r of Object.keys(byRow).map(Number).sort((a, b) => a - b)) {
      let h = 0;
      for (const f of byRow[r]) { f.y = y; let fh = f.height; const n = notes.get(f); if (n) { n.x = f.x; n.y = y + f.height + 48; fh += 48 + n.height; } h = Math.max(h, fh); }
      y += h + 200;
    }
    fitSection(sec);
    return y;
  };

  return Object.assign({}, S, { row, caption, tag, btn, iconBtn, actionCard, dialog, visitSheet, optionSheet, sortSheet, sourceSheet, searchRow, viewMode, blockedAdd, uploads, table, rules, finish });
}
