# QA Matrix — Reviewer-Critical Scenarios

A check is not green until tested on the exact release candidate or an equivalent signed/internal-test build.

## Install / launch
- Clean install → app opens without crash/blank screen.
- First launch → onboarding fits small screen and Continue works.
- Relaunch → onboarding does not repeat after completion.
- Background/resume repeatedly → state remains usable.
- Process killed while app is backgrounded → app recovers without corrupting saved PDFs.

## Camera / permissions
- Camera permission granted.
- Camera permission denied once.
- Camera permission denied permanently / system settings path used.
- Camera unavailable or bind failure → user can still use Files.
- Flash absent → flash control disabled safely.
- Flash present → torch toggles without crash.

## Import / image compatibility
- JPEG normal orientation.
- EXIF rotate 90/180/270.
- Large 12–50 MP image.
- Very narrow/tall image.
- Corrupt/zero-byte/unsupported image → error/fallback, no crash.
- Multiple imported images in one selection.

## Scan processing
- Document detected correctly under good light.
- No document detected → capture still possible/manual crop available.
- White document on light background.
- Dark/low-light document.
- Partial document / skew / shadows.
- Capture while automatic processor is active → editor cannot race file replacement.
- Repeated 20+ captures → no progressive freeze/native-memory exhaustion.

## Editor
- Crop success and cancel.
- Crop failure keeps original.
- Rotate repeatedly.
- Every filter repeatedly.
- Reorder pages forward/back.
- Delete a page.
- Delete last remaining page → flow returns safely.
- Rapid taps while edit is busy → no duplicated or conflicting operations.

## PDF
- 1-page PDF.
- 10-page PDF.
- 20-page PDF.
- 50-page stress PDF where device capacity permits.
- Failed/invalid page → no silently incomplete PDF.
- Low-storage/write failure → draft remains and user receives failure feedback.
- Successful save → only final PDF visible, no temporary output.
- Open PDF with compatible viewer.
- No PDF viewer installed → no app crash.
- Share PDF; cancel share sheet; share to another app.
- Delete PDF → confirmation required; Cancel keeps file; Delete removes file.

## Draft recovery
- Kill app after 1+ captured pages and reopen.
- Kill during automatic document processing and reopen.
- Ensure `.safirbase`, `.tmp`, backup and processing files are never restored as user pages.
- Clear temporary data from Settings.

## Settings / reviewer visibility
- Settings scrolls fully on small phone.
- Privacy & data wording matches current build.
- Account & connectivity accurately says no sign-in/cloud sync.
- Camera permission status changes after Android Settings change.
- Saved PDF / temporary data counts remain sensible.
- About shows correct app version/build/package.

## Offline / connectivity
Core scanner flow must remain usable without network before AdMob integration. After AdMob integration, ad/consent network failures must not block capture, edit, save, open or share.

## Advertising phase (after AdMob/UMP)
- Consent required / consent not required paths.
- Privacy options reset/change path.
- No-fill and ad-load failure.
- Airplane mode.
- Interstitial frequency cap.
- No interstitial unexpectedly between START SCAN tap and the requested scan action.
- No ad overlays on capture/editor controls.
- Test IDs in debug; real IDs only in release configuration.

## Release evidence
For each final candidate record: device model, Android version, test date, app versionCode/versionName, commit/tag, result, screenshots/logs for any failure and the fix commit.
