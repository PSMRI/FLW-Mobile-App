// ---------------------------------------------------------------------------
// Stage 2.10 · ABHA
// Sources: ui/abha_id_activity/* (FindAbha, AadhaarId, AadhaarNumberAsha/Gov, AbhaConsent, AadhaarOtp,
// GenerateMobileOtp, VerifyMobileOtp, CreateAbha) and their layouts
// 2026-11-05 gap fix: session timer + close, Search/Create toggle, consent checklist, find flow, OTP variants,
// error / duplicate / download states, drawer (no beneficiary) variant. See gap_G_abha_supervisor (A1–A38).
// ---------------------------------------------------------------------------
async function buildStage210(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx);
  const S = SK(ctx);
  const G = GK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, bottomBar,
    screenTop, person, snack } = S;
  const { rules, dialog, caption, btn, tag, row } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.10 · ABHA');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };
  const steps = async (p, active) => {
    const r = AL('HORIZONTAL', 'Steps'); r.itemSpacing = 6; add(p, r);
    for (const [i, l] of ['Aadhaar', 'Consent', 'OTP', 'Mobile', 'Done'].entries()) {
      const c = AL('VERTICAL', l); c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 4; r.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      const bar = figma.createRectangle(); bar.resize(56, 4); bar.cornerRadius = 2; bar.fills = [paint(i < active ? 'color/success' : i === active ? 'color/primary' : 'color/outline-variant')]; c.appendChild(bar); bar.layoutSizingHorizontal = 'FILL';
      c.appendChild(await txt(l, 'Label/Small', i === active ? 'color/primary' : 'color/on-surface-variant'));
    }
  };
  const otpBoxes = async (p, digits, error = false) => {
    const r = AL('HORIZONTAL', 'OTP'); r.itemSpacing = 8; r.primaryAxisAlignItems = 'CENTER'; add(p, r);
    for (let i = 0; i < 6; i++) { const b = box('Digit', 44, 52); rad(b, 'radius/md'); fill(b, 'color/surface-container'); stroke(b, error ? 'color/error' : (i === digits.length ? 'color/primary' : 'color/outline'), error || i === digits.length ? 2 : 1); if (digits[i]) b.appendChild(await txt(digits[i], 'Title/Large', 'color/on-surface')); r.appendChild(b); }
    return r;
  };

  // ---- local helpers (gap fix)
  // Session strip under the top bar: 30-min countdown + close (home) icon (AbhaIdActivity.kt:93-105)
  const sessionBar = async (s, time = '28:42', warn = false) => {
    const r = AL('HORIZONTAL', 'Session bar'); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 8; pad(r, 'spacing/md', 'spacing/xs'); fill(r, warn ? 'color/error-container' : 'color/primary-subtle'); add(s, r);
    r.appendChild(icon('clock', warn ? 'color/error' : 'color/primary', 18));
    const t = await txt('Session ends in ' + time, 'Label/Medium', warn ? 'color/error' : 'color/primary'); r.appendChild(t); t.layoutSizingHorizontal = 'FILL';
    const x = AL('HORIZONTAL', 'Close ABHA flow'); x.counterAxisAlignItems = 'CENTER'; x.itemSpacing = 4; r.appendChild(x);
    x.appendChild(icon('close', 'color/on-surface-variant', 20)); x.appendChild(await txt('Close', 'Label/Medium', 'color/on-surface-variant'));
    return r;
  };
  const abhaTop = async (s, title, sub, time, warn) => { const bar = screenTop(s, title, sub, 'none'); await sessionBar(s, time, warn); return bar; };
  // recolour the trailing icon of a text field (validity tick)
  const tint = (f, color) => { const t = f.findOne((n) => n.name === 'Trailing'); if (t) { const g = t.findOne((n) => n.name === 'glyph'); if (g) g.fills = [paint(color)]; } return f; };
  const toggle = (p, sel) => seg(p, 'ABHA', ['Search ABHA', 'Create ABHA'], sel);
  const spinner = (p, d = 56) => {
    const e = figma.createEllipse(); e.name = 'Spinner'; e.resize(d, d); e.fills = [paint('color/primary')];
    e.arcData = { startingAngle: 0, endingAngle: 4.7, innerRadius: 0.82 }; add(p, e, false); return e;
  };
  const centerCol = (p, name) => { const c = AL('VERTICAL', name); c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 12; c.paddingTop = 48; add(p, c); return c; };
  const consentRow = async (p, on, enabled = true) => {
    const c = AL('VERTICAL', 'Consent'); c.itemSpacing = 0; add(p, c);
    const cb = kinst('Checkbox option', `Checked=${on}`, { Label: 'Consent for the creation of your ABHA number' }); add(c, cb);
    const l = await txt('Tap to read the consent (opens the consent screen)', 'Label/Medium', 'color/primary'); c.appendChild(l); wrapText(l);
    if (!enabled) c.opacity = 0.5;
    return c;
  };
  // Aadhaar result card (ABHA number)
  const abhaCard = async (p, name, num) => {
    const cardv = AL('VERTICAL', 'ABHA card'); pad(cardv, 'spacing/lg', 'spacing/lg'); gap(cardv, 'spacing/sm'); rad(cardv, 'radius/xl'); fill(cardv, 'color/primary'); add(p, cardv);
    const r = AL('HORIZONTAL'); r.counterAxisAlignItems = 'CENTER'; gap(r, 'spacing/sm'); add(cardv, r);
    r.appendChild(icon('id_card', 'color/on-primary', 28)); r.appendChild(await txt('ABHA number', 'Label/Large', 'color/primary-container'));
    cardv.appendChild(await txt(num, 'Headline/Small', 'color/on-primary'));
    cardv.appendChild(await txt(name, 'Body/Medium', 'color/primary-container'));
    return cardv;
  };
  const resultHead = async (p, ic, color, title) => {
    const r = AL('VERTICAL', 'Result'); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 8; add(p, r);
    r.appendChild(icon(ic, color, 56));
    const t = await txt(title, 'Title/Large', 'color/on-surface'); r.appendChild(t); t.textAlignHorizontal = 'CENTER';
    return r;
  };

  // 01 ABHA host · Search/Create toggle (opens on Create) · method dropdown · empty form
  {
    const s = F('M2.10-01 · ABHA · find or create', 0, 0);
    await abhaTop(s, 'ABHA', 'Ayushman Bharat Health Account', '29:58');
    const b = body(s); b.itemSpacing = 12;
    await person(b, 'SD', 'Sunita Devi', 'Female · 26 yrs · 98765 43210', [['Neutral', 'No ABHA yet']]);
    await toggle(b, 1);
    select(b, 'Verify with', 'Aadhaar No');
    b.appendChild(await txt('Enter details for Sunita Devi', 'Title/Small', 'color/on-surface'));
    S.tf(b, 'Default', { Label: 'Aadhaar number *', Value: 'Enter 12-digit Aadhaar' }, 'visibility');
    tint(S.tf(b, 'Default', { Label: 'Mobile number *', Value: 'Enter 10-digit mobile' }, 'check_circle'), 'color/outline');
    await consentRow(b, false);
    btn(b, 'Filled', 'Generate OTP', true, true);
    rules(s, [
      'Opens on the CREATE tab by default; remembers the last tab after going back (AadhaarIdFragment.kt:63-92, 207-241)',
      'Search ABHA tab → M2.10-09 (find by mobile). There is no separate chooser screen in the app',
      '"Verify with" dropdown shows on Create only; only "Aadhaar No" today (Fingerprint commented out) (AadhaarIdFragment.kt:108-121)',
      'ASHA / GOV radio is visibility="gone" in code (fragment_aadhaar_id.xml:113-118) → GOV demographic form cannot be reached; not drawn',
      'Session strip: 30:00 countdown starts when ABHA opens; at 0:00 the whole flow closes silently, progress is lost (AbhaIdActivity.kt:96-105). Warning before expiry: Not in app today – needs dev',
      'Close (home) icon closes the whole ABHA flow from any step, no confirm (AbhaIdActivity.kt:93)',
      'System back on this screen closes the whole ABHA flow, no confirm (AadhaarIdFragment.kt:38-44)',
      'Screen goes blank in recents / when the app is in background (Aadhaar privacy); tapjacking protection on (AbhaIdActivity.kt:108-116)',
      'Mitanin: same ABHA flow and labels on all flavours (no flavour checks in abha_id_activity/)',
    ]);
  }
  // 02 Aadhaar entry (ASHA) · valid + consent ticked → Generate OTP enabled
  {
    const s = F('M2.10-02 · Aadhaar number', 1, 0);
    await abhaTop(s, 'Create ABHA', 'Sunita Devi', '27:15');
    const b = body(s); b.itemSpacing = 12;
    await steps(b, 0);
    await toggle(b, 1);
    select(b, 'Verify with', 'Aadhaar No');
    b.appendChild(await txt('Enter details for Sunita Devi', 'Title/Small', 'color/on-surface'));
    tint(S.tf(b, 'Filled', { Label: 'Aadhaar number *', Value: '•••• •••• 4321', Helper: 'Masked like a password. Tap the eye to show. Copy / paste is blocked.', 'Show helper': true }, 'visibility'), 'color/on-surface-variant');
    const ok = row(b, 'Aadhaar valid', 6); ok.appendChild(icon('check_circle', 'color/success', 18)); ok.appendChild(await txt('Valid Aadhaar (Verhoeff check)', 'Label/Medium', 'color/success'));
    tint(S.tf(b, 'Filled', { Label: 'Mobile number *', Value: '98765 43210' }, 'check_circle'), 'color/success');
    await consentRow(b, true);
    bottomBar(s, null, 'Generate OTP');
    rules(s, [
      'Name is read-only from the record ("Enter details for <name>"); editable name field only when ABHA is opened without a beneficiary → M2.10-20 (AadhaarNumberAshaFragment.kt:76-99)',
      'Validity ticks: grey → green as you type; Aadhaar uses Verhoeff check (AadhaarNumberAshaFragment.kt:136-238)',
      'Consent checkbox cannot be ticked directly: tap opens Consent (M2.10-03); comes back ticked after "I Agree". Empty name → toast "Please Enter Beneficiary Name"',
      'Generate OTP stays on this screen; enabled only when Aadhaar valid + mobile valid + consent ticked (:162-164, 202-203)',
      'The mobile typed here is reused later: if it is not the Aadhaar mobile, the mobile OTP is auto-sent to it (M2.10-05). It is never asked again',
      'Step bar is design-only (no step bar in app)',
    ]);
  }
  // 03 Consent · 7 statements, #1 = select all, names inserted, "I Agree" returns to Aadhaar screen
  {
    const s = F('M2.10-03 · Consent', 2, 0);
    await abhaTop(s, 'Create ABHA', 'Consent', '26:40');
    const b = body(s); b.itemSpacing = 8;
    await steps(b, 1);
    b.appendChild(await txt('I hereby declare that:', 'Title/Medium', 'color/on-surface'));
    const items = [
      ['Select all · I am voluntarily sharing my Aadhaar Number / Virtual ID … for creating my ABHA number and ABHA address.', true],
      ['I intend to create ABHA number and address using a document other than Aadhaar.', true],
      ['I consent to linking my past government health records and those of this encounter.', true],
      ['I authorize sharing my health records with healthcare providers for this encounter.', true],
      ['I consent to anonymised use of my government health records for public health.', true],
      ['I, Rekha Kumari, confirm that I have informed and explained the consent to the beneficiary.', true],
      ['I, Sunita Devi, have been explained the consent above and give my consent.', false],
    ];
    for (const [l, on] of items) {
      const cb = kinst('Checkbox option', `Checked=${on}`, { Label: l }); add(b, cb);
      for (const nm of ['Rekha Kumari', 'Sunita Devi']) {
        const i = l.indexOf(nm); if (i < 0) continue;
        const t = cb.findOne((n) => n.name === 'Label' && n.type === 'TEXT');
        try { if (t) t.setRangeFills(i, i + nm.length, [paint('color/primary')]); } catch (e) { /* override not allowed: rule note explains */ }
      }
    }
    btn(b, 'Filled', 'I Agree', true, true);
    rules(s, [
      '7 statements, each with its own checkbox; statement 1 also acts as "select all" (AbhaConsentFragment.kt:57-94)',
      '"I Agree" enabled only when all 7 are ticked (here: 6 of 7 → disabled)',
      'Statement 6 inserts the ASHA\'s name, statement 7 the beneficiary\'s name, in bold italic primary colour (:98-106, 130-154)',
      '"I Agree" returns to the Aadhaar screen with the consent box ticked; it does NOT send the OTP (:51-54)',
      'Full legal text: strings_abha.xml str_aadhaar_consent_1…7 (shortened here)',
    ]);
  }
  // 04 Aadhaar OTP (create flow)
  {
    const s = F('M2.10-04 · Aadhaar OTP', 3, 0);
    await abhaTop(s, 'Verify Aadhaar OTP', 'Sunita Devi', '24:05');
    const b = body(s); b.itemSpacing = 16;
    await steps(b, 2);
    const m = await txt('OTP sent to Aadhaar registered mobile number ending with ******3210', 'Body/Medium', 'color/on-surface'); add(b, m); wrapText(m);
    b.appendChild(await txt('Enter the 6-digit OTP', 'Title/Medium', 'color/on-surface'));
    await otpBoxes(b, '4827');
    const t = await txt("Didn't receive OTP? Please wait 42 seconds", 'Body/Small', 'color/on-surface-variant'); add(b, t); t.textAutoResize = 'HEIGHT';
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    const x = inst('button', 'Style=Text, State=Disabled', { Label: 'Resend OTP', 'Show icon': false }); r.appendChild(x);
    bottomBar(s, null, 'Verify OTP');
    rules(s, [
      'Resend timer is 60 s (not 30 s). Resend shows toast "OTP resent" (AadhaarOtpFragment.kt:203-223)',
      'Exit button is hidden here; it appears only after a network error or an ABDM lock-out message → M2.10-12',
      'Wrong OTP: boxes clear + server error under them → M2.10-12',
      'After success: entered mobile = Aadhaar mobile → result (M2.10-06). Different mobile → OTP auto-sent to the entered mobile → M2.10-05. Find flow → result (M2.10-18) (AadhaarOtpFragment.kt:159-188)',
      'Find-flow variant "Verify ABHA OTP" → M2.10-11',
    ]);
  }
  // 05 Mobile verify (OTP only; mobile reused from screen 02)
  {
    const s = F('M2.10-05 · Mobile number OTP', 4, 0);
    await abhaTop(s, 'Verify mobile OTP', 'For ABHA communication', '21:30');
    const b = body(s); b.itemSpacing = 16;
    await steps(b, 3);
    banner(b, 'Info', 'Different mobile number', 'The mobile you entered is not the Aadhaar mobile, so we sent it an OTP too.', null);
    auto(b, 'Mobile number', '91234 56780 (from the Aadhaar screen)');
    const m = await txt('OTP sent to mobile number ending with ******6780', 'Body/Medium', 'color/on-surface'); add(b, m); wrapText(m);
    await otpBoxes(b, '');
    const t = await txt("Didn't receive OTP? Please wait 58 seconds", 'Body/Small', 'color/on-surface-variant'); add(b, t); t.textAutoResize = 'HEIGHT';
    bottomBar(s, null, 'Verify OTP');
    rules(s, [
      'OTP-only screen: the mobile is NOT editable here; it is the one typed on M2.10-02 (VerifyMobileOtpFragment.kt:40-52). Code follows; no "Check & generate OTP"',
      '60 s resend timer; "OTP was resent" toast; Exit only after lock-out / network failure; wrong OTP clears boxes + inline error (same as M2.10-12)',
      'Back / up here → "Exit – do you want to go back?" (M2.10-13); Yes returns to the Aadhaar screen',
      'GenerateMobileOtpFragment (type mobile + (i) tooltip, "already linked" skip to result) is only on the Fingerprint path, which is disabled → not drawn',
    ]);
  }
  // 06 ABHA created (new) · linked to record · download question
  {
    const s = F('M2.10-06 · ABHA created', 5, 0);
    await abhaTop(s, 'ABHA created', 'Sunita Devi', '19:12');
    const b = body(s); b.itemSpacing = 16;
    await steps(b, 5);
    await resultHead(b, 'check_circle', 'color/success', 'ABHA account successfully created!');
    await abhaCard(b, 'Sunita Devi', '91-4521-7788-3012');
    banner(b, 'Success', 'Linked to beneficiary', 'This ABHA is successfully linked to the record of Sunita Devi.', null);
    const q = S.card(b, 'Download question');
    q.appendChild(await txt('Do you want to download the ABHA card?', 'Title/Small', 'color/on-surface'));
    const qr = row(q, 'Yes / No', 8); btn(qr, 'Outlined', 'No'); btn(qr, 'Filled', 'Yes');
    snack(s, 'Badge', { Message: 'ABHA created', Detail: 'Digital Identity · 23 of 25 for next tier', Action: 'View' });
    rules(s, [
      'Linked text names the beneficiary; the record is pushed to the server at once (push worker) (CreateAbhaFragment.kt:151-156, 177-186)',
      'Download is a Yes / No question: No closes the flow; Yes → M2.10-19 (CreateAbhaFragment.kt:208-212)',
      'Back → "Exit" confirm (M2.10-13), cannot be dismissed by tapping outside (:92-113)',
      'Existing ABHA (isNew = false) → M2.10-16; already linked to another person → M2.10-17',
      '"Digital Identity" badge snackbar: Not in app today – needs dev (no gamification hook in create_abha_id/)',
    ]);
  }
  // 07 Error state (offline) — moved to row 1 to keep max 6 columns
  {
    const s = F('M2.10-07 · ABHA error / offline', 0, 1);
    await abhaTop(s, 'Create ABHA', 'Sunita Devi', '28:10');
    const b = body(s); b.itemSpacing = 16; b.counterAxisAlignItems = 'CENTER';
    const e = kinst('Empty state', null, { Title: 'Could not reach the ABHA server', Body: 'ABHA needs internet. Nothing was lost. Try again when the signal is better.', Icon: 'wifi_off' }); add(b, e, false);
    const eb = e.findOne((n) => n.type === 'INSTANCE' && n.name === 'Button'); if (eb) setP(eb, { Label: 'Try again' });
    banner(b, 'Warning', 'OTP limit', 'After 3 wrong OTPs, wait 30 minutes before trying again.', null);
    rules(s, [
      'Full-screen network error with "Try again" (AbhaIdActivity.kt:52-91; AadhaarIdFragment.kt:130-165 inline panel)',
      'Server error (not offline) shows the server\'s own message → M2.10-15',
      '"3 wrong OTPs → 30 min" banner: Not in app today – needs dev; code only shows the server message + Exit (M2.10-12)',
    ]);
  }

  // ===== NEW FRAMES (gap fix) =====
  // 08 Aadhaar entry · validation + server error
  {
    const s = F('M2.10-08 · Aadhaar number · errors', 1, 1);
    await abhaTop(s, 'Create ABHA', 'Sunita Devi', '26:02');
    const b = body(s); b.itemSpacing = 12;
    await toggle(b, 1);
    select(b, 'Verify with', 'Aadhaar No');
    b.appendChild(await txt('Enter details for Sunita Devi', 'Title/Small', 'color/on-surface'));
    S.tf(b, 'Error', { Label: 'Aadhaar number *', Value: '•••• •••• 4320', Helper: 'Invalid Aadhaar no.', 'Show helper': true }, 'visibility');
    S.tf(b, 'Error', { Label: 'Mobile number *', Value: '98765 432', Helper: 'Invalid mobile no.', 'Show helper': true }, 'check_circle');
    await consentRow(b, true);
    btn(b, 'Filled', 'Generate OTP', true, true);
    banner(b, 'Error', 'UIDAI error', 'Aadhaar number is not linked with any mobile. Please update at an Aadhaar centre.', null);
    const ld = row(b, 'Loading', 10); spinner(ld, 24); ld.appendChild(await txt('Sending Aadhaar OTP…', 'Body/Medium', 'color/on-surface-variant'));
    rules(s, [
      'Inline errors as you type: "Invalid Aadhaar no." / "Invalid mobile no." / "Invalid name" (AadhaarNumberAshaFragment.kt:136-238)',
      'Server error text (e.g. UIDAI message) shows under the Aadhaar field (:241-247)',
      'While the OTP request runs: inline spinner; network failure → error panel (AadhaarIdFragment.kt:130-165)',
    ]);
  }
  // 09 Find ABHA by mobile (Search tab)
  {
    const s = F('M2.10-09 · Find ABHA by mobile', 2, 1);
    await abhaTop(s, 'ABHA', 'Ayushman Bharat Health Account', '29:20');
    const b = body(s); b.itemSpacing = 12;
    await toggle(b, 0);
    b.appendChild(await txt('Already have an ABHA? Find Out Now!', 'Title/Medium', 'color/on-surface'));
    const r = row(b, 'Mobile + search', 8);
    const mf = tint(S.tf(r, 'Filled', { Label: 'Mobile number *', Value: '98765 43210' }, 'check_circle'), 'color/success'); mf.layoutSizingHorizontal = 'FILL';
    btn(r, 'Tonal', 'Search');
    select(b, 'Select ABHA *', 'Sunita Devi : 91-4521-7788-3012');
    await caption(b, '2 ABHAs found for this mobile');
    await consentRow(b, true);
    bottomBar(s, null, 'Generate OTP');
    rules(s, [
      'Search enabled only for a valid 10-digit mobile (FindAbhaFragment.kt:78-88, 156-184)',
      'ABHA dropdown lists every ABHA linked to the mobile as "Name : ABHA number"; disabled until results arrive (:124-139)',
      'Consent checkbox → Consent screen (M2.10-03), same as Create',
      'Generate OTP enabled only when an ABHA is picked + mobile valid + consent ticked; OTP goes to the ABHA-registered mobile → M2.10-11',
      'Errors → M2.10-10',
    ]);
  }
  // 10 Find ABHA · errors
  {
    const s = F('M2.10-10 · Find ABHA · no result / errors', 3, 1);
    await abhaTop(s, 'ABHA', 'Ayushman Bharat Health Account', '28:55');
    const b = body(s); b.itemSpacing = 12;
    await toggle(b, 0);
    b.appendChild(await txt('Already have an ABHA? Find Out Now!', 'Title/Medium', 'color/on-surface'));
    S.tf(b, 'Error', { Label: 'Mobile number *', Value: '91234 56780', Helper: 'No ABHA Found', 'Show helper': true }, 'check_circle');
    const dd = select(b, 'Select ABHA *', 'No results'); dd.opacity = 0.5;
    await consentRow(b, false, false);
    btn(b, 'Filled', 'Generate OTP', true, true);
    banner(b, 'Offline', 'Internet connectivity issue, try again later', 'Search needs internet.', null);
    banner(b, 'Error', 'Some error occurred', 'Shown for any other server failure.', null);
    rules(s, ['Three messages: "No ABHA Found", "Internet connectivity issue, try again later", "Some error occurred" (FindAbhaViewModel.kt:88-108)', 'Dropdown and Generate OTP stay disabled']);
  }
  // 11 Verify ABHA OTP (find flow)
  {
    const s = F('M2.10-11 · Verify ABHA OTP (find flow)', 4, 1);
    await abhaTop(s, 'Verify ABHA OTP', 'Sunita Devi', '27:40');
    const b = body(s); b.itemSpacing = 16;
    const m = await txt('OTP sent to ABHA registered mobile number ending with ******3210', 'Body/Medium', 'color/on-surface'); add(b, m); wrapText(m);
    b.appendChild(await txt('Enter the 6-digit OTP', 'Title/Medium', 'color/on-surface'));
    await otpBoxes(b, '73');
    const t = await txt("Didn't receive OTP? Please wait 51 seconds", 'Body/Small', 'color/on-surface-variant'); add(b, t); t.textAutoResize = 'HEIGHT';
    bottomBar(s, null, 'Verify OTP');
    rules(s, ['Same screen as M2.10-04 with abhaMode = SEARCH: title "Verify ABHA OTP", text "ABHA registered mobile" (AadhaarOtpFragment.kt:93-95, 138-148)', 'Success → find result M2.10-18 (no mobile step, no linking)']);
  }
  // 12 OTP wrong / lock-out (Exit appears)
  {
    const s = F('M2.10-12 · OTP wrong / lock-out', 5, 1);
    await abhaTop(s, 'Verify Aadhaar OTP', 'Sunita Devi', '18:30');
    const b = body(s); b.itemSpacing = 12;
    const m = await txt('OTP sent to Aadhaar registered mobile number ending with ******3210', 'Body/Medium', 'color/on-surface'); add(b, m); wrapText(m);
    await otpBoxes(b, '', true);
    const e = await txt('Invalid OTP value. Please enter a correct OTP.', 'Body/Small', 'color/error'); add(b, e); wrapText(e);
    const x = inst('button', 'Style=Text, State=Enabled', { Label: 'Resend OTP', 'Show icon': false }); add(b, x);
    banner(b, 'Error', 'You have exceeded the maximum OTP attempts', 'Please exit your browser and try again after some time.', null);
    bottomBar(s, 'Exit', 'Verify OTP');
    snack(s, 'Success', { Message: 'OTP was resent', Action: 'OK' });
    rules(s, [
      'Wrong OTP: the boxes clear and the server error shows under them (AadhaarOtpFragment.kt:126-134; VerifyMobileOtpFragment.kt:125-142)',
      'Exit button appears ONLY after a network error or a server message containing "exit your browser" (ABDM lock-out) (AadhaarOtpViewModel.kt:154-163, 206-214)',
      'Exit closes the ABHA flow. Same behaviour on the mobile OTP screen (M2.10-05)',
    ]);
  }
  // 13 Exit confirmation dialog
  {
    const s = F('M2.10-13 · Exit confirmation', 0, 2, true);
    await abhaTop(s, 'Verify mobile OTP', 'For ABHA communication', '20:10');
    const b = body(s); await otpBoxes(b, '');
    await dialog(s, { ic: 'warning', title: 'Exit', body: 'Do you want to go back?', cancel: 'No', ok: 'Yes' });
    rules(s, [
      'Shown on up/back from the mobile-OTP step and from the result screen (AbhaIdActivity.kt:118-169; CreateAbhaFragment.kt:92-113)',
      'Mobile step: Yes → back to the Aadhaar screen. Result screen: Yes → closes the ABHA flow',
      'Cannot be dismissed by tapping outside',
    ]);
  }
  // 14 Starting ABHA (token loading)
  {
    const s = F('M2.10-14 · Starting ABHA (loading)', 1, 2);
    screenTop(s, 'ABHA', 'Ayushman Bharat Health Account', 'none');
    const b = body(s);
    const c = centerCol(b, 'Loading'); spinner(c);
    c.appendChild(await txt('Connecting to ABHA…', 'Title/Medium', 'color/on-surface'));
    const d = await txt('Getting secure access. This needs internet.', 'Body/Medium', 'color/on-surface-variant'); c.appendChild(d); d.textAlignHorizontal = 'CENTER';
    rules(s, ['Full-screen spinner on every launch while the app gets the server token and the ABHA token (AbhaIdViewModel.kt:55-91)', 'Success → M2.10-01; offline → M2.10-07; server error → M2.10-15']);
  }
  // 15 Server error (server message + Try again)
  {
    const s = F('M2.10-15 · ABHA server error', 2, 2);
    screenTop(s, 'ABHA', 'Ayushman Bharat Health Account', 'none');
    const b = body(s); b.counterAxisAlignItems = 'CENTER';
    const e = kinst('Empty state', null, { Title: 'Something went wrong', Body: 'ABDM gateway: Unable to generate session token. Please try again later.', Icon: 'warning' }); add(b, e, false);
    const eb = e.findOne((n) => n.type === 'INSTANCE' && n.name === 'Button'); if (eb) setP(eb, { Label: 'Try again' });
    rules(s, ['Shows the server\'s own error message (not a generic text) + "Try again" (AbhaIdActivity.kt:52-91)']);
  }
  // 16 ABHA already exists (isNew = false)
  {
    const s = F('M2.10-16 · ABHA already exists', 3, 2);
    await abhaTop(s, 'ABHA', 'Sunita Devi', '17:45');
    const b = body(s); b.itemSpacing = 16;
    await resultHead(b, 'warning', 'color/warning', 'This ABHA already exists!');
    await abhaCard(b, 'Sunita Devi', '91-4521-7788-3012');
    banner(b, 'Warning', 'Not linked to beneficiary', 'This ABHA is already linked to someone. Try again using the correct Aadhaar No.', null);
    const q = S.card(b, 'Download question');
    q.appendChild(await txt('Do you want to download the ABHA card?', 'Title/Small', 'color/on-surface'));
    const qr = row(q, 'Yes / No', 8); btn(qr, 'Outlined', 'No'); btn(qr, 'Filled', 'Yes');
    rules(s, ['Server isNew = false: warning icon, "This ABHA already exists!", record is NOT linked (CreateAbhaFragment.kt:164-170)', 'Download question still shows']);
  }
  // 17 Already linked to someone · blocking dialog
  {
    const s = F('M2.10-17 · Already linked to someone (blocking)', 4, 2, true);
    await abhaTop(s, 'ABHA', 'Sunita Devi', '17:20');
    const b = body(s); await abhaCard(b, 'Sunita Devi', '91-4521-7788-3012');
    await dialog(s, { ic: 'warning', icColor: 'color/error', title: 'ABHA already linked', body: 'This ABHA No is already linked to someone, please try again using correct Aadhaar No of Sunita Devi', cancel: null, ok: 'OK' });
    rules(s, ['Mapping API returns code 0 → full-screen blocking dialog naming the beneficiary (First Last) (CreateAbhaViewModel.kt:259-261; CreateAbhaFragment.kt:242-246, 386-407)', 'Only OK; OK closes the whole ABHA flow. Duplicate-ABHA safeguard']);
  }
  // 18 Find result
  {
    const s = F('M2.10-18 · Find result · Here is your ABHA', 5, 2);
    await abhaTop(s, 'ABHA', 'Sunita Devi', '25:10');
    const b = body(s); b.itemSpacing = 16;
    await resultHead(b, 'check_circle', 'color/success', 'Here is your ABHA Number!');
    await abhaCard(b, 'Sunita Devi', '91-4521-7788-3012');
    bottomBar(s, null, 'Done');
    rules(s, ['Find flow (abhaMode SEARCH): name + ABHA number only; no download option and no linking to the record (CreateAbhaFragment.kt:146-150)']);
  }
  // 19 Downloaded card · notification + persistent snackbar
  {
    const s = F('M2.10-19 · ABHA card downloaded', 0, 3, true);
    await abhaTop(s, 'ABHA created', 'Sunita Devi', '18:50');
    const b = body(s); b.itemSpacing = 12;
    // system notification
    const n = AL('HORIZONTAL', 'System notification'); n.itemSpacing = 10; pad(n, 'spacing/md', 'spacing/sm'); rad(n, 'radius/lg'); fill(n, 'color/surface-container-high'); stroke(n, 'color/outline-variant'); add(b, n);
    n.appendChild(icon('download', 'color/primary', 22));
    const nc = AL('VERTICAL', 'Text'); nc.itemSpacing = 2; n.appendChild(nc); nc.layoutSizingHorizontal = 'FILL';
    nc.appendChild(await txt('ABHA card downloaded', 'Title/Small', 'color/on-surface'));
    const nt = await txt('Sunita_Devi_ABHA.png · Tap to open', 'Body/Small', 'color/on-surface-variant'); nc.appendChild(nt); wrapText(nt);
    await caption(b, 'Android notification (system tray)');
    await resultHead(b, 'check_circle', 'color/success', 'ABHA account successfully created!');
    await abhaCard(b, 'Sunita Devi', '91-4521-7788-3012');
    snack(s, 'Success', { Message: 'You can view ABHA card in download folder!!', Action: 'OK' });
    rules(s, ['Yes → saves the card as PNG in the phone\'s Downloads folder + system notification that opens the image (CreateAbhaFragment.kt:279-326)', 'Persistent snackbar "You can view ABHA card in download folder!!"; its OK closes the flow (:256-264)']);
  }
  // 20 Opened from drawer (no beneficiary)
  {
    const s = F('M2.10-20 · Create ABHA from menu (no beneficiary)', 1, 3);
    await abhaTop(s, 'ABHA', 'Ayushman Bharat Health Account', '29:40');
    const b = body(s); b.itemSpacing = 12;
    await toggle(b, 1);
    select(b, 'Verify with', 'Aadhaar No');
    tint(S.tf(b, 'Filled', { Label: 'Aadhaar number *', Value: '•••• •••• 7812' }, 'visibility'), 'color/on-surface-variant');
    tint(S.tf(b, 'Filled', { Label: 'Mobile number *', Value: '99887 66554' }, 'check_circle'), 'color/success');
    tint(S.tf(b, 'Filled', { Label: 'Beneficiary name *', Value: 'Pooja Kumari' }, 'check_circle'), 'color/success');
    await consentRow(b, true);
    banner(b, 'Info', 'Not linked to a record', 'Opened from the menu. The ABHA is saved on its own and is not linked to any beneficiary.', null);
    bottomBar(s, null, 'Generate OTP');
    rules(s, ['Opened from drawer "Create ABHA number" (no benId / benRegId): name is typed and validated ("Invalid name") (AadhaarNumberAshaFragment.kt:76-99)', 'Result screen has no "linked to <name>" line; ABHA saved as a stand-alone record (CreateAbhaViewModel.kt:116-130)']);
  }

  const maxB = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, maxB, 860,
    'ABHA NOTES\n' +
    '• App flow (code): Search/Create toggle on one screen → Consent (checkbox screen) → Aadhaar OTP → Mobile OTP (only if the typed mobile differs) → Result.\n' +
    '• Session: 30-min countdown + close icon on every ABHA screen; the flow closes at 0:00.\n' +
    '• Aadhaar is masked, paste blocked, never stored; OTP = 6 boxes, resend after 60 s.\n' +
    '• GOV (demographic) mode exists in code but its radio is hidden → not designed. Fingerprint method is commented out → not designed.\n' +
    '• ABHA needs internet: loading (14), offline (07), server error (15).\n' +
    '• Gamification badge on ABHA created is design-only (needs dev).');
  fitSection(sec);
  return ['2.10 screens: ' + frames.length];
}
