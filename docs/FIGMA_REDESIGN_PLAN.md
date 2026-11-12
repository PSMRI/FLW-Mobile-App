# FLW App Redesign: Figma Plan and Progress

> Status (2026-11-04): **Design complete.** Phase 1 + Phase 2 (stages 2.0–2.13, ~160 screens) built and verified, plus full click-through prototype navigation (stage 2.14, 561 links). App logic checked for implementation (section 7).
> Field completeness: every form field (1,147) and every card label is in Figma (stages 2.15–2.17); see `docs/FORM_AND_CARD_AUDIT.md`.
> Next: review with product / field team → answer the 4 open implementation decisions (7.4) → development plan document → build in phases.
> Scope rule so far: **design only, no app code changed.**

---

## 1. Figma file

| Item | Value |
|---|---|
| File name | FLW App Redesign |
| Link | https://www.figma.com/design/omOXvgnOhoVpNZf5NgtHWy |
| File key | `omOXvgnOhoVpNZf5NgtHWy` |
| Location | Drafts of team **"Nikhil Fegade's Team"** (`team::927071537679007715`) |
| Plan | Figma **Starter** |
| Pages | `Foundations` (0:1) · `Components` (2:63) · `Screens` (2:64) |

**To do manually:** in Figma, create a project (for example *FLW App Redesign*) and drag this file into it. Claude can't create Figma projects; it can only create files.

---

## 2. Decisions taken

| Topic | Decision |
|---|---|
| Phase 1 scope | Foundations (design system) + core flows (about 10 screens); other modules reuse these patterns |
| Visual direction | Modern **Material 3**, keeping the current brand blue `#006496` and teal `#006874` |
| Requirements source | Codebase (layouts, nav graphs, strings, `IconDataset`, docs/). Confluence was not connected (Atlassian connector not signed in; jira MCP timed out) |
| Font | **Roboto** (same as `res/font`); Hindi uses Noto Sans Devanagari, Assamese uses Noto Sans Bengali |
| Navigation | **Bottom navigation** (Today / Modules / Sync / Profile) replaces the deep side drawer |
| Module identity | Each module has its own colour tone + icon, so it's recognisable without reading |
| Accessibility | Labels always above fields, touch targets 44–56dp, sync state = colour + icon + text |

---

## 3. What is built

### 3.1 Foundations (variables and styles)

