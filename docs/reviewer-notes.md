# Reviewer Notes — Draft

App: Safir Scanner
Package: com.safir.scan
Business model: Free with ads (AdMob planned after core-readiness gate)
Core account requirement: No
Primary permission: Camera

## What the app does
Safir Scanner captures or imports document images, detects/corrects document perspective, lets the user crop/rotate/filter/reorder pages, and creates local multi-page PDFs. Saved PDFs can be opened, shared through Android, or deleted by the user.

## Reviewer path — current gated build
1. Launch app and complete the one-time onboarding.
2. Tap Start Scan.
3. Either grant Camera permission or use Select Files without granting Camera.
4. Capture/import one or more pages.
5. Open Edit, test crop/rotate/filter/reorder/delete page.
6. Save the PDF.
7. From Home, open/share/delete the saved PDF.
8. Open Settings to view privacy/data, permission status, temporary-data control and version/build information.

## Account / authentication behavior
The core scanner remains usable without an account.

Google, Email and Apple authentication foundations are implemented behind a disabled-by-default release gate. The Account entry must not appear unless the exact Firebase/Google/Apple production configuration is complete. If authentication is enabled for the reviewed release, these notes must be replaced with the exact reviewer flow covering:
- Google sign-in;
- Email create/verify/sign-in/reset;
- Apple sign-in;
- provider linking where enabled;
- logout;
- in-app Delete account;
- public account-deletion URL;
- stable reviewer access that does not depend on uncontrolled MFA/inbox access.

Do not request Gmail, Drive or Calendar scopes for sign-in alone.

## Privacy / public links behavior
Scanned document content is processed locally by the scanner. Public Privacy, Support, Terms and developer-site buttons are configuration-gated and must not appear with blank or non-HTTPS destinations.

The final production URLs must be publicly reachable without login and must match the exact production SDK/data behavior. If accounts ship, the public Delete Account URL must also be live.

## Before production submission
This draft must be updated after final authentication decision/configuration and after AdMob/UMP integration so account behavior, advertising behavior, consent path, Privacy Policy, Data safety and reviewer instructions all match the exact signed production AAB.
