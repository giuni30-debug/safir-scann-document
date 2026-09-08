# Authentication Gate — Google, Email, Apple

Status: REQUIRED RELEASE GATE. Do not expose a provider button until that provider is fully configured and tested end-to-end.

## Product rule
Safir Scanner's core scanning flow does not require an account, so guest/local use must remain available. Authentication is optional and may only be enabled when it provides a real account-backed function. Never block scanning behind login just to collect identity.

If authentication is enabled in a production release, all controls below become mandatory before store submission.

## Chosen auth architecture
Use one Firebase Authentication project dedicated to `com.safir.scan`, not another Safir app. One Firebase user identity may link multiple providers so Google, Email and Apple do not create accidental duplicate accounts.

### Google
- Android sign-in uses Credential Manager + Sign in with Google.
- Configure the exact Google/Firebase project for `com.safir.scan`.
- Register the exact release/upload SHA certificate fingerprints required by the final signing configuration.
- Use the server/web client ID where required by Credential Manager/Firebase, never a client ID from another Safir app.
- Request only identity scopes needed for sign-in. Do not request Gmail, Drive, Calendar or other Google-data scopes unless a real app feature needs them.
- Handle cancellation, no credential, offline/network failure, expired/revoked credential and sign-out.

### Email
Production path in the current implementation is verified email/password.

Mandatory behavior:
- email verification before normal email/password sign-in completes;
- password reset;
- rate-limit/abuse handling through the auth provider;
- generic invalid-credential/recovery wording that does not deliberately expose account existence;
- secure session handling;
- logout;
- deletion.

For review, do not force the reviewer to depend on an uncontrolled inbox or MFA. Provide a stable review path/demo account if a release later requires authenticated access.

### Sign in with Apple on Android
- Configure Sign in with Apple in the Apple Developer account.
- Use a Services ID and registered HTTPS return URL for the Firebase/Apple OAuth flow.
- Configure Apple Team ID, Key ID and private key only in provider/server secrets; never commit the private key.
- Use the supported Firebase OAuth provider flow for state/token handling.
- Support Apple private relay addresses correctly if the app sends email to those users.
- Handle reauthentication, revocation/deletion and provider linking safely.

## Account lifecycle — mandatory if account creation is exposed
- Continue without account / guest mode remains available for core scanning.
- Sign in / create account.
- Provider linking for the same user where safe and explicit.
- Session restore after app restart.
- Logout and Credential Manager state clearing.
- Reauthentication for sensitive account actions when required.
- Delete account inside Settings > Account.
- Delete associated account data, not just disable/hide the account.
- Public HTTPS account-deletion page outside the app for Google Play.
- Explain any legally retained data and retention period in Privacy Policy.

Local scanner PDFs are separate device-controlled files and are not silently uploaded or silently deleted when a remote identity changes. Any future cloud/sync behavior reopens the privacy/data gate.

## Settings contract when auth is enabled
Settings > Account must show:
- signed-in identity/provider(s);
- link provider where supported and safe;
- sign out;
- delete account;
- privacy/support access in the final production settings surface;
- app version/build.

If signed out, Settings must not show dead provider controls. If a provider is disabled or misconfigured, do not ship its button.

## Fail-closed configuration rule
The code contains an authentication foundation, but `SAFIR_AUTH_ENABLED` defaults to `false`. Provider UI is exposed only when the required public Firebase/Google configuration passes `AuthPublicConfig.isReady`. Provider secrets are never BuildConfig fields and must remain in the provider console/secret manager.

Required public build inputs before the account entry can appear:
- `SAFIR_FIREBASE_API_KEY`
- `SAFIR_FIREBASE_APP_ID`
- `SAFIR_FIREBASE_PROJECT_ID`
- `SAFIR_GOOGLE_WEB_CLIENT_ID`
- optional Firebase sender ID when needed

This config gate is only a first safety check. It does not turn a provider GREEN by itself; real provider-console setup and real-device end-to-end tests are still required.

## Reviewer reject traps
Block release for any of these:
- login is mandatory although scanning works without it;
- provider button is visible but sign-in cannot finish;
- Apple button is present without valid Services ID/return URL/provider config;
- email account cannot verify/reset password;
- no logout;
- no in-app delete account after account creation is enabled;
- no external delete-account URL for Play;
- duplicate accounts are created silently when the same user changes provider;
- OAuth scopes request Gmail/Drive/Calendar without a feature that needs them;
- wrong SHA/client ID/package/Firebase project;
- secrets or Apple private key in repo/APK/logs;
- reviewer needs private inbox, uncontrolled MFA or unavailable backend;
- Privacy Policy/Data safety omit auth identifiers/provider data.

## Evidence required before GREEN
1. Exact Firebase project/app record mapped to `com.safir.scan`.
2. Release signing SHA-1/SHA-256 fingerprints recorded in the correct project.
3. Google sign-in success/cancel/failure/logout tests.
4. Email create/verify/login/reset/logout tests.
5. Apple sign-in success/cancel/reauth/revocation tests on Android.
6. Same-user provider-linking tests, including explicit Apple linking consent.
7. Delete account test from app plus public deletion URL test.
8. Offline/token-expiry/session-restore tests.
9. Privacy/Data safety updated from the final auth SDK behavior.
10. Reviewer notes updated with exact access path.
11. CI lint/unit tests/APK/AAB/16 KB gates green on the exact auth candidate.

## Current implementation status
Implemented in the working branch, but not yet production-enabled:
- Firebase Authentication SDK foundation;
- Credential Manager / Google ID credential integration;
- verified email/password create/sign-in/reset flow;
- Apple OAuth sign-in and reauthentication flow;
- Google/Apple provider-link methods;
- logout and Credential Manager state clearing;
- in-app Firebase account deletion with recent-login handling;
- account screen hosted by a non-exported Activity;
- Settings account entry hidden unless public auth configuration is complete;
- unit gate preventing incomplete provider configuration from being treated as ready.

Still RED until external configuration and tests exist:
- dedicated Firebase app/project for `com.safir.scan`;
- exact release SHA fingerprints;
- Google web client ID from that exact project;
- Apple Services ID/Team ID/Key/return URL provider configuration;
- public delete-account/privacy/support pages;
- real-device end-to-end provider tests;
- final Play Data safety/privacy/reviewer notes cross-check.
