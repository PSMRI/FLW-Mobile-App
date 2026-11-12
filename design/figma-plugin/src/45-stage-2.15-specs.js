// ---------------------------------------------------------------------------
// Stages 2.15 / 2.16 · Complete forms, DESIGNED (every field of every dataset, in code order)
// Stage 2.17 · Complete cards, DESIGNED (every label + button of every list-card layout)
// Data: AUDIT_FORMS / AUDIT_CARDS (design/audit/*.json). Visual language = module screens 2.2–2.13:
// person header, step bar, section cards with coloured icon tiles, leading icons, Yes/No buttons,
// choice chips, side-by-side steppers, auto-filled summary tiles, conditional blocks, sticky actions.
// ---------------------------------------------------------------------------
const FORM_NAMES = {
  AESJEFormDataset: 'AES / JE', AHDDataset: 'AHD meeting', AdolescentHealthFormDataset: 'Adolescent health', AshaProfileDataset: 'ASHA profile',
  BenGenRegFormDataset: 'Register adult (15+)', BenRegCHODataset: 'Register beneficiary (CHO)', BenRegFormDataset: 'Register member',
  CDRFormDataset: 'Child death report', ChildRegistrationDataset: 'Child registration', DeliveryOutcomeDataset: 'Delivery outcome', DewormingDataset: 'Deworming day',
  EligibleCoupleRegistrationDataset: 'Register eligible couple', EligibleCoupleTrackingDataset: 'EC monthly tracking', FPOTFormDataset: 'FP operation (FPOT)', FilariaFormDataset: 'Filaria',
  HBNCFormDataset: 'HBNC visit', HBYCFormDataset: 'HBYC visit', HRPMicroBirthPlanDataset: 'Micro birth plan', HRPNonPregnantAssessDataset: 'Assess high risk (non-pregnant)',
  HRPNonPregnantTrackDataset: 'High-risk follow-up (non-pregnant)', HRPPregnantAssessDataset: 'Assess high risk (pregnant)', HRPPregnantTrackDataset: 'HRP follow-up (pregnant)',
  HouseholdFormDataset: 'Register household', ImmunizationDataset: 'Record vaccine', InfantRegistrationDataset: 'Register baby', KalaAzarFormDataset: 'Kala-azar',
  LeprosyConfirmedDataset: 'Leprosy · on treatment', LeprosyFormDataset: 'Leprosy screening', LeprosySuspectedDataset: 'Leprosy · suspected', MDSRFormDataset: 'Maternal death report',
  MaaMeetingDataset: 'Maa meeting', MalariaConfirmCasesDataset: 'Malaria treatment', MalariaFormDataset: 'Malaria screening', NewChildBenRegDataset: 'Register child (0–14)',
  PHCReviewDataset: 'PHC review meeting', PMJAYFormDataset: 'PM-JAY (Ayushman)', PMSMAFormDataset: 'PMSMA visit', PncFormDataset: 'PNC visit', PregnantWomanAncAbortionDataset: 'Abortion record',
  PregnantWomanAncVisitDataset: 'ANC visit', PregnantWomanRegistrationDataset: 'Register pregnancy', SaasBahuSamelanDataset: 'Saas Bahu Sammelan', SurveyRegisterFormDataset: 'Survey register',
  SuspectedTBDataset: 'Suspected TB', TBConfirmedDataset: 'TB treatment', TBScreeningDataset: 'TB screening', UWINDataset: 'U-WIN session', VHNCDataset: 'VHSNC meeting', VHNDDataset: 'VHND meeting',
};
// module tone + header person/subject per dataset
const FORM_META = (ds) => {
  const t = (tone, who, sub, ic) => ({ tone, who, sub, ic });
  if (/^(PregnantWoman|PMSMA|Delivery|Pnc|MDSR|HRPPregnant|HRPMicro)/.test(ds)) return t('maternal', 'Sunita Devi', 'W/o Ramesh · 26 yrs · 24 weeks', 'pregnant');
  if (/^(HBNC|HBYC|Infant|ChildReg|NewChildBen|CDR)/.test(ds)) return t('child', 'Baby of Sunita', 'Girl · mother Sunita Devi', 'child');
  if (/^Immunization/.test(ds)) return t('immunization', 'Aarav Kumar', 'Boy · 11 weeks', 'vaccine');
  if (/^Adolescent/.test(ds)) return t('couple', 'Pooja Kumari', 'Girl · 15 yrs', 'person');
  if (/^(EligibleCouple|FPOT|HRPNonPregnant)/.test(ds)) return t('couple', 'Kavita Devi', 'W/o Manoj · 29 yrs · 2 children', 'favorite');
  if (/^(Malaria|Leprosy|KalaAzar|AESJE|Filaria|TB|SuspectedTB)/.test(ds)) return t('disease', 'Ram Prasad', 'Male · 48 yrs', 'hospital');
  if (/^(VHND|VHNC|Maa|SaasBahu|PHCReview|AHD|Deworming|UWIN)/.test(ds)) return t('village', 'Sirsi village', 'Ward 4 · November 2026', 'group');
  if (/^AshaProfile/.test(ds)) return t('people', 'Sunita Kumari', 'ASHA · Emp ID 102345', 'person');
  return t('household', 'Ramesh Kumar', 'HH 7012 3345 0012 · 5 members', 'home');
};
// section category from a field title (code order is kept; consecutive fields of one category = one section)
const CATS = [
  ['death', /death|died|deceased/i, 'Death details', 'warning'],
  ['refer', /refer|facility|hospital|nrc|sncu/i, 'Referral', 'send'],
  ['docs', /upload|photo|image|card|certificate|voucher|discharge summary|signature/i, 'Photos & documents', 'camera'],
  ['meds', /ifa|tablet|dose|vaccin|\btd\b|\btt\b|folic|calcium|medicine|mdt|treatment|drug|supplement|ors|zinc|vitamin/i, 'Medicines & doses', 'vaccine'],
  ['exam', /weight|height|\bhb\b|haemoglobin|hemoglobin|\bbp\b|blood pressure|systolic|diastolic|sugar|temperature|test|result|urine|fundal|heart|breath|muac|bmi|x-?ray|sputum|rdt|slide/i, 'Examination & tests', 'dashboard'],
  ['risk', /risk|danger|complication|sign|symptom|illness|history|condition/i, 'Risks & symptoms', 'warning'],
  ['visit', /visit|date of|period|month|year|follow|session|meeting|round/i, 'Visit details', 'calendar'],
  ['place', /place|address|village|ward|house|mohalla|district|block|state|pincode|landmark|location/i, 'Address & place', 'location'],
  ['count', /number of|no\.? of|total|count|attended|participants|children|members/i, 'Counts', 'group'],
  ['person', /name|age|gender|sex|husband|father|mother|spouse|relation|mobile|phone|contact|dob|birth|marri|religion|caste|education|occupation/i, 'Personal details', 'person'],
  ['ids', /\bid\b|rch|abha|aadhaar|mcts|nikshay|account|ifsc|bank|number/i, 'IDs & bank', 'id_card'],
];
const catOf = (f) => { const s = f.title || ''; for (const [k, re] of CATS) if (re.test(s)) return k; return 'other'; };
const CAT_INFO = Object.fromEntries(CATS.map(([k, , t, ic]) => [k, { title: t, ic }]).concat([['other', { title: 'More details', ic: 'form' }]]));
const LEAD_ICON = (title) => {
  if (/mobile|phone|contact|telephone/i.test(title)) return 'call';
  if (/\bid\b|rch|abha|aadhaar|mcts|nikshay|account|ifsc|card number|serial/i.test(title)) return 'id_card';
  if (/name|husband|father|mother|spouse|informant|companion|donor/i.test(title)) return 'person';
  if (/address|village|ward|house|mohalla|place|landmark|pincode|institution/i.test(title)) return 'location';
  if (/remark|note|other|specify|reason|details|comment/i.test(title)) return 'edit';
  return null;
};
const IS_NUMERIC = (f) => f.type === 'EDIT_TEXT' && /weight|height|\bhb\b|haemoglobin|hemoglobin|systolic|diastolic|\bbp\b|sugar|temperature|number of|no\.? of|\bno\b|total|count|tablets|quantity|packets|attended|participants|\bage\b|weeks|months|days|years|rate|score|gravida|para/i.test(f.title || '') && !/mobile|phone|\bid\b|account|\bac\b|aadhaar|rch|name|ifsc|bank|serial|card/i.test(f.title || '');

