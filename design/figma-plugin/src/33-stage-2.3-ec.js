// ---------------------------------------------------------------------------
// Stage 2.3 · Eligible couple
// Sources: ui/home_activity/eligible_couple/*, EligibleCoupleRegistrationDataset,
// EligibleCoupleTrackingDataset, arrays method_of_contraception, FLW-977 (view mode)
// 2026-11-05 gap fix: gap_C (EC rows) → edited 02–09, added 10–17. Code is the source of truth.
// ---------------------------------------------------------------------------
async function buildStage23(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureSearchMic(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack } = S;
  const { actionCard, dialog, sortSheet, sourceSheet, searchRow, viewMode, uploads, table, rules, caption, tag, btn, row } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.3 · Eligible couple');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };
  const FP = ['ANTRA injection', 'Copper T (IUCD)', 'Condom', 'Mala N (oral pills)', 'Chaya (weekly pill)', 'ECP (emergency pill)', 'Male sterilisation', 'Female sterilisation', 'Any other method'];
  // "Sorted by" label shown above EC lists (EligibleCoupleListFragment.kt:52-64)
  const sortLabel = async (p, v = 'Newest first') => { const r = row(p, 'Sort label', 6); r.appendChild(icon('sort', 'color/on-surface-variant', 16)); r.appendChild(await txt('Sorted: ' + v, 'Label/Medium', 'color/on-surface-variant')); return r; };

  // 01 Hub
  {
    const s = F('M2.3-01 · Eligible couple hub', 0, 0);
    screenTop(s, 'Eligible couples', 'Married women 15–49');
    const b = body(s);
    await stat(b, [['186', 'Registered couples', 'couple'], ['22', 'Visits due this month', 'maternal'], ['64%', 'Using a method', 'immunization']]);
    tileGrid(b, [['couple', 'EC registration', '8 to register', 'person_add'], ['couple', 'EC tracking', '22 due', 'calendar']]);
    banner(b, 'Info', 'Monthly tracking', 'Visit every eligible couple once a month: check for pregnancy and family-planning needs.', null);
    rules(s, ['Stat tiles and "Monthly tracking" banner: not in app today – needs dev (code hub = 2 tiles with counts, IconDataset.kt:512-523)']);
  }
  // 02 Registration list (edited: sort, register/view states, IFA, missed period, sync rule)
  {
    const s = F('M2.3-02 · EC registration list', 1, 0);
    screenTop(s, 'EC registration', '8 to register · 186 registered');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'To register', 'Count 1': '8', 'Label 2': 'Registered', 'Count 2': '186', 'Label 3': 'All', 'Count 3': '194' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or HH ID', { sort: true });
    chips(b, ['All', 'Newly married', 'Age < 20']);
    await sortLabel(b);
    await actionCard(b, { name: 'Priya Kumari', meta: 'W/o Deepak · F · 21 yrs · married 2026', rows: [['No. of children', '0'], ['HH ID', '1023']], icons: [['pill', 'IFA']], actions: [['Filled', 'Register']], footnote: 'No sync icon: no EC record yet' });
    await actionCard(b, { name: 'Kavita Devi', meta: 'W/o Manoj · F · 29 yrs', sync: 'Synced', tags: [['Alert', 'Missed period']], rows: [['No. of children', '2'], ['LMP', '12 Sep 2026']], icons: [['warning', 'Missed', 'color/error'], ['pill', 'IFA']], actions: [['Tonal', 'View']] });
    await actionCard(b, { name: 'Shanti Yadav', meta: 'W/o Raju · F · 33 yrs', sync: 'Pending', tags: [['Neutral', 'Under review']], rows: [['No. of children', '3'], ['LMP', '20 Oct 2026']], icons: [['pill', 'IFA']], actions: [['Tonal', 'View']] });
    rules(s, [
      'Button: red "Register" until LMP saved; green "View" once ecr.lmpDate != 0 (ECRegistrationAdapter.kt:76-85)',
      'IFA button: only women 20–49 with reproductive status 1 or 2 (rv_item_ben_ec_with_form.xml:345-360; BindingUtils.kt:552-561) → opens IFA form',
      'Status: LMP > 35 days ago = "Missed period" + alert icon, else "Under review"; hidden until registered (ECRegistrationAdapter.kt:52-73)',
      'Sync icon only once an EC record exists (:75). Child count from EC record (:51)',
      'Sort icon opens M2.3-10; chosen sort shown above list, default "Newest first" (EligibleCoupleListFragment.kt:52-64)',
      'Opened from Home "Missed period": same screen, missed-period couples only → M2.3-11',
      'Chips "Newly married / Age < 20": not in app today – needs dev',
    ]);
  }
  // 03 Registration form (edited: header strip, read-only children, flavour gates, HRP side effect)
  {
    const s = F('M2.3-03 · EC registration form', 2, 0);
    screenTop(s, 'Register couple', 'Kavita Devi · W/o Manoj', 'more');
    const b = body(s); b.itemSpacing = 12;
    await person(b, 'KD', 'Kavita Devi · F · 29 yrs', 'Husband: Manoj Kumar · Married 14 May 2018 · 2 children');
    section(b, 'Done', 'Couple details', 'Complete');
    text(b, 'RCH ID', '1210 4456 7890');
    auto(b, 'Aadhaar (already on profile)', 'XXXX XXXX 4521');
    section(b, 'Open', 'Children', '2 children', 2);
    await caption(b, 'Filled automatically from household members (read-only)');
    const r1 = AL('HORIZONTAL'); gap(r1, 'spacing/sm'); add(b, r1);
    for (const [l, v] of [['Live children', '2'], ['Boys', '1'], ['Girls', '1']]) { const x = kinst('Read-only field', null, { Label: l, Value: v }); r1.appendChild(x); x.layoutSizingHorizontal = 'FILL'; }
    const kid = async (n, sex, dob, age, gapTxt) => {
      const c = AL('HORIZONTAL', 'Child ' + n); c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/sm', 'spacing/sm'); gap(c, 'spacing/sm'); rad(c, 'radius/md'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(b, c);
      const t = box('No', 32, 32); t.cornerRadius = 16; fill(t, 'module/child/bg'); t.appendChild(await txt(String(n), 'Label/Large', 'module/child/fg')); c.appendChild(t);
      const col = AL('VERTICAL'); col.itemSpacing = 2; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      col.appendChild(await txt(`Child ${n} · ${sex} · ${age}`, 'Title/Small', 'color/on-surface'));
      col.appendChild(await txt(`Born ${dob} · ${gapTxt}`, 'Body/Small', 'color/on-surface-variant'));
      c.appendChild(icon('lock', 'color/on-surface-variant', 18));
    };
    await kid(1, 'Boy', '14 Mar 2020', '6 yrs', 'gap from marriage 1 yr 10 mo');
    await kid(2, 'Girl', '02 Jun 2024', '2 yrs', 'gap 4 yrs 3 mo');
    const bk = row(b, 'Bank details header', 8); section(bk, 'Closed', 'Bank details', '4 fields', 3); tag(bk, 'Neutral', 'Saksham only');
    section(b, 'Open', 'High-risk check', '1 risk found', 4);
    await seg(b, 'Any previous C-section?', ['Yes', 'No'], 0);
    banner(b, 'Warning', 'High-risk answer', 'Saving will also mark Kavita as a high-risk (non-pregnant) woman.', null);
    bottomBar(s, 'Save draft', 'Register');
    rules(s, [
      'Header strip: age + gender, husband, marriage date, no. of children (Fragment:248-271)',
      'Children (up to 9) pre-filled from HH members, read-only; live/boys/girls counts and gaps auto-calculated (Dataset:1528-1550; VM:157-178). "Add child" removed',
      'Nayi Pahal kit section only when live children = 0 → see M2.3-13 (Dataset:1552-1559)',
      'Bank details: Saksham (Assam) builds only (Dataset:1478-1480)',
      'High-risk check hidden on Mitanin. Section header turns red when any answer = Yes (Dataset:1461-1476, 2346-2360)',
      'Saving with any risk = Yes silently creates/updates the non-pregnant HRP assessment and marks her HRP (VM:205-212). Warning banner: not in app today – needs dev',
      'RCH ID and Aadhaar are written back to the beneficiary profile; Aadhaar read-only if it already exists (Dataset:1488-1492, 2540-2560)',
      'After Register: back to list + sync starts (Fragment:279-288). Saved record opens in view mode → M2.3-12',
      '"Save draft": not in app today – needs dev',
    ]);
  }
  // 04 Tracking list (edited: View visits / Add visit, missed period, no follow-up, blocked month)
  {
    const s = F('M2.3-04 · EC tracking list', 3, 0);
    screenTop(s, 'EC tracking', 'November 2026 · 22 due');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'Due', 'Count 1': '22', 'Label 2': 'Done', 'Count 2': '164', 'Label 3': 'All', 'Count 3': '186' }));
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile', { sort: true });
    chips(b, ['All', 'No method', 'ANTRA due']);
    await sortLabel(b);
    await actionCard(b, { name: 'Kavita Devi', meta: '29 yrs · 2 children', sync: 'Synced', tags: [['Maternal', 'ANTRA dose 3 due 12 Nov']], rows: [['Last visit', '03 Oct 2026']], actions: [['Outlined', 'View visits'], ['Filled', 'Add visit']] });
    await actionCard(b, { name: 'Priya Kumari', meta: '21 yrs · 0 children', tags: [['Alert', 'Missed period']], rows: [['LMP', '18 Sep 2026']], icons: [['warning', 'Missed', 'color/error']], actions: [['Filled', 'Add visit']], footnote: 'No visits yet → "View visits" hidden' });
    await actionCard(b, { name: 'Shanti Yadav', meta: '33 yrs · 3 children', sync: 'Pending', tags: [['Alert', 'No follow-up']], rows: [['Last visit', '12 Jun 2026']], icons: [['clock', 'No follow-up', 'color/warning']], actions: [['Outlined', 'View visits'], ['Filled', 'Add visit']] });
    await actionCard(b, { name: 'Rekha Devi', meta: '27 yrs · 1 child', sync: 'Synced', tags: [['Success', 'Visited 02 Nov']], actions: [['Outlined', 'View visits'], ['Filled', 'Add visit', true]], footnote: 'This month already filled' });
    snack(s, 'Error', { Message: "This month's visit already filled", Action: 'OK' });
    rules(s, [
      '"Add visit" blocked when a visit exists for the current calendar month → toast (EligibleCoupleTrackingCache.kt:142-150; Fragment:91-102)',
      '"View visits" only if ≥ 1 visit exists (rv_item_ec_tracking_list.xml:352-379; BindingUtils.kt:391-398) → M2.3-05',
      'Missed period icon + status when LMP > 35 days ago (ECTrackingListAdapter.kt:65-77)',
      'No follow-up icon + last visit date when last visit 90–365 days ago (ECTrackingListAdapter.kt:51-63)',
      'Card: total children + sync icon = all visits synced (xml:79-86, 164-174)',
      'From Home: "No follow-up" (M2.3-14) or "Missed period" mode, source 1/2 (VM:29-33). Supervisor: read-only → M2.3-15',
      'Sort sheet M2.3-10 (:75-88). Chips "No method / ANTRA due": not in app today – needs dev',
    ]);
  }
  // 05 Tracking history sheet (edited: per-visit rows with sync + View)
  {
    const s = F('M2.3-05 · Tracking history sheet', 4, 0, true);
    screenTop(s, 'EC tracking', 'November 2026 · 22 due');
    const b = body(s); b.itemSpacing = 12;
    recordCard(b, { name: 'Kavita Devi', meta: '29 yrs · 2 children · last visit 3 Oct', tag: 'ANTRA dose 3 due 12 Nov', tone: 'Maternal', action: 'View visits' });
    scrim(s);
    const sh = sheet(s, 'Monthly visits');
    sh.appendChild(await txt('Kavita Devi · 2026–27', 'Title/Medium', 'color/on-surface'));
    const grid = AL('HORIZONTAL', 'Months'); grid.layoutWrap = 'WRAP'; grid.itemSpacing = 8; grid.counterAxisSpacing = 8; add(sh, grid);
    const months = [['Apr', 'done', 'Condom'], ['May', 'done', 'Condom'], ['Jun', 'done', 'ANTRA 1'], ['Jul', 'done', '—'], ['Aug', 'missed', 'Missed'], ['Sep', 'done', 'ANTRA 2'], ['Oct', 'done', '—'], ['Nov', 'due', 'Due'], ['Dec', 'next', ''], ['Jan', 'next', ''], ['Feb', 'next', ''], ['Mar', 'next', '']];
    for (const [m, st, d] of months) {
      const c = AL('VERTICAL', m); c.counterAxisAlignItems = 'CENTER'; c.primaryAxisAlignItems = 'CENTER'; c.itemSpacing = 2; c.resize(74, 48); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED'; rad(c, 'radius/md');
      fill(c, st === 'done' ? 'color/success-container' : st === 'due' ? 'color/warning-container' : st === 'missed' ? 'color/error-container' : 'color/surface-container-high'); grid.appendChild(c);
      c.appendChild(await txt(m, 'Title/Small', 'color/on-surface'));
      if (d) c.appendChild(await txt(d, 'Label/Small', st === 'missed' ? 'color/error' : 'color/on-surface-variant'));
    }
    for (const [d, st] of [['Filled on 03 Oct 2026', 'Pending'], ['Filled on 08 Sep 2026', 'Synced'], ['Filled on 05 Jul 2026', 'Synced']]) {
      const r = AL('HORIZONTAL', 'Visit · ' + d); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 8; pad(r, 'spacing/sm', 'spacing/xs'); rad(r, 'radius/md'); fill(r, 'color/surface'); stroke(r, 'color/outline-variant'); add(sh, r);
      const t = await txt(d, 'Body/Medium', 'color/on-surface'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL';
      try { r.appendChild(inst('syncBadge', `Status=${st}`)); } catch (e) { /* ignore */ }
      btn(r, 'Text', 'View');
    }
    add(sh, inst('button', 'Style=Filled, State=Enabled', { Label: 'Start November visit', 'Show icon': false }));
    pinBottom(s, sh);
    rules(s, [
      'One row per saved visit (filled-on date) with its own sync icon and VIEW → opens the form read-only (rv_item_ec_track.xml:30-58; Sheet:40-48)',
      'Month grid + "Start visit" button: not in app today – needs dev (code sheet = visit rows only)',
    ]);
  }
  // 06 Tracking form (edited: ANTRA history, locked dose no., due window, date limits)
  {
    const s = F('M2.3-06 · Tracking form · FP method', 5, 0);
    screenTop(s, 'EC tracking · Nov', 'Kavita Devi · 29 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    await table(b, 'ANTRA doses given', ['Dose', 'Date'], [['Dose 1', '12 Jun 2026'], ['Dose 2', '25 Sep 2026']]);
    date(b, 'Date of visit *', '02 Nov 2026');
    await caption(b, 'Allowed: 01 Nov 2026 (month after last visit) to today');
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    for (const [l, v] of [['Year', '2026–27'], ['Month', 'November']]) { const x = kinst('Read-only field', null, { Label: l, Value: v }); r.appendChild(x); x.layoutSizingHorizontal = 'FILL'; }
    date(b, 'LMP date', '20 Oct 2026');
    await seg(b, 'Pregnancy test done?', ['Yes', 'No'], 0);
    const rv = reveal(b); await seg(rv, 'Test result *', ['Positive', 'Negative'], 1);
    await seg(b, 'Is the woman pregnant?', ['Yes', 'No'], 1);
    await seg(b, 'Using a family planning method, or wants to use one?', ['Yes', 'No'], 0);
    const rv2 = reveal(b); await radios(rv2, 'FP method *', FP.slice(0, 4), 0);
    rv2.appendChild(await txt('+ 5 more methods', 'Label/Large', 'color/primary'));
    date(rv2, 'ANTRA injection date *', '02 Nov 2026');
    auto(rv2, 'ANTRA dose number (auto, locked)', 'Dose 3');
    auto(rv2, 'Next injection due', '17 Jan 2027 to 02 Mar 2027');
    await label(rv2, 'MPA card (photo)'); add(rv2, kinst('File row', 'State=Done', { 'File name': 'MPA_card.jpg' }));
    bottomBar(s, 'Save draft', 'Submit');
    snack(s, 'Success', { Message: 'Tracking form filled successfully', Action: 'OK' });
    rules(s, [
      'ANTRA dose history table shown when saved method contains "ANTRA" (Fragment:93-95, 137-141; VM:110-112, 219-224)',
      'ANTRA dose number auto-calculated from previous dose + visit date, locked (Dataset:383-403)',
      'Next injection due = window 76–120 days after the injection, "dd-mm-yyyy to dd-mm-yyyy" (Dataset:405-416)',
      'Visit date: earliest 1st of month after last visit (or registration date), latest today (Dataset:249-264)',
      'MPA card upload hidden on Mitanin (Dataset:359-363). Upload chooser → M2.3-17',
      'Not pregnant: toast "Tracking form filled successfully", go back, sync (Fragment:151-168, 109-113)',
      'Sterilisation methods → M2.3-16. "Save draft": not in app today – needs dev',
    ]);
  }
  // ---- Row 2
  // 07 Tracking form (pregnant path)
  {
    const s = F('M2.3-07 · Tracking form · found pregnant', 0, 1);
    screenTop(s, 'EC tracking · Nov', 'Priya Kumari · 21 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Date of visit *', '02 Nov 2026'); date(b, 'LMP date', '18 Sep 2026');
    await seg(b, 'Pregnancy test done?', ['Yes', 'No'], 0);
    const rv = reveal(b); await seg(rv, 'Test result *', ['Positive', 'Negative'], 0);
    await seg(b, 'Is the woman pregnant?', ['Yes', 'No'], 0);
    banner(b, 'Success', 'Pregnancy found · about 6 weeks', 'Register the pregnancy now so ANC visits are scheduled.', 'Register');
    const go = inst('button', 'Style=Filled, State=Enabled', { Label: 'Save & register pregnancy', 'Show icon': true, Icon: 'pregnant' }); add(b, go);
    const nt = await txt('FP questions are hidden when the woman is pregnant. "Register pregnancy" opens M2.2-03 pre-filled with LMP.', 'Body/Small', 'color/on-surface-variant'); add(b, nt); nt.textAutoResize = 'HEIGHT';
    rules(s, [
      'Before opening Pregnancy registration, the non-pregnant high-risk answers are copied into a new pregnant HRP assessment (VM:162-197)',
    ]);
  }
  // 08 View mode (FLW-977)
  {
    const s = F('M2.3-08 · Tracking view mode (FLW-977)', 1, 1);
    screenTop(s, 'EC tracking · Oct', 'Kavita Devi · submitted 3 Oct', 'none');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Submitted · view only', 'Every answer is shown exactly as entered, including the FP method.', null);
    await infoCard(b, 'Visit', [['Date of visit', '03 Oct 2026'], ['LMP date', '22 Sep 2026'], ['Pregnancy test done', 'Yes'], ['Test result', 'Negative'], ['Is the woman pregnant', 'No']]);
    await infoCard(b, 'Family planning', [['Using / wants a method', 'Yes'], ['FP method', 'ANTRA injection'], ['ANTRA dose date', '25 Sep 2026'], ['Next dose due', '12 Nov 2026'], ['MPA card', '1 photo']]);
    const nt = await txt('FLW-977: the "Yes + Negative" path must restore the FP method in view mode (today only the "No" branch is restored). Hindi method list must match English order.', 'Body/Small', 'color/on-surface-variant'); add(b, nt); nt.textAutoResize = 'HEIGHT';
    const bar = bottomBar(s, null, 'Submit'); bar.opacity = 0.4;
    rules(s, [
      'View mode: same form, Submit shown but disabled (not hidden) and uploads disabled (Fragment:97-101, 134-135)',
      'code bug: FLW-977 Yes + Negative path does not restore FP method; Hindi MiniLap array misaligned',
    ]);
  }
  // 09 Mitanin variant
  {
    const s = F('M2.3-09 · Tracking form · Mitanin variant', 2, 1);
    screenTop(s, 'EC tracking · Nov', 'Kavita Devi · 29 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Mitanin builds only', 'The FP question is split in two (ectdset_fly_plan_mthd_mitanin / want_to_use).', null);
    await seg(b, 'Are you using any family planning method?', ['Yes', 'No'], 1);
    await seg(b, 'Do you want to use any planning method?', ['Yes', 'No'], 0);
    const rv = reveal(b); await radios(rv, 'FP method *', FP.slice(0, 3), 1);
    await caption(b, 'No MPA card upload and no discharge-summary uploads on Mitanin.');
    bottomBar(s, 'Save draft', 'Submit');
    rules(s, ['Mitanin: MPA card upload hidden (Dataset:359-363, 638-642; Fragment:126)', 'Mitanin: sterilisation discharge-summary uploads hidden (Dataset:337-353, 670-680)']);
  }
  // 10 Sort sheet (NEW)
  {
    const s = F('M2.3-10 · EC sort sheet', 3, 1, true);
    screenTop(s, 'EC registration', '8 to register · 186 registered');
    const b = body(s); recordCard(b, { name: 'Priya Kumari', meta: 'W/o Deepak · 21 yrs', tag: 'Not registered', tone: 'Neutral', action: 'Register' });
    await G.optionSheet(s, 'Sort by', ['Newest first', 'Oldest first', 'Age-wise', 'Syncing first', 'Unsynced first'], 0, { actions: [['Text', 'Cancel'], ['Filled', 'Apply']] });
    rules(s, [
      'Opened from the filter icon on EC registration and EC tracking lists (EcFilterBottomSheetFragment.kt:36-57)',
      '5 options, Apply / Cancel. Selected sort shown as "Sorted: …" label above the list, default Newest first',
    ]);
  }
  // 11 Registration · missed period mode (NEW)
  {
    const s = F('M2.3-11 · EC registration · Missed period mode', 4, 1);
    screenTop(s, 'Missed period', 'Eligible couples · 3');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or HH ID', { sort: true });
    await sortLabel(b);
    await actionCard(b, { name: 'Kavita Devi', meta: 'W/o Manoj · 29 yrs', sync: 'Synced', tags: [['Alert', 'Missed period']], rows: [['No. of children', '2'], ['LMP', '12 Sep 2026']], icons: [['warning', 'Missed', 'color/error'], ['pill', 'IFA']], actions: [['Tonal', 'View']] });
    await actionCard(b, { name: 'Sangeeta Kumari', meta: 'W/o Ajay · 24 yrs', sync: 'Synced', tags: [['Alert', 'Missed period']], rows: [['No. of children', '1'], ['LMP', '30 Aug 2026']], icons: [['warning', 'Missed', 'color/error'], ['pill', 'IFA']], actions: [['Tonal', 'View']] });
    rules(s, [
      'Opened from Home "Missed period": same EC registration list filtered to LMP > 35 days (source == SOURCE_MISSED_PERIOD, EligibleCoupleListViewModel.kt:27-32)',
      'Nav fix: Home "Missed period" goes here, not to the PW list (M2.2-02)',
    ]);
  }
  // 12 Registration view mode (NEW)
  {
    const s = F('M2.3-12 · EC registration · view mode', 5, 1);
    screenTop(s, 'Eligible couple', 'Kavita Devi · W/o Manoj', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Registered 14 Aug 2026', editable: true, reason: 'Tap Edit to change any answer' });
    await person(b, 'KD', 'Kavita Devi · F · 29 yrs', 'Husband: Manoj Kumar · Married 14 May 2018 · 2 children');
    await infoCard(b, 'Couple details', [['RCH ID', '1210 4456 7890'], ['LMP date', '12 Sep 2026'], ['Live children', '2 (1 boy, 1 girl)']]);
    await infoCard(b, 'High-risk check', [['Previous C-section', 'Yes'], ['Marked as HRP', 'Yes (non-pregnant)']]);
    rules(s, [
      'Opens read-only when recordExists && lmpDate != 0; Submit hidden, Edit FAB unlocks the form (EligibleCoupleRegFragment.kt:158-170, 276-278; VM:147-155)',
      'Contradicts kit M2.0-P5 "ask your ANM": EC registration CAN be edited by the ASHA',
    ]);
  }
  // ---- Row 3
  // 13 Nayi Pahal kit · no children + incentive pop-up (NEW)
  {
    const s = F('M2.3-13 · Nayi Pahal kit · incentive pop-up', 0, 2, true);
    screenTop(s, 'Register couple', 'Priya Kumari · W/o Deepak', 'more');
    const b = body(s); b.itemSpacing = 12;
    section(b, 'Open', 'Nayi Pahal kit', 'No live children', 4);
    await seg(b, 'Nayi Pahal kit handed over to the couple?', ['Yes', 'No'], 0);
    const rv = reveal(b); date(rv, 'Date handed over *', '02 Nov 2026');
    await label(rv, 'Photo of couple with kit');
    const ph = AL('HORIZONTAL', 'Photos'); gap(ph, 'spacing/sm'); add(rv, ph); ph.appendChild(kinst('Photo tile', 'State=Empty')); ph.appendChild(kinst('Photo tile', 'State=Empty'));
    await dialog(s, { ic: 'info', icColor: 'color/primary', title: 'Upload kit photo', body: 'Please upload photo of couple with Nayi Pahal kit, to claim your incentive.', cancel: null, ok: 'OK' });
    rules(s, [
      'Kit section only when live children = 0 (removed otherwise, Dataset:1552-1559)',
      'Kit = Yes → pop-up asking for couple photo to claim incentive; all flavours except Mitanin (Dataset:1797; Fragment:243-246)',
      'Mitanin: kit date only, no photo upload, no pop-up (Dataset:1461, 1576, 1799-1810; Fragment:172)',
    ]);
  }
  // 14 Tracking · no follow-up mode (NEW)
  {
    const s = F('M2.3-14 · EC tracking · No follow-up mode', 1, 2);
    screenTop(s, 'No follow-up', 'Eligible couples · 4');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name or mobile', { sort: true });
    await sortLabel(b);
    await actionCard(b, { name: 'Shanti Yadav', meta: '33 yrs · 3 children', sync: 'Synced', tags: [['Alert', 'No follow-up']], rows: [['Last visit', '12 Jun 2026']], icons: [['clock', 'No follow-up', 'color/warning']], actions: [['Outlined', 'View visits'], ['Filled', 'Add visit']] });
    await actionCard(b, { name: 'Meena Devi', meta: '38 yrs · 4 children', sync: 'Synced', tags: [['Alert', 'No follow-up']], rows: [['Last visit', '20 Mar 2026']], icons: [['clock', 'No follow-up', 'color/warning']], actions: [['Outlined', 'View visits'], ['Filled', 'Add visit']] });
    rules(s, [
      'From Home "No follow-up": EC tracking list filtered to last visit 90–365 days ago (source = 1, EligibleCoupleTrackingListViewModel.kt:29-33)',
      'Home "Missed period" (source = 2) opens the same list filtered to LMP > 35 days',
      'Nav fix: design used a household screen for "No follow-up in 30 days"',
    ]);
  }
  // 15 Tracking · supervisor read-only (NEW)
  {
    const s = F('M2.3-15 · EC tracking · Supervisor view', 2, 2);
    screenTop(s, 'EC tracking · Sunita (ASHA)', 'Supervisor · read only');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Viewing ASHA records', 'You can see visits but cannot add or change them.', null);
    searchRow(b, 'Search name or mobile', { sort: true });
    await actionCard(b, { name: 'Kavita Devi', meta: '29 yrs · 2 children', sync: 'Synced', rows: [['Last visit', '03 Oct 2026']], actions: [['Outlined', 'View visits']] });
    await actionCard(b, { name: 'Priya Kumari', meta: '21 yrs · 0 children', tags: [['Alert', 'Missed period']], rows: [['LMP', '18 Sep 2026']] });
    rules(s, [
      'Same EC tracking list opens inside SupervisorActivity (Fragment:150-160; nav_supervisor.xml:45-53)',
      'Add visit hidden for supervisor; banner wording: not in app today – needs dev',
    ]);
  }
  // 16 Tracking form · female sterilisation (NEW)
  {
    const s = F('M2.3-16 · Tracking form · sterilisation', 3, 2);
    screenTop(s, 'EC tracking · Nov', 'Meena Devi · 38 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    date(b, 'Date of visit *', '02 Nov 2026');
    await seg(b, 'Is the woman pregnant?', ['Yes', 'No'], 1);
    await seg(b, 'Using a family planning method, or wants to use one?', ['Yes', 'No'], 0);
    const rv = reveal(b); select(rv, 'FP method *', 'Female sterilisation');
    await uploads(rv, 'Discharge summary (2)', [['discharge_summary_1.jpg', 'Done']], 'Camera, gallery or PDF · max size limit · not on Mitanin');
    banner(b, 'Info', 'Permanently sterilised', 'After saving, Meena is tagged "Permanently Sterilised" and leaves the EC lists.', null);
    bottomBar(s, null, 'Submit');
    rules(s, [
      'Male / female sterilisation → 2 discharge-summary uploads; all flavours except Mitanin (Dataset:337-353, 670-680)',
      'Female sterilisation re-tags the woman "Permanently Sterilised" → removed from EC lists (VM:153-161, 209-215)',
      'Info banner: not in app today – needs dev (side effect exists, not communicated)',
    ]);
  }
  // 17 Upload chooser (NEW)
  {
    const s = F('M2.3-17 · EC upload chooser & errors', 4, 2, true);
    screenTop(s, 'EC tracking · Nov', 'Kavita Devi · 29 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    await label(b, 'MPA card (photo)'); add(b, kinst('File row', 'State=Done', { 'File name': 'MPA_card.jpg' }));
    await sourceSheet(s, 'Tap an uploaded file to view it (image viewer or PDF app)');
    rules(s, [
      'Chooser: Camera / Gallery / PDF (EC reg Fragment:444-466; tracking Fragment:228-334)',
      'Toasts: "File too large", "No document found", "Unable to open document"',
      'Tap uploaded file → image viewer, or PDF handed to an external app',
      'Used by: kit photo (M2.3-13), MPA card (M2.3-06), discharge summaries (M2.3-16)',
    ]);
  }

  // 18 Registration form · Mitanin (NEW, Mitanin addendum)
  {
    const s = F('M2.3-18 · EC registration form · Mitanin', 5, 2);
    screenTop(s, 'Register couple', 'Priya Kumari · W/o Deepak', 'more');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Mitanin builds only', 'No high-risk check, no bank details, no kit photo.', null);
    await person(b, 'PK', 'Priya Kumari · F · 21 yrs', 'Husband: Deepak Kumar · Married 10 Feb 2026 · 0 children');
    section(b, 'Done', 'Couple details', 'Complete');
    section(b, 'Done', 'Children', '0 children', 2);
    section(b, 'Open', 'Nayi Pahal kit', '2 fields', 3);
    await seg(b, 'Nayi Pahal kit handed over to the couple?', ['Yes', 'No'], 0);
    const rv = reveal(b); date(rv, 'Date handed over *', '02 Nov 2026');
    await caption(b, 'No photo upload and no incentive pop-up on Mitanin.');
    bottomBar(s, null, 'Register');
    rules(s, [
      'Mitanin: high-risk check sections removed, so no HRP side effect (EligibleCoupleRegistrationDataset.kt:1461)',
      'Mitanin: kit photo upload removed, date only (Dataset:1576, 1799-1810; Fragment:172)',
      'Mitanin: no "upload photo to claim incentive" pop-up (Fragment:244)',
      'Bank details are Saksham-only, so also absent here (Dataset:1478-1480)',
    ]);
  }

  const endY = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, endY, 860,
    'ELIGIBLE COUPLE NOTES\n' +
    '• Flow: hub (01) → registration list/form (02–03, view 12, kit 13) → monthly tracking list (04) → history sheet (05) → tracking form (06, sterilisation 16) or pregnant path (07) → hands over to M2.2-03.\n' +
    '• Children are pre-filled read-only from household members (up to 9 in the dataset).\n' +
    '• FP method list = arrays.xml method_of_contraception (MiniLap commented out in code). Show the first 4, then "more".\n' +
    '• View mode (08) shows every answered field (fixes FLW-977 residual bugs).\n' +
    '• Every list has voice search (mic in the search bar) and the sort sheet (10).\n' +
    '• Gamification: EC tracking counts toward "Complete Worker" (3+ health areas per quarter); show Badge snackbar only when it moves.');
  fitSection(sec);
  return ['2.3 screens: ' + frames.length];
}
