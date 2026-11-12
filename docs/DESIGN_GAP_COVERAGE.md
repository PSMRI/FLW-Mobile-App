# Design gap coverage (gap → Figma frame / rule note)

> 2026-11-05. Every row of docs/DESIGN_FUNCTIONALITY_GAPS.md mapped to the frame or yellow "RULES (from code)" note that now covers it, plus a Mitanin section per group.
> Rule used: design follows the code. Design-only items are kept and marked "Not in app today – needs dev"; code bugs are drawn as intended behaviour with a "code bug" note.


## Coverage A1 · Home shell, Households, Beneficiaries, Login
Files: design/figma-plugin/src/47-stage-2.19-core.js (new, 58 frames M2.19-01…58), design/figma-plugin/src/43-stage-2.13-polish.js (6 frames edited with rules, 8 new M2.13-06…13)

### Home shell (HomeActivity)
- Home shell #1 → M2.19-11 · Log out (unsynced warning) (new) | rule note
- Home shell #2 → M2.19-08 · Session expired (new) | rule note
- Home shell #3 → M2.19-07 · Account deactivated (new) | rule note
- Home shell #4 → M2.19-06 · Unsupported device (new) | rule note
- Home shell #5 → M2.19-10 · In-app update (new) | rule note
- Home shell #6 → M2.19-09 · Permissions on first open (new) | rule note
- Home shell #7 → M2.19-19 Profile v2 (Sync records row) + M2.19-20 toast (new) | rule note
- Home shell #8 → M2.19-19 (new) | rule note (role rule)
- Home shell #9 → M2.19-19 rule + M2.19-56 · Profile & more · Mitanin (new)
- Home shell #10 → M2.19-19 (new) | rule note (external MS Form, URL per flavour)
- Home shell #11 → M2.19-19 "Support (feedback form)" (new) | rule note
- Home shell #12 → M2.19-19 "Change work location" + M2.19-13 toolbar title (new) | rule note
- Home shell #13 → M2.19-19 avatar camera + Emp-ID fallback (new) | rule note
- Home shell #14 → M2.19-13 rule note (toolbar language/home icons)
- Home shell #15 → M2.19-20 · Sync status sheet (new) | rule note
- Home shell #16 → M2.19-19 rule note (chat support OFF today)
- Home shell #17 → M2.19-19 version caption (new) | rule note
- Home shell #18 → M2.19-13 banner "Syncing automatically…" (new) | rule note

### Home container
- #1 → M2.19-12 · Exit app (new) | rule note
- #2 → M2.19-13 rule note (Modules is default tab; confirm)
- #3 → M2.19-17 rule + M2.19-18 · Dev mode PIN (new)
- #4 → M2.19-13 first-download strip (new) | rule note
- #5 → M2.19-13 rule note (background schema download)

### Today / Scheduler
- #1 → M2.19-13 due card "Suspected high-risk non-pregnant women" (new) | rule note
- #2 → M2.19-13 due card "Confirmed high-risk pregnant women" (new) | rule note
- #3 → M2.19-13 ABHA card with New/Old counts (new) | rule note
- #4 → M2.19-13 RCH ID card (new) | rule note
- #5 → M2.19-15 Missed period + M2.19-16 No follow-up (new) | rule note
- #6 → M2.19-13 impact carousel (arrows, 3 / 8) (new) | rule note (hidden when empty, haptic, tap → badges)
- #7 → M2.19-13 rule note (debug only)
- #8 → M2.19-14 · Today · loading (new)

### Modules grid + Recap
- #1 → M2.19-17 counts on 4 tiles (new) | rule note
- #2 → M2.19-17 "NCD referred list" tile (new) | rule note
- #3 → M2.19-17 recap strip with resume progress + NEW (new) | rule note (reduced motion)
- #4 → M2.19-17 rule note (debug only)

### Missed period
- #1 → M2.19-15 (new) | #2 → M2.19-15 rule/caption (loading)
### Non-follow-up
- #1 → M2.19-16 (new)
### Enable dev mode sheet
- #1 → M2.19-18 (new) | rule note
### Household hub
- #1 → M2.19-21 · Household hub (new)

### Households list
- #1 → M2.19-22 delete icon + M2.19-23 confirm (new) | rule note
- #2 → M2.19-22 deleted card state (new) | rule note
- #3 → M2.19-22 rule + M2.13-09 · Household view mode (new)
- #4 → M2.19-22 0-member card footnote (new) | rule note
- #5 → M2.19-26 · Add member · gender + relation (new) | rule note
- #6 → M2.19-25 · Incomplete form found (new)
- #7 → M2.19-24 · Households · supervisor (new)
- #8 → M2.19-22 search with mic (new) | rule note
- #9 → M2.19-22 rule note (Filaria "Add MDA")

### Household members
- #1 → M2.19-27 delete icons + M2.19-28 confirm (new)
- #2 → M2.19-29 · HOF delete blocked (new)
- #3 → M2.19-27 rule note (+ deceased/deleted cards without delete)
- #4 → M2.19-27 deleted card (new) | rule note
- #5 → M2.19-27 FAB + M2.19-30 loading/supervisor (new) + M2.19-26
- #6 → M2.19-27 Register Wife / Husband / Children / View Children (new) | rule note
- #7 → M2.19-27 Cataract orange/green → M2.19-36…38 (new) | rule note (+ code bug: also navigates to form)
- #8 → M2.19-27 rule + M2.19-46 view mode (new)
- #9 → M2.19-27 rule note (ABHA hide rules)
- #10 → M2.19-27 rule note (call hide + permission toast)
- #11 → M2.19-27 rule note (dead feature today)

### New household registration (M2.13-01 edited)
- #1 → M2.13-06 · Household consent (new) + M2.13-01 rule
- #2 → M2.13-07 · Add head of family? (new) + M2.13-01 rule
- #3 → M2.13-13 · New household · Mitanin (ABHA lookup) (new) + M2.13-08 · ABHA already exists (new)
- #4 → M2.13-09 link-mode banner + M2.19-47 (new) | rule note
- #5 → M2.13-09 · Household view mode (new)
- #6 → M2.13-01 rule note (voice input)
- #7 → M2.13-10 · saving / failed (new)

### Member living elsewhere (M2.13-03 edited)
- #1 → M2.13-03 edited (select + institution) + M2.13-12 · all 18 options + Other reveal (new)
- #2 → M2.13-03 edited (subtitle/banner meaning) + rule note; entry M2.19-35

### All beneficiaries
- #1 → M2.19-31 filter chips + M2.19-32 filter dialog (new)
- #2 → M2.19-33 ABHA list + M2.19-35 Non-household list (new) | rule note (RCH, age ≥30, eligible females by title)
- #3 → M2.19-33 CSV icon + toast (new) | rule note
- #4 → M2.19-35 Add beneficiary (new) | rule note
- #5 → M2.19-31 Cataract (new) → M2.19-36
- #6 → M2.19-34 · ABHA not synced (new) | rule note
- #7 → M2.19-31 New / Old ABHA tags (new)
- #8 → M2.19-31 Mobile verified tags (new) | rule note (hidden on Mitanin, M2.19-58)
- #9 → M2.19-31 deceased card + Date of death (new) | rule note
- #10 → M2.19-31 "No household" tag (new) | rule note
- #11 → M2.19-31 rule note (avatar by age/gender; design keeps initials)
- #12 → M2.19-31 relation rows + rule note
- #13 → M2.19-31 Register Children button + rule note
- #14 → M2.19-31 toast "Please allow permissions first" (new)
- #15 → M2.19-31 search mic (new)
- #16 → M2.19-31 no delete icon + rule note
- #17 → M2.19-31 sync badges + rule note

### ABHA beneficiaries list (AbhaBenFragment)
- #1 → skipped: not reachable in code (legacy); noted as rule on M2.19-33

### Eye surgery visit sheet
- #1 → M2.19-37 / M2.19-38 visit lists (new)
- #2 → M2.19-36 / 37 / 38 add-row rules (new)
- #3 → M2.19-37 footnote + M2.19-39 locked "Eye operated" (new)
### Eye surgery form
- #1 → M2.19-39 (new) | rule note (server-driven)
- #2 → M2.19-39 photo tile (new)
- #3 → M2.19-40 view mode (new)
- #4 → M2.19-39 date error + rule note
- #5 → M2.19-40 toast + M2.19-39 rule note

### IFA distribution form
- #1 → M2.19-41 rule note (entry from EC card)
- #2 → M2.19-41 history table (new)
- #3 → M2.19-41 blocked Add new + M2.19-42 revealed form (new)
- #4 → M2.19-42 date/photo + rule note

### Add member · choose type (M2.13-02 edited)
- #1 → M2.13-02 rule + M2.19-52 consent (new)
- #2 → M2.13-02 warning banner (edited) + M2.13-11 · no IDs left (new)
- #3 → M2.13-02 rule + M2.19-25
- #4 → M2.13-02 rule note
- note (dormant) → M2.13-02 rule note

### Adult registration 3-page legacy
- #1 → skipped: reachable only from dormant type screen; rule note on M2.19-49

### Register member form
- #1 → M2.19-43 · preview (new)
- #2 → M2.19-44 · add spouse? (new)
- #3 → M2.19-45 · add children? (new)
- #4 → M2.19-46 + M2.19-47 + M2.19-48 (new)
- #5 → M2.19-49 OTP + resend timer + verified (new)
- #6 → M2.19-49 rule + M2.19-57 Mitanin (new)
- #7 → M2.19-49 uploads incl. PDF (new) | rule note (Mitanin disabled)
- #8 → M2.19-49 photo camera only (new)
- #9 → M2.19-50 · age picker (new)
- #10 → M2.19-46 view mode + death lock (new)
- #11 → M2.19-52 · registration consent (new) | Mitanin rule
- #12 → M2.19-43 rule note (back navigation)
- #13 → M2.19-49 rule note (voice input)
- #14 → M2.19-43 rule note (auto sync)

### Child registration from mother card
- #1 → M2.19-51 (new) | rule note
- #2 → M2.19-51 rule note + M2.19-52 consent

### Get beneficiary data
- #1 → M2.19-53 page buttons (new) | rule note
- #2 → M2.19-53 error empty state (new)
- note (unreachable) → M2.19-53 rule "Not in app today – needs dev"

### Login activity
- #1 → M2.19-05 (new) | #2 → M2.19-06 (new) | #3 → M2.19-07 (new) | #4 → M2.19-09 (new)

### Sign in
- #1 → M2.19-01 Remember me (new) | rule
- #2 → M2.19-02 different user (new)
- #3 → M2.19-03 offline login (new)
- #4 → M2.19-01 error helper (new)
- #5 → M2.19-01 language entry + M2.19-04 sheet (new)
- #6 → M2.19-01 "Delete my account" link (new) | rule
- #7 → M2.19-01 rule + M2.13-04 rules (edited)
- #8 → M2.19-01 DEMO tag + footer (new) | rule; M2.19-54 Mitanin
- #9 → M2.19-01 rule note (prefill)
- Forgot password → M2.13-05 rule "Not in app today – needs dev"; M2.13-L6 rule pointer

### Language sheet
- #1 → M2.19-04 Bangla option (new)
- #2 → M2.19-04 rule + M2.19-54 (Mitanin En + Hi)
- #3 → M2.19-04 rule note (no Apply; applies at once)

Totals: 127 gap rows (+ 4 notes) → 125 covered, 2 skipped (AbhaBenFragment legacy, 3-page legacy adult form – both unreachable in code; noted as rules).