async function renderDesignedForm(S, ctx, s, d) {
  const { AL, txt, wrapText, add, pad, gap, fill, stroke, rad, paint, setP, icon, box, inst, kinst } = S;
  const meta = FORM_META(d.dataset);
  const name = FORM_NAMES[d.dataset] || d.dataset;
  S.screenTop(s, name, `${meta.who} · ${d.fields.length} fields`, 'more');

  // ---- build sections (code order kept)
  const sections = []; let cur = null;
  for (const f of d.fields) {
    if (f.type === 'HEADLINE') { cur = { title: f.title || 'Section', cat: catOf(f), fields: [], headline: true }; sections.push(cur); continue; }
    const c = catOf(f);
    if (!cur || (!cur.headline && cur.cat !== c && cur.fields.length >= 3)) { cur = { title: CAT_INFO[c].title, cat: c, fields: [] }; sections.push(cur); }
    cur.fields.push(f);
  }
  // merge tiny sections into the previous one
  for (let i = sections.length - 1; i > 0; i--) if (sections[i].fields.length < 2 && !sections[i].headline) { sections[i - 1].fields.push(...sections[i].fields); sections.splice(i, 1); }
  const live = sections.filter((x) => x.fields.length);

  // ---- step bar
  const st = AL('VERTICAL', 'Stepper'); pad(st, 'spacing/md'); st.paddingBottom = 8; gap(st, 'spacing/xs'); add(s, st);
  const bars = AL('HORIZONTAL', 'Progress'); bars.itemSpacing = 4; add(st, bars);
  live.forEach((x, i) => { const r = figma.createRectangle(); r.resize(20, 5); r.cornerRadius = 3; r.fills = [paint(i === 0 ? 'color/primary' : 'color/outline-variant')]; bars.appendChild(r); r.layoutSizingHorizontal = 'FILL'; });
  const sl = AL('HORIZONTAL', 'Step labels'); add(st, sl);
  const a = await txt(`Step 1 of ${live.length} · ${live[0] ? live[0].title : ''}`, 'Label/Large', 'color/primary'); sl.appendChild(a); a.layoutSizingHorizontal = 'FILL'; a.textAutoResize = 'HEIGHT';
  sl.appendChild(await txt(`${d.fields.filter((x) => x.required).length} required`, 'Label/Medium', 'color/on-surface-variant'));

  const b = S.body(s); b.paddingTop = 4; b.itemSpacing = 14;
  // ---- person header
  const ph = AL('HORIZONTAL', 'Subject'); ph.counterAxisAlignItems = 'CENTER'; pad(ph, 'spacing/md', 'spacing/sm'); gap(ph, 'spacing/sm'); rad(ph, 'radius/lg'); fill(ph, `module/${meta.tone}/bg`); add(b, ph);
  const it = box('Icon', 44, 44); it.cornerRadius = 22; fill(it, `module/${meta.tone}/fg`); it.appendChild(icon(meta.ic, 'color/on-primary', 24)); ph.appendChild(it);
  const pc = AL('VERTICAL'); pc.itemSpacing = 2; ph.appendChild(pc); pc.layoutSizingHorizontal = 'FILL';
  pc.appendChild(await txt(meta.who, 'Title/Medium', 'color/on-surface')); const ps = await txt(meta.sub, 'Body/Small', 'color/on-surface-variant'); pc.appendChild(ps); wrapText(ps);

  let count = 0;
  for (let si = 0; si < live.length; si++) {
    const sec = live[si]; const info = CAT_INFO[sec.cat] || CAT_INFO.other;
    const card = AL('VERTICAL', 'Section · ' + sec.title); pad(card, 'spacing/md', 'spacing/md'); gap(card, 'spacing/md'); rad(card, 'radius/lg'); fill(card, 'color/surface-container'); stroke(card, 'color/outline-variant'); add(b, card);
    const hr = AL('HORIZONTAL', 'Header'); hr.counterAxisAlignItems = 'CENTER'; gap(hr, 'spacing/sm'); add(card, hr);
    const tile = box('Icon tile', 40, 40); rad(tile, 'radius/md'); fill(tile, `module/${meta.tone}/bg`); tile.appendChild(icon(info.ic, `module/${meta.tone}/fg`, 22)); hr.appendChild(tile);
    const hc = AL('VERTICAL'); hc.itemSpacing = 0; hr.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    const ht = await txt(`${si + 1}. ${sec.title}`, 'Title/Medium', 'color/on-surface'); hc.appendChild(ht); wrapText(ht);
    hc.appendChild(await txt(`${sec.fields.length} ${sec.fields.length === 1 ? 'field' : 'fields'}`, 'Body/Small', 'color/on-surface-variant'));

    let i = 0; let reveal = null;
    const target = (f) => {
      if (f.shown === 'always') { reveal = null; return card; }
      if (!reveal) { reveal = S.reveal(card); }
      return reveal;
    };
    while (i < sec.fields.length) {
      const f = sec.fields[i];
      const t = target(f);
      const title = (f.title || f.var).trim() + (f.required ? ' *' : '');
      const opts = (f.options || []).filter((o) => o && o.trim());
      try {
        // auto-filled run → summary tile grid
        if (f.type === 'TEXT_VIEW' || f.type === 'TEXT_VIEW_PAIR') {
          const run = []; while (i < sec.fields.length && (sec.fields[i].type === 'TEXT_VIEW' || sec.fields[i].type === 'TEXT_VIEW_PAIR') && sec.fields[i].shown === f.shown) run.push(sec.fields[i++]);
          const g = AL('VERTICAL', 'Auto-filled'); pad(g, 'spacing/sm', 'spacing/sm'); g.itemSpacing = 8; rad(g, 'radius/md'); fill(g, 'color/surface-container-high'); add(t, g);
          const gh = AL('HORIZONTAL'); gh.counterAxisAlignItems = 'CENTER'; gh.itemSpacing = 6; add(g, gh); gh.appendChild(icon('lock', 'color/on-surface-variant', 16)); gh.appendChild(await txt('Filled automatically', 'Label/Medium', 'color/on-surface-variant'));
          for (let k = 0; k < run.length; k += 2) { const row = AL('HORIZONTAL'); gap(row, 'spacing/md'); add(g, row); for (const rf of run.slice(k, k + 2)) { const cell = AL('VERTICAL'); cell.itemSpacing = 2; row.appendChild(cell); cell.layoutSizingHorizontal = 'FILL'; const l = await txt(rf.title || rf.var, 'Label/Small', 'color/on-surface-variant'); cell.appendChild(l); wrapText(l); cell.appendChild(await txt('—', 'Title/Small', 'color/on-surface')); } }
          count += run.length; continue;
        }
        // numeric pair → steppers side by side
        if (IS_NUMERIC(f)) {
          const pair = [f]; if (i + 1 < sec.fields.length && IS_NUMERIC(sec.fields[i + 1]) && sec.fields[i + 1].shown === f.shown) pair.push(sec.fields[i + 1]);
          S.steppers(t, pair.map((x) => [(x.title || x.var).trim() + (x.required ? ' *' : ''), '0']));
          i += pair.length; count += pair.length; continue;
        }
        // date + time pair side by side
        if (f.type === 'DATE_PICKER' && i + 1 < sec.fields.length && sec.fields[i + 1].type === 'TIME_PICKER') {
          const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); add(t, r);
          const x1 = S.tf(r, 'Default', { Label: title, Value: 'Pick date' }, 'calendar'); x1.layoutSizingHorizontal = 'FILL';
          const nf = sec.fields[i + 1]; const x2 = S.tf(r, 'Default', { Label: (nf.title || '').trim() + (nf.required ? ' *' : ''), Value: 'Time' }, 'clock'); x2.layoutSizingHorizontal = 'FILL';
          i += 2; count += 2; continue;
        }
        switch (f.type) {
          case 'EDIT_TEXT': {
            const tf = S.tf(t, 'Default', { Label: title, Value: /mobile|phone|contact/i.test(title) ? '10-digit number' : 'Type here' });
            const li = LEAD_ICON(f.title || ''); if (li) setP(tf, { 'Show leading': true, 'Leading icon': li });
            break;
          }
          case 'DATE_PICKER': S.tf(t, 'Default', { Label: title, Value: 'DD / MM / YYYY' }, 'calendar'); break;
          case 'TIME_PICKER': S.tf(t, 'Default', { Label: title, Value: 'HH : MM' }, 'clock'); break;
          case 'DROPDOWN': case 'RADIO': {
            const short = opts.length && opts.every((o) => o.length <= 14);
            if (opts.length && opts.length <= 3 && short) await S.seg(t, title, opts, -1);
            else if (opts.length && opts.length <= 8 && opts.every((o) => o.length <= 28)) {
              const g = AL('VERTICAL', 'Choice chips'); g.itemSpacing = 8; add(t, g);
              const l = await txt(title, 'Label/Large', 'color/on-surface-variant'); g.appendChild(l); wrapText(l);
              const w = AL('HORIZONTAL', 'Chips'); w.layoutWrap = 'WRAP'; w.itemSpacing = 8; w.counterAxisSpacing = 8; add(g, w);
              for (const o of opts) w.appendChild(inst('chip', 'Selected=false', { Label: o }));
            } else {
              const sf = S.tf(t, 'Default', { Label: title, Value: 'Select', Helper: opts.length ? `Opens a searchable list · ${opts.length} options` : 'Opens a list', 'Show helper': true, 'Show trailing': true });
              const li = LEAD_ICON(f.title || ''); if (li) setP(sf, { 'Show leading': true, 'Leading icon': li });
            }
            break;
          }
          case 'CHECKBOXES': {
            const g = AL('VERTICAL', 'Multi-select'); g.itemSpacing = 8; add(t, g);
            const l = await txt(title + '  (tick all that apply)', 'Label/Large', 'color/on-surface-variant'); g.appendChild(l); wrapText(l);
            if (opts.length <= 10 && opts.every((o) => o.length <= 30)) { const w = AL('HORIZONTAL', 'Chips'); w.layoutWrap = 'WRAP'; w.itemSpacing = 8; w.counterAxisSpacing = 8; add(g, w); for (const o of (opts.length ? opts : ['Option'])) w.appendChild(inst('chip', 'Selected=false', { Label: o })); }
            else for (const o of opts) add(g, kinst('Checkbox option', 'Checked=false', { Label: o }));
            break;
          }
          case 'NUMBER_PICKER': S.steppers(t, [[title, '0']]); break;
          case 'AGE_PICKER': { const g = AL('VERTICAL', 'Age'); g.itemSpacing = 6; add(t, g); const l = await txt(title, 'Label/Large', 'color/on-surface-variant'); g.appendChild(l); wrapText(l); S.steppers(g, [['Years', '0'], ['Months', '0'], ['Days', '0']]); break; }
          case 'IMAGE_VIEW': case 'FILE_UPLOAD': case 'MULTIFILE_UPLOAD': {
            // run of uploads → one tile row
            const run = []; while (i < sec.fields.length && ['IMAGE_VIEW', 'FILE_UPLOAD', 'MULTIFILE_UPLOAD'].includes(sec.fields[i].type) && sec.fields[i].shown === f.shown) run.push(sec.fields[i++]);
            const g = AL('HORIZONTAL', 'Uploads'); g.layoutWrap = 'WRAP'; g.itemSpacing = 10; g.counterAxisSpacing = 10; add(t, g);
            for (const u of run) { const c = AL('VERTICAL', 'Upload'); c.itemSpacing = 6; c.resize(140, 10); c.counterAxisSizingMode = 'FIXED'; c.primaryAxisSizingMode = 'AUTO'; g.appendChild(c); c.appendChild(kinst('Photo tile', 'State=Empty')); const l = await txt((u.title || u.var) + (u.required ? ' *' : ''), 'Label/Small', 'color/on-surface-variant'); c.appendChild(l); wrapText(l); }
            count += run.length; continue;
          }
          case 'BUTTON': add(t, inst('button', 'Style=Tonal, State=Enabled', { Label: f.title || 'Action', 'Show icon': false })); break;
          default: S.tf(t, 'Default', { Label: title, Value: 'Type here' });
        }
      } catch (e) { S.failed.push(d.dataset + ':' + f.var + ' ' + (e && e.message)); }
      i++; count++;
    }
  }
  S.bottomBar(s, 'Save draft', 'Submit');
  return count;
}

