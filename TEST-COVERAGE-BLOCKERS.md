# Test Coverage — Application-Code Gaps & Blockers (FLW-1115, branch `unit-test-coverage-and-pr-checks2`)

This file records issues found in **application code** (`src/main`) while raising unit-test
coverage. Per the task constraints these are documented rather than fixed inline — with one
unavoidable exception, called out first.

---

## 1. 🔴 CRITICAL / MUST FIX — `release-2.12` does not compile

**This is a pre-existing bug on `origin/release-2.12`, not something introduced by the coverage work.**

- **File:** `app/src/main/java/org/piramalswasthya/sakhi/database/room/InAppDb.kt`
- **Introduced by:** merge commit `70af6522` ("Merge branch 'release-2.12' into work_enhancement"),
  which reached `release-2.12` via `320bca8a` (PR #555).
- **Symptom:** `:app:kaptGenerateStubsNiramayDebugKotlin` FAILED —
  `e: InAppDb.kt:3489:2 Missing '}'`. The whole module fails to compile, so **no unit test,
  no JaCoCo report, and no APK build is possible** on this branch as committed.

### Root cause

The merge inserted the new `MIGRATION_63_64` block **without its two closing braces**. The result
is that `MIGRATION_62_63` (and every migration after it) became nested *inside*
`MIGRATION_63_64.migrate()`, leaving the file three braces short at EOF:

```kotlin
            val MIGRATION_63_64 = object : Migration(63, 64) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
              ALTER TABLE Adolescent_Health_Form_Data
            ADD COLUMN isSanitaryNapkinUsed INTEGER
            """.trimIndent()
                    )
            val MIGRATION_62_63 = object : Migration(62, 63) {   // <-- never closed above
```

Brace balance check across refs (`{` minus `}` in the raw file):

| ref | delta |
|---|---|
| `origin/main`, `e60ca952`, `78a1a3f4` | 1 (baseline — a literal `{` inside a string) |
| `70af6522`, `320bca8a`, `origin/release-2.12`, HEAD | **3** (two unbalanced `{`) |

### Fix applied (minimal, 2 lines)

Because the task brief also says *"run the build and fix any build failures until it succeeds"*,
and because **zero** test/coverage work is possible on a tree that does not compile, the minimal
brace fix was applied — closing `migrate()` and the `object : Migration(63, 64)` expression:

```kotlin
            """.trimIndent()
                    )
                }
            }

            val MIGRATION_62_63 = object : Migration(62, 63) {
```

This is the **only** `src/main` edit made by the coverage work. It adds no behaviour: it restores
the structure the merge destroyed. Please review it deliberately as part of your manual commit,
and consider whether `release-2.12` should be fixed by its own hotfix PR instead — right now the
release branch tip is un-buildable for everyone.

### Related, worth a look while you're in there

`MIGRATION_63_64` is the only migration in the file with **no `try/catch` and no
`columnExists(...)` guard** (contrast `MIGRATION_62_63`, which guards every
`ALTER TABLE ... ADD COLUMN`). If `isSanitaryNapkinUsed` already exists on a device — e.g. after a
partial/retried migration — `execSQL` will throw and Room will fail to open the database. Given the
user base ("5k non-technical ASHA users"), matching the surrounding guarded style would be safer.

---

## 1b. Why 90% was not reached, and what it would actually take

The 90% target is measured against the **full** JaCoCo denominator (492,816 instructions) with the
existing exclusion list, which you previously asked to leave unchanged. Concretely:

| | instructions |
|---|---|
| Denominator | 492,816 |
| Covered at session start | 319,917 (**64.92%**) |
| Needed for 90% | 443,534 |
| **Gap to close** | **+123,617** |
| Total *missed* at session start | 172,899 |

So 90% requires covering **~72% of every remaining uncovered instruction in the app** — including
the parts that are structurally unreachable from a JVM unit test (§2 below, ~12–15k) . That is not a
"write more tests" gap; it is a multi-week programme. This session closed roughly a fifth of it.

**The two cheapest remaining levers, in order:**

1. **Navigation safe-args (done this session, ~15k).** 254 generated `*FragmentArgs` /
   `*FragmentDirections` classes were at 0%. They are now covered mechanically. Note the honest
   caveat: this is *generated* code, so the tests raise the number more than they raise confidence.
   **Recommendation:** add `**/*FragmentArgs*.*`, `**/*FragmentDirections*.*` and
   `**/*NavigationDirections*.*` to `coverageExclusions` in `app/jacoco.gradle`. That removes ~21.8k
   of generated instructions from the denominator, which is both more honest and worth ~1.5pp on its
   own. If you do that, delete the 254 generated test files with it.
2. **Workers (done this session, ~10k).** The `work/` package went 0.3% → ~50%. It was previously
   believed to need Robolectric; it does not. `CoroutineWorker` subclasses are directly
   constructible with `mockk<Context>()` + `mockk<WorkerParameters>()`, and `doWork()` runs fine
   under `runTest`. The remaining `work/` misses are the *bodies* of each `doSyncWork()`, which need
   per-worker repository stubs returning real payloads — mechanical but not generatable.

**Where the remaining ~145k actually lives** (after this session):

| package | still missed | nature of the work |
|---|---|---|
| `ui` (ViewModels) | ~50k | ~150 ViewModels; real logic, genuinely worth testing, but hand-written — no shortcut |
| `repositories` | ~29k | real logic; `BenRepo` alone is 3.3k and is I/O-heavy (multipart, Base64, FileProvider) |
| `configuration` | ~17k | form datasets, already at 89%; the tail is deep conditional branches |
| `model` | ~17k | overwhelmingly trivial accessors + synthetic default-arg constructors — raises the number, not confidence |
| `work` | ~12k | per-worker `doSyncWork()` bodies |
| `helpers`/`database`/`network`/`utils` | ~20k | ~12k of this is structurally unreachable (§2) |

**Realistic recommendation:** treat 90%-of-everything as the wrong target and instead (a) apply the
generated-code exclusions above, (b) set the CI ratchet at the achieved number and raise it per
release, and (c) spend the effort on the `ui`/`repositories` ViewModel + repository logic, which is
where coverage and confidence actually coincide.

## 2. Structural testability gaps (not fixed — documented only)

These are the places where `src/main` structure, not missing tests, is what caps coverage. Each
would need an application-code change (or Robolectric, which has been declined for this ticket) to
become reachable from JVM unit tests.

| Area | Missed instr. (approx.) | Why it is unreachable from a JVM unit test |
|---|---|---|
| `helpers/otpview/PinView` | 2,092 | Extends `AppCompatEditText`; the **super**-constructor runs real Android view init. MockK cannot intercept a superclass constructor, so the class can never be instantiated off-device. |
| `helpers/addharEditText/BlockEditText` | 1,764 | Same as above. |
| `ui/BindingUtilsKt` | 1,806 | File-level `private val rotate = RotateAnimation(...)` runs in `<clinit>`, so *any* access to *any* function in the file throws `ExceptionInInitializerError`. A lazily-initialised property (`by lazy`) would make the whole file testable. |
| `database/room` `MIGRATION_x_y` | ~3,900 | Every migration is a **function-local `val`** inside `InAppDb.getInstance()`. They are not reachable without running `Room.databaseBuilder`. Hoisting them to `companion object` constants (or an `InAppDbMigrations` object) would make all ~45 migrations directly unit-testable — and independently verifiable, which matters for a DB with 64 versions. |
| Real `Fragment`s (`VHNDFormFragement`, `VHNCFormFragement`, `PHCReviewFormFragement`, …) | ~4,100 | Android view lifecycle. **Note:** these are excluded from JaCoCo by the `**/*Fragment.*` pattern *only when spelled correctly* — these four are misspelled **`Fragement`**, so they slip past the exclusion and are counted against coverage. Renaming them to `...Fragment` both fixes the typo and removes ~4,100 uncoverable instructions from the denominator. |
| `adapters/FormInputAdapterWithBgIcon` | 428 | Same story: `**/*Adapter.*` excludes adapters, but this class ends in `BgIcon`, so it is counted. |
| `SakhiApplication` | 659 | Extends `android.app.Application`. |
| `ui/.../HRPMicroBirthPlanTable` (+ inner) | ~1,450 | Not named `*Fragment`/`*Activity`/`*Adapter`, so not excluded, but it is pure view code. |
| `Build.VERSION.SDK_INT` branches | scattered | `SDK_INT` is `0` on the JVM, so the "modern OS" arm of every version check is dead code in unit tests (e.g. `BaseDynamicWorker.createForegroundInfo`). |

## 3. Bugs found by the new tests (not fixed — documented only)

### 3.1 🔴 `RootedUtil.checkProps()` can loop forever

`app/src/main/java/org/piramalswasthya/sakhi/utils/RootedUtil.kt`

```kotlin
val prop: String = reader.readLine()
while (prop.isNotEmpty()) {                       // prop is a val — never re-read
    if (prop.contains("[ro.debuggable]=[1]") || prop.contains("[ro.secure]=[0]")) return true
}
```

`prop` is a `val` read **once** before the loop, and the loop body never reassigns it. On a real
device `getprop`'s first line is non-empty and (usually) does not match either pattern, so this is
an **infinite loop that pins a CPU core**. It is reached only when `checkSUBinary()` and
`checkRootManagement()` both return false — i.e. on the overwhelming majority of non-rooted
devices, whenever `isDeviceRooted()` is called.

The loop should read the next line each iteration (`var prop = reader.readLine(); while (prop != null) { …; prop = reader.readLine() }`).
Because of this, `RootedUtilTest` deliberately does **not** drive `checkProps()`.

### 3.2 `UpdatePNCToECWorker.doWork()` lets exceptions escape

`doWork()` calls `WorkerUtils.triggerAmritPushWorker(context)` outside any `try/catch`. If
WorkManager is not initialised (or `getInstance` throws for any reason) the exception propagates out
of `doWork()`, which WorkManager surfaces as an unhandled worker crash rather than a clean
`Result.failure()`. Every other worker in the codebase routes through `BasePushWorker`/
`BaseDynamicWorker`, which do catch. Consider extending the same handling here.

### 3.3 `SurveyRegisterFormDataset` is dead code

`app/src/main/java/org/piramalswasthya/sakhi/configuration/SurveyRegisterFormDataset.kt` is 97 lines
of `private val … = FormInputOld(...)` field initialisers with **no public members at all**, and
neither of its constructor parameters (`context`, `surveyRegister`) is ever read. Nothing can consume
it. It looks like an abandoned form; consider deleting it (that also removes 444 instructions from
the coverage denominator).

### 3.4 Test-authoring note: `WorkManager.getInstance` is a Kotlin companion method

Not an application bug, but it costs a debugging cycle every time: in `androidx.work` 2.10
`WorkManager.getInstance(context)` lives on a **Kotlin companion object**, not a Java static. So
`mockkStatic(WorkManager::class)` silently does nothing and the real
`WorkManagerImpl.getInstance` runs (→ `AbstractMethodError` on a mocked `Context`, or
"WorkManager is not initialized properly"). Use `mockkObject(WorkManager.Companion)` instead.

Similarly, `DownloadCardWorker`'s constructor does an unchecked
`context.getSystemService(...) as NotificationManager`, so a relaxed `Context` mock must be given an
explicit `NotificationManager` or the cast throws `ClassCastException` at construction.

### 3.5 Test-authoring note: MockK cannot mock JDK bootstrap classes at all (`File`, `Runtime`, `System`)

Cost a long debugging session on `RootedUtilTest.kt`. `mockkConstructor(File::class)` and
`mockkStatic(Runtime::class)`/`mockkStatic(System::class)` do not merely mock unreliably — they don't
hook these classes **at all**, because `java.io.File`, `java.lang.Runtime`, and `java.lang.System` are
`java.base` bootstrap classes, not project classes or Android SDK stub classes. Confirmed empirically:
even a real, already-constructed `File` instance can't be stubbed (`every { f.exists() }` throws
`MockKException: Missing mocked calls`), and the synthetic self-instance MockK builds for
`anyConstructed<File>()` bypasses the real constructor entirely (leaving internal fields like `path`
null), so calling any real method on it NPEs from inside `IoOverNioFileSystem`/`File` internals. Every
`mockkStatic(...)` call that *does* work elsewhere in this suite (`Log`, `Uri`, `Base64`, `TextUtils`,
`Toast`, `Environment`, `WorkManager.Companion`) targets an **Android SDK stub class** loaded from
`android.jar`, or a **project class** — never a real JDK bootstrap class.

