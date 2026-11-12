// ---------------------------------------------------------------------------
// Stage 2.0 · Patterns, form kit & states
// ---------------------------------------------------------------------------
const KIT20 = ['Radio option', 'Segmented option', 'Checkbox option', 'Stepper', 'Read-only field', 'Info row',
  'Form section header', 'Photo tile', 'File row', 'Banner', 'Snackbar', 'Dialog', 'Tabs', 'Empty state',
  'Skeleton card', 'Record card', 'Calendar'];

async function buildKit20(ctx) {
  const K = P2(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, spacer, inst, icon, comp, box, fixW, finishSet,
    setTextProp, setBoolProp, textProp, progress, circle, removeNamed } = K;
  const page = ctx.pages.components;
  await figma.setCurrentPageAsync(page);
  removeNamed(page, KIT20);
  const made = [];

  // ---- Radio option
  {
    const vs = [];
    for (const sel of [true, false]) {
      const c = comp(`Selected=${sel}`, 'HORIZONTAL'); c.resize(328, 52); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED';
      c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/md'); gap(c, 'spacing/sm'); rad(c, 'radius/md');
      fill(c, sel ? 'color/primary-subtle' : 'color/surface-container'); stroke(c, sel ? 'color/primary' : 'color/outline-variant', sel ? 2 : 1);
      const ring = box('Ring', 22, 22); ring.cornerRadius = 11; stroke(ring, sel ? 'color/primary' : 'color/outline', 2); if (sel) ring.appendChild(circle(10, 'color/primary'));
      c.appendChild(ring);
      const l = await txt('Option', 'Body/Large', 'color/on-surface', 'Label'); c.appendChild(l); l.layoutSizingHorizontal = 'FILL';
      vs.push(c);
    }
    const set = figma.combineAsVariants(vs, page); set.name = 'Radio option'; finishSet(set, 760);
    setTextProp(set, 'Label', 'Label', 'Option');
    set.description = 'InputType.RADIO. Whole row is the 52dp touch target. Use for 3–6 options; Yes/No uses Segmented option; more than 6 options use Select field + option sheet.';
    made.push(set);
  }
  // ---- Segmented option (Yes / No)
  {
    const vs = [];
    for (const sel of [true, false]) {
      const c = comp(`Selected=${sel}`, 'HORIZONTAL'); c.resize(100, 48); c.primaryAxisSizingMode = 'AUTO'; c.counterAxisSizingMode = 'FIXED';
      c.primaryAxisAlignItems = 'CENTER'; c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/md'); gap(c, 'spacing/2xs'); rad(c, 'radius/md');
      if (sel) { fill(c, 'color/primary'); c.strokes = []; const i = icon('check', 'color/on-primary', 18); i.name = 'Check'; c.appendChild(i); }
      else { fill(c, 'color/surface-container'); stroke(c, 'color/outline'); }
      c.appendChild(await txt('Yes', 'Label/Large', sel ? 'color/on-primary' : 'color/on-surface', 'Label'));
      vs.push(c);
    }
    const set = figma.combineAsVariants(vs, page); set.name = 'Segmented option'; finishSet(set, 360);
    setTextProp(set, 'Label', 'Label', 'Yes');
    set.description = 'Yes/No and 2–3 short choices (RADIO with ≤3 options). Place in a row and set each to Fill width.';
    made.push(set);
  }
  // ---- Checkbox option
  {
    const vs = [];
    for (const on of [true, false]) {
      const c = comp(`Checked=${on}`, 'HORIZONTAL'); c.resize(328, 52); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED';
      c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/md'); gap(c, 'spacing/sm'); rad(c, 'radius/md');
      fill(c, on ? 'color/primary-subtle' : 'color/surface-container'); stroke(c, on ? 'color/primary' : 'color/outline-variant', on ? 2 : 1);
      const sq = box('Box', 22, 22); sq.cornerRadius = 5;
      if (on) { fill(sq, 'color/primary'); sq.appendChild(icon('check', 'color/on-primary', 18)); } else stroke(sq, 'color/outline', 2);
      c.appendChild(sq);
      const l = await txt('Option', 'Body/Large', 'color/on-surface', 'Label'); c.appendChild(l); l.layoutSizingHorizontal = 'FILL';
      vs.push(c);
    }
    const set = figma.combineAsVariants(vs, page); set.name = 'Checkbox option'; finishSet(set, 760);
    setTextProp(set, 'Label', 'Label', 'Option');
    set.description = 'InputType.CHECKBOXES. Multi-select; add a "None of these" option where the form allows it.';
    made.push(set);
  }
  // ---- Stepper (NUMBER_PICKER / AGE_PICKER part)
  {
    const c = comp('Stepper', 'VERTICAL'); fixW(c, 156); c.itemSpacing = 4;
    const lab = await txt('Weight (kg)', 'Label/Large', 'color/on-surface-variant', 'Label'); c.appendChild(lab);
    const row = AL('HORIZONTAL', 'Field'); row.resize(156, 56); row.primaryAxisSizingMode = 'FIXED'; row.counterAxisSizingMode = 'FIXED';
    row.counterAxisAlignItems = 'CENTER'; row.paddingLeft = row.paddingRight = 6; rad(row, 'radius/md'); fill(row, 'color/surface-container'); stroke(row, 'color/outline');
    c.appendChild(row); row.layoutSizingHorizontal = 'FILL';
    const btn = (ic, n) => { const b = box(n, 40, 40); b.cornerRadius = 20; fill(b, 'color/surface-container-high'); b.appendChild(icon(ic, 'color/on-surface', 20)); return b; };
    row.appendChild(btn('minus', 'Minus'));
    const v = await txt('52', 'Title/Large', 'color/on-surface', 'Value'); row.appendChild(v); v.layoutSizingHorizontal = 'FILL'; v.textAlignHorizontal = 'CENTER';
    row.appendChild(btn('add', 'Plus'));
    textProp(c, lab, 'Label', 'Weight (kg)'); textProp(c, v, 'Value', '52');
    c.description = 'InputType.NUMBER_PICKER, and AGE_PICKER as three steppers (Years / Months / Days). Big − / + targets; value can also be typed.';
    made.push(c);
  }
  // ---- Read-only field (TEXT_VIEW)
  {
    const c = comp('Read-only field', 'VERTICAL'); fixW(c, 328); c.itemSpacing = 4;
    const hr = AL('HORIZONTAL', 'Header'); hr.counterAxisAlignItems = 'CENTER'; add(c, hr);
    const lab = await txt('Expected delivery date (EDD)', 'Label/Large', 'color/on-surface-variant', 'Label'); hr.appendChild(lab); spacer(hr);
    const pill = AL('HORIZONTAL', 'Auto'); pill.paddingLeft = pill.paddingRight = 8; pill.paddingTop = pill.paddingBottom = 2; rad(pill, 'radius/sm'); fill(pill, 'color/primary-subtle');
    pill.appendChild(await txt('Auto', 'Label/Small', 'color/primary')); hr.appendChild(pill);
    const bx = AL('HORIZONTAL', 'Field'); bx.resize(328, 56); bx.primaryAxisSizingMode = 'FIXED'; bx.counterAxisSizingMode = 'FIXED'; bx.counterAxisAlignItems = 'CENTER';
    pad(bx, 'spacing/md'); gap(bx, 'spacing/xs'); rad(bx, 'radius/md'); fill(bx, 'color/surface-container-high'); c.appendChild(bx); bx.layoutSizingHorizontal = 'FILL';
    const v = await txt('15 Feb 2027', 'Body/Large', 'color/on-surface', 'Value'); bx.appendChild(v); v.layoutSizingHorizontal = 'FILL';
    bx.appendChild(icon('lock', 'color/on-surface-variant', 18));
    textProp(c, lab, 'Label', 'Expected delivery date (EDD)'); textProp(c, v, 'Value', '15 Feb 2027'); K.boolProp(c, pill, 'Show auto', true);
    c.description = 'InputType.TEXT_VIEW: calculated or pre-filled values (EDD, age, gestational weeks). Grey fill + lock = cannot edit.';
    made.push(c);
  }
  // ---- Info row (TEXT_VIEW_PAIR / view mode)
  {
    const c = comp('Info row', 'HORIZONTAL'); fixW(c, 328); c.counterAxisAlignItems = 'CENTER'; c.paddingTop = c.paddingBottom = 10; gap(c, 'spacing/sm');
    c.strokes = [paint('color/outline-variant')]; c.strokeTopWeight = 0; c.strokeLeftWeight = 0; c.strokeRightWeight = 0; c.strokeBottomWeight = 1;
    const l = await txt('Date of visit', 'Body/Medium', 'color/on-surface-variant', 'Label'); c.appendChild(l); l.layoutSizingHorizontal = 'FILL';
    const v = await txt('14 Aug 2026', 'Title/Small', 'color/on-surface', 'Value'); c.appendChild(v); v.textAlignHorizontal = 'RIGHT';
    textProp(c, l, 'Label', 'Date of visit'); textProp(c, v, 'Value', '14 Aug 2026');
    c.description = 'InputType.TEXT_VIEW_PAIR and every field in view mode (P5).';
    made.push(c);
  }
  // ---- Form section header (HEADLINE)
  {
    const vs = [];
    for (const st of ['Open', 'Closed', 'Done']) {
      const c = comp(`State=${st}`, 'HORIZONTAL'); c.resize(328, 56); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED';
      c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/md'); gap(c, 'spacing/sm'); rad(c, 'radius/md');
      fill(c, st === 'Open' ? 'color/primary-subtle' : 'color/surface-container-high');
      const n = box('Step', 28, 28); n.cornerRadius = 14;
      if (st === 'Done') { fill(n, 'color/success'); n.appendChild(icon('check', 'color/on-primary', 18)); }
      else { fill(n, st === 'Open' ? 'color/primary' : 'color/outline'); n.appendChild(await txt('2', 'Label/Large', 'color/on-primary', 'Number')); }
      c.appendChild(n);
      const t = await txt('Examination', 'Title/Medium', 'color/on-surface', 'Title'); c.appendChild(t); t.layoutSizingHorizontal = 'FILL';
      c.appendChild(await txt(st === 'Done' ? 'Complete' : '3 of 6', 'Label/Medium', st === 'Done' ? 'color/success' : 'color/on-surface-variant', 'Meta'));
      c.appendChild(icon(st === 'Open' ? 'chevron_down' : 'chevron_right', 'color/on-surface-variant', 20));
      vs.push(c);
    }
    const set = figma.combineAsVariants(vs, page); set.name = 'Form section header'; finishSet(set, 760);
    setTextProp(set, 'Title', 'Title', 'Examination'); setTextProp(set, 'Meta', 'Meta', '3 of 6'); setTextProp(set, 'Number', 'Number', '2');
    set.description = 'InputType.HEADLINE. Long forms are split into collapsible numbered sections; Done shows a green tick so the ASHA sees progress.';
    made.push(set);
  }
  // ---- Photo tile (IMAGE_VIEW)
  {
    const vs = [];
    for (const st of ['Empty', 'Captured']) {
      const c = comp(`State=${st}`, 'VERTICAL'); c.resize(104, 104); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED';
      c.primaryAxisAlignItems = 'CENTER'; c.counterAxisAlignItems = 'CENTER'; c.itemSpacing = 6; rad(c, 'radius/md');
      if (st === 'Empty') {
        fill(c, 'color/surface-container-high'); stroke(c, 'color/outline'); c.dashPattern = [6, 4];
        c.appendChild(icon('camera', 'color/on-surface-variant', 28)); c.appendChild(await txt('Add photo', 'Label/Medium', 'color/on-surface-variant', 'Label'));
      } else {
        fill(c, 'module/care/bg'); c.appendChild(icon('person', 'module/care/fg', 48));
        const p = AL('HORIZONTAL', 'Retake'); p.paddingLeft = p.paddingRight = 8; p.paddingTop = p.paddingBottom = 2; rad(p, 'radius/full'); fill(p, 'color/on-surface');
        p.appendChild(await txt('Retake', 'Label/Small', 'color/on-primary')); c.appendChild(p);
      }
      vs.push(c);
    }
    const set = figma.combineAsVariants(vs, page); set.name = 'Photo tile'; finishSet(set, 300);
    set.description = 'InputType.IMAGE_VIEW: camera or gallery. Captured shows a thumbnail with Retake.';
    made.push(set);
  }
  // ---- File row (FILE_UPLOAD / MULTIFILE_UPLOAD)
  {
    const vs = [];
    const spec = { Uploading: ['color/primary-subtle', 'color/primary', '1.2 MB · Uploading 60%', 'color/on-surface-variant', 'close'], Done: ['color/success-container', 'color/success', '1.2 MB · Uploaded', 'color/on-surface-variant', 'check_circle'], Error: ['color/error-container', 'color/error', 'Upload failed · tap to retry', 'color/error', 'close'] };
    for (const [st, [bg, fg, meta, mc, tr]] of Object.entries(spec)) {
      const c = comp(`State=${st}`, 'HORIZONTAL'); fixW(c, 328); c.counterAxisAlignItems = 'CENTER'; c.paddingLeft = c.paddingRight = 12; c.paddingTop = c.paddingBottom = 10;
      gap(c, 'spacing/sm'); rad(c, 'radius/md'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant');
      const t = box('File icon', 40, 40); rad(t, 'radius/sm'); fill(t, bg); t.appendChild(icon('form', fg, 22)); c.appendChild(t);
      const col = AL('VERTICAL', 'Text'); col.itemSpacing = 4; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      col.appendChild(await txt('attendance_register.jpg', 'Title/Small', 'color/on-surface', 'File name'));
      col.appendChild(await txt(meta, 'Body/Small', mc, 'Meta'));
      if (st === 'Uploading') col.appendChild(progress(200, 0.6));
      c.appendChild(icon(tr, st === 'Done' ? 'color/success' : 'color/on-surface-variant', 20));
      vs.push(c);
    }
    const set = figma.combineAsVariants(vs, page); set.name = 'File row'; finishSet(set, 1100);
    setTextProp(set, 'File name', 'File name', 'attendance_register.jpg');
    set.description = 'InputType.FILE_UPLOAD / MULTIFILE_UPLOAD. One row per file; uploads continue offline and retry on sync.';
    made.push(set);
  }
  // ---- Banner
  {
    const vs = [];
    const spec = { Error: ['color/error-container', 'color/error', 'warning', 'color/on-error-container'], Warning: ['color/warning-container', 'color/warning', 'warning', 'color/on-surface'], Info: ['color/primary-subtle', 'color/primary', 'info', 'color/on-surface'], Success: ['color/success-container', 'color/success', 'check_circle', 'color/on-surface'], Offline: ['color/surface-container-high', 'color/on-surface-variant', 'wifi_off', 'color/on-surface'] };
    for (const [st, [bg, fg, ic, tc]] of Object.entries(spec)) {
      const c = comp(`Type=${st}`, 'HORIZONTAL'); fixW(c, 328); pad(c, 'spacing/md', 'spacing/sm'); gap(c, 'spacing/sm'); rad(c, 'radius/md'); fill(c, bg);
      c.appendChild(icon(ic, fg, 20));
      const col = AL('VERTICAL', 'Text'); col.itemSpacing = 2; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      const t = await txt('2 fields need attention', 'Title/Small', tc, 'Title'); col.appendChild(t); wrapText(t);
      const b = await txt('Check the fields marked in red before you submit.', 'Body/Small', tc, 'Body'); col.appendChild(b); wrapText(b);
      c.appendChild(await txt('Show', 'Label/Large', fg, 'Action'));
      vs.push(c);
    }
    const set = figma.combineAsVariants(vs, page); set.name = 'Banner'; finishSet(set, 1100);
    setTextProp(set, 'Title', 'Title', '2 fields need attention'); setTextProp(set, 'Body', 'Body', 'Check the fields marked in red before you submit.');
    setTextProp(set, 'Action', 'Action', 'Show'); setBoolProp(set, 'Action', 'Show action', true);
    set.description = 'Inline messages: validation summary (Error), sync waiting (Warning), info/help (Info), success, and Offline.';
    made.push(set);
  }
  // ---- Snackbar
  {
    const vs = [];
    for (const st of ['Success', 'Error', 'Badge']) {
      const c = comp(`Type=${st}`, 'HORIZONTAL'); fixW(c, 328); c.counterAxisAlignItems = 'CENTER'; pad(c, 'spacing/md', 'spacing/sm'); gap(c, 'spacing/sm'); rad(c, 'radius/md');
      fill(c, 'color/on-surface'); await c.setEffectStyleIdAsync(ctx.ES['Elevation/3'].id);
      if (st === 'Badge') { const m = box('Badge', 36, 36); m.cornerRadius = 18; fill(m, 'module/maternal/bg'); m.appendChild(icon('trophy', 'module/maternal/fg', 20)); c.appendChild(m); }
      else c.appendChild(icon(st === 'Success' ? 'check_circle' : 'warning', st === 'Success' ? 'color/success-container' : 'color/error-container', 20));
      const col = AL('VERTICAL', 'Text'); col.itemSpacing = 2; c.appendChild(col); col.layoutSizingHorizontal = 'FILL';
      const m = await txt(st === 'Error' ? 'Could not save. Try again.' : 'ANC visit 3 saved', 'Body/Medium', 'color/on-primary', 'Message'); col.appendChild(m); wrapText(m);
      if (st === 'Badge') { const d = await txt('Maternal Journey · 3 of 4 ANC visits', 'Label/Small', 'color/primary-container', 'Detail'); col.appendChild(d); wrapText(d); }
      c.appendChild(await txt(st === 'Error' ? 'Retry' : (st === 'Badge' ? 'View' : 'Undo'), 'Label/Large', 'color/primary-container', 'Action'));
      vs.push(c);
    }
    const set = figma.combineAsVariants(vs, page); set.name = 'Snackbar'; finishSet(set, 1100);
    setTextProp(set, 'Message', 'Message', 'ANC visit 3 saved'); setTextProp(set, 'Action', 'Action', 'View'); setTextProp(set, 'Detail', 'Detail', 'Maternal Journey · 3 of 4 ANC visits');
    set.description = 'Confirmation after save. Badge type is used only when the save moves a badge forward (gamification ON).';
    made.push(set);
  }
  // ---- Dialog
  {
    const c = comp('Dialog', 'VERTICAL'); fixW(c, 312); pad(c, 'spacing/xl', 'spacing/xl'); gap(c, 'spacing/md'); rad(c, 'radius/xl'); fill(c, 'color/surface-container');
    await c.setEffectStyleIdAsync(ctx.ES['Elevation/3'].id);
    c.appendChild(icon('warning', 'color/warning', 28));
    const t = await txt('Leave without saving?', 'Title/Large', 'color/on-surface', 'Title'); c.appendChild(t); wrapText(t);
    const b = await txt('Your entries are kept as a draft. You can finish this form later from Drafts.', 'Body/Medium', 'color/on-surface-variant', 'Body'); c.appendChild(b); wrapText(b);
    const acts = AL('HORIZONTAL', 'Actions'); acts.primaryAxisAlignItems = 'MAX'; gap(acts, 'spacing/xs'); add(c, acts);
    acts.appendChild(inst('button', 'Style=Text, State=Enabled', { Label: 'Stay', 'Show icon': false }));
    acts.appendChild(inst('button', 'Style=Filled, State=Enabled', { Label: 'Leave', 'Show icon': false }));
    textProp(c, t, 'Title', 'Leave without saving?'); textProp(c, b, 'Body', 'Your entries are kept as a draft. You can finish this form later from Drafts.');
    c.description = 'Confirmation dialogs (unsaved changes, delete, submit final). Main action on the right.';
    made.push(c);
  }
  // ---- Tabs
  {
    const vs = [];
    for (const act of [1, 2, 3]) {
      const c = comp(`Active=${act}`, 'HORIZONTAL'); c.resize(360, 48); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED'; fill(c, 'color/surface');
      c.strokes = [paint('color/outline-variant')]; c.strokeTopWeight = 0; c.strokeLeftWeight = 0; c.strokeRightWeight = 0; c.strokeBottomWeight = 1;
      const defs = [['Due', '12'], ['Done', '30'], ['All', '42']];
      for (let i = 1; i <= 3; i++) {
        const on = i === act;
        const it = AL('VERTICAL', 'Tab ' + i); it.counterAxisAlignItems = 'CENTER'; it.primaryAxisAlignItems = 'SPACE_BETWEEN'; it.paddingTop = 13; c.appendChild(it);
        it.layoutSizingHorizontal = 'FILL'; it.layoutSizingVertical = 'FILL';
        const r = AL('HORIZONTAL', 'Row'); r.counterAxisAlignItems = 'CENTER'; r.itemSpacing = 6; it.appendChild(r);
        r.appendChild(await txt(defs[i - 1][0], 'Title/Small', on ? 'color/primary' : 'color/on-surface-variant', 'Label ' + i));
        const pill = AL('HORIZONTAL', 'Pill'); pill.paddingLeft = pill.paddingRight = 6; rad(pill, 'radius/full'); fill(pill, on ? 'color/primary' : 'color/surface-container-high');
        pill.appendChild(await txt(defs[i - 1][1], 'Label/Small', on ? 'color/on-primary' : 'color/on-surface-variant', 'Count ' + i)); r.appendChild(pill);
        const ind = figma.createRectangle(); ind.name = 'Indicator'; ind.resize(56, 3); ind.topLeftRadius = 3; ind.topRightRadius = 3; ind.fills = on ? [paint('color/primary')] : []; it.appendChild(ind);
      }
      vs.push(c);
    }
    const set = figma.combineAsVariants(vs, page); set.name = 'Tabs'; finishSet(set, 1200);
    for (let i = 1; i <= 3; i++) { setTextProp(set, 'Label ' + i, 'Label ' + i, ['Due', 'Done', 'All'][i - 1]); setTextProp(set, 'Count ' + i, 'Count ' + i, ['12', '30', '42'][i - 1]); }
    set.description = 'Worklist tabs (P2): Due / Done / All with counts.';
    made.push(set);
  }
  // ---- Empty state
  {
    const c = comp('Empty state', 'VERTICAL'); fixW(c, 296); c.counterAxisAlignItems = 'CENTER'; gap(c, 'spacing/md'); c.paddingTop = c.paddingBottom = 32;
    const ib = box('Illustration', 96, 96); ib.cornerRadius = 48; fill(ib, 'color/primary-subtle'); const ic = icon('group', 'color/primary', 44); ib.appendChild(ic); c.appendChild(ib);
    const t = await txt('No eligible couples yet', 'Title/Medium', 'color/on-surface', 'Title'); c.appendChild(t); wrapText(t); t.textAlignHorizontal = 'CENTER';
    const b = await txt('Couples appear here after you register women aged 15–49 in a household.', 'Body/Medium', 'color/on-surface-variant', 'Body'); c.appendChild(b); wrapText(b); b.textAlignHorizontal = 'CENTER';
    c.appendChild(inst('button', 'Style=Tonal, State=Enabled', { Label: 'Go to households', 'Show icon': false }));
    textProp(c, t, 'Title', 'No eligible couples yet'); textProp(c, b, 'Body', 'Couples appear here after you register women aged 15–49 in a household.');
    const ik = c.addComponentProperty('Icon', 'INSTANCE_SWAP', ctx.IC.group.id); ic.componentPropertyReferences = { mainComponent: ik };
    c.description = 'Empty lists: say why it is empty and what to do next.';
    made.push(c);
  }
  // ---- Skeleton card
  {
    const c = comp('Skeleton card', 'VERTICAL'); fixW(c, 328); pad(c, 'spacing/md', 'spacing/md'); gap(c, 'spacing/sm'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant');
    const bar = (w, h) => { const r = figma.createRectangle(); r.resize(w, h); r.cornerRadius = 4; r.fills = [paint('color/surface-container-high')]; return r; };
    const r1 = AL('HORIZONTAL'); r1.counterAxisAlignItems = 'CENTER'; gap(r1, 'spacing/sm'); c.appendChild(r1);
    r1.appendChild(circle(44, 'color/surface-container-high')); const cc = AL('VERTICAL'); cc.itemSpacing = 8; r1.appendChild(cc); cc.appendChild(bar(160, 14)); cc.appendChild(bar(100, 10));
    c.appendChild(bar(296, 10)); c.appendChild(bar(220, 10));
    c.description = 'Loading placeholder for lists (show 3–4).';
    made.push(c);
  }
  // ---- Record card (compact worklist card)
  {
    const c = comp('Record card', 'VERTICAL'); fixW(c, 328); pad(c, 'spacing/md', 'spacing/md'); gap(c, 'spacing/sm'); rad(c, 'radius/lg'); fill(c, 'color/surface-container'); stroke(c, 'color/outline-variant');
    const r1 = AL('HORIZONTAL', 'Header'); r1.counterAxisAlignItems = 'CENTER'; gap(r1, 'spacing/sm'); add(c, r1);
    r1.appendChild(inst('avatar', null, { Initials: 'SD' }));
    const col = AL('VERTICAL', 'Name block'); col.itemSpacing = 2; r1.appendChild(col); col.layoutSizingHorizontal = 'FILL';
    const nm = await txt('Sunita Devi', 'Title/Medium', 'color/on-surface', 'Name'); col.appendChild(nm); nm.layoutSizingHorizontal = 'FILL'; nm.textTruncation = 'ENDING'; nm.maxLines = 1;
    const mt = await txt('W/o Ramesh · 26 yrs · 24 wks', 'Body/Small', 'color/on-surface-variant', 'Meta'); col.appendChild(mt); mt.layoutSizingHorizontal = 'FILL';
    r1.appendChild(inst('syncBadge', 'Status=Pending'));
    const r2 = AL('HORIZONTAL', 'Footer'); r2.counterAxisAlignItems = 'CENTER'; gap(r2, 'spacing/sm'); add(c, r2);
    r2.appendChild(inst('tag', 'Tone=Maternal', { Label: 'ANC 3 due · 12 Nov' })); spacer(r2);
    const b = inst('button', 'Style=Tonal, State=Enabled', { Label: 'Start ANC 3', 'Show icon': false }); r2.appendChild(b); b.resize(b.width, 40);
    textProp(c, nm, 'Name', 'Sunita Devi'); textProp(c, mt, 'Meta', 'W/o Ramesh · 26 yrs · 24 wks');
    c.description = 'Compact worklist card (P2): who, what is due (tag), one main action. Tap the card for details / visit history.';
    made.push(c);
  }
  // ---- Calendar dialog (DATE_PICKER)
  {
    const c = comp('Calendar', 'VERTICAL'); fixW(c, 328); c.paddingLeft = c.paddingRight = 20; c.paddingTop = c.paddingBottom = 20; c.itemSpacing = 12; rad(c, 'radius/xl'); fill(c, 'color/surface-container');
    await c.setEffectStyleIdAsync(ctx.ES['Elevation/3'].id);
    c.appendChild(await txt('Date of visit', 'Label/Medium', 'color/on-surface-variant', 'Label'));
    c.appendChild(await txt('Mon, 2 Nov 2026', 'Headline/Small', 'color/on-surface', 'Selected'));
    const mr = AL('HORIZONTAL', 'Month'); mr.counterAxisAlignItems = 'CENTER'; mr.itemSpacing = 8; add(c, mr);
    const ml = await txt('November 2026', 'Title/Small', 'color/on-surface'); mr.appendChild(ml); ml.layoutSizingHorizontal = 'FILL';
    mr.appendChild(icon('chevron_left', 'color/on-surface-variant')); mr.appendChild(icon('chevron_right', 'color/on-surface-variant'));
    const grid = AL('VERTICAL', 'Grid'); grid.itemSpacing = 2; add(c, grid);
    const row = () => { const r = AL('HORIZONTAL', 'Week'); r.primaryAxisAlignItems = 'SPACE_BETWEEN'; add(grid, r); return r; };
    const wr = row(); for (const d of ['S', 'M', 'T', 'W', 'T', 'F', 'S']) { const b = box('Day', 40, 24); b.appendChild(await txt(d, 'Label/Small', 'color/on-surface-variant')); wr.appendChild(b); }
    const first = new Date(2026, 10, 1).getDay(); const today = 4, sel = 2;
    let r = row(); for (let i = 0; i < first; i++) r.appendChild(box('Blank', 40, 40));
    for (let d = 1; d <= 30; d++) {
      if ((first + d - 1) % 7 === 0 && d !== 1) r = row();
      const b = box('Day ' + d, 40, 40); b.cornerRadius = 20;
      let color = 'color/on-surface';
      if (d === sel) { fill(b, 'color/primary'); color = 'color/on-primary'; } else if (d === today) { stroke(b, 'color/primary'); color = 'color/primary'; }
      const t = await txt(String(d), 'Body/Medium', color); if (d > today) t.opacity = 0.35; b.appendChild(t); r.appendChild(b);
    }
    const remaining = 7 - r.children.length; for (let i = 0; i < remaining; i++) r.appendChild(box('Blank', 40, 40));
    const hint = await txt('Future dates are not allowed for a visit.', 'Body/Small', 'color/on-surface-variant', 'Hint'); c.appendChild(hint);
    const acts = AL('HORIZONTAL', 'Actions'); acts.primaryAxisAlignItems = 'MAX'; gap(acts, 'spacing/xs'); add(c, acts);
    acts.appendChild(inst('button', 'Style=Text, State=Enabled', { Label: 'Cancel', 'Show icon': false }));
    acts.appendChild(inst('button', 'Style=Text, State=Enabled', { Label: 'OK', 'Show icon': false }));
    textProp(c, hint, 'Hint', 'Future dates are not allowed for a visit.');
    c.description = 'InputType.DATE_PICKER dialog. Always state the allowed range (hint) and grey out dates outside it.';
    made.push(c);
  }

  stackOnPage(made, 1500, 260);
  await kitFixes(ctx);
  // refresh component lookup for later code
  for (const n of made) ctx.N['k:' + n.name] = n;
  return made.map((n) => n.name);
}

// ---------------------------------------------------------------------------
async function buildStage20(ctx) {
  await ensureIcons(ctx);
  const kit = await buildKit20(ctx);
  const K = P2(ctx);
  const { paint, fill, stroke, rad, pad, gap, AL, txt, wrapText, add, spacer, setP, icon, fixW, progress, stageSection, fitSection, phone, body, card, scrim, sheet, pinBottom, note, box } = K;
  const kinst = (name, variant, props = {}) => {
    const src = ctx.N['k:' + name]; let c = src;
    if (src.type === 'COMPONENT_SET') { c = variant ? src.children.find((v) => v.name === variant) : src.defaultVariant; if (!c) throw new Error('Variant ' + variant + ' not in ' + name); }
    const i = c.createInstance(); setP(i, props); return i;
  };
  const inst = K.inst;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.0 · Patterns, form kit & states');
  const hideAct = (bar, all = false) => { const a = bar.findAll((n) => n.name === 'Action'); (all ? a : a.slice(0, 1)).forEach((x) => { x.visible = false; }); };
  const nested = (root, name) => root.findOne((n) => n.type === 'INSTANCE' && n.name === name);
  const label = async (p, s) => { const t = await txt(s, 'Label/Large', 'color/on-surface-variant'); p.appendChild(t); return t; };
  const segRow = async (p, q, opts, selIdx) => {
    const g = AL('VERTICAL', 'Question'); g.itemSpacing = 6; add(p, g);
    const t = await txt(q, 'Label/Large', 'color/on-surface-variant'); g.appendChild(t); wrapText(t);
    const r = AL('HORIZONTAL', 'Options'); gap(r, 'spacing/xs'); add(g, r);
    opts.forEach((o, i) => { const s = kinst('Segmented option', `Selected=${i === selIdx}`, { Label: o }); r.appendChild(s); s.layoutSizingHorizontal = 'FILL'; });
    return g;
  };
  const recordCard = (p, d) => {
    const c = kinst('Record card', null, { Name: d.name, Meta: d.meta }); add(p, c);
    setP(nested(c, 'Avatar'), { Initials: d.name.split(' ').map((w) => w[0]).join('').slice(0, 2) });
    setP(nested(c, 'Sync badge'), { Status: d.sync || 'Synced' });
    setP(nested(c, 'Tag'), { Tone: d.tone || 'Maternal', Label: d.tag });
    const b = nested(c, 'Button'); if (d.action) setP(b, { Label: d.action }); else b.visible = false;
    return c;
  };
  const bottomBar = (s, a, b) => {
    const bar = AL('HORIZONTAL', 'Bottom action bar'); pad(bar, 'spacing/md', 'spacing/sm'); gap(bar, 'spacing/sm'); fill(bar, 'color/surface-container'); add(s, bar);
    bar.strokes = [paint('color/outline-variant')]; bar.strokeTopWeight = 1; bar.strokeBottomWeight = 0; bar.strokeLeftWeight = 0; bar.strokeRightWeight = 0;
    for (const [st, l] of [['Outlined', a], ['Filled', b]]) { const x = inst('button', `Style=${st}, State=Enabled`, { Label: l, 'Show icon': false }); bar.appendChild(x); x.layoutSizingHorizontal = 'FILL'; }
    return bar;
  };
  const tf = (p, state, props, trailingIcon) => {
    const f = inst('textField', `State=${state}`, Object.assign({ 'Show helper': false, 'Show trailing': !!trailingIcon }, props)); add(p, f);
    if (trailingIcon && trailingIcon !== 'dropdown') { const t = f.findOne((n) => n.name === 'Trailing'); if (t && t.type === 'INSTANCE') { t.swapComponent(ctx.IC[trailingIcon]); const g = t.findOne((n) => n.name === 'glyph'); if (g) g.fills = [paint('color/on-surface-variant')]; } }
    return f;
  };
  const frames = [];

  // ===== P1 Module hub
  {
    const s = phone(sec, 'M2.0-P1 · Module hub (Maternal health)', 0, 0); frames.push(s);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'Maternal health', Subtitle: '42 pregnant · 18 PNC mothers' }));
    const b = body(s);
    const st = AL('HORIZONTAL', 'Summary'); gap(st, 'spacing/xs'); add(b, st);
    for (const [n, l, t] of [['12', 'Due for ANC', 'maternal'], ['5', 'High risk', 'disease'], ['3', 'Deliveries due', 'child']]) {
      const c = AL('VERTICAL', 'Stat'); pad(c, 'spacing/sm', 'spacing/sm'); c.itemSpacing = 2; rad(c, 'radius/lg'); fill(c, `module/${t}/bg`); st.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      c.appendChild(await txt(n, 'Number/Medium', `module/${t}/fg`)); const lt = await txt(l, 'Label/Medium', 'color/on-surface'); c.appendChild(lt); wrapText(lt);
    }
    add(b, inst('sectionHeader', null, { Title: 'Registers', 'Show action': false }));
    const grid = AL('HORIZONTAL', 'Grid'); grid.layoutWrap = 'WRAP'; grid.itemSpacing = 16; grid.counterAxisSpacing = 16; add(b, grid);
    for (const [t, n, c, ic] of [['maternal', 'Pregnancy registration', '42 registered', 'pregnant'], ['maternal', 'ANC visits', '12 due', 'calendar'], ['maternal', 'PMSMA', '3 this month', 'hospital'], ['child', 'Delivery outcome', '3 due', 'child'], ['maternal', 'PNC mothers', '18 active', 'favorite'], ['child', 'Infant registration', '9 pending', 'child'], ['care', 'HWC referred', '2 referred', 'send'], ['records', 'Abortion', '1 recorded', 'form']]) {
      const m = inst('moduleTile', `Tone=${t}`, { Title: n, Count: c, Icon: ic }); grid.appendChild(m); m.resize(156, m.height);
    }
  }
  // ===== P2 Worklist
  {
    const s = phone(sec, 'M2.0-P2 · Worklist (ANC visits)', 1, 0); frames.push(s);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'ANC visits', Subtitle: '42 pregnant women' }));
    add(s, kinst('Tabs', 'Active=1'));
    const b = body(s); b.itemSpacing = 12;
    add(b, inst('searchBar', null, { Placeholder: 'Search name, mobile or RCH ID' }));
    const ch = AL('HORIZONTAL', 'Filters'); gap(ch, 'spacing/xs'); add(b, ch); ch.clipsContent = true;
    ['This week', 'Overdue 3', 'High risk'].forEach((l, i) => ch.appendChild(inst('chip', `Selected=${i === 0}`, { Label: l })));
    recordCard(b, { name: 'Pooja Kumari', meta: 'W/o Raju · 22 yrs · 14 wks', tag: 'ANC 2 overdue · 2 Nov', tone: 'Alert', action: 'Start ANC 2', sync: 'Synced' });
    recordCard(b, { name: 'Sunita Devi', meta: 'W/o Ramesh · 26 yrs · 24 wks', tag: 'ANC 3 due · 12 Nov', action: 'Start ANC 3', sync: 'Pending' });
    recordCard(b, { name: 'Rekha Yadav', meta: 'W/o Mohan · 31 yrs · 33 wks', tag: 'ANC 4 due · 18 Nov', action: 'Start ANC 4' });
    recordCard(b, { name: 'Anita Devi', meta: 'W/o Suresh · 28 yrs · 9 wks', tag: 'ANC 1 due · 20 Nov', action: 'Start ANC 1' });
  }
  // ===== P3 Long form
  {
    const s = phone(sec, 'M2.0-P3 · Long form (ANC visit)', 2, 0); frames.push(s);
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: 'ANC visit 3', Subtitle: 'Sunita Devi · 24 weeks' })); hideAct(bar);
    const b = body(s); b.itemSpacing = 12;
    add(b, kinst('Banner', 'Type=Error', { Title: '1 field needs attention', Body: 'Blood pressure must be written like 120/80.', Action: 'Show' }));
    add(b, kinst('Form section header', 'State=Done', { Title: 'Visit details', Meta: 'Complete' }));
    add(b, kinst('Form section header', 'State=Open', { Title: 'Examination', Meta: '4 of 6', Number: '2' }));
    tf(b, 'Filled', { Label: 'Date of visit *', Value: '02 Nov 2026' }, 'calendar');
    add(b, kinst('Read-only field', null, { Label: 'Weeks of pregnancy', Value: '24 weeks 3 days' }));
    const sr = AL('HORIZONTAL', 'Steppers'); gap(sr, 'spacing/md'); add(b, sr);
    for (const [l, v] of [['Weight (kg) *', '54'], ['Hb (g/dl) *', '9.8']]) { const x = kinst('Stepper', null, { Label: l, Value: v }); sr.appendChild(x); x.layoutSizingHorizontal = 'FILL'; }
    tf(b, 'Error', { Label: 'Blood pressure *', Value: '12080', Helper: 'Write it like 120/80', 'Show helper': true, 'Show trailing': true });
    await segRow(b, 'Any danger signs? *', ['Yes', 'No'], 0);
    const rv = AL('VERTICAL', 'Revealed (conditional)'); rv.itemSpacing = 8; rv.paddingLeft = 14; add(b, rv);
    rv.strokes = [paint('color/primary')]; rv.strokeLeftWeight = 3; rv.strokeTopWeight = 0; rv.strokeRightWeight = 0; rv.strokeBottomWeight = 0;
    await label(rv, 'Which danger signs? *');
    for (const [l, on] of [['Severe headache', true], ['Bleeding', false], ['Swelling of face or hands', true]]) add(rv, kinst('Checkbox option', `Checked=${on}`, { Label: l }));
    add(b, kinst('Form section header', 'State=Closed', { Title: 'Counselling & referral', Meta: '5 fields', Number: '3' }));
    add(b, kinst('Form section header', 'State=Closed', { Title: 'Next visit', Meta: '2 fields', Number: '4' }));
    bottomBar(s, 'Save draft', 'Submit');
  }
  // ===== P4 Visit history sheet
  {
    const s = phone(sec, 'M2.0-P4 · Visit history sheet', 3, 0, true); frames.push(s);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'ANC visits', Subtitle: '42 pregnant women' }));
    const b = body(s); b.itemSpacing = 12;
    recordCard(b, { name: 'Sunita Devi', meta: 'W/o Ramesh · 26 yrs · 24 wks', tag: 'ANC 3 due · 12 Nov', action: 'Start ANC 3', sync: 'Pending' });
    recordCard(b, { name: 'Rekha Yadav', meta: 'W/o Mohan · 31 yrs · 33 wks', tag: 'ANC 4 due · 18 Nov', action: 'Start ANC 4' });
    scrim(s);
    const sh = sheet(s, 'Visit history');
    const hd = AL('HORIZONTAL', 'Who'); hd.counterAxisAlignItems = 'CENTER'; gap(hd, 'spacing/sm'); add(sh, hd);
    hd.appendChild(inst('avatar', null, { Initials: 'SD' }));
    const hc = AL('VERTICAL'); hc.itemSpacing = 2; hd.appendChild(hc); hc.layoutSizingHorizontal = 'FILL';
    hc.appendChild(await txt('Sunita Devi', 'Title/Medium', 'color/on-surface'));
    hc.appendChild(await txt('24 weeks · EDD 15 Feb 2027', 'Body/Small', 'color/on-surface-variant'));
    for (const [v, st, d, ic, col] of [['ANC 1', 'Done', '14 Aug · 12 wks', 'check_circle', 'color/success'], ['ANC 2', 'Done', '20 Sep · 18 wks', 'check_circle', 'color/success'], ['ANC 3', 'Due', 'by 12 Nov · 24–28 wks', 'clock', 'color/warning'], ['ANC 4', 'Upcoming', 'after 28 wks', 'lock', 'color/on-surface-variant']]) {
      const r = AL('HORIZONTAL', v); r.counterAxisAlignItems = 'CENTER'; pad(r, 'spacing/sm', 'spacing/sm'); gap(r, 'spacing/sm'); rad(r, 'radius/md');
      fill(r, st === 'Due' ? 'color/warning-container' : 'color/surface-container-high'); add(sh, r);
      r.appendChild(icon(ic, col, 22));
      const c = AL('VERTICAL'); c.itemSpacing = 2; r.appendChild(c); c.layoutSizingHorizontal = 'FILL';
      c.appendChild(await txt(`${v} · ${st}`, 'Title/Small', 'color/on-surface')); c.appendChild(await txt(d, 'Body/Small', 'color/on-surface-variant'));
      if (st === 'Done') r.appendChild(await txt('View', 'Label/Large', 'color/primary'));
    }
    const go = inst('button', 'Style=Filled, State=Enabled', { Label: 'Start ANC 3', 'Show icon': false }); add(sh, go);
    pinBottom(s, sh);
  }
  // ===== P5 View mode
  {
    const s = phone(sec, 'M2.0-P5 · View mode (ANC 1)', 4, 0); frames.push(s);
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: 'ANC visit 1', Subtitle: 'Sunita Devi · submitted 14 Aug' })); hideAct(bar, true);
    const b = body(s); b.itemSpacing = 12;
    const bn = add(b, kinst('Banner', 'Type=Info', { Title: 'Submitted · view only', Body: 'This visit is saved and synced. Ask your ANM if something needs correcting.' })); setP(bn, { 'Show action': false });
    const c1 = card(b, 'Visit details'); c1.itemSpacing = 0;
    c1.appendChild(await txt('Visit details', 'Title/Medium', 'color/on-surface'));
    for (const [l, v] of [['Date of visit', '14 Aug 2026'], ['Weeks of pregnancy', '12'], ['Weight', '52 kg'], ['Hb', '10.2 g/dl'], ['Blood pressure', '110/70'], ['Danger signs', 'None']]) add(c1, kinst('Info row', null, { Label: l, Value: v }));
    const c2 = card(b, 'Counselling'); c2.itemSpacing = 0;
    c2.appendChild(await txt('Counselling & referral', 'Title/Medium', 'color/on-surface'));
    for (const [l, v] of [['IFA tablets given', '30'], ['Calcium given', 'Yes'], ['Referred', 'No'], ['Next visit due', '20 Sep 2026']]) add(c2, kinst('Info row', null, { Label: l, Value: v }));
    const back = inst('button', 'Style=Outlined, State=Enabled', { Label: 'Back to visits', 'Show icon': false }); add(b, back);
  }
  // ===== P6 Screening (CBAC)
  {
    const s = phone(sec, 'M2.0-P6 · Screening with score (CBAC)', 5, 0); frames.push(s);
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: 'CBAC screening', Subtitle: 'Kamla Devi · 61 yrs' })); hideAct(bar);
    const b = body(s); b.itemSpacing = 12;
    const bn = add(b, kinst('Banner', 'Type=Info', { Title: 'Score updates as you answer', Body: 'A score above 4 means the person should be referred for NCD screening.' })); setP(bn, { 'Show action': false });
    const c = card(b, 'Part A · Risk'); c.itemSpacing = 14;
    c.appendChild(await txt('Part A · Risk factors', 'Title/Medium', 'color/on-surface'));
    const q = async (text, pts, sel) => {
      const g = AL('VERTICAL', 'Question'); g.itemSpacing = 8; add(c, g);
      const t = await txt(text, 'Body/Large', 'color/on-surface'); g.appendChild(t); wrapText(t);
      const r = AL('HORIZONTAL', 'Answer'); r.counterAxisAlignItems = 'CENTER'; gap(r, 'spacing/xs'); add(g, r);
      for (const [i, o] of ['Yes', 'No'].entries()) { const so = kinst('Segmented option', `Selected=${i === sel}`, { Label: o }); r.appendChild(so); so.resize(76, 44); }
      spacer(r); r.appendChild(await txt(sel === 0 ? `+${pts}` : '0', 'Title/Medium', sel === 0 ? 'color/error' : 'color/on-surface-variant'));
    };
    add(c, kinst('Info row', null, { Label: 'Age 60 years or more (auto)', Value: '+3' }));
    await q('Do you smoke or use tobacco?', 1, 0);
    await q('Do you drink alcohol daily?', 1, 1);
    await q('Is the waist more than 80 cm (women)?', 2, 0);
    await q('Less than 150 minutes of physical activity a week?', 1, 0);
    await q('Family history of BP, diabetes or heart disease?', 2, 1);
    const sc = AL('VERTICAL', 'Score bar'); pad(sc, 'spacing/md', 'spacing/sm'); gap(sc, 'spacing/xs'); fill(sc, 'color/surface-container'); add(s, sc);
    sc.strokes = [paint('color/outline-variant')]; sc.strokeTopWeight = 1; sc.strokeBottomWeight = 0; sc.strokeLeftWeight = 0; sc.strokeRightWeight = 0;
    const sr = AL('HORIZONTAL'); sr.counterAxisAlignItems = 'CENTER'; gap(sr, 'spacing/sm'); add(sc, sr);
    const sv = AL('VERTICAL'); sr.appendChild(sv); sv.appendChild(await txt('Risk score', 'Label/Medium', 'color/on-surface-variant')); sv.appendChild(await txt('7', 'Number/Large', 'color/error'));
    const tg = inst('tag', 'Tone=Alert', { Label: 'High risk · refer for NCD screening' }); sr.appendChild(tg);
    const go = inst('button', 'Style=Filled, State=Enabled', { Label: 'Save & refer', 'Show icon': false }); add(sc, go);
  }
  // ===== P7 Meeting form
  {
    const s = phone(sec, 'M2.0-P7 · Meeting / campaign form (VHND)', 6, 0); frames.push(s);
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: 'VHND meeting', Subtitle: 'Sirsi village · November' })); hideAct(bar);
    const b = body(s); b.itemSpacing = 12;
    tf(b, 'Filled', { Label: 'Date of meeting *', Value: '02 Nov 2026' }, 'calendar');
    tf(b, 'Filled', { Label: 'Place *', Value: 'Anganwadi centre' }, 'dropdown');
    await label(b, 'Who attended?');
    const grid = AL('HORIZONTAL', 'Counts'); grid.layoutWrap = 'WRAP'; grid.itemSpacing = 16; grid.counterAxisSpacing = 12; add(b, grid);
    for (const [l, v] of [['Pregnant women', '8'], ['Children < 5', '14'], ['Adolescent girls', '6'], ['Others', '3']]) { const x = kinst('Stepper', null, { Label: l, Value: v }); grid.appendChild(x); }
    await label(b, 'Meeting photos (up to 3)');
    const ph = AL('HORIZONTAL', 'Photos'); gap(ph, 'spacing/sm'); add(b, ph);
    for (const st of ['Captured', 'Captured', 'Empty']) ph.appendChild(kinst('Photo tile', `State=${st}`));
    await label(b, 'Attendance register (photo or PDF)');
    add(b, kinst('File row', 'State=Done', { 'File name': 'attendance_register.jpg' }));
    add(b, kinst('File row', 'State=Uploading', { 'File name': 'vhnd_minutes.pdf' }));
    bottomBar(s, 'Save draft', 'Submit');
  }
  // ===== P8 Dashboard
  {
    const s = phone(sec, 'M2.0-P8 · Metrics dashboard', 7, 0); frames.push(s);
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: 'Dashboard', Subtitle: 'October 2026' })); hideAct(bar, true);
    const b = body(s);
    const ch = AL('HORIZONTAL', 'Months'); gap(ch, 'spacing/xs'); add(b, ch);
    ['October', 'September', 'August'].forEach((l, i) => ch.appendChild(inst('chip', `Selected=${i === 0}`, { Label: l })));
    const g = AL('HORIZONTAL', 'KPIs'); g.layoutWrap = 'WRAP'; g.itemSpacing = 12; g.counterAxisSpacing = 12; add(b, g);
    for (const [n, l, t, ic] of [['64', 'Households visited', 'household', 'home'], ['28', 'ANC visits done', 'maternal', 'pregnant'], ['41', 'Vaccines given', 'immunization', 'vaccine'], ['₹3,450', 'Incentive earned', 'couple', 'wallet']]) {
      const c = AL('VERTICAL', l); fixW(c, 158); pad(c, 'spacing/md', 'spacing/sm'); c.itemSpacing = 4; rad(c, 'radius/lg'); fill(c, `module/${t}/bg`); g.appendChild(c);
      c.appendChild(icon(ic, `module/${t}/fg`, 20)); c.appendChild(await txt(n, 'Number/Medium', `module/${t}/fg`)); const lt = await txt(l, 'Label/Medium', 'color/on-surface'); c.appendChild(lt); wrapText(lt);
    }
    const ccard = card(b, 'Chart'); ccard.appendChild(await txt('Home visits per week', 'Title/Medium', 'color/on-surface'));
    const bars = AL('HORIZONTAL', 'Bars'); bars.counterAxisAlignItems = 'MAX'; bars.primaryAxisAlignItems = 'SPACE_BETWEEN'; add(ccard, bars); bars.resize(bars.width, 120); bars.layoutSizingVertical = 'FIXED'; bars.layoutSizingHorizontal = 'FILL';
    for (const [w, h] of [['W1', 60], ['W2', 92], ['W3', 74], ['W4', 110], ['W5', 40]]) {
      const col = AL('VERTICAL', w); col.counterAxisAlignItems = 'CENTER'; col.itemSpacing = 4; bars.appendChild(col);
      const r = figma.createRectangle(); r.resize(32, h - 16); r.topLeftRadius = 6; r.topRightRadius = 6; r.fills = [paint('color/primary')]; col.appendChild(r);
      col.appendChild(await txt(w, 'Label/Small', 'color/on-surface-variant'));
    }
    const lc = card(b, 'Pending'); lc.itemSpacing = 0; lc.appendChild(await txt('Still to do this month', 'Title/Medium', 'color/on-surface'));
    for (const [l, v] of [['ANC visits', '12'], ['Child vaccinations', '8'], ['HBNC visits', '5'], ['Incentive claim', 'Due 5 Nov']]) add(lc, kinst('Info row', null, { Label: l, Value: v }));
  }
  // ===== States row
  // S1 Empty + offline
  {
    const s = phone(sec, 'M2.0-S1 · Empty list + offline', 0, 1); frames.push(s);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'Eligible couples', Subtitle: 'Sirsi village' }));
    const b = body(s);
    add(b, kinst('Banner', 'Type=Offline', { Title: "You're offline", Body: 'Keep working. Everything is saved on this phone and will sync when internet is back.', Action: 'OK' }));
    const e = kinst('Empty state'); add(b, e, false); b.counterAxisAlignItems = 'CENTER';
  }
  // S2 Loading
  {
    const s = phone(sec, 'M2.0-S2 · Loading', 1, 1); frames.push(s);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'All beneficiaries', Subtitle: 'Loading…' }));
    const b = body(s); b.itemSpacing = 12;
    const dl = card(b, 'First download'); dl.appendChild(await txt('Downloading beneficiaries · 62%', 'Title/Small', 'color/on-surface'));
    const pr = progress(296, 0.62, 'color/primary', 8); dl.appendChild(pr);
    const dt = await txt('Keep the app open. This happens only the first time.', 'Body/Small', 'color/on-surface-variant'); dl.appendChild(dt); wrapText(dt);
    for (let i = 0; i < 4; i++) add(b, kinst('Skeleton card'));
  }
  // helper: form behind overlays
  const formBehind = async (s, title) => {
    add(s, inst('statusBar')); const bar = add(s, inst('topAppBar', 'Type=Inner', { Title: title, Subtitle: "Ramesh Kumar's family" })); hideAct(bar);
    const b = body(s); b.itemSpacing = 12;
    tf(b, 'Filled', { Label: 'First name *', Value: 'Sunita' });
    tf(b, 'Default', { Label: 'Relation with head of family *', Value: 'Select relation' }, 'dropdown');
    tf(b, 'Default', { Label: 'Date of visit *', Value: 'DD / MM / YYYY' }, 'calendar');
    tf(b, 'Filled', { Label: 'Mobile number *', Value: '98765 43210' });
    return b;
  };
  // S3 Option sheet
  {
    const s = phone(sec, 'M2.0-S3 · Select field → option sheet', 2, 1, true); frames.push(s);
    await formBehind(s, 'Register member'); scrim(s);
    const sh = sheet(s, 'Option sheet');
    sh.appendChild(await txt('Relation with head of family', 'Title/Medium', 'color/on-surface'));
    add(sh, inst('searchBar', null, { Placeholder: 'Search relation' }));
    for (const [l, on] of [['Self', false], ['Wife', true], ['Husband', false], ['Son', false], ['Daughter', false], ['Mother', false]]) add(sh, kinst('Radio option', `Selected=${on}`, { Label: l }));
    add(sh, inst('button', 'Style=Filled, State=Enabled', { Label: 'Done', 'Show icon': false }));
    pinBottom(s, sh);
  }
  // S4 Date picker
  {
    const s = phone(sec, 'M2.0-S4 · Date picker', 3, 1, true); frames.push(s);
    await formBehind(s, 'ANC visit 3'); scrim(s);
    const c = kinst('Calendar'); s.appendChild(c); c.layoutPositioning = 'ABSOLUTE'; c.x = 16; c.y = Math.round((800 - c.height) / 2);
  }
  // S5 Dialog
  {
    const s = phone(sec, 'M2.0-S5 · Unsaved changes dialog', 4, 1, true); frames.push(s);
    await formBehind(s, 'Register member'); scrim(s);
    const d = kinst('Dialog'); s.appendChild(d); d.layoutPositioning = 'ABSOLUTE'; d.x = 24; d.y = Math.round((800 - d.height) / 2);
  }
  // S6 Saved + badge progress
  {
    const s = phone(sec, 'M2.0-S6 · Saved + badge progress snackbar', 5, 1, true); frames.push(s);
    add(s, inst('statusBar')); add(s, inst('topAppBar', 'Type=Inner', { Title: 'ANC visits', Subtitle: '42 pregnant women' }));
    add(s, kinst('Tabs', 'Active=1', { 'Count 1': '11', 'Count 2': '31' }));
    const b = body(s); b.itemSpacing = 12;
    recordCard(b, { name: 'Sunita Devi', meta: 'W/o Ramesh · 26 yrs · 24 wks', tag: 'ANC 3 done · today', tone: 'Success', sync: 'Pending' });
    recordCard(b, { name: 'Rekha Yadav', meta: 'W/o Mohan · 31 yrs · 33 wks', tag: 'ANC 4 due · 18 Nov', action: 'Start ANC 4' });
    const sn = kinst('Snackbar', 'Type=Badge'); s.appendChild(sn); sn.layoutPositioning = 'ABSOLUTE'; sn.x = 16; sn.y = 800 - sn.height - 24;
  }
  // Form kit reference board
  {
    const k = AL('VERTICAL', 'M2.0-K · Form kit reference (InputType → component)'); sec.appendChild(k); fixW(k, 800);
    k.x = 120 + 6 * 440; k.y = 160 + 1500; pad(k, 'spacing/xl', 'spacing/xl'); k.itemSpacing = 20; rad(k, 'radius/lg'); fill(k, 'color/surface');
    k.appendChild(await txt('Form kit · one component per InputType', 'Headline/Small', 'color/on-surface'));
    const intro = await txt('Forms are generated from configuration/*Dataset.kt. Each InputType maps to exactly one component below, so every form in the app gets the same look without drawing it screen by screen.', 'Body/Medium', 'color/on-surface-variant'); k.appendChild(intro); wrapText(intro);
    const row = async (type, desc, build) => {
      const r = AL('HORIZONTAL', type); gap(r, 'spacing/xl'); add(k, r);
      r.strokes = [paint('color/outline-variant')]; r.strokeTopWeight = 1; r.strokeBottomWeight = 0; r.strokeLeftWeight = 0; r.strokeRightWeight = 0; r.paddingTop = 16;
      const l = AL('VERTICAL', 'Name'); fixW(l, 220); l.itemSpacing = 4; r.appendChild(l);
      l.appendChild(await txt(type, 'Title/Small', 'color/primary')); const d = await txt(desc, 'Body/Small', 'color/on-surface-variant'); l.appendChild(d); wrapText(d);
      const right = AL('VERTICAL', 'Example'); right.itemSpacing = 8; r.appendChild(right); right.layoutSizingHorizontal = 'FILL';
      await build(right);
    };
    await row('EDIT_TEXT', 'Text field, label above.', async (p) => { tf(p, 'Filled', { Label: 'First name *', Value: 'Sunita' }); });
    await row('DROPDOWN', 'Select field → option sheet (searchable > 8 options).', async (p) => { tf(p, 'Default', { Label: 'Relation with head of family *', Value: 'Select relation' }, 'dropdown'); });
    await row('RADIO', '3–6 options as rows; Yes/No as segmented.', async (p) => { add(p, kinst('Radio option', 'Selected=true', { Label: 'Institutional' })); add(p, kinst('Radio option', 'Selected=false', { Label: 'Home' })); await segRow(p, 'Is she pregnant?', ['Yes', 'No', "Don't know"], 0); });
    await row('CHECKBOXES', 'Multi-select rows.', async (p) => { add(p, kinst('Checkbox option', 'Checked=true', { Label: 'Fever' })); add(p, kinst('Checkbox option', 'Checked=false', { Label: 'Cough for 2+ weeks' })); });
    await row('DATE_PICKER / TIME_PICKER', 'Field with calendar / clock icon; opens dialog with allowed range.', async (p) => { tf(p, 'Filled', { Label: 'LMP date *', Value: '18 May 2026' }, 'calendar'); tf(p, 'Default', { Label: 'Time of birth', Value: 'HH : MM' }, 'clock'); });
    await row('AGE_PICKER', 'Years / Months / Days steppers when DOB is unknown.', async (p) => { const r = AL('HORIZONTAL'); gap(r, 'spacing/xs'); add(p, r); for (const [l, v] of [['Years', '26'], ['Months', '4'], ['Days', '0']]) { const x = kinst('Stepper', null, { Label: l, Value: v }); r.appendChild(x); x.layoutSizingHorizontal = 'FILL'; } });
    await row('NUMBER_PICKER', 'Stepper with − / +.', async (p) => { const x = kinst('Stepper', null, { Label: 'Weight (kg) *', Value: '54' }); p.appendChild(x); });
    await row('TEXT_VIEW', 'Read-only / calculated value.', async (p) => { add(p, kinst('Read-only field')); });
    await row('TEXT_VIEW_PAIR', 'Label : value rows (also view mode).', async (p) => { add(p, kinst('Info row', null, { Label: 'RCH ID', Value: '1234 5678 9012' })); add(p, kinst('Info row', null, { Label: 'Blood group', Value: 'B+' })); });
    await row('HEADLINE', 'Numbered, collapsible form sections.', async (p) => { add(p, kinst('Form section header', 'State=Done', { Title: 'Personal details', Meta: 'Complete' })); add(p, kinst('Form section header', 'State=Open', { Title: 'Pregnancy details', Meta: '2 of 5', Number: '2' })); });
    await row('IMAGE_VIEW', 'Photo capture tile.', async (p) => { const r = AL('HORIZONTAL'); gap(r, 'spacing/sm'); p.appendChild(r); r.appendChild(kinst('Photo tile', 'State=Empty')); r.appendChild(kinst('Photo tile', 'State=Captured')); });
    await row('FILE_UPLOAD / MULTIFILE_UPLOAD', 'One row per file with progress / retry.', async (p) => { for (const st of ['Done', 'Uploading', 'Error']) add(p, kinst('File row', `State=${st}`)); });
    await row('BUTTON', 'Filled = main action, Outlined = secondary.', async (p) => { const r = AL('HORIZONTAL'); gap(r, 'spacing/xs'); p.appendChild(r); r.appendChild(inst('button', 'Style=Filled, State=Enabled', { Label: 'Submit', 'Show icon': false })); r.appendChild(inst('button', 'Style=Outlined, State=Enabled', { Label: 'Save draft', 'Show icon': false })); });
    await row('Conditional fields', 'Fields revealed by an answer are indented with a blue line.', async (p) => { const rv = AL('VERTICAL'); rv.itemSpacing = 8; rv.paddingLeft = 14; add(p, rv); rv.strokes = [paint('color/primary')]; rv.strokeLeftWeight = 3; rv.strokeTopWeight = 0; rv.strokeRightWeight = 0; rv.strokeBottomWeight = 0; add(rv, kinst('Checkbox option', 'Checked=true', { Label: 'Severe headache' })); });
    await row('Validation', 'Error summary at top + field-level message.', async (p) => { add(p, kinst('Banner', 'Type=Error')); });
  }
  // design notes
  await note(sec, 120, 160 + 1500 + 900, 760, 'Patterns P1–P8 and states S1–S6 are reused by every module in phase 2. Each module stage only draws its hub, list, one representative form and unique screens; the rest are generated forms that follow P3 with the form kit.');
  fitSection(sec);
  return ['2.0 kit: ' + kit.length + ' components', '2.0 screens: ' + frames.length];
}
