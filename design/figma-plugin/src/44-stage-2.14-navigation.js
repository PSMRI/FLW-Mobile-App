// ---------------------------------------------------------------------------
// Stage 2.14 · Prototype navigation across all screens
// Adds ON_CLICK reactions: bottom nav, back arrows, tiles, due cards, list items,
// record-card buttons, form primary buttons, FABs, snackbars. Safe to re-run (overwrites reactions).
// Frame keys = text before " · " in the frame name (e.g. "02", "M2.2-05").
// ---------------------------------------------------------------------------
const NAV = {
  bottom: { Today: 'M2.19-13', Modules: 'M2.19-17', Sync: '08', Profile: 'M2.19-19' },
  tiles: {
    'Households': 'M2.19-21', 'All beneficiaries': 'M2.19-31', 'Eligible couples': 'M2.3-01', 'Maternal health': 'M2.2-01', 'Child care': 'M2.4-01',
    'Immunization': 'M2.5-01', 'Disease control': 'M2.8-01', 'HWC referrals': 'M2.8-14', 'High-risk cases': 'M2.6-01', 'General OP care': 'M2.8-16',
    'Death reports': 'M2.9-01', 'Village meetings': 'M2.9-05',
    'Pregnancy registration': 'M2.2-02', 'ANC / pregnancy tracking': 'M2.0-P2', 'ANC visits': 'M2.0-P2', 'PMSMA & high risk': 'M2.2-08', 'PMSMA': 'M2.2-08',
    'Abortion': 'M2.2-14', 'Delivery outcome': 'M2.2-09', 'PNC mothers': 'M2.2-12', 'Infant registration': 'M2.4-27', 'Child registration': 'M2.4-29',
    'HWC referral list': 'M2.8-14', 'HWC referred': 'M2.8-14',
    'EC registration': 'M2.3-02', 'EC tracking': 'M2.3-04',
    'Newborns (0–42 days)': 'M2.4-02', 'Children (3–15 m) · HBYC': 'M2.4-06', 'Children under 5': 'M2.4-08', 'Adolescents (10–19)': 'M2.4-10',
    'Child immunization': 'M2.5-02', 'Mother (Td) immunization': 'M2.5-06',
    'Assess high risk': 'M2.6-03', 'Follow-up of HRP': 'M2.6-04', 'Follow-up of HR cases': 'M2.6-06',
    'High-risk pregnant': 'M2.6-02', 'High-risk non-pregnant': 'M2.6-06', 'Register beneficiary': 'M2.6-08',
    'NCD list (30+)': 'M2.7-02', 'CBAC due': 'M2.7-02', 'Priority (score > 4)': 'M2.7-02', 'TB screening': 'M2.7-25', 'Suspected TB': 'M2.7-27',
    'TB on treatment': 'M2.7-29', 'TPT (preventive)': 'M2.7-10', 'Vulnerable population': 'M2.7-11',
    'General check-up': 'M2.8-09', 'Non-communicable diseases': 'M2.7-01', 'Communicable diseases': 'M2.8-02', 'Malaria': 'M2.8-18', 'Kala-azar': 'M2.8-20',
    'AES / JE': 'M2.8-20', 'Filaria': 'M2.8-20', 'Leprosy': 'M2.8-19', 'Tuberculosis (TB)': 'M2.8-17',
    'Child deaths (CDR)': 'M2.9-13', 'Maternal deaths (MDSR)': 'M2.9-02', 'General deaths': 'M2.9-14', 'Non-maternal deaths': 'M2.9-15',
    'VHND / VHSND': 'M2.9-11', 'VHSNC': 'M2.9-25', 'Maa meeting': 'M2.9-32', 'Saas Bahu Sammelan': 'M2.9-34', 'PHC review meeting': 'M2.9-27',
    'Cluster meetings': 'M2.9-46', 'Survey register': 'M2.9-12', 'U-WIN session': 'M2.9-36', 'Pulse Polio': 'M2.9-38', 'ORS distribution': 'M2.9-41',
    'Filaria MDA': 'M2.9-43', 'Cluster Meeting Forms': 'M2.9-46', 'Deworming day (NDD)': 'M2.9-31', 'AHD': 'M2.9-29',
  },
  due: {
    'Women due for ANC': 'M2.0-P2', 'Children due for vaccination': 'M2.5-02', 'Suspected high-risk pregnancies': 'M2.6-02',
    'Low birth weight babies': 'M2.4-27', 'Missed period': 'M2.19-15', 'No follow-up in 30 days': 'M2.19-16',
  },
  list: {
    'My profile details': 'M2.11-01', 'Incentives': 'M2.11-03', 'Badges & achievements': 'M2.1-02', 'Dashboard': 'M2.0-P8', 'Supervisor': 'M2.12-01',
    'Create ABHA number': 'M2.10-01', 'Get beneficiary data': 'M2.11-06', 'Video tutorials & LMS': 'M2.11-05', 'Language · English': 'M2.11-08',
    'Help & support': 'M2.11-09', 'Log out': 'M2.19-11', 'Sync records': '08', 'About Sakhi': 'M2.11-09',
  },
  // record-card button in a list frame → form
  card: {
    'M2.0-P2': 'M2.2-05', 'M2.0-P4': 'M2.2-05', 'M2.0-S6': 'M2.2-05', 'M2.2-02': 'M2.2-03', 'M2.2-08': 'M2.2-07', 'M2.2-12': 'M2.2-13', 'M2.2-15': 'M2.8-15',
    'M2.3-02': 'M2.3-03', 'M2.3-04': 'M2.3-05', 'M2.3-05': 'M2.3-06', 'M2.4-02': 'M2.4-03', 'M2.4-03': 'M2.4-04', 'M2.4-06': 'M2.4-07', 'M2.4-08': 'M2.4-09',
    'M2.4-10': 'M2.4-11', 'M2.5-02': 'M2.5-04', 'M2.5-03': 'M2.5-04', 'M2.6-02': 'M2.6-04', 'M2.7-02': 'M2.7-03', 'M2.7-10': 'M2.7-09',
    'M2.8-09': 'M2.8-10', 'M2.8-14': 'M2.8-15', 'M2.9-02': 'M2.9-04', 'M2.11-04': 'M2.11-04', 'M2.11-06': '06', 'M2.12-05': '06', 'M2.13-L3': '05',
  },
  // primary button of the bottom action bar (or named button) → next screen
  primary: {
    '01': 'M2.13-04', 'M2.19-01': 'M2.13-04', 'M2.19-54': 'M2.13-04', '07': '05', 'M2.0-P3': 'M2.0-S6', 'M2.0-P5': 'M2.0-P2', 'M2.0-P7': 'M2.9-11',
    'M2.2-03': 'M2.2-04', 'M2.2-04': 'M2.2-05', 'M2.2-05': 'M2.0-S6', 'M2.2-06': 'M2.2-04', 'M2.2-07': 'M2.2-08', 'M2.2-09': 'M2.2-10', 'M2.2-10': 'M2.2-11',
    'M2.2-11': 'M2.2-12', 'M2.2-12': 'M2.2-13', 'M2.2-13': 'M2.2-12', 'M2.2-14': 'M2.3-04',
    'M2.3-03': 'M2.3-04', 'M2.3-05': 'M2.3-06', 'M2.3-06': 'M2.3-04', 'M2.3-07': 'M2.2-03', 'M2.3-09': 'M2.3-04',
    'M2.4-03': 'M2.4-04', 'M2.4-04': 'M2.4-02', 'M2.4-05': 'M2.4-02', 'M2.4-07': 'M2.4-06', 'M2.4-09': 'M2.4-08', 'M2.4-11': 'M2.4-10', 'M2.4-12': 'M2.4-01',
    'M2.5-03': 'M2.5-02', 'M2.5-04': 'M2.5-05', 'M2.5-05': 'M2.5-04', 'M2.5-06': 'M2.5-06',
    'M2.6-03': 'M2.6-05', 'M2.6-04': 'M2.6-02', 'M2.6-05': 'M2.6-02', 'M2.6-06': 'M2.6-01', 'M2.6-08': 'M2.6-03',
    'M2.7-03': 'M2.7-04', 'M2.7-05': 'M2.7-06', 'M2.7-06': 'M2.7-02', 'M2.7-07': 'M2.7-08', 'M2.7-08': 'M2.7-09', 'M2.7-09': 'M2.7-01',
    'M2.8-03': 'M2.8-04', 'M2.8-04': 'M2.8-02', 'M2.8-05': 'M2.8-06', 'M2.8-06': 'M2.8-02', 'M2.8-07': 'M2.8-02', 'M2.8-08': 'M2.8-02',
    'M2.8-10': 'M2.8-13', 'M2.8-11': 'M2.8-13', 'M2.8-13': 'M2.8-14', 'M2.8-15': 'M2.8-14',
    'M2.9-03': 'M2.9-01', 'M2.9-04': 'M2.9-02', 'M2.9-06': 'M2.9-11', 'M2.9-07': 'M2.9-05', 'M2.9-08': 'M2.9-05', 'M2.9-09': 'M2.9-05', 'M2.9-10': 'M2.9-05', 'M2.9-12': 'M2.9-12',
    'M2.10-02': 'M2.10-03', 'M2.10-03': 'M2.10-04', 'M2.10-04': 'M2.10-05', 'M2.10-05': 'M2.10-06', 'M2.10-06': '06', 'M2.10-07': 'M2.10-02',
    'M2.11-01': 'M2.11-02', 'M2.11-02': 'M2.11-01', 'M2.11-03': 'M2.11-03', 'M2.11-04': 'M2.11-03', 'M2.11-08': '09',
    'M2.12-01': 'M2.12-02', 'M2.12-03': 'M2.12-02', 'M2.12-04': 'M2.12-02',
    'M2.13-01': 'M2.13-02', 'M2.13-02': '07', 'M2.13-03': '05', 'M2.13-04': 'M2.19-13', 'M2.13-05': 'M2.19-01',
  },
  // other buttons by label (any frame)
  buttons: {
    'Start ANC 3': 'M2.2-05', 'Record 10-week doses': 'M2.5-05', 'Save & register pregnancy': 'M2.2-03', 'Next: Part B1': 'M2.7-04',
    'Start verifying (15)': 'M2.12-02', 'Reject': 'M2.12-04', 'Create ABHA': 'M2.10-01', 'View details': 'M2.2-04', 'Members': '05', 'Add member': 'M2.13-02',
    'See my badges': 'M2.1-02', 'View all badges': 'M2.1-02', 'Download ABHA card': 'M2.10-06', 'Search': 'M2.11-06', 'Go to households': '04',
    'Register': 'M2.2-03', 'Start PNC day 3': 'M2.2-13', 'Start November visit': 'M2.3-06', 'Start day 3 visit': 'M2.4-04', 'Save Td-2': 'M2.5-06',
    'Show 11 children': 'M2.5-02', 'Save referral': 'M2.4-02', 'Refer & add': 'M2.7-07', 'Continue there': 'M2.8-14', 'Stay here': 'M2.8-10',
    'Record death': 'M2.9-04', 'Try again': 'M2.10-02', 'Share log with support': 'M2.11-09',
    'हिन्दी': 'M2.1-07', 'অসমীয়া': 'M2.1-07', 'फिर से देखें': 'M2.1-07', 'बंद करें': 'M2.1-01', 'Close': 'M2.1-01',
  },
  fab: { '04': 'M2.13-01', '05': 'M2.13-02', '06': 'M2.13-02', 'M2.2-02': 'M2.2-03' },
  // whole-instance taps
  instances: { 'Recap strip': 'M2.1-06', 'My Impact card': 'M2.1-02', 'Badge tile': 'M2.1-03', 'Sync banner': '08' },
  flows: [['M2.19-01', '1 · Login → Today'], ['M2.19-13', '2 · ASHA day (Today)'], ['M2.19-17', '3 · All modules'], ['M2.19-54', '7 · Mitanin variant'], ['M2.12-01', '4 · Supervisor'], ['M2.10-01', '5 · ABHA'], ['M2.1-01', '6 · Gamification']],
};