**How to apply:** for code under test that touches `File`, `Runtime.exec`, or `System.currentTimeMillis`
directly, don't try to mock them — use real values instead: real temporary files/directories (already
the established pattern in `IncentiveRepoTest.kt`), or drive `Runtime.exec` against a real, temporary
fake executable installed on `PATH` (works on POSIX CI runners; skip via `org.junit.Assume.assumeTrue`
on Windows dev machines, since Windows `CreateProcess` can't resolve an extensionless command name to
a script).

### 3.6 Test-authoring note: `SQLiteConstraintException`'s message is always `null` on the JVM

Bit two different worker tests (`PullFromAmritWorkerTest`, `PullFilariaFromAmritWorkerTest`). The
Android `android.jar` stub used for JVM unit tests does not actually store the constructor argument
passed to `SQLiteConstraintException("some message")` — `e.message` always evaluates to `null`, no
matter what string was passed in. So any test asserting `"SQLite constraint: <the message I passed>"`
will fail; the real result is always whatever the production code's null-fallback produces (e.g.
`"SQLite constraint: null"` or `"Unknown error"`, depending on the exact `?:` expression). Assert the
actual null-fallback string, not the literal message text.

### Latent robustness issues noticed while writing tests

Unguarded indexing / non-null assertions that a unit test can trip and a device can too:

