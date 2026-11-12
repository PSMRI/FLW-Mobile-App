// ---------------------------------------------------------------------------
// Phase 2 shared helpers
// ---------------------------------------------------------------------------
const EXTRA_ICONS = {
  flame: 'M19.48 12.35c-1.57-4.08-7.16-4.3-5.81-10.23.1-.44-.37-.78-.75-.55C9.29 3.71 6.68 8 8.87 13.62c.18.46-.36.89-.75.59-1.81-1.37-2-3.34-1.84-4.75.06-.52-.62-.77-.91-.34C4.69 10.16 4 11.84 4 14.37c.38 5.6 5.11 7.32 6.81 7.54 2.43.31 5.06-.14 6.95-1.87 2.08-1.93 2.84-5.01 1.72-7.69zm-9.28 5.03c1.44-.35 2.18-1.39 2.38-2.31.33-1.43-.96-2.83-.09-5.09.33 1.87 3.27 3.04 3.27 5.08.08 2.53-2.66 4.7-5.56 2.32z',
  trophy: 'M19 5h-2V3H7v2H5c-1.1 0-2 .9-2 2v1c0 2.55 1.92 4.63 4.39 4.94A5.01 5.01 0 0 0 11 15.9V19H7v2h10v-2h-4v-3.1a5.01 5.01 0 0 0 3.61-2.96C19.08 12.63 21 10.55 21 8V7c0-1.1-.9-2-2-2zM5 8V7h2v3.82C5.84 10.4 5 9.3 5 8zm14 0c0 1.3-.84 2.4-2 2.82V7h2v1z',
  play: 'M8 5v14l11-7z',
  shield: 'M12 1 3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z',
  chevron_down: 'M16.59 8.59 12 13.17 7.41 8.59 6 10l6 6 6-6z',
  chevron_left: 'M15.41 7.41 14 6l-6 6 6 6 1.41-1.41L10.83 12z',
  camera: 'M12 15.2a3.2 3.2 0 1 0 0-6.4 3.2 3.2 0 0 0 0 6.4zM9 2 7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z',
  minus: 'M19 13H5v-2h14v2z',
  wifi_off: 'M23.64 7c-.45-.34-4.93-4-11.64-4-1.5 0-2.89.19-4.15.48L18.18 13.8 23.64 7zm-6.6 8.22L3.27 1.44 2 2.72l2.05 2.06C1.91 5.76.59 6.82.36 7l11.63 14.49.01.01.01-.01 3.9-4.86 3.32 3.32 1.27-1.27-3.46-3.46z',
  share: 'M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z',
  bar_chart: 'M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z',
  info: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z',
};

async function ensureIcons(ctx) {
  await figma.setCurrentPageAsync(ctx.pages.components);
  const frame = ctx.pages.components.children.find((n) => n.name === 'Icons');
  const ink = ctx.V['color/on-surface'];
  const added = [];
  for (const [n, d] of Object.entries(EXTRA_ICONS)) {
    if (ctx.IC[n]) continue;
    const c = figma.createComponent(); c.name = 'Icon/' + n; c.resize(24, 24); c.fills = [];
    const svg = figma.createNodeFromSvg(`<svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="${d}" fill="#000"/></svg>`);
    const vec = svg.children[0]; c.appendChild(vec); svg.remove(); vec.name = 'glyph';
    vec.constraints = { horizontal: 'SCALE', vertical: 'SCALE' };
    vec.fills = [figma.variables.setBoundVariableForPaint({ type: 'SOLID', color: { r: 0, g: 0, b: 0 } }, 'color', ink)];
    frame.appendChild(c); ctx.IC[n] = c; added.push(n);
  }
  return added;
}

