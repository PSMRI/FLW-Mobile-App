// Utility: dumps all variables (resolved) + text styles into a "Token dump" text node (2 columns).
async function buildStageTokens(ctx) {
  const sp = ctx.pages.screens; await figma.setCurrentPageAsync(sp);
  for (const n of [...sp.children]) if (n.name.startsWith('Token dump')) n.remove();
  const hex = (c) => '#' + [c.r, c.g, c.b].map((x) => Math.round(x * 255).toString(16).padStart(2, '0')).join('').toUpperCase();
  const vars = await figma.variables.getLocalVariablesAsync();
  const byId = {}; for (const v of vars) byId[v.id] = v;
  const resolve = (v) => { let val = Object.values(v.valuesByMode)[0]; let g = 0; while (val && val.type === 'VARIABLE_ALIAS' && g++ < 10) { const t = byId[val.id]; val = t ? Object.values(t.valuesByMode)[0] : null; } return val; };
  const lines = [];
  for (const v of vars) { if (v.hiddenFromPublishing && v.resolvedType === 'COLOR' && !v.name.includes('/')) continue; const val = resolve(v); if (val == null) continue; const s = v.resolvedType === 'COLOR' ? hex(val) + (val.a !== undefined && val.a < 1 ? ' a' + val.a.toFixed(2) : '') : String(val); lines.push(v.name + ' = ' + s); }
  const ts = await figma.getLocalTextStylesAsync();
  for (const t of ts) lines.push('TS ' + t.name + ' = ' + t.fontName.family + ' ' + t.fontName.style + ' ' + t.fontSize + '/' + (t.lineHeight.unit === 'PIXELS' ? t.lineHeight.value : t.lineHeight.unit) + ' ls' + (t.letterSpacing.value || 0));
  lines.sort();
  const half = Math.ceil(lines.length / 2); const made = [];
  for (const [i, part] of [[0, lines.slice(0, half)], [1, lines.slice(half)]]) {
    const t = figma.createText(); t.name = 'Token dump ' + i; await t.setTextStyleIdAsync(ctx.TS['Body/Large'].id);
    t.characters = part.join('\n'); sp.appendChild(t); t.x = -9000 + i * 900; t.y = 0; made.push(t);
  }
  figma.viewport.scrollAndZoomIntoView(made);
  return ['tokens: ' + lines.length];
}
