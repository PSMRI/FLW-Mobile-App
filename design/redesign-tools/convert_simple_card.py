"""Converts a simple old 'record card' (title TextView + label/value TextView pairs + icons + buttons, all data-bound)
into the redesign card. Keeps: <data>, every TextView/ImageView/Button id and binding attribute, clicks. Labels
(static @string TextView followed by a value TextView in the same parent) become Sakhi.InfoLabel/InfoValue cells,
2 per row. Prints ids that were on container views (not carried over) so the caller can check Kotlin references."""
import re, sys, xml.etree.ElementTree as ET
A = '{http://schemas.android.com/apk/res/android}'
APP = '{http://schemas.android.com/apk/res-auto}'
TOOLS = '{http://schemas.android.com/tools}'
DROP = {'layout_width', 'layout_height', 'layout_weight', 'layout_margin', 'layout_marginTop', 'layout_marginBottom',
        'layout_marginStart', 'layout_marginEnd', 'layout_marginHorizontal', 'layout_marginVertical', 'layout_gravity',
        'padding', 'paddingTop', 'paddingBottom', 'paddingStart', 'paddingEnd', 'paddingHorizontal', 'paddingVertical',
        'textAppearance', 'textColor', 'textSize', 'textStyle', 'background', 'backgroundTint', 'drawablePadding',
        'gravity', 'elevation', 'layout_below', 'layout_toEndOf', 'layout_toRightOf', 'layout_alignParentEnd',
        'layout_alignParentTop', 'drawableLeft', 'drawableStart', 'style', 'singleLine', 'maxLines', 'ellipsize'}
DROP_APP = {'drawableLeftCompat', 'drawableTint', 'drawableStartCompat', 'cardCornerRadius', 'iconTint', 'tint'}

def tag(e): return e.tag.split('.')[-1]
def attrs(e, keep_text=True):
    out = []
    for k, v in e.attrib.items():
        if k.startswith(A):
            n = k[len(A):]
            if n in DROP or n.startswith('layout_constraint') or (n == 'text' and not keep_text): continue
            out.append(f'android:{n}="{esc(v)}"')
        elif k.startswith(APP):
            n = k[len(APP):]
            if n in DROP_APP or n.startswith('layout_constraint'): continue
            out.append(f'app:{n}="{esc(v)}"')
        elif k.startswith(TOOLS):
            out.append(f'tools:{k[len(TOOLS):]}="{esc(v)}"')
        elif k == 'style':
            continue
        else:
            out.append(f'{k}="{esc(v)}"')
    return out
def esc(v): return v.replace('&', '&amp;').replace('"', '&quot;').replace('<', '&lt;')
def fmt(lines, ind): return ''.join(f'\n{ind}{l}' for l in lines)