async function buildStage214(ctx) {
  try { return await buildStage214Inner(ctx); } catch (e) {
    const sp = ctx.pages.screens; await figma.setCurrentPageAsync(sp);
    for (const n of [...sp.children]) if (n.name === 'Navigation report') n.remove();
    const t = figma.createText(); t.name = 'Navigation report'; await t.setTextStyleIdAsync(ctx.TS['Title/Large'].id); t.characters = 'NAV ERROR: ' + (e && e.message) + ' @ ' + String(e && e.stack).split('\n').slice(0, 3).join(' | '); sp.appendChild(t); t.x = -2400; t.y = 0; figma.viewport.scrollAndZoomIntoView([t]); return ['nav error'];
  }
}
async function buildStage214Inner(ctx) {
  const sp = ctx.pages.screens; await figma.setCurrentPageAsync(sp);
  const frames = [];
  for (const n of sp.children) { if (n.type === 'SECTION') { for (const c of n.children) if (c.type === 'FRAME') frames.push(c); } else if (n.type === 'FRAME') frames.push(n); }
  const byKey = {};
  for (const f of frames) { const k = f.name.split(' · ')[0].trim(); if (!byKey[k]) byKey[k] = f; }
  const stats = { set: 0, failed: 0, missingDest: new Set(), failedAt: [] };
  const nav = (destKey) => {
    if (destKey === 'BACK') return [{ type: 'BACK' }];
    const d = byKey[destKey]; if (!d) { stats.missingDest.add(destKey); return null; }
    return [{ type: 'NODE', destinationId: d.id, navigation: 'NAVIGATE', transition: { type: 'SMART_ANIMATE', easing: { type: 'EASE_OUT' }, duration: 0.25 }, preserveScrollPosition: false, resetScrollPosition: true }];
  };
  const link = async (node, destKey) => {
    if (!node || destKey === curKey) return;
    const actions = nav(destKey); if (!actions) return;
    const reactions = [{ trigger: { type: 'ON_CLICK' }, actions }];
    try { if (node.setReactionsAsync) await node.setReactionsAsync(reactions); else node.reactions = reactions; stats.set++; } catch (e) { stats.failed++; stats.failedAt.push((curKey || '?') + ':' + node.name + ' (' + String(e && e.message).slice(0, 60) + ')'); }
  };
  let curKey = '';
  const label = (inst) => { const t = inst.findOne((n) => n.type === 'TEXT' && (n.name === 'Label' || n.name === 'Title')); return t ? t.characters.trim() : ''; };

  for (const f of frames) {
    const key = f.name.split(' · ')[0].trim(); curKey = key;
    const insts = f.findAllWithCriteria({ types: ['INSTANCE'] });
    for (const i of insts) {
      let mc = null; try { mc = await i.getMainComponentAsync(); } catch (e) { mc = null; }
      const vname = mc ? mc.name : '';
      const nm = mc && mc.parent && mc.parent.type === 'COMPONENT_SET' ? mc.parent.name : (mc ? mc.name : i.name);
      if (nm === 'Bottom navigation') { for (const [lab, dest] of Object.entries(NAV.bottom)) { const it = i.findOne((n) => n.name === lab && n.type === 'FRAME'); if (it && dest !== key) await link(it, dest); } }
      else if (nm === 'Top app bar') {
        const lead = i.findOne((n) => n.name === 'Leading'); if (lead && lead.visible) await link(lead, 'BACK');
        {
          const acts = i.findAll((n) => n.name === 'Action' && n.visible);
          const isHome = vname === 'Type=Home';
          if (isHome) { if (acts[0]) await link(acts[0], 'M2.1-10'); if (acts[1]) await link(acts[1], '08'); }
        }
      }
      else if (nm === 'Module tile') { const d = NAV.tiles[label(i)]; if (d) await link(i, d); }
      else if (nm === 'Due card') { const d = NAV.due[label(i)]; if (d) await link(i, d); }
      else if (nm === 'List item') { const l = label(i); const d = NAV.list[l] || NAV.list[Object.keys(NAV.list).find((k) => l.startsWith(k))]; if (d) await link(i, d); }
      else if (nm === 'Snackbar') { if (vname === 'Type=Badge') await link(i, 'M2.1-02'); }
      else if (nm === 'FAB extended') { const d = NAV.fab[key]; if (d) await link(i, d); }
      else if (NAV.instances[nm] && key !== NAV.instances[nm]) await link(i, NAV.instances[nm]);
      else if (nm === 'Record card') { const b = i.findOne((n) => n.type === 'INSTANCE' && n.name === 'Button' && n.visible); const d = NAV.card[key]; if (b && d) await link(b, d); }
      else if (nm === 'Household card') { for (const b of i.findAll((n) => n.type === 'INSTANCE' && n.name === 'Button')) { const d = NAV.buttons[label(b)]; if (d) await link(b, d); } await link(i.findOne((n) => n.name === 'Header'), '05'); }
      else if (nm === 'Beneficiary card') { for (const b of i.findAll((n) => n.type === 'INSTANCE' && n.name === 'Button')) { const d = NAV.buttons[label(b)]; if (d) await link(b, d); } }
      else if (nm === 'Button' && i.parent && i.parent.name !== 'Bottom action bar') {
        // buttons not inside cards/bars
        let p = i.parent, inCard = false; while (p && p !== f) { if (p.type === 'INSTANCE') { inCard = true; break; } p = p.parent; }
        if (!inCard) { const l = label(i); const d = NAV.buttons[l] || (NAV.primary[key] && /Style=Filled/.test(vname) ? NAV.primary[key] : null); if (d) await link(i, d); }
      }
    }
    // bottom action bar
    const bar = f.findOne((n) => n.name === 'Bottom action bar');
    if (bar) {
      const btns = bar.children.filter((n) => n.type === 'INSTANCE');
      for (const b of btns) {
        let bm = null; try { bm = await b.getMainComponentAsync(); } catch (e) { bm = null; }
        const l = label(b); const filled = /Style=Filled/.test(bm ? bm.name : '');
        if (NAV.buttons[l] && !filled) await link(b, NAV.buttons[l]);
        else if (filled) await link(b, NAV.primary[key] || 'BACK');
        else await link(b, 'BACK');
      }
    }
    // dialog buttons inside a "Dialog" instance: secondary = back
    for (const d of f.findAll((n) => n.type === 'INSTANCE' && n.name === 'Dialog')) {
      const bs = d.findAll((n) => n.type === 'INSTANCE' && n.name === 'Button');
      for (const b of bs) { const dd = NAV.buttons[label(b)]; await link(b, dd || 'BACK'); }
    }
  }
  try { sp.flowStartingPoints = NAV.flows.filter(([k]) => byKey[k]).map(([k, name]) => ({ nodeId: byKey[k].id, name })); } catch (e) { /* ignore */ }
  const miss = [...stats.missingDest];
  const msg = `Navigation report: ${stats.set} tap links on ${frames.length} screens, ${stats.failed} failed${miss.length ? ', missing: ' + miss.join(' ') : ''}. Flows: ${NAV.flows.map((f) => f[1]).join(' | ')}. FAILED: ${stats.failedAt.join(' ; ')}`;
  for (const n of [...sp.children]) if (n.name === 'Navigation report') n.remove();
  const rep = figma.createFrame(); rep.name = 'Navigation report'; rep.layoutMode = 'VERTICAL'; rep.primaryAxisSizingMode = 'AUTO'; rep.counterAxisSizingMode = 'FIXED'; rep.resize(1200, 10);
  rep.paddingLeft = rep.paddingRight = rep.paddingTop = rep.paddingBottom = 32; rep.fills = [{ type: 'SOLID', color: { r: 1, g: 0.96, b: 0.76 } }]; rep.cornerRadius = 16;
  rep.clipsContent = false; rep.itemSpacing = 8;
  const lines = [`${stats.set} tap links on ${frames.length} screens · ${stats.failed} failed · missing: ${miss.length ? miss.join(' ') : 'none'}`].concat(stats.failedAt.map((x) => 'FAILED ' + x));
  for (const ln of lines) { const t = figma.createText(); await t.setTextStyleIdAsync(ctx.TS['Title/Medium'].id); t.characters = ln; rep.appendChild(t); }
  sp.appendChild(rep); rep.x = 0; rep.y = -600; figma.viewport.scrollAndZoomIntoView([rep]);
  return [`navigation: ${stats.set} links on ${frames.length} screens, ${stats.failed} failed${miss.length ? ', missing: ' + miss.join(' ') : ''}, ${NAV.flows.length} flows`];
}