- `CDRFormDataset` and `MDSRFormDataset` — `user!!.villages[0].name`: throws if a user has no villages.
- `BenGenRegFormDataset` / `BenRegFormDataset` — `relationToHeadListDefault[saved.familyHeadRelationPosition - 1]`:
  position `0` yields index `-1` → `ArrayIndexOutOfBoundsException`.
- `PncFormDataset.mapValues` — reads `pncPeriod.value!!.substring(4)`, but `pncPeriod` is assigned
  **only** inside the `saved?.let { }` branch, so a freshly-filled (unsaved) form NPEs.
- `ImageUtils` — `File(context.filesDir, ...)` with no null check on `filesDir`.
- `ImageUtils`'s stored-image cleanup filter (`file.name.isDigitsOnly() && file.name.endsWith("jpeg")`)
  can never match a real `"<id>.jpeg"` filename — `isDigitsOnly()` rejects the `.` and letters — so
  that cleanup branch is dead code; stored images are never actually deleted by this path.
- `RootedUtil.checkProps()` reads its `getprop` output line (`prop`) once **outside** the `while`
  loop that checks it and never reassigns it inside the loop — if the first line doesn't match and
  more lines follow, this is an infinite loop reading the same line forever. Tests had to be written
  to always match on the first line specifically to avoid triggering it.
