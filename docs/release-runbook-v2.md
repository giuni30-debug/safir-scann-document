# Safir Scanner — Release Runbook v2

Purpose: produce one identifiable candidate, validate it, and keep submission/release as explicit separate operations.

## 0. Freeze
1. Confirm repo, branch and applicationId `com.safir.scan`.
2. Freeze one candidate SHA and config version.
3. Record dependency/toolchain versions.
4. Confirm no unrelated Safir identity appears in build/auth/ads/signing configuration.

## 1. One Fix Batch
1. Complete audit matrix first.
2. Repair all confirmed defects in the active batch.
3. Do not run a store build after every file edit.
4. When the batch is complete, run focused regression, then the full CI gate once.
5. Any new defect found by validation opens the next batch; it is not hidden by repeated build-number churn.

## 2. CI gate
Required on the exact candidate:
- lint;
- unit tests;
- debug APK;
- release AAB validation;
- 16 KB native alignment verification;
- no implicit publication.

Record run ID, repo, branch, SHA, version/config, artifact reference and digest when available.

## 3. External identity gate
Before signing/upload:
- exact Google Play app is `com.safir.scan`;
- Play App Signing/upload-key relationship is known;
- Codemagic app/workflow is dedicated to this repo/package;
- Google/Firebase/Apple auth clients refer to the same app identity when auth is enabled;
- AdMob App ID/ad units refer to this app when ads are enabled.

A missing permission/resource is `BLOCKED_EXTERNAL`, not GREEN.

## 4. Signed candidate
Produce the signed AAB from the frozen candidate. Record:
- build/versionCode;
- signing/upload-key reference (not secret bytes);
- artifact digest;
- workflow/run ID.

No publish/submission is implicit.

## 5. Physical-device gate
Install the exact candidate and execute `docs/device-review-matrix-v2.md`:
- clean install/onboarding;
- camera allow/deny/don't-ask-again;
- file import;
- capture/crop/perspective/multi-page;
- OCR editable + text/image/PDF outputs;
- reopen PDF after relaunch;
- 1/10/20/50-page stress;
- process death/background-resume;
- low storage/corrupt input;
- TalkBack/font scaling;
- auth lifecycle if enabled;
- ads consent/no-fill/frequency if enabled.

A CI build is not `VERIFIED_DEVICE`.

## 6. Public/reviewer packet
Verify working HTTPS resources without authentication:
- developer/support site;
- Privacy Policy;
- Terms when shipped;
- Delete Account page when accounts ship;
- app-ads.txt when AdMob is active.

Freeze final reviewer notes, access instructions and store metadata against the same candidate.

## 7. Store declarations
For the final AAB, verify:
- App access;
- Data Safety;
- ads declaration;
- target audience/content rating;
- permissions;
- screenshots/descriptions;
- testing state;
- package/signing.

No form is copied from another app.

## 8. Submission / release
Submission, approval, release authorization and live verification are separate states. Perform them only after explicit authorization. Record provider response/status after each operation.

## 9. Post-release
Monitor crash/ANR, auth, email delivery, AdMob policy/readiness, support issues and any dependency/config change. A material provider/SDK/config change invalidates the dependent evidence and triggers re-audit of that area.
