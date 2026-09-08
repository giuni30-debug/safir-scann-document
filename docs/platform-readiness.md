# Platform / Declaration Readiness

This document separates product facts from platform fields so one app is never mixed with another Safir project.

## Immutable Android identity
- Repository: `giuni30-debug/safir-scann-document`
- Package/applicationId: `com.safir.scan`
- Product name: Safir Scanner
- Platform: Android mobile

Any Play Console, AdMob, Codemagic or signing operation must be checked against this identity before writing.

## Product facts to declare
- Utility: document scanner and local multi-page PDF creator.
- Account creation: no.
- Login: no.
- Document cloud sync/backend: no.
- Camera: yes, used for document capture; file import is an alternative.
- Current core advertising SDK: not yet integrated.
- Final business model: free with ads.
- In-app purchases/subscriptions: no.
- RevenueCat: N/A under current business model.

## Play App content fields — final values/decisions
- App access: all core functionality is accessible without login.
- Contains ads: YES only once the production AdMob build ships; declarations must match the uploaded artifact.
- Data safety: must be completed from the final dependency/data inventory after AdMob is present.
- Content rating: must be completed before release; unrated is not acceptable.
- Target audience: must be explicitly selected before production ad configuration. Do not accidentally target children unless the product and ad stack are intentionally built for Families requirements.

## Required public destinations
Before submission these must exist and work over HTTPS:
- Privacy Policy.
- Support URL/contact page.
- Developer website suitable for Play listing and `app-ads.txt`.
- Root `app-ads.txt` once AdMob publisher/app is finalized.

Delete Account URL is N/A while the app has no account creation.

## AdMob contract
- Create/link the AdMob Android app for exact package `com.safir.scan`.
- Keep App ID separate from individual ad unit IDs.
- Use test ad units in development.
- Implement UMP/privacy messaging and an accessible privacy-options path where required.
- Full-screen ads must not unexpectedly interrupt START SCAN or active scanning/editing.
- The app must continue functioning when an ad has no fill or fails to load.

## Release artifact contract
- Google Play receives AAB, not the debug APK.
- Production artifact must be signed with the approved upload/signing flow.
- versionCode must be unique/increasing.
- target API must remain compliant; current target is API 36.
- native OpenCV compatibility must pass the 16 KB gate.
- final artifact must be traceable to one Git commit/tag and its CI/Codemagic logs.

## Reviewer access statement
No demo account, OTP, invitation, subscription or special hardware is required beyond an ordinary Android device with either a camera or the ability to select image files. Reviewer notes must state this plainly.
