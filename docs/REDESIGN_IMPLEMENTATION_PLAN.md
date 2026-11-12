# FLW App Redesign: Implementation Plan (design-only change)

> Date: 2026-11-04 · Branch studied: `naf_feature_gamification` · Figma: https://www.figma.com/design/omOXvgnOhoVpNZf5NgtHWy
> Companion doc: `docs/FIGMA_REDESIGN_PLAN.md` (what was designed).
>
> **Rule for this work: only the design changes.** Business logic, show/hide rules, validation, sync, Room DB,
> APIs and navigation destinations stay as they are. No DB migration is needed anywhere in this plan.
> Old layouts/code are **commented out, not deleted**.

---

## 1. How the app works today (what the redesign must sit on)

### 1.1 Shell and navigation
| Piece | Today | File |
|---|---|---|
| Main activity | `HomeActivity` (≈1,055 lines): `DrawerLayout` + `Toolbar` + `NavHostFragment` + one global `add_fab` | `ui/home_activity/HomeActivity.kt`, `res/layout/activity_home.xml` |
| Nav graph | `nav_home.xml`: 168 fragments, 246 actions, start = `homeFragment` | `res/navigation/nav_home.xml` |
| Top-level destinations | `homeFragment`, `allHouseholdFragment`, `allBenFragment` (`AppBarConfiguration`) | `HomeActivity.setUpActionBar()` |
| Drawer menu | Home, Supervisor (role-based), ASHA profile, Sync records, Dashboard, Create ABHA, Help & support (chat), Incentives (Mitanin title = "Monthly claim summary"), Badges, Support, Get data, About, Help, Report crash (hidden), Sync dashboard, Delete account, Log out | `res/menu/activity_home_drawer.xml` + click handlers in `HomeActivity` (lines ≈895–1001) |
| Role switch | Supervisor / ANM / CHO → hide Home, show Supervisor | `HomeActivity` ≈ line 308 |
| Home | `HomeFragment` = ViewPager2 with 2 tabs: **0 = `SchedulerFragment`** (to-do / impact), **1 = `HomeIconsFragment`** (module grid from `IconDataset`) | `adapters/HomePagerAdapter.kt` |
| Supervisor app | Separate `SupervisorActivity` + `nav_supervisor.xml` (19 fragments) | `ui/asha_supervisor/*` |

### 1.2 Forms: the engine (logic, **unchanged**)
- Every generated form extends `configuration/Dataset.kt` and holds one flat `list: MutableList<FormElement>`.
- On any answer change: `updateList(formId, index)` → dataset's `handleListOnValueChanged()` → returns an index → the new list is emitted on `listFlow`.
- Show/hide = **inserting/removing elements in that list**:
  - `triggerDependants(...)` (4 overloads) + `triggerDependantsReverse(...)`: **282 calls** across datasets.
  - plus **≈438 direct `list.add/remove/addAll/removeAll`** calls inside datasets.
  - removed elements get `value = null`, `errorText = null` (no stale data).
- Validation lives in the dataset (`validate*` helpers) and fragments scroll to the first error with `rvInputForm.scrollToPosition(index)`.

### 1.3 Forms: the renderer (where the redesign happens)
- `adapters/FormInputAdapter.kt` (≈1,690 lines): `ListAdapter<FormElement>`, one `ViewHolder` per `InputType`
  (EditText, Dropdown, Radio, Checkboxes, DatePicker, TimePicker, TextView/TextViewPair, ImageView, AgePicker, Headline, Button, NumberPicker, FileUpload, MultiFileUpload).
- Diff: `areItemsTheSame = id`, `areContentsTheSame = errorText`.
- Used by **52 fragments**; `FormInputAdapterWithBgIcon` by 3 (EC registration, ANC, ANC abortion).
- Item layouts: `res/layout/rv_item_form_*_v2.xml` (+ `layout_multi_file_upload`, `layout_uploaf_form`).
- Radio rows are created in code (`RadioButton(context)` inside a `RadioGroup`, orientation from `item.orientation`).
- `HEADLINE` = title + optional subtitle; used in 19 datasets (EC registration 17, child-ben registration 10, PMSMA 9, HBNC 8, TB screening 5, HRP track ×2 5, …).
- Server-driven forms (CUFY SAM/ORS/IFA, Pulse Polio, ORS, Filaria MDA, counselling, follow-ups) use their own fragments/adapters (`adapters/dynamicAdapter/*`).