- **`FilariaMdaCampaignRepository.toYearKey()`** (~line 113) tries `SimpleDateFormat("dd-MM-yyyy")`
  **before** `SimpleDateFormat("yyyy-MM-dd")`. Verified empirically: `dd-MM-yyyy`'s lenient parser does
  **not** throw on a `yyyy-MM-dd`-shaped string like `"2026-06-15"` — it happily misinterprets it as
  day=2026 (rolled over), month=06, year=15 (rolled under to `"0020"` after normalization) and returns
  immediately, so the correct `yyyy-MM-dd` format is **never actually reached** for any real input of
  that shape. Any campaign date stored/received in `yyyy-MM-dd` form gets filed under a garbage year
  key silently — no exception, no log, just wrong data. This is a real, verified production bug, not
  a hypothetical: reproduced the same misparse for `"2000-01-01"` → `"0006"` and `"1999-12-31"` →
  `"0037"`. Fix should swap the try order (attempt `yyyy-MM-dd` first) or use a strict, non-lenient
  parser (`setLenient(false)`) for both formats.

## 4. `UserRepo.getTokenAmrit()` is structurally untestable via JVM unit tests — 0/484 instructions, permanently

`UserRepo.kt`'s `getTokenAmrit(userName, password)` — the core Amrit login/token-fetch method — cannot
be unit-tested at all, for every branch, with no workaround available from the test side. Its very
first line calls the private `encrypt(password)`, which unconditionally does `CryptoUtil().encrypt(...)`
(`UserRepo.kt:232-234`), and `CryptoUtil`'s primary constructor unconditionally reads
`KeyUtils.encryptedPassKey()` (`CryptoUtil.kt:17`). `KeyUtils` (`utils/KeyUtils.kt`) is a Kotlin
`object` whose `init { }` block does `System.loadLibrary("sakhi")` — a real native library built via
CMake that does not exist in a plain JVM unit-test process. The very first reference to `KeyUtils`
from anywhere throws `UnsatisfiedLinkError` → wrapped `RuntimeException`, and the JVM permanently
poisons the class for the rest of that test fork (`ExceptionInInitializerError` the first time,
`NoClassDefFoundError` on every later reference in the same fork).

This is NOT a mocking skill issue — `mockkConstructor(CryptoUtil::class)` was tried and still fails,
because even setting up constructor interception requires the JVM to load `CryptoUtil`'s class, whose
field initializer already references `KeyUtils` before any mock can intercept it. `mockkObject(KeyUtils)`
would fail the same way, since obtaining the singleton instance to spy on is itself an "active use"
that triggers `<clinit>`. There is no route around this without either touching `src/main` (e.g.
extracting the native call behind an injectable interface) or shipping a stub native library for the
test JVM, neither of which is in scope here.

