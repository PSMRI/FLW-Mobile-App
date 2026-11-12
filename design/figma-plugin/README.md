# FLW Redesign Builder (local Figma plugin)

A free, private development plugin that builds the pending FLW redesign work directly in the
**FLW App Redesign** Figma file, with no Figma MCP call limits. It doesn't touch the app code.
See `docs/FIGMA_REDESIGN_PLAN.md` for the overall plan.

## One-time setup
1. Install and open the **Figma desktop app** (development plugins can't be imported in the browser).
2. Open the file: https://www.figma.com/design/omOXvgnOhoVpNZf5NgtHWy
3. Menu **Plugins → Development → Import plugin from manifest…**
4. Select `design/figma-plugin/manifest.json` from this repo.

## Run
**Plugins → Development → FLW Redesign Builder →** choose:

| Command | What it builds |
|---|---|
| 1. Build pending screens | `07 · Register member – Step 1`, `08 · Sync`, `09 · Profile & more` on the **Screens** page |
| 2. Build Foundations page | `Foundations overview` frame (principles, colours, type, spacing, radius, elevation) on the **Foundations** page |
| Build everything pending | Both of the above |

A message at the bottom of Figma shows `✅ Built: …` or `❌ FLW builder error: …`.

Re-running is safe: the plugin first deletes its own earlier frames (names starting `07 ·`, `08 ·`, `09 ·`
and `Foundations overview`), then rebuilds them. **Manual edits to those frames are lost on re-run**,
so duplicate a frame before tweaking it by hand.

## After running
Send Claude a screenshot of the new frames (or export them as PNG) plus any error message.
Claude updates `code.js`; then run the same command again.

## Requirements in the file
The plugin finds everything by name, so don't rename these:
- Pages: `Foundations`, `Components`, `Screens`
- Components page: `Icons` frame and the component sets `Button`, `Text field`, `Filter chip`, `Sync badge`,
  `Avatar`, `FAB extended`, `Module tile`, `Due card`, `Status bar`, `Top app bar`, `Bottom navigation`,
  `Search bar`, `Section header`, `List item`, `Tag`, `Beneficiary card`, `Household card`
- Variables (`color/*`, `module/*`, `spacing/*`, `radius/*`), text styles and `Elevation/*` effect styles
