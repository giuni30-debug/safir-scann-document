# AdMob Production Plan — Safir Scanner

Business model: free application funded by advertising. No subscription, IAP or RevenueCat.

## Safety objective
Maximize sustainable ad revenue without creating a Google Play/AdMob reject risk or making scanning feel blocked by ads.

## Placement contract
### Home adaptive banner
Candidate placement: Home/library, visually separated from Start Scan and document action buttons. It must not be positioned to invite accidental taps.

### Interstitial
A full-screen ad is allowed only at a natural break. The production design must NOT trigger an unexpected interstitial after the user taps START SCAN but before the scan action they requested begins.

Preferred candidate: after a successful document save as the completed task transitions back to Home, with frequency control. The app must not interrupt capture, multi-page scanning, crop, filters, page ordering, PDF write or permission flows.

Suggested product cap for testing (not a Google policy number): at most one interstitial every 2 completed document sessions and never within 120 seconds of the previous interstitial. Tune only after policy/retention data is available.

### Rewarded
Do not add rewarded ads unless there is a genuine optional reward/feature that makes sense. Core scan/PDF capability must not be disguised as a forced rewarded ad gate without a separate product decision and policy review.

## Failure behavior
Ad load/no-fill/timeout/error must fail open: the user continues the app flow immediately. Never require an ad click or personal information submission to use the scanner.

## Consent/privacy
- Integrate Google UMP/CMP behavior for applicable regions.
- Provide a privacy-options path when required by the SDK/consent state.
- Test consent required, not required, unavailable network and consent reset/change cases.
- Re-audit Privacy Policy and Play Data safety after the exact Mobile Ads/UMP versions are locked.

## Environment separation
Debug/test builds use Google's test ads or designated test devices. Production ad unit IDs are not used during development interaction/testing. App ID and ad unit IDs are different identifiers and must not be mixed.

## Platform declarations
For the production advertising build:
- Play Console Contains ads = Yes.
- AdMob app must map to `com.safir.scan`.
- app-ads.txt must exist at the root of the developer website with the correct Google publisher entry.
- store developer website must point to the domain serving app-ads.txt.
- target audience/content rating and ad content settings must be mutually consistent.

## Reviewer notes
Final reviewer notes describe when ads can appear, confirm no account is needed, and explain the consent/privacy-options path if visible in the review region.

## Do-not-ship conditions
Do not ship when an interstitial can cover the camera/editor unexpectedly, when the ad blocks the requested action, when close/dismiss behavior is misleading, when the app fails if an ad is unavailable, or when Privacy/Data safety still reflects the pre-AdMob core build.
