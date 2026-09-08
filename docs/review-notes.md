# Review Notes — Safir Scanner

Status: release-candidate template. Update again after production AdMob/UMP and signing are integrated.

## App access
No login, account, OTP, invitation, purchase or subscription is required. All scanner functionality is available without credentials.

## What to test
1. Launch and complete the one-time onboarding.
2. Tap Start Scan.
3. Grant Camera permission and capture a document, or deny Camera and use Select Files.
4. Add multiple pages.
5. Open Edit and test crop, rotate, filters, page reorder and page delete.
6. Save the document as PDF.
7. From Home, Open and Share the PDF, then test Delete confirmation.
8. Open Settings and verify Privacy & data, Account & connectivity, Camera permission, Storage and About/build identity.

## Product behavior
Safir Scanner processes scanned document content locally for its core scanner/PDF flow. It has no document cloud sync/backend and no account system. Android sharing occurs only after the user selects Share.

## Hardware/access fallback
A working camera is useful but not required to review the core workflow because the app can import image files. Camera permission denial must not block image-import review.

## Advertising
Business model is free with ads. Before submission this section must describe the exact production AdMob placements, UMP/privacy-options path and expected reviewer behavior. No production submission is allowed while this section still describes advertising as pending.

## Production links
Before review, verify the exact Privacy Policy URL, Support URL and developer website/app-ads.txt used in Play Console. They must be live and must match the final Data safety/SDK inventory.