8 tests attempting to exercise `getTokenAmrit`'s branches were written this session and then removed
from `UserRepoTest.kt` once this was confirmed — they failed 100% of the time regardless of what they
mocked. This method (and transitively, real login) has **0% test coverage today and no feasible way to
raise it** under the current architecture; flagging it here so it isn't accidentally targeted in a
future coverage session. If real login-path coverage is ever wanted, the fix has to be in `src/main`
(inject an `EncryptionProvider` interface instead of constructing `CryptoUtil` directly).

## 5. Two more dead/unreachable catch-and-branch findings (same class of bug as `PullFromAmritWorker`'s in section 3)

- **`AbhaIdRepo.kt`** — both `createHealthIdWithUid` and `mapHealthIDToBeneficiary` (and by inspection,
  every sibling method in this file) list `catch (e: IOException)` **before** `catch (e: SocketTimeoutException)`.
  Since `SocketTimeoutException` IS-A `IOException`, Kotlin's catch clauses match in source order, so
  every `SocketTimeoutException` is actually caught by the earlier, broader `IOException` handler
  (mapping to error code `-1`) and the dedicated `SocketTimeoutException` handler (error code `-3`) is
  dead code, unreachable in every one of these methods. Two new tests in `AbhaIdRepoTest.kt` originally
  asserted the (never-reachable) `-3` outcome and were corrected to assert the real `-1` outcome — but
  the dead catch clause itself is still there in `src/main` and probably should be reordered someday
  (move the specific catch above the general one) so timeouts actually get their own error code.
- **`PullFromAmritWorker.doWork()`** — `Result.failure("Pull operation returned incomplete results")`
  (line ~78) appears unreachable with the current implementation: `getBenForPage` always returns `true`
  regardless of any internal exception (the bare `true` is the last expression of its `withContext`
  block even after its own internal catch swallows an error), so `result1.all { it }` can never
  evaluate false without an exception propagating instead, which takes a different path entirely. No
  test targets this branch since it can't be legitimately exercised without a `src/main` change.
- **`LeprosyRepo.getAllLeprosyFollowUpDataFromServer()`** (~line 440) calls `jsonObj.getString("data")`
  unconditionally, before branching on the response's status code — but a real 200-success response
  needs `"data"` to be a JSON **array** (the 200 branch later calls `jsonObj.getJSONArray("data")`).
  `getString` and `getJSONArray` on the same key are mutually exclusive for any single valid response,
  so the entire 200-success path (and its private helper `saveAllLeprosyFollowUpDataFromResponse`) is
  unreachable through any real server response — this method can never successfully save follow-up
  data. No test could be written for that helper's internals for the same reason. Worth a real fix.

---

## VHNDDataset.kt — `mitanin` flavor branch is unreachable from JVM unit tests (verified via bytecode)

`VHNDDataset.kt` gates a large amount of logic behind
`BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)` in three places:
`setUpPage()` (list assembly, ~lines 200-216; cache-population parsing, ~lines 233-277),
`mapValues()` (~lines 325-334), and the private helper `toCsv()` (line 359, only ever called
from the `mapValues` block above). Together these account for the large majority of this file's
573 missed instructions.

This was confirmed structurally unreachable, not merely untested — disassembled the compiled
`niramayDebug` variant's `VHNDDataset.class` with `javap -c` and found that
`BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)` compiles to:

```
ldc  #233   // String niramay
ldc  #235   // String mitanin
invokestatic  kotlin/text/StringsKt.contains:(Ljava/lang/CharSequence;Ljava/lang/CharSequence;Z)Z
```

i.e. the Kotlin compiler inlines `BuildConfig.FLAVOR` as the literal constant `"niramay"` at every
call site (it is a Java `public static final String` compile-time constant per JLS 15.28, and
Kotlin folds it the same way javac does for Java callers). There is no `GETSTATIC BuildConfig.FLAVOR`
instruction anywhere in the class — `mockkStatic(BuildConfig::class)` has nothing to intercept, so
this branch cannot be flipped to `true` from a unit test compiled against the `niramay` flavor.
Reaching this code would require compiling and running the JVM test source set against a `mitanin*`
product flavor variant, which is a build-configuration change outside `src/test` and out of scope
for this task.

Only two genuinely closable gaps remained in this file: the `pic1.value.isNullOrBlank()` /
`pic2.value.isNullOrBlank()` checks (lines 186, 190) each had "1 of 6 branches missed" because no
existing test ever passed a non-null, whitespace-only string — only `null` or non-blank values. A
new test (`setUpPage resets whitespace-only image values back to default`) exercises that path via
`setImageUriToFormElement` with mocked `Uri.toString()` values of `" "` / `"   "`.