### Mitanin
- SignInFragment.kt:288-291 DEMO tag on non-prod Mitanin → M2.19-54 · Login · Mitanin (new) + M2.19-01 rule
- build.gradle:124-138 app name "मितानिन" (-Stag/-Uat) → M2.19-54 rule
- LanguageBottomSheet.kt:55,:78 English + Hindi only → M2.19-54 (radios) + M2.19-04 rule; M2.13-L6 rule
- HomeActivity.kt:172-177 Home language dialog En/Hi on Mitanin (others En/Hi/As, Bangla commented) → M2.19-56 rule
- HomeActivity.kt:893-903 "Monthly Claim Summary" → M2.19-56 · Profile & more · Mitanin (new) + M2.19-19 rule
- IconDataset.kt:53-57 "Mitanin/ Village Meetings" tile → M2.19-55 · Modules · Mitanin (new)
- IconDataset.kt:324, :358, :411 Cluster Meetings / no Saas Bahu / no U-WIN (inside VLF hub, stage 2.9 scope) → M2.19-55 rule note
- HouseholdFormDataset.kt:145-188, :221 ABHA/Ayushman check only Mitanin new household → M2.13-13 · New household · Mitanin (new) + M2.13-01 rule
- NewHouseholdFragment.kt:108-110 Mitanin consent text → M2.13-06 rule
- NewHouseholdFragment.kt:213-251 (:222) lookup prefill Mitanin → M2.13-13 rule
- BenRegFormDataset.kt:1217-1219 ABHA check for new non-HOF member → M2.19-57 · Register member · Mitanin (new)
- BenRegFormDataset.kt:938-943 HOF ABHA check commented (captured on household) → M2.19-57 rule
- BenRegFormDataset.kt:945, :966, :1104, :1220, :1238, :1249, :1388, :2623, :2658, :3212 no temporary contact / send OTP on Mitanin → M2.19-57 rule
- BenRegFormDataset.kt:1032-1036, :1346-1350 name read-only for existing ben → M2.19-57 (auto field) + rule
- NewBenRegFragment.kt:719 birth-cert upload picker disabled → M2.19-57 uploads caption + rule; M2.19-49 rule
- NewBenRegFragment.kt:509 Mitanin consent text → M2.19-57 rule + M2.19-52 rule
- NewBenRegFragment.kt:102-107 Submit gated on ABHA fetch (commented) → M2.19-57 rule
- NewBenRegFragment.kt:568-612 lookup prefill + ABHA exists → M2.19-57 rule
- rv_item_ben.xml:509 mobile consent icon hidden → M2.19-58 · All beneficiaries · Mitanin (new) + M2.19-31 rule
- AllHouseholdFragment.kt:256, :287; HouseholdMembersFragment.kt:354; HouseHoldListAdapter.kt:109 internet gate for new ben (commented / no effect) → M2.19-58 rule
- CUFY IFA Mitanin refs (child care) → out of scope (stage 2.4 owner)

---

## Coverage A2: profile, sync, incentives and other screens (stage 2.11, file 41-stage-2.11-profile.js)

### Home shell
- Home shell #1 → M2.11-11 · Log out confirmation (new) | rule note (Yes = FCM token, images, work cancel)
- Home shell #2 → M2.11-10 · Profile menu · roles & flavours (new) | Supervisor item greyed "hidden for ASHA" + rule
- Home shell #3 → M2.11-10 (new) rule + M2.11-21 · Incentives · month · Mitanin (title)
- Home shell #4 → M2.11-13 · Sync records · already running (new) | first-download banner + toast + rule
- Home shell #5 → M2.11-10 (rule: Home toolbar title is the entry point; Profile item "needs dev") + M2.11-29 · Change work location (new)
- Home shell #6 → M2.11-10 (avatar with camera, rule) + M2.11-02 rule
- Home shell #7 → M2.11-10 (Facility line, rule for the employee-ID fallback)
- Home shell #8 → M2.11-09 (edited) rule: external MS Form, URL per flavour, toast when no browser; in-app banner "needs dev"
- Home shell #9 → M2.11-09 (edited) "Support form · opens in browser" item + rule
- Home shell #10 → M2.11-09 / M2.11-10 / M2.11-05 rules (hidden in code, product decision); greyed item on M2.11-10
- Home shell #11 → skipped: see 2.19 (rooted device / emulator dialog)
- Home shell #12 → skipped: see 2.19 (account deactivated)
- Home shell #13 → skipped: see 2.19 (session expired)
- Home shell #14 → skipped: see 2.19 (in-app update)
- Home shell #15 → M2.11-40 · Badge celebration · as built (new) | auto-dismiss, Level only when > 1, M2.1-04 buttons "needs dev"
- Home shell #16 → M2.11-08 (edited) rule: Mitanin has English + Hindi only
- Home shell #17 → M2.11-10 rule + M2.11-08 rule ("Go to Home" icon on inner screens)
- Home shell #18 → M2.11-10 (bell with unread badge drawn) + rule
- Home shell #19 → M2.11-35 · Tray · incentive push (new) + M2.11-32 rules
- Home shell #20 → M2.11-41 · Start-up permissions (new) | rule
- Home shell #21 → M2.11-10 rule only (dormant feature behind a flag that is off; not drawn)

### ASHA profile
- #1 → M2.11-01 (edited) "Add ASHA head" button + M2.11-26 | rule
- #2 → M2.11-01 (edited) "My own family" household card (edit / members / add member) | rule: only the first household
- #3 → M2.11-28 · ASHA family · add member (new) | gender + relation, OK disabled, rule for 0 members
- #4 → M2.11-26 · My profile · first time (not saved) (new)
- #5 → M2.11-27 · My profile · loading (new) | rule: no separate fetch-failed state in code, confirm with dev
- #6 → M2.11-02 (edited) mobile, name, village, DOB/age and joining date are now read-only fields; editable ones listed + rule
- #7 → M2.11-02 / M2.11-26 rules (spinner, then a "Save Successful" or failure toast)

### Sync status quick sheet
- #1 → M2.11-12 · Sync status quick sheet (new) | table with Synced / Un-synced / Syncing
- #2 → M2.11-12 (new) | opens over an inner screen; rule for double-tap safety and the height cap

### Sync dashboard · Status tab
- #1 → M2.11-14 · Sync log · Status tab (new) | engine line + rule for Idle / Running / Complete
- #2 → M2.11-14 (new) | expanded failed-worker list incl. "Blocked by earlier failure in sync chain"
- #3 → M2.11-14 (new) | x / y records bar + per-form table

### Sync dashboard · Logs tab
- #1 → M2.11-07 (edited) Pause button + rule; Resume state on M2.11-15
- #2 → M2.11-07 (edited) "Clear logs" + rule (no confirmation)
- #3 → M2.11-15 · Sync log · empty & export (new)
- #4 → M2.11-15 (new) toast "No log files to export"
- #5 → M2.11-07 rule (auto-scroll only when already at the bottom)

### Incentives · month summary
- #1 → M2.11-03 (edited) month field + Submit; dialog M2.11-16 · Incentives · pick month (new)
- #2 → M2.11-17 · Incentives · claim button states (new) | states 1 and 5 + window rules (non-Mitanin and Mitanin)
- #3 → M2.11-17 (new) | Claim → Claiming… → Claimed; Rejected → Claim again
- #4 → M2.11-17 state 6 + M2.11-21 (new) | Mitanin month-level Reclaim
- #5 → M2.11-03 (edited) claim status pill
- #6 → M2.11-18 · Incentives · claim status dialog (new) | rules: verified view, pending hides date and reason, Mitanin Trainer
- #7 → M2.11-03 (edited) totals are now Pending (unpaid) / Processed (paid)
- #8 → M2.11-19 · Incentives · claim form PDF (new) + M2.11-20 · storage permission (new) | Mitanin / Chhattisgarh toast rule
- #9 → M2.11-03 (edited) collapsible group sections with subtotals
- #10 → M2.11-03 (edited) green-tick / red-cross eligibility icon, hidden on the fixed-monthly row + rule for sorting
- #11 → M2.11-03 (edited) S.No / Activity / No. of claims header + M2.11-21 (Amount + Doc on Mitanin)
- #12 → M2.11-03 rule + M2.11-21 rule

### Incentives · activity detail
- #1 → M2.11-04 (edited) records now show S.No, Ben ID, RCH ID, ABHA ID and amount
- #2 → M2.11-04 (edited) "Upload (n)" icon + M2.11-22 · Incentive docs · add file (new) | camera / gallery / PDF, max 4 files of 5 MB
- #3 → M2.11-23 · Incentive docs · view files (new) + M2.11-24 · delete file? (new)
- #4 → M2.11-04 (edited) per-record Submit / Submitted + M2.11-25 · submit confirm (new); the activity-level "Reclaim ₹600" is marked "needs dev"

### My badges
- No gaps

### My journey
- #1 → M2.11-39 · My journey · quiet day (new)
- #2 → M2.11-39 (new) "Closest to your next badge" (in progress) + rule

### Monthly recap playback
- #1 → M2.11-37 · Monthly recap · controls & gestures (new) | pause icon + rule (hidden on the goodbye scene)
- #2 → M2.11-37 (new) | music toggle icon + rule
- #3 → M2.11-37 (new) | tap zones drawn (left 1/3 back, right 2/3 next, hold = pause)
- #4 → M2.11-37 rule (progress saved per scene, Continue Recap); the Home strip belongs to another owner
- #5 → M2.11-38 · Monthly recap · loading (new) | rule: closes at once when there is no story
- #6 → M2.11-37 rule (time per scene, last scene never auto-advances)

### Learning hub
- #1 → M2.11-05 (edited) rule: a single PDF opened outside the app, "No PDF viewer found" toast; tabs are design-only
- #2 → M2.11-05 rule (not reachable, drawer item hidden)

### Video tutorial player (had no screen in the design)
- #1–#4 → M2.11-36 · Video tutorial player (new) | pause overlay, +10 s double-tap hint, seek bar + rules (auto-play, loop, hides after 3 s)

### Notifications panel
- #1 → M2.11-32 · Notifications · swipe, clear all, open (new) | row shown mid-swipe
- #2 → M2.11-32 "Clear all" + M2.11-33 · Notifications · clear all? (new)
- #3 → M2.11-34 · Notifications · empty & loading (new)
- #4 → M2.11-32 rules (tap destinations; "tap to fix" routing "needs dev")
- #5 → M2.11-34 (new) refresh bar + rule
- #6 → M2.11-32 rule only (role-coloured header in the Supervisor app; the Supervisor screens belong to the 2.12 owner)

### Push notifications in the tray
- #1 → M2.11-35 · Tray · incentive push (new)
- #2 → M2.11-35 rule (role-based destination)

### Choose work location
- #1 → M2.11-29 · Change work location (new) | State / District / Block read-only + rule saying M2.13-04's Block dropdown is wrong (M2.13-04 not edited, not my file)
- #2 → M2.11-29 rule (single village is auto-selected)
- #3 → M2.11-30 · Work location · missing village (new)
- #4 → M2.11-31 · Work location · exit app? (new) + M2.11-29 rule
- #5 → M2.11-29 rule (no user → Login)
- #6 → M2.11-29 rule + M2.11-10 rule

### Also corrected
- M2.11-03: the audit row and the old design both treated per-activity status tags as missing from the app. The code does show them for every flavour (IncentiveGroupedAdapter.kt:41-47: 101 Verified, 103 Rejected, 104 Overdue, otherwise Pending). The rule line now says so; only "Draft" is design-only.

### Mitanin
- HomeActivity.kt:172-180 + LanguageBottomSheet.kt:55-78 (English + Hindi only) → M2.11-08 rule "Mitanin: …"
- HomeActivity.kt:895-903 ("Monthly claim summary") → M2.11-10 menu label + rule; M2.11-21 title
- IncentiveClaimWindow.kt:41-62 (exactly last month, any day; FLW-1177 pilot, old 1-3 / 1-5 rule commented out) → M2.11-17 state 6 + rules; M2.11-21 rule
- IncentivesFragment.kt:259 (row tap ignores eligibility on Mitanin) → M2.11-03 rule, M2.11-21 rule
- IncentivesFragment.kt:326-330 ("Mitanin Trainer" label) → M2.11-18 rule, M2.11-21 rule
- IncentivesFragment.kt:414-422, 520-532 (month-level Reclaim with rejected IDs) → M2.11-21 (Reclaim bar) + M2.11-17 state 6
- IncentivesFragment.kt:621, 1485 + 311-314 (no PDF on Mitanin or Chhattisgarh, toast) → M2.11-19 rule, M2.11-21 rule
- IncentivesFragment.kt:642-646 (Amount + Doc headers) → M2.11-21 table with the Amount and Doc columns
- rv_item_incentive_grouped.xml:76 (eligibility icon on every row) → M2.11-21 rule
- rv_item_incentive.xml:114, 170 (upload / view / submit hidden) → M2.11-42 · Incentives · activity detail · Mitanin (new) + M2.11-04 rule
- Checked with no Mitanin difference in my scope: AshaProfile, sync sheet and dashboard, notifications, LMS / video, recap, journey, ServiceLocation, FBMessaging (the supervisor-side incentive dashboard refs belong to the 2.12 owner)

