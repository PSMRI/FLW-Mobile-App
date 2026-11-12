// ---------------------------------------------------------------------------
// Stage 2.6 · High risk (HRP) + CHO
// Sources: ui/home_activity/hrp_cases, ui/home_activity/cho/*, HRPPregnantAssess/Track, HRPNonPregnantAssess/Track,
// HRPMicroBirthPlanDataset, BenRegCHODataset, IconDataset.getHrpIconsDataset / getCHOIconDataset
// ---------------------------------------------------------------------------
async function buildStage26(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureSearchMic(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { actionCard, dialog, visitSheet, searchRow, viewMode, table, rules, caption, tag, btn, row: gRow } = G;
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack } = S;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.6 · High risk & CHO');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };
  const riskQs = async (p, qs) => { for (const [q, sel] of qs) await seg(p, q, ['Yes', 'No'], sel); };
  // Local: filled button tinted by record state (code colours: red = not started, orange = partial, green = done)
  const TONE = { red: 'color/error', orange: 'color/warning', green: 'color/success', purple: 'module/couple/fg' };
  const stateBtn = (p, label, tone) => {
    const b = btn(p, 'Filled', label); const v = TONE[tone];
    if (v) { const bg = b.findOne((n) => n.name === 'Container' || n.name === 'Background'); if (bg && 'fills' in bg) bg.fills = [paint(v)]; else b.fills = [paint(v)]; }
    return b;
  };
  // Local: actionCard + state-coloured buttons [[label, tone]] + optional caption after the buttons
  const hcard = async (p, d, btns = [], after) => {
    const c = await actionCard(p, d);
    if (btns.length && d.state !== 'deceased' && d.state !== 'deleted') { const ar = gRow(c, 'Actions', 8); ar.layoutWrap = 'WRAP'; ar.counterAxisSpacing = 8; for (const [l, t] of btns) stateBtn(ar, l, t); }
    if (after) await caption(c, after);
    return c;
  };

  // 01 HRP hub
  {
    const s = F('M2.6-01 · High risk hub', 0, 0);
    screenTop(s, 'High risk cases', 'Pregnant & non-pregnant women');
    const b = body(s);
    await stat(b, [['5', 'HRP pregnant', 'disease'], ['9', 'HR non-pregnant', 'couple'], ['4', 'Follow-ups due', 'maternal']]);
    add(b, inst('sectionHeader', null, { Title: 'Pregnant women', 'Show action': false }));
    tileGrid(b, [['disease', 'Assess high risk', '6 to assess', 'warning'], ['disease', 'Follow-up of HRP', '3 due', 'calendar']]);
    add(b, inst('sectionHeader', null, { Title: 'Non-pregnant women', 'Show action': false }));
    tileGrid(b, [['couple', 'Assess high risk', '12 to assess', 'warning'], ['couple', 'Follow-up of HR cases', '1 due', 'calendar']]);
  }
  // 02 HRP pregnant list (Follow-up tab = HRPPregnantListFragment)
  {
    const s = F('M2.6-02 · HRP pregnant list', 1, 0);
    screenTop(s, 'High-risk pregnant women', '5 confirmed · 3 follow-ups due');
    add(s, kinst('Tabs', 'Active=2', { 'Label 1': 'To assess', 'Count 1': '6', 'Label 2': 'Follow-up', 'Count 2': '5', 'Label 3': 'All', 'Count 3': '42' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID');
    chips(b, ['All', 'BP', 'Anaemia', 'Diabetes']);
    await hcard(b, { name: 'Rekha Yadav', meta: '33 wks · BP 150/96 · Husband Suresh Yadav', sync: 'Synced', tags: [['Alert', 'PIH']], rows: [['Follow-ups', '2 of 4 · last 30 Oct'], ['Next visit', '2nd PMSMA']] }, [['Follow up', 'orange'], ['History', 'green']]);
    await hcard(b, { name: 'Sunita Devi', meta: '24 wks · Hb 8.9 g/dl', sync: 'Pending', tags: [['Alert', 'Severe anaemia']], rows: [['Follow-ups', '1 of 4 · done today']] }, [['History', 'green']], 'Follow up hidden: one already done today');
    await hcard(b, { name: 'Kiran Yadav', meta: '36 wks · age 17', sync: 'Synced', tags: [['Success', '4 of 4 follow-ups done']], rows: [['Last', '3rd PMSMA · 09 Oct']] }, [['History', 'green']], 'Follow up hidden: all 4 visits done');
    await hcard(b, { name: 'Meena Kumari', meta: '20 wks · twins', tags: [['Maternal', 'No follow-up yet']] }, [['Follow up', 'red']]);
    rules(s, ['Follow up: hidden after 4 follow-ups, or if one was already done today [HRPTListAdapter.kt:84-100]',
      'History: shown only after ≥ 1 follow-up → opens history sheet M2.6-13 [HRPPregnantListFragment.kt:70-84]',
      'Button colour = record state: red none / orange some / green done [HRPTListAdapter.kt:119-146]',
      'Chips All/BP/Anaemia/Diabetes: not in app today – needs dev',
      'Search bar mic = voice search (speech fills the box) [ibSearch → sttContract]',
      '"To assess" tab = M2.6-09; Supervisor read-only = M2.6-19']);
  }
  // 03 Assess HR (PW)
  {
    const s = F('M2.6-03 · Assess high risk (pregnant)', 2, 0);
    screenTop(s, 'Assess high risk', 'Anita Devi · 15 weeks', 'more');
    const b = body(s); b.itemSpacing = 12;
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    for (const [l, v] of [['LMP', '18 Jul 2026'], ['EDD', '24 Apr 2027']]) { const x = kinst('Read-only field', null, { Label: l, Value: v }); r.appendChild(x); x.layoutSizingHorizontal = 'FILL'; }
    section(b, 'Open', 'Risk factors', '8 questions', 1);
    await riskQs(b, [['More than 3 deliveries?', 1], ['Last delivery less than 18 months ago?', 0], ['Height below 140 cm?', 1], ['Age below 18 or above 35?', 1], ['Rh negative?', 1], ['Previous delivery at home?', 0], ['Bad obstetric history?', 1], ['Twins / multiple pregnancy?', 1]]);
    section(b, 'Closed', 'Children, physical & obstetric history', '3 sections', 2);
    const res = AL('VERTICAL', 'Result'); pad(res, 'spacing/md', 'spacing/sm'); gap(res, 'spacing/xs'); rad(res, 'radius/lg'); fill(res, 'color/error-container'); add(b, res);
    const rr = AL('HORIZONTAL'); rr.counterAxisAlignItems = 'CENTER'; gap(rr, 'spacing/sm'); add(res, rr);
    rr.appendChild(icon('warning', 'color/error', 24));
    const rt = await txt('High risk · 2 factors', 'Title/Medium', 'color/on-error-container'); rr.appendChild(rt); rt.layoutSizingHorizontal = 'FILL';
    const rd = await txt('Short gap since last delivery; previous home delivery. Add to HRP follow-up and make a micro birth plan.', 'Body/Small', 'color/on-error-container'); res.appendChild(rd); wrapText(rd);
    bottomBar(s, 'Save draft', 'Save & add to HRP');
    rules(s, ['EDD is auto-calculated when LMP is entered (LMP + 280 days) [HRPPregnantAssessFragment getIndexOfEdd]',
      'Read-only after first save: no re-assessment or edit → see M2.6-10 [HRPPregnantAssessFragment.kt:48]',
      'Save draft: not in app today – needs dev (app has one Submit)']);
  }
  // 04 HRP follow-up (PW track)
  {
    const s = F('M2.6-04 · HRP follow-up (pregnant)', 3, 0);
    screenTop(s, 'HRP follow-up', 'Rekha Yadav · 33 weeks', 'more');
    const b = body(s); b.itemSpacing = 12;
    await person(b, 'RY', 'Rekha Yadav', 'Husband Suresh Yadav · Ben ID 1203 4471 · RCH 1234 5678 9012 · 98765 11223', [['Alert', 'HRP']]);
    auto(b, 'Visit (auto)', '2nd PMSMA · visit 3 of 4');
    date(b, 'Date of visit *', '02 Nov 2026');
    await checks(b, 'High-risk conditions (confirm with ANM)', [['Pregnancy induced hypertension', true], ['Severe anaemia', false], ['Gestational diabetes', false], ['Antepartum haemorrhage', false], ['Mal-presentation', false]]);
    const more = await txt('+ 7 more: hypothyroidism, poly/oligohydramnios, HIV/syphilis/Hep B, RD malaria, dengue, filaria', 'Label/Medium', 'color/primary'); add(b, more); more.textAutoResize = 'HEIGHT';
    section(b, 'Open', 'Blood pressure', 'Systolic / diastolic', 2);
    steppers(b, [['Systolic (mm Hg)', '150'], ['Diastolic (mm Hg)', '96']]);
    banner(b, 'Error', 'BP 150/96 is high', 'Refer to PHC today if she has headache, blurred vision or swelling.', 'Refer');
    section(b, 'Open', 'Blood sugar', 'RBS / FBS / PPBS / OGTT', 3);
    await seg(b, 'Test done', ['RBS', 'FBS + PPBS', 'OGTT 75 g'], 0);
    text(b, 'Random blood sugar (mg/dL)', '112');
    section(b, 'Closed', 'Haemoglobin & IFA', 'Hb, IFA given, quantity', 4);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Fixed 4 visits, label auto: 1st ANC → 1st PMSMA → 2nd PMSMA → 3rd PMSMA [HRPPregnantTrackViewModel.kt:100-107]',
      '5th attempt: toast "Tracking is done" and screen closes [HRPPregnantTrackFragment.kt:88]',
      'Date of visit: min = day after last HRP / non-HRP visit (or LMP), max = today [HRPPregnantTrackDataset.kt:316-333]',
      'Opened from History sheet → same form, view only, no Submit [HRPPregnantTrackFragment.kt:45-50]',
      'Header shows husband, Ben ID, RCH ID, mobile [llPatientInformation2]',
      'Save draft: not in app today – needs dev']);
  }
  // 05 Micro birth plan
  {
    const s = F('M2.6-05 · Micro birth plan', 4, 0);
    screenTop(s, 'Micro birth plan', 'Rekha Yadav · EDD 20 Dec 2026', 'more');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'For every pregnant woman', 'Fill this with the family. Share it so everyone knows where to go and whom to call.', 'Share');
    section(b, 'Open', 'Where to go', '4 places', 1);
    select(b, 'Nearest SC / HWC', 'HWC Sirsi'); select(b, 'Nearest 24x7 PHC', 'PHC Sirsi'); select(b, 'Nearest FRU', 'District Hospital'); select(b, 'Nearest USG centre', 'CHC Rajnagar');
    section(b, 'Open', 'Blood', 'Group + 2 donors', 2);
    select(b, 'Blood group', 'B+'); text(b, 'Blood donor 1', 'Ramesh Kumar · 98765 43210'); text(b, 'Blood donor 2', 'Mohan Lal · 91234 56780');
    section(b, 'Open', 'Support & transport', '5 fields', 3);
    text(b, 'Birth companion', 'Kamla Devi (mother-in-law)');
    text(b, 'Who will look after the children', 'Sister-in-law');
    text(b, 'Community member for emergency', 'Ward member Sohan · 99887 76655');
    select(b, 'Transport in labour', '108 ambulance');
    bottomBar(s, 'Save draft', 'Save plan');
    rules(s, ['Saved plan reopens read-only with an Edit FAB; Submit hidden until Edit → M2.6-11 [HRPMicroBirthPlanFragment.kt:45-50,76-78]',
      'Share opens the printable table → M2.6-12 (card "Share" button only after a plan exists)',
      'Save draft: not in app today – needs dev']);
  }
  // 06 HR non-pregnant follow-up
  {
    const s = F('M2.6-06 · HR non-pregnant follow-up', 5, 0);
    screenTop(s, 'High-risk follow-up', 'Kavita Devi · 29 yrs · not pregnant', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Date of visit *', '02 Nov 2026');
    await seg(b, 'Visible signs of anaemia?', ['Yes', 'No'], 0);
    await checks(b, 'Clinical assessment (with ANM)', [['Hypertension', false], ['Diabetes', false], ['Severe anaemia', true]]);
    steppers(b, [['Hb (g/dl)', '9.2'], ['BP systolic', '118']]);
    await seg(b, 'IFA supplement given?', ['Yes', 'No'], 0);
    section(b, 'Open', 'Current FP / pregnancy status', '4 fields', 2);
    select(b, 'Family planning adopted', 'ANTRA injection');
    await seg(b, 'Missed period?', ['Yes', 'No'], 0);
    await seg(b, 'Is she pregnant?', ['Yes', 'No'], 0);
    const pv = reveal(b); date(pv, 'LMP *', '20 Sep 2026'); auto(pv, 'EDD (auto · LMP + 280 days)', '27 Jun 2027');
    banner(pv, 'Info', 'She becomes a pregnant HR case', 'On save she moves to High-risk pregnant · Follow-up. Her risk answers are copied.', null);
    const res = AL('HORIZONTAL', 'Risk status'); res.counterAxisAlignItems = 'CENTER'; pad(res, 'spacing/md', 'spacing/sm'); gap(res, 'spacing/sm'); rad(res, 'radius/lg'); fill(res, 'color/warning-container'); add(b, res);
    res.appendChild(icon('warning', 'color/warning', 22));
    const rc = AL('VERTICAL'); rc.itemSpacing = 2; res.appendChild(rc); rc.layoutSizingHorizontal = 'FILL';
    rc.appendChild(await txt('Risk status: still high risk', 'Title/Small', 'color/on-surface'));
    const rd = await txt('Hb below 10. Next follow-up in 1 month.', 'Body/Small', 'color/on-surface-variant'); rc.appendChild(rd); wrapText(rd);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Is pregnant = Yes → on save: reproductive status = pregnant, a pregnant HR assessment is auto-created with copied risk answers, LMP + EDD set [HRPNonPregnantTrackViewModel.kt:118-150]',
      'LMP: max today, min from last visit; shown only in the pregnancy-status section [HRPNonPregnantTrackDataset.kt:91-99,236-262]',
      'EDD = LMP + 280 days [HRPNonPregnantTrackDataset.kt:524-526]',
      'No visit cap (unlike pregnant: 4); one follow-up per day',
      'Save draft: not in app today – needs dev']);
  }
  // 07 CHO home (role variant)
  {
    const s = F('M2.6-07 · CHO home (role variant)', 0, 1);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Home', { Title: 'Namaste, Dr. Meena', Subtitle: 'CHO · HWC Sirsi · 6 villages' }));
    const b = body(s);
    await stat(b, [['1,860', 'Beneficiaries', 'people'], ['14', 'HRP pregnant', 'disease'], ['22', 'HR non-pregnant', 'couple']]);
    tileGrid(b, [['people', 'All beneficiaries', '1,860', 'group'], ['disease', 'High-risk pregnant', '14 · 5 due', 'pregnant'], ['couple', 'High-risk non-pregnant', '22 · 3 due', 'warning'], ['household', 'Register beneficiary', 'New', 'person_add']]);
    banner(b, 'Info', 'CHO sees a smaller home', 'Only the CHO modules (IconDataset.getCHOIconDataset). Same components and colours as ASHA.', null);
    add(s, inst('bottomNav', 'Active=Modules'));
    rules(s, ['"All beneficiaries" → CHO list M2.6-18 (women of the selected village only)']);
  }
  // 08 CHO register beneficiary
  {
    const s = F('M2.6-08 · CHO register beneficiary', 1, 1);
    screenTop(s, 'Register beneficiary', 'HWC Sirsi', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Date of registration *', '02 Nov 2026');
    text(b, 'Name of beneficiary *', 'Lalita Devi');
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    const d1 = date(r, 'Date of birth', '10 Mar 1996'); d1.layoutSizingHorizontal = 'FILL';
    const a1 = text(r, 'Age (yrs)', '30'); a1.layoutSizingHorizontal = 'FILL';
    auto(b, 'Gender', 'Female (fixed)');
    text(b, "Husband's name", 'Rajesh Kumar'); text(b, 'Contact number *', '98111 22334'); text(b, 'RCH ID', '1234 5678 9033');
    await seg(b, 'Is she pregnant? *', ['Yes', 'No'], 0);
    banner(b, 'Info', 'Next: assess high risk', 'After saving she appears in "High-risk pregnant · to assess".', null);
    bottomBar(s, 'Save draft', 'Register');
    snack(s, 'Success', { Message: 'Beneficiary saved', Action: 'OK' });
    rules(s, ['Always a woman; no household picker – a placeholder household (0) is auto-created [BenRegisterCHOViewModel.kt:85-91,108-119]',
      'Age ↔ DOB sync: type age to derive DOB, or pick DOB to fill age; age 12–99 [BenRegCHODataset.kt:36-44,90-104]',
      'Save → toast "saved"; failure → toast "Something went wrong" [BenRegisterCHOFragment SAVE_SUCCESS/SAVE_FAILED]',
      'Save draft: not in app today – needs dev']);
  }

  // ---- New frames (gap fixes 2026-11-05)
  // 09 HRP pregnant · To assess (PregnantListFragment)
  {
    const s = F('M2.6-09 · HRP pregnant list · To assess', 2, 1);
    screenTop(s, 'High-risk pregnant women', 'Assess · 6 women');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'To assess', 'Count 1': '6', 'Label 2': 'Follow-up', 'Count 2': '5', 'Label 3': 'All', 'Count 3': '42' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID');
    await hcard(b, { name: 'Anita Devi', meta: 'Husband Ramesh · 98111 22001', sync: 'Pending', tags: [['Maternal', 'Not assessed']] }, [['Assess', 'red'], ['Micro birth plan', null]]);
    await hcard(b, { name: 'Pooja Kumari', meta: 'Husband Vijay · 99112 33445', sync: 'Synced', tags: [['Maternal', '5 of 8 factors']], rows: [['LMP', '02 Jun 2026'], ['EDD', '09 Mar 2027']] }, [['Assess', 'orange'], ['Micro birth plan', null]]);
    await hcard(b, { name: 'Rekha Yadav', meta: 'Husband Suresh · 98765 11223', sync: 'Synced', tags: [['Alert', 'High risk']], rows: [['LMP', '14 Mar 2026'], ['EDD', '19 Dec 2026']] }, [['Assess', 'green'], ['Micro birth plan', null], ['Share', 'purple']]);
    rules(s, ['Assess: red = not started, orange = some of the 8 factors answered, green = all 8 [HRPAdapter.kt:158-199]',
      'Micro birth plan → M2.6-05; Share (purple, WhatsApp icon) only after a plan exists → M2.6-12 [HRPAdapter.kt:100-199]',
      'LMP / EDD rows only when the assessment has an LMP [HRPAdapter.kt:67-70]',
      'Supervisor role: same list, all buttons hidden → M2.6-19 [HRPAdapter.kt:57-60]']);
  }
  // 10 Assess (pregnant) · view only
  {
    const s = F('M2.6-10 · Assess high risk (pregnant) · view only', 3, 1);
    screenTop(s, 'Assess high risk', 'Rekha Yadav · 33 weeks', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Saved 14 Jul 2026', editable: false, reason: 'An assessment cannot be changed after it is saved.' });
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    for (const [l, v] of [['LMP', '14 Mar 2026'], ['EDD (auto)', '19 Dec 2026']]) { const x = kinst('Read-only field', null, { Label: l, Value: v }); r.appendChild(x); x.layoutSizingHorizontal = 'FILL'; }
    for (const [l, v] of [['More than 3 deliveries?', 'No'], ['Last delivery less than 18 months ago?', 'Yes'], ['Height below 140 cm?', 'No'], ['Previous delivery at home?', 'Yes']]) auto(b, l, v);
    banner(b, 'Error', 'High risk · 2 factors', 'She is in HRP follow-up.', null);
    rules(s, ['Opens read-only once saved; no Submit, no Edit [HRPPregnantAssessFragment.kt:48]', 'Fields greyed but values visible']);
  }
  // 11 Micro birth plan · saved (view + Edit FAB)
  {
    const s = F('M2.6-11 · Micro birth plan · saved (view + Edit)', 4, 1, true);
    screenTop(s, 'Micro birth plan', 'Rekha Yadav · EDD 19 Dec 2026', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Saved 20 Oct 2026', editable: false, reason: 'Tap Edit to change. Submit appears after Edit.' });
    for (const [l, v] of [['Nearest 24x7 PHC', 'PHC Sirsi'], ['Nearest FRU', 'District Hospital'], ['Blood group', 'B+'], ['Blood donor 1', 'Ramesh Kumar · 98765 43210'], ['Birth companion', 'Kamla Devi (mother-in-law)'], ['Transport in labour', '108 ambulance']]) auto(b, l, v);
    const f = inst('fab', null, { Label: 'Edit', Icon: 'edit' }); s.appendChild(f); f.layoutPositioning = 'ABSOLUTE'; f.x = 360 - 16 - f.width; f.y = s.height - 24 - f.height;
    rules(s, ['Saved plan opens read-only; Edit FAB unlocks all fields and shows Submit [HRPMicroBirthPlanFragment.kt:45-50,76-78]']);
  }
  // 12 Micro birth plan · table & share
  {
    const s = F('M2.6-12 · Micro birth plan · table & share', 5, 1, true);
    screenTop(s, 'Micro birth plan', 'Rekha Yadav', 'more');
    const b = body(s); b.itemSpacing = 12;
    await table(b, 'Micro birth plan · Rekha Yadav', ['Item', 'Detail'], [['EDD', '19 Dec 2026'], ['Nearest SC / HWC', 'HWC Sirsi'], ['24x7 PHC', 'PHC Sirsi'], ['FRU', 'District Hospital'], ['USG centre', 'CHC Rajnagar'], ['Blood group', 'B+'], ['Donors', 'Ramesh 98765 43210 · Mohan 91234 56780'], ['Companion', 'Kamla Devi'], ['Transport', '108 ambulance']]);
    btn(b, 'Filled', 'Share on WhatsApp', false, true);
    snack(s, 'Success', { Message: 'Saved to gallery. Opening WhatsApp…', Action: 'OK' });
    rules(s, ['Opened from card "Share" [PregnantListFragment.kt:87]',
      'Share: screenshot of the table → saved to gallery (toast) → WhatsApp or WhatsApp Business with a caption [HRPMicroBirthPlanTable.kt:208-300]',
      'No WhatsApp installed → toast "WhatsApp not installed"']);
  }
  // 13 HRP pregnant follow-up history sheet
  {
    const s = F('M2.6-13 · HRP follow-up history sheet (pregnant)', 0, 2, true);
    screenTop(s, 'High-risk pregnant women', 'Follow-up · 5');
    await visitSheet(s, { title: 'Follow-up history', subtitle: 'Rekha Yadav · 33 wks', visits: [
      { label: '1st ANC', date: '12 Aug 2026', status: 'Synced', tone: 'Success', action: 'View' },
      { label: '1st PMSMA', date: '09 Sep 2026', status: 'Synced', tone: 'Success', action: 'View' },
      { label: '2nd PMSMA', date: '30 Oct 2026', status: 'Pending', tone: 'Neutral', action: 'View' }] });
    rules(s, ['Lists past HRP follow-ups; View → follow-up form M2.6-04 in view-only mode [HRPPregnantTrackBottomSheet.kt]', 'No add button in the sheet (Follow up is on the card)']);
  }
  // 14 HR non-pregnant · assess list
  {
    const s = F('M2.6-14 · HR non-pregnant · assess list', 1, 2);
    screenTop(s, 'High-risk non-pregnant', 'Assess · 12 women');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID');
    await hcard(b, { name: 'Kavita Devi', meta: '29 yrs · Husband Mukesh · 97771 22334', sync: 'Synced', tags: [['Neutral', 'Not assessed']] }, [['Assess', 'red']]);
    await hcard(b, { name: 'Lalita Devi', meta: '30 yrs · Husband Rajesh · 98111 22334', sync: 'Pending', tags: [['Neutral', 'Partly assessed']] }, [['Assess', 'orange']]);
    await hcard(b, { name: 'Sunaina Kumari', meta: '24 yrs · Husband Amit', sync: 'Synced', tags: [['Alert', 'High risk']] }, [['Assess', 'green']]);
    rules(s, ['Assess colour by completeness: red none / orange partial / green complete [HRNPAdapter.kt:81-130]', 'Green opens the saved assessment read-only (M2.6-15)', 'Search with voice mic']);
  }
  // 15 Assess high risk (non-pregnant)
  {
    const s = F('M2.6-15 · Assess high risk (non-pregnant)', 2, 2);
    screenTop(s, 'Assess high risk', 'Kavita Devi · 29 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    section(b, 'Open', 'Information on children', '8 questions', 1);
    await riskQs(b, [['Number of deliveries more than 3?', 1], ['Last delivery less than 18 months ago?', 0], ['Height below 140 cm?', 1], ['Age below 18 or above 35?', 1], ['Miscarriage / abortion?', 1], ['Home delivery of previous pregnancy?', 0], ['Medical issues in pregnancy or delivery?', 1], ['Past C-section?', 1]]);
    section(b, 'Closed', 'Physical observation · Obstetric history · HR conditions', '3 sections', 2);
    banner(b, 'Error', 'High risk', 'She moves to High-risk non-pregnant · Follow-up.', null);
    bottomBar(s, null, 'Submit');
    rules(s, ['Result: high risk if any risk answer is Yes (dataset.isHighRisk) and sets her HRP status [HRPNonPregnantAssessViewModel.kt:112-120,149]',
      'Read-only after save: reopened from a green Assess, no Submit [HRPNonPregnantAssessFragment.kt:48]']);
  }
  // 16 HR non-pregnant · follow-up list
  {
    const s = F('M2.6-16 · HR non-pregnant · follow-up list', 3, 2);
    screenTop(s, 'High-risk non-pregnant', 'Follow-up · 9 women');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID');
    await hcard(b, { name: 'Kavita Devi', meta: '29 yrs · severe anaemia', sync: 'Synced', tags: [['Alert', 'High risk']], rows: [['Follow-ups', '3 · last 02 Oct']] }, [['Follow up', 'orange'], ['History', 'green']]);
    await hcard(b, { name: 'Sunaina Kumari', meta: '24 yrs · hypertension', sync: 'Pending', tags: [['Alert', 'High risk']], rows: [['Follow-ups', '1 · done today']] }, [['History', 'green']], 'Follow up hidden: already done today');
    await hcard(b, { name: 'Rita Devi', meta: '33 yrs', tags: [['Neutral', 'No follow-up yet']] }, [['Follow up', 'red']]);
    rules(s, ['Follow up hidden if one was done today; no visit cap [HRNPTListAdapter.kt:83-105]', 'History only after ≥ 1 follow-up → sheet M2.6-17 [HRPNonPregnantListFragment.kt:83-86]', 'Follow up → form M2.6-06']);
  }
  // 17 HR non-pregnant history sheet
  {
    const s = F('M2.6-17 · HR non-pregnant follow-up history sheet', 4, 2, true);
    screenTop(s, 'High-risk non-pregnant', 'Follow-up · 9');
    await visitSheet(s, { title: 'Follow-up history', subtitle: 'Kavita Devi · 29 yrs', visits: [
      { label: 'Follow-up 1', date: '04 Aug 2026', status: 'Synced', tone: 'Success', action: 'View' },
      { label: 'Follow-up 2', date: '03 Sep 2026', status: 'Synced', tone: 'Success', action: 'View' },
      { label: 'Follow-up 3', date: '02 Oct 2026', status: 'Synced', tone: 'Success', action: 'View' }] });
    rules(s, ['View → follow-up form M2.6-06 in read-only mode [HRNonPregnantTrackBottomSheet.kt]']);
  }
  // 18 CHO beneficiary list
  {
    const s = F('M2.6-18 · CHO beneficiary list', 5, 2);
    screenTop(s, 'All beneficiaries', 'Village Sirsi · 412 women');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID');
    await actionCard(b, { name: 'Lalita Devi', meta: 'F · 30 yrs · Husband Rajesh Kumar', sync: 'Synced', rows: [['Mobile', '98111 22334'], ['Registered', '02 Nov 2026']], icons: [['call', 'Call']] });
    await actionCard(b, { name: 'Kavita Devi', meta: 'F · 29 yrs · Husband Mukesh', sync: 'Pending', rows: [['Mobile', '97771 22334'], ['Registered', '28 Oct 2026']], icons: [['call', 'Call']] });
    await actionCard(b, { name: 'Pooja Kumari', meta: 'F · 22 yrs · Husband Vijay', sync: 'Synced', rows: [['Mobile', '99112 33445'], ['Registered', '15 Oct 2026']], icons: [['call', 'Call']] });
    const bar = AL('HORIZONTAL', 'Bottom action bar'); pad(bar, 'spacing/md', 'spacing/sm'); fill(bar, 'color/surface-container'); add(s, bar);
    stateBtn(bar, 'Add beneficiary', 'orange').layoutSizingHorizontal = 'FILL';
    rules(s, ['Only female beneficiaries of the selected village [RecordsRepo.kt:120-123]',
      'Add beneficiary (orange) → CHO register M2.6-08 [BenListCHOFragment.kt:118-126]',
      'ABHA check (fetch ABHA → number dialog, else ABHA creation): wired but icon hidden today (showAbha commented) [BenListCHOFragment.kt:105-107,151-170]',
      'Back pops twice (returns to CHO home, skipping the village picker) [BenListCHOFragment.kt:58-66,72-75]']);
  }
  // 19 Supervisor · HRP women (read-only)
  {
    const s = F('M2.6-19 · Supervisor · HRP women (read-only)', 0, 3);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'HRP women', Subtitle: 'Supervisor · ASHA Sunita Devi' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID');
    await actionCard(b, { name: 'Rekha Yadav', meta: 'Husband Suresh · 98765 11223', sync: 'Synced', tags: [['Alert', 'High risk']], rows: [['LMP', '14 Mar 2026'], ['EDD', '19 Dec 2026']] });
    await actionCard(b, { name: 'Pooja Kumari', meta: 'Husband Vijay · 99112 33445', sync: 'Synced', tags: [['Maternal', '5 of 8 factors']] });
    banner(b, 'Info', 'View only', 'Supervisors can see the list but cannot assess, follow up or share.', null);
    rules(s, ['Supervisor home tile "HRP women" opens the same list inside SupervisorActivity [IconDataset.kt:258]',
      'All card buttons hidden for supervisor role [HRPAdapter.kt:57-60]']);
  }

  const endY = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, endY, 900,
    'HIGH RISK & CHO NOTES\n' +
    '• Assess (03) uses the screening pattern P6: each Yes adds a risk factor; the result card updates live and the button adds her to HRP follow-up.\n' +
    '• Follow-up (04, 06) groups the 12 dataset conditions; the first 5 are shown, the rest behind "+ 7 more". BP / sugar values out of range show an inline warning with Refer.\n' +
    '• Micro birth plan (05) is grouped into Where to go / Blood / Support & transport and can be shared with the family.\n' +
    '• CHO role (07–08) reuses every ASHA component; only the module list and greeting change.\n' +
    '• Xushrukha flavour uses Modules.HRP: home shows Households, Beneficiaries, EC, Maternal, HRP only.\n' +
    '• Mitanin: no flavour checks in HRP / CHO screens or datasets (checked 2026-11-05); same design applies.\n' +
    '• Gap fixes (09–19): To-assess list with 3 buttons, view-only states, birth plan table/share, history sheets, non-pregnant lists, CHO list, supervisor read-only.');
  fitSection(sec);
  return ['2.6 screens: ' + frames.length];
}
