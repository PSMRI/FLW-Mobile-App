#!/usr/bin/env python3
"""Extract label + bound-value pairs and buttons from list-card layouts (res/layout/rv_item_*.xml).
Usage: python3 extract_cards.py <repo_root> <out.json>"""
import json, re, sys, os, glob, html
root, out = sys.argv[1], sys.argv[2]
res = os.path.join(root, 'app/src/main/res')
strings = {}
for f in glob.glob(os.path.join(res, 'values', '*.xml')):
    for m in re.finditer(r'<string name="([^"]+)"[^>]*>(.*?)</string>', open(f, encoding='utf-8').read(), re.S):
        strings.setdefault(m.group(1), html.unescape(re.sub(r'\s+', ' ', m.group(2))).strip().strip('"').replace("\\'", "'"))
cards = []
for f in sorted(glob.glob(os.path.join(res, 'layout', 'rv_item_*.xml'))):
    name = os.path.basename(f)[:-4]
    if name.startswith('rv_item_form_'): continue
    t = open(f, encoding='utf-8').read()
    items = []
    for m in re.finditer(r'<([\w.]+)\b([^>]*?)/?>', t, re.S):
        tag, attrs = m.group(1).split('.')[-1], m.group(2)
        txt = re.search(r'android:text="([^"]*)"', attrs)
        vis = re.search(r'android:visibility="([^"]*)"', attrs)
        idm = re.search(r'android:id="@\+id/(\w+)"', attrs)
        if not txt: continue
        v = txt.group(1)
        if v.startswith('@string/'):
            kind, val = 'label', strings.get(v[8:], v[8:])
        elif v.startswith('@{'):
            kind, val = 'value', re.sub(r'\s+', ' ', v[2:-1])
        else:
            kind, val = 'label', v
        if 'Button' in tag: kind = 'button'
        items.append({'kind': kind, 'text': val, 'id': idm.group(1) if idm else None,
                      'hidden_by_default': bool(vis and 'gone' in vis.group(1))})
    if items:
        cards.append({'layout': name, 'items': items})
json.dump(cards, open(out, 'w'), ensure_ascii=False, indent=1)
print(f'{len(cards)} card layouts -> {out}')