---

## Coverage B (maternal) · stage 2.2 · design/figma-plugin/src/32-stage-2.2-maternal.js

Frames edited in place: 02, 03, 04 (rules only), 05, 06, 07, 08, 09, 10 (rule only), 12, 13, 14, 15, 16 (rules only). Grid re-laid to 6 columns (positions only; no renames).
New frames: M2.2-17 … M2.2-46 (30 new).

### Cross-cutting
- C1 voice search → searchRow (mic via ensureSearchMic) on 02, 08, 12, 15, 17, 18, 20, 37, 41, 42, 46 (edited/new) | rule on 18
- C2 sort sheet → M2.2-18 · Maternal lists · sort sheet (new) + "Sorted:" label on 02, 08, 20, 41, 46 | rule on 18
- C3 supervisor read-only → M2.2-42 · Supervisor · maternal lists (read-only) (new) | rules on 02, 08, 20, 37, 15
- C4 edit after save → M2.2-19 · Saved records · view mode & edit rules (new) | rules on 03, 05, 06, 07, 09, 13, 14. M2.0-P5 text (stage 2.0) not edited: not my file, noted in rule on 19
- C5 Mitanin hides uploads → M2.2-43/44/45 (new) + rule lines on 05, 09, 13, 14, 26
- C6 upload chooser + viewer → M2.2-26 · Upload chooser (new), M2.2-27 · Uploaded file viewer (new)
- C7 Register red / View green → 02 (edited), 37, 41, 15 (edited, follow-up) via tint
- C8 not followed up warning → M2.2-20, M2.2-23 (warning icon on card) | rules on 08, 12, 20

### Mother care hub
- no gaps

### Pregnancy registration list (M2.2-02)
- #1 RCH filter → M2.2-17 · Registration list · RCH filter (new) | tabs marked "Not in app today – needs dev" on 02
- #2 Register → View, sync after registration → M2.2-02 (edited) | rule
- #3 hidden for supervisor → M2.2-42 (new) | rule on 02

### Pregnancy registration form (M2.2-03)
- #1 prefill → M2.2-03 (edited: prefill banner, LMP label) | rule
- #2 saving writes HRP record → rule on 03
- #3 Mitanin Rh auto → M2.2-03 (edited: auto field) | rule
- #4 edit FAB → M2.2-19 (new) | rule
- #5 after save returns to list → rule on 03

### ANC visits list
- #1 Delivered switch + dialog + date picker → M2.2-20 (new), M2.2-21 (new), M2.2-22 (new)
- #2 Add ANC disabled → M2.2-20 (new) | rule
- #3 ANC visits button only with ≥1 visit → M2.2-20 (Pooja card) | rule
- #4 Mitanin home-visit buttons + limits → M2.2-20, M2.2-46 (new) | rule on 04 (edited)
- #5 list variants by entry point → M2.2-23 (new) | rule
- #6 expired pregnancies auto-closed → rule on 20
- #7 last visit, call, permission toast → M2.2-20 + M2.2-23 (toast)
- C1/C2/C3/C8 → see cross-cutting

### ANC visits history sheet
- #1 view / edit last → M2.2-24 (new)
- #2 PMSMA mode → M2.2-33 (new)
- #3 sync icon, View hidden for supervisor → M2.2-24 (status tags) | rule

### ANC home visits sheet (NO SCREEN)
- #1 list, last editable → M2.2-29 (new)
- #2 empty state → M2.2-30 (new)

### ANC visit form (M2.2-05)
- #1 delivered from 23 wks → M2.2-05 (edited label) + M2.2-25 caption | rule
- #2 ≤12 wks folic acid / no fundal height → M2.2-25 (new)
- #3 visit-number & date limits → M2.2-05 (edited captions), M2.2-25
- #4 Td/TT shared, Td-2 hidden on ANC 1 → M2.2-05 (section meta), M2.2-25 | rule
- #5 PMSMA place locked → M2.2-25 (new)
- #6 Xushrukha Hb/BP required → M2.2-05 (asterisks removed) | rule
- #7 abortion = Yes side effects → rule on 05
- #8 maternal death → MDSR → rule on 05 + 16; M2.2-28 (new); 16 dialog marked "Not in app today – needs dev"
- #9 edit only latest → M2.2-19 | rule
- C5/C6 → M2.2-43, M2.2-26, M2.2-27

### ANC home visit / counselling (M2.2-06)
- #1 Mitanin-only → M2.2-06 (edited subtitle) | rule
- #2 server-driven question set → M2.2-06 (edited: real danger-sign questions) | rule; old checklist kept, marked design-only
- #3 header summary card → M2.2-06 (edited infoCard)
- #4 age risk auto, visit no/day/due locked → M2.2-06 (edited)
- #5 history carried & locked, twin/sickle editable → M2.2-06 (edited)
- #6 danger sign → refer dialog → M2.2-31 (new)
- #7 continue to ANC dialog + toast → M2.2-32 (new)
- #8 date rules / Visit N → rule on 06
- #9 view mode/edit last, image field → M2.2-06 (photo tile) + M2.2-19

### PMSMA form (M2.2-07)
- #1 high-risk alert → M2.2-35 (new)
- #2 prefill ANC count + last PMSMA, visit no → M2.2-07 (edited infoCard) | rule
- #3 edit only latest → rule on 07 + M2.2-19
- #4 consent dialog (dormant) → M2.2-36 (new)

### PMSMA visits sheet (NO SCREEN, dormant)
- #1 → M2.2-34 (new)

### PMSMA / high-risk list (M2.2-08)
- #1 PMSMA button → ANC form with place locked → rule on 08 + M2.2-25
- #2 PMSMA visits button → M2.2-08 (edited card) + M2.2-33
- #3 Delivered switch → M2.2-08 (edited card) | rule → 21/22
- #4 Mitanin home-visit buttons → M2.2-08 (edited card) | rule
- #5 single list, call → rule; tabs + Refer marked "Not in app today – needs dev"

### Delivery outcome list (NO SCREEN)
- #1 → M2.2-37 (new)
- #2 supervisor → M2.2-42 (new) | rule on 37

### Delivery outcome form (M2.2-09)
- #1 complication Death → MDSR → M2.2-28 (new) + caption on 09 | rule
- #2 EC child counts → rule on 09
- #3 view only → M2.2-19 | rule
- #4 JSY voucher / MCP uploads Mitanin → M2.2-09 (edited label), M2.2-44 (new)
- #5 defaults → M2.2-09 (edited label) | rule
- #6 next-steps sheet design-only → rule on 10 "Not in app today – needs dev"

### PNC mothers list (M2.2-12)
- #1 Add PNC only when due → M2.2-12 (edited action label) | rule
- #2 View visits only ≥1 → rule + M2.2-38
- #3 non-follow-up variant → rule on 12
- C1/C2/C8 → searchRow on 12, rules

### PNC visits sheet
- #1 → M2.2-38 (new) + caption on 12 sheet

### PNC visit form (M2.2-13)
- #1 mother death → MDSR → M2.2-13 (edited reveal) | rule
- #2 sterilisation carried → M2.2-13 (caption) | rule
- #3 status change after PNC → rule
- #4 incentive reminder popup → M2.2-39 (new)
- #5 upload on submit dialog → M2.2-40 (new)
- #6 discharge summary photos → M2.2-13 (edited uploads block)
- #7 no edit → M2.2-19 | rule

### Abortion list (NO SCREEN)
- #1 → M2.2-41 (new)

### Abortion / CAC form (M2.2-14)
- #1 sterilisation = Yes side effects → M2.2-14 (edited reveal) | rule
- #2 discharge summary photos → M2.2-14 (edited uploads) | rule
- #3 edit FAB → M2.2-19 | rule
- #4 title "CAC form" → M2.2-14 (edited title)

### HWC referral list (M2.2-15)
- #1 only Maternal + General check-up → rule on 15 (M2.8-14 mismatch noted; not my file)
- #2 follow-up only on GC rows → M2.2-15 (edited: maternal rows no button)
- #3 red Start / green View follow-up → M2.2-15 (edited)
- #4 deceased greyed → M2.2-15 (edited)
- #5 Referred from + supervisor → M2.2-15 (edited) + M2.2-42

### Other agents
- Infant registration list #1–#2, Infant registration form #1–#3 (M2.2-11 left unchanged) → other agent (stage 2.4)
- Child registration list #1–#2, Child registration form #1–#3 → other agent (stage 2.4)
- HBNC schedule #1–#4, HBYC schedule #1–#2, HBNC form #1–#5, HBYC form #1–#2 → other agent (stage 2.4)
- High-risk cases hub (no gaps) → other agent (stage 2.6)

Totals: 76 maternal gap rows (68 screen rows + 8 cross-cutting) covered, 0 skipped. 28 rows marked other agent.

### Mitanin
- PregnantWomanRegistrationDataset.kt:128,137,1009 negative blood group → Rh negative = Yes → M2.2-03 auto field + rule
- PregnantWomanAncVisitDataset.kt:475 MCP headline + front/back uploads removed → M2.2-43 · ANC visit form · Mitanin (new) + rule on 05
- PwAncFormFragment.kt:189 upload tap ignored → rule on 43, 26
- AncVisitListAdapter.kt:136-148 (+142) home-visit buttons Mitanin only → M2.2-46 · ANC visits list · Mitanin (new), Meena card on 20, card on 08; ANC home visit form 06 + sheets 29/30 + dialogs 31/32 all marked Mitanin-only
- DeliveryOutcomeDataset.kt:264,295 MCP uploads removed (new + saved) → M2.2-44 · Delivery outcome form · Mitanin (new)
- DeliveryOutcomeDataset.kt:466 JSY = Yes no voucher reveal → M2.2-44 + rule on 09
- PncFormDataset.kt:276 discharge summaries removed → M2.2-45 · PNC visit form · Mitanin (new)
- PncFormDataset.kt:634 no incentive reminder → M2.2-45 rule + rule on 39
- PncFormFragment.kt:102,160 upload tap ignored, no submit upload prompt → M2.2-45 rule + rule on 40
- PregnantWomanAncAbortionDataset.kt:189 + PwAncAbortionFormFragment.kt:97 discharge summary removed → rule "Mitanin: …" on 14 (small difference)
- IconDataset.kt:53,324,358,411 → VLF only, no maternal hub difference (hub 01 unchanged)
- MDSR / NCD referred / infant / HBNC / HBYC Mitanin refs → out of scope (other stages)

---

## Coverage C: Eligible couple, Child care / infant, Immunization (+ infant / HBNC / HBYC / infant-reg / child-reg rows from gap_B)

Files: design/figma-plugin/src/33-stage-2.3-ec.js, 34-stage-2.4-child.js, 35-stage-2.5-immunization.js
Every frame with a hidden rule has a "RULES (from code)" note via G.rules.

### Cross-cutting (gap_C header, gap_B C1/C2/C6/C7)
- Voice search → all edited/new lists use G.searchRow; all three stages call ensureSearchMic, so the mic is on every search bar (including old ones) | rule note in stage NOTES
- Deceased marker → M2.4-02, M2.4-14, M2.5-02, M2.5-10 (actionCard state 'deceased')
- B C2 Sort sheet (infant reg / child reg lists) → M2.3-10 (shared EC sort sheet) + sort label on M2.4-27, M2.4-29
- B C6 Upload chooser camera/gallery/PDF → M2.3-17, M2.4-12 (uploads with PDF), M2.4-28 rule
- B C7 Register red / View green → M2.4-27, M2.4-29, M2.3-02, M2.4-10
- Design-ahead-of-code items (Save draft, LBW/SNCU chips, CUFY chips, EC chips, EC hub stats, Share list banner, Rota refusal banner, Call 108, filter Status radio) → kept, each marked "Not in app today – needs dev" in the rules of 01/02/03/04/06 (2.3), 01/02/04/05/07/08/10/11/12 (2.4), 01/05 (2.5). Multi-select filter + Status radio removed from M2.5-03 because code differs for all flavours

