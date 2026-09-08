# Privacy Policy Content Specification

This is not the public Privacy Policy. It is the checklist the final public policy must satisfy. The public text must be generated only after the production SDK/data inventory is locked.

## Product identity
Identify Safir Scanner and the responsible developer/contact. State the Android app's purpose: document scanning, image editing and local PDF creation.

## Document content
Explain what happens to camera captures, imported images, temporary draft files and saved PDFs. State where they are processed/stored, and explain that sharing to another app happens only when the user invokes Android Share.

Do not say "we collect no data" after AdMob/UMP is integrated unless the exact final SDK behavior and Google guidance justify that statement.

## Permissions
Explain Camera access and why it is requested. State that file import provides an alternative when Camera is not granted. List any additional production permission only if actually present in the final merged manifest.

## Advertising / third parties
After AdMob is integrated, disclose Google Mobile Ads/UMP and the categories of app/device/advertising data that the final SDK configuration may access, collect, process or share. Describe purposes such as ad delivery, measurement/fraud prevention only to the extent supported by the final configuration/provider documentation.

## Consent / regional choices
Explain applicable consent/privacy choices and how a user can revisit privacy options when the production configuration requires it.

## Retention / deletion
Explain retention for locally stored documents and temporary files, and how the user can delete saved PDFs / clear temporary data. If third-party advertising data has different retention/control mechanisms, describe or link to the provider's applicable controls accurately.

## Accounts
Current app has no account creation. Do not invent account-deletion language that suggests a non-existent account system. If accounts are later added, the policy and delete-account implementation must be reopened before release.

## Security / transfers
Describe security/transport claims only if they are true for the final build. Do not promise encryption, anonymization, locations, countries of processing or retention periods without evidence.

## Contact / changes
Provide a real support/privacy contact and an effective date. Explain how policy changes are communicated as appropriate.

## Cross-check before publish
The final public Privacy Policy must match:
- exact production dependency graph;
- exact merged manifest permissions;
- Play Data safety answers;
- AdMob/UMP configuration;
- in-app privacy wording;
- store listing/app content declarations.

Public URL and in-app destination must both work before store review.
