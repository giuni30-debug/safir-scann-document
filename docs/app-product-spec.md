# Safir Scanner — Product Contract

This file defines what the production app is and what a reviewer must actually see. Store metadata, Privacy Policy, Data Safety, advertising declarations and the shipping binary must match this contract.

## 1. Identity
- Product name: Safir Scanner
- Android package: `com.safir.scan`
- Platform in this repository: Android mobile
- Product type: native document scanner / OCR / PDF utility
- Business model: free with advertising
- Core account requirement: none
- Current document cloud/backend: none

## 2. Primary value
A user can turn camera captures or imported images into processed multi-page documents, edit pages, run local OCR, review/edit extracted text, and export/share PDF, image or text results without creating an account.

## 3. First-run experience
1. Show a concise onboarding that explains the product value.
2. Do not request Camera permission at app launch.
3. Request Camera only when the user enters document capture.
4. Explain why Camera is used before/alongside the permission flow.
5. Keep image import available when Camera is denied.

## 4. Home/library
Home provides Start Scan and a list of saved PDFs. Saved-document actions are Open, Share and Delete. Destructive deletion requires confirmation.

No dead buttons, fake premium controls, hidden login requirement, placeholder content or unavailable declared feature may appear in a release build.

## 5. Capture/import flow
- Camera capture using CameraX.
- File/image import through Android's system picker.
- Multi-page session.
- Live document detection as assistance, not a requirement to capture.
- Capture failure and camera-unavailable states must not crash the app.
- A denied Camera permission must not make the app unusable because file import remains available.
- Removing a captured page requires an explicit confirmation step.

## 6. Processing/editor
The editor supports:
- automatic perspective correction where detected + manual crop fallback;
- rotate;
- Original, Color+, Gray, B&W and Contrast filters;
- page reorder;
- page deletion with confirmation;
- editable on-device OCR launched from the processed scan pages;
- image/text share/export;
- multi-page PDF export.

Heavy image/OpenCV/PDF work must not block the UI long enough to appear frozen or trigger ANR. Processing state must prevent editing a file while automatic processing is still replacing it.

## 7. OCR result
OCR uses a bundled on-device Latin-script model. Processed document images are not uploaded merely to recognize text.

- OCR source may be processed scan pages or images explicitly selected by the user.
- Imported OCR images are normalized/downsampled before recognition.
- Extracted text is editable before Share/TXT export.
- Empty/no-text recognition is a valid failure/empty state, not fake success.
- OCR text and source document pages must not be written into analytics/crash logs.

## 8. PDF result
PDF export is treated as a transaction:
- every selected page must be readable;
- a failed page may not be silently skipped;
- the final PDF is written atomically;
- failure keeps the scan draft available;
- success returns the user to a library that shows the new PDF.

## 9. Draft/recovery
A scan in progress must be recoverable after normal activity/process recreation when usable draft pages still exist. Recent OCR temp inputs are retained across process recreation and stale cache is pruned later instead of being wiped immediately. Temporary processing/editor helper files are not treated as user pages.

## 10. Settings contract
Settings must remain usable on small screens and expose the information a reviewer/user needs:
- Privacy & data behavior;
- Account & connectivity status;
- Camera permission status and a route to Android app settings;
- local storage / temporary data controls;
- app version, build and package identity.

Production release additionally requires working Privacy Policy and Support destinations. These may not be fake, placeholder or dead URLs.

## 11. Accounts and connections
Core scanning remains usable without account creation. The repository contains a gated Google/Email/Apple authentication foundation, disabled by default unless exact production configuration is provided and the auth gate passes.

Auth must not be enabled only to collect identity or display decorative provider buttons. If it ships, the app must provide a real account-backed value and complete login/recovery/session/logout/delete-account behavior. Gmail/Drive/Calendar scopes are not requested for identity-only sign-in.

## 12. Permissions
Current app runtime permission scope is intentionally narrow:
- CAMERA — document capture.

The app must not add location, contacts, microphone, SMS/call log, broad storage, accessibility, overlay, background location or notification permissions unless a real user-facing feature requires them and the relevant policy/disclosure gate is completed.

## 13. User data
Document images, OCR text and generated PDFs are core user content. The scanner/OCR flow processes them locally. Sharing occurs only after the user's explicit Share/export action.

Third-party SDK behavior is part of the app's data story. The final dependency graph/AAB controls Privacy Policy and Data Safety answers. After AdMob/UMP integration, advertising/device-data behavior must be added to the data inventory and store declarations.

## 14. Accessibility
- Icon/compact camera controls have meaningful semantics/state.
- Page selectors and filters expose selected state, not only color.
- Critical Home/editor actions use practical minimum touch targets.
- Detection/errors/destructive confirmation are accompanied by visible text.
- TalkBack, font scaling, focus order and contrast remain mandatory physical-device evidence gates.

## 15. QR scope
QR detection is not a shipping feature in candidate 0.2.0 and is therefore N/A. If added later, QR/URL output may not auto-open a potentially unsafe address without a separate safety design/test gate.

## 16. Advertising contract
Advertising is not allowed to break the scanner task. Production advertising must use test IDs during development and real IDs only for release.

Do not show a full-screen interstitial unexpectedly after the user taps Start Scan but before the requested scan action begins. Interstitials belong only at a natural break and must not cover active capture/edit/login/permission flows. No-fill or consent refusal must not block the scanner.

Consent/privacy choices must be implemented for applicable regions before production AdMob serving.

## 17. Store/reviewer truth
The listing may describe only features present in the exact release build. Screenshots must come from the real app experience. If the app has ads, Play must declare Contains ads. If account access is disabled, review instructions say all core scanning functionality is available without login.

## 18. Release definition
The product is not release-ready merely because it compiles. Release-ready means applicable gates are green, CI is green for the frozen candidate, real-device QA is complete, production policy/support pages work, production declarations match the SDK/data inventory and the signed AAB is the tested artifact.
