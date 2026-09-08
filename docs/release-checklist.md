# Safir Scanner — Release Checklist v2

Legend: GREEN = evidence exists for the stated scope; YELLOW = implementation/design exists but final evidence is missing; RED = release blocker; N/A = not applicable with reason.

Source standard: Safir World Plan A-Z v2.0 / 08.09.2026.

## A. Product completeness
- [ ] GREEN(code) — real native Android document-scanning utility exists.
- [ ] GREEN(code) — onboarding describes actual value and core guest access.
- [ ] GREEN(code) — Camera permission requested in context with Files fallback.
- [ ] GREEN(code) — camera-unavailable path has import fallback and no intentional crash path.
- [ ] GREEN(code) — capture/import -> perspective/manual crop -> multi-page edit -> local OCR -> PDF/image/text output paths exist.
- [ ] GREEN(code) — PDF and page deletion require confirmation.
- [ ] YELLOW — verify every visible control on small/large supported phones.
- [ ] RED — complete real-device reviewer walk-through from clean install.

Detailed product contract: `docs/functional-contract-scanner-v2.md`.

## B. Stability / performance
- [ ] GREEN(previous candidate) — lint + unit tests + debug APK + release AAB validation + 16 KB check passed for pre-batch candidate `c1bf0898...`.
- [ ] YELLOW — current v2 One Fix Batch must pass the same full CI gate once after the batch is frozen.
- [ ] GREEN(code) — PDF export is off the UI thread and uses atomic result handling.
- [ ] GREEN(code) — crop/rotate/filter heavy work is guarded/off UI thread where applicable.
- [ ] GREEN(code) — pending-draft state prevents the known OpenCV/editor race.
- [ ] GREEN(code) — EXIF/large-image normalization exists.
- [ ] GREEN(code) — recent OCR temp state is preserved across process recreation; stale cache is pruned instead of wiped immediately.
- [ ] RED — stress test 1/10/20/50-page documents on real devices.
- [ ] RED — test low storage, corrupted input, process death, background/resume and repeated scan/OCR cycles.
- [ ] RED — prove generated PDF reopens after app relaunch on the exact candidate.

## C. Scanner/OCR contract
- [ ] GREEN(code) — editable bundled on-device Latin-script OCR is reachable from processed scan pages.
- [ ] GREEN(code) — extracted text is editable before Share/TXT export.
- [ ] GREEN(code) — selected processed page can be shared through a scoped FileProvider URI.
- [ ] GREEN(code) — OCR direct imports are normalized/downsampled and limited to 50 pages / 25 MB per source before normalization.
- [ ] YELLOW — verify OCR empty/no-text, poor-quality and unsupported-script behavior on device.
- [ ] RED — verify PDF/image/TXT outputs can be opened by the receiving app / after relaunch as applicable.
- [ ] N/A — QR is not a shipping feature in candidate 0.2.0; no QR auto-open behavior exists.
- [ ] N/A — PDF input/password handling is not applicable because the app does not import PDFs.

## D. Accessibility / UI state
- [ ] GREEN(code) — camera capture and flash have explicit semantic name/state.
- [ ] GREEN(code) — page selectors and filters expose selected state; compact Home/editor actions use practical larger touch targets.
- [ ] GREEN(code) — detection, errors and destructive confirmation include visible text, not color alone.
- [ ] RED — TalkBack walkthrough on exact candidate.
- [ ] RED — large font/display scale, focus order, contrast, small/large device verification.

Accessibility gate: `docs/accessibility-gate-v2.md`.

## E. Identity / build / evidence
- [ ] GREEN — Android applicationId is `com.safir.scan`.
- [ ] GREEN(code) — compileSdk/targetSdk are currently 36 in repository configuration.
- [ ] YELLOW — refresh the official current Target API requirement immediately before the final candidate; do not infer PASS only from the plan year.
- [ ] YELLOW — versionCode/versionName are development-candidate values until final artifact freeze.
- [ ] RED — lock final Play app record to exact `com.safir.scan` before any upload.
- [ ] RED — configure production upload signing / Play App Signing.
- [ ] RED — record release/Play signing fingerprints used by Google OAuth.
- [ ] RED — lock Codemagic app/workflow for this exact repo/package and produce signed release AAB.
- [ ] RED — record final run ID, SHA/config and artifact digest.
- [ ] RED — physical-device evidence tied to exact build/device/OS/scenario.

## F. Accounts / Google / Email / Apple
Core scanner must remain usable without login.

