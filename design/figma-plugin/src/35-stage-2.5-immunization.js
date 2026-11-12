// ---------------------------------------------------------------------------
// Stage 2.5 · Immunization
// Sources: ui/home_activity/immunization_due/*, model/Immunization.kt (ChildImmunizationCategory),
// configuration/ImmunizationDataset. Vaccine names come from the VACCINE table (server); UIP schedule used as sample.
// 2026-11-05 gap fix: gap_C (immunization rows) + Mitanin addendum → edited 01–06, added 07–14.
// ---------------------------------------------------------------------------
async function buildStage25(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureSearchMic(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack } = S;
  const { actionCard, dialog, searchRow, viewMode, rules, caption, tag, btn, row } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.5 · Immunization');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };
  const STAT = { given: ['color/success-container', 'color/success', 'check_circle'], due: ['color/warning-container', 'color/warning', 'clock'], overdue: ['color/error-container', 'color/error', 'warning'], next: ['color/surface-container-high', 'color/on-surface-variant', 'lock'], missed: ['color/surface-container-high', 'color/error', 'close'] };
  const dosePill = async (p, name, st, sub) => {
    const [bg, fg, ic] = STAT[st];
    const c = AL('HORIZONTAL', name); c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 6; c.paddingLeft = 8; c.paddingRight = 10; c.paddingTop = c.paddingBottom = 6; rad(c, 'radius/sm'); fill(c, bg); p.appendChild(c);
    c.appendChild(icon(ic, fg, 16));
    const col = AL('VERTICAL'); c.appendChild(col); col.appendChild(await txt(name, 'Label/Large', 'color/on-surface')); if (sub) col.appendChild(await txt(sub, 'Label/Small', fg));
    return c;
  };
  const stageCard = async (p, title, age, doses, open = true) => {
    const c = AL('VERTICAL', title); pad(c, 'spacing/md', 'spacing/sm'); gap(c, 'spacing/xs'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(p, c);
    const h = AL('HORIZONTAL'); h.counterAxisAlignItems = 'CENTER'; add(c, h);
    const ht = await txt(title, 'Title/Small', 'color/on-surface'); h.appendChild(ht); ht.layoutSizingHorizontal = 'FILL';
    h.appendChild(await txt(age, 'Label/Medium', 'color/on-surface-variant'));
    if (open) { const w = AL('HORIZONTAL', 'Doses'); w.layoutWrap = 'WRAP'; w.itemSpacing = 6; w.counterAxisSpacing = 6; add(c, w); for (const d of doses) await dosePill(w, ...d); }
    return c;
  };
  const STAGES = ['All', 'Birth', '6 wk', '10 wk', '14 wk', '9–12 m', '16–24 m', '5–6 y'];
  // Two quick-filter cards (Due this month / Scheduled next month), single select (ChildImmunizationListFragment.kt:134-200)
  const quickFilters = async (p, sel, a = '12', b2 = '7') => {
    const r = AL('HORIZONTAL', 'Quick filters'); gap(r, 'spacing/sm'); add(p, r);
    for (const [i, n, l] of [[0, a, 'Due this month'], [1, b2, 'Scheduled next month']]) {
      const c = AL('VERTICAL', 'Quick filter · ' + l); pad(c, 'spacing/md', 'spacing/sm'); c.itemSpacing = 2; rad(c, 'radius/lg'); fill(c, i === sel ? 'module/immunization/bg' : 'color/surface-container'); stroke(c, i === sel ? 'module/immunization/fg' : 'color/outline-variant', i === sel ? 2 : 1); r.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      c.appendChild(await txt(n, 'Number/Medium', i === sel ? 'module/immunization/fg' : 'color/on-surface'));
      const t = await txt(l, 'Label/Medium', 'color/on-surface'); c.appendChild(t); wrapText(t);
    }
    return r;
  };
  const stageStrip = (p, sel) => chips(p, STAGES, sel);
  // Vaccine row in the code bottom sheet: state given → View, pending → Fill, missed → no button
  const vaccRow = async (p, name, due, st, disabled = false) => {
    const r = AL('HORIZONTAL', 'Vaccine · ' + name); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 8; pad(r, 'spacing/sm', 'spacing/xs'); rad(r, 'radius/md'); fill(r, 'color/surface'); stroke(r, 'color/outline-variant'); add(p, r);
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 0; r.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    col.appendChild(await txt(name, 'Title/Small', 'color/on-surface'));
    col.appendChild(await txt('Due: ' + due, 'Body/Small', 'color/on-surface-variant'));
    if (st === 'given') { tag(r, 'Success', 'Given'); btn(r, 'Text', 'View', disabled); }
    else if (st === 'missed') tag(r, 'Neutral', 'Missed');
    else if (st === 'overdue') { tag(r, 'Alert', 'Overdue'); btn(r, 'Tonal', 'Fill', disabled); }
    else { tag(r, 'Child', 'Pending'); btn(r, 'Tonal', 'Fill', disabled); }
    return r;
  };
  const catHead = async (p, t) => { const h = await txt(t, 'Label/Large', 'color/on-surface-variant'); add(p, h); return h; };

  // 01 Hub
  {
    const s = F('M2.5-01 · Immunization hub', 0, 0);
    screenTop(s, 'Immunization', 'Children & mothers');
    const b = body(s);
    await stat(b, [['8', 'Children due this week', 'immunization'], ['3', 'Overdue', 'disease'], ['41', 'Fully immunized (12 m)', 'child']]);
    tileGrid(b, [['immunization', 'Child immunization', '8 due · 3 overdue', 'vaccine'], ['maternal', 'Mother (Td) immunization', '4 due', 'pregnant']]);
    banner(b, 'Info', 'Next session day: Wed 5 Nov', 'Village Health & Nutrition Day at Anganwadi centre. Remind families today.', 'Share list');
    rules(s, [
      'Code hub has ONLY the "Child immunization" tile; Mother immunization is in nav but has no tile → unreachable (IconDataset.kt:755-767; nav_home.xml:338). PO to decide: re-enable in code or drop',
      'Stat tiles and "Share list" session banner: not in app today – needs dev',
    ]);
  }
  // 02 Child due list (edited to code: quick filters, stage strip, summary, card fields, deceased)
  {
    const s = F('M2.5-02 · Child due list', 1, 0);
    screenTop(s, 'Child immunization', 'Children born in the last 6 years');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search child or mother name', { filter: true });
    await quickFilters(b, 0);
    stageStrip(b, 4);
    await caption(b, '12 beneficiaries · Due this month · 14 wk', 'color/on-surface');
    await actionCard(b, { name: 'Aarav Kumar', meta: 'Boy · mother Sunita Devi', rows: [['Child RCH ID', '1234 5678 9020'], ['Mother RCH ID', '1210 4456 7890'], ['DOB', '12 Aug 2026']], actions: [['Filled', 'Show vaccines']] });
    await actionCard(b, { name: 'Riya Kumari', meta: 'Girl · mother Pinki Devi', sync: 'Pending', rows: [['Child RCH ID', '1234 5678 6620'], ['Mother RCH ID', '1210 4456 1102'], ['DOB', '02 Jan 2026']], actions: [['Filled', 'Show vaccines']] });
    await actionCard(b, { name: 'Baby of Rani', initials: 'BR', meta: 'Girl · mother Rani Devi', state: 'deceased', rows: [['DOB', '24 Oct 2026']], actions: [['Filled', 'Show vaccines']] });
    rules(s, [
      'Quick-filter cards "Due this month" / "Scheduled next month" with counts; single-select, tap again to clear; counts respect search + stage (Fragment:134-200; VM:209-286)',
      'Stage strip (also filter sheet M2.5-03): All, Birth, 6 wk, 10 wk, 14 wk, 9–12 m, 16–24 m, 5–6 y (VM:60-69)',
      'Result summary line "N beneficiaries · filter · stage" (VM:288-313). Empty states → M2.5-07',
      'Scope: children born in the last 6 years, newest first (VM:75-84, 172)',
      'Card: Child RCH ID, Mother RCH ID, DOB; "Show vaccines" opens vaccine sheet M2.5-09. Deceased icon (xml:67-72)',
      'From Home: "Due only" preset → M2.5-08',
      'Old tabs "Overdue / This week / All" removed: no this-week logic in code',
    ]);
  }
  // 03 Dose-stage filter sheet (edited to code: single select, applies on tap)
  {
    const s = F('M2.5-03 · Filter by age stage', 2, 0, true);
    screenTop(s, 'Child immunization', 'Children born in the last 6 years');
    const b = body(s); recordCard(b, { name: 'Aarav Kumar', meta: 'Boy · DOB 12 Aug 2026', tag: 'Penta-2 overdue', tone: 'Alert', action: 'Show vaccines' });
    scrim(s);
    const sh = sheet(s, 'Filter');
    const hr = row(sh, 'Head', 8); const ht = await txt('Select dose stage', 'Title/Medium', 'color/on-surface'); hr.appendChild(ht); ht.layoutSizingHorizontal = 'FILL'; hr.appendChild(icon('close', 'color/on-surface-variant', 24));
    await radios(sh, null, ['All', 'Birth', '6 weeks', '10 weeks', '14 weeks', '9–12 months', '16–24 months', '5–6 years'], 3);
    pinBottom(s, sh);
    rules(s, [
      'Single select; applies immediately on tap and closes; close icon dismisses (ChildImmunizationFilterBottomSheetFragment.kt:35-47)',
      '10 years, 16 years and Catch-up were removed in code (FLW-1144); no Status radio, no Clear / Show buttons',
    ]);
  }
  // 04 Vaccination card (edited: missed state, due date on every dose, view, fillable next month; PCV only Mitanin)
  {
    const s = F('M2.5-04 · Child vaccination card', 3, 0);
    screenTop(s, 'Aarav Kumar', 'Boy · born 12 Aug 2026 · 11 weeks', 'more');
    const b = body(s); b.itemSpacing = 12;
    const pr = AL('VERTICAL', 'Progress'); pad(pr, 'spacing/md', 'spacing/sm'); gap(pr, 'spacing/xs'); rad(pr, 'radius/lg'); fill(pr, 'module/immunization/bg'); add(b, pr);
    pr.appendChild(await txt('6 of 20 doses given · 3 overdue · 1 missed', 'Title/Small', 'color/on-surface'));
    const pb = S.progress(296, 0.3, 'module/immunization/fg', 8); pr.appendChild(pb); pb.layoutSizingHorizontal = 'FILL';
    await stageCard(b, 'At birth', 'Done', [['BCG', 'given', 'Given 12 Aug · View'], ['OPV-0', 'given', 'Given 12 Aug · View'], ['Hep B-0', 'missed', 'Missed · too late']]);
    await stageCard(b, '6 weeks', 'Done', [['OPV-1', 'given', 'Given 23 Sep · View'], ['Penta-1', 'given', 'Given 23 Sep · View'], ['Rota-1', 'given', 'Given 23 Sep · View'], ['fIPV-1', 'given', 'Given 23 Sep · View']]);
    await stageCard(b, '10 weeks', 'Overdue 5 days', [['OPV-2', 'overdue', 'Due 21 Oct'], ['Penta-2', 'overdue', 'Due 21 Oct'], ['Rota-2', 'overdue', 'Due 21 Oct']]);
    await stageCard(b, '14 weeks', 'Due 18 Nov · can fill now', [['OPV-3', 'due', 'Due 18 Nov'], ['Penta-3', 'due', 'Due 18 Nov'], ['Rota-3', 'due', 'Due 18 Nov'], ['fIPV-2', 'due', 'Due 18 Nov']]);
    await stageCard(b, '9–12 months', 'From 12 May 2027', [['MR-1', 'next', 'Due 12 May'], ['JE-1', 'next', 'Due 12 May'], ['Vit A-1', 'next', 'Due 12 May']]);
    await stageCard(b, '16–24 months', 'Later', [], false);
    const go = inst('button', 'Style=Filled, State=Enabled', { Label: 'Record 10-week doses', 'Show icon': true, Icon: 'vaccine' }); add(b, go);
    rules(s, [
      'Code shows this as a bottom sheet (M2.5-09), layout difference only',
      'Missed: past max age → no button, cannot be given (VaccineState.MISSED, ChildImmunizationListViewModel.kt:152-157)',
      'Given dose → "View" opens the saved record read-only (BindingUtils.kt:99-101) → M2.5-12',
      'Due date shown on every dose (rv_item_imm_vaccine.xml:75-79)',
      'Doses due within the next month are listed and fillable (Pending, not locked) (VM:116-137); debug/UAT: all stages birth–16–24 m fillable early',
      'PCV-1, PCV-2, PCV-Booster exist on Mitanin only (ImmunizationRepo.kt:303-311) → M2.5-14',
    ]);
  }
  // 05 Vaccine record form (edited: select all, MCP upload, rules)
  {
    const s = F('M2.5-05 · Record vaccines (10 weeks)', 4, 0);
    screenTop(s, 'Record vaccines', 'Aarav Kumar · 10-week doses', 'more');
    const b = body(s); b.itemSpacing = 12;
    await infoCard(b, 'Child (auto)', [['Mother', 'Sunita Devi'], ['Date of birth', '12 Aug 2026'], ['Doses taken so far', '6']]);
    date(b, 'Date of vaccination *', '02 Nov 2026');
    await label(b, 'Which vaccines were given today? *');
    add(b, kinst('Checkbox option', 'Checked=false', { Label: 'Select all' }));
    for (const [v, on] of [['OPV-2', true], ['Penta-2', true], ['Rota-2', false]]) add(b, kinst('Checkbox option', `Checked=${on}`, { Label: `${v} · due 21 Oct` }));
    banner(b, 'Warning', 'Rota-2 not given', 'It will stay on the due list. Add a reason if the family refused.', 'Add reason');
    select(b, 'Vaccinated place *', 'VHND · Anganwadi centre'); select(b, 'Vaccinated by *', 'ANM');
    await label(b, 'MCP card (2 photos)');
    const ph = AL('HORIZONTAL', 'Photos'); gap(ph, 'spacing/sm'); add(b, ph); ph.appendChild(kinst('Photo tile', 'State=Captured')); ph.appendChild(kinst('Photo tile', 'State=Empty'));
    bottomBar(s, null, 'Save 2 doses');
    snack(s, 'Badge', { Message: '2 doses saved', Detail: 'Child Fully Protected · 2 of 3 children for Tier 1', Action: 'View' });
    rules(s, [
      '"Select all" checkbox for the stage\'s vaccines (ImmunizationFormFragment.kt:96, 385-413)',
      'Submit hidden until ≥ 1 vaccine is ticked; hidden for an existing record (:246-254)',
      'No MCP photo → pop-up M2.5-11 before save (all except Mitanin) (:120-128)',
      'MCP card: 2 photos, camera or gallery, size-limit toast, tap to view (:266-323). Hidden on Mitanin → M2.5-13',
      'Tapping a missed dose → toast "Immunization cannot be done as it is missed" (ChildImmunizationVaccineAdapter.kt:71-81); debug/UAT can save missed doses (:420-424)',
      'Saving: spinner replaces form; "Save successful" / "Something went wrong" toast; go back + sync (:130-162)',
      'Rota refusal-reason banner: not in app today – needs dev',
    ]);
  }
  // 06 Mother Td list + sheet (edited to code: Fill opens full form, dependent dose)
  {
    const s = F('M2.5-06 · Mother Td immunization', 5, 0, true);
    screenTop(s, 'Mother immunization', 'Pregnant women · Td');
    const b = body(s); b.itemSpacing = 12;
    recordCard(b, { name: 'Sunita Devi', meta: '24 wks · Td-1 given 14 Aug', tag: 'Td-2 due', tone: 'Maternal', action: 'Show vaccines' });
    recordCard(b, { name: 'Anita Devi', meta: '15 wks · pregnant before in 2024', tag: 'Td booster due', tone: 'Maternal', action: 'Show vaccines' });
    scrim(s);
    const sh = sheet(s, 'Td doses');
    sh.appendChild(await txt('Sunita Devi · Td', 'Title/Medium', 'color/on-surface'));
    await vaccRow(sh, 'Td-1', '12 Aug 2026', 'given');
    await vaccRow(sh, 'Td-2', '11 Sep 2026', 'pending');
    await vaccRow(sh, 'Td booster', '—', 'missed');
    await caption(sh, 'Fill opens the full Record-vaccine form (M2.5-05).');
    pinBottom(s, sh);
    rules(s, [
      'Screen unreachable in code today: no hub tile (IconDataset.kt:755-767)',
      'Td-2 fillable only if Td-1 given, else shown as missed; doses timed from LMP (MotherImmunizationListViewModel.kt:31-58)',
      'Given → View, missed → no button, Fill → full Record-vaccine form, not an inline date (Sheet:40-49; BindingUtils.kt:86-106)',
    ]);
  }
  // ---- Row 2
  // 07 Empty states (NEW)
  {
    const s = F('M2.5-07 · Child due list · empty states', 0, 1);
    screenTop(s, 'Child immunization', 'Children born in the last 6 years');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search child or mother name', { filter: true });
    await quickFilters(b, 1, '12', '0');
    stageStrip(b, 7);
    await caption(b, '0 beneficiaries · Scheduled next month · 5–6 y', 'color/on-surface');
    const e1 = kinst('Empty state', null, { Title: 'No match for filters', Body: 'Clear a filter or pick another stage.', Icon: 'filter' }); add(b, e1, false);
    await caption(b, 'Without any filter or search:');
    const e2 = kinst('Empty state', null, { Title: 'No records found', Body: 'Children appear here after birth registration.', Icon: 'vaccine' }); add(b, e2, false);
    rules(s, ['"No match for filters" when a search / quick filter / stage is active, else "No records found" (VM:315-318; Fragment:150-157)', 'Empty state has no button in code']);
  }
  // 08 Due-only mode (NEW)
  {
    const s = F('M2.5-08 · Child due list · Due only (from Home)', 1, 1);
    screenTop(s, 'Immunization due', 'Overdue doses only');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search child or mother name', { filter: true });
    stageStrip(b, 0);
    await caption(b, '3 beneficiaries · Overdue · All', 'color/on-surface');
    await actionCard(b, { name: 'Aarav Kumar', meta: 'Boy · mother Sunita Devi', tags: [['Alert', 'Penta-2 overdue']], rows: [['Child RCH ID', '1234 5678 9020'], ['DOB', '12 Aug 2026']], actions: [['Filled', 'Show vaccines']] });
    await actionCard(b, { name: 'Golu Kumar', meta: 'Boy · mother Rinki Devi', tags: [['Alert', 'MR-2 overdue']], rows: [['Child RCH ID', '1234 5678 4410'], ['DOB', '02 Jul 2024']], actions: [['Filled', 'Show vaccines']] });
    rules(s, ['Opened from Home scheduler with showDueOnly: only children with an OVERDUE dose (VM:40-41, 257-260; SchedulerFragment.kt:107)']);
  }
  // 09 Vaccine sheet (code layout) + missed toast (NEW)
  {
    const s = F('M2.5-09 · Vaccine sheet · fill / view / missed', 2, 1, true);
    screenTop(s, 'Child immunization', 'Children born in the last 6 years');
    const b = body(s); recordCard(b, { name: 'Aarav Kumar', meta: 'Boy · DOB 12 Aug 2026', tag: 'Penta-2 overdue', tone: 'Alert', action: 'Show vaccines' });
    scrim(s);
    const sh = sheet(s, 'Vaccines');
    sh.appendChild(await txt('Aarav Kumar · vaccines', 'Title/Medium', 'color/on-surface'));
    await catHead(sh, 'At birth');
    await vaccRow(sh, 'BCG', '12 Aug 2026', 'given');
    await vaccRow(sh, 'Hep B-0', '13 Aug 2026', 'missed');
    await catHead(sh, '10 weeks');
    await vaccRow(sh, 'Penta-2', '21 Oct 2026', 'overdue');
    await catHead(sh, '14 weeks');
    await vaccRow(sh, 'Penta-3', '18 Nov 2026', 'pending');
    pinBottom(s, sh);
    snack(s, 'Error', { Message: 'Immunization cannot be done as it is missed', Action: 'OK' });
    rules(s, [
      'Bottom sheet grouped by stage; every dose shows "Due: dd-mm-yyyy" (rv_item_imm_vaccine.xml:75-79)',
      'Given → View (read-only, M2.5-12); Pending / Overdue → Fill (M2.5-05); Missed → no button (BindingUtils.kt:99-105)',
      'Tap a missed dose → toast "Immunization cannot be done as it is missed"',
    ]);
  }
  // 10 Vaccine sheet · deceased (NEW)
  {
    const s = F('M2.5-10 · Vaccine sheet · deceased child', 3, 1, true);
    screenTop(s, 'Child immunization', 'Children born in the last 6 years');
    const b = body(s); recordCard(b, { name: 'Baby of Rani', meta: 'Girl · DOB 24 Oct 2026', tag: 'Deceased', tone: 'Neutral', action: 'Show vaccines' });
    scrim(s);
    const sh = sheet(s, 'Vaccines · deceased');
    sh.appendChild(await txt('Baby of Rani · vaccines', 'Title/Medium', 'color/on-surface'));
    tag(sh, 'Neutral', 'Deceased');
    await catHead(sh, 'At birth');
    await vaccRow(sh, 'BCG', '24 Oct 2026', 'given');
    await vaccRow(sh, 'OPV-0', '08 Nov 2026', 'pending', true);
    await catHead(sh, '6 weeks');
    await vaccRow(sh, 'Penta-1', '05 Dec 2026', 'pending', true);
    pinBottom(s, sh);
    rules(s, ['Deceased child: all "Fill" buttons disabled (rv_item_imm_vaccine.xml:95, 154; Sheet:63-73)']);
  }
  // 11 MCP incentive pop-up (NEW)
  {
    const s = F('M2.5-11 · Record vaccines · MCP upload reminder', 4, 1, true);
    screenTop(s, 'Record vaccines', 'Aarav Kumar · 10-week doses', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Date of vaccination *', '02 Nov 2026');
    await label(b, 'MCP card (2 photos)');
    const ph = AL('HORIZONTAL', 'Photos'); gap(ph, 'spacing/sm'); add(b, ph); ph.appendChild(kinst('Photo tile', 'State=Empty')); ph.appendChild(kinst('Photo tile', 'State=Empty'));
    await dialog(s, { ic: 'info', icColor: 'color/primary', title: 'Upload MCP card?', body: 'Do you want to upload the MCP card (immunization page) photo to claim your incentive?', cancel: 'Skip & save', ok: 'Upload' });
    rules(s, ['Submit with no MCP photo → reminder pop-up; skip saves anyway (ImmunizationFormFragment.kt:120-128)', 'Mitanin: no pop-up, saves directly (:121)']);
  }
  // 12 View mode (NEW)
  {
    const s = F('M2.5-12 · Recorded dose · view mode', 5, 1);
    screenTop(s, 'Record vaccines', 'Aarav Kumar · 6-week doses', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Given 23 Sep 2026', editable: false, reason: 'Recorded doses cannot be changed' });
    await infoCard(b, 'Vaccination', [['Vaccines', 'OPV-1, Penta-1, Rota-1, fIPV-1'], ['Date of vaccination', '23 Sep 2026'], ['Vaccinated place', 'VHND · Anganwadi centre'], ['Vaccinated by', 'ANM']]);
    const ph = AL('HORIZONTAL', 'Photos'); gap(ph, 'spacing/sm'); add(b, ph); ph.appendChild(kinst('Photo tile', 'State=Captured')); ph.appendChild(kinst('Photo tile', 'State=Captured'));
    rules(s, ['Existing record opens read-only, Submit hidden; Edit FAB is commented out in code (ImmunizationFormFragment.kt:164-191)', 'Tap photo → view uploaded document']);
  }
  // ---- Row 3 (Mitanin addendum)
  // 13 Record vaccines · Mitanin (NEW)
  {
    const s = F('M2.5-13 · Record vaccines · Mitanin', 0, 2);
    screenTop(s, 'Record vaccines', 'Aarav Kumar · 6-week doses', 'more');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Mitanin builds only', 'No MCP card upload and no upload reminder.', null);
    date(b, 'Date of vaccination *', '23 Sep 2026');
    await label(b, 'Which vaccines were given today? *');
    add(b, kinst('Checkbox option', 'Checked=true', { Label: 'Select all' }));
    for (const v of ['OPV-1', 'Penta-1', 'Rota-1', 'fIPV-1', 'PCV-1']) add(b, kinst('Checkbox option', 'Checked=true', { Label: `${v} · due 23 Sep` }));
    select(b, 'Vaccinated place *', 'VHND · Anganwadi centre'); select(b, 'Vaccinated by *', 'ANM');
    bottomBar(s, null, 'Save 5 doses');
    rules(s, [
      'Mitanin: MCP card 1 & 2 removed from the form (ImmunizationDataset.kt:139-142); upload taps ignored (ImmunizationFormFragment.kt:175)',
      'Mitanin: Submit saves directly, no MCP reminder pop-up (:121)',
      'Mitanin: PCV vaccines appear in the stage list (ImmunizationRepo.kt:303-311)',
    ]);
  }
  // 14 Vaccination card · Mitanin (NEW)
  {
    const s = F('M2.5-14 · Child vaccination card · Mitanin', 1, 2);
    screenTop(s, 'Aarav Kumar', 'Boy · born 12 Aug 2026 · 11 weeks', 'more');
    const b = body(s); b.itemSpacing = 12;
    await stageCard(b, '6 weeks', 'Done', [['OPV-1', 'given', 'Given 23 Sep · View'], ['Penta-1', 'given', 'Given 23 Sep · View'], ['Rota-1', 'given', 'Given 23 Sep · View'], ['fIPV-1', 'given', 'Given 23 Sep · View'], ['PCV-1', 'given', 'Given 23 Sep · View']]);
    await stageCard(b, '14 weeks', 'Due 18 Nov · can fill now', [['OPV-3', 'due', 'Due 18 Nov'], ['Penta-3', 'due', 'Due 18 Nov'], ['Rota-3', 'due', 'Due 18 Nov'], ['fIPV-2', 'due', 'Due 18 Nov'], ['PCV-2', 'due', 'Due 18 Nov']]);
    await stageCard(b, '9–12 months', 'From 12 May 2027', [['MR-1', 'next', 'Due 12 May'], ['JE-1', 'next', 'Due 12 May'], ['PCV-Booster', 'next', 'Due 12 May'], ['Vit A-1', 'next', 'Due 12 May']]);
    rules(s, ['Mitanin only: PCV-1, PCV-2, PCV-Booster are kept from the server vaccine list; other flavours drop them (ImmunizationRepo.kt:303-311)']);
  }

  const endY = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, endY, 900,
    'IMMUNIZATION NOTES\n' +
    '• Stages in code: Birth, 6 wk, 10 wk, 14 wk, 9–12 m, 16–24 m, 5–6 y (10 y, 16 y, Catch-up removed, FLW-1144). Vaccine names/ages come from the VACCINE table; the UIP schedule here is sample data.\n' +
    '• Vaccine sheet (09) is where doses are seen and opened: given = View, pending / overdue = Fill, missed = no button. Card view (04) is the redesign layout of the same data.\n' +
    '• Record form (05) records several doses with one date/place; unticked doses stay due.\n' +
    '• Early entry for QA is debug/UAT only (earlyEntryEnabled).\n' +
    '• Mitanin: no MCP upload / reminder (13); PCV vaccines only on Mitanin (14).\n' +
    '• Gamification: completing birth–12 m doses → "Child Fully Protected" (tiers 3 / 10 / 25 / 60 children).');
  fitSection(sec);
  return ['2.5 screens: ' + frames.length];
}
