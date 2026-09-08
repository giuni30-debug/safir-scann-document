# Safir Scanner

Native Android document scanner / OCR utility prepared under Safir World Plan A-Z v2.0 reviewer-readiness gates.

## Product
Safir Scanner captures or imports document images, detects edges, corrects perspective, supports multi-page editing (crop, rotate, filters, reorder/delete), performs editable on-device OCR and exports local PDF/text/image results. Saved PDFs can be opened, shared through Android, or deleted by the user.

- Package: `com.safir.scan`
- Current app version: `0.2.0` / versionCode `2`
- Android target: API 36
- Minimum Android: API 26
- Core use: available without account
- Document backend/cloud sync: none
- Core runtime permission: CAMERA, optional because image import remains available
- OCR: bundled on-device Latin-script model
- Business model: free with ads; AdMob/UMP is intentionally not integrated until the core product gate is green
- Authentication: Google/Email/Apple foundation is gated and disabled by default until exact production provider configuration and a real account-backed product use pass the auth gate

## Build verification
CI runs lint, unit tests, debug APK, release AAB validation and native 16 KB page-size checks. CI does not publish to a store.

```bash
gradle :app:lint :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease
```

## A-Z v2 working method

`Inventory -> Functional Contract -> audit -> confirmed defects/root cause -> One Fix Batch -> focused regression -> full CI -> device evidence -> release gate.`

Do not launch a store build for every small edit. Device/store/auth/AdMob states remain separate evidence gates.

## Release documentation
- `docs/a-z-v2-audit-20260908.md` — current requirement / evidence / gap / action matrix
- `docs/app-manifest.json` — persistent identity/candidate/integration gate index
- `docs/app-product-spec.md` — exact product contract and user flow
- `docs/functional-contract-scanner-v2.md` — scanner/OCR feature acceptance contract
- `docs/integration-map-v2.md` — anti-mixup map for Play/Codemagic/Auth/AdMob
- `docs/data-inventory.md` — data/SDK/permission map
- `docs/accessibility-gate-v2.md` — accessibility evidence gate
- `docs/device-review-matrix-v2.md` — physical reviewer/device scenarios
- `docs/release-runbook-v2.md` — single-candidate release procedure
- `docs/incident-recovery-v2.md` — failure/recovery rules
- `docs/reviewer-reject-matrix.md` — reviewer rejection risks and controls
- `docs/release-checklist.md` — hard release gate
- `docs/reviewer-notes.md` — reviewer walkthrough draft
- `docs/auth-login-gate.md` — Google/Email/Apple gate
- `docs/admob-plan.md` — free-with-ads monetization gate
- `docs/repo-identity.md` — repository/package identity gate

## Release rule
A store upload is blocked while any applicable mandatory item is RED/UNKNOWN or dependent evidence is stale. Internal N/A items require a reason. `RELEASE_READY` is not the same as `STORE_APPROVED` or `LIVE_VERIFIED`.
