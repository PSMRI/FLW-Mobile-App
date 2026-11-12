// Utility: dumps a developer spec (geometry, padding, gap, radius, fills, strokes, fonts) of SPEC_FRAME into a text node.
const SPEC_FRAME = 'M2.2-02 ·';
async function buildStageSpec(ctx) {
  const sp = ctx.pages.screens; await figma.setCurrentPageAsync(sp);
  for (const n of [...sp.children]) if (n.name.startsWith('Spec dump')) n.remove();
  let target = null;
  for (const s of sp.children) { if (s.type === 'SECTION') { for (const f of s.children) if (f.name.startsWith(SPEC_FRAME)) target = f; } else if (s.name.startsWith(SPEC_FRAME)) target = s; }
  if (!target) throw new Error('frame not found ' + SPEC_FRAME);
  const hex = (p) => { if (!p || p.type !== 'SOLID') return p ? p.type : ''; const c = p.color; return '#' + [c.r, c.g, c.b].map((x) => Math.round(x * 255).toString(16).padStart(2, '0')).join('').toUpperCase() + (p.opacity !== undefined && p.opacity < 1 ? '@' + p.opacity.toFixed(2) : ''); };
  const lines = []; const fx = target.absoluteTransform[0][2], fy = target.absoluteTransform[1][2];
  const walk = (n, d) => {
    if (!n.visible) return;
    if (d > 10) return;
    const x = Math.round(n.absoluteTransform[0][2] - fx), y = Math.round(n.absoluteTransform[1][2] - fy);
    let l = '  '.repeat(d) + n.type[0] + ' ' + n.name.slice(0, 28) + ` [${x},${y} ${Math.round(n.width)}x${Math.round(n.height)}]`;
    if ('fills' in n && Array.isArray(n.fills) && n.fills.length && n.fills[0].visible !== false) l += ' bg' + hex(n.fills[0]);
    if ('strokes' in n && n.strokes.length && n.strokeWeight) l += ' st' + hex(n.strokes[0]) + '/' + (typeof n.strokeWeight === 'number' ? n.strokeWeight : 'mix');
    if ('cornerRadius' in n && n.cornerRadius && n.cornerRadius !== figma.mixed) l += ' r' + n.cornerRadius;
    if ('layoutMode' in n && n.layoutMode !== 'NONE') l += ` ${n.layoutMode[0]} p${n.paddingTop},${n.paddingRight},${n.paddingBottom},${n.paddingLeft} g${n.itemSpacing}`;
    if ('layoutSizingHorizontal' in n) l += ` sz${n.layoutSizingHorizontal[0]}${n.layoutSizingVertical[0]}`;
    if ('layoutMode' in n && n.layoutMode !== 'NONE') l += ` al${n.primaryAxisAlignItems[0]}${n.counterAxisAlignItems[0]}`;
    if (n.type === 'TEXT') { const fn = n.fontName === figma.mixed ? 'mix' : n.fontName.style; l += ` "${n.characters.slice(0, 40).replace(/\n/g, ' ')}" ${n.fontSize}/${n.lineHeight && n.lineHeight.unit === 'PIXELS' ? n.lineHeight.value : 'auto'} ${fn}`; }
    lines.push(l);
    if ('children' in n && n.type !== 'TEXT') for (const c of n.children) walk(c, d + 1);
  };
  walk(target, 0);
  const cols = 2, per = Math.ceil(lines.length / cols); const made = [];
  for (let i = 0; i < cols; i++) {
    const t = figma.createText(); t.name = 'Spec dump ' + i; await t.setTextStyleIdAsync(ctx.TS['Body/Small'].id);
    t.characters = lines.slice(i * per, (i + 1) * per).join('\n') || ' '; sp.appendChild(t); t.x = -16000 + i * 1100; t.y = 0; made.push(t);
  }
  figma.viewport.scrollAndZoomIntoView(made);
  const sent = await sendSpecToLocal('FRAME ' + target.name + ' ' + Math.round(target.width) + 'x' + Math.round(target.height) + '\n' + lines.join('\n'));
  return ['spec lines: ' + lines.length + (sent ? ' (sent to localhost)' : ' (local receiver not running)')];
}

// Dev only: posts the spec text to http://localhost:8765 (receiver: design/figma-plugin/spec_receiver.py) so it can be read exactly.
function sendSpecToLocal(text) {
  return new Promise((resolve) => {
    figma.showUI(`<script>onmessage=(e)=>{const t=e.data.pluginMessage;fetch('http://localhost:8765/spec',{method:'POST',body:t}).then(()=>parent.postMessage({pluginMessage:'ok'},'*')).catch(()=>parent.postMessage({pluginMessage:'fail'},'*'));}</script>`, { visible: false });
    const timer = setTimeout(() => resolve(false), 5000);
    figma.ui.onmessage = (m) => { clearTimeout(timer); resolve(m === 'ok'); };
    figma.ui.postMessage(text);
  });
}
