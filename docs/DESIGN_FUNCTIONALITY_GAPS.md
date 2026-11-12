# Design vs code: missing functionality, screen by screen

> Date: 2026-11-05 · Branch: `naf_revamp_flw` · Figma: https://www.figma.com/design/omOXvgnOhoVpNZf5NgtHWy
> Method: every Fragment / BottomSheet / Dialog read in code (fragment + layout + ViewModel + adapter), each feature checked against the design source (`design/figma-plugin/src/*.js`, incl. the generated form/card specs).
> Ordinary form fields are NOT listed here (covered by `docs/FORM_AND_CARD_AUDIT.md`). This file lists screen-level functionality: actions, show/hide rules, dialogs, filters, multi-visit logic, flavour/role gates, view/edit modes.
> **Missing** = no trace in design · **Partial** = screen exists but the feature/rule/state is absent or wrong.
> Caveat: frames 01–06 (Login, Today, Modules, Households, Members, All beneficiaries) were drawn via Figma MCP, not the plugin, so items marked **Missing\*** need a check on the canvas.

## Summary

| # | Group | Screens | Missing | Partial |
|---|---|---|---|---|
| A1 | Household, beneficiary, Home, login | 25 | 70 | 57 |
| A2 | Profile, sync, incentives, notifications, misc | 19 | 39 | 43 |
| B | Maternal, HRP, infant (HBNC/HBYC) | 28 | 52 | 56 |
| C | Child care, immunization, eligible couple | 31 | 46 | 60 |
| D | Disease control, communicable, general OP | 27 | 71 | 32 |
| E | NCD, CBAC, TB, CHO | 38 | 68 | 51 |
| F | Village-level forms, death reports | 31 | 74 | 30 |
| G | ABHA, Supervisor | 19 | 44 | 39 |
| | **Total** | **218** | **464** | **368** |

## Cross-cutting gaps (apply to many screens)
1. **Voice search mic** on every searchable list: not in the design kit.
2. **Deceased state** on cards (grey, Death tag, actions hidden), and "Death" outcomes marking the person dead app-wide (Malaria, Kala-azar, AES, Leprosy, TB, Delivery, PNC → MDSR/CDR).
3. **View-only after save + Edit button** (rules differ per form: always / latest visit only / hidden when dead). Design kit says "view only, ask your ANM" → contradicts code.
4. **Sort sheet (5 options)** and "not followed up" icon (last visit 90–365 days) on maternal/EC lists.
5. **Frequency / "Add new" blocking rules** (once a month, once a year, 365 days for CBAC, visit date after previous visit, …).
6. **Multi-visit flows** (Visit 1…n sheets with View + Add new visit): Malaria, Leprosy cycles, CUFY, TB suspected, referral follow-ups, Eye surgery, ANC home visit.
7. **Flavour gates** (Mitanin hides uploads/tiles/renames; Saksham bank details; Xushrukha BP/Hb required) and **role gates** (Supervisor read-only lists with buttons hidden).
8. **Incentive pop-ups** ("upload X to claim incentive") and file uploads (Camera/Gallery/PDF, 5 MB, max N).
9. **Missing navigation levels**: disease tile → sub-hub → household list → member list → form; Household hub → Households / Non-household; Today cards → Missed period / Non-follow-up screens.
10. **App-wide dialogs**: logout with unsynced count, session expired, account deactivated, rooted device, in-app update, incomplete install.

## Decisions needed (design vs code disagree)
| Item | Code | Design | Choose |
|---|---|---|---|
| ABHA ASHA/GOV switch | hidden (`fragment_aadhaar_id.xml:113`) | shown | drop from design or build GOV |
| ABHA mobile OTP | reuses mobile from step 1 | asks again | |
| Mother (Td) immunization | no tile, unreachable | tile shown | |
| Edit after save | Edit allowed (per rules) | "ask your ANM" | |
| Add member dialog | gender + filtered relation list | Child/Adult chooser | |
| Malaria Pv treatment | 4 days (14 only Mitanin) | 14 days all | |
| Vulnerable population filter | ~40 risk factors | 4 categories | |
| Delete account / Support | external Microsoft Forms | in-app | |
| LMS, Forgot password | hidden in code | shown | |
| High-risk "PMSMA" button | opens ANC form (place = PMSMA) | opens PMSMA form | |

## Code bugs found on the way (NOT fixed, separate tickets)
- Communicable grid: all 5 disease tiles show the TB-screening count (`IconDataset.kt:587-614`).
- AES "herd of pigs" question never shows ("AES / JE" vs "AES_JE") and its answer is not saved (`AESSuspectedListFragment.kt:58`).
- Supervisor ASHA list from Overdue always shows "Overdue: 0" (`IncentiveVerificationFragment.kt:165`).
- Household members: tapping Cataract opens the sheet AND navigates to the form (`HouseholdMembersFragment.kt:421-433`).
- Village hub overdue red border matches tiles by English title → may never show in Hindi/Assamese (`VillageLevelFormsViewModel.kt:31-70`).
- Card spec extractor missed buttons on `rv_item_ncd_cbac` and `rv_item_ben_with_hrpa_form` (design spec pages incomplete).

---

## A1 · Home shell, Households, Beneficiaries, Login — missing functionality in design

Summary: 25 screens checked, 70 missing features, 57 partial.

**How to read this**
- Code paths are relative to `app/src/main/java/org/piramalswasthya/sakhi/` unless they start with `res/`.
- Design sources searched: `design/figma-plugin/src/*.js` (frame names, labels, NAV map in `44-stage-2.14-navigation.js`, card specs built from `AUDIT_CARDS` in `45-stage-2.15-specs.js`) and `docs/FIGMA_REDESIGN_PLAN.md`.
- **Caveat:** frames **01 Login, 02 Today, 03 Modules, 04 Households, 05 Household members, 06 All beneficiaries** were drawn through the Figma MCP. Their content is **not** in the plugin source. For them I relied on component properties (Household card = HOF name / HH ID / Members / Phone + buttons "Members", "Add beneficiary"; Beneficiary card = Name, Meta, Ben ID, Mobile, HOF, Registered on, actions, tags), the NAV tap map (due cards, tiles, buttons, FABs) and the 2.17 card spec (`rv_item_ben`, `rv_item_household`). An item marked **Missing\*** has no trace in any of those sources. Check it on the canvas before you add it.
- "Partial" = the screen exists in the design, but this rule, state or variant is absent or different.

---

### Home shell / drawer / toolbar (`HomeActivity`, ui/home_activity/HomeActivity.kt) — design frames: 02 · Home – Today, 08 · Sync, 09 · Profile & more (drawer is replaced by the bottom nav and Profile)

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Logout confirmation dialog that shows how many records are not yet synced ("N records not processed / all records synced, are you sure?"). On Yes it clears the FCM token, beneficiary images and all background work | HomeActivity.kt:203-227, opened from drawer item at :941 | Missing | Design "Log out" (09) goes straight to 01 Login (NAV.list). There is no warning about unsynced data, so data-loss risk is not shown |
| 2 | Session-expired dialog (cannot be dismissed). OK logs the user out and returns to Login | HomeActivity.kt:507-530 | Missing | |
| 3 | Account-deactivated dialog, raised by the server | HomeActivity.kt:486-503 (also LoginActivity.kt:109) | Missing | |
| 4 | "Unsupported device" block on rooted phones and emulators (release builds only). The only button is Exit | HomeActivity.kt:391-398, :665-672 | Missing | |
| 5 | In-app update prompt (Play in-app update) | HomeActivity.kt:423-424 | Missing | |
| 6 | Start-up permission prompt (call, storage, mic, camera) | HomeActivity.kt:532-551 | Missing | |
| 7 | "Sync records" manual push + pull. A second tap within 15 min shows the toast "Please wait Syncing in Progress" | HomeActivity.kt:911-926 | Partial | Design has a "Sync now" button (08) and a "Sync records" row only in the supervisor profile (M2.12-06). The 15-min throttle message is missing |
| 8 | Supervisor / ANM / CHO login hides "Home" and shows the "Supervisor" menu item | HomeActivity.kt:308-314 | Partial | 09 lists "Supervisor" for every user. The role rule is missing |
| 9 | Mitanin builds rename "Incentives" to "Monthly claim summary" | HomeActivity.kt:895-903 | Partial | 09 shows only "Incentives" |
| 10 | "Delete my account" opens an external MS Forms page, with a different URL per flavour (saksham/niramay/xushrukha vs others). If no browser: toast | HomeActivity.kt:954-975 | Partial | M2.11-09 draws it as an in-app "Delete" banner. The real behaviour is an external web form |
| 11 | "Support" opens an external MS Forms feedback link | HomeActivity.kt:978-991 | Partial | Design "Help & support" → M2.11-09 (helpline, WhatsApp…). The external form link is not shown |
| 12 | Tap the toolbar title (village name) on Home → change work location (closes Home, opens ServiceLocationActivity) | HomeActivity.kt:131-135, :741; HomeFragment.kt:186-198 | Partial | M2.13-04 says "You can change this later in Profile", but 09 has no "Change work location" item |
| 13 | Drawer header: tap the avatar → pick a profile photo from the gallery. Employee ID falls back to user ID when blank. "Facility/sub-centre" line | HomeActivity.kt:837-878, :874-877 | Partial | 09 shows avatar + edit icon. Photo picking and the Emp-ID fallback rule are not specified |
| 14 | Toolbar "Language" icon on the Home screen (bottom sheet; app restarts) and "Go to Home" icon on every other screen (they swap with `showMenuHome`) | HomeActivity.kt:683-716, :757-760 | Partial | Language moved to Profile (M2.11-08). Home shortcut is replaced by the bottom nav. Fine if intended |
| 15 | Toolbar "Sync status" icon opens a sync bottom sheet (double-tap guarded) | HomeActivity.kt:718-731 | Partial | Design uses a full Sync screen (08) and a banner. Bottom-sheet variant not drawn |
| 16 | Chat-support FAB + WebView bottom sheet (currently off: `isChatSupportEnabled=false`). Drawer "Help & support (chat)" and "Report crash" are hidden | HomeActivity.kt:113, :372-389, :553-656, :317-319 | Missing | Hidden today. Noted so it is not lost if re-enabled |
| 17 | App version shown at the bottom of the drawer | HomeActivity.kt:418 | Partial | Only "About Sakhi · v2.14" inside M2.11-09 |
| 18 | Auto background push whenever unsynced count > 0 (debounced) | HomeActivity.kt:407-417 | Partial | Background behaviour. Design banner "12 records not synced" has no "syncing automatically" state |

### Home container (`HomeFragment`, ui/home_activity/home/HomeFragment.kt) — design frame: 02 · Home – Today / 03 · Home – Modules (bottom nav)

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Back press on Home → "Exit application? Yes/No" dialog | HomeFragment.kt:46-57, :61-69 | Missing | |
| 2 | Home opens on the **Modules** tab (index 1) by default, not on Scheduler/Today | HomeFragment.kt:196 | Partial | Design treats Today as the landing tab. Confirm the intended default |
| 3 | Long-press the Modules tab → developer-mode PIN sheet. A second long-press disables dev mode (toast) | HomeFragment.kt:162-175 | Missing | Hidden support/dev feature |
| 4 | First-sync download progress strip on Home ("Downloading… N%") while the pull/push worker runs | HomeFragment.kt:117-150 | Partial | M2.0-S2 shows a "First download" card on All beneficiaries only, not on Home/Today |
| 5 | Downloads all dynamic form schemas for the current language when Home opens | HomeFragment.kt:105-109 | Partial | No loading/failed state for this in design (background only) |

### Today / Scheduler (`SchedulerFragment`, ui/home_activity/home/SchedulerFragment.kt) — design frames: 02 · Home – Today, M2.1-01 · Today (gamification ON)

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Card "Suspected high-risk **non-pregnant** women" with count → HRP non-pregnant list | SchedulerFragment.kt:107-109, count :141-144; layout fragment_scheduler.xml:405-472 | Missing | NAV.due has only ANC, vaccination, suspected HR pregnancies, LBW, missed period, no follow-up |
| 2 | Card "Confirmed high-risk pregnant women" with count → ANC list (source=3) | SchedulerFragment.kt:104-106, :146-149 | Missing | |
| 3 | ABHA card showing two counts (new ABHA / old ABHA) → All-beneficiaries ABHA list (source 1) | SchedulerFragment.kt:113-116, :151-159; fragment_scheduler.xml:727-842 | Missing | The ABHA list also has CSV download (see All beneficiaries) |
| 4 | RCH-ID card with count → All-beneficiaries RCH list (source 2) | SchedulerFragment.kt:117-120, :161-164 | Missing | |
| 5 | Missed-period card opens an intermediate **Missed period** screen (EC registration vs EC tracking split). No-follow-up card opens a **Non-follow-up** screen (ANC / PNC / EC split) | SchedulerFragment.kt:121-126 | Partial | Design links "Missed period" → M2.2-02 and "No follow-up in 30 days" → 06 All beneficiaries. The split screens are not designed |
| 6 | Impact dashboard carousel: prev/next arrows, swipe with peek, "3 / 8" counter, haptic tick. Hidden when the badge list is empty (remote kill). Tap → Badge shelf | SchedulerFragment.kt:207-294 | Partial | Design "My Impact card" is a static card (rows). Carousel controls, counter and the empty/hidden rule are missing |
| 7 | Debug only: long-press the Impact title to stage demo badge progress | SchedulerFragment.kt:262-273 | Missing | Debug only, low priority |
| 8 | Loading spinner state for the to-do list | SchedulerFragment.kt:62-73 | Partial | M2.0-S2 skeleton exists for lists, not for Today |

### Modules grid + Monthly Recap strip (`HomeIconsFragment`, `MonthlyRecapStrip`, ui/home_activity/home/HomeIconsFragment.kt) — design frame: 03 · Home – Modules, M2.1-01 (recap strip)

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Module tiles show live counts (Households, Beneficiaries, NCD referred list, Immunization) | configuration/IconDataset.kt:67-140 | Partial | M2.13-L2/L5 set `Show count=false`. Check that 03 shows counts on these 4 tiles |
| 2 | "NCD referred list" tile on Home | IconDataset.kt:103-108 | Partial | NAV.tiles has "HWC referrals" → M2.8-14. Confirm it is the same list and label |
| 3 | Monthly Recap strip lives on the **Modules** tab, with a resume progress bar, NEW / DONE badges and a reduced-motion rule (no animation) | HomeIconsFragment.kt:73-210 | Partial | Design puts the strip on Today (M2.1-01). The resume progress bar and reduced-motion rule are not specified |
| 4 | Debug-only forced recap preview state (`monthly_recap_preview_state`) | HomeIconsFragment.kt:83-89 | Missing | Debug only |

### Missed period (`MissedPeriodFragment`, ui/home_activity/home/MissedPeriodFragment.kt) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Two count cards: "Eligible couple registration" (missed period, EC list source 2) and "Eligible couple tracking" (missed period, EC tracking source 2) | MissedPeriodFragment.kt:51-69 | Missing | Design Today card jumps straight to M2.2-02 Pregnancy registration list. The EC split and both filtered lists are missing |
| 2 | Loading state | MissedPeriodFragment.kt:39-49 | Missing | |

### Non-follow-up cases (`NonFollowUpFragment`, ui/home_activity/home/NonFollowUpFragment.kt) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Three count cards: ANC non-follow-up → ANC list (source 1), PNC → PNC mothers (source 1), EC → EC tracking (source 1) | NonFollowUpFragment.kt:51-79 | Missing | Design "No follow-up in 30 days" goes to 06 All beneficiaries, which has no follow-up filter |

### Enable dev mode sheet (`EnableDevModeBottomSheetFragment`, ui/home_activity/home/EnableDevModeBottomSheetFragment.kt) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | PIN entry. The correct PIN enables dev mode (toast "Dev Mode Enabled!"). A wrong PIN shows the toast "Invalid PIN!" | EnableDevModeBottomSheetFragment.kt:34-43 | Missing | Planned in 2.13 ("dev-mode sheet") but not built |

### Household hub (`HouseholdFragment`, ui/home_activity/all_household/HouseholdFragment.kt) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | The "Household" home tile first opens a hub with two tiles: **Households** (count) and **Non-household** (count of people with no household) | HouseholdFragment.kt:82-95; IconDataset.kt:494-506 | Missing | Design tile "Households" → 04 directly. There is no entry point to the non-household list anywhere in the design |

### Households list (`AllHouseholdFragment`, ui/home_activity/all_household/AllHouseholdFragment.kt + adapters/HouseHoldListAdapter.kt + res/layout/rv_item_household.xml) — design frame: 04 · Households (+ M2.13-L3 Hindi)

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Delete household** icon on the card → confirm dialog "Delete Household – Are you sure you want to delete <HOF>?". This is a soft delete (deactivate, marked for sync) | AllHouseholdFragment.kt:88-97, adapter :80-105, VM AllHouseholdViewModel.kt:128-143 | Missing | Design card has only Members / Add beneficiary. Delete is not gated by sync state, and there is no undo (icon hides once deleted) |
| 2 | Deleted household state: grey card, red "Deleted Record" label, Add-beneficiary hidden, Members button greyed, taps on card / members / add are blocked | HouseHoldListAdapter.kt:83-92; AllHouseholdFragment.kt:213, :222, :231-246 | Partial | 2.17 card spec shows a "Deleted Record" tag only. The disabled actions and grey state are not specified |
| 3 | Tap card body → open the household form (view mode with Edit FAB). **ASHA role only** | AllHouseholdFragment.kt:211-220 | Missing\* | Design card header → 05 Members (NAV). No "view/edit household" entry |
| 4 | "Add beneficiary" on a household with **0 members** goes straight to Head-of-family registration (relation = Self) | AllHouseholdFragment.kt:231-238 | Missing | |
| 5 | "Add beneficiary" on a household with members → dialog: choose gender, then **relation-to-head list filtered** by who already exists (father/mother/wife/husband removed once registered; unmarried HOF removes spouse/child relations; same-sex spouse rules). OK stays disabled until a relation is picked | AllHouseholdFragment.kt:99-160, :380-427 | Partial | Design → M2.13-02 (Child vs Adult). Register-member form 07 has a plain relation dropdown. Neither the gender+relation pre-dialog nor the filtering rules appear |
| 6 | "New household registration" button with **draft check**: dialog "Incomplete form found – Open draft / Create new" | AllHouseholdFragment.kt:66-76, :283-286 | Partial | FAB → M2.13-01 exists. The resume-or-discard draft dialog is missing (design only says "finish later from Drafts") |
| 7 | ASHA-supervisor role: list is read-only. "New household" hidden, card edit and Add-beneficiary disabled; screen hosted in SupervisorActivity | AllHouseholdFragment.kt:179-191, :205-209, :212, :231 | Missing | No read-only role variant of 04 |
| 8 | Voice search (mic icon → speech-to-text into search) | AllHouseholdFragment.kt:288-289, :58-63 | Missing\* | Search bar component has a placeholder only |
| 9 | Filaria disease mode: card shows "Add MDA" instead of "Add beneficiary" (same adapter reused from disease module) | HouseHoldListAdapter.kt:63-77 | Partial | M2.8-07 covers MDA. Noted only because the shared card changes by context |