- [ ] GREEN — guest/local scanning path exists and remains available.
- [ ] YELLOW — Google/Email/Apple auth code foundation exists behind `SAFIR_AUTH_ENABLED=false` by default.
- [ ] YELLOW — Settings exposes Account only when auth configuration is complete; Account screen includes sign-in, recovery, provider linking, logout and delete-account controls.
- [ ] RED — define/ship a real account-backed value before enabling account creation; no decorative login.
- [ ] RED — dedicated Firebase Authentication project/app mapped only to `com.safir.scan`.
- [ ] RED — Google sign-in verified end-to-end with exact client IDs and relevant release/Play signing fingerprints.
- [ ] RED — Email authentication verified end-to-end: signup, verification, sign-in, reset/recovery, expired/reused links, reauth, errors and logout.
- [ ] RED if Email auth ships — monitored support/privacy contacts plus MX/SPF/DKIM/DMARC and real delivery/bounce tests.
- [ ] RED — Sign in with Apple verified end-to-end with exact Services ID/return URL/Team ID/Key ID/provider secrets outside repo.
- [ ] RED — Apple private-relay delivery tested when relay addresses are used.
- [ ] RED — provider linking/identity-collision behavior tested by verified provider subject, not email similarity.
- [ ] RED — callback double-fire/cancel/timeout/wrong-audience/expired-session/logout-login/reinstall scenarios tested.
- [ ] RED — account deletion removes associated account data, revokes sessions/provider access where applicable and public HTTPS deletion resource works when accounts ship.
- [ ] RED — reviewer access does not depend on uncontrolled inbox/MFA when authenticated functions require access.
- [ ] GREEN(contract) — Gmail/Drive/Calendar scopes are forbidden for identity-only sign-in.
- [ ] RED — final Privacy/Data Safety/reviewer notes include actual auth identifiers/providers/backend behavior when enabled.

Detailed gates: `docs/auth-login-gate.md`, `docs/email-delivery-gate-v2.md`.

## G. Permissions, data and security
- [ ] GREEN(code) — core runtime permission scope is Camera; broad storage/location/contacts/mic/SMS/call-log/accessibility/overlay/background-location permissions are not part of the product contract.
- [ ] GREEN — data inventory includes local OCR and gated auth SDK impact.
- [ ] RED — final dependency/merged-manifest audit on exact production AAB.
- [ ] RED — verify no scanned page/OCR text/token/full auth link appears in analytics/crash/evidence logs.
- [ ] RED — public HTTPS Privacy Policy matches exact production SDK/runtime behavior.
- [ ] YELLOW — app contains a validated public-link gate; Privacy/Support/Terms buttons cannot appear with incomplete/non-HTTPS configuration.
- [ ] RED — complete Play Data Safety from final SDK/data inventory; do not copy another app's form.
- [ ] RED if accounts/backend ship — authorization tests prevent cross-account access and stale/replayed tokens.

## H. Advertising — business model FREE + ADS
- [ ] N/A — RevenueCat/Play Billing/IAP for candidate 0.2.0 because no paid digital access is sold.
- [ ] RED — integrate Google Mobile Ads only after core batch is green.
- [ ] RED — UMP/CMP consent and privacy-options re-entry for applicable regions/public.
- [ ] RED — define safe placements and frequency caps.
- [ ] RED — use Google test ads/devices during development/internal review.
- [ ] RED — create production AdMob app/ad units for exact `com.safir.scan` identity.
- [ ] RED — declare Contains ads in Play Console.
- [ ] RED — publish developer website and valid root `app-ads.txt`, then verify AdMob crawl/readiness separately.
- [ ] RED — update data inventory, Privacy Policy and Data Safety after final AdMob SDK/config.
- [ ] RED — test consent accept/refuse/privacy re-entry/no-fill/offline/background/frequency-cap paths.

Interstitial rule: only at a natural break. Never over active scanning, login confirmation or a control that encourages accidental taps. Core scanning remains usable when ad fill/consent is unavailable.

## I. Public pages / support
- [ ] YELLOW — public content pack is prepared in repo.
- [ ] GREEN(code) — build can enforce HTTPS Privacy, Support and developer-site URLs before those links are exposed.
- [ ] RED — production Privacy Policy URL published/reachable without authentication.
- [ ] RED — production Support URL/contact published, monitored and reachable.
- [ ] RED — developer website published for app identity/app-ads.txt.
- [ ] YELLOW — Terms/EULA decision documented; URL must be HTTPS if shipped.
- [ ] RED when accounts ship — public Delete Account URL published/reachable.
- [ ] GREEN(doc) — incident/recovery runbook exists; production support operation still requires a monitored contact.

No fake URLs, `example.com`, TODO links or inactive pages may be present in the production binary/listing.

## J. Store listing / App content
- [ ] RED — final title/short/full description locked to shipping features and limitations.
- [ ] RED — real screenshots from exact final UI; no fictitious features.
- [ ] RED — final category, target audience and content rating.
- [ ] RED — App access matches guest/auth behavior and includes reviewer credentials/instructions when required.
- [ ] RED — ads declaration after AdMob integration.
- [ ] RED — privacy/Data Safety/app-content answers reviewed against exact AAB.
- [ ] RED — upload accepted/processed/submitted/in-review/approved/live tracked as separate external states.

## K. Reviewer package / evidence
- [ ] YELLOW — reviewer walkthrough draft exists.
- [ ] RED — reviewer can reproduce core scan/OCR/output flow without live coordination.
- [ ] RED — update reviewer notes after final AdMob/auth/public-link configuration and physical test.
- [ ] RED — record exact release version/build/commit/config/workflow/run/artifact digest.
- [ ] RED — verify no backend/remote config can secretly hide/change reviewer-visible shipping behavior.
- [ ] RED — physical-device evidence includes device/OS, before/after observation, effect observed and scenario complete.

## L. Final green gate
Submission is allowed only when every applicable RED is GREEN, YELLOW has final evidence, N/A remains genuinely N/A, the exact signed AAB passed the same reviewer path on real hardware, and Play listing/privacy/Data Safety/ads/auth/account declarations all tell the same story.

`RELEASE_READY` is not `STORE_APPROVED`, does not authorize release, and does not guarantee approval.
