# Safir Scanner — Release Checklist v2

Legend: GREEN = evidence exists; YELLOW = implemented but still needs final QA/declaration; RED = release blocker; N/A = not applicable with reason.

Source standard: `docs/plan-v2-refresh.md` + Safir World Plan A-Z v2.0 / 08.09.2026.

## A. Product completeness
- [ ] GREEN — real native document-scanning utility exists.
- [ ] GREEN — onboarding describes actual value and no account requirement for core scanning.
- [ ] GREEN — Camera permission requested in context with Files fallback.
- [ ] GREEN — camera-unavailable path does not intentionally crash.
- [ ] GREEN — multi-page capture/import/editor/PDF/library flow exists.
- [ ] GREEN — PDF deletion requires confirmation.
- [ ] RED — editable OCR exists inside the scanner workflow for selected page(s).
- [ ] RED — first-class image export/share path exists and produces an externally openable image.
- [ ] RED — first-class text export/share path exists after user-editable OCR review.
- [ ] YELLOW — define/test supported image size and page-count limits; reject/downsample safely.
- [ ] N/A — password-protected PDF input while the product does not import PDFs; reclassify if PDF import is added.
- [ ] RED — production accessibility pass: TalkBack, font scaling, focus, contrast and touch targets.
- [ ] RED — complete real-device reviewer walk-through from clean install.

Detailed product contract: `docs/functional-contract-scanner-v2.md`.
Accessibility gate: `docs/accessibility-gate-v2.md`.

## B. Stability / performance
- [ ] GREEN — lint + unit-test + debug APK + release AAB validation run in CI.
- [ ] GREEN — PDF export moved off UI thread and uses atomic result handling.
- [ ] GREEN — crop/rotate/filter heavy work guarded/off UI thread where applicable.
- [ ] GREEN — draft processing state prevents known OpenCV/editor race.
- [ ] GREEN — EXIF/large-image normalization exists.
- [ ] GREEN — native 16 KB compatibility check exists in CI.
- [ ] RED — stress test 1/10/20/50 page documents on real devices.
- [ ] RED — test low storage, corrupted input, process death, background/resume and repeated scan cycles.
- [ ] RED — prove generated PDF reopens after app relaunch on the exact candidate.
- [ ] RED — once OCR ships, test no-text/poor-quality/unsupported-script/timeout paths without fake success.

## C. Identity / build / evidence
- [ ] GREEN — Android package is `com.safir.scan`.
- [ ] GREEN — compileSdk/targetSdk currently set to 36.
- [ ] YELLOW — refresh the official current Target API requirement again immediately before the final candidate; the v2 PDF itself leaves that source unverified.
- [ ] YELLOW — versionCode/versionName correct for current development candidate.
- [ ] RED — lock final Play app record to exact package before any upload.
- [ ] RED — configure production upload signing / Play App Signing.
- [ ] RED — record release-signing fingerprints used by Google OAuth.
- [ ] RED — produce signed release AAB from controlled CI/Codemagic workflow.
- [ ] RED — record candidate commit, config version, workflow/run ID and artifact digest.
- [ ] RED — tag exact store artifact commit only after final internal approval gate.
- [ ] RED — physical-device evidence tied to exact build/device/OS/scenario.

## D. Accounts / Google / Email / Apple
Core scanner must remain usable without login unless a real account-backed function later makes login necessary.

- [ ] GREEN — guest/local scanning path exists and must remain available.
- [ ] YELLOW — Google/Email/Apple auth code foundation exists behind a disabled-by-default release gate; provider buttons stay hidden until real configuration is complete.
- [ ] YELLOW — Settings exposes Account only when auth configuration is complete; Account screen includes sign-in, recovery, provider linking, logout and delete-account controls.
- [ ] RED — define the real account-backed value before enabling account creation; no decorative/unjustified login.
- [ ] RED — dedicated Firebase Authentication project/app mapped only to `com.safir.scan`; never reuse another Safir app identity.
- [ ] RED — Google sign-in verified end-to-end with exact web/server client, Android package and relevant release/Play signing fingerprints.
- [ ] RED — Email authentication verified end-to-end: signup, verification, sign-in, reset/recovery, expired/reused links, reauth, errors and logout.
- [ ] RED if Email auth ships — monitored support/privacy contacts plus MX/SPF/DKIM/DMARC and real delivery/bounce tests.
- [ ] RED — Sign in with Apple verified end-to-end with exact Services ID/return URL/Team ID/Key ID/provider secrets outside repo.
- [ ] RED — Apple private-relay delivery tested when relay addresses are used.
- [ ] RED — provider linking/identity-collision behavior tested by verified provider subject, not email similarity.
- [ ] RED — callback double-fire/cancel/timeout/wrong-audience/expired-session/logout-login/reinstall scenarios tested.
- [ ] RED — account deletion removes associated account data, revokes sessions/provider access where applicable and survives restore/reinstall semantics.
- [ ] RED — public HTTPS Delete Account URL exists and works whenever account creation is exposed.
- [ ] RED — reviewer access does not depend on uncontrolled inbox/MFA; exact demo/reviewer path is documented when authenticated features require it.
- [ ] GREEN — OAuth contract explicitly forbids Gmail/Drive/Calendar scopes unless a real shipping feature needs them.
- [ ] RED — final Privacy Policy/Data safety/reviewer notes include auth identifiers/providers and backend behavior.