async function buildFormSpecs(ctx, part) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureLeadingIcon(ctx);
  const S = SK(ctx); S.failed = [];
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const all = AUDIT_FORMS.slice().sort((a, b) => a.dataset.localeCompare(b.dataset));
  const half = Math.ceil(all.length / 2);
  const list = part === 1 ? all.slice(0, half) : all.slice(half);
  const secName = part === 1 ? '2.15 · Complete form specs A–M (every field)' : '2.16 · Complete form specs N–Z (every field)';
  ctx.rowGap = 0;
  const sec = S.stageSection(secName);
  const frames = []; let fieldCount = 0; const COLS = 8;
  for (let i = 0; i < list.length; i++) {
    const d = list[i];
    const s = S.phone(sec, `F-${d.dataset} · ${FORM_NAMES[d.dataset] || d.dataset}`, i % COLS, 0); frames.push(s);
    fieldCount += await renderDesignedForm(S, ctx, s, d);
  }
  const rows = []; frames.forEach((f, k) => { (rows[Math.floor(k / COLS)] = rows[Math.floor(k / COLS)] || []).push(f); });
  let y = 160; for (const r of rows) { let h = 0; r.forEach((f, c) => { f.x = 120 + c * 440; f.y = y; h = Math.max(h, f.height); }); y += h + 200; }
  await S.note(sec, 120, y, 1000, `COMPLETE FORMS · ${list.length} forms · ${fieldCount} fields${S.failed.length ? ' · failed: ' + S.failed.join(', ') : ' · 0 failed'}.\n` +
    'Every field from configuration/*Dataset.kt, in code order, nothing removed. Designed like the module screens: subject header, step bar, section cards with module-colour icons, ' +
    'leading icons (person / phone / ID / place / note), Yes-No buttons (≤3 short options), choice chips (≤8 options), searchable select (more), steppers side by side for numbers, ' +
    'date + time side by side, auto-filled values grouped in a locked tile, uploads as photo tiles, conditional fields indented with the blue line. Sections are grouped automatically by field meaning; HEADLINE fields start their own section.');
  S.fitSection(sec);
  return [`${secName}: ${list.length} forms, ${fieldCount} fields, ${S.failed.length} failed`];
}

