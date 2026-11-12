// ---------------------------------------------------------------------------
// Stage 2.18 · Gap-fix patterns (reference for the patterns in 22-gap-kit.js)
// ---------------------------------------------------------------------------
async function buildStage218(ctx) {
  await ensureIcons(ctx); await kitFixes(ctx); await ensureSearchMic(ctx);
  const G = GK(ctx);
  const { phone, body, screenTop, stageSection, actionCard, dialog, visitSheet, sortSheet, sourceSheet, searchRow, viewMode, blockedAdd, uploads, table, rules, finish, banner, tag, caption, add, AL } = G;
  await figma.setCurrentPageAsync(ctx.pages.screens);
  const sec = stageSection('2.18 · Gap-fix patterns (shared)');
  const frames = []; const rowMap = new Map();
  const F = (name, col, row, fixed) => { const s = phone(sec, name, col, row, fixed); frames.push(s); rowMap.set(s, row); return s; };

  // P1 list with all card states
  {
    const s = F('M2.18-P1 · List card states (normal / deceased / deleted)', 0, 0);
    screenTop(s, 'Household members', 'HH 1023 · Ramesh Kumar');
    const b = body(s); b.itemSpacing = 12;
    searchRow(b, 'Search name, mobile or ID', { sort: true, filter: true });
    await actionCard(b, { name: 'Sunita Devi', meta: 'Wife · F · 34 yrs · Ben ID 1203 4456', sync: 'Synced', tags: [['Maternal', 'Pregnant · 24 wks']], rows: [['Mobile', '98XXXXXX21'], ['ABHA', 'Linked']], icons: [['call', 'Call'], ['id_card', 'ABHA'], ['eye_surgery', 'Cataract', 'color/warning'], ['delete', 'Delete', 'color/error']], actions: [['Tonal', 'Edit']] });
    await actionCard(b, { name: 'Ram Prasad', meta: 'Father · M · 71 yrs', state: 'deceased', stateNote: 'Deceased · 02 Oct 2026', icons: [['call', 'Call'], ['delete', 'Delete', 'color/error']] });
    await actionCard(b, { name: 'Mohan Kumar', meta: 'Son · M · 12 yrs', state: 'deleted', icons: [['call', 'Call']] });
    rules(s, ['Delete icon: hidden for dead / already-deleted members', 'Head of family: cannot delete while other members exist (toast)', 'Deleted / deceased cards: greyed, all actions blocked']);
  }
  // P2 delete confirm
  {
    const s = F('M2.18-P2 · Destructive confirm dialog', 1, 0, true);
    screenTop(s, 'Household members', 'HH 1023');
    await dialog(s, { ic: 'delete', title: 'Delete Sunita Devi?', body: 'The member is removed from this household and all modules. This cannot be undone.', ok: 'Delete', danger: true });
  }
  // P3 visit sheet
  {
    const s = F('M2.18-P3 · Visits sheet (1…n + add)', 2, 0, true);
    screenTop(s, 'Children under 5', '34 children');
    await visitSheet(s, { title: 'SAM visits', subtitle: 'Aarav Kumar · 2 yrs 3 mths', visits: [{ label: 'Visit 1', date: '04 Aug 2026', status: 'Referred to NRC', tone: 'Alert', action: 'View' }, { label: 'Visit 2', date: '06 Sep 2026', status: 'Follow-up', tone: 'Child', action: 'View' }], add: { label: 'Add new visit', disabled: true, reason: 'Already filled this month · next on 1 Dec' } });
  }
  // P4 view mode + uploads + table
  {
    const s = F('M2.18-P4 · View mode, uploads, history table', 3, 0);
    screenTop(s, 'IFA distribution', 'Sunita Devi · 34 yrs', 'more');
    const b = body(s); b.itemSpacing = 12;
    await viewMode(b, { saved: 'Saved 12 Oct 2026', editable: true });
    await table(b, 'Bottles given', ['Date', 'Bottles', 'By'], [['12 Aug', '1', 'ASHA'], ['10 Sep', '1', 'ASHA'], ['12 Oct', '2', 'ASHA']]);
    await uploads(b, 'Supporting documents (max 4)', [['IFA_slip_oct.jpg', 'Done']]);
    await blockedAdd(b, 'Add new', 'Once per month · next on 1 Nov 2026');
  }
  // P5 sort + source sheet
  {
    const s = F('M2.18-P5 · Sort sheet', 4, 0, true);
    screenTop(s, 'ANC visits', '42 pregnant women');
    await sortSheet(s);
  }
  {
    const s = F('M2.18-P6 · Add file source', 5, 0, true);
    screenTop(s, 'Delivery outcome', 'Sunita Devi');
    await sourceSheet(s);
  }
  await finish(sec, frames, (f) => rowMap.get(f));
  return ['2.18 patterns: ' + frames.length];
}
