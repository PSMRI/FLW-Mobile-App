// ---------------------------------------------------------------------------
// Stage 2.12 · Supervisor module
// Sources: ui/asha_supervisor/* (SupervisorHome, IncentiveDashboard, IncentiveVerification, WorkerDetail,
// BeneficiaryDetail, SupervisorProfile), fragment_incentive_dashboard.xml, fragment_worker_detail.xml
// 2026-11-05 gap fix: Incentive/KPI tabs, KPI dashboard, tappable tiles + sub-centre list, decided rows,
// grouped activities, multi-select reject reasons, read-only/empty detail, Mitanin Trainer variants,
// shell dialogs (no internet, logout, exit, unsupported device), language sheet. See gap_G_abha_supervisor (B1–B44).
// ---------------------------------------------------------------------------
async function buildStage212(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack, progress } = S;
  const { rules, dialog, caption, btn, tag, row, actionCard, table, optionSheet } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.12 · Supervisor');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };
  const supTop = (s, title, sub) => { add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Home', { Title: title, Subtitle: sub })); bar.fills = [paint('module/care/bg')]; return bar; };
  // Mitanin: toolbar uses supervisor_toolbar #037E62 (SupervisorActivity.kt:188-191) + green logo
  const mitTop = (s, title, sub) => { const bar = supTop(s, title, sub); bar.fills = [{ type: 'SOLID', color: { r: 3 / 255, g: 126 / 255, b: 98 / 255 } }]; for (const t of bar.findAll((n) => n.type === 'TEXT')) t.fills = [{ type: 'SOLID', color: { r: 1, g: 1, b: 1 } }]; return bar; };
  const statusTile = async (p, n, l, sub, tone, showSub = true) => {
    const c = AL('VERTICAL', l); pad(c, 'spacing/md', 'spacing/sm'); c.itemSpacing = 2; rad(c, 'radius/lg'); fill(c, `module/${tone}/bg`); p.appendChild(c); c.layoutSizingHorizontal = 'FILL';
    const top = AL('HORIZONTAL', 'Count row'); top.counterAxisAlignItems = 'CENTER'; c.appendChild(top); top.layoutSizingHorizontal = 'FILL';
    const nn = await txt(n, 'Number/Medium', `module/${tone}/fg`); top.appendChild(nn); nn.layoutSizingHorizontal = 'FILL';
    top.appendChild(icon('chevron_right', `module/${tone}/fg`, 20));
    c.appendChild(await txt(l, 'Title/Small', 'color/on-surface'));
    if (showSub && sub) { const st = await txt(sub, 'Body/Small', 'color/on-surface-variant'); c.appendChild(st); wrapText(st); }
  };
  const ashaRow = async (p, name, area, amt, status, tone) => {
    const c = AL('VERTICAL', name); pad(c, 'spacing/md', 'spacing/sm'); c.itemSpacing = 6; rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(p, c);
    const r = AL('HORIZONTAL', 'Head'); r.counterAxisAlignItems = 'CENTER'; gap(r, 'spacing/sm'); add(c, r);
    r.appendChild(inst('avatar', null, { Initials: name.split(' ').map((w) => w[0]).join('').slice(0, 2) }));
    const col = AL('VERTICAL'); col.itemSpacing = 2; r.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    col.appendChild(await txt(name, 'Title/Small', 'color/on-surface')); col.appendChild(await txt(amt == null ? area : `${area} · ₹${amt}`, 'Body/Small', 'color/on-surface-variant'));
    r.appendChild(inst('tag', `Tone=${tone}`, { Label: status }));
    return c;
  };
  // decided rows: verified / rejected detail lines (AshaWorkerAdapter.kt:60-110)
  const decidedRow = async (p, name, area, amt, status, tone, lines) => {
    const c = await ashaRow(p, name, area, amt, status, tone);
    for (const l of lines) { const t = await txt(l, 'Body/Small', 'color/on-surface-variant'); c.appendChild(t); wrapText(t); }
    return c;
  };
  // two-tab strip (Incentive Dashboard / KPI Dashboard)
  const tabs2 = async (s, labels, active) => {
    const r = AL('HORIZONTAL', 'Tabs'); fill(r, 'color/surface'); add(s, r);
    r.strokes = [paint('color/outline-variant')]; r.strokeTopWeight = 0; r.strokeLeftWeight = 0; r.strokeRightWeight = 0; r.strokeBottomWeight = 1;
    for (let i = 0; i < labels.length; i++) {
      const on = i === active;
      const it = AL('VERTICAL', 'Tab ' + labels[i]); it.counterAxisAlignItems = 'CENTER'; it.itemSpacing = 10; it.paddingTop = 13; r.appendChild(it); it.layoutSizingHorizontal = 'FILL';
      it.appendChild(await txt(labels[i], 'Title/Small', on ? 'color/primary' : 'color/on-surface-variant'));
      const ind = figma.createRectangle(); ind.name = 'Indicator'; ind.resize(80, 3); ind.topLeftRadius = 3; ind.topRightRadius = 3; ind.fills = on ? [paint('color/primary')] : []; it.appendChild(ind);
    }
    return r;
  };
  const monthCard = async (p, title, month) => {
    const c = AL('HORIZONTAL', 'Month card'); c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 8; pad(c, 'spacing/md', 'spacing/sm'); rad(c, 'radius/md'); fill(c, 'color/primary-subtle'); add(p, c);
    c.appendChild(icon('calendar', 'color/primary', 20));
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 0; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    col.appendChild(await txt(title, 'Label/Medium', 'color/on-surface-variant')); col.appendChild(await txt(month, 'Title/Small', 'color/on-surface'));
    return c;
  };
  const strip = async (p, s) => { const t = await txt(s, 'Label/Large', 'color/on-surface-variant'); add(p, t); wrapText(t); return t; };
  const groupHead = async (p, title, n, total, open = true) => {
    const r = AL('HORIZONTAL', 'Group · ' + title); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 8; pad(r, 'spacing/md', 'spacing/sm'); rad(r, 'radius/md'); fill(r, 'color/surface-container-high'); add(p, r);
    r.appendChild(icon(open ? 'chevron_down' : 'chevron_right', 'color/on-surface-variant', 20));
    const t = await txt(title, 'Title/Small', 'color/on-surface'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL';
    r.appendChild(await txt(`${n} claims · ₹${total}`, 'Label/Medium', 'color/on-surface-variant'));
    return r;
  };
  const headerIds = async (p, emp, reviewer) => { await caption(p, emp); await caption(p, reviewer); };

  // 01 Supervisor home / incentive dashboard
  {
    const s = F('M2.12-01 · Supervisor home · verification dashboard', 0, 0);
    supTop(s, 'Rajnagar', 'Supervisor · 4 sub-centres · 32 ASHAs');
    await tabs2(s, ['Incentive Dashboard', 'KPI Dashboard'], 0);
    const b = body(s); b.itemSpacing = 12;
    const fr = AL('HORIZONTAL'); gap(fr, 'spacing/sm'); add(b, fr);
    const m = S.select(fr, 'Month', 'October 2026'); m.layoutSizingHorizontal = 'FILL'; const sc = S.select(fr, 'Sub-centre', 'All sub-centres'); sc.layoutSizingHorizontal = 'FILL';
    banner(b, 'Warning', '6 claims are overdue', 'Verify by the 12th so ASHAs are paid on time.', 'Show');
    const r0 = AL('HORIZONTAL', 'Row 0'); gap(r0, 'spacing/xs'); add(b, r0);
    await statusTile(r0, '32', 'Total ASHAs', 'Tap to view ASHAs grouped', 'people');
    const r1 = AL('HORIZONTAL', 'Row 1'); gap(r1, 'spacing/xs'); add(b, r1);
    await statusTile(r1, '9', 'Pending', 'Awaiting your verification', 'child'); await statusTile(r1, '6', 'Overdue', 'Past verify-by 12th', 'disease');
    const r2 = AL('HORIZONTAL', 'Row 2'); gap(r2, 'spacing/xs'); add(b, r2);
    await statusTile(r2, '14', 'Verified', 'Forwarded to CHO / ANM', 'immunization'); await statusTile(r2, '2', 'Rejected', 'Back with ASHA for correction', 'records');
    const r3 = AL('HORIZONTAL', 'Row 3'); gap(r3, 'spacing/xs'); add(b, r3);
    await statusTile(r3, '1', 'Not claimed', 'ASHAs yet to submit', 'village');
    add(b, inst('sectionHeader', null, { Title: 'Sub-centres under you (4)', 'Show action': false }));
    for (const [n, c] of [['SC Sirsi', 9], ['SC Rajnagar', 8], ['SC Bhairopur', 8], ['SC Kamalpur', 7]]) {
      const r = AL('HORIZONTAL', n); r.counterAxisAlignItems = 'CENTER'; pad(r, 'spacing/md', 'spacing/sm'); gap(r, 'spacing/sm'); rad(r, 'radius/md'); fill(r, 'color/surface-container'); stroke(r, 'color/outline-variant'); add(b, r);
      const t = await txt(n, 'Title/Small', 'color/on-surface'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL';
      r.appendChild(await txt(`${c} ASHAs`, 'Body/Small', 'color/on-surface-variant')); r.appendChild(icon('chevron_right', 'color/on-surface-variant', 20));
    }
    add(b, inst('button', 'Style=Filled, State=Enabled', { Label: 'Start verifying (15)', 'Show icon': false }));
    rules(s, [
      'Home has two tabs: Incentive Dashboard / KPI Dashboard (M2.12-07) (SupervisorHomeFragment.kt:118-142)',
      'Toolbar title = current village name in the chosen language (not "Namaste") (:145-160). Home shows Language icon + notification bell with unread badge; inner screens show a Home icon instead of Language (SupervisorActivity.kt:365-417)',
      'Every tile (Total, Pending, Overdue, Verified, Rejected, Unclaimed) opens the ASHA list filtered to that status (IncentiveDashboardFragment.kt:331-345)',
      'Sub-centre list: tap → verification list for that sub-centre. Picker includes "All sub-centres"; empty list → toast "No sub-centres available" (:122-142, 250-283)',
      'Online-only: offline → blocking dialog M2.12-08; server errors → toast (:179-209)',
      'Back on home → "Exit application?" (M2.12-11)',
      'Long-press KPI tab: hidden dev-mode sheet / toast "Dev Mode Disabled!" (internal only) (:128-140)',
      'Pending records auto-push silently (max once / 2 min, online); first full pull on first launch → progress on KPI tab (SupervisorActivity.kt:233-244)',
      'Overdue banner and "Start verifying" button: Not in app today – needs dev',
      'Mitanin variant → M2.12-13',
    ]);
  }
  // 02 ASHA list by status
  {
    const s = F('M2.12-02 · ASHAs to verify', 1, 0);
    supTop(s, 'Incentive verification', 'October 2026');
    add(s, kinst('Tabs', 'Active=1', { 'Label 1': 'To verify', 'Count 1': '15', 'Label 2': 'Verified', 'Count 2': '14', 'Label 3': 'Rejected', 'Count 3': '2' }));
    const b = body(s); b.itemSpacing = 10;
    await monthCard(b, 'ASHA monthly detail', 'Month: October, 2026');
    await strip(b, 'Verified: 14 · Pending: 9 · Rejected: 2');
    add(b, inst('searchBar', null, { Placeholder: 'Search ASHA name or ID' }));
    chips(b, ['All', 'Overdue', 'SC Sirsi']);
    await ashaRow(b, 'Sunita Kumari', 'SC Sirsi · 5 activities', '3,450', 'Overdue', 'Alert');
    await ashaRow(b, 'Rani Yadav', 'SC Sirsi · 4 activities', '2,100', 'Pending', 'Child');
    await decidedRow(b, 'Kusum Devi', 'SC Rajnagar · 6 activities', '4,050', 'Verified', 'Success', ['Verified on 06 Nov 2026', 'By: Anju Sharma (Supervisor)']);
    await decidedRow(b, 'Lalita Kumari', 'SC Rajnagar · 3 activities', '1,350', 'Rejected', 'Alert', ['Rejected on 05 Nov 2026 · By: Anju Sharma (Supervisor)', 'Reason: Incomplete documentation, Other', 'Other: MCP card photo missing']);
    const un = await ashaRow(b, 'Meera Paswan', 'SC Rajnagar · not submitted', null, 'Not claimed', 'Neutral'); un.opacity = 0.6;
    rules(s, [
      'List is opened from a dashboard tile and filtered to that status (Pending / Overdue / Verified / Rejected / Unclaimed / All); search by name only',
      'Status strip "Verified: n · Pending: n · Rejected: n"; from Overdue it shows "Overdue: …" — code bug: hard-coded "Overdue: 0" (IncentiveVerificationFragment.kt:156-174)',
      'Verified rows: "Verified on <date>" + "By: <name> (role)"; Rejected rows: date + reasons + other reason + by whom (AshaWorkerAdapter.kt:60-110)',
      'Unclaimed ASHAs cannot be opened and the amount is hidden (:127-130; AshaWorkerAdapter.kt:119-123)',
      'List refreshes when returning from the detail after verify / reject (onResume :105-113)',
      'Mitanin: plain month card "Mitanin monthly detail", search hint "Search Mitanin", verifier shown as "Mitanin Trainer" (:66-99)',
      'Tabs and filter chips: Not in app today – needs dev (code filters by the tile tapped)',
    ]);
  }
  // 03 ASHA monthly detail (worker detail)
  {
    const s = F('M2.12-03 · ASHA monthly detail', 2, 0);
    supTop(s, 'Sunita Kumari', 'October 2026 · SC Sirsi');
    const b = body(s); b.itemSpacing = 12;
    await headerIds(b, 'EmployeeID: 40517, October 2026', 'Supervisor ID: 55021');
    await stat(b, [['5', 'Activities claimed', 'care'], ['₹3,450', 'Total claimed', 'couple'], ['12 Nov', 'Verify by', 'disease']]);
    add(b, inst('sectionHeader', null, { Title: 'Activities', 'Show action': false }));
    await groupHead(b, 'Maternal health', 4, '1,500');
    await table(b, null, ['S.No', 'Activity', 'No. of claims', 'Amount'], [['1', 'ANC registration in 1st trimester', '3', '₹900'], ['2', 'Institutional delivery (JSY)', '1', '₹600']]);
    await groupHead(b, 'Child health', 3, '850');
    await table(b, null, ['S.No', 'Activity', 'No. of claims', 'Amount'], [['3', 'Full immunization of child', '2', '₹600'], ['4', 'HBNC visits completed', '1', '₹250']]);
    await groupHead(b, 'Community', 1, '200', false);
    add(b, kinst('Checkbox option', 'Checked=true', { Label: "I have checked the documents on the ASHA's phone" }));
    bottomBar(s, 'Reject', 'Verify all');
    const note2 = await txt('Verify commits every activity in one action.', 'Body/Small', 'color/on-surface-variant'); add(b, note2); note2.textAutoResize = 'HEIGHT';
    rules(s, [
      'Activities grouped by programme: collapsible header with claim count + group total; columns S.No / Activity / No. of claims / Amount (GroupedActivityAdapter.kt:28-104)',
      'Header: "EmployeeID: <id>, <Month Year>" + reviewer ID ("Supervisor ID" / Mitanin "Trainer ID") (WorkerDetailFragment.kt:106-108, 134-135)',
      'Tap activity row → claim records (M2.12-05)',
      'Verify without the documents tick → toast "Please verify documents before approving"; nothing to verify → "No records to verify" (:313-336)',
      'Success → toast, then auto-return to the ASHA list',
      'Already decided month → read-only (M2.12-15); no activities → empty (M2.12-16); Mitanin → per-activity ticks (M2.12-14)',
    ]);
  }
  // 04 Reject with reason (multi-select + Other)
  {
    const s = F('M2.12-04 · Reject with reason', 3, 0, true);
    supTop(s, 'Sunita Kumari', 'October 2026 · SC Sirsi');
    const b = body(s); await stat(b, [['5', 'Activities', 'care'], ['₹3,450', 'Claimed', 'couple']]);
    scrim(s);
    const sh = sheet(s, 'Reject');
    sh.appendChild(await txt('Reject October claim?', 'Title/Large', 'color/on-surface'));
    await checks(sh, 'Select rejection reasons *', [['Incomplete documentation', true], ['Incorrect data', false], ['Beneficiary data mismatch', false], ['Calculation error', false], ['Duplicate claim', false], ['Ineligible activity', false], ['Outside service period', false], ['Other', true]]);
    S.tf(sh, 'Focused', { Label: "Reason for 'Other' *", Value: 'MCP card photo missing for Riya Kumari', Helper: 'Shown only when "Other" is ticked.', 'Show helper': true });
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(sh, r);
    const a = inst('button', 'Style=Outlined, State=Enabled', { Label: 'Cancel', 'Show icon': false }); r.appendChild(a); a.layoutSizingHorizontal = 'FILL';
    const c = inst('button', 'Style=Filled, State=Enabled', { Label: 'Confirm rejection', 'Show icon': false }); r.appendChild(c); c.layoutSizingHorizontal = 'FILL';
    pinBottom(s, sh);
    rules(s, [
      'Multi-select checklist of 7 fixed reasons + "Other" (WorkerDetailFragment.kt:208-221, 360-395)',
      'Free-text box shows and is required only when "Other" is ticked',
      'Toasts: "Please select at least one rejection reason", "Please provide the reason for \'Other\'"',
      'Success → toast + return to the ASHA list; ASHA sees the reasons (M2.11-04)',
    ]);
  }
  // 05 Activity records (beneficiary detail)
  {
    const s = F('M2.12-05 · Claim records → beneficiary', 4, 0);
    supTop(s, 'Full immunization of child', 'Sunita Kumari · 2 claims');
    const b = body(s); b.itemSpacing = 12;
    await strip(b, '3 records · ₹900');
    await actionCard(b, { name: 'Aarav Kumar', meta: 'Ben ID 7012 4455 0091', rows: [['RCH ID', '1210 0456 7781'], ['ABHA number', '91-4521-7788-3012'], ['Amount', '₹300']] });
    await actionCard(b, { name: 'Riya Kumari', meta: 'Ben ID 7012 4455 0127', rows: [['RCH ID', '1210 0456 7790'], ['ABHA number', '—'], ['Amount', '₹300']] });
    await actionCard(b, { name: 'VHND mobilisation', initials: 'VH', meta: 'Meeting claim · no beneficiary', rows: [['Claim date', '14 Oct 2026'], ['Amount', '₹300']] });
    rules(s, [
      'Row: Ben ID, Name, RCH ID, ABHA number, amount; meeting / register claims with no beneficiary show "Claim date" (BeneficiaryAdapter.kt:36-70)',
      'Summary "<n> · ₹<total>"; empty state when there are no records; after any decision return to the activity list (BeneficiaryDetailFragment.kt:218-256)',
      'Document-status tags and vaccines info card: Not in app today – needs dev (no such data in the payload)',
      'Mitanin per-row ✓ / ✕ → M2.12-17',
    ]);
  }
  // 06 Supervisor profile
  {
    const s = F('M2.12-06 · Supervisor profile', 5, 0);
    supTop(s, 'My profile', 'Supervisor');
    const b = body(s); b.itemSpacing = 12;
    const pc = await person(b, 'AS', 'Anju Sharma', 'Supervisor · EMP. ID 55021', [['Neutral', '4 sub-centres'], ['Neutral', '32 ASHAs']]);
    const cam = row(pc, 'Change photo', 6); cam.appendChild(icon('camera', 'color/primary', 18)); cam.appendChild(await txt('Tap avatar to change photo', 'Label/Medium', 'color/primary'));
    await infoCard(b, 'Contact info', [['Mobile', '98111 22334'], ['Email', 'anju.sharma@nhm.gov.in']]);
    await infoCard(b, 'Posting', [['District', 'Darbhanga'], ['Block', 'Rajnagar'], ['Sub-centre', 'SC Sirsi']]);
    await infoCard(b, 'Bank details', [['Account no.', '—'], ['IFSC', '—']]);
    const g = AL('VERTICAL', 'Items'); pad(g, 'spacing/xs', 'spacing/xs'); rad(g, 'radius/lg'); fill(g, 'color/surface-container'); stroke(g, 'color/outline-variant'); add(b, g);
    for (const [l, ic, d] of [['Sync records', 'sync'], ['Create ABHA number', 'id_card'], ['Help & support', 'help'], ['Language · English', 'translate'], ['Delete my account', 'close', true], ['Log out', 'logout', true]]) {
      const it = inst('listItem', 'Active=false', { Label: l, Icon: ic, 'Show chevron': !d }); add(g, it);
      if (d) { for (const x of it.findAll((n) => n.name === 'glyph')) x.fills = [paint('color/error')]; const t = it.findOne((n) => n.type === 'TEXT'); if (t) t.fills = [paint('color/error')]; }
    }
    await caption(b, 'APK Version 2.14.0');
    rules(s, [
      'Contact (mobile, email) + posting (district, block, sub-centre) + EMP. ID (SupervisorProfileFragment.kt:66-93)',
      'Bank details labels exist in the layout but the code never fills them (shows blank) — product to decide: show or drop (fragment_supervisor_profile.xml:397-506)',
      'Tap the avatar in the menu header → photo picker (SupervisorActivity.kt:442-459)',
      'Help & support → LMS / video tutorials screen (not M2.11-09) (:514-520)',
      'Delete my account → opens an external Microsoft Form in the browser (prod vs non-prod URL); no in-app delete (:529-547)',
      'Log out → confirm (M2.12-09). Language → M2.12-10',
      'Mitanin: role subtitle "Mitanin Trainer" for role "ASHA Supervisor", else the raw role (e.g. ANM); green NHM logo (SupervisorProfileFragment.kt:71-86)',
    ]);
  }

  // ===== NEW FRAMES (gap fix) =====
  // 07 KPI dashboard
  {
    const s = F('M2.12-07 · KPI dashboard', 0, 1);
    supTop(s, 'Rajnagar', 'Supervisor · 4 sub-centres · 32 ASHAs');
    await tabs2(s, ['Incentive Dashboard', 'KPI Dashboard'], 1);
    const b = body(s); b.itemSpacing = 12;
    const dl = AL('VERTICAL', 'Download progress'); dl.itemSpacing = 6; pad(dl, 'spacing/md', 'spacing/sm'); rad(dl, 'radius/md'); fill(dl, 'color/primary-subtle'); add(b, dl);
    dl.appendChild(await txt('Downloading… 64%', 'Label/Large', 'color/primary'));
    add(dl, progress(296, 0.64));
    const fr = AL('HORIZONTAL', 'Filters 1'); gap(fr, 'spacing/sm'); add(b, fr);
    const m = S.select(fr, 'Month / Year', 'Oct 2026'); m.layoutSizingHorizontal = 'FILL'; const v = S.select(fr, 'Village', 'Rajnagar'); v.layoutSizingHorizontal = 'FILL';
    const fr2 = AL('HORIZONTAL', 'Filters 2'); fr2.counterAxisAlignItems = 'CENTER'; gap(fr2, 'spacing/sm'); add(b, fr2);
    const a = S.select(fr2, 'ASHA worker', 'All'); a.layoutSizingHorizontal = 'FILL'; btn(fr2, 'Tonal', 'Submit');
    tileGrid(b, [
      ['household', 'Households', '248', 'group'], ['people', 'Beneficiaries', '1,104', 'person'],
      ['couple', 'Eligible couples', '96', 'group'], ['maternal', 'Pregnant women', '42', 'pregnant'],
      ['maternal', 'ANC visits', '186', 'calendar'], ['disease', 'HRP women', '14', 'warning'],
      ['maternal', 'Deliveries', '18', 'check_circle'], ['immunization', 'Routine immunization', '412', 'vaccine'],
      ['care', 'NCD screened', '320', 'form'], ['disease', 'NCD priority', '27', 'warning'],
      ['disease', 'TB cases', '6', 'shield'], ['records', 'Incentive verification', '15', 'check'],
    ]);
    rules(s, [
      '12 tiles with live counts; each opens the matching list read-only for the supervisor (SupervisorFragment.kt:251-263; IconDataset.kt:225-299)',
      '"Downloading… x%" shows while the first full pull / push worker runs (:212-245)',
      'Filter bar: Village auto-selected when only one. Sub-centre filter invisible. Only Village is wired — Month, ASHA worker and Submit have no handler: product to decide (design or remove) (:157-175)',
    ]);
  }
  // 08 No internet (blocking)
  {
    const s = F('M2.12-08 · No internet (blocking)', 1, 1, true);
    supTop(s, 'Rajnagar', 'Supervisor · 4 sub-centres · 32 ASHAs');
    const b = body(s); await stat(b, [['32', 'Total ASHAs', 'people'], ['9', 'Pending', 'child']]);
    await dialog(s, { ic: 'wifi_off', icColor: 'color/on-surface-variant', title: 'No internet connection', body: 'Please check your network settings. This dialog will close automatically when connectivity is restored.', cancel: null, ok: null });
    rules(s, ['Incentive dashboard is online-only. Dialog cannot be dismissed; closes itself and data reloads when the connection returns (NoInternetDialog.kt:8-17; IncentiveDashboardFragment.kt:179-209)']);
  }
  // 09 Logout confirmation
  {
    const s = F('M2.12-09 · Logout confirmation', 2, 1, true);
    supTop(s, 'My profile', 'Supervisor');
    const b = body(s); await person(b, 'AS', 'Anju Sharma', 'Supervisor · EMP. ID 55021');
    await dialog(s, { ic: 'logout', title: 'Logout', body: 'Are you sure to logout?', cancel: 'No', ok: 'Yes', danger: true });
    rules(s, ['From menu or profile Logout row (SupervisorActivity.kt:126-140; SupervisorProfileFragment.kt:32-46)', 'Yes also clears the push token, beneficiary images and background jobs']);
  }
  // 10 Language sheet
  {
    const s = F('M2.12-10 · Language (by flavour)', 3, 1, true);
    supTop(s, 'Rajnagar', 'Supervisor');
    await dialog(s, { ic: null, title: 'Choose application language', cancel: 'Cancel', ok: null, extra: async (c) => { await radios(c, null, ['English', 'हिन्दी (Hindi)', 'বাংলা (Bangla)', 'অসমীয়া (Assamese)'], 0); } });
    rules(s, ['Single-choice dialog from the toolbar Language icon. Non-Mitanin flavours: English, Hindi, Bangla, Assamese (SupervisorActivity.kt:92-123)', 'Mitanin: English + Hindi only (isMitanin skips Bangla / Assamese, :98)', 'Picking a language applies it and restarts the app']);
  }
  // 11 Exit application
  {
    const s = F('M2.12-11 · Exit application', 4, 1, true);
    supTop(s, 'Rajnagar', 'Supervisor · 4 sub-centres · 32 ASHAs');
    const b = body(s); await stat(b, [['32', 'Total ASHAs', 'people'], ['9', 'Pending', 'child']]);
    await dialog(s, { ic: 'warning', title: 'Exit', body: 'Do you want to exit application?', cancel: 'No', ok: 'Yes' });
    rules(s, ['Back on supervisor home (SupervisorHomeFragment.kt:41-65)']);
  }
  // 12 Unsupported device
  {
    const s = F('M2.12-12 · Unsupported device (blocking)', 5, 1, true);
    supTop(s, 'Rajnagar', 'Supervisor');
    await dialog(s, { ic: 'shield', icColor: 'color/error', title: 'Unsupported device', body: 'This app cannot run on a rooted device or emulator for data security.', cancel: null, ok: 'Close app' });
    rules(s, ['Release builds only, rooted device / emulator (SupervisorActivity.kt:217-224, 353-360); same dialog as the ASHA app']);
  }
  // ===== MITANIN VARIANT (flavour mitanin / mitaninStag / mitaninUat) =====
  // 13 Incentive dashboard · Mitanin (Trainer and ANM see the same tiles)
  {
    const s = F('M2.12-13 · Incentive dashboard · Mitanin', 0, 2);
    mitTop(s, 'Kurud', 'Mitanin Trainer · 3 sub-centres · 24 Mitanins');
    await tabs2(s, ['Incentive Dashboard', 'KPI Dashboard'], 0);
    const b = body(s); b.itemSpacing = 12;
    const fr = AL('HORIZONTAL'); gap(fr, 'spacing/sm'); add(b, fr);
    const m = S.select(fr, 'Month', 'October 2026'); m.layoutSizingHorizontal = 'FILL'; const sc = S.select(fr, 'Sub-centre', 'All sub-centres'); sc.layoutSizingHorizontal = 'FILL';
    const r0 = AL('HORIZONTAL', 'Row 0'); gap(r0, 'spacing/xs'); add(b, r0);
    await statusTile(r0, '24', 'Total Mitanins', 'Tap to view Mitanin grouped.', 'people');
    const r1 = AL('HORIZONTAL', 'Row 1'); gap(r1, 'spacing/xs'); add(b, r1);
    await statusTile(r1, '7', 'Pending', null, 'child', false); await statusTile(r1, '3', 'Overdue', 'Past 5th (due date)', 'disease');
    const r2 = AL('HORIZONTAL', 'Row 2'); gap(r2, 'spacing/xs'); add(b, r2);
    await statusTile(r2, '12', 'Verified', null, 'immunization', false); await statusTile(r2, '1', 'Rejected', null, 'records', false);
    const r3 = AL('HORIZONTAL', 'Row 3'); gap(r3, 'spacing/xs'); add(b, r3);
    await statusTile(r3, '1', 'Mitanins unclaimed', null, 'village', false);
    add(b, inst('sectionHeader', null, { Title: 'Sub-centres under you (3)', 'Show action': false }));
    for (const [n, c] of [['SC Kurud', 9], ['SC Bhakhara', 8], ['SC Charmudiya', 7]]) {
      const r = AL('HORIZONTAL', n); r.counterAxisAlignItems = 'CENTER'; pad(r, 'spacing/md', 'spacing/sm'); gap(r, 'spacing/sm'); rad(r, 'radius/md'); fill(r, 'color/surface-container'); stroke(r, 'color/outline-variant'); add(b, r);
      const t = await txt(n, 'Title/Small', 'color/on-surface'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL';
      r.appendChild(await txt(`Mitanin: ${c}`, 'Body/Small', 'color/on-surface-variant')); r.appendChild(icon('chevron_right', 'color/on-surface-variant', 20));
    }
    rules(s, [
      'Mitanin: labels "Total Mitanins", "Mitanins unclaimed", "Tap to view Mitanin grouped."; green logo (IncentiveDashboardFragment.kt:76-108)',
      'Mitanin: Pending / Verified / Rejected / Unclaimed subtitles hidden; Overdue keeps subtitle "Past 5th (due date)" (other flavours: past 12th) (:78-86)',
      'Mitanin: Overdue tile shown for both Trainer and ANM (FLW-1169) (:90-101)',
      'Mitanin role gate: Trainer (role "ASHA Supervisor") → Verified tile opens the "approved" list (isApproved); ANM / other role → "verified" list (:87-99, 333-338)',
      'Mitanin: sub-centre rows read "Mitanin: n" (SubCenterAdapter.kt:28-29)',
      'Mitanin: toolbar colour supervisor_toolbar #037E62 (SupervisorActivity.kt:188-191); drawer menu item "Mitanin Trainer" for role "ASHA Supervisor", else the role name e.g. ANM (:494-506)',
      'Mitanin: language list English + Hindi only (M2.12-10)',
    ]);
  }
  // 14 ASHA monthly detail · Mitanin (Trainer) · per-activity ticks
  {
    const s = F('M2.12-14 · ASHA monthly detail · Mitanin', 1, 2);
    mitTop(s, 'Kamla Sahu', 'October 2026 · SC Kurud');
    const b = body(s); b.itemSpacing = 10;
    b.appendChild(await txt('Kamla Sahu', 'Title/Medium', 'color/on-surface'));
    await caption(b, 'EmployeeID: 61203, October 2026');
    await stat(b, [['4', 'Claims', 'care'], ['₹2,900', 'Total claimed', 'couple']]);
    await groupHead(b, 'Monthly Honorarium', 1, '2,000');
    add(b, kinst('Checkbox option', 'Checked=true', { Label: 'Monthly Honorarium · ₹2,000' }));
    const ht = row(b, 'Honorarium tag', 6); tag(ht, 'Success', 'Approved'); await caption(ht, 'Default activity · cannot be opened');
    await groupHead(b, 'Maternal health', 3, '900');
    add(b, kinst('Checkbox option', 'Checked=true', { Label: 'ANC registration in 1st trimester · 2 claims · ₹600' }));
    add(b, kinst('Checkbox option', 'Checked=false', { Label: 'Institutional delivery (JSY) · 1 claim · ₹300' }));
    await caption(b, 'Verify will commit every activity you have ticked.');
    bottomBar(s, 'Reject', 'Verify');
    rules(s, [
      'Mitanin + month actionable (any role): Verify / Reject apply only to ticked activities (FLW-1171) (WorkerDetailFragment.kt:84-87, 166-196)',
      'Mitanin: "Monthly Honorarium" rows pre-ticked and listed first; default activities cannot be opened, show "approved" badge when approved (ActivityAdapter.kt:88-111)',
      'Mitanin: Verify / Reject disabled until ≥ 1 row ticked (:136-138)',
      'Mitanin: documents checkbox hidden and not required for Verify (:123, 318-320); worker name + summary card shown (:121-122)',
      'Mitanin: reviewer-ID line ("Mitanin Trainer ID: …") is set but hidden (tvSupervisorInfo GONE, :122, 133-134) → not drawn',
      'Mitanin: toolbar title = Mitanin name with green logo (:410-416); Verify button always visible (FLW-1145, :244-251)',
      'ANM view of the same screen → M2.12-19',
    ]);
  }
  // 15 Monthly detail · read-only after decision
  {
    const s = F('M2.12-15 · Monthly detail · already decided', 2, 2);
    supTop(s, 'Kusum Devi', 'October 2026 · SC Rajnagar');
    const b = body(s); b.itemSpacing = 12;
    await headerIds(b, 'EmployeeID: 40522, October 2026', 'Supervisor ID: 55021');
    banner(b, 'Success', 'Verified by Supervisor', 'Verified on 06 Nov 2026 by Anju Sharma.', null);
    await groupHead(b, 'Maternal health', 4, '2,400');
    await table(b, null, ['S.No', 'Activity', 'No. of claims', 'Amount'], [['1', 'ANC registration in 1st trimester', '4', '₹1,200'], ['2', 'Institutional delivery (JSY)', '2', '₹1,200']]);
    await groupHead(b, 'Child health', 3, '1,650', false);
    rules(s, ['Verify / Reject card hidden when the month is Verified / Approved / Rejected; "Verified by <role>" banner for verified months (WorkerDetailFragment.kt:80-82, 111-117, 271-272; AshaWorker.kt:74-94)', 'Records still open for viewing', 'Mitanin: ticks column also hidden once decided (showActivityCheckboxes needs actionable)']);
  }
  // 16 Monthly detail · no activities
  {
    const s = F('M2.12-16 · Monthly detail · no activities', 3, 2);
    supTop(s, 'Rani Yadav', 'October 2026 · SC Sirsi');
    const b = body(s); b.itemSpacing = 12; b.counterAxisAlignItems = 'CENTER';
    await headerIds(b, 'EmployeeID: 40531, October 2026', 'Supervisor ID: 55021');
    const e = kinst('Empty state', null, { Title: 'No activities found', Body: 'This ASHA has no incentive activities for October 2026.', Icon: 'form' }); add(b, e, false);
    const eb = e.findOne((n) => n.type === 'INSTANCE' && n.name === 'Button'); if (eb) eb.visible = false;
    rules(s, ['Records empty → "No activities found"; table and Verify / Reject hidden (WorkerDetailFragment.kt:258-262)', 'Claim records screen has the same kind of empty state (BeneficiaryDetailFragment.kt:218-256)']);
  }
  // 17 Claim records · Mitanin · per-row approve / reject
  {
    const s = F('M2.12-17 · Claim records · Mitanin', 4, 2);
    mitTop(s, 'ANC registration in 1st trimester', 'Kamla Sahu · 3 claims');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'Approve or reject directly', 'Approve or reject this activity directly using ✓ / ✕.', null);
    await strip(b, '3 · ₹900');
    await actionCard(b, { name: 'Geeta Netam', initials: '1', meta: 'Ben ID: 8120 3345 0012', rows: [['RCH ID', '2210 0981 3345'], ['ABHA Number', 'N/A'], ['Amount', '₹300']], icons: [['check', 'Approve', 'color/success'], ['close', 'Reject', 'color/error']] });
    await actionCard(b, { name: 'Savitri Dhruw', initials: '2', meta: 'Ben ID: 8120 3345 0040', rows: [['RCH ID', '2210 0981 3352'], ['ABHA Number', '91-6610-2231-0045'], ['Amount', '₹300']], icons: [['check', 'Approve', 'color/success'], ['close', 'Reject', 'color/error']] });
    await actionCard(b, { name: 'Mitanin meeting', initials: '3', meta: 'Claim date: 14 Oct 2026', rows: [['Amount', '₹300']], icons: [['check', 'Approve', 'color/success'], ['close', 'Reject', 'color/error']] });
    rules(s, [
      'Mitanin row layout (layout_mt_inner): S.No, Ben ID, name, RCH ID, ABHA Number, amount, ✓ / ✕ (BeneficiaryAdapter.kt:46-85)',
      'Mitanin: meeting / register claims with no beneficiary hide Ben ID / name / RCH / ABHA and show "Claim date" (:52-69)',
      'Mitanin + month actionable (Trainer and ANM): ✓ approves that one record at once, no confirm; ✕ opens the reason sheet (M2.12-04) for that one record (BeneficiaryDetailFragment.kt:75-77, 146-160)',
      '✓ / ✕ hidden and inert once the month is decided; double taps ignored while a request runs; after a decision → back to the activity list',
      'Other flavours: no per-row actions (decide the whole month on M2.12-03)',
    ]);
  }
  // 18 ASHA list · Mitanin
  {
    const s = F('M2.12-18 · ASHAs to verify · Mitanin', 5, 2);
    mitTop(s, 'Incentive verification', 'October 2026');
    const b = body(s); b.itemSpacing = 10;
    await monthCard(b, 'Mitanin monthly detail', 'Month: October, 2026');
    await strip(b, 'Verified: 12 · Pending: 7 · Rejected: 1');
    add(b, inst('searchBar', null, { Placeholder: 'Search Mitanin' }));
    await ashaRow(b, 'Kamla Sahu', 'SC Kurud · 4 activities', '2,900', 'Pending', 'Child');
    await decidedRow(b, 'Sukhiya Markam', 'SC Kurud · 5 activities', '3,200', 'Verified', 'Success', ['Verified on 04 Nov 2026', 'By: Rajesh Verma', '(Mitanin Trainer)']);
    await decidedRow(b, 'Hemlata Sahu', 'SC Bhakhara · 2 activities', '2,300', 'Rejected', 'Alert', ['Rejected on 03 Nov 2026', 'By: Rajesh Verma', '(Mitanin Trainer)', 'Reason: Duplicate claim']);
    rules(s, [
      'Mitanin: month card title "Mitanin monthly detail" (plain card), search hint "Search Mitanin" (IncentiveVerificationFragment.kt:67-69)',
      'Mitanin: decided by a Trainer (role "ASHA Supervisor") → "By: <name>" + "(Mitanin Trainer)" on verified / rejected rows (AshaWorkerAdapter.kt:68-69, 95-96, 133-134)',
    ]);
  }
  // 19 ANM monthly detail · Mitanin
  {
    const s = F('M2.12-19 · ANM monthly detail · Mitanin', 0, 3);
    mitTop(s, 'Kamla Sahu', 'October 2026 · SC Kurud');
    const b = body(s); b.itemSpacing = 10;
    b.appendChild(await txt('Kamla Sahu', 'Title/Medium', 'color/on-surface'));
    await caption(b, 'EmployeeID: 61203, October 2026');
    await stat(b, [['4', 'Claims', 'care'], ['₹2,900', 'Total claimed', 'couple']]);
    const vb = await txt('Verified by: ANM', 'Label/Large', 'color/primary'); add(b, vb);
    await groupHead(b, 'Monthly Honorarium', 1, '2,000');
    add(b, kinst('Checkbox option', 'Checked=true', { Label: 'Monthly Honorarium · ₹2,000' }));
    await groupHead(b, 'Maternal health', 3, '900');
    add(b, kinst('Checkbox option', 'Checked=true', { Label: 'ANC registration in 1st trimester · 2 claims · ₹600' }));
    add(b, kinst('Checkbox option', 'Checked=true', { Label: 'Institutional delivery (JSY) · 1 claim · ₹300' }));
    bottomBar(s, 'Reject', 'Verify');
    rules(s, [
      'Mitanin, logged-in role ≠ "ASHA Supervisor" (ANM): role line "Verified by: <role>" (WorkerDetailFragment.kt:126-128)',
      'ANM reaches months from the "verified" list (Trainer-approved months) — month-level step of the Trainer → ANM chain',
      'Same per-activity ticks, disabled-until-ticked buttons and ✓ / ✕ on records as the Trainer (M2.12-14, M2.12-17)',
      'Drawer menu item reads the role name (e.g. "ANM") (SupervisorActivity.kt:499-501)',
    ]);
  }
  // 20 Supervisor profile · Mitanin
  {
    const s = F('M2.12-20 · Supervisor profile · Mitanin', 1, 3);
    mitTop(s, 'Profile', 'Mitanin Trainer');
    const b = body(s); b.itemSpacing = 12;
    await person(b, 'RV', 'Rajesh Verma', 'Mitanin Trainer · EMP. ID 70114', [['Neutral', '3 sub-centres'], ['Neutral', '24 Mitanins']]);
    await infoCard(b, 'Contact info', [['Mobile', '94252 11876'], ['Email', 'rajesh.verma@cg.gov.in']]);
    await infoCard(b, 'Posting', [['District', 'Dhamtari'], ['Block', 'Kurud'], ['Sub-centre', 'SC Kurud']]);
    const g = AL('VERTICAL', 'Items'); pad(g, 'spacing/xs', 'spacing/xs'); rad(g, 'radius/lg'); fill(g, 'color/surface-container'); stroke(g, 'color/outline-variant'); add(b, g);
    for (const [l, ic, d] of [['Mitanin Trainer', 'check'], ['Help & support', 'help'], ['Language · English', 'translate'], ['Delete my account', 'close', true], ['Log out', 'logout', true]]) {
      const it = inst('listItem', 'Active=false', { Label: l, Icon: ic, 'Show chevron': !d }); add(g, it);
      if (d) { for (const x of it.findAll((n) => n.name === 'glyph')) x.fills = [paint('color/error')]; const t = it.findOne((n) => n.type === 'TEXT'); if (t) t.fills = [paint('color/error')]; }
    }
    rules(s, [
      'Mitanin: subtitle "Mitanin Trainer" for role "ASHA Supervisor", otherwise the raw role (e.g. ANM) (SupervisorProfileFragment.kt:71-86)',
      'Mitanin: toolbar shows green logo + title "Profile" (:114-120)',
      'Mitanin: drawer item "Mitanin Trainer" (ANM: role name) opens the supervisor home (SupervisorActivity.kt:494-506)',
    ]);
  }
  const maxB = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, maxB, 900,
    'SUPERVISOR NOTES\n' +
    '• Supervisor screens use a teal top bar (module/care) so supervisors always know they are in supervisor mode.\n' +
    '• Home = two tabs: Incentive Dashboard (01) and KPI Dashboard (07). Incentive data is online-only (08).\n' +
    '• Dashboard tiles = fragment_incentive_dashboard: Total, Pending, Overdue (past 12th; Mitanin past 5th), Verified, Rejected, Not claimed — each opens the filtered ASHA list.\n' +
    '• Monthly detail (03) = fragment_worker_detail: grouped by programme; verify commits all activities; reject = multi-select reasons (04).\n' +
    '• Mitanin variant: dashboard (13), monthly detail with ticks (14), per-record ✓/✕ (17), list (18), ANM detail (19), profile (20). Trainer → approved list, ANM → verified list.');
  fitSection(sec);
  return ['2.12 screens: ' + frames.length];
}