### 1.4 Theme & flavours
- One theme `Theme.Sakhi` (Material3 Light) in `res/values/themes.xml` (+ v31/v33), colours in `res/values/colors.xml` (M3 palette with primary `#006496`, already matching the design).
- Flavour folders only override launcher icons / splash (`saksham`, `niramay`, `mitanin`, `xushrukha`, …), so **one token set serves all flavours**.
- Fonts in `res/font`: Roboto, Open Sans.

### 1.5 Data needed by the "new" screens already exists
| Need | Existing source |
|---|---|
| ANC visits per woman | `MaternalHealthDao.getAllActiveAncRecords(benId)`, `getLatestAnc`, `getSavedRecord(benId, visit)` |
| PNC visits | `PncDao` |
| HBNC day schedule | `HbncDayListFragment` / `HbncDao` (screen already exists) |
| Vaccines by stage | `ImmunizationDao.getBenWithImmunizationRecords`, `getVaccinesForCategory(ChildImmunizationCategory)`, `ChildImmunizationVaccineBottomSheetFragment` |
| ANC visit history sheet | `AncBottomSheetFragment` (exists) |
| CBAC live score | `CbacViewModel` (`raAgeScore`, `raSmokeScore`, …, total) |
| Gamification gate | `helpers/GamificationConfigProvider.isMechanicEnabled(mechanic, userId)` |

---

## 2. Global approach

1. **Tokens first**: map Figma variables → `colors.xml`, `dimens.xml`, `themes.xml` (+ `TextAppearance.*` styles, `ShapeAppearance.*`). Every screen improves without touching Kotlin.
2. **Re-skin, don't rebuild**: change XML item layouts and view styles; keep IDs and data-binding variables so ViewHolders/fragments compile unchanged.
3. **Behaviour additions only in the renderer / shell**, never in datasets (one exception, 3.2, is a 10-line generic hook).
4. **One switch** for the risky parts: `UiFlags` object (compile-time constants per flavour) to turn on bottom nav and collapsible sections, so QA can compare and we can roll back instantly.
5. One module per branch/PR; targeted tests on `niramay` + `mitaninUat` after each step.

---

## 3. The 6 open items: how each is solved

### 3.1 Collapsible numbered sections (HEADLINE)
**Problem:** `HEADLINE` is a plain title; the design shows numbered, collapsible sections with "x of y" / "Complete".