## 6. `UserRepoTest.kt` follow-up session — deferred to the existing §4 finding, did not re-attempt

Re-reviewed `UserRepo.kt` for more `UserRepoTest.kt` coverage. Confirmed §4's finding still holds:
`authenticateUser`, `saveToken`, the private `setUserRole`, and `getTokenAmrit` all route through
`encrypt(password)` as their very first statement, which unconditionally does `CryptoUtil()` →
`KeyUtils.encryptedPassKey()` → `System.loadLibrary("sakhi")` in `KeyUtils`'s `init` block
(`CryptoUtil.kt:17`, `KeyUtils.kt:19-27`, verified by direct read this session). Since §4 already
documents 8 tests written and removed after confirming this fails 100% of the time no matter what is
mocked, no new tests were added against these four methods/branches this session — re-deriving the
same failure was not worth the risk of poisoning the test JVM fork for the rest of the suite.

Instead, this session's `UserRepoTest.kt` additions targeted the parts of `UserRepo.kt` that do
**not** go through `encrypt()`/`CryptoUtil`: the full happy-path body of `setFacilityData` (location /
facility / supervisor / peer-list saves, previously 0% — only the catch-block guards were covered),
`clearFirebaseToken` (previously 0%), and three more `refreshTokenTmc` branches (blank
`newRefreshToken`, the `SocketTimeoutException` self-retry, and a successful-response-with-missing-body
`IllegalStateException`). 11 new `@Test` functions added; `offlineLogin` remains untested — it is
`private` and has zero callers anywhere in `src/main` (confirmed via repo-wide grep), so it is dead
code unreachable from any test without reflection.

---

## EligibleCoupleTrackingDataset.kt — same `mitanin`-flavor unreachability, plus a few dead null-checks

