// ---------------------------------------------------------------------------
// Stage 2.11 · Profile area + utilities
// Sources: asha_profile (AshaProfileDataset), incentives (fragment_incentives, rv_item_incentive_grouped),
// lms (video tutorial, user guide), get_ben_data, sync_dashboard (logs tab), bottom_sheet_language, help/about
// 2026-11-05 gap fix (gap_A2): M2.11-10…41 added — drawer/profile rules, logout, sync sheet + dashboard,
// incentive claim states / status dialog / PDF / per-beneficiary documents / Mitanin variant,
// ASHA own family, change work location, notifications, tray push, video player, recap controls, journey, badge pop-up.
// App-wide session-expired / account-deactivated / rooted-device / in-app-update dialogs live in stage 2.19.
// ---------------------------------------------------------------------------
Object.assign(EXTRA_ICONS, {
  pause: 'M6 19h4V5H6v14zm8-14v14h4V5h-4z',
  volume_up: 'M3 9v6h4l5 5V4L7 9H3zm13.5 3A4.5 4.5 0 0 0 14 7.97v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z',
  volume_off: 'M16.5 12A4.5 4.5 0 0 0 14 7.97v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51A8.796 8.796 0 0 0 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3 3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06a8.99 8.99 0 0 0 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4 9.91 6.09 12 8.18V4z',
  place: 'M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 0 1 0-5 2.5 2.5 0 0 1 0 5z',
  bell: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  fast_forward: 'M4 18l8.5-6L4 6v12zm9-12v12l8.5-6L13 6z',
  home: 'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z',
});

