# Account Data Contract

This contract applies only when account creation is enabled.

## Minimum identity data
- internal auth user ID;
- provider identifier(s): Google, Email, Apple;
- email only when the provider supplies it and the feature needs it;
- display name only when supplied/needed.

Do not collect Gmail contents, Drive files, Calendar data, Contacts or additional Google profile scopes for sign-in alone.

## Local scanner content
Account identity must not silently upload existing local scans. Any future cloud backup/sync feature must be a separate, explicit product feature with its own consent, storage, retention, deletion and privacy documentation.

## Retention/deletion
- auth/account data deleted when account deletion completes, except documented legal retention if any;
- local PDFs remain controlled by the user/device unless the deletion UX explicitly offers local deletion too;
- provider unlinking must not accidentally orphan an account with no usable sign-in method without confirmation/recovery.

## Privacy mapping
Final Privacy Policy and Play Data safety must reflect the exact Firebase/Auth SDK behavior, identifiers, provider data, logs/diagnostics and any backend account profile fields actually used in the production build.