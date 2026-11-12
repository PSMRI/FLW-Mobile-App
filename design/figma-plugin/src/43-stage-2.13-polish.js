// ---------------------------------------------------------------------------
// Stage 2.13 · Remaining screens + Hindi / Assamese + click-through prototype
// Sources: HouseholdFormDataset, fragment_new_ben_reg_type, fragment_non_household_living, ServiceLocationActivity,
// login (forgot password), values-hi / values-as strings
// ---------------------------------------------------------------------------
async function buildStage213(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx);
  const S = SK(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, setP, icon, box, inst, kinst, phone, body, scrim, sheet, pinBottom, note,
    stageSection, fitSection, label, date, select, text, auto, seg, radios, checks, steppers, section, reveal, banner, infoCard, recordCard, bottomBar,
    screenTop, chips, stat, tileGrid, person, snack } = S;
  const G = GK(ctx);
  // script fonts
  const SCRIPT = { deva: 'Noto Sans Devanagari', beng: 'Noto Sans Bengali' };
  const loaded = {};
  for (const fam of Object.values(SCRIPT)) for (const st of ['Regular', 'Medium', 'SemiBold', 'Bold']) { try { await figma.loadFontAsync({ family: fam, style: st }); loaded[fam + st] = true; } catch (e) { /* skip */ } }
  const toScript = (frame, key) => {
    const fam = SCRIPT[key]; let n = 0;
    for (const t of frame.findAll((x) => x.type === 'TEXT')) {
      if (t.fontName === figma.mixed) continue;
      const st = loaded[fam + t.fontName.style] ? t.fontName.style : (loaded[fam + 'Medium'] ? 'Medium' : 'Regular');
      try { t.fontName = { family: fam, style: st }; n++; } catch (e) { /* locked */ }
    }
    return n;
  };
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.13 · Remaining screens, languages & prototype');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };

  // 01 New household
  {
    const s = F('M2.13-01 · New household registration', 0, 0);
    screenTop(s, 'New household', 'Sirsi · ward 4', 'none');
    const b = body(s); b.itemSpacing = 12;
    section(b, 'Open', 'Family details', '9 fields', 1);
    const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(b, r);
    const a = text(r, 'HOF first name *', 'Mohan'); a.layoutSizingHorizontal = 'FILL'; const c = text(r, 'Surname', 'Yadav'); c.layoutSizingHorizontal = 'FILL';
    text(b, 'HOF mobile number *', '91234 56780');
    const r2 = AL('HORIZONTAL'); gap(r2, 'spacing/sm'); add(b, r2);
    const h1 = text(r2, 'House no.', '118'); h1.layoutSizingHorizontal = 'FILL'; const h2 = select(r2, 'Ward', 'Ward 4'); h2.layoutSizingHorizontal = 'FILL';
    text(b, 'Mohalla', 'Yadav tola');
    await seg(b, 'Economic status *', ['APL', 'BPL', "Don't know"], 1);
    await seg(b, 'Has ABHA / Ayushman card?', ['Yes', 'No'], 1);
    section(b, 'Closed', 'House details', 'Area, type, ownership', 2);
    section(b, 'Closed', 'Amenities', 'Kitchen, fuel, water, electricity, toilet', 3);
    bottomBar(s, 'Save draft', 'Save & add members');
    G.rules(s, [
      'Consent dialog first, cannot be dismissed (M2.13-06) (NewHouseholdFragment.kt:96-130)',
      'After saving a NEW household → "Add head of family?" dialog (M2.13-07); Yes opens HOF registration (relation Self) – code skips the Child/Adult choice (:132-148)',
      'Edit mode: save just goes back (:347-357)',
      'Mitanin only: "Do you have ABHA ID / Ayushman ID?" + lookup, before Village, new household only (HouseholdFormDataset.kt:221) → M2.13-13; other flavours do not show this question',
      'Existing household opens in view mode with Edit FAB (M2.13-09) (:254-256, :297-300)',
      'Link mode (from a non-household person, "Create new household"): save makes that person HOF, toast "Beneficiary linked", back to Non-household list (:337-346)',
      'Saving spinner; toasts "Save successful" / save failed (M2.13-10) (:325-367)',
      'Draft: New household first asks Open draft / Create new (M2.19-25)',
      'Voice input (mic) on text fields (:57-63, :260-263)',
    ]);
  }
  // 02 Beneficiary type
  {
    const s = F('M2.13-02 · Add member · choose type', 1, 0);
    screenTop(s, 'Add member', "Mohan Yadav's family", 'none');
    const b = body(s); b.itemSpacing = 16;
    b.appendChild(await txt('Who are you adding?', 'Title/Large', 'color/on-surface'));
    const opt = async (t, d, ic, tone, on) => {
      const c = AL('HORIZONTAL', t); c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/md', 'spacing/lg'); gap(c, 'spacing/md'); rad(c, 'radius/lg'); fill(c, on ? 'color/primary-subtle' : 'color/surface-container'); stroke(c, on ? 'color/primary' : 'color/outline-variant', on ? 2 : 1); add(b, c);
      const tl = box('Icon', 56, 56); rad(tl, 'radius/lg'); fill(tl, `module/${tone}/bg`); tl.appendChild(icon(ic, `module/${tone}/fg`, 30)); c.appendChild(tl);
      const col = AL('VERTICAL'); col.itemSpacing = 2; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      col.appendChild(await txt(t, 'Title/Medium', 'color/on-surface')); const dd = await txt(d, 'Body/Small', 'color/on-surface-variant'); col.appendChild(dd); wrapText(dd);
    };
    await opt('Child or adolescent', 'Newborn, infant, child or adolescent (0 to 14 years)', 'child', 'child', false);
    await opt('Adult', 'General registration (15 years and above)', 'person', 'people', true);
    banner(b, 'Warning', 'Beneficiary IDs running low', 'Connect to the internet at the earliest to get more IDs.', null);
    bottomBar(s, null, 'Continue');
    G.rules(s, [
      'Dormant in code: nothing navigates here and "Child" does nothing (NewBenRegTypeFragment.kt:142-145). Live flow = gender + relation dialog (M2.19-26)',
      'Consent dialog on entry, tick required, Cancel exits (:38-66)',
      'Draft per type: Open draft / Create new (:68-87, :158-180)',
      'No selection → toast "Please select type of beneficiary" (:182-186)',
      'ID stock: few left → warning above + ID fetch; none left → Continue hidden (M2.13-11) (:115-139). Applies to every registration entry',
    ]);
  }
  // 03 Not living in household
  {
    const s = F('M2.13-03 · Member living elsewhere', 2, 0);
    screenTop(s, 'Current living place', 'New beneficiary · no household', 'none');
    const b = body(s); b.itemSpacing = 12;
    banner(b, 'Info', 'No household', 'For people who live without a household: homeless, or in an institution.', null);
    select(b, 'Place of Current Living *', 'Govt Hostel');
    text(b, 'Name of Institution *', 'Kasturba Gandhi Balika Vidyalaya');
    bottomBar(s, null, 'Continue');
    G.rules(s, [
      'Meaning: people with NO household (homeless / institution), not "family member away" (NonHouseholdLivingFragment.kt:85-103)',
      'Entry: Non-household list → Add beneficiary (M2.19-35)',
      '18 places (strings_home.xml non_household_living_places) – full list M2.13-12',
      '"Name of Institution *" only for 7 institution places: school/college, rehab centre, orphanage, old-age home, private / govt / NGO hostel',
      '"Other" → "Other place *" text appears',
      'Continue creates the shared "household 0" record and opens registration as Self',
    ]);
  }
  // 04 Service location
  {
    const s = F('M2.13-04 · Choose work location (after login)', 3, 0);
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Home', { Title: 'Where do you work today?', Subtitle: 'You can change this later in Profile' })); bar.findAll((n) => n.name === 'Action').forEach((x) => { x.visible = false; });
    const b = body(s); b.itemSpacing = 12;
    auto(b, 'State', 'Assam'); auto(b, 'District', 'Kamrup');
    select(b, 'Block *', 'Rajnagar'); select(b, 'Village *', 'Sirsi');
    banner(b, 'Info', 'Only your assigned villages are shown', 'Ask your supervisor if a village is missing.', null);
    bottomBar(s, null, 'Continue to app');
    G.rules(s, [
      'ASHA only, and only when no work location is saved; else straight to Home (SignInFragment.kt:253-280)',
      'Supervisor / ANM / CHO: skipped – first village auto-selected, Supervisor app opens',
      'Later: tap the village name on the Home toolbar, or Profile → Change work location (HomeActivity.kt:131-135)',
    ]);
  }
  // 05 Forgot password
  {
    const s = F('M2.13-05 · Forgot password', 4, 0);
    screenTop(s, 'Reset password', 'asha_sunita', 'none');
    const b = body(s); b.itemSpacing = 12;
    const st = AL('HORIZONTAL', 'Steps'); st.itemSpacing = 6; add(b, st);
    for (const [i, l] of ['Username', 'OTP', 'New password'].entries()) { const r = figma.createRectangle(); r.resize(100, 4); r.cornerRadius = 2; r.fills = [paint(i < 2 ? 'color/success' : 'color/primary')]; st.appendChild(r); r.layoutSizingHorizontal = 'FILL'; }
    b.appendChild(await txt('Set a new password', 'Title/Large', 'color/on-surface'));
    S.tf(b, 'Focused', { Label: 'New password *', Value: '••••••••', Helper: 'At least 8 characters with a number.', 'Show helper': true, 'Show trailing': true }, 'visibility');
    S.tf(b, 'Filled', { Label: 'Confirm password *', Value: '••••••••' }, 'visibility');
    bottomBar(s, null, 'Save & log in');
    G.rules(s, ['Not in app today – needs dev: "Forgot password" button is visibility=gone (fragment_sign_in.xml:246-254)']);
  }
  // ---- Row 2: languages
  const STR = {
    hi: { sub: ['दूसरी और तीसरी तिमाही', '3 बकाया', 'पीएचसी भेजें'], today: 'नमस्ते, सुनीता', todo: 'इस सप्ताह के काम', anc: 'एएनसी के लिए देय महिलाएं', imm: 'टीकाकरण के लिए देय बच्चे', hrp: 'एचआरपी मामले', modules: 'मॉड्यूल', tiles: ['सभी घरेलू सूची', 'सभी लाभार्थियों की सूची', 'लक्ष्य दम्पति सूची', 'माता की देखभाल', 'बच्चे की देखभाल', 'टीकाकरण नियत सूची', 'रोग नियंत्रण', ' मृत्यु रिपोर्ट'], hh: 'परिवार', addBen: 'लाभार्थी जोड़ें', hhId: 'परिवार आईडी' },
    as: { sub: ['দ্বিতীয় আৰু তৃতীয় ত্ৰৈমাসিক', '3 বাকী', 'PHC লৈ পঠিয়াওক'], today: 'নমস্কাৰ, সুনীতা', todo: 'এই সপ্তাহৰ কাম', anc: 'ANC ৰ বাবে নিৰ্ধাৰিত মহিলা', imm: 'টিকাকৰণৰ বাবে নিৰ্ধাৰিত শিশু', hrp: 'এইচ.আৰ.পি. কেচসমূহ', modules: 'মডিউল', tiles: ['সমস্ত পাৰিবাৰিক তালিকা', 'সমস্ত হিতাধিকাৰীৰ তালিকা', 'উপযুক্ত দম্পতিৰ তালিকা', 'মাকৰ যত্ন', 'শিশুৰ যত্ন', 'টিকাকৰণৰ প্ৰাপ্য তালিকা', 'ৰোগ নিয়ন্ত্ৰণ', 'মৃত্যুৰ প্ৰতিবেদন'], login: 'আগবাঢ়ি যাবলৈ লগ ইন কৰক', user: 'ব্যৱহাৰকাৰীৰ নাম', pass: 'পাছৱৰ্ড', loginBtn: 'লগইন কৰক' },
  };
  const tones = ['household', 'people', 'couple', 'maternal', 'child', 'immunization', 'disease', 'records'];
  const icons = ['home', 'group', 'favorite', 'pregnant', 'child', 'vaccine', 'hospital', 'form'];
  const todayLang = async (col, lang) => {
    const L = STR[lang];
    const s = F(`M2.13-L${col + 1} · Today (${lang === 'hi' ? 'Hindi' : 'Assamese'})`, col, 1);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Home', { Title: L.today, Subtitle: 'ASHA · Sirsi, Ward 4' }));
    const b = body(s); add(b, inst('sectionHeader', null, { Title: L.todo, 'Show action': false }));
    for (const [i, [tone, t, c, ic]] of [['Maternal', L.anc, '12', 'pregnant'], ['Immunization', L.imm, '8', 'vaccine'], ['Alert', L.hrp, '3', 'warning']].entries()) add(b, inst('dueCard', `Tone=${tone}`, { Title: t, Subtitle: L.sub[i], Count: c, Icon: ic }));
    add(s, inst('bottomNav', 'Active=Today'));
    toScript(s, lang === 'hi' ? 'deva' : 'beng');
  };
  const modulesLang = async (col, lang) => {
    const L = STR[lang];
    const s = F(`M2.13-L${col + 1} · Modules (${lang === 'hi' ? 'Hindi' : 'Assamese'})`, col, 1);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Home', { Title: L.modules, Subtitle: '12' }));
    const b = body(s); tileGrid(b, L.tiles.map((t, i) => [tones[i], t.trim(), '', icons[i]]));
    for (const m of b.findAll((n) => n.type === 'INSTANCE' && n.name === 'Module tile')) setP(m, { 'Show count': false });
    add(s, inst('bottomNav', 'Active=Modules'));
    toScript(s, lang === 'hi' ? 'deva' : 'beng');
  };
  await todayLang(0, 'hi');
  await modulesLang(1, 'hi');
  {
    const s = F('M2.13-L3 · Households (Hindi)', 2, 1);
    screenTop(s, 'सभी घरेलू सूची', 'वार्ड 4 · 248 परिवार');
    const b = body(s); b.itemSpacing = 12;
    for (const [n, id] of [['रमेश कुमार', '7012 3345 0012'], ['मोहन लाल यादव', '7012 3345 0013']]) { const h = inst('hhCard', null, { 'HOF name': n, 'HH ID': 'परिवार आईडी ' + id, Members: '5 सदस्य', Phone: '98765 43210' }); add(b, h); const bt = h.findAll((x) => x.type === 'INSTANCE' && x.name === 'Button'); if (bt[0]) setP(bt[0], { Label: 'सदस्य' }); if (bt[1]) setP(bt[1], { Label: 'लाभार्थी जोड़ें' }); }
    toScript(s, 'deva');
  }
  await todayLang(3, 'as');
  await modulesLang(4, 'as');
  {
    const s = F('M2.13-L6 · Login (Assamese)', 5, 1);
    add(s, inst('statusBar'));
    const b = body(s); b.paddingTop = 40; b.itemSpacing = 16;
    b.appendChild(await txt('উৎপ্ৰেৰণা', 'Headline/Large', 'color/primary'));
    b.appendChild(await txt(STR.as.login, 'Title/Medium', 'color/on-surface'));
    text(b, STR.as.user, 'asha_sunita'); S.tf(b, 'Focused', { Label: STR.as.pass, Value: '••••••••' }, 'visibility');
    const lb = inst('button', 'Style=Filled, State=Enabled', { Label: STR.as.loginBtn, 'Show icon': false }); add(b, lb);
    toScript(s, 'beng');
    G.rules(s, ['Full login rules (remember me, error text, offline, different user, language, delete account, DEMO) → M2.19-01…04', 'Assamese is not offered on Mitanin builds']);
  }

  // ---- Row 3: gap screens (household registration flow)
  {
    const s = F('M2.13-06 · Household consent', 0, 2, true);
    screenTop(s, 'New household', 'Sirsi · ward 4', 'none');
    await G.dialog(s, { ic: 'shield', icColor: 'color/primary', title: 'Consent', body: 'I have explained to the family why their household details are collected and they agree to share them.', cancel: 'Cancel', ok: 'Continue',
      extra: async (c) => { add(c, kinst('Checkbox option', 'Checked=false', { Label: 'Family has given consent' })); } });
    G.rules(s, ['Cannot be dismissed; OK without tick → toast "Please tick the checkbox"; Cancel exits (NewHouseholdFragment.kt:96-130)', 'Mitanin: own consent text (mitanin_consent_text, :108-110)']);
  }
  {
    const s = F('M2.13-07 · Add head of family?', 1, 2, true);
    screenTop(s, 'New household', 'Sirsi · ward 4', 'none');
    await G.dialog(s, { ic: 'person_add', icColor: 'color/primary', title: 'Add head of family?', body: 'Mohan Yadav', cancel: 'No', ok: 'Yes' });
    G.rules(s, ['After saving a new household; cannot be dismissed (NewHouseholdFragment.kt:132-148)', 'Yes → HOF registration form (relation Self); No → back to Households']);
  }
  {
    const s = F('M2.13-08 · ABHA already exists', 2, 2, true);
    screenTop(s, 'New household', 'Sirsi · ward 4', 'none');
    await G.dialog(s, { ic: 'id_card', icColor: 'color/primary', title: 'ABHA already exists', body: 'This ABHA / Ayushman number is already registered. Do you want to create a new ABHA instead?', cancel: 'No', ok: 'Create ABHA' });
    G.rules(s, ['Server error code "ABHA already exists" after lookup (NewHouseholdFragment.kt:150-163, :272-285)', 'Network error → toast', 'Mitanin flavour only (lookup field shown only there)']);
  }
  {
    const s = F('M2.13-09 · Household view mode / link mode', 3, 2);
    screenTop(s, 'Household details', 'HH 7012 3345 0013 · Mohan Yadav', 'none');
    const b = body(s); b.itemSpacing = 12;
    await G.viewMode(b, { saved: 'Registered 14 Feb 2026', editable: true });
    await infoCard(b, 'Family details', [['HOF name', 'Mohan Yadav'], ['HOF mobile', '91234 56780'], ['House no.', '118'], ['Ward', 'Ward 4'], ['Economic status', 'BPL']]);
    banner(b, 'Info', 'Link mode', 'Opened from Rakesh Paswan (no household): saving makes him head of this new household.', null);
    G.rules(s, ['Card body tap on Households (ASHA only) opens this view; Edit unlocks fields (NewHouseholdFragment.kt:254-256, :297-300)', 'Link mode banner shows only when opened via "Create new household" from a non-household person (:337-346)']);
  }
  {
    const s = F('M2.13-10 · Household save · saving / failed', 4, 2, true);
    screenTop(s, 'New household', 'Sirsi · ward 4', 'none');
    const b = body(s); b.itemSpacing = 12;
    text(b, 'HOF first name *', 'Mohan'); text(b, 'HOF mobile number *', '91234 56780');
    bottomBar(s, 'Save draft', 'Saving…');
    snack(s, 'Error', { Message: 'Could not save. Try again.', Action: 'Retry' });
    G.rules(s, ['Spinner while saving; success toast "Save successful" (M2.0-S6) (NewHouseholdFragment.kt:325-367)', 'Failure toast; form stays filled']);
  }
  {
    const s = F('M2.13-11 · Add member · no IDs left', 5, 2);
    screenTop(s, 'Add member', "Mohan Yadav's family", 'none');
    const b = body(s); b.itemSpacing = 16;
    banner(b, 'Error', 'No more beneficiary IDs', 'Connect to the internet to get more IDs. Registration is paused.', null);
    await G.blockedAdd(b, 'Continue', 'Hidden in code until new IDs are downloaded');
    G.rules(s, ['0 IDs left → Continue hidden + ID fetch worker triggered (NewBenRegTypeFragment.kt:115-139)']);
  }
  {
    const s = F('M2.13-12 · Living place · all options + Other', 0, 3);
    screenTop(s, 'Current living place', 'New beneficiary · no household', 'none');
    const b = body(s); b.itemSpacing = 8;
    await radios(b, 'Place of Current Living *', ['Footpath', 'Railway Platform', 'Bus Station', 'Prison', 'Under a Tree', 'In a Temple', 'In a Mosque', 'In a Church', 'Any other Praying place', 'Educational Institution (School/College)', 'Ekalavya School', 'Rehabilitation center', 'Orphanage Home', 'Old Age Home', 'Private Hostel', 'Govt Hostel', "NGO's Hostel", 'Other'], 17);
    const rv = reveal(b); text(rv, 'Other place *', 'Construction site camp');
    bottomBar(s, null, 'Continue');
    G.rules(s, ['Dropdown in code (NonHouseholdLivingFragment.kt:45-66); empty → "required" error on Continue', 'Other → "Other place *" required; institution places → "Name of Institution *" required']);
  }
  {
    const s = F('M2.13-13 · New household · Mitanin (ABHA lookup)', 1, 3);
    screenTop(s, 'New household', 'Raipur · ward 7', 'none');
    const b = body(s); b.itemSpacing = 12;
    await seg(b, 'Do you have ABHA ID / Ayushman ID?', ['Yes', 'No'], 0);
    const rv = reveal(b);
    const ar = AL('HORIZONTAL', 'Lookup'); gap(ar, 'spacing/sm'); ar.counterAxisAlignItems = 'CENTER'; add(rv, ar);
    const ai = S.tf(ar, 'Filled', { Label: 'ABHA / Ayushman number', Value: '91-4455-2201-7788', Helper: '14-digit ABHA or 9–14 letters/digits Ayushman', 'Show helper': true }); ai.layoutSizingHorizontal = 'FILL';
    G.btn(ar, 'Tonal', 'Submit');
    banner(rv, 'Success', 'Details found', 'HOF name, mobile and gender filled from ABHA.', null);
    select(b, 'Village *', 'Raipur');
    text(b, 'HOF first name *', 'Ramesh'); text(b, 'Surname', 'Sahu');
    text(b, 'HOF mobile number *', '98260 11223');
    bottomBar(s, 'Save draft', 'Save & add members');
    G.rules(s, [
      'Mitanin only, new household only: question inserted before Village (HouseholdFormDataset.kt:145-188, :221)',
      'Submit fetches details from the server and prefills the form (NewHouseholdFragment.kt:213-251)',
      'Format: 14-digit ABHA or 9-14 alphanumeric Ayushman',
      '"ABHA already exists" → M2.13-08; network error → toast',
      'Mitanin consent text on entry (M2.13-06)',
    ]);
  }

  // ---- Prototype links (main ASHA day)
  const proto = await linkPrototype(ctx);
  const maxB = await G.finish(sec, frames, (f) => rowMap.get(f));
  await note(sec, 120, maxB, 1000,
    'LANGUAGE & PROTOTYPE NOTES\n' +
    '• Hindi / Assamese screens use the app\'s own strings (values-hi / values-as) with Noto Sans Devanagari / Bengali. Check: long Hindi titles wrap to 2 lines in tiles and due cards; Assamese brand name "উৎপ্ৰেৰণা" (Saksham build).\n' +
    '• Translation issue found: values-as anc_visit = "ANC দৰকাৰি" and household_id = "পৰাৰ পৰিচয়পত্ৰ" look wrong; ask the Assamese reviewer.\n' +
    '• Click-through prototype (Present ▶ from Screens page, flow "ASHA day: ANC visit"): ' + proto + '.');
  fitSection(sec);
  return ['2.13 screens: ' + frames.length, 'prototype: ' + proto];
}

