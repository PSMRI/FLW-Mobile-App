// ---------------------------------------------------------------------------
// Stage 2.7 · NCD + TB
// Sources: ui/home_activity/non_communicable_diseases/* (cbac, ncd_* lists, ncd_referred, tb_*, tpt, vulnerable_population),
// fragment_cbac.xml strings, TBScreeningDataset, SuspectedTBDataset, TBConfirmedDataset
// ---------------------------------------------------------------------------
async function buildStage27(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureSearchMic(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { actionCard, dialog, visitSheet, optionSheet, sourceSheet, searchRow, viewMode, blockedAdd, uploads, table, rules, caption, tag, btn, iconBtn, row: gRow } = G;
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack, overlayCenter } = S;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.7 · NCD & TB');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };
  const partTabs = async (p, parts, active) => {
    const r = AL('HORIZONTAL', 'Parts'); r.itemSpacing = 6; add(p, r);
    for (const [i, l] of parts.entries()) { const c = AL('HORIZONTAL', l); c.primaryAxisAlignItems = 'CENTER'; c.counterAxisAlignItems = 'CENTER'; c.paddingTop = c.paddingBottom = 8; rad(c, 'radius/sm');
      fill(c, i < active ? 'color/success-container' : i === active ? 'color/primary' : 'color/surface-container-high'); r.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      c.appendChild(await txt(l, 'Label/Medium', i === active ? 'color/on-primary' : 'color/on-surface')); }
  };
  // Local: filled button tinted by record state (code colours: red = not started, orange = partial, green = done)
  const TONE = { red: 'color/error', orange: 'color/warning', green: 'color/success' };
  const stateBtn = (p, label, tone, disabled) => {
    const b = btn(p, 'Filled', label, !!disabled); const v = TONE[tone];
    if (v && !disabled) { const bg = b.findOne((n) => n.name === 'Container' || n.name === 'Background'); if (bg && 'fills' in bg) bg.fills = [paint(v)]; else b.fills = [paint(v)]; }
    return b;
  };
  // Local: actionCard + state-coloured buttons [[label, tone, disabled?]] + caption after the buttons
  const hcard = async (p, d, btns = [], after) => {
    const c = await actionCard(p, d);
    if (btns.length && d.state !== 'deceased' && d.state !== 'deleted') { const ar = gRow(c, 'Actions', 8); ar.layoutWrap = 'WRAP'; ar.counterAxisSpacing = 8; for (const [l, t, dis] of btns) stateBtn(ar, l, t, dis); }
    if (after) await caption(c, after);
    return c;
  };
  // Local: filter icon + selected-filter label row (code: ivFilter + tvSelectedFilter)
  const filterLabel = async (p, text) => { const r = gRow(p, 'Selected filter', 8); r.appendChild(icon('filter', 'color/primary', 20)); const t = await txt(text, 'Label/Large', 'color/primary'); r.appendChild(t); return r; };
  const scoreBar = async (s, score, tone, tagLabel, btn) => {
    const sc = AL('VERTICAL', 'Score bar'); pad(sc, 'spacing/md', 'spacing/sm'); gap(sc, 'spacing/xs'); fill(sc, 'color/surface-container'); add(s, sc);
    sc.strokes = [paint('color/outline-variant')]; sc.strokeTopWeight = 1; sc.strokeBottomWeight = 0; sc.strokeLeftWeight = 0; sc.strokeRightWeight = 0;
    const r = AL('HORIZONTAL'); r.counterAxisAlignItems = 'CENTER'; gap(r, 'spacing/sm'); add(sc, r);
    const v = AL('VERTICAL'); r.appendChild(v); v.appendChild(await txt('CBAC score', 'Label/Medium', 'color/on-surface-variant')); v.appendChild(await txt(String(score), 'Number/Large', tone === 'Alert' ? 'color/error' : 'color/success'));
    r.appendChild(inst('tag', `Tone=${tone}`, { Label: tagLabel }));
    add(sc, inst('button', 'Style=Filled, State=Enabled', { Label: btn, 'Show icon': false }));
  };

  // 01 Hub
  {
    const s = F('M2.7-01 · NCD & TB hub', 0, 0);
    screenTop(s, 'NCD & TB', 'People 30+ and TB care');
    const b = body(s);
    await stat(b, [['38', 'CBAC due', 'care'], ['11', 'High score (> 4)', 'disease'], ['3', 'TB on treatment', 'records']]);
    add(b, inst('sectionHeader', null, { Title: 'NCD screening', 'Show action': false }));
    tileGrid(b, [['care', 'NCD list (30+)', '612 people', 'group'], ['care', 'CBAC due', '38 eligible', 'form'], ['disease', 'Priority (score > 4)', '11 people', 'warning'], ['care', 'HWC referrals', '6 to follow up', 'send']]);
    add(b, inst('sectionHeader', null, { Title: 'Tuberculosis', 'Show action': false }));
    tileGrid(b, [['records', 'TB screening', '24 to screen', 'search'], ['disease', 'Suspected TB', '2 people', 'warning'], ['records', 'TB on treatment', '3 people', 'hospital'], ['records', 'TPT (preventive)', '5 contacts', 'shield']]);
    tileGrid(b, [['couple', 'Vulnerable population', '41 people', 'group']]);
  }
  // 02 NCD list with tabs (CBAC due = NcdEligibleListFragment)
  {
    const s = F('M2.7-02 · NCD list · CBAC due / priority', 1, 0);
    screenTop(s, 'NCD screening', 'Age 30+ · not pregnant');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'CBAC due', 'Count 1': '38', 'Label 2': 'Priority', 'Count 2': '11', 'Label 3': 'Not eligible', 'Count 3': '24' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or HH ID');
    const fr = gRow(b, 'Filters', 8); chips(fr, ['All', 'Screened', 'Not screened'], 0); const age = select(fr, 'Age', '35+'); age.layoutSizingHorizontal = 'FIXED'; age.resize(110, age.height);
    await hcard(b, { name: 'Kamla Devi', meta: 'Female · 61 yrs · HH 1023', sync: 'Synced', tags: [['Success', 'Screened']], rows: [['Last CBAC', '14 Nov 2025'], ['Score', '6']] }, [['Add CBAC', 'red', true], ['View CBAC', 'green']], 'Add CBAC blocked: toast "Available on 14 Nov 2026"');
    await hcard(b, { name: 'Ram Prasad', meta: 'Male · 48 yrs · HH 1031', sync: 'Pending', tags: [['Alert', 'Not screened']] }, [['Add CBAC', 'red']]);
    recordCard(b, { name: 'Sita Devi', meta: 'Female · 35 yrs', tag: 'Draft · Part B1', tone: 'Child', action: 'Continue', sync: 'Pending' });
    rules(s, ['List: age ≥ min NCD age, not pregnant, not deactivated, not dead [BenDao.kt:1054]',
      'Chips All / Screened / Not screened + age-band dropdown 35+, 40+ … 80+ (5-year steps) [NcdEligibleListFragment.kt:66-69,134-142]',
      'Add CBAC blocked for 365 days after the last CBAC → toast "Available on <date>" [NcdCbacBenListAdapter.kt:98-111]',
      'View CBAC only when ≥ 1 CBAC saved → history sheet M2.7-14 [rv_item_ncd_cbac.xml:324-334]',
      'Card sync icon = all CBACs synced [rv_item_ncd_cbac.xml:69-78]',
      'Tabs are 3 separate lists in code (hub tiles): Priority / Not eligible are read-only → M2.7-13',
      'Draft / Continue card: not in app today – needs dev (CBAC has no draft)']);
  }
  // 03 CBAC Part A
  {
    const s = F('M2.7-03 · CBAC · Part A risk', 2, 0);
    screenTop(s, 'CBAC', 'Kamla Devi · 61 yrs · female', 'more');
    const b = body(s); b.itemSpacing = 12;
    await partTabs(b, ['A Risk', 'B1 Signs', 'B2 Women', 'B3 Elderly', 'C / D'], 0);
    date(b, 'Date of CBAC *', '14 Nov 2026');
    auto(b, 'Age (auto from profile)', '61 yrs · 3 points');
    await radios(b, 'Do you smoke or use gutka / khaini?', ['Never (0)', 'Used to / sometimes (1)', 'Daily (2)'], 0);
    await seg(b, 'Do you drink alcohol daily? (+1)', ['Yes', 'No'], 1);
    await radios(b, 'Waist (women)', ['80 cm or less (0)', '81–90 cm (1)', 'More than 90 cm (2)'], 2);
    await seg(b, 'At least 150 minutes of activity a week?', ['Yes (0)', 'No (+1)'], 1);
    await seg(b, 'Family history of BP, diabetes or heart disease? (+2)', ['Yes', 'No'], 1);
    await scoreBar(s, 6, 'Alert', 'Score > 4 · refer for NCD screening', 'Next: Part B1');
    rules(s, ['Date of CBAC: min = last CBAC + 365 days (or registration date), max = today [CbacFragment.kt:811-838]',
      'Age is not asked: auto-filled and scored from the beneficiary age [CbacViewModel.kt:239-258]',
      'Waist options by gender: women ≤80 / 81–90 / >90 cm; men ≤90 / 91–100 / >100 cm [CbacFragment.kt:849-856]',
      'Part B2 (women) hidden for male beneficiaries [CbacFragment.kt:851-853]',
      'Part B3 (elderly) shown only for age ≥ 60 [CbacFragment.kt:1239-1242,1343]',
      'Save draft (05): not in app today – needs dev']);
  }
  // 04 CBAC Part B1 + TB popup
  {
    const s = F('M2.7-04 · CBAC · B1 signs + TB alert', 3, 0, true);
    screenTop(s, 'CBAC', 'Kamla Devi · 61 yrs', 'more');
    const b = body(s); b.itemSpacing = 10;
    await partTabs(b, ['A Risk', 'B1 Signs', 'B2 Women', 'B3 Elderly', 'C / D'], 1);
    await checks(b, 'Does the person have any of these? (tick all)', [['Cough for more than 2 weeks *', true], ['Blood in sputum *', false], ['Fever for more than 2 weeks *', true], ['Loss of weight *', false], ['Night sweats *', false], ['Shortness of breath', false], ['None of these', false]]);
    scrim(s);
    const d = kinst('Dialog', null, { Title: 'Possible TB', Body: 'Refer to the Medical Officer and collect a sputum sample. The person will be added to TB screening.' });
    overlayCenter(s, d);
    const btns = d.findAll((n) => n.type === 'INSTANCE' && n.name === 'Button'); if (btns[0]) setP(btns[0], { Label: 'Later' }); if (btns[1]) setP(btns[1], { Label: 'Refer & add' });
    rules(s, ['In code each B1 sign is a Yes/No; "None of these" sets all B1/B2 answers to No, unticking clears them [CbacFragment.kt:394-560]',
      'Dialog title "Presumptive TB Case!" with OK only; 3 messages: sputum (* Yes) / screen family (** Yes) / both → M2.7-16 [CbacFragment.kt:686-726]',
      'After OK (any * Yes): "Refer?" Yes/No dialog → M2.7-17; Later / Refer & add buttons: not in app today – needs dev',
      'TB suspicion: TB screening record auto-created on save [CbacFragment.kt:805-808,281-294]']);
  }
  // 05 CBAC C/D + result
  {
    const s = F('M2.7-05 · CBAC · mood, environment & result', 4, 0);
    screenTop(s, 'CBAC', 'Kamla Devi · 61 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    await partTabs(b, ['A Risk', 'B1 Signs', 'B2 Women', 'B3 Elderly', 'C / D'], 4);
    select(b, 'Fuel used for cooking', 'Firewood / crop residue');
    select(b, 'Occupational exposure', 'None');
    add(b, inst('sectionHeader', null, { Title: 'Last 2 weeks: how often bothered by…', 'Show action': false }));
    await radios(b, 'Little interest or pleasure in doing things', ['Not at all (0)', 'Several days (1)', 'More than half the days (2)', 'Nearly every day (3)'], 1);
    await radios(b, 'Feeling down, depressed or hopeless', ['Not at all (0)', 'Several days (1)', 'More than half the days (2)', 'Nearly every day (3)'], 0);
    const res = AL('VERTICAL', 'Result'); pad(res, 'spacing/md', 'spacing/sm'); gap(res, 'spacing/xs'); rad(res, 'radius/lg'); fill(res, 'color/error-container'); add(b, res);
    res.appendChild(await txt('Refer: 2 findings', 'Title/Medium', 'color/on-error-container'));
    for (const l of ['NCD suspected (score ≥ 4): visit the nearest HWC or call 104', 'Collect sputum sample (a * sign is Yes)', 'Trace all family members (a ** sign is Yes)', 'Refer to MOIC (PHQ-2 score > 3)']) { const t = await txt('• ' + l, 'Body/Small', 'color/on-error-container'); res.appendChild(t); wrapText(t); }
    bottomBar(s, 'Save draft', 'Submit CBAC');
    rules(s, ['Inline result messages: sputum / trace family / MOIC / NCD suspected [CbacFragment.kt:944-968,754-770,1420-1436]',
      'COPD refer prompt when any occupational exposure is chosen [CbacFragment.kt:1375-1384]',
      'Mitanin: exposure list adds "Other sources" → free-text field → M2.7-33 [Konstants.kt:9-14; CbacFragment.kt:1388-1392]',
      'On save (auto flags on the beneficiary): HRP suspected (women status 1–3 with listed signs), TB suspected, NCD priority (score ≥ 4 or PHQ-2) [CbacViewModel.kt:603-676]',
      'Missing answer → dialog naming the first unanswered question → M2.7-20 [CbacViewModel.kt:715-755]',
      'After save: beneficiary marked referred, sync starts, toast "CBAC screening submitted", back to list [CbacFragment.kt:324-334]',
      'Save draft: not in app today – needs dev']);
  }
  // 06 NCD referral follow-up
  {
    const s = F('M2.7-06 · NCD referral & follow-up', 5, 0);
    screenTop(s, 'NCD referral', 'Kamla Devi · score 6', 'more');
    const b = body(s); b.itemSpacing = 12;
    select(b, 'Referred to *', 'HWC Sirsi (CHO)'); date(b, 'Referral date *', '02 Nov 2026');
    auto(b, 'Reason for referral (from caller)', 'NCD suspected · CBAC score 6');
    select(b, 'Additional services *', 'Diabetes & BP check');
    add(b, inst('sectionHeader', null, { Title: 'Follow-up', 'Show action': false }));
    await seg(b, 'Did she visit the HWC?', ['Yes', 'No', 'Not yet'], 0);
    const rv = reveal(b); date(rv, 'Date visited', '05 Nov 2026'); select(rv, 'Diagnosis', 'Hypertension · on treatment');
    bottomBar(s, 'Save draft', 'Save');
    rules(s, ['One shared refer form, reached from CBAC, ANC counselling, TB screening, General check-up [nav_home.xml]',
      'Title: Maternal / TB → "HWC refer form", GC → "Refer to facility", else "NCD refer form" [NCDReferalFormFragment.kt:131-139]',
      'Reason prefilled by the caller; "Additional services" required [ReferalFormDataset.kt:29-45]',
      'Referral date: last 90 days only [ReferalFormDataset.kt:48-56]',
      'Read-only once saved (Submit hidden) [form/NCDReferalFormFragment.kt:52-56]',
      'Submit returns the referral to the caller, which saves itself (TB screening auto-saves) [:76-89]',
      'Follow-up part is a separate server-driven form → M2.7-21 / M2.7-22',
      'Save draft: not in app today – needs dev']);
  }
  // 07 TB screening
  {
    const s = F('M2.7-07 · TB screening', 0, 1);
    screenTop(s, 'TB screening', 'Ram Prasad · 48 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Date of visit *', '02 Nov 2026');
    await seg(b, 'Does the person have symptoms?', ['Symptomatic', 'Asymptomatic'], 0);
    await checks(b, 'Symptoms (tick all)', [['Cough for more than 2 weeks', true], ['Blood in sputum', false], ['Fever for more than 2 weeks', false], ['Loss of weight', true], ['Night sweats', false], ['Loss of appetite', false]]);
    await checks(b, 'Risk factors / key population', [['Age over 60', false], ['Diabetic', false], ['Tobacco user', true], ['BMI below 18.5', false], ['Contact with a TB patient', false], ['TB in last 5 years', false]]);
    select(b, 'HIV status', 'Not known');
    await caption(b, 'Child (under 15): + Failure to gain weight, Decreased activity / playfulness');
    banner(b, 'Warning', 'Presumptive TB', 'Moves to the Suspected TB list. Arrange sputum test and chest X-ray.', null);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Child-only symptoms and gender-based fields [TBScreeningDataset.kt:347,632-633]',
      'Any * / ** symptom Yes → "Refer to HWC facility?" dialog on Submit → M2.7-26 [TBScreeningFormFragment.kt:48-62,180-205]',
      'Read-only after first save: no edit, Submit disabled [TBScreeningFormFragment.kt:88-91]',
      'Opened from TB screening list (M2.7-25) and Vulnerable population (M2.7-11)',
      'Save draft: not in app today – needs dev']);
  }
  // 08 Suspected TB
  {
    const s = F('M2.7-08 · Suspected TB · tests', 1, 1);
    screenTop(s, 'Suspected TB', 'Ram Prasad · 48 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    auto(b, 'Visit (auto)', 'Visit 1 of 2');
    date(b, 'Date of visit *', '06 Nov 2026'); select(b, 'Type of TB case', 'Previously treated');
    const rs = reveal(b); select(rs, 'Reason for suspicion', 'Contact of TB patient');
    await seg(b, 'Chest X-ray done?', ['Yes', 'No'], 0);
    const rv = reveal(b); select(rv, 'X-ray result', 'Suggestive of TB');
    await seg(b, 'Sputum sample collected?', ['Yes', 'No'], 0);
    const rv2 = reveal(b); select(rv2, 'Submitted at', 'DMC, CHC Rajnagar'); text(rv2, 'Nikshay ID', 'NK-2026-77812');
    section(b, 'Closed', 'Visit 2: result', 'Sputum result, TB confirmed, TPT', 2);
    bottomBar(s, 'Save draft', 'Save');
    rules(s, ['Two visits: Visit 1 = date, case type, sputum / where / Nikshay ID; Visit 2 = sputum result, TB confirmed, TPT → M2.7-28 [SuspectedTBDataset.kt:255-310]',
      '"Reason for suspicion" only for case types 2–3 [SuspectedTBDataset.kt:261-263]',
      '"TPT recommended?" only when TB is not confirmed [SuspectedTBDataset.kt:306-308]',
      'Button "Save" in visit 1, "Submit" in visit 2; hidden once visit 2 is complete [TBSuspectedFragment.kt:56-71]',
      'X-ray and DR-TB questions: not in the dataset today – needs dev',
      'Save draft: not in app today – needs dev']);
  }
  // ---- Row 2
  // 09 TB treatment follow-up
  {
    const s = F('M2.7-09 · TB treatment follow-up', 2, 1);
    screenTop(s, 'TB treatment', 'Ram Prasad · Nikshay NK-2026-77812', 'more');
    const b = body(s); b.itemSpacing = 12;
    await infoCard(b, 'Treatment', [['Regimen', 'Drug-sensitive (6 months)'], ['Started', '10 Nov 2026'], ['Expected completion', '10 May 2027']]);
    await label(b, 'Monthly follow-ups');
    const grid = AL('HORIZONTAL', 'Months'); grid.layoutWrap = 'WRAP'; grid.itemSpacing = 8; grid.counterAxisSpacing = 8; add(b, grid);
    for (const [m, st] of [['Nov', 'done'], ['Dec', 'done'], ['Jan', 'due'], ['Feb', ''], ['Mar', ''], ['Apr', '']]) {
      const c = AL('VERTICAL', m); c.counterAxisAlignItems = 'CENTER'; c.primaryAxisAlignItems = 'CENTER'; c.resize(96, 48); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED'; rad(c, 'radius/md');
      fill(c, st === 'done' ? 'color/success-container' : st === 'due' ? 'color/warning-container' : 'color/surface-container-high'); grid.appendChild(c);
      c.appendChild(await txt(m, 'Title/Small', 'color/on-surface'));
    }
    await seg(b, 'Taking medicines regularly?', ['Yes', 'Missed some', 'Stopped'], 0);
    await seg(b, 'Any discomfort / side effects?', ['Yes', 'No'], 1);
    await seg(b, 'Completed the full course?', ['Yes', 'Not yet'], 1);
    await table(b, 'Previous follow-ups', ['#', 'Date'], [['1', '10 Dec 2026'], ['2', '09 Jan 2027']]);
    date(b, 'Follow-up date * (last 90 days)', '08 Feb 2027');
    select(b, 'Treatment outcome', 'Death');
    const dv = reveal(b); date(dv, 'Date of death *', '06 Feb 2027'); select(dv, 'Place of death *', 'Home'); auto(dv, 'Reason (prefilled)', 'Tuberculosis');
    banner(dv, 'Warning', 'Marks her as dead in the whole app', 'Ram Prasad will show as deceased in every list.', null);
    bottomBar(s, 'Save draft', 'Save follow-up');
    rules(s, ['Previous follow-up dates listed above the form [TBConfirmedFormFragment.kt:50-57]',
      'Regimen and treatment start date lock after first save; follow-up date enabled only after first save, last 90 days [TBConfirmedDataset.kt:58-67,219-232]',
      'Outcome = Death → beneficiary marked dead everywhere (date / place / reason prefilled "Tuberculosis") [TBConfirmedViewModel.kt:135-146]',
      'Month grid: not in app today – needs dev (app shows the dated list)',
      'Save draft: not in app today – needs dev']);
  }
  // 10 TPT list
  {
    const s = F('M2.7-10 · TPT for household contacts', 4, 1);
    screenTop(s, 'TB preventive treatment', '5 people recommended');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Who is on this list?', 'People marked "Recommended for TPT" in the Suspected TB form (TB not confirmed).', null);
    searchRow(b, 'Search name or ID');
    await hcard(b, { name: 'Savitri Devi', meta: 'F · 44 yrs · HH 1031', tags: [['Alert', 'TPT not started']] }, [['Start TPT', 'red']]);
    await hcard(b, { name: 'Golu Kumar', meta: 'M · 2 yrs 4 m', sync: 'Pending', tags: [['Child', 'On TPT · 3HP']], rows: [['Follow-up 1', '10 Dec 2026'], ['Follow-up 2', '09 Jan 2027']] }, [['Add follow-up', 'orange']]);
    await hcard(b, { name: 'Pinki Kumari', meta: 'F · 12 yrs', sync: 'Synced', tags: [['Success', 'Completed']], rows: [['Follow-up 1', '05 Aug'], ['Follow-up 2', '04 Sep'], ['Follow-up 3', '06 Oct']] }, [['View details', 'green']]);
    await actionCard(b, { name: 'Mohan Lal', meta: 'M · 67 yrs', state: 'deceased', stateNote: 'Death' });
    rules(s, ['Source = Suspected-TB form "Recommended for TPT" (not household contacts) [TPTListViewModel observeRecommendedTpt]',
      'Start TPT (red) → Add follow-up (orange) → View details (green, after completion) [TPTListAdapter.kt:67-84,156-165]',
      'Card prints "Follow-up n: date" [TPTListAdapter.kt:57-61]',
      'Button hidden if the person died (ben dead or TPT outcome = death); card grey + "Death" tag [:63-65,170-180]',
      'Sync icon = all TPT follow-ups synced [:47-53]']);
  }
  // 11 Vulnerable population
  {
    const s = F('M2.7-11 · Vulnerable population', 5, 1);
    screenTop(s, 'Vulnerable population', '41 people');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID', { filter: true });
    await filterLabel(b, 'Filter: Diabetes');
    await hcard(b, { name: 'Bhagwati Devi', meta: 'F · 78 yrs · HH 1044', tags: [['Alert', 'Age > 60'], ['Alert', 'Diabetes'], ['Neutral', 'Tobacco user']] }, [['Screen', 'red']]);
    await hcard(b, { name: 'Shyam Lal', meta: 'M · 66 yrs · HH 1052', sync: 'Synced', tags: [['Alert', 'Diabetes'], ['Alert', 'Hypertension']] }, [['View screening', 'green']]);
    await actionCard(b, { name: 'Ramkali Devi', meta: 'F · 81 yrs', state: 'deceased', stateNote: 'Death', tags: [['Alert', 'Diabetes']] });
    rules(s, ['Membership = TB key-population risks (TB screening), CBAC risk codes, diabetes / hypertension diagnosis, pregnant, lactating, age > 60, prison [VulnerablePopulationViewModel.kt:35-66]',
      'Each card lists the person\'s risk factors [VulnerableListAdapter.kt:54-60]',
      'Screen (red) / View screening (green) → TB screening form M2.7-07 [:109-111]',
      'Filter icon → risk-factor sheet M2.7-32; chosen filter shown as label',
      'Old chips Elderly alone / Bedridden / Disability: not in app – removed from the list design']);
  }

  // ---- New frames (gap fixes 2026-11-05)
  // 12 Supervisor · NCD screened
  {
    const s = F('M2.7-12 · Supervisor · NCD screened list', 3, 1);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'NCD screened', Subtitle: 'Supervisor · ASHA Sunita Devi' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await actionCard(b, { name: 'Kamla Devi', meta: 'F · 61 yrs · HH 1023', sync: 'Synced', rows: [['Registered', '12 Jan 2024']], icons: [['call', 'Call'], ['id_card', 'ABHA', 'color/primary', true]] });
    await actionCard(b, { name: 'Ram Prasad', meta: 'M · 48 yrs · HH 1031', sync: 'Pending', rows: [['Registered', '03 Mar 2024']], icons: [['call', 'Call'], ['id_card', 'ABHA', 'color/primary', true]] });
    rules(s, ['Only from Supervisor home tile "NCD screened"; ASHA tile commented out [IconDataset.kt:276,718-723]',
      'Every NCD beneficiary, search + Supervisor action-bar title [NcdListFragment.kt:181-185]',
      'Call places a phone call; no permission → toast "Please allow permissions first" [NcdListFragment.kt:92-116]',
      'ABHA icon shown but does nothing (empty handler) [NcdListFragment.kt:88] – code bug',
      'Supervisor also opens NCD priority, HWC referred and TB cases lists read-only (no action buttons)']);
  }
  // 13 Priority / not eligible lists (read-only)
  {
    const s = F('M2.7-13 · NCD priority & not-eligible lists (read-only)', 0, 2);
    screenTop(s, 'NCD screening', 'Latest CBAC score');
    add(s, kinst('Tabs', 'Active=2', { 'Label 1': 'CBAC due', 'Count 1': '38', 'Label 2': 'Priority', 'Count 2': '11', 'Label 3': 'Not eligible', 'Count 3': '24' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or HH ID');
    await actionCard(b, { name: 'Kamla Devi', meta: 'F · 61 yrs · CBAC 14 Nov 2025', sync: 'Synced', tags: [['Alert', 'Score 6']] });
    await actionCard(b, { name: 'Ram Prasad', meta: 'M · 48 yrs · CBAC 02 Oct 2026', sync: 'Synced', tags: [['Alert', 'Score 5']] });
    await caption(b, 'Not eligible tab: same card, latest CBAC score ≤ 4 (e.g. "Score 2").');
    rules(s, ['Priority = latest CBAC score > 4; no action buttons [NcdPriorityListFragment.kt:59-73; RecordsRepo.kt:141-143]',
      'Not eligible = latest CBAC score ≤ 4 (not age-based) [NcdNonEligibleListFragment.kt:51-56; RecordsRepo.kt:146-148]',
      'Priority also opened from Supervisor home with Supervisor action bar [IconDataset.kt:282]']);
  }
  // 14 CBAC history sheet
  {
    const s = F('M2.7-14 · CBAC history sheet', 1, 2, true);
    screenTop(s, 'NCD screening', 'CBAC due · 38');
    await visitSheet(s, { title: 'CBAC history', subtitle: 'Kamla Devi · 61 yrs', visits: [
      { label: 'CBAC', date: 'Filled 14 Nov 2025', status: 'Synced', tone: 'Success', action: 'View' },
      { label: 'CBAC', date: 'Filled 10 Nov 2024', status: 'Synced', tone: 'Success', action: 'View' },
      { label: 'CBAC', date: 'Filled 02 Nov 2023', status: 'Pending', tone: 'Neutral', action: 'View' }] });
    rules(s, ['Newest first; each row = fill date, View, per-record sync icon [NcdBottomSheetFragment.kt:37-60]', 'View → CBAC in read-only mode M2.7-15 (passes cbacId)']);
  }
  // 15 CBAC view mode
  {
    const s = F('M2.7-15 · CBAC · view mode', 2, 2);
    screenTop(s, 'CBAC', 'Kamla Devi · 61 yrs · female', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Filled 14 Nov 2025', editable: false, reason: 'A saved CBAC cannot be changed. Next CBAC from 14 Nov 2026.' });
    for (const [l, v] of [['Date of CBAC', '14 Nov 2025'], ['Age (auto)', '61 yrs · 3 points'], ['Tobacco', 'Never (0)'], ['Waist', 'More than 90 cm (2)'], ['Physical activity', 'No (+1)'], ['Cough > 2 weeks *', 'Yes'], ['Fuel for cooking', 'Firewood']]) auto(b, l, v);
    banner(b, 'Error', 'CBAC score 6 · NCD suspected', 'Referred to HWC Sirsi on 14 Nov 2025.', null);
    rules(s, ['cbacId != 0 → whole form read-only, Submit hidden, values restored [CbacFragment.kt:63-65,772-783]']);
  }
  // 16 TB alert · family variant
  {
    const s = F('M2.7-16 · CBAC · TB alert (sputum + family)', 3, 2, true);
    screenTop(s, 'CBAC', 'Kamla Devi · 61 yrs', 'more');
    const b = body(s); b.itemSpacing = 10;
    await partTabs(b, ['A Risk', 'B1 Signs', 'B2 Women', 'B3 Elderly', 'C / D'], 1);
    await seg(b, 'Cough for more than 2 weeks? *', ['Yes', 'No'], 0);
    await seg(b, 'Anyone in the family has TB? **', ['Yes', 'No'], 0);
    await dialog(s, { ic: 'warning', title: 'Presumptive TB Case!', body: 'Refer to HWC and collect the Sputum sample, and inform ANM/MPW to screen all family members.', cancel: null, ok: 'OK' });
    rules(s, ['Message by answers [CbacFragment.kt:686-726]:', '* only → "Refer to HWC and collect the Sputum sample."', '** only → "Refer to HWC and inform ANM/MPW to screen all family members."', 'both → message shown here', '** = family has TB / taking TB drugs']);
  }
  // 17 Refer? dialog
  {
    const s = F('M2.7-17 · CBAC · Refer? dialog (per suspicion)', 4, 2, true);
    screenTop(s, 'CBAC', 'Kamla Devi · 61 yrs', 'more');
    const b = body(s); b.itemSpacing = 10;
    await partTabs(b, ['A Risk', 'B1 Signs', 'B2 Women', 'B3 Elderly', 'C / D'], 1);
    await seg(b, 'Loss of sensation in palm or sole?', ['Yes', 'No'], 0);
    await dialog(s, { ic: 'share', title: 'Leprosy suspected', body: 'Do you want to refer to HWC facility?', cancel: 'No', ok: 'Yes' });
    rules(s, ['Same Yes/No dialog, shown once per CBAC per type [CbacFragment.kt:97-153; CbacViewModel.kt:519-525]:',
      'NCD (score > 4) · TB (after TB alert) · Leprosy (any of 12 skin / nerve signs) · COPD (occupational exposure chosen) · Cancer (lump / bleeding / discharge Yes) · Geriatric / depression (PHQ-2 > 3)',
      'Yes → referral sheet M2.7-18; No → closes',
      'Leprosy signs also auto-create a Leprosy-suspected record [CbacViewModel.kt:972-987]']);
  }
  // 18 In-CBAC referral sheet
  {
    const s = F('M2.7-18 · CBAC · referral sheet', 5, 2, true);
    screenTop(s, 'CBAC', 'Kamla Devi · 61 yrs', 'more');
    scrim(s);
    const sh = sheet(s, 'Sheet · Referral'); sh.counterAxisAlignItems = 'MIN';
    await person(sh, 'KD', 'Kamla Devi', 'F · 61 yrs · HH 1023 · 98765 00112');
    select(sh, 'Refer to facility *', 'HWC Sirsi');
    auto(sh, 'Reason (prefilled)', 'Leprosy suspected');
    select(sh, 'Additional services *', 'Skin examination');
    date(sh, 'Referral date *', '14 Nov 2026');
    btn(sh, 'Filled', 'Submit', false, true);
    pinBottom(s, sh);
    rules(s, ['Bottom sheet 60 % height inside CBAC [CbacFragment.kt:1571-1663]', 'Submit → toast; referral attached to this CBAC', 'Full-screen version for other callers = M2.7-06']);
  }
  // 19 B2 / B3 alerts
  {
    const s = F('M2.7-19 · CBAC · B2 / B3 alerts', 0, 3, true);
    screenTop(s, 'CBAC', 'Kamla Devi · 61 yrs', 'more');
    const b = body(s); b.itemSpacing = 10;
    await partTabs(b, ['A Risk', 'B1 Signs', 'B2 Women', 'B3 Elderly', 'C / D'], 3);
    await seg(b, 'Bleeding after menopause?', ['Yes', 'No'], 0);
    banner(b, 'Warning', 'Inform ASHA facilitator', 'Shown inline under the question (plus an alert when ticked).', null);
    await seg(b, 'Feeling unsteady while standing or walking?', ['Yes', 'No'], 0);
    await dialog(s, { ic: 'warning', title: 'Alert', body: 'Send to MOIC of nearest health centre.', cancel: null, ok: 'OK' });
    rules(s, ['B2 bleeding after menopause = Yes → "Inform ASHA facilitator" alert + inline note [CbacFragment.kt:1206-1219]',
      'B3 (unsteady, needs help, forgetting names …) Yes → "Send to MOIC of nearest health centre" [CbacFragment.kt:623-646]']);
  }
  // 20 Missing-field dialog
  {
    const s = F('M2.7-20 · CBAC · missing answer dialog', 1, 3, true);
    screenTop(s, 'CBAC', 'Ram Prasad · 48 yrs', 'more');
    const b = body(s); b.itemSpacing = 10;
    await partTabs(b, ['A Risk', 'B1 Signs', 'B2 Women', 'B3 Elderly', 'C / D'], 4);
    select(b, 'Fuel used for cooking', 'LPG');
    bottomBar(s, null, 'Submit CBAC');
    await dialog(s, { ic: 'warning', title: 'Missing answer', body: 'Please answer: "Do you drink alcohol daily?"', cancel: null, ok: 'OK' });
    rules(s, ['Names the first unanswered question [CbacFragment.kt:336-341; CbacViewModel.kt:715-755]']);
  }
  // 21 Referral follow-up · visits
  {
    const s = F('M2.7-21 · Referral follow-up · visits & dates', 2, 3);
    screenTop(s, 'NCD follow-up', 'Kamla Devi · 61 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    auto(b, 'Referred on', '14 Nov 2025');
    auto(b, 'Visit (auto)', 'Visit-1 · follow-up 4 of 6');
    await table(b, 'Visit history', ['Visit', 'Diagnosis', 'Follow-up', 'Date'], [['Visit-1', 'Hypertension', 'Start', '20 Nov 2025'], ['', '', '1', '18 Dec 2025'], ['', '', '2', '15 Jan 2026'], ['', '', '3', '12 Feb 2026']]);
    date(b, 'Follow-up date *', '10 Mar 2026');
    banner(b, 'Error', 'Date must be in March 2026', 'Follow-up must be in the month after the last follow-up.', null);
    select(b, 'Treatment status', 'On treatment');
    bottomBar(s, null, 'Save');
    rules(s, ['Server-driven form; different form for NCD, TB referral and General check-up [NCDReferalFormFragment.kt:134-168]',
      'Main visit (treatment start) then up to 6 monthly follow-ups, then a new visit number; label "Visit-n" auto [ViewModel:93-111,248-256]',
      'Follow-up date: after treatment date, exactly in the next calendar month after the last follow-up, not a future month (own error text) [ViewModel:573-611]',
      'Title: "General check-up follow-up" vs "NCD follow-up"; "Referred on" header on TB / GC forms [:312-316,343-354]']);
  }
  // 22 Referral follow-up · view + Edit / locked + photo
  {
    const s = F('M2.7-22 · Referral follow-up · view, Edit & photo', 3, 3, true);
    screenTop(s, 'NCD follow-up', 'Kamla Devi · 61 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Saved 12 Feb 2026', editable: false, reason: 'NCD: Edit FAB unlocks. TB / General check-up: locked once filled (no Save, no Edit).' });
    auto(b, 'Diagnosis', 'Hypertension');
    await uploads(b, 'Prescription photo', [['prescription_feb.jpg', 'Done']], 'Take photo or choose from gallery · max 5 MB');
    const f = inst('fab', null, { Label: 'Edit', Icon: 'edit' }); s.appendChild(f); f.layoutPositioning = 'ABSOLUTE'; f.x = 360 - 16 - f.width; f.y = 800 - 24 - f.height;
    rules(s, ['View mode + Edit FAB that unlocks the form [NCDReferalFormFragment.kt:296-302,319-320,335-336]',
      'TB / GC follow-up: locked once filled, no Edit [:319-320; ViewModel:218-244]',
      'Photo field: Take photo / Choose from gallery; size check (maxSizeMB, default 5) [:142,271-295]']);
  }
  // 23 Referral type filter sheet
  {
    const s = F('M2.7-23 · Referral type filter sheet', 4, 3, true);
    screenTop(s, 'HWC referred', '18 referrals');
    await optionSheet(s, 'Referral type', ['All', 'NCD', 'TB', 'Leprosy', 'Geriatric', 'HRP', 'Maternal'], 2);
    rules(s, ['Grid of types; picking one closes the sheet [NCDReferTypeFilter.kt:36-57]', 'Mitanin: adds "Child" → M2.7-34 [NcdRefferedListViewModel.kt:126-128]']);
  }
  // 24 HWC referred list · button states (complements M2.8-14)
  {
    const s = F('M2.7-24 · HWC referred list · follow-up states', 5, 3);
    screenTop(s, 'HWC referred', '18 referrals');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID', { filter: true });
    await filterLabel(b, 'Filter: TB');
    await hcard(b, { name: 'Kamla Devi', meta: 'NCD · referred 14 Nov 2025', sync: 'Synced', rows: [['Referred from', 'ASHA Sunita Devi'], ['Facility', 'HWC Sirsi']] }, [['View follow-up', 'green']]);
    await hcard(b, { name: 'Shyam Lal', meta: 'Leprosy · referred 02 Nov 2026', rows: [['Referred from', 'ASHA Sunita Devi']] }, [['Start follow-up', 'red']]);
    await hcard(b, { name: 'Ram Prasad', meta: 'Presumptive TB · referred 30 Oct 2026', rows: [['Referred from', 'ASHA Sunita Devi']] }, [['TB suspected follow-up', 'orange']]);
    await actionCard(b, { name: 'Ramkali Devi', meta: 'NCD · referred 01 Sep 2026', state: 'deceased', stateNote: 'Death' });
    rules(s, ['Filter icon + selected label → type sheet M2.7-23 [NcdRefferedList.kt:89-105]; month chips on M2.8-14: not in app – needs dev',
      'Button: Start follow-up (red, none) / View follow-up (green) / TB suspected follow-up [NcdReferListAdapter.kt:103-125]',
      'Presumptive TB opens Suspected TB form (M2.7-08), not the referral follow-up [NcdRefferedList.kt:71-76,166-172]',
      '"Referred from" = logged-in ASHA name [NcdReferListAdapter.kt:61]',
      'Deceased: grey, Death tag, button hidden [:90-95]; Supervisor: read-only [:155-165]']);
  }
  // 25 TB screening list
  {
    const s = F('M2.7-25 · TB screening list (household)', 0, 4);
    screenTop(s, 'TB screening', 'HH 1031 · Ram Prasad');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await hcard(b, { name: 'Ram Prasad', meta: 'M · 48 yrs · Head', sync: 'Synced', rows: [['Nikshay ID', 'NK-2026-77812']] }, [['View screening', 'green']]);
    await hcard(b, { name: 'Savitri Devi', meta: 'F · 44 yrs · Wife', rows: [['Nikshay ID', '—']] }, [['Screen', 'red']]);
    await actionCard(b, { name: 'Ganga Prasad', meta: 'M · 74 yrs · Father', state: 'deceased', stateNote: 'Death' });
    rules(s, ['Household-scoped: opened per household, or from TB confirmed "Family members" (contact tracing) [TBScreeningListViewModel.kt:21-27]',
      'Screen (red) / View screening (green); sync icon only after screening [TbScreeningListAdapter.kt:55,91-93]',
      'Shows Nikshay ID instead of RCH ID [:49-50]; deceased card grey, button hidden [:97-106]']);
  }
  // 26 TB screening · refer dialog
  {
    const s = F('M2.7-26 · TB screening · refer to HWC dialog', 1, 4, true);
    screenTop(s, 'TB screening', 'Ram Prasad · 48 yrs', 'more');
    const b = body(s); b.itemSpacing = 10;
    await checks(b, 'Symptoms', [['Cough for more than 2 weeks *', true], ['Loss of weight', true]]);
    bottomBar(s, null, 'Submit');
    await dialog(s, { ic: 'share', title: 'Presumptive TB Case', body: 'Do you want to refer to HWC facility?', cancel: 'No', ok: 'Yes' });
    rules(s, ['Any * / ** symptom Yes → this dialog [TBScreeningDataset.kt:623-640]',
      'Yes → refer form (M2.7-06, title "HWC refer form") → on return the screening auto-saves',
      'No → next Submit saves without referral [TBScreeningFormFragment.kt:48-62,180-205]']);
  }
  // 27 TB suspected list
  {
    const s = F('M2.7-27 · TB suspected list', 2, 4);
    screenTop(s, 'TB suspected', '2 people');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await hcard(b, { name: 'Ram Prasad', meta: 'M · 48 yrs · HH 1031', sync: 'Synced', rows: [['Nikshay ID', 'NK-2026-77812']] }, [['TB suspected follow-up', 'green']]);
    await hcard(b, { name: 'Raju Yadav', meta: 'M · 39 yrs · HH 1060' }, [['TB suspected follow-up', 'red']]);
    await actionCard(b, { name: 'Ganga Prasad', meta: 'M · 74 yrs', state: 'deceased', stateNote: 'Death' });
    rules(s, ['Button colour: red no record / green record exists [TbSuspectedListAdapter.kt:110-116]',
      'Deceased: button only if a record already exists [:93-100]',
      'Supervisor "TB cases" tile opens this list, button hidden [IconDataset.kt:288-290; :52-56]']);
  }
  // 28 Suspected TB · visit 2 + positive alert
  {
    const s = F('M2.7-28 · Suspected TB · visit 2 + positive alert', 3, 4, true);
    screenTop(s, 'Suspected TB', 'Ram Prasad · 48 yrs', 'more');
    const b = body(s); b.itemSpacing = 10;
    auto(b, 'Visit (auto)', 'Visit 2 of 2');
    auto(b, 'Visit 1 · sputum collected at (locked)', 'DMC, CHC Rajnagar · NK-2026-77812');
    select(b, 'Sputum result *', 'Positive');
    await seg(b, 'Is TB confirmed?', ['Yes', 'No'], 0);
    bottomBar(s, null, 'Submit');
    await dialog(s, { ic: 'warning', title: 'TB suspected', body: 'Sputum test is positive.', cancel: null, ok: 'OK' });
    rules(s, ['Visit-1 fields locked in visit 2 [SuspectedTBDataset.kt:255-310]',
      'Sputum result Positive → alert [TBSuspectedFragment.kt:107-114]',
      'TB confirmed = No → "TPT recommended?" appears',
      'Save writes referral status: TB confirmed / Recommend TPT / Not recommended → feeds TPT list M2.7-10 [TBSuspectedViewModel.kt:118-140]',
      'Submit hidden once result + confirmed + TPT answered']);
  }
  // 29 TB confirmed list
  {
    const s = F('M2.7-29 · TB confirmed list', 4, 4);
    screenTop(s, 'TB confirmed', '3 on treatment');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await hcard(b, { name: 'Ram Prasad', meta: 'M · 48 yrs · NK-2026-77812', sync: 'Synced', rows: [['Treatment started', '10 Nov 2026']] }, [['Follow-up', 'green'], ['Family members', 'orange']]);
    await hcard(b, { name: 'Sushila Devi', meta: 'F · 52 yrs · NK-2026-80021' }, [['Follow-up', 'red'], ['Family members', 'orange']]);
    rules(s, ['Follow-up: red no follow-up yet / green otherwise → M2.7-09 [TbConfirmedListAdapter.kt:110-111]',
      'Follow-up only for role "asha" [:52-56]',
      'Family members (orange) → household TB screening list M2.7-25 for contact tracing [TBConfirmedListFragment.kt:63-79]']);
  }
  // 30 TPT start / follow-up form
  {
    const s = F('M2.7-30 · TPT start / follow-up form', 5, 4);
    screenTop(s, 'TPT', 'Golu Kumar · 2 yrs 4 m', 'more');
    const b = body(s); b.itemSpacing = 12;
    select(b, 'Regimen * (locked after 1st visit)', '3HP');
    date(b, 'TPT start date * (locked)', '12 Nov 2026');
    auto(b, 'Expected completion (auto)', '12 Feb 2027');
    await table(b, 'Follow-up schedule', ['Month', 'Date'], [['Month-1', '10 Dec 2026'], ['Month-2', '09 Jan 2027']]);
    auto(b, 'Follow-up (auto)', 'Month-3');
    date(b, 'Follow-up date *', '08 Feb 2027');
    await seg(b, 'Taking medicines regularly?', ['Yes', 'No'], 0);
    bottomBar(s, null, 'Save');
    rules(s, ['Server-driven TPT form: regimen, start date, monthly follow-up, outcome [TPTStartViewModel.kt:58-160]',
      'Expected completion from regimen: 6H 6 m, 3HP / 3RH 3 m, 4R 4 m, 1HP 1 m … [:505-528]',
      'Follow-up label "Month-n", max 6; regimen + start date lock after first visit [:108-131]',
      'Dates: start ≥ referral follow-up date and ≤ today; one follow-up per month; after previous; completion after last follow-up (toast errors) [:371-411]']);
  }
  // 31 TPT completion
  {
    const s = F('M2.7-31 · TPT completion & outcome', 0, 5);
    screenTop(s, 'TPT', 'Mohan Lal · 67 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Treatment completed?', ['Yes', 'No'], 0);
    date(b, 'Actual completion date *', '04 Feb 2027');
    select(b, 'Outcome *', 'Death');
    const dv = reveal(b); date(dv, 'Date of death *', '02 Feb 2027'); select(dv, 'Place of death *', 'Hospital'); text(dv, 'Reason', 'Heart attack');
    await viewMode(b, { saved: 'after Save', editable: false, reason: 'Save hidden after completion or after 6 follow-ups.' });
    rules(s, ['Death fields only when completed + outcome = death [TPTStartViewModel.kt:180-210]',
      'After completion or 6 follow-ups: form read-only, Save hidden [TPTStartFragment.kt:87-90; VM:354-369]']);
  }
  // 32 Vulnerable filter sheet
  {
    const s = F('M2.7-32 · Vulnerable risk-factor filter', 1, 5, true);
    screenTop(s, 'Vulnerable population', '41 people');
    scrim(s);
    const sh = sheet(s, 'Sheet · Risk factor'); 
    const hd = gRow(sh, 'Head', 8); const t = await txt('Filter by risk factor', 'Title/Large', 'color/on-surface'); hd.appendChild(t); t.layoutSizingHorizontal = 'FILL'; hd.appendChild(icon('close', 'color/on-surface-variant', 22));
    const grid = AL('HORIZONTAL', 'Grid'); grid.layoutWrap = 'WRAP'; grid.itemSpacing = 8; grid.counterAxisSpacing = 8; add(sh, grid);
    const opts = ['All', 'Diabetes', 'Hypertension', 'HIV risk', 'Prison', 'Migrant', 'Tea-garden worker', 'Silica exposure', 'Mine worker', 'Tobacco user', 'Alcohol', 'Malnutrition', 'Age > 60', 'Pregnant', 'Lactating', 'Contact of TB patient', 'Past TB', 'Slum dweller', 'Homeless', '+ ~20 more'];
    opts.forEach((l, i) => grid.appendChild(inst('chip', `Selected=${i === 1}`, { Label: l })));
    pinBottom(s, sh);
    rules(s, ['About 40 key-population risk factors; tap one → applied and sheet closes; label shows the choice [VulnerableFilterBottomSheet.kt:34-50; VulnerablePopulationViewModel.kt:81-133]']);
  }

  // 33 CBAC environment · Mitanin
  {
    const s = F('M2.7-33 · CBAC · environment · Mitanin', 2, 5);
    screenTop(s, 'CBAC', 'Sukhiya Bai · 58 yrs · female', 'more');
    const b = body(s); b.itemSpacing = 12;
    await partTabs(b, ['A Risk', 'B1 Signs', 'B2 Women', 'B3 Elderly', 'C / D'], 4);
    select(b, 'Fuel used for cooking', 'Firewood / crop residue');
    await radios(b, 'Occupational exposure', ['Crop residue burning', 'Burning of garbage – leaves', 'Industries with smoke, gas, dust (brick kilns, glass factories)', 'Other sources'], 3);
    const ov = reveal(b); text(ov, 'Specify other source *', 'Rice mill dust');
    banner(b, 'Info', 'COPD suspected', 'Any exposure chosen → "Refer?" dialog (M2.7-17).', null);
    rules(s, ['Mitanin only: 4th option "Other sources" + free-text field [Konstants.kt:9-14; strings_ncd.xml cbac_type_occupational_exposure_mitanin]',
      'Field shown only when "Other sources" chosen; cleared when changed [CbacFragment.kt:1388-1392]',
      'Other flavours: 3 options, no free text (M2.7-05)']);
  }
  // 34 Referral type filter · Mitanin
  {
    const s = F('M2.7-34 · Referral type filter sheet · Mitanin', 3, 5, true);
    screenTop(s, 'HWC referred', '18 referrals');
    await optionSheet(s, 'Referral type', ['All', 'NCD', 'TB', 'Leprosy', 'Geriatric', 'HRP', 'Maternal', 'Child'], 7);
    rules(s, ['Mitanin only: extra "Child" type [NcdRefferedListViewModel.kt:126-128]']);
  }

  const endY = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, endY, 900,
    'NCD & TB NOTES\n' +
    '• CBAC is split into 5 parts shown as a stepper (A risk, B1 signs, B2 women, B3 elderly, C/D environment + mood). The score bar stays at the bottom and updates live (pattern P6).\n' +
    '• Answers marked * in B1 trigger the TB dialog (fragment_cbac.xml note): refer to HWC and collect sputum; ** = screen family members (M2.7-16).\n' +
    '• Result card lists the referral messages already in strings (visit HWC / call 104, sputum, family, MOIC).\n' +
    '• TB flow: screening list (25) → screening (07) → suspected list (27) → suspected visit 1/2 (08, 28) → confirmed list (29) → treatment (09); TPT list (10) → TPT form (30, 31).\n' +
    '• In code NCD sits under Disease control and TB under Communicable → TB; this hub merges them (layout choice).\n' +
    '• Every list: search mic = voice search; deceased cards grey with "Death" tag and no action; supervisor sees lists read-only.\n' +
    '• Gamification: counts toward "Complete Worker" (active in 3+ health areas each quarter).');
  fitSection(sec);
  return ['2.7 screens: ' + frames.length];
}
