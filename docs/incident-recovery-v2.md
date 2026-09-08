# Safir Scanner — Incident & Recovery Runbook v2

## Incident record
Record: incident ID, app version/build/SHA, device/OS, time, affected route, expected vs observed result, redacted logs/evidence, reproducibility, severity, owner and recovery state. Never attach document pages, OCR text, tokens, passwords or private auth payloads to public evidence.

## Core scanner failures

### Camera unavailable / permission denied
- Keep import path usable.
- Do not loop permission requests after permanent denial; route to Android Settings.
- Record camera/bind error without user document content.

### Capture/import processing failure
- Preserve the original/draft when possible.
- Mark the operation failed; never show success because a callback fired.
- Allow retry or another input.

### OpenCV/crop/filter failure
- Keep the last valid page.
- Do not replace a valid source with a zero-byte/temp file.
- Reopen the editor in a recoverable state.

### PDF export failure
- Do not silently skip unreadable pages.
- Remove incomplete temp output.
- Keep draft pages available.
- On retry, create one new intended artifact; do not duplicate because an earlier response was uncertain.

### OCR failure
- Keep source pages.
- Keep user-edited text if already present.
- State no-text/unsupported/recognizer failure honestly.
- Never upload OCR content as crash evidence.

### Low memory / large input
- Downsample to documented working limits.
- Reject unsupported size/count with a clear error instead of continuing toward OOM/ANR.

## Account/auth incidents (only when enabled)
- A provider callback is not success until the correct authenticated session exists.
- On expired/revoked credentials, reauthenticate; do not silently bind a different provider identity by matching email text.
- If deletion fails after partial processor cleanup, keep a durable request state and report the remaining blocker; do not claim deletion complete.
- Private Apple/Firebase/email keys stay server-side and are rotated after exposure.

## Email incidents
Track accepted, delivered, bounced and complaint states separately. A provider accepting a send request is not proof of inbox delivery. Keep resend rate limits and avoid leaking whether an account exists where the auth design intentionally masks it.

## Ad incidents (only when enabled)
- No fill / consent refusal must not block scanning.
- Disable a problematic placement through controlled configuration only when the approved product behavior remains truthful.
- Never auto-click/live-test ads.
- Policy-center problems remain a separate monetization blocker; they do not become store approval state.

## Recovery verification
Recovery is complete only when the failed scenario is rerun on the affected candidate/config, the postcondition is observed, and dependent evidence is refreshed. A code commit alone is `FIXED_CODE`, not `VERIFIED_DEVICE` or `RELEASE_READY`.