async function linkPrototype(ctx) {
  const sp = ctx.pages.screens;
  const all = [];
  for (const n of sp.children) { if (n.type === 'SECTION') for (const c of n.children) all.push(c); else all.push(n); }
  const frame = (prefix) => all.find((n) => n.type === 'FRAME' && n.name.startsWith(prefix));
  const go = async (node, dest) => {
    if (!node || !dest) return false;
    const reactions = [{ trigger: { type: 'ON_CLICK' }, actions: [{ type: 'NODE', destinationId: dest.id, navigation: 'NAVIGATE', transition: { type: 'DISSOLVE', easing: { type: 'EASE_OUT' }, duration: 0.2 }, preserveScrollPosition: false }] }];
    try { if (node.setReactionsAsync) await node.setReactionsAsync(reactions); else node.reactions = reactions; return true; } catch (e) { return false; }
  };
  const today = frame('02 · Home – Today'), modules = frame('03 · Home – Modules'), worklist = frame('M2.0-P2'), anc = frame('M2.2-05'), saved = frame('M2.0-S6'), celeb = frame('M2.1-04'), badges = frame('M2.1-02');
  let ok = 0;
  if (today) {
    if (await go(today.findOne((n) => n.type === 'INSTANCE' && n.name === 'Due card'), worklist)) ok++;
    const nav = today.findOne((n) => n.type === 'INSTANCE' && n.name === 'Bottom navigation');
    if (nav && await go(nav.findOne((n) => n.name === 'Modules'), modules)) ok++;
  }
  if (worklist) { const cards = worklist.findAll((n) => n.type === 'INSTANCE' && n.name === 'Record card'); const btn = cards[1] && cards[1].findOne((n) => n.type === 'INSTANCE' && n.name === 'Button'); if (await go(btn, anc)) ok++; }
  if (anc) { const bar = anc.findOne((n) => n.name === 'Bottom action bar'); const sub = bar && bar.children[bar.children.length - 1]; if (await go(sub, saved)) ok++; }
  if (saved && await go(saved.findOne((n) => n.type === 'INSTANCE' && n.name === 'Snackbar'), celeb)) ok++;
  if (celeb) { const btn = celeb.findOne((n) => n.type === 'INSTANCE' && n.name === 'Button'); if (await go(btn, badges)) ok++; }
  try { if (today) sp.flowStartingPoints = [{ nodeId: today.id, name: 'ASHA day: ANC visit' }]; } catch (e) { /* ignore */ }
  return ok + ' of 6 links set';
}