### EC hub
- no gaps → M2.3-01 (edited: rule for stat tiles/banner not in app)

### EC sort sheet
- #1 → M2.3-10 · EC sort sheet (new)
- #2 → M2.3-02, M2.3-04, M2.3-11, M2.3-14 (edited/new, "Sorted: Newest first" label)

### EC registration list (M2.3-02)
- #1 IFA button → M2.3-02 (edited) | rule age 20–49 + status 1/2
- #2 Register/View → M2.3-02 (edited)
- #3 Missed period icon/status → M2.3-02 (edited)
- #4 Sync only after record → M2.3-02 (edited) | rule
- #5 Child count → M2.3-02 (edited, value row)
- #6 Missed period mode → M2.3-11 (new)
- #7 Sort → M2.3-10 (new) + label on M2.3-02

### EC registration form (M2.3-03)
- #1 View mode + Edit → M2.3-12 (new)
- #2 Children pre-filled read-only → M2.3-03 (edited: lock icons, auto counts, Add child removed)
- #3 Kit only with no live children → M2.3-13 (new) + rule on M2.3-03 (kit section removed from the 2-children example)
- #4 Incentive pop-up → M2.3-13 (new, dialog)
- #5 Kit photo hidden on Mitanin → M2.3-13 rule + M2.3-18 (new)
- #6 Camera/Gallery/PDF, too-large toast, view file → M2.3-17 (new)
- #7 High-risk hidden on Mitanin, red header → M2.3-03 rule + M2.3-18 (red header in rule only: kit section-header has no red variant)
- #8 Silent HRP side effect → M2.3-03 (edited: warning banner marked "not in app today") + M2.3-12
- #9 Bank details Saksham only → M2.3-03 (edited: "Saksham only" tag + rule)
- #10 RCH/Aadhaar write-back → M2.3-03 (edited: fields + rule)
- #11 Header strip → M2.3-03, M2.3-12 (person card)
- #12 After save → M2.3-03 rule

### EC tracking list (M2.3-04)
- #1 Month already filled toast → M2.3-04 (edited: disabled Add visit + toast)
- #2 View visits / Add visit → M2.3-04 (edited)
- #3 Missed period → M2.3-04 (edited)
- #4 No follow-up icon → M2.3-04 (edited)
- #5 Home modes → M2.3-14 (new) + rule on M2.3-11/M2.3-14
- #6 Supervisor → M2.3-15 (new)
- #7 Sort → M2.3-10
- #8 Children + all-synced icon → M2.3-04 (edited) | rule

### EC visit history sheet (M2.3-05)
- #1 Per-visit row with sync + VIEW → M2.3-05 (edited) | month grid flagged "not in app"

### EC tracking form
- #1 ANTRA history table → M2.3-06 (edited)
- #2 Locked dose number → M2.3-06 (edited)
- #3 Due window → M2.3-06 (edited)
- #4 Visit date limits → M2.3-06 (edited caption + rule)
- #5 Permanently sterilised re-tag → M2.3-16 (new)
- #6 Discharge uploads (non-Mitanin) → M2.3-16 (new) + M2.3-09 rule
- #7 MPA hidden on Mitanin → M2.3-09 (edited) + M2.3-06 rule
- #8 Upload chooser / toasts / view → M2.3-17 (new)
- #9 HRP answers copied to pregnant HRP → M2.3-07 (edited rule)
- #10 Success toast + back + sync → M2.3-06 (edited snack + rule)
- #11 View mode Submit disabled → M2.3-08 (edited: disabled bar + rule)

### Immunization hub (M2.5-01)
- #1 Mother tile unreachable → M2.5-01 rule (tile kept; PO to decide)

### Child immunization stages
- dead code → skipped: no inbound navigation

### Child immunization list (M2.5-02)
- #1 Quick-filter cards → M2.5-02 (edited, tabs replaced)
- #2 Result summary → M2.5-02 (edited)
- #3 Filtered vs plain empty → M2.5-07 (new)
- #4 8 stages strip → M2.5-02 (edited) + M2.5-03
- #5 Due only mode → M2.5-08 (new)
- #6 Scope 6 years → M2.5-02 rule
- #7 Card RCH IDs / DOB / Show vaccines → M2.5-02 (edited)
- #8 Deceased → M2.5-02 (edited)

### Dose-stage filter sheet (M2.5-03)
- #1 Single select applies on tap, close icon → M2.5-03 (edited)

### Vaccine sheet
- #1 Missed state → M2.5-04 (edited), M2.5-09 (new)
- #2 View on given → M2.5-09, M2.5-12 (new)
- #3 Deceased disables Fill → M2.5-10 (new)
- #4 Due date on every dose → M2.5-04 (edited), M2.5-09
- #5 Next-month doses fillable → M2.5-04 (edited 14-week stage + rule)
- #6 Bottom sheet layout → M2.5-09 (new, code layout) + M2.5-04 rule

### Record vaccines form (M2.5-05)
- #1 Select all → M2.5-05 (edited)
- #2 Submit hidden until one ticked → M2.5-05 rule
- #3 MCP incentive pop-up → M2.5-11 (new)
- #4 MCP hidden on Mitanin → M2.5-13 (new)
- #5 Missed toast → M2.5-09 (new, snackbar)
- #6 Debug saves missed → M2.5-05 rule
- #7 Saving spinner/toasts → M2.5-05 rule
- #8 Read-only existing record → M2.5-12 (new)
- #9 Two MCP photos, size, view → M2.5-05 (edited) + rule

### Mother immunization list / sheet (M2.5-06)
- list #1 unreachable → M2.5-06 rule
- list #2 dependent-dose rule → M2.5-06 rule + sheet Td booster "missed"
- sheet #1 Fill opens full form, View, missed hides → M2.5-06 (edited: inline date + Save removed)

### Child care hub
- no gaps → M2.4-01 (rule: stat tiles and Child registration tile not in app)

### Newborn list (M2.4-02)
- #1 Search + voice → M2.4-02 (edited)
- #2 RCH filter dialog → M2.4-13 (new)
- #3 Overdue > 42 days → M2.4-02 (edited)
- #4 HBNC button → M2.4-02 (edited)
- #5 Sync hidden → M2.4-02 (edited, actionCards without sync) | rule

### Legacy HBNC day list / card / Part I / II / visit
- skipped: dead code / unreachable; noted in M2.4-03 rules

### Child list HBYC (M2.4-06)
- #1 Search → M2.4-06 (edited)
- #2 RCH filter → M2.4-13 (new)
- #3 HBYC → month grid → M2.4-15 (new)

### Legacy HBYC month list
- #1 → covered by M2.4-15 (live ChildMonthListFragment concept)

### Legacy HBYC form
- #1 read-only once filled → M2.4-07 rule (legacy screen skipped as unreachable)

### Children under 5 list (M2.4-08)
- #1 Search → M2.4-08 (edited), M2.4-19
- #2 Three buttons → M2.4-08 (edited card), M2.4-19 (new)
- #3 SAM status labels → M2.4-19 (new)
- #4 scope → no gap (rule on M2.4-08)

### CUFY visit sheet
- #1 Visit 1…n + Add → M2.4-20 (new)
- #2 SAM add hidden in NRC pathway → M2.4-20 (new)
- #3 IFA non-Mitanin latest only with Add+View → M2.4-21 (new)
- #4 IFA Mitanin rows → M2.4-22 (new)

### CUFY form (M2.4-09)
- #1 View mode, Edit only SAM, date locked → M2.4-23 (new)
- #2 SAM follow-up dates → M2.4-23 (new)
- #3 IFA history table (Mitanin variant) → M2.4-24 (new), M2.4-30 (new Mitanin)
- #4 IFA date window → M2.4-24 (new)
- #5 One IFA per month → M2.4-24 (new)
- #6 SAM date unique/later → M2.4-09 rule
- #7 Photo dialog → M2.4-09 (edited: photo tile + rule)
- #8 Child info card → M2.4-09 (edited: person card)
- #9 Per-form sync → M2.4-09 rule
- #10 SAM alert dialog → M2.4-25 (new)

### Adolescent list (M2.4-10)
- #1 Girls only form → M2.4-10 (edited: boy card without button)
- #2 Register/View → M2.4-10 (edited)
- #3 Consent icon (not Mitanin) → M2.4-10 (edited)
- #4 Sync only once record → M2.4-10 (edited)
- #5 Relation label → M2.4-10 (edited meta D/o, S/o) + rule

### Adolescent form (M2.4-11)
- #1 One record, view + Edit → M2.4-26 (new)
- #2 Toasts → M2.4-11 rule

### Adolescent shell
- no gaps → skipped

### gap_B · Infant registration list
- #1 One card per baby, twins → M2.4-27 (new)
- #2 LBW variant → M2.4-27 rule
- C1/C2/C7 → M2.4-27

### gap_B · Infant registration form (M2.2-11 is in stage 2.2, not owned)
- #1 Per baby index → M2.4-28 (new)
- #2 SNCU = Yes → 4 uploads (non-Mitanin) → M2.4-28 (new)
- #3 View only after save → M2.4-28 rule

### gap_B · Child registration list
- #1 Register/View list → M2.4-29 (new)
- #2 View opens member form → M2.4-29 rule

### gap_B · Child registration form (M2.4-12)
- #1 Creates HH member + EC child slot → M2.4-12 rule
- #2 PDF birth certificate → M2.4-12 (edited, uploads)
- #3 View only → M2.4-12 rule

### gap_B · HBNC schedule (M2.4-03)
- #1 Mandatory / Optional → M2.4-03 (edited)
- #2 Unlock order → M2.4-03 rule
- #3 Baby dead disables → M2.4-14 (new)
- #4 Visit date, RCH header, sync → M2.4-03 (edited)

### gap_B · HBYC schedule
- #1 Month cards 3–15 → M2.4-15 (new)
- #2 Child dead disables → M2.4-15 caption + rule

### gap_B · HBNC form (M2.4-04 / 05)
- #1 Mitanin danger dialog + referral → M2.4-16 (new) + M2.4-05 (edited: reason, rules; Call 108 flagged)
- #2 Infant death → CDR → M2.4-17 (new)
- #3 SNCU discharged auto Yes → M2.4-04 (edited)
- #4 Visit-date checks → M2.4-04 (edited date field + rule)
- #5 Image field / read-only done day → M2.4-04 (edited photo + rule)

### gap_B · HBYC form (M2.4-07)
- #1 Mitanin danger → refer → M2.4-18 (new)
- #2 Date validation, image, read-only done month → M2.4-07 (edited)

