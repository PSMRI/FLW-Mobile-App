"""Regenerates the TB-family list cards (screening / suspected / confirmed / vulnerable / TPT) from their legacy_ layouts.
Cell order puts Nikshay ID next to Gender (the TB adapters always hide RCH ID and show Nikshay ID); hidden-by-default
cells (RCH, father / husband / spouse) get their own rows so a hidden one never leaves a half-empty row."""
import sys; sys.path.insert(0, '.')
from card import card, ids_of
L = '../../app/src/main/res/layout/'
b = 'benWithTb'
BTNROW = '''            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="@dimen/ds_space_sm"
                android:divider="@drawable/ds_gap_8"'''
INNER = '''        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingHorizontal="@dimen/ds_space_md"'''

def cells(rows):
    return [
        {'label': '@string/beneficiary_id', 'value': f'dsBenIdText="@{{{b}.ben.benId}}"', 'row_id': rows[0]},
        {'label': '@string/mobile_number', 'value': f'android:text="@{{{b}.ben.mobileNo}}"'},
        {'label': '@string/nikshay_id', 'value': f'android:text="@{{({b}.tbSuspected == null || {b}.tbSuspected.nikshayId == null || {b}.tbSuspected.nikshayId.empty) ? @string/ds_empty_value : {b}.tbSuspected.nikshayId}}"', 'id': 'nikshayLayout', 'row_id': rows[1]},
        {'label': '@string/gender', 'value': f'android:text="@{{{b}.ben.gender}}"'},
        {'label': '@string/tv_rch_id', 'value': f'android:text="@{{({b}.ben.rchId == null || {b}.ben.rchId.empty) ? @string/ds_empty_value : {b}.ben.rchId}}"', 'id': 'RCHlayout', 'row_id': rows[2], 'solo': True},
        {'label': '@string/tv_father_name_ph', 'value': f'android:text="@{{{b}.ben.fatherName}}"', 'id': 'ncd_father_name', 'cell_attrs': '\n                        showLayout="@{father}"', 'solo': True},
        {'label': '@string/tv_husband_name_ph', 'value': f'android:text="@{{{b}.ben.spouseName}}"', 'id': 'ncd_husband_name', 'cell_attrs': '\n                        showLayout="@{husband}"', 'solo': True},
        {'label': '@string/tv_spouse_name_ph', 'value': f'android:text="@{{{b}.ben.spouseName}}"', 'id': 'ncd_spouse_name', 'cell_attrs': '\n                        showLayout="@{spouse}"', 'solo': True},
    ]

STATUS = '''
                <ImageView
                    android:id="@+id/status"
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:layout_marginStart="@dimen/ds_space_xs"
                    android:contentDescription="@string/iv_death_indicator"
                    android:src="@drawable/ic_verified" />
'''
DEATH = f'''
                <TextView
                    android:id="@+id/iv_isDeath"
                    style="@style/Sakhi.Tag"
                    android:layout_marginStart="@dimen/ds_space_xs"
                    android:backgroundTint="@color/ds_surface_container_high"
                    android:contentDescription="@string/iv_death_indicator"
                    android:text="@string/ds_deceased"
                    android:textColor="@color/ds_on_surface"
                    android:visibility="@{{{b}.ben.isDeath == true ? View.VISIBLE : View.GONE}}" />
'''
SYNC_IMG = '''
                <ImageView
                    android:id="@+id/iv_sync_state"
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:layout_marginStart="@dimen/ds_space_xs"
                    android:contentDescription="@string/iv_sync_state_tv"
                    android:visibility="gone" />
'''
def risk(note):
    return f'''            <!-- {note} -->
            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/riskfactRV"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="@dimen/ds_space_xs"
                android:nestedScrollingEnabled="false" />

'''
HIST = '''            <TextView
                android:id="@+id/tv_tpt_follow_up_history"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="@dimen/ds_space_xs"
                android:background="@drawable/bg_ds_field_row"
                android:padding="@dimen/ds_space_sm"
                android:textAppearance="@style/TextAppearance.Sakhi.BodySmall"
                android:textColor="@color/ds_on_surface_variant"
                android:visibility="gone"
                tools:text="Follow-up history&#10;Follow-up 1: 30-10-2026" />

'''
def btn(i, kind, click, text, extra=()):
    return {'id': i, 'kind': kind, 'onClick': click, 'text': text, 'attrs': ['android:layout_weight="1"', *extra]}

