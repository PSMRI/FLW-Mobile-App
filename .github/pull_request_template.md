## Jira ticket
<!-- e.g. FLW-1234 -->

## What changed and why

## How it was tested
<!-- Flavors and build type tested, tests added or run -->

## Checklist
- [ ] Room schema changed → DB version bumped, new migration added and tested (no edits to old migrations)
- [ ] `proguard-rules.pro` changed → every new `-keep` is narrow and has a comment saying why (see `docs/PLAY_VITALS_MONITORING.md` §5)
- [ ] New image/bitmap code decodes with downsampling and does not keep bitmaps after the screen closes