### Mitanin
- EligibleCoupleRegistrationDataset.kt:1461 (HRP sections removed) → M2.3-18 (new) + M2.3-03 rule
- EligibleCoupleRegistrationDataset.kt:1576, 1799-1810; EligibleCoupleRegFragment.kt:172 (kit photo removed) → M2.3-18, M2.3-13 rule
- EligibleCoupleRegFragment.kt:244 (no incentive pop-up) → M2.3-18, M2.3-13 rule
- EligibleCoupleRegistrationDataset.kt:1186, 1433 → commented out, no effect (skipped)
- EligibleCoupleTrackingDataset.kt:118-131, 295-319, 425-597, 719-745, 809 (split FP question "using" / "want to use") → M2.3-09 (existing Mitanin frame)
- EligibleCoupleTrackingDataset.kt:347, 676 (discharge uploads hidden) → M2.3-09 rule, M2.3-16 rule
- EligibleCoupleTrackingDataset.kt:359, 638; EligibleCoupleTrackingFormFragment.kt:126 (MPA card hidden) → M2.3-09 rule + caption, M2.3-06 rule
- InfantRegistrationDataset.kt:325, 376; InfantRegFragment.kt:97 (SNCU uploads removed) → M2.4-28 rule
- HBNCFormFragment.kt:61, 201 (danger → refer, Mitanin only) → M2.4-16, M2.4-05 rule
- HBYCFormFragment.kt:55, 172 (danger → refer, Mitanin only) → M2.4-18
- CUFYBottomSheetFragment.kt:35-36, 101 (IFA rows as visits) → M2.4-22
- CUFYFormFragment.kt:79-80, 264-290 (IFA table = visit number + date) → M2.4-30 (new)
- CUFYFormFragment.kt:383 → commented out (skipped)
- CUFYFormViewModel.kt:67-72, 389-400 (bottle count hidden, still sent) → M2.4-30 rule
- CUFYFormRepository.kt:126 (visit numbering order) → M2.4-30 rule
- rv_item_adolscent_health_list.xml:349 (consent icon hidden) → M2.4-10 rule
- ImmunizationFormFragment.kt:121 (no MCP reminder) → M2.5-13 (new), M2.5-11 rule
- ImmunizationFormFragment.kt:175; ImmunizationDataset.kt:139-142 (MCP fields removed) → M2.5-13 (new)
- ImmunizationRepo.kt:303-311 (PCV-1/2/Booster Mitanin only) → M2.5-14 (new); M2.5-04 default card corrected (PCV removed, wrong for all non-Mitanin flavours)
- IconDataset.kt:53, 324, 358, 411 (VLF tiles) → out of my scope (village stage)

---

## Coverage D · Disease control (design/figma-plugin/src/38-stage-2.8-disease.js)

Disease control hub: no gaps (M2.8-01 untouched)

Communicable grid #1 → M2.8-17 · TB hub (new) + rule on M2.8-02 (edited)
Communicable grid #2 → M2.8-02 (edited) | rule note "code bug: all tiles show TB-screening count"
TB hub #1 → M2.8-17 · TB hub (Communicable → TB) (new)
Malaria sub-hub #1 → M2.8-18 · Malaria sub-hub (new)
Leprosy sub-hub #1 → M2.8-19 · Leprosy sub-hub (new)
Disease household list #1 → M2.8-20 (new) | rule (title/icon per disease)
Disease household list #2 → M2.8-20 (new) search row + rule
Disease household list #3 → M2.8-20 (new) searchRow mic (kit) + rule
Disease household list #4 → M2.8-20 (new) | rule
Disease household list #5 → M2.8-20 (new) deleted card "Deleted Record"
Disease household list #6 → M2.8-20 (new) "N members" button + rule
Disease household list #7 → M2.8-21 · Disease household list · Filaria (Add MDA) (new)
Disease household list #8 → M2.8-20 (new) | rule
Disease household list #9 → M2.8-20 (new) delete icon drawn | rule "code bug: tap does nothing – intended hide"
Disease household list #10 → M2.8-22 · Disease lists · empty state (new)
Malaria members #1 → M2.8-23 · Malaria household members (new) coloured status buttons
Malaria members #2 → M2.8-23 (new) status icons + rule
Malaria members #3 → M2.8-23 (new) dead card w/o button + rule
Malaria members #4 → M2.8-23/24 (new) IRS + net switches, one-at-a-time rule
Malaria members #5 → M2.8-24 · Malaria · IRS round (new)
Malaria members #6 → M2.8-25 · Malaria · IRS limit (4 rounds) (new)
Malaria members #7 → M2.8-24 (new) history table
Malaria members #8 → M2.8-24 (new) toast "IRS submitted"
Malaria members #9 → M2.8-26 · Malaria · Mosquito net form (new)
Malaria members #10 → M2.8-26 (upload + 5 MB error) + M2.8-27 · Mosquito net · photo source (new)
Malaria members #11 → M2.8-26 (new) date error helper + rule
Malaria members #12 → M2.8-26 (new) history table
Malaria members #13 → M2.8-26 | rule (toasts)
Malaria members #14 → M2.8-22 (new) shared empty state
AES/JE members #1 → M2.8-28 · AES/JE household members (new)
AES/JE members #2 → M2.8-28 (new) dead card + rule
AES/JE members #3 → M2.8-28 (new) pig-herd question drawn | rule "code bug: never shows, not saved – drop or fix"
Kala-azar members #1 → M2.8-29 · Kala-azar household members (new)
Kala-azar members #2 → M2.8-29 (new) dead card + rule
Filaria members #1 → M2.8-30 · Filaria household members (new)
Filaria members #2 → M2.8-30 (new) dead card + rule
Leprosy members #1 → M2.8-31 · Leprosy household members (new)
Leprosy members #2 → M2.8-31 (new) "Visits" button + rule
Leprosy members #3 → M2.8-32 · Leprosy · earlier visits sheet (new)
Leprosy members #4 → M2.8-31 (new) dead card + rule
Leprosy suspected list #1 → M2.8-33 · Leprosy suspected list (new)
Leprosy suspected list #2 → M2.8-33 rule (no search) + M2.8-22 empty state
Leprosy confirmed list #1 → M2.8-34 · Leprosy confirmed list (new)
Malaria confirmed list #1 → M2.8-35 · Malaria confirmed list (new)
Malaria confirmed list #2 → M2.8-35 (new) Follow up / Day 2 buttons
Malaria confirmed list #3 → M2.8-35 (new) dead card w/o button
Malaria screening #1 → M2.8-03 (edited: "Visit no. 2" in header) + rule; M2.8-36 visits table
Malaria screening #2 → M2.8-36 · Malaria screening · view + earlier visits (new)
Malaria screening #3 → M2.8-36 (new) view mode + Edit FAB + rule (FAB condition)
Malaria screening #4 → M2.8-03 (edited: case status auto/locked field) | rule (calc + test type only when Suspected)
Malaria screening #5 → M2.8-03 (edited: 90-day helper text) | rule
Malaria screening #6 → M2.8-37 · Status Died → marks dead everywhere (new) | rule (warning banner = needs dev)
Malaria screening #7 → already designed
Malaria screening #8 → M2.8-03 | rule "Save draft not in app today – needs dev"
Malaria treatment #1 → M2.8-04 (edited: Pf only, parasite type auto) + M2.8-38 · Malaria treatment · Pv + view mode (new) + M2.8-51 · Malaria treatment · Pv · Mitanin (new)
Malaria treatment #2 → M2.8-04 (edited: auto completion date) | rule
Malaria treatment #3 → M2.8-04 (edited: Day N dropdown + Submit) + M2.8-38 view mode/Edit FAB | rule
Kala-azar form #1 → M2.8-39 · Kala-azar / AES / Leprosy · view mode (new) + rule on M2.8-08 (edited)
Kala-azar form #2 → M2.8-37 (new) + rule on M2.8-08
Kala-azar form #3 → M2.8-08 (edited) | rule (90 days)
AES/JE form #1 → M2.8-39 (new) + rule on M2.8-08
AES/JE form #2 → M2.8-37 (new) + rule on M2.8-08
AES/JE form #3 → M2.8-08 (edited) | rule
Filaria form #1 → M2.8-42 · Filaria · view mode + child under 2 (new) + rule on M2.8-07 (edited)
Filaria form #2 → M2.8-42 (new) locked DEC field + rule on M2.8-07
Filaria form #3 → M2.8-42 (new) locked Other side effect + rule
Filaria MDA #1 → M2.8-40 · Filaria MDA (household) (new) + rule on M2.8-07
Filaria MDA #2 → M2.8-41 · Filaria MDA · already done this month (new)
Filaria MDA #3 → M2.8-40 (new) date helper + rule
Filaria MDA #4 → M2.8-40 (new) MDA history table
Filaria MDA #5 → M2.8-40 (new) upload block (+ picker M2.8-27)
Filaria MDA #6 → M2.8-40 | rule (toasts)
Leprosy screening #1 → M2.8-39 (new) + rule on M2.8-05 (edited)
Leprosy screening #2 → M2.8-05 (edited: "Visit 1" in header) | rule
Leprosy screening #3 → already designed (rule line added on M2.8-05)
Leprosy screening #4 → M2.8-37 (new) | rule on M2.8-05
Leprosy screening #5 → M2.8-05 (edited: helper text 1 year / 90 days) | rule
Suspected leprosy form #1 → M2.8-43 · Suspected leprosy form (new)
Suspected leprosy form #2 → M2.8-43 (new) banner + rule
Suspected leprosy form #3 → M2.8-43 (new) helper + rule
Suspected leprosy form #4 → M2.8-43 | rule
Leprosy confirmed MDT #1 → M2.8-06 (edited: earlier follow-ups table)
Leprosy confirmed MDT #2 → M2.8-06 (edited) | rule
Leprosy confirmed MDT #3 → M2.8-06 (edited: "locked once set" label) | rule
Leprosy confirmed MDT #4 → M2.8-06 (edited: follow-up date helper) + M2.8-45 (end date when Completed) | rule (toast errors)
Leprosy confirmed MDT #5 → M2.8-45 · Leprosy follow-up · treatment completed (new) toast
Leprosy confirmed MDT #6 → M2.8-06 | rule (toasts)
Leprosy past visit #1 → M2.8-44 · Leprosy past visit (read-only) (new) | rule "code bug: fields render enabled"
GC list #1 → M2.8-09 (edited: searchRow with mic) | rule
GC list #2 → M2.8-09 | rule
GC list #3 → already designed (rule line added)
GC list #4 → M2.8-22 (new) + rule on M2.8-09
Add check-up #1 → M2.8-46 · Add check-up · pointer links (new)
Add check-up #2 → M2.8-47 · Add check-up · loading / form not available (new)
Add check-up #3 → M2.8-47 (new) spinner
Add check-up #4 → M2.8-48 · Add check-up · conflict, Other, errors (new) toast
Add check-up #5 → M2.8-48 (new) "Name the problem *" error field
Add check-up #6 → M2.8-48 (new) field error + rule; rule on M2.8-10 (edited)
Add check-up #7 → M2.8-12 (edited) | rule (Cataract → All Beneficiaries)
Add check-up #8 → M2.8-49 · Add check-up · already referred dialog (new) + M2.8-12 note text fixed
Add check-up #9 → M2.8-50 · Add check-up · could not save (new) + rule on M2.8-13 (edited)
Add check-up #10 → already designed (rule line on M2.8-10)
General OP care #1 → M2.8-16 (edited: call icon on each card) | rule (permission toast)
General OP care #2 → M2.8-16 (edited: referral detail rows on cards)
General OP care #3 → M2.8-16 | rule (sort)
General OP care #4 → M2.8-16 (edited: searchRow mic) | rule
General OP care #5 → M2.8-22 (new) + rule on M2.8-16
General OP care #6 → M2.8-16 | rule "View button not in app today – needs dev (confirm)"

Extra: M2.8-11 rule "Call 108 not in app today – needs dev".
Skipped: none.

