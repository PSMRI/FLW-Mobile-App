// ---------------------------------------------------------------------------
// Screen-building helpers shared by module stages (2.2+)
// ---------------------------------------------------------------------------
function SK(ctx) {
  const K = P2(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, spacer, setP, icon, box } = K;
  const inst = K.inst;
  const kinst = (name, variant, props = {}) => {
    const src = getComp(ctx, name); let c = src;
    if (src.type === 'COMPONENT_SET') { c = variant ? src.children.find((v) => v.name === variant) : src.defaultVariant; if (!c) throw new Error('Variant ' + variant + ' not in ' + name); }
    const i = c.createInstance(); setP(i, props); return i;
  };
  const nested = (root, name) => root.findOne((n) => n.type === 'INSTANCE' && n.name === name);
  const hideAct = (bar, all = false) => { const a = bar.findAll((n) => n.name === 'Action'); (all ? a : a.slice(0, 1)).forEach((x) => { x.visible = false; }); };
  const label = async (p, s) => { const t = await txt(s, 'Label/Large', 'color/on-surface-variant'); p.appendChild(t); wrapText(t); return t; };
  const tf = (p, state, props, trailingIcon) => {
    const f = inst('textField', `State=${state}`, Object.assign({ 'Show helper': false, 'Show trailing': !!trailingIcon }, props)); add(p, f);
    if (trailingIcon && trailingIcon !== 'dropdown') { const t = f.findOne((n) => n.name === 'Trailing'); if (t && t.type === 'INSTANCE') { t.swapComponent(ctx.IC[trailingIcon]); const g = t.findOne((n) => n.name === 'glyph'); if (g) g.fills = [paint('color/on-surface-variant')]; } }
    return f;
  };
  const date = (p, l, v, filled = true) => tf(p, filled ? 'Filled' : 'Default', { Label: l, Value: v }, 'calendar');
  const select = (p, l, v, filled = true) => tf(p, filled ? 'Filled' : 'Default', { Label: l, Value: v }, 'dropdown');
  const text = (p, l, v, filled = true) => tf(p, filled ? 'Filled' : 'Default', { Label: l, Value: v });
  const auto = (p, l, v) => add(p, kinst('Read-only field', null, { Label: l, Value: v }));
  const seg = async (p, q, opts, selIdx) => {
    const g = AL('VERTICAL', 'Question'); g.itemSpacing = 6; add(p, g);
    const t = await txt(q, 'Label/Large', 'color/on-surface-variant'); g.appendChild(t); wrapText(t);
    const r = AL('HORIZONTAL', 'Options'); gap(r, 'spacing/xs'); add(g, r);
    opts.forEach((o, i) => { const s = kinst('Segmented option', `Selected=${i === selIdx}`, { Label: o }); r.appendChild(s); s.layoutSizingHorizontal = 'FILL'; });
    return g;
  };
  const radios = async (p, q, opts, selIdx) => { if (q) await label(p, q); for (let i = 0; i < opts.length; i++) add(p, kinst('Radio option', `Selected=${i === selIdx}`, { Label: opts[i] })); };
  const checks = async (p, q, opts) => { if (q) await label(p, q); for (const [o, on] of opts) add(p, kinst('Checkbox option', `Checked=${!!on}`, { Label: o })); };
  const steppers = (p, items) => { const r = AL('HORIZONTAL', 'Steppers'); gap(r, 'spacing/md'); add(p, r); for (const [l, v] of items) { const x = kinst('Stepper', null, { Label: l, Value: v }); r.appendChild(x); x.layoutSizingHorizontal = 'FILL'; } return r; };
  const section = (p, state, title, meta, num) => add(p, kinst('Form section header', `State=${state}`, Object.assign({ Title: title, Meta: meta }, num ? { Number: String(num) } : {})));
  const reveal = (p) => { const rv = AL('VERTICAL', 'Revealed (conditional)'); rv.itemSpacing = 10; rv.paddingLeft = 14; add(p, rv); rv.strokes = [paint('color/primary')]; rv.strokeLeftWeight = 3; rv.strokeTopWeight = 0; rv.strokeRightWeight = 0; rv.strokeBottomWeight = 0; return rv; };
  const banner = (p, type, title, bodyText, action) => { const b = add(p, kinst('Banner', `Type=${type}`, { Title: title, Body: bodyText, Action: action || 'OK' })); if (!action) setP(b, { 'Show action': false }); return b; };
  const infoCard = async (p, title, rows) => { const c = K.card(p, title); c.itemSpacing = 0; c.appendChild(await txt(title, 'Title/Medium', 'color/on-surface')); for (const [l, v] of rows) add(c, kinst('Info row', null, { Label: l, Value: v })); return c; };
  const recordCard = (p, d) => {
    const c = kinst('Record card', null, { Name: d.name, Meta: d.meta }); add(p, c);
    setP(nested(c, 'Avatar'), { Initials: d.name.startsWith('Baby of ') ? 'B' + d.name.split(' ')[2][0] : d.name.split(' ').map((w) => w[0]).join('').slice(0, 2) });
    setP(nested(c, 'Sync badge'), { Status: d.sync || 'Synced' });
    setP(nested(c, 'Tag'), { Tone: d.tone || 'Maternal', Label: d.tag });
    const b = nested(c, 'Button'); if (d.action) setP(b, { Label: d.action }); else b.visible = false;
    return c;
  };
  const bottomBar = (s, a, b) => {
    const bar = AL('HORIZONTAL', 'Bottom action bar'); pad(bar, 'spacing/md', 'spacing/sm'); gap(bar, 'spacing/sm'); fill(bar, 'color/surface-container'); add(s, bar);
    bar.strokes = [paint('color/outline-variant')]; bar.strokeTopWeight = 1; bar.strokeBottomWeight = 0; bar.strokeLeftWeight = 0; bar.strokeRightWeight = 0;
    for (const [st, l] of [['Outlined', a], ['Filled', b]]) { if (!l) continue; const x = inst('button', `Style=${st}, State=Enabled`, { Label: l, 'Show icon': false }); bar.appendChild(x); x.layoutSizingHorizontal = 'FILL'; }
    return bar;
  };
  const screenTop = (s, title, subtitle, actions = 'search') => {
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: title, Subtitle: subtitle }));
    if (actions === 'none') hideAct(bar, true); else if (actions === 'more') hideAct(bar);
    return bar;
  };
  const chips = (p, items, selIdx = 0) => { const r = AL('HORIZONTAL', 'Filters'); gap(r, 'spacing/xs'); r.clipsContent = true; add(p, r); items.forEach((l, i) => r.appendChild(inst('chip', `Selected=${i === selIdx}`, { Label: l }))); return r; };
  const stat = async (p, items) => {
    const st = AL('HORIZONTAL', 'Summary'); gap(st, 'spacing/xs'); add(p, st);
    for (const [n, l, t] of items) { const c = AL('VERTICAL', 'Stat'); pad(c, 'spacing/sm', 'spacing/sm'); c.itemSpacing = 2; rad(c, 'radius/lg'); fill(c, `module/${t}/bg`); st.appendChild(c); c.layoutSizingHorizontal = 'FILL'; c.appendChild(await txt(n, 'Number/Medium', `module/${t}/fg`)); const lt = await txt(l, 'Label/Medium', 'color/on-surface'); c.appendChild(lt); wrapText(lt); }
    return st;
  };
  const tileGrid = (p, tiles) => { const g = AL('HORIZONTAL', 'Grid'); g.layoutWrap = 'WRAP'; g.itemSpacing = 16; g.counterAxisSpacing = 16; add(p, g); for (const [t, n, c, ic] of tiles) { const m = inst('moduleTile', `Tone=${t}`, { Title: n, Count: c, Icon: ic }); g.appendChild(m); m.resize(156, m.height); } return g; };
  // person header (beneficiary in a form/detail)
  const person = async (p, initials, name, meta, tags = []) => {
    const c = AL('VERTICAL', 'Person'); pad(c, 'spacing/md', 'spacing/sm'); gap(c, 'spacing/xs'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(p, c);
    const r = AL('HORIZONTAL'); r.counterAxisAlignItems = 'CENTER'; gap(r, 'spacing/sm'); add(c, r);
    r.appendChild(inst('avatar', null, { Initials: initials }));
    const col = AL('VERTICAL'); col.itemSpacing = 2; r.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    col.appendChild(await txt(name, 'Title/Medium', 'color/on-surface')); const m = await txt(meta, 'Body/Small', 'color/on-surface-variant'); col.appendChild(m); wrapText(m);
    if (tags.length) { const tr = AL('HORIZONTAL', 'Tags'); tr.itemSpacing = 6; tr.layoutWrap = 'WRAP'; tr.counterAxisSpacing = 6; add(c, tr); for (const [tone, l] of tags) tr.appendChild(inst('tag', `Tone=${tone}`, { Label: l })); }
    return c;
  };
  const overlayCenter = (s, node) => { s.appendChild(node); node.layoutPositioning = 'ABSOLUTE'; node.x = Math.round((360 - node.width) / 2); node.y = Math.round((s.height - node.height) / 2); return node; };
  const snack = (s, type, props) => { const sn = kinst('Snackbar', `Type=${type}`, props); s.appendChild(sn); sn.layoutPositioning = 'ABSOLUTE'; sn.x = 16; const bar = s.children.find((n) => n.name === 'Bottom action bar' || n.name === 'Bottom navigation'); sn.y = bar ? bar.y - sn.height - 12 : s.height - sn.height - 24; return sn; };
  return Object.assign({}, K, { inst, kinst, nested, hideAct, label, tf, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar, screenTop, chips, stat, tileGrid, person, overlayCenter, snack });
}

