# Auth Provider Decision — Safir Scanner

Decision: use Firebase Authentication as the single identity layer if/when account features are enabled.

Why:
- official Android support for Google sign-in through Credential Manager;
- email authentication support including verification/reset flows;
- official Sign in with Apple support on Android through OAuth;
- one user identity can link multiple providers;
- avoids building password/token infrastructure ourselves.

Hard constraints:
- Firebase project must be dedicated to `com.safir.scan` or explicitly isolated for this app with verified app registration; never copy another Safir app's OAuth/Firebase identity by guesswork.
- `google-services.json`, client IDs, SHA fingerprints and provider configuration must be generated from the exact project/app/signing identity.
- Apple private key is a server/provider secret and must never be committed to Git or packaged in the APK/AAB.
- Core scanner remains available without account until a real account-backed feature is defined.
- No Google data scopes beyond sign-in identity unless a shipping feature explicitly needs them.

Production enablement order:
1. Lock final Android package/signing identity.
2. Create/verify exact Firebase Android app for `com.safir.scan`.
3. Configure Google provider + SHA fingerprints/client IDs.
4. Configure Email provider and recovery/verification behavior.
5. Configure Apple Developer Services ID/return URL/key and Firebase Apple provider.
6. Implement account UI and provider linking.
7. Implement logout, session recovery and deletion.
8. Publish external Delete Account URL.
9. Update Privacy Policy/Data safety/reviewer notes.
10. Run full real-device reviewer QA before exposing auth in production.