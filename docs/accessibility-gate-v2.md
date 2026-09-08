# Accessibility Gate v2 — Safir Scanner

Release blocker until tested on the exact candidate.

## Required checks
- TalkBack announces every actionable control with a meaningful name and state.
- Camera capture control is announced as an action, not only as a visual dot/icon.
- Page selectors announce page number and selected state.
- Crop handles/actions, filters, delete confirmation, Save PDF, Settings, Account and policy links have accessible labels.
- Important state is not conveyed by color alone; text/state labels accompany detection, errors and selection.
- Touch targets are at least practical Android accessibility size; small chips must remain tappable without accidental neighboring activation.
- Font scaling is tested at large accessibility sizes without clipping critical controls or hiding actions.
- Focus order is logical from top to bottom and does not jump behind overlays/dialog-like states.
- Error/loading text is exposed to accessibility services and remains understandable without viewing the screen.
- Contrast is checked for normal text, disabled controls and status text against the gradient/glass backgrounds.
- System back/navigation and external Android Settings return to a usable state.

## Device matrix
Test at minimum one smaller phone and one larger phone with TalkBack on/off, display/font size increased, portrait flow, background/resume and permission denial.

## Evidence
Record build SHA/version, device/OS, scenario, observed TalkBack output, screenshots where useful, and PASS/FAIL/BLOCKED. A code review alone cannot mark this GREEN.