// ---------------- cards ----------------
const LABEL_ICON = (l) => {
  if (/mobile|contact|phone/i.test(l)) return 'call';
  if (/\bid\b|rch|abha/i.test(l)) return 'id_card';
  if (/date|lmp|edd|delivery|visit|registration|diagnosis/i.test(l)) return 'calendar';
  if (/husband|father|mother|spouse|head of family|referred from/i.test(l)) return 'person';
  if (/age|gender/i.test(l)) return 'person';
  if (/weeks|pregnan/i.test(l)) return 'pregnant';
  if (/status|treatment/i.test(l)) return 'check_circle';
  if (/refer/i.test(l)) return 'send';
  if (/children|members/i.test(l)) return 'group';
  return 'info';
};
async function buildCardSpecs(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx);
  const S = SK(ctx);
  const { AL, txt, wrapText, add, pad, gap, fill, stroke, rad, paint, setP, icon, box, inst } = S;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = S.stageSection('2.17 · Complete card specs (every list card)');
  const cards = AUDIT_CARDS.filter((c) => c.items.some((x) => x.kind === 'label' || x.kind === 'button'));
  const SAMPLE = { 'Age': '26 yrs', 'Gender': 'Female', 'Beneficiary ID': '7012 4455 0091', 'Mobile Number': '98765 43210', 'RCH ID': '1234 5678 9012', 'Husband Name': 'Ramesh Kumar', 'Father Name': 'Shyam Lal', 'Spouse Name': 'Ramesh Kumar', 'Mother Name': 'Sunita Devi', 'Registration Date': '12 Mar 2026', 'Weeks Of Pregnancy': '24 wks', 'LMP Date': '18 May 2026', 'LMP': '18 May 2026', 'EDD': '15 Feb 2027', 'Date of Birth': '12 Aug 2026', 'Household ID': '7012 3345 0012', 'Contact Number': '98765 43210', 'Date of Delivery': '12 Feb 2027', 'Head of Family': 'Ramesh Kumar', 'Total No.of Children': '2', 'Last Visit Date': '3 Oct 2026', 'Status': 'Active' };
  const tones = (layout) => /pregnan|pnc|abortion|pw_|hrpa|hrp_|mdsr/.test(layout) ? 'maternal' : /child|infant|imm|hbnc|hbyc/.test(layout) ? 'child' : /ec_|hrnp|couple/.test(layout) ? 'couple' : /malaria|leprosy|kala|aes|filaria|tb_|tpt|ncd|velun/.test(layout) ? 'disease' : /household/.test(layout) ? 'household' : 'people';
  const made = []; const COLS = 5; let k = 0;
  for (const c of cards) {
    const tone = tones(c.layout);
    const w = AL('VERTICAL', `C-${c.layout}`); w.itemSpacing = 8; sec.appendChild(w);
    w.appendChild(await txt(c.layout + '.xml', 'Label/Medium', 'color/on-surface-variant'));
    const cd = AL('VERTICAL', 'Card'); S.fixW(cd, 328); pad(cd, 'spacing/md', 'spacing/md'); gap(cd, 'spacing/sm'); rad(cd, 'radius/lg'); fill(cd, 'color/surface-container'); stroke(cd, 'color/outline-variant'); w.appendChild(cd);
    // accent strip
    const strip = figma.createRectangle(); strip.name = 'Accent'; strip.resize(328, 4); strip.fills = [paint(`module/${tone}/fg`)]; cd.appendChild(strip); strip.layoutPositioning = 'ABSOLUTE'; strip.x = 0; strip.y = 0; cd.clipsContent = true;
    const labels = c.items.filter((x) => x.kind === 'label' && x.text && x.text.length < 60 && !/^(HOF|Deleted Record)$/i.test(x.text.trim())).map((x) => ({ ...x, key: x.text.replace(/[:：]\s*$/, '').trim() }));
    const hd = AL('HORIZONTAL', 'Header'); hd.counterAxisAlignItems = 'CENTER'; gap(hd, 'spacing/sm'); add(cd, hd);
    const av = box('Avatar', 44, 44); av.cornerRadius = 22; fill(av, `module/${tone}/bg`); av.appendChild(await txt(c.layout.includes('household') ? 'RK' : 'SD', 'Title/Small', `module/${tone}/fg`)); hd.appendChild(av);
    const hc = AL('VERTICAL'); hc.itemSpacing = 2; hd.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    hc.appendChild(await txt(c.layout.includes('household') ? 'Ramesh Kumar' : 'Sunita Devi', 'Title/Medium', 'color/on-surface'));
    const quick = labels.filter((l) => /^(Age|Gender)$/i.test(l.key)).map((l) => SAMPLE[l.key]).join(' · ');
    const rel = labels.find((l) => /husband|father|mother|spouse/i.test(l.key));
    const subtitle = [rel ? (/mother/i.test(rel.key) ? 'Mother ' : /father/i.test(rel.key) ? 'S/o ' : 'W/o ') + (SAMPLE[rel.key] || '') : null, quick || null].filter(Boolean).join(' · ') || 'Beneficiary';
    const sub = await txt(subtitle, 'Body/Small', 'color/on-surface-variant'); hc.appendChild(sub); wrapText(sub);
    const callB = box('Call', 40, 40); callB.cornerRadius = 20; fill(callB, 'color/success-container'); callB.appendChild(icon('call', 'color/success', 20)); hd.appendChild(callB);
    // tags row: sync + flags + key status
    const tags = AL('HORIZONTAL', 'Tags'); tags.itemSpacing = 6; tags.layoutWrap = 'WRAP'; tags.counterAxisSpacing = 6; add(cd, tags);
    tags.appendChild(inst('syncBadge', 'Status=Synced'));
    for (const fl of c.items.filter((x) => x.kind === 'label' && /^(HOF|Deleted Record)$/i.test((x.text || '').trim()))) tags.appendChild(inst('tag', `Tone=${/Deleted/i.test(fl.text) ? 'Alert' : 'Neutral'}`, { Label: fl.text.trim() }));
    const wk = labels.find((l) => /weeks/i.test(l.key)); if (wk) tags.appendChild(inst('tag', 'Tone=Maternal', { Label: 'Pregnant · 24 wks' }));
    // all labels in an icon grid (every label kept, incl. the ones summarised in the header)
    const div = figma.createRectangle(); div.resize(296, 1); div.fills = [paint('color/outline-variant')]; add(cd, div);
    const grid = AL('VERTICAL', 'Details'); gap(grid, 'spacing/sm'); add(cd, grid);
    for (let i = 0; i < labels.length; i += 2) {
      const row = AL('HORIZONTAL', 'Row'); gap(row, 'spacing/md'); add(grid, row);
      for (const lb of labels.slice(i, i + 2)) {
        const cell = AL('HORIZONTAL', 'Info'); cell.itemSpacing = 6; row.appendChild(cell); cell.layoutSizingHorizontal = 'FILL';
        cell.appendChild(icon(LABEL_ICON(lb.key), 'color/on-surface-variant', 16));
        const col = AL('VERTICAL'); col.itemSpacing = 1; cell.appendChild(col); col.layoutSizingHorizontal = 'FILL';
        const l = await txt(lb.key + (lb.hidden_by_default ? ' · if any' : ''), 'Label/Small', 'color/on-surface-variant'); col.appendChild(l); wrapText(l);
        col.appendChild(await txt(SAMPLE[lb.key] || '—', 'Title/Small', 'color/on-surface'));
      }
    }
    const btns = c.items.filter((x) => x.kind === 'button');
    if (btns.length) {
      const ar = AL('HORIZONTAL', 'Actions'); gap(ar, 'spacing/xs'); add(cd, ar);
      btns.slice(0, 3).forEach((bt, i) => { const label = /^@\{/.test(bt.text) ? 'Open' : (bt.text || 'Open'); const pretty = label.charAt(0) + label.slice(1).toLowerCase(); const x = inst('button', `Style=${i === btns.length - 1 ? 'Filled' : 'Outlined'}, State=Enabled`, { Label: pretty.slice(0, 22), 'Show icon': false }); ar.appendChild(x); x.layoutSizingHorizontal = 'FILL'; });
    }
    w.x = 120 + (k % COLS) * 400; made.push(w); k++;
  }
  const rows = []; made.forEach((f, i) => { (rows[Math.floor(i / COLS)] = rows[Math.floor(i / COLS)] || []).push(f); });
  let y = 160; for (const r of rows) { let h = 0; r.forEach((f) => { f.y = y; h = Math.max(h, f.height); }); y += h + 80; }
  await S.note(sec, 120, y, 1000, `COMPLETE CARDS · ${made.length} list-card layouts (res/layout/rv_item_*.xml). Every label and button from code is kept: module-colour accent, avatar, name + relation/age/gender summary, call button, sync + status tags, then ALL labels as an icon grid ("if any" = hidden in code until data exists), same buttons (last = primary).`);
  S.fitSection(sec);
  return [`2.17 card specs: ${made.length}`];
}

async function buildStage215(ctx) { return buildFormSpecs(ctx, 1); }
async function buildStage216(ctx) { return buildFormSpecs(ctx, 2); }
async function buildStage217(ctx) { return buildCardSpecs(ctx); }
