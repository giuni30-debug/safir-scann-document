# Safir Scanner — Release Checklist

Legend: GREEN = evidence exists; YELLOW = implemented but still needs final QA/declaration; RED = release blocker; N/A = not applicable with reason.

## A. Product completeness
- [ ] GREEN — real document-scanning utility exists.
- [ ] GREEN — onboarding describes actual value and no account requirement for core scanning.
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
Core scanner must remain usable without login unless a real account-backed function later makes login necessary.

- [ ] GREEN — guest/local scanning path exists and must remain available.
- [ ] RED — define the real account-backed value before enabling account creation; no decorative/unjustified login.
- [ ] RED — dedicated Firebase Authentication project/app mapped only to `com.safir.scan`; never reuse another Safir app identity.
- [ ] RED — Google sign-in via Android Credential Manager + Sign in with Google, using exact client IDs and release signing SHA fingerprints.
- [ ] RED — Email authentication fully implemented: verification, sign-in, reset/recovery, errors and logout.
- [ ] RED — Sign in with Apple on Android fully configured with Apple Services ID, valid HTTPS return URL, Team ID/Key ID/private key kept outside repo, and complete OAuth flow.
- [ ] RED — provider linking/identity-collision behavior tested so Google/Email/Apple do not silently create duplicate user identities.
- [ ] RED — session restore, logout and revoked/expired-token handling tested.
- [ ] RED — Settings > Account shows current identity/provider(s), logout and Delete account when auth is enabled.
- [ ] RED — account deletion removes associated data, not just disables the account.
- [ ] RED — public HTTPS Delete Account URL exists and works whenever account creation is exposed.
- [ ] RED — reviewer access does not depend on uncontrolled inbox/MFA; exact demo/reviewer path is documented when authenticated features require it.
- [ ] RED — OAuth scopes remain minimal: do not request Gmail/Drive/Calendar unless a real shipping feature needs them.
- [ ] RED — final Privacy Policy/Data safety/reviewer notes include auth identifiers/providers and backend behavior.

Detailed gate: `docs/auth-login-gate.md`.

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
- [ ] YELLOW — Terms/EULA decision documented.
- [ ] RED when accounts ship — public Delete Account URL.

No fake URLs, `example.com`, TODO links or inactive pages may be present in the production binary/listing.

## H. Store listing / App content
- [ ] RED — final app title within Play metadata rules.
- [ ] RED — short description and full description that describe only shipping features.
- [ ] RED — real screenshots from final release UI; no fictitious screens/features.
- [ ] RED — final category.
- [ ] RED — target audience decision.
- [ ] RED — content rating questionnaire completed accurately.
- [ ] RED — App access declaration matches final guest/auth behavior and includes reviewer credentials/instructions where required.
- [ ] RED — ads declaration after AdMob integration.
- [ ] RED — privacy/Data safety/app content answers reviewed against final AAB.

## I. Reviewer package
- [ ] YELLOW — reviewer walkthrough exists in `reviewer-notes.md`.
- [ ] RED — update reviewer notes after final AdMob/consent implementation.
- [ ] RED when auth ships — update reviewer notes with Google/Email/Apple access, demo path, logout and deletion path.
- [ ] RED — record exact release version, commit/tag and artifact digest.
- [ ] RED — verify no backend/remote configuration can change reviewer-visible behavior unexpectedly during review.

## J. Final green gate
Submission is allowed only when:
1. every applicable RED above is GREEN;
2. YELLOW items have final evidence;
3. N/A items are still genuinely not applicable;
4. the exact signed AAB has passed the same reviewer path on real hardware;
5. Play listing, privacy, Data safety, ads declarations, auth/account declarations and the binary all tell the same story.
