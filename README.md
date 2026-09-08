# Safir Scanner

Native Android document scanner prepared under the repository's 2026 reviewer-readiness gate.

## Product
Safir Scanner captures or imports document images, detects document edges, corrects perspective, supports multi-page editing (crop, rotate, filters, reorder/delete) and exports local PDF files. Saved PDFs can be opened, shared through Android, or deleted by the user.

- Package: `com.safir.scan`
- Current app version: `0.2.0` / versionCode `2`
- Android target: API 36
- Minimum Android: API 26
- Account: none
- Document backend/cloud sync: none
- Core permission: CAMERA, optional because image import remains available
- Business model: free with ads; AdMob/UMP is intentionally not integrated until the core product gate is green

## Build verification
CI runs lint, unit tests, debug APK, release AAB validation and native 16 KB page-size checks.

```bash
gradle :app:lint :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease
```

## Release documentation
- `docs/app-product-spec.md` — exact product contract and user flow
- `docs/reviewer-reject-matrix.md` — what a reviewer could reject and our control/evidence
- `docs/release-checklist.md` — hard release gate
- `docs/data-inventory.md` — data/SDK/permission map
- `docs/review-notes.md` — exact reviewer walkthrough
- `docs/policy-evidence-2026.md` — official Google/Android policy evidence used by the gate
- `docs/store-listing-draft.md` — factual Play listing draft
- `docs/platform-readiness.md` — platform declarations and blockers
- `docs/repo-identity.md` — anti-mixup identity gate
- `docs/reviewer-gate-2026.md` — policy-oriented reviewer gate

## Release rule
A store upload is blocked while any applicable mandatory item in `docs/release-checklist.md` is red. Internal N/A items must include the reason they do not apply.