### Household members (`HouseholdMembersFragment`, ui/home_activity/all_household/household_members/HouseholdMembersFragment.kt + adapters/BenListAdapter.kt + res/layout/rv_item_ben.xml) — design frame: 05 · Household members

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Delete (remove) beneficiary** icon on each member card → confirm "Delete Beneficiary – Are you sure you want to delete <name>?" → soft delete (deactivate, marked for sync) | HouseholdMembersFragment.kt:284-293, :452-469, :478; BenListAdapter.kt:309-353; rv_item_ben.xml:398-413 | Missing | Not in the Beneficiary card component or the 2.17 spec. Only a generic "delete" confirmation dialog exists in the kit (30-stage-2.0-kit.js:218) |
| 2 | **Head of family cannot be deleted while other members exist** (toast "Head of Family cannot be deleted when other members exist.") | HouseholdMembersFragment.kt:454-466; HouseholdMembersViewModel.kt:170-178 | Missing | |
| 3 | Delete icon hidden for dead beneficiaries and for already-deleted ones. Delete is not gated by sync state | BenListAdapter.kt:288-299, :312-349 | Missing | |
| 4 | Deleted member state: card turns grey, "Deleted Record" label, ABHA / call / sync / action buttons hidden, all taps blocked (`canProceed`) | BenListAdapter.kt:312-327; HouseholdMembersFragment.kt:309-311 | Partial | 2.17 spec shows a "Deleted Record" tag only |
| 5 | "Add member" FAB is shown **only for ASHA role** and is disabled until existing members load. Then the gender + filtered relation dialog opens (same rules as Households #5) | HouseholdMembersFragment.kt:341-351, :68-107, :197-239 | Partial | Design FAB → M2.13-02 Child/Adult. Role gate, disabled-while-loading and relation filtering are missing |
| 6 | Card buttons by family state: **Register Wife** (married male, no spouse), **Register Husband** (married female, no spouse), **Register Children** (married female with spouse, "has children" = yes, 0 children), **View Children** (female with children > 0) | BenListAdapter.kt:187-246 | Partial | 2.17 spec shows only "Register Children" + "Register Wife". "Register Husband", "View Children" and all show rules are missing |
| 7 | **Cataract surgery** button on members aged ≥ 40 and alive. Orange if a surgery record exists, green if none. Opens the eye-surgery visit sheet | rv_item_ben.xml:662-677; ui/BindingUtils.kt:542-550; BenListAdapter.kt:106-113 | Missing | No "cataract" or "eye surgery" anywhere in the design source except a GC routing dialog (38-stage-2.8-disease.js:163) |
| 8 | Card tap → open the member's registration form (view mode). ASHA role only, not if deleted | HouseholdMembersFragment.kt:365-378 | Missing\* | |
| 9 | ABHA button → if found, opens the ABHA flow for that member. Hidden for supervisor and dead members | HouseholdMembersFragment.kt:415-417, :578-580; BenListAdapter.kt:78-82; rv_item_ben.xml:588-596 | Partial | NAV "Create ABHA" → M2.10-01 exists. Hide rules are missing |
| 10 | Call icon (phone call). Not for deleted members. If permission is denied: toast + permission request | HouseholdMembersFragment.kt:436-451 | Partial | Card spec has a call button. Hidden-when-dead/deleted rule and permission toast are missing |
| 11 | Malaria-mode ON/OFF switch and "tap member → Malaria/Kala-azar form" when the list is opened from Disease control | HouseholdMembersFragment.kt:325-337, :303-310, :583-602 | Partial | Dead today (`isFromDisease` is hard-coded 0, VM:27). Noted only |

### New household registration (`NewHouseholdFragment`, ui/home_activity/all_household/new_household_registration/NewHouseholdFragment.kt) — design frame: M2.13-01 · New household registration

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Consent dialog** (cannot be dismissed; checkbox must be ticked, else toast "Please tick the checkbox"; Cancel exits). Mitanin uses its own consent text | NewHouseholdFragment.kt:96-130, :386-388 | Missing | Design consent only exists for ABHA (M2.10-03) |
| 2 | After save of a new household: dialog "Add head of family? <HOF name>": Yes → HOF registration form, No → back. In edit mode: just back | NewHouseholdFragment.kt:132-148, :347-357 | Partial | Design primary "Save & add members" → M2.13-02 Child/Adult. Code skips the type choice and goes straight to HOF |
| 3 | ABHA / Ayushman card lookup: enter the number, tap Submit, details are fetched from the server and **prefill the form (Mitanin only)**. "ABHA already exists" error code → dialog offering to create ABHA. Network error toast | NewHouseholdFragment.kt:213-251, :272-285, :150-163; HouseholdFormDataset.kt:145-188 | Partial | M2.13-01 shows the Yes/No question only. Lookup button, prefill, format validation (14-digit ABHA or 9-14 alnum Ayushman) and error dialog are missing |
| 4 | **Link mode**: when opened from a non-household beneficiary ("Create new household"), saving links that person as HOF and returns to the Non-household list (toast "Beneficiary linked") | NewHouseholdFragment.kt:337-346; NewHouseholdViewModel.kt:53-54, :125-126 | Missing | |
| 5 | View mode for an existing household, with an **Edit FAB** to unlock fields | NewHouseholdFragment.kt:254-256, :297-300 | Partial | Generic view pattern exists (M2.0-P5), not for household |
| 6 | Voice input (mic) on text fields | NewHouseholdFragment.kt:57-63, :260-263 | Missing\* | |
| 7 | Saving state (spinner), "Save successful" toast, save-failed toast | NewHouseholdFragment.kt:325-367 | Partial | Kit snackbar M2.0-S6 covers success. Failure state not drawn |

### Member living elsewhere (`NonHouseholdLivingFragment`, ui/home_activity/all_household/non_household/NonHouseholdLivingFragment.kt) — design frame: M2.13-03 · Member living elsewhere

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Place list is 18 options (footpath, railway platform, bus station, prison, under a tree, temple, mosque, church, other praying place, school/college, Ekalavya school, rehab centre, orphanage, old-age home, private / govt / NGO hostel, other). "Institution name *" only for 7 institution places. "Other place *" text only for "Other" | NonHouseholdLivingFragment.kt:45-66; res/values/strings_home.xml:28-56 | Partial | Design shows 5 different options and always shows the institution field. The "Other → specify" reveal and the institution rule are missing |
| 2 | Purpose/context: this is for people with **no household** (homeless/institution). On Continue it creates the shared "household 0" record and opens registration as Self | NonHouseholdLivingFragment.kt:85-103 | Partial | Design frames it as "member not living at home now" (Rohit, 17, of a family). That is a different meaning. Entry is from the Non-household list (+ Add beneficiary), which is missing |

### All beneficiaries list + Beneficiary card (`AllBenFragment`, ui/home_activity/all_ben/AllBenFragment.kt + adapters/BenListAdapter.kt / BenPagingAdapter.kt + res/layout/rv_item_ben.xml) — design frame: 06 · All beneficiaries

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Filter dialog**: All / With ABHA / Without ABHA / Age above 40 | AllBenFragment.kt:76-117, :144-146; res/layout/alert_filter.xml | Missing\* | No ABHA filter text anywhere in the design source |
| 2 | Same screen reused as 4 more lists by `source`: **ABHA list** (1), **RCH list** (2), age ≥ 30 (3), eligible females (4), **Non-household list** (5), each with its own title | AllBenFragment.kt:136-140, :151-155, :375-400; BenDao.kt:234-235 | Missing | Only "All beneficiaries" is designed |
| 3 | **Download CSV** icon (ABHA/RCH/source 3-4 lists) → writes `ABHAUsers_<time>.csv` to Downloads. Toasts "CSV Downloaded" / "No data to export" | AllBenFragment.kt:147-155; AllBenViewModel.kt:117-150 | Missing | |
| 4 | Non-household list: "Add beneficiary" button → Living-place form → registration with no household | AllBenFragment.kt:136-140, :303-307 | Missing | |
| 5 | **Cataract Surgery** button: age ≥ 40 and alive. Orange = already has an eye-surgery record, green = none. Opens the eye-surgery visit sheet | rv_item_ben.xml:662-677; BindingUtils.kt:542-550; BenListAdapter.kt:106-113; AllBenFragment.kt:233-246, :404-424 | Missing | Reported as missing. Confirmed: no eye/cataract trace in the design |
| 6 | ABHA button: if the beneficiary is **not yet synced** (no server ID) → alert "Please wait for the record to sync and try again". Otherwise opens the ABHA flow. Hidden for supervisor, invisible for dead people | AllBenFragment.kt:357-370, :333-341; BenListAdapter.kt:78-82; rv_item_ben.xml:588-596 | Partial | "Create ABHA" → M2.10-01 is designed. The not-synced block and hide rules are missing |
| 7 | ABHA number on the card, with a **new / old ABHA** marker | rv_item_ben.xml:600-636 | Partial | 2.17 spec keeps the "ABHA Number" label. New/old marker is missing |
| 8 | Mobile-number **consent verified / not verified** icon next to the mobile (hidden on Mitanin) | rv_item_ben.xml:505-511 | Missing | |
| 9 | Dead beneficiary state: grey card, death icon, "Date of death" row, call / sync / ABHA / spouse-child buttons / delete hidden | BenListAdapter.kt:288-307; rv_item_ben.xml:153-158, :552-565 | Partial | Spec lists "Date of Death · if any". The hiding rules and death icon are missing |
| 10 | "Non-household" icon on cards of people with no household (hhId = 0). HOF badge vs "Head of family: name" row | BenListAdapter.kt:100-101, :118-128 | Partial | HOF tag is in the spec. Non-household marker is missing |
| 11 | Avatar picture changes by age and gender (baby, infant, boy, girl, man, woman, elderly man/woman ≥ 60) | BenListAdapter.kt:131-183 | Partial | Design uses initials avatar |
| 12 | Relation line rules: father vs husband vs spouse name chosen by gender, age > 15 and "Not Available" values | BenListAdapter.kt:255-286 | Partial | Spec lists all three labels. The rule for which one shows is missing |
| 13 | Register Wife / Husband / Children / View Children buttons (same rules as Household members #6). Tap opens the spouse or child registration form. ASHA only | AllBenFragment.kt:171-221; BenListAdapter.kt:187-246 | Partial | |
| 14 | Call icon. Permission denied → toast "Please allow permissions first" + request | AllBenFragment.kt:248-259 | Partial | Call button designed. Error path is not |
| 15 | Voice search | AllBenFragment.kt:309-310 | Missing\* | |
| 16 | Delete beneficiary is **not** available on this list (only in Household members) | BenPagingAdapter default `isSoftDeleteEnabled=false`; AllBenFragment.kt:262-270 | Partial | Make sure the redesigned card does not offer delete here, or decide it should |
| 17 | Sync-state icon per card (hidden for dead people) | BenListAdapter.kt:83, :296 | Partial | Spec shows a sync badge. Hide rule is missing |

### ABHA beneficiaries list (`AbhaBenFragment`, ui/home_activity/all_ben/AbhaBenFragment.kt) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Separate ABHA list with the same filter (All / With / Without / Age > 30), CSV download, voice search, ABHA button and call | AbhaBenFragment.kt:82-140, :274-291 | Missing | **Not reachable in code** (nav action exists, nothing navigates to it). Probably legacy, so confirm before designing |

### Eye surgery visit sheet (`EyeSurgeryBottomSheetFragment`, ui/home_activity/all_ben/eye_surgery_registration/eyeBottomsheet/EyeSurgeryBottomSheetFragment.kt) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Bottom sheet with the beneficiary name and a list of saved visits ("Left/Right/Both eye visit N"). Tap a visit → form in view mode | EyeSurgeryBottomSheetFragment.kt:45-120 | Missing | |
| 2 | "Add new visit" row appears only while an eye is still available: none done → Left/Right/Both. Left done → Right only. Right done → Left only. Both or L+R done → no add | EyeSurgeryBottomSheetFragment.kt:103-114, :122-150; EyeSurgeryListViewModel.kt:19-38 | Missing | |
| 3 | If only one eye is left, the form opens with that eye **pre-selected and locked** | EyeSurgeryBottomSheetFragment.kt:128-138; EyeSurgeryFormViewModel.kt:128-168 | Missing | |

### Eye surgery / cataract form (`EyeSurgeryFormFragment`, ui/home_activity/all_ben/eye_surgery_registration/EyeSurgeryFormFragment.kt) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Whole form (server-driven schema `EYE_SURGERY_FORM_ID`, rendered by FormRendererAdapter). Beneficiary name / gender / age header | EyeSurgeryFormFragment.kt:137-170 | Missing | Not in AUDIT_FORMS either (it is not a Dataset.kt form), so field list is also absent |
| 2 | Photo upload field: camera or gallery picker, image compressed to base64 | EyeSurgeryFormFragment.kt:64-127, :211-215 | Missing | |
| 3 | View mode with **Edit FAB**. Save button hidden in view mode | EyeSurgeryFormFragment.kt:180-194, :224-225 | Missing | |
| 4 | Visit-date rules: not in the future; must be after the previous visit date (error text). Date picker limited by min/max | EyeSurgeryFormFragment.kt:200-210, :243-258 | Missing | |
| 5 | Save → toast "Eye surgery details saved successfully" / "Failed to submit form", back to list (list refreshes button colour) | EyeSurgeryFormFragment.kt:296-314 | Missing | |

### IFA distribution form (`BenIfaFormFragment`, ui/home_activity/all_ben/ben_ifa/BenIfaFormFragment.kt) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Opened from the **IFA** button on the EC registration list card: female, age 20-49, reproductive status 1 or 2 | res/layout/rv_item_ben_ec_with_form.xml:352-354; BindingUtils.kt:552-561 | Missing | The All-beneficiaries IFA route (AllBenFragment.kt:233-241) is dead (button always passes isIFA=false) |
| 2 | History table of IFA bottles already given (date + bottles) | BenIfaFormFragment.kt:193-205 | Missing | |
| 3 | "Add new" button reveals the form. **Only one entry per month**: otherwise toast "Already added for this month" | BenIfaFormFragment.kt:177-188; BenIfaFormViewModel.kt:62-66 | Missing | |
| 4 | Visit-date rules (not future, after previous, already-filled dates blocked), photo upload, server-driven fields | BenIfaFormFragment.kt:233-270, :125-136; BenIfaFormViewModel.kt:239-270 | Missing | |

### Add member · choose type (`NewBenRegTypeFragment`, ui/home_activity/all_ben/new_ben_registration/NewBenRegTypeFragment.kt) — design frame: M2.13-02 · Add member · choose type

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Consent dialog on entry (tick required, Cancel exits) | NewBenRegTypeFragment.kt:38-66, :106-107 | Missing | |
| 2 | **Beneficiary-ID stock warning**: when few IDs are left, the warning "running low, connect to internet". When none are left, Continue is hidden ("no more IDs, connect to internet"). Both trigger an ID fetch | NewBenRegTypeFragment.kt:115-139 | Missing | This rule matters on every registration entry |
| 3 | Draft dialog per type (Open draft / Create new) | NewBenRegTypeFragment.kt:68-87, :158-180 | Missing | |
| 4 | No selection → toast "Please select type of beneficiary" | NewBenRegTypeFragment.kt:182-186 | Missing | |
| — | **Note:** this screen is **not reachable in code** today (nothing navigates to it), and the "Child" path does nothing (:142-145). The live flow is the gender + relation dialog (Households #5). Design M2.13-02 is based on this dormant screen | | | |

### Adult registration, 3-page legacy (`NewBenRegG15Fragment` / `NewBenRegG15ObjectFragment`, ui/home_activity/all_ben/new_ben_registration/ben_age_more_15/) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | 3 tabs "Beneficiary details / ID details / Reproductive status" with Prev / Next / Submit, error dialog, GPS-off dialog, save then back to Home | NewBenRegG15Fragment.kt:83-228 | Missing | Reachable only from the unreachable NewBenRegTypeFragment, so legacy. Design 07's 3-step stepper reads like this. Confirm which flow is current |

### Register member form (`NewBenRegFragment` + `PreviewBottomSheet` + `AgePickerDialog`, ui/home_activity/all_ben/new_ben_registration/ben_form/) — design frame: 07 · Register member – Step 1 (+ 2.15 field spec BenRegFormDataset)

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Preview bottom sheet** before saving: shows all entered values with "Edit" / "Submit" | NewBenRegFragment.kt:615-620, :979-1004; PreviewBottomsheet.kt | Missing | |
| 2 | After save, **"Add spouse?"** dialog when the HOF is married and this person isn't ("Would you like to add <name>'s wife/husband?"): Yes opens the form again for the spouse | NewBenRegFragment.kt:227-270, :808-812 | Missing | |
| 3 | After save, **"Add children?"** dialog when "Do you have children" = yes: Yes opens Child registration | NewBenRegFragment.kt:272-316, :814-819 | Missing | |
| 4 | **Link to household** button for a non-household person (view of existing record): choose "Link to existing household" (searchable household picker → relation-to-HOF picker with same filtering rules → toast "Beneficiary linked") or "Create new household" (household form in link mode) | NewBenRegFragment.kt:369-503, :632-635 | Missing | |
| 5 | Mobile **OTP verification**: Send OTP / Resend OTP with countdown timer. OTP auto-validated at 6 digits when online | NewBenRegFragment.kt:654-712, :1084-1105 | Partial | Spec lists "Send OTP" / "Enter OTP" fields. Timer, resend and auto-verify behaviour are missing (ABHA OTP pattern M2.10-04 could be reused) |
| 6 | ABHA / Ayushman lookup with prefill (Mitanin), "ABHA exists" → create-ABHA dialog, network error toast. Mitanin new non-HOF members: Submit gated on ABHA fetch (currently commented) | NewBenRegFragment.kt:350-367, :568-612, :104-107 | Partial | Same as New household #3 |
| 7 | Birth-certificate upload front/back: camera / gallery / **PDF**. File-size toast. View the uploaded image or PDF. Disabled on Mitanin | NewBenRegFragment.kt:183-210, :716-735, :845-872, :941-963, :1111-1150 | Partial | Spec lists the FILE_UPLOAD fields. PDF option, viewer and Mitanin hide rule are missing |
| 8 | Photo capture from camera | NewBenRegFragment.kt:923-939 | Partial | 07 shows "Add photo (optional)". Camera-only (no gallery) is not specified |
| 9 | Age picker dialog (years / months / days spinners with min/max) as an alternative to DOB | AgePickerDialog.kt:41-139 | Partial | 07 helper says "enter age in years instead". Months/days and limits are missing |
| 10 | View mode for an existing person with **Edit FAB**. **Edit hidden for dead beneficiaries** | NewBenRegFragment.kt:624-631, :839-842 | Partial | Generic view pattern only. Death lock is missing |
| 11 | Consent dialog for new registrations (Mitanin text variant) | NewBenRegFragment.kt:505-545, :1055 | Missing | |
| 12 | Back / after-save navigation: no-household person → back to Non-household list. Otherwise back to the household | NewBenRegFragment.kt:81-100 | Missing | |
| 13 | Voice input on text fields | NewBenRegFragment.kt:109-116 | Missing\* | |
| 14 | Save auto-triggers an upload sync | NewBenRegFragment.kt:806 | Partial | |

### Child registration from mother card (`NewChildAsBenRegistrationFragment`, ui/home_activity/all_ben/new_ben_registration/new_child_ben/NewChildAsBenRegistrationFragment.kt) — design frame: NO SCREEN IN DESIGN

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Opened from "Register Children" / "View Children" on a mother's card. **Bulk-register up to 9 children** in one form (number of live children, then name / DOB / auto age / sex per child, with **auto-calculated gaps** between marriage and child 1 and between consecutive children, and live male/female counts) | NewChildAsBenRegistrationFragment.kt:238-305; NewChildBenRegDataset in AUDIT_FORMS | Missing | Fields exist in the 2.16 spec, but there is no screen, entry point or "View children" mode |
| 2 | Consent dialog, GPS-off settings dialog, view mode + Edit FAB, save toast, auto-sync | NewChildAsBenRegistrationFragment.kt:72-135, :159-160, :208-234 | Missing | |

### Get beneficiary data (`GetBenFragment`, ui/home_activity/get_ben_data/GetBenFragment.kt) — design frame: M2.11-06 · Get beneficiary data

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Loads the server beneficiary list page by page, with **page-number buttons** (toast "Page N clicked") | GetBenFragment.kt:48-89; GetBenViewModel.kt:31-52 | Partial | Design shows search by ID/mobile + "Download" per record + full download. That is a different model |
| 2 | Server error state (empty result → error panel) | GetBenFragment.kt:58-62 | Missing | |
| — | **Note:** this screen is **not reachable** in code (drawer item commented out, activity_home_drawer.xml:81). Design adds it under Profile → Tools | | | |

### Login activity (`LoginActivity`, ui/login_activity/LoginActivity.kt) — design frame: 01 · Login (+ M2.13-L6 Assamese)

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Incomplete install** dialog (app installed without its native part): "Open Play Store" / "Exit" | LoginActivity.kt:171-195 | Missing | |
| 2 | Rooted / emulator block (release) | LoginActivity.kt:95-104 | Missing | |
| 3 | Account-deactivated dialog | LoginActivity.kt:109-126 | Missing | |
| 4 | Notification-permission request on first open (Android 13+) | LoginActivity.kt:160-169 | Missing | |

### Sign in (`SignInFragment`, ui/login_activity/sign_in/SignInFragment.kt) — design frame: 01 · Login, M2.13-L6, M2.13-04 (work location), M2.13-05 (forgot password)

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **"Remember me"** checkbox. If username + password are remembered, the app **logs in automatically** on open | SignInFragment.kt:213-229, :240-248; layout fragment_sign_in.xml:227-230 | Missing\* | Not in M2.13-L6 |
| 2 | **Different-user login** dialog: "<user> is logged in on this phone… N records not synced". Yes: if unsynced records exist, it only pushes them; otherwise it logs out the old user and clears data | SignInFragment.kt:67-104, :336-339 | Missing | Data-loss guard |
| 3 | **Offline login** with the cached password when the same user signs in without internet. Wrong cached password → "Invalid Password" | SignInFragment.kt:311-334 | Missing | |
| 4 | Login error text shown under the fields | SignInFragment.kt:232-238 | Missing\* | |
| 5 | Language picker on the login screen (bottom sheet; app restarts) | SignInFragment.kt:176-188 | Partial | Design language sheet is only in Profile (M2.11-08) |
| 6 | "Delete my account" link on the login screen (external form, URL by flavour) | SignInFragment.kt:190-209; fragment_sign_in.xml:235-238 | Missing\* | |
| 7 | Role routing after login: Supervisor / ANM / CHO → Supervisor app with auto location (first village). ASHA → work-location screen only if no location is saved, else Home | SignInFragment.kt:253-280 | Partial | M2.13-04 is always shown after login in the prototype (NAV.primary 01 → M2.13-04). The "only first time" rule and role routing are missing |
| 8 | "DEMO" tag on non-production Mitanin builds. "Powered by … version" footer | SignInFragment.kt:125, :288-291 | Missing | |
| 9 | Remembered username/password prefilled | SignInFragment.kt:219-227 | Missing\* | |

### Language sheet (`LanguageBottomSheet`, ui/login_activity/LanguageBottomSheet.kt) — design frame: M2.11-08 · Change language

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | 4th option **Bangla** (non-Mitanin) | LanguageBottomSheet.kt:89-100 | Missing | Design shows English / Hindi / Assamese only |
| 2 | **Mitanin builds show only English + Hindi** (no Assamese/Bangla) | LanguageBottomSheet.kt:55, :78 | Missing | Flavour rule not noted |
| 3 | Selecting a language applies at once (no Apply button) | LanguageBottomSheet.kt:36-50 | Partial | Design adds an "Apply" button. Fine if this is an intended change |

---

No gaps: none. Every screen in scope has at least one gap. (MonthlyRecapStrip is covered under Modules. PreviewBottomSheet and AgePickerDialog under Register member. NewBenRegG15ObjectFragment under the 3-page legacy form.)

#### Side observations (code, not design, so not counted)
- HouseholdMembersFragment.kt:421-433: Cataract button tap opens the visit sheet **and** also navigates straight to the eye-surgery form (both fire for ASHA). Likely a bug.
- AllBenFragment.kt:303-307: non-household "Add beneficiary" `else` branch navigates with an AllHousehold action and is unreachable.
- Strings: `age_above_30_years` text is "Age above 40 years" while AbhaBen calls it AGE_ABOVE_30.
- Design adds features not in code: Forgot password (M2.13-05; the code button is `visibility=gone`, fragment_sign_in.xml:246-254), GetBen search by ID/mobile.

---

## Profile, drawer, sync, incentives, gamification & utilities: functionality missing from the design
Summary: 19 screens checked, 39 missing features, 43 partial.

Paths below are relative to `app/src/main/java/org/piramalswasthya/sakhi/` unless they start with `res/`. Design sources are in `design/figma-plugin/src/`: 10-core-phase1 (08 Sync, 09 Profile & more), 31-stage-2.1-gamification (M2.1-xx), 41-stage-2.11-profile (M2.11-xx), 43-stage-2.13-polish (M2.13-04), 44-stage-2.14-navigation (links), 30-stage-2.0-kit (M2.0-P8, snackbars).

---

### Home shell: side drawer, toolbar and app-wide dialogs (`HomeActivity`, ui/home_activity/HomeActivity.kt + res/menu/activity_home_drawer.xml, res/menu/home_toolbar.xml)  — design frame: 09 · Profile & more (gamification ON) / M2.1-11 · Profile (gamification OFF) / 02 · Home – Today (app bar)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Logout confirmation that warns how many records are still unsynced ("N not processed" or "All records synced", then "Are you sure?"). On Yes it removes the FCM token, deletes beneficiary images and cancels all background work | Always on Log out (HomeActivity.kt:203-226, 941-945) | Missing | Design has only a "Log out" row that links straight to Login (44-…navigation:37). No confirmation and no data-loss warning |
| 2 | Role gate in the drawer. Supervisor, ANM and CHO logins hide "Home" and show "Supervisor". ASHA logins hide "Supervisor" | Role check on the logged-in user (HomeActivity.kt:308-314) | Partial | The ASHA profile list in the design always shows "Supervisor" (10-core:…'Supervisor','group'; M2.1-11). There is no rule saying it is hidden for ASHA |
| 3 | The Incentives menu item is renamed "Monthly claim summary" on Mitanin builds | `FLAVOR.contains("mitanin")` (HomeActivity.kt:895-903) | Partial | Design always uses "Incentives" |
| 4 | "Sync records" action. It pushes data, and also pulls if the first full download has not finished. A second tap within 15 min shows the toast "Please wait Syncing in Progress" | HomeActivity.kt:911-926 | Partial | Design has "Sync now" (08 · Sync) but no cooldown/disabled state, and no "first download still pending" state |
| 5 | Tapping the toolbar title on Home opens the change work village screen (ServiceLocationActivity) | Only on Home, when the home shortcut icon is hidden (HomeActivity.kt:131-135, 740-755) | Missing | M2.13-04 says "You can change this later in Profile", but neither Profile list (09 / M2.1-11) has a "Change village / work location" item. No way in |
| 6 | Tapping the avatar in the drawer header opens the gallery to set a profile photo, which is saved locally and shown right away | HomeActivity.kt:228-239, 874-877 | Partial | Design header shows an edit pencil, and photo change only appears inside Edit profile (M2.11-02). The quick gallery pick from the header is not shown |
| 7 | The drawer header shows "Facility: <sub-centre>". The Employee ID falls back to the login User ID when the employee ID is blank | HomeActivity.kt:837-860 | Partial | Design shows "ASHA · Employee ID" and "village · block". No facility line, and the blank-ID fallback is not specified |
| 8 | "Delete my account" opens an external Microsoft Forms page in the browser. The form URL depends on the flavour (saksham/niramay/xushrukha get one form, others another). A toast appears if no browser is available | HomeActivity.kt:954-976 | Partial | Design shows an in-app warning banner with a "Delete" button (M2.11-09). The real flow is an external form, and the flavour-specific URLs are not captured |
| 9 | "Support" opens an external Microsoft Forms support page | HomeActivity.kt:978-993 | Partial | Design "Help & support" (M2.11-09) lists helpline/WhatsApp/videos/report problem. The actual "Support form" link is not there |
| 10 | "Help & support" (LMS) and "Report crash" drawer items are hidden in code, so LMS is not reachable from the drawer | HomeActivity.kt:318-319 | Partial | Design shows "Video tutorials & LMS" as reachable from Profile (10-core). Product decision needed |
| 11 | Unsupported-device block. On release builds, rooted phones and emulators get a non-cancellable "This app cannot run on rooted devices or emulators" dialog with Exit. It is checked on create and on every resume | HomeActivity.kt:391-398, 666-673 | Missing | No design trace |
| 12 | "Account deactivated" dialog with the server's message, non-cancellable | On a deactivation event from the API layer (HomeActivity.kt:486-503) | Missing | No design trace |
| 13 | "Session expired" dialog, non-cancellable. OK clears prefs, cancels work and sends the user to Login | On forced logout after a token refresh fails (HomeActivity.kt:507-530) | Missing | No design trace |
| 14 | In-app update from Play: forced (immediate) or flexible, with an "update downloaded – restart" snackbar and a retry snackbar | On start/resume (HomeActivity.kt:418-419, 677; helpers/InAppUpdateHelper.kt:67-144) | Missing | No design trace |
| 15 | Badge celebration pop-up (confetti, badge art, "Level N" only when level > 1). It closes by itself after a fixed time, or on a tap outside. It has no buttons | Whenever a badge milestone is reached (HomeActivity.kt:763-809) | Partial | M2.1-04 has "See my badges" / "Close" buttons and a story line. Code auto-dismisses, has no buttons, and shows a level label only above level 1 |
| 16 | The language choice on Mitanin builds offers only English and Hindi (no Assamese) | `isMitanin` (HomeActivity.kt:171-180; ui/login_activity/LanguageBottomSheet.kt:55-78) | Partial | M2.11-08 always shows 3 languages |
| 17 | The language toolbar icon shows only on Home. Inner screens show a "Go to Home" icon instead | `showMenuHome` (HomeActivity.kt:683-686, 757-760) | Partial | Design moves language to Profile and uses the bottom nav. Fine, but the "Go home from deep screens" shortcut is not specified for screens without the bottom nav |
| 18 | The bell icon shows an unread-count badge and opens the notification panel | HomeActivity.kt:688-696 (BellBadgeHelper) | Partial | The bell is linked to M2.1-10 (44-…navigation), but no unread-count badge is drawn in the plugin source. Check the Top app bar component |
| 19 | Tapping an incentive push notification (tray) opens the Incentives screen and marks the notification read, on both cold and warm start | HomeActivity.kt:446-482, 253-268 | Partial | The design tray (M2.1-09) shows only the evening/recap notifications. There is no incentive push and no routing to Incentives |
| 20 | Runtime permission prompt at start (phone call, storage, microphone, camera) | Every launch (HomeActivity.kt:532-550) | Missing | No permission-explainer screen in the design |
| 21 | Floating chat-support button and chat web sheet (behind a code flag, currently off) | `isChatSupportEnabled` (HomeActivity.kt:113, 372-388, 553-655) | Missing | Dormant feature, so low priority |

### ASHA profile (`AshaProfileFragment`, ui/home_activity/asha_profile/AshaProfileFragment.kt + AshaProfileViewModel.kt, res/layout/fragment_asha_profile.xml)  — design frame: M2.11-01 · My profile details / M2.11-02 · Edit profile
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | "Add ASHA head" button that registers the ASHA's own household (opens New Household with isAshaFamily = Yes) | Always on the profile screen (fragment_asha_profile.xml:59-66; AshaProfileFragment.kt:109-115) | Missing | No "ASHA's own family" anywhere in the design |
| 2 | ASHA's own household card under the profile: tap to edit the household, view members, or add a member. Only the first household is listed | Shown once the profile is saved (AshaProfileFragment.kt:85-87, 117-160; AshaProfileViewModel.kt:42-44) | Missing | |
| 3 | Add-member dialog: choose gender, then a relation-to-head list that hides relations that can't apply (father/mother already registered, spouse options based on the head's marital status and gender). OK stays disabled until a relation is picked. If the household has 0 members it goes straight to registering the head | AshaProfileFragment.kt:131-143, 282-429 | Missing | Same rule set as the Household "Add member" flow, but on the profile screen |
| 4 | First-time profile (nothing saved yet) opens editable with a Save button. After saving it becomes read-only with an Edit button | `recordExists` (AshaProfileFragment.kt:83-95, 163-168, 180-185) | Partial | Design shows only "view + Edit profile" (M2.11-01→02). The empty first-time state is not designed |
| 5 | Profile is pulled from the server on open, with a full-screen loader until the form is ready | AshaProfileViewModel.kt:91-95; AshaProfileFragment.kt:79, 98-104 | Partial | No loading/fetch-failed state for the profile in the design |
| 6 | Mobile number, ASHA name, village, DOB, age and date of joining are read-only (text views). Only alternate mobile, father/spouse, bank, population and contacts are editable | AshaProfileDataset field types (25-audit-data AUDIT_FORMS) | Partial | M2.11-02 shows "Mobile number *" as an editable field. In code it is locked |
| 7 | Save result toasts ("Save Successful" or failure) and a saving spinner | AshaProfileFragment.kt:170-196 | Partial | Design doesn't specify a save confirmation (the kit's Snackbar Type=Success could cover it) |

### Sync status quick sheet (`SyncBottomSheetFragment`, ui/home_activity/sync/SyncBottomSheetFragment.kt + res/layout/bottom_sheet_sync.xml, adapters/SyncStatusAdapter.kt)  — design frame: 08 · Sync
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Table per form type with three counts: Synced / Un-synced / Syncing | Opened from the cloud icon in the toolbar (HomeActivity.kt:718-731; bottom_sheet_sync.xml:78-95) | Partial | 08 shows one total, a "waiting" count and a single status badge per module. The separate "Syncing" (in-flight) count is not shown |
| 2 | Opens as a quick bottom sheet over any screen (double-tap safe), sized to the content and capped in height | HomeActivity.kt:718-731; SyncBottomSheetFragment.kt:39-41 | Partial | Design replaces it with a full Sync tab. No quick-look overlay from inner screens |

### Sync dashboard · Status tab (`SyncDashboardFragment` / `SyncStatusTabFragment`, ui/home_activity/sync_dashboard/*)  — design frame: M2.11-07 · Sync log (tab "Status" drawn as label only) / 08 · Sync
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Background-sync status line: Idle / "Sync Running (x / y workers)" / Complete | WorkManager push+pull state (SyncStatusTabFragment.kt:89-136; SyncDashboardViewModel.kt:34-35) | Missing | Design shows record-level sync state only, never the engine state |
| 2 | "Failed: N worker(s) – tap to see details". Expands a list of each failed sync step with its error. Steps that never ran show "Blocked by earlier failure in sync chain" | Only when a worker failed (SyncStatusTabFragment.kt:79-83, 109-118; SyncDashboardViewModel.kt:37-63) | Partial | M2.11-07 has an "Errors (2)" tab label but no content. 08 shows a record-level failure note, not the technical error list |
| 3 | Overall progress "x / y records synced" with a bar, plus a per-form table | SyncStatusTabFragment.kt:58-71 | Partial | Drawn on 08 (records-synced line). The Status tab of the Sync log itself is not drawn |

### Sync dashboard · Logs tab (`SyncLogsTabFragment`, ui/home_activity/sync_dashboard/SyncLogsTabFragment.kt)  — design frame: M2.11-07 · Sync log
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Pause/Resume live log button that freezes the list while reading | SyncLogsTabFragment.kt:42-48, 76 | Missing | |
| 2 | "Clear Logs" button, which clears right away with no confirmation | SyncLogsTabFragment.kt:38-40 | Missing | |
| 3 | Empty state "No sync logs yet. Trigger a sync to see activity." | When there are no logs (SyncLogsTabFragment.kt:80-81) | Missing | |
| 4 | Export shows the toast "No log files to export" when there is nothing to share | SyncLogsTabFragment.kt:63-70 | Missing | "Share log with support" itself is designed |
| 5 | Live log auto-scrolls to the newest line, but only if the user was already at the bottom | SyncLogsTabFragment.kt:78-87 | Missing | Minor |

### Incentives · month summary (`IncentivesFragment`, ui/home_activity/incentives/IncentivesFragment.kt + IncentiveClaimWindow.kt + IncentivesViewModel.kt, res/layout/fragment_incentives.xml, rv_item_incentive_grouped.xml)  — design frame: M2.11-03 · Incentives · month
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Month–year picker dialog (any month back to 2020) plus a "Submit" button that loads that month | IncentivesFragment.kt:191-235, 378-394, 631-640 | Partial | Design has 3 month chips only. No way to reach older months, and no explicit load step |
| 2 | Claim button visibility rule. Non-Mitanin: last month only until the 12th of this month, older months always, current month never. Mitanin: only exactly last month, any day of this month. Toast "Claim is not allowed for the selected month" | IncentiveClaimWindow.kt:28-68; IncentivesFragment.kt:446-460 | Partial | Design shows "Submit October claim" and mentions "before 12 Nov" in a banner. The hidden/disallowed states and the Mitanin rule are not designed |
| 3 | Claim button states: Claim → "Claiming…" (disabled) → "Claimed" (disabled). After a month-level rejection, Claim is enabled again (non-Mitanin) | IncentivesFragment.kt:463-511, 537-615 | Partial | No loading/claimed/disabled states in the design |
| 4 | Mitanin-only "Reclaim" at month level that resubmits only the rejected records' IDs | `isMitaninVariant && rejected (status 103)` (IncentivesFragment.kt:414-422, 520-532) | Partial | Design reclaim is per activity on the detail screen (M2.11-04) and not flavour-gated |
| 5 | "Incentive claim status" pill (Verified green / Pending orange / Rejected red) shown once a claim exists | IncentivesFragment.kt:465-504; fragment_incentives.xml:192-213 | Partial | Design uses per-activity tags plus an error banner. There is no month-level claim status pill |
| 6 | Claim status dialog (tap the pill): Verified by / Rejected by (name + role; on Mitanin, "ASHA Supervisor" shows as "Mitanin Trainer"), verified/rejected date, claim date, rejection reason. Pending hides the date and reason | IncentivesFragment.kt:107-172, 322-340 | Partial | M2.11-04 shows "Rejected by … · date" plus the reason. There is no dialog, no claim date, no verified-by view and no role mapping |
| 7 | Totals: Pending (unpaid) amount and Processed (paid) amount | IncentivesFragment.kt:346-358; fragment_incentives.xml:347-372 | Partial | Design stats are Claimed / Verified / Rejected. The paid vs unpaid split is not shown |
| 8 | Download the "ASHA Incentive master claim form" PDF (activity table, certification, ASHA bank/contact and signature blocks) by tapping the Pending total. It is saved to Downloads, with a snackbar to open it and a storage-permission dialog that leads to Settings | Not on Mitanin, and not for Chhattisgarh state (state id 8), which shows the toast "download not available" (IncentivesFragment.kt:620-629, 666-1230, 1460-1505; IncentivesViewModel.kt:96) | Missing | No PDF/claim-form download anywhere in the design |
| 9 | Activities grouped by programme group in collapsible sections with group subtotals | adapters/IncentiveGroupSectionAdapter.kt:12-80 | Missing | Design is a flat list of 5 activities |
| 10 | Per-activity eligibility icon: green tick = eligible, red cross = documents needed. Hidden for fixed monthly incentives on non-Mitanin. Fixed monthly ("PER_MONTH") incentives are sorted to the top | rv_item_incentive_grouped.xml:67-77; IncentivesViewModel.kt:191-215 | Missing | Design tags show Verified/Pending/Rejected/Draft only |
| 11 | Table columns S.No / Activity / No. of claims / Amount claimed / Doc. The Amount and Doc headers only show on Mitanin | IncentivesFragment.kt:642-646; fragment_incentives.xml:257-317 | Partial | Design rows show "claims · ₹amount" without the Doc column or its flavour rule |
| 12 | Tapping an activity opens the beneficiary list only if a record has a beneficiary or (non-Mitanin) any record is not eligible. Otherwise the tap does nothing | IncentivesFragment.kt:253-278 | Partial | Design implies every activity opens |

### Incentives · activity detail (`IncentivesSubFragment`, ui/home_activity/incentives/IncentivesSubFragment.kt + adapters/IncentiveListAdapter.kt, res/layout/rv_item_incentive.xml, utils/UploadSourceDialog.kt, utils/FileViewerDialog.kt)  — design frame: M2.11-04 · Rejected activity → reclaim
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Row shows serial number, beneficiary ID + name, RCH ID, ABHA ID and amount | rv_item_incentive.xml:31-97 | Partial | Design record card shows name and one meta line only |
| 2 | Upload supporting documents. The source chooser offers Camera / Gallery / PDF document. Max 4 files and 5 MB each; photos are auto-compressed. Oversize files are listed in a toast, and the file-count badge is shown on the button | Only non-Mitanin AND record not eligible (rv_item_incentive.xml:114-151; IncentivesSubFragment.kt:60-64, 149-170, 189-560) | Partial | Design has a single "Add photo" action. No source choice, limits, PDF support or counter |
| 3 | View uploaded files dialog with delete per file ("Delete File?" confirmation). Delete is blocked after submission | Same gate as row 2 (IncentivesSubFragment.kt:588-620, 689-700; utils/FileViewerDialog.kt:135-167) | Missing | |
| 4 | Per-beneficiary "Submit" of documents. It asks to upload files first and shows "Already submitted" if done. The confirmation says "Submit N file(s)… cannot be undone", the button becomes "Submitted", and on success the app returns to the list and re-pulls | Non-Mitanin, record not eligible (rv_item_incentive.xml:160-170; IncentivesSubFragment.kt:121-145, 621-686; IncentivesViewModel.kt:249-298) | Partial | Design has one "Reclaim ₹600" button for the whole activity. Per-record submit and its confirmation are not designed |

### My badges (`BadgeShelfFragment`, ui/home_activity/badges/BadgeShelfFragment.kt + adapters/BadgeShelfAdapter.kt)  — design frame: M2.1-02 · My badges / M2.1-03 · Badge detail
No gaps. The code is a plain catalogue (art, name, how to earn, locked tiers dimmed). The design has more than the code.

### My journey (`JourneyFragment`, ui/home_activity/journey/JourneyFragment.kt + JourneyViewModel.kt)  — design frame: M2.1-05 · My journey
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Narrative changes with today's work: zero / one / busy messages | JourneyFragment.kt:39-48 | Partial | Design shows only the "busy" story. A zero-activity day is not designed |
| 2 | Badge row shows the top 3 badges closest to their next milestone (in progress, not only earned) | JourneyViewModel.kt:38-43 | Partial | Design shows "Recent badges" (earned) |

### Monthly recap playback (`MonthlyRecapPlaybackFragment`, ui/home_activity/monthly_recap/*)  — design frame: M2.1-07 · Monthly Recap · scene / M2.1-08 · end
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Pause/Play button. Pausing also pauses the animation and the music | Hidden on the last (goodbye) scene (MonthlyRecapPlaybackFragment.kt:66, 142, 308-332) | Missing | Design shows only prev/next chevrons and close |
| 2 | Background music on/off toggle that mutes only the music while the story keeps playing | MonthlyRecapPlaybackFragment.kt:67-69, 334-357; RecapMusicController.kt | Missing | |
| 3 | Story gestures: tap right 2/3 = next, tap left 1/3 = back, press-and-hold = pause | Not on the goodbye scene (MonthlyRecapPlaybackFragment.kt:147, 362-400) | Missing | |
| 4 | Resume where she left off ("Continue Recap"). Progress is saved per scene and playback restarts at the saved scene | MonthlyRecapPlaybackViewModel.kt:54-90 | Partial | Design has no resume state (the Home recap strip states are someone else's scope) |
| 5 | Loading state before the first scene. Closes straight away if there is no story | MonthlyRecapPlaybackFragment.kt:112-123; ViewModel.kt:55-58 | Missing | |
| 6 | Each scene's reading time adapts to the text length. The last scene never auto-advances or returns home by itself | MonthlyRecapPlaybackFragment.kt:223-262, 440-450 | Partial | Timing rules are not specified in the design |

### Learning / Help & support hub (`LmsFragment`, ui/home_activity/lms/LmsFragment.kt + configuration/IconDataset.kt:476-492)  — design frame: M2.11-05 · Learning · videos & guide
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | "User guide" tile opens a bundled PDF in an external PDF viewer app, with the toast "No PDF viewer found" if none is installed | LmsFragment.kt:58-66, 100-131 | Partial | Design has a "User guide (9)" tab of items. In code it is a single PDF opened outside the app |
| 2 | The screen is not reachable today because its drawer item ("Help & support") is hidden | HomeActivity.kt:318 | Partial | See Home shell #10 |

### Video tutorial player (`VideoTutorialFragment`, ui/home_activity/lms/videoTutorial/VideoTutorialFragment.kt)  — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Full-screen video player that auto-plays and loops | VideoTutorialFragment.kt:… setOnPreparedListener | Missing | M2.11-05 lists videos but has no player screen |
| 2 | Play/Pause overlay button that auto-hides after 3 s. Tapping the video toggles play | VideoTutorialFragment.kt (videoView/btnPlayPause click, hidePlayPauseButtonWithDelay) | Missing | |
| 3 | Seek bar to scrub | VideoTutorialFragment.kt (seekBar listener) | Missing | |
| 4 | Double-tap to jump forward 10 s | VideoTutorialFragment.kt (gestureDetector.onDoubleTap) | Missing | |

### In-app notifications panel (`NotificationPanelFragment`, ui/notifications/NotificationPanelFragment.kt + NotificationAdapter.kt + NotificationPanelViewModel.kt)  — design frame: M2.1-10 · Notifications panel
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Swipe left/right to dismiss a single notification | NotificationPanelFragment.kt (attachSwipeToDismiss) | Missing | |
| 2 | "Clear all" button with a confirmation dialog | NotificationPanelFragment.kt (btnClearAll, confirmClearAll) | Missing | |
| 3 | Empty state when there are no notifications | NotificationPanelFragment.kt (layoutEmpty) | Missing | |
| 4 | Tapping marks the item read. Incentive notifications (including approval and unrecognised incentive targets) close the panel and open Incentives. Other types only mark read | NotificationPanelFragment.kt (onNotificationTapped) | Partial | Design shows read/unread dots but no tap destinations. "2 records failed to sync · tap to fix" implies routing that code does not have |
| 5 | Panel refreshes from the server on open | NotificationPanelViewModel.kt (init → refresh) | Partial | No loading/refresh state |
| 6 | The same panel opens from the Supervisor/ANM/CHO app with a role-coloured header | NotificationPanelFragment.kt (open(fm, toolbarColor)) | Missing | Supervisor-side notifications are not designed (M2.12 has none) |

### Push notifications in the system tray (`FBMessaging`, ui/home_activity/FBMessaging.kt)  — design frame: M2.1-09 · Evening notification (9 PM)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Server push notifications (e.g. incentive approved/rejected) appear in the tray with expandable text. Each one stacks separately | Every FCM message (FBMessaging.kt showNotification) | Partial | M2.1-09 shows only local evening/recap notifications. No server/incentive push example |
| 2 | Tapping a push opens the Supervisor app for Supervisor/ANM/CHO roles and the ASHA app otherwise, then deep-links (incentives) | Role check (FBMessaging.kt showNotification) | Missing | No role-based destination in the design |

### Choose work location (`ServiceLocationActivity`, ui/service_location_activity/ServiceLocationActivity.kt + ServiceLocationViewModel.kt)  — design frame: M2.13-04 · Choose work location (after login)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | State, District and Block are fixed, read-only values from the user's profile, shown in the current language. Only Village is selectable | ServiceLocationViewModel.kt:92-157; Activity (actv*Dropdown, commented-out block dropdown) | Partial | Design shows "Block *" as a dropdown |
| 2 | Village is auto-selected when the ASHA has only one village | ServiceLocationActivity.kt (villageList.size == 1) | Missing | |
| 3 | "Missing Detail – Please select Village" dialog on Continue without a village | ServiceLocationActivity.kt (incompleteLocationAlert) | Missing | |
| 4 | Back button: goes home if a location is already saved, otherwise asks "Exit Application?" (Yes/No) | ServiceLocationActivity.kt (onBackPressedCallback, exitAlert) | Missing | |
| 5 | If no logged-in user is found, it bounces to Login | ServiceLocationActivity.kt (isNoUserFound) | Missing | Edge case |
| 6 | Reached again later by tapping the Home toolbar title | HomeActivity.kt:131-135 | Missing | See Home shell #5. The design promises "change later in Profile" but has no entry |

---

No gaps: My badges (`BadgeShelfFragment`), Dashboard (`DashboardFragment`: a placeholder "Hello" screen not linked from the drawer; the design's M2.0-P8 is new scope), Help (`HelpFragment`: "COMING SOON" placeholder, not linked), About us (`AboutUsFragment`: static text, not linked; M2.11-09 covers "About"), User guide (`UserGuideFragment`: dead code, not in nav_home.xml), `MessageUpdate.kt` (interface only, no UI).

Out of scope here, covered by other groups: Get beneficiary data (get_ben_data), ABHA activity, Supervisor activity/drawer, Login/Language bottom sheet internals.

---

## Group B: Maternal health, HRP hub, Infant (HBNC/HBYC): functionality missing from the design

Summary: 28 screens checked, 52 missing features, 56 partial (cross-cutting C1–C8 counted once).

Scope: `ui/home_activity/maternal_health/**`, `ui/home_activity/hrp_cases/**`, `ui/home_activity/infant/**`.
Paths below are relative to `app/src/main/java/org/piramalswasthya/sakhi/`, and layouts to `app/src/main/res/layout/`.
Design sources checked: `32-stage-2.2-maternal.js`, `30-stage-2.0-kit.js` (P2/P4/P5), `34-stage-2.4-child.js`, `36-stage-2.6-hrp-cho.js`, `38-stage-2.8-disease.js` (M2.8-14/15), `42-stage-2.12-supervisor.js`, `44-stage-2.14-navigation.js`, `45-stage-2.15-specs.js` + `25-audit-data.js` (AUDIT_CARDS only lists the static labels and buttons in the card XML. Buttons that code shows, hides or renames at runtime are not included).

---

### Cross-cutting gaps (apply to most list or form screens below; counted once here)

| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| C1 | **Voice search** (mic button that fills the search box from speech) | Every list here: e.g. `PwRegistrationListFragment.kt:142`, `PwAncVisitsListFragment.kt` ibSearch, PNC, Abortion, Infant, Child reg, HWC, PMSMA lists | Missing | No mic, voice or speech anywhere in the design. The search bar only has typing. |
| C2 | **Sort sheet**: Newest first / Oldest first / Age-wise / Syncing first / Unsynced first, with the chosen sort shown as a label above the list | `PwRegistrationListFragment.kt:99` (same block in ANC, PMSMA-HR, PNC, Abortion, Infant reg, Child reg, HWC lists) | Missing | The design has filter chips and tabs but no sort control. |
| C3 | **ASHA Supervisor read-only use of the lists**: PW registration, ANC visits, Delivery outcome and HWC referral lists open inside SupervisorActivity. Register/Add ANC/PMSMA/Home-visit/View buttons are hidden for the supervisor role | `nav_supervisor.xml:56-80`. `PwRegistrationListAdapter.kt:55`. `AncVisitListAdapter.kt:111`. `AncVisitAdapter` (View hidden). `BenListAdapterForForm.kt:59`. `HwcReferredFragment.kt` onStart | Missing | Supervisor stage M2.12 only covers incentive verification. There is no supervisor view of beneficiary lists. |
| C4 | **Edit after save**: a saved form opens read-only with an Edit (pencil) button that unlocks it. PW registration and Abortion can always be edited. ANC, PMSMA and ANC home visit can be edited only when opened from the **latest** visit. Delivery outcome, PNC and Infant reg have no edit (Submit disabled) | `PregnancyRegistrationFormFragment.kt:43,71`. `PwAncFormFragment.kt:279`. `PmsmaFragment.kt:119`. `AntenatalCounsellingFragment.kt:198`. `PwAncAbortionFormFragment.kt:157`. `DeliveryOutcomeFragment.kt:106`. `PncFormFragment.kt:97` | Partial | Design P5 "Submitted · view only… Ask your ANM if something needs correcting" says no edit at all, which contradicts code. The "latest visit only" rule is not shown. |
| C5 | **Mitanin hides all document uploads**: MCP card front/back (ANC, Delivery), JSY voucher, delivery and abortion discharge summaries, SNCU discharge summaries (Infant reg, PNC) | `PregnantWomanAncVisitDataset.kt:475`. `DeliveryOutcomeDataset.kt:264,466`. `PncFormDataset.kt:276`. `InfantRegistrationDataset.kt:325,376`. `PregnantWomanAncAbortionDataset.kt:189`. Upload tap ignored in `PwAncFormFragment.kt:189`, `PwAncAbortionFormFragment.kt:97`, `InfantRegFragment.kt:97` | Partial | The design shows MCP photo tiles and Documents file rows with no flavour note. |
| C6 | **Upload chooser and viewer**: dialog "Pick from Gallery / Open Camera / Pick a PDF / Cancel" (PDF only on ANC MCP card, Delivery outcome, Child-reg birth certificate; hidden elsewhere). "File too large" toast. Tapping an uploaded file opens an image viewer or hands a PDF to an external app | `layout_media_options.xml`. `PwAncFormFragment.kt:69,84`. `PncFormFragment.kt:206`. `PwAncAbortionFormFragment.kt:234`. `InfantRegFragment.kt:196` | Partial | The kit Photo tile (camera or gallery) and File row exist. There is no PDF option, no size-limit error, and no full-screen viewer. |
| C7 | **Card status button colour**: red "Register"/"Add" when no record, green "View" when a record exists | `PwRegistrationListAdapter.kt:62-64`. `InfantRegistrationAdapter.kt`. `ChildRegistrationAdapter.kt`. `AncAbortionListAdapter.kt:36-41`. `BenListAdapterForForm.kt:114` | Partial | The design shows only the to-do action ("Register", "Start"). The done/"View" state of the same card is not drawn. |
| C8 | **"Not followed up" warning icon** on a card when the last visit was 90–365 days ago | `AncVisitListAdapter.kt:127`. `PncVisitListAdapter.kt:51` | Missing | No equivalent tag or icon in the design. |

---

### Mother care hub (`MotherCareFragment`, ui/home_activity/maternal_health/MotherCareFragment.kt): design frame M2.2-01 · Maternal health hub
No gaps. All 9 tiles (incl. HWC referral list) with counts are designed.

### Pregnancy registration list (`PwRegistrationListFragment`, maternal_health/pregnant_women_registration/list/PwRegistrationListFragment.kt): design frame M2.2-02 · Pregnancy registration list
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **RCH filter dialog**: "Filter list → Beneficiaries with RCH ID" checkbox + Apply/Cancel | `PwRegistrationListFragment.kt:52-72,112`. `alert_filter.xml` | Missing | The design has tabs (To register / Registered / All) that code does not have, and no RCH filter. |
| 2 | Card button switches **Register → View** once registered. Sync icon appears only after a registration exists | `PwRegistrationListAdapter.kt:60-64` | Partial | The design shows "Register"/"Continue" only. See C7. |
| 3 | Button hidden for supervisor | `PwRegistrationListAdapter.kt:55` | Missing | See C3. |
| + | C1 voice search, C2 sort | | | |

### Pregnancy registration form (`PregnancyRegistrationFormFragment`, maternal_health/pregnant_women_registration/form/): design frame M2.2-03 · Pregnancy registration form
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Prefill from earlier records**: EC registration, last EC-tracking visit date (LMP), existing HRP assessment, number of children, number of completed pregnancies, previous (closed) pregnancy registration | `PregnancyRegistrationFormViewModel.kt:111-120` | Partial | The design shows auto Weeks/EDD only. It does not show which answers come pre-filled from EC or HRP records. |
| 2 | **Saving also writes the "Assess high risk (pregnant)" HRP record** and updates the beneficiary record (e.g. reproductive status) | `PregnancyRegistrationFormViewModel.kt:159-171` | Missing | The design's high-risk banner has no link to the HRP assessment (M2.6-03). |
| 3 | Mitanin: picking a negative blood group auto-sets "Rh negative = Yes" | `PregnantWomanRegistrationDataset.kt:128,1008-1013` | Missing | The flavour rule is not in the design. |
| 4 | Edit FAB on a saved registration (no time limit) | `PregnancyRegistrationFormFragment.kt:43,71` | Partial | See C4. |
| 5 | After save, returns to the list (no next-step prompt) and triggers sync | `PregnancyRegistrationFormFragment.kt:85-90` | Partial | The design's "Register" button does not show what happens next. Minor. |

### ANC visits list (`PwAncVisitsListFragment`, maternal_health/pregnant_woment_anc_visits/list/PwAncVisitsListFragment.kt): design frame M2.0-P2 · Worklist (ANC visits) / M2.2-04 timeline
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **"Delivery status" Delivered / Not delivered switch on the card** → confirm dialog "Has the pregnant woman delivered?" → **delivery-date picker** (between last ANC date and today; error if a later ANC exists) → marks the woman delivered | Switch shown only when weeks of pregnancy > 22: `MaternalHealth.kt:797-798`, `rv_item_pregnancy_visit.xml:408`. Dialog `AncVisitListAdapter.kt:77-104`. Picker `PwAncVisitsListFragment.kt:271-330` | Missing | This is the main shortcut for moving a woman to Delivery outcome. It is not designed. |
| 2 | **Add ANC button greyed (disabled) until the next visit is allowed**: ANC 1 ≥ 5 weeks after LMP, then ≥ 28 days after the last ANC, max 8, and only while a registration is active | `MaternalHealth.kt:723-733`. `AncVisitListAdapter.kt:24-27,158` | Partial | The design shows due/overdue tags but no disabled "not yet due" state. |
| 3 | "ANC visits" history button shown only when at least one visit exists | `AncVisitListAdapter.kt:160` | Partial | P4 history sheet exists. The empty-history state is not shown. |
| 4 | **Mitanin only: "Add home visit" / "View home visits" buttons**. Add only if < 9 home visits and ≥ 30 days since the last one | `AncVisitListAdapter.kt:136-148`. `RecordsRepo.kt:629-657` | Partial | M2.2-04 has a "Home visit" quick action with no flavour gate and no limit or gap rule. |
| 5 | **Different list variants by entry point**: non-follow-up list (source 1), "Due for ANC" (2), "HRP confirmed" (3) | `PwAncVisitsListViewModel.kt:42-46`. Opened from `NonFollowUpFragment.kt:62`, `SchedulerFragment.kt:103,113` | Partial | Nav map points "Women due for ANC" to P2. The non-follow-up and HRP-confirmed variants have no title or state in the design. |
| 6 | Pregnancies past their expiry are auto-closed whenever the list opens | `PwAncVisitsListViewModel.kt:36` | Missing | No "pregnancy closed / expired" state in the design. |
| 7 | Card shows Last visit date. Call button calls the woman. If call permission is missing it asks for permission with the toast "Please allow permissions first" | `AncVisitListAdapter.kt:118-131`. `PwAncVisitsListFragment.kt:152-166` | Partial | The permission-denied path is not designed. |
| + | C1, C2, C3, C8 | | | |

### ANC visits history sheet (`AncBottomSheetFragment`, .../list/AncBottomSheetFragment.kt): design frame M2.0-P4 · Visit history sheet
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Tapping **View** on a visit opens it read-only. Only the **last** visit opens with the Edit option | `AncBottomSheetFragment.kt:58-77` (passes isLast). `AncVisitAdapter.kt:66` | Partial | P4 shows "View" on done visits but no edit-last rule. |
| 2 | In PMSMA mode the sheet lists only ANC visits that were high-risk or held at the PMSMA site (place id 3) | `PwAncVisitsListViewModel.kt:95-101` | Missing | No "PMSMA visits" history in the design. |
| 3 | Per-visit sync icon. View hidden for supervisor | `rv_item_pregnancy_anc.xml:54`. `AncVisitAdapter.kt:46-50` | Partial | See C3. |

### ANC home visits sheet (`AncHomeVisitBottomSheetFragment`, .../list/AncHomeVisitBottomSheetFragment.kt): design frame NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | List of past ANC home visits (Mitanin). Tapping one opens it read-only. The last one can be edited | `AncHomeVisitBottomSheetFragment.kt:38-52` | Missing | There is no home-visit history in the design. |
| 2 | Empty state when there are no home visits (stub) | `AncHomeVisitBottomSheetFragment.kt:69-80` | Missing | |

### ANC visit form (`PwAncFormFragment`, maternal_health/pregnant_woment_anc_visits/form/PwAncFormFragment.kt): design frame M2.2-05 · ANC visit form (full), M2.2-16 maternal death branch
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **"Has she delivered?" appears only from 23 weeks**. Saying Yes marks the woman delivered (moves her to Delivery outcome) | `PregnantWomanAncVisitDataset.kt:541-545` (Konstants.minWeekToShowDelivered = 23). `PwAncFormViewModel.kt:180-185` | Partial | The design always shows the question and no week rule. |
| 2 | **≤ 12 weeks: Folic-acid tablets field instead of IFA, and fundal height hidden** | `PregnantWomanAncVisitDataset.kt:546-552,567-572` | Partial | The design always shows fundal height. |
| 3 | **Visit-number and date limits**: ANC number options only above the last one. Date ≥ LMP + 5 weeks (ANC 1) or ≥ last ANC + 28 days, and ≤ today | `PregnantWomanAncVisitDataset.kt:507-523` | Partial | The design has the ANC-period dropdown but not the limits. |
| 4 | **Td/TT dates are stored on the registration record and shared by all visits**. Td-2 hidden on the first ANC | `PregnantWomanAncVisitDataset.kt:504-505,596-598` | Partial | The design only shows the section as "Complete". |
| 5 | **Opened from the PMSMA list: Place of ANC is locked to the PMSMA site** (read-only) | `PregnantWomanAncVisitDataset.kt:488-493`. `PmsmaHighRiskListFragment.kt` addVisit (fromPmsma = true) | Missing | |
| 6 | Xushrukha: BP and Hb required. Other flavours: optional | `PregnantWomanAncVisitDataset.kt:480-487` | Partial | The design marks Hb and BP with an asterisk for every flavour. |
| 7 | Abortion = Yes closes the pregnancy, deactivates all ANC records and turns the woman back into an Eligible Couple | `PwAncFormViewModel.kt:187-216` | Partial | Designed in M2.2-14 banner but not shown on the ANC form. |
| 8 | Maternal death: marks the beneficiary dead (date, cause, place copied) and **goes straight to the MDSR form after save** | `PwAncFormViewModel.kt:218-250`. `PwAncFormFragment.kt:293` | Partial | M2.2-16 shows a "Record death?" confirm dialog that code does not have. Code navigates to MDSR automatically. |
| 9 | Edit FAB only when opened as the latest visit | `PwAncFormFragment.kt:279` | Partial | See C4. |
| + | C5 (Mitanin: no MCP card), C6 (camera/gallery/PDF, size limit, viewer) | | | |

### ANC home visit / antenatal counselling (`AntenatalCounsellingFragment`, maternal_health/pregnant_woment_anc_visits/homeVisit/AntenatalCounsellingFragment.kt): design frame M2.2-06 · ANC home visit & counselling
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Mitanin-only screen** (entry buttons hidden in other flavours) | `AncVisitListAdapter.kt:142-148` | Missing | The design frame has no flavour gate. |
| 2 | **Server-driven form** of ~21 risk/danger-sign questions (swelling, high BP, convulsions, anaemia, reduced fetal movement, age risk, child gap, short height, bleeding, twins, C-section, sickle cell, malaria, jaundice, etc.) | `AntenatalCounsellingViewModel.kt:152-163` | Partial | The design draws a counselling-topics checklist plus one danger-sign Yes/No. The real question set and its source are different. |
| 3 | **Header summary card**: name, husband, age, registration date, LMP, weeks, EDD, phone, last home-visit date | `fragment_antenatal_counselling.xml:19-265`. `AntenatalCounsellingFragment.kt` benList collect | Missing | |
| 4 | **Age risk auto-calculated** (age < 18 or > 35). Visit number, visit day and due date locked | `AntenatalCounsellingViewModel.kt:68-75,376-381` | Missing | |
| 5 | **One-time history questions carried from the previous visit and locked** (child gap, height, weight, miscarriage, 4+ deliveries, first delivery, C-section, pre-existing disease, prolonged labour). Twin and sickle-cell carried over but editable | `AntenatalCounsellingViewModel.kt:166-172,390-433` | Missing | |
| 6 | **Any danger sign → dialog "Danger signs identified – refer to HWC?"**. Yes opens the HWC referral form (type MATERNAL). On return the visit is saved. Not asked again once referred | `AntenatalCounsellingFragment.kt:584-605,656` | Missing | |
| 7 | **After save: "Continue to ANC visit?" dialog**. Yes opens the next ANC form if it is due, otherwise toast "Next ANC visit is not yet due". No returns to the list | `AntenatalCounsellingFragment.kt:438-465` | Missing | |
| 8 | Visit date ≤ today (yesterday if today is already used), ≥ previous visit + 1 day. "Visit N" numbering | `AntenatalCounsellingViewModel.kt:651-677` | Partial | The design has only a date field. |
| 9 | View mode with Edit FAB only on the last visit. Image pick field | `AntenatalCounsellingFragment.kt:198`, pick_image handler | Partial | See C4. |

### PMSMA visit form (`PmsmaFragment`, maternal_health/pmsma/PmsmaFragment.kt): design frame M2.2-07 · PMSMA visit (9th of month)
Note: in code this form is effectively unreachable. The PMSMA button is hidden in the ANC list (`hidePmsma=true`) and `PmsmaBottomSheetFragment` is never shown.
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **High-risk selected → alert "High risk pregnancy – HRP case, please visit the nearest HWC or call 104"** | `PmsmaFragment.kt:92-99`. `PmsmaViewModel` onHighRiskSelected | Missing | The design reveals Reason/Referred but no alert. |
| 2 | Prefill also includes ANC count and the last PMSMA visit. The PMSMA visit number is per woman | `PmsmaViewModel.kt` init (setUpFirstPage) | Partial | The design's auto card covers most of it. |
| 3 | Edit FAB only from the latest PMSMA visit | `PmsmaFragment.kt:119` | Partial | See C4. |
| 4 | Consent dialog (tick checkbox to proceed, else go back) is defined but never shown | `PmsmaFragment.kt:42-65` | Missing | Dormant code. Listed for completeness. |

### PMSMA visits sheet (`PmsmaBottomSheetFragment`, maternal_health/pmsma/list/PmsmaBottomSheetFragment.kt): design frame NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | List of PMSMA visits → open a visit (the last one editable). Works from both the High-risk and ANC lists | `PmsmaBottomSheetFragment.kt:40-62` | Missing | Dormant (never opened in current code). |

### PMSMA / high-risk pregnancies list (`PmsmaHighRiskListFragment`, maternal_health/pmsma/pmsmaHighRiskList/PmsmaHighRiskListFragment.kt): design frame M2.2-08 · High-risk pregnancies list
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **"PMSMA" card button opens the ANC form with Place = PMSMA locked** (not the PMSMA form). Greyed until the next ANC is due | `PmsmaHighRiskListFragment.kt:96-104`. `AncVisitListAdapter.kt:159`. `rv_item_pregnancy_visit.xml:483` | Partial | The design maps PMSMA → M2.2-07 PMSMA form, which does not match code. |
| 2 | **"PMSMA visits" history button** (high-risk or PMSMA-site ANC visits) | `PmsmaHighRiskListFragment.kt:118-133`. `PwAncVisitsListViewModel.kt:95-101` | Missing | |
| 3 | Same Delivered switch + date picker as the ANC list (> 22 weeks) | `AncVisitListAdapter.kt:77`. `rv_item_pregnancy_visit.xml:408` | Missing | See ANC list #1. |
| 4 | Mitanin home-visit buttons also appear on this list | `AncVisitListAdapter.kt:136-148` | Missing | |
| 5 | List = all high-risk pregnant women (single list). Call button | `PwAncVisitsListViewModel.kt:52,62-70` | Partial | The design has Suspected/Confirmed/Referred tabs that code does not have. The "Refer" action is not in code. |
| + | C1, C2, C3, C8 | | | |

### Delivery outcome list (`DeliveryOutcomeListFragment`, maternal_health/delivery_outcome/list/DeliveryOutcomeListFragment.kt): design frame NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **List of delivered women** with search and a "Register" button per card (red when no outcome, green once filled) that opens the Delivery outcome form | `DeliveryOutcomeListFragment.kt` benAdapter. `BenListAdapterForForm.kt:114-160` | Missing | The hub tile exists. The list screen is not drawn. |
| 2 | Supervisor sees the list read-only (button hidden) | `BenListAdapterForForm.kt:59-62`. `nav_supervisor.xml:80` | Missing | See C3. |
| + | C1 | | | |

### Delivery outcome form (`DeliveryOutcomeFragment`, maternal_health/delivery_outcome/DeliveryOutcomeFragment.kt): design frame M2.2-09 · Delivery outcome form, M2.2-10 next steps
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Complication = "Death" → mother marked dead (date, cause, place) and the app goes to the MDSR form after save** | `DeliveryOutcomeViewModel.kt:148-163`. `DeliveryOutcomeFragment.kt:133` | Missing | The design has the death branch only from ANC (M2.2-16). |
| 2 | Saving adds live births and total births to the mother's EC record child counts | `DeliveryOutcomeViewModel.kt:166-178` | Missing | |
| 3 | Saved outcome is view-only (Submit disabled, no edit) | `DeliveryOutcomeFragment.kt:106` | Partial | See C4. |
| 4 | JSY = Yes reveals the JSY voucher upload (non-Mitanin only). MCP card uploads hidden for Mitanin | `DeliveryOutcomeDataset.kt:264-267,466-472` | Partial | See C5. |
| 5 | Defaults: delivery date = today, still births = 0, discharge date limited to delivery date … +1 month | `DeliveryOutcomeDataset.kt:268-274` | Partial | |
| 6 | After save, code just returns to the list | `DeliveryOutcomeFragment.kt:130-134` | Partial | The design's "next steps" sheet (register baby, PNC day 1) is design-only. Code has no such prompt. Flagging the mismatch. |

### PNC mothers list (`PncMotherListFragment`, maternal_health/pnc/list/PncMotherListFragment.kt): design frame M2.2-12 · PNC mothers · visit schedule
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | "Add PNC" button shown only when a PNC day (1, 3, 7, 14, 21, 28, 42) is due by days since delivery and not yet done. Hidden otherwise | `PNC.kt:203-215`. `PncVisitListAdapter.kt:63-64` | Partial | The design's schedule sheet shows due and locked days but the card always shows "Start". |
| 2 | "View visits" only when at least one PNC exists | `PncVisitListAdapter.kt:61-62` | Partial | |
| 3 | Non-follow-up variant of the list (source 1) | `PncMotherListViewModel.kt` allBenList. `NonFollowUpFragment.kt:72` | Partial | |
| + | C1, C2, C8 | | | |

### PNC visits sheet (`PncBottomSheetFragment`, maternal_health/pnc/list/PncBottomSheetFragment.kt): design frame M2.2-12 (sheet)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | "PNC visit N" rows → open that visit read-only | `PncBottomSheetFragment.kt:27-32` | Partial | The design's day grid has no "view a done visit" action. |

### PNC visit form (`PncFormFragment`, maternal_health/pnc/form/PncFormFragment.kt): design frame M2.2-13 · PNC visit form
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Mother death → beneficiary marked dead and the app goes to the MDSR form** | `PncFormViewModel.kt:159-173`. `PncFormFragment.kt:140` | Partial | The design has "Is the mother alive?" with no death fields or MDSR step. |
| 2 | **Permanent sterilisation chosen in an earlier PNC is carried into later visits** | `PncFormViewModel.kt:116-125`. `PncFormDataset` setUpPage | Missing | |
| 3 | **Status change after PNC**: day-42 PNC (or ≥ 60 days after delivery) sets the woman to "Permanently sterilised" if sterilised, otherwise back to "Eligible couple" | `PncFormViewModel.kt:266-290` | Missing | |
| 4 | **Incentive reminder popup** when a contraception method other than condom is picked: "Reminder – upload delivery discharge summary" (non-Mitanin) | `PncFormDataset.kt:628-636`. `PncFormFragment.kt:130` | Missing | |
| 5 | **On Submit, if no discharge summary is uploaded → "Do you want to upload delivery discharge summary?" Yes/No** (non-Mitanin) | `PncFormFragment.kt:159-162` | Missing | |
| 6 | Delivery discharge summary photos (up to 4, camera/gallery, no PDF) | `PncFormFragment.kt:206`. `PncFormDataset.kt:276` | Missing | The design form has no documents. See C5/C6. |
| 7 | No edit after save | `PncFormFragment.kt:97` | Partial | See C4. |

### Abortion list (`AbortionListFragment`, maternal_health/abortion/list/AbortionListFragment.kt): design frame NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **List of women whose pregnancy ended in abortion** (shows abortion date, LMP, EDD, weeks) with "Add" (red) / "View" (green) button for the CAC form | `AncAbortionListAdapter.kt:36-46`. `rv_item_abortion.xml:293-372` | Missing | M2.2-14 is the form. The list (hub tile "Abortion") is not drawn. |
| + | C1, C2 | | | |

### Abortion / CAC form (`PwAncAbortionFormFragment`, maternal_health/abortion/form/PwAncAbortionFormFragment.kt): design frame M2.2-14 · Abortion record
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Post-abortion sterilisation = Yes → beneficiary set to "Permanently sterilised" and an EC-tracking record auto-created (female sterilisation, on sterilisation date)** | `PwAncAbortionFormViewModel.kt:148-167` | Missing | The design says she "moves back to the eligible couple list" only. |
| 2 | Abortion discharge summary photos (camera/gallery, non-Mitanin) | `PwAncAbortionFormFragment.kt:97,234`. `PregnantWomanAncAbortionDataset.kt:189` | Missing | See C5/C6. |
| 3 | Edit FAB on a saved record | `PwAncAbortionFormFragment.kt:157` | Partial | See C4. |
| 4 | Screen title "CAC form" (comprehensive abortion care) | `PwAncAbortionFormFragment.kt` onStart | Partial | Naming differs ("Abortion"). Minor. |

### Infant registration list (`InfantRegListFragment`, maternal_health/infant_reg/list/InfantRegListFragment.kt): design frame NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **One card per baby** ("1st/2nd baby of <mother>" for twins) with Register/View | `InfantReg.kt:170-180`. `InfantRegistrationAdapter.kt` | Missing | The list screen is not drawn. Multiple babies per delivery are not shown anywhere. |
| 2 | **"Low birth weight babies" variant** of this list (opened from the scheduler/Today) | `InfantRegListViewModel.kt:21-24`. `InfantRegListFragment.kt:114`. `SchedulerFragment.kt:119` | Partial | The nav map sends "Low birth weight babies" to M2.4-02 (HBNC newborn list), but code opens the infant registration list. |
| + | C1, C2, C7 | | | |

### Infant registration form (`InfantRegFragment`, maternal_health/infant_reg/form/InfantRegFragment.kt): design frame M2.2-11 · Infant registration
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Registration is **per baby index** (twins/triplets each get their own form) | `InfantRegViewModel.kt:14-15,63-69` | Missing | |
| 2 | **SNCU = Yes → 4 SNCU discharge-summary photo uploads** (non-Mitanin) | `InfantRegistrationDataset.kt:320-330,372-382` | Missing | The design has "Admitted to SNCU?" with no reveal. |
| 3 | View-only after save (Submit disabled) | `InfantRegFragment.kt:120` | Partial | See C4. |

### Child registration list (`ChildRegListFragment`, maternal_health/child_reg/list/ChildRegListFragment.kt): design frame NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | List of registered infants due for child registration, Register (red) / View (green) | `ChildRegistrationAdapter.kt` | Missing | |
| 2 | **"View" on an already-registered child opens the full beneficiary (member) registration form** of that child, not the child-reg form | `ChildRegListFragment.kt:68-75` | Missing | |
| + | C1, C2 | | | |

### Child registration form (`ChildRegFragment`, maternal_health/child_reg/form/ChildRegFragment.kt): design frame M2.4-12 · Child registration
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Saving creates a new household member (child beneficiary)** and fills the child's DOB, gender and age into the mother's EC record (child slot 1–9) | `ChildRegViewModel.kt:109-150` | Missing | |
| 2 | Birth certificate front/back accepts camera, gallery **or PDF** | `ChildRegFragment.kt:183-204` | Partial | See C6. The design has photo tiles only. |
| 3 | View-only after save | `ChildRegFragment.kt:155` | Partial | See C4. |

### HWC referral list (`HwcReferredFragment`, maternal_health/hwc/list/HwcReferredFragment.kt): design frames M2.2-15 · HWC referral list, M2.8-14 · HWC referred list (all sources)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Only Maternal and General check-up referrals are listed** | `HwcReferredViewModel.kt:71` | Partial | M2.8-14 shows "all sources" incl. TB, which code does not list. |
| 2 | **Follow-up button only on General check-up rows**. Maternal rows have none | `HwcReferredFragment.kt:97` | Partial | M2.2-15 shows "Follow up" on maternal rows, which contradicts code. |
| 3 | Follow-up button: red "Start follow-up" if none done, green "View follow-up" if done | `NcdReferListAdapter.kt:108-127` | Partial | The design shows "View follow-up" only. |
| 4 | **Deceased beneficiary: card greyed out and follow-up hidden** | `NcdReferListAdapter.kt:91-95` | Missing | |
| 5 | "Referred from" shows the logged-in ASHA's name. Opens in Supervisor screen for non-ASHA roles | `NcdReferListAdapter.kt` referredFrom. `HwcReferredFragment.kt` onStart | Missing | See C3. |
| + | C1, C2 | | | |

### High-risk cases hub (`HrpCasesFragment`, hrp_cases/HrpCasesFragment.kt): design frame M2.6-01 · High risk hub
No gaps. Pregnant (Assess / Follow-up) and Non-pregnant (Assess / Follow-up) tiles with counts are designed. `HrpCasesViewModel` search is unused by the UI.

### HBNC visit schedule (`InfantDayListFragment`, infant/InfantDayListFragment.kt): design frame M2.4-03 · HBNC schedule & visit card
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Mandatory vs Optional label on each day card**: Day 1/3/7/42 "Mandatory *", Day 14/21/28 "Optional" | `VisitCardAdapter.kt:89-110` | Missing | The design grid treats all 7 days the same. |
| 2 | **Unlock order**: Day 3 after Day 1, Day 7 after Day 3, Days 14/21/28/42 all open once Day 7 is done | `HBNCFormViewModel.kt:355-362` | Partial | The design shows done/due/locked but not the "14–42 open together after day 7" rule. |
| 3 | **Baby recorded dead → all remaining visits disabled** | `InfantDayListFragment.kt:76-81`. `VisitCardAdapter.kt:123-130` | Missing | |
| 4 | Visit date shown on each done card, RCH/ben ID header. Background form sync starts on open | `InfantDayListFragment.kt:52`, tvRchId | Partial | |

### HBYC visit schedule (`ChildMonthListFragment`, infant/ChildMonthListFragment.kt): design frame NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Month-card schedule (3, 6, 9, 12, 15 months)**: only months the child has reached are shown. Next undone month editable, done months view-only | `HBYCFormViewModel.kt:326-370` | Missing | M2.4-06 goes from list straight to the form. There is no HBYC schedule screen. |
| 2 | Child dead → visits disabled | `ChildMonthListFragment.kt` isBenDead observer | Missing | |

### HBNC visit form (`HBNCFormFragment`, infant/hbnc/HBNCFormFragment.kt): design frames M2.4-04 · HBNC visit form (day 3), M2.4-05 · HBNC danger sign → refer
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Danger sign → "Danger signs identified – refer to HWC?" → referral bottom dialog** (reason "Suspected newborn complication"). **Mitanin only**, once per visit | `HBNCFormFragment.kt:201-203`, showDangerSignDialog / showReferralDialog | Partial | M2.4-05 has a refer sheet (with Call 108, not in code) and no Mitanin gate. |
| 2 | **Baby not alive → "Infant Death Reported – complete the CDR form" → opens CDR** | `HBNCFormFragment.kt:167`, showDeathAlertDialog | Missing | |
| 3 | **SNCU baby: "Discharged from SNCU" auto-set to Yes** | `HBNCFormFragment.kt:211-218` | Missing | |
| 4 | Visit-date checks: not after today, not before delivery, after previous visit, on or after the day's due date (with the date shown in the error) | `HBNCFormFragment.kt:276-310` | Partial | |
| 5 | Image field (Take photo / Choose from gallery, size limit). Read-only when opened from a done day | `HBNCFormFragment.kt` showImagePickerDialog, isViewMode | Partial | See C6. |

### HBYC visit form (`HBYCFormFragment`, infant/hbyc/HBYCFormFragment.kt): design frame M2.4-07 · HBYC visit form
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Danger sign (child sick, developmental delay, diarrhoea, breathing difficulty) → refer dialog + referral sheet** ("Suspected illness in young child", type CHILD). **Mitanin only** | `HBYCFormFragment.kt:172`. `HBYCFormViewModel.kt:84-90` | Partial | The design reveals "Referred to RBSK" on development delay only. There is no dialog or flavour gate. |
| 2 | Visit-date validation and image upload. Read-only for done months | `HBYCFormFragment.kt:153-216` | Partial | See C6. |

---

No gaps: MotherCareFragment (Maternal health hub), HrpCasesFragment (High-risk hub).

Screens with NO SCREEN IN DESIGN: Delivery outcome list, Abortion list, Infant registration list, Child registration list, ANC home-visits sheet, PMSMA visits sheet (dormant), HBYC month schedule.

Out of scope but referenced: the maternal death report form (MDSR) and CDR live under death reports. The HRP assess and track forms live under `high_risk/`. No maternal-death screen exists inside `maternal_health/`; the death branch is handled inside the ANC, Delivery and PNC forms (above).

---

## Group C: Child care, Immunization, Eligible couple. Functionality in code that the design is missing

Summary: 31 screens checked, 46 missing features, 60 partial (voice search and the deceased marker are each counted once per list in the cross-cutting notes, not in the tables).

Paths are relative to `app/src/main/java/org/piramalswasthya/sakhi/` unless they start with `res/`. Design sources checked: `design/figma-plugin/src/33-stage-2.3-ec.js`, `34-stage-2.4-child.js`, `35-stage-2.5-immunization.js`, the generic kit/patterns (`30-stage-2.0-kit.js`, `10-core-phase1.js`, `43-…`, `44-…`), and AUDIT_CARDS/AUDIT_FORMS (`25-audit-data.js`).

Patterns that apply to every list in this group and are not repeated in each table below:
- **Voice search (microphone button next to the search box)** is used on every list here (`SpeechToTextContract`). The design search bar has no mic icon, and no design file mentions voice. **Missing everywhere.**
- **Deceased marker on the card** (`iv_isDeath`, shown when `isDeath == true`) appears on the infant, child and immunization cards. The design has no "deceased" state anywhere, including the kit. **Missing everywhere.**
- The design's "Save draft" button (on every form) and its chips such as "Low birth weight", "SNCU" and "SAM / MAM" have **no code behind them**. These go the other way (design ahead of code) and are listed at the end only for awareness.

---

### Eligible couple hub (`EligibleCoupleFragment`, ui/home_activity/eligible_couple/EligibleCoupleFragment.kt): design frame M2.3-01 · Eligible couple hub
No gaps. The two tiles with live counts (IconDataset.kt:512-523) are in the design.

### EC sort sheet (`EcFilterBottomSheetFragment`, ui/home_activity/eligible_couple/EcFilterBottomSheetFragment.kt): design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | "Sort by" bottom sheet with 5 options: Newest first, Oldest first, Age-wise, Syncing first, Unsynced first. It has Apply and Cancel buttons | Opened from the filter icon on the EC registration and EC tracking lists (EcFilterBottomSheetFragment.kt:36-57) | Missing | The design has chips ("All / Newly married / Age < 20") but no sort at all. No design file contains "sort" or "newest". |
| 2 | Selected sort shown as a text label above the list ("Newest first" by default) | EligibleCoupleListFragment.kt:52-64; EligibleCoupleTrackingListFragment.kt:75-87 | Missing | |

### EC registration list (`EligibleCoupleListFragment`, ui/home_activity/eligible_couple/registration/eligible_couple_list/EligibleCoupleListFragment.kt): design frame M2.3-02 · EC registration list
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **IFA button on the couple card** opens the beneficiary IFA form | Shown only for women aged 20–49 with reproductive status 1 or 2 (res/layout/rv_item_ben_ec_with_form.xml:345-360; BindingUtils.kt:552-561). Navigates via EligibleCoupleListFragment.kt:76-86 | Missing | The design card has no IFA action, and AUDIT_CARDS does not list it either. |
| 2 | Card button switches between **"Register" (red)** and **"View" (green)** | "View" once LMP is saved, i.e. `ecr.lmpDate != 0` (ECRegistrationAdapter.kt:76-85) | Partial | The design shows Register and Continue but never a View state for an already-registered couple, even though it has a "Registered" tab. |
| 3 | **Missed-period alert icon and "Status: Missed period / Under review"** on the card, plus the LMP date | LMP more than 35 days ago means Missed period, otherwise Under review. Hidden until registered (ECRegistrationAdapter.kt:52-73) | Missing | No missed-period tag in M2.3-02. |
| 4 | Sync icon on the card only once an EC record exists | ECRegistrationAdapter.kt:75 | Partial | The kit has a sync badge, but the rule "no badge before registration" is not shown. |
| 5 | Child count on the card ("No. of children") | ECRegistrationAdapter.kt:51 | Partial | The design meta line has "2 children" as free text only. |
| 6 | **"Missed period" list mode**: the same screen opened from Home shows only couples with a missed period | `source == SOURCE_MISSED_PERIOD` (EligibleCoupleListViewModel.kt:27-32) | Partial | The design navigation maps "Missed period" to the PW list (M2.2-02), not to an EC list. |
| 7 | Sort sheet and selected-sort label | :52-64 | Missing | See EcFilterBottomSheetFragment. |

### EC registration form (`EligibleCoupleRegFragment`, ui/home_activity/eligible_couple/registration/eligible_couple_reg/EligibleCoupleRegFragment.kt): design frame M2.3-03 · EC registration form
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **View mode with an Edit button.** A completed registration opens read-only, Submit is hidden and the Edit FAB unlocks the form | `recordExists && lmpDate != 0` (EligibleCoupleRegFragment.kt:158-170, 276-278; ViewModel:147-155) | Missing | The design has no view or edit state for EC registration. The kit view mode (M2.0-P5) says "ask your ANM", which means no edit, and that contradicts the code. |
| 2 | **Children are pre-filled automatically from household members** (up to 9) and shown read-only. Live, boys and girls counts are calculated, and the gap from marriage or from the previous child is computed | EligibleCoupleRegistrationDataset.kt:1528-1550; ViewModel:157-178 | Partial | The design shows editable children, an "Add child" button and steppers. In code these values are not typed in. |
| 3 | **Nayi Pahal kit section only for couples with no live children** | Removed when live children > 0 (EligibleCoupleRegistrationDataset.kt:1552-1559) | Partial | The design shows the kit section on a couple with 2 children, so the rule is missing. |
| 4 | **Incentive pop-up: "Please upload photo of couple with Nayi Pahal kit, to claim your incentive"** | Kit = Yes, all flavours except Mitanin (Dataset:1797; Fragment:243-246) | Missing | |
| 5 | **Kit photo upload is hidden on Mitanin builds**: date only, no photos | Dataset:1461, 1576, 1799-1810; Fragment:172 | Missing | |
| 6 | Upload chooser with **Camera, Gallery and PDF**, plus a "file too large" toast and a view-uploaded-file action (image viewer or PDF app) | Fragment:444-466, 59-140, 177-226 | Partial | The kit photo tile covers camera and gallery only. PDF, the size-limit error and view-document are not designed. |
| 7 | **High-risk check hidden on Mitanin**. Each high-risk section header turns red when any answer is "Yes" | Dataset:1461-1476, 1515-1525, 2346-2360 | Partial | The design shows a closed "High-risk check" section with no flavour gate and no red flag state. |
| 8 | **Saving silently creates or updates the non-pregnant high-risk assessment and marks the woman as HRP** if any risk answer is Yes | ViewModel:205-212; Dataset:2555 | Missing | This side effect is not communicated in the design (no banner or confirmation). |
| 9 | **Bank details only on Saksham (Assam) builds** | Dataset:1478-1480 | Partial | The design shows the "Bank details" section without a flavour gate. |
| 10 | RCH ID and Aadhaar entered here are **written back to the beneficiary profile**. Aadhaar is shown read-only if it already exists | Dataset:1488-1492, 2540-2560 | Missing | |
| 11 | Header strip shows the woman's age and gender, husband's name, marriage date and number of children | Fragment:248-271 | Partial | The design header has name and husband only. |
| 12 | After saving: go back to the list and trigger sync | Fragment:279-288 | Partial | The design primary button is "Register" but the destination is not shown. |

### EC tracking list (`EligibleCoupleTrackingListFragment`, ui/home_activity/eligible_couple/tracking/list/EligibleCoupleTrackingListFragment.kt): design frame M2.3-04 · EC tracking list
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **"This month's visit already filled" toast.** "Add visit" is blocked when a visit exists for the current calendar month | `allowFill` false (EligibleCoupleTrackingCache.kt:142-150; Fragment:91-102) | Partial | The design has Due and Done tabs but no blocked or "already done" state on the action. |
| 2 | Two separate card buttons: **"View visits"** (shown only if at least one visit exists) and **"Add visit"** | res/layout/rv_item_ec_tracking_list.xml:352-379; `list_avail` BindingUtils.kt:391-398 | Partial | The design has a single "Visit" action that leads to the history sheet. |
| 3 | **Missed-period icon and status (Missed period / Under review)** with LMP date | LMP more than 35 days ago (ECTrackingListAdapter.kt:65-77) | Missing | |
| 4 | **"No follow-up" icon** with last visit date | Last visit between 90 and 365 days ago (ECTrackingListAdapter.kt:51-63) | Missing | |
| 5 | **List modes from Home**: "No follow-up" and "Missed period" | `source` 1 or 2 (EligibleCoupleTrackingListViewModel.kt:29-33) | Partial | Design navigation maps "No follow-up in 30 days" to a household screen and "Missed period" to the PW list. |
| 6 | **Supervisor role**: the same list opens inside the Supervisor app (read access to ASHA's EC tracking) | Fragment:150-160; res/navigation/nav_supervisor.xml:45-53 | Missing | Not present in the supervisor stage (42-*). |
| 7 | Sort sheet and selected-sort label | :75-88 | Missing | |
| 8 | Card shows total children and an "all visits synced" sync icon | res/layout/rv_item_ec_tracking_list.xml:79-86, 164-174 | Partial | |

### EC visit history sheet (`ECTrackingListBottomSheetFragment`, ui/home_activity/eligible_couple/tracking/list/ECTrackingListBottomSheetFragment.kt): design frame M2.3-05 · Tracking history sheet
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | One row per saved visit (filled-on date) with **its own sync icon** and a **VIEW** button that opens the form read-only | res/layout/rv_item_ec_track.xml:30-58; Sheet:40-48 | Partial | The design month grid shows done, missed and due, but no per-visit sync state and no explicit "view" action. |

### EC tracking form (`EligibleCoupleTrackingFormFragment`, ui/home_activity/eligible_couple/tracking/form/EligibleCoupleTrackingFormFragment.kt): design frames M2.3-06 / 07 / 08 / 09
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **ANTRA dose history table** (all past ANTRA doses with dates) above the form | Shown when the saved method contains "ANTRA" (Fragment:93-95, 137-141; ViewModel:110-112, 219-224) | Missing | |
| 2 | **ANTRA dose number is auto-calculated and locked**: next dose number from the previous dose and the visit date | EligibleCoupleTrackingDataset.kt:383-388, 395-403 | Partial | The design shows "ANTRA dose date" and "Next dose due" but not the locked dose-number field. |
| 3 | **Next injection due is shown as a window**: "dd-mm-yyyy to dd-mm-yyyy" (76–120 days after the injection) | Dataset:405-416 | Partial | The design shows a single date ("25 Jan 2027"). |
| 4 | **Visit date restricted**: earliest is the 1st of the month after the last visit (or the registration date); latest is today | Dataset:249-264 | Missing | |
| 5 | **Female sterilisation re-tags the woman as "Permanently Sterilised"**, which takes her out of EC lists | ViewModel:153-161, 209-215 | Missing | No confirmation or explanation in the design. |
| 6 | **Discharge summary uploads (2) for sterilisation methods**, all flavours except Mitanin | Dataset:337-353, 670-680 | Partial | The fields are in the spec, but the trigger (method = male or female sterilisation) and the Mitanin gate are not in any screen. |
| 7 | **MPA card upload hidden on Mitanin** | Dataset:359-363, 638-642; Fragment:126 | Partial | The M2.3-09 Mitanin variant does not say the upload disappears. |
| 8 | Upload chooser **Camera / Gallery / PDF**, size-limit toast, "No document found" and "Unable to open document" toasts, and view-uploaded-document | Fragment:228-334 | Partial | |
| 9 | **Pregnant path copies the non-pregnant high-risk answers into a new pregnant HRP assessment** before opening Pregnancy registration | ViewModel:162-197 | Missing | The design M2.3-07 shows the hand-off only. |
| 10 | Not pregnant: "Tracking form filled successfully" toast, go back, and trigger sync | Fragment:151-168, 109-113 | Partial | |
| 11 | View mode: Submit disabled (not hidden) and uploads disabled | Fragment:97-101, 134-135 | Partial | M2.3-08 shows a different layout (info cards). |

### Immunization hub (`ImmunizationDueTypeFragment`, ui/home_activity/immunization_due/ImmunizationDueTypeFragment.kt): design frame M2.5-01 · Immunization hub
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | The hub has **only the "Child immunization" tile**. Mother immunization exists in navigation but has no tile, so it cannot be reached | IconDataset.kt:755-767; res/navigation/nav_home.xml:338 | Partial | The design shows a "Mother (Td) immunization" tile. Decide whether to keep it in the design or re-enable it in code. |

### Child immunization stages (`ChildImmunizationCategoriesFragment`, ui/home_activity/immunization_due/child_immunization/categories/ChildImmunizationCategoriesFragment.kt): design frame: NO SCREEN IN DESIGN
No gaps. The screen is dead code: its list submit is commented out (:69) and there is no inbound navigation.

### Child immunization list (`ChildImmunizationListFragment`, ui/home_activity/immunization_due/child_immunization/list/ChildImmunizationListFragment.kt): design frame M2.5-02 · Child due list
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Two quick-filter cards with counts: "Due this month" and "Scheduled next month"** (single-select, tap again to clear). Each count respects the search and dose stage | Fragment:134-200; ViewModel:209-286, 333-337 | Partial | The design uses tabs "Overdue / This week / All". There is no next-month bucket and no this-week logic in code. |
| 2 | **Result summary line**, e.g. "12 beneficiaries · Due this month · 9–12 months" | ViewModel:288-313; Fragment:147-149 | Missing | |
| 3 | **Filtered empty state**: "No match for filters" vs "No records found" | ViewModel:315-318; Fragment:150-157 | Partial | The kit empty state does not distinguish the two. |
| 4 | Dose-stage selector has **8 options** (All, Birth, 6 wk, 10 wk, 14 wk, 9–12 m, 16–24 m, 5–6 y), also as a horizontal tab strip on the list | ViewModel:60-69; Fragment:66 | Partial | The design sheet also lists 10 years, 16 years and Catch-up, which code removed (FLW-1144). |
| 5 | **"Due only" mode from Home**: only children with an OVERDUE dose | `showDueOnly` arg (ViewModel:40-41, 257-260; home/SchedulerFragment.kt:107) | Partial | Design navigation goes to M2.5-02, but the overdue-only preset is not shown. |
| 6 | List scope is children born in the last 6 years, newest first | ViewModel:75-84, 172 | Partial | The design "All 96" count implies a different scope. |
| 7 | Card shows Child RCH ID, Mother RCH ID and DOB. The **"Show vaccines"** button opens the vaccine sheet | res/layout/rv_item_ben_child_immunization.xml:280-378 | Partial | The design action is "Give". |
| 8 | Deceased child icon on the card | res/layout/rv_item_ben_child_immunization.xml:67-72 | Missing | |

### Dose-stage filter sheet (`ChildImmunizationFilterBottomSheetFragment`, …/list/ChildImmunizationFilterBottomSheetFragment.kt): design frame M2.5-03 · Filter by age stage
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Sheet applies **immediately on tap** (single select, closes itself). The close icon dismisses it | Sheet:35-47 | Partial | The design has a multi-select chip grid, a Status radio and Clear / "Show 11 children" buttons, none of which exist in code. |

### Vaccine sheet for a child (`ChildImmunizationVaccineBottomSheetFragment`, …/list/ChildImmunizationVaccineBottomSheetFragment.kt): design frame M2.5-04 · Child vaccination card
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **"Missed" state**: past the maximum age, the button is hidden and the dose cannot be given | VaccineState.MISSED (ChildImmunizationListViewModel.kt:152-157; BindingUtils.kt:103-105) | Missing | The design states are given, due, overdue and not-yet. There is no "missed / too late". |
| 2 | **"View" on a given dose** opens the saved record read-only | DONE gives the "View" label (BindingUtils.kt:99-101) | Missing | |
| 3 | **Deceased child: all "Fill" buttons disabled** | res/layout/rv_item_imm_vaccine.xml:95, 154; Sheet:63-73 | Missing | |
| 4 | Due date shown on every dose ("Due: dd-mm-yyyy") | res/layout/rv_item_imm_vaccine.xml:75-79 | Partial | The design shows dates only for given and overdue doses. |
| 5 | **Doses due within the next month are already listed and fillable** (PENDING, not locked). On debug/UAT, all stages from birth to 16–24 months can be filled early | ViewModel:116-137; model/Immunization.kt:30-38 | Partial | The design shows future doses greyed out with a lock. |
| 6 | Bottom sheet, not a full-screen card | Sheet | Partial | Layout difference only. |

### Record vaccines form (`ImmunizationFormFragment`, ui/home_activity/immunization_due/child_immunization/form/ImmunizationFormFragment.kt): design frame M2.5-05 · Record vaccines (10 weeks)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **"Select all" checkbox** for the stage's vaccines | Fragment:96, 385-413 | Missing | |
| 2 | **Submit is hidden until at least one vaccine is switched on**, and hidden for an existing record | Fragment:246-254 | Partial | The design shows "Save 2 doses" but not the hidden or empty state. |
| 3 | **Incentive reminder pop-up: "Do you want to upload MCP card…to claim your incentive?"** when no MCP photo is attached (all flavours except Mitanin) | Fragment:120-128 | Missing | |
| 4 | **MCP card upload hidden on Mitanin** | Fragment:174-181 | Missing | |
| 5 | Tapping a missed dose shows the toast **"Immunization cannot be done as it is missed"** | ChildImmunizationVaccineAdapter.kt:71-81 | Missing | |
| 6 | Debug/UAT builds allow saving missed doses (QA badge testing) | Fragment:420-424 | Partial | The design note covers early entry only. |
| 7 | Saving: spinner replaces the form; "Save successful" or "Something went wrong" toast; go back and sync | Fragment:130-162 | Partial | The design shows only the badge snackbar. |
| 8 | Existing record opens read-only (the Edit FAB is commented out) | Fragment:164-191 | Missing | There is no view-mode frame for a recorded dose. |
| 9 | Two MCP photos (camera or gallery), size-limit toast, tap to view photo | Fragment:266-323 | Partial | |

### Mother immunization list (`MotherImmunizationListFragment`, ui/home_activity/immunization_due/mother_immunization/list/MotherImmunizationListFragment.kt): design frame M2.5-06 · Mother Td immunization
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Screen is **unreachable** (no hub tile) | IconDataset.kt:755-767 | Partial | The design treats it as live. |
| 2 | **Dependent-dose rule**: Td-2 is fillable only if Td-1 was given, otherwise shown as missed. Doses are timed from LMP | MotherImmunizationListViewModel.kt:31-58 | Partial | The design describes the "4 weeks after Td-1" timing only. |

### Mother vaccine sheet (`MotherImmunizationVaccineBottomSheetFragment`, …/MotherImmunizationVaccineBottomSheetFragment.kt): design frame M2.5-06 (sheet)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Each dose's "Fill" opens the full Record-vaccine form (not an inline date). Given doses show "View" and missed doses hide the button | Sheet:40-49; BindingUtils.kt:86-106 | Partial | The design saves Td-2 inline from the sheet. |

### Child care hub (`ChildCareFragment`, ui/home_activity/child_care/ChildCareFragment.kt): design frame M2.4-01 · Child care hub
No gaps. The 4 tiles with counts (IconDataset.kt:447-474) are designed.

### Newborn / infant list (`InfantListFragment`, ui/home_activity/child_care/infant_list/InfantListFragment.kt): design frame M2.4-02 · Newborn list (HBNC)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Search box** (name and other fields) with voice search | Fragment:112-133 | Missing | M2.4-02 has no search bar. |
| 2 | **Filter dialog: "Show only records with an RCH ID"** | Fragment:38-64, 85-87; CommonUtils.kt:58-68 | Missing | The design chips are LBW/SNCU (not implemented in code). |
| 3 | **Overdue icon when the baby is older than 42 days** (HBNC window over) | InfantListAdapter.kt:54-63 | Partial | The design uses "Day 7 missed" tags. |
| 4 | "HBNC" card button opens the HBNC day schedule (InfantDayListFragment, outside this scope) | Fragment:89-99; res/layout/rv_item_ben_child_care_infant.xml:328-333 | Partial | Designed as "Visit" leading to M2.4-03. |
| 5 | Sync icon intentionally hidden on cards | InfantListAdapter.kt:46 (`showSyncIcon=false`) | Partial | The design shows a "Pending" sync badge. |

### Legacy HBNC day list (`HbncDayListFragment`, ui/home_activity/child_care/infant_list/hbnc_form/HbncDayListFragment.kt): design frame M2.4-03 (closest). **No inbound navigation; dead code.**
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Sequential unlock: Visit card, then Part I, then Part II, then day tiles. Day tiles (1, 3, 7, 14, 21, 28, 42) appear only for days already elapsed | HbncDayListViewModel.kt:40-95 | Partial | Listed for completeness. The live flow is InfantDayListFragment. |

### Legacy HBNC visit card / Part I / Part II / day visit (`HbncVisitCardFragment`, `HbncPartIFragment`, `HbncPartIIFragment`, `HbncVisitFragment`, …/hbnc_form/{card,part_1,part_2,visit}/): design frame M2.4-04 (closest). **Unreachable.**
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Validation alert dialogs raised by the dataset (Part I and day visit) | part_1/HbncPartIFragment.kt:35-50; visit/HbncVisitFragment.kt:36-50 | Missing | Only relevant if these screens are revived. |
| 2 | Read-only once filled (submit hidden); spinner while saving; error toast | card/HbncVisitCardFragment.kt:50-107 (same pattern in the others) | Partial | |

### Child list (HBYC) (`ChildListFragment`, ui/home_activity/child_care/child_list/ChildListFragment.kt): design frame M2.4-06 · Children 3–15 months (HBYC)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Search box** with voice search | Fragment:116-137 | Missing | M2.4-06 has no search bar. |
| 2 | **Filter dialog: "Show only records with an RCH ID"** | Fragment:42-65, 87-89 | Missing | |
| 3 | **"HBYC" button opens a month schedule grid** (ChildMonthListFragment, outside scope) before the form | Fragment:91-103 | Partial | The design goes straight from the card to the "HBYC · 9 months" form, with no month-grid frame (unlike HBNC M2.4-03). |

### Legacy HBYC month list (`HbycMonthListFragment`, …/child_list/hbyc/HbycMonthListFragment.kt): design frame: NO SCREEN IN DESIGN. **No inbound navigation; dead code.**
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Month tiles for 3, 6, 9, 12 and 15 months with filled and sync state | HbycMonthListViewModel.kt:22-41 | Missing | Superseded by ChildMonthListFragment. Same concept missing in design (see ChildList #3). |

### Legacy HBYC form (`HbycFragment`, …/child_list/hbyc/form/HbycFragment.kt): design frame M2.4-07 · HBYC visit form. **Unreachable.**
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Read-only once filled (submit disabled); spinner while saving | HbycFragment.kt:54-83 | Partial | |

### Children under 5 list (`CUFYListFragment`, ui/home_activity/child_care/children_under_five_years/CUFYListFragment.kt): design frame M2.4-08 · Children under 5 · forms
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Search box** with voice search | Fragment:81-100 | Missing | M2.4-08 has chips but no search. |
| 2 | **Three buttons on each card: SAM, ORS and IFA syrup**, each opening its own visit sheet | res/layout/children_under_five_years_item.xml:345-367 | Partial | The design has a single "Forms" action and a combined sheet. |
| 3 | **SAM button label is the child's live SAM status**: "Check SAM", then "Referred to NRC", "NRC admitted", "Follow-up SAM", and back to "Check SAM" when discharged as Improved | CUFYFormRepository.kt:339-375; CUFYListViewModel.kt:80-111 | Partial | The design shows only "Check SAM". |
| 4 | List scope is children aged 0–5 years | RecordsRepo.kt:251 | No gap | |

### Children under 5 visit sheet (`CUFYBottomSheetFragment`, …/children_under_five_years/CUFYBottomSheetFragment.kt): design frame M2.4-08 (sheet "Forms")
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Per-form visit history**: "Visit 1…n" rows with VIEW, plus an "Add new visit" row with ADD | Sheet:163-191; res/layout/rv_item_children_care_bottom_sheet.xml:31-59 | Missing | The design sheet lists the 3 forms with only a "last visit" subtitle. |
| 2 | **SAM: "Add new visit" hidden once the child is in the NRC pathway** (status is not "Check SAM") | Sheet:175-188 | Missing | |
| 3 | **IFA (non-Mitanin): only the latest visit is shown, with both Add and View on that row.** "Add new visit" appears only when there is no visit yet | Sheet:129-161 | Missing | |
| 4 | **IFA (Mitanin): rows "IFA Visit 1, 2…" are view-only**, plus an "Add new visit" row | Sheet:101-128 | Partial | The design note mentions "IFA Visit 1, 2…" but no frame shows it. |

### Children under 5 form: SAM / ORS / IFA (`CUFYFormFragment`, …/children_forms/CUFYFormFragment.kt): design frame M2.4-09 · SAM check → NRC referral
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **View mode for saved visits**; **Edit FAB only on SAM records** (to update NRC status). The visit date is locked when editing | Fragment:176-187, 318-322, 382-388 | Missing | |
| 2 | **SAM follow-up visit dates list** shown on saved SAM records | Fragment:423-430, 746-770; ViewModel:325-340 | Missing | |
| 3 | **IFA visit / bottle history table** above the form. On Mitanin it shows visit numbers and the bottle count is hidden | Fragment:261-287; ViewModel:71, 394-395 | Missing | |
| 4 | **IFA date window**: no older than 10 months, after the previous IFA visit, not in the future, not before birth | Fragment:295-316, 486-512 | Missing | |
| 5 | **One IFA visit per calendar month** (field error and toast) | Fragment:600-632 | Missing | |
| 6 | **SAM visit date must be unique and later than all earlier visits** (toasts) | Fragment:644-715 | Missing | |
| 7 | Photo field: **Take photo / Choose from gallery** dialog with a size check | Fragment:431-457, 119-140 | Partial | The kit photo tile exists but this form does not use it. |
| 8 | Child info card (reuses the infant card with the HBNC button hidden) | Fragment:191-196 | Partial | |
| 9 | After save: start a separate sync worker per form (SAM, ORS, IFA) and go back | Fragment:230-247 | Partial | |
| 10 | MUAC and weight-for-height **"SAM case detected" alert dialog** | Fragment:358-375; HelperUtil.kt:974-999 | Partial | The design shows an in-page red banner, not a blocking dialog. |

### Adolescent list (`AdolescentListFragment`, ui/home_activity/child_care/adolescent_list/list/AdolescentListFragment.kt): design frame M2.4-10 · Adolescent list
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Form button only for girls** (hidden for boys) | AdolescentHealthListAdapter.kt:66-70 | Partial | The design shows a boy (Rohit Kumar) with a "Visit" action, which is wrong. |
| 2 | Button **"Register" (red) before the first record, "View" (green) after** | AdolescentHealthListAdapter.kt:107-108 | Partial | The design always says "Visit". |
| 3 | **Mobile consent verified / not-verified icon** (hidden on Mitanin) | res/layout/rv_item_adolscent_health_list.xml:338-341 | Missing | |
| 4 | Sync icon only once a record exists | AdolescentHealthListAdapter.kt:53 | Partial | |
| 5 | Relation shown as father, husband or spouse depending on gender and age | AdolescentHealthListAdapter.kt:72-105 | Partial | |

### Adolescent health form (`AdolescentHealthFormFragment` in form/AdolescentFormFragment.kt): design frame M2.4-11 · Adolescent health form
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **One record per adolescent, edited in place.** A saved record opens read-only with an **Edit FAB** | AdolescentFormFragment.kt:40-73; AdolescentHealthViewModel.kt:69-79 | Missing | The design implies repeat visits ("Last visit", "Follow-up due") and has no view or edit state. |
| 2 | "Saving…", "Saved Successfully!" and "Save Failed!" toasts; Submit disabled while saving; sync after save | AdolescentFormFragment.kt:75-93 | Partial | |

### Adolescent health shell (`AdolescentHealthFragment`, ui/home_activity/child_care/adolescent_list/AdolescentHealthFragment.kt): design frame: NO SCREEN IN DESIGN
No gaps. It is an empty shell with no logic and no navigation.

---

No gaps: EligibleCoupleFragment (EC hub), ChildCareFragment (Child care hub), ChildImmunizationCategoriesFragment (dead code), AdolescentHealthFragment (empty shell).

#### For awareness only: design shows it, code does not have it
"Save draft" on EC registration, EC tracking, HBNC, HBYC and adolescent forms. Child registration tile on the Child care hub. Newborn chips (LBW / SNCU). CUFY chips (SAM check due, SAM / MAM). EC registration chips (Newly married, Age < 20). EC hub stat tiles and "Monthly tracking" banner. Immunization "Share list" session banner. Rota "not given → add refusal reason" banner. HBNC "Refer now / Call 108" sheet. Immunization filter Status radio and multi-select.

---

## Disease control, Communicable diseases, General OP care — missing functionality in design

Summary: 27 screens checked, 71 missing features, 32 partial.

Paths are relative to `app/src/main/java/org/piramalswasthya/sakhi/` unless they start with `res/`.
Design source checked: `design/figma-plugin/src/38-stage-2.8-disease.js` (frames M2.8-01…16), `37-stage-2.7-ncd-tb.js` (M2.7-01 TB tiles), `44-stage-2.14-navigation.js` (tile → frame map), `45-stage-2.15-specs.js` + `25-audit-data.js` (AUDIT_CARDS / AUDIT_FORMS).

**The main structural gap:** in the design, a disease tile goes straight to one person's form (`44-stage-2.14-navigation.js:23-24`: Malaria → M2.8-03, Leprosy → M2.8-05, Filaria → M2.8-07, Kala-azar/AES → M2.8-08). In the code, every communicable-disease screening goes **disease tile → (sub-hub) → household list → household members list → form**. None of the sub-hubs, household lists, member lists or the village-wide suspected/confirmed lists has a frame. The member-list card specs (`rv_item_*_member_list`, `rv_item_malaria_*`) in 2.17 card specs show labels only and leave out the status buttons. The extractor found no buttons on them.

---

### Disease control hub (`DiseaseControlFragment`, ui/home_activity/disease_control/DiseaseControlFragment.kt) — design frame: M2.8-01 · Disease control hub
No gaps. 3 tiles (General check-up / NCD / Communicable) match `IconDataset.getDiseaseControlDataset` (configuration/IconDataset.kt:557-581). In the code the tiles show no counts, but the design shows counts. That is a design extra, not a gap.

### Communicable diseases grid (`CommunicableDiseasesGridFragment`, ui/home_activity/disease_control/communicable/CommunicableDiseasesGridFragment.kt) — design frame: M2.8-02 · Communicable diseases
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | TB tile opens a separate **TB hub** (TB screening / suspected / confirmed / TPT / vulnerable) | configuration/IconDataset.kt:617-622 → CdFragment | Partial | The nav map sends "Tuberculosis (TB)" straight to M2.7-07 TB screening. The TB hub in the design only exists inside the NCD & TB hub (M2.7-01). |
| 2 | Tile counts: in the code, Malaria, Kala-azar, AES/JE, Filaria and Leprosy all show the **same TB-screening count**. TB has no count. | IconDataset.kt:587, 593, 600, 607, 614 (`recordsRepo.tbScreeningListCount`) | Partial | The design shows a different count for each disease ("4 suspected · 1 confirmed"). The code count looks like a bug, so we need to agree which number each tile shows. |

### TB hub (`CdFragment`, ui/home_activity/communicable_diseases/CdFragment.kt) — design frame: NO SCREEN IN DESIGN (TB tiles only in M2.7-01 · NCD & TB hub)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | A TB-only hub reached from Communicable → TB, with 5 tiles and counts: TB screening (opens the **household list**), TB suspected, TB confirmed, TPT, Vulnerable population | IconDataset.kt:782-818 | Partial | The tiles exist in M2.7-01, but under a combined NCD & TB hub. The code puts TB under Communicable diseases. In the design, TB screening opens one person's form, not a household list. |

### Malaria sub-hub (`MalariaIconsFragment`, ui/home_activity/disease_control/malaria/form/MalariaIconsFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Malaria sub-hub with 2 tiles: **Malaria** (count = households, opens the malaria household list) and **Malaria confirmed** (count = confirmed cases, opens the confirmed list) | IconDataset.kt:821-840 | Missing | The design goes Malaria tile → screening form. |

### Leprosy sub-hub (`LeprosyFragment`, ui/home_activity/disease_control/leprosy/LeprosyFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Leprosy sub-hub with 3 tiles: **Leprosy screening** (count = households → household list), **Leprosy suspected** (count), **Leprosy confirmed** (count) | IconDataset.kt:531-553 | Missing | The design goes Leprosy tile → screening form. The suspected and confirmed lists are only mentioned in a banner ("She will move to the Leprosy suspected list"). |

### Disease household list (`AllHouseHoldDiseaseControlFragment`, ui/home_activity/disease_control/AllHouseHoldDiseaseControlFragment.kt) — design frame: NO SCREEN IN DESIGN
Shared by Malaria, Kala-azar, AES/JE, Filaria, Leprosy screening and TB screening.
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | List of all households for the chosen disease. Toolbar title and icon change with the disease. | AllHouseHoldDiseaseControlFragment.kt:170-187 | Missing | |
| 2 | Search by household ID, head-of-family name or phone number | AllHouseHoldDiseaseControlViewModel.kt:82-96 | Missing | |
| 3 | **Voice search** (mic button) | AllHouseHoldDiseaseControlFragment.kt:40-44, 145-146 | Missing | No mic or voice search exists anywhere in the design kit. |
| 4 | Sort: deactivated households last, then newest first | AllHouseHoldDiseaseControlViewModel.kt:98-101 | Missing | |
| 5 | Deactivated households shown greyed with a **"Deleted Record"** label and a grey members button | adapters/HouseHoldListAdapter.kt:83-91 | Missing | |
| 6 | **"N members"** button opens that household's member list for the current disease (malaria / AES / KA / filaria / leprosy / TB) | AllHouseHoldDiseaseControlFragment.kt:61-118; res/layout/rv_item_household.xml:210-219 | Missing | |
| 7 | **"Add MDA"** button on each household card, **Filaria only** → Filaria MDA form for that household | HouseHoldListAdapter.kt:63-70; AllHouseHoldDiseaseControlFragment.kt:123-130 | Missing | |
| 8 | "Add beneficiary" button hidden in disease mode | HouseHoldListAdapter.kt:64, 107 | Missing | The rule is worth stating on the household card spec. |
| 9 | Soft-delete (deactivate) icon shown on active households, but tapping it does nothing here | HouseHoldListAdapter.kt:80-101; AllHouseHoldDiseaseControlFragment.kt:131-133 (empty handler) | Missing | Probably should be hidden. Flag before designing. |
| 10 | Empty state "No records found" | AllHouseHoldDiseaseControlFragment.kt:139-140 | Missing | |

### Malaria household members + IRS + mosquito net (`MalariaSuspectedListFragment`, ui/home_activity/disease_control/malaria/form/list/MalariaSuspectedListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Members of the household, each with a coloured **status button** that shows the latest case status: "Screening" (red, none yet), "Suspected" (orange), "Malaria confirmed" (green), "Not confirmed" (grey), "Treatment started" (purple), "Not suspected" (green), "View" | adapters/MalariaMemberListAdapter.kt:54-94 | Missing | |
| 2 | Status icon on the card: mosquito / warning / tick / pill, depending on the case status | MalariaMemberListAdapter.kt:63-87 | Missing | |
| 3 | Button hidden for a dead member with no malaria record. Death icon on the card. Sync icon only when a record exists. | MalariaMemberListAdapter.kt:54, 133-141; res/layout/rv_item_malaria_members_list.xml:71-76 | Missing | |
| 4 | **IRS ON/OFF switch** opens an IRS-round panel (only one of IRS / mosquito net can be open at a time) | MalariaSuspectedListFragment.kt:132-178 | Missing | No IRS (indoor residual spraying) anywhere in the design. |
| 5 | IRS round form: date fixed to today, round dropdown pre-filled from the last round, **Submit** | configuration/IRSRoundDataSet.kt:26-63; Fragment.kt:221-239 | Missing | |
| 6 | IRS submit **disabled after 4 rounds in a year** for the household | malaria/form/list/MalariaIRSViewModel.kt:92-97, 100-104; repositories/MalariaRepo.kt:606-610 | Missing | |
| 7 | IRS round history list (Sr. no + round no.) | Fragment.kt:213-219; adapters/IrsRoundListAdapter.kt | Missing | |
| 8 | Toast "IRS submitted" then back | Fragment.kt:241-250 | Missing | |
| 9 | **Mosquito net ON/OFF switch** opens the household mosquito-net form (server-driven) | Fragment.kt:159-177, 256-266 | Missing | |
| 10 | Mosquito-net form **photo upload** (camera or file, max 5 MB, error if larger) | Fragment.kt:61-117, 304-306, 317-328 | Missing | |
| 11 | Mosquito-net visit date rules: not in the future, must be after the previous visit, and not today if today is already filled | Fragment.kt:346-363; mosquito_net/MosquitoNetFormViewModel.kt:233-266 | Missing | |
| 12 | Mosquito-net history table ("Net distributed? Yes/No" + date) | Fragment.kt:274-290 | Missing | |
| 13 | Toasts "Data saved successfully" / "Data already exist" | Fragment.kt:399-408 | Missing | |
| 14 | Empty state | Fragment.kt:196-199 | Missing | |

### AES/JE household members (`AESSuspectedListFragment`, ui/home_activity/disease_control/aes_je/list/AESSuspectedListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Member list with a **Register** (red, no record) / **View** (green) button | adapters/AESMemberListAdapter.kt:77-79 | Missing | |
| 2 | Button hidden for a dead member who has no AES record. Death icon. Sync icon when a record exists. | AESMemberListAdapter.kt:77 | Missing | |
| 3 | "Is there a herd of pigs near the house?" Yes/No question at household level (JE risk) | AESSuspectedListFragment.kt:58-62; res/layout/fragment_display_search_rv_button.xml:117-155 | Missing | In practice it never shows: the code compares the disease title "AES / JE" with the enum name "AES_JE", and the answer is not saved. Decide whether to drop it or fix it. |

### Kala-azar household members (`KalaAzarSuspectedListFragment`, ui/home_activity/disease_control/kala_azar/list/KalaAzarSuspectedListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Member list with a **Register** / **View** button | adapters/KalaAzarMemberListAdapter.kt:91-92 | Missing | |
| 2 | Button hidden for a dead member with no record. Death and sync icons. | KalaAzarMemberListAdapter.kt:52, 81-90 | Missing | |

### Filaria household members (`FilariaSuspectedListFragment`, ui/home_activity/disease_control/filaria/list/FilariaSuspectedListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Member list with a **Register** / **View** button | adapters/FilariaMemberListAdapter.kt:93-94 | Missing | |
| 2 | Button hidden for a dead member with no record. Death and sync icons. | FilariaMemberListAdapter.kt (bind: isDeath / filaria null) | Missing | |

### Leprosy household members (`LeprosySuspectedListFragment`, ui/home_activity/disease_control/leprosy/list/LeprosySuspectedListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Status button: **Screening** (no record or no symptoms), **Suspected** (symptoms), **Follow up** (confirmed, no follow-up this visit), **View follow-up** (confirmed and already followed up), **View** | adapters/LeprosyMemberListAdapter.kt:17-31 | Missing | |
| 2 | **"Visits"** button only when the person has finished at least one earlier leprosy visit cycle (currentVisitNumber > 1) | LeprosyMemberListAdapter.kt:70-83 | Missing | |
| 3 | **Visits bottom sheet** listing earlier visit cycles (Visit 1…n-1), with an empty message. Tapping one opens that visit read-only. | LeprosySuspectedListFragment.kt:93-133 | Missing | |
| 4 | Button hidden for a dead member with no record. Death and sync icons. | LeprosyMemberListAdapter.kt:90, 119-127 | Missing | |

### Leprosy suspected list — village-wide (`SuspectedLeprosyListFragment`, ui/home_activity/disease_control/leprosy/suspected/list/SuspectedLeprosyListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | All leprosy-suspected people across households, with a status button → Suspected leprosy form | SuspectedLeprosyListFragment.kt (onViewCreated 6-18); SuspectedLeprosyViewModel.kt:17 | Missing | The design only mentions it in the M2.8-05 banner text. |
| 2 | Empty state. There is no search here (search is hidden in the code). | Fragment onViewCreated (llSearch GONE) | Missing | |

### Leprosy confirmed list (`LeprosyConfirmedListFragment`, ui/home_activity/disease_control/leprosy/confirmed/list/LeprosyConfirmedListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | All confirmed leprosy cases. The button reads **Follow up** (red) until this visit has a follow-up, then **View follow-up** (green). Opens the confirmed / MDT follow-up form. | LeprosyConfirmedListFragment.kt (onViewCreated); LeprosyMemberListAdapter.kt:21-27 | Missing | |

### Malaria confirmed list (`ConfirmedMalariaLIstFragment`, ui/home_activity/disease_control/malaria/form/confirmed/list/ConfirmedMalariaLIstFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | All confirmed malaria cases. Card shows the **date of diagnosis** and a case-status icon. | res/layout/rv_item_malaria_cases_confirmed_list.xml:319-340; adapters/MalariaConfirmedCasesListAdapter.kt:49-68 | Missing | |
| 2 | Button reads **"Follow up"** (red) until treatment starts, then the **last saved day** ("Day 2") in green | MalariaConfirmedCasesListAdapter.kt:114-115 | Missing | |
| 3 | Button hidden for a dead person with no follow-up | MalariaConfirmedCasesListAdapter.kt:104-112 | Missing | |

### Malaria screening form (`MalariaFormFragment`, ui/home_activity/disease_control/malaria/form/form/MalariaFormFragment.kt) — design frame: M2.8-03 · Malaria screening
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Multi-visit**: each submit saves a new visit (visit no. +1). The header shows "Visit no." | malaria/form/form/MalariaFormViewModel.kt:165-168; Fragment.kt:90, 100-102 | Partial | No visit number in the design. |
| 2 | **Earlier visits list** under the form (visit no., date, status). Only "alive" visits are listed. | Fragment.kt:85-96; MalariaFormViewModel.kt:77-84; adapters/VisitsListAdapter.kt:45-47 | Missing | |
| 3 | Opens **read-only** when a record exists. **Edit (pencil) FAB** only when the last case status is Suspected / Not confirmed / Not suspected and the person is not dead. Submit is hidden in view mode. | Fragment.kt:43-61 | Partial | The design shows only an empty editable form with Save draft / Submit. |
| 4 | **Case status is auto-calculated and locked**: Suspected if any main symptom = Yes or 2 or more other symptoms = Yes, otherwise Not suspected. Test type only appears when Suspected. | configuration/MalariaFormDataset.kt:178-186, 590-622 | Partial | The design shows "Case status *" as a free dropdown. |
| 5 | Dates limited to the **last 90 days** up to today | MalariaFormDataset.kt:26-27, 47-48 | Partial | The rule isn't shown in the design (no helper text). |
| 6 | Status "Died" marks the **beneficiary as dead** across the app (date, place, reason). Toast "Beneficiary marked as death". | MalariaFormViewModel.kt:142-163; Fragment.kt:115-121 | Partial | The design has an Alive/Died toggle but no warning or confirmation that this marks the person dead everywhere. |
| 7 | Header card with name, age and gender (patient info) | Fragment.kt:97-103 | — | Designed (subtitle). |
| 8 | No "Save draft" in the code | — | — | The design adds a Save-draft button that the code doesn't have (design extra). |

### Malaria confirmed follow-up / treatment (`ConfirmedCaseFollowUpFormFragment`, ui/home_activity/disease_control/malaria/form/confirmed/form/ConfirmedCaseFollowUpFormFragment.kt) — design frame: M2.8-04 · Malaria confirmed · treatment
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Pf vs Pv picked automatically** from the slide-test result. Only one day-tracker is shown: Pf → 3 days, Pv → 4 days, **Mitanin flavour Pv → 14 days**. | ConfirmedCaseFollowUpViewModel.kt:87; configuration/MalariaConfirmCasesDataset.kt:54-63, 106-125, 151-159 | Partial | The design shows both trackers and "Pv 14 days" for everyone. Add the flavour gate and the one-at-a-time rule. |
| 2 | Completion date **auto-set** to start + 3 days (Pf) or + 4 days (Pv), and limited to that range | MalariaConfirmCasesDataset.kt:209-230 | Missing | |
| 3 | One record per person: opens read-only with an **Edit FAB**, and the edit overwrites it (the code has no per-day save) | Fragment.kt:39-49 | Partial | The design shows a "Save day 2" bottom bar. The code uses a "Day N" dropdown plus Submit. |

### Kala-azar form (`KalaAzarFormFragment`, ui/home_activity/disease_control/kala_azar/form/KalaAzarFormFragment.kt) — design frame: M2.8-08 · Kala-azar (AES/JE same pattern)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Opens **read-only** if a record exists. **Edit FAB** hidden once the person is marked dead. | Fragment.kt:40-52 | Partial | |
| 2 | Status "Death" marks the beneficiary dead app-wide. Toast "Beneficiary marked as death". | kala_azar/form/KalaAzarFormViewModel.kt:128-145; Fragment.kt (setMessage) | Partial | |
| 3 | Visit and death dates limited to the last 90 days | configuration/KalaAzarFormDataset.kt:21-22, 42-43, 112-113 | Partial | |

### AES / JE form (`AEFFormFragment`, ui/home_activity/disease_control/aes_je/form/AEFFormFragment.kt) — design frame: M2.8-08 (banner "AES / JE uses the same form" only)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Read-only view + **Edit FAB** (hidden if dead). Submit hidden in view mode. | Fragment.kt:39-52 | Partial | |
| 2 | Death status marks the beneficiary dead app-wide | aes_je/form/AESFormViewModel.kt:117-134 | Partial | |
| 3 | Dates limited to the last 90 days | configuration/AESJEFormDataset.kt:21-22, 42-43 | Partial | |

### Filaria form (`FilariaFormFragment`, ui/home_activity/disease_control/filaria/form/FilariaFormFragment.kt) — design frame: M2.8-07 · Filaria case & MDA
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Read-only view + **Edit FAB** whenever a record exists (no death check) | Fragment.kt:41-50 | Partial | |
| 2 | Child **under 2 years**: "DEC & Albendazole dose" is locked to "Yes" | configuration/FilariaFormDataset.kt:163-171, 285-301 | Missing | |
| 3 | Saved "Other side effect" text is locked when re-opened | FilariaFormDataset.kt:150-156 | Partial | |

### Filaria MDA (household) form (`FilariaMDAFormFragment`, ui/home_activity/disease_control/filaria/form/FilariaMDAFormFragment.kt) — design frame: merged into M2.8-07 as one "MDA medicine given?" question
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | A **separate household-level** MDA distribution form (server-driven), opened from "Add MDA" on the household card | Fragment.kt:1-25; AllHouseHoldDiseaseControlFragment.kt:123-130 | Missing | The design treats MDA as a per-person question. |
| 2 | **Once per household per month**. Toast "You have already submitted this form for this month". | filaria/form/FilariaMDAFormViewModel.kt:203-208, 260-276 | Missing | |
| 3 | Visit date not in the future and after the previous visit (error under the field) | Fragment.kt:98-115 | Missing | |
| 4 | **MDA history table** (distribution date, medicine distributed Yes/No) | Fragment.kt:27-44 | Missing | |
| 5 | Photo / file upload field (camera or gallery, size limit) | Fragment.kt:58-60 (pick_image → showImagePickerDialog) | Missing | |
| 6 | Toasts: saved / failed to submit | Fragment.kt:157-163 | Missing | |

### Leprosy screening form (`LeprosyFormFragment`, ui/home_activity/disease_control/leprosy/form/LeprosyFormFragment.kt) — design frame: M2.8-05 · Leprosy screening
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Opens **read-only** if screened. **Edit FAB** unless dead. | Fragment.kt:41-56 | Partial | |
| 2 | **Visit-cycle label** ("Visit N") on the form. A new cycle starts after treatment completes. | configuration/LeprosyFormDataset.kt:286-291, 325 | Missing | |
| 3 | Leprosy status is auto-set and locked: Yes → Suspected, No → Screening | LeprosyFormDataset.kt:104-113, 445-458 | — | Designed (warning banner). Listed only to confirm. |
| 4 | Death status marks the beneficiary dead app-wide | form/LeprosyFormViewModel.kt:121-138 | Partial | |
| 5 | Home-visit date: up to 1 year back. Other dates: 90 days. | LeprosyFormDataset.kt:41-42, 62-63 | Partial | |

### Suspected leprosy form (`SuspectedLeprosyFormFragment`, ui/home_activity/disease_control/leprosy/suspected/from/SuspectedLeprosyFormFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Form for a suspected case. Screening details (home-visit date, status, referred to, symptoms, visit no.) are shown **pre-filled and locked**. | configuration/LeprosySuspectedDataset.kt:18-60, 70-110 | Missing | |
| 2 | Entering **Type of leprosy** confirms the case and moves the person to the Confirmed list | LeprosySuspectedDataset.kt:245-250 | Missing | |
| 3 | Treatment start date cannot be before the home-visit date | LeprosySuspectedDataset.kt:274 | Missing | |
| 4 | Always editable (no view mode / edit FAB). Toast on save. | SuspectedLeprosyFormFragment.kt:43-44 | Missing | |

### Leprosy confirmed / MDT follow-up (`LeprosyConfirmedFromFragment`, ui/home_activity/disease_control/leprosy/confirmed/form/LeprosyConfirmedFromFragment.kt) — design frame: M2.8-06 · Leprosy confirmed · MDT
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **List of earlier follow-ups** (date + treatment status) above the form | Fragment.kt:51-60, 129-136; FollowUpDatesAdapter | Missing | |
| 2 | Each save adds a new follow-up (repeatable monthly) | confirmed/form/LeprosyConfirmedFromViewModel.kt:170-189 | Partial | The design shows a single "Save visit". |
| 3 | Treatment start date **locked once set** | configuration/LeprosyConfirmedDataset.kt:656-663 | Missing | |
| 4 | Follow-up date: not before the home visit and not in the future. Treatment end date required when status = Completed. Errors shown as a toast. | LeprosyConfirmedDataset.kt:592-653; Fragment.kt:97-102 | Partial | |
| 5 | **"Treatment completed" closes the visit cycle** and resets the person to screening for the next visit. Toast "Visit completed, starting next visit screening". | LeprosyConfirmedFromViewModel.kt:191-205; LeprosyRepo.kt:305-317; Fragment.kt:117-121 | Missing | |
| 6 | Toasts "Follow-up saved" / "Failed to save follow-up" | Fragment.kt:107-125 | Partial | |

### Leprosy past visit (`LeprosyVisitFragment`, ui/home_activity/disease_control/leprosy/visits/LeprosyVisitFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Read-only view of one **earlier visit cycle**: its screening data plus its follow-up list. There is no submit. | LeprosyVisitFragment.kt (onViewCreated); visits/LeprosyVisitViewModel.kt:33, 112 | Missing | The fields are rendered enabled but cannot be saved. |

### General check-up list (`GeneralCheckupListFragment`, ui/home_activity/disease_control/general_checkup/list/GeneralCheckupListFragment.kt) — design frame: M2.8-09 · General check-up list
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Voice search** (mic) | GeneralCheckupListFragment.kt:32-37, 72 | Missing | |
| 2 | **Dead beneficiaries are excluded** from the list | general_checkup/list/GeneralCheckupListViewModel.kt:25 | Partial | The rule isn't stated. |
| 3 | Card shows husband's name (woman over 15 with a spouse) or else father's name | general_checkup/list/GeneralCheckupListAdapter.kt:31-37 | — | Designed ("W/o / S/o"). |
| 4 | Empty state | Fragment.kt:66 | Missing | |

### Add check-up (`GeneralCheckupFormFragment`, ui/home_activity/disease_control/general_checkup/form/GeneralCheckupFormFragment.kt) — design frames: M2.8-10 · Add check-up, M2.8-11, M2.8-12
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Pointer links** "Looking for TB, malaria, leprosy…? Use Communicable Diseases" and "High BP, diabetes…? Use NCD" open those hubs. Shown only **before a problem type is chosen**. | GeneralCheckupFormFragment.kt:129-139, 153-155; res/values/strings_general_checkup.xml:16-17 | Missing | The hub banner in M2.8-01 is different (static, on the hub). |
| 2 | **Form not available** state (form not downloaded yet / server form incomplete) with a **Try again** button. Refer is disabled until the form is ready. | Fragment.kt:107-125; strings_general_checkup.xml:26-28 | Missing | |
| 3 | Loading spinner while the server form loads | Fragment.kt:111 | Missing | |
| 4 | Problems owned by **two different modules cannot both be ticked**. The second is rejected with a toast "…already selected… Untick it first." | Fragment.kt:94-101; GeneralCheckupFormViewModel.kt:183, 203-210; strings:35 | Missing | |
| 5 | "Other" problem needs a **name typed in** (required, with a character limit) | strings_general_checkup.xml:18, 25; ViewModel.kt:223-235, 382 | Partial | The design shows an "Other" checkbox with no text field or error. |
| 6 | Validation errors (no type / no problem / date outside 1 month back to today) shown as a toast plus a field error | Fragment.kt:161-164; ViewModel.kt:348-370 | Partial | Only the date helper text is designed. |
| 7 | Routing "Continue in <module>" for **Cataract** goes to **All Beneficiaries** (the user then picks the eye from the card), not straight to a form | Fragment.kt:207-209 | Partial | The design only says "Continue there". |
| 8 | Duplicate dialog's action is **"View in HWC Referrals"** → HWC referred list. Cancel. Can refer again next month. | Fragment.kt:215-226; strings:56-58 | Partial | The design note says "Show the existing referral / Close". |
| 9 | After the refer form returns: the check-up is saved, toast "Referral submitted", back to the list. "Could not save" toast on failure. | Fragment.kt:233-267 | Partial | The success snackbar is designed on the refer form. The failure state is not. |
| 10 | Earlier check-ups header **tap to expand/collapse**. "No earlier check-ups" text when there are none. | Fragment.kt:70-81 | — | Designed (collapsed section). |

### General OP care (`GeneralOpCareFragment`, ui/home_activity/general_op_care/GeneralOpCareFragment.kt) — design frame: M2.8-16 · General OP care
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Call** button on each card (asks for phone permission if missing, toast "Please allow permissions first") | GeneralOpCareFragment.kt:30-47; res/layout/rv_general_opd_ben.xml:98-104 | Missing | |
| 2 | Card shows the **referral details**: referred date, referred to, follow-up date, referred from (village), referral reason, registration date | res/layout/rv_general_opd_ben.xml:254-460 | Partial | The design card shows only "OPD date" and a complaint tag. |
| 3 | Sorted newest first (modified / visit / registration date) | general_op_care/GeneralOpCareViewModel.kt:17-21 | Missing | |
| 4 | **Voice search** (mic) | Fragment.kt:13-18, 65-66 | Missing | |
| 5 | Empty state | Fragment.kt:55-59 | Missing | |
| 6 | Read-only list (records come down from the server, no open/edit) | adapters/GeneralOPDAdapter.kt | Partial | The design has a "View" button that the code doesn't have. Confirm whether it's wanted. |

---

No gaps: Disease control hub (`DiseaseControlFragment`).

---

## NCD, TB and CHO / HRP: functionality missing from the design

Summary: 38 screens checked, 68 missing features, 51 partial (115 rows plus 4 cross-cutting).

Scope: `ui/home_activity/non_communicable_diseases/**` and `ui/home_activity/cho/**` (paths below are relative to `app/src/main/java/org/piramalswasthya/sakhi/`).
Design sources checked: `37-stage-2.7-ncd-tb.js` (M2.7-01…11), `36-stage-2.6-hrp-cho.js` (M2.6-01…08), `38-stage-2.8-disease.js` (M2.8-13/14/15 HWC referral), `30-stage-2.0-kit.js` (generic patterns P4 visit-history sheet, P5 view mode, P6 CBAC score), `42-stage-2.12-supervisor.js`, `44-stage-2.14-navigation.js`, `45-stage-2.15-specs.js` + `25-audit-data.js` (AUDIT_FORMS / AUDIT_CARDS).

Where the code puts things: the NCD hub sits under Disease control and has 3 tiles (eligible / priority / not eligible). TB is a different hub (Disease control → Communicable → TB, `CdFragment`, outside this scope) with 5 tiles: screening, suspected, confirmed, TPT, vulnerable. The HWC referred list is a **Home** tile. Design M2.7-01 puts NCD and TB on one hub. That is a layout choice, so it is not counted as a gap.

### Cross-cutting (applies to every list in this scope)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| X1 | Voice search (mic button fills the search box by speech) | every list: `binding.ibSearch.setOnClickListener { sttContract.launch(Unit) }`, e.g. `ncd_eligible_list/NcdEligibleListFragment.kt:64` | Partial | Design search bar only has a placeholder. No mic is visible in any 2.6/2.7 frame. |
| X2 | Deceased beneficiary card: grey background, "Death" tag, action button hidden | `adapters/TbScreeningListAdapter.kt:97-106`, `TbSuspectedListAdapter.kt:93-100`, `TbConfirmedListAdapter.kt:94-101`, `TPTListAdapter.kt:63-65,111`, `NcdReferListAdapter.kt:90-95`, `VulnerableListAdapter.kt:115-123`; layouts `rv_item_tb_*_list.xml:77/88` (`isDeath ? VISIBLE`) | Missing | No dead/deceased state on any record card in design. |
| X3 | Supervisor role sees read-only lists (all card action buttons hidden) | `HRPAdapter.kt:57-60`, `TbSuspectedListAdapter.kt:52-55`, `TbConfirmedListAdapter.kt:52-56` (Follow-up only for role "asha"), `NcdPriorityListFragment` `showActionButtons=false` | Missing | Supervisor design (M2.12) covers incentive verification only. It has no read-only HRP / NCD / TB lists. |
| X4 | Button colour shows record state (red = not started, orange = partial, green = done) | `HRPAdapter.kt:158-199`, `HRPTListAdapter.kt:119-146`, `TbScreeningListAdapter.kt:91-93`, `TbConfirmedListAdapter.kt:110`, `TPTListAdapter.kt:67-84`, `NcdReferListAdapter.kt:113-125` | Partial | Design uses a status tag plus one primary action. The 3-state button label and colour logic is not mapped anywhere. |

### NCD hub (`NcdFragment`, ui/home_activity/non_communicable_diseases/NcdFragment.kt) — design frame: M2.7-01 · NCD & TB hub
No gaps. The design has more on this hub than the code does.

### NCD list, supervisor "NCD screened" (`NcdListFragment`, non_communicable_diseases/ncd_list/NcdListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Supervisor list of every beneficiary for NCD, with search and an action-bar title for the Supervisor activity | reached only from Supervisor home tile `sup_ncd_screened` (`IconDataset.kt:276`); ASHA tile commented out (`IconDataset.kt:718-723`); role switch `NcdListFragment.kt:181-185` | Missing | |
| 2 | Card shows Call, ABHA, sync icon and registration date. Tapping Call places a phone call, or shows a "Please allow permissions first" toast | `NcdListFragment.kt:92-116` | Missing | The ABHA click handler is empty (`:88`), so the ABHA icon does nothing here. |

### NCD eligible list / CBAC worklist (`NcdEligibleListFragment`, non_communicable_diseases/ncd_eligible_list/NcdEligibleListFragment.kt) — design frame: M2.7-02 · NCD list · CBAC due / priority
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 3 | Category chips: All / Screened / Not screened | `NcdEligibleListViewModel.kt` categoryData() + filter (`benList` combine); `NcdEligibleListFragment.kt:69` | Partial | Design tabs are CBAC due / Priority / Not eligible. Screened vs not-screened is not shown. |
| 4 | Age-band dropdown filter (35+, 40+ … 80+ in 5-year steps) | `NcdEligibleListFragment.kt:66-67,134-142`; `NcdEligibleListViewModel.kt` yearsList()/matchesYearBand() | Missing | |
| 5 | "Add CBAC" is blocked for 365 days after the last CBAC. Toast: "Available on <date>" | `NcdCbacBenListAdapter.kt:98-111` (Konstants.minMillisBwtweenCbacFiling = 365 days), `NcdEligibleListFragment.kt:85-91` | Missing | Design tag says "CBAC due (yearly)" but has no blocked state or toast. |
| 6 | "View CBAC" button (opens CBAC history sheet) shown only when at least 1 CBAC is saved | `rv_item_ncd_cbac.xml:324-334` (`list_avail`), `NcdEligibleListFragment.kt:74-83` | Missing | AUDIT_CARDS for `rv_item_ncd_cbac` has no buttons (the extractor missed both). |
| 7 | Card sync icon (all CBACs synced or not) | `rv_item_ncd_cbac.xml:69-78` (`benWithCbac.allSynced`) | Partial | Design card shows a sync tag only on the Draft example. |
| 8 | List covers age ≥ min NCD age, not pregnant, not deactivated, not dead | `BenDao.kt:1054` | Partial | Design subtitle says "People aged 30+". Exclusion of pregnant women is not stated. |

### CBAC history sheet (`NcdBottomSheetFragment`, non_communicable_diseases/ncd_eligible_list/NcdBottomSheetFragment.kt) — design frame: NO SCREEN IN DESIGN (generic M2.0-P4 only)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 9 | Bottom sheet listing past CBACs (newest first), each with fill date, View button and a per-record sync icon | `NcdBottomSheetFragment.kt:37-60`; `rv_item_ncd_cbac_element.xml:33-55`; reverse order `NcdEligibleListViewModel.kt` ncdDetails | Missing | Only the generic ANC history sheet exists. Nothing CBAC-specific. |
| 10 | View opens the CBAC in read-only mode | `NcdBottomSheetFragment.kt:39-47` (passes cbacId) → `CbacFragment.kt:772` setUpView | Partial | Generic P5 view mode exists. There is no CBAC view-mode frame. |

### CBAC form (`CbacFragment`, non_communicable_diseases/cbac/CbacFragment.kt) — design frame: M2.7-03 / M2.7-04 / M2.7-05 (+ M2.0-P6)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 11 | View mode: whole form read-only, Save hidden, values restored | `CbacFragment.kt:63-65` (cbacId != 0), `:772-783` | Partial | Generic only (P5). No CBAC view frame. |
| 12 | Date of CBAC with a picker. Earliest allowed date = last CBAC + 365 days (or the registration date); latest = today | `CbacFragment.kt:811-838`; `CbacViewModel.kt:234` | Missing | Design has no date field on CBAC. |
| 13 | Age is auto-filled and scored from the beneficiary's age (not answered) | `CbacViewModel.kt:239-258`; `CbacFragment.kt:857` raAgeText | Partial | P6 shows "Age … (auto)". M2.7-03 wrongly shows age as a radio question. |
| 14 | Waist options differ by gender (male / female cut-offs) | `CbacFragment.kt:849-856` | Partial | Design shows only "Waist (women)". |
| 15 | Part B2 (women's questions) hidden for male beneficiaries | `CbacFragment.kt:851-853` (`cbacLlEdWomen GONE`) | Partial | Design has a B2 tab but no rule for men. |
| 16 | Part B3 (elderly questions) shown only for age ≥ 60 | `CbacFragment.kt:1239-1242,1343` | Partial | Design has a B3 tab but no age rule. |
| 17 | "None of these" checkbox sets all B1/B2 symptom answers to No, and unticking clears them | `CbacFragment.kt:394-560` | Missing | A "None of these" option exists only in the generic kit. |
| 18 | Inline result messages: "collect sputum" (any B1 * question Yes), "trace all family members" (** questions), "refer to MOIC" (PHQ-2 > 3), "NCD suspected" (score ≥ 4) | `CbacFragment.kt:944-968` (ast1/ast2/astMoic), `:754-770`, `:1420-1436` | Partial | M2.7-05 result card covers NCD and TB only. |
| 19 | TB alert dialog with 3 message variants: sputum only / family screening only / both | `CbacFragment.kt:686-726` | Partial | M2.7-04 shows one "Possible TB" dialog. The family-screening (**) variant is missing. |
| 20 | "Refer?" Yes/No dialog per suspicion type, each shown once per CBAC: NCD (score > 4), TB, Leprosy, COPD (occupational exposure chosen), Cancer (lump / bleeding / discharge Yes), Geriatric/depression (PHQ-2 > 3) | `CbacFragment.kt:97-153` buildAsReferAlertDialog; triggers `:232-250` leprosy, `:713-724` TB, `:754-763` NCD, `:1375-1384` COPD, `:1424-1430` geriatric, `:1468-1477` cancer; `CbacViewModel.kt:519-525` | Partial | Design shows only the TB "Refer & add". Leprosy, COPD, cancer, geriatric and NCD refer prompts are missing. |
| 21 | Referral bottom sheet (60 % height) inside CBAC: patient header plus the referral form (facility, reason prefilled, additional services, date). Submit → toast, the referral is attached to this CBAC | `CbacFragment.kt:1571-1663` | Partial | Design has a separate full-screen NCD referral (M2.7-06). The in-CBAC sheet is not designed. |
| 22 | Leprosy suspicion from CBAC (any of 12 skin / nerve signs) auto-creates a Leprosy-suspected record | `CbacFragment.kt:232-245`; `CbacViewModel.kt:972-987` | Missing | |
| 23 | TB suspicion auto-creates a TB screening record on save, toast "CBAC screening submitted" | `CbacFragment.kt:805-808,281-294` | Partial | M2.7-04 dialog text says "will be added to TB screening". |
| 24 | Bleeding after menopause = Yes → "Inform ASHA facilitator" alert plus inline note | `CbacFragment.kt:1206-1219` | Missing | |
| 25 | Elderly B3 answers (unsteady, needs help, forgetting names…) Yes → "Send to MOIC of nearest health centre" alert | `CbacFragment.kt:623-646` (called `:1248-1269`) | Missing | |
| 26 | Occupational exposure = "Other" reveals a free-text field | `CbacFragment.kt:1388-1392` | Partial | Design shows the select with no reveal. |
| 27 | Automatic flags on save: HRP suspected (women of reproductive status 1–3 with listed signs), TB suspected, NCD priority (score ≥ 4 or PHQ-2) set on the beneficiary | `CbacViewModel.kt:603-676` | Missing | These feed other lists (HRP, NCD priority) but are invisible in the design. |
| 28 | Missing-field dialog naming the first unanswered question | `CbacFragment.kt:336-341`; `CbacViewModel.kt:715-755` | Partial | Design relies on generic validation. |
| 29 | After save: marks the beneficiary as referred, triggers sync, returns to the list | `CbacFragment.kt:324-334` | Partial | Design has "Submit CBAC" but no post-save state. |

### CBAC object page (`CbacObjectFragment`, non_communicable_diseases/cbac/CbacObjectFragment.kt) — design frame: n/a
Unused stub: no navigation or references, so there is nothing to design.

### NCD priority list (`NcdPriorityListFragment`, non_communicable_diseases/ncd_priority_list/NcdPriorityListFragment.kt) — design frame: M2.7-02 (Priority tab)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 30 | Read-only list (no action buttons) of people whose latest CBAC score is > 4 | `NcdPriorityListFragment.kt:59-73` (`showActionButtons=false`); `RecordsRepo.kt:141-143` | Partial | Design puts "Start CBAC" style actions on cards. In code, priority rows have no action. |
| 31 | Also opened from Supervisor home ("NCD priority") with a Supervisor action bar | `NcdPriorityListFragment.kt:119-130`; `IconDataset.kt:282` | Missing | See X3. |

### NCD not-eligible list (`NcdNonEligibleListFragment`, non_communicable_diseases/ncd_non_eligible_list/NcdNonEligibleListFragment.kt) — design frame: M2.7-02 (Not eligible tab)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 32 | Read-only list of people whose latest CBAC score is ≤ 4 (not a list of "never eligible" people) | `NcdNonEligibleListFragment.kt:51-56`; `RecordsRepo.kt:146-148` | Partial | The design tab name suggests age-based ineligibility. The real rule is low score. |

### HWC referred list (`NcdRefferedList`, non_communicable_diseases/ncd_referred/list/NcdRefferedList.kt) — design frame: M2.8-14 · HWC referred list (all sources)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 33 | Referral-type filter (filter icon + selected-filter label → bottom sheet) | `NcdRefferedList.kt:89-97,99-105` | Missing | Design has month chips, which the code does not have. Type filter is absent. |
| 34 | Follow-up button: 3 labels and colours. "Start follow-up" (red, none yet) / "View follow-up" (green) / "TB suspected follow-up" (presumptive TB) | `NcdReferListAdapter.kt:103-125` | Partial | Design shows only "View follow-up". |
| 35 | Presumptive-TB referral opens the Suspected TB form instead of the referral follow-up | `NcdRefferedList.kt:71-76,166-172` | Missing | |
| 36 | "Referred from" shows the logged-in ASHA's name | `NcdReferListAdapter.kt:61` | Partial | AUDIT_CARDS has the label. The design card meta uses module source instead. |
| 37 | Also opened from Supervisor | `NcdRefferedList.kt:155-165` | Missing | See X3. |

### Referral type filter sheet (`NCDReferTypeFilter`, non_communicable_diseases/ncd_referred/filter/NCDReferTypeFilter.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 38 | Bottom sheet grid: All / NCD / TB / Leprosy / Geriatric / HRP / Maternal (+ Child on **Mitanin** only); picking one closes the sheet | `NCDReferTypeFilter.kt:36-57`; flavour gate `NcdRefferedListViewModel.kt:126-128` | Missing | Flavour-specific option. |

### Referral follow-up form, NCD / TB / General check-up (`ncd_referred.followUp.NCDReferalFormFragment`, non_communicable_diseases/ncd_referred/followUp/NCDReferalFormFragment.kt) — design frame: M2.7-06 · NCD referral & follow-up; M2.8-15 · HWC referral follow-up
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 39 | Server-driven form (fields from form JSON). Different form for NCD, TB referral and General check-up | `NCDReferalFormFragment.kt:134-168`; VM `:71-86` | Partial | Design draws fixed fields. |
| 40 | Multi-visit logic: main visit (treatment start) then up to 6 monthly follow-ups, then a new visit number. Visit label auto-filled ("Visit-n") | `NCDReferalFormViewModel.kt:93-111,248-256` | Missing | |
| 41 | Follow-up date rules: after the treatment date, exactly in the next calendar month after the last follow-up, not in a future month. Specific error messages | `NCDReferalFormViewModel.kt:573-611`; `NCDReferalFormFragment.kt:227-235` | Missing | M2.8-15 only says "cannot be before referral date". |
| 42 | Visit history table: visits grouped with diagnosis, listing each follow-up number and date | `NCDReferalFormFragment.kt:170-205` | Missing | |
| 43 | View mode with an Edit FAB that unlocks the form | `NCDReferalFormFragment.kt:296-302,319-320,335-336` | Partial | Generic P5 only. No edit FAB. |
| 44 | TB / GC referral follow-up locked once already filled (no Save, no Edit) | `NCDReferalFormFragment.kt:319-320`; VM `:218-244` | Missing | |
| 45 | Photo / file field: "Take photo / Choose from gallery" picker with a size check | `NCDReferalFormFragment.kt:142,271-295`; VM `:520` maxSizeMB | Missing | Only the generic photo tile exists. |
| 46 | "Referred on" date header (TB / GC forms) | `NCDReferalFormFragment.kt:312-316` | Partial | Present in M2.8-15 (auto "Referred on"), not in M2.7-06. |
| 47 | Title switches: General check-up follow-up vs NCD follow-up | `NCDReferalFormFragment.kt:343-354` | Partial | |

### Referral form, NCD / HWC / TB / GC refer (`ncd_referred.form.NCDReferalFormFragment`, non_communicable_diseases/ncd_referred/form/NCDReferalFormFragment.kt) — design frame: M2.7-06 (partly), M2.8-13 (GC refer)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 48 | Reached from CBAC, ANC counselling, TB screening and General check-up. Title changes: Maternal/TB → "HWC refer form", GC → "Refer to facility", else "NCD refer form" | `nav_home.xml:715-720,1100-1105,1210-1214,1944-1945`; `NCDReferalFormFragment.kt:131-139` | Partial | Not shown as a shared screen for ANC / TB. |
| 49 | "Additional services" field (required) and referral reason prefilled from the caller | `configuration/dynamicDataSet/ReferalFormDataset.kt:29-45` | Missing | M2.7-06 has reason checkboxes instead and no "Additional services". |
| 50 | Referral date limited to the last 90 days | `ReferalFormDataset.kt:48-56` | Missing | |
| 51 | Read-only once saved (Submit hidden) | `NCDReferalFormFragment.kt:52-56` | Partial | |
| 52 | After submit, returns the referral to the calling form, which then saves itself (TB screening auto-saves) | `NCDReferalFormFragment.kt:76-89`; `TBScreeningFormFragment.kt` REFERRAL_DONE observer | Missing | |

### TB screening list (`TBScreeningListFragment`, non_communicable_diseases/tb_screening/list/TBScreeningListFragment.kt) — design frame: NO SCREEN IN DESIGN (tile maps straight to form M2.7-07)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 53 | Household-scoped TB screening list (opened per household, or from TB confirmed "Family members" for contact tracing) | `TBScreeningListViewModel.kt:21-27` (hhId arg); `TBConfirmedListFragment.kt:66-72` | Missing | |
| 54 | Card button "Screen" (red) vs "View screening" (green); sync icon only after screening | `TbScreeningListAdapter.kt:55,91-93` | Missing | AUDIT_CARDS for this card has no button. |
| 55 | Nikshay ID row shown instead of RCH ID | `TbScreeningListAdapter.kt:49-50` | Partial | In spec card as label. |

### TB screening form (`TBScreeningFormFragment`, non_communicable_diseases/tb_screening/form/TBScreeningFormFragment.kt) — design frame: M2.7-07 · TB screening
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 56 | Read-only after the first save. No edit and Submit disabled | `TBScreeningFormFragment.kt:88-91` | Missing | Design shows Save draft / Submit only. |
| 57 | "Refer to HWC facility?" Yes/No dialog when any * or ** symptom is Yes. Yes → referral form → auto-save; No → next Submit saves without referral | `TBScreeningFormFragment.kt:48-62,180-205`; `TBScreeningDataset.kt:623-640` | Missing | Design shows a passive "Presumptive TB" banner. |
| 58 | Child-specific symptoms (failure to gain weight, decreased activity) and gender-based fields | `TBScreeningDataset.kt:347,632-633` | Partial | Spec lists fields. The child/gender trigger is not visible in M2.7-07. |
| 59 | Opened also from the Vulnerable population list | `VulnerablePopulationListFragment.kt:63` | Partial | Design gives Vulnerable cards a "Visit" action instead. |

### TB suspected list (`TBSuspectedListFragment`, non_communicable_diseases/tb_suspected/list/TBSuspectedListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 60 | List of presumptive-TB people with search and a "TB suspected follow-up" button (colour by whether a record exists) | `TBSuspectedListFragment.kt:58-68`; `TbSuspectedListAdapter.kt:110-116` | Missing | |
| 61 | Button hidden for Supervisor. For a deceased person it shows only if a record already exists | `TbSuspectedListAdapter.kt:52-56,93-100` | Missing | |
| 62 | Also the Supervisor "TB cases" tile | `IconDataset.kt:288-290`; `TBSuspectedListFragment.kt:102-110` | Missing | See X3. |

### TB suspected form (`TBSuspectedFragment`, non_communicable_diseases/tb_suspected/form/TBSuspectedFragment.kt) — design frame: M2.7-08 · Suspected TB · tests
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 63 | Two-visit flow. Visit 1 = date, case type, sputum collected / where / Nikshay ID. Visit 2 = sputum result, TB confirmed, TPT recommended. Visit-1 fields lock in visit 2; auto label "Visit 1/2" | `SuspectedTBDataset.kt:255-310` | Missing | Design shows everything on one page. |
| 64 | Submit hidden once visit 2 is complete (result + confirmed + TPT answered); button says "Save" vs "Submit" | `TBSuspectedFragment.kt:56-61,69-71`; VM `:85-87` | Missing | |
| 65 | Sputum result Positive → alert "TB suspected · sputum test" | `TBSuspectedFragment.kt:107-114`; `SuspectedTBDataset.kt:477-480` | Missing | |
| 66 | Save also writes a TB referral follow-up status (TB confirmed / Recommend TPT / Not recommended) that feeds the TPT list | `TBSuspectedViewModel.kt:118-140` | Missing | |
| 67 | "Reason for suspicion" appears only for case types 2–3; "TPT recommended?" only when TB is not confirmed | `SuspectedTBDataset.kt:261-263,306-308` | Partial | Design shows TPT question unconditionally. |

### TB confirmed list (`TBConfirmedListFragment`, non_communicable_diseases/tb_confirmed/list/TBConfirmedListFragment.kt) — design frame: NO SCREEN IN DESIGN (tile maps to form M2.7-09)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 68 | List with Follow-up button (red if no follow-up yet, green otherwise) and "Family members" button (orange) → household TB contact screening | `TbConfirmedListAdapter.kt:110-111`; `TBConfirmedListFragment.kt:63-79` | Partial | Both buttons are in spec cards. The list screen and contact-tracing destination are not. |
| 69 | Follow-up button visible only for role "asha" | `TbConfirmedListAdapter.kt:52-56` | Missing | Role gate. |

### TB confirmed / treatment form (`TBConfirmedFormFragment`, non_communicable_diseases/tb_confirmed/from/TBConfirmedFormFragment.kt) — design frame: M2.7-09 · TB treatment follow-up
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 70 | Previous follow-up dates list above the form | `TBConfirmedFormFragment.kt:50-57` | Partial | Design shows a month grid, not the dated history list. |
| 71 | Regimen and treatment start date lock after the first save; follow-up date enabled only after first save, limited to the last 90 days | `TBConfirmedDataset.kt:58-67,219-232` | Missing | |
| 72 | Treatment outcome = Death → beneficiary is marked dead across the app (date / place / reason, reason prefilled "Tuberculosis") | `TBConfirmedViewModel.kt:135-146`; `TBConfirmedDataset.kt:140-170,202` | Missing | High impact side-effect. |

### TPT list (`TPTListFragment`, non_communicable_diseases/tpt/list/TPTListFragment.kt) — design frame: M2.7-10 · TPT for household contacts
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 73 | List source = people the Suspected-TB form marked "Recommended for TPT" (not household contacts of a patient) | `TPTListViewModel.kt` (repository.observeRecommendedTpt) | Partial | Design frames it as contacts of one patient. |
| 74 | Button states: "Start TPT" (red) → "Add follow-up" (orange) → "View details" (green, after treatment completed) | `TPTListAdapter.kt:67-84,156-165` | Partial | Design has Start TPT / Follow up but no completed "View details" state. |
| 75 | Follow-up history printed on the card ("Follow-up n: date") | `TPTListAdapter.kt:57-61` | Missing | |
| 76 | Button hidden if the person died (ben dead, or TPT outcome = death) | `TPTListAdapter.kt:63-65,170-180` | Missing | |
| 77 | Sync icon reflects whether all TPT follow-ups are synced | `TPTListAdapter.kt:47-53` | Partial | |

### TPT start / follow-up form (`TPTStartFragment`, non_communicable_diseases/tpt/start/TPTStartFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 78 | Whole server-driven TPT form (regimen, start date, monthly follow-up, outcome) | `TPTStartViewModel.kt:58-160` | Missing | No TPT form in design or AUDIT_FORMS. |
| 79 | Expected completion date auto-calculated from regimen (6H=6 m, 3HP/3RH=3 m, 4R=4 m, 1HP=1 m…) | `TPTStartViewModel.kt:505-528` | Missing | |
| 80 | Monthly follow-up label auto "Month-n" (max 6). Regimen and start date lock after the first visit | `TPTStartViewModel.kt:108-131` | Missing | |
| 81 | Date rules: start ≥ referral follow-up date and ≤ today; one follow-up per month; follow-up after previous; actual completion after follow-up (toast errors) | `TPTStartViewModel.kt:371-411`; `TPTStartFragment.kt` toasts | Missing | |
| 82 | Death fields shown only when treatment completed + outcome = death | `TPTStartViewModel.kt:180-210` | Missing | |
| 83 | Follow-up schedule table; Save hidden after completion or after 6 follow-ups (form becomes read-only) | `TPTStartFragment.kt:87-90`; VM `:354-369` | Missing | |

### Vulnerable population list (`VulnerablePopulationListFragment`, non_communicable_diseases/vulnerable_population/list/VulnerablePopulationListFragment.kt) — design frame: M2.7-11 · Vulnerable population
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 84 | Membership = TB key-population risk factors from TB screening, CBAC risk codes, NCD diagnoses (diabetes / hypertension), pregnancy, lactating, age > 60, prison | `VulnerablePopulationViewModel.kt:35-66` | Partial | Design shows "Elderly alone / Bedridden / Disability", which is a different concept. |
| 85 | Per-card list of the person's risk factors | `VulnerableListAdapter.kt:54-60` (riskfactRV) | Missing | |
| 86 | Card button "Screen" (red) / "View screening" (green) → TB screening form | `VulnerableListAdapter.kt:109-111`; fragment `:63` | Partial | Design action is "Visit". |

### Vulnerable risk-factor filter (`VulnerableFilterBottomSheet`, non_communicable_diseases/vulnerable_population/list/VulnerableFilterBottomSheet.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 87 | Filter bottom sheet with about 40 key-population risk factors (Diabetes, HIV-risk, Prison, Migrant, Tea-garden worker, Silica…) plus selected-filter label | `VulnerableFilterBottomSheet.kt:34-50`; `VulnerablePopulationViewModel.kt:81-133`; `VulnerablePopulationListFragment.kt:99-115` | Missing | Design uses 4 chips that do not exist in code. |

### CHO beneficiary list (`BenListCHOFragment`, cho/beneficiary/list/BenListCHOFragment.kt) — design frame: NO SCREEN IN DESIGN (tile on M2.6-07 only)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 88 | List of **female** beneficiaries of the selected village, with search | `RecordsRepo.kt:120-123` | Missing | |
| 89 | "Add beneficiary" (orange) bottom button → CHO register | `BenListCHOFragment.kt:118-126` | Missing | |
| 90 | ABHA check: fetch ABHA, then show the number in a dialog, or open the ABHA creation flow | `BenListCHOFragment.kt:105-107,151-170`; VM fetchAbha | Partial | ABHA icon hidden (`showAbha` commented); wired but dormant. |
| 91 | Custom back: pops twice | `BenListCHOFragment.kt:58-66,72-75` | Missing | Navigation behaviour. |

### CHO register beneficiary (`BenRegisterCHOFragment`, cho/beneficiary/register/BenRegisterCHOFragment.kt) — design frame: M2.6-08 · CHO register beneficiary
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 92 | Always registers a woman with no household (placeholder household 0 auto-created); no household picker | `BenRegisterCHOViewModel.kt:85-91,108-119` | Partial | Design does not state female-only / no household. |
| 93 | Age ↔ DOB sync; age 12–99 | `BenRegCHODataset.kt:36-44,90-104` | Partial | Design shows DOB + read-only age. Typing age to derive DOB is not shown. |
| 94 | Save toast and error toast "Something went wrong" | `BenRegisterCHOFragment.kt` SAVE_SUCCESS/SAVE_FAILED | Partial | |

### HRP pregnant hub (`HRPPregnantFragment`) and HRP non-pregnant hub (`HRPNonPregnantFragment`), cho/beneficiary/*_women/ — design frame: M2.6-01 · High risk hub (merged)
No gaps. The design merges both hubs into one.

### HRP pregnant, assess list (`PregnantListFragment`, cho/beneficiary/pregnant_women/list/PregnantListFragment.kt) — design frame: M2.6-02 · HRP pregnant list ("To assess" tab)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 95 | 3 card buttons: Assess (red → orange if partial → green if all 8 factors answered), Micro birth plan, Share (purple, WhatsApp icon, only after a birth plan exists) | `PregnantListFragment.kt:94-99`; `HRPAdapter.kt:100-199` | Partial | Design card has a single "Follow up". AUDIT_CARDS has no buttons for `rv_item_ben_with_hrpa_form`. |
| 96 | LMP / EDD rows shown only when the assessment has an LMP | `HRPAdapter.kt:67-70` (`hasLmp`) | Partial | |
| 97 | Also the Supervisor "HRP women" list, all buttons hidden | `IconDataset.kt:258`; `HRPAdapter.kt:57-60` | Missing | See X3. |

### Assess high risk, pregnant (`HRPPregnantAssessFragment`, cho/beneficiary/pregnant_women/assess/HRPPregnantAssessFragment.kt) — design frame: M2.6-03 · Assess high risk (pregnant)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 98 | Read-only after first save (no re-assessment or edit) | `HRPPregnantAssessFragment.kt:48` | Missing | Design implies an editable draft. |
| 99 | EDD auto-calculated from LMP | `HRPPregnantAssessFragment.kt` getIndexOfEdd refresh | Partial | Shown read-only in design; the trigger isn't noted. |

### HRP pregnant, follow-up list (`HRPPregnantListFragment`, cho/beneficiary/pregnant_women/list_hrp/HRPPregnantListFragment.kt) — design frame: M2.6-02 ("Follow-up" tab)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 100 | "Follow up" hidden after 4 follow-ups or if one was already done today | `HRPTListAdapter.kt:84-100` | Missing | |
| 101 | "History" button (only after ≥ 1 follow-up) → history bottom sheet | `HRPPregnantListFragment.kt:70-84` | Missing | |

### HRP pregnant follow-up history sheet (`HRPPregnantTrackBottomSheet`, cho/beneficiary/pregnant_women/list_hrp/HRPPregnantTrackBottomSheet.kt) — design frame: NO SCREEN IN DESIGN (generic M2.0-P4)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 102 | Sheet listing past HRP follow-ups with a View button → follow-up form in read-only mode | `HRPPregnantTrackBottomSheet.kt` (HRPPregTrackAdapter → trackId); `rv_item_hrp_track_ben.xml:45-52` | Missing | |

### HRP pregnant follow-up form (`HRPPregnantTrackFragment`, cho/beneficiary/pregnant_women/track/HRPPregnantTrackFragment.kt) — design frame: M2.6-04 · HRP follow-up (pregnant)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 103 | Fixed 4-visit schedule with an auto label: 1st ANC, 1st PMSMA, 2nd PMSMA, 3rd PMSMA. A 5th attempt shows toast "Tracking is done" and closes | `HRPPregnantTrackViewModel.kt:100-107`; fragment `:88` | Missing | |
| 104 | Date of visit min = day after the last HRP / non-HRP visit (or LMP); max = today | `HRPPregnantTrackDataset.kt:316-333`; VM `:119-133` | Missing | |
| 105 | Past follow-up opened in view-only mode (from history) | `HRPPregnantTrackFragment.kt:45-50` | Partial | Generic P5 only. |
| 106 | Patient info header with husband name, ID, RCH ID, mobile | `HRPPregnantTrackFragment.kt` llPatientInformation2 block | Partial | Design subtitle shows name + weeks only. |

### Micro birth plan form (`HRPMicroBirthPlanFragment`, cho/beneficiary/pregnant_women/micro_birth_plan/HRPMicroBirthPlanFragment.kt) — design frame: M2.6-05 · Micro birth plan
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 107 | Saved plan opens read-only with an Edit FAB that unlocks it; Submit hidden until editing | `HRPMicroBirthPlanFragment.kt:45-50,76-78` | Missing | |

### Micro birth plan table / share (`HRPMicroBirthPlanTable`, cho/beneficiary/pregnant_women/micro_birth_plan_table/HRPMicroBirthPlanTable.kt) — design frame: NO SCREEN IN DESIGN (only a "Share" action on M2.6-05 banner)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 108 | Printable table view of the birth plan | whole fragment; opened from card "Share" (`PregnantListFragment.kt:87`) | Missing | |
| 109 | Share button: screenshots the table, saves it to the gallery (toast), opens WhatsApp / WhatsApp Business with a caption; toast if WhatsApp is not installed | `HRPMicroBirthPlanTable.kt:67-71,208-300` | Partial | Design has a "Share" label only. No preview, WhatsApp target or error state. |

### HR non-pregnant, assess list (`NonPregnantListFragment`, cho/beneficiary/non_pregnant_women/list/NonPregnantListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 110 | List of non-pregnant women to assess, search, "Assess" button red / orange / green by completeness | `NonPregnantListFragment.kt:50-67`; `HRNPAdapter.kt:81-130` | Missing | Only the hub tile "Assess high risk (12 to assess)" exists. |

### Assess high risk, non-pregnant (`HRPNonPregnantAssessFragment`, cho/beneficiary/non_pregnant_women/assess/HRPNonPregnantAssessFragment.kt) — design frame: NO SCREEN IN DESIGN (fields only in spec F-HRPNonPregnantAssessDataset)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 111 | Assessment with high-risk result, read-only after save | `HRPNonPregnantAssessFragment.kt:48`; VM `:112-120` (isHighRisk) | Missing | No result card or locked state. |

### HR non-pregnant, follow-up list (`HRPNonPregnantListFragment`, cho/beneficiary/non_pregnant_women/list_hrp/HRPNonPregnantListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 112 | List with "Follow up" (hidden if already done today, no visit cap) and "History" (after ≥ 1 follow-up) | `HRPNonPregnantListFragment.kt:83-86`; `HRNPTListAdapter.kt:83-105` | Missing | |

### HR non-pregnant follow-up history sheet (`HRNonPregnantTrackBottomSheet`, cho/beneficiary/non_pregnant_women/list_hrp/HRNonPregnantTrackBottomSheet.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 113 | Past follow-ups list with View → read-only form | `HRNonPregnantTrackBottomSheet.kt` | Missing | |

### HR non-pregnant follow-up form (`HRPNonPregnantTrackFragment`, cho/beneficiary/non_pregnant_women/track/HRPNonPregnantTrackFragment.kt) — design frame: M2.6-06 · HR non-pregnant follow-up
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 114 | "Is pregnant = Yes" converts her to pregnant. The beneficiary's reproductive status is updated, a pregnant HR assessment is auto-created with copied risk answers, and LMP / EDD (LMP + 280 days) are set | `HRPNonPregnantTrackViewModel.kt:118-150`; `HRPNonPregnantTrackDataset.kt:524-526` | Missing | Design shows "Missed period?" but not the transition or where she goes next. |
| 115 | LMP field (max today, min from last visit) inside the pregnancy-status section | `HRPNonPregnantTrackDataset.kt:91-99,236-262` | Partial | |

No gaps: NcdFragment (NCD hub), HRPPregnantFragment, HRPNonPregnantFragment (merged into M2.6-01). CbacObjectFragment is unused and not reachable.

---

## Death reports & Village level forms — missing functionality in design

Summary: 31 screens checked, 74 missing features, 30 partial.

Paths: `D/` = `app/src/main/java/org/piramalswasthya/sakhi/ui/home_activity/death_reports/`, `V/` = `.../ui/home_activity/village_level_forms/`, `A/` = `.../sakhi/adapters/`, `C/` = `.../sakhi/configuration/`, `R/` = `.../sakhi/repositories/`.
Design sources: `39-stage-2.9-death-vlf.js` (M2.9-01..12), `38-stage-2.8-disease.js` (M2.8-07), `30-stage-2.0-kit.js` (P7), `44-stage-2.14-navigation.js` (tile → frame map), `25-audit-data.js` / `45-stage-2.15-specs.js` (field specs; these have no flavour info).

General notes for this group:
- Design frames assume one "Mitanin-style" form for all flavours. The code adds or removes tiles and fields based on flavour in many places (see each screen).
- Every village meeting/campaign list has an "add new" button that is turned off by a frequency rule. Each list uses a different rule. The design shows none of these rules.
- The design shows "Save draft". No form in this group has drafts: one Submit saves the record, and after that it opens view-only. This is extra in the design, not a code gap, so it is not counted.

---

### Death reports hub (`DeathReportsFragment`, D/DeathReportsFragment.kt) — design frame: M2.9-01 · Death reports hub
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Each tile shows a live count of records for the **currently selected village** only (all death lists use the same village filter) | C/IconDataset.kt:632-652; BenDao.kt:828,842,997,1009 (`villageId = :selectedVillage`) | Partial | The design shows "1 pending / 3 this year", which suggests a pending/year count. The code shows a plain total for the selected village, with no pending or year breakdown. |
| 2 | Rules for who appears in each list: CDR = dead and age ≤ 14 yrs; MDSR = dead, isMdsr, reproductive status PW/PNC/abortion **and** a maternal-death flag in ANC/delivery outcome/PNC; General = dead, age ≥ 15, not a maternal death; Non-maternal = dead female 15–49, not a maternal death | BenDao.kt:819-844, 997, 1009; model/MDSR.kt:171-175; Konstants.maxAgeForCdr=14 | Partial | The design banner only says "a death recorded anywhere creates a pending report". It does not state the age, gender or source rules. |

### Child death list (`CdrListFragment`, D/cdr/CdrListFragment.kt) — design frame: NO SCREEN IN DESIGN (only the MDSR list M2.9-02 exists)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Search by name/ID, with a **voice search (mic)** button | CdrListFragment.kt:85-107 | Missing | There is no search bar or mic on any death list in the design. Voice search appears nowhere in the design. |
| 2 | Card button "CDR" is **red when the form is not filled and green when it is filled** | A/BenListAdapterForForm.kt:141-166 | Partial | M2.9-02 uses Pending/Submitted tags. There is no CDR-specific list. |
| 3 | Card buttons are hidden for the **ASHA Supervisor** role | A/BenListAdapterForForm.kt:59-67 | Missing | The design has no role gate. |
| 4 | Card sync-state icon | rv_item_ben_with_form.xml:86-91 | Missing | |
| 5 | Card fields: reg. date, ben ID, gender, mobile, father, age, **date of death**, RCH ID, family head; tapping the card body only shows a debug toast "Ben : id clicked" | rv_item_ben_with_form.xml:110-232; CdrListFragment.kt:58-60 | Partial | The design death card shows only name + "Died … · …". |
| 6 | Empty state "No records found" | CdrListFragment.kt:77-80 | Missing | |

### Maternal death list (`MdsrListFragment`, D/mdsr/MdsrListFragment.kt) — design frame: M2.9-02 · Maternal deaths list
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Search + voice-search mic | MdsrListFragment.kt (same pattern as CDR) | Missing | The design has tabs but no search. |
| 2 | Card form buttons hidden for ASHA Supervisor | A/BenListAdapterForForm.kt:59-67 | Missing | |
| 3 | Sync icon on card; red/green "MDSR" button showing filled state | A/BenListAdapterForForm.kt:141-166 | Partial | The design uses tags plus a "Report" action. The code has no Pending/Submitted/All tabs; it shows one list with colour state. |
| 4 | Empty state | MdsrListFragment.kt:52-54 | Missing | |

### General deaths list (`GdrListFragment`, D/gdr/GdrListFragment.kt) — design frame: NO SCREEN IN DESIGN (tile maps to M2.9-02)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Read-only list**: no form button and no sync icon (isGeneralForm=true); it only shows the deceased beneficiaries | GdrListFragment.kt:26; A/BenListAdapterForForm.kt:102-107 | Partial | The design reuses the MDSR list with a "Report" action and Pending tags. That is wrong for this list because there is no report to file. |
| 2 | Search + voice-search mic | D/BaseListFragment.kt:51-105 | Missing | |
| 3 | Empty state | D/BaseListFragment.kt:87 | Missing | |

### Non-maternal deaths list (`NmdsrListFragment`, D/nmdsr/NmdsrListFragment.kt) — design frame: NO SCREEN IN DESIGN (tile maps to M2.9-02)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Read-only list of dead women aged 15–49 who are not maternal deaths; no form button and no sync icon | NmdsrListFragment.kt:25; BenDao.kt:833-844 | Partial | Same issue as GDR: the design implies a report action. |
| 2 | Search + voice-search mic; empty state | D/BaseListFragment.kt:51-105 | Missing | |

### Child death report form (`CdrObjectFragment`, D/cdr/CdrObjectFragment.kt) — design frame: M2.9-03 · Child death report (CDR)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **One-time form**: once saved it reopens fully read-only, with uploads disabled and Submit disabled. Saved reports cannot be edited. | CdrObjectFragment.kt:150-154 | Missing | The design shows only the edit state and a "Save draft". There is no view-only state. |
| 2 | Three document slots: CDR form page 1, CDR form page 2, **death certificate**. Each opens a chooser: **Camera / Gallery / PDF / Cancel** | CdrObjectFragment.kt:308-330; C/CDRFormDataset.kt:233 | Partial | The design shows 2 photo tiles ("CDR form from ANM"). There is no death-certificate slot, no PDF option and no chooser dialog. |
| 3 | File larger than 5 MB is rejected with a toast | CdrObjectFragment.kt:237-243 | Missing | |
| 4 | Tapping an uploaded file previews it: image in a dialog, PDF in an external viewer | CdrObjectFragment.kt:129-148, 379-408 | Missing | |
| 5 | **Mitanin flavour: all 3 upload slots removed** (taps on upload are also ignored) | C/CDRFormDataset.kt:236-240; CdrObjectFragment.kt:122 | Missing | |
| 6 | Auto-filled from the beneficiary and the ASHA: name, DOB, age, gender, parents, address (trimmed to 50 chars), house no, mohalla, state/district/block/village, mobile, date and place of death, first informant = ASHA username | C/CDRFormDataset.kt:255-273 | Partial | The design auto-card shows only DOB, age and parents. First informant is shown as the father, but the code puts in the ASHA's name. |
| 7 | Date of notification can only be between the date of death and today | C/CDRFormDataset.kt:243-253 | Missing | |
| 8 | After saving, the app goes back and starts a sync. On failure it shows the toast "Saving … failed". There is a loading spinner while saving. | CdrObjectFragment.kt:167-199 | Missing | |

### Maternal death report form (`MdsrObjectFragment`, D/mdsr/MdsrObjectFragment.kt) — design frame: M2.9-04 · Maternal death report (MDSR)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **"When did death occur" is filled automatically and locked**. It has 4 separate Yes/No questions (pregnancy / delivery / ≤42 days after delivery / abortion), taken from ANC maternal-death, delivery-outcome "DEATH", PNC mother-death and abortion records | D/mdsr/MdsrObjectViewModel.kt:~78-100; C/MDSRFormDataset.kt:96-127, 329-332 | Partial | The design shows one editable radio group that the ASHA picks herself. |
| 2 | **Cause of death is calculated automatically**: "Suspected maternal death" vs "Non-maternal death". It becomes non-maternal if the ANC/PNC cause was "Accident". Picking "Suspected maternal" shows "specify reasons". | C/MDSRFormDataset.kt:334-342, 362-369; arrays.xml:256 | Partial | The design shows a "Cause of death" dropdown with medical causes (e.g. Eclampsia). The 2-option list and the auto-calculation are not designed. |
| 3 | One-time form: read-only after save, with uploads and Submit disabled | MdsrObjectFragment.kt:96-118 | Missing | |
| 4 | Uploads: MDSR form 1, MDSR form 2, death certificate. Chooser: Camera / Gallery / PDF / Cancel, max 5 MB, preview on tap | MdsrObjectFragment.kt:181-200 | Partial | The design has file rows but no chooser and no size error. |
| 5 | Mitanin flavour: all upload slots removed | C/MDSRFormDataset.kt:306-310; MdsrObjectFragment.kt:103 | Missing | |
| 6 | Auto-filled reporter = ASHA username, designation fixed to "Asha"; husband, RCH ID, age, father, date/time/place of death come from the beneficiary | C/MDSRFormDataset.kt:312-328 | Partial | The design shows reporter/designation as editable fields. |
| 7 | Save → go back and start sync; failure toast; loading state | MdsrObjectFragment.kt:128-152 | Missing | |

### Village level forms hub (`VillageLevelFormsFragment`, V/VillageLevelFormsFragment.kt) — design frame: M2.9-05 · Village level forms hub
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Overdue red border on a tile** after the 7th of the month if that form has not been submitted this month. Applies to VHND, VHNC, PHC, AHD, NDD, U-WIN, Pulse Polio, ORS. | V/VillageLevelFormsViewModel.kt:31-70; VillageLevelFormsFragment.kt:72-88 | Missing | The design shows only "Due 5 Nov" style subtitles, with no overdue state. Note: the code matches tiles by English title, so the flag probably never shows in Hindi or Assamese. |
| 2 | **Mitanin flavour hides Saas Bahu Sammelan and U-WIN tiles** | C/IconDataset.kt:358-368, 411-421 | Missing | The design shows both tiles for every flavour. |
| 3 | **Mitanin renames "PHC review meeting" to "Cluster review meeting"** (same screen) | C/IconDataset.kt:322-331 | Partial | The design shows "PHC review meeting" and "Cluster meetings" as two separate tiles. The code has one tile with a flavour-based name. |
| 4 | Hub has no Survey register tile (that screen is an unfinished placeholder) | C/IconDataset.kt:320-428 | Partial | The design shows a Survey register tile plus frame M2.9-12. That is extra in the design, noted for awareness. |
| 5 | Filaria MDA campaign tile opens the campaign history (village-level, yearly) | C/IconDataset.kt:401-407 | Partial | The design maps "Filaria MDA" to M2.8-07, which is a per-person Filaria case form, not the campaign. |

### VHND list (`VHNDListFragement`, V/vhnd/VHNDListFragement.kt) — design frame: M2.9-11 · Meetings list + badge progress
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | "Add VHND" button **turned off once a VHND exists for the current month** | VHNDListFragement.kt:58-68; R/VLFRepo.kt:1582-1612 | Missing | The design list has no add button and no disabled state. |
| 2 | Card shows date, no. attended, place, and a **"View"** button that opens the saved record | layout_vhnd_item.xml:33-114 | Partial | The design card has no View action and shows a sync tag the code does not have. |
| 3 | Empty state "No records found" | VHNDListFragement.kt:73-80 | Missing | |

### VHND form (`VHNDFormFragement`, V/vhnd/VHNDFormFragement.kt) — design frame: M2.9-06 · VHND form
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Mitanin-only fields**: PW/lactating/children counts and the 6 nutrition-education checklists. Other flavours show only date, place, total attended and 2 photos. | C/VHNDDataset.kt:194-222 | Partial | The design shows the Mitanin version for all flavours. |
| 2 | View mode: an existing record opens read-only with Submit disabled | VHNDFormFragement.kt:65-97 | Missing | |
| 3 | Date can only be in the last 60 days up to today, and defaults to today | C/VHNDDataset.kt:39-40, 225 | Missing | |
| 4 | Photo chooser dialog (Take photo / Choose from gallery); image larger than 5 MB rejected with a toast; tap a photo to preview it; "No image found" toast | VHNDFormFragement.kt:82-93, 165-201, 276-280 | Partial | The design has photo tiles only. |
| 5 | Toast on save success or failure, then go back | VHNDFormFragement.kt:119-141 | Missing | |

### VHSNC list (`VHNCListFragement`, V/vhnc/VHNCListFragement.kt) — design frame: NO SCREEN IN DESIGN (tile maps to M2.9-10)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | List of past VHSNC meetings (date, attended, place, View) | layout_vhnc_item.xml | Missing | |
| 2 | Add button turned off when this month is already filled | VHNCListFragement.kt:72-74 | Missing | |
| 3 | Empty state | VHNCListFragement.kt:64-66 | Missing | |

### VHSNC form (`VHNCFormFragement`, V/vhnc/VHNCFormFragement.kt) — design frame: M2.9-10 (generic) + 2.15 spec
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Date picker cannot go below the month after the latest month already filled**. It is also limited to the last 2 months up to today. | C/VHNCDataset.kt:55-56, 85-99 | Missing | |
| 2 | View-only mode for a saved record; Submit disabled | VHNCFormFragement.kt:93-95 | Missing | |
| 3 | 2 photos with camera/gallery chooser, 5 MB limit, preview | VHNCFormFragement.kt:173-207, 284-285 | Partial | |
| 4 | Counts must be at least 1 (min=1) | C/VHNCDataset.kt:130-196 | Missing | The generic P7 steppers allow 0. |

### PHC review list (`PHCReviewListFragement`, V/phc_review_meeting/PHCReviewListFragement.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | List (date, attended, place, View) + Add button turned off once filled this month | PHCReviewListFragement.kt:47-78 | Missing | |
| 2 | Mitanin: title and button read "Cluster review meeting" | PHCReviewListFragement.kt:57-61, 88-91 | Missing | |

### PHC review form (`PHCReviewFormFragement`, V/phc_review_meeting/PHCReviewFormFragement.kt) — design frame: M2.9-10 (generic) + 2.15 spec
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Mitanin-only checklists** "Participants attended" and "Mitanin activity". Other flavours: date, place, village, total, 2 photos. | C/PHCReviewDataset.kt:128-137, 153 | Partial | The spec lists the fields with no flavour gate. |
| 2 | Date limited to the last 60 days up to today | C/PHCReviewDataset.kt:43-44 | Missing | |
| 3 | View-only for a saved record; photo chooser, 5 MB limit, preview | PHCReviewFormFragement.kt:85-93, 162-200 | Partial | |

### AHD list (`AHDListFragment`, V/ahd/AHDListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | List (AHD date, "Mobilized for AHD", place, View) + Add button turned off once filled this month | AHDListFragment.kt:43-66; layout_ahd_item.xml | Missing | |

### AHD form (`AHDFormFragment`, V/ahd/AHDFormFragment.kt) — design frame: M2.9-10 (generic) + 2.15 spec
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **AHD date can only be today** (min = max = now) | C/AHDDataset.kt:46-47 | Missing | |
| 2 | "Mobilized for AHD = Yes" shows Place + Date | C/AHDDataset.kt:105-127 | Partial | The generic M2.9-10 frame shows date, place and count always visible. The 2.15 spec marks them conditional. |
| 3 | View-only for a saved record; photo chooser, 5 MB limit | AHDFormFragment.kt:85-93, 177-221 | Partial | |

### Deworming list (`DewormingListFragment`, V/national_deworming_day/DewormingListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Add button **turned off if any NDD was recorded in the last 6 months** | DewormingListFragment.kt:59-61; VLFDao.kt:170-179 | Missing | |
| 2 | Card: date, "round conducted?", place, out-of-school children count, View | layout_deworming_item.xml | Missing | |

### Deworming form (`DewormingFormFragment`, V/national_deworming_day/DewormingFormFragment.kt) — design frame: M2.9-09 · Deworming day (NDD)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | NDD date limited to the last 1 month up to today | C/DewormingDataset.kt:25-33, 51-52 | Missing | |
| 2 | View-only after save; 2 photos max (the design shows 3 tiles); camera/gallery chooser; 5 MB limit | DewormingFormFragment.kt:80-88, 141-184 | Partial | |

### Maa meeting list (`MaaMeetingListFragment`, V/maa_meeting/MaaMeetingListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | "New Maa meeting" turned off until **3 months have passed since the last meeting** | MaaMeetingListFragment.kt:41-46; R/MaaMeetingRepo.kt:201-215 | Missing | The hub subtitle "Due this month" contradicts the quarterly rule. |
| 2 | Card with sync-state icon, date, place, participants, View | layout_maameeting_item.xml:58-175 | Missing | |

### Maa meeting form (`MaaMeetingFormFragment`, V/maa_meeting/MaaMeetingFormFragment.kt) — design frame: M2.9-07 · Maa meeting
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Mitanin-only fields**: village, PW count, lactating count, breastfeeding checklist. Other flavours: date, place, participants. | C/MaaMeetingDataset.kt:112-120 | Partial | The design shows the Mitanin version for all flavours. |
| 2 | Up to **5 uploads (photos or minutes of meeting)**, accepting JPEG/PNG/PDF only; "Only JPEG, PNG, PDF allowed" toast; 5 MB limit; opens the file in an external viewer | MaaMeetingFormFragment.kt:160-195 | Missing | M2.9-07 has no upload section. |
| 3 | In view mode only the filled upload slots are shown | C/MaaMeetingDataset.kt:124-130 | Missing | |
| 4 | Missing required fields show an **"Alert — fill required fields" dialog** | MaaMeetingFormFragment.kt:145-155 | Missing | |
| 5 | Submit hidden for an existing record | MaaMeetingFormFragment.kt:58-62 | Missing | |

### Saas Bahu Sammelan list (`SaasBahuSammelanListFragment`, V/saas_bahu_sammelan/SaasBahuSammelanListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Add button turned off if an entry already exists **this calendar month** | SaasBahuSammelanListFragment.kt:58-73 | Missing | The hub subtitle says "Quarterly", which contradicts the code. |
| 2 | Card: sync icon, date, place, participants, **ASHA ID**, View | layout_saas_bahu_list.xml:57-164 | Missing | |
| 3 | Whole module hidden in Mitanin | C/IconDataset.kt:358 | Missing | |

### Saas Bahu Sammelan form (`SaasBahuSamelanFragment`, V/saas_bahu_sammelan/SaasBahuSamelanFragment.kt) — design frame: M2.9-10 (generic)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Up to 5 photos/MoM (JPEG/PNG/PDF), 5 MB limit; view mode shows only the filled slots | SaasBahuSamelanFragment.kt:48-76; C/SaasBahuSamelanDataset.kt:53-95 | Partial | M2.9-10 says "up to 5 photos / minutes" but has no PDF or type check. |
| 2 | Date limited to the last 2 months up to today | C/SaasBahuSamelanDataset.kt:27-28 | Missing | |
| 3 | Required-fields alert dialog; Submit hidden for a saved record; sync starts after save | SaasBahuSamelanFragment.kt:104, 151-157, 195-211 | Missing | |

### U-WIN session list (`UwinListFragment`, V/u_win_forms/UwinListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | "Add new" is always on (multiple sessions per month allowed) | UwinListFragment.kt:45-49 | Missing | |
| 2 | Card: sync-state icon, session date, place, participants, View | item_uwin_list.xml:57-131 | Missing | |
| 3 | Module hidden in Mitanin | C/IconDataset.kt:411 | Missing | |

### U-WIN session form (`UwinFragment`, V/u_win_forms/UwinFragment.kt) — design frame: M2.9-10 · Simple meeting (U-WIN / Saas Bahu)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Upload reminder on Submit**: if no file is uploaded, a dialog asks "Do you want to upload the U-WIN immunization page photo to claim your incentive?" | UwinFragment.kt:139-143 | Missing | |
| 2 | **Duplicate-date block**: toast "A U-WIN session has already been submitted for this date" | UwinFragment.kt:128-131; UwinViewModel.kt (DUPLICATE_DATE) | Missing | |
| 3 | 2 upload slots (U-WIN summary page 1 and 2), camera or gallery (images only), 5 MB limit | UwinFragment.kt:58-64, 145-171 | Partial | The design says "up to 5". |
| 4 | Saved record: date, place and count locked, Submit hidden | C/UWINDataset.kt:75-98; UwinFragment.kt:85-87 | Missing | |
| 5 | Session date limited to the last 2 months up to today | C/UWINDataset.kt:17-29 | Missing | |

### Pulse Polio campaign list (`PulsePolioCampaignListFragment`, V/pulse_polio/PulsePolioCampaignListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Add button turned off when **2 campaigns are already recorded this year** or **less than 6 months have passed since the last one** | V/pulse_polio/PulsePolioCampaignListViewModel.kt:59-90 | Missing | |
| 2 | Downloads past campaigns from the server when the list opens | PulsePolioCampaignListViewModel.kt:47-55 | Missing | |
| 3 | Card: campaign date + "Number of children: n" + View | A/PulsePolioCampaignAdapter.kt:49-50 | Missing | |

### Pulse Polio campaign form (`PulsePolioCampaignFormFragment`, V/pulse_polio/PulsePolioCampaignFormFragment.kt) — design frame: M2.9-08 · Pulse Polio campaign (fields marked "illustrative")
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | **Server-driven form**: field list, show/hide rules and validation come from a downloaded schema (cached schema first) | PulsePolioCampaignViewModel.kt:~60-130 | Partial | The design admits its fields are illustrative. The real schema is not designed. |
| 2 | Campaign photos: **keeps only the last 2** (a 3rd photo replaces the oldest); camera or file; size limit comes from the schema (default 5 MB) and shows an inline error | PulsePolioCampaignFormFragment.kt:64-142 | Partial | |
| 3 | View-only once saved (Save button hidden) | PulsePolioCampaignFormFragment.kt:175 | Missing | |
| 4 | Missing fields: toast "Please fill all required fields" and scroll to the first error | PulsePolioCampaignFormFragment.kt:269-290 | Missing | |
| 5 | After save, a sync worker starts | PulsePolioCampaignViewModel.kt (`PulsePolioCampaignPushWorker.enqueue`) | Missing | |

### ORS distribution list (`ORSCampaignListFragment`, V/ors_campaign/ORSCampaignListFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Add button turned off until **1 month has passed since the last campaign** | V/ors_campaign/ORSCampaignListViewModel.kt:65-87 | Missing | The hub says "Jun–Jul", but the code has no season rule. |
| 2 | Downloads from the server on open; card shows date + "Number of families: n" + View | ORSCampaignListViewModel.kt:54-62; A/ORSCampaignAdapter.kt:45-46 | Missing | |

### ORS distribution form (`ORSCampaignFormFragment`, V/ors_campaign/ORSCampaignFormFragment.kt) — design frame: NO SCREEN IN DESIGN (tile maps to the generic M2.9-10)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Server-driven form (identical engine to Pulse Polio): schema fields and rules, 2 rolling photos, view-only after save, sync after save | ORSCampaignFormFragment.kt (same as Pulse Polio) | Missing | There is no AUDIT_FORMS spec for ORS. |

### Filaria MDA campaign history (`FilariaMdaCampaignHistory`, V/filaria_mda/list/FilariaMdaCampaignHistory.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | "History of MDA campaign" list. Card: start date, **no. of families, no. of individuals**, View | FilariaMdaCampaignHistory.kt:53-90; layout_mda_campaign_item.xml | Missing | |
| 2 | "Add MDA" button turned off when **this year's campaign is already recorded** (once per year) | FilariaMdaCampaignHistory.kt:54-57; FilariaMdaFormCampaignViewModel.kt:74-80 | Missing | |

### Filaria MDA campaign form (`FilariaMdaCampaignFormFragment`, V/filaria_mda/FilariaMdaCampaignFormFragment.kt) — design frame: NO SCREEN IN DESIGN (tile wrongly maps to M2.8-07 Filaria case form)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| 1 | Server-driven form (LF_MDA_CAMPAIGN schema); view-only when opened from history (Save hidden) | FilariaMdaCampaignFormFragment.kt:180-188 | Missing | |
| 2 | Visit date must be on or before today and **after the previous campaign's date** (inline errors) | FilariaMdaCampaignFormFragment.kt:270-300 | Missing | |
| 3 | Duplicate-year block: toast "You have already submitted this form for this Year" | FilariaMdaFormCampaignViewModel.kt:~229-232 | Missing | |
| 4 | Max 2 campaign photos (camera or file); "Unable to open camera" toast | FilariaMdaCampaignFormFragment.kt:95-157 | Missing | |
| 5 | Starts an MDA sync as soon as the screen opens | FilariaMdaCampaignFormFragment.kt:178 | Missing | Background behaviour; FYI only. |

### Survey register (`SurveyRegisterFragment`, V/survey_register/SurveyRegisterFragment.kt) — design frame: M2.9-12 · Survey register
(No functionality in code: a placeholder layout showing "hello" that cannot be reached from the hub. The design is ahead of the code, so there are no gaps to list.)

---
No gaps: SurveyRegisterFragment (code is only a placeholder).

---

## ABHA + Supervisor — missing functionality in design
Summary: 19 screens checked, 44 missing features, 39 partial.

Design sources checked: `design/figma-plugin/src/40-stage-2.10-abha.js` (M2.10-01…07), `42-stage-2.12-supervisor.js` (M2.12-01…06), `10-core-phase1.js` (profile list), `41-stage-2.11-profile.js` (language sheet, delete account), `44-stage-2.14-navigation.js`, `25-audit-data.js`.
Paths below are relative to `app/src/main/java/org/piramalswasthya/sakhi/ui/`.

---

### Part A — ABHA (`abha_id_activity/**`)

### ABHA shell (`AbhaIdActivity`, abha_id_activity/AbhaIdActivity.kt) — design frame: no shell drawn; the parts appear in M2.10-01…07
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A1 | **30-minute session countdown** in the toolbar (mm:ss). The whole ABHA flow closes itself when it reaches 0 | Always, starts when the flow opens (AbhaIdActivity.kt:96-105) | Missing | Not on any frame and not in the notes. The ASHA loses her progress when time runs out, so the design needs the timer and an "expired" state |
| A2 | **Home / close icon** in the toolbar closes the whole ABHA flow from any step | Always (AbhaIdActivity.kt:93) | Partial | The frames use `screenTop(...,'none')`, so there is no close action. Only M2.10-04/05 show "Exit" |
| A3 | **Confirm before leaving** ("Exit – do you want to go back?" Yes/No). On the mobile step, Yes goes back to the Aadhaar screen. On the result screen, Yes closes the flow | Up/back on the mobile-OTP step or the result screen (AbhaIdActivity.kt:118-169) | Missing | No exit-confirmation dialog in 2.10 |
| A4 | **Startup state: getting a token.** A full-screen spinner shows while the app gets the server and ABHA tokens. If that fails, a full-screen error with the server message and "Try again" | On every launch (AbhaIdViewModel.kt:55-91; AbhaIdActivity.kt:52-91) | Partial | M2.10-07 shows offline/"Try again" but no loading state. There is also no separate "server error" variant that shows the server's own message |
| A5 | **Screen goes blank when the app is in the background** (Aadhaar privacy). Tapjacking protection is on | onPause/onResume (AbhaIdActivity.kt:108-116, 44-49, 214-220) | Missing | Worth one line in the notes: screenshots/recents show a blank screen |
| A6 | **Can be opened without a beneficiary** (from the drawer "Create ABHA number"). There is then no name to prefill, and the ABHA is saved as a stand-alone record | No benId/benRegId extras (CreateAbhaViewModel.kt:116-130) | Partial | Every 2.10 frame assumes a person ("Sunita Devi"). There is no variant where the name is typed and the result is not linked |

### Find / Create host (`AadhaarIdFragment`, abha_id_activity/aadhaar_id/AadhaarIdFragment.kt) — design frame: M2.10-01 · ABHA · find or create
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A7 | **"Search ABHA / Create ABHA" toggle on one screen.** It opens on **Create** by default and remembers the last tab after going back | Always (AadhaarIdFragment.kt:63-92, 207-241; AadhaarIdViewModel.kt:107) | Partial | The design has a separate chooser screen with two cards. Code shows a segmented toggle and opens straight into Create |
| A8 | **"Aadhaar" verification-method dropdown** (only "Aadhaar No" today; Fingerprint is commented out). Shown only on the Create tab | Create tab (AadhaarIdFragment.kt:67-68, 108-121; AadhaarIdViewModel.kt:32-33) | Missing | Not drawn. Fingerprint (FP) would hide the ASHA/GOV radio and send the user to the mobile-OTP path |
| A9 | **Back on this screen closes the whole ABHA flow** (no confirmation) | System back (AadhaarIdFragment.kt:38-44) | Missing | — |
| A10 | Inline **loading spinner / network-error panel** for the Aadhaar OTP request | state LOADING / ERROR_NETWORK (AadhaarIdFragment.kt:130-165) | Partial | Covered generally by M2.10-07. No loading state is drawn |

### Aadhaar number – ASHA mode (`AadhaarNumberAshaFragment`, abha_id_activity/aadhaar_id/aadhaar_num_asha/AadhaarNumberAshaFragment.kt) — design frame: M2.10-02 · Aadhaar number
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A11 | **Show/hide (eye) toggle** on the Aadhaar number. The field is masked as a password by default. **Copy/paste is blocked** | Always (AadhaarNumberAshaFragment.kt:66, 249-262) | Partial | Design shows a static mask and a lock icon. No eye toggle, and no note that paste is blocked |
| A12 | **Live validity ticks**: a grey or green check icon next to Aadhaar, mobile and name. Inline errors "Invalid Aadhaar no." / "Invalid mobile no." / "Invalid name". Aadhaar uses the Verhoeff check | On typing (AadhaarNumberAshaFragment.kt:136-238) | Partial | The design has no valid or error states for these fields |
| A13 | **Beneficiary name is read-only, prefilled from the record** ("Enter … <name>"). The name field shows only when the flow was opened without a beneficiary | benId>0 → name label; else editable name (AadhaarNumberAshaFragment.kt:76-99) | Partial | Design always shows an editable "Beneficiary name *" |
| A14 | **Consent is a checkbox on this screen** ("Consent for the creation of your ABHA number"). Tapping it opens the consent screen. It cannot be ticked directly. It shows the toast "Please Enter Beneficiary Name" if the name is empty. It comes back ticked after "I Agree" | (AadhaarNumberAshaFragment.kt:126-133, 264-271; layout fragment_aadhaar_number_asha.xml:218-245) | Partial | Design uses a "Next: consent" button and separate consent and OTP steps. In code, **Generate OTP stays on this screen** and is enabled only when Aadhaar valid + mobile valid + consent ticked (:162-164, 202-203) |
| A15 | Server error text shown under the Aadhaar field (e.g. UIDAI message) | errorMessage (AadhaarNumberAshaFragment.kt:241-247) | Missing | No server-error state for the Aadhaar entry |

### Aadhaar number – GOV mode (`AadhaarNumberGovFragment`, abha_id_activity/aadhaar_id/aadhaar_num_gov/AadhaarNumberGovFragment.kt) — design frame: M2.10-02 (ASHA/GOV segment only)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A16 | **GOV (demographic) form**: Aadhaar no., full name, date of birth (date picker), gender radio, **State → District cascading dropdowns** loaded from the server, "Generate ABHA" enabled only when all are filled | GOV radio (AadhaarNumberGovFragment.kt:42-158; AadhaarNumberGovViewModel.kt:53-80) | Missing | Design draws only the "ASHA / GOV" segment and never shows the GOV fields. **Mismatch:** in code the ASHA/GOV radio group is `visibility="gone"` (fragment_aadhaar_id.xml:113-118), so GOV cannot be reached today. Decide whether to drop it from the design or build the screen |
| A17 | GOV success goes **straight to the ABHA result** (no OTP) | ABHA_GENERATED_SUCCESS (AadhaarIdFragment.kt:172-178) | Missing | — |

### Consent (`AbhaConsentFragment`, abha_id_activity/aadhaar_id/aadhaar_consent/AbhaConsentFragment.kt) — design frame: M2.10-03 · Consent
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A18 | **7 consent statements, each with its own checkbox.** Item 1 acts as **"select all"**. "I Agree" is enabled only when all 7 are ticked | (AbhaConsentFragment.kt:57-94, 97-117) | Partial | Design shows 4 bullets and a single checkbox. The legal text (UIDAI/ABDM wording, strings_abha.xml:99-112) is longer and itemised |
| A19 | **The ASHA's name and the beneficiary's name are inserted** into statements 6 and 7, in bold italic and the primary colour ("I, @ashaName, confirm…", "I, @beneficiaryName, have been explained…") | (AbhaConsentFragment.kt:98-106, 130-154) | Missing | — |
| A20 | "I Agree" **returns to the Aadhaar screen with consent ticked**. It does not send the OTP | (AbhaConsentFragment.kt:51-54) | Partial | Design primary button is "Send Aadhaar OTP" |

### Find existing ABHA (`FindAbhaFragment`, abha_id_activity/aadhaar_id/find_abha/FindAbhaFragment.kt) — design frame: NO SCREEN IN DESIGN (only the entry card on M2.10-01)
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A21 | **Search ABHA by mobile number**: header "Already have an ABHA? Find Out Now!", mobile field with a validity tick, and a "Search" button enabled only for a valid mobile | Search tab (FindAbhaFragment.kt:78-88, 156-184) | Missing | The whole screen is undrawn |
| A22 | **Pick one ABHA from the results**: a dropdown lists every ABHA linked to that mobile as "Name : ABHA number". It is disabled until results arrive | After search (FindAbhaFragment.kt:124-139, 186-192) | Missing | — |
| A23 | **Search errors**: "No ABHA Found", "Internet connectivity issue, try again later", "Some error occurred" | (FindAbhaViewModel.kt:88-108) | Missing | — |
| A24 | Consent checkbox + "Consent for creation" link to the consent screen. **Generate OTP** is enabled only when an ABHA is picked + mobile is valid + consent is ticked. The OTP goes to the ABHA-registered mobile | (FindAbhaFragment.kt:141-154, 268-274; FindAbhaViewModel.kt:113-147) | Missing | — |

### Aadhaar / ABHA OTP (`AadhaarOtpFragment`, abha_id_activity/aadhaar_otp/AadhaarOtpFragment.kt) — design frame: M2.10-04 · Aadhaar OTP
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A25 | **Two variants of this screen**: "Verify Aadhaar OTP" (create) and **"Verify ABHA OTP"** (find flow). The message reads "OTP sent to Aadhaar / ABHA registered mobile ending ******1234" | abhaMode SEARCH vs CREATE (AadhaarOtpFragment.kt:93-95, 138-148) | Partial | Only the Aadhaar variant is drawn |
| A26 | **Exit button appears only after certain failures** (network error, or a server message containing "exit your browser", i.e. ABDM lock-out) | showExit (AadhaarOtpFragment.kt:112-124; AadhaarOtpViewModel.kt:154-163, 206-214) | Partial | Design always shows "Exit" in the bottom bar |
| A27 | **Wrong-OTP behaviour**: the OTP box clears and the server error shows under it. Toasts "OTP resent" / "OTP was resent". The resend timer is **60 s** | (AadhaarOtpFragment.kt:41-53, 126-134, 203-223) | Partial | Design shows a 0:24/30 s timer and the "3 wrong OTPs → wait 30 min" banner, but no inline wrong-OTP error. The timer value differs (code 60 s, notes say 30 s) |
| A28 | **Branch after Aadhaar OTP**: if the entered mobile = the Aadhaar mobile → straight to the result. Otherwise an OTP is **auto-sent to the entered mobile** → mobile-verify step. In the find flow → straight to the result | OTP_VERIFY_SUCCESS (AadhaarOtpFragment.kt:159-188) | Partial | The design note mentions "Mobile (only if different)", but M2.10-05 asks the user to type the mobile again. Code never asks again; it reuses the one entered on screen 1 |

### Generate mobile OTP (`GenerateMobileOtpFragment`, abha_id_activity/generate_mobile_otp/GenerateMobileOtpFragment.kt) — design frame: M2.10-05 · Mobile number OTP
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A29 | **"Already linked" shortcut**: if the server says the mobile is already linked, the OTP step is skipped and the user goes to the result | apiResponse.mobileLinked (GenerateMobileOtpFragment.kt:113-125) | Missing | Only reachable on the Fingerprint path (FP is disabled today), so low priority |
| A30 | Info (i) icon on the mobile field shows a tooltip on tap. The back button asks to confirm and returns to the Aadhaar screen | (GenerateMobileOtpFragment.kt:49-60, 87-89) | Missing | — |

### Verify mobile OTP (`VerifyMobileOtpFragment`, abha_id_activity/verify_mobile_otp/VerifyMobileOtpFragment.kt) — design frame: M2.10-05 · Mobile number OTP
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A31 | **OTP-only screen**: the mobile number is not editable here. The message is "OTP sent to mobile ending ******1234", with a 60 s resend timer and Resend | (VerifyMobileOtpFragment.kt:40-52, 69-72, 164-165) | Partial | The design frame shows an editable mobile field + "Check & generate OTP" |
| A32 | Exit button only after lock-out/network failure. Inline wrong-OTP error. "OTP was resent" toast | (VerifyMobileOtpFragment.kt:88-100, 125-142) | Partial | Same as A26/A27 |

### ABHA result / download (`CreateAbhaFragment`, abha_id_activity/create_abha_id/CreateAbhaFragment.kt) — design frame: M2.10-06 · ABHA created
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| A33 | **"This ABHA already exists!" state** (warning icon). The text says the ABHA was **not linked** ("already linked to someone, try again using correct Aadhaar No") | Server isNew = false (CreateAbhaFragment.kt:164-170) | Missing | Design shows only the happy "created" state |
| A34 | **Blocking full-screen dialog "This ABHA No is already linked to someone… of <First Last>"**. OK closes the whole flow | Mapping API returns code 0 (CreateAbhaViewModel.kt:259-261; CreateAbhaFragment.kt:242-246, 386-407) | Missing | Important duplicate-ABHA safeguard; no design |
| A35 | **Find-flow result "Here is your ABHA Number!"** with name + ABHA number, **no download option**, and no linking | abhaMode SEARCH (CreateAbhaFragment.kt:146-150) | Missing | — |
| A36 | **Download is a Yes/No question** ("Do you want to download the ABHA card?"). **No** closes the flow. **Yes** saves the card as a PNG to the phone's **Downloads** folder, posts a **system notification** that opens the image, and shows a persistent snackbar "You can view ABHA card in download folder!!" whose OK closes the flow | (CreateAbhaFragment.kt:208-212, 231-233, 256-264, 279-326) | Partial | Design has one "Download ABHA card" button + "Done". It does not show the Yes/No choice, the notification or the downloaded confirmation |
| A37 | **Linked-to text names the beneficiary** ("This ABHA is successfully linked to the record of <name>") and the record syncs to the server at once (push worker) | benId present (CreateAbhaFragment.kt:151-156, 177-186) | Partial | Design banner exists. The not-linked case (opened from the drawer, A6) and the immediate sync are not shown |
| A38 | Back press → **"Exit – do you want to go back?"** dialog (cannot be dismissed by tapping outside) | (CreateAbhaFragment.kt:92-113) | Missing | — |

---

### Part B — Supervisor (`asha_supervisor/**`)

### Supervisor shell (`SupervisorActivity`, asha_supervisor/SupervisorActivity.kt) — design frame: M2.12-06 · Supervisor profile (menu items) + M2.12 top bar
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| B1 | **Mitanin role naming**: the "Supervisor" home menu item is renamed **"Mitanin Trainer"** when role = "ASHA Supervisor", or to the user's role name (e.g. ANM). Other flavours say "Supervisor" | FLAVOR mitanin (SupervisorActivity.kt:494-506) | Missing | Design note says "can reuse with role labels; not drawn separately" |
| B2 | **Language list depends on the flavour**: Mitanin = English + Hindi only. Other flavours add **Bangla** and Assamese. The app restarts on change | (SupervisorActivity.kt:92-123) | Partial | M2.11-08 shows English/Hindi/Assamese only. No Bangla, no flavour rule |
| B3 | **Change profile photo**: tap the avatar in the menu header to open the photo picker | (SupervisorActivity.kt:442-459) | Missing | Supervisor profile (M2.12-06) has a static avatar |
| B4 | **App version label** "APK Version x.y.z" | Always (SupervisorActivity.kt:245, 362) | Missing | — |
| B5 | **"Help & support" opens the LMS / video tutorials screen** | (SupervisorActivity.kt:514-520) | Partial | In the design, the prototype links Help & support to M2.11-09 help/about |
| B6 | **"Delete my account" opens an external Microsoft Form** (a different URL for prod vs non-prod flavours) | (SupervisorActivity.kt:529-547) | Partial | Design M2.11-09 shows an in-app "Delete" confirmation banner; code leaves the app |
| B7 | **Logout confirmation** ("Are you sure to logout?" Yes/No) that also clears the push token, beneficiary images and background jobs | (SupervisorActivity.kt:126-140, 509-512) | Missing | Design has a "Log out" item only, no confirmation |
| B8 | **"Unsupported device" blocking dialog** on rooted devices/emulators (release builds) | !DEBUG && rooted (SupervisorActivity.kt:217-224, 353-360) | Missing | App-wide, but the supervisor shell has its own copy |
| B9 | **Silent auto-sync**: pending records are pushed automatically (at most once per 2 min, when online). The first full data pull runs on first launch | (SupervisorActivity.kt:233-244, 427-431) | Missing | No sync status or progress indicator on supervisor screens (see B15) |
| B10 | Toolbar **Home** icon appears on inner screens to jump back to the supervisor home. On home it is replaced by the **Language** icon. The **notification bell with unread badge** opens the notification panel | (SupervisorActivity.kt:365-417, 426-429) | Partial | The design top bar is "Type=Home" everywhere; no home shortcut or language icon on inner screens |
| B11 | **Mitanin toolbar colour** (`supervisor_toolbar`) | FLAVOR mitanin (SupervisorActivity.kt:188-191) | Partial | Design uses teal for all supervisors; it does not say whether Mitanin keeps its own colour |

### Supervisor home tabs (`SupervisorHomeFragment`, asha_supervisor/SupervisorHomeFragment.kt) — design frame: M2.12-01 · Supervisor home · verification dashboard
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| B12 | **Two tabs: "Incentive Dashboard" and "KPI Dashboard"** | Always (SupervisorHomeFragment.kt:118-142; SuperVisorAdapter.kt:11-18) | Partial | The design home is only the incentive dashboard. The KPI tab has no frame (see B14-B16) |
| B13 | **Hidden developer mode**: long-press the KPI tab to open the "enable dev mode" sheet, or to disable it with the toast "Dev Mode Disabled!" | (SupervisorHomeFragment.kt:128-140) | Missing | Internal-only; one line in the notes is enough |
| B13a | Back on home → **"Exit application?"** confirmation. The toolbar title = the **current village name** in the chosen language | (SupervisorHomeFragment.kt:41-65, 145-160) | Missing | Design title is "Namaste, Anju" |

### KPI dashboard (`SupervisorFragment`, asha_supervisor/supervisor/SupervisorFragment.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| B14 | **12-tile KPI grid with live counts**, each opening the matching list (read-only for the supervisor): Households, Beneficiaries, Eligible couples, Pregnant women, ANC visits, HRP women, Deliveries, Routine immunization, NCD screened, NCD priority, TB cases, Incentive verification | Always (SupervisorFragment.kt:251-263; configuration/IconDataset.kt:225-299) | Missing | The supervisor's whole "view the ASHA's data" entry point is undrawn |
| B15 | **Full-download progress bar** ("Downloading… x%") while the first data pull runs | Pull/push worker RUNNING (SupervisorFragment.kt:212-245) | Missing | — |
| B16 | **Filter bar: Month/Year, Village dropdown (auto-selected when only one), ASHA Worker dropdown, Submit**. Sub-centre is invisible. **Only the Village picker is wired**; Month, ASHA and Submit have no handler | (SupervisorFragment.kt:157-175; layout fragment_supervisor.xml:100-232) | Missing | Flag to product: these half-built filters need a decision (design them or remove them) |

### Incentive dashboard (`IncentiveDashboardFragment`, asha_supervisor/incentiveDashboard/IncentiveDashboardFragment.kt) — design frame: M2.12-01 · Supervisor home · verification dashboard
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| B17 | **"Total ASHAs" tile** (count; tap → list of all ASHAs, "Tap to view ASHAs grouped") | Always (IncentiveDashboardFragment.kt:107-110, 304, 342) | Missing | The design shows "32 ASHAs" only in the subtitle |
| B18 | **Every status tile can be tapped** and opens the ASHA list filtered to that status (Pending / Overdue / Verified / Rejected / Unclaimed / All) | (IncentiveDashboardFragment.kt:331-345) | Partial | Design has a single "Start verifying (15)" button; the tiles look static |
| B19 | **"Sub-centres under you (N)" list** with each sub-centre's ASHA count; tap → the verification list for that sub-centre | (IncentiveDashboardFragment.kt:70-73, 122-142, 318-319; SubCenterAdapter.kt:18-38) | Missing | — |
| B20 | Sub-centre picker includes **"All sub-centres"**. Toast "No sub-centres available" when the list is empty | (IncentiveDashboardFragment.kt:250-283) | Partial | Design shows the "Sub-centre: All" select only |
| B21 | **Blocking "No internet connection" dialog** (cannot be dismissed). It closes itself and the data reloads when the connection returns. Toasts for server errors | Offline (IncentiveDashboardFragment.kt:179-209; dialog/NoInternetDialog.kt) | Missing | The supervisor dashboard is online-only and the design does not say so |
| B22 | **Mitanin flavour labels**: "Total Mitanins", "Mitanins unclaimed", "Tap to view Mitanin grouped". Overdue = **"Past 5th (due date)"** (other flavours: past 12th). Tile subtitles are hidden. Green logo | FLAVOR mitanin (IncentiveDashboardFragment.kt:76-117) | Missing | The design note covers only the 12th |
| B23 | **Role gate (Mitanin)**: for the Trainer (role "ASHA Supervisor") the Verified tile opens the **"approved"** list; for other roles (ANM) it opens the "verified" list | (IncentiveDashboardFragment.kt:87-97, 333-338) | Missing | Two-role Trainer → ANM chain is not drawn |

### ASHA list to verify (`IncentiveVerificationFragment`, asha_supervisor/supervisor/incentiveVerification/IncentiveVerificationFragment.kt) — design frame: M2.12-02 · ASHAs to verify
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| B24 | **Row detail for decided claims**: Verified → "Verified on <date>" + **"By: <name> (role)"** (Mitanin shows "Mitanin Trainer"). Rejected → rejected date + **reason + other reason** + by whom | status VERIFIED/REJECTED (AshaWorkerAdapter.kt:60-110) | Partial | Design rows show name, area, amount and a status tag only |
| B25 | **Unclaimed ASHAs cannot be opened** and their amount is hidden | status UNCLAIMED (IncentiveVerificationFragment.kt:127-130; AshaWorkerAdapter.kt:119-123) | Partial | Design lists "Not claimed" with ₹0 but does not say it cannot be tapped |
| B26 | **Status-count strip** "Verified: n · Pending: n · Rejected: n". When opened from Overdue it shows "Overdue: …" instead (hard-coded **"Overdue: 0"**, a side bug) | (IncentiveVerificationFragment.kt:156-174) | Partial | Design uses tabs with counts (To verify / Verified / Rejected). There is no Overdue or Unclaimed tab, but code can be opened in those states |
| B27 | Header **"Month: October, 2026"** (non-Mitanin uses a monthly-detail card; Mitanin uses a plain month card titled "Mitanin monthly detail", search hint "Search Mitanin") | FLAVOR (IncentiveVerificationFragment.kt:66-99) | Partial | Month is in the subtitle; Mitanin wording not drawn |
| B28 | List **refreshes when returning** from the detail screen (after a verify or reject) | onResume (IncentiveVerificationFragment.kt:105-113) | Missing | — |

### ASHA monthly detail + reject sheet (`WorkerDetailFragment`, asha_supervisor/supervisor/incentiveVerification/WorkerDetailFragment.kt) — design frames: M2.12-03 · ASHA monthly detail, M2.12-04 · Reject with reason
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| B29 | **Read-only mode after a decision**: the Verify/Reject card is hidden when the month is already Verified/Approved/Rejected. A **"Verified by <role>"** banner appears for verified months | isClaimActionable (WorkerDetailFragment.kt:80-82, 111-117, 271-272; model/AshaWorker.kt:74-94) | Missing | Design draws only the actionable state |
| B30 | **Mitanin per-activity checkboxes**: Verify/Reject act only on the ticked activities. **"Monthly Honorarium" rows come pre-ticked** and listed first. **Verify/Reject are disabled until ≥1 row is ticked**. The documents checkbox is hidden; a "Verify will commit every activity…" note is shown | FLAVOR mitanin && actionable (WorkerDetailFragment.kt:84-87, 118-138, 166-196, 232-241; GroupedActivityAdapter.kt:28-44) | Missing | FLW-1171 behaviour; the design has only "Verify all" |
| B31 | **Activities grouped by programme** (collapsible group header with claim count + group total ₹), table columns S.No / Activity / No. of claims / Amount | (GroupedActivityAdapter.kt:28-104; fragment_worker_detail.xml:282-375) | Partial | Design shows a flat list |
| B32 | **Default (honorarium) activities cannot be opened** and show an "approved" badge when already approved | isDefault (ActivityAdapter.kt:88-111) | Missing | — |
| B33 | **Rejection reasons: multi-select checklist of 7 fixed reasons + "Other"** (Incomplete documentation, Incorrect data, Beneficiary data mismatch, Calculation error, Duplicate claim, Ineligible activity, Outside service period). The free-text box shows and is **required only when "Other" is ticked** | Reject sheet (WorkerDetailFragment.kt:208-221, 360-395) | Partial | Design: single-choice radio of 4 different reasons + an always-required "Details for the ASHA" box. Wording and logic both differ |
| B34 | **Validation toasts**: "Please verify documents before approving", "Please select at least one rejection reason", "Please provide the reason for 'Other'", "No records to verify". A success toast then **auto-return to the ASHA list** | (WorkerDetailFragment.kt:313-336, 290-297, 371-388) | Partial | The documents checkbox is drawn but not the blocked state or the success feedback |
| B35 | **Empty state "No activities found"** (hides the table and actions) | records empty (WorkerDetailFragment.kt:258-262) | Missing | — |
| B36 | Header shows **"EmployeeID: <id>, <Month Year>"** and the **reviewer's own ID** ("Supervisor ID" or "Trainer ID") | (WorkerDetailFragment.kt:106-108, 134-135, 150-151) | Partial | The design header has name / month / sub-centre only |

### Claim records (`BeneficiaryDetailFragment`, asha_supervisor/supervisor/incentiveVerification/BeneficiaryDetailFragment.kt) — design frame: M2.12-05 · Claim records → beneficiary
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| B37 | **Per-row ✓ approve / ✕ reject (Mitanin Trainer only)**. ✓ approves that one record at once with no confirmation. ✕ opens the reason sheet for that one record. Info banner: "Approve or reject this activity directly using ✓ / ✕." Double taps are ignored while a request is running | FLAVOR mitanin && actionable (BeneficiaryDetailFragment.kt:75-77, 146-160, 226) | Missing | Design rows have a "View" action only |
| B38 | **Row content**: Ben ID, Name, **RCH ID, ABHA number**, amount. **Meeting/register claims with no beneficiary show "Claim date"** instead | (BeneficiaryAdapter.kt:36-70) | Partial | Design rows show name, meta, a document tag and a vaccines info card. The document-status tag ("No MCP photo") is design-only; the code has no such data |
| B39 | Summary "<n> · ₹<total>" + an **empty state** when there are no records. After any decision, **return to the activity list** | (BeneficiaryDetailFragment.kt:218-256) | Partial | No empty state or summary drawn |

### Supervisor profile (`SupervisorProfileFragment`, asha_supervisor/profile/SupervisorProfileFragment.kt) — design frame: M2.12-06 · Supervisor profile
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| B40 | **Contact info (mobile, email) and posting (district, block, sub-centre)** sections, plus EMP. ID | Always (SupervisorProfileFragment.kt:66-93) | Partial | Design person card shows the ID + block + counts only; mobile, email, district and sub-centre are missing |
| B41 | **Bank details section** (Account no., IFSC). The labels are in the layout, but the code never fills the values | (fragment_supervisor_profile.xml:397-506) | Partial | Not in design. Product should decide whether to show or drop it |
| B42 | Role subtitle: Mitanin shows **"Mitanin Trainer"** for role "ASHA Supervisor", otherwise the raw role. Green NHM logo for Mitanin | FLAVOR (SupervisorProfileFragment.kt:71-86) | Missing | — |
| B43 | **Logout row → confirmation dialog** | (SupervisorProfileFragment.kt:32-46, 94-96) | Missing | Same as B7 |

### No-internet dialog (`NoInternetDialog`, asha_supervisor/dialog/NoInternetDialog.kt) — design frame: NO SCREEN IN DESIGN
| # | Functionality in code | When shown / rule (file:line) | Status | Note |
|---|---|---|---|---|
| B44 | Dialog that cannot be dismissed: "No internet connection – please check your network settings; this dialog will close automatically when connectivity is restored" (wifi-off icon) | Offline on the incentive dashboard (NoInternetDialog.kt:8-17) | Missing | See B21 |

---

#### Design-only items (in Figma but not in code; for information)
- M2.10 step bar Aadhaar → Consent → OTP → Mobile → Done; "Digital Identity" badge snackbar on ABHA created (no gamification hook in `create_abha_id/`).
- M2.10-07 "After 3 wrong OTPs wait 30 min" banner: code only shows the server message + Exit (A26).
- M2.12-01 "6 claims are overdue – verify by the 12th" warning banner and "Start verifying (15)" CTA: no such banner in code.
- M2.12-02 filter chips (All / Overdue / SC …) and tabs: code filters by the tile tapped, plus search only.
- M2.12-05 document-status tags and the vaccines info card: the code payload has no document flag or vaccine list.

No gaps: — (every screen in scope has at least one gap).

---
