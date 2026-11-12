// ---------------------------------------------------------------------------
// Stage 2.9 · Death reports + Village level forms / campaigns
// Sources: ui/home_activity/death_reports/*, village_level_forms/*, IconDataset.getDeathReportDataset / getVLFDataset,
// CDR, MDSR, VHND, MaaMeeting, UWIN, SurveyRegister, AHD, Deworming, PHCReview, SaasBahuSamelan datasets
// 2026-11-05 gap pass (gap_F_village_death): M2.9-13…44 added, 01–12 edited in place; grid is 6 columns.
// ---------------------------------------------------------------------------
async function buildStage29(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureSearchMic(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack } = S;
  const { rules, caption, tag, btn, row, dialog, searchRow, viewMode, blockedAdd, uploads, actionCard } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.9 · Death reports & village forms');
  const frames = []; const rowMap = new Map();
  // Grid: frames are placed in creation order, max 6 per row (col/row args kept for readability only)
  const F = (name, _col, _row, fixed) => { const i = frames.length; const col = i % 6, r = Math.floor(i / 6); const s = phone(sec, name, col, r, fixed); frames.push(s); rowMap.set(s, r); return s; };
  const photos = (p, states) => { const r = AL('HORIZONTAL', 'Photos'); gap(r, 'spacing/sm'); add(p, r); for (const st of states) r.appendChild(kinst('Photo tile', `State=${st}`)); return r; };
  const grid2 = (p, items) => { const g = AL('HORIZONTAL', 'Counts'); g.layoutWrap = 'WRAP'; g.itemSpacing = 16; g.counterAxisSpacing = 12; add(p, g); for (const [l, v] of items) g.appendChild(kinst('Stepper', null, { Label: l, Value: v })); return g; };

  // ---- local helpers
  // red (not filled) / green (filled) form button used on death list cards (BenListAdapterForForm)
  const statePill = async (p, l, filled) => {
    const b = AL('HORIZONTAL', 'Form button · ' + (filled ? 'filled' : 'not filled')); b.counterAxisAlignItems = 'CENTER'; b.itemSpacing = 6; b.paddingLeft = b.paddingRight = 16; b.paddingTop = b.paddingBottom = 8; b.cornerRadius = 20;
    fill(b, filled ? 'color/success' : 'color/error'); p.appendChild(b);
    b.appendChild(icon(filled ? 'check' : 'form', 'color/on-primary', 18));
    b.appendChild(await txt(l, 'Label/Large', 'color/on-primary'));
    return b;
  };
  const syncB = (p, st) => { try { const x = inst('syncBadge', `Status=${st}`); p.appendChild(x); return x; } catch (e) { return null; } };
  const valuesGrid = async (p, rows) => {
    const g = AL('HORIZONTAL', 'Values'); g.layoutWrap = 'WRAP'; g.itemSpacing = 8; g.counterAxisSpacing = 6; add(p, g);
    for (const [l, v] of rows) { const cell = AL('VERTICAL', 'Value'); cell.itemSpacing = 0; g.appendChild(cell); S.fixW(cell, 140); const a = await txt(l, 'Label/Small', 'color/on-surface-variant'); cell.appendChild(a); wrapText(a); const b = await txt(v, 'Body/Medium', 'color/on-surface'); cell.appendChild(b); wrapText(b); }
    return g;
  };
  // death list card (rv_item_ben_with_form): fields + optional sync icon + optional red/green form button
  const deathCard = async (p, d) => {
    const c = AL('VERTICAL', 'Card · ' + d.name); pad(c, 'spacing/md', 'spacing/sm'); c.itemSpacing = 8; rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(p, c);
    const top = row(c, 'Header', 12);
    top.appendChild(inst('avatar', null, { Initials: d.name.split(' ').map((w) => w[0]).join('').slice(0, 2) }));
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 2; top.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    col.appendChild(await txt(d.name, 'Title/Medium', 'color/on-surface'));
    const m = await txt(d.meta, 'Body/Small', 'color/on-surface-variant'); col.appendChild(m); wrapText(m);
    if (d.sync) syncB(top, d.sync);
    await valuesGrid(c, d.rows);
    if (d.form) { const r = row(c, 'Form action', 8); r.primaryAxisAlignItems = 'MAX'; await statePill(r, d.form, !!d.filled); }
    return c;
  };
  const deathRows = (o) => [['Date of death', o.dod], ['Age', o.age], ['Gender', o.gender], ['Ben ID', o.ben], ['Reg. date', o.reg], ['Mobile', o.mob], ["Father's name", o.father], ['RCH ID', o.rch], ['Family head', o.head]];
  // meeting / campaign list card (layout_vhnd_item etc.): title + values + optional sync + View
  const meetCard = async (p, d) => {
    const c = AL('VERTICAL', 'Card · ' + d.title); pad(c, 'spacing/md', 'spacing/sm'); c.itemSpacing = 8; rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(p, c);
    const top = row(c, 'Header', 12);
    const ib = box('Icon', 40, 40); ib.cornerRadius = 20; fill(ib, `module/${d.tone || 'village'}/bg`); ib.appendChild(icon(d.ic || 'calendar', `module/${d.tone || 'village'}/fg`, 20)); top.appendChild(ib);
    const t = await txt(d.title, 'Title/Small', 'color/on-surface'); top.appendChild(t); t.layoutSizingHorizontal = 'FILL';
    if (d.sync) syncB(top, d.sync);
    await valuesGrid(c, d.rows);
    const r = row(c, 'Actions', 8); r.primaryAxisAlignItems = 'MAX'; btn(r, 'Outlined', d.action || 'View');
    return c;
  };
  const addBtn = async (p, l, blocked) => { if (blocked) return blockedAdd(p, l, blocked); return btn(p, 'Filled', l, false, true); };
  const emptyState = (p, title, bodyText, ic = 'form') => {
    const e = kinst('Empty state', null, { Title: title, Body: bodyText, Icon: ic }); add(p, e, false);
    for (const n of e.children) if (n.type === 'INSTANCE') n.visible = false; // no CTA in app
    p.counterAxisAlignItems = 'CENTER'; return e;
  };
  // upload slots (fixed named slots: CDR/MDSR/U-WIN). slots = [[slotName, fileName|null]]
  const slots = async (p, title, list, rulesText) => {
    const c = AL('VERTICAL', 'Uploads · ' + title); c.itemSpacing = 8; add(p, c);
    await label(c, title);
    for (const [sl, fileName] of list) {
      const st = await txt(sl, 'Label/Medium', 'color/on-surface-variant'); c.appendChild(st);
      if (fileName) { add(c, kinst('File row', 'State=Done', { 'File name': fileName })); continue; }
      const r = AL('HORIZONTAL', 'Empty slot · ' + sl); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 10; pad(r, 'spacing/md', 'spacing/sm'); rad(r, 'radius/md'); stroke(r, 'color/outline'); r.dashPattern = [6, 4]; add(c, r);
      r.appendChild(icon('cloud_upload', 'color/primary', 22)); r.appendChild(await txt('Tap to add', 'Label/Large', 'color/primary'));
    }
    if (rulesText) await caption(c, rulesText);
    return c;
  };
  const flavourTag = (p, l = 'Mitanin only') => { const r = row(p, 'Flavour gate', 6); tag(r, 'Alert', l); return r; };
  const choiceList = (opts) => async (c) => { for (const [ic, l] of opts) { const r = row(c, 'Option · ' + l, 16); r.paddingTop = r.paddingBottom = 10; r.appendChild(icon(ic, 'color/primary', 24)); r.appendChild(await txt(l, 'Body/Large', 'color/on-surface')); } };
  const overdue = (grid, titles) => { for (const m of grid.children) { const tl = m.findOne((n) => n.type === 'TEXT' && titles.includes(n.characters)); if (tl) { m.strokes = [paint('color/error')]; m.strokeWeight = 2; m.strokeAlign = 'INSIDE'; } } };

  // ======================= DEATH REPORTS =======================
  // 01 Death reports hub
  {
    const s = F('M2.9-01 · Death reports hub', 0, 0);
    screenTop(s, 'Death reports', 'Village: Sirsi');
    const b = body(s);
    banner(b, 'Info', 'Counts are for the selected village', 'Each list shows deceased beneficiaries of the village picked on the home screen.', null);
    tileGrid(b, [['child', 'Child deaths (CDR)', '2', 'child'], ['maternal', 'Maternal deaths (MDSR)', '1', 'pregnant'], ['records', 'General deaths', '5', 'person'], ['records', 'Non-maternal deaths', '1', 'form']]);
    rules(s, ['Tile count = plain total of records in the selected village (no pending / year split) [IconDataset.kt:632-652; BenDao.kt:828-1009]',
      'CDR list: dead and age ≤ 14 yrs [BenDao.kt:819; Konstants.maxAgeForCdr]',
      'MDSR list: dead, isMdsr, status PW / PNC / abortion AND maternal-death flag in ANC, delivery outcome or PNC [MDSR.kt:171-175]',
      'General deaths: dead, age ≥ 15, not a maternal death [BenDao.kt:997]',
      'Non-maternal deaths: dead female 15–49, not a maternal death [BenDao.kt:833-844]',
      'Report "within 24 hours" subtitle: not in app today – needs dev']);
  }
  // 02 Maternal death list
  {
    const s = F('M2.9-02 · Maternal deaths list', 1, 0);
    screenTop(s, 'Maternal deaths (MDSR)', 'Sirsi · 2 records');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'Pending', 'Count 1': '1', 'Label 2': 'Submitted', 'Count 2': '1', 'Label 3': 'All', 'Count 3': '2' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await deathCard(b, { name: 'Rekha Yadav', meta: 'W/o Mohan Yadav · 26 yrs', sync: 'Pending', form: 'MDSR', filled: false, rows: deathRows({ dod: '30 Oct 2026', age: '26 yrs', gender: 'Female', ben: '1203 4456 7781', reg: '14 Mar 2026', mob: '98XXXXXX21', father: 'Ram Lal', rch: '1220 0045 3321', head: 'Mohan Yadav' }) });
    await deathCard(b, { name: 'Meera Devi', meta: 'W/o Suresh · 31 yrs', sync: 'Synced', form: 'MDSR', filled: true, rows: deathRows({ dod: '12 Jun 2026', age: '31 yrs', gender: 'Female', ben: '1203 4412 0091', reg: '02 Jan 2026', mob: '97XXXXXX40', father: 'Shiv Prasad', rch: '1220 0045 1187', head: 'Suresh Kumar' }) });
    rules(s, ['MDSR button: red = form not filled, green = filled; tap opens the MDSR form [BenListAdapterForForm.kt:141-166]',
      'Sync icon shows the report sync state',
      'Search by name / ID with voice mic [MdsrListFragment]',
      'ASHA Supervisor role: form buttons hidden (see M2.9-17) [BenListAdapterForForm.kt:59-67]',
      'Pending / Submitted / All tabs: not in app today – needs dev (app shows one list with colour state)',
      'No records → "No records found" (M2.9-16)']);
  }
  // 03 CDR form
  {
    const s = F('M2.9-03 · Child death report (CDR)', 2, 0);
    screenTop(s, 'Child death report', 'Baby of Pinki · boy', 'more');
    const b = body(s); b.itemSpacing = 12;
    await infoCard(b, 'Child (auto)', [['Name', 'Baby of Pinki'], ['Date of birth', '24 Oct 2026'], ['Age', '9 days'], ['Gender', 'Male'], ["Mother's name", 'Pinki Devi'], ["Father's name", 'Sonu Kumar'], ['Mobile', '98XXXXXX63'], ['Address', 'H.No 12, Mohalla Purani Basti, Sirsi'], ['Village', 'Sirsi · Block Jagdalpur']]);
    section(b, 'Closed', 'Address', '6 fields · auto', 2);
    section(b, 'Open', 'Death details', '6 fields', 3);
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    const d1 = date(r, 'Date of death *', '02 Nov 2026'); d1.layoutSizingHorizontal = 'FILL';
    const d2 = S.tf(r, 'Filled', { Label: 'Time', Value: '06:10 AM' }, 'clock'); d2.layoutSizingHorizontal = 'FILL';
    auto(b, 'Place of death (from record)', 'Home');
    auto(b, 'First informant (ASHA)', 'Sunita Kumari');
    date(b, 'Date of notification *', '02 Nov 2026');
    await slots(b, 'Documents', [['CDR form · page 1', 'CDR_form_p1.jpg'], ['CDR form · page 2', null], ['Death certificate', null]], 'Camera, gallery or PDF · max 5 MB each · tap a file to preview');
    bottomBar(s, 'Save draft', 'Submit report');
    rules(s, ['Auto-filled from beneficiary + ASHA: name, DOB, age, gender, parents, address (cut to 50 chars), house no, mohalla, state/district/block/village, mobile, date & place of death [CDRFormDataset.kt:255-273]',
      'First informant = logged-in ASHA username (not the father) [CDRFormDataset.kt:255-273]',
      'Date of notification: between date of death and today [CDRFormDataset.kt:243-253]',
      '3 upload slots, each opens chooser Camera / Gallery / PDF / Cancel (M2.9-19) [CdrObjectFragment.kt:308-330]',
      'File > 5 MB rejected with toast; tap file = preview (image dialog, PDF external) (M2.9-20) [CdrObjectFragment.kt:237-243, 379-408]',
      'Mitanin flavour: all 3 upload slots removed [CDRFormDataset.kt:236-240]',
      'One-time form: after save reopens read-only (M2.9-18) [CdrObjectFragment.kt:150-154]',
      'Submit → spinner → go back + start sync; failure toast "Saving … failed" [CdrObjectFragment.kt:167-199]',
      'Save draft: not in app today – needs dev']);
  }
  // 04 MDSR form
  {
    const s = F('M2.9-04 · Maternal death report (MDSR)', 3, 0);
    screenTop(s, 'Maternal death report', 'Rekha Yadav · W/o Mohan', 'more');
    const b = body(s); b.itemSpacing = 12;
    await infoCard(b, 'From beneficiary (auto)', [['Husband', 'Mohan Yadav'], ["Father's name", 'Ram Lal'], ['Age', '26 yrs'], ['RCH ID', '1220 0045 3321'], ['Date & time of death', '30 Oct 2026 · 04:20 AM'], ['Place of death', 'CHC Bakawand']]);
    await label(b, 'When did death occur? (auto · locked)');
    auto(b, 'During pregnancy', 'Yes');
    auto(b, 'During delivery', 'No');
    auto(b, 'Within 42 days after delivery', 'No');
    auto(b, 'During / after abortion', 'No');
    await caption(b, 'Taken from ANC, delivery outcome, PNC and abortion records.');
    auto(b, 'Cause of death (auto)', 'Suspected maternal death');
    const rv = reveal(b); text(rv, 'Specify reasons *', 'Fits at 34 weeks, BP very high');
    section(b, 'Open', 'Reporting', '4 fields', 2);
    auto(b, 'Name of reporting person', 'Sunita Kumari'); auto(b, 'Designation', 'Asha');
    section(b, 'Closed', 'ANM verification', 'Name, sub-centre', 3);
    section(b, 'Closed', 'Field investigation (MO)', 'Filled by MO I/C', 4);
    await slots(b, 'Documents', [['MDSR form 1', 'MDSR_form_ANM_1.jpg'], ['MDSR form 2', null], ['Death certificate', 'death_certificate.pdf']], 'Camera, gallery or PDF · max 5 MB each · tap a file to preview');
    bottomBar(s, 'Save draft', 'Submit report');
    rules(s, ['"When did death occur": 4 Yes/No answers auto-filled and locked from ANC maternal death, delivery outcome = DEATH, PNC mother death, abortion [MdsrObjectViewModel.kt:78-100; MDSRFormDataset.kt:96-127]',
      'Cause of death auto: "Suspected maternal death" or "Non-maternal death"; non-maternal if ANC/PNC cause = Accident [MDSRFormDataset.kt:334-342]',
      'Suspected maternal → shows "Specify reasons" [MDSRFormDataset.kt:362-369]',
      'Reporter = ASHA username, designation fixed "Asha"; husband, RCH ID, age, father, date/time/place of death from beneficiary [MDSRFormDataset.kt:312-328]',
      'Uploads: MDSR form 1, MDSR form 2, death certificate; chooser Camera / Gallery / PDF / Cancel; max 5 MB; preview on tap [MdsrObjectFragment.kt:181-200]',
      'Mitanin flavour: upload slots removed [MDSRFormDataset.kt:306-310]',
      'One-time form: read-only after save, uploads + Submit disabled [MdsrObjectFragment.kt:96-118]',
      'Submit → spinner → back + sync; failure toast [MdsrObjectFragment.kt:128-152]',
      'Save draft: not in app today – needs dev']);
  }
  // 05 VLF hub
  {
    const s = F('M2.9-05 · Village level forms hub', 4, 0);
    screenTop(s, 'Village meetings & campaigns', 'November 2026 · 9 Nov');
    const b = body(s);
    const bp = add(b, kinst('Badge progress row', null, { Name: 'Community Voice', Detail: '1 of 2 meeting types this quarter' }));
    const bm = bp.findOne((n) => n.type === 'INSTANCE' && n.name === 'Badge medal'); if (bm) setP(bm, { Badge: 'Community Voice' });
    const bpb = bp.findOne((n) => n.type === 'INSTANCE' && n.name === 'Progress bar'); if (bpb) setP(bpb, { Value: '50' });
    add(b, inst('sectionHeader', null, { Title: 'Meetings', 'Show action': false }));
    const g1 = tileGrid(b, [['village', 'VHND / VHSND', 'Overdue', 'calendar'], ['village', 'VHSNC', 'Done Nov', 'group'], ['maternal', 'Maa meeting', 'Every 3 months', 'favorite'], ['village', 'Saas Bahu Sammelan', 'Monthly', 'group'], ['village', 'PHC review meeting', 'Overdue', 'hospital'], ['village', 'Cluster meetings', '—', 'group'], ['records', 'Survey register', 'Updated Sep', 'form']]);
    overdue(g1, ['VHND / VHSND', 'PHC review meeting']);
    add(b, inst('sectionHeader', null, { Title: 'Campaigns & days', 'Show action': false }));
    const g2 = tileGrid(b, [['immunization', 'U-WIN session', '2 this month', 'vaccine'], ['immunization', 'Pulse Polio', '2 per year', 'vaccine'], ['care', 'ORS distribution', 'Overdue', 'hospital'], ['couple', 'Filaria MDA', 'Once a year', 'shield'], ['child', 'Deworming day (NDD)', 'Every 6 months', 'child'], ['village', 'AHD', 'Overdue', 'calendar']]);
    overdue(g2, ['ORS distribution', 'AHD']);
    const lg = row(b, 'Legend', 8); const sw = box('Swatch', 20, 14); sw.cornerRadius = 4; sw.strokes = [paint('color/error')]; sw.strokeWeight = 2; lg.appendChild(sw);
    await caption(lg, 'Red border = overdue: after the 7th and not submitted this month');
    rules(s, ['Overdue red border after the 7th of the month if not submitted this month: VHND, VHNC, PHC, AHD, NDD, U-WIN, Pulse Polio, ORS [VillageLevelFormsViewModel.kt:31-70]',
      'code bug: overdue match uses English tile titles, so the border probably never shows in Hindi / Assamese [VillageLevelFormsFragment.kt:72-88]',
      'Mitanin: Saas Bahu Sammelan and U-WIN tiles hidden; PHC review tile renamed "Cluster Meeting Forms" (M2.9-21) [IconDataset.kt:322-421]',
      '"Cluster meetings" is the same tile as PHC review (flavour name), not a separate one – design extra',
      'Survey register tile: not in app today – needs dev (code screen is a placeholder)',
      'Filaria MDA tile opens the village-level campaign history (M2.9-43), not the per-person Filaria form [IconDataset.kt:401-407]']);
  }
  // 06 VHND form (Mitanin version)
  {
    const s = F('M2.9-06 · VHND form', 5, 0);
    screenTop(s, 'VHND', 'Sirsi village · November', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'VHND date *', '05 Nov 2026'); select(b, 'Place of session *', 'Anganwadi centre');
    flavourTag(b);
    grid2(b, [['Total attended', '31'], ['Pregnant (ANC)', '8'], ['Lactating (PNC)', '6'], ['Children (vaccines)', '14']]);
    await checks(b, 'Health & nutrition education given', [['Balanced diet', true], ['Care during pregnancy and delivery', true], ['Importance of breastfeeding', true], ['Complementary feeding', false], ['Hygiene and sanitation', false], ['Family planning', true]]);
    await label(b, 'Photos (2)'); photos(b, ['Captured', 'Empty']);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Mitanin: this frame is the Mitanin version. Mitanin only: PW / lactating / children counts + 6 nutrition-education checklists (saved / reloaded only on Mitanin) [VHNDDataset.kt:233, 325]. Other flavours: date, place, total attended, 2 photos (M2.9-23) [VHNDDataset.kt:194-222]',
      'Date: last 60 days up to today, defaults to today [VHNDDataset.kt:39-40, 225]',
      'Photo tap → chooser Take photo / Choose from gallery (M2.9-24); > 5 MB rejected with toast; tap photo to preview; "No image found" toast [VHNDFormFragement.kt:82-93, 165-201]',
      'Submit → toast success / failure, then back [VHNDFormFragement.kt:119-141]',
      'Saved record opens read-only, Submit disabled [VHNDFormFragement.kt:65-97]',
      'Save draft: not in app today – needs dev']);
  }
  // 07 Maa meeting (Mitanin version)
  {
    const s = F('M2.9-07 · Maa meeting', 6, 0);
    screenTop(s, 'Maa meeting', 'Breastfeeding support', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Meeting date *', '12 Nov 2026'); select(b, 'Place *', 'Panchayat bhawan');
    flavourTag(b);
    auto(b, 'Village', 'Sirsi');
    grid2(b, [['Pregnant women', '7'], ['Lactating mothers', '9']]);
    await checks(b, 'During breastfeeding counselling check list', [['Select all', false], ['Breastfeed within the first hour of birth', true], ['Only mother\u2019s milk for the first 6 months', true], ['After 6 months, add complementary food', true], ['Continue breastfeeding for 2 years or more', false], ['Family should support the mother', true], ['No honey, water or ghutti in first 6 months', true]]);
    auto(b, 'Total attended', '16');
    await uploads(b, 'Photos / minutes of meeting (up to 5)', [['maa_meeting_photo1.jpg', 'Done'], ['MoM_12Nov.pdf', 'Done']], 'JPEG, PNG or PDF only · max 5 MB each · tap to open');
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Mitanin only: village, PW count, lactating count, breastfeeding checklist. Other flavours: date, place, participants [MaaMeetingDataset.kt:112-120]',
      'Up to 5 uploads (photo or MoM), JPEG / PNG / PDF only → else toast "Only JPEG, PNG, PDF allowed"; 5 MB limit; opens in external viewer [MaaMeetingFormFragment.kt:160-195]',
      'View mode: only filled upload slots shown [MaaMeetingDataset.kt:124-130]',
      'Missing required fields → "Alert" dialog (M2.9-33) [MaaMeetingFormFragment.kt:145-155]',
      'Mitanin: this frame is the Mitanin version; checklist = maa_breastfeeding array (8 items + Select all) [MaaMeetingDataset.kt:77-79, 112-120]',
      'Other flavours: date, place, participants + uploads only. "Min 2 uploads for non-Mitanin" check is commented out – no rule today [MaaMeetingFormFragment.kt:134]',
      'Submit hidden for a saved record [MaaMeetingFormFragment.kt:58-62]',
      'Save draft: not in app today – needs dev']);
  }
  // 08 Pulse polio campaign
  {
    const s = F('M2.9-08 · Pulse Polio campaign', 7, 0);
    screenTop(s, 'Pulse Polio', 'Round · 12 Dec 2026', 'more');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Server-driven form', 'Fields, show/hide and checks come from the downloaded form schema. Fields below are an example.', null);
    await radios(b, 'Day', ['Booth day', 'House-to-house day 1', 'House-to-house day 2'], 0);
    date(b, 'Date *', '12 Dec 2026'); select(b, 'Booth / area *', 'Booth 3 · school');
    grid2(b, [['Children 0–5 given drops', '86'], ['Houses missed', '4']]);
    await label(b, 'Campaign photos (last 2 kept)'); photos(b, ['Captured', 'Captured']);
    await caption(b, 'Camera or file · max 5 MB (from schema). A 3rd photo replaces the oldest.');
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Server-driven: schema downloaded, cached schema used first; fields / rules / validation from schema [PulsePolioCampaignViewModel.kt:60-130]',
      'Photos: keeps only last 2 (3rd replaces oldest); camera or file; size limit from schema (default 5 MB) → inline error [PulsePolioCampaignFormFragment.kt:64-142]',
      'Missing fields → toast "Please fill all required fields" + scroll to first error (M2.9-39) [PulsePolioCampaignFormFragment.kt:269-290]',
      'Saved → view-only, Save hidden (M2.9-40) [PulsePolioCampaignFormFragment.kt:175]',
      'After save a sync worker starts [PulsePolioCampaignPushWorker.enqueue]',
      'Save draft: not in app today – needs dev']);
  }
  // 09 Deworming day (NDD)
  {
    const s = F('M2.9-09 · Deworming day (NDD)', 0, 1);
    screenTop(s, 'National Deworming Day', 'Round · Feb 2027', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Was the deworming round conducted? *', ['Yes', 'No'], 0);
    const rv = reveal(b); date(rv, 'Date of NDD *', '10 Feb 2027'); select(rv, 'Place *', 'Government school');
    steppers(b, [['Out-of-school children sent to AWC', '6']]);
    await label(b, 'Photos (max 2)'); photos(b, ['Captured', 'Empty']);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['NDD date: last 1 month up to today [DewormingDataset.kt:25-33, 51-52]',
      'Max 2 photos; camera / gallery chooser (M2.9-24); 5 MB limit [DewormingFormFragment.kt:141-184]',
      'Saved record opens view-only [DewormingFormFragment.kt:80-88]',
      'Save draft: not in app today – needs dev']);
  }
  // 10 U-WIN session form
  {
    const s = F('M2.9-10 · Simple meeting (U-WIN / Saas Bahu)', 1, 1);
    screenTop(s, 'U-WIN session', 'Sirsi · November', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Session date *', '05 Nov 2026'); select(b, 'Place *', 'Sub-centre'); steppers(b, [['Participants attended', '22']]);
    banner(b, 'Info', 'Saas Bahu and AHD have their own frames', 'Saas Bahu: M2.9-35 · AHD: M2.9-30.', null);
    await slots(b, 'U-WIN summary pages', [['U-WIN summary page 1', 'uwin_summary_p1.jpg'], ['U-WIN summary page 2', null]], 'Camera or gallery (images only) · max 5 MB');
    bottomBar(s, null, 'Submit');
    rules(s, ['Session date: last 2 months up to today [UWINDataset.kt:17-29]',
      '2 upload slots (summary page 1 & 2), images only, camera or gallery, 5 MB limit [UwinFragment.kt:58-64, 145-171]',
      'Submit with no file → reminder dialog (M2.9-37) [UwinFragment.kt:139-143]',
      'Same date already submitted → toast "A U-WIN session has already been submitted for this date" [UwinFragment.kt:128-131]',
      'Saved record: date, place, count locked; Submit hidden [UWINDataset.kt:75-98; UwinFragment.kt:85-87]']);
  }
  // 11 VHND list + badge
  {
    const s = F('M2.9-11 · Meetings list + badge progress', 2, 1);
    screenTop(s, 'VHND', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add VHND', 'VHND for November already added · next from 1 Dec');
    for (const [m, n, pl] of [['05 Nov 2026', '31', 'Anganwadi centre'], ['08 Oct 2026', '27', 'Anganwadi centre'], ['10 Sep 2026', '29', 'Sub-centre']]) {
      await meetCard(b, { title: m, rows: [['No. attended', n], ['Place', pl]] });
    }
    snack(s, 'Badge', { Message: 'VHND saved', Detail: 'Community Voice · 2 of 2 meeting types · earned!', Action: 'See badge' });
    rules(s, ['"Add VHND" disabled once a VHND exists for the current month [VHNDListFragement.kt:58-68; VLFRepo.kt:1582-1612]',
      'Card: date, no. attended, place, View → opens saved record read-only [layout_vhnd_item.xml]',
      'No sync status on VHND cards in app',
      'No records → "No records found" (M2.9-22)']);
  }
  // 12 Survey register
  {
    const s = F('M2.9-12 · Survey register', 3, 1);
    screenTop(s, 'Survey register', 'Sirsi · ward 4', 'more');
    const b = body(s); b.itemSpacing = 12;
    text(b, 'Facilitator / supervisor', 'Anju Sharma');
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    const a = date(r, 'From', '01 Sep 2026'); a.layoutSizingHorizontal = 'FILL'; const c = date(r, 'Till', '30 Sep 2026'); c.layoutSizingHorizontal = 'FILL';
    section(b, 'Closed', 'Boundary houses', 'First, middle, last', 2);
    section(b, 'Open', 'Household', 'HH 7012 3345 0012', 3);
    auto(b, 'Head of house', 'Ramesh Kumar');
    grid2(b, [['Family members', '5'], ['Eligible couples', '1'], ['Pregnant women', '1'], ['Children 0–1 m', '0'], ['Children 1 m–1 yr', '1'], ['Children 2–5 yr', '0']]);
    bottomBar(s, 'Previous', 'Next household');
    rules(s, ['Not in app today – needs dev (SurveyRegisterFragment is a placeholder, no hub tile)']);
  }

  // ======================= NEW (gap pass) · death lists =======================
  // 13 CDR list
  {
    const s = F('M2.9-13 · Child deaths list (CDR)');
    screenTop(s, 'Child deaths (CDR)', 'Sirsi · 2 records');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await deathCard(b, { name: 'Baby of Pinki', meta: 'S/o Sonu Kumar · 9 days', sync: 'Pending', form: 'CDR', filled: false, rows: deathRows({ dod: '02 Nov 2026', age: '9 days', gender: 'Male', ben: '1203 4490 1102', reg: '25 Oct 2026', mob: '98XXXXXX63', father: 'Sonu Kumar', rch: '—', head: 'Ramesh Kumar' }) });
    await deathCard(b, { name: 'Anshika Kumari', meta: 'D/o Raju Sahu · 3 yrs', sync: 'Synced', form: 'CDR', filled: true, rows: deathRows({ dod: '18 Aug 2026', age: '3 yrs', gender: 'Female', ben: '1203 4471 5520', reg: '04 Feb 2024', mob: '96XXXXXX18', father: 'Raju Sahu', rch: '1220 0091 4410', head: 'Raju Sahu' }) });
    rules(s, ['Shows dead beneficiaries aged ≤ 14 yrs in the selected village [BenDao.kt:819-828]',
      'Search by name / ID + voice mic [CdrListFragment.kt:85-107]',
      'CDR button red = not filled, green = filled [BenListAdapterForForm.kt:141-166]',
      'Sync icon per card [rv_item_ben_with_form.xml:86-91]',
      'Card body tap: only a debug toast "Ben : id clicked" – no navigation [CdrListFragment.kt:58-60]',
      'ASHA Supervisor: form buttons hidden (M2.9-17)', 'Empty → "No records found" (M2.9-16)']);
  }
  // 14 General deaths list (read-only)
  {
    const s = F('M2.9-14 · General deaths list (read-only)');
    screenTop(s, 'General deaths', 'Sirsi · 5 records');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await deathCard(b, { name: 'Ram Prasad Verma', meta: 'S/o Bhola Ram · 71 yrs', rows: deathRows({ dod: '02 Oct 2026', age: '71 yrs', gender: 'Male', ben: '1203 4402 8813', reg: '11 Nov 2023', mob: '94XXXXXX02', father: 'Bhola Ram', rch: '—', head: 'Ram Prasad Verma' }) });
    await deathCard(b, { name: 'Kamla Bai', meta: 'W/o Late Hiralal · 64 yrs', rows: deathRows({ dod: '21 Sep 2026', age: '64 yrs', gender: 'Female', ben: '1203 4418 6620', reg: '19 Dec 2023', mob: '—', father: 'Govind Das', rch: '—', head: 'Dinesh Kumar' }) });
    rules(s, ['Read-only list: no form button, no sync icon (isGeneralForm = true) [GdrListFragment.kt:26; BenListAdapterForForm.kt:102-107]',
      'Shows dead beneficiaries aged ≥ 15 who are not maternal deaths [BenDao.kt:997]',
      'Search + voice mic; empty → "No records found" [BaseListFragment.kt:51-105]']);
  }
  // 15 Non-maternal deaths list (read-only)
  {
    const s = F('M2.9-15 · Non-maternal deaths list (read-only)');
    screenTop(s, 'Non-maternal deaths', 'Sirsi · 1 record');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await deathCard(b, { name: 'Sangeeta Netam', meta: 'W/o Mangal Netam · 29 yrs', rows: deathRows({ dod: '14 Jul 2026', age: '29 yrs', gender: 'Female', ben: '1203 4433 9017', reg: '08 May 2024', mob: '93XXXXXX77', father: 'Budhram', rch: '—', head: 'Mangal Netam' }) });
    rules(s, ['Read-only list of dead women 15–49 yrs who are not maternal deaths; no form button, no sync icon [NmdsrListFragment.kt:25; BenDao.kt:833-844]',
      'Search + voice mic; empty → "No records found" [BaseListFragment.kt:51-105]']);
  }
  // 16 Death list empty
  {
    const s = F('M2.9-16 · Death list · empty state');
    screenTop(s, 'Child deaths (CDR)', 'Sirsi · 0 records');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    emptyState(b, 'No records found', 'No deaths recorded for this village.', 'form');
    rules(s, ['Same empty state on CDR, MDSR, General and Non-maternal lists, also when a search has no match [CdrListFragment.kt:77-80; MdsrListFragment.kt:52-54; BaseListFragment.kt:87]']);
  }
  // 17 Supervisor view
  {
    const s = F('M2.9-17 · Death list · ASHA Supervisor view');
    screenTop(s, 'Maternal deaths (MDSR)', 'Supervisor · Sirsi');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await deathCard(b, { name: 'Rekha Yadav', meta: 'W/o Mohan Yadav · 26 yrs', sync: 'Pending', rows: deathRows({ dod: '30 Oct 2026', age: '26 yrs', gender: 'Female', ben: '1203 4456 7781', reg: '14 Mar 2026', mob: '98XXXXXX21', father: 'Ram Lal', rch: '1220 0045 3321', head: 'Mohan Yadav' }) });
    rules(s, ['Role ASHA Supervisor: CDR / MDSR form buttons hidden on every card – list is view-only [BenListAdapterForForm.kt:59-67]']);
  }
  // 18 Death report saved (view only)
  {
    const s = F('M2.9-18 · Death report saved (view only)');
    screenTop(s, 'Child death report', 'Baby of Pinki · submitted', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Submitted 02 Nov 2026', editable: false, reason: 'Death reports are one-time forms and cannot be edited.' });
    auto(b, 'Date of death', '02 Nov 2026'); auto(b, 'Place of death', 'Home'); auto(b, 'First informant (ASHA)', 'Sunita Kumari'); auto(b, 'Date of notification', '02 Nov 2026');
    await slots(b, 'Documents (view only)', [['CDR form · page 1', 'CDR_form_p1.jpg'], ['Death certificate', 'death_certificate.pdf']], 'Uploads disabled · tap a file to preview');
    const bar = bottomBar(s, null, 'Submit report'); bar.children[0].opacity = 0.4;
    rules(s, ['CDR and MDSR reopen fully read-only after save: all fields, uploads and Submit disabled [CdrObjectFragment.kt:150-154; MdsrObjectFragment.kt:96-118]',
      'Mitanin flavour: no Documents block on either form [CDRFormDataset.kt:236-240; MDSRFormDataset.kt:306-310]']);
  }
  // 19 Upload chooser
  {
    const s = F('M2.9-19 · Death report · add file chooser', null, null, true);
    screenTop(s, 'Child death report', 'Baby of Pinki · boy', 'more');
    body(s);
    await dialog(s, { ic: 'cloud_upload', icColor: 'color/primary', title: 'Death certificate', body: 'Choose how to add the file. Max 5 MB.', ok: null, cancel: 'Cancel', extra: choiceList([['camera', 'Camera'], ['image', 'Gallery'], ['pdf', 'PDF']]) });
    snack(s, 'Error', { Message: 'File is larger than 5 MB. Choose a smaller file.', Action: 'OK' });
    rules(s, ['Each CDR / MDSR slot opens: Camera / Gallery / PDF / Cancel [CdrObjectFragment.kt:308-330; MdsrObjectFragment.kt:181-200]',
      'File > 5 MB → toast, file not attached [CdrObjectFragment.kt:237-243]']);
  }
  // 20 Preview
  {
    const s = F('M2.9-20 · Death report · file preview', null, null, true);
    screenTop(s, 'Maternal death report', 'Rekha Yadav', 'more');
    body(s);
    await dialog(s, { ic: null, title: 'MDSR_form_ANM_1.jpg', body: null, ok: 'Close', cancel: null, extra: async (c) => { const im = box('Image preview', 264, 300); im.cornerRadius = 8; fill(im, 'color/surface-container-high'); im.layoutMode = 'VERTICAL'; im.primaryAxisAlignItems = 'CENTER'; im.counterAxisAlignItems = 'CENTER'; im.appendChild(icon('image', 'color/on-surface-variant', 48)); c.appendChild(im); } });
    rules(s, ['Tap uploaded image → preview dialog; PDF → opens in external PDF viewer [CdrObjectFragment.kt:129-148, 379-408]']);
  }

  // ======================= NEW · village forms =======================
  // 21 Mitanin hub
  {
    const s = F('M2.9-21 · VLF hub (Mitanin)');
    screenTop(s, 'Mitanin / Village Meetings', 'November 2026');
    const b = body(s);
    add(b, inst('sectionHeader', null, { Title: 'Meetings', 'Show action': false }));
    const g = tileGrid(b, [['village', 'VHND / VHSND', 'Overdue', 'calendar'], ['village', 'VHSNC', 'Done Nov', 'group'], ['maternal', 'Maa meeting', 'Every 3 months', 'favorite'], ['village', 'Cluster Meeting Forms', 'Done Nov', 'hospital']]);
    overdue(g, ['VHND / VHSND']);
    add(b, inst('sectionHeader', null, { Title: 'Campaigns & days', 'Show action': false }));
    tileGrid(b, [['immunization', 'Pulse Polio', '2 per year', 'vaccine'], ['care', 'ORS distribution', 'Done Nov', 'hospital'], ['couple', 'Filaria MDA', 'Once a year', 'shield'], ['child', 'Deworming day (NDD)', 'Every 6 months', 'child'], ['village', 'AHD', 'Done Nov', 'calendar']]);
    rules(s, ['Mitanin hides Saas Bahu Sammelan and U-WIN tiles [IconDataset.kt:358-368, 411-421]',
      'Hub title "Mitanin/ Village Meetings" [IconDataset.kt:53-54]',
      'PHC review tile is titled "Cluster Meeting Forms" (same screen, list + form title change; M2.9-46/47) [IconDataset.kt:322-331; PHCReviewListFragement.kt:57-61]']);
  }
  // 22 VHND list empty
  {
    const s = F('M2.9-22 · Meeting list · empty state');
    screenTop(s, 'VHND', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add VHND');
    emptyState(b, 'No records found', 'Add this month\'s VHND after the session.', 'calendar');
    rules(s, ['Same "No records found" on VHND, VHSNC, PHC, AHD, NDD, Maa meeting, Saas Bahu, U-WIN, Pulse Polio, ORS, MDA lists [VHNDListFragement.kt:73-80; VHNCListFragement.kt:64-66]']);
  }
  // 23 VHND view-only, other flavours
  {
    const s = F('M2.9-23 · VHND saved · view only (other flavours)');
    screenTop(s, 'VHND', 'Sirsi village · November', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Saved 05 Nov 2026', editable: false });
    auto(b, 'VHND date', '05 Nov 2026'); auto(b, 'Place of session', 'Anganwadi centre'); auto(b, 'Total attended', '31');
    await label(b, 'Photos (tap to preview)'); photos(b, ['Captured', 'Captured']);
    const bar = bottomBar(s, null, 'Submit'); bar.children[0].opacity = 0.4;
    rules(s, ['Sakhi / Saksham / Niramay / Xushrukha VHND: only date, place, total attended, 2 photos [VHNDDataset.kt:194-222]',
      'Saved record opens read-only, Submit disabled (same for VHSNC, PHC, AHD, NDD) [VHNDFormFragement.kt:65-97]']);
  }
  // 24 Photo chooser
  {
    const s = F('M2.9-24 · Meeting photo chooser + 5 MB error', null, null, true);
    screenTop(s, 'VHND', 'Sirsi village · November', 'more');
    body(s);
    await dialog(s, { ic: 'camera', icColor: 'color/primary', title: 'Add photo', body: null, ok: null, cancel: 'Cancel', extra: choiceList([['camera', 'Take photo'], ['image', 'Choose from gallery']]) });
    snack(s, 'Error', { Message: 'Image is larger than 5 MB', Action: 'OK' });
    rules(s, ['Image-only chooser used by VHND, VHSNC, PHC review, AHD, NDD, U-WIN [VHNDFormFragement.kt:165-201]',
      '> 5 MB → toast; no image → "No image found" toast [VHNDFormFragement.kt:276-280]']);
  }
  // 25 VHSNC list
  {
    const s = F('M2.9-25 · VHSNC list');
    screenTop(s, 'VHSNC meetings', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add VHSNC', 'November already filled · next from 1 Dec');
    for (const [d, n, pl] of [['03 Nov 2026', '12', 'Panchayat bhawan'], ['06 Oct 2026', '11', 'Panchayat bhawan']]) await meetCard(b, { title: d, ic: 'group', rows: [['No. attended', n], ['Place', pl]] });
    rules(s, ['Card: date, attended, place, View [layout_vhnc_item.xml]', 'Add disabled when this month is already filled [VHNCListFragement.kt:72-74]', 'Empty → "No records found" [VHNCListFragement.kt:64-66]']);
  }
  // 26 VHSNC form
  {
    const s = F('M2.9-26 · VHSNC form');
    screenTop(s, 'VHSNC meeting', 'Sirsi · December', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'VHSNC meeting date *', '04 Dec 2026'); select(b, 'Place of VHSNC meeting *', 'Anganwadi Centre');
    steppers(b, [['Number of ASHAs participated in VHSNC meeting *', '3']]);
    await caption(b, 'Count must be 1 or more.');
    await label(b, 'Photos (2)'); photos(b, ['Captured', 'Empty']);
    bottomBar(s, null, 'Submit');
    rules(s, ['Date: from the month after the latest filled month, within last 2 months, up to today [VHNCDataset.kt:55-56, 85-99]',
      'Counts min = 1 (0 not allowed) [VHNCDataset.kt:130-196]',
      '2 photos, camera / gallery chooser (M2.9-24), 5 MB, preview [VHNCFormFragement.kt:173-207]',
      'Saved record: view-only, Submit disabled [VHNCFormFragement.kt:93-95]',
      'Mitanin: 8 extra fields, count relabelled "Number of Mitanins…" (M2.9-45) [VHNCDataset.kt:240-260]']);
  }
  // 27 PHC review list
  {
    const s = F('M2.9-27 · PHC review list');
    screenTop(s, 'PHC review meeting', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add PHC review meeting', 'November already filled · next from 1 Dec');
    for (const [d, n, pl] of [['07 Nov 2026', '18', 'PHC Bakawand'], ['09 Oct 2026', '16', 'PHC Bakawand']]) await meetCard(b, { title: d, ic: 'hospital', rows: [['No. attended', n], ['Place', pl]] });
    rules(s, ['Card: date, attended, place, View; Add disabled once filled this month [PHCReviewListFragement.kt:47-78]',
      'Mitanin: title + button read "Cluster Meeting Forms" (M2.9-46) [PHCReviewListFragement.kt:57-61, 88-91]']);
  }
  // 28 PHC review form
  {
    const s = F('M2.9-28 · PHC review form');
    screenTop(s, 'PHC review meeting', 'November', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Meeting date *', '07 Nov 2026'); select(b, 'Place *', 'PHC Bakawand'); auto(b, 'Village', 'Sirsi'); steppers(b, [['Total attended', '18']]);
    await label(b, 'Photos (2)'); photos(b, ['Captured', 'Empty']);
    bottomBar(s, null, 'Submit');
    rules(s, ['Default flavours: date, place, village, total, 2 photos. Mitanin adds 2 checklists (M2.9-47) [PHCReviewDataset.kt:128-137, 153]',
      'Date: last 60 days up to today [PHCReviewDataset.kt:43-44]',
      'Saved → view-only; photo chooser, 5 MB, preview [PHCReviewFormFragement.kt:85-93, 162-200]']);
  }
  // 29 AHD list
  {
    const s = F('M2.9-29 · AHD list');
    screenTop(s, 'AHD', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add AHD');
    for (const [d, mob, pl] of [['09 Oct 2026', 'Yes', 'Anganwadi centre'], ['11 Sep 2026', 'No', '—']]) await meetCard(b, { title: d, rows: [['Mobilized for AHD', mob], ['Place', pl]] });
    rules(s, ['Card: AHD date, "Mobilized for AHD", place, View [layout_ahd_item.xml]', 'Add disabled once filled this month (enabled here: none in November) [AHDListFragment.kt:43-66]']);
  }
  // 30 AHD form
  {
    const s = F('M2.9-30 · AHD form');
    screenTop(s, 'AHD', 'Sirsi · November', 'more');
    const b = body(s); b.itemSpacing = 12;
    auto(b, 'AHD date (today)', '09 Nov 2026');
    await seg(b, 'Mobilized for AHD? *', ['Yes', 'No'], 0);
    const rv = reveal(b); select(rv, 'Place *', 'Anganwadi centre'); date(rv, 'Date *', '09 Nov 2026');
    await label(b, 'Photos (2)'); photos(b, ['Empty', 'Empty']);
    bottomBar(s, null, 'Submit');
    rules(s, ['AHD date can only be today (min = max = now) [AHDDataset.kt:46-47]', 'Mobilized = Yes shows Place + Date [AHDDataset.kt:105-127]', 'Saved → view-only; photo chooser, 5 MB [AHDFormFragment.kt:85-93, 177-221]']);
  }
  // 31 Deworming list
  {
    const s = F('M2.9-31 · Deworming (NDD) list');
    screenTop(s, 'National Deworming Day', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add NDD', 'NDD recorded on 10 Aug 2026 · next after 10 Feb 2027');
    await meetCard(b, { title: '10 Aug 2026', tone: 'child', ic: 'child', rows: [['Round conducted?', 'Yes'], ['Place', 'Government school'], ['Out-of-school children', '6']] });
    rules(s, ['Add disabled if any NDD recorded in the last 6 months [DewormingListFragment.kt:59-61; VLFDao.kt:170-179]', 'Card: date, round conducted?, place, out-of-school count, View [layout_deworming_item.xml]']);
  }
  // 32 Maa meeting list
  {
    const s = F('M2.9-32 · Maa meeting list');
    screenTop(s, 'Maa meetings', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'New Maa meeting', 'Last meeting 12 Sep 2026 · next after 12 Dec 2026');
    await meetCard(b, { title: '12 Sep 2026', tone: 'maternal', ic: 'favorite', sync: 'Synced', rows: [['Place', 'Panchayat bhawan'], ['Participants', '16']] });
    await meetCard(b, { title: '10 Jun 2026', tone: 'maternal', ic: 'favorite', sync: 'Synced', rows: [['Place', 'Anganwadi centre'], ['Participants', '14']] });
    rules(s, ['"New Maa meeting" disabled until 3 months after the last meeting [MaaMeetingListFragment.kt:41-46; MaaMeetingRepo.kt:201-215]', 'Card: sync icon, date, place, participants, View [layout_maameeting_item.xml]']);
  }
  // 33 Required fields alert
  {
    const s = F('M2.9-33 · Required fields alert', null, null, true);
    screenTop(s, 'Maa meeting', 'Breastfeeding support', 'more');
    body(s);
    await dialog(s, { ic: 'warning', title: 'Alert', body: 'Please fill all required fields.', ok: 'OK', cancel: null });
    rules(s, ['Shown on Submit with missing required fields: Maa meeting, Saas Bahu Sammelan [MaaMeetingFormFragment.kt:145-155; SaasBahuSamelanFragment.kt:151-157]']);
  }
  // 34 Saas Bahu list
  {
    const s = F('M2.9-34 · Saas Bahu Sammelan list');
    screenTop(s, 'Saas Bahu Sammelan', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add Saas Bahu Sammelan', 'Already added for November · next from 1 Dec');
    await meetCard(b, { title: '06 Nov 2026', ic: 'group', sync: 'Pending', rows: [['Place', 'Community hall'], ['Participants', '24'], ['ASHA ID', '7104523']] });
    rules(s, ['Add disabled if an entry exists this calendar month (not quarterly) [SaasBahuSammelanListFragment.kt:58-73]', 'Card: sync icon, date, place, participants, ASHA ID, View [layout_saas_bahu_list.xml]', 'Module hidden in Mitanin [IconDataset.kt:358]']);
  }
  // 35 Saas Bahu form
  {
    const s = F('M2.9-35 · Saas Bahu Sammelan form');
    screenTop(s, 'Saas Bahu Sammelan', 'Sirsi · November', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Date *', '06 Nov 2026'); select(b, 'Place *', 'Community hall'); steppers(b, [['Participants *', '24']]);
    await uploads(b, 'Photos / minutes of meeting (up to 5)', [['sammelan_photo.jpg', 'Done'], ['MoM_06Nov.pdf', 'Done']], 'JPEG, PNG or PDF · max 5 MB each');
    bottomBar(s, null, 'Submit');
    rules(s, ['Date: last 2 months up to today [SaasBahuSamelanDataset.kt:27-28]', 'Up to 5 photos / MoM (JPEG, PNG, PDF), 5 MB; view mode shows only filled slots [SaasBahuSamelanFragment.kt:48-76]',
      'Missing fields → Alert dialog (M2.9-33); Submit hidden for saved record; sync starts after save [SaasBahuSamelanFragment.kt:104, 195-211]']);
  }
  // 36 U-WIN list
  {
    const s = F('M2.9-36 · U-WIN session list');
    screenTop(s, 'U-WIN sessions', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add new');
    await meetCard(b, { title: '05 Nov 2026', tone: 'immunization', ic: 'vaccine', sync: 'Pending', rows: [['Place', 'Sub-centre'], ['Participants', '22']] });
    await meetCard(b, { title: '02 Nov 2026', tone: 'immunization', ic: 'vaccine', sync: 'Synced', rows: [['Place', 'Anganwadi centre'], ['Participants', '17']] });
    rules(s, ['"Add new" always enabled – many sessions per month allowed (one per date) [UwinListFragment.kt:45-49]', 'Card: sync icon, date, place, participants, View [item_uwin_list.xml]', 'Module hidden in Mitanin [IconDataset.kt:411]']);
  }
  // 37 U-WIN upload reminder
  {
    const s = F('M2.9-37 · U-WIN upload reminder', null, null, true);
    screenTop(s, 'U-WIN session', 'Sirsi · November', 'more');
    body(s);
    await dialog(s, { ic: 'cloud_upload', icColor: 'color/primary', title: 'Upload photo?', body: 'Do you want to upload the U-WIN immunization page photo to claim your incentive?', cancel: 'No, submit', ok: 'Upload' });
    rules(s, ['Shown on Submit when no file is uploaded [UwinFragment.kt:139-143]', 'Duplicate date → toast "A U-WIN session has already been submitted for this date" [UwinFragment.kt:128-131]']);
  }
  // 38 Pulse polio list
  {
    const s = F('M2.9-38 · Pulse Polio campaign list');
    screenTop(s, 'Pulse Polio campaigns', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add campaign', 'Last campaign 15 Jun 2026 · next after 15 Dec 2026');
    await meetCard(b, { title: '15 Jun 2026', tone: 'immunization', ic: 'vaccine', rows: [['Number of children', '86']] });
    await meetCard(b, { title: '19 Jan 2026', tone: 'immunization', ic: 'vaccine', rows: [['Number of children', '91']] });
    rules(s, ['Add disabled when 2 campaigns already this year OR < 6 months since the last one [PulsePolioCampaignListViewModel.kt:59-90]', 'Downloads past campaigns from server on open [PulsePolioCampaignListViewModel.kt:47-55]', 'Card: date + "Number of children: n" + View [PulsePolioCampaignAdapter.kt:49-50]']);
  }
  // 39 Campaign missing fields
  {
    const s = F('M2.9-39 · Campaign form · missing fields');
    screenTop(s, 'Pulse Polio', 'New campaign', 'more');
    const b = body(s); b.itemSpacing = 12;
    S.tf(b, 'Error', { Label: 'Campaign date *', Value: '', Helper: 'This field is required', 'Show helper': true }, 'calendar');
    S.tf(b, 'Error', { Label: 'Number of children *', Value: '', Helper: 'This field is required', 'Show helper': true });
    await label(b, 'Campaign photos'); photos(b, ['Captured', 'Empty']);
    await caption(b, 'photo_3.jpg is 6.2 MB – max 5 MB', 'color/error');
    bottomBar(s, null, 'Save');
    snack(s, 'Error', { Message: 'Please fill all required fields', Action: 'OK' });
    rules(s, ['Missing fields: toast + scroll to first error; inline errors from schema [PulsePolioCampaignFormFragment.kt:269-290]', 'Photo over schema size limit → inline error [PulsePolioCampaignFormFragment.kt:64-142]', 'Same engine for ORS and Filaria MDA campaign forms']);
  }
  // 40 Pulse polio view-only
  {
    const s = F('M2.9-40 · Pulse Polio campaign · view only');
    screenTop(s, 'Pulse Polio', 'Campaign · 15 Jun 2026', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Saved 15 Jun 2026', editable: false });
    auto(b, 'Campaign date', '15 Jun 2026'); auto(b, 'Number of children', '86');
    await label(b, 'Campaign photos'); photos(b, ['Captured', 'Captured']);
    rules(s, ['Saved campaign opens view-only, Save button hidden [PulsePolioCampaignFormFragment.kt:175]', 'Same for ORS and Filaria MDA (opened from history)']);
  }
  // 41 ORS list
  {
    const s = F('M2.9-41 · ORS distribution list');
    screenTop(s, 'ORS distribution', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add ORS campaign', 'Last campaign 20 Oct 2026 · next after 20 Nov 2026');
    await meetCard(b, { title: '20 Oct 2026', tone: 'care', ic: 'hospital', rows: [['Number of families', '42']] });
    rules(s, ['Add disabled until 1 month after the last campaign (no season rule) [ORSCampaignListViewModel.kt:65-87]', 'Downloads from server on open; card: date + "Number of families: n" + View [ORSCampaignListViewModel.kt:54-62; ORSCampaignAdapter.kt:45-46]']);
  }
  // 42 ORS form
  {
    const s = F('M2.9-42 · ORS distribution form');
    screenTop(s, 'ORS distribution', 'New campaign', 'more');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Server-driven form', 'Fields come from the ORS campaign schema. Example fields shown.', null);
    date(b, 'Campaign date *', '22 Nov 2026'); steppers(b, [['Number of families *', '38']]);
    await label(b, 'Campaign photos (last 2 kept)'); photos(b, ['Captured', 'Empty']);
    bottomBar(s, null, 'Save');
    rules(s, ['Same engine as Pulse Polio: schema fields / rules, 2 rolling photos, view-only after save (M2.9-40), sync after save [ORSCampaignFormFragment.kt]', 'No AUDIT_FORMS spec for ORS – confirm schema with backend']);
  }
  // 43 Filaria MDA history
  {
    const s = F('M2.9-43 · Filaria MDA campaign history');
    screenTop(s, 'History of MDA campaign', 'Sirsi village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add MDA', '2026 campaign already recorded · next in 2027');
    await meetCard(b, { title: 'Started 10 Feb 2026', tone: 'couple', ic: 'shield', rows: [['No. of families', '118'], ['No. of individuals', '512']] });
    await meetCard(b, { title: 'Started 13 Feb 2025', tone: 'couple', ic: 'shield', rows: [['No. of families', '112'], ['No. of individuals', '497']] });
    rules(s, ['Card: start date, no. of families, no. of individuals, View [FilariaMdaCampaignHistory.kt:53-90]', '"Add MDA" disabled when this year\'s campaign is recorded (once per year) [FilariaMdaFormCampaignViewModel.kt:74-80]']);
  }
  // 44 Filaria MDA form
  {
    const s = F('M2.9-44 · Filaria MDA campaign form');
    screenTop(s, 'MDA campaign', 'Sirsi · 2027', 'more');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Server-driven form', 'LF_MDA_CAMPAIGN schema. Example fields shown.', null);
    S.tf(b, 'Error', { Label: 'Campaign start date *', Value: '05 Feb 2026', Helper: 'Must be after the previous campaign (10 Feb 2026)', 'Show helper': true }, 'calendar');
    steppers(b, [['No. of families', '120'], ['No. of individuals', '520']]);
    await label(b, 'Campaign photos (max 2)'); photos(b, ['Captured', 'Empty']);
    bottomBar(s, null, 'Save');
    rules(s, ['Visit date ≤ today and after previous campaign date → inline errors [FilariaMdaCampaignFormFragment.kt:270-300]',
      'Same year already submitted → toast "You have already submitted this form for this Year" [FilariaMdaFormCampaignViewModel.kt:229-232]',
      'Max 2 photos (camera or file); "Unable to open camera" toast [FilariaMdaCampaignFormFragment.kt:95-157]',
      'Opened from history → view-only, Save hidden [FilariaMdaCampaignFormFragment.kt:180-188]',
      'Starts an MDA sync as soon as the screen opens [FilariaMdaCampaignFormFragment.kt:178]']);
  }

  // ======================= MITANIN variants (addendum) =======================
  // 45 VHSNC form · Mitanin
  {
    const s = F('M2.9-45 · VHSNC form · Mitanin');
    screenTop(s, 'VHSNC meeting', 'Kurud · December', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'VHSNC meeting date *', '04 Dec 2026'); select(b, 'Place of VHSNC meeting *', 'Anganwadi Centre');
    flavourTag(b);
    auto(b, 'Village name', 'Kurud');
    grid2(b, [['Number of Mitanins participated in VHSNC meeting *', '3'], ['Number of ANM/ MPW attended *', '1'], ['Number of AWW attended *', '1'], ['Number of pregnant women *', '6'], ['Number of lactating mothers *', '5'], ['Number of Committee members present *', '9']]);
    await seg(b, 'Follow-up the previous meeting minutes', ['Yes', 'No'], 0);
    await label(b, 'Photos (2)'); photos(b, ['Captured', 'Empty']);
    bottomBar(s, null, 'Submit');
    rules(s, ['Mitanin adds: village name, ANM/MPW, AWW, PW, lactating mothers, committee members, follow-up of previous minutes (Yes/No) [VHNCDataset.kt:240-250]',
      'Mitanin: count label is "Number of Mitanins participated…" instead of "…ASHAs…" [VHNCDataset.kt:252-253]',
      'Same date / photo / view-only rules as M2.9-26']);
  }
  // 46 Cluster meeting list · Mitanin
  {
    const s = F('M2.9-46 · Cluster meeting list · Mitanin');
    screenTop(s, 'Cluster Meeting Forms', 'Kurud village');
    const b = body(s); b.itemSpacing = 12;
    await addBtn(b, 'Add Cluster Meeting Forms', 'November already filled · next from 1 Dec');
    for (const [d, n, pl] of [['07 Nov 2026', '18', 'PHC Kurud'], ['09 Oct 2026', '16', 'PHC Kurud']]) await meetCard(b, { title: d, ic: 'hospital', rows: [['No. attended', n], ['Place', pl]] });
    rules(s, ['Mitanin: screen title and add button use "Cluster Meeting Forms" (R.string.icon_cluster_phc) instead of "PHC Review Meeting Forms" [PHCReviewListFragement.kt:57, 88]',
      'Same monthly add rule as M2.9-27']);
  }
  // 47 Cluster meeting form · Mitanin
  {
    const s = F('M2.9-47 · Cluster meeting form · Mitanin');
    screenTop(s, 'Cluster Meeting Forms', 'November', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Meeting date *', '07 Nov 2026'); select(b, 'Place *', 'PHC Kurud'); auto(b, 'Village', 'Kurud'); steppers(b, [['Total attended', '18']]);
    flavourTag(b);
    await checks(b, 'Participants Attended', [['Mitanins', true], ['MT', true], ['ANM/ RHO', true]]);
    await checks(b, 'Mitanin Activity check list', [['Select all', false], ['New schemes / guidelines shared', true], ['Data registers and records checked', true], ['Training and capacity building discussed', false], ['Review of Dava Peti (medicine kit)', true], ['Supply distribution carried out', false], ['Progress of PW and breastfeeding mothers discussed', true], ['Referral of serious cases discussed', true], ['Updates on child immunization', true]]);
    await label(b, 'Photos (2)'); photos(b, ['Captured', 'Empty']);
    bottomBar(s, null, 'Submit');
    rules(s, ['Mitanin adds "Participants Attended" (Mitanins / MT / ANM-RHO) and "Mitanin Activity check list" (activity_checklist) – both optional [PHCReviewDataset.kt:78-96, 128-137]',
      'Saved values reloaded only on Mitanin [PHCReviewDataset.kt:153-165]',
      'Mitanin: form title "Cluster Meeting Forms" [PHCReviewFormFragement.kt:142]']);
  }
  // 48 Death report · Mitanin
  {
    const s = F('M2.9-48 · Child death report (CDR) · Mitanin');
    screenTop(s, 'Child death report', 'Baby of Kavita · girl', 'more');
    const b = body(s); b.itemSpacing = 12;
    await infoCard(b, 'Child (auto)', [['Name', 'Baby of Kavita'], ['Date of birth', '20 Oct 2026'], ['Age', '13 days'], ['Gender', 'Female'], ["Mother's name", 'Kavita Dhruw'], ['Village', 'Kurud · Block Dhamtari']]);
    section(b, 'Closed', 'Address', '6 fields · auto', 2);
    section(b, 'Open', 'Death details', '6 fields', 3);
    date(b, 'Date of death *', '02 Nov 2026'); auto(b, 'Place of death (from record)', 'Home'); auto(b, 'First informant (Mitanin)', 'Sarita Sahu');
    date(b, 'Date of notification *', '02 Nov 2026');
    flavourTag(b, 'Mitanin · no Documents section');
    bottomBar(s, null, 'Submit report');
    rules(s, ['Mitanin: all 3 upload slots removed from CDR (form pages 1–2, death certificate) [CDRFormDataset.kt:236-240]',
      'Mitanin: upload taps ignored [CdrObjectFragment.kt:122]',
      'MDSR on Mitanin: same – MDSR form 1/2 and death certificate removed [MDSRFormDataset.kt:306-310; MdsrObjectFragment.kt:103]',
      'Death lists (CDR / MDSR / GDR / NMDSR) and their criteria have no Mitanin difference']);
  }

  const endY = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, endY, 860,
    'DEATH REPORTS & VILLAGE FORMS NOTES\n' +
    '• Death lists (CDR / MDSR / GDR / NMDSR) only show deceased beneficiaries of the selected village; CDR and MDSR have red/green form buttons, GDR and NMDSR are read-only. CDR / MDSR are one-time forms.\n' +
    '• Village forms follow pattern P7: date, place, counts as steppers, checklists, photos / files. Each list has its own add-frequency rule (see rule notes).\n' +
    '• No form in this group has a draft: one Submit saves, then the record opens view-only.\n' +
    '• VLF hub shows "Community Voice" progress (2+ meeting types per quarter) when gamification is ON.\n' +
    '• Pulse Polio / ORS / Filaria MDA campaign forms are server-driven (schema); fields shown are examples.\n' +
    '• Mitanin flavour: hub title "Mitanin / Village Meetings" (IconDataset vlfTitle); no Saas Bahu / U-WIN; extra Mitanin-only fields on VHND, Maa meeting, PHC review.');
  fitSection(sec);
  return ['2.9 screens: ' + frames.length];
}