// In-place fixes to kit components (idempotent; keeps instances linked)
async function kitFixes(ctx) {
  const sh = ctx.pages.components.children.find((n) => n.name === 'Form section header');
  if (sh) for (const v of sh.children) {
    const t = v.findOne((n) => n.name === 'Title'); const m = v.findOne((n) => n.name === 'Meta');
    if (t && t.type === 'TEXT') { t.layoutSizingHorizontal = 'FILL'; t.textAutoResize = 'HEIGHT'; t.maxLines = 1; t.textTruncation = 'ENDING'; t.minWidth = 120; }
    if (m && m.type === 'TEXT') { m.textAutoResize = 'WIDTH_AND_HEIGHT'; m.maxWidth = 120; m.maxLines = 1; m.textTruncation = 'ENDING'; m.textAlignHorizontal = 'RIGHT'; }
  }
  const tfSet = ctx.pages.components.children.find((n) => n.name === 'Text field');
  if (tfSet) for (const v of tfSet.children) for (const nm of ['Helper', 'Label']) { const h = v.findOne((n) => n.name === nm); if (h && h.type === 'TEXT') { h.layoutSizingHorizontal = 'FILL'; h.textAutoResize = 'HEIGHT'; } }
  // radio / checkbox rows: label wraps, row grows (min 52)
  for (const setName of ['Radio option', 'Checkbox option']) {
    const set = ctx.pages.components.children.find((n) => n.name === setName);
    if (set) for (const v of set.children) { v.counterAxisSizingMode = 'AUTO'; v.minHeight = 52; v.paddingTop = v.paddingBottom = 12; const l = v.findOne((n) => n.name === 'Label'); if (l && l.type === 'TEXT') { l.layoutSizingHorizontal = 'FILL'; l.textAutoResize = 'HEIGHT'; } }
  }
  const st = ctx.pages.components.children.find((n) => n.name === 'Stepper');
  if (st) { const l = st.findOne((n) => n.name === 'Label'); if (l && l.type === 'TEXT') { l.layoutSizingHorizontal = 'FILL'; l.textAutoResize = 'HEIGHT'; } }
  const ir = ctx.pages.components.children.find((n) => n.name === 'Info row');
  if (ir) { const l = ir.findOne((n) => n.name === 'Label'); if (l && l.type === 'TEXT') { l.textAutoResize = 'HEIGHT'; } }
  const ro = ctx.pages.components.children.find((n) => n.name === 'Read-only field');
  if (ro) { const l = ro.findOne((n) => n.name === 'Label'); if (l && l.type === 'TEXT') { l.layoutSizingHorizontal = 'FILL'; l.textAutoResize = 'HEIGHT'; l.maxLines = 3; l.textTruncation = 'ENDING'; } }
}

