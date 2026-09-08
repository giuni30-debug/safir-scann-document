# Safir Scanner — Plan A-Z v2.0 refresh

Source of truth for release-readiness work: **CONTROL SAFIR WORLD / EDIȚIA 2.0 — Plan A-Z pentru aplicații mobile pregătite de review, revizia 08.09.2026**.

This refresh does not certify implementation. Every status still requires repository, build, platform, backend and/or physical-device evidence.

## Rules adopted immediately

1. Keep separate verdicts for Play/App Review, OAuth, AdMob, CI/signing, public docs and account lifecycle. `Connected` is not proof that the required operation is allowed.
2. Work in coordinated Fix Batches: audit → confirmed gaps → one reviewable batch → focused regression → full tests → production candidate. Do not create store builds for every small defect.
3. A scanner release contract is now: capture/import → crop/perspective → multi-page → editable OCR → export PDF/image/text → share/delete. Local processing claims must remain true for document content.
4. The common product gate now explicitly includes loading/error/offline/retry states, refused permissions, unavailable providers, expired sessions, accessibility, small/large devices, background/resume and low-storage behavior.
5. If account creation is enabled, Google/Email/Apple are not decorative buttons: full session, linking, logout, reauth, deletion, reviewer access and provider-specific configuration are required.
6. Email auth additionally requires an operational domain/inbox/delivery contract: monitored support/privacy contacts, verified sending domain, SPF/DKIM/DMARC, recovery templates and delivery/bounce handling.
7. Public Privacy/Support/Delete Account resources are release artifacts. Placeholder or dead URLs block release.
8. Reviewer notes must be reproducible without live coordination and must describe the exact candidate build, permissions, data, auth and monetization behavior.
9. Statuses are evidence-backed. A code fix is not `VERIFIED_DEVICE`; a green CI job is not store approval.
10. Submission, release, financial changes and destructive account/data operations remain explicit gates; readiness work never performs them implicitly.

## Impact on current Safir Scanner

### Already aligned or substantially implemented
- Native document capture/import, perspective correction, multi-page editing and local PDF generation.
- Camera permission is contextual and Files import remains available when permission is refused.
- Draft recovery, background PDF work, atomic PDF write path and page-processing coordination exist.
- Google/Email/Apple auth foundation is feature-gated; incomplete public config cannot be enabled silently.
- Public-link configuration is feature-gated and requires HTTPS before it can be exposed.
- CI checks lint, unit tests, debug APK, release AAB validation and 16 KB native compatibility.

### Newly explicit RED/YELLOW items from v2.0
- **RED:** editable OCR in the scanner workflow.
- **RED:** export/share of recognized text and image output as first-class scanner outcomes.
- **RED:** production accessibility pass with TalkBack, text scaling, focus and touch-target evidence.
- **RED:** physical-device evidence for clean install, 1/10/20/50 pages, low storage, process death, background/resume and repeated cycles.
- **RED if Email auth ships:** monitored support/privacy mailboxes plus SPF/DKIM/DMARC and real delivery/recovery tests.
- **RED if auth ships:** exact Firebase/Google/Apple console resources, signing fingerprints, reviewer-safe access and public deletion page.
- **RED:** public Privacy/Support/developer website and final Play Data safety mapping for the exact shipping SDK graph.
- **RED:** AdMob/UMP/app-ads.txt/Contains ads and safe placement evidence before monetized production release.

## Target API note
The v2.0 PDF explicitly records its Target API source as unverified in that document. Do not turn that PDF statement into a PASS by itself. The Android target requirement must be refreshed again from the current official Google source and the exact Play account before the final candidate.

## Release rule
`RELEASE_READY` for Safir Scanner is allowed only when all applicable RED items are evidence-backed GREEN, YELLOW items have final evidence, and the exact signed candidate has passed the reviewer path on real hardware. It is still distinct from `STORE_APPROVED` and `LIVE_VERIFIED`.
