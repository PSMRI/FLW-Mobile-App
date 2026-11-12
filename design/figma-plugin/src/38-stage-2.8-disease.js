// ---------------------------------------------------------------------------
// Stage 2.8 · Disease control + General check-up / OP
// Sources: ui/home_activity/disease_control/*, general_op_care, IconDataset (disease/communicable/malaria/leprosy),
// Malaria*, Leprosy*, Filaria, KalaAzar, AESJE datasets, docs/GENERAL_CHECKUP_BRD_ANALYSIS.md (prototype v0.5)
// ---------------------------------------------------------------------------
async function buildStage28(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureSearchMic(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { actionCard, dialog, visitSheet, sourceSheet, searchRow, viewMode, blockedAdd, uploads, table, rules, finish, caption, tag, btn, row } = G;
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack, overlayCenter } = S;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.8 · Disease control & check-up');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };
  // checkbox row with optional module tag / emergency style
  const checkRow = async (p, l, on, tag, danger) => {
    const r = AL('HORIZONTAL', l); r.counterAxisAlignItems = 'CENTER'; pad(r, 'spacing/md', 'spacing/sm'); gap(r, 'spacing/sm'); rad(r, 'radius/md');
    fill(r, on ? (danger ? 'color/error-container' : 'color/primary-subtle') : 'color/surface-container'); stroke(r, on ? (danger ? 'color/error' : 'color/primary') : 'color/outline-variant', on ? 2 : 1); add(p, r);
    const sq = box('Box', 22, 22); sq.cornerRadius = 5; if (on) { fill(sq, danger ? 'color/error' : 'color/primary'); sq.appendChild(icon('check', 'color/on-primary', 18)); } else stroke(sq, 'color/outline', 2); r.appendChild(sq);
    const t = await txt(l, 'Body/Large', danger ? 'color/error' : 'color/on-surface'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL'; t.textAutoResize = 'HEIGHT';
    if (tag) r.appendChild(inst('tag', 'Tone=Neutral', { Label: '→ ' + tag }));
  };
  // ---- local helpers for the gap frames (M2.8-17+)
  // new frames fill the 6-column grid after M2.8-16 (slot 16 = col 4, row 2)
  let slot = 16;
  const NF = (name, fixed) => { const c = slot % 6, r = Math.floor(slot / 6); slot++; return F(name, c, r, fixed); };
  // coloured status pill (member-list buttons: colour carries the case status in the code)
  const statusBtn = async (p, l, colorVar, ic) => {
    const b = AL('HORIZONTAL', 'Status button · ' + l); b.paddingLeft = b.paddingRight = 14; b.paddingTop = b.paddingBottom = 8; b.itemSpacing = 6; b.cornerRadius = 18; b.counterAxisAlignItems = 'CENTER'; fill(b, colorVar); p.appendChild(b);
    if (ic) b.appendChild(icon(ic, 'color/on-primary', 16));
    b.appendChild(await txt(l, 'Label/Large', 'color/on-primary'));
    return b;
  };
  // member card used by all disease member lists: d = {name, meta, status:[label,colorVar,icon], dead, sync, extra:[[style,label]], statusIcon, note}
  const memberCard = async (p, d) => {
    const c = AL('VERTICAL', 'Member card · ' + d.name); pad(c, 'spacing/md', 'spacing/sm'); c.itemSpacing = 8; rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, d.dead ? 'color/outline' : 'color/outline-variant'); add(p, c);
    const top = AL('HORIZONTAL', 'Header'); top.itemSpacing = 12; top.counterAxisAlignItems = 'CENTER'; add(c, top);
    top.appendChild(inst('avatar', null, { Initials: d.name.split(' ').map((w) => w[0]).join('').slice(0, 2) }));
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 2; top.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    col.appendChild(await txt(d.name, 'Title/Medium', 'color/on-surface'));
    const m = await txt(d.meta, 'Body/Small', 'color/on-surface-variant'); col.appendChild(m); wrapText(m);
    if (d.statusIcon) top.appendChild(icon(d.statusIcon[0], d.statusIcon[1], 22));
    if (d.dead) top.appendChild(icon('close', 'color/error', 20));
    if (d.sync) { try { top.appendChild(inst('syncBadge', null, { Status: d.sync })); } catch (e) { /* ignore */ } }
    if (d.dead) { const tr = AL('HORIZONTAL', 'Tags'); tr.itemSpacing = 6; add(c, tr); tag(tr, 'Neutral', 'Deceased'); }
    const ar = AL('HORIZONTAL', 'Actions'); ar.itemSpacing = 8; ar.counterAxisAlignItems = 'CENTER'; ar.primaryAxisAlignItems = 'MAX'; add(c, ar);
    for (const [st, l] of d.extra || []) btn(ar, st, l);
    if (d.status) await statusBtn(ar, d.status[0], d.status[1], d.status[2]);
    if (d.note) await caption(c, d.note);
    if (d.dead) c.opacity = 0.7;
    return c;
  };
  // household card for the shared disease household list
  const hhCard = async (p, { id, head, phone: ph, ward, members, state, mda }) => {
    const acts = [['Tonal', members + ' members']]; if (mda) acts.push(['Outlined', 'Add MDA']);
    return actionCard(p, { name: head, initials: head.split(' ').map((w) => w[0]).join('').slice(0, 2), meta: 'HH ID ' + id + ' · ' + ph + ' · ' + ward, state, stateNote: 'Deleted Record', rows: [['Members', String(members)], ['Head of family', head]], icons: state === 'deleted' ? [] : [['delete', 'Deactivate', 'color/error']], actions: acts });
  };
  // ON/OFF switch row (IRS / mosquito net)
  const switchRow = async (p, l, on) => {
    const r = AL('HORIZONTAL', 'Switch · ' + l); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 12; pad(r, 'spacing/md', 'spacing/sm'); rad(r, 'radius/md'); fill(r, on ? 'color/primary-subtle' : 'color/surface-container'); stroke(r, 'color/outline-variant'); add(p, r);
    const t = await txt(l, 'Body/Large', 'color/on-surface'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL'; t.textAutoResize = 'HEIGHT';
    r.appendChild(await txt(on ? 'ON' : 'OFF', 'Label/Medium', on ? 'color/primary' : 'color/on-surface-variant'));
    const tr = figma.createFrame(); tr.name = 'Track'; tr.resize(52, 32); tr.cornerRadius = 16; fill(tr, on ? 'color/primary' : 'color/surface-container-high'); if (!on) stroke(tr, 'color/outline', 2); r.appendChild(tr);
    const k = figma.createEllipse(); k.name = 'Thumb'; const d = on ? 24 : 16; k.resize(d, d); fill(k, on ? 'color/on-primary' : 'color/outline'); tr.appendChild(k); k.x = on ? 52 - d - 4 : 8; k.y = (32 - d) / 2;
    return r;
  };
  // Android toast (no action), absolute near the bottom of the frame
  const toast = async (s, msg, y = 700) => {
    const t = AL('HORIZONTAL', 'Toast'); t.paddingLeft = t.paddingRight = 16; t.paddingTop = t.paddingBottom = 10; t.cornerRadius = 20; fill(t, 'color/on-surface');
    const m = await txt(msg, 'Body/Medium', 'color/surface'); t.appendChild(m);
    s.appendChild(t); t.layoutPositioning = 'ABSOLUTE'; if (t.width > 312) { m.resize(280, m.height); m.textAutoResize = 'HEIGHT'; } t.x = Math.round((360 - t.width) / 2); t.y = y;
    return t;
  };
  // Edit FAB (shown in view mode when the code allows editing)
  const editFab = (s) => { const f = inst('fab', null, { Label: 'Edit', Icon: 'edit' }); s.appendChild(f); f.layoutPositioning = 'ABSOLUTE'; f.x = 360 - 16 - f.width; f.y = 800 - 24 - f.height; return f; };
  // helper-text date field
  const dateH = (p, l, v, h) => S.tf(p, 'Filled', { Label: l, Value: v, Helper: h, 'Show helper': true, 'Show trailing': true }, 'calendar');
  // empty state without the default button
  const empty = (p, title, bodyText, ic = 'search') => { const e = kinst('Empty state', null, { Title: title, Body: bodyText, Icon: ic }); add(p, e, false); const bt = e.findOne((n) => n.type === 'INSTANCE' && n.name === 'Button'); if (bt) bt.visible = false; return e; };
  // simple spinner
  const spinner = (p) => { const e = figma.createEllipse(); e.name = 'Spinner'; e.resize(40, 40); e.fills = []; e.strokes = [paint('color/primary')]; e.strokeWeight = 4; e.arcData = { startingAngle: 0, endingAngle: Math.PI * 1.5, innerRadius: 0 }; p.appendChild(e); return e; };

  const dayTrack = async (p, title, days, doneN) => {
    await label(p, title);
    const g = AL('HORIZONTAL', title); g.layoutWrap = 'WRAP'; g.itemSpacing = 6; g.counterAxisSpacing = 6; add(p, g);
    for (let i = 1; i <= days; i++) { const c = box('D' + i, 36, 36); c.cornerRadius = 18; fill(c, i <= doneN ? 'color/success' : i === doneN + 1 ? 'color/warning-container' : 'color/surface-container-high'); c.appendChild(await txt(String(i), 'Label/Medium', i <= doneN ? 'color/on-primary' : 'color/on-surface')); g.appendChild(c); }
  };

  // 01 Disease control hub
  {
    const s = F('M2.8-01 · Disease control hub', 0, 0);
    screenTop(s, 'Disease control', 'Check-ups, NCD and infections');
    const b = body(s);
    tileGrid(b, [['care', 'General check-up', '3 to follow up', 'hospital'], ['care', 'Non-communicable diseases', '38 CBAC due', 'favorite'], ['disease', 'Communicable diseases', '6 diseases', 'warning']]);
    banner(b, 'Info', 'Where things live', 'NCD (CBAC, TB) screens are stage 2.7. General check-up is for everyday problems and referrals to HWC.', null);
  }
  // 02 Communicable grid
  {
    const s = F('M2.8-02 · Communicable diseases', 1, 0);
    screenTop(s, 'Communicable diseases', '6 diseases · 9 follow-ups');
    const b = body(s);
    await stat(b, [['4', 'Malaria suspected', 'disease'], ['2', 'Leprosy on MDT', 'records'], ['3', 'TB on treatment', 'care']]);
    tileGrid(b, [['disease', 'Malaria', '4 suspected · 1 confirmed', 'warning'], ['village', 'Kala-azar', '0 cases', 'search'], ['child', 'AES / JE', '0 cases', 'hospital'], ['couple', 'Filaria', '2 cases · MDA due', 'person'], ['records', 'Leprosy', '1 suspected · 2 on MDT', 'hospital'], ['care', 'Tuberculosis (TB)', '2 suspected · 3 on Rx', 'shield']]);
    rules(s, ['Malaria → Malaria sub-hub (M2.8-18). Leprosy → Leprosy sub-hub (M2.8-19). Kala-azar, AES/JE, Filaria → disease household list (M2.8-20) [IconDataset.kt:582-616]',
      'TB → TB hub (M2.8-17, CdFragment), not straight to the TB screening form [IconDataset.kt:617-622]',
      'Mitanin: same 6 tiles, same targets – no flavour check in getCommunicableDiseasesGridDataset / getDiseaseControlDataset [IconDataset.kt:557-622]',
      'code bug: Malaria, Kala-azar, AES/JE, Filaria and Leprosy tiles all show the TB-screening count; TB tile has no count [IconDataset.kt:587-614]. Design shows per-disease counts – agree which number each tile shows']);
  }
  // 03 Malaria screening
  {
    const s = F('M2.8-03 · Malaria screening', 2, 0);
    screenTop(s, 'Malaria', 'Mohan Lal · 42 yrs · M · Visit no. 2', 'more');
    const b = body(s); b.itemSpacing = 12;
    dateH(b, 'Screening date *', '02 Nov 2026', 'Last 90 days up to today.');
    await seg(b, 'Beneficiary status', ['Alive', 'Died'], 0);
    await checks(b, 'Symptoms (tick all)', [['Fever for more than 2 weeks', true], ['Shaking chills', true], ['Headache', true], ['Muscle aches', false], ['Vomiting', false], ['Diarrhoea', false]]);
    auto(b, 'Case status (auto, locked)', 'Suspected');
    await seg(b, 'Test type', ['RDT', 'Slide (Pf / Pv)'], 0);
    const rv = reveal(b); await seg(rv, 'RDT result *', ['Positive', 'Negative'], 0); date(rv, 'Date of test', '02 Nov 2026');
    select(b, 'Referred to', 'PHC Sirsi');
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Multi-visit: each Submit saves a new visit, visit no. +1; header shows "Visit no." [MalariaFormViewModel.kt:165-168]',
      'Case status is auto-set and locked: Suspected if any main symptom = Yes or 2+ other symptoms = Yes, else Not suspected [MalariaFormDataset.kt:590-622]',
      'Test type only shows when case status = Suspected [MalariaFormDataset.kt:178-186]',
      'All dates: last 90 days up to today [MalariaFormDataset.kt:26-27, 47-48]',
      'Existing record opens read-only with earlier visits list → see M2.8-36',
      'Status "Died" marks the beneficiary dead app-wide → confirm M2.8-37',
      '"Save draft" is not in app today – needs dev']);
  }
  // 04 Malaria confirmed treatment
  {
    const s = F('M2.8-04 · Malaria confirmed · treatment', 3, 0);
    screenTop(s, 'Malaria treatment', 'Mohan Lal · Pf positive', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Treatment start date *', '02 Nov 2026'); select(b, 'Treatment given *', 'ACT + Primaquine (Pf)');
    auto(b, 'Parasite type (from slide test)', 'Pf');
    await dayTrack(b, 'Pf · day-wise tracking (3 days)', 3, 1);
    select(b, 'Day *', 'Day 2');
    auto(b, 'Date of completion (auto: start + 3 days)', '05 Nov 2026');
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Pf / Pv picked automatically from the slide-test result; only ONE tracker shows [ConfirmedCaseFollowUpViewModel.kt:87]',
      'Pf → 3 days, Pv → 4 days (M2.8-38); Mitanin: Pv → 14 days (M2.8-51) [MalariaConfirmCasesDataset.kt:54-63, 106-125, 151-159]',
      'Completion date auto = start + 3 (Pf) / + 4 (Pv) days and limited to that range [MalariaConfirmCasesDataset.kt:209-230]',
      'One record per person: "Day N" dropdown + Submit overwrites it; no per-day save. Reopens read-only with Edit FAB [Fragment.kt:39-49]',
      '"Save draft" is not in app today – needs dev']);
  }
  // 05 Leprosy screening
  {
    const s = F('M2.8-05 · Leprosy screening', 4, 0);
    screenTop(s, 'Leprosy screening', 'Sita Devi · 35 yrs · Visit 1', 'more');
    const b = body(s); b.itemSpacing = 12;
    dateH(b, 'Home visit date *', '02 Nov 2026', 'Up to 1 year back. Other dates: last 90 days.');
    await checks(b, 'Check for these signs', [['Pale or reddish patch with less feeling', true], ['Recurrent ulcers on palm or sole', false], ['Tingling / numbness on palm or sole', true], ['Thickened skin', false], ['Nodules on skin', false], ['Difficulty holding objects', false]]);
    banner(b, 'Warning', 'Leprosy suspected', 'Refer for confirmation. She will move to the Leprosy suspected list.', null);
    select(b, 'Referred to *', 'PHC Sirsi (MO)');
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['"Visit N" = visit cycle; a new cycle starts after treatment completes [LeprosyFormDataset.kt:286-291, 325]',
      'Leprosy status auto-set and locked: signs Yes → Suspected, No → Screening [LeprosyFormDataset.kt:445-458]',
      'Home-visit date up to 1 year back; other dates 90 days [LeprosyFormDataset.kt:41-42, 62-63]',
      'Already screened → opens read-only with Edit FAB, no FAB if dead (M2.8-39)',
      'Status "Death" marks the beneficiary dead app-wide (dialog M2.8-37) [LeprosyFormViewModel.kt:121-138]',
      '"Save draft" is not in app today – needs dev']);
  }
  // 06 Leprosy confirmed / MDT
  {
    const s = F('M2.8-06 · Leprosy confirmed · MDT', 5, 0);
    screenTop(s, 'Leprosy · on treatment', 'Ram Kumar · MB', 'more');
    const b = body(s); b.itemSpacing = 12;
    await infoCard(b, 'Treatment', [['Type confirmed', 'Multibacillary (MB)'], ['Treatment start (locked once set)', '15 Jun 2026'], ['Expected completion', '15 Jun 2027 (12 months)']]);
    await table(b, 'Earlier follow-ups', ['Follow-up date', 'Treatment status'], [['15 Jul 2026', 'Ongoing'], ['14 Aug 2026', 'Ongoing'], ['15 Sep 2026', 'Ongoing'], ['13 Oct 2026', 'Ongoing']]);
    dateH(b, 'Follow-up date *', '02 Nov 2026', 'Not before the home visit. Not in the future.');
    await seg(b, 'MDT blister pack received this month?', ['Yes', 'No'], 0);
    select(b, 'Treatment status *', 'Ongoing · month 5 of 12');
    await seg(b, 'New signs or reactions?', ['Yes', 'No'], 1);
    text(b, 'Remarks', 'Taking medicine regularly');
    bottomBar(s, 'Save draft', 'Save visit');
    rules(s, ['Each save adds a NEW follow-up (repeatable monthly); list of earlier follow-ups above [LeprosyConfirmedFromViewModel.kt:170-189]',
      'Treatment start date locked once set [LeprosyConfirmedDataset.kt:656-663]',
      'Treatment end date required when status = Completed; date errors shown as a toast [LeprosyConfirmedDataset.kt:592-653]',
      'Status Completed closes the visit cycle → see M2.8-45',
      'Toasts: "Follow-up saved" / "Failed to save follow-up" [Fragment.kt:107-125]',
      '"Save draft" is not in app today – needs dev']);
  }
  // 07 Filaria + MDA
  {
    const s = F('M2.8-07 · Filaria case & MDA', 0, 1);
    screenTop(s, 'Filaria', 'Shyam Lal · 66 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Home visit date *', '02 Nov 2026');
    await seg(b, 'Suffering from filariasis?', ['Yes', 'No'], 0);
    const rv = reveal(b); await checks(rv, 'Which part of the body?', [['Legs', true], ['Hands', false], ['Scrotum / hydrocele', false], ['Breast', false]]);
    await seg(b, 'MDA medicine given?', ['Yes', 'No'], 0);
    await seg(b, 'Any side effect?', ['Yes', 'No'], 1);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Record exists → read-only with Edit FAB, always (no death check) [FilariaFormFragment.kt:41-50] – see M2.8-42',
      'Child under 2 yrs: "DEC & Albendazole dose" locked to Yes [FilariaFormDataset.kt:163-171, 285-301]',
      'Saved "Other side effect" text is locked when re-opened [FilariaFormDataset.kt:150-156]',
      'Household MDA distribution is a SEPARATE household form ("Add MDA" on the household card, M2.8-40), not this per-person question',
      '"Save draft" is not in app today – needs dev']);
  }
  // 08 Kala-azar / AES-JE (same pattern)
  {
    const s = F('M2.8-08 · Kala-azar (AES/JE same pattern)', 1, 1);
    screenTop(s, 'Kala-azar', 'Raju Paswan · 23 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Visit date *', '02 Nov 2026');
    await seg(b, 'Beneficiary status', ['Alive', 'Died'], 0);
    select(b, 'Case status *', 'Suspected');
    await seg(b, 'Kala-azar RDT', ['Positive', 'Negative', 'Not done'], 2);
    select(b, 'Referred to', 'CHC Rajnagar'); date(b, 'Follow-up date', '09 Nov 2026');
    banner(b, 'Info', 'AES / JE uses the same form', 'Visit date, status, case status date, follow-up and referral (AESJEFormDataset).', null);
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Record exists → read-only, Submit hidden, Edit FAB; FAB hidden once the person is dead (M2.8-39) [KalaAzarFormFragment.kt:40-52, AEFFormFragment.kt:39-52]',
      'Status "Death" marks the beneficiary dead app-wide; toast "Beneficiary marked as death" (M2.8-37) [KalaAzarFormViewModel.kt:128-145, AESFormViewModel.kt:117-134]',
      'Visit and death dates: last 90 days up to today [KalaAzarFormDataset.kt:21-22, 42-43, 112-113; AESJEFormDataset.kt:21-22, 42-43]',
      '"Save draft" is not in app today – needs dev']);
  }
  // ---- Row 2: General check-up (BRD v0.5)
  // 09 GC list
  {
    const s = F('M2.8-09 · General check-up list', 2, 1);
    screenTop(s, 'General check-up', 'All beneficiaries');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search by name or ID');
    for (const [n, m, e] of [['Kamla Devi', 'W/o Ram · 61 yrs · female', '2 earlier check-ups'], ['Raju Paswan', 'S/o Shyam · 23 yrs · male', 'No earlier check-ups']]) {
      const c = recordCard(b, { name: n, meta: m, tag: e, tone: 'Neutral', action: 'Add check-up' });
    }
    rules(s, ['Search bar has voice search (mic) [GeneralCheckupListFragment.kt:32-37, 72]',
      'Dead beneficiaries are excluded from this list [GeneralCheckupListViewModel.kt:25]',
      'Meta shows husband\'s name (woman over 15 with spouse), else father\'s name [GeneralCheckupListAdapter.kt:31-37]',
      'Empty list → "No records found" empty state (M2.8-22)']);
  }
  // 10 Add check-up form
  {
    const s = F('M2.8-10 · Add check-up', 3, 1);
    screenTop(s, 'Add check-up', 'Kamla Devi · 61 yrs · F · 7012 4455 0101', 'more');
    const b = body(s); b.itemSpacing = 12;
    section(b, 'Closed', 'Earlier check-ups (2)', 'Latest 4 Oct');
    S.tf(b, 'Filled', { Label: 'Date of check-up *', Value: '02 Nov 2026', Helper: 'Today or up to one month back. Not a future date.', 'Show helper': true, 'Show trailing': true }, 'calendar');
    select(b, 'Type of problem *', 'Fever, cough and common illness');
    await label(b, 'What is the problem? *');
    await checkRow(b, 'Fever', true); await checkRow(b, 'Cough', true); await checkRow(b, 'Headache', false); await checkRow(b, 'Loose motions (diarrhoea)', false);
    await checkRow(b, 'Other', false);
    const hint = await txt('Select one or more. A tag on the right means another module handles it.', 'Body/Small', 'color/on-surface-variant'); add(b, hint); hint.textAutoResize = 'HEIGHT';
    banner(b, 'Info', 'Priority: Routine', 'Refer to the HWC within the month.', null);
    bottomBar(s, null, 'Refer to facility');
    rules(s, ['Before a problem type is chosen: pointer links to Communicable Diseases / NCD (M2.8-46) [GeneralCheckupFormFragment.kt:129-139, 153-155]',
      'Form loading / "Form not available" + Try again; Refer disabled until ready (M2.8-47) [Fragment.kt:107-125]',
      '"Other" ticked → "Name the problem *" text field, required, character limit (M2.8-48) [GeneralCheckupFormViewModel.kt:223-235, 382]',
      'Problems owned by two different modules cannot both be ticked → toast (M2.8-48) [ViewModel.kt:183, 203-210]',
      'Validation (no type / no problem / date outside 1 month back to today) → toast + field error [ViewModel.kt:348-370]',
      'Earlier check-ups header: tap to expand/collapse; "No earlier check-ups" when none [Fragment.kt:70-81]',
      'Mitanin: same form (no flavour check in general_checkup/*)']);
  }
  // 11 Emergency + owned option
  {
    const s = F('M2.8-11 · Emergency & owned problems', 4, 1);
    screenTop(s, 'Add check-up', 'Raju Paswan · 23 yrs · M', 'more');
    const b = body(s); b.itemSpacing = 12;
    select(b, 'Type of problem *', 'Emergency');
    await checkRow(b, 'Snake bite', true, null, true); await checkRow(b, 'Very hard to breathe', false, null, true); await checkRow(b, 'Serious injury or accident', false, null, true);
    banner(b, 'Error', 'Priority: Emergency', 'Refer immediately. Call 108 if needed.', 'Call 108');
    add(b, inst('sectionHeader', null, { Title: 'Owned by another module (example)', 'Show action': false }));
    await checkRow(b, 'Pregnant woman with a problem', false, 'Maternal Health');
    await checkRow(b, 'Newborn (under 28 days) is sick', false, 'Child Care');
    bottomBar(s, null, 'Refer to facility');
    rules(s, ['"Call 108" banner action is not in app today – needs dev']);
  }
  // 12 Routing modal
  {
    const s = F('M2.8-12 · Routing & dedup dialogs', 5, 1, true);
    screenTop(s, 'Add check-up', 'Kamla Devi · 61 yrs', 'more');
    const b = body(s); await checkRow(b, 'Cloudy eyesight (may be cataract)', true, 'Cataract referral');
    scrim(s);
    const d = kinst('Dialog', null, { Title: 'Another module handles this', Body: '"Cloudy eyesight" is handled in Cataract referral. Continue there. One episode creates one referral, so no second referral is made from General Check-up.' });
    overlayCenter(s, d);
    const btns = d.findAll((n) => n.type === 'INSTANCE' && n.name === 'Button'); if (btns[0]) setP(btns[0], { Label: 'Stay here' }); if (btns[1]) setP(btns[1], { Label: 'Continue there' });
    const nt = await txt('Dedup dialog: "Already referred this month" → View in HWC Referrals / Cancel (drawn in M2.8-49).', 'Body/Small', 'color/on-primary'); s.appendChild(nt); nt.layoutPositioning = 'ABSOLUTE'; nt.resize(328, 40); nt.textAutoResize = 'HEIGHT'; nt.x = 16; nt.y = d.y + d.height + 16;
    rules(s, ['"Continue in <module>" for Cataract opens All Beneficiaries (the ASHA then picks the eye from the card), not a form directly [GeneralCheckupFormFragment.kt:207-209]',
      'Other owned problems open that module\'s list/form',
      'Duplicate (already referred this month): "View in HWC Referrals" → HWC referred list, or Cancel; can refer again next month [Fragment.kt:215-226]']);
  }
  // 13 Refer to facility
  {
    const s = F('M2.8-13 · Refer to facility', 0, 2);
    screenTop(s, 'Refer to facility', 'Kamla Devi · 7012 4455 0101', 'none');
    const b = body(s); b.itemSpacing = 12;
    auto(b, 'Reason for referral', 'General Check-up: Fever, Cough');
    const pr = AL('HORIZONTAL', 'Priority'); pr.counterAxisAlignItems = 'CENTER'; gap(pr, 'spacing/sm'); add(b, pr);
    pr.appendChild(await txt('Priority', 'Label/Large', 'color/on-surface-variant')); pr.appendChild(inst('tag', 'Tone=Neutral', { Label: 'Routine' }));
    S.tf(b, 'Filled', { Label: 'Referral date *', Value: '02 Nov 2026', Helper: 'Prefilled from the check-up. The incentive counts in the month of this date.', 'Show helper': true, 'Show trailing': true }, 'calendar');
    select(b, 'Referral facility *', 'HWC');
    const hint = await txt('Any of HWC, PHC, CHC, Medical College or District Hospital counts for incentive activity 26.', 'Body/Small', 'color/on-surface-variant'); add(b, hint); hint.textAutoResize = 'HEIGHT';
    bottomBar(s, null, 'Submit referral');
    snack(s, 'Success', { Message: 'Referral submitted. It is in the HWC Referred List.', Action: 'View' });
    rules(s, ['On return from this form the check-up is saved, toast "Referral submitted", back to the list [GeneralCheckupFormFragment.kt:233-267]',
      'Save failure → "Could not save" toast, stays on the form (M2.8-50)']);
  }
  // 14 HWC referred list (all sources)
  {
    const s = F('M2.8-14 · HWC referred list (all sources)', 1, 2);
    screenTop(s, 'HWC referred list', 'All referrals · November 2026');
    const b = body(s); b.itemSpacing = 12;
    add(b, inst('searchBar', null, { Placeholder: 'Search name or ID' }));
    chips(b, ['November 2026', 'October', 'All']);
    recordCard(b, { name: 'Raju Paswan', meta: 'From: General check-up · to District Hospital · 2 Nov', tag: 'EMERGENCY · snake bite', tone: 'Alert', action: 'View follow-up', sync: 'Pending' });
    recordCard(b, { name: 'Kamla Devi', meta: 'From: General check-up · to HWC · 2 Nov', tag: 'Fever, Cough', tone: 'Neutral', action: 'View follow-up' });
    recordCard(b, { name: 'Ram Prasad', meta: 'From: TB screening · to PHC · 30 Oct', tag: 'Higher facility: CHC', tone: 'Success', action: 'View follow-up' });
    recordCard(b, { name: 'Sunita Devi', meta: 'From: ANC · to PHC · 2 Nov', tag: 'Severe anaemia', tone: 'Maternal', action: 'View follow-up' });
  }
  // 15 Follow-up form
  {
    const s = F('M2.8-15 · HWC referral follow-up', 2, 2);
    screenTop(s, 'HWC referral follow-up', 'Kamla Devi · General check-up', 'none');
    const b = body(s); b.itemSpacing = 12;
    auto(b, 'Referred on', '02 Nov 2026');
    S.tf(b, 'Filled', { Label: 'Follow-up date *', Value: '06 Nov 2026', Helper: 'Cannot be before the referral date.', 'Show helper': true, 'Show trailing': true }, 'calendar');
    await radios(b, 'Follow-up status *', ['Treated at HWC', 'Medicines given', 'Not yet visited', 'Referred to higher facility', 'Other'], 3);
    const rv = reveal(b); select(rv, 'Referred to *', 'CHC');
    bottomBar(s, null, 'Save');
  }
  // 16 General OP care
  {
    const s = F('M2.8-16 · General OP care', 3, 2);
    screenTop(s, 'General OP care', 'OPD visits at HWC');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await actionCard(b, { name: 'Bhola Ram', meta: 'Male · 55 yrs · Ben ID 3401 2287', sync: 'Synced', tags: [['Neutral', 'Fever · medicines given']], rows: [['Referred date', '01 Nov 2026'], ['Referred to', 'PHC Sirsi'], ['Follow-up date', '08 Nov 2026'], ['Referred from', 'Village Sirsi'], ['Referral reason', 'Fever 5 days'], ['Registration date', '12 Mar 2024']], icons: [['call', 'Call']], actions: [['Text', 'View']] });
    await actionCard(b, { name: 'Lalita Devi', meta: 'Female · 30 yrs · Ben ID 3401 1902', sync: 'Synced', tags: [['Neutral', 'Skin rash']], rows: [['Referred date', '28 Oct 2026'], ['Referred to', 'HWC Rajnagar'], ['Follow-up date', '04 Nov 2026'], ['Referred from', 'Village Rajnagar'], ['Referral reason', 'Skin rash'], ['Registration date', '02 Jan 2025']], icons: [['call', 'Call']], actions: [['Text', 'View']] });
    rules(s, ['Read-only list: records come down from the server, no open/edit [GeneralOPDAdapter.kt]. "View" button is not in app today – needs dev (confirm if wanted)',
      'Call icon: asks for phone permission if missing; toast "Please allow permissions first" [GeneralOpCareFragment.kt:30-47]',
      'Card shows referred date, referred to, follow-up date, referred from (village), referral reason, registration date [rv_general_opd_ben.xml:254-460]',
      'Sorted newest first (modified / visit / registration date) [GeneralOpCareViewModel.kt:17-21]',
      'Search bar has voice search (mic) [Fragment.kt:13-18, 65-66]',
      'Empty → "No records found" empty state (M2.8-22) [Fragment.kt:55-59]']);
  }

  // =====================================================================
  // Gap frames (2026-11-05, docs/DESIGN_FUNCTIONALITY_GAPS.md · Disease)
  // Navigation in code: disease tile → (sub-hub) → household list → member list → form
  // =====================================================================
  const RED = 'color/error', ORANGE = 'color/warning', GREEN = 'color/success', GREY = 'color/outline', PURPLE = 'module/couple/fg';
  // 17 TB hub
  {
    const s = NF('M2.8-17 · TB hub (Communicable → TB)');
    screenTop(s, 'Tuberculosis (TB)', 'Communicable diseases');
    const b = body(s);
    tileGrid(b, [['care', 'TB screening', '248 households', 'search'], ['disease', 'TB suspected', '2', 'warning'], ['records', 'TB confirmed', '3', 'check_circle'], ['child', 'TPT', '5', 'pill'], ['village', 'Vulnerable population', '14', 'group']]);
    rules(s, ['TB is reached from Communicable diseases → TB (CdFragment), not only from the NCD & TB hub M2.7-01 [IconDataset.kt:782-818]',
      'TB screening tile opens the disease household list (M2.8-20) → members → TB screening form (M2.7-07)',
      'Suspected / confirmed / TPT / vulnerable tiles open the existing TB lists in stage 2.7']);
  }
  // 18 Malaria sub-hub
  {
    const s = NF('M2.8-18 · Malaria sub-hub');
    screenTop(s, 'Malaria', 'Communicable diseases');
    const b = body(s);
    tileGrid(b, [['disease', 'Malaria', '248 households', 'search'], ['records', 'Malaria confirmed', '1 case', 'check_circle']]);
    rules(s, ['2 tiles [IconDataset.kt:821-840]', 'Malaria: count = households → disease household list (M2.8-20) → members (M2.8-23)', 'Malaria confirmed: count = confirmed cases → confirmed list (M2.8-35)']);
  }
  // 19 Leprosy sub-hub
  {
    const s = NF('M2.8-19 · Leprosy sub-hub');
    screenTop(s, 'Leprosy', 'Communicable diseases');
    const b = body(s);
    tileGrid(b, [['records', 'Leprosy screening', '248 households', 'search'], ['disease', 'Leprosy suspected', '1', 'warning'], ['care', 'Leprosy confirmed', '2', 'check_circle']]);
    rules(s, ['3 tiles [IconDataset.kt:531-553]', 'Screening: count = households → household list (M2.8-20) → members (M2.8-31)', 'Suspected → village-wide suspected list (M2.8-33)', 'Confirmed → confirmed list (M2.8-34)']);
  }
  // 20 Disease household list (shared)
  {
    const s = NF('M2.8-20 · Disease household list (shared)');
    screenTop(s, 'Malaria', '248 households');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search HH ID, head of family or phone');
    await hhCard(b, { id: '1023', head: 'Ramesh Kumar', phone: '98XXXXXX21', ward: 'Sirsi ward 4', members: 5 });
    await hhCard(b, { id: '1019', head: 'Shyam Lal Yadav', phone: '97XXXXXX40', ward: 'Sirsi ward 2', members: 3 });
    await hhCard(b, { id: '0987', head: 'Mahesh Paswan', phone: '96XXXXXX11', ward: 'Sirsi ward 1', members: 4, state: 'deleted' });
    rules(s, ['Shared by Malaria, Kala-azar, AES/JE, Filaria, Leprosy and TB screening; toolbar title + icon change with the disease [AllHouseHoldDiseaseControlFragment.kt:170-187]',
      'Search by HH ID, head-of-family name or phone; voice search (mic) [ViewModel.kt:82-96; Fragment.kt:40-44]',
      'Sort: deactivated households last, then newest first [ViewModel.kt:98-101]',
      'Deactivated household: greyed, "Deleted Record" label, grey members button [HouseHoldListAdapter.kt:83-91]',
      '"N members" → that household\'s member list for the current disease (malaria M2.8-23 / AES M2.8-28 / KA M2.8-29 / filaria M2.8-30 / leprosy M2.8-31 / TB) [Fragment.kt:61-118]',
      '"Add beneficiary" button is hidden in disease mode [HouseHoldListAdapter.kt:64, 107]',
      'code bug: deactivate (delete) icon shows on active households but tapping does nothing here [Fragment.kt:131-133] – intended: hide it',
      'Empty → "No records found" (M2.8-22)']);
  }
  // 21 Household list · Filaria variant
  {
    const s = NF('M2.8-21 · Disease household list · Filaria (Add MDA)');
    screenTop(s, 'Filaria', '248 households');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search HH ID, head of family or phone');
    await hhCard(b, { id: '1023', head: 'Ramesh Kumar', phone: '98XXXXXX21', ward: 'Sirsi ward 4', members: 5, mda: true });
    await hhCard(b, { id: '1019', head: 'Shyam Lal Yadav', phone: '97XXXXXX40', ward: 'Sirsi ward 2', members: 3, mda: true });
    rules(s, ['"Add MDA" shows on household cards for Filaria ONLY → household Filaria MDA form (M2.8-40) [HouseHoldListAdapter.kt:63-70; Fragment.kt:123-130]']);
  }
  // 22 Empty state (shared)
  {
    const s = NF('M2.8-22 · Disease lists · empty state');
    screenTop(s, 'Malaria', '0 households');
    const b = body(s); b.counterAxisAlignItems = 'CENTER';
    searchRow(b, 'Search HH ID, head of family or phone');
    empty(b, 'No records found', 'Nothing matches. Check the spelling or clear the search.');
    rules(s, ['Same "No records found" empty state on: disease household list, all disease member lists, leprosy suspected/confirmed lists, malaria confirmed list, general check-up list, General OP care',
      'Leprosy suspected list has NO search bar (hidden in code)']);
  }
  // 23 Malaria household members
  {
    const s = NF('M2.8-23 · Malaria household members');
    screenTop(s, 'Malaria · HH 1023', 'Ramesh Kumar · 5 members');
    const b = body(s); b.itemSpacing = 12;
    await switchRow(b, 'IRS (indoor residual spraying)', false);
    await switchRow(b, 'Mosquito net', false);
    await memberCard(b, { name: 'Ramesh Kumar', meta: 'Head · M · 45 yrs', status: ['Screening', RED], statusIcon: ['warning', RED] });
    await memberCard(b, { name: 'Sunita Devi', meta: 'Wife · F · 40 yrs', status: ['Suspected', ORANGE], statusIcon: ['warning', ORANGE], sync: 'Pending' });
    await memberCard(b, { name: 'Mohan Lal', meta: 'Father · M · 72 yrs', status: ['Malaria confirmed', GREEN], statusIcon: ['check_circle', GREEN], sync: 'Synced' });
    await memberCard(b, { name: 'Pooja Kumari', meta: 'Daughter · F · 17 yrs', status: ['Treatment started', PURPLE], statusIcon: ['pill', PURPLE], sync: 'Synced' });
    await memberCard(b, { name: 'Ravi Kumar', meta: 'Son · M · 14 yrs', status: ['Not confirmed', GREY], sync: 'Synced' });
    await memberCard(b, { name: 'Kalawati Devi', meta: 'Mother · F · 70 yrs', dead: true, note: 'No malaria record → no button' });
    rules(s, ['Status button shows latest case status: Screening (red, none yet), Suspected (orange), Malaria confirmed (green), Not confirmed (grey), Treatment started (purple), Not suspected (green), View [MalariaMemberListAdapter.kt:54-94]',
      'Status icon: mosquito / warning / tick / pill by case status [Adapter.kt:63-87]',
      'Dead member with no malaria record: button hidden, death icon; sync icon only when a record exists [Adapter.kt:54, 133-141]',
      'IRS switch ON → IRS round panel (M2.8-24); Mosquito net ON → net form (M2.8-26). Only one can be open at a time [MalariaSuspectedListFragment.kt:132-178]',
      'Screening / Suspected → screening form (M2.8-03 / M2.8-36)']);
  }
  // 24 IRS round panel
  {
    const s = NF('M2.8-24 · Malaria · IRS round');
    screenTop(s, 'Malaria · HH 1023', 'Ramesh Kumar · 5 members');
    const b = body(s); b.itemSpacing = 12;
    await switchRow(b, 'IRS (indoor residual spraying)', true);
    await switchRow(b, 'Mosquito net', false);
    auto(b, 'IRS date (today, fixed)', '05 Nov 2026');
    select(b, 'IRS round *', 'Round 3');
    btn(b, 'Filled', 'Submit', false, true);
    await table(b, 'IRS rounds this year', ['Sr. no.', 'Round no.'], [['1', 'Round 1'], ['2', 'Round 2']]);
    await toast(s, 'IRS submitted');
    rules(s, ['Date fixed to today; round dropdown pre-filled from the last round [IRSRoundDataSet.kt:26-63]',
      'Submit → toast "IRS submitted", then back [Fragment.kt:241-250]', 'History list: Sr. no. + round no. [IrsRoundListAdapter.kt]',
      'Opening IRS closes the mosquito-net panel and vice versa']);
  }
  // 25 IRS limit
  {
    const s = NF('M2.8-25 · Malaria · IRS limit (4 rounds)');
    screenTop(s, 'Malaria · HH 1019', 'Shyam Lal Yadav · 3 members');
    const b = body(s); b.itemSpacing = 12;
    await switchRow(b, 'IRS (indoor residual spraying)', true);
    auto(b, 'IRS date (today, fixed)', '05 Nov 2026');
    select(b, 'IRS round *', 'Round 4');
    await blockedAdd(b, 'Submit', '4 IRS rounds already done this year for this household');
    await table(b, 'IRS rounds this year', ['Sr. no.', 'Round no.'], [['1', 'Round 1'], ['2', 'Round 2'], ['3', 'Round 3'], ['4', 'Round 4']]);
    rules(s, ['IRS Submit disabled after 4 rounds in a year for the household [MalariaIRSViewModel.kt:92-104; MalariaRepo.kt:606-610]',
      'Reason text under the button is a design addition – code only disables it']);
  }
  // 26 Mosquito net form
  {
    const s = NF('M2.8-26 · Malaria · Mosquito net form');
    screenTop(s, 'Malaria · HH 1023', 'Ramesh Kumar · 5 members');
    const b = body(s); b.itemSpacing = 12;
    await switchRow(b, 'IRS (indoor residual spraying)', false);
    await switchRow(b, 'Mosquito net', true);
    S.tf(b, 'Error', { Label: 'Visit date *', Value: '10 Sep 2026', Helper: 'Must be after the previous visit (12 Sep 2026).', 'Show helper': true, 'Show trailing': true }, 'calendar');
    await seg(b, 'Mosquito net distributed? *', ['Yes', 'No'], 0);
    await uploads(b, 'Photo of net', [['net_hh1023.jpg', 'Done']], 'Camera or file · max 5 MB');
    banner(b, 'Error', 'File too large', 'Choose a photo under 5 MB.', null);
    btn(b, 'Filled', 'Submit', false, true);
    await table(b, 'Mosquito net history', ['Date', 'Net distributed?'], [['12 Sep 2026', 'Yes'], ['15 Mar 2026', 'No']]);
    rules(s, ['Server-driven form opened by the Mosquito net switch [Fragment.kt:159-177, 256-266]',
      'Visit date: not in the future, after the previous visit, not today if today is already filled [Fragment.kt:346-363; MosquitoNetFormViewModel.kt:233-266]',
      'Photo: camera or file, max 5 MB, error if larger (source sheet M2.8-27) [Fragment.kt:61-117, 304-328]',
      'History table: net distributed Yes/No + date [Fragment.kt:274-290]',
      'Toasts: "Data saved successfully" / "Data already exist" [Fragment.kt:399-408]']);
  }
  // 27 Photo source sheet
  {
    const s = NF('M2.8-27 · Mosquito net · photo source', true);
    screenTop(s, 'Malaria · HH 1023', 'Mosquito net');
    await G.optionSheet(s, 'Add photo', [['camera', 'Take photo'], ['image', 'Choose file']], -1, { subtitle: 'Max 5 MB' });
    rules(s, ['Same picker on the Filaria MDA form (M2.8-40)']);
  }
  // 28 AES/JE members
  {
    const s = NF('M2.8-28 · AES/JE household members');
    screenTop(s, 'AES / JE · HH 1023', 'Ramesh Kumar · 5 members');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Is there a herd of pigs near the house?', ['Yes', 'No'], 1);
    await memberCard(b, { name: 'Ramesh Kumar', meta: 'Head · M · 45 yrs', status: ['Register', RED] });
    await memberCard(b, { name: 'Pooja Kumari', meta: 'Daughter · F · 17 yrs', status: ['View', GREEN], sync: 'Synced' });
    await memberCard(b, { name: 'Kalawati Devi', meta: 'Mother · F · 70 yrs', dead: true, note: 'No AES record → no button' });
    rules(s, ['Register (red, no record) / View (green) → AES/JE form [AESMemberListAdapter.kt:77-79]',
      'Dead member with no AES record: button hidden; death icon; sync icon when a record exists',
      'code bug: pig-herd question never shows (title "AES / JE" compared with enum "AES_JE") and the answer is not saved [AESSuspectedListFragment.kt:58-62] – decide: drop or fix']);
  }
  // 29 Kala-azar members
  {
    const s = NF('M2.8-29 · Kala-azar household members');
    screenTop(s, 'Kala-azar · HH 1019', 'Shyam Lal Yadav · 3 members');
    const b = body(s); b.itemSpacing = 12;
    await memberCard(b, { name: 'Shyam Lal Yadav', meta: 'Head · M · 50 yrs', status: ['Register', RED] });
    await memberCard(b, { name: 'Raju Paswan', meta: 'Son · M · 23 yrs', status: ['View', GREEN], sync: 'Pending' });
    await memberCard(b, { name: 'Phoolmati Devi', meta: 'Mother · F · 76 yrs', dead: true, note: 'No record → no button' });
    rules(s, ['Register / View → Kala-azar form (M2.8-08) [KalaAzarMemberListAdapter.kt:91-92]', 'Dead member with no record: button hidden; death + sync icons [Adapter.kt:52, 81-90]']);
  }
  // 30 Filaria members
  {
    const s = NF('M2.8-30 · Filaria household members');
    screenTop(s, 'Filaria · HH 1023', 'Ramesh Kumar · 5 members');
    const b = body(s); b.itemSpacing = 12;
    await memberCard(b, { name: 'Ramesh Kumar', meta: 'Head · M · 45 yrs', status: ['Register', RED] });
    await memberCard(b, { name: 'Shyam Lal', meta: 'Father · M · 66 yrs', status: ['View', GREEN], sync: 'Synced' });
    await memberCard(b, { name: 'Kalawati Devi', meta: 'Mother · F · 70 yrs', dead: true, note: 'No record → no button' });
    rules(s, ['Register / View → Filaria form (M2.8-07 / M2.8-42) [FilariaMemberListAdapter.kt:93-94]', 'Dead member with no record: button hidden; death + sync icons']);
  }
  // 31 Leprosy members
  {
    const s = NF('M2.8-31 · Leprosy household members');
    screenTop(s, 'Leprosy · HH 1023', 'Ramesh Kumar · 5 members');
    const b = body(s); b.itemSpacing = 12;
    await memberCard(b, { name: 'Ramesh Kumar', meta: 'Head · M · 45 yrs', status: ['Screening', RED] });
    await memberCard(b, { name: 'Sita Devi', meta: 'Wife · F · 35 yrs', status: ['Suspected', ORANGE], sync: 'Pending' });
    await memberCard(b, { name: 'Ram Kumar', meta: 'Brother · M · 38 yrs · Visit 2', status: ['Follow up', RED], extra: [['Outlined', 'Visits']], sync: 'Synced' });
    await memberCard(b, { name: 'Ravi Kumar', meta: 'Son · M · 14 yrs', status: ['View follow-up', GREEN], sync: 'Synced' });
    await memberCard(b, { name: 'Kalawati Devi', meta: 'Mother · F · 70 yrs', dead: true, note: 'No record → no button' });
    rules(s, ['Status button: Screening (no record / no symptoms), Suspected (symptoms), Follow up (confirmed, not followed up this visit), View follow-up (done), View [LeprosyMemberListAdapter.kt:17-31]',
      '"Visits" only when an earlier visit cycle is finished (currentVisitNumber > 1) → sheet M2.8-32 [Adapter.kt:70-83]',
      'Dead member with no record: button hidden; death + sync icons [Adapter.kt:90, 119-127]']);
  }
  // 32 Leprosy visits sheet
  {
    const s = NF('M2.8-32 · Leprosy · earlier visits sheet', true);
    screenTop(s, 'Leprosy · HH 1023', 'Ramesh Kumar · 5 members');
    await visitSheet(s, { title: 'Earlier visits', subtitle: 'Ram Kumar · now on Visit 2', visits: [{ label: 'Visit 1', date: '15 Jun 2025 – 20 Jun 2026', status: 'Treatment completed', tone: 'Success', action: 'View' }], footnote: 'No earlier visits → "No previous visits found".' });
    rules(s, ['Lists visit cycles 1 … n-1; tap → that visit read-only (M2.8-44) [LeprosySuspectedListFragment.kt:93-133]']);
  }
  // 33 Leprosy suspected (village-wide)
  {
    const s = NF('M2.8-33 · Leprosy suspected list');
    screenTop(s, 'Leprosy suspected', 'All households · 2 people', 'none');
    const b = body(s); b.itemSpacing = 12;
    await memberCard(b, { name: 'Sita Devi', meta: 'HH 1023 · F · 35 yrs', status: ['Suspected', ORANGE], sync: 'Pending' });
    await memberCard(b, { name: 'Geeta Kumari', meta: 'HH 1011 · F · 28 yrs', status: ['Suspected', ORANGE], sync: 'Synced' });
    rules(s, ['All leprosy-suspected people across households → Suspected leprosy form (M2.8-43) [SuspectedLeprosyViewModel.kt:17]', 'No search bar (hidden in code). Empty → M2.8-22']);
  }
  // 34 Leprosy confirmed list
  {
    const s = NF('M2.8-34 · Leprosy confirmed list');
    screenTop(s, 'Leprosy confirmed', 'All households · 2 cases');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await memberCard(b, { name: 'Ram Kumar', meta: 'HH 1023 · M · 38 yrs · MB', status: ['Follow up', RED], sync: 'Synced' });
    await memberCard(b, { name: 'Babulal Sahu', meta: 'HH 1007 · M · 52 yrs · PB', status: ['View follow-up', GREEN], sync: 'Synced' });
    rules(s, ['Button "Follow up" (red) until this visit has a follow-up, then "View follow-up" (green) → MDT follow-up form (M2.8-06) [LeprosyMemberListAdapter.kt:21-27]']);
  }
  // 35 Malaria confirmed list
  {
    const s = NF('M2.8-35 · Malaria confirmed list');
    screenTop(s, 'Malaria confirmed', '3 cases');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or ID');
    await memberCard(b, { name: 'Mohan Lal', meta: 'HH 1023 · M · 72 yrs · Diagnosed 02 Nov 2026', statusIcon: ['check_circle', GREEN], status: ['Follow up', RED], sync: 'Synced' });
    await memberCard(b, { name: 'Pooja Kumari', meta: 'HH 1023 · F · 17 yrs · Diagnosed 28 Oct 2026', statusIcon: ['pill', PURPLE], status: ['Day 2', GREEN], sync: 'Pending' });
    await memberCard(b, { name: 'Jagdish Prasad', meta: 'HH 0990 · M · 61 yrs · Diagnosed 10 Oct 2026', dead: true, note: 'Dead, no follow-up → no button' });
    rules(s, ['Card shows date of diagnosis + case-status icon [rv_item_malaria_cases_confirmed_list.xml:319-340]',
      'Button "Follow up" (red) until treatment starts, then the last saved day ("Day 2") in green → M2.8-04 / M2.8-38 [MalariaConfirmedCasesListAdapter.kt:114-115]',
      'Dead person with no follow-up: button hidden [Adapter.kt:104-112]']);
  }
  // 36 Malaria screening view mode + visits
  {
    const s = NF('M2.8-36 · Malaria screening · view + earlier visits');
    screenTop(s, 'Malaria', 'Sunita Devi · 40 yrs · F · Visit no. 2', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Visit 2 · 02 Nov 2026', editable: false, reason: 'Use the Edit button below to change it.' });
    auto(b, 'Screening date', '02 Nov 2026'); auto(b, 'Case status (auto)', 'Suspected'); auto(b, 'Test type', 'RDT'); auto(b, 'RDT result', 'Negative');
    await table(b, 'Earlier visits', ['Visit no.', 'Date', 'Status'], [['1', '14 Aug 2026', 'Not suspected'], ['2', '02 Nov 2026', 'Suspected']]);
    editFab(s);
    rules(s, ['Record exists → read-only; Submit hidden [MalariaFormFragment.kt:43-61]',
      'Edit FAB only when last case status = Suspected / Not confirmed / Not suspected AND person not dead',
      'Earlier visits list (visit no., date, status); only "alive" visits listed [Fragment.kt:85-96; VisitsListAdapter.kt:45-47]']);
  }
  // 37 Died → marks dead everywhere
  {
    const s = NF('M2.8-37 · Status Died → marks dead everywhere');
    screenTop(s, 'Malaria', 'Mohan Lal · 72 yrs · M', 'more');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Beneficiary status', ['Alive', 'Died'], 1);
    const rv = reveal(b); dateH(rv, 'Date of death *', '01 Nov 2026', 'Last 90 days up to today.'); select(rv, 'Place of death *', 'Home'); select(rv, 'Reason of death *', 'Fever / malaria');
    banner(b, 'Warning', 'This marks Mohan Lal as dead', 'Saving marks him dead in every module. His tasks and due lists stop.', null);
    bottomBar(s, null, 'Submit');
    await toast(s, 'Beneficiary marked as death', 640);
    rules(s, ['Status Died/Death marks the beneficiary dead app-wide with date, place, reason; toast "Beneficiary marked as death" [MalariaFormViewModel.kt:142-163; KalaAzarFormViewModel.kt:128-145; AESFormViewModel.kt:117-134; LeprosyFormViewModel.kt:121-138]',
      'Same for Kala-azar, AES/JE, Leprosy screening forms',
      'Warning banner before save is not in app today – needs dev']);
  }
  // 38 Malaria treatment · Pv + view mode
  {
    const s = NF('M2.8-38 · Malaria treatment · Pv + view mode');
    screenTop(s, 'Malaria treatment', 'Pooja Kumari · Pv positive', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Day 2 · 30 Oct 2026', editable: false, reason: 'Edit overwrites this one record.' });
    auto(b, 'Treatment start date', '28 Oct 2026'); auto(b, 'Parasite type (from slide test)', 'Pv');
    await dayTrack(b, 'Pv · day-wise tracking (4 days)', 4, 2);
    auto(b, 'Date of completion (auto: start + 4 days)', '01 Nov 2026');
    editFab(s);
    rules(s, ['Only the tracker matching the slide result shows: Pv → 4 days (Day 1–4) [MalariaConfirmCasesDataset.kt:106-125, 151-159]',
      'Mitanin: Pv tracker = 14 days → M2.8-51',
      'Opens read-only with Edit FAB; one record per person, edit overwrites it [ConfirmedCaseFollowUpFormFragment.kt:39-49]']);
  }
  // 39 KA / AES / Leprosy view mode (dead)
  {
    const s = NF('M2.8-39 · Kala-azar / AES / Leprosy · view mode');
    screenTop(s, 'Kala-azar', 'Phoolmati Devi · 76 yrs · Deceased', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: '20 Oct 2026', editable: false, reason: 'Person is marked dead – editing is closed.' });
    auto(b, 'Visit date', '20 Oct 2026'); auto(b, 'Beneficiary status', 'Death'); auto(b, 'Date of death', '18 Oct 2026'); auto(b, 'Case status', 'Suspected');
    rules(s, ['Record exists → read-only, Submit hidden; Edit FAB shown unless the person is dead [KalaAzarFormFragment.kt:40-52; AEFFormFragment.kt:39-52; LeprosyFormFragment.kt:41-56]',
      'Alive person → same screen with Edit FAB (as M2.8-36)']);
  }
  // 40 Filaria MDA household form
  {
    const s = NF('M2.8-40 · Filaria MDA (household)');
    screenTop(s, 'Filaria MDA', 'HH 1023 · Ramesh Kumar', 'none');
    const b = body(s); b.itemSpacing = 12;
    dateH(b, 'Distribution date *', '05 Nov 2026', 'Not in the future. After the previous visit.');
    await seg(b, 'Medicine distributed? *', ['Yes', 'No'], 0);
    await uploads(b, 'Photo / file', [['mda_hh1023_nov.jpg', 'Done']], 'Camera or gallery · size limit');
    bottomBar(s, null, 'Submit');
    await table(b, 'MDA history', ['Distribution date', 'Medicine distributed'], [['08 Oct 2026', 'Yes'], ['10 Sep 2026', 'No']]);
    rules(s, ['Separate HOUSEHOLD-level server-driven form, opened from "Add MDA" (M2.8-21) [FilariaMDAFormFragment.kt:1-25]',
      'Visit date not in the future and after the previous visit; error under the field [Fragment.kt:98-115]',
      'History table: distribution date + medicine distributed Yes/No [Fragment.kt:27-44]',
      'Photo picker: camera or gallery with size limit (M2.8-27) [Fragment.kt:58-60]',
      'Toasts: saved / failed to submit [Fragment.kt:157-163]']);
  }
  // 41 MDA once a month
  {
    const s = NF('M2.8-41 · Filaria MDA · already done this month');
    screenTop(s, 'Filaria MDA', 'HH 1019 · Shyam Lal Yadav', 'none');
    const b = body(s); b.itemSpacing = 12;
    dateH(b, 'Distribution date *', '05 Nov 2026', 'Not in the future. After the previous visit.');
    await seg(b, 'Medicine distributed? *', ['Yes', 'No'], 0);
    await table(b, 'MDA history', ['Distribution date', 'Medicine distributed'], [['02 Nov 2026', 'Yes']]);
    bottomBar(s, null, 'Submit');
    await toast(s, 'You have already submitted this form for this month', 640);
    rules(s, ['Once per household per month; Submit shows this toast [FilariaMDAFormViewModel.kt:203-208, 260-276]']);
  }
  // 42 Filaria view + child under 2
  {
    const s = NF('M2.8-42 · Filaria · view mode + child under 2');
    screenTop(s, 'Filaria', 'Aarav Kumar · 1 yr 6 mths', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: '02 Nov 2026', editable: false, reason: 'Edit FAB always shown when a record exists.' });
    auto(b, 'Home visit date', '02 Nov 2026');
    auto(b, 'DEC & Albendazole dose (locked: under 2 yrs)', 'Yes');
    auto(b, 'Other side effect (locked once saved)', 'Mild itching');
    editFab(s);
    rules(s, ['Child under 2 yrs: "DEC & Albendazole dose" locked to Yes [FilariaFormDataset.kt:163-171, 285-301]',
      'Saved "Other side effect" text locked when re-opened [FilariaFormDataset.kt:150-156]', 'Edit FAB whenever a record exists, no death check [FilariaFormFragment.kt:41-50]']);
  }
  // 43 Suspected leprosy form
  {
    const s = NF('M2.8-43 · Suspected leprosy form');
    screenTop(s, 'Suspected leprosy', 'Sita Devi · 35 yrs · Visit 1', 'none');
    const b = body(s); b.itemSpacing = 12;
    section(b, 'Open', 'Screening (locked)', 'From the screening form');
    auto(b, 'Home visit date', '02 Nov 2026'); auto(b, 'Leprosy status', 'Suspected'); auto(b, 'Referred to', 'PHC Sirsi (MO)'); auto(b, 'Symptoms', 'Pale patch with less feeling, tingling'); auto(b, 'Visit no.', '1');
    select(b, 'Type of leprosy', 'Paucibacillary (PB)');
    banner(b, 'Info', 'This confirms the case', 'Sita Devi moves to the Leprosy confirmed list.', null);
    dateH(b, 'Treatment start date *', '05 Nov 2026', 'Not before the home-visit date.');
    bottomBar(s, null, 'Submit');
    rules(s, ['Screening details pre-filled and locked [LeprosySuspectedDataset.kt:18-110]',
      'Entering Type of leprosy confirms the case → Confirmed list (M2.8-34) [Dataset.kt:245-250]',
      'Treatment start date not before home-visit date [Dataset.kt:274]',
      'Always editable (no view mode / Edit FAB); toast on save [SuspectedLeprosyFormFragment.kt:43-44]']);
  }
  // 44 Leprosy past visit (read-only)
  {
    const s = NF('M2.8-44 · Leprosy past visit (read-only)');
    screenTop(s, 'Leprosy · Visit 1', 'Ram Kumar · 38 yrs', 'none');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Visit 1 · closed 20 Jun 2026', editable: false, reason: 'Earlier visit cycle. Cannot be changed.' });
    auto(b, 'Home visit date', '15 Jun 2025'); auto(b, 'Leprosy status', 'Confirmed'); auto(b, 'Type of leprosy', 'Multibacillary (MB)');
    await table(b, 'Follow-ups (Visit 1)', ['Follow-up date', 'Treatment status'], [['15 Jul 2025', 'Ongoing'], ['…', '…'], ['20 Jun 2026', 'Completed']]);
    rules(s, ['Read-only view of one earlier visit cycle: its screening data + its follow-up list; no submit [LeprosyVisitFragment.kt; LeprosyVisitViewModel.kt:33, 112]',
      'code bug: fields render enabled though they cannot be saved – intended: read-only']);
  }
  // 45 Leprosy treatment completed
  {
    const s = NF('M2.8-45 · Leprosy follow-up · treatment completed');
    screenTop(s, 'Leprosy · on treatment', 'Ram Kumar · MB · Visit 1', 'more');
    const b = body(s); b.itemSpacing = 12;
    dateH(b, 'Follow-up date *', '20 Jun 2026', 'Not before the home visit. Not in the future.');
    select(b, 'Treatment status *', 'Completed');
    const rv = reveal(b); date(rv, 'Treatment end date *', '20 Jun 2026');
    bottomBar(s, null, 'Save visit');
    await toast(s, 'Visit completed, starting next visit screening', 640);
    rules(s, ['"Treatment completed" closes the visit cycle and resets the person to screening for Visit 2 [LeprosyConfirmedFromViewModel.kt:191-205; LeprosyRepo.kt:305-317]',
      'Treatment end date required when status = Completed', 'Person then shows "Screening" + "Visits" on the member list (M2.8-31)']);
  }
  // 46 GC pointer links
  {
    const s = NF('M2.8-46 · Add check-up · pointer links');
    screenTop(s, 'Add check-up', 'Kamla Devi · 61 yrs · F', 'more');
    const b = body(s); b.itemSpacing = 12;
    dateH(b, 'Date of check-up *', '05 Nov 2026', 'Today or up to one month back. Not a future date.');
    select(b, 'Type of problem *', 'Select', false);
    banner(b, 'Info', 'Looking for TB, malaria, leprosy…?', 'Use Communicable Diseases.', 'Open');
    banner(b, 'Info', 'High BP, diabetes…?', 'Use NCD.', 'Open');
    bottomBar(s, null, 'Refer to facility');
    rules(s, ['Pointer links open Communicable Diseases (M2.8-02) / NCD (M2.7-01); shown ONLY before a problem type is chosen [GeneralCheckupFormFragment.kt:129-139, 153-155]']);
  }
  // 47 GC loading / not available
  {
    const s = NF('M2.8-47 · Add check-up · loading / form not available');
    screenTop(s, 'Add check-up', 'Kamla Devi · 61 yrs · F', 'more');
    const b = body(s); b.itemSpacing = 16; b.counterAxisAlignItems = 'CENTER';
    spinner(b); await caption(b, 'Loading form…');
    empty(b, 'Form not available', 'The check-up form is not downloaded yet or is incomplete. Connect to internet and try again.', 'wifi_off');
    btn(b, 'Tonal', 'Try again');
    bottomBar(s, null, 'Refer to facility');
    const bar = s.children.find((n) => n.name === 'Bottom action bar'); if (bar) bar.opacity = 0.4;
    rules(s, ['Spinner while the server form loads [Fragment.kt:111]', 'Form not downloaded / server form incomplete → "Form not available" + Try again; Refer disabled until ready [Fragment.kt:107-125]']);
  }
  // 48 GC conflict + Other + validation
  {
    const s = NF('M2.8-48 · Add check-up · conflict, Other, errors');
    screenTop(s, 'Add check-up', 'Kamla Devi · 61 yrs · F', 'more');
    const b = body(s); b.itemSpacing = 12;
    select(b, 'Type of problem *', 'Eye, ear and skin');
    await checkRow(b, 'Cloudy eyesight (may be cataract)', true, 'Cataract referral');
    await checkRow(b, 'Cough more than 2 weeks', false, 'TB screening');
    await checkRow(b, 'Other', true);
    S.tf(b, 'Error', { Label: 'Name the problem *', Value: '', Helper: 'Required. Max 50 characters.', 'Show helper': true, 'Show trailing': false });
    bottomBar(s, null, 'Refer to facility');
    await toast(s, 'Cataract referral is already selected. Untick it first.', 620);
    rules(s, ['Problems owned by two different modules cannot both be ticked; second rejected with toast "…already selected… Untick it first." [Fragment.kt:94-101; ViewModel.kt:183, 203-210]',
      '"Other" needs the problem name typed in: required, character limit [ViewModel.kt:223-235, 382]',
      'Errors (no type / no problem / date outside 1 month back) → toast + field error [Fragment.kt:161-164; ViewModel.kt:348-370]']);
  }
  // 49 GC duplicate dialog
  {
    const s = NF('M2.8-49 · Add check-up · already referred dialog', true);
    screenTop(s, 'Add check-up', 'Kamla Devi · 61 yrs', 'more');
    await dialog(s, { ic: 'info', icColor: 'color/primary', title: 'Already referred this month', body: 'Kamla Devi was referred for this on 02 Nov 2026. You can refer again next month.', cancel: 'Cancel', ok: 'View in HWC Referrals' });
    rules(s, ['"View in HWC Referrals" → HWC referred list (M2.8-14); Cancel closes [GeneralCheckupFormFragment.kt:215-226; strings_general_checkup.xml:56-58]']);
  }
  // 50 GC save failed
  {
    const s = NF('M2.8-50 · Add check-up · could not save');
    screenTop(s, 'Add check-up', 'Kamla Devi · 61 yrs · F', 'more');
    const b = body(s); b.itemSpacing = 12;
    dateH(b, 'Date of check-up *', '02 Nov 2026', 'Today or up to one month back. Not a future date.');
    select(b, 'Type of problem *', 'Fever, cough and common illness');
    await checkRow(b, 'Fever', true); await checkRow(b, 'Cough', true);
    bottomBar(s, null, 'Refer to facility');
    await toast(s, 'Could not save the check-up. Try again.', 640);
    rules(s, ['After the refer form returns, saving the check-up failed → "Could not save" toast, stays here [GeneralCheckupFormFragment.kt:233-267]', 'Success → toast "Referral submitted", back to the list (M2.8-13)']);
  }

  // 51 Malaria treatment · Pv · Mitanin
  {
    const s = NF('M2.8-51 · Malaria treatment · Pv · Mitanin');
    screenTop(s, 'Malaria treatment', 'Kumari Bai Netam · Pv positive', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Treatment start date *', '28 Oct 2026'); auto(b, 'Parasite type (from slide test)', 'Pv');
    await dayTrack(b, 'Pv · day-wise tracking (14 days)', 14, 8);
    select(b, 'Day *', 'Day 9');
    auto(b, 'Date of completion (auto: start + 4 days)', '01 Nov 2026');
    bottomBar(s, null, 'Submit');
    rules(s, ['Mitanin (mitanin / mitaninStag / mitaninUat): Pv day dropdown = Day 1–14 (daysPvMitanin) instead of Day 1–4 [MalariaConfirmCasesDataset.kt:54-56; res/values/strings.xml:1362-1377]',
      'Pf tracker unchanged (3 days) on Mitanin',
      'code bug: completion date is still auto-set and limited to start + 4 days for Pv, which ends before day 14 on Mitanin [MalariaConfirmCasesDataset.kt:209-230] – intended: start + 14 days']);
  }

  const maxB = await finish(sec, frames, (f) => rowMap.get(f)) - 120;
  await note(sec, 120, maxB + 80, 960,
    'DISEASE CONTROL NOTES\n' +
    '• Hub (01) = IconDataset.getDiseaseControlDataset: General check-up, NCD (stage 2.7), Communicable (02).\n' +
    '• Each communicable disease follows screening → (suspected) → confirmed / treatment with day- or month-tracking grids (04, 06).\n' +
    '• General check-up (09–15) follows docs/GENERAL_CHECKUP_BRD_ANALYSIS.md v0.5: date limit hint, type → problems, emergency options in red, owned options tagged "→ Module", routing + dedup dialogs, refer form with incentive hint, HWC referred list from ALL sources with EMERGENCY / pending / follow-up chips, follow-up form.\n' +
    '• Open item from the BRD analysis: NCD_REFER unique index would drop month-on-month GC referrals (backend/DB, not design).\n' +
    '• Gap frames 17–50 (2026-11-05): code navigation = disease tile → sub-hub (17 TB, 18 Malaria, 19 Leprosy) → shared household list (20/21) → member list with status buttons (23, 28–31) → form. Village-wide lists 33/34/35. Malaria IRS (24/25) + mosquito net (26/27), Filaria household MDA (40/41), leprosy visit cycles (31/32/44/45), view modes + death side-effect (36–39, 42), general check-up states (46–50).\n' +
    '• Prototype map (44-stage-2.14) still sends Malaria → M2.8-03, Leprosy → M2.8-05 etc.; should point to 18 / 19 / 20.');
  fitSection(sec);
  return ['2.8 screens: ' + frames.length];
}
