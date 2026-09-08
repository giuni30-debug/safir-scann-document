# Reviewer Reject Matrix — Safir Scanner

Purpose: think like the reviewer before every upload. A row is green only when the production artifact and supporting declarations have evidence.

| Reviewer risk | What would look wrong | Required control | Current evidence / gate |
|---|---|---|---|
| Broken functionality | crash, force close, freeze, blank/unresponsive screen | lint/tests/build plus real-device scenario testing; heavy processing off UI thread | CI + core hardening; real-device matrix still required |
| Limited/unclear utility | app appears like a shell/template or cannot complete a real task | full capture/import → edit → PDF → open/share/delete flow | implemented |
| Misleading metadata | listing/screenshots promise absent features | store copy and screenshots generated only after final binary is locked | submission gate |
| Camera permission misuse | permission appears before context, denial blocks app, or request has no feature need | request in capture context; explain use; Files import fallback; Android Settings path | implemented |
| Excess permissions | location/contacts/mic/SMS/etc. without core need | manifest allow-list; release manifest audit | current manifest should contain Camera only |
| Privacy inconsistency | app says local/no collection while integrated SDKs collect/transfer data not disclosed | exact SDK/data inventory mapped to Privacy Policy and Data safety | core inventory exists; must re-audit after AdMob |
| Missing privacy policy | Play Console link absent, dead, inaccessible, or not available in app | public HTTPS privacy policy + in-app working destination/text | BLOCKER before submission |
| Missing support | reviewer/user has no working support destination | public Support URL/contact that actually works | BLOCKER before submission |
| Account review blocked | reviewer cannot sign in or account deletion missing | if accounts exist: demo access + deletion. If none: declare no login required | N/A now — no accounts |
| Hidden backend dependency | app core fails because server/demo account is offline | no backend dependency for document processing, or keep required backend online | no document backend in current design |
| Ad policy violation | unexpected interstitial before intended action, ads over controls, accidental taps, hard-to-dismiss ads | natural-break placements; UMP; correct ad IDs; test ads during development | BLOCKER until AdMob implementation is reviewed |
| Ads declaration mismatch | app serves ads but Play says no ads | Play App content: Contains ads = Yes for production ad build | submission gate |
| Data safety mismatch | declarations ignore AdMob/SDK data | review exact shipping SDKs and fill form from data inventory | submission gate |
| Target API rejection | target below current Play requirement | target/compile API 36 | implemented |
| Native compatibility | native OpenCV libraries incompatible with required page sizes | automated native alignment/16 KB check + release-device test | CI gate added |
| Wrong package identity | AAB/package differs from Play app record/AdMob app | treat `com.safir.scan` as permanent identity; cross-check before every platform write | identity gate |
| Wrong artifact/signing | debug/unsigned/wrong key artifact uploaded | production signed AAB; Play App Signing/upload key verified | BLOCKER before production release |
| Version conflict | versionCode reused or wrong build submitted | monotonically increase versionCode; tag exact release commit | release gate |
| Placeholder/dead URLs | buttons or legal links open nothing | no placeholder links in production; HTTP/HTTPS checks before submit | legal URLs not yet wired, so submission blocked |
| Destructive UX | accidental PDF/page deletion | confirmation for PDF delete; guarded editor delete | implemented/QA required |
| File corruption/silent loss | PDF saves with missing page, scan disappears after failure | atomic PDF transaction; draft retention on failure; recovery | implemented/QA required |
| Image incompatibility | imported photo rotated incorrectly or huge image causes memory failure | EXIF normalization/downsampling and stress testing | implemented/QA required |
| Content rating missing | app has no valid Play content rating | complete questionnaire accurately; keep rating consistent with ads/target audience | submission gate |
| Target audience mismatch | child targeting/ads declarations conflict with actual listing/ad SDK setup | choose intended audience before release; configure ads and listing consistently | product decision required before AdMob production |
| Reviewer cannot understand flow | unusual behavior is not explained | concise reviewer notes with exact test path and no-login statement | `reviewer-notes.md` |

## Hard rule
Do not solve a reviewer concern with wording if the binary still behaves incorrectly. Fix the product first; then make metadata and declarations describe the fixed product exactly.