### Mitanin
- MalariaConfirmCasesDataset.kt:54-56 (daysPvMitanin, Day 1–14 vs Day 1–4) → M2.8-51 · Malaria treatment · Pv · Mitanin (new) + "Mitanin:" rule lines on M2.8-04 and M2.8-38
- Found while checking it: on Mitanin, Pv completion date is still auto start + 4 days (MalariaConfirmCasesDataset.kt:209-230), so it ends before day 14 → rule note on M2.8-51 "code bug, intended start + 14"
- IconDataset.kt:557-622 (disease control hub + communicable grid + sub-hubs 531-553, 782-840): no flavour checks → rule "Mitanin: same 6 tiles" on M2.8-02
- general_checkup/*, Malaria/Leprosy/Filaria/KalaAzar/AESJE/IRS datasets, disease_control/*, general_op_care/*: no Mitanin checks → rule "Mitanin: same form" on M2.8-10. Nothing else to draw
- Out of scope, noted for the 2.9 owner: IconDataset.kt:358 hides a VLF tile on Mitanin (string saas_samelan, but it navigates to FilariaMdaCampaignHistoryFragment); IconDataset.kt:324 PHC review → "Cluster review"

---

## Coverage E: NCD / TB / CHO / HRP (gap_E_ncd_cho.md + hrp_cases in gap_B_maternal.md)

Files: design/figma-plugin/src/36-stage-2.6-hrp-cho.js, 37-stage-2.7-ncd-tb.js

### Cross-cutting
- X1 voice search → ensureSearchMic(ctx) called in both stages (mic on every Search bar instance); G.searchRow on all new/edited lists; rule line on M2.6-02 (edited) + note
- X2 deceased card → M2.7-10 (edited), M2.7-11 (edited), M2.7-24, M2.7-25, M2.7-27 (new) state:'deceased' "Death" tag, no actions | rules
- X3 supervisor read-only → M2.6-19 (new, HRP women), M2.7-12 (new, NCD screened), rules on M2.7-13/24/27/29
- X4 3-state button colour → local stateBtn (red/orange/green) on M2.6-02, -09, -14, -16; M2.7-02, -10, -11, -24, -25, -27, -29 | rules

### NCD / CBAC
- #1 → M2.7-12 · Supervisor · NCD screened list (new)
- #2 → M2.7-12 (new) | rule: call toast; ABHA handler empty (code bug)
- #3 → M2.7-02 (edited) chips All/Screened/Not screened
- #4 → M2.7-02 (edited) age-band dropdown + rule
- #5 → M2.7-02 (edited) disabled Add CBAC + toast caption + rule
- #6 → M2.7-02 (edited) View CBAC button + rule
- #7 → M2.7-02 (edited) sync badge on every card + rule
- #8 → M2.7-02 (edited) subtitle + rule (not pregnant, not dead…)
- #9 → M2.7-14 · CBAC history sheet (new)
- #10 → M2.7-15 · CBAC · view mode (new)
- #11 → M2.7-15 (new)
- #12 → M2.7-03 (edited) Date of CBAC field + rule
- #13 → M2.7-03 (edited) age radio replaced by auto field
- #14 → M2.7-03 (edited) rule with male/female cut-offs
- #15 → M2.7-03 (edited) rule
- #16 → M2.7-03 (edited) rule
- #17 → M2.7-04 (edited) "None of these" option + rule
- #18 → M2.7-05 (edited) result card with 4 messages + rule
- #19 → M2.7-16 · CBAC · TB alert (sputum + family) (new) + rules on M2.7-04
- #20 → M2.7-17 · CBAC · Refer? dialog (new), rule lists all 6 triggers
- #21 → M2.7-18 · CBAC · referral sheet (new)
- #22 → M2.7-17 (new) rule
- #23 → M2.7-04 (edited) rule
- #24 → M2.7-19 · CBAC · B2 / B3 alerts (new)
- #25 → M2.7-19 (new)
- #26 → M2.7-33 · CBAC · environment · Mitanin (new). "Other sources" exists only on Mitanin, so M2.7-05 keeps 3 options plus a rule
- #27 → M2.7-05 (edited) rule (auto flags on save)
- #28 → M2.7-20 · CBAC · missing answer dialog (new)
- #29 → M2.7-05 (edited) rule (referred, sync, toast, back)
- #30 → M2.7-13 · NCD priority & not-eligible lists (read-only) (new)
- #31 → M2.7-13 (new) rule + M2.7-12 rule
- #32 → M2.7-13 (new) caption + rule

### HWC referred / referral forms
- #33 → M2.7-24 (new) filter icon + label; M2.7-23 sheet
- #34 → M2.7-24 · HWC referred list · follow-up states (new)
- #35 → M2.7-24 (new) rule
- #36 → M2.7-24 (new) "Referred from" row
- #37 → M2.7-24 (new) rule
- #38 → M2.7-23 · Referral type filter sheet (new, 7 types) + M2.7-34 · Referral type filter sheet · Mitanin (new, + Child)
- #39 → M2.7-21 · Referral follow-up · visits & dates (new) rule
- #40 → M2.7-21 (new) Visit-n auto + rule
- #41 → M2.7-21 (new) date error banner + rule
- #42 → M2.7-21 (new) visit history table
- #43 → M2.7-22 · Referral follow-up · view, Edit & photo (new) Edit FAB
- #44 → M2.7-22 (new) rule (TB/GC locked)
- #45 → M2.7-22 (new) uploads block + size rule
- #46 → M2.7-21 (new) "Referred on" field
- #47 → M2.7-21 (new) rule
- #48 → M2.7-06 (edited) rules (callers + titles)
- #49 → M2.7-06 (edited) reason prefilled + Additional services
- #50 → M2.7-06 (edited) rule
- #51 → M2.7-06 (edited) rule
- #52 → M2.7-06 (edited) rule; M2.7-26 rule (auto-save)

### TB
- #53 → M2.7-25 · TB screening list (household) (new)
- #54 → M2.7-25 (new)
- #55 → M2.7-25 (new) Nikshay ID row
- #56 → M2.7-07 (edited) rule
- #57 → M2.7-26 · TB screening · refer to HWC dialog (new)
- #58 → M2.7-07 (edited) child-symptom caption + rule
- #59 → M2.7-07 (edited) rule; M2.7-11 (edited) Screen button
- #60 → M2.7-27 · TB suspected list (new)
- #61 → M2.7-27 (new) deceased card + rule
- #62 → M2.7-27 (new) rule
- #63 → M2.7-08 (edited, now visit 1) + M2.7-28 · Suspected TB · visit 2 + positive alert (new)
- #64 → M2.7-08 (edited) Save label + rule; M2.7-28 rule
- #65 → M2.7-28 (new) dialog
- #66 → M2.7-28 (new) rule
- #67 → M2.7-08 (edited) reason reveal + rule; TPT question removed from visit 1 (rule)
- #68 → M2.7-29 · TB confirmed list (new)
- #69 → M2.7-29 (new) rule
- #70 → M2.7-09 (edited) previous follow-ups table
- #71 → M2.7-09 (edited) date field + rule
- #72 → M2.7-09 (edited) outcome Death reveal + warning + rule
- #73 → M2.7-10 (edited) banner + rule
- #74 → M2.7-10 (edited) 3 button states
- #75 → M2.7-10 (edited) follow-up rows on card
- #76 → M2.7-10 (edited) deceased card + rule
- #77 → M2.7-10 (edited) sync + rule
- #78 → M2.7-30 · TPT start / follow-up form (new)
- #79 → M2.7-30 (new) auto completion + rule
- #80 → M2.7-30 (new) Month-n + lock labels
- #81 → M2.7-30 (new) rule
- #82 → M2.7-31 · TPT completion & outcome (new)
- #83 → M2.7-30 schedule table + M2.7-31 read-only state (new)
- #84 → M2.7-11 (edited) risk tags + rule
- #85 → M2.7-11 (edited) per-card risk tags
- #86 → M2.7-11 (edited) Screen / View screening
- #87 → M2.7-32 · Vulnerable risk-factor filter (new)

### CHO
- #88 → M2.6-18 · CHO beneficiary list (new)
- #89 → M2.6-18 (new) orange Add beneficiary
- #90 → M2.6-18 (new) rule note (icon hidden, so no dialog drawn)
- #91 → M2.6-18 (new) rule
- #92 → M2.6-08 (edited) Gender fixed field + rule
- #93 → M2.6-08 (edited) Age is now editable + rule
- #94 → M2.6-08 (edited) success snackbar + rule (error toast)

### HRP
- #95 → M2.6-09 · HRP pregnant list · To assess (new)
- #96 → M2.6-09 (new) LMP/EDD only on assessed cards + rule
- #97 → M2.6-19 · Supervisor · HRP women (read-only) (new)
- #98 → M2.6-10 · Assess high risk (pregnant) · view only (new) + rule on M2.6-03
- #99 → M2.6-03 (edited) rule; M2.6-10 "EDD (auto)"
- #100 → M2.6-02 (edited) cards with Follow up hidden + rule
- #101 → M2.6-02 (edited) History button + rule
- #102 → M2.6-13 · HRP follow-up history sheet (pregnant) (new)
- #103 → M2.6-04 (edited) Visit auto label + rule
- #104 → M2.6-04 (edited) rule
- #105 → M2.6-04 (edited) rule; M2.6-13 rule
- #106 → M2.6-04 (edited) person header
- #107 → M2.6-11 · Micro birth plan · saved (view + Edit) (new) + rule on M2.6-05
- #108 → M2.6-12 · Micro birth plan · table & share (new)
- #109 → M2.6-12 (new) snackbar + rules (WhatsApp missing toast)
- #110 → M2.6-14 · HR non-pregnant · assess list (new)
- #111 → M2.6-15 · Assess high risk (non-pregnant) (new)
- #112 → M2.6-16 · HR non-pregnant · follow-up list (new)
- #113 → M2.6-17 · HR non-pregnant follow-up history sheet (new)
- #114 → M2.6-06 (edited) Is she pregnant + reveal + banner + rule
- #115 → M2.6-06 (edited) LMP in reveal + rule

### gap_B_maternal.md: hrp_cases
- High-risk cases hub (HrpCasesFragment): no gap rows. Nothing to do (M2.6-01 unchanged).

Totals: 119 rows (X1–X4 + 115). Covered 119, skipped 0.

### Mitanin
I checked mitanin_refs.txt, IconDataset.kt and every ncd / cbac / tb / tpt / cho / hrp screen and dataset in scope.
- helpers/Konstants.kt:9-14 + strings_ncd.xml:279-284: the CBAC occupational exposure list has an extra 4th option "Other sources" with a free-text field (CbacFragment.kt:1388-1398, CbacViewModel.kt:564-577) → M2.7-33 · CBAC · environment · Mitanin (new). Rule line on M2.7-05. The default M2.7-05 had wrongly shown "Other", so it is back to the 3-option list.
- NcdRefferedListViewModel.kt:126-128: the referral type filter adds "Child" → M2.7-34 · Referral type filter sheet · Mitanin (new). Rule line on M2.7-23.
- HRP / CHO (cho/**, hrp_cases/**, HRP*Dataset, BenRegCHODataset): no Mitanin checks. Note added on stage 2.6.
- IconDataset.kt: the HRP module gate (lines 60-64) is Xushrukha only, not Mitanin. The Mitanin checks at lines 53, 324, 358 and 411 are village-level forms, which are outside this scope. NCD / TB / CHO tiles are not gated for Mitanin.
- TB / TPT / Vulnerable / referral form datasets: no Mitanin checks.
- Out of scope but seen: HomeActivity.kt:172-177 (no Assamese language on Mitanin) and :897 ("Monthly claim summary" drawer title).

---

## Coverage F · Death reports & Village level forms (file: design/figma-plugin/src/39-stage-2.9-death-vlf.js)

Death reports hub #1 → M2.9-01 · Death reports hub (edited) | tiles show plain totals; banner "counts for selected village"; rule
Death reports hub #2 → M2.9-01 · Death reports hub (edited) | rule lines for CDR / MDSR / General / Non-maternal criteria
CDR list #1 → M2.9-13 · Child deaths list (CDR) (new) | search row with mic + rule
CDR list #2 → M2.9-13 (new) | red "CDR" pill (not filled) / green (filled) + rule
CDR list #3 → M2.9-17 · Death list · ASHA Supervisor view (new) | card without buttons + rule
CDR list #4 → M2.9-13 (new) | sync badge on card
CDR list #5 → M2.9-13 (new) | card shows reg date, ben ID, gender, mobile, father, age, DoD, RCH ID, family head; rule: tap = debug toast only
CDR list #6 → M2.9-16 · Death list · empty state (new) | "No records found"
MDSR list #1 → M2.9-02 · Maternal deaths list (edited) | search row with mic
MDSR list #2 → M2.9-17 (new) + rule on M2.9-02
MDSR list #3 → M2.9-02 (edited) | cards now sync badge + red/green MDSR pill; rule "tabs not in app today – needs dev"
MDSR list #4 → M2.9-16 (new) | rule lists all 4 death lists
GDR list #1 → M2.9-14 · General deaths list (read-only) (new) | no button, no sync + rule
GDR list #2 → M2.9-14 (new) | search row with mic
GDR list #3 → M2.9-16 (new) | rule
NMDSR list #1 → M2.9-15 · Non-maternal deaths list (read-only) (new) | rule
NMDSR list #2 → M2.9-15 (new) search + M2.9-16 empty
CDR form #1 → M2.9-18 · Death report saved (view only) (new) | locked view mode, uploads + Submit disabled
CDR form #2 → M2.9-03 (edited) 3 named slots incl. death certificate + M2.9-19 · Death report · add file chooser (new) Camera/Gallery/PDF/Cancel
CDR form #3 → M2.9-19 (new) | 5 MB error snackbar + rule
CDR form #4 → M2.9-20 · Death report · file preview (new) | image dialog; rule PDF → external viewer
CDR form #5 → rule note on M2.9-03 and M2.9-18 (Mitanin: no upload slots)
CDR form #6 → M2.9-03 (edited) | auto card extended (name, gender, mobile, address, village), place of death auto, first informant = ASHA; rule
CDR form #7 → rule note on M2.9-03 (notification date between DoD and today)
CDR form #8 → rule note on M2.9-03 (spinner, back + sync, failure toast)
MDSR form #1 → M2.9-04 (edited) | 4 locked auto Yes/No answers replace editable radios + rule
MDSR form #2 → M2.9-04 (edited) | cause of death auto ("Suspected maternal death") + "Specify reasons" reveal + rule
MDSR form #3 → M2.9-18 (new) | rule covers MDSR
MDSR form #4 → M2.9-04 (edited) 3 slots + M2.9-19 chooser/5 MB + M2.9-20 preview
MDSR form #5 → rule note on M2.9-04 / M2.9-18
MDSR form #6 → M2.9-04 (edited) | reporter + designation "Asha" now read-only auto; beneficiary auto card
MDSR form #7 → rule note on M2.9-04
VLF hub #1 → M2.9-05 (edited) | red border on overdue tiles + legend; rule incl. "code bug: English title match"
VLF hub #2 → M2.9-21 · VLF hub (Mitanin) (new) | no Saas Bahu / U-WIN
VLF hub #3 → M2.9-21 (new) "Cluster Meeting Forms" tile + rule on M2.9-05 (Cluster meetings tile = design extra)
VLF hub #4 → rule note on M2.9-05 and M2.9-12 ("Survey register: not in app today – needs dev")
VLF hub #5 → M2.9-43 · Filaria MDA campaign history (new) + rule on M2.9-05 | tile text kept "Filaria MDA" so 2.14 nav still resolves
VHND list #1 → M2.9-11 (edited) | disabled "Add VHND" with reason
VHND list #2 → M2.9-11 (edited) | cards: date, attended, place, View; sync tag removed (rule: no sync on VHND)
VHND list #3 → M2.9-22 · Meeting list · empty state (new)
VHND form #1 → M2.9-06 (edited) "Mitanin only" tag + M2.9-23 · VHND saved · view only (other flavours) (new)
VHND form #2 → M2.9-23 (new) | view-only, Submit disabled
VHND form #3 → rule note on M2.9-06 (60 days, default today)
VHND form #4 → M2.9-24 · Meeting photo chooser + 5 MB error (new) + rules on M2.9-06 / M2.9-24 (preview, "No image found")
VHND form #5 → rule note on M2.9-06
VHSNC list #1 → M2.9-25 · VHSNC list (new)
VHSNC list #2 → M2.9-25 (new) | disabled add with reason
VHSNC list #3 → M2.9-22 (new) rule + rule on M2.9-25
VHSNC form #1 → M2.9-26 · VHSNC form (new) | rule
VHSNC form #2 → rule on M2.9-26 (+ M2.9-23 shows pattern)
VHSNC form #3 → M2.9-26 (new) 2 photos + M2.9-24 chooser
VHSNC form #4 → M2.9-26 (new) | caption "count must be 1 or more" + rule
PHC list #1 → M2.9-27 · PHC review list (new)
PHC list #2 → rule on M2.9-27 + M2.9-21 Mitanin tile
PHC form #1 → M2.9-28 · PHC review form (new, default fields) + M2.9-47 · Cluster meeting form · Mitanin (new) | rule
PHC form #2 → rule on M2.9-28
PHC form #3 → rule on M2.9-28 (+ M2.9-23/24 patterns)
AHD list #1 → M2.9-29 · AHD list (new)
AHD form #1 → M2.9-30 · AHD form (new) | date shown as locked "today" + rule
AHD form #2 → M2.9-30 (new) | Mobilized Yes reveals Place + Date
AHD form #3 → rule on M2.9-30
Deworming list #1 → M2.9-31 · Deworming (NDD) list (new) | disabled add with 6-month reason
Deworming list #2 → M2.9-31 (new) | card fields
Deworming form #1 → rule on M2.9-09 (edited)
Deworming form #2 → M2.9-09 (edited) photos cut to 2 + rules
Maa list #1 → M2.9-32 · Maa meeting list (new) | 3-month block; hub subtitle changed to "Every 3 months" on M2.9-05
Maa list #2 → M2.9-32 (new) | sync badge, date, place, participants, View
Maa form #1 → M2.9-07 (edited) | "Mitanin only" tag + rule
Maa form #2 → M2.9-07 (edited) | uploads block (5, JPEG/PNG/PDF) + rule
Maa form #3 → rule on M2.9-07
Maa form #4 → M2.9-33 · Required fields alert (new)
Maa form #5 → rule on M2.9-07
Saas Bahu list #1 → M2.9-34 · Saas Bahu Sammelan list (new) | monthly block; hub subtitle changed to "Monthly"
Saas Bahu list #2 → M2.9-34 (new) | card incl. ASHA ID + sync
Saas Bahu list #3 → M2.9-21 (new) + rule on M2.9-34
Saas Bahu form #1 → M2.9-35 · Saas Bahu Sammelan form (new) | uploads with type/size rule
Saas Bahu form #2 → rule on M2.9-35
Saas Bahu form #3 → M2.9-33 (new) + rule on M2.9-35
U-WIN list #1 → M2.9-36 · U-WIN session list (new) | Add always enabled
U-WIN list #2 → M2.9-36 (new) | card
U-WIN list #3 → M2.9-21 (new) + rule
U-WIN form #1 → M2.9-37 · U-WIN upload reminder (new)
U-WIN form #2 → rule on M2.9-10 and M2.9-37 (duplicate-date toast)
U-WIN form #3 → M2.9-10 (edited) | 2 named slots, images only
U-WIN form #4 → rule on M2.9-10
U-WIN form #5 → rule on M2.9-10
Pulse Polio list #1 → M2.9-38 · Pulse Polio campaign list (new) | disabled add with reason
Pulse Polio list #2 → rule on M2.9-38
Pulse Polio list #3 → M2.9-38 (new) | card
Pulse Polio form #1 → M2.9-08 (edited) | banner now "Server-driven form" + rule (real schema still not designed – fields are examples)
Pulse Polio form #2 → M2.9-08 (edited) "last 2 kept" + M2.9-39 inline size error
Pulse Polio form #3 → M2.9-40 · Pulse Polio campaign · view only (new)
Pulse Polio form #4 → M2.9-39 · Campaign form · missing fields (new)
Pulse Polio form #5 → rule on M2.9-08
ORS list #1 → M2.9-41 · ORS distribution list (new) | hub subtitle no longer "Jun–Jul"
ORS list #2 → M2.9-41 (new) | rule + card
ORS form #1 → M2.9-42 · ORS distribution form (new) + M2.9-40 rule (view-only)
Filaria MDA history #1 → M2.9-43 · Filaria MDA campaign history (new)
Filaria MDA history #2 → M2.9-43 (new) | disabled add, once per year
Filaria MDA form #1 → M2.9-44 · Filaria MDA campaign form (new) | server-driven banner + rule (view-only from history)
Filaria MDA form #2 → M2.9-44 (new) | date field in error state + rule
Filaria MDA form #3 → rule on M2.9-44
Filaria MDA form #4 → M2.9-44 (new) max 2 photos + rule
Filaria MDA form #5 → rule on M2.9-44 (FYI)
Survey register → no gaps; M2.9-12 got rule "Not in app today – needs dev"

Totals: 104 gap rows covered, 0 skipped.

### Mitanin
(The scope was checked against mitanin_refs.txt plus the IconDataset and *Dataset files for the death and village-forms screens. The addendum added 4 frames, M2.9-45…48, so the stage now has 48 frames.)
- IconDataset.kt:53-54 vlfTitle "Mitanin/ Village Meetings" → M2.9-21 · VLF hub (Mitanin) (title + rule)
- IconDataset.kt:322-331 PHC tile → "Cluster Meeting Forms" (was drawn as "Cluster review meeting"; corrected to the real string icon_cluster_phc) → M2.9-21 tile + rule
- IconDataset.kt:358 Saas Bahu hidden; :411 U-WIN hidden → M2.9-21 (tiles absent) + rules on M2.9-34 / M2.9-36
- PHCReviewListFragement.kt:57, 88 list title / add button "Cluster Meeting Forms" → M2.9-46 · Cluster meeting list · Mitanin (new)
- PHCReviewFormFragement.kt:142 form title → M2.9-47 · Cluster meeting form · Mitanin (new)
- PHCReviewDataset.kt:78-96, 128-137, 153-165, 258-259 Participants Attended (Mitanins/MT/ANM-RHO) + Mitanin Activity check list (8 items) → M2.9-47 (new). M2.9-28 is now the default version (those checklists moved out of it).
- VHNCDataset.kt:240-253 Mitanin adds village, ANM/MPW, AWW, PW, LM, committee members, follow-up Yes/No; count label "Mitanins" vs "ASHAs" → M2.9-45 · VHSNC form · Mitanin (new). M2.9-26 corrected to the default fields (date, place, no. of ASHAs, 2 photos); it was wrong for all flavours.
- VHNDDataset.kt:200-215, 233, 325 Mitanin-only counts + 6 checklists → M2.9-06 (existing frame = Mitanin version, tag + rule); default version is M2.9-23
- MaaMeetingDataset.kt:112-120 (+ :77-79 checklist entries) Mitanin village / PW / LM / breastfeeding checklist → M2.9-07 (existing = Mitanin version; checklist items corrected to the maa_breastfeeding array) + rule
- MaaMeetingFormFragment.kt:134 commented-out non-Mitanin "min 2 uploads" check → rule on M2.9-07 (no effect today)
- MaaMeetingFormViewModel.kt:62,105 / MaaMeetingRepo / entities: storage of mitaninActivityCheckList only, no UI → nothing to draw
- CDRFormDataset.kt:236-240 + CdrObjectFragment.kt:122 uploads removed / taps ignored → M2.9-48 · Child death report (CDR) · Mitanin (new)
- MDSRFormDataset.kt:306-310 + MdsrObjectFragment.kt:103 uploads removed → rule on M2.9-48, M2.9-04, M2.9-18
- Death lists / BenDao criteria: no Mitanin difference → rule on M2.9-48
- Not in scope for this stage: NcdRefferedListViewModel.kt:126, supervisor ActivityAdapter.kt:143, migrations / VLFDao (storage only)

---

## Coverage G — ABHA (40-stage-2.10-abha.js) + Supervisor (42-stage-2.12-supervisor.js)

### ABHA
ABHA shell #A1 → session strip on every ABHA frame (edited all, via abhaTop) | rule on M2.10-01 (30:00, closes at 0; expiry warning "needs dev")
ABHA shell #A2 → Close icon in session strip, all frames (edited) | rule on M2.10-01
ABHA shell #A3 → M2.10-13 · Exit confirmation (new) | rule
ABHA shell #A4 → M2.10-14 · Starting ABHA (loading) (new) + M2.10-15 · ABHA server error (new) | rules; M2.10-07 rule edited
ABHA shell #A5 → M2.10-01 (edited) | rule note (blank in recents, tapjacking)
ABHA shell #A6 → M2.10-20 · Create ABHA from menu (no beneficiary) (new) | rule
Find/Create #A7 → M2.10-01 (edited: Search/Create toggle, opens on Create; chooser cards removed) | rule
Find/Create #A8 → M2.10-01 (edited: "Verify with: Aadhaar No" dropdown) | rule (FP commented out)
Find/Create #A9 → M2.10-01 | rule note
Find/Create #A10 → M2.10-08 (new, inline spinner) + M2.10-07 | rule
ASHA mode #A11 → M2.10-02 (edited: eye icon, helper text paste blocked) | rule
ASHA mode #A12 → M2.10-02 (edited: valid ticks) + M2.10-08 · Aadhaar number · errors (new: invalid states) | rule
ASHA mode #A13 → M2.10-02 (edited: name read-only label) + M2.10-20 (editable name) | rule
ASHA mode #A14 → M2.10-02 (edited: consent checkbox + Generate OTP on same screen; "Next: consent" removed) | rule
ASHA mode #A15 → M2.10-08 (new: UIDAI server error) | rule
GOV mode #A16 → rule note only (M2.10-01 + NOTES): radio is gone in code → not designed (per lead: follow the code)
GOV mode #A17 → rule note only (same reason)
Consent #A18 → M2.10-03 (edited: 7 itemised checkboxes, #1 select all, I Agree disabled) | rule
Consent #A19 → M2.10-03 (edited: ASHA/beneficiary names in statements 6/7, primary colour via setRangeFills in try) | rule
Consent #A20 → M2.10-03 (edited: button "I Agree", returns to Aadhaar screen) | rule
Find ABHA #A21 → M2.10-09 · Find ABHA by mobile (new)
Find ABHA #A22 → M2.10-09 (new: ABHA dropdown "Name : ABHA") | rule
Find ABHA #A23 → M2.10-10 · Find ABHA · no result / errors (new)
Find ABHA #A24 → M2.10-09 (new: consent + Generate OTP gating) | rule
Aadhaar OTP #A25 → M2.10-11 · Verify ABHA OTP (find flow) (new) + M2.10-04 edited text
Aadhaar OTP #A26 → M2.10-04 (edited: Exit removed) + M2.10-12 · OTP wrong / lock-out (new, Exit shown) | rule
Aadhaar OTP #A27 → M2.10-12 (new: wrong-OTP inline error, resent toast) + M2.10-04 (edited: 60 s) | rule
Aadhaar OTP #A28 → M2.10-04 rule (branch) + M2.10-05 (edited: mobile reused, not re-asked) | rule
Generate mobile OTP #A29 → rule note on M2.10-05 (Fingerprint path only, disabled) | skipped as frame: unreachable today
Generate mobile OTP #A30 → M2.10-05 rule + M2.10-13 (back confirm) | (i) tooltip: rule only, screen unreachable (FP path)
Verify mobile OTP #A31 → M2.10-05 (edited: OTP-only, read-only mobile, 60 s) | rule
Verify mobile OTP #A32 → M2.10-05 rule + M2.10-12 (shared error/Exit state)
Result #A33 → M2.10-16 · ABHA already exists (new)
Result #A34 → M2.10-17 · Already linked to someone (blocking) (new)
Result #A35 → M2.10-18 · Find result · Here is your ABHA (new)
Result #A36 → M2.10-06 (edited: Yes/No question) + M2.10-19 · ABHA card downloaded (new: notification + snackbar) | rule
Result #A37 → M2.10-06 (edited: "linked to the record of <name>") + M2.10-20 (not linked) | rule (immediate push)
Result #A38 → M2.10-13 (new) | rule on M2.10-06
Design-only: step bar → rule "design-only" (M2.10-02); badge snackbar → "Not in app today – needs dev" (M2.10-06); 3-OTP/30-min banner → "needs dev" (M2.10-07)

### Supervisor
Shell #B1 → M2.12-13 · Mitanin Trainer dashboard (new) | rule (menu label)
Shell #B2 → M2.12-10 · Language (by flavour) (new) | rule
Shell #B3 → M2.12-06 (edited: "Tap avatar to change photo") | rule
Shell #B4 → M2.12-06 (edited: "APK Version 2.14.0")
Shell #B5 → M2.12-06 | rule (Help → LMS / video tutorials)
Shell #B6 → M2.12-06 | rule (external Microsoft Form)
Shell #B7 → M2.12-09 · Logout confirmation (new) | rule
Shell #B8 → M2.12-12 · Unsupported device (blocking) (new) | rule
Shell #B9 → M2.12-01 rule + M2.12-07 progress bar
Shell #B10 → M2.12-01 | rule only (Home vs Language icon, bell badge) — not drawn: top-bar component variant change is out of scope
Shell #B11 → M2.12-13 | rule only (Mitanin supervisor_toolbar colour; exact colour not in brief)
Home #B12 → M2.12-01 (edited: Incentive/KPI tabs) + M2.12-07 · KPI dashboard (new)
Home #B13 → M2.12-01 | rule (dev mode, internal)
Home #B13a → M2.12-01 (edited: title = village "Rajnagar") + M2.12-11 · Exit application (new)
KPI #B14 → M2.12-07 (new: 12 tiles) | rule
KPI #B15 → M2.12-07 (new: Downloading… 64%) | rule
KPI #B16 → M2.12-07 (new: Month/Village/ASHA/Submit) | rule flags unwired filters for product
Incentive #B17 → M2.12-01 (edited: Total ASHAs tile)
Incentive #B18 → M2.12-01 (edited: chevrons on tiles) | rule
Incentive #B19 → M2.12-01 (edited: "Sub-centres under you (4)" list) | rule
Incentive #B20 → M2.12-01 (edited: "All sub-centres") | rule (empty toast)
Incentive #B21 → M2.12-08 · No internet (blocking) (new) | rule on M2.12-01
Incentive #B22 → M2.12-13 (new: Mitanin labels, Past 5th, no subtitles) | rule
Incentive #B23 → M2.12-13 | rule (Trainer → approved list, ANM → verified list)
ASHA list #B24 → M2.12-02 (edited: verified / rejected rows with date, by, reasons) | rule
ASHA list #B25 → M2.12-02 (edited: unclaimed greyed, no amount) | rule
ASHA list #B26 → M2.12-02 (edited: status strip) | rule incl. code bug "Overdue: 0"
ASHA list #B27 → M2.12-02 (edited: month card) | rule (Mitanin wording)
ASHA list #B28 → M2.12-02 | rule
Detail #B29 → M2.12-15 · Monthly detail · already decided (new)
Detail #B30 → M2.12-14 · Mitanin monthly detail · activity ticks (new)
Detail #B31 → M2.12-03 (edited: grouped by programme, table columns)
Detail #B32 → M2.12-14 (new: honorarium approved, cannot open) | rule
Detail #B33 → M2.12-04 (edited: 7 reasons + Other multi-select, conditional text) | rule
Detail #B34 → M2.12-03 + M2.12-04 | rules (toasts, auto-return)
Detail #B35 → M2.12-16 · Monthly detail · no activities (new)
Detail #B36 → M2.12-03 (edited: EmployeeID + Supervisor ID) + M2.12-14 (Trainer ID)
Claim records #B37 → M2.12-17 · Mitanin claim records · ✓ / ✕ (new)
Claim records #B38 → M2.12-05 (edited: Ben ID, RCH, ABHA, amount; meeting claim date) | rule (doc tags/vaccines needs dev)
Claim records #B39 → M2.12-05 (edited: summary strip) + M2.12-16 rule (empty) | rule
Profile #B40 → M2.12-06 (edited: contact + posting cards)
Profile #B41 → M2.12-06 (edited: bank card with blank values) | rule for product decision
Profile #B42 → M2.12-06 rule + M2.12-13 subtitle "Mitanin Trainer"
Profile #B43 → M2.12-09 (new) | rule
No-internet #B44 → M2.12-08 (new)
Design-only: overdue banner, Start verifying, tabs/chips, doc tags/vaccines → "Not in app today – needs dev" rules

Totals: 83 gap rows; 83 covered (A16/A17/A29/B10/B11 by rule note only, no drawing); 0 skipped.

#### Corrections after reading the Mitanin code (supersede the rows above)
- B30/B37: per-activity ticks and per-row ✓/✕ are gated by flavour + month actionable, NOT Trainer-only → both Trainer and ANM (rules on M2.12-14/17/19).
- B36 (Mitanin): the "Mitanin Trainer ID" line is set but tvSupervisorInfo is GONE → not drawn on Mitanin; rule on M2.12-14.
- B23: Verified tile label is "Verified" for both Mitanin roles (only the list it opens differs).
- B2: language picker is a single-choice dialog (M2.12-10 redrawn as dialog), order English, Hindi, Bangla, Assamese.
- Frames renamed (new, unreferenced): M2.12-13 · Incentive dashboard · Mitanin, M2.12-14 · ASHA monthly detail · Mitanin, M2.12-17 · Claim records · Mitanin.

### Mitanin
SupervisorActivity.kt:93-98 (language: English + Hindi only) → rule on M2.12-10 + M2.12-13
SupervisorActivity.kt:188-191 (toolbar colour supervisor_toolbar #037E62) → drawn on all Mitanin frames (mitTop) + rule M2.12-13
SupervisorActivity.kt:494-506 (menu item "Mitanin Trainer" / role name) → M2.12-20 (new) + rules M2.12-13, M2.12-19
SupervisorProfileFragment.kt:71-86 (subtitle Mitanin Trainer / raw role, green logo) → M2.12-20 · Supervisor profile · Mitanin (new)
SupervisorProfileFragment.kt:114-120 (green logo, title "Profile") → M2.12-20 rule
IncentiveDashboardFragment.kt:76-86 (labels, hidden subtitles, Overdue "Past 5th (due date)", green logo) → M2.12-13 · Incentive dashboard · Mitanin (new)
IncentiveDashboardFragment.kt:87-101 (Trainer isApproved → approved list; ANM → verified; Overdue tile both roles, FLW-1169) → M2.12-13 rules
SubCenterAdapter.kt:28-29 ("Mitanin: n") → M2.12-13 (drawn)
IncentiveVerificationFragment.kt:67-69 ("Mitanin monthly detail", "Search Mitanin") → M2.12-18 · ASHAs to verify · Mitanin (new)
AshaWorkerAdapter.kt:68-69, 95-96, 133-134 ("By: name (Mitanin Trainer)") → M2.12-18 (drawn)
WorkerDetailFragment.kt:84-87, 136-138, 166-196 (ticks, disabled until ticked) → M2.12-14 · ASHA monthly detail · Mitanin (new)
WorkerDetailFragment.kt:120-123 (name + summary card shown, reviewer ID + documents checkbox hidden) → M2.12-14 (drawn) + rule
WorkerDetailFragment.kt:126-131 (ANM: "Verified by: <role>"; Trainer: Verify label) → M2.12-19 · ANM monthly detail · Mitanin (new)
WorkerDetailFragment.kt:244-251 (Verify always visible on Mitanin, FLW-1145) → M2.12-14 rule
WorkerDetailFragment.kt:318-320 (documents tick not required on Mitanin) → M2.12-14 rule
WorkerDetailFragment.kt:410-416 (toolbar = worker name + green logo) → M2.12-14/19 (drawn) + rule
ActivityAdapter.kt:25/42/143 (TYPE_MITANIN layout disabled for all flavours) → no design change (noted here only)
BeneficiaryDetailFragment.kt:71-77 (row actions = Mitanin && actionable) → M2.12-17 · Claim records · Mitanin (new) + rule
BeneficiaryAdapter.kt:21-85, 97, 128, 159 (layout_mt_inner: S.No, Ben ID, RCH, ABHA, amount, claim date for meetings, ✓/✕) → M2.12-17 (drawn)
ABHA (abha_id_activity/**): no Mitanin checks → rule line on M2.10-01 "Mitanin: same ABHA flow"
Out of scope (ASHA app, not my files): HomeActivity.kt:172-177 language list, HomeActivity.kt:897 "Monthly claim summary" menu label.

---
