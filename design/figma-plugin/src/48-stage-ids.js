// Utility: writes a "Section IDs" text node listing key frame ids (for share links), then removes nothing else.
const ID_FRAMES = ['M2.19-13', 'M2.19-17', 'M2.19-19', 'M2.19-22', 'M2.19-27', 'M2.19-28', 'M2.19-36', 'M2.19-39', 'M2.19-41', 'M2.19-54',
  'M2.18-P1', 'M2.2-17', 'M2.3-10', 'M2.4-19', 'M2.5-07', 'M2.6-09', 'M2.7-25', 'M2.8-20', 'M2.8-23', 'M2.9-13', 'M2.9-25', 'M2.10-08', 'M2.11-03', 'M2.11-11', 'M2.12-07', 'M2.13-06'];
async function buildStageIds(ctx) {
  const sp = ctx.pages.screens; await figma.setCurrentPageAsync(sp);
  for (const n of [...sp.children]) if (n.name === 'Section IDs') n.remove();
  const byKey = {};
  for (const s of sp.children) if (s.type === 'SECTION') for (const f of s.children) if (f.type === 'FRAME') { const k = f.name.split(' · ')[0].trim(); if (!byKey[k]) byKey[k] = f; }
  const lines = ID_FRAMES.map((k) => (byKey[k] ? byKey[k].id + '  |  ' + byKey[k].name : 'MISSING ' + k));
  const t = figma.createText(); t.name = 'Section IDs'; await t.setTextStyleIdAsync(ctx.TS['Title/Large'].id);
  t.characters = lines.join('\n'); sp.appendChild(t); t.x = -3000; t.y = 0;
  figma.viewport.scrollAndZoomIntoView([t]);
  return ['ids: ' + lines.length];
}
