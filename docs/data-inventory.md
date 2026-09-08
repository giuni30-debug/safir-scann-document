# Data Inventory — Safir Scanner

Status: pre-AdMob build with gated authentication foundation and bundled on-device OCR.

## User content
- Camera-captured document images: processed locally on device.
- User-selected images: imported by user and processed locally.
- Temporary scan pages: app cache under `scan_draft`.
- OCR-selected images: temporary app cache under `ocr_imports`; cleared on fresh process start and replaced on the next OCR import.
- OCR share copies: temporary app cache under `share_exports`; cleared on fresh process start.
- Recognized OCR text: held in app UI memory for user review/edit; exported/shared only after explicit user action.
- Saved PDFs: app-private documents directory.
- Sharing occurs only after an explicit user Share/Export action through Android/system destinations.
- Authentication must never silently upload existing scans; account identity and document storage remain separate unless a future explicit sync feature is designed and re-reviewed.

## Permissions
- CAMERA: used only for document capture.
- Image/OCR input uses the system picker rather than broad media-library permission.
- No location, contacts, microphone, SMS, call log, notification, storage-wide, accessibility, overlay, or background location permission in the current product contract.

## OCR
The build uses the bundled ML Kit Latin text-recognition library. The model is packaged with the app, so OCR does not depend on downloading a recognition model at first use. Safir Scanner's OCR contract is on-device document recognition; scanned images/OCR text must not be uploaded by this feature or written to analytics/crash logs.

Current limitation: Latin-script recognition only in the first implementation. Unsupported scripts/poor-quality/no-text inputs must be presented as OCR limitations or failure/empty states, not fake success.

## Accounts / authentication foundation
The binary contains a gated Firebase Authentication foundation for Google, Email and Apple. `SAFIR_AUTH_ENABLED` defaults to false and provider controls must not be exposed unless the required public configuration is complete and the auth reviewer gate is green.

When authentication is enabled, the minimum expected identity surface is:
- Firebase internal user ID;
- sign-in provider identifier(s);
- email when supplied/required by the chosen provider;
- display name when supplied and used;
- email verification state where applicable.

The sign-in flow must not request Gmail contents, Drive files, Calendar data, Contacts or broader Google OAuth scopes for identity-only sign-in.

Account deletion, provider linking, session restore, logout and reauthentication are release requirements once account creation is exposed. Local PDFs remain device-controlled user files and are not automatically deleted with the remote identity unless the UI explicitly offers that action.

## Third-party code in current build
- AndroidX / Jetpack Compose
- CameraX
- OpenCV
- ML Kit Text Recognition (bundled Latin model)
- Firebase Authentication
- Android Credential Manager / Google ID credential library

These libraries and their exact production behavior must be re-audited from the final dependency graph/AAB before Data safety and Privacy Policy are finalized.

## Planned advertising change
When AdMob/UMP is added, this inventory and the Privacy Policy / Play Data safety declarations must be updated again before release. Do not keep a blanket 'no data collected' claim after authentication or advertising SDKs are integrated unless the final runtime behavior and applicable store guidance support that statement.
