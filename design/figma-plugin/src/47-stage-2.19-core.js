// ---------------------------------------------------------------------------
// Stage 2.19 · Core screens v2 (login, home, household, beneficiary)
// Redraws frames 01–06 (drawn outside the plugin, not editable) with every rule the code has,
// plus the app-wide dialogs of LoginActivity / HomeActivity and the missing household / beneficiary flows.
// Gap source: scratchpad gap_A1_household_ben_home.md (code is the source of truth).
// ---------------------------------------------------------------------------
async function buildStage219(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureSearchMic(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, sheet, scrim, pinBottom, note,
    stageSection, fitSection, fixW, progress, text, select, date, auto, tf, seg, radios, steppers, section, reveal, banner, infoCard, bottomBar,
    screenTop, chips, tileGrid, person } = S;
  const { row, caption, tag, btn, iconBtn, actionCard, dialog, visitSheet, optionSheet, searchRow, viewMode, blockedAdd, uploads, table, rules, finish } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.19 · Core screens v2 (login, home, household, beneficiary)');
  const frames = []; const rowMap = new Map();
  const F = (name, col, r, fixed) => { const s = phone(sec, name, col, r, fixed); frames.push(s); rowMap.set(s, r); return s; };

  // ---- local helpers -------------------------------------------------------
  const toast = async (s, msg, fromBottom = 120) => {
    const t = AL('HORIZONTAL', 'Toast'); t.counterAxisAlignItems = 'CENTER'; pad(t, 'spacing/md', 'spacing/sm'); rad(t, 'radius/full'); fill(t, 'color/on-surface');
    const x = await txt(msg, 'Body/Medium', 'color/on-primary'); t.appendChild(x);
    s.appendChild(t); t.layoutPositioning = 'ABSOLUTE';
    if (t.width > 328) { fixW(t, 328); wrapText(x); }
    t.x = Math.round((360 - t.width) / 2); t.y = Math.max(0, s.height - fromBottom);
    return t;
  };
  const fab = (s, label, ic, disabled = false, fromBottom = 24) => {
    const f = inst('fab', null, { Label: label, Icon: ic }); s.appendChild(f); f.layoutPositioning = 'ABSOLUTE';
    f.x = 360 - 16 - f.width; f.y = s.height - fromBottom - f.height; if (disabled) f.opacity = 0.4; return f;
  };
  const homeTop = (s, title, subtitle) => { add(s, inst('statusBar')); return add(s, inst('topAppBar', 'Type=Home', { Title: title, Subtitle: subtitle })); };
  const due = (p, tone, t, sub, c, ic) => add(p, inst('dueCard', `Tone=${tone}`, { Title: t, Subtitle: sub, Count: c, Icon: ic }));
  const hdr = (p, t, action) => add(p, inst('sectionHeader', null, action ? { Title: t, Action: action } : { Title: t, 'Show action': false }));
  // big tappable count card (Missed period / Non-follow-up / Household hub)
  const countTile = async (p, ic, tone, title, sub, count) => {
    const c = AL('HORIZONTAL', 'Count card · ' + title); c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/md', 'spacing/md'); gap(c, 'spacing/md'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant'); add(p, c);
    const ib = box('Icon', 52, 52); rad(ib, 'radius/lg'); fill(ib, `module/${tone}/bg`); ib.appendChild(icon(ic, `module/${tone}/fg`, 28)); c.appendChild(ib);
    const col = AL('VERTICAL', 'Text'); col.itemSpacing = 2; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    const a = await txt(title, 'Title/Medium', 'color/on-surface'); col.appendChild(a); wrapText(a);
    if (sub) await caption(col, sub);
    c.appendChild(await txt(count, 'Number/Medium', `module/${tone}/fg`));
    c.appendChild(icon('chevron_right', 'color/on-surface-variant', 20));
    return c;
  };
  const listGroup = async (p, title, items) => {
    const g = AL('VERTICAL', title); gap(g, 'spacing/2xs'); add(p, g);
    g.appendChild(await txt(title, 'Label/Large', 'color/on-surface-variant'));
    const cc = AL('VERTICAL', 'Items'); pad(cc, 'spacing/xs', 'spacing/xs'); rad(cc, 'radius/lg'); fill(cc, 'color/surface-container'); stroke(cc, 'color/outline-variant'); add(g, cc);
    for (const [l, ic, danger] of items) {
      const it = inst('listItem', 'Active=false', { Label: l, Icon: ic, 'Show chevron': !danger }); add(cc, it);
      if (danger) { for (const x of it.findAll((n) => n.name === 'glyph')) x.fills = [S.paint('color/error')]; const t = it.findOne((n) => n.type === 'TEXT'); if (t) t.fills = [S.paint('color/error')]; }
    }
    return g;
  };
  const loginBehind = async (s) => {
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 40; b.itemSpacing = 16;
    b.appendChild(await txt('Sakhi', 'Headline/Large', 'color/primary'));
    text(b, 'Username', 'asha_sunita'); tf(b, 'Filled', { Label: 'Password', Value: '••••••••' }, 'visibility');
    btn(b, 'Filled', 'Log in', false, true);
    return b;
  };
  const membersBehind = async (s) => {
    screenTop(s, 'Household members', 'HH 7012 3345 0012 · Ramesh Kumar');
    const b = body(s); b.itemSpacing = 12;
    await actionCard(b, { name: 'Ramesh Kumar', meta: 'Head of family · M · 46 yrs', tags: [['Neutral', 'HOF']], icons: [['call', 'Call'], ['delete', 'Delete', 'color/error']] });
    await actionCard(b, { name: 'Sunita Devi', meta: 'Wife · F · 34 yrs', icons: [['call', 'Call'], ['delete', 'Delete', 'color/error']] });
    return b;
  };
  const allBenBehind = async (s, title = 'All beneficiaries', sub = '1,104 beneficiaries') => {
    screenTop(s, title, sub);
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID', { filter: true });
    await actionCard(b, { name: 'Sunita Devi', meta: 'F · 34 yrs · Ben ID 1203 4456', rows: [['Mobile', '98765 43210'], ['Husband', 'Ramesh Kumar']], icons: [['call', 'Call'], ['id_card', 'ABHA']] });
    return b;
  };

  // =========================================================================
  // ROW 0 · Login (01 v2) — SignInFragment / LoginActivity
  // =========================================================================
  {
    const s = F('M2.19-01 · Login v2 (remember me, errors, language)', 0, 0);
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 24; b.itemSpacing = 14;
    const top = row(b, 'Brand row', 8);
    const brand = await txt('Sakhi', 'Headline/Large', 'color/primary'); top.appendChild(brand);
    tag(top, 'Alert', 'DEMO');
    S.spacer(top);
    const lang = row(top, 'Language', 4); lang.appendChild(icon('translate', 'color/primary', 22)); lang.appendChild(await txt('English', 'Label/Large', 'color/primary'));
    b.appendChild(await txt('Log in to continue', 'Title/Medium', 'color/on-surface'));
    text(b, 'Username', 'asha_sunita');
    tf(b, 'Error', { Label: 'Password', Value: '••••••••', Helper: 'Invalid username or password', 'Show helper': true }, 'visibility');
    add(b, kinst('Checkbox option', 'Checked=true', { Label: 'Remember me' }));
    btn(b, 'Filled', 'Log in', false, true);
    const links = row(b, 'Links', 8); links.primaryAxisAlignItems = 'SPACE_BETWEEN';
    btn(links, 'Text', 'Forgot password?'); btn(links, 'Text', 'Delete my account');
    await caption(b, 'Powered by Piramal Swasthya · Version 2.14.0 (214)');
    rules(s, [
      'Remember me ticked → username + password saved; next app open logs in automatically (SignInFragment.kt:213-248)',
      'Remembered username / password are prefilled (:219-227)',
      'Login error text shows under the fields (:232-238)',
      'Language (top right) opens the language sheet, app restarts (:176-188) → M2.19-04',
      '"Delete my account" opens an external MS Forms page; URL differs: saksham/niramay/xushrukha vs others; no browser → toast (:190-209)',
      '"DEMO" tag only on non-production Mitanin builds; footer = "Powered by … version" (:125, :288-291)',
      'Forgot password: button is gone in code (fragment_sign_in.xml:246-254) – Not in app today – needs dev',
      'After login: Supervisor / ANM / CHO → Supervisor app, first village auto-selected; ASHA → work location (M2.13-04) only if none saved, else Home (:253-280)',
    ]);
  }
  {
    const s = F('M2.19-02 · Login · different user on this phone', 1, 0, true);
    await loginBehind(s);
    await dialog(s, { ic: 'warning', title: 'Another user is logged in', body: 'asha_rekha is logged in on this phone. 14 records are not synced yet. Continue as asha_sunita?', cancel: 'No', ok: 'Yes',
      extra: async (c) => { await caption(c, 'If all records are synced the text reads: "All records synced". Yes then logs out asha_rekha and clears her data.'); } });
    rules(s, [
      'Shown when the username differs from the user saved on the phone (SignInFragment.kt:67-104, :336-339)',
      'Yes + unsynced records → only pushes the old user\'s records (no logout yet) – data-loss guard',
      'Yes + nothing unsynced → logs out old user, clears local data, logs in new user',
    ]);
  }
  {
    const s = F('M2.19-03 · Login · offline (same user)', 2, 0);
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 24; b.itemSpacing = 14;
    b.appendChild(await txt('Sakhi', 'Headline/Large', 'color/primary'));
    banner(b, 'Offline', 'No internet', 'You can still log in with the password you used last time on this phone.', null);
    text(b, 'Username', 'asha_sunita');
    tf(b, 'Error', { Label: 'Password', Value: '••••••', Helper: 'Invalid Password', 'Show helper': true }, 'visibility');
    add(b, kinst('Checkbox option', 'Checked=false', { Label: 'Remember me' }));
    btn(b, 'Filled', 'Log in', false, true);
    rules(s, [
      'Offline + same user as last login → checks the cached password on the phone (SignInFragment.kt:311-334)',
      'Wrong cached password → "Invalid Password" under the field',
      'Offline + different user → cannot log in (server needed)',
    ]);
  }
  {
    const s = F('M2.19-04 · Language sheet (login + Profile)', 3, 0, true);
    await loginBehind(s);
    await optionSheet(s, 'Choose language', ['English', 'Hindi (हिन्दी)', 'Assamese (অসমীয়া)', 'Bangla (বাংলা)'], 0, { subtitle: 'The app restarts in the language you tap.' });
    rules(s, [
      'Same sheet on Login and in Profile (LanguageBottomSheet.kt)',
      '4 options incl. Bangla on non-Mitanin builds (:89-100)',
      'Mitanin builds: English + Hindi only (:55, :78)',
      'Tapping a language applies at once – no Apply button (:36-50); M2.11-08 "Apply" is a design change',
    ]);
  }
  {
    const s = F('M2.19-05 · Incomplete install', 4, 0, true);
    await loginBehind(s);
    await dialog(s, { ic: 'warning', title: 'App not installed completely', body: 'Some parts of Sakhi are missing on this phone. Please install it again from the Play Store.', cancel: 'Exit', ok: 'Open Play Store' });
    rules(s, ['Shown when the native library (SQLCipher) is missing – sideloaded APK without its ABI split (LoginActivity.kt:171-195)', 'Cannot be dismissed; Exit closes the app']);
  }
  {
    const s = F('M2.19-06 · Unsupported device (rooted / emulator)', 5, 0, true);
    await loginBehind(s);
    await dialog(s, { ic: 'lock', title: 'Unsupported device', body: 'Sakhi cannot run on a rooted phone or an emulator, to keep health data safe.', cancel: null, ok: 'Exit', danger: true });
    rules(s, ['Release builds only (LoginActivity.kt:95-104, HomeActivity.kt:391-398, :665-672)', 'Only button is Exit; cannot be dismissed']);
  }

  // =========================================================================
  // ROW 1 · App-wide dialogs (HomeActivity / LoginActivity)
  // =========================================================================
  const todayBehind = async (s) => {
    homeTop(s, 'Namaste, Sunita', 'Sirsi · Ward 4');
    const b = body(s); b.itemSpacing = 12;
    hdr(b, 'To-do this week');
    due(b, 'Maternal', 'Women due for ANC', '2nd & 3rd trimester visits', '12', 'pregnant');
    due(b, 'Immunization', 'Children due for vaccination', 'Includes 3 overdue', '8', 'vaccine');
    add(s, inst('bottomNav', 'Active=Today'));
    return b;
  };
  {
    const s = F('M2.19-07 · Account deactivated', 0, 1, true);
    await todayBehind(s);
    await dialog(s, { ic: 'lock', title: 'Account deactivated', body: 'Your account has been deactivated. Please contact your supervisor.', cancel: null, ok: 'OK', danger: true });
    rules(s, ['Raised by the server on any call (HomeActivity.kt:486-503) and on login (LoginActivity.kt:109-126)', 'OK logs out and returns to Login']);
  }
  {
    const s = F('M2.19-08 · Session expired', 1, 1, true);
    await todayBehind(s);
    await dialog(s, { ic: 'clock', title: 'Session expired', body: 'Please log in again to continue. Your saved records stay on this phone.', cancel: null, ok: 'OK' });
    rules(s, ['Cannot be dismissed (HomeActivity.kt:507-530)', 'OK logs the user out and opens Login']);
  }
  {
    const s = F('M2.19-09 · Permissions on first open', 2, 1, true);
    await todayBehind(s);
    await dialog(s, { ic: 'info', icColor: 'color/primary', title: 'Allow access', body: 'Sakhi needs these to work in the field:', cancel: 'Not now', ok: 'Allow',
      extra: async (c) => {
        for (const [ic, l] of [['call', 'Phone – call beneficiaries'], ['form', 'Storage – save files and CSV'], ['mic', 'Microphone – voice search'], ['camera', 'Camera – photos and documents'], ['notifications', 'Notifications (Android 13+)']]) {
          const r = row(c, 'Permission', 10); r.appendChild(icon(ic, 'color/primary', 20)); const t = await txt(l, 'Body/Medium', 'color/on-surface'); r.appendChild(t); wrapText(t);
        }
      } });
    rules(s, ['System prompts in code; this frame shows the set asked for', 'Home start-up: call, storage, mic, camera (HomeActivity.kt:532-551)', 'Notifications: asked on first Login open, Android 13+ (LoginActivity.kt:160-169)']);
  }
  {
    const s = F('M2.19-10 · In-app update', 3, 1, true);
    await todayBehind(s);
    scrim(s);
    const sh = sheet(s, 'Sheet · Update');
    sh.appendChild(icon('refresh', 'color/primary', 32));
    const t = await txt('Update available', 'Title/Large', 'color/on-surface'); sh.appendChild(t);
    const d = await txt('A new version of Sakhi is ready on the Play Store. Update now to get the latest forms.', 'Body/Medium', 'color/on-surface-variant'); add(sh, d); d.textAutoResize = 'HEIGHT'; d.textAlignHorizontal = 'CENTER';
    const ar = row(sh, 'Actions', 8); ar.primaryAxisAlignItems = 'MAX'; add(sh, ar); btn(ar, 'Text', 'Later'); btn(ar, 'Filled', 'Update');
    pinBottom(s, sh);
    rules(s, ['Google Play in-app update flow, checked on Home open (HomeActivity.kt:423-424)', 'The look is Play\'s own UI; frame shows intent only']);
  }
  {
    const s = F('M2.19-11 · Log out (unsynced warning)', 4, 1, true);
    homeTop(s, 'Profile', 'Sunita Kumari');
    const b = body(s); await listGroup(b, 'App', [['Language · English', 'translate'], ['Log out', 'logout', true]]);
    await dialog(s, { ic: 'logout', title: 'Log out?', body: '12 records are not processed yet. If you log out now they may be lost. Log out anyway?', cancel: 'No', ok: 'Log out', danger: true,
      extra: async (c) => { await caption(c, 'When everything is synced the text is: "All records synced. Are you sure you want to log out?"'); } });
    rules(s, ['Count of unprocessed records shown (HomeActivity.kt:203-227)', 'Yes: clears FCM token, beneficiary images and all background work, then Login', 'Replaces 09 "Log out" → 01 direct link']);
  }
  {
    const s = F('M2.19-12 · Exit app (back on Home)', 5, 1, true);
    await todayBehind(s);
    await dialog(s, { ic: 'logout', title: 'Exit application?', body: null, cancel: 'No', ok: 'Yes' });
    rules(s, ['System back on the Home screen (HomeFragment.kt:46-69)', 'Yes closes the app (user stays logged in)']);
  }

  // =========================================================================
  // ROW 2 · Today (02 v2), Missed period, Non-follow-up, Modules (03 v2), dev mode
  // =========================================================================
  {
    const s = F('M2.19-13 · Today v2 (all cards)', 0, 2);
    homeTop(s, 'Sirsi · Ward 4', 'Tap to change work location');
    const b = body(s); b.itemSpacing = 12;
    // first sync strip
    const dl = S.card(b, 'First download strip'); dl.appendChild(await txt('Downloading… 62%', 'Title/Small', 'color/on-surface'));
    const pr = progress(296, 0.62, 'color/primary', 8); dl.appendChild(pr); pr.layoutSizingHorizontal = 'FILL';
    await caption(dl, 'First sync: beneficiaries are being downloaded.');
    banner(b, 'Warning', '12 records not synced', 'Syncing automatically in the background…', 'Sync now');
    hdr(b, 'To-do');
    due(b, 'Maternal', 'Women due for ANC', 'ANC list', '12', 'pregnant');
    due(b, 'Immunization', 'Children due for vaccination', 'Includes 3 overdue', '8', 'vaccine');
    due(b, 'Alert', 'Suspected high-risk pregnancies', 'Refer to PHC', '3', 'warning');
    due(b, 'Alert', 'Confirmed high-risk pregnant women', 'Opens ANC list (confirmed HRP)', '5', 'pregnant');
    due(b, 'Alert', 'Suspected high-risk non-pregnant women', 'Opens HRP non-pregnant list', '4', 'warning');
    due(b, 'Child', 'Low birth weight babies', 'Newborns under 2.5 kg', '2', 'child');
    due(b, 'Maternal', 'Missed period', 'Registration and tracking', '6', 'calendar');
    due(b, 'Neutral', 'No follow-up in 30 days', 'ANC, PNC and EC', '9', 'clock');
    // ABHA card with two counts
    const ab = S.card(b, 'ABHA card'); const ar = row(ab, 'Head', 10); ar.appendChild(icon('id_card', 'color/primary', 24)); const at = await txt('ABHA created', 'Title/Medium', 'color/on-surface'); ar.appendChild(at); at.layoutSizingHorizontal = 'FILL'; ar.appendChild(icon('chevron_right', 'color/on-surface-variant', 20));
    await S.stat(ab, [['214', 'New ABHA', 'immunization'], ['388', 'Old ABHA', 'people']]);
    due(b, 'Neutral', 'RCH ID available', 'Beneficiaries with an RCH ID', '642', 'id_card');
    // impact carousel
    const im = S.card(b, 'Impact carousel'); const ir = row(im, 'Head', 8);
    const it = await txt('My impact', 'Title/Medium', 'color/on-surface'); ir.appendChild(it); it.layoutSizingHorizontal = 'FILL';
    ir.appendChild(icon('chevron_left', 'color/on-surface-variant', 22)); ir.appendChild(await txt('3 / 8', 'Label/Large', 'color/on-surface-variant')); ir.appendChild(icon('chevron_right', 'color/primary', 22));
    im.appendChild(await txt('42', 'Number/Large', 'module/maternal/fg'));
    await caption(im, 'mothers got all 4 ANC visits this year');
    rules(s, [
      'Home opens on the Modules tab by default (HomeFragment.kt:196) – design lands on Today; confirm',
      'Toolbar title = village; tap → change work location (closes Home, opens ServiceLocationActivity) (HomeFragment.kt:186-198)',
      'Toolbar has Language and Sync-status icons on Home; other screens show a Home icon (HomeActivity.kt:683-760)',
      'Download strip only while first pull/push worker runs (HomeFragment.kt:117-150)',
      'Auto push whenever unsynced > 0 (debounced) (HomeActivity.kt:407-417)',
      'Missed period → M2.19-15; No follow-up → M2.19-16 (SchedulerFragment.kt:121-126)',
      'Confirmed HRP → ANC list source 3; non-pregnant HR → HRP non-pregnant list (:104-109)',
      'ABHA card → All-ben ABHA list (source 1); RCH card → RCH list (source 2) (:113-120)',
      'Impact carousel: arrows, swipe with peek, "3 / 8", haptic tick; hidden when badge list is empty (remote kill); tap → Badge shelf (:207-294)',
      'Debug only: long-press "My impact" stages demo badge progress (:262-273)',
      'Home also downloads all form schemas for the current language in the background (HomeFragment.kt:105-109)',
    ]);
  }
  {
    const s = F('M2.19-14 · Today · loading', 1, 2);
    homeTop(s, 'Sirsi · Ward 4', 'Loading…');
    const b = body(s); b.itemSpacing = 12;
    hdr(b, 'To-do');
    for (let i = 0; i < 4; i++) add(b, kinst('Skeleton card'));
    add(s, inst('bottomNav', 'Active=Today'));
    rules(s, ['Spinner/skeleton while counts load (SchedulerFragment.kt:62-73)']);
  }
  {
    const s = F('M2.19-15 · Missed period', 2, 2);
    screenTop(s, 'Missed period', 'Women whose period is late', 'none');
    const b = body(s); b.itemSpacing = 12;
    await countTile(b, 'favorite', 'couple', 'Eligible couple registration', 'Missed period · opens EC list (filtered)', '4');
    await countTile(b, 'favorite', 'couple', 'Eligible couple tracking', 'Missed period · opens EC tracking (filtered)', '2');
    await caption(b, 'Loading: counts show a spinner until ready.');
    rules(s, ['NEW screen (MissedPeriodFragment.kt:51-69)', 'Registration → EC list source 2; Tracking → EC tracking source 2', 'Loading state while counts load (:39-49)', 'Replaces Today → M2.2-02 link']);
  }
  {
    const s = F('M2.19-16 · No follow-up cases', 3, 2);
    screenTop(s, 'No follow-up in 30 days', 'Not visited for 30 days', 'none');
    const b = body(s); b.itemSpacing = 12;
    await countTile(b, 'pregnant', 'maternal', 'ANC non-follow-up', 'Opens ANC list (source 1)', '5');
    await countTile(b, 'child', 'child', 'PNC non-follow-up', 'Opens PNC mothers (source 1)', '3');
    await countTile(b, 'favorite', 'couple', 'Eligible couple non-follow-up', 'Opens EC tracking (source 1)', '1');
    rules(s, ['NEW screen (NonFollowUpFragment.kt:51-79)', 'Replaces Today → 06 All beneficiaries link']);
  }
  {
    const s = F('M2.19-17 · Modules v2 (counts + recap strip)', 4, 2);
    homeTop(s, 'Modules', 'Sirsi · Ward 4');
    const b = body(s); b.itemSpacing = 12;
    const rc = S.card(b, 'Monthly recap strip'); const rr = row(rc, 'Head', 8);
    const rt = await txt('October recap', 'Title/Medium', 'color/on-surface'); rr.appendChild(rt); rt.layoutSizingHorizontal = 'FILL'; tag(rr, 'Success', 'NEW');
    const rp = progress(296, 0.5, 'color/primary', 6); rc.appendChild(rp); rp.layoutSizingHorizontal = 'FILL';
    await caption(rc, 'Resume · 3 of 6 seen');
    const g = tileGrid(b, [['household', 'Household', '248', 'home'], ['people', 'All beneficiaries', '1,104', 'group'], ['couple', 'Eligible couples', '', 'favorite'], ['maternal', 'Maternal health', '', 'pregnant'],
      ['child', 'Child care', '', 'child'], ['immunization', 'Immunization', '96', 'vaccine'], ['disease', 'Disease control', '', 'hospital'], ['care', 'NCD referred list', '7', 'form'], ['records', 'Death reports', '', 'form'], ['village', 'Village meetings', '', 'group']]);
    for (const m of g.findAll((n) => n.type === 'INSTANCE' && n.name === 'Module tile')) { const tt = m.componentProperties; const ck = Object.keys(tt).find((k) => k.startsWith('Count')); if (ck && tt[ck].value === '') setP(m, { 'Show count': false }); }
    add(s, inst('bottomNav', 'Active=Modules'));
    rules(s, [
      'Live counts only on Household, All beneficiaries, NCD referred list, Immunization (IconDataset.kt:67-140)',
      '"NCD referred list" tile (IconDataset.kt:103-108) = HWC referrals list M2.8-14; label from code',
      'Household tile → Household hub M2.19-21 (not straight to the list)',
      'Recap strip lives on Modules (HomeIconsFragment.kt:73-210): resume progress bar, NEW / DONE badge; reduced motion = no animation',
      'Long-press the Modules tab → dev-mode PIN sheet (M2.19-18); long-press again disables dev mode (toast) (HomeFragment.kt:162-175)',
      'Debug only: forced recap preview state (HomeIconsFragment.kt:83-89)',
    ]);
  }
  {
    const s = F('M2.19-18 · Dev mode PIN', 5, 2, true);
    homeTop(s, 'Modules', 'Sirsi · Ward 4');
    add(s, inst('bottomNav', 'Active=Modules'));
    scrim(s);
    const sh = sheet(s, 'Sheet · Dev mode');
    sh.appendChild(await txt('Enable developer mode', 'Title/Large', 'color/on-surface'));
    tf(sh, 'Error', { Label: 'PIN', Value: '••••', Helper: 'Invalid PIN!', 'Show helper': true });
    btn(sh, 'Filled', 'Enable', false, true);
    pinBottom(s, sh);
    rules(s, ['Hidden support feature (EnableDevModeBottomSheetFragment.kt:34-43)', 'Correct PIN → toast "Dev Mode Enabled!"; wrong PIN → toast "Invalid PIN!"']);
  }

  // =========================================================================
  // ROW 3 · Profile v2, sync sheet, Household hub, Households (04 v2)
  // =========================================================================
  {
    const s = F('M2.19-19 · Profile & more v2 (role / flavour rules)', 0, 3);
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 8;
    const head = AL('VERTICAL', 'Profile header'); pad(head, 'spacing/lg', 'spacing/lg'); gap(head, 'spacing/sm'); rad(head, 'radius/xl'); fill(head, 'color/primary'); add(b, head);
    const hr = row(head, 'Row', 12);
    const av = inst('avatar', null, { Initials: 'SK' }); av.resize(64, 64); hr.appendChild(av);
    const hc = AL('VERTICAL'); hc.itemSpacing = 2; hr.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    hc.appendChild(await txt('Sunita Kumari', 'Title/Large', 'color/on-primary'));
    hc.appendChild(await txt('ASHA · Employee ID 102345', 'Body/Medium', 'color/primary-container'));
    hc.appendChild(await txt('Sub-centre Sirsi · Block Rajnagar', 'Body/Small', 'color/primary-container'));
    hr.appendChild(icon('camera', 'color/on-primary'));
    await listGroup(b, 'My work', [['My profile details', 'person'], ['Incentives', 'wallet'], ['Badges & achievements', 'star'], ['Dashboard', 'dashboard'], ['Supervisor', 'group']]);
    await listGroup(b, 'Tools', [['Sync records', 'sync'], ['Change work location', 'location'], ['Create ABHA number', 'id_card'], ['Video tutorials & LMS', 'visibility']]);
    await listGroup(b, 'App', [['Language · English', 'translate'], ['Support (feedback form)', 'help'], ['Delete my account', 'close', true], ['Log out', 'logout', true]]);
    await caption(b, 'Version 2.14.0 (214)');
    add(s, inst('bottomNav', 'Active=Profile'));
    rules(s, [
      'Supervisor / ANM / CHO login: "Home" hidden, "Supervisor" shown; ASHA: no Supervisor item (HomeActivity.kt:308-314)',
      'Mitanin: "Incentives" reads "Monthly claim summary" (:895-903)',
      'Sync records = manual push + pull; 2nd tap within 15 min → toast "Please wait Syncing in Progress" (:911-926)',
      'Change work location → ServiceLocationActivity (same as tapping the Home title) (:131-135, :741)',
      'Avatar camera → pick photo from gallery; Employee ID falls back to user ID when blank (:837-878)',
      'Support opens an external MS Forms feedback link (:978-991)',
      'Delete my account opens an external MS Forms page, URL per flavour; no browser → toast (:954-975) – not an in-app delete',
      'App version at the bottom (:418)',
      'Chat-support FAB + WebView sheet, "Help & support (chat)", "Report crash": OFF today (isChatSupportEnabled=false, :113, :372-389, :553-656)',
      'Get beneficiary data: drawer item commented out (activity_home_drawer.xml:81) – not reachable; see M2.19-53',
    ]);
  }
  {
    const s = F('M2.19-20 · Sync status sheet (toolbar)', 1, 3, true);
    await todayBehind(s);
    scrim(s);
    const sh = sheet(s, 'Sheet · Sync status');
    sh.appendChild(await txt('Sync status', 'Title/Large', 'color/on-surface'));
    await table(sh, null, ['Records', 'Synced', 'Waiting'], [['Beneficiaries', '1,100', '4'], ['Households', '248', '0'], ['ANC visits', '181', '5'], ['Immunization', '412', '3']]);
    btn(sh, 'Filled', 'Sync now', false, true);
    pinBottom(s, sh);
    await toast(s, 'Please wait Syncing in Progress', 60);
    rules(s, ['Toolbar sync icon opens this sheet; double-tap guarded (HomeActivity.kt:718-731)', 'Full screen 08 Sync stays for the bottom nav', 'Toast when a sync was started in the last 15 min']);
  }
  {
    const s = F('M2.19-21 · Household hub', 2, 3);
    screenTop(s, 'Household', 'Sirsi · Ward 4', 'none');
    const b = body(s); b.itemSpacing = 12;
    await countTile(b, 'home', 'household', 'Households', 'All registered households', '248');
    await countTile(b, 'person', 'people', 'Non-household', 'People with no household (homeless, institution)', '6');
    rules(s, ['NEW screen: Household tile opens this hub first (HouseholdFragment.kt:82-95; IconDataset.kt:494-506)', 'Households → M2.19-22; Non-household → M2.19-35']);
  }
  {
    const s = F('M2.19-22 · Households v2 (delete, states)', 3, 3);
    screenTop(s, 'Households', '248 households');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search HOF name, mobile or HH ID');
    await actionCard(b, { name: 'Ramesh Kumar', initials: 'RK', meta: 'HH ID 7012 3345 0012', sync: 'Synced', rows: [['Members', '5'], ['Mobile', '98765 43210']], icons: [['delete', 'Delete', 'color/error']], actions: [['Outlined', 'Members'], ['Tonal', 'Add beneficiary']] });
    await actionCard(b, { name: 'Mohan Lal Yadav', initials: 'MY', meta: 'HH ID 7012 3345 0013', rows: [['Members', '0'], ['Mobile', '91234 56780']], icons: [['delete', 'Delete', 'color/error']], actions: [['Outlined', 'Members'], ['Tonal', 'Add beneficiary']], footnote: '0 members: Add beneficiary opens Head-of-family registration (relation = Self)' });
    await actionCard(b, { name: 'Shankar Das', initials: 'SD', meta: 'HH ID 7012 3345 0019', state: 'deleted', rows: [['Members', '3']], actions: [['Outlined', 'Members', true]], footnote: 'Deleted: grey card, Add beneficiary hidden, Members greyed, card taps blocked' });
    fab(s, 'New household', 'add');
    rules(s, [
      'Delete icon → confirm M2.19-23; soft delete (deactivate + marked for sync); not gated by sync state; no undo (AllHouseholdFragment.kt:88-97; VM:128-143)',
      'Deleted state: grey, "Deleted Record" label, add hidden, members greyed, taps blocked (HouseHoldListAdapter.kt:83-92)',
      'Tap card body → household form in view mode with Edit (M2.13-09) – ASHA role only (:211-220)',
      '0 members: Add beneficiary → HOF registration, relation Self (:231-238)',
      'With members: Add beneficiary → gender + filtered relation dialog M2.19-26 (:99-160)',
      'New household FAB checks for a draft first → M2.19-25 (:66-76)',
      'Search has a mic (voice search) (:58-63, :288-289)',
      'Filaria disease mode: same card shows "Add MDA" instead of "Add beneficiary" (adapter :63-77; see M2.8-07)',
    ]);
  }
  {
    const s = F('M2.19-23 · Delete household confirm', 4, 3, true);
    screenTop(s, 'Households', '248 households');
    const b = body(s); b.itemSpacing = 12;
    await actionCard(b, { name: 'Ramesh Kumar', meta: 'HH ID 7012 3345 0012', icons: [['delete', 'Delete', 'color/error']], actions: [['Outlined', 'Members'], ['Tonal', 'Add beneficiary']] });
    await dialog(s, { ic: 'delete', title: 'Delete Household', body: 'Are you sure you want to delete Ramesh Kumar?', cancel: 'No', ok: 'Yes', danger: true });
    rules(s, ['Text from code (AllHouseholdFragment.kt:88-97)', 'Yes → household deactivated, marked for sync, card turns to Deleted state']);
  }
  {
    const s = F('M2.19-24 · Households · supervisor (read-only)', 5, 3);
    screenTop(s, 'Households', 'Sunita Kumari · 248 households');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'View only', 'Supervisors can see households but cannot add or edit.', null);
    searchRow(b, 'Search HOF name, mobile or HH ID');
    await actionCard(b, { name: 'Ramesh Kumar', initials: 'RK', meta: 'HH ID 7012 3345 0012', rows: [['Members', '5']], actions: [['Outlined', 'Members'], ['Tonal', 'Add beneficiary', true]] });
    await actionCard(b, { name: 'Mohan Lal Yadav', initials: 'MY', meta: 'HH ID 7012 3345 0013', rows: [['Members', '0']], actions: [['Outlined', 'Members'], ['Tonal', 'Add beneficiary', true]] });
    rules(s, ['ASHA-supervisor role, hosted in SupervisorActivity (AllHouseholdFragment.kt:179-191, :205-212, :231)', 'No New household FAB; card edit and Add beneficiary disabled', 'Household members (05) for supervisor: no Add member FAB, no ABHA button']);
  }

  // =========================================================================
  // ROW 4 · Draft dialog, add-member dialog, Household members (05 v2), delete
  // =========================================================================
  {
    const s = F('M2.19-25 · Incomplete form found (draft)', 0, 4, true);
    screenTop(s, 'Households', '248 households');
    await dialog(s, { ic: 'form', icColor: 'color/primary', title: 'Incomplete form found', body: 'You have a household form that was not finished. Open it or start a new one?', cancel: 'Create new', ok: 'Open draft' });
    rules(s, ['Before New household (AllHouseholdFragment.kt:66-76, :283-286)', 'Same dialog per type on member registration (NewBenRegTypeFragment.kt:68-87)', 'Create new discards the draft']);
  }
  {
    const s = F('M2.19-26 · Add member · gender + relation', 1, 4, true);
    await membersBehind(s);
    await dialog(s, { ic: 'person_add', icColor: 'color/primary', title: 'Add member', body: null, cancel: 'Cancel', ok: 'OK',
      extra: async (c) => {
        await seg(c, 'Gender', ['Female', 'Male'], 1);
        await radios(c, 'Relation to head of family', ['Son', 'Brother', 'Grandson', 'Other'], 0);
        await caption(c, 'Hidden: Father, Mother (already registered), Husband/Wife (HOF spouse exists).');
      } });
    rules(s, [
      'Opens from Add beneficiary (household with members) and Add member FAB (AllHouseholdFragment.kt:99-160, :380-427)',
      'Relation list filtered: father/mother/wife/husband removed once registered; unmarried HOF → no spouse/child relations; same-sex spouse rules',
      'OK disabled until a relation is picked',
      'Live flow in code; M2.13-02 Child/Adult screen is dormant',
    ]);
  }
  {
    const s = F('M2.19-27 · Household members v2 (all buttons)', 2, 4);
    screenTop(s, 'Household members', 'HH 7012 3345 0012 · Ramesh Kumar');
    const b = body(s); b.itemSpacing = 12;
    await actionCard(b, { name: 'Ramesh Kumar', meta: 'Head of family · M · 46 yrs · Ben ID 1203 4455', sync: 'Synced', tags: [['Neutral', 'HOF']], rows: [['Mobile', '98765 43210'], ['Wife', 'Sunita Devi']], icons: [['call', 'Call'], ['id_card', 'ABHA'], ['eye_surgery', 'Cataract', 'color/success'], ['delete', 'Delete', 'color/error']] });
    await actionCard(b, { name: 'Sunita Devi', meta: 'Wife · F · 34 yrs · Ben ID 1203 4456', sync: 'Pending', rows: [['Husband', 'Ramesh Kumar'], ['Children', '0 (has children = Yes)']], icons: [['call', 'Call'], ['id_card', 'ABHA'], ['delete', 'Delete', 'color/error']], actions: [['Tonal', 'Register Children']] });
    await actionCard(b, { name: 'Suresh Kumar', meta: 'Son · M · 24 yrs · married', rows: [['Father', 'Ramesh Kumar']], icons: [['call', 'Call'], ['id_card', 'ABHA'], ['delete', 'Delete', 'color/error']], actions: [['Tonal', 'Register Wife']] });
    await actionCard(b, { name: 'Pooja Kumari', meta: 'Daughter · F · 22 yrs · married', rows: [['Father', 'Ramesh Kumar']], icons: [['call', 'Call'], ['id_card', 'ABHA'], ['delete', 'Delete', 'color/error']], actions: [['Tonal', 'Register Husband']] });
    await actionCard(b, { name: 'Kamla Devi', meta: 'Mother · F · 68 yrs', rows: [['Children', '4']], icons: [['call', 'Call'], ['id_card', 'ABHA'], ['eye_surgery', 'Cataract', 'color/warning'], ['delete', 'Delete', 'color/error']], actions: [['Outlined', 'View Children']] });
    await actionCard(b, { name: 'Ram Prasad', meta: 'Father · M · 71 yrs', state: 'deceased', stateNote: 'Deceased · 02 Oct 2026', rows: [['Date of death', '02 Oct 2026']] });
    await actionCard(b, { name: 'Mohan Kumar', meta: 'Son · M · 12 yrs', state: 'deleted', stateNote: 'Deleted Record' });
    fab(s, 'Add member', 'person_add');
    rules(s, [
      'Delete icon → confirm M2.19-28; soft delete (HouseholdMembersFragment.kt:284-293, :452-469)',
      'HOF cannot be deleted while other members exist → toast (M2.19-29) (:454-466)',
      'Delete hidden for dead and already-deleted members; not gated by sync (BenListAdapter.kt:288-349)',
      'Deleted: grey, "Deleted Record", ABHA/call/sync/actions hidden, taps blocked (:312-327)',
      'Register Wife: married male, no spouse · Register Husband: married female, no spouse · Register Children: married female with spouse, has children = Yes, 0 children · View Children: female with children > 0 (:187-246)',
      'Register Wife/Husband → spouse form; Register/View Children → child registration M2.19-51',
      'Cataract: age ≥ 40 and alive; orange = eye-surgery record exists, green = none → visits sheet M2.19-36 (rv_item_ben.xml:662-677; BindingUtils.kt:542-550)',
      'Code bug: Cataract tap also navigates straight to the form (HouseholdMembersFragment.kt:421-433) – intended: sheet only',
      'Card tap → member registration form in view mode (M2.19-46) – ASHA only, not if deleted (:365-378)',
      'ABHA button hidden for supervisor and dead members (:415-417); call hidden for dead/deleted; denied permission → toast + request (:436-451)',
      'Add member FAB: ASHA only, disabled until members load, then M2.19-26 (:341-351)',
      'Malaria ON/OFF switch + tap → Malaria/Kala-azar form when opened from Disease control: dead today (isFromDisease = 0, VM:27)',
    ]);
  }
  {
    const s = F('M2.19-28 · Delete beneficiary confirm', 3, 4, true);
    await membersBehind(s);
    await dialog(s, { ic: 'delete', title: 'Delete Beneficiary', body: 'Are you sure you want to delete Sunita Devi?', cancel: 'No', ok: 'Yes', danger: true });
    rules(s, ['Text from code (HouseholdMembersFragment.kt:452-469)', 'Yes → deactivated, marked for sync, card shows Deleted Record', 'Not offered on All beneficiaries (BenPagingAdapter isSoftDeleteEnabled=false)']);
  }
  {
    const s = F('M2.19-29 · HOF delete blocked', 4, 4, true);
    await membersBehind(s);
    await toast(s, 'Head of Family cannot be deleted when other members exist.', 140);
    rules(s, ['Tapping Delete on the HOF while other active members exist (HouseholdMembersViewModel.kt:170-178)', 'Delete the other members first']);
  }
  {
    const s = F('M2.19-30 · Household members · loading / supervisor', 5, 4);
    screenTop(s, 'Household members', 'HH 7012 3345 0012 · Ramesh Kumar');
    const b = body(s); b.itemSpacing = 12;
    for (let i = 0; i < 2; i++) add(b, kinst('Skeleton card'));
    await caption(b, 'ASHA: Add member FAB is disabled until members have loaded.');
    await actionCard(b, { name: 'Ramesh Kumar', meta: 'Head of family · M · 46 yrs', tags: [['Neutral', 'HOF']], rows: [['Mobile', '98765 43210']], icons: [['call', 'Call']], footnote: 'Supervisor view: no ABHA, no delete, no Add member FAB, card tap does nothing' });
    fab(s, 'Add member', 'person_add', true);
    rules(s, ['FAB disabled while loading (HouseholdMembersFragment.kt:341-351)', 'Supervisor: read only (:205-212, :415-417)']);
  }

  // =========================================================================
  // ROW 5 · All beneficiaries (06 v2), filter, ABHA / RCH lists, non-household
  // =========================================================================
  {
    const s = F('M2.19-31 · All beneficiaries v2 (card rules)', 0, 5);
    screenTop(s, 'All beneficiaries', '1,104 beneficiaries');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID', { filter: true });
    chips(b, ['All', 'With ABHA', 'Without ABHA', 'Age above 40'], 0);
    await actionCard(b, { name: 'Sunita Devi', meta: 'F · 34 yrs · Ben ID 1203 4456', sync: 'Synced', tags: [['Success', 'New ABHA'], ['Success', 'Mobile verified']], rows: [['ABHA', '91-4455-2201-7788'], ['Husband', 'Ramesh Kumar'], ['Head of family', 'Ramesh Kumar'], ['Registered', '12 Mar 2025']], icons: [['call', 'Call'], ['id_card', 'ABHA']], actions: [['Tonal', 'Register Children']] });
    await actionCard(b, { name: 'Rakesh Paswan', meta: 'M · 38 yrs · Ben ID 1203 4501', sync: 'Pending', tags: [['Alert', 'No household'], ['Neutral', 'Mobile not verified']], rows: [['Father', 'Not available']], icons: [['call', 'Call'], ['id_card', 'ABHA']] });
    await actionCard(b, { name: 'Lakshmi Devi', meta: 'F · 63 yrs · Ben ID 1203 4470', tags: [['Neutral', 'Old ABHA']], rows: [['Husband', 'Shyam Lal']], icons: [['call', 'Call'], ['id_card', 'ABHA'], ['eye_surgery', 'Cataract', 'color/warning']] });
    await actionCard(b, { name: 'Ram Prasad', meta: 'M · 71 yrs', state: 'deceased', stateNote: 'Deceased', rows: [['Date of death', '02 Oct 2026']] });
    await toast(s, 'Please allow permissions first', 60);
    rules(s, [
      'Filter icon → M2.19-32 (All / With ABHA / Without ABHA / Age above 40) (AllBenFragment.kt:76-146)',
      'ABHA number with New / Old ABHA marker (rv_item_ben.xml:600-636)',
      'Mobile consent verified / not verified icon; hidden on Mitanin (:505-511)',
      'Non-household icon when hhId = 0; HOF badge vs "Head of family: name" row (BenListAdapter.kt:100-128)',
      'Avatar picture by age & gender: baby, infant, boy, girl, man, woman, elderly ≥ 60 (:131-183) – design uses initials',
      'Relation line: father / husband / spouse chosen by gender, age > 15; "Not Available" shown as is (:255-286)',
      'Register Wife/Husband/Children, View Children: same rules as M2.19-27; ASHA only (AllBenFragment.kt:171-221)',
      'Cataract: age ≥ 40 + alive; orange = record exists, green = none → M2.19-36 (:233-246)',
      'ABHA: not synced yet → alert M2.19-34; hidden for supervisor, invisible for dead (:357-370)',
      'Dead: grey, death icon, Date of death row; call/sync/ABHA/spouse-child buttons hidden (BenListAdapter.kt:288-307)',
      'Sync icon per card, hidden for dead (:83, :296)',
      'No Delete on this list (only in Household members)',
      'Call denied permission → toast "Please allow permissions first" + request (:248-259)',
      'Search has voice mic (:309-310)',
    ]);
  }
  {
    const s = F('M2.19-32 · Filter dialog', 1, 5, true);
    await allBenBehind(s);
    await dialog(s, { ic: 'filter', icColor: 'color/primary', title: 'Filter', body: null, cancel: 'Cancel', ok: 'Apply',
      extra: async (c) => { await radios(c, null, ['All', 'With ABHA', 'Without ABHA', 'Age above 40 years'], 1); } });
    rules(s, ['res/layout/alert_filter.xml; AllBenFragment.kt:76-117', 'Same filter on ABHA / RCH lists', 'String age_above_30_years says "Age above 40 years" (strings mismatch)']);
  }
  {
    const s = F('M2.19-33 · ABHA list (CSV download)', 2, 5);
    screenTop(s, 'ABHA beneficiaries', '602 with ABHA');
    const b = body(s); b.itemSpacing = 12;
    const r = row(b, 'Tools', 8); searchRow(r, 'Search name or ABHA', { filter: true }).layoutSizingHorizontal = 'FILL';
    await iconBtn(r, 'download', 'color/primary', 'CSV').label;
    await actionCard(b, { name: 'Sunita Devi', meta: 'F · 34 yrs · Ben ID 1203 4456', tags: [['Success', 'New ABHA']], rows: [['ABHA', '91-4455-2201-7788']], icons: [['call', 'Call'], ['id_card', 'ABHA']] });
    await actionCard(b, { name: 'Lakshmi Devi', meta: 'F · 63 yrs', tags: [['Neutral', 'Old ABHA']], rows: [['ABHA', '91-2210-9087-1144']], icons: [['call', 'Call'], ['id_card', 'ABHA']] });
    await toast(s, 'CSV Downloaded', 60);
    rules(s, [
      'Same All-ben screen reused by source: 1 ABHA list, 2 RCH list, 3 age ≥ 30, 4 eligible females, 5 Non-household – each with its own title (AllBenFragment.kt:136-155, :375-400)',
      'CSV icon on ABHA / RCH / source 3-4 lists → Downloads/ABHAUsers_<time>.csv; toasts "CSV Downloaded" / "No data to export" (AllBenViewModel.kt:117-150)',
      'Opened from Today ABHA card (source 1) and RCH card (source 2)',
      'Legacy AbhaBenFragment (same features) is not reachable in code – not designed separately',
    ]);
  }
  {
    const s = F('M2.19-34 · ABHA · record not synced', 3, 5, true);
    await allBenBehind(s);
    await dialog(s, { ic: 'sync', icColor: 'color/warning', title: 'Not synced yet', body: 'Please wait for the record to sync and try again.', cancel: null, ok: 'OK' });
    rules(s, ['ABHA tap on a beneficiary with no server ID (AllBenFragment.kt:357-370)', 'Synced → ABHA flow M2.10-01']);
  }
  {
    const s = F('M2.19-35 · Non-household list', 4, 5);
    screenTop(s, 'Non-household', '6 people with no household');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID');
    btn(b, 'Filled', 'Add beneficiary', false, true);
    await actionCard(b, { name: 'Rakesh Paswan', meta: 'M · 38 yrs · Railway platform', tags: [['Alert', 'No household']], rows: [['Mobile', '90011 22334']], icons: [['call', 'Call'], ['id_card', 'ABHA']] });
    await actionCard(b, { name: 'Gita Bai', meta: 'F · 72 yrs · Old-age home', tags: [['Alert', 'No household']], icons: [['call', 'Call'], ['eye_surgery', 'Cataract', 'color/success']] });
    rules(s, [
      'NEW: source 5 of All-ben list, opened from the Household hub (AllBenFragment.kt:136-140)',
      'Add beneficiary → living place form M2.13-03 → registration with no household (relation Self, shared household 0)',
      'Card tap → registration view with "Link to household" (M2.19-46)',
      'Code note: non-household Add beneficiary else-branch is unreachable (:303-307)',
    ]);
  }

  // =========================================================================
  // ROW 6 · Eye surgery (cataract) + IFA
  // =========================================================================
  const benBehind = async (s, t = 'All beneficiaries') => { screenTop(s, t, '1,104 beneficiaries'); const b = body(s); await actionCard(b, { name: 'Lakshmi Devi', meta: 'F · 63 yrs', icons: [['call', 'Call'], ['eye_surgery', 'Cataract', 'color/warning']] }); };
  {
    const s = F('M2.19-36 · Eye surgery visits · none yet', 0, 6, true);
    await benBehind(s);
    await visitSheet(s, { title: 'Eye surgery visits', subtitle: 'Lakshmi Devi · F · 63 yrs', visits: [], add: { label: 'Add new visit' }, footnote: 'No eye done yet: the form lets you choose Left, Right or Both.' });
    rules(s, ['NEW (EyeSurgeryBottomSheetFragment.kt:45-150)', 'Add row only while an eye is still available: none → Left/Right/Both; Left done → Right only; Right done → Left only; Both or L+R → no add (EyeSurgeryListViewModel.kt:19-38)']);
  }
  {
    const s = F('M2.19-37 · Eye surgery visits · left eye done', 1, 6, true);
    await benBehind(s);
    await visitSheet(s, { title: 'Eye surgery visits', subtitle: 'Lakshmi Devi · F · 63 yrs', visits: [{ label: 'Left eye visit 1', date: '12 Aug 2026', status: 'Done', tone: 'Success', action: 'View' }], add: { label: 'Add new visit · Right eye' }, footnote: 'Only one eye left: the form opens with Right eye pre-selected and locked.' });
    rules(s, ['Tap a visit → form in view mode (M2.19-40)', 'One eye left → eye pre-selected and locked on the form (EyeSurgeryFormViewModel.kt:128-168)']);
  }
  {
    const s = F('M2.19-38 · Eye surgery visits · both done', 2, 6, true);
    await benBehind(s);
    await visitSheet(s, { title: 'Eye surgery visits', subtitle: 'Lakshmi Devi · F · 63 yrs', visits: [{ label: 'Left eye visit 1', date: '12 Aug 2026', status: 'Done', tone: 'Success', action: 'View' }, { label: 'Right eye visit 2', date: '20 Oct 2026', status: 'Done', tone: 'Success', action: 'View' }], add: { label: 'Add new visit', disabled: true, reason: 'Both eyes done. No more visits.' } });
    rules(s, ['Code hides the add row; design shows it disabled with the reason – either is fine']);
  }
  {
    const s = F('M2.19-39 · Eye surgery form (new)', 3, 6);
    screenTop(s, 'Cataract / eye surgery', 'Lakshmi Devi · F · 63 yrs', 'none');
    const b = body(s); b.itemSpacing = 12;
    await person(b, 'LD', 'Lakshmi Devi', 'Female · 63 yrs · Ben ID 1203 4470');
    tf(b, 'Error', { Label: 'Date of surgery *', Value: '05 Aug 2026', Helper: 'Must be after the previous visit (12 Aug 2026)', 'Show helper': true }, 'calendar');
    auto(b, 'Eye operated', 'Right eye');
    select(b, 'Place of surgery *', 'District hospital');
    text(b, 'Hospital name', 'DH Kamrup');
    await seg(b, 'Lens implanted?', ['Yes', 'No'], 0);
    add(b, kinst('Photo tile', 'State=Empty'), false);
    await caption(b, 'Photo: camera or gallery');
    bottomBar(s, null, 'Save');
    rules(s, [
      'Server-driven form (EYE_SURGERY_FORM_ID via FormRendererAdapter) – fields shown are an example (EyeSurgeryFormFragment.kt:137-170)',
      'Header: name / gender / age',
      'Visit date: not in the future; after previous visit date; picker limited min/max (:200-258)',
      'Eye locked when only one eye is left',
      'Photo: camera or gallery, compressed to base64 (:64-127)',
      'Save → toast "Eye surgery details saved successfully" / "Failed to submit form"; back to list, button colour refreshes (:296-314)',
    ]);
  }
  {
    const s = F('M2.19-40 · Eye surgery form · view mode', 4, 6);
    screenTop(s, 'Cataract / eye surgery', 'Lakshmi Devi · Left eye visit 1', 'none');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Saved 12 Aug 2026', editable: true });
    await infoCard(b, 'Left eye visit 1', [['Date of surgery', '12 Aug 2026'], ['Eye operated', 'Left eye'], ['Place of surgery', 'District hospital'], ['Lens implanted', 'Yes']]);
    add(b, kinst('Photo tile', 'State=Captured'), false);
    await toast(s, 'Eye surgery details saved successfully', 80);
    rules(s, ['View mode: Edit FAB unlocks fields; Save hidden in view mode (EyeSurgeryFormFragment.kt:180-194, :224-225)']);
  }
  {
    const s = F('M2.19-41 · IFA distribution (history)', 5, 6);
    screenTop(s, 'IFA distribution', 'Sunita Devi · F · 34 yrs', 'none');
    const b = body(s); b.itemSpacing = 12;
    await person(b, 'SD', 'Sunita Devi', 'Female · 34 yrs · Eligible couple', [['Maternal', 'Reproductive status: Eligible']]);
    await table(b, 'IFA bottles given', ['Date', 'Bottles'], [['12 Aug 2026', '1'], ['10 Sep 2026', '1'], ['08 Oct 2026', '2']]);
    await blockedAdd(b, 'Add new', 'Already added for this month');
    rules(s, [
      'NEW (BenIfaFormFragment.kt)',
      'Entry: IFA button on the EC registration list card – female, age 20-49, reproductive status 1 or 2 (rv_item_ben_ec_with_form.xml:352-354; BindingUtils.kt:552-561)',
      'All-ben IFA route is dead (button always isIFA=false)',
      'Only one entry per month: else toast "Already added for this month" (BenIfaFormViewModel.kt:62-66)',
    ]);
  }

  // =========================================================================
  // ROW 7 · IFA form, registration preview / spouse / children, view + link
  // =========================================================================
  {
    const s = F('M2.19-42 · IFA distribution · add new', 0, 7);
    screenTop(s, 'IFA distribution', 'Sunita Devi · F · 34 yrs', 'none');
    const b = body(s); b.itemSpacing = 12;
    await table(b, 'IFA bottles given', ['Date', 'Bottles'], [['12 Aug 2026', '1'], ['10 Sep 2026', '1']]);
    const rv = reveal(b);
    date(rv, 'Date of distribution *', '05 Nov 2026');
    steppers(rv, [['Bottles given *', '1']]);
    await uploads(rv, 'Photo (optional)', [['ifa_nov.jpg', 'Done']], 'Camera or gallery');
    bottomBar(s, 'Cancel', 'Save');
    rules(s, ['"Add new" reveals the form (BenIfaFormFragment.kt:177-188)', 'Date: not future, after previous, already-filled dates blocked (VM:239-270)', 'Fields are server-driven; photo upload (:125-136, :233-270)']);
  }
  {
    const s = F('M2.19-43 · Register member · preview before save', 1, 7, true);
    screenTop(s, 'Register member', "Ramesh Kumar's family", 'none');
    scrim(s);
    const sh = sheet(s, 'Sheet · Preview');
    sh.appendChild(await txt('Check before saving', 'Title/Large', 'color/on-surface'));
    const ic = AL('VERTICAL', 'Values'); ic.itemSpacing = 0; add(sh, ic);
    for (const [l, v] of [['Name', 'Sunita Devi'], ['Gender', 'Female'], ['Date of birth', '14 Jan 1992 (34 yrs)'], ['Relation to HOF', 'Wife'], ['Mobile', '98765 43210'], ['Marital status', 'Married'], ['Has children', 'Yes']]) add(ic, kinst('Info row', null, { Label: l, Value: v }));
    const ar = row(sh, 'Actions', 8); add(sh, ar); const e = btn(ar, 'Outlined', 'Edit'); e.layoutSizingHorizontal = 'FILL'; const sb = btn(ar, 'Filled', 'Submit'); sb.layoutSizingHorizontal = 'FILL';
    pinBottom(s, sh);
    rules(s, ['NEW: preview sheet with all entered values (NewBenRegFragment.kt:615-620, :979-1004; PreviewBottomsheet.kt)', 'Edit closes the sheet; Submit saves and auto-triggers upload sync (:806)', 'Save → back to the household; non-household person → back to Non-household list (:81-100)']);
  }
  {
    const s = F('M2.19-44 · After save · add spouse?', 2, 7, true);
    await membersBehind(s);
    await dialog(s, { ic: 'person_add', icColor: 'color/primary', title: 'Add spouse?', body: "Would you like to add Suresh Kumar's wife?", cancel: 'No', ok: 'Yes' });
    rules(s, ['After save when the person is married and has no spouse registered (NewBenRegFragment.kt:227-270, :808-812)', 'Text says wife or husband by gender', 'Yes opens the registration form again for the spouse']);
  }
  {
    const s = F('M2.19-45 · After save · add children?', 3, 7, true);
    await membersBehind(s);
    await dialog(s, { ic: 'child', icColor: 'color/primary', title: 'Add children?', body: 'Sunita Devi has children. Would you like to register them now?', cancel: 'No', ok: 'Yes' });
    rules(s, ['After save when "Do you have children" = Yes (NewBenRegFragment.kt:272-316, :814-819)', 'Yes opens child registration M2.19-51', 'If both apply, spouse prompt comes first']);
  }
  {
    const s = F('M2.19-46 · Member view mode + link to household', 4, 7);
    screenTop(s, 'Beneficiary details', 'Rakesh Paswan · no household', 'none');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Registered 02 Sep 2026', editable: true });
    banner(b, 'Info', 'Not linked to a household', 'Link Rakesh to an existing household or create a new one.', null);
    btn(b, 'Tonal', 'Link to household', false, true);
    await infoCard(b, 'Personal details', [['Name', 'Rakesh Paswan'], ['Gender', 'Male'], ['Age', '38 yrs'], ['Mobile', '90011 22334'], ['Living place', 'Railway platform']]);
    await viewMode(b, { saved: 'Deceased · 02 Oct 2026', editable: false, reason: 'Edit is hidden for dead beneficiaries' });
    rules(s, ['Existing person opens in view mode with Edit FAB (NewBenRegFragment.kt:624-631)', 'Edit hidden for dead beneficiaries (:839-842)', '"Link to household" only for non-household people → M2.19-47 (:369-503, :632-635)']);
  }
  {
    const s = F('M2.19-47 · Link to household · choose', 5, 7, true);
    screenTop(s, 'Beneficiary details', 'Rakesh Paswan · no household', 'none');
    await optionSheet(s, 'Link to household', [['home', 'Link to existing household'], ['add', 'Create new household']], -1, { subtitle: 'Rakesh Paswan · M · 38 yrs' });
    rules(s, ['Existing → household picker + relation (M2.19-48)', 'Create new → household form in link mode (M2.13-01): saving makes this person HOF and returns to Non-household list with toast "Beneficiary linked" (NewHouseholdFragment.kt:337-346)']);
  }

  // =========================================================================
  // ROW 8 · Link picker, OTP / age / documents, age picker, child bulk reg, consent, Get ben data
  // =========================================================================
  {
    const s = F('M2.19-48 · Link · pick household + relation', 0, 8);
    screenTop(s, 'Link to household', 'Rakesh Paswan', 'none');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search HOF name or HH ID');
    await radios(b, 'Household', ['Ramesh Kumar · HH 0012', 'Mohan Lal Yadav · HH 0013', 'Shyam Lal · HH 0021'], 2);
    select(b, 'Relation to head of family *', 'Brother');
    await caption(b, 'Relation list uses the same filtering as Add member (M2.19-26).');
    bottomBar(s, null, 'Link');
    await toast(s, 'Beneficiary linked', 110);
    rules(s, ['Searchable household picker → relation picker (NewBenRegFragment.kt:369-503)', 'Success toast "Beneficiary linked"; person leaves the Non-household list']);
  }
  {
    const s = F('M2.19-49 · Register member · OTP, age, documents', 1, 8);
    screenTop(s, 'Register member', "Ramesh Kumar's family", 'none');
    const b = body(s); b.itemSpacing = 12;
    const mr = row(b, 'Mobile', 8); const m = text(mr, 'Mobile number *', '98765 43210'); m.layoutSizingHorizontal = 'FILL'; btn(mr, 'Tonal', 'Send OTP');
    tf(b, 'Focused', { Label: 'Enter OTP', Value: '4 8 2 1 _ _', Helper: 'Resend OTP in 0:42', 'Show helper': true });
    banner(b, 'Success', 'Mobile verified', 'OTP is checked automatically at 6 digits when online.', null);
    tf(b, 'Filled', { Label: 'Date of birth *', Value: '14 Jan 1992', Helper: "Don't know? Enter age (years / months / days)", 'Show helper': true }, 'calendar');
    const ph = row(b, 'Photo', 12); ph.appendChild(kinst('Photo tile', 'State=Empty')); await caption(ph, 'Photo: camera only (no gallery)');
    await uploads(b, 'Birth certificate (front / back)', [['birth_cert_front.jpg', 'Done'], ['birth_cert_back.pdf', 'Done']], 'Camera, gallery or PDF · size limit toast · tap to view image or PDF');
    rules(s, [
      'OTP: Send / Resend with countdown; auto-verify at 6 digits when online (NewBenRegFragment.kt:654-712, :1084-1105)',
      'Age picker dialog (M2.19-50) as alternative to DOB (AgePickerDialog.kt:41-139)',
      'Photo from camera only (:923-939)',
      'Birth certificate front/back: camera / gallery / PDF, file-size toast, viewer; hidden on Mitanin (:183-210, :716-735, :1111-1150)',
      'ABHA / Ayushman lookup + prefill (Mitanin), "ABHA exists" dialog, network toast – same as household (M2.13-08) (:350-367, :568-612)',
      'Mitanin new non-HOF: Submit gated on ABHA fetch – currently commented out (:104-107)',
      'Voice input (mic) on text fields (:109-116)',
      'Legacy 3-page adult form (NewBenRegG15Fragment) is reachable only from the dormant type screen – not designed',
    ]);
  }
  {
    const s = F('M2.19-50 · Age picker', 2, 8, true);
    screenTop(s, 'Register member', "Ramesh Kumar's family", 'none');
    await dialog(s, { ic: 'calendar', icColor: 'color/primary', title: 'Enter age', body: null, cancel: 'Cancel', ok: 'OK',
      extra: async (c) => { steppers(c, [['Years', '34'], ['Months', '2'], ['Days', '10']]); await caption(c, 'Limits follow the relation and form (min / max age).'); } });
    rules(s, ['Years / months / days spinners with min/max (AgePickerDialog.kt:41-139)', 'OK fills an estimated date of birth']);
  }
  {
    const s = F('M2.19-51 · Register children (from mother card)', 3, 8);
    screenTop(s, 'Register children', 'Sunita Devi · F · 34 yrs', 'none');
    const b = body(s); b.itemSpacing = 12;
    steppers(b, [['Number of live children *', '2']]);
    auto(b, 'Gap: marriage to 1st child', '1 yr 8 mths');
    section(b, 'Open', 'Child 1', 'Done', 1);
    const r1 = AL('VERTICAL', 'Child 1'); r1.itemSpacing = 10; add(b, r1);
    text(r1, 'Name *', 'Aarav Kumar'); date(r1, 'Date of birth *', '03 Mar 2016'); auto(r1, 'Age', '10 yrs'); await seg(r1, 'Sex *', ['Male', 'Female'], 0);
    auto(b, 'Gap: child 1 to child 2', '3 yrs 4 mths');
    section(b, 'Closed', 'Child 2', '1 of 4', 2);
    await S.stat(b, [['1', 'Male', 'child'], ['1', 'Female', 'maternal']]);
    bottomBar(s, null, 'Save');
    rules(s, [
      'NEW (NewChildAsBenRegistrationFragment.kt:238-305); fields in 2.16 spec NewChildBenRegDataset',
      'From Register Children / View Children on a mother card; View Children opens in view mode with Edit',
      'Up to 9 children in one form; auto age; auto gaps marriage→child 1 and between children; live male/female counts',
      'Consent dialog on entry; GPS-off → settings dialog; save toast + auto sync (:72-135, :159-160, :208-234)',
    ]);
  }
  {
    const s = F('M2.19-52 · Registration consent', 4, 8, true);
    screenTop(s, 'Register member', "Ramesh Kumar's family", 'none');
    await dialog(s, { ic: 'shield', icColor: 'color/primary', title: 'Consent', body: 'I have explained to the family why their health details are collected and they agree to share them.', cancel: 'Cancel', ok: 'Continue',
      extra: async (c) => { add(c, kinst('Checkbox option', 'Checked=false', { Label: 'Family has given consent' })); } });
    await toast(s, 'Please tick the checkbox', 90);
    rules(s, [
      'Cannot be dismissed; tick required else toast; Cancel exits the form',
      'Shown on new household (NewHouseholdFragment.kt:96-130), new member (NewBenRegFragment.kt:505-545), child registration, and type screen (NewBenRegTypeFragment.kt:38-66)',
      'Mitanin builds use their own consent text (NewHouseholdFragment.kt:386-388; NewBenRegFragment.kt:1055)',
    ]);
  }
  {
    const s = F('M2.19-53 · Get beneficiary data (code version)', 5, 8);
    screenTop(s, 'Get beneficiary data', 'From server · page 2 of 14', 'none');
    const b = body(s); b.itemSpacing = 12;
    for (const [n, m] of [['Anita Kumari', 'F · 29 yrs · Ben ID 1203 5511'], ['Bablu Yadav', 'M · 41 yrs · Ben ID 1203 5512'], ['Chanda Devi', 'F · 55 yrs · Ben ID 1203 5513']]) await actionCard(b, { name: n, meta: m });
    const pg = row(b, 'Pages', 6); for (const p of ['1', '2', '3', '4', '…', '14']) btn(pg, p === '2' ? 'Filled' : 'Outlined', p);
    add(b, kinst('Empty state', null, { Title: 'Could not load data', Body: 'The server returned no data. Try again later.', Icon: 'wifi_off' }), false);
    rules(s, ['Code: server list loaded page by page with page-number buttons (toast "Page N clicked") (GetBenFragment.kt:48-89)', 'Empty result → error panel (:58-62)', 'Not reachable today (drawer item commented out); M2.11-06 search/download design is a different model – Not in app today – needs dev']);
  }

  // =========================================================================
  // ROW 9 · Mitanin variants (mitanin / mitaninStag / mitaninUat, Chhattisgarh)
  // =========================================================================
  {
    const s = F('M2.19-54 · Login · Mitanin', 0, 9);
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 24; b.itemSpacing = 14;
    const top = row(b, 'Brand row', 8);
    top.appendChild(await txt('Mitanin', 'Headline/Large', 'color/primary')); tag(top, 'Alert', 'DEMO'); S.spacer(top);
    const lang = row(top, 'Language', 4); lang.appendChild(icon('translate', 'color/primary', 22)); lang.appendChild(await txt('Hindi', 'Label/Large', 'color/primary'));
    b.appendChild(await txt('Log in to continue', 'Title/Medium', 'color/on-surface'));
    text(b, 'Username', 'mitanin_rekha'); tf(b, 'Filled', { Label: 'Password', Value: '••••••••' }, 'visibility');
    add(b, kinst('Checkbox option', 'Checked=true', { Label: 'Remember me' }));
    btn(b, 'Filled', 'Log in', false, true);
    await radios(b, 'Language sheet on Mitanin', ['English', 'Hindi'], 1);
    await caption(b, 'Powered by Piramal Swasthya · Version 2.14.0 (214)');
    rules(s, [
      'Mitanin: app name "मितानिन" (-Stag / -Uat suffix) from build.gradle resValue app_native_name (:124-138)',
      'Mitanin: "DEMO" tag only on mitaninStag / mitaninUat or debug builds; hidden on production mitanin (SignInFragment.kt:288-291)',
      'Mitanin: language sheet shows English + Hindi only (LanguageBottomSheet.kt:55, :78; HomeActivity.kt:172-177)',
      'Mitanin: role routing, remember me, offline login, different-user dialog same as M2.19-01…03',
    ]);
  }
  {
    const s = F('M2.19-55 · Modules · Mitanin', 1, 9);
    homeTop(s, 'Modules', 'Raipur · Ward 7');
    const b = body(s); b.itemSpacing = 12;
    const g = tileGrid(b, [['household', 'Household', '312', 'home'], ['people', 'All beneficiaries', '1,386', 'group'], ['couple', 'Eligible couples', '', 'favorite'], ['maternal', 'Maternal health', '', 'pregnant'],
      ['child', 'Child care', '', 'child'], ['immunization', 'Immunization', '118', 'vaccine'], ['disease', 'Disease control', '', 'hospital'], ['care', 'NCD referred list', '5', 'form'], ['records', 'Death reports', '', 'form'], ['village', 'Mitanin/ Village Meetings', '', 'group']]);
    for (const m of g.findAll((n) => n.type === 'INSTANCE' && n.name === 'Module tile')) { const tt = m.componentProperties; const ck = Object.keys(tt).find((k) => k.startsWith('Count')); if (ck && tt[ck].value === '') setP(m, { 'Show count': false }); }
    add(s, inst('bottomNav', 'Active=Modules'));
    rules(s, [
      'Mitanin: village tile title "Mitanin/ Village Meetings" instead of "ASHA/ Village Meetings" (IconDataset.kt:53-57)',
      'Mitanin, inside village meetings: "Cluster Meetings" replaces "PHC review"; no Saas Bahu Sammelan; no U-WIN session (IconDataset.kt:324, :358, :411) – see stage 2.9',
      'Other tiles and counts same as M2.19-17',
    ]);
  }
  {
    const s = F('M2.19-56 · Profile & more · Mitanin', 2, 9);
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 8;
    await person(b, 'RS', 'Rekha Sahu', 'Mitanin · Employee ID 220187 · Raipur');
    await listGroup(b, 'My work', [['My profile details', 'person'], ['Monthly Claim Summary', 'wallet'], ['Badges & achievements', 'star'], ['Dashboard', 'dashboard']]);
    await listGroup(b, 'App', [['Language · Hindi', 'translate'], ['Support (feedback form)', 'help'], ['Delete my account', 'close', true], ['Log out', 'logout', true]]);
    add(s, inst('bottomNav', 'Active=Profile'));
    rules(s, [
      'Mitanin: "Incentives" item reads "Monthly Claim Summary" (HomeActivity.kt:893-903)',
      'Mitanin: language choice English / Hindi only (HomeActivity.kt:172-177)',
      'Non-Mitanin Home language dialog = English / Hindi / Assamese (Bangla commented out, :178); login sheet also has Bangla',
      'Delete-account form URL: Mitanin uses the "others" URL (HomeActivity.kt:954-975)',
    ]);
  }
  {
    const s = F('M2.19-57 · Register member · Mitanin', 3, 9);
    screenTop(s, 'Register member', "Ramesh Sahu's family", 'none');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Do you have ABHA ID / Ayushman ID?', ['Yes', 'No'], 0);
    const rv = reveal(b);
    const ar = row(rv, 'Lookup', 8); const ai = text(ar, 'ABHA / Ayushman number', '91-4455-2201-7788'); ai.layoutSizingHorizontal = 'FILL'; btn(ar, 'Tonal', 'Submit');
    banner(rv, 'Success', 'Details found', 'Name, gender and date of birth were filled from ABHA.', null);
    auto(b, 'Date of registration', '05 Nov 2026');
    auto(b, 'First name', 'Kavita');
    text(b, 'Last name', 'Sahu');
    text(b, 'Mobile number *', '98260 11223');
    await uploads(b, 'Birth certificate (front / back)', [], 'Mitanin: tapping upload does nothing (picker disabled)');
    rules(s, [
      'Mitanin: ABHA / Ayushman check + lookup on NEW non-HOF members (ben == null) before Date of registration (BenRegFormDataset.kt:1217-1219); for HOF it is captured on the household form (:938-943)',
      'Mitanin: lookup prefills the form; "ABHA exists" → create-ABHA dialog (NewBenRegFragment.kt:568-612)',
      'Mitanin: no "Temporary contact number / belongs to / Send OTP" fields – no OTP verification (BenRegFormDataset.kt:945, :1220, :2623, :2658)',
      'Mitanin: name of an already-registered beneficiary is read-only (TEXT_VIEW) (BenRegFormDataset.kt:1032-1036, :1346-1350)',
      'Mitanin: birth-certificate upload picker disabled (NewBenRegFragment.kt:719)',
      'Mitanin: own consent text on entry (strings_ben.xml mitanin_consent_text; NewBenRegFragment.kt:509)',
      'Mitanin: Submit gated on ABHA fetch for new non-HOF – currently commented out (NewBenRegFragment.kt:107)',
    ]);
  }
  {
    const s = F('M2.19-58 · All beneficiaries · Mitanin', 4, 9);
    screenTop(s, 'All beneficiaries', '1,386 beneficiaries');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID', { filter: true });
    await actionCard(b, { name: 'Kavita Sahu', meta: 'F · 27 yrs · Ben ID 2203 1145', sync: 'Synced', tags: [['Success', 'New ABHA']], rows: [['Mobile', '98260 11223'], ['Husband', 'Ramesh Sahu']], icons: [['call', 'Call'], ['id_card', 'ABHA']] });
    await actionCard(b, { name: 'Bhagwati Bai', meta: 'F · 61 yrs', rows: [['Mobile', '98271 55660']], icons: [['call', 'Call'], ['id_card', 'ABHA'], ['eye_surgery', 'Cataract', 'color/success']] });
    rules(s, [
      'Mitanin: no mobile-consent verified / not-verified icon next to the mobile (rv_item_ben.xml:509)',
      'Mitanin: household list / members internet gate for new beneficiary is commented out – registration works offline (AllHouseholdFragment.kt:256, :287; HouseholdMembersFragment.kt:354; HouseHoldListAdapter.kt:109)',
      'Everything else as M2.19-31',
    ]);
  }

  const y = await finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, y, 1200,
    'CORE SCREENS v2 · NOTES\n' +
    '• These frames replace the content of 01 Login, 02 Today, 03 Modules, 04 Households, 05 Household members and 06 All beneficiaries, which were drawn outside the plugin. Use M2.19-01 / 13 / 17 / 22 / 27 / 31 as the reference versions.\n' +
    '• Everything in a yellow RULES note comes from the app code. "Not in app today – needs dev" marks design-only items.\n' +
    '• App-wide dialogs (session expired, deactivated, rooted, update, permissions, logout, exit) belong to HomeActivity / LoginActivity and apply on every screen.');
  fitSection(sec);
  return ['2.19 core v2: ' + frames.length];
}
