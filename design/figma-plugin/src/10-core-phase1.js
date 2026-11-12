// FLW Redesign Builder: local Figma development plugin.
// Builds the pending screens of the FLW App Redesign file using the
// existing variables, styles and components (no Figma MCP limits).
// Standard Plugin API only. Safe to re-run: it removes its own earlier output first.
// See docs/FIGMA_REDESIGN_PLAN.md.

const SCREEN_PREFIXES = ['07 ·', '08 ·', '09 ·'];
const FOUNDATION_FRAME = 'Foundations overview';

// ---------------------------------------------------------------------------
// Context: pages, variables, styles, components
// ---------------------------------------------------------------------------
async function loadContext() {
  const page = (name) => {
    const p = figma.root.children.find((x) => x.name === name);
    if (!p) throw new Error(`Page "${name}" not found. Open the "FLW App Redesign" file.`);
    return p;
  };
  const ctx = { pages: { foundations: page('Foundations'), components: page('Components'), screens: page('Screens') } };
  await ctx.pages.components.loadAsync();
  await ctx.pages.screens.loadAsync();
  await ctx.pages.foundations.loadAsync();

  ctx.V = {};
  for (const v of await figma.variables.getLocalVariablesAsync()) ctx.V[v.name] = v;
  if (!ctx.V['color/primary']) throw new Error('Design tokens missing (color/primary). Wrong file?');
  ctx.TS = {};
  for (const s of await figma.getLocalTextStylesAsync()) ctx.TS[s.name] = s;
  ctx.ES = {};
  for (const s of await figma.getLocalEffectStylesAsync()) ctx.ES[s.name] = s;

  const fonts = [['Inter', 'Regular'], ['Roboto', 'Regular'], ['Roboto', 'Medium'], ['Roboto', 'SemiBold'], ['Roboto', 'Bold']];
  for (const [family, style] of fonts) await figma.loadFontAsync({ family, style });
  ctx.fonts = { deva: null, beng: null };
  try { await figma.loadFontAsync({ family: 'Noto Sans Devanagari', style: 'Medium' }); ctx.fonts.deva = { family: 'Noto Sans Devanagari', style: 'Medium' }; } catch (e) { /* optional */ }
  try { await figma.loadFontAsync({ family: 'Noto Sans Bengali', style: 'Medium' }); ctx.fonts.beng = { family: 'Noto Sans Bengali', style: 'Medium' }; } catch (e) { /* optional */ }

  const cp = ctx.pages.components;
  ctx.N = {};
  const names = {
    button: 'Button', textField: 'Text field', chip: 'Filter chip', syncBadge: 'Sync badge', avatar: 'Avatar',
    fab: 'FAB extended', moduleTile: 'Module tile', dueCard: 'Due card', statusBar: 'Status bar',
    topAppBar: 'Top app bar', bottomNav: 'Bottom navigation', searchBar: 'Search bar',
    sectionHeader: 'Section header', listItem: 'List item', tag: 'Tag', benCard: 'Beneficiary card', hhCard: 'Household card',
  };
  for (const [k, n] of Object.entries(names)) {
    const node = cp.children.find((x) => x.name === n && (x.type === 'COMPONENT_SET' || x.type === 'COMPONENT'));
    if (!node) throw new Error(`Component "${n}" not found on Components page.`);
    ctx.N[k] = node;
  }
  const iconsFrame = cp.children.find((x) => x.name === 'Icons');
  if (!iconsFrame) throw new Error('Icons frame not found on Components page.');
  ctx.IC = {};
  for (const c of iconsFrame.children) if (c.type === 'COMPONENT') ctx.IC[c.name.replace('Icon/', '')] = c;
  return ctx;
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
function helpers(ctx) {
  const { V, TS, N, IC } = ctx;
  const paint = (n) => {
    if (!V[n]) throw new Error('Missing variable ' + n);
    return figma.variables.setBoundVariableForPaint({ type: 'SOLID', color: { r: 0, g: 0, b: 0 } }, 'color', V[n]);
  };
  const fill = (node, n) => { node.fills = n ? [paint(n)] : []; };
  const stroke = (node, n, w = 1) => { node.strokes = [paint(n)]; node.strokeWeight = w; node.strokeAlign = 'INSIDE'; };
  const rad = (node, n) => { for (const k of ['topLeftRadius', 'topRightRadius', 'bottomLeftRadius', 'bottomRightRadius']) node.setBoundVariable(k, V[n]); };
  const pad = (node, h, v) => {
    if (h) { node.setBoundVariable('paddingLeft', V[h]); node.setBoundVariable('paddingRight', V[h]); }
    if (v) { node.setBoundVariable('paddingTop', V[v]); node.setBoundVariable('paddingBottom', V[v]); }
  };
  const gap = (node, n) => node.setBoundVariable('itemSpacing', V[n]);
  const AL = (dir, name) => {
    const f = figma.createFrame();
    f.layoutMode = dir; f.primaryAxisSizingMode = 'AUTO'; f.counterAxisSizingMode = 'AUTO';
    f.fills = []; f.clipsContent = false; if (name) f.name = name;
    return f;
  };
  const txt = async (s, style, color, name) => {
    const t = figma.createText();
    await t.setTextStyleIdAsync(TS[style].id);
    t.characters = s; fill(t, color); if (name) t.name = name;
    return t;
  };
  const wrapText = (t) => { t.layoutSizingHorizontal = 'FILL'; t.textAutoResize = 'HEIGHT'; };
  const add = (p, c, fillW = true) => { p.appendChild(c); if (fillW) c.layoutSizingHorizontal = 'FILL'; return c; };
  const spacer = (row) => { const s = AL('HORIZONTAL', 'Spacer'); row.appendChild(s); s.layoutSizingHorizontal = 'FILL'; s.layoutSizingVertical = 'FIXED'; s.resize(s.width, 1); };
  const setP = (i, props) => {
    const cp = i.componentProperties; const o = {};
    for (const [n, val] of Object.entries(props)) {
      const k = Object.keys(cp).find((key) => key.split('#')[0] === n);
      if (!k) throw new Error(`No property "${n}" on ${i.name}`);
      o[k] = cp[k].type === 'INSTANCE_SWAP' ? IC[val].id : val;
    }
    if (Object.keys(o).length) i.setProperties(o);
  };
  const inst = (key, variantName, props = {}) => {
    const src = N[key];
    let c = src;
    if (src.type === 'COMPONENT_SET') {
      c = variantName ? src.children.find((v) => v.name === variantName) : src.defaultVariant;
      if (!c) throw new Error(`Variant "${variantName}" not found in ${src.name}`);
    }
    const i = c.createInstance(); setP(i, props); return i;
  };
  const icon = (name, color, size = 24) => {
    const i = IC[name].createInstance(); i.name = 'Icon';
    if (size !== 24) i.resize(size, size);
    const g = i.findOne((n) => n.name === 'glyph'); if (g) g.fills = [paint(color)];
    return i;
  };
  const recolorIcon = (instance, color) => { const g = instance.findOne((n) => n.name === 'glyph'); if (g) g.fills = [paint(color)]; };
  return { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, spacer, setP, inst, icon, recolorIcon };
}

// ---------------------------------------------------------------------------
// Screens 07–09
// ---------------------------------------------------------------------------
async function buildScreens(ctx) {
  const H = helpers(ctx);
  const { fill, stroke, rad, pad, gap, AL, txt, wrapText, add, spacer, inst, icon, recolorIcon, paint } = H;
  const sp = ctx.pages.screens;
  await figma.setCurrentPageAsync(sp);

  // Design fix: the drawer is replaced by bottom nav + Profile, so the Home app bar has no menu button.
  const homeBar = ctx.N.topAppBar.children.find((v) => v.name === 'Type=Home');
  if (homeBar) {
    const lead = homeBar.findChild((n) => n.name === 'Leading');
    if (lead && lead.visible) { lead.visible = false; homeBar.paddingLeft = 16; }
  }

  // remove earlier/partial output of this builder (and of the interrupted MCP run)
  for (const n of [...sp.children]) if (SCREEN_PREFIXES.some((p) => n.name.startsWith(p))) n.remove();

  let maxX = 0;
  for (const c of sp.children) maxX = Math.max(maxX, c.x + c.width + 120);
  const screen = (name) => {
    const s = figma.createFrame(); s.name = name; s.resize(360, 800);
    s.layoutMode = 'VERTICAL'; s.counterAxisSizingMode = 'FIXED'; s.primaryAxisSizingMode = 'AUTO'; s.minHeight = 800;
    fill(s, 'color/surface'); s.clipsContent = true; s.x = maxX; s.y = 0; maxX += 480;
    sp.appendChild(s); return s;
  };
  const body = (s) => { const b = AL('VERTICAL', 'Content'); pad(b, 'spacing/md', 'spacing/md'); gap(b, 'spacing/md'); add(s, b); return b; };
  const card = (p, name) => { const c = AL('VERTICAL', name); pad(c, 'spacing/md', 'spacing/md'); gap(c, 'spacing/md'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(p, c); return c; };
  const hideActions = (bar) => { for (const a of bar.findAll((n) => n.name === 'Action')) a.visible = false; };
  const created = [];

  // ===== 07 · Register member – Step 1 =====
  {
    const s = screen('07 · Register member – Step 1');
    add(s, inst('statusBar'));
    const tb = add(s, inst('topAppBar', 'Type=Inner', { Title: 'Register member', Subtitle: "Ramesh Kumar's family" }));
    const acts = tb.findAll((n) => n.name === 'Action'); if (acts[0]) acts[0].visible = false;

    const st = AL('VERTICAL', 'Stepper'); pad(st, 'spacing/md'); st.paddingBottom = 12; gap(st, 'spacing/xs'); add(s, st);
    const bars = AL('HORIZONTAL', 'Progress'); bars.itemSpacing = 6; add(st, bars);
    [true, false, false].forEach((on) => { const r = figma.createRectangle(); r.resize(100, 6); r.cornerRadius = 3; r.fills = [paint(on ? 'color/primary' : 'color/outline-variant')]; bars.appendChild(r); r.layoutSizingHorizontal = 'FILL'; });
    const sl = AL('HORIZONTAL', 'Step labels'); sl.counterAxisAlignItems = 'CENTER'; add(st, sl);
    sl.appendChild(await txt('Step 1 of 3 · Personal details', 'Label/Large', 'color/primary')); spacer(sl);
    sl.appendChild(await txt('Next: Family', 'Label/Medium', 'color/on-surface-variant'));

    const b = body(s); b.paddingTop = 4;
    const c1 = card(b, 'Personal details');
    const ct = await txt('Personal details', 'Title/Medium', 'color/on-surface'); c1.appendChild(ct);

    const pr = AL('HORIZONTAL', 'Photo'); pr.counterAxisAlignItems = 'CENTER'; gap(pr, 'spacing/md'); add(c1, pr);
    const ph = AL('HORIZONTAL', 'Photo slot'); ph.resize(64, 64); ph.primaryAxisSizingMode = 'FIXED'; ph.counterAxisSizingMode = 'FIXED';
    ph.primaryAxisAlignItems = 'CENTER'; ph.counterAxisAlignItems = 'CENTER'; rad(ph, 'radius/full'); fill(ph, 'color/surface-container-high');
    stroke(ph, 'color/outline'); ph.dashPattern = [4, 4]; ph.appendChild(icon('person', 'color/on-surface-variant', 32)); pr.appendChild(ph);
    const pcol = AL('VERTICAL'); pcol.itemSpacing = 2; pr.appendChild(pcol); pcol.layoutSizingHorizontal = 'FILL';
    pcol.appendChild(await txt('Add photo', 'Title/Small', 'color/primary'));
    const pt = await txt('Optional · helps identify the member', 'Body/Small', 'color/on-surface-variant'); pcol.appendChild(pt); wrapText(pt);

    add(c1, inst('textField', 'State=Filled', { Label: 'First name *', Value: 'Sunita', 'Show helper': false, 'Show trailing': false }));
    add(c1, inst('textField', 'State=Filled', { Label: 'Last name', Value: 'Devi', 'Show helper': false, 'Show trailing': false }));

    const g = AL('VERTICAL', 'Gender'); gap(g, 'spacing/xs'); add(c1, g);
    g.appendChild(await txt('Gender *', 'Label/Large', 'color/on-surface-variant'));
    const seg = AL('HORIZONTAL', 'Options'); gap(seg, 'spacing/xs'); add(g, seg);
    for (const [l, on] of [['Female', true], ['Male', false], ['Other', false]]) {
      const c = inst('chip', `Selected=${on}`, { Label: l }); seg.appendChild(c);
      c.resize(c.width, 44); c.layoutSizingHorizontal = 'FILL';
      try { c.primaryAxisAlignItems = 'CENTER'; } catch (e) { /* not overridable */ }
    }

    const dob = add(c1, inst('textField', 'State=Default', { Label: 'Date of birth *', Value: 'DD / MM / YYYY', Helper: "Don't know? Enter age in years instead", 'Show trailing': true }));
    const tr = dob.findOne((n) => n.name === 'Trailing');
    if (tr && tr.type === 'INSTANCE') { tr.swapComponent(ctx.IC.calendar); recolorIcon(tr, 'color/on-surface-variant'); }
    add(c1, inst('textField', 'State=Default', { Label: 'Relation with head of family *', Value: 'Select relation', 'Show helper': false, 'Show trailing': true }));
    add(c1, inst('textField', 'State=Error', { Label: 'Mobile number *', Value: '98765', Helper: 'Enter a valid 10-digit mobile number', 'Show trailing': true }));

    const tip = AL('HORIZONTAL', 'Autosave tip'); gap(tip, 'spacing/sm'); pad(tip, 'spacing/md', 'spacing/sm'); rad(tip, 'radius/md'); fill(tip, 'color/primary-subtle'); add(b, tip);
    tip.appendChild(icon('check_circle', 'color/primary', 20));
    const tt = await txt('Your entries are saved on this phone automatically. You can finish later from Drafts.', 'Body/Small', 'color/on-surface'); tip.appendChild(tt); wrapText(tt);

    const bar = AL('HORIZONTAL', 'Bottom action bar'); pad(bar, 'spacing/md', 'spacing/sm'); gap(bar, 'spacing/sm'); fill(bar, 'color/surface-container');
    await bar.setEffectStyleIdAsync(ctx.ES['Elevation/2'].id); add(s, bar);
    const x1 = inst('button', 'Style=Outlined, State=Enabled', { Label: 'Save draft', 'Show icon': false }); bar.appendChild(x1); x1.layoutSizingHorizontal = 'FILL';
    const x2 = inst('button', 'Style=Filled, State=Enabled', { Label: 'Next', 'Show icon': false }); bar.appendChild(x2); x2.layoutSizingHorizontal = 'FILL';
    created.push(s);
  }

  // ===== 08 · Sync =====
  {
    const s = screen('08 · Sync');
    add(s, inst('statusBar'));
    const tb = add(s, inst('topAppBar', 'Type=Home', { Title: 'Sync', Subtitle: 'Last synced yesterday, 6:40 PM' })); hideActions(tb);
    const b = body(s);

    const hero = AL('VERTICAL', 'Sync status'); pad(hero, 'spacing/lg', 'spacing/lg'); gap(hero, 'spacing/sm'); rad(hero, 'radius/xl'); fill(hero, 'color/primary-container'); add(b, hero);
    const hr = AL('HORIZONTAL'); hr.counterAxisAlignItems = 'CENTER'; gap(hr, 'spacing/sm'); add(hero, hr);
    hr.appendChild(icon('cloud_upload', 'color/primary', 32));
    const hc = AL('VERTICAL'); hr.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    hc.appendChild(await txt('12 records waiting', 'Headline/Small', 'color/on-primary-container'));
    hc.appendChild(await txt('Saved safely on this phone', 'Body/Medium', 'color/on-primary-container'));
    const track = figma.createFrame(); track.name = 'Progress'; track.resize(280, 8); track.cornerRadius = 4; fill(track, 'color/surface-container'); add(hero, track); track.layoutSizingVertical = 'FIXED';
    const prog = figma.createRectangle(); prog.resize(270, 8); prog.cornerRadius = 4; prog.fills = [paint('color/primary')]; track.appendChild(prog);
    prog.constraints = { horizontal: 'SCALE', vertical: 'STRETCH' };
    hero.appendChild(await txt('1,092 of 1,104 records synced (99%)', 'Label/Medium', 'color/on-primary-container'));
    const sbtn = inst('button', 'Style=Filled, State=Enabled', { Label: 'Sync now', 'Show icon': true, Icon: 'sync' });
    add(hero, sbtn); sbtn.resize(sbtn.width, 52); sbtn.layoutSizingHorizontal = 'FILL';

    add(b, inst('sectionHeader', null, { Title: 'By module', Action: 'Sync log' }));
    const tbl = card(b, 'Module status'); tbl.itemSpacing = 0; tbl.paddingTop = 4; tbl.paddingBottom = 4;
    const rows = [['Households', '248', '0', 'Synced'], ['Beneficiaries', '1,104', '4', 'Pending'], ['ANC visits', '186', '5', 'Pending'], ['Immunization', '412', '0', 'Synced'], ['Eligible couple tracking', '96', '2', 'Failed'], ['Village meetings', '14', '1', 'Syncing']];
    for (let i = 0; i < rows.length; i++) {
      const [m, tot, p, stt] = rows[i];
      const r = AL('HORIZONTAL', 'Row'); r.counterAxisAlignItems = 'CENTER'; r.paddingTop = 12; r.paddingBottom = 12; gap(r, 'spacing/sm'); add(tbl, r);
      if (i > 0) { r.strokes = [paint('color/outline-variant')]; r.strokeTopWeight = 1; r.strokeBottomWeight = 0; r.strokeLeftWeight = 0; r.strokeRightWeight = 0; }
      const col = AL('VERTICAL'); col.itemSpacing = 2; r.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      col.appendChild(await txt(m, 'Title/Small', 'color/on-surface'));
      col.appendChild(await txt(`${tot} records${p !== '0' ? ` · ${p} waiting` : ''}`, 'Body/Small', 'color/on-surface-variant'));
      r.appendChild(inst('syncBadge', `Status=${stt}`));
    }
    const note = AL('HORIZONTAL', 'Failed help'); gap(note, 'spacing/sm'); pad(note, 'spacing/md', 'spacing/sm'); rad(note, 'radius/md'); fill(note, 'color/error-container'); add(b, note);
    note.appendChild(icon('warning', 'color/error', 20));
    const nt = await txt('2 eligible-couple records failed. Tap a record to see what needs fixing, then sync again.', 'Body/Small', 'color/on-error-container'); note.appendChild(nt); wrapText(nt);
    add(s, inst('bottomNav', 'Active=Sync'));
    created.push(s);
  }

  // ===== 09 · Profile & more (replaces the side drawer) =====
  {
    const s = screen('09 · Profile & more');
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 8;
    const head = AL('VERTICAL', 'Profile header'); pad(head, 'spacing/lg', 'spacing/lg'); gap(head, 'spacing/md'); rad(head, 'radius/xl'); fill(head, 'color/primary'); add(b, head);
    const hr = AL('HORIZONTAL'); hr.counterAxisAlignItems = 'CENTER'; gap(hr, 'spacing/md'); add(head, hr);
    const av = inst('avatar', null, { Initials: 'SK' }); av.resize(64, 64); hr.appendChild(av);
    const hc = AL('VERTICAL'); hc.itemSpacing = 2; hr.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    hc.appendChild(await txt('Sunita Kumari', 'Title/Large', 'color/on-primary'));
    hc.appendChild(await txt('ASHA · Employee ID 102345', 'Body/Medium', 'color/primary-container'));
    hc.appendChild(await txt('Sirsi village · Block Rajnagar', 'Body/Small', 'color/primary-container'));
    hr.appendChild(icon('edit', 'color/on-primary'));
    const stats = AL('HORIZONTAL', 'Stats'); gap(stats, 'spacing/xs'); add(head, stats);
    for (const [v, l] of [['₹3,450', 'Incentive this month'], ['6', 'Badges earned'], ['94%', 'Visits on time']]) {
      const c = AL('VERTICAL'); pad(c, 'spacing/sm', 'spacing/sm'); c.itemSpacing = 2; rad(c, 'radius/md');
      c.fills = [{ type: 'SOLID', color: { r: 1, g: 1, b: 1 }, opacity: 0.14 }]; stats.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      c.appendChild(await txt(v, 'Title/Medium', 'color/on-primary'));
      const lt = await txt(l, 'Label/Small', 'color/primary-container'); c.appendChild(lt); wrapText(lt);
    }
    const group = async (title, items) => {
      const g = AL('VERTICAL', title); gap(g, 'spacing/2xs'); add(b, g);
      g.appendChild(await txt(title, 'Label/Large', 'color/on-surface-variant'));
      const cc = AL('VERTICAL', 'Items'); pad(cc, 'spacing/xs', 'spacing/xs'); rad(cc, 'radius/lg'); fill(cc, 'color/surface-container'); stroke(cc, 'color/outline-variant'); add(g, cc);
      for (const [l, ic, danger] of items) {
        const it = inst('listItem', 'Active=false', { Label: l, Icon: ic, 'Show chevron': !danger }); add(cc, it);
        if (danger) {
          for (const x of it.findAll((n) => n.name === 'glyph')) x.fills = [paint('color/error')];
          const t = it.findOne((n) => n.type === 'TEXT'); if (t) t.fills = [paint('color/error')];
        }
      }
    };
    await group('My work', [['My profile details', 'person'], ['Incentives', 'wallet'], ['Badges & achievements', 'star'], ['Dashboard', 'dashboard'], ['Supervisor', 'group']]);
    await group('Tools', [['Create ABHA number', 'id_card'], ['Get beneficiary data', 'cloud_upload'], ['Video tutorials & LMS', 'visibility']]);
    await group('App', [['Language · English', 'translate'], ['Help & support', 'help'], ['About Sakhi', 'form'], ['Delete my account', 'close', true], ['Log out', 'logout', true]]);
    add(s, inst('bottomNav', 'Active=Profile'));
    created.push(s);
  }

  figma.viewport.scrollAndZoomIntoView(created);
  return created.map((n) => n.name);
}

// ---------------------------------------------------------------------------
// Foundations page
// ---------------------------------------------------------------------------
async function buildFoundations(ctx) {
  const H = helpers(ctx);
  const { fill, stroke, rad, pad, gap, AL, txt, wrapText, add, paint } = H;
  const fp = ctx.pages.foundations;
  await figma.setCurrentPageAsync(fp);
  for (const n of [...fp.children]) if (n.name === FOUNDATION_FRAME) n.remove();

  const resolveHex = async (v) => {
    let val = v.valuesByMode[Object.keys(v.valuesByMode)[0]];
    let guard = 0;
    while (val && val.type === 'VARIABLE_ALIAS' && guard++ < 5) {
      const p = await figma.variables.getVariableByIdAsync(val.id);
      val = p.valuesByMode[Object.keys(p.valuesByMode)[0]];
    }
    if (!val || typeof val.r !== 'number') return '';
    const h = (x) => Math.round(x * 255).toString(16).padStart(2, '0');
    return ('#' + h(val.r) + h(val.g) + h(val.b)).toUpperCase();
  };

  const root = AL('VERTICAL', FOUNDATION_FRAME); root.resize(1240, 100); root.counterAxisSizingMode = 'FIXED'; root.primaryAxisSizingMode = 'AUTO';
  root.paddingLeft = root.paddingRight = root.paddingTop = root.paddingBottom = 48; root.itemSpacing = 48; fill(root, 'color/surface');
  fp.appendChild(root); root.x = 0; root.y = 0;

  const section = async (title, sub) => {
    const s = AL('VERTICAL', title); s.itemSpacing = 16; add(root, s);
    s.appendChild(await txt(title, 'Headline/Small', 'color/on-surface'));
    if (sub) { const t = await txt(sub, 'Body/Medium', 'color/on-surface-variant'); s.appendChild(t); wrapText(t); }
    return s;
  };
  const wrapRow = (p, name, g = 16) => { const r = AL('HORIZONTAL', name); r.layoutWrap = 'WRAP'; r.itemSpacing = g; r.counterAxisSpacing = g; add(p, r); return r; };

  // header
  const hd = AL('VERTICAL', 'Header'); hd.itemSpacing = 8; add(root, hd);
  hd.appendChild(await txt('FLW App: Design foundations', 'Headline/Large', 'color/on-surface'));
  const sub = await txt('Material 3 based design system for the ASHA Front Line Worker app (Sakhi / Saksham / Mitanin / Niramay / Xushrukha). Tokens live in the Primitives, Color and Dimension variable collections. Font: Roboto (Noto Sans Devanagari / Bengali for Hindi & Assamese).', 'Body/Large', 'color/on-surface-variant');
  hd.appendChild(sub); wrapText(sub);

  // principles
  const pr = await section('Design principles');
  const prow = wrapRow(pr, 'Principles');
  const principles = [
    ['Recognise, don\'t read', 'Every module has its own colour tone + icon so ASHAs find it without reading.', 'apps', 'module/maternal'],
    ['Big touch targets', 'All tappable items are 44–56dp. Primary buttons are full width.', 'check_circle', 'module/immunization'],
    ['Labels always visible', 'Field labels sit above inputs, never only as placeholders. Required fields marked *.', 'form', 'module/household'],
    ['Status in words', 'Sync, risk and programme status use colour + icon + text, never colour alone.', 'sync', 'module/care'],
    ['Offline first', 'Forms autosave as drafts. Sync tab shows exactly what is waiting and what failed.', 'cloud_upload', 'module/child'],
    ['Language ready', 'Allow ~30% longer text for Hindi / Assamese. Avoid fixed-width text boxes.', 'translate', 'module/couple'],
  ];
  for (const [t, d, ic, tone] of principles) {
    const c = AL('VERTICAL', t); c.resize(364, 10); c.counterAxisSizingMode = 'FIXED'; c.primaryAxisSizingMode = 'AUTO';
    pad(c, 'spacing/lg', 'spacing/lg'); gap(c, 'spacing/sm'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); prow.appendChild(c);
    const tile = AL('HORIZONTAL', 'Icon tile'); tile.resize(44, 44); tile.primaryAxisSizingMode = 'FIXED'; tile.counterAxisSizingMode = 'FIXED';
    tile.primaryAxisAlignItems = 'CENTER'; tile.counterAxisAlignItems = 'CENTER'; rad(tile, 'radius/md'); fill(tile, tone + '/bg'); tile.appendChild(H.icon(ic, tone + '/fg')); c.appendChild(tile);
    c.appendChild(await txt(t, 'Title/Medium', 'color/on-surface'));
    const dt = await txt(d, 'Body/Medium', 'color/on-surface-variant'); c.appendChild(dt); wrapText(dt);
  }

  // colours
  const cs = await section('Colour', 'Semantic tokens (Color collection) alias the Primitives collection. Light mode only on the Starter plan; dark values to be added after upgrade.');
  const colourGroups = [
    ['Brand & surface', (n) => /^color\/(primary|on-primary|secondary|surface|on-surface|outline)/.test(n)],
    ['Status', (n) => /^color\/(error|on-error|success|warning|accent)/.test(n)],
    ['Module tones', (n) => n.startsWith('module/')],
  ];
  const colorVars = Object.values(ctx.V).filter((v) => v.resolvedType === 'COLOR' && (v.name.startsWith('color/') || v.name.startsWith('module/')));
  for (const [gname, test] of colourGroups) {
    const gl = await txt(gname, 'Title/Medium', 'color/on-surface'); cs.appendChild(gl);
    const row = wrapRow(cs, gname, 12);
    for (const v of colorVars.filter((x) => test(x.name))) {
      const sw = AL('VERTICAL', v.name); sw.resize(132, 10); sw.counterAxisSizingMode = 'FIXED'; sw.primaryAxisSizingMode = 'AUTO'; sw.itemSpacing = 0;
      rad(sw, 'radius/md'); stroke(sw, 'color/outline-variant'); fill(sw, 'color/surface-container'); sw.clipsContent = true; row.appendChild(sw);
      const r = figma.createRectangle(); r.resize(132, 56); r.fills = [paint(v.name)]; sw.appendChild(r); r.layoutSizingHorizontal = 'FILL';
      const meta = AL('VERTICAL', 'Meta'); meta.itemSpacing = 2; meta.paddingLeft = meta.paddingRight = 10; meta.paddingTop = meta.paddingBottom = 8; add(sw, meta);
      const nt = await txt(v.name.replace('color/', '').replace('module/', ''), 'Label/Small', 'color/on-surface'); meta.appendChild(nt); wrapText(nt);
      meta.appendChild(await txt(await resolveHex(v), 'Body/Small', 'color/on-surface-variant'));
    }
  }

  // typography
  const ty = await section('Typography', 'Roboto. Use Number styles for counts on worklists and dashboards.');
  const tl = AL('VERTICAL', 'Type scale'); tl.itemSpacing = 0; rad(tl, 'radius/lg'); fill(tl, 'color/surface-container'); stroke(tl, 'color/outline-variant'); add(ty, tl);
  const order = ['Display/Small', 'Headline/Large', 'Headline/Small', 'Title/Large', 'Title/Medium', 'Title/Small', 'Body/Large', 'Body/Medium', 'Body/Small', 'Label/Large', 'Label/Medium', 'Label/Small', 'Number/Large', 'Number/Medium'];
  for (let i = 0; i < order.length; i++) {
    const st = ctx.TS[order[i]]; if (!st) continue;
    const r = AL('HORIZONTAL', order[i]); r.counterAxisAlignItems = 'CENTER'; r.paddingLeft = r.paddingRight = 20; r.paddingTop = r.paddingBottom = 12; r.itemSpacing = 24; add(tl, r);
    if (i > 0) { r.strokes = [paint('color/outline-variant')]; r.strokeTopWeight = 1; r.strokeBottomWeight = 0; r.strokeLeftWeight = 0; r.strokeRightWeight = 0; }
    const meta = AL('VERTICAL'); meta.resize(220, 10); meta.counterAxisSizingMode = 'FIXED'; meta.primaryAxisSizingMode = 'AUTO'; r.appendChild(meta);
    meta.appendChild(await txt(order[i], 'Label/Large', 'color/on-surface'));
    const lh = st.lineHeight && st.lineHeight.unit === 'PIXELS' ? st.lineHeight.value : '';
    meta.appendChild(await txt(`${st.fontName.style} · ${st.fontSize}/${lh}`, 'Body/Small', 'color/on-surface-variant'));
    const sample = order[i].startsWith('Number') ? '1,104' : 'Women due for ANC visit';
    const sm = await txt(sample, order[i], 'color/on-surface'); r.appendChild(sm); sm.layoutSizingHorizontal = 'FILL';
  }

  // spacing & radius
  const sr = await section('Spacing, radius & elevation');
  const srow = AL('HORIZONTAL', 'Columns'); srow.itemSpacing = 32; add(sr, srow);
  const spc = AL('VERTICAL', 'Spacing'); spc.itemSpacing = 10; srow.appendChild(spc); spc.layoutSizingHorizontal = 'FILL';
  spc.appendChild(await txt('Spacing', 'Title/Medium', 'color/on-surface'));
  for (const n of ['spacing/2xs', 'spacing/xs', 'spacing/sm', 'spacing/md', 'spacing/lg', 'spacing/xl', 'spacing/2xl', 'spacing/3xl']) {
    const v = ctx.V[n]; if (!v) continue; const val = v.valuesByMode[Object.keys(v.valuesByMode)[0]];
    const r = AL('HORIZONTAL', n); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 12; spc.appendChild(r);
    const l = await txt(`${n.split('/')[1]} · ${val}`, 'Label/Medium', 'color/on-surface'); l.resize(80, l.height); l.textAutoResize = 'HEIGHT'; r.appendChild(l);
    const bar = figma.createRectangle(); bar.resize(val * 4, 12); bar.cornerRadius = 2; bar.fills = [paint('color/primary')]; r.appendChild(bar);
  }
  const rc = AL('VERTICAL', 'Radius'); rc.itemSpacing = 10; srow.appendChild(rc); rc.layoutSizingHorizontal = 'FILL';
  rc.appendChild(await txt('Radius', 'Title/Medium', 'color/on-surface'));
  const rr = AL('HORIZONTAL', 'Samples'); rr.itemSpacing = 16; rc.appendChild(rr);
  for (const n of ['radius/sm', 'radius/md', 'radius/lg', 'radius/xl', 'radius/full']) {
    const v = ctx.V[n]; if (!v) continue;
    const col = AL('VERTICAL', n); col.itemSpacing = 6; col.counterAxisAlignItems = 'CENTER'; rr.appendChild(col);
    const sq = figma.createFrame(); sq.resize(56, 56); rad(sq, n); fill(sq, 'color/primary-container'); stroke(sq, 'color/primary'); col.appendChild(sq);
    col.appendChild(await txt(n.split('/')[1], 'Label/Medium', 'color/on-surface'));
  }
  const ec = AL('VERTICAL', 'Elevation'); ec.itemSpacing = 10; srow.appendChild(ec); ec.layoutSizingHorizontal = 'FILL';
  ec.appendChild(await txt('Elevation', 'Title/Medium', 'color/on-surface'));
  const er = AL('HORIZONTAL', 'Samples'); er.itemSpacing = 20; ec.appendChild(er);
  for (const n of ['Elevation/1', 'Elevation/2', 'Elevation/3']) {
    if (!ctx.ES[n]) continue;
    const c = AL('VERTICAL', n); c.resize(88, 72); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED'; c.primaryAxisAlignItems = 'CENTER'; c.counterAxisAlignItems = 'CENTER';
    rad(c, 'radius/lg'); fill(c, 'color/surface-container'); await c.setEffectStyleIdAsync(ctx.ES[n].id); er.appendChild(c);
    c.appendChild(await txt(n.replace('Elevation/', 'Level '), 'Label/Medium', 'color/on-surface'));
  }

  figma.viewport.scrollAndZoomIntoView([root]);
  return [root.name];
}

