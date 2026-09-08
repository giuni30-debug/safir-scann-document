# Safir Scanner — Product Contract

This file defines what the production app is and what a reviewer must actually see. Store metadata, Privacy Policy, Data safety, advertising declarations and the shipping binary must match this contract.

## 1. Identity
- Product name: Safir Scanner
- Android package: `com.safir.scan`
- Platform in this repository: Android mobile
- Product type: native document scanner / PDF utility
- Business model: free with advertising
- Account requirement: none
- Current document cloud/backend: none

## 2. Primary value
A user can turn camera captures or imported images into a multi-page PDF without creating an account. The scanner provides document detection/perspective correction and manual editing controls.

## 3. First-run experience
1. Show a concise onboarding that explains the product value.
2. Do not request Camera permission at app launch.
3. Request Camera only when the user enters document capture.
4. Explain why Camera is used before/alongside the permission flow.
5. Keep image import available when Camera is denied.

## 4. Home/library
Home must provide a clear Start Scan action and a list of saved PDFs. Saved-document actions are Open, Share and Delete. Destructive deletion requires confirmation.

No dead buttons, fake premium controls, hidden login requirement, placeholder content or unavailable declared feature may appear in a release build.

## 5. Capture/import flow
- Camera capture using CameraX.
- File import for images.
- Multi-page session.
- Live document detection as assistance, not a requirement to capture.
- Capture failure and camera-unavailable states must not crash the app.
- A denied Camera permission must not make the app unusable because file import remains available.

## 6. Processing/editor
The editor supports:
- perspective crop/manual crop;
- rotate;
- Original, Color+, Gray, B&W and Contrast filters;
- page reorder;
- page deletion;
- multi-page PDF export.

Heavy image/OpenCV/PDF work must not block the UI long enough to appear frozen or trigger ANR. Processing state must prevent editing a file while automatic processing is still replacing it.

## 7. PDF result
PDF export is treated as a transaction:
- every selected page must be readable;
- a failed page may not be silently skipped;
- the final PDF is written atomically;
- failure keeps the scan draft available;
- success returns the user to a library that shows the new PDF.

## 8. Draft/recovery
A scan in progress must be recoverable after normal activity/process recreation when usable draft pages still exist. Temporary processing files and editor base files are not treated as user pages.

## 9. Settings contract
Settings must remain usable on small screens and expose the information a reviewer/user needs:
- Privacy & data behavior;
- Account & connectivity status;
- Camera permission status and a route to Android app settings;
- local storage / temporary data controls;
- app version, build and package identity.

Production release additionally requires working Privacy Policy and Support destinations. These may not be fake, placeholder or dead URLs.

## 10. Accounts and connections
Current production design has no account creation, sign-in, social login, OAuth, user profile, remote document storage or cloud sync. Therefore account deletion, reviewer demo credentials and Sign in with Apple are N/A for this Android build unless an account feature is later added.

If any account or remote service is added later, this contract, Data Safety, Privacy Policy and reviewer instructions must be changed before upload.

## 11. Permissions
Current app permission scope is intentionally narrow:
- CAMERA — document capture.

The app must not add location, contacts, microphone, SMS/call log, broad storage, accessibility, overlay, background location or notification permissions unless a real user-facing feature requires them and the relevant policy/disclosure gate is completed.

## 12. User data
Document images and generated PDFs are core user content. The current scanner flow processes them locally. Sharing occurs only after the user's explicit Share action.

Third-party SDK behavior is part of the app's data story. After AdMob/UMP integration, advertising/device data behavior must be added to the data inventory, Privacy Policy and Play Data safety answers.

## 13. Advertising contract
Advertising is not allowed to break the scanner task. Production advertising must use test IDs during development and real IDs only for release.

Do not show a full-screen interstitial unexpectedly after the user taps Start Scan but before the requested scan action begins. Interstitials belong only at a natural break and must be dismissible according to Google policy. Never place ads over capture controls, crop/edit gestures, permission dialogs or system UI.

Consent/privacy choices must be implemented for applicable regions before production AdMob serving.

## 14. Store/reviewer truth
The listing may describe only features present in the exact release build. Screenshots must come from the real app experience. If the app has ads, Play must declare Contains ads. If no account exists, review instructions explicitly say all functionality is available without login.

## 15. Release definition
The product is not release-ready merely because it compiles. Release-ready means the applicable rows in `release-checklist.md` are green, CI is green, real-device QA is complete, production policy/support pages work, production declarations match the SDK/data inventory and the signed AAB is the tested artifact.
