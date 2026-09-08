# Authentication Gate — Google, Email, Apple

Status: REQUIRED DESIGN GATE. Do not expose a provider button until that provider is fully configured and tested end-to-end.

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
Preferred production path: email link or verified email/password, depending on final UX.

Mandatory behavior if email/password is used:
- email verification;
- password reset;
- rate-limit/abuse handling through the auth provider;
- clear invalid-credential errors without revealing sensitive account existence details;
- secure session handling;
- logout;
- deletion.

For review, do not force the reviewer to depend on an uncontrolled inbox or MFA. Provide a stable review path/demo account when the release actually requires authenticated access.

### Sign in with Apple on Android
- Configure Sign in with Apple in the Apple Developer account.
- Use a Services ID and registered HTTPS return URL for the Firebase/Apple OAuth flow.
- Configure Apple Team ID, Key ID and private key only in provider/server secrets; never commit the private key.
- Handle nonce/state and token validation through the supported provider flow.
- Support Apple private relay addresses correctly if the app sends email to those users.
- Handle revocation/deletion and provider unlinking safely.

## Account lifecycle — mandatory if account creation is exposed
- Continue without account / guest mode remains available for core scanning.
- Sign in / create account.
- Provider linking for the same user where safe and explicit.
- Session restore after app restart.
- Logout.
- Reauthentication for sensitive account actions when required.
- Delete account inside Settings > Account.
- Delete associated account data, not just disable/hide the account.
- Public HTTPS account-deletion page outside the app for Google Play.
- Explain any legally retained data and retention period in Privacy Policy.

## Settings contract when auth is enabled
Settings > Account must show:
- signed-in identity/provider(s);
- link/unlink provider where supported and safe;
- sign out;
- delete account;
- privacy policy;
- support/contact;
- app version/build.

If signed out, Settings must not show dead provider controls. If a provider is disabled or misconfigured, do not ship its button.

## Reviewer reject traps
Block release for any of these:
- login is mandatory although scanning works without it;
- Google button opens but cannot finish sign-in;
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
2. Release signing SHA fingerprints recorded.
3. Google sign-in success/failure/logout tests.
4. Email create/verify/login/reset/logout tests, if email is enabled.
5. Apple sign-in success/cancel/revocation tests on Android, if Apple is enabled.
6. Same-user provider-linking test.
7. Delete account test from app plus public deletion URL test.
8. Offline/token-expiry tests.
9. Privacy/Data safety updated from the final auth SDK behavior.
10. Reviewer notes updated with exact access path.

## Current release status
Current core build intentionally has no auth SDK or account creation and correctly states that no account is required. This document makes Google/Email/Apple a controlled future/final-release gate; they must not be exposed piecemeal.