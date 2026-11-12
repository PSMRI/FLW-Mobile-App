"""Generates a redesign list card (Figma card pattern) for an existing rv_item layout, keeping its data block and ids."""
import re

def cell(label, value_attr, extra_id=None, end=False, cell_attrs=''):
    idattr = f'\n                        android:id="@+id/{extra_id}"' if extra_id else ''
    idattr += cell_attrs
    margin = '' if end else '\n                        android:layout_marginEnd="@dimen/ds_space_xs"'
    return f'''                    <LinearLayout{idattr}
                        style="@style/Sakhi.InfoCell"{margin}>

                        <TextView
                            style="@style/Sakhi.InfoLabel"
                            android:text="{label}" />

                        <TextView
                            style="@style/Sakhi.InfoValue"
                            {value_attr} />
                    </LinearLayout>
'''

def _pairs(cells):
    """Two cells per row; a cell with solo=True takes a row on its own."""
    i = 0
    while i < len(cells):
        if cells[i].get('solo') or i + 1 >= len(cells) or cells[i + 1].get('solo'):
            yield cells[i:i+1]; i += 1
        else:
            yield cells[i:i+2]; i += 2

def rows(cells):
    out = ''
    for pair in _pairs(cells):
        rid = pair[0].get('row_id')
        rattr = f'\n                    android:id="@+id/{rid}"' if rid else ''
        rattr += pair[0].get('row_attrs', '')
        body = cell(pair[0]['label'], pair[0]['value'], pair[0].get('id'), cell_attrs=pair[0].get('cell_attrs', ''))
        if len(pair) > 1:
            body += cell(pair[1]['label'], pair[1]['value'], pair[1].get('id'), end=True, cell_attrs=pair[1].get('cell_attrs', ''))
        else:
            body += '''                    <Space
                        android:layout_width="0dp"
                        android:layout_height="0dp"
                        android:layout_weight="1" />
'''
        out += f'''                <LinearLayout{rattr}
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:baselineAligned="false"
                    android:orientation="horizontal">

{body}                </LinearLayout>
'''
    return out

def button(b):
    style = {'filled': 'Widget.Sakhi.Button', 'tonal': 'Widget.Sakhi.Button.Tonal', 'outlined': 'Widget.Sakhi.Button.Outlined'}[b.get('kind', 'filled')]
    extra = ''.join(f'\n                    {a}' for a in b.get('attrs', []))
    return f'''                <com.google.android.material.button.MaterialButton
                    android:id="@+id/{b['id']}"
                    style="@style/{style}"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:maxLines="1"
                    android:ellipsize="end"
                    android:onClick="{b['onClick']}"
                    android:text="{b['text']}"{extra} />
'''

def card(legacy_src, note, ids, name_expr, cells, buttons, sync_expr=None, tags='', header_extra=''):
    data = legacy_src[legacy_src.index('    <data>'):legacy_src.index('</data>') + len('</data>')]
    if 'import type="android.view.View"' not in data:
        data = data.replace('    <data>', '    <data>\n        <import type="android.view.View" />', 1)
    sync = f'''
                <TextView
                    android:id="@+id/{ids['sync']}"
                    style="@style/Sakhi.Badge"
                    android:layout_marginStart="@dimen/ds_space_xs"
                    android:contentDescription="@string/iv_sync_state_tv"
                    dsSyncBadge="{sync_expr}"
                    tools:text="Synced" />
''' if sync_expr else ''
    btns = ''.join(button(b) for b in buttons)
    return f'''<?xml version="1.0" encoding="utf-8"?>
<!--
{note}
-->
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

{data}

    <com.google.android.material.card.MaterialCardView
        android:id="@+id/{ids['card']}"
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
                android:id="@+id/{ids['title_bar']}"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <FrameLayout
                    android:layout_width="44dp"
                    android:layout_height="44dp"
                    android:background="@drawable/bg_ds_pill"
                    android:backgroundTint="@color/ds_primary_container">

                    <TextView
                        dsInitials="{name_expr}"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center"
                        android:importantForAccessibility="no"
                        android:textAppearance="@style/TextAppearance.Sakhi.TitleSmall"
                        android:textColor="@color/ds_on_primary_container" />
                </FrameLayout>

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="@dimen/ds_space_sm"
                    android:layout_weight="1"
                    android:orientation="vertical">

                    <TextView
                        android:id="@+id/{ids['name']}"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:ellipsize="end"
                        android:maxLines="1"
                        android:text="{name_expr}"
                        android:textAppearance="@style/TextAppearance.Sakhi.TitleMedium"
                        android:textColor="@color/ds_on_surface" />

                    <LinearLayout
                        android:id="@+id/{ids['meta_row']}"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="2dp"
                        android:orientation="horizontal">

                        <TextView
                            android:id="@+id/age"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:ellipsize="end"
                            android:maxLines="2"
                            android:textAppearance="@style/TextAppearance.Sakhi.BodySmall"
                            android:textColor="@color/ds_on_surface_variant" />
                    </LinearLayout>
                </LinearLayout>
{header_extra}{sync}            </LinearLayout>
{tags}
            <LinearLayout
                android:id="@+id/{ids['values']}"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">

{rows(cells)}            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="@dimen/ds_space_sm"
                android:divider="@drawable/ds_gap_8"
                android:orientation="horizontal"
                android:showDividers="middle">

{btns}            </LinearLayout>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
</layout>
'''

def ids_of(s):
    return set(re.findall(r'@\+id/(\w+)', s))
