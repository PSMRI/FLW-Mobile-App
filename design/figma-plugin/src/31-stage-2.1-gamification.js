// ---------------------------------------------------------------------------
// Stage 2.1 · Gamification
// Source of truth: badges/domain/BadgeDefinitions.kt (naf_feature_gamification)
// ---------------------------------------------------------------------------
const GAM21 = ['Tier stars', 'Progress bar', 'Badge medal', 'Badge tile', 'Streak chip', 'Badge progress row', 'My Impact card', 'Recap strip'];

// [title, module tone, icon, how to earn (strings.xml), kind, milestones]
const BADGES = [
  ['Steady Syncer', 'care', 'sync', 'Sync your app at least once every week.', 'Weekly streak', [2, 4, 6, 8], 'weeks'],
  ['Timely Reporter', 'household', 'clock', 'Submit your monthly incentive form on time.', 'Monthly streak', [1, 3, 6, 12], 'months'],
  ['Complete Worker', 'immunization', 'check_circle', 'Stay active in 3+ health areas every quarter.', 'Quarterly', [3], 'areas'],
  ['Community Voice', 'village', 'group', 'Organise 2+ meeting types (MAA, NDD, AHD, U-WIN) each quarter.', 'Quarterly', [2], 'meeting types'],
  ['Maternal Journey', 'maternal', 'pregnant', 'Complete maternal care (4+ ANC) ending in a facility delivery. Home and in-transit deliveries do not count.', 'Milestones', [1, 5, 15, 30], 'journeys'],
  ['Child Fully Protected', 'child', 'vaccine', 'Complete every birth, 9 and 12 month dose for each child. Twins count separately.', 'Milestones', [3, 10, 25, 60], 'children'],
  ['Digital Identity', 'people', 'id_card', 'Create ABHA IDs after household registration.', 'Milestones', [10, 25, 75, 150], 'ABHA IDs'],
  ['Vulnerable Baby Cared For', 'couple', 'favorite', 'Complete all 7 HBNC visits for an SNCU / low-birth-weight baby.', 'Per case', [1, 2, 3], 'babies'],
  ['Critical Referral Made', 'disease', 'send', 'Refer a SAM child to the NRC.', 'Per case', [1], 'referrals'],
];

function getComp(ctx, name) {
  const n = ctx.pages.components.children.find((x) => x.name === name && (x.type === 'COMPONENT_SET' || x.type === 'COMPONENT'));
  if (!n) throw new Error('Component "' + name + '" missing. Run stage 2.0 first.');
  return n;
}

