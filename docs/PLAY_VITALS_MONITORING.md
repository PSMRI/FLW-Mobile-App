# Play Memory & DEX Monitoring Checklist (FLW-1216)

Google Play starts penalising apps that break the memory, bitmap or DEX optimisation
thresholds in **February 2027** (lower visibility, restricted publishing). This checklist
keeps the fixes from FLW-1211…1215 from regressing.

## Thresholds (P90, rolling 28 days, Play installs only)

| Metric | Threshold |
|---|---|
| Dynamic memory (anon RSS + swap, foreground) | 4 GB device: **2 GB** · 6–8 GB: 2.25 GB · 12 GB: 3.25 GB · 16 GB: 4.25 GB |
| Bitmap memory outside foreground | Perceptible/background: **200 MB** · cached: **400 MB** |
| DEX optimisation | **≥ 25 %** of DEX shrunk/obfuscated/optimised (only when DEX > 10 MB) |

Sideloaded installs are not counted in vitals, but the penalty still applies to the Play listing.

## Play apps

| App | Package |
|---|---|
| Saksham | `org.piramalswasthya.sakhi.saksham` |
| Niramay | `org.piramalswasthya.sakhi.niramay` |
| Xushrukha | `org.piramalswasthya.sakhi.xushrukha` |
| Mitanin | `org.piramalswasthya.sakhi.mitanin` |

## When

- **After every Play release** (about 7 days after rollout, once vitals have data): sections 1–3.
- **Every release build:** section 4.
- **January 2027 checkpoint** (before enforcement): all sections, all apps, and record the result on FLW-1216.

## 1. Play Console vitals (per app)

Play Console → app → **Android vitals** → **Memory** and **App size / DEX optimisation**.

- [ ] Dynamic memory P90 is below the threshold for every device RAM bucket.
- [ ] Background and cached bitmap memory P90 is below 200 MB / 400 MB.
- [ ] DEX optimisation is ≥ 25 % (or DEX is below 10 MB).
- [ ] No metric shows a warning, and none moved more than 20 % toward its threshold since the last check.
      If one did, raise a ticket with the version where it started.

## 2. Crashlytics (per app)

Each flavor is a separate Firebase app, so pick the app first; this is the flavor filter.
Firebase console → **Crashlytics** → filter by latest version.

- [ ] Search issues for `OutOfMemoryError`. Note the count and affected users vs the previous version.
- [ ] No **new** OOM issue in the latest version. A new one → ticket with the top stack frame.
- [ ] Crash stack traces are de-obfuscated (readable class names). If not, the R8 mapping file
      upload failed for that build.

## 3. Firebase Performance (per app)

Firebase console → **Performance** → **Custom traces**. Compare the latest version's P90 with the
previous version's.

| Trace | What it times | Flag if P90 grows more than |
|---|---|---|
| `login` | `UserRepo.authenticateUser` | 30 % |
| `full_pull` | First / incremental full pull (attr `type`) | 30 % |
| `ben_download` | Beneficiary download in `PullFromAmritWorker` | 30 % |
| `sync_push` | Each push worker (attr `worker`, `result`) | 30 % |
| `db_open` | Room DB open (attr `kind`) | 30 % |
| `db_encrypt_check` | SQLCipher encryption check (release only) | 30 % |

- [ ] No trace grew past its limit. If one did, raise a ticket with trace name, attribute and versions.
- [ ] Network request success rate shows no new drop on the main API host.

## 4. Release build DEX (each release, any one flavor)

After `bundle<Flavor>Release`:

```bash
# Total DEX size in the bundle
unzip -l app/build/outputs/bundle/<flavor>Release/app-<flavor>-release.aab \
  | awk '/\.dex$/ {s+=$1; n++} END {printf "dex files: %d\ntotal dex: %.2f MB\n", n, s/1048576}'

# Share of app classes R8 renamed (proxy for obfuscation coverage; trend only)
grep -E '^org\.piramalswasthya\.sakhi\.\S+ -> \S+:$' app/build/outputs/mapping/<flavor>Release/mapping.txt \
  | awk '{t++; if ($1 != substr($3,1,length($3)-1)) r++} END {printf "app classes renamed: %d/%d (%.1f%%)\n", r, t, 100*r/t}'
```

- [ ] DEX size did not grow more than 10 % since the last release without a known reason (new library, etc.).
- [ ] Renamed share did not drop since the last release.

## 5. Keep-rule review (every PR)

CI job **PR Checks → proguard-keep-rules** (`.github/scripts/check_proguard_keeps.py`) checks
every `-keep` line a PR adds to a `.pro` file:

- It needs a `#` comment directly above it saying why (one comment can cover a group added together).
- It must not keep a wildcard class or package wholesale, e.g. `-keep class org.piramalswasthya.sakhi.** { *; }`
  or `-keepnames class com.foo.**`. Keep the specific classes or members instead.
- If a broad rule is truly unavoidable (a library that reflects over all its classes), write
  `# keep-broad-ok: <reason>` above it. The reviewer must agree with the reason.

Rules that existed before this check are not flagged; they are being narrowed under FLW-1211.

## Log

| Date | Version | App | Memory P90 | Bitmap BG/cached | DEX opt % | New OOMs | Trace regressions | By |
|---|---|---|---|---|---|---|---|---|
| | | | | | | | | |