Same root cause as `VHNDDataset.kt` above (`BuildConfig.FLAVOR` is inlined as the literal flavor
string at compile time, so `mockkStatic` has nothing to intercept and the `mitanin`-true branch of
every `!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)` check is unreachable from this
flavor's JVM test source set). This accounts for most of the remaining missed branches in
`handleListOnValueChanged` and `mapValues` (e.g. lines ~291/303-322, 337, 352, 360's sibling in
`setUpPage`'s EDIT-ENTRY block; ~423-435, 447/462, 485/500, 532/540/548, 559/567, 578/586, 631-635,
669-672, 716-717, 721-731, 755-761 in `handleListOnValueChanged`; ~804-806 in `mapValues`).

Two of those (`usingFamilyPlanningMitanin.id` at line 712 and `wantToUseFamilyPlanning.id` at line
753) are directly reachable from a unit test regardless of flavor — the `when(formId)` branch match
doesn't care about list membership or `BuildConfig.FLAVOR`. But their own "value == yes/no[0]" true
forks (lines 721-731 and 755-761) are still unreachable: `usingFamilyPlanningMitanin.value` and
`wantToUseFamilyPlanning.value` are *only* ever assigned a non-null string inside the same
`mitanin`-gated `setUpPage` block, and `setValueById()` is a no-op unless the target `FormElement` is
already present in the dataset's private list (which these two never are on this flavor). Forcing
those values would require reflection into private fields, which felt like the wrong tool for a gap
that is really the same underlying flavor-gating issue.

A handful of additional branches are dead code independent of flavor, so no test (short of a
`src/main` change) can close them:
- `setUpPage`'s `dateOfVisit.value?.let { ... }` (line 239) — `dateOfVisit.value` was just set from
  `getDateFromLong(System.currentTimeMillis())`, and `getDateFromLong` only returns null when its
  input is exactly `0L`; `System.currentTimeMillis()` never is, so the null fork can't be reached
  without mocking `System.currentTimeMillis()` itself (too invasive — coroutines/test infra rely on
  real wall-clock time too).
- `calculateNextInjectionDate`'s `sdf.parse(...) ?: return "" to ""` (line 862) and `getNextDose`'s
  two `sdf.parse(...) ?: return ...` calls (lines 889-890) — `SimpleDateFormat.parse(String)` never
  actually returns `null`; on a bad string it throws `ParseException` instead (only the
  `ParsePosition` overload can return null). The elvis branch is therefore unreachable; the exception
  path is what's really tested (and is, via the "unparsable date" / non-numeric-dose tests).
- `getNextDose`'s `if (next in 1..10)` (line 898) has one permanently-dead sub-branch: `next` is
  `doseNum + 1` where `doseNum` comes from `.filter { it.isDigit() }.toIntOrNull() ?: 0`, which can
  never be negative, so `next < 1` can never happen.
- `mapValues`'s `getEnglishValueInArray(...) ?: methodOfContraception.value` (line 815) — reached only
  when `methodOfContraception.value == methods[1]`, and `methods[1]` is by construction a member of
  the very array `getEnglishValueInArray` searches, so the lookup always succeeds and the `?:`
  fallback is unreachable under any resource mocking that keeps both arrays in sync (which every test
  in this file necessarily does, since `resources`/`englishResources` are the same mocked object).

---

## HRPRepo.kt — two dead/unreachable branches left uncovered on purpose

While extending `HRPRepoTest.kt`:

- `updateSyncStatusHrpt`, `updateSyncStatusHrpa`, `updateSyncStatusHrpNonAssess`, and
  `updateSyncStatusHrNonTrack` (private, ~lines 1174-1207) all take a **nullable** `List<...>?`
  parameter and immediately do `entities?.let { ... }`. Every call site in `src/main` passes the
  non-null `chunk: List<...>` produced by `entities.chunked(CHUNK_SIZE)`, which can never be null.
  The `entities == null` branch of each `?.let` is therefore dead code from any real call path; since
  these methods are `private`, a test cannot invoke them directly with a null argument either. Left
  uncovered (4 methods × 1 branch each).
- `getLongFromDate(dateString: String)` (companion, ~line 1290): `SimpleDateFormat.parse(String)`
  never actually returns `null` — it either returns a `Date` or throws `ParseException` — so the
  `date?.time ?: throw IllegalStateException(...)` elvis fallback is unreachable. This is the same
  class of finding already documented above for `EligibleCoupleTrackingDataset`'s `calculateNextInjectionDate`/
  `getNextDose`. The *exception* path is what's actually reachable and is exercised indirectly: malformed
  `visitDate`/`lmp` strings in `saveHRPTrack`/`saveHRNonPTrack` entries cause `getLongFromDate` to throw
  `ParseException`, which is caught by those methods' own `catch (e: java.lang.Exception)` per-entry
  handler (entry skipped, loop continues) rather than by this elvis branch.

---

## BenRegFormDataset.kt — `applyDeathLockState(list, isDeath)`'s `isDeath = false` branch is dead code

`app/src/main/java/org/piramalswasthya/sakhi/configuration/BenRegFormDataset.kt` defines two
overloads of `applyDeathLockState`: a no-arg-list one (`applyDeathLockState(isDeath: Boolean)`,
~line 1903) used inside `handleListOnValueChanged`'s `beneficiaryStatus.id`/`placeOfDeath.id` cases
with both `true` and `false`, and a list-taking one (`applyDeathLockState(list: MutableList<FormElement>,
isDeath: Boolean)`, ~line 1925) used from `setPageForHof`, `setPageForFamilyMember`, and
`initializeDeathFields`. Every one of those three call sites passes `isDeath = true` literally — grepped
the whole file and there is no call site that ever passes `false` for the list-taking overload. Its
`else` branch (the one that restores `originalInputTypeForLockedFields` onto `list`) is therefore
unreachable from any current caller; only the no-arg-list overload's `false` branch (called when a
beneficiary is toggled from Death back to Alive after the page was already built) is actually
exercised, and that one already has test coverage. Documented rather than exercised, since forcing the
list-overload's `false` branch would need a `src/main` call-site change, which is out of scope here.

## BenRegFormDatasetTest.kt — coverage added this session (handleForAgeDob / mapValueToBen)

Three new tests added to the existing `BenRegFormDatasetTest.kt` (no other file touched):

