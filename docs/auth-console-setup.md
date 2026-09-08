# Safir Scanner — Authentication Console Setup

This is the controlled external configuration order for the optional Google / Email / Apple account flow. Do not reuse Firebase, OAuth or Apple provider identities from another Safir application.

## Fixed identity
- Android package: `com.safir.scan`
- Repository: `giuni30-debug/safir-scann-document`
- Core scanner remains available without an account.
- Authentication stays disabled until every enabled provider passes end-to-end QA.

## 1. Release signing first
Before final Google provider configuration, record the SHA-1 and SHA-256 fingerprints of the exact Android upload/release signing identity used for the Play build. Do not use debug SHA fingerprints as production evidence.

## 2. Dedicated Firebase configuration
Create/select a Firebase project intentionally assigned to Safir Scanner and add Android app `com.safir.scan`.

Enable only providers that will ship:
- Email/Password
- Google
- Apple

Record public app values into the controlled build environment, never hard-code another app's values:
- `SAFIR_FIREBASE_API_KEY`
- `SAFIR_FIREBASE_APP_ID`
- `SAFIR_FIREBASE_PROJECT_ID`
- `SAFIR_FIREBASE_SENDER_ID` when applicable
- `SAFIR_GOOGLE_WEB_CLIENT_ID`

Set `SAFIR_AUTH_ENABLED=true` only after those public values are complete. The Gradle build intentionally fails if auth is enabled while required public values are missing.

## 3. Google
- Add the exact release SHA-1/SHA-256 fingerprints to the Safir Scanner Firebase/Google project.
- Enable Google sign-in in Firebase Authentication.
- Use the web/server client ID belonging to that exact project for `SAFIR_GOOGLE_WEB_CLIENT_ID`.
- Keep consent branding, privacy/support domains and application identity consistent.
- Identity-only sign-in must not request Gmail, Drive, Calendar, Contacts or other unrelated Google scopes.

Required QA: first-time account chooser, returning account, cancellation, no credential, offline/network error, session restore, logout, reauthentication, account deletion, and provider linking.

## 4. Email
Enable Email/Password only when all of these work:
- account creation;
- verification email;
- verified login;
- invalid credential behavior;
- password recovery;
- logout/session restore;
- reauthentication for sensitive actions;
- account deletion.

Use provider-side abuse/rate protections and email-enumeration protections where available. Reviewer access must never depend on an uncontrolled personal inbox if authentication is required for review.

## 5. Sign in with Apple on Android
Configure in the Apple Developer account and Firebase Apple provider:
- Services ID dedicated/approved for this authentication flow;
- Firebase-authorized HTTPS return URL;
- Apple Team ID;
- Apple Key ID;
- Apple private key stored only in the provider/secret manager, never GitHub/APK/BuildConfig.

Firebase's Apple provider handles the OAuth browser/provider exchange. The Android app asks only for `name` and `email` through the provider flow.

Required QA: first sign-in, Hide My Email/private relay account, cancel, repeat sign-in, reauthentication, explicit provider-link consent, account deletion/revocation behavior.

## 6. Account deletion and public policy
Before account creation is visible in production:
- in-app Delete account must work;
- public HTTPS account-deletion URL must exist;
- Privacy Policy must include authentication identifiers/providers, purposes, retention and deletion;
- Play Data safety must be rebuilt from the exact shipping SDK/runtime behavior;
- review notes must state that scanning works without login and explain the optional account path.

## 7. Activation gate
Do not activate auth in the production build until all of these are true:
1. exact `com.safir.scan` Firebase app verified;
2. exact release signing fingerprints verified;
3. Google tested on real device;
4. Email tested end to end;
5. Apple tested on Android end to end;
6. linking/duplicate-identity behavior tested;
7. logout/reauth/delete tested;
8. privacy/delete/support URLs live;
9. final Data safety/reviewer notes match the binary;
10. CI and real-device GREEN GATE both pass.