FORM = f'@{{()-> clickListener.onClickForm({b})}}'
SCREEN_IDS = {'card': 'cv_content', 'title_bar': 'll_title_tb_screening_list_bar', 'name': 'tv_tb_name', 'meta_row': 'll_tb_meta', 'values': 'linear_tb_screening_list_Layout', 'sync': 'iv_sync_state'}
SUSP_IDS = dict(SCREEN_IDS, title_bar='ll_title_tb_suspected_list_bar', values='linear_tb_suspected_list_Layout')
SCREEN_ROWS = ['ll_tb_screening_list_details', 'll_tb_screening_list_details_2', 'll_tb_screening_list_details_3']
SUSP_ROWS = ['ll_tb_suspected_list_details', 'll_tb_suspected_list_details_2', 'll_tb_suspected_list_details_3']

SPECS = [
    ('rv_item_tb_screening_list', SCREEN_IDS, SCREEN_ROWS, [btn('btn_form_tb', 'filled', FORM, '@string/screen')],
     f'@{{{b}.tb.syncState}}', STATUS + DEATH, None, risk('kept for id parity (unused on this list; the vulnerable list has its own layout)'),
     'TB screening card: screened icon + Deceased tag + sync badge; Screen (filled) / View screening (tonal) set in TbScreeningListAdapter.'),
    ('rv_item_tb_suspected_list', SUSP_IDS, SUSP_ROWS, [btn('btn_form_tb', 'filled', FORM, '@string/tb_suspected_follow_up')],
     f'@{{{b}.tbSuspected.syncState}}', DEATH, 'contstraintLayoutV', '',
     'TB suspected card: Deceased tag + sync badge; follow-up button (pending = filled, done = tonal; TbSuspectedListAdapter).'),
    ('rv_item_tb_confirmed_list', SUSP_IDS, SUSP_ROWS,
     [btn('btn_family_members', 'outlined', f'@{{()-> clickListener.onFamilyMembers({b})}}', '@string/view_family_members', ['app:icon="@drawable/ic_ds_group"', 'app:iconGravity="textStart"']),
      btn('btn_form_tb', 'filled', FORM, '@string/follow_up')],
     f'@{{{b}.latestTbSyncState}}', DEATH, 'contstraintLayoutV', '',
     'TB confirmed card: Deceased tag + sync badge; Family members (outlined) + Follow up (pending = filled, done = tonal; TbConfirmedListAdapter).'),
    ('rv_item_velunaribility_list', SCREEN_IDS, SCREEN_ROWS, [btn('btn_form_tb', 'filled', FORM, '@string/screen')],
     f'@{{{b}.tb.syncState}}', STATUS + DEATH, None, risk('risk factors as warning tags (rv_item_risk_factor)'),
     'TB vulnerable-population card: screened icon + Deceased tag + sync badge; risk-factor tags; Screen / View screening (VulnerableListAdapter).'),
    ('rv_item_tpt_followup_list', SCREEN_IDS, SCREEN_ROWS, [btn('btn_form_tb', 'filled', FORM, '@string/start_tpt')],
     None, STATUS + DEATH + SYNC_IMG, 'contstraintLayoutV', HIST,
     'TB TPT card: status icon + Deceased tag + sync icon (ImageView set by TPTListAdapter); follow-up history box; Start TPT (filled) / Add follow-up (amber) / View details (tonal).'),
]

for name, ids, rows, btns, sync, header, inner_id, before_btns, what in SPECS:
    legacy = open(L + 'legacy_' + name + '.xml').read()
    note = f'''    {what}
    Initials + name + age (adapter); ben ID / mobile, Nikshay ID / gender, then RCH ID / father / husband / spouse on rows of
    their own (adapters show one). Generated by design/redesign-tools/gen_tb.py. Every id, binding and click is kept.
    Old layout: legacy_{name}.xml.'''
    out = card(legacy, note, ids, f'@{{{b}.ben.benFullName}}', cells(rows), btns, sync_expr=sync, header_extra=header)
    if inner_id:
        assert out.count(INNER) == 1
        out = out.replace(INNER, INNER.replace('<LinearLayout', f'<LinearLayout\n            android:id="@+id/{inner_id}"'))
    if before_btns:
        out = out.replace(BTNROW, before_btns + BTNROW, 1)
    missing = ids_of(legacy) - ids_of(out)
    assert not missing, (name, missing)
    open(L + name + '.xml', 'w').write(out)
    print(name, 'ok')
