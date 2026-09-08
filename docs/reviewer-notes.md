# Reviewer Notes — Draft

App: Safir Scanner
Package: com.safir.scan
Business model: Free with ads (AdMob planned after core-readiness gate)
Account required: No
Primary permission: Camera

## What the app does
Safir Scanner captures or imports document images, detects/corrects document perspective, lets the user crop/rotate/filter/reorder pages, and creates local multi-page PDFs. Saved PDFs can be opened, shared through Android, or deleted by the user.

## Reviewer path
1. Launch app and complete the one-time onboarding.
2. Tap Start Scan.
3. Either grant Camera permission or use Select Files without granting Camera.
4. Capture/import one or more pages.
5. Open Edit, test crop/rotate/filter/reorder/delete page.
6. Save the PDF.
7. From Home, open/share/delete the saved PDF.
8. Open Settings to view privacy/data, permission status, temporary-data control, version/build information.

## Privacy behavior in the core build
Scanned document content is processed locally by the app. No user account is required and no document backend/cloud upload exists in the current core build.

## Before production submission
This draft must be updated after AdMob/UMP integration so the advertising behavior, consent path, privacy disclosures and Data safety answers match the exact production binary. Production Privacy Policy and Support URLs must be real and working before review.