async function buildStage211(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack, fixW, circle, overlayCenter } = S;
  const { dialog, sourceSheet, actionCard, table, rules, btn, tag, caption, row, iconBtn } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.11 · Profile, incentives & utilities');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };
  // elig: 'ok' = green tick (eligible), 'docs' = red cross (documents needed), null = hidden (fixed monthly, non-Mitanin)
  const actRow = async (p, n, title, claims, amt, status, elig) => {
    const tones = { Verified: 'Success', Pending: 'Child', Rejected: 'Alert', Draft: 'Neutral' };
    const c = AL('VERTICAL', title); pad(c, 'spacing/md', 'spacing/sm'); gap(c, 'spacing/xs'); rad(c, 'radius/md'); fill(c, status === 'Rejected' ? 'color/error-container' : 'color/surface-container'); stroke(c, 'color/outline-variant'); add(p, c);
    const r = AL('HORIZONTAL'); r.counterAxisAlignItems = 'CENTER'; gap(r, 'spacing/sm'); add(c, r);
    const t = await txt(`${n}. ${title}`, 'Title/Small', 'color/on-surface'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL'; t.textAutoResize = 'HEIGHT';
    if (elig) r.appendChild(icon(elig === 'ok' ? 'check_circle' : 'close', elig === 'ok' ? 'color/success' : 'color/error', 20));
    r.appendChild(inst('tag', `Tone=${tones[status]}`, { Label: status }));
    c.appendChild(await txt(`${claims} claims · ₹${amt}`, 'Body/Small', 'color/on-surface-variant'));
    return c;
  };
  // ---- local helpers (gap fix)
  const listGroup = async (p, title, items) => {
    const g = AL('VERTICAL', title || 'Group'); g.itemSpacing = 4; add(p, g);
    if (title) g.appendChild(await txt(title, 'Label/Large', 'color/on-surface-variant'));
    const cc = AL('VERTICAL', 'Items'); pad(cc, 'spacing/xs', 'spacing/xs'); rad(cc, 'radius/lg'); fill(cc, 'color/surface-container'); stroke(cc, 'color/outline-variant'); add(g, cc);
    for (const [l, ic, opt] of items) {
      const o = opt || {};
      const it = inst('listItem', 'Active=false', { Label: l, Icon: ic, 'Show chevron': !o.danger }); add(cc, it);
      if (o.danger) { for (const x of it.findAll((n) => n.name === 'glyph')) x.fills = [paint('color/error')]; const t = it.findOne((n) => n.type === 'TEXT'); if (t) t.fills = [paint('color/error')]; }
      if (o.hidden) it.opacity = 0.35;
    }
    return cc;
  };
  const notifRow = async (p, { ic, tone, title, bodyText, time, unread }) => {
    const r = AL('HORIZONTAL', 'Notification · ' + title); r.itemSpacing = 12; pad(r, 'spacing/md', 'spacing/sm'); rad(r, 'radius/md'); fill(r, unread ? 'color/primary-subtle' : 'color/surface-container'); stroke(r, 'color/outline-variant');
    if (p) add(p, r);
    const ib = box('Icon', 36, 36); ib.cornerRadius = 18; fill(ib, `module/${tone}/bg`); ib.appendChild(icon(ic, `module/${tone}/fg`, 20)); r.appendChild(ib);
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 2; r.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    const t = await txt(title, 'Title/Small', 'color/on-surface'); col.appendChild(t); wrapText(t);
    if (bodyText) await caption(col, bodyText);
    if (time) col.appendChild(await txt(time, 'Label/Small', 'color/on-surface-variant'));
    if (unread) r.appendChild(circle(8, 'color/primary'));
    return r;
  };
  const hideSnackAction = (sn) => { const a = sn.findOne((n) => n.name === 'Action'); if (a) a.visible = false; return sn; };
  const darken = (s) => { s.fills = [paint('color/on-surface')]; };
  const abs = (s, n, x, y) => { s.appendChild(n); n.layoutPositioning = 'ABSOLUTE'; n.x = x; n.y = y; return n; };
  const statusPill = async (p, labelText, tone, value) => {
    const r = row(p, 'Claim status', 8);
    const l = await txt(labelText, 'Label/Large', 'color/on-surface-variant'); r.appendChild(l); l.layoutSizingHorizontal = 'FILL';
    tag(r, tone, value); r.appendChild(icon('info', 'color/on-surface-variant', 18));
    return r;
  };
  const monthPicker = (p) => {
    const r = row(p, 'Month picker', 8);
    const f = inst('textField', 'State=Filled', { Label: 'Month & year', Value: 'October 2026', 'Show helper': false, 'Show trailing': true }); r.appendChild(f); f.layoutSizingHorizontal = 'FILL';
    btn(r, 'Filled', 'Submit');
    return r;
  };

  // 01 Profile details (gap fix: ASHA own family card + Add ASHA head)
  {
    const s = F('M2.11-01 · My profile details', 0, 0);
    screenTop(s, 'My profile', 'ASHA · Sirsi village', 'none');
    const b = body(s); b.itemSpacing = 12;
    const h = AL('HORIZONTAL', 'Header'); h.counterAxisAlignItems = 'CENTER'; gap(h, 'spacing/md'); add(b, h);
    const av = inst('avatar', null, { Initials: 'SK' }); av.resize(64, 64); h.appendChild(av);
    const hc = AL('VERTICAL'); hc.itemSpacing = 2; h.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    hc.appendChild(await txt('Sunita Kumari', 'Title/Large', 'color/on-surface')); hc.appendChild(await txt('Emp ID 102345 · joined Apr 2019', 'Body/Small', 'color/on-surface-variant'));
    await infoCard(b, 'Personal', [['Date of birth', '12 Mar 1990 (36 yrs)'], ['Mobile', '98765 00011'], ['Father / spouse', 'Rajesh Kumar'], ['ABHA', '91-4521-7788-0001']]);
    await infoCard(b, 'Work area', [['Village', 'Sirsi (rural)'], ['Population covered', '1,104'], ['Facility', 'HWC Sirsi · 102233']]);
    await infoCard(b, 'Contacts', [['Supervisor', 'Anju Sharma · 98111 00022'], ['CHO', 'Dr. Meena · 98111 00033'], ['ANM 1', 'Rani Devi · 98111 00044'], ['AWW', 'Kusum · 98111 00055']]);
    await infoCard(b, 'Bank', [['Account', 'XXXX XXXX 4410'], ['IFSC', 'SBIN0001234']]);
    add(b, inst('sectionHeader', null, { Title: 'My own family', 'Show action': false }));
    await actionCard(b, { name: 'Sunita Kumari (ASHA head)', meta: 'HH 7012 3345 0001 · Sirsi · 4 members', tags: [['Neutral', 'ASHA family']], sync: 'Synced', icons: [['edit', 'Edit'], ['group', 'Members'], ['person_add', 'Add member']] });
    btn(b, 'Outlined', 'Add ASHA head', false, true);
    bottomBar(s, null, 'Edit profile');
    rules(s, ['Opens with a full-screen loader while the profile is pulled from the server (see M2.11-27) [AshaProfileViewModel.kt:91-95]',
      'Nothing saved yet → editable form with Save (M2.11-26); after Save → read-only with Edit [AshaProfileFragment.kt:83-95]',
      '"Add ASHA head" always shown: opens New household with isAshaFamily = Yes [fragment_asha_profile.xml:59-66]',
      'Own-family card shown once the profile is saved; only the FIRST household is listed [AshaProfileFragment.kt:85-87]',
      'Card tap = edit household · Members = member list · Add member = dialog M2.11-28; 0 members → straight to register the head [:131-143]']);
  }
  // 02 Edit profile (gap fix: locked fields as read-only)
  {
    const s = F('M2.11-02 · Edit profile', 1, 0);
    screenTop(s, 'Edit profile', 'Changes sync to your supervisor', 'none');
    const b = body(s); b.itemSpacing = 12;
    const pr = AL('HORIZONTAL', 'Photo'); pr.counterAxisAlignItems = 'CENTER'; gap(pr, 'spacing/md'); add(b, pr); pr.appendChild(kinst('Photo tile', 'State=Captured')); pr.appendChild(await txt('Change photo', 'Label/Large', 'color/primary'));
    auto(b, 'Login username', 'asha_sunita'); auto(b, 'ASHA employee ID', '102345');
    auto(b, 'ASHA name', 'Sunita Kumari'); auto(b, 'Mobile number', '98765 00011'); auto(b, 'Village', 'Sirsi');
    auto(b, 'Date of birth · age', '12 Mar 1990 · 36 yrs'); auto(b, 'Date of joining', '01 Apr 2019');
    text(b, 'Alternate mobile', '91234 00011'); text(b, 'Father / spouse name', 'Rajesh Kumar'); text(b, 'Population covered', '1,104');
    section(b, 'Closed', 'Bank details', '2 fields', 2); section(b, 'Closed', 'Contacts (supervisor, CHO, ANM, AWW)', '8 fields', 3);
    bottomBar(s, 'Cancel', 'Save');
    rules(s, ['Read-only (text, not inputs): mobile, ASHA name, village, DOB, age, date of joining [AshaProfileDataset]',
      'Editable: alternate mobile, father/spouse, bank, population, contacts',
      'Save shows a spinner, then Snackbar Success "Save Successful" or Error on failure [AshaProfileFragment.kt:170-196]',
      'Photo can also be picked straight from the avatar in the profile header (gallery, saved locally) — see M2.11-10']);
  }
  // 03 Incentives month (gap fix: month picker + Submit, claim pill, paid/unpaid totals, grouped sections, eligibility icons)
  {
    const s = F('M2.11-03 · Incentives · month', 2, 0);
    screenTop(s, 'Incentives', 'October 2026 claim', 'none');
    const b = body(s); b.itemSpacing = 12;
    monthPicker(b);
    await statusPill(b, 'Incentive claim status', 'Child', 'Pending');
    await stat(b, [['₹2,850', 'Pending (unpaid) · tap for claim form PDF', 'couple'], ['₹600', 'Processed (paid)', 'immunization']]);
    banner(b, 'Error', '1 activity rejected', 'Supervisor: "MCP card photo missing". Fix and claim again before 12 Nov.', 'Fix now');
    add(b, inst('sectionHeader', null, { Title: 'Activities (5)', Action: 'Claim status' }));
    const hd = row(b, 'Table head', 8); for (const l of ['S.No', 'Activity', 'No. of claims']) { const t = await txt(l, 'Label/Medium', 'color/on-surface-variant'); hd.appendChild(t); if (l === 'Activity') t.layoutSizingHorizontal = 'FILL'; }
    section(b, 'Open', 'Routine (fixed monthly)', 'Subtotal ₹200', 1);
    await actRow(b, 1, 'VHND mobilisation', 1, 200, 'Draft', null);
    section(b, 'Open', 'Maternal health', 'Subtotal ₹1,500', 2);
    await actRow(b, 2, 'ANC registration in 1st trimester', 3, 900, 'Verified', 'ok');
    await actRow(b, 3, 'Institutional delivery (JSY)', 1, 600, 'Verified', 'ok');
    section(b, 'Open', 'Child health', 'Subtotal ₹850', 3);
    await actRow(b, 4, 'Full immunization of child', 2, 600, 'Rejected', 'docs');
    await actRow(b, 5, 'HBNC visits completed', 1, 250, 'Pending', 'ok');
    section(b, 'Closed', 'Community processes', 'Subtotal ₹0', 4);
    bottomBar(s, null, 'Claim');
    rules(s, ['Pick any month back to Jan 2020 (dialog M2.11-16), then tap Submit to load it [IncentivesFragment.kt:191-235]',
      'Claim button window + states: see M2.11-17 [IncentiveClaimWindow.kt:28-68]',
      'Claim status pill appears once a claim exists; tap → dialog M2.11-18 [:465-504]',
      'Totals = Pending (unpaid) and Processed (paid). Tap Pending → claim form PDF (M2.11-19, not Mitanin / not Chhattisgarh)',
      'Activities grouped by programme group, collapsible, with group subtotal [IncentiveGroupSectionAdapter.kt]',
      'Fixed monthly (PER_MONTH) incentives sorted to the top; their eligibility icon is hidden on non-Mitanin',
      'Green tick = eligible, red cross = documents needed [rv_item_incentive_grouped.xml:67-77]',
      'Columns S.No / Activity / No. of claims; Amount + Doc headers only on Mitanin (M2.11-21)',
      'Row tap opens the beneficiary list only if a record has a beneficiary or (non-Mitanin) any record is not eligible; otherwise nothing [:253-278]',
      'Per-activity status label is in app (all flavours): 101 Verified · 103 Rejected · 104 Overdue · else Pending [IncentiveGroupedAdapter.kt:41-47]. "Draft" is design-only',
      'Mitanin: see M2.11-21 (month) and M2.11-42 (activity detail)']);
  }
  // 04 Rejected claim → reclaim (gap fix: real row fields + per-record upload / view / submit)
  {
    const s = F('M2.11-04 · Rejected activity → reclaim', 3, 0);
    screenTop(s, 'Full immunization of child', 'October 2026 · ₹600', 'none');
    const b = body(s); b.itemSpacing = 12;
    const rj = AL('VERTICAL', 'Rejection'); pad(rj, 'spacing/md', 'spacing/sm'); gap(rj, 'spacing/xs'); rad(rj, 'radius/lg'); fill(rj, 'color/error-container'); add(b, rj);
    rj.appendChild(await txt('Rejected by Anju Sharma · 3 Nov', 'Title/Small', 'color/on-error-container'));
    const rr = await txt('Reason: "MCP card photo missing for Riya Kumari."', 'Body/Medium', 'color/on-error-container'); rj.appendChild(rr); wrapText(rr);
    add(b, inst('sectionHeader', null, { Title: 'Records in this claim', 'Show action': false }));
    await actionCard(b, { name: '1. Aarav Kumar', meta: 'Fully immunized 28 Oct', tags: [['Success', 'Eligible']], rows: [['Ben ID', '7012 3345 0021'], ['RCH ID', '1023 4455 6677'], ['ABHA ID', '91-4521-7788-0102'], ['Amount', '₹300']] });
    await actionCard(b, { name: '2. Riya Kumari', meta: 'Fully immunized 30 Oct', tags: [['Alert', 'Documents needed']], rows: [['Ben ID', '7012 3345 0034'], ['RCH ID', '1023 4455 6690'], ['ABHA ID', '—'], ['Amount', '₹300']], icons: [['cloud_upload', 'Upload (2)'], ['visibility', 'View files']], actions: [['Tonal', 'Submit']] });
    await actionCard(b, { name: '3. Kabir Yadav', meta: 'Fully immunized 31 Oct', tags: [['Alert', 'Documents needed']], rows: [['Ben ID', '7012 3345 0047'], ['Amount', '₹300']], icons: [['cloud_upload', 'Upload (3)', null, true], ['visibility', 'View files']], actions: [['Tonal', 'Submitted', true]] });
    bottomBar(s, null, 'Reclaim ₹600');
    rules(s, ['Row: S.No, beneficiary ID + name, RCH ID, ABHA ID, amount [rv_item_incentive.xml:31-97]',
      'Upload / View files / Submit only on non-Mitanin AND record not eligible [IncentivesSubFragment.kt:60-64]',
      'Upload → source sheet M2.11-22 (camera / gallery / PDF), max 4 files, 5 MB each; count badge on the button',
      'View files → M2.11-23, delete → M2.11-24 (blocked after submit)',
      'Submit → confirm M2.11-25; no files → asks to upload first; already done → "Already submitted"; then "Submitted" (disabled), back to list + re-pull',
      'Activity-level "Reclaim ₹600" bar: not in app today – needs dev (Mitanin has month-level Reclaim, M2.11-21)']);
  }
  // 05 Learning (LMS)
  {
    const s = F('M2.11-05 · Learning · videos & guide', 4, 0);
    screenTop(s, 'Learning', 'Videos and user guide', 'none');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'Videos', 'Count 1': '24', 'Label 2': 'User guide', 'Count 2': '9', 'Label 3': 'Saved', 'Count 3': '3' }));
    const b = body(s); b.itemSpacing = 12;
    chips(b, ['All', 'Maternal', 'Child', 'App how-to']);
    for (const [t, d, tone] of [['How to do an ANC visit in the app', '4 min · Hindi · downloaded', 'maternal'], ['HBNC: checking a newborn for danger signs', '6 min · Hindi', 'child'], ['Syncing your data', '2 min · Hindi · downloaded', 'care']]) {
      const c = AL('HORIZONTAL', t); c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/sm', 'spacing/sm'); gap(c, 'spacing/sm'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(b, c);
      const th = box('Thumb', 96, 64); rad(th, 'radius/md'); fill(th, `module/${tone}/bg`); th.appendChild(icon('play', `module/${tone}/fg`, 28)); c.appendChild(th);
      const col = AL('VERTICAL'); col.itemSpacing = 2; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      const tt = await txt(t, 'Title/Small', 'color/on-surface'); col.appendChild(tt); wrapText(tt); col.appendChild(await txt(d, 'Body/Small', 'color/on-surface-variant'));
    }
    rules(s, ['Video tap → full-screen player M2.11-36',
      'App today: "User guide" is ONE bundled PDF opened in an external PDF app; toast "No PDF viewer found" if none [LmsFragment.kt:58-66, 100-131]',
      'Not reachable today: drawer item "Help & support" is hidden [HomeActivity.kt:318] — product decision needed',
      'Tabs / Saved / chips are design-only — not in app today, needs dev']);
  }
  // 06 Get beneficiary data
  {
    const s = F('M2.11-06 · Get beneficiary data', 5, 0);
    screenTop(s, 'Get beneficiary data', 'Download records from the server', 'none');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'When to use this', 'If a beneficiary is missing on this phone (new phone, re-install, or added by ANM).', null);
    text(b, 'Beneficiary ID or mobile', '7012 4455 0133', false);
    add(b, inst('button', 'Style=Filled, State=Enabled', { Label: 'Search', 'Show icon': true, Icon: 'search' }));
    recordCard(b, { name: 'Priya Kumari', meta: 'Girl · 2 yrs · HH 7012 3345 0014', tag: 'Found on server', tone: 'Success', action: 'Download' });
    const dl = S.card(b, 'Full download'); dl.appendChild(await txt('Download all records for my area', 'Title/Small', 'color/on-surface'));
    const p = S.progress(296, 0.4, 'color/primary', 8); dl.appendChild(p); p.layoutSizingHorizontal = 'FILL';
    dl.appendChild(await txt('Paused · will continue on Wi-Fi or good signal', 'Body/Small', 'color/on-surface-variant'));
  }
  // 07 Sync log (gap fix: pause/resume + clear logs)
  {
    const s = F('M2.11-07 · Sync log', 1, 1);
    screenTop(s, 'Sync log', 'For support teams', 'none');
    add(s, kinst('Tabs', 'Active=2', { 'Label 1': 'Status', 'Count 1': '6', 'Label 2': 'Log', 'Count 2': '42', 'Label 3': 'Errors', 'Count 3': '2' }));
    const b = body(s); b.itemSpacing = 0;
    const ctl = row(b, 'Log controls', 8); ctl.paddingBottom = 8;
    const pb = inst('button', 'Style=Tonal, State=Enabled', { Label: 'Pause', 'Show icon': true, Icon: 'pause' }); ctl.appendChild(pb);
    btn(ctl, 'Text', 'Clear logs');
    for (const [t, d, st] of [['09:02 · Beneficiaries', '4 records uploaded', 'Synced'], ['09:01 · EC tracking', '2 records rejected: missing LMP', 'Failed'], ['09:01 · ANC visits', '5 records uploaded', 'Synced'], ['08:59 · Download', '12 new records from server', 'Synced'], ['Yesterday 18:40 · Auto sync', 'Started on good signal', 'Syncing']]) {
      const r = AL('HORIZONTAL', t); r.counterAxisAlignItems = 'CENTER'; r.paddingTop = r.paddingBottom = 12; gap(r, 'spacing/sm'); add(b, r);
      r.strokes = [paint('color/outline-variant')]; r.strokeTopWeight = 0; r.strokeLeftWeight = 0; r.strokeRightWeight = 0; r.strokeBottomWeight = 1;
      const col = AL('VERTICAL'); col.itemSpacing = 2; r.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      col.appendChild(await txt(t, 'Title/Small', 'color/on-surface')); const dd = await txt(d, 'Body/Small', 'color/on-surface-variant'); col.appendChild(dd); wrapText(dd);
      r.appendChild(inst('syncBadge', `Status=${st}`));
    }
    const sh = inst('button', 'Style=Outlined, State=Enabled', { Label: 'Share log with support', 'Show icon': true, Icon: 'share' }); add(b, sh);
    rules(s, ['Pause freezes the live list while reading; button becomes "Resume" [SyncLogsTabFragment.kt:42-48]',
      'Clear logs clears at once, no confirmation [:38-40]',
      'Live log auto-scrolls to the newest line only if the user was already at the bottom [:78-87]',
      'Share/export with nothing to share → toast "No log files to export" (M2.11-15)',
      'Status tab = M2.11-14; empty log = M2.11-15']);
  }
  // 08 Language sheet
  {
    const s = F('M2.11-08 · Change language', 2, 1, true);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Home', { Title: 'Profile', Subtitle: 'Sunita Kumari' }));
    scrim(s);
    const sh = sheet(s, 'Language');
    sh.appendChild(await txt('Choose your language', 'Title/Large', 'color/on-surface'));
    const opts = [['English', null, true], ['हिंदी', 'deva', false], ['অসমীয়া', 'beng', false]];
    for (const [l, f, on] of opts) { const o = kinst('Radio option', `Selected=${on}`, { Label: l }); add(sh, o); if (f) { const t = o.findOne((n) => n.type === 'TEXT'); if (t) (f === 'deva' ? S.deva : S.beng)(t); } }
    const nt = await txt('The app restarts in the new language. Your data stays the same.', 'Body/Small', 'color/on-surface-variant'); add(sh, nt); nt.textAutoResize = 'HEIGHT';
    add(sh, inst('button', 'Style=Filled, State=Enabled', { Label: 'Apply', 'Show icon': false }));
    pinBottom(s, sh);
    rules(s, ['Mitanin builds: only English + हिंदी (no অসমীয়া) [HomeActivity.kt:171-180; LanguageBottomSheet.kt:55-78]',
      'Language icon is on the Home toolbar only; inner screens show a "Go to Home" icon instead (M2.11-10)']);
  }
  // 09 Help & about (gap fix: support form + delete-account rules)
  {
    const s = F('M2.11-09 · Help, about & delete account', 0, 1);
    screenTop(s, 'Help & support', 'We are here to help', 'none');
    const b = body(s); b.itemSpacing = 12;
    const g = AL('VERTICAL', 'Items'); pad(g, 'spacing/xs', 'spacing/xs'); rad(g, 'radius/lg'); fill(g, 'color/surface-container'); stroke(g, 'color/outline-variant'); add(b, g);
    for (const [l, ic] of [['Support form · opens in browser', 'form'], ['Call helpline (104)', 'call'], ['WhatsApp support', 'send'], ['Video tutorials', 'play'], ['Report a problem (with sync log)', 'warning'], ['About Sakhi · v2.14 (build 214)', 'info']]) add(g, inst('listItem', 'Active=false', { Label: l, Icon: ic }));
    banner(b, 'Warning', 'Delete my account', 'Your login will be removed. Records already synced stay with the health department.', 'Delete');
    rules(s, ['"Support" opens an external Microsoft Forms page in the browser [HomeActivity.kt:978-993]',
      'Helpline / WhatsApp / Report a problem: not in app today – needs dev',
      '"Delete my account" opens an external Microsoft Form in the browser; URL by flavour: saksham / niramay / xushrukha = form A, others = form B; toast if no browser [:954-976]',
      'In-app delete banner + confirm: not in app today – needs dev',
      '"Help & support" (LMS) and "Report crash" drawer items are hidden in code [:318-319] — product decision']);
  }

  // ===================== GAP FIX FRAMES (M2.11-10 …) =====================
  // 10 Profile menu · role & flavour rules (drawer)
  {
    const s = F('M2.11-10 · Profile menu · roles & flavours', 3, 1);
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 8; b.itemSpacing = 12;
    const bar = row(b, 'App bar icons (Home)', 12); bar.primaryAxisAlignItems = 'MAX';
    const bt = await txt('Home toolbar:', 'Label/Medium', 'color/on-surface-variant'); bar.appendChild(bt); bt.layoutSizingHorizontal = 'FILL';
    bar.appendChild(icon('translate', 'color/on-surface', 22)); bar.appendChild(icon('cloud_upload', 'color/on-surface', 22));
    const bell = AL('HORIZONTAL', 'Bell + badge'); bar.appendChild(bell); bell.appendChild(icon('bell', 'color/on-surface', 22));
    const bd = box('Badge', 18, 18); bd.cornerRadius = 9; fill(bd, 'color/error'); bd.appendChild(await txt('3', 'Label/Small', 'color/on-primary')); bell.appendChild(bd);
    const head = AL('VERTICAL', 'Profile header'); pad(head, 'spacing/lg', 'spacing/lg'); gap(head, 'spacing/sm'); rad(head, 'radius/xl'); fill(head, 'color/primary'); add(b, head);
    const hr = row(head, 'Header row', 12);
    const avw = AL('VERTICAL', 'Avatar (tap = gallery)'); avw.counterAxisAlignItems = 'MAX'; hr.appendChild(avw);
    const av = inst('avatar', null, { Initials: 'SK' }); av.resize(64, 64); avw.appendChild(av); avw.appendChild(icon('camera', 'color/on-primary', 18));
    const hc = AL('VERTICAL'); hc.itemSpacing = 2; hr.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    hc.appendChild(await txt('Sunita Kumari', 'Title/Large', 'color/on-primary'));
    hc.appendChild(await txt('ASHA · Employee ID 102345', 'Body/Medium', 'color/primary-container'));
    hc.appendChild(await txt('Facility: HSC Sirsi', 'Body/Small', 'color/primary-container'));
    await listGroup(b, 'My work', [['My profile details', 'person'], ['Incentives  (Mitanin: Monthly claim summary)', 'wallet'], ['Badges & achievements', 'star'], ['Supervisor · hidden for ASHA', 'group', { hidden: true }]]);
    await listGroup(b, 'Tools', [['Create ABHA number', 'id_card'], ['Get beneficiary data', 'cloud_upload'], ['Change work location', 'place'], ['Sync records', 'sync'], ['Help & support (LMS) · hidden', 'help', { hidden: true }]]);
    await listGroup(b, 'App', [['Language · English', 'translate'], ['Support', 'form'], ['Delete my account', 'close', { danger: true }], ['Log out', 'logout', { danger: true }]]);
    add(s, inst('bottomNav', 'Active=Profile'));
    rules(s, ['Role gate: Supervisor / ANM / CHO hide "Home" and show "Supervisor"; ASHA hides "Supervisor" [HomeActivity.kt:308-314]',
      'Mitanin: "Incentives" is renamed "Monthly claim summary" [:895-903]',
      'Avatar tap opens the gallery; photo saved locally and shown at once [:228-239]',
      'Header shows "Facility: <sub-centre>"; Employee ID falls back to login User ID when blank [:837-860]',
      'Change work location: app entry today = tap the Home toolbar title (only on Home, when the home shortcut icon is hidden) [:131-135]. Profile item: not in app today – needs dev',
      'Sync records: push; also pull if first full download not done; 2nd tap within 15 min → toast (M2.11-13) [:911-926]',
      'Bell shows unread count badge, opens Notifications (M2.11-32) [:688-696]',
      'Language icon only on Home; inner screens without bottom nav show a "Go to Home" icon [:683-686]',
      'Log out → confirm M2.11-11. Support / Delete → external forms (M2.11-09)',
      'Floating chat-support button + web sheet exist behind a code flag (off) [:113, 372-388] — not drawn',
      'Session expired / account deactivated / rooted device / in-app update: see stage 2.19']);
  }
  // 11 Logout confirmation
  {
    const s = F('M2.11-11 · Log out confirmation', 4, 1, true);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Home', { Title: 'Profile', Subtitle: 'Sunita Kumari' }));
    await dialog(s, { ic: 'logout', title: 'Log out?', body: '12 records not processed. Are you sure?', cancel: 'No', ok: 'Yes', danger: true,
      extra: async (c) => { const r = row(c, 'Unsynced warning', 8); pad(r, 'spacing/sm', 'spacing/xs'); rad(r, 'radius/md'); fill(r, 'color/warning-container'); r.appendChild(icon('cloud_upload', 'color/warning', 18)); const t = await txt('Sync first so these records are not lost.', 'Body/Small', 'color/on-surface'); r.appendChild(t); wrapText(t); } });
    rules(s, ['Always on Log out. Message = "N not processed" or "All records synced", then "Are you sure?" [HomeActivity.kt:203-226]',
      'Yes: removes FCM token, deletes beneficiary images, cancels all background work, goes to Login [:941-945]']);
  }
  // 12 Sync quick sheet
  {
    const s = F('M2.11-12 · Sync status quick sheet', 5, 1, true);
    screenTop(s, 'ANC visits', '42 pregnant women');
    scrim(s);
    const sh = sheet(s, 'Sync status');
    const t = await txt('Sync status', 'Title/Large', 'color/on-surface'); add(sh, t);
    await table(sh, null, ['Form', 'Synced', 'Un-synced', 'Syncing'], [['Beneficiary', '1,100', '4', '0'], ['ANC visit', '181', '3', '2'], ['Immunization', '412', '0', '0'], ['EC tracking', '94', '2', '0'], ['HBNC', '58', '1', '0']]);
    pinBottom(s, sh);
    rules(s, ['Opens from the cloud icon in the toolbar over any screen; double-tap safe [HomeActivity.kt:718-731]',
      'Three counts per form: Synced / Un-synced / Syncing (in flight) [bottom_sheet_sync.xml:78-95]',
      'Height fits the content, capped [SyncBottomSheetFragment.kt:39-41]']);
  }
  // 13 Sync records · already running
  {
    const s = F('M2.11-13 · Sync records · already running', 0, 2, true);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Home', { Title: 'Sync', Subtitle: 'Syncing now…' }));
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'First download not finished', 'Sync records will upload your changes and also download the rest of your area.', null);
    const c = S.card(b, 'Sync hero'); c.appendChild(await txt('12 records waiting', 'Title/Medium', 'color/on-surface'));
    const p = S.progress(296, 0.6, 'color/primary', 8); c.appendChild(p); p.layoutSizingHorizontal = 'FILL';
    const sb = inst('button', 'Style=Filled, State=Enabled', { Label: 'Sync now', 'Show icon': true, Icon: 'sync' }); add(c, sb);
    hideSnackAction(snack(s, 'Error', { Message: 'Please wait Syncing in Progress' }));
    rules(s, ['Sync pushes data; if the first full download has not finished it also pulls [HomeActivity.kt:911-926]',
      'Second tap within 15 min → toast "Please wait Syncing in Progress" (button stays enabled)']);
  }
  // 14 Sync dashboard · Status tab
  {
    const s = F('M2.11-14 · Sync log · Status tab', 1, 2);
    screenTop(s, 'Sync log', 'For support teams', 'none');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'Status', 'Count 1': '6', 'Label 2': 'Log', 'Count 2': '42', 'Label 3': 'Errors', 'Count 3': '2' }));
    const b = body(s); b.itemSpacing = 12;
    const eng = S.card(b, 'Engine status'); const er = row(eng, 'Engine', 8); er.appendChild(icon('sync', 'color/primary', 22));
    const et = await txt('Sync Running (3 / 8 workers)', 'Title/Small', 'color/on-surface'); er.appendChild(et); et.layoutSizingHorizontal = 'FILL';
    eng.appendChild(await txt('1,092 / 1,104 records synced', 'Label/Medium', 'color/on-surface-variant'));
    const p = S.progress(296, 0.95, 'color/primary', 8); eng.appendChild(p); p.layoutSizingHorizontal = 'FILL';
    const fl = AL('VERTICAL', 'Failed workers'); pad(fl, 'spacing/md', 'spacing/sm'); fl.itemSpacing = 8; rad(fl, 'radius/md'); fill(fl, 'color/error-container'); add(b, fl);
    const fr = row(fl, 'Failed head', 8); fr.appendChild(icon('warning', 'color/error', 20)); const ft = await txt('Failed: 2 worker(s) – tap to hide details', 'Title/Small', 'color/on-error-container'); fr.appendChild(ft); wrapText(ft);
    for (const [w, e] of [['Push · EC tracking', 'HTTP 500 · server error while saving'], ['Push · ANC visits', 'Timeout after 60 s'], ['Pull · Child registration', 'Blocked by earlier failure in sync chain'], ['Pull · Immunization', 'Blocked by earlier failure in sync chain']]) {
      const col = AL('VERTICAL', w); col.itemSpacing = 0; add(fl, col); col.appendChild(await txt(w, 'Label/Large', 'color/on-error-container')); await caption(col, e, 'color/on-error-container');
    }
    await table(b, 'Per form', ['Form', 'Synced', 'Un-synced', 'Syncing'], [['Beneficiary', '1,100', '4', '0'], ['ANC visit', '181', '3', '2'], ['EC tracking', '94', '2', '0']]);
    rules(s, ['Engine line: Idle / "Sync Running (x / y workers)" / Complete [SyncStatusTabFragment.kt:89-136]',
      '"Failed: N worker(s) – tap to see details" only when a worker failed; tap toggles the step list with errors [:79-83, 109-118]',
      'Steps that never ran: "Blocked by earlier failure in sync chain" [SyncDashboardViewModel.kt:37-63]',
      'Overall "x / y records synced" bar + per-form table [:58-71]']);
  }
  // 15 Sync log · empty & export toast
  {
    const s = F('M2.11-15 · Sync log · empty & export', 2, 2, true);
    screenTop(s, 'Sync log', 'For support teams', 'none');
    add(s, kinst('Tabs', 'Active=2', { 'Label 1': 'Status', 'Count 1': '0', 'Label 2': 'Log', 'Count 2': '0', 'Label 3': 'Errors', 'Count 3': '0' }));
    const b = body(s); b.itemSpacing = 12; b.counterAxisAlignItems = 'CENTER';
    const ctl = row(b, 'Log controls', 8);
    const rb = inst('button', 'Style=Tonal, State=Enabled', { Label: 'Resume', 'Show icon': true, Icon: 'play' }); ctl.appendChild(rb);
    btn(ctl, 'Text', 'Clear logs'); iconBtn(ctl, 'share', 'color/primary');
    const e = kinst('Empty state', null, { Title: 'No sync logs yet', Body: 'Trigger a sync to see activity.', Icon: 'history' }); add(b, e, false);
    const eb = e.findOne((n) => n.type === 'INSTANCE' && n.name.toLowerCase().includes('button')); if (eb) eb.visible = false;
    hideSnackAction(snack(s, 'Error', { Message: 'No log files to export' }));
    rules(s, ['Empty: "No sync logs yet. Trigger a sync to see activity." [SyncLogsTabFragment.kt:80-81]',
      'Export with nothing to share → toast "No log files to export" [:63-70]',
      'Paused state shown: button reads "Resume"']);
  }
  // 16 Incentives · pick month
  {
    const s = F('M2.11-16 · Incentives · pick month', 3, 2, true);
    screenTop(s, 'Incentives', 'October 2026 claim', 'none');
    await dialog(s, { ic: 'calendar', icColor: 'color/primary', title: 'Select month', cancel: 'Cancel', ok: 'OK',
      extra: async (c) => { select(c, 'Month', 'September'); select(c, 'Year', '2025'); await caption(c, 'Any month from January 2020 up to this month.'); } });
    rules(s, ['Month–year picker, any month back to 2020 [IncentivesFragment.kt:191-235, 631-640]',
      'OK fills the month field; the list loads only after tapping "Submit" [:378-394]']);
  }
  // 17 Incentives · claim button states
  {
    const s = F('M2.11-17 · Incentives · claim button states', 4, 2);
    screenTop(s, 'Incentives', 'Claim button · all states', 'none');
    const b = body(s); b.itemSpacing = 16;
    const st = async (title, sub, fn) => { const c = S.card(b, title); c.appendChild(await txt(title, 'Title/Small', 'color/on-surface')); await caption(c, sub); await fn(c); };
    await st('1 · Can claim', 'October 2026, viewed on 5 Nov (before 12 Nov)', async (c) => { btn(c, 'Filled', 'Claim', false, true); });
    await st('2 · Claiming', 'Request sent, waiting for the server', async (c) => { btn(c, 'Filled', 'Claiming…', true, true); });
    await st('3 · Claimed', 'Claim exists; status pill shows', async (c) => { await statusPill(c, 'Claim status', 'Child', 'Pending'); btn(c, 'Filled', 'Claimed', true, true); });
    await st('4 · Rejected → claim again', 'Month-level rejection (non-Mitanin)', async (c) => { await statusPill(c, 'Claim status', 'Alert', 'Rejected'); btn(c, 'Filled', 'Claim', false, true); });
    await st('5 · Not allowed', 'Current month, or last month after the 12th', async (c) => { const r = row(c, 'Toast', 6); r.appendChild(icon('lock', 'color/on-surface-variant', 16)); const t = await txt('No button. Toast: "Claim is not allowed for the selected month"', 'Body/Small', 'color/on-surface-variant'); r.appendChild(t); wrapText(t); });
    await st('6 · Mitanin · Reclaim', 'Month rejected (status 103)', async (c) => { await statusPill(c, 'Claim status', 'Alert', 'Rejected'); btn(c, 'Filled', 'Reclaim', false, true); });
    rules(s, ['Non-Mitanin: last month only until the 12th of this month; older months always; current month never [IncentiveClaimWindow.kt:28-68]',
      'Mitanin: only exactly last month, any day of this month',
      'Claim → "Claiming…" (disabled) → "Claimed" (disabled) [IncentivesFragment.kt:463-511]',
      'After month-level rejection Claim is enabled again (non-Mitanin) [:537-615]',
      'Mitanin Reclaim resubmits only the rejected records\' IDs [:414-422, 520-532]']);
  }
  // 18 Incentives · claim status dialog
  {
    const s = F('M2.11-18 · Incentives · claim status dialog', 5, 2, true);
    screenTop(s, 'Incentives', 'October 2026 claim', 'none');
    await dialog(s, { ic: 'info', icColor: 'color/primary', title: 'Incentive claim status', cancel: null, ok: 'OK',
      extra: async (c) => {
        const r = row(c, 'Pill', 8); tag(r, 'Alert', 'Rejected');
        for (const [l, v] of [['Rejected by', 'Anju Sharma · ASHA Supervisor'], ['Rejected on', '03 Nov 2026'], ['Claimed on', '01 Nov 2026'], ['Reason', 'MCP card photo missing for Riya Kumari']]) add(c, kinst('Info row', null, { Label: l, Value: v }));
      } });
    rules(s, ['Tap the status pill to open [IncentivesFragment.kt:107-172, 322-340]',
      'Verified: "Verified by" (name + role), verified date, claim date. Rejected: same + rejection reason',
      'Pending: hides the date and reason',
      'Mitanin: role "ASHA Supervisor" is shown as "Mitanin Trainer" [IncentivesFragment.kt:326-330]']);
  }
  // 19 Incentives · claim form PDF
  {
    const s = F('M2.11-19 · Incentives · claim form PDF', 0, 3, true);
    screenTop(s, 'Incentives', 'October 2026 claim', 'none');
    const b = body(s); b.itemSpacing = 12;
    const pv = S.card(b, 'PDF preview'); const pr = row(pv, 'Title', 8); pr.appendChild(icon('pdf', 'color/error', 24)); const pt = await txt('ASHA Incentive master claim form · Oct 2026', 'Title/Small', 'color/on-surface'); pr.appendChild(pt); wrapText(pt);
    await table(pv, null, ['S.No', 'Activity', 'Claims', '₹'], [['1', 'ANC registration', '3', '900'], ['2', 'Institutional delivery', '1', '600'], ['3', 'Full immunization', '2', '600'], ['4', 'HBNC visits', '1', '250']]);
    await caption(pv, 'Then: certification · ASHA bank & contact details · ASHA / ANM / MO signature blocks');
    snack(s, 'Success', { Message: 'Claim form saved to Downloads', Action: 'Open' });
    rules(s, ['Tap the Pending (unpaid) total to generate the PDF; saved to Downloads, snackbar "Open" [IncentivesFragment.kt:620-629, 666-1230]',
      'Not on Mitanin. Chhattisgarh (state id 8): toast "download not available" [IncentivesViewModel.kt:96]',
      'No storage permission → dialog M2.11-20 [:1460-1505]']);
  }
  // 20 Incentives · storage permission
  {
    const s = F('M2.11-20 · Incentives · storage permission', 1, 3, true);
    screenTop(s, 'Incentives', 'October 2026 claim', 'none');
    await dialog(s, { ic: 'lock', icColor: 'color/primary', title: 'Storage permission needed', body: 'Allow storage access to save the claim form PDF to Downloads.', cancel: 'Cancel', ok: 'Open settings' });
    rules(s, ['"Open settings" goes to the app\'s system settings page [IncentivesFragment.kt:1460-1505]']);
  }
  // 21 Incentives · Mitanin variant
  {
    const s = F('M2.11-21 · Incentives · month · Mitanin', 2, 3);
    screenTop(s, 'Monthly claim summary', 'October 2026 · Mitanin', 'none');
    const b = body(s); b.itemSpacing = 12;
    monthPicker(b);
    await statusPill(b, 'Incentive claim status', 'Alert', 'Rejected');
    await stat(b, [['₹2,400', 'Pending (unpaid)', 'couple'], ['₹800', 'Processed (paid)', 'immunization']]);
    await table(b, 'Routine & maternal', ['S.No', 'Activity', 'Claims', 'Amount', 'Doc'], [['1', 'Monthly honorarium', '1', '₹1,000', '✓'], ['2', 'ANC 1st trimester', '3', '₹900', '✓'], ['3', 'Full immunization', '2', '₹600', '✗'], ['4', 'HBNC visits', '1', '₹250', '✓']]);
    bottomBar(s, null, 'Reclaim');
    rules(s, ['Mitanin (mitanin / mitaninStag / mitaninUat): drawer item + screen title "Monthly claim summary" [HomeActivity.kt:895-903]',
      'Mitanin: Amount + Doc column headers shown [IncentivesFragment.kt:642-646]',
      'Mitanin: claim only exactly last month, any day of this month (FLW-1177 Chhattisgarh pilot; old days 1-3 / 1-5-after-rejection rule kept commented) [IncentiveClaimWindow.kt:41-62]',
      'Mitanin: month rejected (103) → "Reclaim" resubmits only the rejected records\' IDs [IncentivesFragment.kt:520]',
      'Mitanin: eligibility icon shown on every row incl. fixed monthly [rv_item_incentive_grouped.xml:76]',
      'Mitanin + Chhattisgarh state: tapping Pending total → toast "download not available", no PDF [:621, 1485]',
      'Mitanin: activity tap opens list only if a record has a beneficiary (eligibility ignored) [:259]',
      'Mitanin: claim status dialog shows "Mitanin Trainer" for ASHA Supervisor [:326-330]',
      'Per-activity status: Verified / Rejected / Overdue (104) / Pending']);
  }
  // 22 Incentive docs · add file
  {
    const s = F('M2.11-22 · Incentive docs · add file', 3, 3, true);
    screenTop(s, 'Full immunization of child', 'Riya Kumari · ₹300', 'none');
    await sourceSheet(s, 'Max 4 files · 5 MB each · photos are compressed');
    rules(s, ['Source: Camera / Gallery / PDF document [IncentivesSubFragment.kt:189-560]',
      'Max 4 files, 5 MB each; photos auto-compressed; oversize files listed in a toast',
      'Upload button shows the file-count badge, e.g. "Upload (2)"']);
  }
  // 23 Incentive docs · view files
  {
    const s = F('M2.11-23 · Incentive docs · view files', 4, 3, true);
    screenTop(s, 'Full immunization of child', 'Riya Kumari · ₹300', 'none');
    await dialog(s, { ic: 'visibility', icColor: 'color/primary', title: 'Uploaded files (2)', cancel: null, ok: 'Close',
      extra: async (c) => {
        for (const n of ['MCP_card_riya.jpg', 'Immunization_slip.pdf']) { const r = row(c, 'File', 8); const f = kinst('File row', 'State=Done', { 'File name': n }); r.appendChild(f); f.layoutSizingHorizontal = 'FILL'; iconBtn(r, 'delete', 'color/error'); }
      } });
    rules(s, ['Same gate as upload: non-Mitanin AND record not eligible [IncentivesSubFragment.kt:588-620]',
      'Tap a file to open it; delete icon → confirm M2.11-24; delete blocked after submission [FileViewerDialog.kt:135-167]']);
  }
  // 24 Incentive docs · delete file
  {
    const s = F('M2.11-24 · Incentive docs · delete file?', 5, 3, true);
    screenTop(s, 'Full immunization of child', 'Riya Kumari · ₹300', 'none');
    await dialog(s, { ic: 'delete', title: 'Delete File?', body: 'MCP_card_riya.jpg will be removed from this claim.', cancel: 'No', ok: 'Yes', danger: true });
    rules(s, ['Only before the record is submitted [IncentivesSubFragment.kt:689-700]']);
  }
  // 25 Incentive docs · submit confirm
  {
    const s = F('M2.11-25 · Incentive docs · submit confirm', 0, 4, true);
    screenTop(s, 'Full immunization of child', 'Riya Kumari · ₹300', 'none');
    await dialog(s, { ic: 'cloud_upload', icColor: 'color/primary', title: 'Submit documents?', body: 'Submit 2 file(s) for Riya Kumari? This cannot be undone.', cancel: 'Cancel', ok: 'Submit' });
    rules(s, ['Per beneficiary, non-Mitanin, record not eligible [rv_item_incentive.xml:160-170]',
      'No files yet → asks to upload first; already done → "Already submitted" [IncentivesSubFragment.kt:121-145]',
      'Success: button becomes "Submitted" (disabled), returns to list and re-pulls [:621-686; IncentivesViewModel.kt:249-298]']);
  }
  // 26 My profile · first time
  {
    const s = F('M2.11-26 · My profile · first time (not saved)', 1, 4);
    screenTop(s, 'My profile', 'Fill your details once', 'none');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Complete your profile', 'After you save, the profile becomes view-only with an Edit button.', null);
    auto(b, 'ASHA name', 'Sunita Kumari'); auto(b, 'Mobile number', '98765 00011'); auto(b, 'Village', 'Sirsi'); auto(b, 'Date of birth · age', '12 Mar 1990 · 36 yrs'); auto(b, 'Date of joining', '01 Apr 2019');
    text(b, 'Alternate mobile', 'Enter number', false); text(b, 'Father / spouse name', 'Enter name', false); text(b, 'Population covered', 'Enter number', false);
    section(b, 'Closed', 'Bank details', '0 of 2', 2); section(b, 'Closed', 'Contacts', '0 of 8', 3);
    btn(b, 'Outlined', 'Add ASHA head', false, true);
    bottomBar(s, null, 'Save');
    rules(s, ['recordExists = false → editable with Save; after Save → read-only + Edit [AshaProfileFragment.kt:83-95, 163-168, 180-185]',
      'Save: spinner, then toast "Save Successful" or failure']);
  }
  // 27 My profile · loading
  {
    const s = F('M2.11-27 · My profile · loading', 2, 4, true);
    screenTop(s, 'My profile', 'ASHA · Sirsi village', 'none');
    const b = body(s); b.itemSpacing = 12;
    await caption(b, 'Fetching your profile from the server…', 'color/on-surface-variant', 'Body/Medium');
    for (let i = 0; i < 3; i++) add(b, kinst('Skeleton card'));
    rules(s, ['Profile pulled from the server on open; full-screen loader until the form is ready [AshaProfileViewModel.kt:91-95; AshaProfileFragment.kt:79, 98-104]',
      'No separate fetch-failed screen in code (form shows local data) — confirm with dev']);
  }
  // 28 ASHA family · add member
  {
    const s = F('M2.11-28 · ASHA family · add member', 3, 4, true);
    screenTop(s, 'My profile', 'ASHA · Sirsi village', 'none');
    const c = await dialog(s, { ic: 'person_add', icColor: 'color/primary', title: 'Add family member', cancel: 'Cancel', ok: null,
      extra: async (d) => { await seg(d, 'Gender', ['Male', 'Female', 'Other'], 1); await radios(d, 'Relation to head', ['Daughter', 'Daughter-in-law', 'Sister', 'Grandmother'], -1); } });
    const acts = c.findOne((n) => n.name === 'Actions'); if (acts) btn(acts, 'Filled', 'OK', true);
    c.y = Math.round((800 - c.height) / 2);
    rules(s, ['Choose gender, then relation; relations that can\'t apply are hidden (father/mother already registered; spouse by head\'s marital status + gender) [AshaProfileFragment.kt:282-429]',
      'OK disabled until a relation is picked',
      'Household with 0 members → skips this and registers the head']);
  }
  // 29 Change work location
  {
    const s = F('M2.11-29 · Change work location', 4, 4);
    screenTop(s, 'Choose work location', 'Sunita Kumari', 'none');
    const b = body(s); b.itemSpacing = 12;
    auto(b, 'State', 'Bihar'); auto(b, 'District', 'Darbhanga'); auto(b, 'Block', 'Bahadurpur');
    select(b, 'Village *', 'Sirsi');
    banner(b, 'Info', 'Only your assigned villages are shown', 'Ask your supervisor if a village is missing.', null);
    bottomBar(s, null, 'Continue');
    rules(s, ['State, District, Block = fixed read-only values from the profile, in the current language; only Village is selectable [ServiceLocationViewModel.kt:92-157] (M2.13-04 shows Block as a dropdown — code: read-only)',
      'Only one village → auto-selected',
      'Continue without village → dialog M2.11-30',
      'Back: location already saved → Home; otherwise "Exit Application?" (M2.11-31)',
      'No logged-in user → bounces to Login',
      'Reached later by tapping the Home toolbar title [HomeActivity.kt:131-135]']);
  }
  // 30 Work location · missing village
  {
    const s = F('M2.11-30 · Work location · missing village', 5, 4, true);
    screenTop(s, 'Choose work location', 'Sunita Kumari', 'none');
    await dialog(s, { ic: 'warning', title: 'Missing Detail', body: 'Please select Village', cancel: null, ok: 'OK' });
    rules(s, ['incompleteLocationAlert [ServiceLocationActivity.kt]']);
  }
  // 31 Work location · exit app
  {
    const s = F('M2.11-31 · Work location · exit app?', 0, 5, true);
    screenTop(s, 'Choose work location', 'Sunita Kumari', 'none');
    await dialog(s, { ic: 'logout', title: 'Exit Application?', body: 'You have not chosen a work location yet.', cancel: 'No', ok: 'Yes' });
    rules(s, ['Back with no saved location → this dialog; Yes closes the app [ServiceLocationActivity.kt onBackPressedCallback, exitAlert]']);
  }
  // 32 Notifications panel · actions
  {
    const s = F('M2.11-32 · Notifications · swipe, clear all, open', 1, 5);
    screenTop(s, 'Notifications', '2 unread', 'none');
    const b = body(s); b.itemSpacing = 10;
    const hr = row(b, 'Head', 8); const ht = await txt('Today', 'Label/Large', 'color/on-surface-variant'); hr.appendChild(ht); ht.layoutSizingHorizontal = 'FILL'; btn(hr, 'Text', 'Clear all');
    await notifRow(b, { ic: 'wallet', tone: 'immunization', title: 'Incentive approved', bodyText: 'Your October claim of ₹2,250 was verified by Anju Sharma.', time: '10:12 · opens Incentives', unread: true });
    await notifRow(b, { ic: 'wallet', tone: 'maternal', title: 'Incentive rejected', bodyText: 'Full immunization: MCP card photo missing.', time: '09:40 · opens Incentives', unread: true });
    // swiping row
    const sw = figma.createFrame(); sw.name = 'Swipe to dismiss'; sw.clipsContent = true; fill(sw, 'color/error-container'); sw.cornerRadius = 12;
    const card = await notifRow(null, { ic: 'trophy', tone: 'child', title: 'Badge earned: Child Protector', bodyText: 'Marks read only', time: 'Yesterday' });
    sw.appendChild(card); fixW(card, 328); sw.resize(328, Math.max(64, card.height)); card.x = -96; card.y = 0;
    const di = icon('delete', 'color/error', 24); sw.appendChild(di); di.x = 328 - 48; di.y = Math.round((sw.height - 24) / 2);
    b.appendChild(sw);
    await notifRow(b, { ic: 'clock', tone: 'care', title: 'Evening summary', bodyText: 'You visited 4 families today.', time: 'Yesterday 21:00' });
    rules(s, ['Swipe left/right dismisses one notification [NotificationPanelFragment.kt attachSwipeToDismiss]',
      '"Clear all" → confirm M2.11-33',
      'Tap marks read. Incentive types (incl. approval + unrecognised incentive targets) close the panel and open Incentives; others only mark read',
      '"Tap to fix" sync routing: not in app today – needs dev',
      'Refreshes from the server on open (M2.11-34)',
      'Same panel opens from the Supervisor / ANM / CHO app with a role-coloured header']);
  }
  // 33 Notifications · clear all confirm
  {
    const s = F('M2.11-33 · Notifications · clear all?', 2, 5, true);
    screenTop(s, 'Notifications', '2 unread', 'none');
    await dialog(s, { ic: 'delete', title: 'Clear all notifications?', body: 'All notifications will be removed from this list.', cancel: 'Cancel', ok: 'Clear all', danger: true });
    rules(s, ['btnClearAll → confirmClearAll [NotificationPanelFragment.kt]']);
  }
  // 34 Notifications · empty & loading
  {
    const s = F('M2.11-34 · Notifications · empty & loading', 3, 5, true);
    screenTop(s, 'Notifications', 'Refreshing…', 'none');
    const b = body(s); b.itemSpacing = 12; b.counterAxisAlignItems = 'CENTER';
    const p = S.progress(328, 0.35, 'color/primary', 4); add(b, p);
    const e = kinst('Empty state', null, { Title: 'No notifications', Body: 'Updates about your incentives and badges will appear here.', Icon: 'bell' }); add(b, e, false);
    const eb = e.findOne((n) => n.type === 'INSTANCE' && n.name.toLowerCase().includes('button')); if (eb) eb.visible = false;
    rules(s, ['Panel refreshes from the server on open; thin progress bar while loading [NotificationPanelViewModel.kt init → refresh]',
      'layoutEmpty shown when the list is empty']);
  }
  // 35 Tray · incentive push
  {
    const s = F('M2.11-35 · Tray · incentive push', 4, 5, true);
    darken(s); add(s, inst('statusBar'));
    const b = body(s); b.itemSpacing = 10;
    b.appendChild(await txt('Thu, 5 Nov', 'Title/Medium', 'color/on-primary'));
    await notifRow(b, { ic: 'wallet', tone: 'immunization', title: 'Sakhi · Incentive approved', bodyText: 'Your October 2026 claim of ₹2,250 was verified by Anju Sharma (ASHA Supervisor). Payment will be processed this month.', time: 'now · expanded' });
    await notifRow(b, { ic: 'wallet', tone: 'maternal', title: 'Sakhi · Incentive rejected', bodyText: 'Full immunization of child: MCP card photo missing.', time: '9 min ago' });
    await notifRow(b, { ic: 'clock', tone: 'care', title: 'Sakhi · Your day in review', bodyText: 'See what you did today.', time: '9:00 PM' });
    rules(s, ['Every server push shows in the tray with expandable text; each stacks separately [FBMessaging.kt showNotification]',
      'Tap: Supervisor / ANM / CHO roles open the Supervisor app, others the ASHA app; incentive pushes deep-link to Incentives',
      'Tap marks the notification read, on cold and warm start [HomeActivity.kt:446-482, 253-268]']);
  }
  // 36 Video tutorial player
  {
    const s = F('M2.11-36 · Video tutorial player', 5, 5, true);
    darken(s); add(s, inst('statusBar'));
    const top = row(s, 'Top', 12); pad(top, 'spacing/md', 'spacing/sm'); top.appendChild(icon('chevron_left', 'color/on-primary', 24));
    const tt = await txt('How to do an ANC visit in the app', 'Title/Medium', 'color/on-primary'); top.appendChild(tt); wrapText(tt);
    const pp = box('Play/Pause', 72, 72); pp.cornerRadius = 36; pp.fills = [{ type: 'SOLID', color: { r: 0, g: 0, b: 0 }, opacity: 0.5 }]; pp.appendChild(icon('pause', 'color/on-primary', 40)); abs(s, pp, 144, 340);
    const ff = row(null, 'Double-tap hint', 6); pad(ff, 'spacing/sm', 'spacing/xs'); ff.cornerRadius = 16; ff.fills = [{ type: 'SOLID', color: { r: 0, g: 0, b: 0 }, opacity: 0.5 }]; ff.appendChild(icon('fast_forward', 'color/on-primary', 18)); ff.appendChild(await txt('+10 s', 'Label/Large', 'color/on-primary')); abs(s, ff, 250, 360);
    const sk = AL('VERTICAL', 'Seek'); sk.itemSpacing = 6; fixW(sk, 328); abs(s, sk, 16, 700);
    const pr = S.progress(328, 0.35, 'color/primary', 4); sk.appendChild(pr);
    const tr = row(sk, 'Times', 8); const t1 = await txt('1:24', 'Label/Small', 'color/on-primary'); tr.appendChild(t1); t1.layoutSizingHorizontal = 'FILL'; tr.appendChild(await txt('4:02', 'Label/Small', 'color/on-primary'));
    rules(s, ['Full-screen; auto-plays and loops [VideoTutorialFragment.kt setOnPreparedListener]',
      'Play/Pause overlay auto-hides after 3 s; tapping the video toggles play',
      'Seek bar to scrub',
      'Double-tap jumps forward 10 s']);
  }
  // 37 Monthly recap · controls & gestures
  {
    const s = F('M2.11-37 · Monthly recap · controls & gestures', 0, 6, true);
    darken(s); add(s, inst('statusBar'));
    const bars = row(s, 'Scene progress', 4); pad(bars, 'spacing/md', 'spacing/sm');
    for (let i = 0; i < 6; i++) { const p = S.progress(48, i < 2 ? 1 : (i === 2 ? 0.5 : 0), 'color/on-primary', 3); bars.appendChild(p); }
    const ctl = row(s, 'Controls', 16); pad(ctl, 'spacing/md', 'spacing/xs'); ctl.primaryAxisAlignItems = 'MAX';
    ctl.appendChild(icon('volume_up', 'color/on-primary', 24)); ctl.appendChild(icon('pause', 'color/on-primary', 24)); ctl.appendChild(icon('close', 'color/on-primary', 24));
    const sc = AL('VERTICAL', 'Scene'); sc.itemSpacing = 8; pad(sc, 'spacing/lg', 'spacing/lg'); add(s, sc);
    sc.appendChild(await txt('October 2026', 'Label/Large', 'color/primary-container'));
    const h = await txt('You protected 18 children with vaccines this month', 'Headline/Large', 'color/on-primary'); sc.appendChild(h); wrapText(h);
    const zone = async (name, x, w, l) => { const z = AL('VERTICAL', name); z.resize(w, 420); z.primaryAxisSizingMode = 'FIXED'; z.counterAxisSizingMode = 'FIXED'; z.primaryAxisAlignItems = 'MAX'; z.counterAxisAlignItems = 'CENTER'; z.paddingBottom = 12; z.fills = [{ type: 'SOLID', color: { r: 1, g: 1, b: 1 }, opacity: 0.08 }]; z.strokes = [paint('color/on-primary')]; z.dashPattern = [6, 4]; z.appendChild(await txt(l, 'Label/Medium', 'color/on-primary')); abs(s, z, x, 340); };
    await zone('Tap zone · back', 0, 120, 'Tap: back');
    await zone('Tap zone · next', 120, 240, 'Tap: next · hold: pause');
    rules(s, ['Pause/Play also pauses animation + music; hidden on the last (goodbye) scene [MonthlyRecapPlaybackFragment.kt:66, 142, 308-332]',
      'Music on/off mutes only the music; story keeps playing [:67-69, 334-357]',
      'Tap right 2/3 = next, left 1/3 = back, press-and-hold = pause; not on goodbye scene [:147, 362-400]',
      'Progress saved per scene; "Continue Recap" restarts at the saved scene [ViewModel.kt:54-90]',
      'Scene time adapts to text length; last scene never auto-advances or returns home by itself [:223-262, 440-450]']);
  }
  // 38 Monthly recap · loading
  {
    const s = F('M2.11-38 · Monthly recap · loading', 1, 6, true);
    darken(s); add(s, inst('statusBar'));
    const b = body(s); b.counterAxisAlignItems = 'CENTER'; b.paddingTop = 320; b.itemSpacing = 16;
    const p = S.progress(200, 0.4, 'color/on-primary', 4); b.appendChild(p);
    b.appendChild(await txt('Preparing your month…', 'Title/Medium', 'color/on-primary'));
    rules(s, ['Loading before the first scene; if there is no story the screen closes straight away [MonthlyRecapPlaybackFragment.kt:112-123; ViewModel.kt:55-58]']);
  }
  // 39 My journey · quiet day
  {
    const s = F('M2.11-39 · My journey · quiet day', 2, 6);
    screenTop(s, 'My journey', 'Sunday, 1 November', 'none');
    const b = body(s); b.itemSpacing = 12;
    const hero = AL('VERTICAL', 'Greeting'); pad(hero, 'spacing/lg', 'spacing/lg'); gap(hero, 'spacing/xs'); rad(hero, 'radius/xl'); fill(hero, 'color/primary'); add(b, hero);
    hero.appendChild(await txt('Good evening, Sunita', 'Title/Large', 'color/on-primary'));
    const nar = await txt('No visits recorded today. Rest well — tomorrow is a new day.', 'Body/Large', 'color/primary-container'); hero.appendChild(nar); wrapText(nar);
    add(b, inst('sectionHeader', null, { Title: 'Closest to your next badge', 'Show action': false }));
    for (const [n, d, pct] of [['Maternal Journey', '3 of 4 ANC visits', 0.75], ['Child Fully Protected', '2 of 3 children', 0.66], ['Community Voice', '1 of 2 meetings', 0.5]]) {
      const c = S.card(b, n); const r = row(c, 'Head', 8); r.appendChild(icon('trophy', 'color/accent', 20)); const t = await txt(n, 'Title/Small', 'color/on-surface'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL';
      const p = S.progress(296, pct, 'color/primary', 6); c.appendChild(p); p.layoutSizingHorizontal = 'FILL'; await caption(c, d);
    }
    rules(s, ['Narrative changes with today\'s work: zero / one / busy messages (M2.1-05 = busy) [JourneyFragment.kt:39-48]',
      'Badge row = top 3 badges closest to their next milestone (in progress, not only earned) [JourneyViewModel.kt:38-43]']);
  }
  // 40 Badge celebration · as built
  {
    const s = F('M2.11-40 · Badge celebration · as built', 3, 6, true);
    screenTop(s, 'ANC visits', '42 pregnant women');
    scrim(s);
    const cols = ['module/maternal/fg', 'module/immunization/fg', 'module/child/fg', 'module/couple/fg'];
    for (let i = 0; i < 24; i++) { const r = figma.createRectangle(); r.name = 'Confetti'; r.resize(6, 12); r.cornerRadius = 2; r.fills = [paint(cols[i % cols.length])]; r.rotation = (i * 37) % 90; abs(s, r, (i * 53) % 340, 80 + ((i * 97) % 600)); }
    const c = AL('VERTICAL', 'Celebration card'); fixW(c, 280); pad(c, 'spacing/xl', 'spacing/xl'); gap(c, 'spacing/md'); c.counterAxisAlignItems = 'CENTER'; rad(c, 'radius/xl'); fill(c, 'color/surface-container');
    c.appendChild(await txt('Congratulations!', 'Headline/Large', 'color/on-surface'));
    const md = box('Badge art', 104, 104); md.cornerRadius = 52; fill(md, 'module/maternal/bg'); md.appendChild(icon('trophy', 'module/maternal/fg', 56)); c.appendChild(md);
    c.appendChild(await txt('Maternal Journey', 'Title/Large', 'color/on-surface'));
    tag(c, 'Maternal', 'Level 2');
    overlayCenter(s, c);
    rules(s, ['Confetti + badge art + name; "Level N" only when level > 1 [HomeActivity.kt:763-809]',
      'No buttons: closes by itself after a fixed time, or on a tap outside',
      'M2.1-04 buttons "See my badges / Close" and story line: not in app today – needs dev']);
  }
  // 41 Start-up permissions
  {
    const s = F('M2.11-41 · Start-up permissions', 4, 6, true);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Home', { Title: 'Sakhi', Subtitle: 'Sirsi · tap to change village' }));
    await dialog(s, { ic: 'call', icColor: 'color/primary', title: 'Allow Sakhi to make and manage phone calls?', body: 'Used for the call buttons on beneficiary cards.', cancel: 'Don\'t allow', ok: 'Allow' });
    rules(s, ['System prompts on every launch for any permission not yet granted: phone call, storage, microphone, camera [HomeActivity.kt:532-550]',
      'No in-app explainer screen in code']);
  }

  // 42 Incentives · activity detail · Mitanin
  {
    const s = F('M2.11-42 · Incentives · activity detail · Mitanin', 5, 6);
    screenTop(s, 'Full immunization of child', 'October 2026 · Mitanin', 'none');
    const b = body(s); b.itemSpacing = 12;
    await actionCard(b, { name: '1. Aarav Sahu', meta: 'Fully immunized 28 Oct', tags: [['Success', 'Eligible']], rows: [['Ben ID', '7012 3345 0021'], ['RCH ID', '1023 4455 6677'], ['ABHA ID', '91-4521-7788-0102'], ['Amount', '₹300']] });
    await actionCard(b, { name: '2. Riya Verma', meta: 'Fully immunized 30 Oct', tags: [['Alert', 'Not eligible']], rows: [['Ben ID', '7012 3345 0034'], ['RCH ID', '1023 4455 6690'], ['ABHA ID', '—'], ['Amount', '₹300']] });
    rules(s, ['Mitanin: no Upload / View files / Submit on any row [rv_item_incentive.xml:114, 170]',
      'Mitanin: row fields same as other flavours (S.No, Ben ID + name, RCH, ABHA, amount)',
      'Corrections go through month-level "Reclaim" (M2.11-21)']);
  }

  const endY = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, endY, 860,
    'PROFILE & INCENTIVE NOTES\n' +
    '• Profile (01–02) shows AshaProfileDataset grouped as Personal / Work area / Contacts / Bank; ID fields are read-only. First-time (26), loading (27), own family (01, 28).\n' +
    '• Incentives (03–04, 16–25): month picker + Submit, claim pill + status dialog, paid/unpaid totals, grouped activities, claim window states, PDF, per-beneficiary documents. Mitanin variant = 21 (month) + 42 (activity detail).\n' +
    '• Drawer rules (role / flavour) = 10. Logout = 11. Sync sheet / dashboard = 12–15, 07.\n' +
    '• Notifications = 32–35 (M2.1-10 is the base panel). Video player = 36. Recap controls = 37–38. Journey quiet day = 39. Badge pop-up as built = 40.\n' +
    '• Session expired / account deactivated / rooted device / in-app update dialogs: stage 2.19.\n' +
    '• Dashboard = pattern P8 (M2.0-P8). Language change (08) uses the native script for each option.');
  fitSection(sec);
  return ['2.11 screens: ' + frames.length];
}