Detailed gates: `docs/auth-login-gate.md`, `docs/email-delivery-gate-v2.md`.

## E. Permissions, data and security
- [ ] GREEN — current functional permission scope is Camera only.
- [ ] GREEN — broad storage/location/contacts/mic/SMS/call-log/accessibility/overlay permissions are not part of product contract.
- [ ] GREEN — core data inventory exists and includes gated auth SDK impact.
- [ ] RED — final dependency/manifest audit on exact production AAB.
- [ ] RED — verify no scanned page/OCR text/token/full auth link appears in analytics/crash/evidence logs.
- [ ] RED — public HTTPS Privacy Policy matches exact production SDK/runtime behavior.
- [ ] YELLOW — app contains a validated public-link gate; Privacy/Support/Terms buttons cannot appear with incomplete/non-HTTPS configuration.
- [ ] RED — in-app working Privacy Policy destination for production URLs.
- [ ] RED — complete Play Data safety form from final SDK/data inventory.
- [ ] RED if accounts/backend ship — authorization tests prevent cross-account access and stale/replayed tokens.

## F. Advertising — business model FREE + ADS
- [ ] RED — add Google Mobile Ads SDK only after core gate stays green.
- [ ] RED — add Google UMP/CMP consent flow for applicable regions/public.
- [ ] RED — define safe placements and frequency caps.
- [ ] RED — use Google test ads/devices during development and internal review.
- [ ] RED — create production AdMob app/ad units for exact `com.safir.scan` identity.
- [ ] RED — declare Contains ads in Play Console.
- [ ] RED — publish developer website and valid root `app-ads.txt`, then verify AdMob crawl/readiness status separately.
- [ ] RED — update data inventory, Privacy Policy and Data safety after AdMob integration.
- [ ] RED — test consent accept/refuse/privacy re-entry/no-fill/offline/background/frequency-cap paths.

Interstitial rule: only at a natural break. Never over active scanning, login confirmation or a control that encourages accidental taps. Core scanning remains usable when ad fill/consent is unavailable.

## G. Public pages / support
- [ ] YELLOW — final content pack prepared in `docs/public-pages-content.md`.
- [ ] GREEN — build can enforce HTTPS Privacy, Support and developer-site URLs before those links are exposed.
- [ ] RED — production Privacy Policy URL published and reachable without authentication.
- [ ] RED — production Support URL/contact path published, monitored and reachable.
- [ ] RED — developer website published for app identity and app-ads.txt.
- [ ] YELLOW — Terms/EULA content decision documented; if shipped, URL must be HTTPS.
- [ ] RED when accounts ship — public Delete Account URL published and reachable.
- [ ] RED — incident/support procedure records a support path and recoverable diagnostics without sensitive content.

No fake URLs, `example.com`, TODO links or inactive pages may be present in the production binary/listing.

## H. Store listing / App content
- [ ] RED — final app title within Play metadata rules.
- [ ] RED — short/full descriptions describe only shipping features and actual limitations.
- [ ] RED — real screenshots from final release UI; no fictitious screens/features.
- [ ] RED — final category.
- [ ] RED — target audience decision.
- [ ] RED — content rating questionnaire completed accurately.
- [ ] RED — App access declaration matches final guest/auth behavior and includes reviewer credentials/instructions where required.
- [ ] RED — ads declaration after AdMob integration.
- [ ] RED — privacy/Data safety/app content answers reviewed against final AAB.
- [ ] RED — upload accepted/processed/submitted/in-review/approved/live are tracked as separate external states.

## I. Reviewer package
- [ ] YELLOW — reviewer walkthrough exists in `reviewer-notes.md`.
- [ ] RED — reviewer can reproduce core scan flow without live coordination.
- [ ] RED — update reviewer notes after final OCR/export/accessibility behavior is fixed.
- [ ] RED — update reviewer notes after final AdMob/consent implementation.
- [ ] RED when auth ships — update reviewer notes with Google/Email/Apple access, demo path, logout, reauth and deletion path.
- [ ] RED — record exact release version/build/commit/config/artifact digest.
- [ ] RED — verify no backend/remote configuration can change reviewer-visible behavior unexpectedly during review.
- [ ] RED — reviewer/support backend/providers are healthy for the review window.

## J. Evidence / invalidation discipline
- [ ] GREEN — code changes are not treated as device verification.
- [ ] RED — each applicable gate has evidence reference, timestamp, candidate version and outcome.
- [ ] RED — new build invalidates device evidence for the changed candidate.
- [ ] RED — auth config change invalidates auth/session evidence.
- [ ] RED — new SDK/provider invalidates dependent data/privacy/consent audit.
- [ ] RED — unknown/stale applicable evidence blocks final GREEN.

## K. Final green gate
Submission is allowed only when:
1. every applicable RED above is GREEN;
2. YELLOW items have final evidence;
3. N/A items are still genuinely not applicable;
4. the exact signed AAB has passed the same reviewer path on real hardware;
5. Play listing, privacy, Data safety, ads declarations, auth/account declarations and the binary all tell the same story;
6. `RELEASE_READY` is treated as separate from `STORE_APPROVED` and `LIVE_VERIFIED`.
