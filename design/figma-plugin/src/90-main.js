// ---------------------------------------------------------------------------
// Command dispatcher
// ---------------------------------------------------------------------------
// Stages built by "Phase 2: build current stage(s)". Change this list to move on
// (no manifest change / re-import needed).
const CURRENT_STAGES = ['spec'];

// "Phase 2: show current stage(s)" zooms to frames whose names start with these prefixes (empty = whole sections).
const REVIEW_FOCUS = ['M2.19-01 ·'];

const STAGES = {
  '2.0': { name: '2.0 · Patterns, form kit & states', build: (ctx) => buildStage20(ctx) },
  '2.1': { name: '2.1 · Gamification', build: (ctx) => buildStage21(ctx) },
  '2.2': { name: '2.2 · Maternal health', build: (ctx) => buildStage22(ctx) },
  '2.3': { name: '2.3 · Eligible couple', build: (ctx) => buildStage23(ctx) },
  '2.4': { name: '2.4 · Child care & infant', build: (ctx) => buildStage24(ctx) },
  '2.5': { name: '2.5 · Immunization', build: (ctx) => buildStage25(ctx) },
  '2.6': { name: '2.6 · High risk & CHO', build: (ctx) => buildStage26(ctx) },
  '2.7': { name: '2.7 · NCD & TB', build: (ctx) => buildStage27(ctx) },
  '2.8': { name: '2.8 · Disease control & check-up', build: (ctx) => buildStage28(ctx) },
  '2.9': { name: '2.9 · Death reports & village forms', build: (ctx) => buildStage29(ctx) },
  '2.10': { name: '2.10 · ABHA', build: (ctx) => buildStage210(ctx) },
  '2.11': { name: '2.11 · Profile, incentives & utilities', build: (ctx) => buildStage211(ctx) },
  '2.12': { name: '2.12 · Supervisor', build: (ctx) => buildStage212(ctx) },
  '2.13': { name: '2.13 · Remaining screens, languages & prototype', build: (ctx) => buildStage213(ctx) },
  '2.14': { name: '2.14 · Navigation', build: (ctx) => buildStage214(ctx) },
  '2.15': { name: '2.15 · Complete form specs A–M (every field)', build: (ctx) => buildStage215(ctx) },
  '2.16': { name: '2.16 · Complete form specs N–Z (every field)', build: (ctx) => buildStage216(ctx) },
  '2.17': { name: '2.17 · Complete card specs (every list card)', build: (ctx) => buildStage217(ctx) },
  '2.18': { name: '2.18 · Gap-fix patterns (shared)', build: (ctx) => buildStage218(ctx) },
  '2.19': { name: '2.19 · Core screens v2 (login, home, household, beneficiary)', build: (ctx) => buildStage219(ctx) },
  'ids': { name: 'Section IDs', build: (ctx) => buildStageIds(ctx) },
  'tokens': { name: 'Token dump', build: (ctx) => buildStageTokens(ctx) },
  'spec': { name: 'Spec dump', build: (ctx) => buildStageSpec(ctx) },
};

async function main() {
  const ctx = await loadContext();
  const cmd = figma.command || 'all';

  if (cmd === 'review-screens' || cmd === 'review-all') {
    const sp = ctx.pages.screens; await figma.setCurrentPageAsync(sp);
    const nodes = sp.children.filter((n) => cmd === 'review-all' || SCREEN_PREFIXES.some((p) => n.name.startsWith(p)));
    figma.currentPage.selection = [];
    figma.viewport.scrollAndZoomIntoView(nodes);
    return 'Showing ' + nodes.length + ' screens';
  }

  if (cmd === 'stage-current' || cmd === 'stage-review') {
    const done = [];
    for (const id of CURRENT_STAGES) {
      const st = STAGES[id];
      if (!st) throw new Error('Stage ' + id + ' is not implemented yet');
      if (cmd === 'stage-current') done.push(...(await st.build(ctx)));
    }
    const sp = ctx.pages.screens; await figma.setCurrentPageAsync(sp);
    const secs = sp.children.filter((n) => CURRENT_STAGES.some((id) => n.name === STAGES[id].name));
    let focus = secs;
    if (cmd === 'stage-review' && REVIEW_FOCUS.length) {
      focus = [];
      for (const sec of secs) for (const c of sec.children) if (REVIEW_FOCUS.some((p) => c.name.startsWith(p))) focus.push(c);
    }
    figma.currentPage.selection = [];
    if (focus.length && !CURRENT_STAGES.includes('2.14')) figma.viewport.scrollAndZoomIntoView(focus);
    return cmd === 'stage-current' ? '✅ Built: ' + done.join(', ') : 'Showing stages ' + CURRENT_STAGES.join(', ');
  }

  const done = [];
  if (cmd === 'screens' || cmd === 'all') done.push(...(await buildScreens(ctx)));
  if (cmd === 'foundations' || cmd === 'all') done.push(...(await buildFoundations(ctx)));
  return '✅ Built: ' + done.join(', ');
}

main()
  .then((msg) => figma.closePlugin(msg))
  .catch(async (e) => { if (CURRENT_STAGES.includes('spec')) await sendSpecToLocal('ERROR ' + (e && e.message ? e.message : String(e))); figma.closePlugin('❌ FLW builder error: ' + (e && e.message ? e.message : String(e)) + (e && e.stack ? ' @ ' + String(e.stack).split('\n')[1] : '')); });
