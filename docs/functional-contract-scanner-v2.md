# Safir Scanner — Functional Contract v2

This contract implements the scanner-specific acceptance model from Safir World Plan A-Z v2.0. Statuses below are implementation states, not store approval.

## Product scope
Document scanner / OCR utility for Android. Core use remains available without an account. Document content is intended to stay on-device unless the user explicitly shares/exports it.

## Feature contracts

### `scanner.capture`
Input: camera permission + visible document.
Result: non-empty JPEG draft page stored in app cache.
Failure states: permission denied, camera unavailable/busy, capture error, zero-byte output.
Postcondition: page can be reopened and processed after app resume/restart.
Current: GREEN in code; physical-device evidence still RED.

### `scanner.import_image`
Input: user-selected image through system document picker.
Result: imported page copied into app-private draft storage without broad media-library permission.
Failure states: unreadable URI, corrupt/empty image, unsupported format.
Current: GREEN in code; corrupt/large-file device evidence still RED.

### `scanner.perspective_crop`
Input: draft page.
Result: automatic perspective correction where detected, with manual crop fallback.
Failure states: no document detected, OpenCV failure, extreme geometry.
Current: GREEN/YELLOW; low-light/skew/background regression evidence required.

### `scanner.multi_page_edit`
Input: one or more pages.
Result: reorder, rotate, crop, filter, delete with original-page safety and clear failure feedback.
Current: GREEN in code; 20/50-page stress and memory evidence RED.

### `scanner.ocr_editable`
Input: processed page(s) from the scan editor or images selected directly by the user.
Result: OCR text produced locally with the bundled Latin-script ML Kit model, shown in an editable field before export.
Failure states: no text, unsupported script, poor quality, unreadable input, recognizer failure.
Data rule: no document text may appear in analytics/crash logs.
Current: GREEN in code after the v2 core batch; CI and physical-device evidence still required.

### `scanner.export_pdf`
Input: selected pages.
Result: real non-empty PDF written atomically to app-private documents storage and reopenable after relaunch.
Failure states: page decode error, low storage, write/fsync/rename failure.
Current: GREEN in code; low-storage/reopen-on-device evidence RED.

### `scanner.export_image`
Input: selected processed OCR/source page.
Result: explicit Android share action through a scoped FileProvider URI; another app must be able to open the shared image.
Current: GREEN in code; external receiving-app/device evidence still RED.

### `scanner.export_text`
Input: user-reviewed OCR text.
Result: explicit Share or TXT export. Text remains editable before export.
Current: GREEN in code; external receiving-app/TXT reopen evidence still RED.

### `scanner.library`
Result: saved PDF list, open, explicit share, confirmed delete.
Current: GREEN in code; external viewer/share failure regression still YELLOW.

### `scanner.settings`
Minimum: privacy/data explanation, camera permission state, storage controls, account/auth only when configured, public policy/support links only when valid, app version/build.
Current: GREEN/YELLOW; production URLs and final consent/auth entries remain external RED gates.

## QR scope
`scanner.qr` is N/A for candidate 0.2.0 because QR detection is not a shipping feature. Therefore no QR URL is auto-opened. If QR is introduced later, safe-preview/confirmation and malicious-URL handling become mandatory before activation.

## Input limits
- OCR: maximum 50 images per operation.
- OCR import: maximum 25 MB per selected source before normalization.
- OCR imports are normalized/downsampled to a scanner-friendly working size.
- PDF/editor paths downsample large bitmaps to avoid unbounded memory use.

The production candidate must still stress-test these limits on physical devices. UI must reject or downsample safely instead of exhausting memory.

Password-protected PDF input is N/A because Safir Scanner does not import PDF files. If PDF import is added, this item becomes applicable and must be designed/tested before release.

## Accessibility contract
- Camera capture and flash have explicit semantic labels/state.
- Page selectors and filters expose selected state, not only color.
- Compact Home/editor actions use practical minimum touch targets.
- Visible text accompanies detection, error and destructive-confirmation states.
- TalkBack/font-scaling/focus/contrast remain physical-device release gates.

## Required physical evidence
- clean install and first launch;
- camera permission allow/deny/don't-ask-again;
- Files import without Camera permission;
- 1, 10, 20 and 50 pages;
- skew, low light, white background, large images;
- background/resume and process kill/recovery;
- low storage and failed export;
- generated PDF reopened after relaunch;
- processed scan -> OCR -> edit -> share/export text/image;
- TalkBack/text scaling/accessibility pass.

The feature is not considered complete because a toast or loading state says success; the resulting artifact/output must be observed and usable.
