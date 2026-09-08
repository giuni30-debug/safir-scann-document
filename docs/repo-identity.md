# Repository / Platform Identity Gate

Every platform write must be checked against this file first.

## Source identity
- GitHub owner: `giuni30-debug`
- Repository: `safir-scann-document`
- Release-hardening branch: `fix/safir-scanner-a-z-release-20260908`
- Production branch target: `main` only after release gate and PR checks pass

## App identity
- Name: Safir Scanner
- Android applicationId/package: `com.safir.scan`
- Current versionName: `0.2.0`
- Current versionCode: `2`
- Android target: API 36

## Business identity
- Distribution: Google Play / Android
- Price: free
- Monetization: AdMob advertising
- IAP/subscriptions: none
- RevenueCat: N/A
- Account/login: none

## Cross-platform guard
Do not use credentials, Play package IDs, AdMob app IDs, RevenueCat projects, Codemagic apps or signing identities from another Safir application just because the names are similar.

Before configuring Google Play, AdMob or Codemagic, verify that the platform record explicitly belongs to `com.safir.scan` / Safir Scanner. If the connected provider defaults to another package, stop the write.

## Final identity evidence
Before production upload record:
- Play Console app record/package;
- Play App Signing status/upload certificate fingerprints;
- Codemagic application/workflow ID;
- AdMob Android app ID;
- production ad unit IDs;
- developer website/domain;
- Privacy Policy URL;
- Support URL;
- final Git commit/tag;
- signed AAB digest.

No field above should be copied from a different Safir project.