def convert(src_path, dst_path, note):
    raw = open(src_path).read()
    data = raw[raw.index('<data>') - 4:raw.index('</data>') + len('</data>')]
    root = ET.fromstring(raw)
    view = [c for c in root if c.tag != 'data'][0]
    title = None; cells = []; icons = []; buttons = []; extra_text = []; container_ids = []
    used = set()
    def walk(e):
        kids = list(e)
        for i, k in enumerate(kids):
            t = tag(k)
            if id(k) in used: continue
            if t in ('TextView', 'MaterialTextView'):
                txt = k.get(A + 'text', '')
                if txt.startswith('@string/') and i + 1 < len(kids) and tag(kids[i + 1]) in ('TextView', 'MaterialTextView'):
                    cells.append((k, kids[i + 1])); used.add(id(kids[i + 1])); continue
                if title is None and txt.startswith('@{') or (title is None and k.get(A + 'id')):
                    set_title(k); continue
                extra_text.append(k)
            elif t in ('ImageView', 'AppCompatImageView'):
                if k.get(A + 'id'): icons.append(k)
            elif t in ('Button', 'MaterialButton'):
                buttons.append(k)
            else:
                if k.get(A + 'id'): container_ids.append(k.get(A + 'id'))
                walk(k)
    def set_title(k):
        nonlocal title; title = k
    if view.get(A + 'id'): container_ids.append(view.get(A + 'id'))
    walk(view)
    out_cells = ''
    for r in range(0, len(cells), 2):
        pair = cells[r:r + 2]
        body = ''
        for j, (lab, val) in enumerate(pair):
            m = '' if j == 1 or len(pair) == 1 else '\n                        android:layout_marginEnd="@dimen/ds_space_xs"'
            lab_id = f'\n                            android:id="{lab.get(A + "id")}"' if lab.get(A + 'id') else ''
            body += f'''                    <LinearLayout
                        style="@style/Sakhi.InfoCell"{m}>

                        <TextView{lab_id}
                            style="@style/Sakhi.InfoLabel"
                            android:text="{lab.get(A + 'text')}" />

                        <TextView
                            style="@style/Sakhi.InfoValue"{fmt(attrs(val), '                            ')} />
                    </LinearLayout>
'''
        if len(pair) == 1:
            body += '''                    <Space
                        android:layout_width="0dp"
                        android:layout_height="0dp"
                        android:layout_weight="1" />
'''
        out_cells += f'''                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:baselineAligned="false"
                    android:orientation="horizontal">

{body}                </LinearLayout>
'''
    for t in extra_text:
        out_cells += f'''                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="@dimen/ds_space_xs"
                    android:textAppearance="@style/TextAppearance.Sakhi.BodySmall"
                    android:textColor="@color/ds_on_surface_variant"{fmt(attrs(t), '                    ')} />
'''
    icon_x = ''
    for ic in icons:
        icon_x += f'''
                <ImageView
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:layout_marginStart="@dimen/ds_space_xs"{fmt(attrs(ic), '                    ')} />
'''
    btn_x = ''
    for k, bt in enumerate(buttons):
        style = 'Widget.Sakhi.Button.Tonal'
        btn_x += f'''                <com.google.android.material.button.MaterialButton
                    style="@style/{style}"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"{fmt([a.replace('@string/view"', '@string/ds_view"') for a in attrs(bt)], '                    ')} />
'''
    title_x = f'''                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:textAppearance="@style/TextAppearance.Sakhi.TitleMedium"
                    android:textColor="@color/ds_on_surface"{fmt(attrs(title), '                    ')} />
''' if title is not None else ''
    first_id = container_ids[0] if container_ids else None
    card_id = f'\n        android:id="{first_id}"' if first_id else ''
    xml = f'''<?xml version="1.0" encoding="utf-8"?>
<!--
{note}
    Generated by design/redesign-tools/convert_simple_card.py (redesign card: title + icons, label/value grid, tonal action).
    Every TextView / ImageView / Button id, binding and click is kept. Old layout: legacy_{dst_path.split('/')[-1]}.
-->
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    {data}

    <com.google.android.material.card.MaterialCardView{card_id}
        style="@style/Widget.Sakhi.Card"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="@dimen/ds_space_md"
        android:layout_marginTop="@dimen/ds_space_sm">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingHorizontal="@dimen/ds_space_md"
            android:paddingTop="@dimen/ds_space_sm"
            android:paddingBottom="@dimen/ds_space_md">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:minHeight="44dp"
                android:orientation="horizontal">

{title_x}{icon_x}            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">

{out_cells}            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="@dimen/ds_space_sm"
                android:divider="@drawable/ds_gap_8"
                android:gravity="end"
                android:orientation="horizontal"
                android:showDividers="middle">

{btn_x}            </LinearLayout>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
</layout>
'''
    old_ids = set(re.findall(r'@\+id/(\w+)', raw)); new_ids = set(re.findall(r'@\+id/(\w+)', xml))
    open(dst_path, 'w').write(xml)
    return sorted(old_ids - new_ids), len(cells), len(buttons), len(icons), len(extra_text)

if __name__ == '__main__':
    import shutil
    L = '../../app/src/main/res/layout/'
    for name in sys.argv[1:]:
        import os
        if not os.path.exists(L + 'legacy_' + name + '.xml'):
            shutil.copy(L + name + '.xml', L + 'legacy_' + name + '.xml')
        print(name, convert(L + 'legacy_' + name + '.xml', L + name + '.xml', f'    {name} · redesign.'))
