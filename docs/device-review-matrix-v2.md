# Physical Reviewer Matrix v2 — Safir Scanner

`VERIFIED_DEVICE` requires the exact candidate build on physical hardware. Emulator/mock/CI evidence is useful but insufficient for this gate.

## Core matrix

| ID | Scenario | Required observable result |
|---|---|---|
| D01 | Clean install | No crash; onboarding clear; guest scan available. |
| D02 | Camera allow | Camera opens; capture succeeds; page remains usable. |
| D03 | Camera deny | Files import remains usable; no permission loop or dead end. |
| D04 | Don't ask again | Open Android Settings path works and return state is usable. |
| D05 | Camera unavailable/busy | Truthful error; Files fallback works; no crash. |
| D06 | 1 page | Edit → PDF → reopen after relaunch succeeds. |
| D07 | 10 pages | No freeze/ANR/OOM; order and edits persist into PDF. |
| D08 | 20 pages | Same as D07 with acceptable responsiveness. |
| D09 | 50 pages | Stress boundary: no silent page loss; failure is explicit if device limit is reached. |
| D10 | Large image | Safe downsample/normalization; no OOM. |
| D11 | Corrupt input | Import/OCR rejects safely with useful message. |
| D12 | Low storage | Export fails truthfully; draft remains recoverable; no empty success artifact. |
| D13 | Background/resume | Camera/editor state remains coherent or recovers safely. |
| D14 | Process kill during draft | Completed pages recover; temporary helpers do not appear as pages. |
| D15 | Process kill during PDF save | No partial PDF presented as successful; draft remains recoverable. |
| D16 | Delete PDF | Confirmation required; correct file removed only after confirm. |
| D17 | Share PDF | Receiver can open the shared artifact with scoped permission. |
| D18 | OCR local | Imported image → recognized text → editable field. Poor/no text gives truthful state. |
| D19 | TXT export | Edited OCR text exports to a user-selected destination and can be opened. |
| D20 | Image share | Selected OCR image can be opened by receiver; only scoped cache URI is granted. |
| D21 | TalkBack | Critical actions are announced clearly and in logical focus order. |
| D22 | Large font/display | Critical controls remain reachable; no clipped irreversible action. |
| D23 | Repeated cycles | Multiple scan/edit/save/delete cycles do not accumulate broken state or crash. |

## Auth matrix when enabled
Google/Email/Apple scenarios from `docs/auth-review-tests.md` become part of the physical matrix, including cancel, timeout, expired session, logout/login, provider linking, reauth and deletion.

## Advertising matrix when enabled
Consent accept/refuse, privacy re-entry, no-fill/offline, interstitial frequency cap and background/resume become part of the same exact-candidate device evidence.

## Evidence record
For every executed case store: test ID, PASS/FAIL/BLOCKED, candidate SHA/version, device model/OS, timestamp, observed before/after state, artifact or screenshot reference, and defect reference if failed.