function P2(ctx) {
  const H = helpers(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, icon } = H;

  // component with auto layout
  const comp = (name, dir) => { const c = figma.createComponent(); c.name = name; c.fills = []; if (dir) c.layoutMode = dir; return c; };
  // fixed size auto-layout frame (centered content)
  const box = (name, w, h, dir = 'HORIZONTAL') => {
    const f = AL(dir, name); f.resize(w, h); f.primaryAxisSizingMode = 'FIXED'; f.counterAxisSizingMode = 'FIXED';
    f.primaryAxisAlignItems = 'CENTER'; f.counterAxisAlignItems = 'CENTER'; return f;
  };
  // width fixed, height hugs
  const fixW = (f, w) => {
    f.resize(w, Math.max(1, f.height));
    if (f.layoutMode === 'VERTICAL') { f.counterAxisSizingMode = 'FIXED'; f.primaryAxisSizingMode = 'AUTO'; }
    else if (f.layoutMode === 'HORIZONTAL') { f.primaryAxisSizingMode = 'FIXED'; f.counterAxisSizingMode = 'AUTO'; }
    return f;
  };
  const finishSet = (set, w) => {
    set.layoutMode = 'HORIZONTAL'; set.layoutWrap = 'WRAP'; set.itemSpacing = 24; set.counterAxisSpacing = 24;
    set.paddingLeft = set.paddingRight = set.paddingTop = set.paddingBottom = 24;
    set.fills = [paint('color/surface')]; set.cornerRadius = 16;
    set.resize(w, 10); set.primaryAxisSizingMode = 'FIXED'; set.counterAxisSizingMode = 'AUTO';
  };
  const textProp = (owner, node, name, def) => { const k = owner.addComponentProperty(name, 'TEXT', def); node.componentPropertyReferences = Object.assign({}, node.componentPropertyReferences || {}, { characters: k }); return k; };
  const boolProp = (owner, node, name, def) => { const k = owner.addComponentProperty(name, 'BOOLEAN', def); node.componentPropertyReferences = Object.assign({}, node.componentPropertyReferences || {}, { visible: k }); return k; };
  // apply a property to the same-named node in every variant of a set
  const setTextProp = (set, nodeName, propName, def) => { const k = set.addComponentProperty(propName, 'TEXT', def); for (const v of set.children) { const n = v.findOne((x) => x.name === nodeName); if (n) n.componentPropertyReferences = { characters: k }; } return k; };
  const setBoolProp = (set, nodeName, propName, def) => { const k = set.addComponentProperty(propName, 'BOOLEAN', def); for (const v of set.children) { const n = v.findOne((x) => x.name === nodeName); if (n) n.componentPropertyReferences = Object.assign({}, n.componentPropertyReferences || {}, { visible: k }); } return k; };

  const progress = (w, pct, color = 'color/primary', h = 6) => {
    const t = figma.createFrame(); t.name = 'Progress'; t.resize(w, h); t.cornerRadius = h / 2; fill(t, 'color/outline-variant'); t.clipsContent = true;
    const r = figma.createRectangle(); r.name = 'Value'; r.resize(Math.max(1, Math.round(w * pct)), h); r.cornerRadius = h / 2; r.fills = [paint(color)]; t.appendChild(r);
    return t;
  };
  const circle = (d, color) => { const e = figma.createEllipse(); e.resize(d, d); e.fills = [paint(color)]; return e; };
  const divider = (w = 296) => { const d = figma.createRectangle(); d.name = 'Divider'; d.resize(w, 1); d.fills = [paint('color/outline-variant')]; return d; };
  const deva = (t) => { if (ctx.fonts && ctx.fonts.deva) t.fontName = ctx.fonts.deva; return t; };
  const beng = (t) => { if (ctx.fonts && ctx.fonts.beng) t.fontName = ctx.fonts.beng; return t; };

  // remove top-level nodes on a page by exact name
  const removeNamed = (page, names) => { for (const n of [...page.children]) if (names.includes(n.name)) n.remove(); };

  // Sections on the Screens page
  const stageSection = (name) => {
    const sp = ctx.pages.screens;
    removeNamed(sp, [name]);
    let maxY = 0;
    for (const c of sp.children) maxY = Math.max(maxY, c.y + c.height);
    const s = figma.createSection(); s.name = name; sp.appendChild(s); s.x = 0; s.y = maxY + 240;
    return s;
  };
  const fitSection = (s) => {
    let w = 0, h = 0;
    for (const c of s.children) { w = Math.max(w, c.x + c.width); h = Math.max(h, c.y + c.height); }
    s.resizeWithoutConstraints(w + 120, h + 120);
  };
  // phone frame inside a section; grid position col,row
  const phone = (section, name, col, row = 0, fixedHeight = false) => {
    const s = figma.createFrame(); s.name = name; s.resize(360, 800);
    s.layoutMode = 'VERTICAL'; s.counterAxisSizingMode = 'FIXED'; s.primaryAxisSizingMode = fixedHeight ? 'FIXED' : 'AUTO';
    if (!fixedHeight) s.minHeight = 800;
    fill(s, 'color/surface'); s.clipsContent = true; section.appendChild(s);
    s.x = 120 + col * 440; s.y = 160 + row * (ctx.rowGap || 1500);
    return s;
  };
  const body = (s, name = 'Content') => { const b = AL('VERTICAL', name); pad(b, 'spacing/md', 'spacing/md'); gap(b, 'spacing/md'); add(s, b); return b; };
  const card = (p, name) => { const c = AL('VERTICAL', name); pad(c, 'spacing/md', 'spacing/md'); gap(c, 'spacing/sm'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(p, c); return c; };
  // dim + bottom sheet over a fixed-height phone frame
  const scrim = (s) => { const r = figma.createRectangle(); r.name = 'Scrim'; r.resize(360, 800); r.fills = [{ type: 'SOLID', color: { r: 0.06, g: 0.08, b: 0.1 }, opacity: 0.45 }]; s.appendChild(r); r.layoutPositioning = 'ABSOLUTE'; r.x = 0; r.y = 0; return r; };
  const sheet = (s, name) => {
    const sh = AL('VERTICAL', name); fill(sh, 'color/surface-container'); sh.topLeftRadius = 24; sh.topRightRadius = 24;
    pad(sh, 'spacing/md'); sh.paddingTop = 12; sh.paddingBottom = 24; gap(sh, 'spacing/sm'); sh.counterAxisAlignItems = 'CENTER';
    s.appendChild(sh); sh.layoutPositioning = 'ABSOLUTE'; fixW(sh, 360);
    const hdl = figma.createRectangle(); hdl.name = 'Handle'; hdl.resize(32, 4); hdl.cornerRadius = 2; hdl.fills = [paint('color/outline')]; sh.appendChild(hdl);
    return sh;
  };
  const pinBottom = (s, sh) => { sh.x = 0; sh.y = s.height - sh.height; };
  // annotation sticky next to a frame (for developers / flavour notes)
  const note = async (section, x, y, w, text) => {
    const n = AL('VERTICAL', 'Note'); n.x = x; n.y = y; section.appendChild(n); fixW(n, w); pad(n, 'spacing/md', 'spacing/sm');
    rad(n, 'radius/md'); n.fills = [{ type: 'SOLID', color: { r: 1, g: 0.96, b: 0.76 } }];
    const t = await txt(text, 'Body/Medium', 'color/on-surface'); n.appendChild(t); wrapText(t);
    return n;
  };
  return Object.assign({}, H, { comp, box, fixW, finishSet, textProp, boolProp, setTextProp, setBoolProp, progress, circle, divider, deva, beng, removeNamed, stageSection, fitSection, phone, body, card, scrim, sheet, pinBottom, note });
}

// place a list of component nodes in a column on the Components page
function stackOnPage(nodes, x, y0, gapY = 64) {
  let y = y0;
  for (const n of nodes) { n.x = x; n.y = y; y += n.height + gapY; }
  return y;
}