**Solution (renderer only, no dataset change):**
- `FormInputAdapter` computes sections from the current list: a section = a `HEADLINE` plus all elements until the next `HEADLINE`.
- Section number = order of the headline; meta text = "x of y" (required fields filled / required fields) or "Complete" (green tick) – computed from `FormElement.value`/`required`, which the adapter already holds.
- Collapse = the adapter keeps **all items in the list** (so `scrollToPosition(errorIndex)` indexes stay valid) and gives items of a collapsed section `GONE` + zero height in `onBindViewHolder`. Tapping the headline toggles a `collapsedHeadlineIds: MutableSet<Int>` and calls `notifyItemRangeChanged`.
- **Safety rules:** (a) default = all sections **expanded** (today's behaviour); (b) a section with any `errorText != null` auto-expands; (c) in view mode (`isEnabled = false`) sections start collapsed except the first; (d) behind `UiFlags.COLLAPSIBLE_SECTIONS`.
- Forms without HEADLINE (most short forms) look the same as today, only re-skinned.

**Files:** `FormInputAdapter.kt` (HeadlineViewHolder + bind gating), `rv_item_form_headline_v2.xml`.
**Risk:** low–medium (presentation only). **Needs from you:** confirm "expanded by default" (recommended).

### 3.2 Indented follow-up questions (blue line)
**Problem:** the renderer can't tell which questions were revealed by an answer, and the reveals happen through 282 `triggerDependants` calls **and** ≈438 direct list edits, so tagging inside `triggerDependants` alone would miss many.

**Solution (one generic hook, zero rule changes):**
- Add one UI-only field to `FormElement`: `var revealedBy: Int? = null` (not persisted; `FormElement` is not a Room entity, so no migration).
- In `Dataset.updateList(formId, index)`: snapshot the ids before `handleListOnValueChanged`, compare after; every **newly inserted** element gets `revealedBy = formId`; removed ones get `revealedBy = null`. This one place catches all 282 + 438 mutation paths without touching any dataset.
- Renderer: if `revealedBy != null` → start padding 14dp + 3dp primary left bar (a background drawable on the item root). Nested reveals (revealed element that reveals more) get the same single level, to keep it simple.
- Elements present from page setup (`setUpPage`) are never marked, so the first render is identical to today.

**Files:** `model/FormElement.kt` (+1 field), `configuration/Dataset.kt` (`updateList`, ≈10 lines), `FormInputAdapter.kt` (apply indent), 1 drawable.
**Risk:** low–medium (touches the engine entry point but not its rules; unit test: insert/remove marks correctly, values untouched).
**Needs from you:** OK to add this 10-line hook in `Dataset.updateList` (the only engine touch in the whole plan).

### 3.3 Yes/No as segmented buttons
**Solution (renderer only):** in `RadioInputViewHolder.bind`, when `entries.size <= 3` and every label is short (≤ 14 chars) → inflate a `MaterialButtonToggleGroup` (single-selection, required) instead of `RadioButton`s; otherwise keep full-width radio rows (3–6 options) as designed. Selection still calls the same `formValueListener.onValueChanged(item, index)` and sets `item.value` exactly as today. Disabled / view mode = same `isEnabled` path.
**Files:** `FormInputAdapter.kt` (RadioInputViewHolder), `rv_item_form_radio_v2.xml` (+ toggle-group container).
**Risk:** low (visual; same callbacks). **Needs from you:** nothing.

### 3.4 Bottom navigation instead of the drawer
**Solution (shell only; all 168 destinations and 246 actions unchanged):**
- Add a `BottomNavigationView` to `activity_home.xml` with 4 items:
  | Tab | Goes to | New code? |
  |---|---|---|
  | **Today** | `homeFragment`, pager position 0 (`SchedulerFragment`) | no |
  | **Modules** | `homeFragment`, pager position 1 (`HomeIconsFragment`) | small: `HomeFragment` exposes `selectTab(i)` |
  | **Sync** | `syncDashboardFragment` (exists) | no |
  | **Profile** | **new `ProfileMenuFragment`**: a list of the *same* drawer items | yes (1 fragment + layout) |
- **Reuse the drawer logic:** move today's drawer click handlers (`HomeActivity` ≈ lines 895–1001: sync, ABHA, chat, logout, delete account, support, incentives title for Mitanin, …) into one `HomeMenuActions` helper; the drawer (if kept) and `ProfileMenuFragment` both call it. Visibility rules (role-based Home/Supervisor, ChatFragment, Report crash hidden, gamification Badges) move with it.
- Bottom bar visible only on top-level destinations (`addOnDestinationChangedListener`); inner screens keep the toolbar back arrow as today. `AppBarConfiguration` top-level set extended with `syncDashboardFragment` + `profileMenuFragment`.
- Global `add_fab` stays as is (its show/hide logic is untouched).
- Sync tab badge = existing unsynced count (same source as the "Sync records" menu / sync dashboard).
- `UiFlags.BOTTOM_NAV = false` → drawer exactly as today (instant rollback). Drawer code is commented, not removed, when the flag is made permanent.
- **Supervisor/ANM/CHO roles:** `SupervisorActivity` is untouched in this phase; inside `HomeActivity` the role switch still hides Home and shows Supervisor in the Profile list.

**Files:** `activity_home.xml`, `HomeActivity.kt` (setup only), `HomeFragment.kt` (`selectTab`), new `ProfileMenuFragment` + `HomeMenuActions`, `nav_home.xml` (+1 destination), `menu/bottom_nav_home.xml`.
**Risk:** **high** (shell + back stack). Mitigated by the flag and by not changing any destination.
**Needs from you:** (1) ship behind the flag in the first release or later? (2) keep the drawer as a secondary entry when the flag is on? (recommended: no drawer, Profile tab replaces it).

### 3.5 "New" screens: mostly re-skins of screens that already exist
| Design screen | Already in the app | Work |
|---|---|---|
| ANC / PNC visit-history sheet (P4) | `AncBottomSheetFragment`, PNC list sheet | Re-skin layout; same data and clicks |
| HBNC 7-visit schedule | `HbncDayListFragment` | Re-skin to the day-grid layout |
| Vaccination card by stage | `ChildImmunizationVaccineBottomSheetFragment` + `ImmunizationDao` | Re-skin: group existing rows by `ChildImmunizationCategory`, status colours |
| CBAC step bar + sticky score | `CbacFragment` (single long layout, ≈147 views) + live scores in `CbacViewModel` | Add a part header strip and a sticky score bar bound to the existing total; no question logic changes |
| EC monthly history sheet | `ECTrackingListBottomSheet` | Re-skin |
| **Care timeline (pregnant woman)** | not present | **Only genuinely new screen**: read-only, built from existing DAO queries (registration, ANC records, PMSMA, delivery outcome, PNC). Opened from the PW list card. Optional for v1 |

**Risk:** low–medium; only the care timeline adds a new fragment.
**Needs from you:** include the care timeline in v1, or later? (recommended: later; re-skins first).

### 3.6 Gamification ON / OFF
**Solution (no backend dependency for the design work):**
- All new gamification UI (My Impact card, recap strip, badge snackbar, Badges row in Profile, celebration) checks the existing single gate `GamificationConfigProvider.isMechanicEnabled(mechanic, userId)`. Today it returns the code default (and `true` in DEBUG builds).
- When **FLW-1251** (mobile) lands after **AMM-2556** (backend), only the provider changes; the UI already follows it. Nothing else to wire.
- OFF layouts are the baseline (designed: Today OFF = screen 02, Profile OFF = M2.1-11), so hiding never leaves gaps.
- Badge **logic** (`badges/domain/*`) is not touched.

**Risk:** low. **Needs from you:** confirm OFF is the default for unknown users (product), and the backend timeline for AMM-2556 (not blocking the design work).

---

## 4. Token mapping (Figma → Android)
| Figma | Android |
|---|---|
| `color/primary`, `on-primary`, `primary-container`, … | `colors.xml` `md_theme_light_*` (already M3; values aligned) + `Theme.Sakhi` colour attrs |
| Module tones `module/<x>/bg|fg` (10 pairs) | `colors.xml` `module_<x>_bg/fg` |
| Success / warning / accent | `colors.xml` `status_success*`, `status_warning*`, `accent` |
| Spacing `2xs…3xl` (4–40) | `dimens.xml` `space_2xs … space_3xl` |
| Radius `sm…xl` (8–24) | `ShapeAppearance.Sakhi.Small/Medium/Large` |
| Text styles (Display…Number) | `TextAppearance.Sakhi.*` in `styles.xml` (Roboto from `res/font`) |
| Elevation 1–3 | card elevation dimens + `Widget.Sakhi.Card` |
| Components (Button, Text field, Chip, Sync badge, Tag, cards…) | `Widget.Sakhi.*` styles + reusable include layouts |

Dark mode: not in scope (Figma Starter allows 1 mode); tokens are named so a `values-night` set can be added later.

---

## 5. Build order (each step shippable on its own)
| # | Step | Touches | Logic change |
|---|---|---|---|
| 1 | Tokens, theme, styles, shapes | `res/values*` | none |
| 2 | Form kit: all `rv_item_form_*` layouts + Yes/No segmented (3.3) | `FormInputAdapter` (views only) | none |
| 3 | Collapsible sections (3.1) behind flag | `FormInputAdapter` | none |
| 4 | Indented follow-ups (3.2) | `FormElement` +1 field, `Dataset.updateList` hook | presentation tag only |
| 5 | Cards & lists: ben / household / record cards, tags, sync badge, search, chips, empty/loading | `rv_item_*` list layouts | none |
| 6 | Home: Today (`SchedulerFragment`), Modules (`HomeIconsFragment`), module hubs | layouts | none |
| 7 | Re-skin existing sheets/schedules (3.5) | layouts | none |
| 8 | Bottom navigation + Profile tab (3.4) behind flag | shell | navigation entry points only |
| 9 | Gamification UI behind the existing gate (3.6) | Today, Profile, snackbars | none |
| 10 | (Optional) Care timeline | new fragment | read-only |

---

## 6. Testing per step
- Build `niramayDebug` + `mitaninUatDebug`; run targeted unit tests for touched classes.
- New unit tests: `Dataset.updateList` marks `revealedBy` on insert/clears on remove (3.2); section counting & auto-expand on error (3.1); radio→segmented keeps the same value/index (3.3).
- Manual check per form: answer every conditional question once; values, validation messages, scroll-to-error and saved data identical to today.
- QA on low-end devices for each flavour; Hindi and Assamese (long labels must wrap, not cut).
- Rollback: `UiFlags` for steps 3 and 8; all other steps are layout-only and revertible per PR.

---

## 7. What I need from you
| # | Decision / input | Recommended |
|---|---|---|
| 1 | Sections expanded by default (3.1) | Yes |
| 2 | OK to add the 10-line `revealedBy` hook in `Dataset.updateList` (3.2) | Yes: the only engine touch |
| 3 | Bottom nav: first release behind flag, or later? Keep drawer too? (3.4) | First release, flag ON in UAT only; no drawer when ON |
| 4 | Care timeline in v1? (3.5) | Later |
| 5 | Gamification default OFF for unknown users; AMM-2556 timeline (3.6) | OFF |
| 6 | Base branch for development (currently `naf_feature_gamification`) | Your call |
| 7 | Hindi/Assamese reviewer for new strings (and fix of wrong `values-as` strings) | n/a |
| 8 | QA devices / flavour priority | n/a |

Everything else (all coding, layouts, tests, PRs) can be done without further input.