async function buildGam21Components(ctx) {
  const K = P2(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, spacer, setP, icon, comp, box, fixW, finishSet, setTextProp, textProp, progress, removeNamed } = K;
  const page = ctx.pages.components; await figma.setCurrentPageAsync(page);
  removeNamed(page, GAM21);
  const made = [];

  // ---- Tier stars (0–4) and Progress bar (variants instead of nested colour overrides)
  const tierVs = [];
  for (let t = 0; t <= 4; t++) {
    const c = comp(`Tier=${t}`, 'HORIZONTAL'); c.itemSpacing = 1; c.primaryAxisSizingMode = 'AUTO'; c.counterAxisSizingMode = 'AUTO';
    for (let i = 0; i < 4; i++) { const st = icon('star', i < t ? 'color/accent' : 'color/outline-variant', 14); st.name = 'Star'; c.appendChild(st); }
    tierVs.push(c);
  }
  const tierSet = figma.combineAsVariants(tierVs, page); tierSet.name = 'Tier stars'; finishSet(tierSet, 600);
  tierSet.description = 'Badge tier (milestones in BadgeDefinitions.kt, up to 4).';
  made.push(tierSet);
  const pbVs = [];
  for (const v of [25, 50, 66, 75, 90, 100]) {
    const c = comp(`Value=${v}`); c.resize(240, 6); c.cornerRadius = 3; fill(c, 'color/outline-variant'); c.clipsContent = true;
    const r = figma.createRectangle(); r.name = 'Value'; r.resize(Math.round(240 * v / 100), 6); r.cornerRadius = 3; r.fills = [paint('color/primary')]; c.appendChild(r);
    r.constraints = { horizontal: 'SCALE', vertical: 'STRETCH' };
    pbVs.push(c);
  }
  const pbSet = figma.combineAsVariants(pbVs, page); pbSet.name = 'Progress bar'; finishSet(pbSet, 600);
  pbSet.description = 'Badge / task progress. Resize freely (value scales); pick the nearest Value.';
  made.push(pbSet);
  const pbar = (v, w) => { const i = pbSet.children.find((x) => x.name === `Value=${v}`).createInstance(); if (w) i.resize(w, 6); return i; };
  const tierOf = (t) => tierSet.children.find((x) => x.name === `Tier=${t}`).createInstance();

  // ---- Badge medal: Badge × State
  const vs = [];
  for (const state of ['Earned', 'InProgress', 'Locked']) for (const [name, tone, ic] of BADGES) {
    const c = comp(`Badge=${name}, State=${state}`, 'HORIZONTAL'); c.resize(72, 72); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED';
    c.primaryAxisAlignItems = 'CENTER'; c.counterAxisAlignItems = 'CENTER'; c.cornerRadius = 36; c.clipsContent = false;
    const core = box('Core', 50, 50); core.cornerRadius = 25;
    if (state === 'Earned') { fill(c, `module/${tone}/bg`); stroke(c, `module/${tone}/fg`, 3); fill(core, `module/${tone}/fg`); core.appendChild(icon(ic, 'color/on-primary', 26)); }
    else if (state === 'InProgress') { fill(c, 'color/surface-container'); stroke(c, `module/${tone}/fg`, 3); c.dashPattern = [7, 5]; fill(core, `module/${tone}/bg`); core.appendChild(icon(ic, `module/${tone}/fg`, 26)); }
    else { fill(c, 'color/surface-container-high'); stroke(c, 'color/outline-variant', 3); fill(core, 'color/outline-variant'); core.appendChild(icon(ic, 'color/on-surface-variant', 26)); }
    c.appendChild(core);
    if (state === 'Locked') { const l = box('Lock', 24, 24); l.cornerRadius = 12; fill(l, 'color/on-surface-variant'); l.appendChild(icon('lock', 'color/on-primary', 14)); c.appendChild(l); l.layoutPositioning = 'ABSOLUTE'; l.x = 50; l.y = 48; }
    vs.push(c);
  }
  const medal = figma.combineAsVariants(vs, page); medal.name = 'Badge medal'; finishSet(medal, 940);
  medal.description = 'Badge art for the 9 badges in BadgeDefinitions.kt. Earned = full colour; InProgress = dashed ring; Locked = grey + lock. Use rescale() to enlarge (keeps proportions).';
  made.push(medal);
  const medalOf = (name, state) => medal.children.find((v) => v.name === `Badge=${name}, State=${state}`).createInstance();

  // ---- Badge tile
  {
    const ts = [];
    for (const state of ['Earned', 'InProgress', 'Locked']) {
      const c = comp(`State=${state}`, 'VERTICAL'); fixW(c, 104); c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 6; c.paddingTop = c.paddingBottom = 8;
      c.appendChild(medalOf('Maternal Journey', state));
      const n = await txt('Maternal Journey', 'Label/Medium', state === 'Locked' ? 'color/on-surface-variant' : 'color/on-surface', 'Name'); c.appendChild(n); wrapText(n); n.textAlignHorizontal = 'CENTER';
      if (state === 'Earned') {
        c.appendChild(tierOf(2));
      } else if (state === 'InProgress') {
        c.appendChild(pbar(75, 72));
        c.appendChild(await txt('3 of 4 ANC', 'Label/Small', 'color/on-surface-variant', 'Progress text'));
      } else c.appendChild(await txt('Not started', 'Label/Small', 'color/on-surface-variant', 'Progress text'));
      ts.push(c);
    }
    const set = figma.combineAsVariants(ts, page); set.name = 'Badge tile'; finishSet(set, 420);
    setTextProp(set, 'Name', 'Name', 'Maternal Journey'); setTextProp(set, 'Progress text', 'Progress text', '3 of 4 ANC');
    set.description = 'My Badges grid item. Swap the nested Badge medal (Badge + State). Earned shows tier stars (up to 4 tiers, from BadgeDefinitions milestones).';
    made.push(set);
  }
  // ---- Streak chip
  {
    const ss = [];
    const spec = { Active: ['color/warning-container', 'flame', 'color/accent', '6-week streak · 1 grace left'], AtRisk: ['color/error-container', 'flame', 'color/error', 'Sync this week to keep your streak'], Paused: ['color/primary-subtle', 'shield', 'color/primary', 'Streak paused · holiday'] };
    for (const [st, [bg, ic, fg, label]] of Object.entries(spec)) {
      const c = comp(`State=${st}`, 'HORIZONTAL'); c.resize(100, 34); c.primaryAxisSizingMode = 'AUTO'; c.counterAxisSizingMode = 'FIXED'; c.counterAxisAlignItems = 'CENTER';
      c.paddingLeft = 10; c.paddingRight = 12; c.itemSpacing = 6; rad(c, 'radius/full'); fill(c, bg);
      c.appendChild(icon(ic, fg, 18)); c.appendChild(await txt(label, 'Label/Large', 'color/on-surface', 'Text'));
      ss.push(c);
    }
    const set = figma.combineAsVariants(ss, page); set.name = 'Streak chip'; finishSet(set, 900);
    setTextProp(set, 'Text', 'Text', '6-week streak · 1 grace left');
    set.description = 'Weekly / monthly streaks (StreakEngine). AtRisk when this period is not yet done and no grace token is left; Paused during a server "freeze" (holidays, campaigns).';
    made.push(set);
  }
  // ---- Badge progress row
  {
    const c = comp('Badge progress row', 'HORIZONTAL'); fixW(c, 296); c.counterAxisAlignItems = 'CENTER'; gap(c, 'spacing/sm');
    const m = medalOf('Maternal Journey', 'InProgress'); m.rescale(44 / 72); c.appendChild(m);
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 4; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    const n = await txt('Maternal Journey', 'Title/Small', 'color/on-surface', 'Name'); col.appendChild(n);
    const pr = pbar(75); col.appendChild(pr); pr.layoutSizingHorizontal = 'FILL';
    const d = await txt('3 of 4 ANC visits · Sunita Devi', 'Body/Small', 'color/on-surface-variant', 'Detail'); col.appendChild(d); wrapText(d);
    textProp(c, n, 'Name', 'Maternal Journey'); textProp(c, d, 'Detail', '3 of 4 ANC visits · Sunita Devi');
    c.description = 'Closest badges to earn (My Impact card, Journey). Progress bar width = progress %.';
    made.push(c);
  }
  // ---- My Impact card
  const prow = made[made.length - 1];
  {
    const c = comp('My Impact card', 'VERTICAL'); fixW(c, 328); pad(c, 'spacing/md', 'spacing/md'); gap(c, 'spacing/sm'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant');
    const h = AL('HORIZONTAL', 'Header'); h.counterAxisAlignItems = 'CENTER'; gap(h, 'spacing/sm'); add(c, h);
    const t = box('Icon tile', 40, 40); rad(t, 'radius/md'); fill(t, 'module/couple/bg'); t.appendChild(icon('trophy', 'module/couple/fg', 22)); h.appendChild(t);
    const hc = AL('VERTICAL'); hc.itemSpacing = 2; h.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    hc.appendChild(await txt('My impact', 'Title/Medium', 'color/on-surface', 'Title'));
    hc.appendChild(await txt('6 badges earned · 3 close to earning', 'Body/Small', 'color/on-surface-variant', 'Subtitle'));
    const sc = getComp(ctx, 'Streak chip'); const sci = (sc.type === 'COMPONENT_SET' ? sc.defaultVariant : sc).createInstance(); c.appendChild(sci);
    const rows = [['Maternal Journey', '3 of 4 ANC visits · Sunita Devi', '75'], ['Child Fully Protected', '2 of 3 children fully protected', '66'], ['Digital Identity', '22 of 25 ABHA IDs · next tier', '90']];
    for (const [n, d, p] of rows) {
      const r = prow.createInstance(); add(c, r); setP(r, { Name: n, Detail: d });
      const mi = r.findOne((x) => x.type === 'INSTANCE' && x.name === 'Badge medal'); if (mi) setP(mi, { Badge: n });
      const pb = r.findOne((x) => x.type === 'INSTANCE' && x.name === 'Progress bar'); if (pb) setP(pb, { Value: p });
    }
    c.appendChild(await txt('See all badges  ›', 'Label/Large', 'color/primary', 'Link'));
    c.description = 'Today tab (gamification ON). Replaces item_badge_carousel. Hidden when gamification is OFF for the ASHA.';
    made.push(c);
  }
  // ---- Recap strip
  {
    const rs = [];
    const spec = { New: ['Your October recap is ready', 'See the impact you made this month.', 'New'], InProgress: ['October recap', 'Pick up where you left off.', 'Continue'], Done: ['October recap', 'Look back on your month anytime.', 'Watch again'] };
    for (const [st, [t, b, pill]] of Object.entries(spec)) {
      const c = comp(`State=${st}`, 'HORIZONTAL'); fixW(c, 328); c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/md', 'spacing/sm'); gap(c, 'spacing/sm'); rad(c, 'radius/lg'); fill(c, 'module/couple/bg');
      const pl = box('Play', 44, 44); pl.cornerRadius = 22; fill(pl, 'module/couple/fg'); pl.appendChild(icon('play', 'color/on-primary', 24)); c.appendChild(pl);
      const col = AL('VERTICAL', 'Text'); col.itemSpacing = 2; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      const tt = await txt(t, 'Title/Small', 'color/on-surface', 'Title'); col.appendChild(tt); wrapText(tt);
      const bb = await txt(b, 'Body/Small', 'color/on-surface-variant', 'Body'); col.appendChild(bb); wrapText(bb);
      const p = AL('HORIZONTAL', 'Pill'); p.paddingLeft = p.paddingRight = 8; p.paddingTop = p.paddingBottom = 3; rad(p, 'radius/full'); fill(p, st === 'Done' ? 'color/surface-container' : 'module/couple/fg');
      p.appendChild(await txt(pill, 'Label/Small', st === 'Done' ? 'module/couple/fg' : 'color/on-primary')); c.appendChild(p);
      rs.push(c);
    }
    const set = figma.combineAsVariants(rs, page); set.name = 'Recap strip'; finishSet(set, 1100);
    set.description = 'Monthly Recap entry on Today, shown on days 1–7 of the month (MonthlyRecapAvailability). New → Continue → Watch again.';
    made.push(set);
  }
  stackOnPage(made, 2900, 260);
  return made.map((n) => n.name);
}

// ASHA didi character (simple flat illustration; replace with final art later)
function ashaDidi(K, scale = 1) {
  const { paint } = K;
  const hex = (h) => ({ r: parseInt(h.slice(1, 3), 16) / 255, g: parseInt(h.slice(3, 5), 16) / 255, b: parseInt(h.slice(5, 7), 16) / 255 });
  const f = figma.createFrame(); f.name = 'ASHA didi (placeholder art)'; f.resize(140, 190); f.fills = []; f.clipsContent = false;
  const el = (name, x, y, w, h, color, isVar) => { const e = figma.createEllipse(); e.name = name; e.x = x; e.y = y; e.resize(w, h); e.fills = [isVar ? paint(color) : { type: 'SOLID', color: hex(color) }]; f.appendChild(e); return e; };
  const rc = (name, x, y, w, h, r, color, isVar) => { const e = figma.createRectangle(); e.name = name; e.x = x; e.y = y; e.resize(w, h); e.cornerRadius = r; e.fills = [isVar ? paint(color) : { type: 'SOLID', color: hex(color) }]; f.appendChild(e); return e; };
  rc('Saree', 14, 96, 112, 94, 44, 'module/maternal/fg', true);
  rc('Pallu', 72, 100, 30, 90, 14, '#F48FB1');
  rc('Neck', 60, 78, 20, 24, 8, '#B9773F');
  el('Hair back', 30, 16, 80, 70, '#2B1B17');
  el('Bun', 92, 14, 30, 30, '#2B1B17');
  el('Face', 40, 26, 60, 64, '#C68642');
  el('Hair top', 38, 18, 64, 26, '#2B1B17');
  el('Eye L', 54, 52, 6, 7, '#2B1B17'); el('Eye R', 80, 52, 6, 7, '#2B1B17');
  el('Bindi', 67.5, 40, 5, 5, '#D32F2F');
  rc('Smile', 61, 70, 18, 4, 2, '#7B3F1E');
  el('ASHA pin', 36, 120, 18, 18, 'color/accent', true);
  if (scale !== 1) f.rescale(scale);
  return f;
}

async function buildStage21(ctx) {
  await ensureIcons(ctx);
  const comps = await buildGam21Components(ctx);
  const K = P2(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, spacer, setP, icon, fixW, progress, stageSection, fitSection, phone, body, card, scrim, sheet, pinBottom, note, box, deva, beng, divider } = K;
  const inst = K.inst;
  const g = (name, variant, props = {}) => { const src = getComp(ctx, name); let c = src; if (src.type === 'COMPONENT_SET') { c = variant ? src.children.find((v) => v.name === variant) : src.defaultVariant; if (!c) throw new Error('Variant ' + variant + ' not in ' + name); } const i = c.createInstance(); setP(i, props); return i; };
  const nestedAll = (root, name) => root.findAll((n) => n.type === 'INSTANCE' && n.name === name);
  const tile = (name, state, tier, progressText, pct) => {
    const t = g('Badge tile', `State=${state}`, { Name: name });
    if (progressText) setP(t, { 'Progress text': progressText });
    const m = nestedAll(t, 'Badge medal')[0]; if (m) setP(m, { Badge: name, State: state });
    if (state === 'Earned') { const ts = t.findOne((n) => n.type === 'INSTANCE' && n.name === 'Tier stars'); if (ts) setP(ts, { Tier: String(tier || 1) }); }
    if (state === 'InProgress' && pct) { const pb = t.findOne((n) => n.type === 'INSTANCE' && n.name === 'Progress bar'); if (pb) setP(pb, { Value: String(pct) }); }
    return t;
  };
  const medal = (name, state, size) => { const m = g('Badge medal', `Badge=${name}, State=${state}`); if (size) m.rescale(size / 72); return m; };
  const hideAct = (bar, all = false) => { const a = bar.findAll((n) => n.name === 'Action'); (all ? a : a.slice(0, 1)).forEach((x) => { x.visible = false; }); };

  await figma.setCurrentPageAsync(ctx.pages.screens);
  // mark phase-1 frames as the ON / OFF baselines
  for (const n of ctx.pages.screens.children) {
    if (n.name === '02 · Home – Today') n.name = '02 · Home – Today (gamification OFF)';
    if (n.name === '09 · Profile & more') n.name = '09 · Profile & more (gamification ON)';
  }
  const sec = stageSection('2.1 · Gamification');
  const frames = [];

  // ===== G01 Today (gamification ON)
  {
    const s = phone(sec, 'M2.1-01 · Today (gamification ON)', 0, 0); frames.push(s);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Home', { Title: 'Namaste, Sunita', Subtitle: 'ASHA · Sirsi village, Ward 4' }));
    const b = body(s); b.itemSpacing = 12;
    add(b, g('Recap strip', 'State=New'));
    add(b, g('Banner', 'Type=Warning', { Title: '12 records not synced', Body: 'Last synced yesterday, 6:40 PM', Action: 'Sync now' }));
    add(b, inst('sectionHeader', null, { Title: 'To-do this week', 'Show action': false }));
    for (const [tone, t, sub, c, ic] of [['Maternal', 'Women due for ANC', '2nd & 3rd trimester visits', '12', 'pregnant'], ['Immunization', 'Children due for vaccination', 'Includes 3 overdue', '8', 'vaccine'], ['Alert', 'Suspected high-risk pregnancies', 'Refer to PHC', '3', 'warning']])
      add(b, inst('dueCard', `Tone=${tone}`, { Title: t, Subtitle: sub, Count: c, Icon: ic }));
    add(b, g('My Impact card'));
    add(s, inst('bottomNav', 'Active=Today'));
  }
  // ===== G02 My badges
  {
    const s = phone(sec, 'M2.1-02 · My badges', 1, 0); frames.push(s);
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: 'My badges', Subtitle: '6 earned · 3 in progress' })); hideAct(bar, true);
    const b = body(s);
    const sum = AL('VERTICAL', 'Summary'); pad(sum, 'spacing/md', 'spacing/md'); gap(sum, 'spacing/sm'); rad(sum, 'radius/lg'); fill(sum, 'module/couple/bg'); add(b, sum);
    const r = AL('HORIZONTAL'); r.counterAxisAlignItems = 'CENTER'; gap(r, 'spacing/sm'); add(sum, r);
    const tb = box('Trophy', 48, 48); tb.cornerRadius = 24; fill(tb, 'module/couple/fg'); tb.appendChild(icon('trophy', 'color/on-primary', 26)); r.appendChild(tb);
    const rc = AL('VERTICAL'); rc.itemSpacing = 2; r.appendChild(rc); rc.layoutSizingHorizontal = 'FILL';
    rc.appendChild(await txt('6 badges earned', 'Title/Large', 'color/on-surface')); rc.appendChild(await txt('Best streak: 9 weeks of syncing', 'Body/Small', 'color/on-surface-variant'));
    sum.appendChild(g('Streak chip', 'State=Active'));
    const grid = async (title, items) => {
      add(b, inst('sectionHeader', null, { Title: title, 'Show action': false }));
      const gr = AL('HORIZONTAL', title); gr.layoutWrap = 'WRAP'; gr.itemSpacing = 8; gr.counterAxisSpacing = 8; add(b, gr);
      for (const it of items) gr.appendChild(tile(...it));
    };
    await grid('Earned (4)', [['Steady Syncer', 'Earned', 3], ['Digital Identity', 'Earned', 2], ['Community Voice', 'Earned', 1], ['Timely Reporter', 'Earned', 2]]);
    await grid('Almost there (3)', [['Maternal Journey', 'InProgress', 0, '3 of 4 ANC', 75], ['Child Fully Protected', 'InProgress', 0, '2 of 3 children', 66], ['Complete Worker', 'InProgress', 0, '2 of 3 areas', 66]]);
    await grid('Not started (2)', [['Vulnerable Baby Cared For', 'Locked', 0, 'Not started'], ['Critical Referral Made', 'Locked', 0, 'Not started']]);
  }
  // ===== G03 Badge detail sheet
  {
    const s = phone(sec, 'M2.1-03 · Badge detail', 2, 0, true); frames.push(s);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'My badges', Subtitle: '6 earned · 3 in progress' }));
    scrim(s);
    const sh = sheet(s, 'Badge detail');
    sh.appendChild(medal('Child Fully Protected', 'InProgress', 96));
    const t = await txt('Child Fully Protected', 'Headline/Small', 'color/on-surface'); sh.appendChild(t);
    sh.appendChild(inst('tag', 'Tone=Child', { Label: 'Milestone badge · 4 tiers' }));
    const d = await txt('Complete every birth, 9 and 12 month dose for each child. Twins count separately.', 'Body/Medium', 'color/on-surface-variant'); add(sh, d); d.textAutoResize = 'HEIGHT'; d.textAlignHorizontal = 'CENTER';
    const pc = AL('VERTICAL', 'Progress'); pad(pc, 'spacing/md', 'spacing/sm'); gap(pc, 'spacing/xs'); rad(pc, 'radius/md'); fill(pc, 'module/child/bg'); add(sh, pc);
    pc.appendChild(await txt('Tier 1 · 2 of 3 children', 'Title/Small', 'color/on-surface'));
    const pb = progress(296, 0.66, 'module/child/fg', 8); pc.appendChild(pb); pb.layoutSizingHorizontal = 'FILL';
    pc.appendChild(await txt('1 more child to earn your first star', 'Body/Small', 'color/on-surface-variant'));
    const tiers = AL('HORIZONTAL', 'Tiers'); gap(tiers, 'spacing/xs'); add(sh, tiers);
    for (const [i, n] of [[1, 3], [2, 10], [3, 25], [4, 60]]) {
      const c = AL('VERTICAL', 'Tier ' + i); c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 2; pad(c, null, 'spacing/xs'); rad(c, 'radius/sm'); fill(c, 'color/surface-container-high'); tiers.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      const sr = AL('HORIZONTAL'); sr.itemSpacing = 0; c.appendChild(sr); for (let k = 0; k < i; k++) sr.appendChild(icon('star', 'color/outline', 12));
      c.appendChild(await txt(n + ' children', 'Label/Small', 'color/on-surface'));
    }
    const nt = await txt('Badges are calculated on this phone from the forms you submit. They work offline.', 'Body/Small', 'color/on-surface-variant'); add(sh, nt); nt.textAutoResize = 'HEIGHT'; nt.textAlignHorizontal = 'CENTER';
    pinBottom(s, sh);
  }
  // ===== G04 Celebration
  {
    const s = phone(sec, 'M2.1-04 · Badge earned (celebration)', 3, 0, true); frames.push(s);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'ANC visits', Subtitle: '42 pregnant women' }));
    scrim(s).opacity = 1;
    let seed = 11; const rnd = () => { seed = (seed * 16807) % 2147483647; return seed / 2147483647; };
    const cols = ['color/accent', 'module/maternal/fg', 'module/immunization/fg', 'module/household/fg', 'module/child/fg', 'module/couple/fg'];
    for (let i = 0; i < 46; i++) { const r = figma.createRectangle(); r.name = 'Confetti'; r.resize(6, 12); r.cornerRadius = 2; r.fills = [paint(cols[i % cols.length])]; s.appendChild(r); r.layoutPositioning = 'ABSOLUTE'; r.x = Math.round(rnd() * 350); r.y = Math.round(rnd() * 360); r.rotation = Math.round(rnd() * 180); }
    const c = AL('VERTICAL', 'Celebration card'); fixW(c, 312); pad(c, 'spacing/xl', 'spacing/xl'); gap(c, 'spacing/md'); c.counterAxisAlignItems = 'CENTER'; rad(c, 'radius/xl'); fill(c, 'color/surface-container');
    s.appendChild(c); c.layoutPositioning = 'ABSOLUTE';
    c.appendChild(await txt('Congratulations!', 'Headline/Large', 'color/on-surface'));
    c.appendChild(medal('Maternal Journey', 'Earned', 120));
    const n = await txt('You earned Maternal Journey', 'Title/Large', 'color/on-surface'); add(c, n); n.textAutoResize = 'HEIGHT'; n.textAlignHorizontal = 'CENTER';
    const st = AL('HORIZONTAL'); st.itemSpacing = 2; c.appendChild(st); for (let i = 0; i < 4; i++) st.appendChild(icon('star', i < 1 ? 'color/accent' : 'color/outline-variant', 20));
    const d = await txt('Sunita Devi had 4 ANC visits and delivered safely at the PHC. Thank you for walking with her.', 'Body/Medium', 'color/on-surface-variant'); add(c, d); d.textAutoResize = 'HEIGHT'; d.textAlignHorizontal = 'CENTER';
    const b1 = inst('button', 'Style=Filled, State=Enabled', { Label: 'See my badges', 'Show icon': false }); add(c, b1);
    const b2 = inst('button', 'Style=Text, State=Enabled', { Label: 'Close', 'Show icon': false }); c.appendChild(b2);
    c.x = 24; c.y = Math.round((800 - c.height) / 2) + 20;
  }
  // ===== G05 Journey
  {
    const s = phone(sec, 'M2.1-05 · My journey (from evening notification)', 4, 0); frames.push(s);
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: 'My journey', Subtitle: 'Monday, 2 November' })); hideAct(bar, true);
    const b = body(s);
    const hero = AL('VERTICAL', 'Greeting'); pad(hero, 'spacing/lg', 'spacing/lg'); gap(hero, 'spacing/xs'); rad(hero, 'radius/xl'); fill(hero, 'color/primary'); add(b, hero);
    hero.appendChild(await txt('Good evening, Sunita', 'Title/Large', 'color/on-primary'));
    const nar = await txt('Today you visited 4 families, completed 2 ANC visits and protected 2 children with vaccines.', 'Body/Large', 'color/primary-container'); hero.appendChild(nar); wrapText(nar);
    const stat = async (title, items, tone) => {
      add(b, inst('sectionHeader', null, { Title: title, 'Show action': false }));
      const r = AL('HORIZONTAL', title); gap(r, 'spacing/xs'); add(b, r);
      for (const [n, l] of items) { const c = AL('VERTICAL'); pad(c, 'spacing/sm', 'spacing/sm'); c.itemSpacing = 2; rad(c, 'radius/lg'); fill(c, `module/${tone}/bg`); r.appendChild(c); c.layoutSizingHorizontal = 'FILL'; c.appendChild(await txt(n, 'Number/Medium', `module/${tone}/fg`)); const lt = await txt(l, 'Label/Medium', 'color/on-surface'); c.appendChild(lt); wrapText(lt); }
    };
    await stat('Today', [['4', 'Home visits'], ['6', 'Forms saved'], ['2', 'Vaccines']], 'immunization');
    await stat('Since you joined', [['248', 'Households'], ['112', 'Pregnancies tracked'], ['410', 'Children vaccinated']], 'household');
    add(b, inst('sectionHeader', null, { Title: 'Recent badges', 'Show action': false }));
    const rb = AL('HORIZONTAL', 'Badges'); rb.itemSpacing = 8; add(b, rb);
    rb.appendChild(tile('Maternal Journey', 'Earned', 1)); rb.appendChild(tile('Steady Syncer', 'Earned', 3)); rb.appendChild(tile('Digital Identity', 'Earned', 2));
    add(b, inst('button', 'Style=Outlined, State=Enabled', { Label: 'View all badges', 'Show icon': false }));
  }
  // ===== G06 Recap: language
  {
    const s = phone(sec, 'M2.1-06 · Monthly Recap · choose language', 5, 0, true); frames.push(s); fill(s, 'module/couple/bg');
    s.primaryAxisAlignItems = 'CENTER'; s.counterAxisAlignItems = 'CENTER'; s.itemSpacing = 20; s.paddingLeft = s.paddingRight = 24;
    s.appendChild(ashaDidi(K, 1));
    const t = await txt('नमस्ते! अपनी मासिक झलक देखने के लिए भाषा चुनें', 'Title/Large', 'color/on-surface'); deva(t); add(s, t); t.textAutoResize = 'HEIGHT'; t.textAlignHorizontal = 'CENTER';
    const t2 = await txt('নমস্কাৰ! আপোনাৰ মাহেকীয়া সাৰাংশ চাবলৈ ভাষা বাছনি কৰক', 'Body/Large', 'color/on-surface-variant'); beng(t2); add(s, t2); t2.textAutoResize = 'HEIGHT'; t2.textAlignHorizontal = 'CENTER';
    for (const [l, f] of [['हिन्दी', deva], ['অসমীয়া', beng]]) { const bt = inst('button', 'Style=Filled, State=Enabled', { Label: l, 'Show icon': false }); add(s, bt); bt.resize(bt.width, 56); bt.layoutSizingHorizontal = 'FILL'; const lt = bt.findOne((n) => n.type === 'TEXT'); if (lt) f(lt); }
  }
  // ===== G07 Recap: scene
  {
    const s = phone(sec, 'M2.1-07 · Monthly Recap · scene', 6, 0, true); frames.push(s); fill(s, 'module/couple/bg');
    s.paddingLeft = s.paddingRight = 16; s.paddingTop = 20; s.itemSpacing = 16;
    const top = AL('HORIZONTAL', 'Progress'); top.counterAxisAlignItems = 'CENTER'; top.itemSpacing = 4; add(s, top);
    for (let i = 0; i < 5; i++) { const r = figma.createRectangle(); r.resize(50, 4); r.cornerRadius = 2; r.fills = [paint(i < 2 ? 'module/couple/fg' : 'color/surface-container')]; top.appendChild(r); r.layoutSizingHorizontal = 'FILL'; }
    top.appendChild(icon('close', 'color/on-surface', 24));
    const sp1 = AL('VERTICAL'); add(s, sp1); sp1.layoutSizingVertical = 'FIXED'; sp1.resize(sp1.width, 40); sp1.layoutSizingHorizontal = 'FILL';
    const bub = AL('VERTICAL', 'Speech bubble'); pad(bub, 'spacing/lg', 'spacing/lg'); gap(bub, 'spacing/xs'); rad(bub, 'radius/xl'); fill(bub, 'color/surface-container'); add(s, bub);
    bub.appendChild(await txt('36', 'Display/Small', 'module/couple/fg'));
    const bt = await txt('अक्टूबर में आपने 36 परिवारों से मुलाकात की और 4 गर्भवती महिलाओं का पंजीकरण किया!', 'Title/Large', 'color/on-surface'); deva(bt); bub.appendChild(bt); wrapText(bt);
    const bt2 = await txt('In October you visited 36 families and registered 4 pregnant women!', 'Body/Small', 'color/on-surface-variant'); bub.appendChild(bt2); wrapText(bt2);
    const ill = AL('HORIZONTAL', 'Character'); ill.primaryAxisAlignItems = 'MIN'; add(s, ill); ill.appendChild(ashaDidi(K, 1.2));
    const ctr = AL('HORIZONTAL', 'Controls'); ctr.counterAxisAlignItems = 'CENTER'; ctr.primaryAxisAlignItems = 'SPACE_BETWEEN'; add(s, ctr);
    for (const ic of ['chevron_left', 'chevron_right']) { const bx = box(ic, 56, 56); bx.cornerRadius = 28; fill(bx, 'color/surface-container'); bx.appendChild(icon(ic, 'color/on-surface', 28)); ctr.appendChild(bx); }
  }
  // ===== G08 Recap: end
  {
    const s = phone(sec, 'M2.1-08 · Monthly Recap · end', 7, 0, true); frames.push(s); fill(s, 'module/couple/bg');
    s.primaryAxisAlignItems = 'CENTER'; s.counterAxisAlignItems = 'CENTER'; s.itemSpacing = 16; s.paddingLeft = s.paddingRight = 24;
    s.appendChild(ashaDidi(K, 1));
    const t = await txt('शाबाश, सुनीता!', 'Headline/Large', 'color/on-surface'); deva(t); s.appendChild(t);
    const t2 = await txt('इस महीने आपने 2 नए बैज जीते', 'Title/Medium', 'color/on-surface-variant'); deva(t2); s.appendChild(t2);
    const r = AL('HORIZONTAL'); r.itemSpacing = 8; s.appendChild(r); r.appendChild(tile('Maternal Journey', 'Earned', 1)); r.appendChild(tile('Community Voice', 'Earned', 1));
    const b1 = inst('button', 'Style=Filled, State=Enabled', { Label: 'फिर से देखें', 'Show icon': true, Icon: 'sync' }); add(s, b1); const l1 = b1.findOne((n) => n.type === 'TEXT'); if (l1) deva(l1);
    const b2 = inst('button', 'Style=Text, State=Enabled', { Label: 'बंद करें', 'Show icon': false }); s.appendChild(b2); const l2 = b2.findOne((n) => n.type === 'TEXT'); if (l2) deva(l2);
  }
  // ===== G09 Evening notification (system)
  {
    const s = phone(sec, 'M2.1-09 · Evening notification (9 PM)', 0, 1, true); frames.push(s); fill(s, 'color/on-surface');
    s.paddingLeft = s.paddingRight = 16; s.paddingTop = 48; s.itemSpacing = 12; s.counterAxisAlignItems = 'CENTER';
    s.appendChild(await txt('9:00', 'Display/Small', 'color/on-primary')); s.appendChild(await txt('Monday, 2 November', 'Body/Medium', 'color/primary-container'));
    const sp1 = AL('VERTICAL'); add(s, sp1); sp1.layoutSizingVertical = 'FIXED'; sp1.resize(sp1.width, 24); sp1.layoutSizingHorizontal = 'FILL';
    const nc = async (title, bodyText, action, ic) => {
      const c = AL('VERTICAL', 'Notification'); pad(c, 'spacing/md', 'spacing/sm'); gap(c, 'spacing/2xs'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); add(s, c);
      const h = AL('HORIZONTAL'); h.counterAxisAlignItems = 'CENTER'; h.itemSpacing = 6; c.appendChild(h);
      const ai = box('App icon', 20, 20); ai.cornerRadius = 6; fill(ai, 'color/primary'); ai.appendChild(icon(ic, 'color/on-primary', 14)); h.appendChild(ai);
      h.appendChild(await txt('Sakhi · now', 'Label/Small', 'color/on-surface-variant'));
      c.appendChild(await txt(title, 'Title/Small', 'color/on-surface'));
      const bb = await txt(bodyText, 'Body/Medium', 'color/on-surface-variant'); c.appendChild(bb); wrapText(bb);
      if (action) c.appendChild(await txt(action, 'Label/Large', 'color/primary'));
    };
    await nc('Great work today, Sunita!', 'You saved 6 forms today. Sync before 9 AM to keep your 6-week streak.', 'OPEN MY JOURNEY', 'star');
    await nc('Your October recap is ready', 'See the impact you made this month.', 'WATCH NOW', 'play');
  }
  // ===== G10 In-app notifications panel
  {
    const s = phone(sec, 'M2.1-10 · Notifications panel', 1, 1); frames.push(s);
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: 'Notifications', Subtitle: '3 new' })); hideAct(bar, true);
    const b = body(s); b.itemSpacing = 8;
    for (const [t, d, time, ic, tone, unread] of [['New badge: Maternal Journey', 'Sunita Devi completed her care journey.', '2h', 'trophy', 'maternal', true], ['Your October recap is ready', 'Watch your month in 1 minute.', '1d', 'play', 'couple', true], ['Great work today!', '6 forms saved. Your streak continues.', '1d', 'flame', 'child', true], ['2 records failed to sync', 'Eligible couple tracking · tap to fix', '2d', 'warning', 'disease', false], ['Incentive approved · ₹1,200', 'September claim verified by ANM.', '5d', 'wallet', 'household', false]]) {
      const r = AL('HORIZONTAL', 'Item'); pad(r, 'spacing/sm', 'spacing/sm'); gap(r, 'spacing/sm'); rad(r, 'radius/lg'); fill(r, unread ? 'color/surface-container' : 'color/surface'); if (unread) stroke(r, 'color/outline-variant'); add(b, r);
      const it = box('Icon', 40, 40); it.cornerRadius = 20; fill(it, `module/${tone}/bg`); it.appendChild(icon(ic, `module/${tone}/fg`, 20)); r.appendChild(it);
      const c = AL('VERTICAL'); c.itemSpacing = 2; r.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      const tt = await txt(t, 'Title/Small', 'color/on-surface'); c.appendChild(tt); wrapText(tt);
      const dd = await txt(d, 'Body/Small', 'color/on-surface-variant'); c.appendChild(dd); wrapText(dd);
      const rr = AL('VERTICAL'); rr.counterAxisAlignItems = 'MAX'; rr.itemSpacing = 6; r.appendChild(rr); rr.appendChild(await txt(time, 'Label/Small', 'color/on-surface-variant'));
      if (unread) { const dot = figma.createEllipse(); dot.resize(8, 8); dot.fills = [paint('color/primary')]; rr.appendChild(dot); }
    }
  }
  // ===== G11 Profile (gamification OFF)
  {
    const s = phone(sec, 'M2.1-11 · Profile (gamification OFF)', 2, 1); frames.push(s);
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 8;
    const head = AL('VERTICAL', 'Profile header'); pad(head, 'spacing/lg', 'spacing/lg'); gap(head, 'spacing/md'); rad(head, 'radius/xl'); fill(head, 'color/primary'); add(b, head);
    const hr = AL('HORIZONTAL'); hr.counterAxisAlignItems = 'CENTER'; gap(hr, 'spacing/md'); add(head, hr);
    const av = inst('avatar', null, { Initials: 'SK' }); av.resize(64, 64); hr.appendChild(av);
    const hc = AL('VERTICAL'); hc.itemSpacing = 2; hr.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    hc.appendChild(await txt('Sunita Kumari', 'Title/Large', 'color/on-primary')); hc.appendChild(await txt('ASHA · Employee ID 102345', 'Body/Medium', 'color/primary-container'));
    const stats = AL('HORIZONTAL', 'Stats'); gap(stats, 'spacing/xs'); add(head, stats);
    for (const [v, l] of [['₹3,450', 'Incentive this month'], ['94%', 'Visits on time']]) { const c = AL('VERTICAL'); pad(c, 'spacing/sm', 'spacing/sm'); c.itemSpacing = 2; rad(c, 'radius/md'); c.fills = [{ type: 'SOLID', color: { r: 1, g: 1, b: 1 }, opacity: 0.14 }]; stats.appendChild(c); c.layoutSizingHorizontal = 'FILL'; c.appendChild(await txt(v, 'Title/Medium', 'color/on-primary')); const lt = await txt(l, 'Label/Small', 'color/primary-container'); c.appendChild(lt); wrapText(lt); }
    const grp = AL('VERTICAL', 'My work'); pad(grp, 'spacing/xs', 'spacing/xs'); rad(grp, 'radius/lg'); fill(grp, 'color/surface-container'); stroke(grp, 'color/outline-variant'); add(b, grp);
    for (const [l, ic] of [['My profile details', 'person'], ['Incentives', 'wallet'], ['Dashboard', 'dashboard'], ['Supervisor', 'group']]) add(grp, inst('listItem', 'Active=false', { Label: l, Icon: ic }));
    add(s, inst('bottomNav', 'Active=Profile'));
  }
  // notes
  await note(sec, 120 + 3 * 440, 160 + 1500, 760,
    'GAMIFICATION RULES FOR ALL STAGES\n' +
    '• ON/OFF per ASHA (FLW-1251 / AMM-2556). OFF is the baseline: Today = screen 02, Profile = M2.1-11 (no badges stat or menu, no My Impact card, no recap strip, no celebrations, no evening notification).\n' +
    '• ON adds: Recap strip (days 1–7), My Impact card (after the to-do list, never before work), Badges in Profile, celebration dialog, 9 PM notification, Journey.\n' +
    '• No leaderboards or ranking between ASHAs. Badges celebrate real outcomes (BadgeDefinitions.kt) and explain how to earn them.\n' +
    '• Every module form: after save, if a badge moved forward, show Snackbar Type=Badge (see M2.0-S6); otherwise Type=Success.\n' +
    '• Badge art here is a placeholder set in module colours; tiers (up to 4 stars) follow the milestones in code.\n' +
    '• Monthly Recap content exists only in Hindi and Assamese (recap_content.json); the English line is for reviewers.');
  fitSection(sec);
  return ['2.1 components: ' + comps.length, '2.1 screens: ' + frames.length];
}