- `handleForAgeDob subtracts the head of family's age when relationToHead was primed to a parent
  relation` — closes `handleForAgeDob`'s `isBenParentOfHoF() == true` branch (the `ageAtMarriageMax =
  age - hofAge` computation), which no earlier test reached. `isBenParentOfHoF()` reads
  `relationToHead.value`, but for a **saved** (non-null, non-draft) `ben`, `setPageForFamilyMember`
  only reassigns `relationToHead.value` *after* its internal `handleForAgeDob(agePopup.id)` call has
  already run — so the only way to make the check true at read-time is to prime `relationToHead.value`
  from an earlier call on the *same* dataset instance (a first `ben == null` call with
  `relationToHeadId = 0`, whose own `ben == null` branch assigns `relationToHead.value` eagerly),
  then immediately follow it with a second, saved-`ben` call at the same `relationToHeadId`.
- `mapValueToBen leaves the ben untouched when the form rchId matches it exactly` and
  `mapValueToBen safely no-ops every ben field write when ben is null but rchId is populated` — close
  the `it != rchIdFromBen` equality branch (previously only its blank-value early-return and its
  not-equal arms were tested) and the null-`ben` path with a populated `rchId.value` (previously
  `mapValueToBen(null)` was only called with the form's `rchId` left at its default/blank value, never
  reaching the `isUpdated = true` / safe-call body).

## VLFRepo.kt — single-JSONObject `data` payload silently fails to save (real app bug, not fixed)

`getORSCampaignFromServer` / `getPulsePolioCampaignFromServer` explicitly handle a `data` field that
is a plain `JSONObject` (as opposed to a `JSONArray` or a stringified array) by calling
`saveORSCampaignFromServer(dataValue.toString())` / `saveFilariaMdaCampaignFromServer(...)` — i.e. the
raw object string, e.g. `{"id":34,"fields":{...}}`. The private `saveXXXFromServer(dataObj: String)`
helpers unconditionally do `org.json.JSONArray(dataObj)` on that string, which throws
`org.json.JSONException` ("A JSONArray text must start with '['") for a bare object. That exception is
caught by the helper's own internal `try/catch`, so nothing is ever saved to `VLFDao` — while the
outer `getXXXCampaignFromServer()` still returns `1` (success) regardless, because the `return@withContext 1`
in the `is JSONObject ->` branch happens unconditionally after calling the helper, not based on its
result. Net effect: a real single-object server payload for these two endpoints is silently dropped
(no `saveRecord` call, no error surfaced, misleading `1` return value implying success). The `is
JSONObject` branch would need to wrap the object as a one-element array (e.g. `"[$dataValue]"`) before
delegating, but that is a `src/main` behavior change, out of scope here. Tests
(`getORSCampaignFromServer stores a single json object` / `getPulsePolioCampaignFromServer stores a
single json object` in `VLFRepoTest.kt`) were adjusted to assert the actual observed behavior
(`saveRecord` NOT called) rather than the originally-intended one, so they still exercise the `is
JSONObject` branch for coverage purposes without asserting incorrect behavior.

## CbacViewModel.kt — `submitForm`'s HRP `when(reproductiveStatusId) { 5, 4 -> false; else -> true }` has dead match arms

`app/src/main/java/org/piramalswasthya/sakhi/ui/home_activity/non_communicable_diseases/cbac/CbacViewModel.kt`,
`submitForm()` (~lines 586-622). The `when` block that decides `flagForHrp` for `5, 4 -> false` is only
ever reached from inside `if (ben.genDetails?.reproductiveStatusId == 1 || ... == 2 || ... == 3)` — i.e.
by the time execution reaches the `when`, `reproductiveStatusId` is *guaranteed* to be `1`, `2`, or `3`.
The `5, 4 -> false` match arm can therefore never fire; `flagForHrp` is unconditionally `true` whenever
the outer guard and the female-symptom check both pass. This looks like a leftover from when the outer
guard's value set didn't match the `when`'s exclusion set (probably intended to exclude non-fertile
statuses from HRP flagging). Not fixed here (behavior change, out of scope) — new tests
(`submitForm treats reproductive status two/three as hrp eligible`, `submitForm skips the hrp block
when reproductive details are absent`) were written to assert the actual (always-true-when-reached)
behavior rather than the seemingly-intended one, closing the reachable branches of the outer `||` guard
(`reproductiveStatusId == 2`, `== 3`, and the `ben.genDetails == null` case) without pretending the dead
`5, 4` arm is reachable.

Also noted, not a bug but a recurring structural pattern across most of this ViewModel's `set*`
counter methods (`setCoughing`, `setFhTb`, `setUnsteady`, etc.): each does
`if (i == 1) _ast1.value = _ast1.value?.plus(1) else if (i == 2 && ast1.value!! > 0) ...`. Every
`_astX` `MutableLiveData<Int>` is constructed with a non-null initial value and is only ever reassigned
non-null `Int`s by this class's own setters, so the `?.`/`!!` null branches on `.value` can never be
taken through the public API. JaCoCo reports these as partially-missed branches (`"1 of 4"`, `"2 of 6"`
etc. on `ast1`/`ast2`/`astMoic` updates, `checkForReferral`-adjacent `referralList.value`/
`_completedReferrals.value` elvis checks, etc.) but they are dead code from any real call path, not a
coverage gap a test can close without reflection to force a null `LiveData` value.
