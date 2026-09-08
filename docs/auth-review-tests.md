# Auth Reviewer Test Matrix

Run only on a build where auth is actually enabled.

## Guest
- Clean install -> scanner usable without account.
- Decline sign-in -> no blocked core scan flow.

## Google
- New Google user sign-in.
- Returning user sign-in.
- User cancels chooser.
- No network.
- Revoked/expired credential.
- Logout and sign-in again.
- Verify same user does not get duplicate identity when linked to Email/Apple.

## Email
- Create account.
- Email verification.
- Wrong password/invalid credentials.
- Password reset/recovery.
- Offline/timeout.
- Logout and session restore.

## Apple on Android
- New Apple sign-in.
- Returning Apple sign-in.
- Cancel flow.
- Private Relay email case.
- Revoked authorization.
- Logout/sign-in again.
- Link/unlink behavior where supported.

## Delete account
- Initiate from Settings > Account.
- Reauthenticate if required.
- Account becomes unusable after deletion.
- Associated account data is removed according to policy.
- External delete-account web route works without requiring installation.

## Reviewer access
- If any reviewed feature requires login, provide a deterministic review account/path that does not depend on reviewer-owned inbox, uncontrolled MFA or private hardware.
- Record exact provider, credentials/instructions, backend status and deletion path in review notes.

Any failure above is RED.