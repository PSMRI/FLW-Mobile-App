// ---------------------------------------------------------------------------
// Stage 2.4 · Child care + infant
// Sources: ui/home_activity/child_care/*, infant/*, HBNCFormDataset, HBYCFormDataset,
// ChildRegistrationDataset, AdolescentHealthFormDataset, CUFY dynamic forms (SAM / ORS / IFA)
// 2026-11-05 gap fix: gap_C (child rows) + gap_B (infant / HBNC / HBYC / infant reg / child reg)
// → edited 02–12, added 13–29. Code is the source of truth.
// ---------------------------------------------------------------------------
async function buildStage24(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureSearchMic(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack, overlayCenter } = S;
  const { actionCard, dialog, visitSheet, sortSheet, searchRow, viewMode, uploads, table, rules, caption, tag, btn, row } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.4 · Child care & infant');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };
  // states: done | due | missed | next | off (disabled: baby/child dead)
  const dayGrid = async (p, days) => {
    const grid = AL('HORIZONTAL', 'Days'); grid.layoutWrap = 'WRAP'; grid.itemSpacing = 8; grid.counterAxisSpacing = 8; add(p, grid);
    for (const [d, st, sub] of days) {
      const c = AL('VERTICAL', d); c.counterAxisAlignItems = 'CENTER'; c.primaryAxisAlignItems = 'CENTER'; c.itemSpacing = 2; c.resize(74, 64); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED'; rad(c, 'radius/md');
      fill(c, st === 'done' ? 'color/success-container' : st === 'due' ? 'color/warning-container' : st === 'missed' ? 'color/error-container' : 'color/surface-container-high'); grid.appendChild(c);
      if (st === 'off') c.opacity = 0.4;
      c.appendChild(await txt(d, 'Title/Small', 'color/on-surface'));
      if (sub) c.appendChild(await txt(sub, 'Label/Small', st === 'missed' ? 'color/error' : 'color/on-surface-variant'));
    }
    return grid;
  };
  const sortLabel = async (p, v = 'Newest first') => { const r = row(p, 'Sort label', 6); r.appendChild(icon('sort', 'color/on-surface-variant', 16)); r.appendChild(await txt('Sorted: ' + v, 'Label/Medium', 'color/on-surface-variant')); return r; };
  const muac = async (p, sel) => {
    await label(p, 'MUAC tape colour *');
    const cr = AL('HORIZONTAL', 'MUAC'); gap(cr, 'spacing/xs'); add(p, cr);
    for (const [l, tone] of [['Green', 'immunization'], ['Yellow', 'child'], ['Red', 'disease']]) {
      const on = l === sel;
      const c = AL('HORIZONTAL', l); c.counterAxisAlignItems = 'CENTER'; c.primaryAxisAlignItems = 'CENTER'; c.itemSpacing = 6; pad(c, 'spacing/sm'); c.resize(100, 48); c.counterAxisSizingMode = 'FIXED'; c.primaryAxisSizingMode = 'FIXED'; rad(c, 'radius/md');
      fill(c, `module/${tone}/bg`); if (on) stroke(c, `module/${tone}/fg`, 3); cr.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      const dot = figma.createEllipse(); dot.resize(14, 14); dot.fills = [paint(`module/${tone}/fg`)]; c.appendChild(dot);
      c.appendChild(await txt(l, 'Label/Large', 'color/on-surface'));
    }
  };
  const photos = (p, n, captured = 0) => { const ph = AL('HORIZONTAL', 'Photos'); gap(ph, 'spacing/sm'); ph.layoutWrap = 'WRAP'; ph.counterAxisSpacing = 8; add(p, ph); for (let i = 0; i < n; i++) ph.appendChild(kinst('Photo tile', `State=${i < captured ? 'Captured' : 'Empty'}`)); return ph; };

  // 01 Hub
  {
    const s = F('M2.4-01 · Child care hub', 0, 0);
    screenTop(s, 'Child care', '96 children under 5');
    const b = body(s);
    await stat(b, [['5', 'HBNC visits due', 'child'], ['2', 'Low birth weight', 'disease'], ['4', 'SAM checks due', 'care']]);
    tileGrid(b, [['child', 'Newborns (0–42 days)', '9 babies', 'child'], ['child', 'Children (3–15 m) · HBYC', '31 children', 'calendar'], ['care', 'Children under 5', '96 children', 'group'], ['couple', 'Adolescents (10–19)', '58 adolescents', 'person']]);
    add(b, inst('sectionHeader', null, { Title: 'Register', 'Show action': false }));
    tileGrid(b, [['child', 'Child registration', '9 pending', 'person_add']]);
    rules(s, ['Code hub = 4 tiles with counts (IconDataset.kt:447-474)', 'Stat tiles and "Child registration" tile here: not in app today – needs dev (code: Child registration lives under Mother care → M2.4-29)']);
  }
  // 02 Newborn list (edited: search + RCH filter, overdue > 42 days, no sync icon, HBNC button, deceased)
  {
    const s = F('M2.4-02 · Newborn list (HBNC)', 1, 0);
    screenTop(s, 'Newborns', '9 babies · 5 visits due');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'Due', 'Count 1': '5', 'Label 2': 'Done', 'Count 2': '31', 'Label 3': 'All', 'Count 3': '9' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search baby, mother or RCH ID', { filter: true });
    chips(b, ['All', 'Low birth weight', 'SNCU']);
    await actionCard(b, { name: 'Baby of Sunita', initials: 'BS', meta: 'Girl · 3 days · mother Sunita Devi', tags: [['Child', 'HBNC day 3 due today']], rows: [['RCH ID', '1234 5678 9012'], ['DOB', '30 Oct 2026']], actions: [['Filled', 'HBNC']] });
    await actionCard(b, { name: 'Baby of Pinki', initials: 'BP', meta: 'Boy · 46 days · mother Pinki Devi', tags: [['Alert', 'Overdue · over 42 days']], rows: [['RCH ID', '1234 5678 7781'], ['DOB', '18 Sep 2026']], icons: [['warning', 'Overdue', 'color/error']], actions: [['Filled', 'HBNC']] });
    await actionCard(b, { name: 'Baby of Rani', initials: 'BR', meta: 'Girl · 12 days · mother Rani Devi', state: 'deceased', stateNote: 'Deceased', actions: [['Filled', 'HBNC']] });
    rules(s, [
      'Search box with voice search (InfantListFragment.kt:112-133)',
      'Filter icon → dialog "Show only records with an RCH ID" → M2.4-13 (Fragment:38-64, 85-87)',
      'Overdue icon when baby is older than 42 days (HBNC window over) (InfantListAdapter.kt:54-63)',
      'Sync icon intentionally hidden on these cards (showSyncIcon=false, :46)',
      '"HBNC" button → HBNC day schedule M2.4-03 (Fragment:89-99). Deceased marker iv_isDeath when isDeath',
      'Chips "Low birth weight / SNCU": not in app today – needs dev',
    ]);
  }
  // 03 HBNC schedule (edited: mandatory / optional, unlock order, RCH header, visit date)
  {
    const s = F('M2.4-03 · HBNC schedule & visit card', 2, 0, true);
    screenTop(s, 'Newborns', '9 babies · 5 visits due');
    const b = body(s); recordCard(b, { name: 'Baby of Sunita', meta: 'Girl · 3 days · 2,450 g', tag: 'HBNC day 3 due today · LBW', tone: 'Alert', action: 'HBNC' });
    scrim(s);
    const sh = sheet(s, 'HBNC schedule');
    sh.appendChild(await txt('Baby of Sunita · HBNC', 'Title/Medium', 'color/on-surface'));
    sh.appendChild(await txt('RCH ID 1234 5678 9012', 'Body/Small', 'color/on-surface-variant'));
    await dayGrid(sh, [['Day 1', 'done', 'Done 31 Oct'], ['Day 3', 'due', 'Mandatory *'], ['Day 7', 'next', 'Mandatory *'], ['Day 14', 'next', 'Optional'], ['Day 21', 'next', 'Optional'], ['Day 28', 'next', 'Optional'], ['Day 42', 'next', 'Mandatory *']]);
    const wc = AL('VERTICAL', 'Weight trend'); pad(wc, 'spacing/md', 'spacing/sm'); gap(wc, 'spacing/xs'); rad(wc, 'radius/md'); fill(wc, 'module/child/bg'); add(sh, wc);
    wc.appendChild(await txt('Low birth weight baby: 7 visits needed', 'Title/Small', 'color/on-surface'));
    const wt = await txt('Complete all 7 visits to earn "Vulnerable Baby Cared For" (gamification ON).', 'Body/Small', 'color/on-surface-variant'); wc.appendChild(wt); wrapText(wt);
    add(sh, inst('button', 'Style=Filled, State=Enabled', { Label: 'Start day 3 visit', 'Show icon': false }));
    pinBottom(s, sh);
    rules(s, [
      'Day 1 / 3 / 7 / 42 labelled "Mandatory *", Day 14 / 21 / 28 "Optional" (VisitCardAdapter.kt:89-110)',
      'Unlock: Day 3 after Day 1, Day 7 after Day 3; Days 14, 21, 28, 42 all open together once Day 7 is done (HBNCFormViewModel.kt:355-362)',
      'Done card shows visit date and opens read-only. Header shows RCH / Ben ID. Form sync starts on open (InfantDayListFragment.kt:52)',
      'Baby dead → all remaining visits disabled → M2.4-14',
      'Legacy HbncDayListFragment / HBNC Part I, Part II, visit card: dead code (no inbound nav), not designed',
    ]);
  }
  // 04 HBNC visit form (edited: SNCU auto, photo, rules)
  {
    const s = F('M2.4-04 · HBNC visit form (day 3)', 3, 0);
    screenTop(s, 'HBNC · day 3', 'Baby of Sunita · 2,450 g at birth', 'more');
    const b = body(s); b.itemSpacing = 12;
    section(b, 'Done', 'Birth details (auto)', 'Complete');
    date(b, 'Visit date *', '02 Nov 2026');
    auto(b, 'Discharged from SNCU (auto: SNCU baby)', 'Yes');
    section(b, 'Open', 'Baby check', '8 checks', 2);
    steppers(b, [['Weight today (g) *', '2,420'], ['Temperature (°F)', '98.4']]);
    await seg(b, 'Breastfed at least 8 times in 24 hours?', ['Yes', 'No'], 0);
    await seg(b, 'Yellow eyes, palms or skin (jaundice)?', ['Yes', 'No'], 1);
    await seg(b, 'Breathing faster than 60 per minute?', ['Yes', 'No'], 1);
    await seg(b, 'Pus or redness at the navel?', ['Yes', 'No'], 1);
    await seg(b, 'Eyes swollen or with pus?', ['Yes', 'No'], 1);
    await checks(b, 'Sepsis signs (tick any seen)', [['All limbs limp', false], ['Feeding less / stopped', false], ['Chest pulls in while breathing', false], ['Cold to touch or feverish', false], ['Swollen belly / vomits often', false]]);
    await label(b, 'Photo (optional)'); photos(b, 1, 0);
    section(b, 'Closed', 'Mother check', '5 questions', 3);
    section(b, 'Closed', 'Counselling', 'Warmth, breastfeeding, cord care', 4);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, [
      'SNCU baby: "Discharged from SNCU" auto-set to Yes (HBNCFormFragment.kt:211-218)',
      'Visit date: not after today, not before delivery, after previous visit, on/after the day\'s due date (error shows the date) (:276-310)',
      'Image: Take photo / Choose from gallery, size limit toast (showImagePickerDialog)',
      'Opened from a done day → read-only (isViewMode)',
      'Danger sign (Mitanin) → M2.4-16; baby not alive → M2.4-17',
      '"Save draft": not in app today – needs dev',
    ]);
  }
  // 05 Danger sign → refer (edited: reason, rules)
  {
    const s = F('M2.4-05 · HBNC danger sign → refer', 4, 0, true);
    screenTop(s, 'HBNC · day 7', 'Baby of Pinki · 9 days', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Breathing faster than 60 per minute?', ['Yes', 'No'], 0);
    await checks(b, 'Sepsis signs (tick any seen)', [['Feeding less / stopped', true], ['Chest pulls in while breathing', true]]);
    scrim(s);
    const sh = sheet(s, 'Refer now');
    const ic = box('Alert', 56, 56); ic.cornerRadius = 28; fill(ic, 'color/error-container'); ic.appendChild(icon('warning', 'color/error', 30)); sh.appendChild(ic);
    sh.appendChild(await txt('Possible sepsis: refer today', 'Title/Large', 'color/on-surface'));
    const d = await txt('Fast breathing and chest indrawing are danger signs. Send the baby to SNCU / PHC now. Keep the baby warm and breastfeeding on the way.', 'Body/Medium', 'color/on-surface-variant'); add(sh, d); d.textAutoResize = 'HEIGHT'; d.textAlignHorizontal = 'CENTER';
    auto(sh, 'Reason', 'Suspected newborn complication');
    select(sh, 'Refer to *', 'HWC Sirsi');
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(sh, r);
    const c1 = inst('button', 'Style=Tonal, State=Enabled', { Label: 'Call 108', 'Show icon': true, Icon: 'call' }); r.appendChild(c1); c1.layoutSizingHorizontal = 'FILL';
    const c2 = inst('button', 'Style=Filled, State=Enabled', { Label: 'Save referral', 'Show icon': false }); r.appendChild(c2); c2.layoutSizingHorizontal = 'FILL';
    pinBottom(s, sh);
    rules(s, [
      'Mitanin only: danger sign → dialog M2.4-16 → this referral bottom sheet, once per visit (HBNCFormFragment.kt:201-203)',
      'Reason pre-set "Suspected newborn complication"; referral goes to HWC',
      '"Call 108": not in app today – needs dev',
    ]);
  }
  // 06 Child list (HBYC) (edited: search + RCH filter, HBYC button → month schedule)
  {
    const s = F('M2.4-06 · Children 3–15 months (HBYC)', 5, 0);
    screenTop(s, 'HBYC', 'Children 3–15 months · 31');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'Due', 'Count 1': '7', 'Label 2': 'Done', 'Count 2': '24', 'Label 3': 'All', 'Count 3': '31' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search child, mother or RCH ID', { filter: true });
    recordCard(b, { name: 'Aarav Kumar', meta: 'Boy · 6 months · mother Sunita Devi', tag: 'HBYC 6-month visit due', tone: 'Child', action: 'HBYC' });
    recordCard(b, { name: 'Riya Kumari', meta: 'Girl · 10 months · yellow on MCP card', tag: 'Underweight · watch', tone: 'Alert', action: 'HBYC' });
    recordCard(b, { name: 'Dev Yadav', meta: 'Boy · 12 months', tag: 'HBYC 12-month visit due', tone: 'Child', action: 'HBYC', sync: 'Pending' });
    rules(s, [
      'Search box with voice search (ChildListFragment.kt:116-137)',
      'Filter dialog "Show only records with an RCH ID" → M2.4-13 (Fragment:42-65, 87-89)',
      '"HBYC" button opens the month schedule M2.4-15 first, then the form (Fragment:91-103)',
    ]);
  }
  // ---- Row 2
  // 07 HBYC form (edited: photo, rules)
  {
    const s = F('M2.4-07 · HBYC visit form', 0, 1);
    screenTop(s, 'HBYC · 9 months', 'Riya Kumari · girl', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Visit date *', '02 Nov 2026');
    await seg(b, 'Vaccinated for age?', ['Yes', 'No'], 0);
    await seg(b, 'ORS packet given?', ['Yes', 'No'], 0);
    await seg(b, 'Vitamin A given for age?', ['Yes', 'No'], 0);
    await label(b, 'Weight for age (colour on MCP card) *');
    const cr = AL('HORIZONTAL', 'Colours'); gap(cr, 'spacing/xs'); add(b, cr);
    for (const [l, on, tone] of [['Green', false, 'immunization'], ['Yellow', true, 'child'], ['Orange', false, 'disease']]) {
      const c = AL('HORIZONTAL', l); c.counterAxisAlignItems = 'CENTER'; c.primaryAxisAlignItems = 'CENTER'; c.itemSpacing = 6; pad(c, 'spacing/sm'); c.resize(100, 48); c.counterAxisSizingMode = 'FIXED'; c.primaryAxisSizingMode = 'FIXED'; rad(c, 'radius/md');
      fill(c, `module/${tone}/bg`); if (on) stroke(c, `module/${tone}/fg`, 3); cr.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      const dot = figma.createEllipse(); dot.resize(14, 14); dot.fills = [paint(`module/${tone}/fg`)]; c.appendChild(dot);
      c.appendChild(await txt(l, 'Label/Large', 'color/on-surface'));
    }
    await seg(b, 'Was the child ill?', ['Yes', 'No'], 1);
    await seg(b, 'Delay in growth or development?', ['Yes', 'No'], 0);
    const rv = reveal(b); select(rv, 'Referred to *', 'RBSK team');
    await label(b, 'Photo (optional)'); photos(b, 1, 0);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, [
      'Mitanin only: child sick / developmental delay / diarrhoea / breathing difficulty → refer dialog + referral sheet → M2.4-18 (HBYCFormFragment.kt:172; VM:84-90)',
      'Visit-date validation and image upload (Take photo / Gallery, size limit) (:153-216)',
      'Done month opens read-only. Legacy HbycFragment: unreachable, not designed',
      '"Save draft": not in app today – needs dev',
    ]);
  }
  // 08 Children under 5 + forms sheet (edited: card with 3 buttons + rules)
  {
    const s = F('M2.4-08 · Children under 5 · forms', 1, 1, true);
    screenTop(s, 'Children under 5', '96 children · 4 SAM checks due');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search child or mother');
    chips(b, ['All', 'SAM check due', 'SAM / MAM']);
    await actionCard(b, { name: 'Golu Kumar', meta: 'Boy · 2 yrs 4 m · mother Rinki', actions: [['Outlined', 'Check SAM'], ['Outlined', 'ORS'], ['Outlined', 'IFA syrup']] });
    scrim(s);
    const sh = sheet(s, 'Forms');
    sh.appendChild(await txt('Golu Kumar · forms', 'Title/Medium', 'color/on-surface'));
    for (const [t, d, ic, tone, st] of [['SAM check (MUAC)', 'Last: never · due now', 'warning', 'disease', 'Start'], ['ORS & zinc', 'Diarrhoea episodes', 'hospital', 'care', 'Add'], ['IFA syrup', 'Visit 3 · last 5 Oct', 'vaccine', 'immunization', 'Add']]) {
      const r = AL('HORIZONTAL', t); r.counterAxisAlignItems = 'CENTER'; pad(r, 'spacing/sm', 'spacing/sm'); gap(r, 'spacing/sm'); rad(r, 'radius/md'); fill(r, 'color/surface-container-high'); add(sh, r);
      const tl = box('Icon', 40, 40); tl.cornerRadius = 20; fill(tl, `module/${tone}/bg`); tl.appendChild(icon(ic, `module/${tone}/fg`, 20)); r.appendChild(tl);
      const c = AL('VERTICAL'); c.itemSpacing = 2; r.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      c.appendChild(await txt(t, 'Title/Small', 'color/on-surface')); c.appendChild(await txt(d, 'Body/Small', 'color/on-surface-variant'));
      r.appendChild(await txt(st, 'Label/Large', 'color/primary'));
    }
    const nt = await txt('These forms are server-driven (CUFY dynamic forms); fields come from the form JSON.', 'Body/Small', 'color/on-surface-variant'); add(sh, nt); nt.textAutoResize = 'HEIGHT';
    pinBottom(s, sh);
    rules(s, [
      'Code card has 3 buttons: SAM / ORS / IFA syrup, each opens its own visit sheet (children_under_five_years_item.xml:345-367) → M2.4-19…22',
      'Combined "Forms" sheet: not in app today – needs dev',
      'Search with voice (CUFYListFragment.kt:81-100). List scope = children 0–5 yrs (RecordsRepo.kt:251)',
      'Chips "SAM check due / SAM / MAM": not in app today – needs dev',
    ]);
  }
  // 09 SAM check → NRC referral (edited: child card, photo, rules)
  {
    const s = F('M2.4-09 · SAM check → NRC referral', 2, 1);
    screenTop(s, 'SAM check', 'Golu Kumar · 2 yrs 4 m', 'more');
    const b = body(s); b.itemSpacing = 12;
    await person(b, 'GK', 'Golu Kumar', 'Boy · 2 yrs 4 m · mother Rinki Devi · RCH 1234 5678 4410');
    banner(b, 'Info', 'Server-driven form', 'Fields shown are illustrative. Confirm with the CUFY SAM form JSON.', null);
    date(b, 'Date *', '02 Nov 2026');
    await muac(b, 'Red');
    await seg(b, 'Swelling of both feet (oedema)?', ['Yes', 'No'], 1);
    banner(b, 'Error', 'SAM: severe acute malnutrition', 'Refer to the NRC (Nutrition Rehabilitation Centre) today.', null);
    select(b, 'Refer to *', 'NRC, District Hospital');
    await label(b, 'Photo'); photos(b, 1, 0);
    bottomBar(s, null, 'Save & refer to NRC');
    snack(s, 'Badge', { Message: 'Referral saved', Detail: 'Critical Referral Made · badge earned', Action: 'See badge' });
    rules(s, [
      'Child info card = infant card with HBNC button hidden (CUFYFormFragment.kt:191-196)',
      'MUAC / weight-for-height SAM → blocking alert dialog, see M2.4-25 (Fragment:358-375; HelperUtil.kt:974-999)',
      'SAM visit date must be unique and later than all earlier visits (toasts) (Fragment:644-715)',
      'Photo: Take photo / Choose from gallery dialog with size check (Fragment:431-457, 119-140)',
      'After save: separate sync worker per form (SAM, ORS, IFA), go back (Fragment:230-247). Saved → M2.4-23',
    ]);
  }
  // 10 Adolescent list (edited: girls only form, Register / View, consent icon, sync)
  {
    const s = F('M2.4-10 · Adolescent list', 3, 1);
    screenTop(s, 'Adolescents', '10–19 years · 58');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or HH ID');
    chips(b, ['All', 'Girls', 'Follow-up due']);
    await actionCard(b, { name: 'Pooja Kumari', meta: 'Girl · 15 yrs · D/o Ramesh Kumar', sync: 'Synced', icons: [['check_circle', 'Consent', 'color/success']], actions: [['Tonal', 'View']] });
    await actionCard(b, { name: 'Neha Yadav', meta: 'Girl · 13 yrs · D/o Suresh Yadav', icons: [['warning', 'Not verified', 'color/warning']], actions: [['Filled', 'Register']], footnote: 'No record yet → no sync icon' });
    await actionCard(b, { name: 'Rohit Kumar', meta: 'Boy · 17 yrs · S/o Mahesh Kumar', icons: [['check_circle', 'Consent', 'color/success']], footnote: 'Boys: no form button' });
    rules(s, [
      'Form button only for girls (AdolescentHealthListAdapter.kt:66-70)',
      'Red "Register" before the first record, green "View" after (:107-108)',
      'Mobile consent verified / not-verified icon; hidden on Mitanin (rv_item_adolscent_health_list.xml:338-341)',
      'Sync icon only once a record exists (:53). Relation = father / husband / spouse by gender and age (:72-105)',
      'Chips: not in app today – needs dev',
    ]);
  }
  // 11 Adolescent form (edited: rules)
  {
    const s = F('M2.4-11 · Adolescent health form', 4, 1);
    screenTop(s, 'Adolescent health', 'Pooja Kumari · 15 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Visit date *', '02 Nov 2026'); select(b, 'Health status *', 'Healthy');
    await seg(b, 'IFA tablets given?', ['Yes', 'No'], 0);
    const rv = reveal(b); steppers(rv, [['Number of IFA tablets', '4']]);
    await seg(b, 'Menstrual hygiene awareness given?', ['Yes', 'No'], 0);
    await seg(b, 'Sanitary napkins given?', ['Yes', 'No'], 0);
    const rv2 = reveal(b); steppers(rv2, [['Packets given', '2']]); select(rv2, 'Place *', 'Anganwadi centre');
    await seg(b, 'Counselling given?', ['Yes', 'No'], 0);
    const rv3 = reveal(b); select(rv3, 'Counselling type', 'Nutrition & anaemia');
    await seg(b, 'Referred to a health facility?', ['Yes', 'No'], 1);
    date(b, 'Follow-up date', '10 Dec 2026');
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, [
      'One record per adolescent, edited in place (no repeat visits). Saved record → view mode M2.4-26 (AdolescentFormFragment.kt:40-73)',
      'Toasts "Saving…", "Saved Successfully!", "Save Failed!"; Submit disabled while saving; sync after save (:75-93)',
      '"Save draft": not in app today – needs dev',
    ]);
  }
  // 12 Child registration (edited: PDF upload, side effects)
  {
    const s = F('M2.4-12 · Child registration', 5, 1);
    screenTop(s, 'Register child', 'Mother: Sunita Devi', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Date of registration *', '02 Nov 2026');
    text(b, 'Name of child *', 'Aarav Kumar'); text(b, 'Child RCH ID', '1234 5678 9020');
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    const d1 = date(r, 'Date of birth *', '12 Feb 2026'); d1.layoutSizingHorizontal = 'FILL';
    await seg(b, 'Sex *', ['Girl', 'Boy', 'Other'], 1);
    auto(b, "Mother's name", 'Sunita Devi'); auto(b, "Father's name", 'Ramesh Kumar');
    await seg(b, 'Whose mobile number?', ['Mother', 'Father', 'Other'], 0);
    text(b, 'Birth certificate number', 'BR-2026-004512');
    steppers(b, [['Weight at birth (g)', '2,450']]);
    select(b, 'Place of birth', 'PHC Sirsi');
    await uploads(b, 'Birth certificate (front & back)', [['birth_cert_front.jpg', 'Done'], ['birth_cert_back.pdf', 'Done']], 'Camera, gallery or PDF · size limit');
    bottomBar(s, 'Save draft', 'Register');
    rules(s, [
      'Saving creates a new household member (child beneficiary) and fills DOB, gender, age into the mother\'s EC record (child slot 1–9) (ChildRegViewModel.kt:109-150)',
      'Birth certificate front / back: camera, gallery or PDF (ChildRegFragment.kt:183-204)',
      'View-only after save, Submit disabled (:155)',
      '"Save draft": not in app today – needs dev',
    ]);
  }
  // ---- Row 3
  // 13 RCH filter dialog (NEW)
  {
    const s = F('M2.4-13 · RCH ID filter dialog', 0, 2, true);
    screenTop(s, 'Newborns', '9 babies · 5 visits due');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search baby, mother or RCH ID', { filter: true });
    recordCard(b, { name: 'Baby of Sunita', meta: 'Girl · 3 days', tag: 'HBNC day 3 due', tone: 'Child', action: 'HBNC' });
    await dialog(s, { ic: 'filter', icColor: 'color/primary', title: 'Filter', body: null, cancel: 'Cancel', ok: 'OK', extra: async (c) => { add(c, kinst('Checkbox option', 'Checked=true', { Label: 'Show only records with an RCH ID' })); } });
    rules(s, ['Same dialog on Newborn list (InfantListFragment.kt:38-64) and HBYC child list (ChildListFragment.kt:42-65) (CommonUtils.kt:58-68)']);
  }
  // 14 HBNC schedule · baby died (NEW)
  {
    const s = F('M2.4-14 · HBNC schedule · baby died', 1, 2, true);
    screenTop(s, 'Newborns', '9 babies · 5 visits due');
    const b = body(s); recordCard(b, { name: 'Baby of Rani', meta: 'Girl · 12 days', tag: 'Deceased', tone: 'Neutral', action: 'HBNC' });
    scrim(s);
    const sh = sheet(s, 'HBNC schedule · deceased');
    sh.appendChild(await txt('Baby of Rani · HBNC', 'Title/Medium', 'color/on-surface'));
    banner(sh, 'Error', 'Baby recorded dead', 'Remaining HBNC visits are closed.', null);
    await dayGrid(sh, [['Day 1', 'done', 'Done 25 Oct'], ['Day 3', 'done', 'Done 27 Oct'], ['Day 7', 'done', 'Done 31 Oct'], ['Day 14', 'off', 'Disabled'], ['Day 21', 'off', 'Disabled'], ['Day 28', 'off', 'Disabled'], ['Day 42', 'off', 'Disabled']]);
    pinBottom(s, sh);
    rules(s, ['Baby recorded dead → all remaining visit cards disabled; done ones stay viewable (InfantDayListFragment.kt:76-81; VisitCardAdapter.kt:123-130)', 'Banner text: not in app today – needs dev']);
  }
  // 15 HBYC month schedule (NEW)
  {
    const s = F('M2.4-15 · HBYC month schedule', 2, 2, true);
    screenTop(s, 'HBYC', 'Children 3–15 months · 31');
    const b = body(s); recordCard(b, { name: 'Riya Kumari', meta: 'Girl · 10 months', tag: 'HBYC 9-month visit due', tone: 'Child', action: 'HBYC' });
    scrim(s);
    const sh = sheet(s, 'HBYC schedule');
    sh.appendChild(await txt('Riya Kumari · HBYC', 'Title/Medium', 'color/on-surface'));
    sh.appendChild(await txt('RCH ID 1234 5678 6620', 'Body/Small', 'color/on-surface-variant'));
    await dayGrid(sh, [['3 months', 'done', 'View'], ['6 months', 'done', 'View'], ['9 months', 'due', 'Fill now']]);
    await caption(sh, '12 and 15 months appear once Riya reaches that age. Child dead → all months disabled.');
    add(sh, inst('button', 'Style=Filled, State=Enabled', { Label: 'Start 9-month visit', 'Show icon': false }));
    pinBottom(s, sh);
    rules(s, [
      'Months 3, 6, 9, 12, 15: only months the child has reached are shown (HBYCFormViewModel.kt:326-370)',
      'Next undone month editable; done months open view-only',
      'Child dead → visits disabled (ChildMonthListFragment isBenDead observer)',
      'Replaces legacy HbycMonthListFragment (dead code)',
    ]);
  }
  // 16 HBNC danger dialog · Mitanin (NEW)
  {
    const s = F('M2.4-16 · HBNC danger sign dialog (Mitanin)', 3, 2, true);
    screenTop(s, 'HBNC · day 7', 'Baby of Pinki · 9 days', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Breathing faster than 60 per minute?', ['Yes', 'No'], 0);
    await checks(b, 'Sepsis signs (tick any seen)', [['Chest pulls in while breathing', true]]);
    await dialog(s, { title: 'Danger signs identified', body: 'Refer the baby to HWC?', cancel: 'No', ok: 'Refer' });
    rules(s, ['Mitanin builds only, once per visit (HBNCFormFragment.kt:201-203, showDangerSignDialog)', 'Refer → referral bottom sheet with reason "Suspected newborn complication" (M2.4-05)']);
  }
  // 17 HBNC infant death → CDR (NEW)
  {
    const s = F('M2.4-17 · HBNC infant death → CDR', 4, 2, true);
    screenTop(s, 'HBNC · day 14', 'Baby of Rani · 12 days', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Visit date *', '06 Nov 2026');
    await seg(b, 'Is the baby alive?', ['Yes', 'No'], 1);
    await dialog(s, { ic: 'warning', title: 'Infant death reported', body: 'Please complete the Child Death Review (CDR) form.', cancel: null, ok: 'Open CDR form', danger: true });
    rules(s, ['Baby not alive → alert then opens the CDR form (HBNCFormFragment.kt:167, showDeathAlertDialog)', 'Afterwards remaining HBNC visits are disabled (M2.4-14)']);
  }
  // 18 HBYC danger dialog · Mitanin (NEW)
  {
    const s = F('M2.4-18 · HBYC danger sign → refer (Mitanin)', 5, 2, true);
    screenTop(s, 'HBYC · 9 months', 'Riya Kumari · girl', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Was the child ill?', ['Yes', 'No'], 0);
    await seg(b, 'Diarrhoea?', ['Yes', 'No'], 0);
    await dialog(s, { title: 'Danger signs identified', body: 'Refer the child to HWC? Reason: Suspected illness in young child.', cancel: 'No', ok: 'Refer' });
    rules(s, [
      'Mitanin only: child sick, developmental delay, diarrhoea or breathing difficulty → dialog → referral sheet (HBYCFormFragment.kt:172; VM:84-90)',
      'Referral reason "Suspected illness in young child", type CHILD',
    ]);
  }
  // ---- Row 4
  // 19 CUFY card buttons & SAM status (NEW)
  {
    const s = F('M2.4-19 · Children under 5 · SAM status buttons', 0, 3);
    screenTop(s, 'Children under 5', '96 children');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search child or mother');
    for (const [n, m, st] of [['Golu Kumar', 'Boy · 2 yrs 4 m · mother Rinki', 'Check SAM'], ['Anjali Kumari', 'Girl · 1 yr 8 m · mother Puja', 'Referred to NRC'], ['Sonu Yadav', 'Boy · 3 yrs · mother Lalita', 'NRC admitted'], ['Khushi Devi', 'Girl · 2 yrs · mother Mamta', 'Follow-up SAM']]) {
      await actionCard(b, { name: n, meta: m, actions: [['Outlined', st], ['Outlined', 'ORS'], ['Outlined', 'IFA syrup']] });
    }
    rules(s, [
      'SAM button label = live SAM status: Check SAM → Referred to NRC → NRC admitted → Follow-up SAM → back to Check SAM when discharged Improved (CUFYFormRepository.kt:339-375; CUFYListViewModel.kt:80-111)',
      'Each button opens its own visit sheet (M2.4-20 / 21 / 22)',
    ]);
  }
  // 20 SAM visits sheet (NEW)
  {
    const s = F('M2.4-20 · SAM visits sheet', 1, 3, true);
    screenTop(s, 'Children under 5', '96 children');
    await visitSheet(s, { title: 'SAM visits', subtitle: 'Anjali Kumari · 1 yr 8 m · Referred to NRC', visits: [{ label: 'Visit 1', date: '04 Sep 2026', action: 'View' }, { label: 'Visit 2', date: '06 Oct 2026', action: 'View' }], footnote: '"Add new visit" hidden: child is in the NRC pathway.' });
    rules(s, ['Rows "Visit 1…n" with VIEW + "Add new visit" row with ADD (CUFYBottomSheetFragment.kt:163-191)', 'SAM: "Add new visit" hidden once status is not "Check SAM" (Sheet:175-188)']);
  }
  // 21 IFA visits sheet · non-Mitanin (NEW)
  {
    const s = F('M2.4-21 · IFA visits sheet', 2, 3, true);
    screenTop(s, 'Children under 5', '96 children');
    const sh = await visitSheet(s, { title: 'IFA syrup visits', subtitle: 'Golu Kumar · 2 yrs 4 m', visits: [{ label: 'Latest visit', date: '05 Oct 2026 · 1 bottle', action: 'View' }], footnote: '"Add new visit" row appears only when there is no visit yet.' });
    const r = sh.findOne((n) => n.name === 'Visit · Latest visit'); if (r) btn(r, 'Filled', 'Add');
    pinBottom(s, sh);
    rules(s, ['Non-Mitanin: only the latest visit, with both Add and View on that row (Sheet:129-161)', 'No visit yet → single "Add new visit" row']);
  }
  // 22 IFA visits sheet · Mitanin (NEW)
  {
    const s = F('M2.4-22 · IFA visits sheet (Mitanin)', 3, 3, true);
    screenTop(s, 'Children under 5', '96 children');
    await visitSheet(s, { title: 'IFA syrup visits', subtitle: 'Golu Kumar · Mitanin', visits: [{ label: 'IFA Visit 1', date: '03 Aug 2026', action: 'View' }, { label: 'IFA Visit 2', date: '04 Sep 2026', action: 'View' }, { label: 'IFA Visit 3', date: '05 Oct 2026', action: 'View' }], add: { label: 'Add new visit' } });
    rules(s, ['Mitanin: rows "IFA Visit 1, 2…" view-only, plus "Add new visit" (Sheet:101-128)']);
  }
  // 23 SAM record view mode (NEW)
  {
    const s = F('M2.4-23 · SAM record · view & edit', 4, 3);
    screenTop(s, 'SAM check', 'Anjali Kumari · 1 yr 8 m', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Saved 04 Sep 2026', editable: true, reason: 'Edit to update NRC status' });
    auto(b, 'Visit date (locked when editing)', '04 Sep 2026');
    await muac(b, 'Red');
    select(b, 'NRC status', 'NRC admitted');
    await table(b, 'SAM follow-up visits', ['Visit', 'Date'], [['Follow-up 1', '20 Sep 2026'], ['Follow-up 2', '06 Oct 2026']]);
    rules(s, [
      'Saved visits open read-only; Edit FAB only on SAM records (ORS / IFA no edit) (CUFYFormFragment.kt:176-187, 318-322)',
      'Visit date locked when editing (:382-388)',
      'SAM follow-up dates list on saved SAM records (:423-430, 746-770; VM:325-340)',
    ]);
  }
  // 24 IFA syrup form (NEW)
  {
    const s = F('M2.4-24 · IFA syrup form', 5, 3);
    screenTop(s, 'IFA syrup', 'Golu Kumar · 2 yrs 4 m', 'more');
    const b = body(s); b.itemSpacing = 12;
    await table(b, 'IFA history', ['Visit', 'Date', 'Bottles'], [['1', '03 Aug 2026', '1'], ['2', '04 Sep 2026', '1'], ['3', '05 Oct 2026', '1']]);
    date(b, 'Date of IFA visit *', '02 Nov 2026');
    await caption(b, 'Allowed: after 05 Oct 2026, not in future, not before birth, max 10 months back');
    steppers(b, [['Bottles given', '1']]);
    banner(b, 'Error', 'IFA already given this month', 'Only one IFA visit per calendar month.', null);
    bottomBar(s, null, 'Submit');
    rules(s, [
      'IFA visit / bottle history table above the form; Mitanin shows visit numbers, bottle count hidden (Fragment:261-287; VM:71, 394-395)',
      'Date window: ≤ 10 months old, after previous IFA visit, not future, not before birth (Fragment:295-316, 486-512)',
      'One IFA visit per calendar month: field error + toast (Fragment:600-632)',
    ]);
  }
  // ---- Row 5
  // 25 SAM detected dialog (NEW)
  {
    const s = F('M2.4-25 · SAM case detected dialog', 0, 4, true);
    screenTop(s, 'SAM check', 'Golu Kumar · 2 yrs 4 m', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Date *', '02 Nov 2026');
    await muac(b, 'Red');
    await dialog(s, { title: 'SAM case detected', body: 'MUAC is in the red zone. Refer Golu to the NRC.', cancel: null, ok: 'OK', danger: true });
    rules(s, ['MUAC or weight-for-height in SAM range → blocking alert dialog (Fragment:358-375; HelperUtil.kt:974-999)', 'Replaces the in-page red banner idea in M2.4-09']);
  }
  // 26 Adolescent view mode (NEW)
  {
    const s = F('M2.4-26 · Adolescent health · view mode', 1, 4);
    screenTop(s, 'Adolescent health', 'Pooja Kumari · 15 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Saved 12 Oct 2026', editable: true });
    await infoCard(b, 'Record', [['Visit date', '12 Oct 2026'], ['Health status', 'Healthy'], ['IFA tablets', '4'], ['Sanitary napkins', '2 packets · Anganwadi'], ['Counselling', 'Nutrition & anaemia'], ['Follow-up date', '10 Dec 2026']]);
    rules(s, ['One record per adolescent; saved record opens read-only, Edit FAB unlocks and saves in place (AdolescentFormFragment.kt:40-73; AdolescentHealthViewModel.kt:69-79)']);
  }
  // 27 Infant registration list (NEW)
  {
    const s = F('M2.4-27 · Infant registration list', 2, 4);
    screenTop(s, 'Infant registration', '6 babies');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search mother or RCH ID', { sort: true });
    await sortLabel(b);
    await actionCard(b, { name: '1st baby of Sunita Devi', initials: 'B1', meta: 'Delivered 30 Oct 2026 · twins', actions: [['Filled', 'Register']] });
    await actionCard(b, { name: '2nd baby of Sunita Devi', initials: 'B2', meta: 'Delivered 30 Oct 2026 · twins', sync: 'Synced', actions: [['Tonal', 'View']] });
    await actionCard(b, { name: 'Baby of Geeta Devi', initials: 'BG', meta: 'Delivered 21 Oct 2026', sync: 'Pending', actions: [['Tonal', 'View']] });
    rules(s, [
      'One card per baby; twins / triplets shown as "1st / 2nd baby of <mother>" (InfantReg.kt:170-180)',
      'Red "Register" when no record, green "View" after (InfantRegistrationAdapter)',
      '"Low birth weight babies" from Today / scheduler opens THIS list filtered (InfantRegListViewModel.kt:21-24; SchedulerFragment.kt:119), not M2.4-02',
      'Search with voice + sort sheet (as M2.3-10)',
    ]);
  }
  // 28 Infant registration form · per baby + SNCU uploads (NEW)
  {
    const s = F('M2.4-28 · Infant registration · twins & SNCU', 3, 4);
    screenTop(s, 'Infant registration', '2nd baby of Sunita Devi', 'more');
    const b = body(s); b.itemSpacing = 12;
    tag(b, 'Child', 'Baby 2 of 2');
    text(b, 'Baby name', 'Baby of Sunita'); await seg(b, 'Sex *', ['Girl', 'Boy', 'Other'], 0);
    steppers(b, [['Weight at birth (g) *', '2,100']]);
    await seg(b, 'Admitted to SNCU?', ['Yes', 'No'], 0);
    const rv = reveal(b); await label(rv, 'SNCU discharge summary (4 photos)'); photos(rv, 4, 2);
    bottomBar(s, null, 'Submit');
    rules(s, [
      'Registration is per baby index; twins / triplets each get their own form (InfantRegViewModel.kt:14-15, 63-69)',
      'SNCU = Yes → 4 discharge-summary photo uploads, non-Mitanin only (InfantRegistrationDataset.kt:320-330, 372-382)',
      'Mitanin: SNCU = Yes adds no uploads; upload taps ignored (InfantRegistrationDataset.kt:325, 376; InfantRegFragment.kt:97)',
      'View-only after save, Submit disabled, no edit (InfantRegFragment.kt:120)',
      'Upload chooser camera / gallery; size-limit toast; tap to view',
    ]);
  }
  // 29 Child registration list (NEW)
  {
    const s = F('M2.4-29 · Child registration list', 4, 4);
    screenTop(s, 'Child registration', '9 pending');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search mother or RCH ID', { sort: true });
    await sortLabel(b);
    await actionCard(b, { name: 'Baby of Sunita', initials: 'BS', meta: 'Girl · born 30 Oct 2026 · mother Sunita Devi', actions: [['Filled', 'Register']] });
    await actionCard(b, { name: 'Aarav Kumar', meta: 'Boy · born 12 Feb 2026 · mother Rekha Devi', sync: 'Synced', actions: [['Tonal', 'View']] });
    rules(s, [
      'Registered infants due for child registration; red Register / green View (ChildRegistrationAdapter)',
      'Register → M2.4-12. "View" opens the full member (beneficiary) registration form of the child, not the child-reg form (ChildRegListFragment.kt:68-75)',
      'Search with voice + sort sheet',
    ]);
  }

  // 30 IFA syrup form · Mitanin (NEW, Mitanin addendum)
  {
    const s = F('M2.4-30 · IFA syrup form · Mitanin', 5, 4);
    screenTop(s, 'IFA syrup', 'Golu Kumar · 2 yrs 4 m', 'more');
    const b = body(s); b.itemSpacing = 12;
    await table(b, 'IFA visits', ['IFA visit', 'Date of provision'], [['IFA Visit 1', '03 Aug 2026'], ['IFA Visit 2', '04 Sep 2026'], ['IFA Visit 3', '05 Oct 2026']]);
    date(b, 'Date of IFA visit *', '02 Nov 2026');
    await caption(b, 'No bottle count field on Mitanin.');
    bottomBar(s, null, 'Submit');
    rules(s, [
      'Mitanin (FLW-1129): history panel = "IFA Visit N | Date of provision", no S.No / bottle column (CUFYFormFragment.kt:264-290)',
      'Mitanin: bottle-count field hidden from UI but still sent to server (CUFYFormViewModel.kt:389-400)',
      'Visit sheet rows numbered by visit date (CUFYFormRepository.kt:126) → M2.4-22',
      'Same date window and one-per-month rules as M2.4-24',
    ]);
  }

  const endY = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, endY, 860,
    'CHILD CARE NOTES\n' +
    '• Newborns: list (02) → 7-visit HBNC schedule sheet (03) → HBNC form (04). Danger sign (Mitanin) → dialog (16) → refer sheet (05). Baby dead → CDR (17), visits closed (14).\n' +
    '• HBNC questions are rewritten as short yes/no (the dataset text has long instructions; keep those as help text under each question).\n' +
    '• HBYC: list (06) → month schedule (15) → form (07). MCP-card colour is a big colour picker. Mitanin danger → refer (18).\n' +
    '• Children under 5: 3 buttons per card (19) → SAM / IFA visit sheets (20–22) → server-driven CUFY forms (09, 23, 24, 25).\n' +
    '• Infant & child registration lists (27, 29) live under Mother care in code.\n' +
    '• Gamification: LBW baby with all 7 HBNC visits → "Vulnerable Baby Cared For"; SAM → NRC referral → "Critical Referral Made"; full birth–12 m doses → "Child Fully Protected" (stage 2.5).');
  fitSection(sec);
  return ['2.4 screens: ' + frames.length];
}
