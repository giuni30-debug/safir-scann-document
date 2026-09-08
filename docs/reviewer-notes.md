# Reviewer Notes — Draft

App: Safir Scanner
Package: com.safir.scan
Business model: Free with ads (AdMob planned after core-readiness gate)
Core account requirement: No
Primary runtime permission: Camera

## What the app does
Safir Scanner captures or imports document images, detects/corrects perspective, supports multi-page crop/rotate/filter/reorder/delete, performs editable on-device OCR, and lets the user export/share PDF, image and text results. Saved PDFs can be opened, shared through Android, or deleted by the user.

## Reviewer path — core scanner
1. Launch app and complete the one-time onboarding.
2. Tap Start Scan.
3. Either grant Camera permission or use Select Files without granting Camera.
4. Capture/import one or more pages.
5. Open Edit and test crop/rotate/filter/reorder/delete page.
6. Tap OCR & TEXT. The processed scan pages are passed to the OCR screen.
7. Tap Recognize text, review/edit the recognized text, then test Share text or Export TXT. Test Share selected image for a processed page.
8. Return to the editor and Save PDF.
9. From Home, open/share/delete the saved PDF.
10. Open Settings to view privacy/data, permission status, temporary-data control and version/build information.

Expected data behavior for the core flow: scan pages, OCR and PDF generation stay on-device; content leaves the app only after the user's explicit Share/export action.

## Failure/permission checks
- Deny Camera: Select Files remains usable.
- Permanently deny Camera: Android Settings route is available.
- Camera unavailable: import remains available.
- OCR no-text/poor source: UI reports the state; user can edit text manually.
- PDF save failure: scanned pages remain available and no partial success is claimed.
- Delete/remove actions require confirmation.

## Account / authentication behavior
The core scanner remains usable without an account.

Google, Email and Apple authentication foundations are implemented behind a disabled-by-default release gate. The Account entry must not appear unless the exact Firebase/Google/Apple production configuration is complete. If authentication is enabled for the reviewed release, these notes must be replaced with the exact reviewer flow covering Google sign-in; Email create/verify/sign-in/reset; Apple sign-in; provider linking where enabled; logout; reauthentication; in-app Delete account; public account-deletion URL; and stable reviewer access that does not depend on uncontrolled MFA/inbox access.

Do not request Gmail, Drive or Calendar scopes for sign-in alone.

## Privacy / public links behavior
Public Privacy, Support, Terms and developer-site buttons are configuration-gated and must not appear with blank or non-HTTPS destinations. Final production URLs must be publicly reachable without login and match the exact production SDK/data behavior. If accounts ship, the public Delete Account URL must also be live.

## Advertising behavior
AdMob is not part of the core batch yet. When integrated, core scanning must continue when there is no fill, network failure or applicable consent refusal. Interstitials may appear only at a natural break, never over active scanning/login/permission actions. Internal review uses test ads only.

## Before production submission
Replace this draft with the exact signed-candidate version/build/SHA/config, physical-device evidence, final authentication state, AdMob/UMP behavior, public URLs, Data Safety/ads declarations and any reviewer credentials/instructions required by the final build.
