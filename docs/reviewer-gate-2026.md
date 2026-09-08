# Reviewer Gate 2026 — Safir Scanner

This document is the release gate for reviewer readiness. The app does not move to Play Console submission until every applicable item is green.

## Product / functionality
- App installs, launches, resumes, and remains responsive.
- No crash, freeze, ANR, dead button, placeholder, fake feature, or misleading claim.
- Core flow works end to end: onboarding -> scan/import -> multi-page -> crop/rotate/filter/reorder/delete -> save PDF -> open/share/delete PDF.
- Camera denial does not block the app; file import remains available.
- Camera unavailable/bind failure falls back safely.
- App state survives backgrounding and interruptions; unfinished scan work is not silently lost.
- PDF creation is atomic: on failure, no incomplete PDF is presented as success.
- Large images and long documents are handled without blocking the UI thread or exhausting memory.

## Permissions / privacy
- Request only CAMERA and only when the user starts a camera flow.
- Explain why camera access is needed before/around the runtime request.
- Respect denial; offer import and Android Settings rather than pressuring the user.
- Privacy information is accessible in-app.
- Public Privacy Policy URL must be active before submission.
- Play Data safety answers must match the shipping binary and every SDK in it.

## Ads
- Final business model: FREE + ADS.
- RevenueCat, subscriptions and Play Billing are N/A unless product scope changes.
- AdMob/UMP is integrated only after core app stability is green.
- Interstitials appear only at logical breaks, never unexpectedly during an active scan/edit task.
- No interstitial at launch, no ad over camera/editor controls, no ad after every action.
- If an ad fails to load, the scan flow continues immediately.
- Test ads only in development builds; production IDs only in release.
- Contains Ads declaration, consent flow, privacy text, Data safety and SDK behavior must agree.

## Store / metadata
- Store title, description, screenshots and claims exactly match actual app behavior.
- No screenshots or copy promising functionality not present in the build.
- Target audience and content rating are completed accurately.
- Reviewer access instructions are supplied if any restricted path is introduced later.
- App is free; there is no account and therefore no account-deletion flow in the current product.

## Technical release
- applicationId: com.safir.scan
- compileSdk: 36
- targetSdk: 36
- Build lint + unit tests + debug APK + release AAB must pass.
- Production release must use a real upload/release signing identity; debug signing is forbidden.
- Release AAB is tested on a Play test track before production.
- Pre-review checks have no unresolved issue that could make the app non-functional or policy-inconsistent.

## Reviewer kill-list
Submission is blocked if any of these is true:
- crash / freeze / non-responsive screen
- unfinished or misleading feature
- broken Privacy/Support URL
- permission requested without a user-visible reason
- user cannot proceed after denying camera
- Data safety does not match SDK behavior
- unexpected/disruptive interstitials
- store metadata does not match the app
- debug build/signing used for release
- incomplete PDF can be reported as saved
- known critical bug is left for the reviewer to discover
