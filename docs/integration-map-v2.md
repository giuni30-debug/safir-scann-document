# Safir Scanner — Integration Map v2

This file records identity links and their current evidence state. A connection being visible in an account is not proof that the operation needed by this app is authorized.

| Area | Canonical identity for this app | Current state | Evidence required before GREEN |
|---|---|---|---|
| GitHub | `giuni30-debug/safir-scann-document` | VERIFIED_REPO | branch/SHA/config + CI artifact provenance |
| Android package | `com.safir.scan` | VERIFIED_CODE | exact Play record must match |
| Google Play | `com.safir.scan` only | BLOCKED_EXTERNAL | exact app record, app signing, track/access, countries, declarations |
| Codemagic | app/workflow dedicated to this repo/package | BLOCKED_EXTERNAL | exact app ID/workflow, repo access, signing refs, signed AAB |
| Firebase Auth | dedicated Firebase app for `com.safir.scan` | BLOCKED_EXTERNAL | project/app IDs, enabled providers, release fingerprints, end-to-end tests |
| Google OAuth | Android client for `com.safir.scan` + signing cert; web/server client when backend audience requires | BLOCKED_EXTERNAL | client IDs, origins/redirects where applicable, branding/domain, minimal scopes |
| Email | real support inbox + transactional sender domain | BLOCKED_EXTERNAL | MX/SPF/DKIM/DMARC, delivered/bounced evidence, reset/signup tests |
| Apple login on Android | Services ID linked to correct Apple configuration | BLOCKED_EXTERNAL | Services ID, Team ID, Key ID/private key server-side, HTTPS return URL, relay tests |
| AdMob | Android app for `com.safir.scan` | BLOCKED_EXTERNAL | AdMob App ID, ad units, UMP, test ads, app-ads.txt, readiness/policy center |
| RevenueCat | none for current Free + Ads model | N/A | becomes applicable only if paid digital access is introduced |
| App Store Connect | no iOS build in this repository | N/A_FOR_CURRENT_REPO | separate iOS product/build would open Apple store gates |

## Anti-mixup rules

- Never reuse another Safir app's package, OAuth client, Firebase app, AdMob App ID/ad unit, signing key, Codemagic app or store record because its display name looks similar.
- Android `namespace` is not accepted as proof of store identity; the release `applicationId` and signed artifact are checked.
- Every external write must be followed by an authoritative read when the provider/API supports it.
- Secrets are represented by references only. No private key, service-account JSON, SMTP/API secret or signing material is committed here.

## Auth release rule

Google/Email/Apple controls may be visible only when the exact production configuration is complete and the full login -> protected session -> logout/recovery/delete lifecycle has evidence. Core scanning stays available as guest.

## Ads release rule

`store_ready` and `ads_monetization_ready` are separate. If AdMob has no fill or is not yet ready, the scanner must still perform its primary function and may not simulate a live ad.
