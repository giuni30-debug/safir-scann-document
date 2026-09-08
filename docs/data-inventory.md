# Data Inventory — Safir Scanner

Status: pre-AdMob core build.

## User content
- Camera-captured document images: processed locally on device.
- User-selected images: imported by user and processed locally.
- Temporary scan pages: app cache under scan_draft.
- Saved PDFs: app-private documents directory.
- Sharing occurs only after an explicit user Share action through Android's share sheet.

## Permissions
- CAMERA: used only for document capture.
- No location, contacts, microphone, SMS, call log, notification, storage-wide, accessibility, overlay, or background location permission in the current core build.

## Accounts / backend
- No account creation or login.
- No backend upload for scanned documents.
- No cloud sync.
- No account deletion requirement while account creation remains absent.

## Third-party code in current core build
- AndroidX / Jetpack Compose
- CameraX
- OpenCV

These libraries must be re-audited at release time for the exact shipping dependency graph.

## Planned advertising change
When AdMob/UMP is added, this inventory and the Privacy Policy / Play Data safety declarations must be updated before release. Do not keep a blanket 'no data collected' claim after an advertising SDK is integrated unless the final SDK behavior and Play guidance support that statement.