- **Primitives** collection: 34 hidden colour primitives.
- **Color** collection (mode: Light), semantic tokens aliased to primitives:
  - `color/primary`, `on-primary`, `primary-container`, `on-primary-container`, `primary-subtle`
  - `color/secondary`, `secondary-container`
  - `color/surface`, `surface-container`, `surface-container-high`, `on-surface`, `on-surface-variant`
  - `color/outline`, `outline-variant`
  - `color/error`, `error-container`, `on-error-container`, `success`, `success-container`, `warning`, `warning-container`, `accent` (#FD5901)
  - Module tones (`bg` + `fg` each): `household`, `people`, `couple`, `maternal`, `child`, `disease`, `care`, `immunization`, `village`, `records`
- **Dimension** collection: spacing `2xs 4 · xs 8 · sm 12 · md 16 · lg 20 · xl 24 · 2xl 32 · 3xl 40`; radius `sm 8 · md 12 · lg 16 · xl 24 · full 999`
- **Text styles (Roboto):** Display/Small, Headline/Large, Headline/Small, Title/Large, Title/Medium, Title/Small, Body/Large, Body/Medium, Body/Small, Label/Large, Label/Medium, Label/Small, Number/Large, Number/Medium
- **Effect styles:** Elevation/1, Elevation/2, Elevation/3

### 3.2 Components (Components page)

| Component | Node ID | Variants / properties |
|---|---|---|
| Icons (41, `Icon/<name>`) | 3:2 | home, menu, search, sync, person, person_add, group, add, chevron_right, arrow_back, notifications, call, check_circle, cloud_upload, calendar, favorite, pregnant, child, vaccine, warning, lock, translate, form, filter, hospital, dashboard, apps, more_vert, logout, wallet, star, help, location, send, dropdown, check, edit, close, clock, visibility, id_card |
| Button | 4:34 | Style = Filled / Tonal / Outlined / Text × State = Enabled / Disabled; Label, Show icon, Icon |
| Text field | 4:70 | State = Default / Focused / Filled / Error / Disabled; Label, Value, Helper, Show helper, Show trailing |
| Filter chip | 4:77 | Selected = true / false; Label |
| Sync badge | 4:94 | Status = Synced / Pending / Syncing / Failed |
| Avatar | 4:95 | Initials |
| FAB extended | 4:97 | Label, Icon |
| Module tile | 6:121 | Tone = 10 module tones; Title, Count, Show count, Icon |
| Due card | 6:172 | Tone = Maternal / Child / Alert / Immunization / Neutral; Title, Subtitle, Count, Icon |
| Status bar | 6:173 | – |
| Top app bar | 6:203 | Type = Home / Inner; Title, Subtitle, Show subtitle |
| Bottom navigation | 6:296 | Active = Today / Modules / Sync / Profile (Sync has pending badge) |
| Search bar | 6:297 | Placeholder |
| Section header | 6:303 | Title, Action, Show action |
| List item | 6:318 | Active = true / false; Label, Icon, Show chevron |
| Tag | 7:89 | Tone = Maternal / Child / Success / Alert / Neutral; Label |
| Beneficiary card | 7:90 | Name, Meta, Beneficiary ID, Mobile, Head of family, Registered on, Show actions, Show tags (replaces `rv_item_ben.xml`) |
| Household card | 7:135 | HOF name, HH ID, Members, Phone (replaces `rv_item_household.xml`) |

### 3.3 Screens (Screens page, 360 × 800+)

| # | Screen | Node ID | Status | Replaces |
|---|---|---|---|---|
| 01 | Login | 9:2 | ✅ Done | `fragment_sign_in.xml` |
| 02 | Home – Today | 9:60 | ✅ Done | `SchedulerFragment` |
| 03 | Home – Modules | 9:206 | ✅ Done | `HomeIconsFragment` / `IconDataset` |
| 04 | Households | 11:316 | ✅ Done | `AllHouseholdFragment` |
| 05 | Household members | 11:502 | ✅ Done | `fragment_household_members.xml` |
| 06 | All beneficiaries | 11:700 | ✅ Done | `AllBenFragment` |
| 07 | Register member – Step 1 | plugin | ✅ Done | `NewBenRegFragment` |
| 08 | Sync | plugin | ✅ Done | `fragment_sync_dashboard.xml` / `bottom_sheet_sync.xml` |
| 09 | Profile & more | plugin | ✅ Done | Nav drawer (`activity_home_drawer.xml`) + `fragment_asha_profile.xml` |

---

## 4. Phase 1 screen specs (all built ✅)

### 07 · Register member – Step 1
- Inner app bar: "Register member" / "Ramesh Kumar's family"
- Stepper: 3-segment progress bar, "Step 1 of 3 · Personal details", "Next: Family"
- Card "Personal details": photo slot (dashed circle) + "Add photo (optional)"; First name *, Last name; Gender * as 3 chips (Female / Male / Other); Date of birth * with calendar icon + helper "Don't know? Enter age instead"; Relation with head of family * (dropdown); Mobile number * in **Error** state as example
- Info tip: "Your entries are saved on this phone automatically…"
- Sticky bottom bar: Outlined "Save draft" + Filled "Next"

### 08 · Sync
- Home-type app bar "Sync" / "Last synced yesterday, 6:40 PM"
- Hero card: "12 records waiting", "Saved safely on this phone", progress bar, "1,092 of 1,104 records synced (99%)", Filled "Sync now" (sync icon)
- Section "By module" (action: Sync log): rows Households / Beneficiaries / ANC visits / Immunization / EC tracking / Village meetings, each with count + Sync badge
- Error note: "2 eligible-couple records failed. Tap a record to see what needs fixing…"
- Bottom nav: Active = Sync

### 09 · Profile & more (replaces drawer)
- Blue header: avatar, "Sunita Kumari", "ASHA · Employee ID …", village/block, edit icon; stats: Incentive this month, Badges earned, Visits on time
- Groups of list items:
  - **My work:** My profile details, Incentives, Badges & achievements, Dashboard, Supervisor
  - **Tools:** Create ABHA number, Get beneficiary data, Video tutorials & LMS
  - **App:** Language, Help & support, About, Delete my account (red), Log out (red)
- Bottom nav: Active = Profile

### Foundations page
- Colour swatches (semantic + module tones), type scale specimen, spacing/radius scale, short design principles

### Phase 2: see section 4A (all stages built ✅)

---

## 4A. Phase 2 plan: all modules + gamification

### 4A.1 What phase 2 covers
The app has **~150 fragments** across 25+ feature packages (counted 2026-11-04), plus ABHA (10), Supervisor (8),
Login/OTP/Service location and Notifications. Designing each one by hand isn't sensible. Most screens are one of
**8 repeating patterns**, and almost all forms are generated from `configuration/*Dataset.kt` using **15 input
types** (`model/InputType.kt`). So phase 2 works in two layers:

1. **Pattern + form kit**: design every pattern and input type once.
2. **Module flows**: for each module, design the hub, the main list, **one fully drawn representative form**,
   and any screen that is unique. Other forms in the module are listed as "uses pattern P3" with their sections.

Target: **~95 designed frames** that cover all ~170 destinations, plus the gamification layer across the app.

### 4A.2 The 8 screen patterns (stage 2.0)
| ID | Pattern | Used by (examples) |
|---|---|---|
| P1 | **Module hub**: sub-module tiles with counts | Maternal care, Child care, NCD, Disease control, Malaria, Leprosy, VLF, Death reports, HRP, CHO, LMS |
| P2 | **Worklist**: search, filter chips, tabs (Due / Done / All), record cards with status + primary action | PW registration list, ANC visits list, EC list, Infant list, NCD eligible/priority, TB lists, Suspected/confirmed lists |
| P3 | **Long form**: sections, conditional fields, autosave draft, sticky Save / Next | Pregnancy registration, ANC, Delivery outcome, PNC, EC registration/tracking, HBNC, HBYC, CBAC, disease forms |
| P4 | **Visit history sheet**: bottom sheet of visits (ANC 1–4, PNC days, HBNC days) with status + "Add visit" | AncBottomSheet, PncBottomSheet, PmsmaBottomSheet, NcdBottomSheet, CUFYBottomSheet, ECTrackingListBottomSheet |
| P5 | **View mode**: completed form, read-only, locked fields, "Edit" only where allowed | All forms after submit (FLW-977 view-mode issues) |
| P6 | **Screening / assessment**: yes/no questions, live score, result banner (risk level) and referral CTA | CBAC, HRP pregnant/non-pregnant assess, TB screening, Leprosy screening |
| P7 | **Meeting / campaign form**: date, place, participant counts, photos/files | VHND, VHNC, PHC review, Saas-Bahu Sammelan, Maa meeting, AHD, Deworming, Pulse Polio, ORS, Filaria MDA, U-WIN |
| P8 | **Metrics / dashboard**: KPI tiles, charts, month picker | Dashboard, Incentives, Supervisor dashboards, Sync dashboard |

**Common states (stage 2.0):** empty list, loading/skeleton, offline banner, sync error, no search results,
confirmation dialog, unsaved-changes dialog, success snackbar, date picker dialog, filter bottom sheet,
permission request (camera/location), app update / force update.

### 4A.3 Form kit: one component per input type (stage 2.0)
| `InputType` | Component to design | Notes |
|---|---|---|
| EDIT_TEXT | Text field (exists) | Add number/phone/ID variants, character counter |
| DROPDOWN | **Select field** + **option bottom sheet** (searchable when >8 options) | Replaces spinners |
| RADIO | **Radio group**: vertical and 2–3 option segmented | Yes/No as large segmented buttons |
| CHECKBOXES | **Checkbox group** | Multi-select, "None of these" option |
| DATE_PICKER | Date field (exists) + **calendar dialog** | Show allowed range hint (e.g. "LMP within last 280 days") |
| TIME_PICKER | **Time field** + clock dialog | |
| AGE_PICKER | **Age picker**: Years / Months / Days steppers | Used when DOB unknown |
| NUMBER_PICKER | **Stepper** (– value +) | Weight, Hb, counts |
| TEXT_VIEW / TEXT_VIEW_PAIR | **Read-only field** and **label : value row** | Auto-calculated values (EDD, age, gestational weeks) |
| HEADLINE | **Form section header** with step count | Sections collapse in long forms |
| IMAGE_VIEW | **Photo capture** tile (camera / gallery / retake) | Beneficiary photo, MCP card |
| FILE_UPLOAD / MULTIFILE_UPLOAD | **File upload** list with progress and remove | Reports, meeting photos |
| BUTTON | Button (exists) | |
| *(cross-cutting)* | **Conditional field reveal**, **validation summary banner**, **required marker**, **auto-filled badge** | |

### 4A.4 Gamification layer (designed throughout, not as an add-on)
Source: branch `naf_feature_gamification` (see `docs/GAMIFICATION_ASHA_ACCESS_ANALYSIS.md`). Parts and what to design:

| Part (code) | Today | Redesign deliverables |
|---|---|---|
| **Badges**: 9 badges in `BadgeDefinitions.kt`: Steady Syncer, Timely Reporter, Complete Worker, Community Voice, Maternal Journey, Child Fully Protected, Digital Identity, Vulnerable Baby Cared For, Critical Referral Made. Kinds: weekly streak, monthly streak, quarterly, cumulative (tiers) | `fragment_badge_shelf.xml`, `item_badge.xml` | **Badge component** (states: locked / in progress with % / earned / tier 1-2-3 / streak at risk), **badge art set** for the 9 badges, **My Badges shelf** (earned, in progress, locked groups), **badge detail sheet** (what it's for, how to earn it, progress, history) |
| **Celebration** (`BadgeCelebrations`, confetti) | `dialog_badge_celebration.xml` | **Celebration dialog/full-screen** with confetti, badge, tier, "Share with supervisor?" (optional), "See my badges" |
| **Streaks + grace / freeze** (`StreakEngine`) | not visible | **Streak indicator** (weeks/months, grace tokens left, "freeze active" during holidays) on Today and badge detail |
| **My Impact carousel** (Today tab, `item_badge_carousel.xml`) | carousel in Scheduler | Redesigned **My Impact card** on **02 Today** (top 3 in-progress badges + streak); update screen 02 |
| **Monthly Recap** (story playback, ASHA didi character, days 1–7 of month, Hindi/Assamese) | `monthly_recap_strip.xml`, `fragment_monthly_recap_playback.xml` | **Recap strip** states (New / Continue / Watch again) on Today; **story playback** frames (language choice, 4–5 scenes, progress bar, prev/next, replay, close); character + speech-bubble style |
| **Journey** (`fragment_journey.xml`, opened from evening notification) | greeting, narrative, today, lifetime, badges | **Journey screen**: greeting, "today you…", lifetime totals, recent badges, "View all badges" |
| **Evening notification** (21:00) | system notification | **Notification templates** (system tray + in-app Notification panel item) |
| **Access control** (per-ASHA on/off, FLW-1251 / AMM-2556) | flag | Design every touchpoint **with gamification ON and OFF**. With OFF: no My Impact card, no recap strip, Profile hides badges stat/menu, no celebrations. Layouts must not leave gaps |

**Gamification hooks inside module flows** (designed in each module stage):
- After saving a form that advances a badge (e.g. ANC 4 + facility delivery → *Maternal Journey*; HBNC day 7 for LBW → *Vulnerable Baby*; ABHA created → *Digital Identity*; SAM → NRC referral → *Critical Referral*; meeting forms → *Community Voice*; sync → *Steady Syncer*; incentive claim → *Timely Reporter*): **success snackbar with badge progress** ("2 of 3 children fully protected").
- Record cards can show a small "counts toward badge" hint where useful (not on every card).

**Gamification design principles (proposed, to confirm):**
- Celebrate effort, don't pressure. No public leaderboards or ranking between ASHAs (fairness across very different villages).
- Badges reflect real health outcomes from the code rules. No fake points.
- Every badge explains exactly how to earn it, in simple words, in Hindi and Assamese.
- Works fully offline. Celebrations appear when earned locally; server sync is silent.
- Never block work. Celebrations are dismissible and recap is optional.

### 4A.5 Stages, scope and order
| Stage | Module | Key screens to design (≈ frames) | Source |
|---|---|---|---|
| **2.0** | **Pattern + form kit + states** | 15 form components, 8 pattern templates, ~12 states (≈ 35 components/frames) | `model/InputType.kt`, common layouts |
| **2.1** | **Gamification core** | Badge component + 9 badge art, My Badges shelf, badge detail, celebration, streak, Journey, Monthly Recap strip + 5 playback frames, notification templates, **revised 02 Today with My Impact**, Profile with/without gamification (≈ 14) | `badges/*`, `ui/home_activity/{badges,journey,monthly_recap}` |
| **2.2** | **Maternal health** | Mother care hub; PW registration list; Pregnancy registration form; ANC visits list + ANC history sheet; **ANC visit form (representative P3)**; PMSMA + high-risk list; Delivery outcome form; PNC mother list + PNC form; Abortion; Antenatal counselling; HWC referred; badge success snackbar (≈ 13) | `maternal_health/*`, `PregnantWomanAncVisitDataset` |
| **2.3** | **Eligible couple** | EC hub; EC list; **EC registration** (FP method); EC tracking list + sheet; tracking form incl. "Yes + Negative" FP path (FLW-977) (≈ 6) | `eligible_couple/*` |
| **2.4** | **Child care + Infant** | Child care hub; Infant list; HBNC day list + **HBNC visit form** (Part I/II, visit card); HBYC month list + form; Child list (<5) + CUFY form; Adolescent list + form; Infant/child registration (≈ 12) | `child_care/*`, `infant/*`, `HBNCFormDataset` |
| **2.5** | **Immunization** | Immunization categories; child due list (overdue/due/upcoming); **vaccine record sheet** (dose cards, mark given, date); mother (TD/TT) list + sheet; U-WIN list/form (≈ 6) | `immunization_due/*`, `village_level_forms/Uwin*` |
| **2.6** | **High risk (HRP) + CHO** | HRP hub; pregnant/non-pregnant lists; **assess (P6)**; track form; micro birth plan; CHO ben register/list (≈ 9) | `hrp_cases`, `cho/*` |
| **2.7** | **NCD + TB** | NCD hub; eligible / priority / non-eligible lists; **CBAC (P6 with score)**; NCD referral; TB screening → suspected → confirmed → TPT; vulnerable population (≈ 10) | `non_communicable_diseases/*` |
| **2.8** | **Disease control + General OP / check-up** | Disease control hub; communicable disease grid; Malaria (hub, suspected list, form, confirmed); Leprosy (screening, suspected, confirmed, visit); Filaria (+MDA); Kala-azar; AES/JE; confirmed-case follow-up; General check-up list/form; General OP care (≈ 12) | `disease_control/*`, `general_op_care` |
| **2.9** | **Death reports + Village level forms / campaigns** | Death reports hub; CDR / MDSR / GDR / NMDSR lists + one form; VLF hub; VHND / VHNC / PHC review / Saas-Bahu / Maa meeting / AHD / Deworming (P7, one drawn + list); campaigns: Pulse Polio, ORS, Filaria MDA; Survey register (≈ 9) | `death_reports/*`, `village_level_forms/*` |
| **2.10** | **ABHA** | Find ABHA; Aadhaar entry (ASHA/Gov); consent; Aadhaar OTP; mobile OTP generate/verify; ABHA created card; error states (≈ 7) | `ui/abha_id_activity/*` |
| **2.11** | **Profile area + utilities** | ASHA profile details/edit; Incentives (month list → claim detail, rejected/reclaim states); Dashboard; LMS / video tutorials / user guide; Notification panel; Get beneficiary data; Help/About; Language sheet; Sync log (≈ 10) | `asha_profile`, `incentives`, `dashboard`, `lms`, `notifications`, `get_ben_data`, `sync_dashboard` |
| **2.12** | **Supervisor module** | Supervisor home; worker list → worker detail; beneficiary detail; incentive verification (verify / reject with reason); incentive dashboard; supervisor profile (≈ 7) | `ui/asha_supervisor/*` |
| **2.13** | **Remaining + polish** | Household registration (NewHousehold), non-household living, ben registration type, child-as-ben registration, Login extras (forgot password, OTP, service location), dev-mode sheet; **Hindi + Assamese copies of 6 key screens**; **click-through prototype** of the main ASHA day (Today → ANC due → ANC form → save → badge) (≈ 12) | `all_household`, `all_ben`, `login_activity`, `service_location_activity` |

**Order rationale:** 2.0 first because every later stage reuses the kit. Gamification (2.1) comes next because it changes Today
and Profile and adds hooks into every module form. Then modules by field usage (maternal → EC → child →
immunization → HRP → NCD/TB → disease → VLF/death), then ABHA, profile, supervisor, polish.

### 4A.6 How each stage is built (local plugin)
- Each stage is a file `design/figma-plugin/src/3x-stage-2.x-<name>.js`; shared helpers in `src/20-phase2-common.js`, phase 1 in
  `src/10-core-phase1.js`, dispatcher in `src/90-main.js`. `node build.js` joins them into `code.js` (Figma needs one file).
  **Edit `src/`, never `code.js`.**
- Menu has generic commands: **"Phase 2: build current stage(s)"** builds the stages in `CURRENT_STAGES` (src/90-main.js) and
  **"Phase 2: show current stage(s)"** zooms to them (or to `REVIEW_FOCUS` frames). Moving to the next stage = edit
  `CURRENT_STAGES` + add the stage file; no re-import needed.
- Output goes into **Figma Sections on the Screens page** (`2.2 Maternal health`, …) because of the Starter 3-page limit.
  New reusable pieces (form kit, badge component) go on the Components page.
- Frame naming: `M<stage>-<nn> · <screen>`, e.g. `M2.2-05 · ANC visit form`.
- Claude runs each stage in Figma desktop, checks it by screenshot, fixes it, then hands over for review.
- Re-running a stage replaces only that stage's section, so manual edits should be made on duplicates.

### 4A.7 Review checkpoints
After each stage: Claude posts screenshots and a short change list. You (or the product owner) approve or comment before the
next stage starts. Stages 2.0 + 2.1 together are the first checkpoint, because they fix the look of everything after.

### 4A.8 Open questions (need answers before or during 2.0 / 2.1)
1. **Flavours:** design once with notes for differences (Mitanin wording "Mitanin" vs "ASHA", Xushrukha = HRP-only home,
   per-state logos), or separate frames per flavour? *Proposed: once + annotations; logo/name slot in Login & Profile.*
2. **Gamification principles:** OK with **no leaderboards/rankings** and optional "share with supervisor"?
3. **Gamification OFF state:** confirm the default for unknown users is OFF (per FLW-1251), so OFF layouts are the baseline.
4. **Supervisor module:** include the Mitanin Trainer → ANM incentive verification flow (FLW-1169 to 1173) in 2.12?
5. **Badge art:** simple flat icons in module colours (Claude can draw), or a designer/illustrator for final art?
6. **Languages:** are Hindi + Assamese samples of 6 key screens enough, or every screen?
7. **Confluence:** connect Atlassian so BRDs can be checked for each module before its stage?

---

## 5. Figma Starter plan limits (why work stopped)

| Limit | Impact |
|---|---|
| **MCP tool-call limit** | Reached while building screens 07–09; Claude can't read or write the file until it resets or the plan is upgraded |
| 1 variable mode | No Dark mode tokens (dark values can be added after upgrade) |
| 3 pages per file | Everything lives in Foundations / Components / Screens; phase 2 needs sections inside the Screens page, or an upgrade |

Options:
- **Upgrade to Figma Professional** (team settings → Upgrade). This removes the page cap, allows Light/Dark modes and raises the MCP limit. Details: https://developers.figma.com/docs/figma-mcp-server/rate-limits-access/
- **Wait for the limit to reset.** Figma's docs (checked 2026-11-04) give Starter a **monthly** cap of "up to 20 per month", so expect a reset around **early December 2026**. Figma doesn't publish the exact reset date; it is probably the calendar month or the billing cycle.
- For comparison, **Professional** (Full or Dev seat) allows **200 calls per day, 10 per minute**. That's enough to finish phase 1 and phase 2.

### Free alternatives (no Figma MCP limit)

Figma itself is free to use. Only Claude's access through the Figma MCP connector is capped. These options avoid that cap:

| # | Option | Cost | Pros | Cons |
|---|---|---|---|---|
| 1 | **Local Figma plugin** *(recommended)* | Free | No call limit; builds into the **same** Figma file with the same components and tokens | Claude can't see the result; you share screenshots and Claude adjusts. A few MCP-only helpers must be rewritten in the standard Plugin API |
| 2 | **Penpot** (open-source alternative to Figma) | Free (cloud or self-hosted) | No page/mode limits (dark mode possible); likely has an MCP server (confirm before relying on it) | Moves the design off Figma; import the current file with a Figma-to-Penpot export plugin; the team may expect Figma |
| 3 | **HTML mockups** | Free | Fast; Claude can build and check every screen itself; can be shared as a link | Not editable design layers; importing into Figma later needs an HTML-to-Figma plugin (limited free tiers) |
| 4 | **Excalidraw / draw.io wireframes** | Free | Quick agreement on flows | Low fidelity only |

**Recommendation:** option 1 keeps the existing Figma file with no extra cost. Option 3 is a good free way to review all ~170 screens quickly before detailed design.

#### How option 1 works
1. Claude writes `design/figma-plugin/manifest.json` + `code.js` (outside the app code; no app code changes).
2. Open the Figma **desktop app** → open *FLW App Redesign* → **Plugins → Development → Import plugin from manifest…** → select `design/figma-plugin/manifest.json`.
3. Run it: **Plugins → Development → FLW Redesign Builder**. It creates the next screens on the Screens page.
4. Share a screenshot (or export the frame as PNG); Claude fixes the script; re-run.

#### What to type for each option
- Option 1:
  ```
  Set up the free Figma local plugin (option 1 in docs/FIGMA_REDESIGN_PLAN.md)
  in design/figma-plugin/ and build screens 07–09 and the Foundations page.
  Design only — no app code changes.
  ```
- Option 2:
  ```
  Help me move the FLW redesign to Penpot (option 2 in docs/FIGMA_REDESIGN_PLAN.md):
  check the Penpot MCP setup, then import the existing Figma file.
  ```
- Option 3:
  ```
  Build the FLW redesign screens as HTML mockups (option 3 in
  docs/FIGMA_REDESIGN_PLAN.md) using the same tokens and components,
  starting with screens 07–09. Design only — no app code changes.
  ```

### How to check if the limit is enabled again
In Claude Code, type:

```
check if the Figma MCP limit has reset by calling whoami and taking a screenshot of the Screens page in file omOXvgnOhoVpNZf5NgtHWy
```

If it returns a screenshot (not "You've reached the Figma MCP tool call limit"), you can continue.

If Figma shows as disconnected, run `/mcp`, choose **claude.ai Figma** and sign in again.

---

## 6. What to type to resume

### Continue with the local plugin (no limit)

```
Continue the Figma redesign with the local plugin (design/figma-plugin).
Read docs/FIGMA_REDESIGN_PLAN.md, add the next module flow to code.js,
run it in Figma desktop and verify by screenshot. Design only — no app code changes.
```

> Claude drives Figma desktop via macOS automation (Android Studio needs Accessibility
> permission). Don't type in Android Studio while Claude is clicking in Figma.
> New menu items in manifest.json need a re-import (Plugins → Development → Import plugin from manifest).

### Resume phase 1 (when the limit is enabled)

```
Continue the Figma redesign. Read docs/FIGMA_REDESIGN_PLAN.md first.
File key omOXvgnOhoVpNZf5NgtHWy. First check the Screens page for partly
created screens 07–09 and clean them up, then build 07 Register member,
08 Sync and 09 Profile & more as per section 4, then the Foundations page.
Design only — no code changes.
```

### Start phase 2 (after phase 1 is approved)

```
Start phase 2 of the Figma redesign: read section 4A of docs/FIGMA_REDESIGN_PLAN.md
and build stage 2.0 (pattern + form kit) and 2.1 (gamification core) with the local
plugin, run it in Figma desktop and verify by screenshot. Design only — no app code changes.
```

Next stages: replace the stage number, e.g. `build stage 2.2 (Maternal health)`.

### If the file was moved into a Figma project

```
The Figma file is now in project <paste project URL>. Continue the
redesign from docs/FIGMA_REDESIGN_PLAN.md and create any new files in
that project.
```

### When Confluence is available

```
I've connected Atlassian (/mcp → claude.ai Atlassian). Read the FLW
Confluence pages for <module> and update the Figma screens and
docs/FIGMA_REDESIGN_PLAN.md with anything missing.
```

### When design is signed off (development)

```
The Figma redesign is approved. Using docs/FIGMA_REDESIGN_PLAN.md and
the Figma file, create a development plan: map tokens to colors.xml /
themes.xml, components to XML/Compose, and screens to fragments.
```

---

## 7. App logic check & implementation readiness

Checked on 2026-11-04 against the code on `naf_feature_gamification`, to answer: *can this UI be implemented without breaking the app's logic?*

### 7.1 How show / hide works in the app today
- **One form engine:** all generated forms extend `configuration/Dataset.kt`. When an answer changes, `updateList()` calls the form's `handleListOnValueChanged()`, which uses `triggerDependants(...)` to **insert or remove follow-up questions in one flat list**. Removed questions also get their value and error cleared, so no stale data is saved.
- **282 show/hide rules** across the datasets. Largest: beneficiary registration 39, Malaria 29, EC tracking 26, ANC 20, Suspected TB 14, Leprosy 13, EC registration 13, HBNC 12, general registration 12, PNC 11, Kala-azar 11, pregnancy registration 10.
- **One form renderer:** every generated form is drawn by `adapters/FormInputAdapter.kt` (≈1,690 lines) with one layout per `InputType`. The design's form kit (stage 2.0) is one component per `InputType`, so it maps 1:1.
- **Screen-level show/hide:** about 158 files in `ui/` set view visibility directly (lists, cards, home, sync). These keep their logic; only layouts change.
- **Server-driven forms** (`repositories/dynamicRepo/*`: CUFY SAM/ORS/IFA, Filaria MDA, GC/NCD/TB referral follow-ups, campaigns) render from form JSON and also go through shared renderers.

### 7.2 What this means
The redesign can be applied **as a visual layer**: theme + design tokens + `FormInputAdapter` item layouts + card/list/hub layouts. **None of the 282 rules, validation, sync, or the Room database need to change**, so no DB migration is needed and every form gets the new look at once.

### 7.3 Where the design goes beyond today's logic (needs a small code change each)
| # | Design element | Today | Change needed | Risk |
|---|---|---|---|---|
| 1 | Collapsible numbered form sections | `HEADLINE` is a plain title | Adapter: collapse/expand + "x of y done" count | Medium |
| 2 | Conditional questions indented with blue line | Renderer doesn't know which answer revealed a question | `triggerDependants` tags inserted elements with their parent id | Low–medium (touches the engine) |
| 3 | Yes/No as segmented buttons | Radio group | Adapter picks style when a RADIO has ≤ 3 short options | Low (visual) |
| 4 | Bottom navigation replaces the drawer | `nav_home.xml` + drawer in `HomeActivity` | Navigation restructure | **High**: do behind a switch |
| 5 | New screens: care timeline, PNC/HBNC schedule, vaccination card, CBAC stepper | Not present | New layouts over existing data/queries | Medium |
| 6 | Gamification ON/OFF per ASHA | Flag not available | Needs FLW-1251 (mobile), blocked by AMM-2556 (backend) | Blocked |

### 7.4 Decisions needed before development
1. Collapsible sections (7.3 #1): yes / later?
2. Indented conditional questions (7.3 #2): OK to touch the form engine?
3. Bottom navigation (7.3 #4): ship in the first release, or after, behind a switch?
4. Gamification screens (7.3 #6): wait for FLW-1251, or build hidden behind the current default?

### 7.5 What can be done independently vs needs others
| Can be done by the developer (Claude) alone | Needs the team |
|---|---|
| Tokens → `colors.xml` / `themes.xml` / `dimens.xml` for every flavour | Real-device testing on low-end phones, every flavour (Sakhi, Saksham, Mitanin, Niramay, Xushrukha) |
| Form kit in `FormInputAdapter` (all 15 input types) | Product decisions in 7.4 |
| Redesigned cards, lists, hubs, Today, Profile, Sync, new screens | Hindi / Assamese review (some existing `values-as` strings look wrong) |
| Logic, validation, sync and DB left untouched; old layouts commented out, not deleted | Server-driven form fields (SAM / ORS / IFA, Pulse Polio) and backend gamification flag |
| Builds + targeted tests (niramay + mitaninUat) after each step; one module per branch/PR | QA sign-off |

> **Detailed solution for every item above: see `docs/REDESIGN_IMPLEMENTATION_PLAN.md`.**

### 7.6 Recommended build order
1. **Development plan document**: every screen → fragment / layout, with risk and effort.
2. **Theme & tokens** (colours, type, spacing, shapes) for all flavours.
3. **Form kit** in `FormInputAdapter`: every form improves at once, no logic change.
4. **Cards, lists, hubs** (beneficiary / household / record cards, module tiles, worklists).
5. **Today, Profile, Sync** screens.
6. **Navigation restructure** (bottom nav) behind a switch.
7. **Gamification screens** after FLW-1251.

---

## 8. Change log

| Date | Change |
|---|---|
| 2026-11-04 | **Complete forms & cards redesigned** (UI/UX pass on 2.15–2.17): every form now uses the module-screen style: subject header, step bar, section cards with module-colour icon tiles (sections auto-grouped by meaning, HEADLINEs kept), leading icons on inputs (new Text field props *Show leading* / *Leading icon*), M3 Yes/No buttons, choice chips, searchable select, steppers side by side, date+time pairs, auto-filled tile, photo tiles, conditional blue blocks. Cards: module accent, avatar, relation/age summary, call button, sync/status tags, all labels as icon grid, same buttons. 1,072 inputs + 75 section titles = all 1,147 fields, 0 failed. Extractor fixes: fully-qualified `InputType`, `<array>` resources, inline `arrayOf(getString…)`. |
| 2026-11-04 | **Field & card completeness audit** (`docs/FORM_AND_CARD_AUDIT.md`): hand-drawn screens had 327/1,147 dataset fields (28%). Added stages 2.15/2.16 = complete form specs for all 49 forms, every one of 1,147 fields in code order with real type, required `*`, options and conditional grouping (0 failed); stage 2.17 = complete card specs for 42 `rv_item_*` card layouts (every label + button). Extractors: `design/audit/extract_forms.py`, `extract_cards.py` → `forms.json` / `cards.json`, embedded by `build.js`. |
| 2026-11-04 | `docs/REDESIGN_IMPLEMENTATION_PLAN.md` created: app structure map + design-only solution for all 6 open items (renderer-side collapsible sections, generic `revealedBy` hook in `Dataset.updateList`, segmented Yes/No, bottom nav behind `UiFlags` with Profile tab reusing drawer actions, re-skin of existing sheets, gamification via existing `GamificationConfigProvider`). |
| 2026-11-04 | **App logic check** added (section 7): show/hide = 282 `triggerDependants` rules in `Dataset.kt`, rendered by `FormInputAdapter`; redesign can be a visual layer; 6 design items need small code changes; 4 decisions listed. PDF exported to ~/Downloads. |
| 2026-11-04 | **Stage 2.14 prototype navigation** (src/44-stage-2.14-navigation.js, map in `NAV`): 561 tap links on all screens, 0 failed, all 114 destinations exist. Bottom nav, back arrows (Back action), home bell → notifications / sync icon → Sync, module & hub tiles → lists, Today due cards → lists, list-card buttons → forms, form primary → next step (secondary → back), FABs, profile menu items, badge snackbars → My badges, dialogs. Flows: Login → Today, ASHA day, All modules, Supervisor, ABHA, Gamification. On-canvas "Navigation report" shows the result. Re-run after any screen rebuild (stage rebuilds drop links on replaced frames). |
| 2026-11-04 | **Stages 2.10–2.13 built and verified.** 2.10 ABHA (7): find/create, Aadhaar (ASHA/GOV), consent, Aadhaar OTP boxes, mobile OTP, ABHA created card + Digital Identity badge, offline/OTP-limit error. 2.11 Profile & utilities (9): profile details/edit (AshaProfileDataset), incentives month view with status per activity, rejected → reclaim, learning videos, get beneficiary data, sync log, language sheet, help/about/delete account. 2.12 Supervisor (6): verification dashboard (pending/overdue/verified/rejected/not claimed), ASHAs to verify, ASHA monthly detail with verify-all, reject with reason, claim records, supervisor profile. 2.13 (11 + prototype): new household, add-member type, living elsewhere, work location, reset password, Hindi Today/Modules/Households, Assamese Today/Modules/Login using values-hi/values-as strings; click-through prototype flow "ASHA day: ANC visit" (6 of 6 links). |
| 2026-11-04 | **Stage 2.9 Death reports & village forms built and verified** (12 frames): death reports hub (CDR / MDSR / GDR / NMDSR), maternal deaths list, CDR form, MDSR form (timing, cause, reporting, ANM verification, documents), VLF hub with 12 meetings/campaigns + Community Voice progress, VHND form (attendance steppers + education checklist + photos), Maa meeting, Pulse Polio (fields illustrative), Deworming day, simple meeting pattern (U-WIN / Saas Bahu / AHD), meetings list + Community Voice badge snackbar, survey register. |
| 2026-11-04 | **Stage 2.8 Disease control & check-up built and verified** (16 frames): disease control hub (General check-up / NCD / Communicable), communicable grid (6 diseases), malaria screening (symptoms, RDT/slide), malaria treatment with Pf 3-day / Pv 14-day tracking, leprosy screening + MDT visit, filaria + MDA, kala-azar (AES/JE same pattern), General check-up per BRD v0.5 (list, add check-up with type → problems, emergency red options + Call 108, owned-module tags, routing/dedup dialog, refer to facility with incentive hint, HWC referred list from all sources, follow-up form), General OP care. Kit fix: text-field helper text wraps. |
| 2026-11-04 | **Stage 2.7 NCD & TB built and verified** (11 frames): hub, NCD list (CBAC due / priority / not eligible), CBAC as a 5-part stepper with real fragment_cbac questions + live score bar, B1 signs with TB alert dialog, mood (PHQ-2) / environment + referral result card, NCD referral & follow-up, TB screening (symptomatic / risk factors / HIV), suspected TB (X-ray, sputum, Nikshay ID, DR-TB, TPT), TB treatment monthly follow-up, TPT for contacts, vulnerable population. |
| 2026-11-04 | **Stage 2.6 High risk & CHO built and verified** (8 frames): HRP hub (pregnant / non-pregnant × assess / follow-up), HRP pregnant list, assess high risk (8 factors + live result card), HRP follow-up (12 conditions, BP with inline refer warning, RBS/FBS+PPBS/OGTT), micro birth plan (where to go / blood / support & transport, shareable), non-pregnant HR follow-up with risk status, CHO home role variant, CHO beneficiary registration. |
| 2026-11-04 | **Stage 2.5 Immunization built and verified** (6 frames): hub with session-day banner, child due list (Overdue / This week / All), age-stage filter sheet (ChildImmunizationCategory), child vaccination card by stage with dose status pills, multi-dose record form with refusal reason + Child Fully Protected badge snackbar, mother Td list + dose sheet. |
| 2026-11-04 | **Stage 2.4 Child care & infant built and verified** (12 frames): hub, newborn list, HBNC 7-visit schedule (LBW → badge), HBNC day form (short yes/no danger signs + sepsis checklist), danger sign → refer sheet with Call 108, HBYC list + form (MCP colour picker, RBSK referral), children under 5 + CUFY forms sheet, SAM check → NRC (Critical Referral badge), adolescent list + form, child registration. Helpers: baby initials, snackbar above bottom bar. |
| 2026-11-04 | **Stage 2.3 Eligible couple built and verified** (9 frames): hub, registration list, registration form (children as repeating cards, Nayi Pahal kit), tracking list, monthly history sheet (Apr–Mar), tracking form with FP method + ANTRA dates + MPA card, pregnant path → M2.2-03, view mode for FLW-977, Mitanin two-question variant. |
| 2026-11-04 | **Stage 2.2 Maternal health built and verified** (16 frames, fields from the *Dataset.kt files): hub, registration list, registration form with 8 high-risk checks, care timeline, full ANC form, home visit/counselling, PMSMA form, high-risk list, delivery outcome, delivery → next steps (+ Maternal Journey badge), infant registration, PNC schedule sheet, PNC form, abortion, HWC referral list, maternal-death branch dialog. Kit fixes: section-header subtitle and read-only label now truncate; rows auto-stack (relayoutRows). |
| 2026-11-04 | **Stages 2.0 + 2.1 built and verified.** 2.0: 17 kit components (radio, segmented, checkbox, stepper, read-only field, info row, section header, photo tile, file row, banner, snackbar, dialog, tabs, empty state, skeleton, record card, calendar) + 14 frames (P1–P8 patterns, S1–S6 states) + form-kit reference board. 2.1: 8 components (tier stars, progress bar, badge medal ×27, badge tile, streak chip, badge progress row, My Impact card, recap strip) + 11 frames (Today ON, My badges, badge detail, celebration, Journey, Recap ×3, evening notification, notifications panel, Profile OFF) + rules note. Phase-1 frames renamed 02 = gamification OFF, 09 = ON. Defaults used for open questions: annotate flavours, no leaderboards, OFF baseline, placeholder badge art. |
| 2026-11-04 | Phase 2 planned (section 4A): 8 patterns, form kit for 15 input types, gamification layer, 14 stages (2.0–2.13, ≈ 160 frames), open questions. |
| 2026-11-04 | Plugin imported and run in Figma desktop (driven by Claude via macOS automation). Built 07 Register, 08 Sync, 09 Profile & more and the Foundations overview; verified by screenshot. Design fix: Home top app bar no longer shows the ☰ menu (drawer replaced by bottom nav + Profile); applies to 02, 03, 08. |
| 2026-11-04 | Option 1 chosen. Local plugin created in `design/figma-plugin/` (manifest.json, code.js, README.md): builds 07 Register, 08 Sync, 09 Profile and the Foundations overview. Waiting for the first run in Figma desktop. |
| 2026-11-04 | File created; foundations, 41 icons, 17 components, screens 01–06 built. Paused at the Starter MCP limit before 07–09. |

### 2026-11-05 · Functionality gap fix + Mitanin
- Audit `docs/DESIGN_FUNCTIONALITY_GAPS.md` (218 screens, 464 missing / 368 partial) → all covered in Figma; mapping in `docs/DESIGN_GAP_COVERAGE.md`.
- New shared kit `src/22-gap-kit.js` (`GK(ctx)`: actionCard with deceased/deleted states, dialog, visitSheet, sort/source sheets, searchRow, viewMode, blockedAdd, uploads, table, **rules()** yellow "RULES (from code)" notes, finish()). Search bar component now has a voice mic (`ensureSearchMic`).
- Stage 2.18 = pattern reference; **stage 2.19 = Core screens v2** (Login, Today, Modules, Profile, Households, Members, All beneficiaries redrawn with all functionality: delete beneficiary/household, cataract/eye surgery, IFA, non-household, registration prompts, app-wide dialogs).
- Stages 2.2–2.13 edited in place (no renames) + new frames; ~413 frames in these stages (was ~127), 35 Mitanin variant frames.
- Prototype (2.14) re-run: bottom nav → v2 Today/Modules/Profile; tiles → new sub-hubs/lists; 1205 links, 0 failed. Re-run 2.14 after rebuilding any stage.
- Design follows code everywhere (incl. the 10 open decisions). Items only in design are marked "Not in app today – needs dev".
