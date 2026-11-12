#!/usr/bin/env python3
"""Extract every FormElement from configuration/*Dataset.kt into forms.json.
For each dataset: fields in declaration order with title (English strings.xml), inputType, required,
options, and whether the field is in the initial page list (setUpPage) or only revealed later (conditional).
Usage: python3 extract_forms.py <repo_root> <out.json>
"""
import json, re, sys, os, glob, html

root, out = sys.argv[1], sys.argv[2]
cfg = os.path.join(root, 'app/src/main/java/org/piramalswasthya/sakhi/configuration')
res = os.path.join(root, 'app/src/main/res/values')

strings, arrays = {}, {}
for f in glob.glob(os.path.join(res, '*.xml')):
    t = open(f, encoding='utf-8').read()
    for m in re.finditer(r'<string name="([^"]+)"[^>]*>(.*?)</string>', t, re.S):
        strings.setdefault(m.group(1), html.unescape(re.sub(r'\s+', ' ', m.group(2))).strip().strip('"').replace("\\'", "'"))
    for m in re.finditer(r'<(?:string-)?array name="([^"]+)"[^>]*>(.*?)</(?:string-)?array>', t, re.S):
        items = []
        for it in re.findall(r'<item>(.*?)</item>', m.group(2), re.S):
            it = it.strip()
            r = re.match(r'@string/(\w+)', it)
            items.append(strings.get(r.group(1), it) if r else html.unescape(it).replace("\\'", "'"))
        arrays.setdefault(m.group(1), items)

def balanced(src, start):
    """return text inside parentheses starting at src[start]=='('"""
    depth, i = 0, start
    while i < len(src):
        c = src[i]
        if c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                return src[start + 1:i]
        elif c == '"':
            i += 1
            while i < len(src) and src[i] != '"':
                if src[i] == '\\': i += 1
                i += 1
        i += 1
    return src[start + 1:]

def resolve_title(body, name):
    m = re.search(r'title\s*=\s*\w+\.getString\(\s*R\.string\.(\w+)', body)
    if m: return strings.get(m.group(1), m.group(1))
    m = re.search(r'title\s*=\s*"([^"]*)"', body)
    if m: return m.group(1)
    return name

def resolve_entries(body):
    m = re.search(r'entries\s*=\s*\w+\.getStringArray\(\s*R\.array\.(\w+)', body)
    if m: return arrays.get(m.group(1), [m.group(1)])
    m = re.search(r'arrayId\s*=\s*R\.array\.(\w+)', body)
    if m: return arrays.get(m.group(1), [m.group(1)])
    m = re.search(r'entries\s*=\s*arrayOf\((.*?)\)', body, re.S)
    if m:
        out = []
        for part in re.findall(r'"([^"]*)"|R\.string\.(\w+)', balanced(body, body.find('arrayOf(', m.start()) + 7)):
            out.append(part[0] or strings.get(part[1], part[1]))
        return out
    return None

data = []
for f in sorted(glob.glob(os.path.join(cfg, '*Dataset.kt'))):
    name = os.path.basename(f)[:-3]
    if name == 'Dataset': continue
    src = open(f, encoding='utf-8').read()
    fields, order = [], {}
    for m in re.finditer(r'(?:private\s+|internal\s+)?va[lr]\s+(\w+)\s*=\s*FormInput(?:Old)?\s*\(|(?:private\s+|internal\s+)?va[lr]\s+(\w+)\s*=\s*FormElement\s*\(', src):
        body = balanced(src, m.end() - 1)
        it = re.search(r'inputType\s*=\s*(?:[\w.]*\.)?([A-Z_]{4,})', body)
        req = re.search(r'required\s*=\s*(true|false)', body)
        f_ = {
            'var': m.group(1) or m.group(2),
            'title': resolve_title(body, m.group(1) or m.group(2)),
            'type': it.group(1) if it else '?',
            'required': bool(req and req.group(1) == 'true'),
        }
        ent = resolve_entries(body)
        if ent: f_['options'] = ent
        fields.append(f_)
    # initial page = every field referenced in the body of (non-commented) setUpPage functions, up to setUpPage(list)
    initial = set()
    clean = re.sub(r'//[^\n]*', '', src)
    for m in re.finditer(r'fun\s+setUpPage\s*\(', clean):
        start = m.end()
        end = clean.find('setUpPage(list', start)
        if end == -1: end = clean.find('setUpPage(', start)
        if end == -1: end = min(len(clean), start + 20000)
        body = clean[start:end]
        lm = re.search(r'(?:mutableListOf|listOf|arrayListOf)\s*(?:<[^>]*>)?\s*\(', body)
        if lm:
            first = balanced(body, lm.end() - 1)
            initial.update(x['var'] for x in fields if re.search(r'\b' + re.escape(x['var']) + r'\b', first))
        # unconditional / flavour-only additions: list.add(x) not nested in value checks
        for am in re.finditer(r'list\.(?:add|addAll)\s*\(', body):
            ctx = body[max(0, am.start() - 250):am.start()]
            last_if = ctx.rfind('if (')
            cond = ctx[last_if:] if last_if != -1 else ''
            if last_if == -1 or 'FLAVOR' in cond or 'BuildConfig' in cond:
                arg = balanced(body, am.end() - 1)
                initial.update(x['var'] for x in fields if re.search(r'\b' + re.escape(x['var']) + r'\b', arg))
    if not initial:
        initial = {x['var'] for x in fields}
    for x in fields:
        x['shown'] = 'always' if x['var'] in initial else 'conditional'
    # trigger sources: element that reveals others
    for x in fields:
        x['revealsOthers'] = bool(re.search(r'triggerDependants\w*\(\s*(?:source\s*=\s*)?' + re.escape(x['var']) + r'\b', src))
    data.append({'dataset': name, 'fields': fields})

json.dump(data, open(out, 'w'), ensure_ascii=False, indent=1)
tot = sum(len(d['fields']) for d in data)
print(f'{len(data)} datasets, {tot} fields -> {out}')
