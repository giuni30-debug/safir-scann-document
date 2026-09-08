# Email Delivery Gate v2 — Safir Scanner

Applies only if Email authentication/account creation ships. Login code alone is not enough.

## Ownership and contacts
- Owner-controlled domain with recoverable admin accounts and MFA.
- Monitored support inbox and privacy contact.
- Do not present an alias as a support mailbox unless replies are actually received and handled.

## DNS / sender authentication
- MX configured for reception where required.
- Sending domain verified with the transactional provider.
- SPF configured for the real sender path.
- DKIM enabled with the provider's actual keys.
- DMARC configured and aligned; tighten policy only after observing real delivery.
- Never copy DNS values from another Safir app/project.

## Transactional auth mail
- Signup verification and password reset use production templates with Safir Scanner branding and correct language.
- Links are HTTPS/deep links authorized for the exact auth project.
- SMTP/API credentials stay server-side; none in APK/repo/logs.
- Retry/rate-limit/bounce/complaint handling is monitored.
- Marketing opt-in is separate from transactional authentication mail.

## Reviewer tests
1. New email account → message received → verification link consumed → login works.
2. Repeated signup does not leak whether an account exists and does not create duplicates.
3. Expired/reused verification link shows a truthful failure and safe resend path.
4. Password reset token expires and is single-use.
5. Reauthentication path works before sensitive deletion when required.
6. Delivery states distinguish accepted/sent/delivered/bounced; UI never claims delivery from an API acceptance alone.
7. Test at least one Gmail inbox and, when Sign in with Apple relay is used, an Apple private-relay address.
8. Logs contain no full tokens, passwords or unnecessary personal data.

## GREEN evidence
Domain/DNS evidence, provider configuration, test message IDs, received-mail proof, callback/verification result, recovery test and reviewer notes for the exact production auth configuration.