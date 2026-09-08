# Safir Scanner — Release Checklist

Legend: GREEN = evidence exists; YELLOW = implemented but still needs final QA/declaration; RED = release blocker; N/A = not applicable with reason.

## A. Product completeness
- [ ] GREEN — real document-scanning utility exists.
- [ ] GREEN — onboarding describes actual value and no account requirement.
- [ ] GREEN — Camera permission requested in context with Files fallback.
- [ ] GREEN — camera-unavailable path does not intentionally crash.
- [ ] GREEN — multi-page capture/import/editor/PDF/library flow exists.
- [ ] GREEN — PDF deletion requires confirmation.
- [ ] YELLOW — verify every visible control on small/large supported phones.
- [ ] RED — complete real-device reviewer walk-through from clean install.

## B. Stability / performance
- [ ] GREEN — lint + unit-test + debug APK + release AAB validation run in CI.
- [ ] GREEN — PDF export moved off UI thread and uses atomic result handling.
- [ ] GREEN — crop/rotate/filter heavy work guarded/off UI thread where applicable.
- [ ] GREEN — draft processing state prevents known OpenCV/editor race.
- [ ] GREEN — EXIF/large-image normalization exists.
- [ ] GREEN — native 16 KB compatibility check exists in CI.
- [ ] RED — stress test 1/10/20/50 page documents on real devices.
- [ ] RED — test low storage, corrupted input, process death, background/resume and repeated scan cycles.

## C. Identity / build
- [ ] GREEN — Android package is `com.safir.scan`.
- [ ] GREEN — targetSdk/compileSdk 36.
- [ ] YELLOW — versionCode/versionName correct for current development candidate.
- [ ] RED — lock final Play app record to exact package before any upload.
- [ ] RED — configure production upload signing / Play App Signing.
- [ ] RED — produce signed release AAB from controlled CI/Codemagic workflow.
- [ ] RED — tag exact store artifact commit after final approval gate.

## D. Accounts / login / connections
- [ ] N/A — account creation: app has no account system.
- [ ] N/A — sign-in/social login: app has no sign-in.
- [ ] N/A — delete account: no account can be created.
- [ ] N/A — reviewer demo credentials: all core functionality is available without login.
- [ ] GREEN — no document backend/cloud sync dependency in current product contract.
- [ ] RED if later added — any auth/backend/OAuth feature must reopen privacy, account deletion and reviewer-access gates.

## E. Permissions and user data
- [ ] GREEN — current functional permission scope is Camera only.
- [ ] GREEN — broad storage/location/contacts/mic/SMS/call-log/accessibility/overlay permissions are not part of product contract.
- [ ] GREEN — core data inventory exists.
- [ ] RED — final dependency/manifest audit on exact production AAB.
- [ ] RED — public HTTPS Privacy Policy that matches exact production SDK behavior.
- [ ] RED — in-app working Privacy Policy destination/text for production.
- [ ] RED — complete Play Data safety form from final SDK/data inventory.

## F. Advertising — business model FREE + ADS
- [ ] RED — add Google Mobile Ads SDK only after core gate stays green.
- [ ] RED — add Google UMP/CMP consent flow for applicable regions.
- [ ] RED — define safe ad placements and frequency caps.
- [ ] RED — use Google test ads during development.
- [ ] RED — create production AdMob app/ad units for exact `com.safir.scan` identity.
- [ ] RED — declare Contains ads in Play Console.
- [ ] RED — add developer website and valid root `app-ads.txt` with correct publisher entry.
- [ ] RED — update data inventory, Privacy Policy and Data safety after AdMob integration.
- [ ] RED — test consent reset/privacy-options path and ad failure/no-fill path.

Interstitial rule: never show a surprise full-screen ad after START SCAN but before the requested scan action begins. Do not cover capture/edit controls or force ad interaction to use the scanner.

## G. Public pages / support
- [ ] RED — production Privacy Policy URL.
- [ ] RED — production Support URL/contact path that responds.
- [ ] RED — developer website required for app-ads.txt.
- [ ] YELLOW — Terms/EULA decision documented; no subscription/account/UGC currently makes it lower risk but it can still be published.
- [ ] N/A — Delete Account URL while no account creation exists.

No fake URLs, `example.com`, TODO links or inactive pages may be present in the production binary/listing.

## H. Store listing / App content
- [ ] RED — final app title within Play metadata rules.
- [ ] RED — short description and full description that describe only shipping features.
- [ ] RED — real screenshots from final release UI; no fictitious screens/features.
- [ ] RED — final category.
- [ ] RED — target audience decision.
- [ ] RED — content rating questionnaire completed accurately.
- [ ] RED — App access declaration: no login/restrictions.
- [ ] RED — ads declaration after AdMob integration.
- [ ] RED — privacy/Data safety/app content answers reviewed against final AAB.

## I. Reviewer package
- [ ] YELLOW — reviewer walkthrough exists in `reviewer-notes.md`.
- [ ] RED — update reviewer notes after final AdMob/consent implementation.
- [ ] RED — record exact release version, commit/tag and artifact digest.
- [ ] RED — verify no backend/remote configuration can change reviewer-visible behavior unexpectedly during review.

## J. Final green gate
Submission is allowed only when:
1. every applicable RED above is GREEN;
2. YELLOW items have final evidence;
3. N/A items are still genuinely not applicable;
4. the exact signed AAB has passed the same reviewer path on real hardware;
5. Play listing, privacy, Data safety, ads declarations and the binary all tell the same story.
