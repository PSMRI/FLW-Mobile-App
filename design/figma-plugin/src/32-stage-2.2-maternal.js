// ---------------------------------------------------------------------------
// Stage 2.2 · Maternal health
// Sources: ui/home_activity/maternal_health/*, configuration/PregnantWomanRegistrationDataset,
// PregnantWomanAncVisitDataset, PMSMAFormDataset, DeliveryOutcomeDataset, PncFormDataset,
// InfantRegistrationDataset, IconDataset.getMotherCareDataset
// 2026-11-05 gap fix: gap_B (maternal rows) → edited 02–09, 12–16, added 17–42. Code is the source of truth.
// Infant / child-reg / HBNC / HBYC rows are covered in stage 2.4; HRP rows in stage 2.6.
// ---------------------------------------------------------------------------
async function buildStage22(ctx) {
  await ensureIcons(ctx);
  await kitFixes(ctx);
  await ensureSearchMic(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, spacer, setP, icon, box, inst, kinst, phone, body, card, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, overlayCenter, snack, progress, fixW } = S;
  const { actionCard, dialog, visitSheet, optionSheet, searchRow, viewMode, uploads, rules, caption } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  ctx.rowGap = 1800;
  const sec = stageSection('2.2 · Maternal health');
  const frames = [];
  const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };

  // ---- local helpers (gap fix)
  // "Sorted: …" label above maternal lists (PwRegistrationListFragment.kt:99 and same block in other lists)
  const sortLabel = async (p, v = 'Newest first') => { const r = G.row(p, 'Sort label', 6); r.appendChild(icon('sort', 'color/on-surface-variant', 16)); r.appendChild(await txt('Sorted: ' + v, 'Label/Medium', 'color/on-surface-variant')); return r; };
  // Recolour the button whose label is `label` (red = no record yet, green = record exists; C7)
  const tint = (root, lbl, v) => {
    for (const t of root.findAll((n) => n.type === 'TEXT' && n.characters === lbl)) {
      let b = t.parent; while (b && b !== root && b.type !== 'INSTANCE') b = b.parent;
      if (!b || b.type !== 'INSTANCE') continue;
      const bg = b.findOne((n) => n.name === 'Container' || n.name === 'Background');
      (bg && 'fills' in bg ? bg : b).fills = [paint(v)];
    }
  };
  // Toast (Android toast look) placed absolutely in a frame
  const toast = async (s, msg, y) => {
    const t = AL('HORIZONTAL', 'Toast'); pad(t, 'spacing/md', 'spacing/sm'); t.cornerRadius = 20; t.fills = [{ type: 'SOLID', color: { r: 0.19, g: 0.2, b: 0.22 } }];
    s.appendChild(t); t.layoutPositioning = 'ABSOLUTE'; fixW(t, 300);
    const x = await txt(msg, 'Body/Medium', 'color/on-primary'); t.appendChild(x); wrapText(x); x.textAlignHorizontal = 'CENTER';
    t.x = 30; t.y = y || (s.height - t.height - 96); return t;
  };
  // Switch row (Delivered / Not delivered switch on the ANC card)
  const toggle = async (p, lbl, on, sub) => {
    const r = AL('HORIZONTAL', 'Switch row · ' + lbl); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 8; add(p, r);
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 0; r.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    const t = await txt(lbl, 'Label/Large', 'color/on-surface'); col.appendChild(t); wrapText(t);
    if (sub) { const u = await txt(sub, 'Body/Small', 'color/on-surface-variant'); col.appendChild(u); wrapText(u); }
    const tr = box('Switch', 44, 24); tr.primaryAxisAlignItems = on ? 'MAX' : 'MIN'; tr.paddingLeft = 3; tr.paddingRight = 3; tr.cornerRadius = 12;
    fill(tr, on ? 'color/primary' : 'color/surface-container-high'); stroke(tr, 'color/outline');
    const k = figma.createEllipse(); k.resize(18, 18); k.fills = [paint(on ? 'color/on-primary' : 'color/outline')]; tr.appendChild(k); r.appendChild(tr);
    return r;
  };
  // ANC card as built by AncVisitListAdapter (rv_item_pregnancy_visit.xml)
  const ancCard = async (p, d) => {
    const c = await actionCard(p, { name: d.name, meta: d.meta, sync: d.sync || 'Synced', tags: d.tags, rows: d.rows, icons: d.noCall ? [] : [['call', 'Call']], actions: d.actions, footnote: d.footnote });
    if (d.warn) { const hdr = c.findOne((n) => n.name === 'Header'); if (hdr) hdr.appendChild(icon('warning', 'color/warning', 22)); }
    if (d.delivered !== undefined) {
      const t = await toggle(c, 'Delivery status', d.delivered, d.delivered ? 'Delivered' : 'Not delivered · switch shown after 22 weeks');
      const ar = c.findOne((n) => n.name === 'Actions'); if (ar) c.insertChild(c.children.indexOf(ar), t);
    }
    return c;
  };
  const heading = async (p, s) => { const t = await txt(s, 'Title/Small', 'color/on-surface'); add(p, t); return t; };

  // 01 Hub
  {
    const s = F('M2.2-01 · Maternal health hub', 0, 0);
    screenTop(s, 'Maternal health', '42 pregnant · 18 PNC mothers');
    const b = body(s);
    await stat(b, [['12', 'Due for ANC', 'maternal'], ['5', 'High risk', 'disease'], ['3', 'Deliveries due', 'child']]);
    add(b, inst('sectionHeader', null, { Title: 'Pregnancy', 'Show action': false }));
    tileGrid(b, [['maternal', 'Pregnancy registration', '6 to register', 'pregnant'], ['maternal', 'ANC / pregnancy tracking', '42 pregnant', 'calendar'], ['disease', 'PMSMA & high risk', '5 high risk', 'warning'], ['records', 'Abortion', '1 recorded', 'form']]);
    add(b, inst('sectionHeader', null, { Title: 'Delivery & after', 'Show action': false }));
    tileGrid(b, [['child', 'Delivery outcome', '3 due', 'child'], ['maternal', 'PNC mothers', '18 active', 'favorite'], ['child', 'Infant registration', '2 pending', 'child'], ['child', 'Child registration', '9 pending', 'person_add']]);
    add(b, inst('sectionHeader', null, { Title: 'Referrals', 'Show action': false }));
    tileGrid(b, [['care', 'HWC referral list', '2 referred', 'send']]);
  }
  // 02 Registration list
  {
    const s = F('M2.2-02 · Pregnancy registration list', 1, 0);
    screenTop(s, 'Pregnancy registration', 'Women 15–49 · 6 to register');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'To register', 'Count 1': '6', 'Label 2': 'Registered', 'Count 2': '42', 'Label 3': 'All', 'Count 3': '186' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or HH ID', { sort: true, filter: true });
    await sortLabel(b);
    banner(b, 'Info', 'Missed period list', '4 women reported a missed period this month. Confirm and register.', 'View');
    recordCard(b, { name: 'Anita Devi', meta: 'W/o Suresh · 28 yrs · missed period 3 Oct', tag: 'Pregnancy confirmed (test)', tone: 'Maternal', action: 'Register' });
    recordCard(b, { name: 'Pooja Kumari', meta: 'W/o Raju · 22 yrs · EC since 2024', tag: 'Missed period', tone: 'Neutral', action: 'Register' });
    recordCard(b, { name: 'Kiran Yadav', meta: 'W/o Vinod · 19 yrs', tag: 'Missed period', tone: 'Neutral', action: 'Register' });
    recordCard(b, { name: 'Meena Kumari', meta: 'W/o Ajay · 34 yrs', tag: 'Draft saved · 2 Nov', tone: 'Child', action: 'Continue' });
    const reg = await actionCard(b, { name: 'Sunita Devi', meta: 'W/o Ramesh · 26 yrs · registered 20 Jul', sync: 'Synced', tags: [['Maternal', 'Registered · 24 wks']], actions: [['Filled', 'View']] });
    tint(reg, 'View', 'color/success');
    tint(s, 'Register', 'color/error');
    const f = inst('fab', null, { Label: 'New registration', Icon: 'person_add' }); s.appendChild(f); f.layoutPositioning = 'ABSOLUTE'; f.x = 360 - 16 - f.width; f.y = s.height - 24 - f.height;
    rules(s, [
      'Card button: red "Register" when no registration, green "View" once registered [PwRegistrationListAdapter.kt:60-64]',
      'Sync icon shows only after a registration exists [PwRegistrationListAdapter.kt:60]',
      'Filter icon → "Beneficiaries with RCH ID" dialog (M2.2-17) [PwRegistrationListFragment.kt:52-72]',
      'Sort icon → sort sheet (M2.2-18). Mic = voice search [PwRegistrationListFragment.kt:99,142]',
      'Supervisor role: button hidden, list read-only (M2.2-42) [PwRegistrationListAdapter.kt:55]',
      'Tabs To register / Registered / All: Not in app today – needs dev (code has one list)',
      '"Draft saved · Continue": Not in app today – needs dev',
    ]);
  }
  // 03 Pregnancy registration form
  {
    const s = F('M2.2-03 · Pregnancy registration form', 4, 0);
    screenTop(s, 'Register pregnancy', 'Anita Devi · W/o Suresh', 'more');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Pre-filled from earlier records', 'LMP from the last EC-tracking visit, 2 children and 2 completed pregnancies from the EC record, risk answers from the HRP assessment.', null);
    section(b, 'Done', 'Registration details', 'Complete');
    section(b, 'Open', 'Pregnancy dates', '1 of 3', 2);
    date(b, 'LMP date * (from EC tracking 12 Oct)', '18 Jul 2026');
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    for (const [l, v] of [['Weeks pregnant', '15 wks'], ['EDD', '24 Apr 2027']]) { const x = kinst('Read-only field', null, { Label: l, Value: v }); r.appendChild(x); x.layoutSizingHorizontal = 'FILL'; }
    section(b, 'Closed', 'Tests & measurements', '9 fields', 3);
    section(b, 'Closed', 'Past history', '6 fields', 4);
    section(b, 'Open', 'High-risk check', '8 questions', 5);
    const qs = [['Age below 18 or above 35?', 1], ['Height below 140 cm?', 1], ['More than 3 deliveries?', 1], ['Last delivery less than 18 months ago?', 0], ['Rh negative?', 1], ['Previous delivery at home?', 0], ['Bad obstetric history?', 1], ['Twins / multiple pregnancy?', 1]];
    for (const [q, sel] of qs) await seg(b, q, ['Yes', 'No'], sel);
    auto(b, 'Mitanin only: Rh negative (auto when blood group is negative, e.g. B −ve)', 'Yes');
    banner(b, 'Warning', 'Possible high-risk pregnancy', '2 risk factors found. Confirm with the ANM / MO and plan PMSMA visit.', null);
    bottomBar(s, 'Save draft', 'Register');
    rules(s, [
      'Pre-fill: EC registration, last EC-tracking date (LMP), HRP assessment, no. of children, completed pregnancies, previous closed registration [PregnancyRegistrationFormViewModel.kt:111-120]',
      'Saving also writes the HRP "Assess high risk (pregnant)" record (M2.6-03) and updates the beneficiary (reproductive status → pregnant) [PregnancyRegistrationFormViewModel.kt:159-171]',
      'Mitanin: negative blood group auto-sets "Rh negative = Yes" [PregnantWomanRegistrationDataset.kt:128,1008-1013]',
      'Saved registration opens view-only with Edit, no time limit (M2.2-19) [PregnancyRegistrationFormFragment.kt:43,71]',
      'After save: back to the list, sync starts. No next-step prompt [PregnancyRegistrationFormFragment.kt:85-90]',
      '"Save draft": Not in app today – needs dev',
    ]);
  }
  // 04 Pregnancy timeline (detail)
  {
    const s = F('M2.2-04 · Pregnant woman · care timeline', 0, 1);
    screenTop(s, 'Sunita Devi', 'W/o Ramesh · 26 yrs · HH 7012 3345 0012', 'more');
    const b = body(s); b.itemSpacing = 12;
    await person(b, 'SD', 'Sunita Devi', '24 weeks · EDD 15 Feb 2027 · RCH 1234 5678 9012', [['Maternal', 'Pregnant · 2nd trimester'], ['Alert', 'High risk · Hb 8.9'], ['Success', 'ABHA linked']]);
    const acts = AL('HORIZONTAL', 'Quick actions'); gap(acts, 'spacing/xs'); add(b, acts);
    for (const [l, ic] of [['Call', 'call'], ['Home visit', 'home'], ['Counselling', 'help'], ['Visits', 'calendar']]) {
      const t = AL('VERTICAL', l); t.counterAxisAlignItems = 'CENTER'; t.itemSpacing = 4; pad(t, null, 'spacing/sm'); rad(t, 'radius/md'); fill(t, 'color/primary-subtle'); acts.appendChild(t); t.layoutSizingHorizontal = 'FILL';
      t.appendChild(icon(ic, 'color/primary', 22)); t.appendChild(await txt(l, 'Label/Medium', 'color/primary'));
    }
    add(b, inst('sectionHeader', null, { Title: 'Care journey', 'Show action': false }));
    const tl = AL('VERTICAL', 'Timeline'); add(b, tl);
    const steps = [['Registered', '20 Jul · 2 wks', 'done'], ['ANC 1', '14 Aug · 12 wks', 'done'], ['ANC 2', '20 Sep · 18 wks', 'done'], ['PMSMA', '9 Oct · PHC Sirsi', 'done'], ['ANC 3', 'Due by 12 Nov', 'due'], ['ANC 4', 'After 28 weeks', 'next'], ['Delivery', 'EDD 15 Feb 2027 · plan: PHC', 'next'], ['PNC (7 visits)', 'Day 1 to day 42', 'next']];
    for (let i = 0; i < steps.length; i++) {
      const [t, d, st] = steps[i];
      const row = AL('HORIZONTAL', t); gap(row, 'spacing/sm'); add(tl, row);
      const railc = AL('VERTICAL', 'Rail'); railc.counterAxisAlignItems = 'CENTER'; row.appendChild(railc);
      const dot = box('Dot', 28, 28); dot.cornerRadius = 14;
      if (st === 'done') { fill(dot, 'color/success'); dot.appendChild(icon('check', 'color/on-primary', 16)); }
      else if (st === 'due') { fill(dot, 'color/warning'); dot.appendChild(icon('clock', 'color/on-primary', 16)); }
      else { fill(dot, 'color/surface-container-high'); stroke(dot, 'color/outline-variant'); }
      railc.appendChild(dot);
      if (i < steps.length - 1) { const ln = figma.createRectangle(); ln.resize(2, 30); ln.fills = [paint(st === 'done' ? 'color/success' : 'color/outline-variant')]; railc.appendChild(ln); }
      const col = AL('VERTICAL'); col.itemSpacing = 2; col.paddingTop = 4; row.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      col.appendChild(await txt(t, 'Title/Small', st === 'next' ? 'color/on-surface-variant' : 'color/on-surface'));
      col.appendChild(await txt(d, 'Body/Small', st === 'due' ? 'color/warning' : 'color/on-surface-variant'));
      if (st === 'due') { const x = inst('button', 'Style=Filled, State=Enabled', { Label: 'Start ANC 3', 'Show icon': false }); row.appendChild(x); x.resize(x.width, 40); }
    }
    rules(s, [
      '"Home visit": Mitanin only. Add while < 9 home visits and ≥ 30 days since the last [AncVisitListAdapter.kt:136-148, RecordsRepo.kt:629-657]',
      'Other flavours: Home visit / Counselling actions hidden',
      'This detail screen is design-only: in code the ANC list card (M2.2-20) holds these actions',
    ]);
  }
  // 05 ANC visit form (complete reference)
  {
    const s = F('M2.2-05 · ANC visit form (full)', 0, 2);
    screenTop(s, 'ANC visit 3', 'Sunita Devi · 24 weeks', 'more');
    const b = body(s); b.itemSpacing = 12;
    section(b, 'Open', 'Visit', '4 fields', 1);
    date(b, 'ANC date *', '02 Nov 2026');
    await caption(b, 'Allowed: from last ANC + 28 days (ANC 1: LMP + 5 weeks) up to today');
    select(b, 'Place of ANC *', 'Sub-centre / HWC'); select(b, 'ANC period *', 'ANC 3 (24–28 weeks)');
    await caption(b, 'Only ANC numbers after the last saved one are offered (max 8)');
    add(b, kinst('Info row', null, { Label: 'Weeks of pregnancy', Value: '24 wks 3 days' }));
    section(b, 'Open', 'Pregnancy status', '3 questions', 2);
    await seg(b, 'Any abortion since last visit?', ['Yes', 'No'], 1);
    await seg(b, 'Is the woman alive?', ['Yes', 'No'], 0);
    await seg(b, 'Has she delivered? (from 23 weeks)', ['Yes', 'No'], 1);
    section(b, 'Open', 'Examination', '6 fields', 3);
    steppers(b, [['Weight (kg)', '54'], ['Hb (g/dl)', '8.9']]);
    steppers(b, [['BP systolic', '120'], ['BP diastolic', '80']]);
    text(b, 'Fundal height / uterus size (weeks)', '24');
    await seg(b, 'Urine albumin', ['Present', 'Absent'], 1); await seg(b, 'Random blood sugar test', ['Done', 'Not done'], 0);
    section(b, 'Done', 'Td / TT doses', 'Saved on registration');
    section(b, 'Closed', 'Supplements', '2 fields', 5);
    section(b, 'Open', 'High risk', 'Hb below 9', 6);
    await seg(b, 'Any high-risk condition?', ['Yes', 'No'], 0);
    const rv = reveal(b); select(rv, 'Condition *', 'Severe anaemia (Hb < 9)'); select(rv, 'Referral facility', 'PHC Sirsi'); await seg(rv, 'Is HRP confirmed by ANM / MO?', ['Yes', 'No'], 1);
    section(b, 'Open', 'MCP card', 'Camera, gallery or PDF', 7);
    const ph = AL('HORIZONTAL', 'MCP photos'); gap(ph, 'spacing/sm'); add(b, ph); ph.appendChild(kinst('Photo tile', 'State=Captured')); ph.appendChild(kinst('Photo tile', 'State=Empty'));
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, [
      '"Has she delivered?" only from 23 weeks; Yes marks her delivered → Delivery outcome list [PregnantWomanAncVisitDataset.kt:541-545, PwAncFormViewModel.kt:180-185]',
      '≤ 12 weeks: Folic acid instead of IFA, fundal height hidden (M2.2-25) [Dataset.kt:546-572]',
      'Date & ANC-number limits as captions above [PregnantWomanAncVisitDataset.kt:507-523]',
      'Td/TT dates live on the registration record, shared by all visits; Td-2 hidden on ANC 1 [Dataset.kt:504-505,596-598]',
      'Opened from PMSMA list: Place = PMSMA site, locked (M2.2-25) [Dataset.kt:488-493]',
      'Xushrukha: Hb and BP required (*). Other flavours optional [Dataset.kt:480-487]',
      'Abortion = Yes: closes pregnancy, deactivates all ANC records, woman back to Eligible couple [PwAncFormViewModel.kt:187-216]',
      'Alive = No: marks beneficiary dead (date, cause, place) and opens MDSR after save (M2.2-16, M2.2-28) [PwAncFormViewModel.kt:218-250]',
      'Mitanin: MCP card section hidden. Upload chooser M2.2-26, viewer M2.2-27 [Dataset.kt:475, PwAncFormFragment.kt:69-189]',
      'Saved visit: view-only; Edit only when opened as the latest visit (M2.2-19) [PwAncFormFragment.kt:279]',
      '"Save draft": Not in app today – needs dev',
    ]);
  }
  // 06 Antenatal counselling / home visit (Mitanin only)
  {
    const s = F('M2.2-06 · ANC home visit & counselling', 0, 3);
    screenTop(s, 'ANC home visit', 'Mitanin only · Sunita Devi', 'more');
    const b = body(s); b.itemSpacing = 12;
    await infoCard(b, 'Sunita Devi', [['Husband', 'Ramesh Kumar'], ['Age', '26 yrs'], ['Registered', '20 Jul 2026'], ['LMP · weeks', '18 May 2026 · 24 wks'], ['EDD', '22 Feb 2027'], ['Phone', '98765 43210'], ['Last home visit', '02 Oct 2026']]);
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    for (const [l, v] of [['Visit no.', 'Visit 3'], ['Visit day', 'Day 168'], ['Due date', '01 Nov']]) { const x = kinst('Read-only field', null, { Label: l, Value: v }); r.appendChild(x); x.layoutSizingHorizontal = 'FILL'; }
    date(b, 'Date of home visit *', '02 Nov 2026');
    auto(b, 'Age risk (auto: below 18 or above 35)', 'No');
    section(b, 'Done', 'History (from visit 1 · locked)', '9 answers');
    auto(b, 'Gap from last child below 2 years', 'No'); auto(b, 'Height below 140 cm', 'No'); auto(b, 'Previous C-section', 'No');
    await seg(b, 'Twin pregnancy? (carried, editable)', ['Yes', 'No'], 1);
    await seg(b, 'Sickle cell disease? (carried, editable)', ['Yes', 'No'], 1);
    section(b, 'Open', 'Danger signs today', '12 questions', 2);
    await seg(b, 'Swelling of hands / face?', ['Yes', 'No'], 1);
    await seg(b, 'High BP / severe headache?', ['Yes', 'No'], 1);
    await seg(b, 'Convulsions?', ['Yes', 'No'], 1);
    await seg(b, 'Reduced fetal movement?', ['Yes', 'No'], 1);
    await seg(b, 'Bleeding?', ['Yes', 'No'], 1);
    await caption(b, '+ anaemia, malaria, jaundice and more (server-driven list)');
    await label(b, 'Photo (optional)');
    const ph = AL('HORIZONTAL', 'Photo'); add(b, ph); ph.appendChild(kinst('Photo tile', 'State=Empty'));
    await checks(b, 'Counselling topics (design only)', [['Diet and rest', true], ['IFA & calcium every day', true], ['Danger signs in pregnancy', true], ['Birth preparedness & transport (108 / 102)', false], ['Institutional delivery & JSY', false], ['Breastfeeding within 1 hour', false], ['Family planning after delivery', false]]);
    text(b, 'Notes (optional)', 'Husband will arrange transport', true);
    banner(b, 'Info', 'Next ANC due 12 Nov', 'A reminder will appear on Today 3 days before.', null);
    bottomBar(s, null, 'Save visit');
    rules(s, [
      'Mitanin only: entry buttons hidden in other flavours [AncVisitListAdapter.kt:142-148]',
      'Server-driven form, ~21 risk / danger-sign questions [AntenatalCounsellingViewModel.kt:152-163]',
      'Header summary card from the beneficiary record [fragment_antenatal_counselling.xml:19-265]',
      'Age risk auto (< 18 or > 35). Visit no., day, due date locked [ViewModel.kt:68-75,376-381]',
      'History answers carried from the previous visit and locked; twin & sickle cell carried but editable [ViewModel.kt:166-172,390-433]',
      'Any danger sign → "refer to HWC?" dialog (M2.2-31) [AntenatalCounsellingFragment.kt:584-605]',
      'After save → "Continue to ANC visit?" (M2.2-32) [Fragment.kt:438-465]',
      'Date ≤ today, ≥ previous visit + 1 day; "Visit N" numbering [ViewModel.kt:651-677]',
      'Saved visit view-only; Edit only on the latest visit (M2.2-19). History sheet M2.2-29',
      'Counselling topics checklist and "Next ANC due" banner: Not in app today – needs dev',
    ]);
  }
  // 07 PMSMA form
  {
    const s = F('M2.2-07 · PMSMA visit (9th of month)', 2, 4);
    screenTop(s, 'PMSMA visit', 'Sunita Devi · PHC Sirsi · 9 Nov', 'more');
    const b = body(s); b.itemSpacing = 12;
    await infoCard(b, 'Woman details (auto)', [['RCH / MCTS no.', '1234 5678 9012'], ['Husband', 'Ramesh Kumar'], ['Mobile', '98765 43210'], ['ANC visits so far', '2'], ['Last PMSMA visit', '9 Oct 2026'], ['PMSMA visit no.', '2'], ['LMP · EDD', '18 May · 15 Feb 2027']]);
    section(b, 'Open', 'Mother status', '2 fields', 2); steppers(b, [['Weight (kg)', '55'], ['BP (sys)', '118']]);
    section(b, 'Open', 'Fetal status', '2 fields', 3); text(b, 'Fetal heart rate (per min)', '142'); await seg(b, 'Twin pregnancy?', ['Yes', 'No'], 1);
    section(b, 'Done', 'Investigations', 'Complete');
    section(b, 'Done', 'Interventions', 'Complete');
    section(b, 'Open', 'High-risk factors', '3 fields', 6);
    await seg(b, 'High-risk symptoms found?', ['Yes', 'No'], 0);
    const rv = reveal(b); text(rv, 'Reason *', 'Severe anaemia (Hb 8.9)'); await seg(rv, 'Referred to higher facility?', ['Yes', 'No'], 0);
    section(b, 'Closed', 'Advice & MO sign', '3 fields', 7);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, [
      'Dormant in code: PMSMA button hidden in ANC list (hidePmsma = true); the PMSMA card button opens the ANC form instead (M2.2-25)',
      'High risk = Yes → alert "HRP case, visit nearest HWC or call 104" (M2.2-35) [PmsmaFragment.kt:92-99]',
      'Pre-fill includes ANC count and last PMSMA visit; PMSMA visit no. is per woman [PmsmaViewModel setUpFirstPage]',
      'Edit only from the latest PMSMA visit (M2.2-19, sheet M2.2-34) [PmsmaFragment.kt:119]',
      'Consent dialog defined but never shown (M2.2-36) [PmsmaFragment.kt:42-65]',
    ]);
  }
  // 08 PMSMA / high-risk list
  {
    const s = F('M2.2-08 · High-risk pregnancies list', 0, 4);
    screenTop(s, 'High-risk pregnancies', '5 confirmed · 3 suspected');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'Suspected', 'Count 1': '3', 'Label 2': 'Confirmed', 'Count 2': '5', 'Label 3': 'Referred', 'Count 3': '4' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile', { sort: true });
    await sortLabel(b);
    chips(b, ['Next PMSMA 9 Dec', 'Anaemia', 'BP']);
    recordCard(b, { name: 'Sunita Devi', meta: '24 wks · Hb 8.9 g/dl', tag: 'Severe anaemia', tone: 'Alert', action: 'PMSMA', sync: 'Pending' });
    recordCard(b, { name: 'Rekha Yadav', meta: '33 wks · BP 150/96', tag: 'High BP · refer today', tone: 'Alert', action: 'Refer' });
    recordCard(b, { name: 'Kiran Yadav', meta: '11 wks · age 17', tag: 'Age below 18', tone: 'Maternal', action: 'PMSMA' });
    await heading(b, 'Card as in code (Mitanin build)');
    await ancCard(b, { name: 'Lata Bai', meta: '30 yrs · 26 wks · EDD 02 Feb 2027', tags: [['Alert', 'High risk']], rows: [['Last ANC', '09 Oct 2026'], ['ANC done', '3 of 8']], delivered: false, actions: [['Filled', 'PMSMA', true], ['Outlined', 'PMSMA visits'], ['Outlined', 'Add home visit'], ['Text', 'View home visits']], footnote: 'PMSMA greyed until the next ANC is due (8 Nov)' });
    banner(b, 'Info', 'PMSMA day: 9th of every month', 'Bring all high-risk women to the PHC for a free check-up by the doctor.', null);
    rules(s, [
      'One list of all high-risk pregnant women [PwAncVisitsListViewModel.kt:52,62-70]',
      '"PMSMA" button opens the ANC form with Place = PMSMA locked (M2.2-25), greyed until next ANC due [PmsmaHighRiskListFragment.kt:96-104]',
      '"PMSMA visits" → high-risk or PMSMA-site ANC visits sheet (M2.2-33) [Fragment.kt:118-133]',
      'Delivered switch + date picker as ANC list (> 22 weeks) (M2.2-21/22)',
      'Mitanin: Add / View home visit buttons also here [AncVisitListAdapter.kt:136-148]',
      'Warning icon when last visit 90–365 days ago; supervisor: buttons hidden (C3, C8)',
      'Tabs Suspected / Confirmed / Referred and "Refer" button: Not in app today – needs dev',
    ]);
  }
  // 09 Delivery outcome form
  {
    const s = F('M2.2-09 · Delivery outcome form', 1, 5);
    screenTop(s, 'Delivery outcome', 'Sunita Devi · EDD 15 Feb 2027', 'more');
    const b = body(s); b.itemSpacing = 12;
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    const d1 = date(r, 'Delivery date * (default today)', '12 Feb 2027'); d1.layoutSizingHorizontal = 'FILL';
    const d2 = S.tf(r, 'Filled', { Label: 'Time', Value: '04:35 AM' }, 'clock'); d2.layoutSizingHorizontal = 'FILL';
    select(b, 'Place of delivery *', 'PHC Sirsi (institutional)');
    await radios(b, 'Type of delivery *', ['Normal', 'Caesarean (C-section)', 'Assisted (forceps / vacuum)'], 0);
    await seg(b, 'Any complication?', ['Yes', 'No'], 1);
    await caption(b, 'Complication = "Death" reveals date, cause and place of death (M2.2-28)');
    add(b, inst('sectionHeader', null, { Title: 'Outcome', 'Show action': false }));
    steppers(b, [['Live births', '1'], ['Still births', '0']]);
    const r2 = AL('HORIZONTAL'); gap(r2, 'spacing/sm'); add(b, r2);
    const d3 = date(r2, 'Discharge date', '14 Feb 2027'); d3.layoutSizingHorizontal = 'FILL';
    const d4 = S.tf(r2, 'Filled', { Label: 'Time', Value: '11:00 AM' }, 'clock'); d4.layoutSizingHorizontal = 'FILL';
    await seg(b, 'JSY beneficiary?', ['Yes', 'No'], 0);
    await label(b, 'Documents (not in Mitanin)');
    add(b, kinst('File row', 'State=Done', { 'File name': 'MCP_card_page1.jpg' }));
    add(b, kinst('File row', 'State=Done', { 'File name': 'JSY_voucher.jpg' }));
    await caption(b, 'Camera, gallery or PDF (M2.2-26)');
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, [
      'Complication = Death → mother marked dead (date, cause, place); MDSR form opens after save (M2.2-28) [DeliveryOutcomeViewModel.kt:148-163]',
      'Saving adds live births and total births to the EC record child counts [ViewModel.kt:166-178]',
      'Saved outcome view-only: Submit disabled, no edit (M2.2-19) [DeliveryOutcomeFragment.kt:106]',
      'JSY = Yes reveals JSY voucher upload (non-Mitanin). Mitanin: MCP card uploads hidden [DeliveryOutcomeDataset.kt:264-267,466-472]',
      'Defaults: date = today, still births = 0; discharge date from delivery date up to +1 month [Dataset.kt:268-274]',
      'After save: back to the Delivery outcome list (M2.2-37) [Fragment.kt:130-134]',
      '"Save draft": Not in app today – needs dev',
    ]);
  }
  // 10 After delivery: next steps + badge
  {
    const s = F('M2.2-10 · Delivery saved → next steps', 2, 5, true);
    screenTop(s, 'Delivery outcome', 'Sunita Devi', 'none');
    const b = body(s); banner(b, 'Success', 'Delivery outcome saved', '1 live birth at PHC Sirsi on 12 Feb 2027.', null);
    scrim(s);
    const sh = sheet(s, 'Next steps');
    sh.appendChild(await txt('What next?', 'Title/Large', 'color/on-surface'));
    const st = async (t, d, btn, tone, ic) => {
      const r = AL('HORIZONTAL', t); r.counterAxisAlignItems = 'CENTER'; pad(r, 'spacing/sm', 'spacing/sm'); gap(r, 'spacing/sm'); rad(r, 'radius/md'); fill(r, `module/${tone}/bg`); add(sh, r);
      r.appendChild(icon(ic, `module/${tone}/fg`, 24));
      const c = AL('VERTICAL'); c.itemSpacing = 2; r.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      c.appendChild(await txt(t, 'Title/Small', 'color/on-surface')); const dd = await txt(d, 'Body/Small', 'color/on-surface-variant'); c.appendChild(dd); wrapText(dd);
      const x = inst('button', 'Style=Filled, State=Enabled', { Label: btn, 'Show icon': false }); r.appendChild(x); x.resize(x.width, 40);
    };
    await st('Register the baby', 'Birth weight, birth doses (OPV0, BCG, Hep B, Vit K)', 'Register', 'child', 'child');
    await st('PNC day 1 visit', 'Due today · then day 3, 7, 14, 21, 28, 42', 'Start', 'maternal', 'favorite');
    const sn = kinst('Snackbar', 'Type=Badge', { Message: 'Maternal Journey earned!', Detail: '4 ANC visits + facility delivery · Tier 1', Action: 'See badge' }); add(sh, sn);
    const nt = await txt('Gamification ON only. When OFF, the badge row is not shown.', 'Body/Small', 'color/on-surface-variant'); add(sh, nt); nt.textAutoResize = 'HEIGHT';
    pinBottom(s, sh);
    rules(s, ['Next-steps sheet: Not in app today – needs dev. Code just returns to the Delivery outcome list [DeliveryOutcomeFragment.kt:130-134]']);
  }
  // 11 Infant registration (gap rows handled in stage 2.4)
  {
    const s = F('M2.2-11 · Infant registration', 3, 5);
    screenTop(s, 'Register baby', 'Mother: Sunita Devi · born 12 Feb', 'more');
    const b = body(s); b.itemSpacing = 12;
    text(b, 'Baby name (if given)', 'Baby of Sunita');
    await seg(b, 'Gender *', ['Girl', 'Boy', 'Other'], 0);
    await seg(b, 'Term *', ['Full term', 'Pre-term'], 0);
    steppers(b, [['Weight at birth (g) *', '2450']]);
    banner(b, 'Warning', 'Low birth weight (< 2500 g)', 'Baby needs extra HBNC visits. Counts toward the "Vulnerable Baby Cared For" badge.', null);
    await seg(b, 'Cried at birth?', ['Yes', 'No'], 0);
    await seg(b, 'Breastfeeding started within 1 hour?', ['Yes', 'No'], 0);
    await seg(b, 'Admitted to SNCU?', ['Yes', 'No'], 1);
    await seg(b, 'Any birth defect?', ['Yes', 'No'], 1);
    await checks(b, 'Birth doses given', [['OPV 0', true], ['BCG', true], ['Hepatitis B-0', true], ['Vitamin K', false]]);
    bottomBar(s, 'Save draft', 'Register baby');
  }
  // 12 PNC list + visit sheet
  {
    const s = F('M2.2-12 · PNC mothers · visit schedule', 4, 5, true);
    screenTop(s, 'PNC mothers', '18 mothers · 6 visits due');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile', { sort: true });
    recordCard(b, { name: 'Sunita Devi', meta: 'Delivered 12 Feb · PHC Sirsi', tag: 'PNC day 3 due today', tone: 'Maternal', action: 'Add PNC' });
    recordCard(b, { name: 'Geeta Kumari', meta: 'Delivered 2 Feb · home', tag: 'PNC day 14 due', tone: 'Maternal', action: 'Add PNC' });
    scrim(s);
    const sh = sheet(s, 'PNC schedule');
    sh.appendChild(await txt('PNC visits · Sunita Devi', 'Title/Medium', 'color/on-surface'));
    const grid = AL('HORIZONTAL', 'Days'); grid.layoutWrap = 'WRAP'; grid.itemSpacing = 8; grid.counterAxisSpacing = 8; add(sh, grid);
    for (const [d, st] of [['Day 1', 'done'], ['Day 3', 'due'], ['Day 7', 'next'], ['Day 14', 'next'], ['Day 21', 'next'], ['Day 28', 'next'], ['Day 42', 'next']]) {
      const c = AL('VERTICAL', d); c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 4; c.resize(74, 64); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED'; c.primaryAxisAlignItems = 'CENTER'; rad(c, 'radius/md');
      fill(c, st === 'done' ? 'color/success-container' : st === 'due' ? 'color/warning-container' : 'color/surface-container-high'); grid.appendChild(c);
      c.appendChild(icon(st === 'done' ? 'check_circle' : st === 'due' ? 'clock' : 'lock', st === 'done' ? 'color/success' : st === 'due' ? 'color/warning' : 'color/on-surface-variant', 20));
      c.appendChild(await txt(d, 'Label/Medium', 'color/on-surface'));
    }
    await caption(sh, 'Tap a done day (green) to view that visit read-only');
    add(sh, inst('button', 'Style=Filled, State=Enabled', { Label: 'Start PNC day 3', 'Show icon': false }));
    pinBottom(s, sh);
    rules(s, [
      '"Add PNC" only when a PNC day (1, 3, 7, 14, 21, 28, 42) is due by days since delivery and not done; hidden otherwise [PNC.kt:203-215, PncVisitListAdapter.kt:63-64]',
      '"View visits" only when ≥ 1 PNC exists → visits sheet M2.2-38 [PncVisitListAdapter.kt:61-62]',
      'Non-follow-up variant of this list (source 1) opened from Non-follow-up screen, same cards [NonFollowUpFragment.kt:72]',
      'Warning icon when last visit 90–365 days ago [PncVisitListAdapter.kt:51]. Voice search + sort sheet (M2.2-18)',
      'Day-grid sheet is design-only; code shows a list of done visits (M2.2-38)',
    ]);
  }
  // 13 PNC visit form
  {
    const s = F('M2.2-13 · PNC visit form', 0, 6);
    screenTop(s, 'PNC visit · day 3', 'Sunita Devi · delivered 12 Feb', 'more');
    const b = body(s); b.itemSpacing = 12;
    add(b, kinst('Info row', null, { Label: 'Date of delivery', Value: '12 Feb 2027' }));
    select(b, 'PNC period *', 'Day 3'); date(b, 'Visit date *', '15 Feb 2027');
    steppers(b, [['IFA tablets given', '30']]);
    await seg(b, 'Using a contraception method?', ['Yes', 'No'], 0);
    const rv = reveal(b); select(rv, 'Method *', 'PPIUCD');
    await caption(rv, 'If sterilised in an earlier PNC, permanent sterilisation is carried into this visit');
    await seg(b, 'Any danger sign in mother?', ['Yes', 'No'], 0);
    const rv2 = reveal(b); await checks(rv2, null, [['Heavy bleeding', false], ['Fever', true], ['Foul-smelling discharge', false]]); select(rv2, 'Referral facility', 'PHC Sirsi');
    await seg(b, 'Is the mother alive?', ['Yes', 'No'], 0);
    const rv3 = reveal(b); await caption(rv3, 'If "No" (mother died):'); date(rv3, 'Date of death *', '—'); select(rv3, 'Cause of death *', 'Select'); select(rv3, 'Place of death *', 'Select');
    await caption(rv3, 'Submit marks her dead and opens the MDSR form');
    await uploads(b, 'Delivery discharge summary (up to 4 · not in Mitanin)', [['Discharge_summary_p1.jpg', 'Done']], 'Camera or gallery · no PDF · size limit');
    text(b, 'Remarks', 'Advised rest and fluids');
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, [
      'Mother death → beneficiary marked dead, MDSR form opens [PncFormViewModel.kt:159-173, PncFormFragment.kt:140]',
      'Permanent sterilisation from an earlier PNC carried into later visits [PncFormViewModel.kt:116-125]',
      'Status after PNC: day 42 (or ≥ 60 days after delivery) → "Permanently sterilised" if sterilised, else back to "Eligible couple" [ViewModel.kt:266-290]',
      'Method other than condom → incentive reminder popup (M2.2-39), non-Mitanin [PncFormDataset.kt:628-636]',
      'Submit with no discharge summary → "upload?" dialog (M2.2-40), non-Mitanin [PncFormFragment.kt:159-162]',
      'Discharge summary: up to 4 photos, camera/gallery, no PDF; hidden for Mitanin [Fragment.kt:206, Dataset.kt:276]',
      'No edit after save (M2.2-19) [PncFormFragment.kt:97]. "Save draft": Not in app today – needs dev',
    ]);
  }
  // 14 Abortion record (CAC form)
  {
    const s = F('M2.2-14 · Abortion record', 4, 6);
    screenTop(s, 'CAC form', 'Kiran Yadav · 11 weeks', 'more');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Recorded from ANC visit', 'Abortion was marked "Yes" in ANC 2. Complete the details below.', null);
    date(b, 'Abortion date *', '28 Oct 2026');
    await radios(b, 'Type of abortion *', ['Spontaneous', 'Induced'], 0);
    select(b, 'Place / facility *', 'CHC Rajnagar');
    await seg(b, 'Any complication?', ['Yes', 'No'], 1);
    await seg(b, 'Post-abortion sterilisation done?', ['Yes', 'No'], 0);
    const rv = reveal(b); date(rv, 'Sterilisation date *', '30 Oct 2026');
    await caption(rv, 'Saving sets her to "Permanently sterilised" and adds an EC-tracking record (female sterilisation) on this date');
    await uploads(b, 'Abortion discharge summary (not in Mitanin)', [['CAC_discharge.jpg', 'Done']], 'Camera or gallery · no PDF');
    text(b, 'Remarks', 'Counselled on family planning');
    banner(b, 'Warning', 'Pregnancy record will close', 'She moves back to the eligible couple list (or "Permanently sterilised" if sterilisation = Yes).', null);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, [
      'Screen title in code: "CAC form" (comprehensive abortion care)',
      'Sterilisation = Yes → status "Permanently sterilised" + auto EC-tracking record (female sterilisation) [PwAncAbortionFormViewModel.kt:148-167]',
      'Discharge summary photos: camera/gallery [PwAncAbortionFormFragment.kt:234]',
      'Mitanin: both discharge summary uploads removed, upload tap ignored [PregnantWomanAncAbortionDataset.kt:189, Fragment.kt:97]',
      'Saved record: view-only with Edit, always editable (M2.2-19) [Fragment.kt:157]',
      'List: M2.2-41. "Save draft": Not in app today – needs dev',
    ]);
  }
  // 15 Referral list
  {
    const s = F('M2.2-15 · HWC referral list', 5, 6);
    screenTop(s, 'Referral list', 'Referred to HWC / PHC');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'Open', 'Count 1': '2', 'Label 2': 'Visited', 'Count 2': '7', 'Label 3': 'All', 'Count 3': '9' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile', { sort: true });
    recordCard(b, { name: 'Rekha Yadav', meta: 'Maternal · referred 30 Oct · PHC Sirsi', tag: 'High BP in pregnancy', tone: 'Alert' });
    recordCard(b, { name: 'Sunita Devi', meta: 'Maternal · referred 2 Nov · PHC Sirsi', tag: 'Severe anaemia', tone: 'Alert', sync: 'Pending' });
    recordCard(b, { name: 'Geeta Kumari', meta: 'Maternal · referred 10 Oct · CHC Rajnagar', tag: 'Visited 12 Oct', tone: 'Success' });
    const g1 = await actionCard(b, { name: 'Ramesh Kumar', meta: 'General check-up · referred 1 Nov', tags: [['Alert', 'High blood sugar']], rows: [['Referred from', 'Sunita Kumari (ASHA)']], actions: [['Filled', 'Start follow-up']] });
    tint(g1, 'Start follow-up', 'color/error');
    const g2 = await actionCard(b, { name: 'Shanti Devi', meta: 'General check-up · referred 18 Oct', tags: [['Success', 'Follow-up done']], rows: [['Referred from', 'Sunita Kumari (ASHA)']], actions: [['Filled', 'View follow-up']] });
    tint(g2, 'View follow-up', 'color/success');
    await actionCard(b, { name: 'Mohan Lal', meta: 'General check-up · referred 5 Oct', state: 'deceased', stateNote: 'Deceased · 20 Oct 2026', rows: [['Referred from', 'Sunita Kumari (ASHA)']] });
    rules(s, [
      'Lists only Maternal and General check-up referrals [HwcReferredViewModel.kt:71] (M2.8-14 "all sources" incl. TB does not match code)',
      'Follow-up button only on General check-up rows; Maternal rows have none [HwcReferredFragment.kt:97]',
      'Red "Start follow-up" if none done, green "View follow-up" if done [NcdReferListAdapter.kt:108-127]',
      'Deceased beneficiary: card greyed, follow-up hidden [NcdReferListAdapter.kt:91-95]',
      '"Referred from" = logged-in ASHA name; opens in Supervisor screen for non-ASHA roles (M2.2-42)',
      'Tabs Open / Visited / All: Not in app today – needs dev',
    ]);
  }
  // 16 Maternal death branch
  {
    const s = F('M2.2-16 · Maternal death (branch)', 4, 2, true);
    screenTop(s, 'ANC visit 4', 'Rekha Yadav · 34 weeks', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Is the woman alive?', ['Yes', 'No'], 1);
    const rv = reveal(b); date(rv, 'Date of death *', '30 Oct 2026'); select(rv, 'Reason *', 'Eclampsia'); select(rv, 'Place of death *', 'On the way to facility');
    scrim(s);
    const d = kinst('Dialog', null, { Title: 'Record maternal death?', Body: 'This closes her pregnancy record and starts an MDSR report. Please check the details with your ANM.' });
    overlayCenter(s, d);
    const btns = d.findAll((n) => n.type === 'INSTANCE' && n.name === 'Button');
    if (btns[0]) setP(btns[0], { Label: 'Go back' }); if (btns[1]) setP(btns[1], { Label: 'Record death' });
    rules(s, [
      '"Record maternal death?" dialog: Not in app today – needs dev',
      'Code: Submit marks the beneficiary dead (date, cause, place copied) and goes straight to the MDSR form [PwAncFormViewModel.kt:218-250, PwAncFormFragment.kt:293]',
    ]);
  }

  // ======================= NEW FRAMES (gap fix) =======================
  // 17 RCH filter dialog
  {
    const s = F('M2.2-17 · Registration list · RCH filter', 2, 0, true);
    screenTop(s, 'Pregnancy registration', 'Women 15–49 · 6 to register');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or HH ID', { sort: true, filter: true });
    recordCard(b, { name: 'Anita Devi', meta: 'W/o Suresh · 28 yrs', tag: 'Missed period', tone: 'Neutral', action: 'Register' });
    await dialog(s, { ic: 'filter', icColor: 'color/primary', title: 'Filter list', cancel: 'Cancel', ok: 'Apply', extra: async (c) => { add(c, kinst('Checkbox option', 'Checked=true', { Label: 'Beneficiaries with RCH ID' })); } });
    rules(s, ['Filter icon → this dialog; Apply shows only women with an RCH ID [PwRegistrationListFragment.kt:52-72,112, alert_filter.xml]']);
  }
  // 18 Sort sheet
  {
    const s = F('M2.2-18 · Maternal lists · sort sheet', 3, 0, true);
    screenTop(s, 'ANC visits', '42 pregnant women');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile', { sort: true });
    await sortLabel(b, 'Newest first');
    await optionSheet(s, 'Sort by', ['Newest first', 'Oldest first', 'Age-wise', 'Syncing first', 'Unsynced first'], 0);
    rules(s, [
      'Same sort sheet on PW registration, ANC, PMSMA high-risk, PNC, Abortion and HWC lists [PwRegistrationListFragment.kt:99]',
      'Chosen sort shown as "Sorted: …" label above the list',
      'Search bar mic = voice search, fills the search box from speech [PwRegistrationListFragment.kt:142]',
    ]);
  }
  // 19 View mode & edit rules
  {
    const s = F('M2.2-19 · Saved records · view mode & edit rules', 5, 0);
    screenTop(s, 'Register pregnancy', 'Sunita Devi · saved', 'more');
    const b = body(s); b.itemSpacing = 12;
    await heading(b, 'Pregnancy registration · CAC form');
    await viewMode(b, { saved: 'Saved 20 Jul 2026', editable: true, reason: 'Always editable (pencil unlocks the form)' });
    await heading(b, 'ANC · PMSMA · ANC home visit');
    await viewMode(b, { saved: 'ANC 3 · 02 Nov 2026', editable: true, reason: 'Opened from the latest visit: Edit allowed' });
    await viewMode(b, { saved: 'ANC 2 · 20 Sep 2026', editable: false, reason: 'Older visit: view only. Only the latest visit can be edited' });
    await heading(b, 'Delivery outcome · PNC');
    await viewMode(b, { saved: 'Delivery · 12 Feb 2027', editable: false, reason: 'No edit after save (Submit disabled)' });
    auto(b, 'LMP date', '18 May 2026'); auto(b, 'EDD', '22 Feb 2027'); auto(b, 'Blood group', 'B +ve');
    rules(s, [
      'PW registration & CAC: Edit FAB always [PregnancyRegistrationFormFragment.kt:43,71, PwAncAbortionFormFragment.kt:157]',
      'ANC / PMSMA / home visit: Edit only when opened as the latest visit [PwAncFormFragment.kt:279, PmsmaFragment.kt:119, AntenatalCounsellingFragment.kt:198]',
      'Delivery outcome, PNC: no edit, Submit disabled [DeliveryOutcomeFragment.kt:106, PncFormFragment.kt:97]',
      'Overrides M2.0-P5 text "view only… ask your ANM" for these forms',
    ]);
  }
  // 20 ANC visits list · card states
  {
    const s = F('M2.2-20 · ANC visits list · card states', 1, 1);
    screenTop(s, 'Pregnant women · ANC', '42 women');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile', { sort: true });
    await sortLabel(b);
    await ancCard(b, { name: 'Sunita Devi', meta: '26 yrs · 24 wks · EDD 15 Feb 2027', tags: [['Maternal', 'ANC 3 due']], rows: [['Last visit', '20 Sep 2026'], ['ANC done', '2 of 8']], delivered: false, actions: [['Filled', 'Add ANC'], ['Outlined', 'ANC visits']] });
    await ancCard(b, { name: 'Kiran Yadav', meta: '17 yrs · 11 wks', tags: [['Neutral', 'ANC 2 not yet due']], rows: [['Last visit', '25 Oct 2026'], ['ANC done', '1 of 8']], actions: [['Filled', 'Add ANC', true], ['Outlined', 'ANC visits']], footnote: 'Add ANC opens 22 Nov (28 days after the last ANC)' });
    await ancCard(b, { name: 'Pooja Kumari', meta: '22 yrs · 4 wks · registered 1 Nov', tags: [['Maternal', 'Registered']], rows: [['Last visit', '—']], actions: [['Filled', 'Add ANC', true]], footnote: 'ANC 1 opens 5 weeks after LMP. "ANC visits" hidden until a visit exists' });
    await ancCard(b, { name: 'Rekha Yadav', meta: '31 yrs · 34 wks', warn: true, tags: [['Alert', 'Not followed up · 4 months']], rows: [['Last visit', '02 Jul 2026'], ['ANC done', '2 of 8']], delivered: false, actions: [['Filled', 'Add ANC'], ['Outlined', 'ANC visits']] });
    await ancCard(b, { name: 'Meena Kumari', meta: 'Mitanin build · 29 yrs · 28 wks', tags: [['Maternal', 'ANC 4 due']], rows: [['Last visit', '05 Oct 2026'], ['Home visits', '3 of 9']], delivered: false, actions: [['Filled', 'Add ANC'], ['Outlined', 'ANC visits'], ['Outlined', 'Add home visit'], ['Text', 'View home visits']], footnote: 'Add home visit while < 9 visits and ≥ 30 days since the last' });
    rules(s, [
      'Delivery status switch only when weeks > 22 → confirm (M2.2-21) → date (M2.2-22) [MaternalHealth.kt:797-798, AncVisitListAdapter.kt:77-104]',
      'Add ANC greyed until allowed: ANC 1 ≥ 5 wks after LMP, then ≥ 28 days after last ANC, max 8, only with active registration [MaternalHealth.kt:723-733]',
      '"ANC visits" only when ≥ 1 visit (sheet M2.2-24) [AncVisitListAdapter.kt:160]',
      'Mitanin only: Add / View home visits (< 9 and ≥ 30 days) [AncVisitListAdapter.kt:136-148, RecordsRepo.kt:629-657]',
      'Warning icon when last visit 90–365 days ago [AncVisitListAdapter.kt:127]',
      'Expired pregnancies auto-closed each time the list opens [PwAncVisitsListViewModel.kt:36]',
      'Call: calls the woman; asks permission if missing (M2.2-23). Supervisor: all buttons hidden (M2.2-42)',
    ]);
  }
  // 21 Delivered confirm
  {
    const s = F('M2.2-21 · ANC list · "Has she delivered?" confirm', 2, 1, true);
    screenTop(s, 'Pregnant women · ANC', '42 women');
    const b = body(s); b.itemSpacing = 12;
    await ancCard(b, { name: 'Lata Bai', meta: '30 yrs · 38 wks · EDD 10 Nov 2026', tags: [['Maternal', 'Near EDD']], rows: [['Last visit', '20 Oct 2026']], delivered: true, actions: [['Filled', 'Add ANC'], ['Outlined', 'ANC visits']] });
    await dialog(s, { ic: 'pregnant', icColor: 'color/primary', title: 'Has the pregnant woman delivered?', body: 'Lata Bai · 38 weeks. Next, choose the delivery date.', cancel: 'No', ok: 'Yes' });
    rules(s, ['Switch → this dialog. No: switch goes back. Yes: delivery date picker (M2.2-22) [AncVisitListAdapter.kt:77-104]']);
  }
  // 22 Delivery date picker
  {
    const s = F('M2.2-22 · ANC list · delivery date picker', 3, 1, true);
    screenTop(s, 'Pregnant women · ANC', '42 women');
    scrim(s);
    const c = kinst('Calendar'); s.appendChild(c); c.layoutPositioning = 'ABSOLUTE'; c.x = 16; c.y = Math.round((800 - c.height) / 2);
    await toast(s, 'A later ANC visit exists (20 Oct). Pick a date after it.', 96);
    rules(s, [
      'Date between last ANC date and today; error if a later ANC exists [PwAncVisitsListFragment.kt:271-330]',
      'OK marks the woman delivered → she moves to the Delivery outcome list (M2.2-37)',
    ]);
  }
  // 23 ANC list variants
  {
    const s = F('M2.2-23 · ANC list variants · non-follow-up / HRP', 4, 1, true);
    screenTop(s, 'Non follow-up · ANC', '7 women · no visit in 90+ days');
    const b = body(s); b.itemSpacing = 12;
    chips(b, ['Non follow-up', 'Due for ANC', 'HRP confirmed'], 0);
    await ancCard(b, { name: 'Rekha Yadav', meta: '31 yrs · 34 wks', warn: true, tags: [['Alert', 'Last visit 02 Jul']], delivered: false, actions: [['Filled', 'Add ANC'], ['Outlined', 'ANC visits']] });
    await ancCard(b, { name: 'Savitri Devi', meta: '24 yrs · 20 wks', warn: true, tags: [['Alert', 'Last visit 10 Jun']], actions: [['Filled', 'Add ANC'], ['Outlined', 'ANC visits']] });
    await toast(s, 'Please allow permissions first');
    rules(s, [
      'Same list, 3 entry points: 1 Non-follow-up (NonFollowUpFragment.kt:62), 2 Due for ANC, 3 HRP confirmed (SchedulerFragment.kt:103,113) [PwAncVisitsListViewModel.kt:42-46]',
      'Chips here only label the variant: in code there are no chips, the entry point decides the list',
      'Call without phone permission → asks permission + toast "Please allow permissions first" [PwAncVisitsListFragment.kt:152-166]',
    ]);
  }
  // 24 ANC visits history sheet
  {
    const s = F('M2.2-24 · ANC visits sheet · view / edit last', 5, 1, true);
    screenTop(s, 'Pregnant women · ANC', '42 women');
    await visitSheet(s, { title: 'ANC visits', subtitle: 'Sunita Devi · 24 wks', visits: [{ label: 'ANC 1', date: '14 Aug 2026', status: 'Synced', tone: 'Success', action: 'View' }, { label: 'ANC 2', date: '20 Sep 2026', status: 'Synced', tone: 'Success', action: 'View' }, { label: 'ANC 3 · latest', date: '02 Nov 2026', status: 'Editable', tone: 'Maternal', action: 'View' }], footnote: 'Only the latest visit opens with Edit. View hidden for supervisor.' });
    rules(s, ['View opens the visit read-only; only the last visit gets the Edit option [AncBottomSheetFragment.kt:58-77, AncVisitAdapter.kt:66]', 'Per-visit sync icon; View hidden for supervisor [AncVisitAdapter.kt:46-50]']);
  }
  // 25 ANC visit 1 · early pregnancy · PMSMA place locked
  {
    const s = F('M2.2-25 · ANC visit 1 · ≤ 12 wks, from PMSMA list', 1, 2);
    screenTop(s, 'ANC visit 1', 'Kiran Yadav · 10 weeks · from PMSMA', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'ANC date *', '28 Oct 2026');
    await caption(b, 'Allowed: 25 Sep (LMP + 5 weeks) to today');
    auto(b, 'Place of ANC (locked · opened from PMSMA)', 'PMSMA site');
    select(b, 'ANC period *', 'ANC 1');
    section(b, 'Open', 'Pregnancy status', '2 questions', 2);
    await seg(b, 'Any abortion since last visit?', ['Yes', 'No'], 1);
    await seg(b, 'Is the woman alive?', ['Yes', 'No'], 0);
    await caption(b, '"Has she delivered?" not shown before 23 weeks');
    section(b, 'Open', 'Examination', '4 fields', 3);
    steppers(b, [['Weight (kg)', '47'], ['Hb (g/dl)', '10.2']]);
    steppers(b, [['BP systolic', '110'], ['BP diastolic', '70']]);
    await caption(b, 'Fundal height hidden up to 12 weeks');
    section(b, 'Open', 'Supplements', '1 field', 4);
    steppers(b, [['Folic acid tablets', '30']]);
    section(b, 'Open', 'Td / TT doses', 'Shared with all visits', 5);
    date(b, 'Td-1 date', '28 Oct 2026');
    await caption(b, 'Td-2 hidden on the first ANC');
    bottomBar(s, null, 'Submit');
    rules(s, [
      '≤ 12 weeks: Folic acid replaces IFA; fundal height hidden [PregnantWomanAncVisitDataset.kt:546-552,567-572]',
      'From PMSMA list ("PMSMA" card button, fromPmsma = true): Place locked to PMSMA site [Dataset.kt:488-493]',
      'Td dates saved on the registration record, shared by all ANC visits [Dataset.kt:504-505,596-598]',
    ]);
  }
  // 26 Upload chooser
  {
    const s = F('M2.2-26 · Upload chooser · gallery / camera / PDF', 2, 2, true);
    screenTop(s, 'ANC visit 3', 'Sunita Devi · 24 weeks', 'more');
    const b = body(s); b.itemSpacing = 12;
    section(b, 'Open', 'MCP card', 'Camera, gallery or PDF', 7);
    const ph = AL('HORIZONTAL', 'MCP photos'); gap(ph, 'spacing/sm'); add(b, ph); ph.appendChild(kinst('Photo tile', 'State=Empty'));
    await dialog(s, { ic: 'cloud_upload', icColor: 'color/primary', title: 'Upload MCP card (front)', cancel: 'Cancel', ok: null, extra: async (c) => {
      for (const [ic, l] of [['image', 'Pick from Gallery'], ['camera', 'Open Camera'], ['pdf', 'Pick a PDF']]) { const r = G.row(c, 'Option · ' + l, 16); r.paddingTop = 10; r.paddingBottom = 10; r.appendChild(icon(ic, 'color/primary', 24)); r.appendChild(await txt(l, 'Body/Large', 'color/on-surface')); }
    } });
    await toast(s, 'File too large');
    rules(s, [
      'Dialog "Pick from Gallery / Open Camera / Pick a PDF / Cancel" [layout_media_options.xml, PwAncFormFragment.kt:69,84]',
      'PDF only on ANC MCP card and Delivery outcome; hidden for PNC, CAC, home visit',
      'Over the size limit → toast "File too large"',
      'Mitanin: all document uploads hidden (MCP, JSY voucher, discharge summaries) [C5]',
    ]);
  }
  // 27 Document viewer
  {
    const s = F('M2.2-27 · Uploaded file viewer', 3, 2, true);
    s.fills = [{ type: 'SOLID', color: { r: 0.05, g: 0.05, b: 0.06 } }];
    s.counterAxisAlignItems = 'CENTER'; s.itemSpacing = 24;
    const hd = AL('HORIZONTAL', 'Viewer bar'); hd.counterAxisAlignItems = 'CENTER'; hd.itemSpacing = 16; pad(hd, 'spacing/md', 'spacing/md'); add(s, hd);
    hd.appendChild(icon('close', 'color/on-primary', 24));
    hd.appendChild(await txt('MCP_card_front.jpg', 'Title/Medium', 'color/on-primary'));
    const im = box('Image', 328, 460, 'VERTICAL'); im.itemSpacing = 8; rad(im, 'radius/md'); fill(im, 'color/surface-container-high'); s.appendChild(im);
    im.appendChild(icon('image', 'color/on-surface-variant', 64));
    im.appendChild(await txt('MCP card · front page', 'Body/Medium', 'color/on-surface-variant'));
    const ft = await txt('Pinch to zoom · PDFs open in an external PDF app', 'Body/Small', 'color/on-primary'); s.appendChild(ft);
    rules(s, ['Tap an uploaded image → full-screen viewer; PDF handed to an external app [PwAncFormFragment.kt, PncFormFragment.kt:206]']);
  }
  // 28 Delivery outcome death → MDSR
  {
    const s = F('M2.2-28 · Death recorded → MDSR (delivery / PNC)', 5, 2);
    screenTop(s, 'Delivery outcome', 'Rekha Yadav · EDD 05 Nov 2026', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Delivery date *', '30 Oct 2026');
    await seg(b, 'Any complication?', ['Yes', 'No'], 0);
    const rv = reveal(b); select(rv, 'Complication *', 'Death');
    const rv2 = reveal(rv); date(rv2, 'Date of death *', '30 Oct 2026'); select(rv2, 'Cause of death *', 'Post-partum haemorrhage'); select(rv2, 'Place of death *', 'CHC Rajnagar');
    banner(b, 'Info', 'After Submit the MDSR form opens', 'She is marked dead with this date, cause and place. Complete the maternal death report next.', null);
    bottomBar(s, null, 'Submit');
    rules(s, [
      'Delivery: complication "Death" → mother marked dead, MDSR form opens after save [DeliveryOutcomeViewModel.kt:148-163, DeliveryOutcomeFragment.kt:133]',
      'Same flow from ANC (alive = No) [PwAncFormFragment.kt:293] and PNC (mother death) [PncFormFragment.kt:140]',
      'No confirm dialog in code (see M2.2-16)',
    ]);
  }
  // 29 ANC home visits sheet
  {
    const s = F('M2.2-29 · ANC home visits sheet (Mitanin)', 1, 3, true);
    screenTop(s, 'Pregnant women · ANC', 'Mitanin · 42 women');
    await visitSheet(s, { title: 'ANC home visits', subtitle: 'Meena Kumari · 28 wks', visits: [{ label: 'Visit 1', date: '05 Aug 2026', status: 'Synced', tone: 'Success', action: 'View' }, { label: 'Visit 2', date: '05 Sep 2026', status: 'Synced', tone: 'Success', action: 'View' }, { label: 'Visit 3 · latest', date: '05 Oct 2026', status: 'Editable', tone: 'Maternal', action: 'View' }], footnote: 'Opens read-only. Only the last visit can be edited.' });
    rules(s, ['"View home visits" (Mitanin) → this sheet; tap opens read-only, last one editable [AncHomeVisitBottomSheetFragment.kt:38-52]']);
  }
  // 30 ANC home visits · empty
  {
    const s = F('M2.2-30 · ANC home visits sheet · empty', 2, 3, true);
    screenTop(s, 'Pregnant women · ANC', 'Mitanin · 42 women');
    scrim(s);
    const sh = sheet(s, 'Sheet · ANC home visits');
    add(sh, await txt('ANC home visits', 'Title/Large', 'color/on-surface'));
    add(sh, kinst('Empty state', null, { Title: 'No home visits yet', Body: 'Home visits you save for this woman will show here.', Icon: 'home' }));
    pinBottom(s, sh);
    rules(s, ['Empty state when no home visits exist [AncHomeVisitBottomSheetFragment.kt:69-80]']);
  }
  // 31 Home visit danger sign → refer
  {
    const s = F('M2.2-31 · Home visit · danger signs → refer to HWC', 3, 3, true);
    screenTop(s, 'ANC home visit', 'Mitanin only · Sunita Devi', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Swelling of hands / face?', ['Yes', 'No'], 0);
    await seg(b, 'High BP / severe headache?', ['Yes', 'No'], 0);
    await dialog(s, { title: 'Danger signs identified', body: 'Refer Sunita Devi to the HWC?', cancel: 'No', ok: 'Yes, refer' });
    rules(s, [
      'Any danger sign on Save → this dialog [AntenatalCounsellingFragment.kt:584-605]',
      'Yes → HWC referral form (type MATERNAL); on return the visit is saved. Not asked again once referred [Fragment.kt:656]',
      'No → visit saved without referral',
    ]);
  }
  // 32 Home visit saved → continue to ANC
  {
    const s = F('M2.2-32 · Home visit saved → continue to ANC?', 4, 3, true);
    screenTop(s, 'ANC home visit', 'Mitanin only · Sunita Devi', 'more');
    await dialog(s, { ic: 'check_circle', icColor: 'color/success', title: 'Continue to ANC visit?', body: 'Home visit saved.', cancel: 'No', ok: 'Yes' });
    await toast(s, 'Next ANC visit is not yet due');
    rules(s, ['Yes → next ANC form if due, else toast "Next ANC visit is not yet due". No → back to the list [AntenatalCounsellingFragment.kt:438-465]']);
  }
  // 33 PMSMA visits sheet (high-risk list)
  {
    const s = F('M2.2-33 · PMSMA visits sheet (high-risk list)', 1, 4, true);
    screenTop(s, 'High-risk pregnancies', '5 confirmed');
    await visitSheet(s, { title: 'PMSMA visits', subtitle: 'Sunita Devi · ANC visits that were high-risk or at the PMSMA site', visits: [{ label: 'ANC 2', date: '20 Sep 2026 · high risk', status: 'Synced', tone: 'Success', action: 'View' }, { label: 'ANC 3 · latest', date: '09 Oct 2026 · PMSMA site', status: 'Editable', tone: 'Maternal', action: 'View' }] });
    rules(s, ['"PMSMA visits" button → ANC visits filtered to high-risk or place = PMSMA site (id 3) [PwAncVisitsListViewModel.kt:95-101, PmsmaHighRiskListFragment.kt:118-133]']);
  }
  // 34 PMSMA form visits sheet (dormant)
  {
    const s = F('M2.2-34 · PMSMA form visits sheet (dormant)', 5, 4, true);
    screenTop(s, 'High-risk pregnancies', '5 confirmed');
    await visitSheet(s, { title: 'PMSMA visits', subtitle: 'Sunita Devi', visits: [{ label: 'PMSMA visit 1', date: '09 Sep 2026', action: 'View' }, { label: 'PMSMA visit 2 · latest', date: '09 Oct 2026', status: 'Editable', tone: 'Maternal', action: 'View' }] });
    rules(s, ['Dormant: PmsmaBottomSheetFragment is never shown in current code [PmsmaBottomSheetFragment.kt:40-62]', 'Lists PMSMA form visits; last one editable; works from High-risk and ANC lists']);
  }
  // 35 PMSMA high-risk alert
  {
    const s = F('M2.2-35 · PMSMA · high-risk alert', 3, 4, true);
    screenTop(s, 'PMSMA visit', 'Sunita Devi · PHC Sirsi · 9 Nov', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'High-risk symptoms found?', ['Yes', 'No'], 0);
    await dialog(s, { ic: 'warning', title: 'High risk pregnancy', body: 'This is an HRP case. Please visit the nearest HWC or call 104.', cancel: null, ok: 'OK' });
    rules(s, ['Shown when high risk is selected [PmsmaFragment.kt:92-99, PmsmaViewModel.onHighRiskSelected]', 'PMSMA form itself is dormant (see M2.2-07)']);
  }
  // 36 PMSMA consent (dormant)
  {
    const s = F('M2.2-36 · PMSMA · consent (dormant)', 4, 4, true);
    screenTop(s, 'PMSMA visit', 'Sunita Devi', 'more');
    await dialog(s, { ic: 'info', icColor: 'color/primary', title: 'Consent', body: 'Take the woman\'s consent before the PMSMA check-up.', cancel: 'Go back', ok: 'Proceed', extra: async (c) => { add(c, kinst('Checkbox option', 'Checked=true', { Label: 'Consent taken' })); } });
    rules(s, ['Dormant: defined but never shown. Tick to proceed, else go back [PmsmaFragment.kt:42-65]']);
  }
  // 37 Delivery outcome list
  {
    const s = F('M2.2-37 · Delivery outcome list', 0, 5);
    screenTop(s, 'Delivery outcome', '3 delivered · 1 to record');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile');
    const c1 = await actionCard(b, { name: 'Lata Bai', meta: 'W/o Mohan · 30 yrs · delivered 30 Oct 2026', tags: [['Child', 'Outcome not recorded']], actions: [['Filled', 'Register']] });
    tint(c1, 'Register', 'color/error');
    const c2 = await actionCard(b, { name: 'Sunita Devi', meta: 'W/o Ramesh · 26 yrs · delivered 12 Feb 2027', sync: 'Synced', tags: [['Success', 'Outcome recorded']], actions: [['Filled', 'View']] });
    tint(c2, 'View', 'color/success');
    const c3 = await actionCard(b, { name: 'Geeta Kumari', meta: 'W/o Raju · 23 yrs · delivered 2 Feb 2027', sync: 'Pending', tags: [['Success', 'Outcome recorded']], actions: [['Filled', 'View']] });
    tint(c3, 'View', 'color/success');
    rules(s, [
      'Women marked delivered (ANC form or Delivered switch) [DeliveryOutcomeListFragment.kt]',
      'Red "Register" when no outcome, green "View" once filled → form M2.2-09 [BenListAdapterForForm.kt:114-160]',
      'Supervisor: read-only, button hidden (M2.2-42) [BenListAdapterForForm.kt:59-62]. Voice search in bar',
    ]);
  }
  // 38 PNC visits sheet
  {
    const s = F('M2.2-38 · PNC visits sheet', 5, 5, true);
    screenTop(s, 'PNC mothers', '18 mothers');
    await visitSheet(s, { title: 'PNC visits', subtitle: 'Sunita Devi · delivered 12 Feb', visits: [{ label: 'PNC visit 1', date: '12 Feb 2027 · Day 1', status: 'Synced', tone: 'Success', action: 'View' }, { label: 'PNC visit 2', date: '15 Feb 2027 · Day 3', status: 'Pending', tone: 'Neutral', action: 'View' }], footnote: 'Read-only. PNC visits cannot be edited.' });
    rules(s, ['"View visits" → "PNC visit N" rows; tap opens read-only [PncBottomSheetFragment.kt:27-32]']);
  }
  // 39 PNC discharge-summary reminder
  {
    const s = F('M2.2-39 · PNC · discharge summary reminder', 1, 6, true);
    screenTop(s, 'PNC visit · day 3', 'Sunita Devi · delivered 12 Feb', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Using a contraception method?', ['Yes', 'No'], 0);
    const rv = reveal(b); select(rv, 'Method *', 'Female sterilisation');
    await dialog(s, { ic: 'info', icColor: 'color/primary', title: 'Reminder', body: 'Upload the delivery discharge summary to claim the incentive.', cancel: null, ok: 'OK' });
    rules(s, ['Method other than condom → reminder popup; non-Mitanin only [PncFormDataset.kt:628-636, PncFormFragment.kt:130]']);
  }
  // 40 PNC upload on submit
  {
    const s = F('M2.2-40 · PNC · upload discharge summary? (submit)', 2, 6, true);
    screenTop(s, 'PNC visit · day 3', 'Sunita Devi · delivered 12 Feb', 'more');
    const b = body(s); b.itemSpacing = 12;
    await uploads(b, 'Delivery discharge summary (up to 4)', [], 'Camera or gallery · no PDF');
    bottomBar(s, null, 'Submit');
    await dialog(s, { ic: 'cloud_upload', icColor: 'color/primary', title: 'Upload delivery discharge summary?', body: 'No discharge summary is attached to this visit.', cancel: 'No', ok: 'Yes' });
    rules(s, ['Submit with no discharge summary → this dialog; non-Mitanin [PncFormFragment.kt:159-162]', 'Yes → upload chooser (camera / gallery, M2.2-26). No → visit saved']);
  }
  // 41 Abortion (CAC) list
  {
    const s = F('M2.2-41 · Abortion (CAC) list', 3, 6);
    screenTop(s, 'Abortion', '2 women');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile', { sort: true });
    await sortLabel(b);
    const c1 = await actionCard(b, { name: 'Kiran Yadav', meta: 'W/o Vinod · 19 yrs', tags: [['Alert', 'Abortion']], rows: [['Abortion date', '28 Oct 2026'], ['LMP', '18 Aug 2026'], ['EDD', '25 May 2027'], ['Weeks', '10 wks']], actions: [['Filled', 'Add']] });
    tint(c1, 'Add', 'color/error');
    const c2 = await actionCard(b, { name: 'Radha Devi', meta: 'W/o Shyam · 27 yrs', sync: 'Synced', tags: [['Success', 'CAC form filled']], rows: [['Abortion date', '04 Sep 2026'], ['LMP', '10 Jul 2026'], ['EDD', '16 Apr 2027'], ['Weeks', '8 wks']], actions: [['Filled', 'View']] });
    tint(c2, 'View', 'color/success');
    rules(s, ['Women whose pregnancy ended in abortion; red "Add" / green "View" → CAC form (M2.2-14) [AncAbortionListAdapter.kt:36-46, rv_item_abortion.xml]']);
  }
  // 42 Supervisor read-only lists
  {
    const s = F('M2.2-42 · Supervisor · maternal lists (read-only)', 0, 7);
    screenTop(s, 'Pregnancy registration', 'ASHA Supervisor · ASHA: Sunita Kumari');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'View only', 'Supervisor role: Register, Add ANC, PMSMA, Home visit and View buttons are hidden.', null);
    searchRow(b, 'Search name or mobile', { sort: true });
    await heading(b, 'Pregnancy registration');
    await actionCard(b, { name: 'Anita Devi', meta: 'W/o Suresh · 28 yrs', sync: 'Synced', tags: [['Maternal', 'Registered']] });
    await heading(b, 'ANC visits');
    await ancCard(b, { name: 'Sunita Devi', meta: '26 yrs · 24 wks', noCall: true, tags: [['Maternal', 'ANC 3 due']], rows: [['Last visit', '20 Sep 2026'], ['ANC done', '2 of 8']] });
    await heading(b, 'Delivery outcome');
    await actionCard(b, { name: 'Geeta Kumari', meta: 'Delivered 2 Feb 2027', tags: [['Success', 'Outcome recorded']] });
    await heading(b, 'HWC referral list');
    await actionCard(b, { name: 'Rekha Yadav', meta: 'Maternal · referred 30 Oct', tags: [['Alert', 'High BP in pregnancy']], rows: [['Referred from', 'Sunita Kumari (ASHA)']] });
    rules(s, [
      'PW registration, ANC visits, Delivery outcome and HWC referral lists open inside SupervisorActivity [nav_supervisor.xml:56-80]',
      'Buttons hidden: PwRegistrationListAdapter.kt:55, AncVisitListAdapter.kt:111, AncVisitAdapter (View), BenListAdapterForForm.kt:59',
      'HWC referral list opens in the Supervisor screen for non-ASHA roles [HwcReferredFragment.kt onStart]',
    ]);
  }

  // ======================= MITANIN VARIANTS (addendum) =======================
  // 43 ANC form · Mitanin (no MCP card section)
  {
    const s = F('M2.2-43 · ANC visit form · Mitanin', 1, 7);
    screenTop(s, 'ANC visit 3', 'Meena Kumari · 24 weeks · Mitanin', 'more');
    const b = body(s); b.itemSpacing = 12;
    section(b, 'Done', 'Visit', 'Complete');
    section(b, 'Done', 'Pregnancy status', 'Complete');
    section(b, 'Done', 'Examination', 'Complete');
    section(b, 'Done', 'Td / TT doses', 'Saved on registration');
    section(b, 'Done', 'Supplements', 'Complete');
    section(b, 'Open', 'High risk', '1 question', 6);
    await seg(b, 'Any high-risk condition?', ['Yes', 'No'], 1);
    await caption(b, 'No MCP card section on Mitanin: front/back uploads and their heading are removed');
    bottomBar(s, null, 'Submit');
    rules(s, [
      'Mitanin: MCP card headline + front/back uploads removed [PregnantWomanAncVisitDataset.kt:475-479]',
      'Mitanin: upload tap ignored, no chooser [PwAncFormFragment.kt:189]',
      'All other fields and rules as M2.2-05 / M2.2-25',
    ]);
  }
  // 44 Delivery outcome · Mitanin (no MCP uploads, JSY reveals nothing)
  {
    const s = F('M2.2-44 · Delivery outcome form · Mitanin', 2, 7);
    screenTop(s, 'Delivery outcome', 'Meena Kumari · Mitanin', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Delivery date *', '12 Feb 2027');
    select(b, 'Place of delivery *', 'CHC Bilha (institutional)');
    await radios(b, 'Type of delivery *', ['Normal', 'Caesarean (C-section)', 'Assisted (forceps / vacuum)'], 0);
    steppers(b, [['Live births', '1'], ['Still births', '0']]);
    date(b, 'Discharge date', '14 Feb 2027');
    await seg(b, 'JSY beneficiary?', ['Yes', 'No'], 0);
    await caption(b, 'JSY = Yes shows no voucher upload on Mitanin. No Documents section.');
    bottomBar(s, null, 'Submit');
    rules(s, [
      'Mitanin: MCP card uploads removed (new and saved record) [DeliveryOutcomeDataset.kt:264-267,295-298]',
      'Mitanin: JSY = Yes does not reveal the JSY voucher upload [DeliveryOutcomeDataset.kt:466]',
      'Other rules as M2.2-09',
    ]);
  }
  // 45 PNC form · Mitanin (no discharge summary, no reminder / submit prompt)
  {
    const s = F('M2.2-45 · PNC visit form · Mitanin', 3, 7);
    screenTop(s, 'PNC visit · day 3', 'Meena Kumari · Mitanin', 'more');
    const b = body(s); b.itemSpacing = 12;
    select(b, 'PNC period *', 'Day 3'); date(b, 'Visit date *', '15 Feb 2027');
    await seg(b, 'Using a contraception method?', ['Yes', 'No'], 0);
    const rv = reveal(b); select(rv, 'Method *', 'Female sterilisation');
    await caption(rv, 'No incentive reminder popup on Mitanin');
    await seg(b, 'Is the mother alive?', ['Yes', 'No'], 0);
    await caption(b, 'No delivery discharge summary uploads. Submit saves directly (no upload prompt).');
    bottomBar(s, null, 'Submit');
    rules(s, [
      'Mitanin: 4 delivery discharge summary uploads removed [PncFormDataset.kt:276-281]',
      'Mitanin: no "Reminder – upload discharge summary" popup [PncFormDataset.kt:634]',
      'Mitanin: no "upload discharge summary?" dialog on Submit [PncFormFragment.kt:160]; upload tap ignored [:102]',
    ]);
  }
  // 46 ANC visits list · Mitanin
  {
    const s = F('M2.2-46 · ANC visits list · Mitanin', 4, 7);
    screenTop(s, 'Pregnant women · ANC', 'Mitanin · 38 women');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile', { sort: true });
    await sortLabel(b);
    await ancCard(b, { name: 'Meena Kumari', meta: '29 yrs · 28 wks', tags: [['Maternal', 'ANC 4 due']], rows: [['Last visit', '05 Oct 2026'], ['Home visits', '3 of 9']], delivered: false, actions: [['Filled', 'Add ANC'], ['Outlined', 'ANC visits'], ['Outlined', 'Add home visit'], ['Text', 'View home visits']] });
    await ancCard(b, { name: 'Durga Sahu', meta: '24 yrs · 16 wks', tags: [['Maternal', 'ANC 2 due']], rows: [['Last visit', '20 Oct 2026'], ['Home visits', '9 of 9']], actions: [['Filled', 'Add ANC'], ['Outlined', 'ANC visits'], ['Outlined', 'Add home visit', true], ['Text', 'View home visits']], footnote: '9 home visits done: Add home visit hidden / disabled' });
    await ancCard(b, { name: 'Kamla Verma', meta: '27 yrs · 12 wks', tags: [['Maternal', 'Registered']], rows: [['Last visit', '25 Oct 2026'], ['Home visits', '1 of 9']], actions: [['Filled', 'Add ANC', true], ['Outlined', 'ANC visits'], ['Outlined', 'Add home visit', true], ['Text', 'View home visits']], footnote: 'Last home visit 20 Oct: next allowed from 19 Nov (30 days)' });
    rules(s, [
      'Mitanin: "Add home visit" / "View home visits" on ANC and high-risk list cards; hidden on other flavours [AncVisitListAdapter.kt:136-148]',
      'Add home visit only while < 9 visits and ≥ 30 days since the last [RecordsRepo.kt:629-657] → form M2.2-06, sheet M2.2-29/30',
    ]);
  }

  const endY = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, endY, 900,
    'MATERNAL HEALTH NOTES\n' +
    '• Rows: 0 registration (01–03, filter 17, sort 18, view mode 19) · 1 ANC list (04, 20–24) · 2 ANC form (05, 25–28, 16) · 3 home visit, Mitanin (06, 29–32) · 4 PMSMA / high risk (08, 33, 07, 35, 36, 34) · 5 delivery & PNC list (37, 09–12, 38) · 6 PNC form, CAC, HWC (13, 39–41, 14, 15) · 7 supervisor (42) + Mitanin variants (43 ANC form, 44 Delivery, 45 PNC, 46 ANC list).\n' +
    '• Forms follow P3: numbered sections, conditional fields indented in blue, auto values grey with lock. All field names come from the *Dataset.kt files.\n' +
    '• Every list: voice search (mic) and sort sheet (18). Cards: red Register/Add → green View once a record exists.\n' +
    '• Death branch (ANC, Delivery, PNC) saves and opens the MDSR form directly (28).\n' +
    '• Gamification hooks: ANC submit → Snackbar Badge "Maternal Journey x of 4 ANC"; facility delivery after 4+ ANC → celebration (M2.1-04); LBW baby → "Vulnerable Baby" progress. Hidden when gamification is OFF.\n' +
    '• Flavours: Mitanin builds say "Mitanin" instead of "ASHA", get ANC home visits (06, 29–32), hide all document uploads and get Rh auto-fill; Xushrukha makes Hb/BP required and uses only HRP flows (08) from this module.');
  fitSection(sec);
  return ['2.2 screens: ' + frames.length];
}