// Stack frames row by row: each row starts 200px below the tallest frame of the previous row.
function relayoutRows(frames, rowOf, top = 160) {
  const rows = {};
  for (const f of frames) { const r = rowOf(f); (rows[r] = rows[r] || []).push(f); }
  let y = top;
  for (const r of Object.keys(rows).map(Number).sort((a, b) => a - b)) {
    let h = 0; for (const f of rows[r]) { f.y = y; h = Math.max(h, f.height); }
    y += h + 200;
  }
  return y;
}

// Adds an optional leading icon to every Text field variant (idempotent). Props: "Show leading", "Leading icon".
async function ensureLeadingIcon(ctx) {
  const set = ctx.pages.components.children.find((n) => n.name === 'Text field' && n.type === 'COMPONENT_SET');
  if (!set) return;
  const defs = set.componentPropertyDefinitions;
  if (Object.keys(defs).some((k) => k.startsWith('Show leading'))) return;
  const ink = ctx.V['color/on-surface-variant'];
  const created = [];
  for (const v of set.children) {
    const field = v.findOne((n) => n.name === 'Field'); if (!field) continue;
    const ic = ctx.IC.person.createInstance(); ic.name = 'Leading'; ic.resize(22, 22);
    const g = ic.findOne((n) => n.name === 'glyph'); if (g) g.fills = [figma.variables.setBoundVariableForPaint({ type: 'SOLID', color: { r: 0, g: 0, b: 0 } }, 'color', ink)];
    field.insertChild(0, ic); created.push(ic);
  }
  const kb = set.addComponentProperty('Show leading', 'BOOLEAN', false);
  const ki = set.addComponentProperty('Leading icon', 'INSTANCE_SWAP', ctx.IC.person.id);
  for (const ic of created) ic.componentPropertyReferences = { visible: kb, mainComponent: ki };
}
