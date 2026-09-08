# Safir Scanner — Public Pages Content Pack

Status: DRAFT CONTENT. This file is not a substitute for live HTTPS pages. Release stays blocked until the final pages are published, reachable without login and cross-checked against the exact production AAB.

## 1. Privacy Policy — required content

Safir Scanner is a document-scanning app for Android. The core scanner captures or imports document images, processes them on the device and saves generated PDFs in app-private local storage. Documents are not uploaded merely because the app is installed or because the user signs in.

The final public Privacy Policy must accurately describe every data path present in the shipping build, including:
- camera-captured and user-selected document images;
- temporary scan files and saved PDFs;
- explicit Android sharing initiated by the user;
- optional account identifiers if Google, Email or Apple authentication is enabled;
- Firebase Authentication behavior and provider data actually used;
- advertising/measurement data introduced by Google Mobile Ads and UMP when AdMob is enabled;
- diagnostics or analytics only if they are actually present in the final dependency graph;
- retention and deletion behavior;
- contact/support method;
- user privacy/ads controls and how to exercise them.

Never claim "no data collected" after auth or advertising SDKs are enabled unless the exact final SDK behavior and Play declarations support that statement.

## 2. Support page — required content

The public Support page must identify Safir Scanner and give a real contact path. It should cover at least:
- camera permission problems;
- importing images from Files;
- scanning/crop/filter/PDF export issues;
- opening or sharing PDFs;
- account sign-in/recovery/deletion if accounts ship;
- privacy/ads consent questions when AdMob ships;
- app version/build information users should include in a report.

The link must work without signing in and must not be a placeholder.

## 3. Terms / EULA — recommended content

If published, Terms should describe:
- the app as a document scanner and PDF utility;
- acceptable use;
- user responsibility for documents they scan/share;
- local storage limitations and backup responsibility;
- optional account behavior if accounts ship;
- advertising-funded business model;
- service availability and updates;
- support/contact and governing terms.

Do not describe subscriptions, premium purchases, cloud sync or other features that are not in the shipping product.

## 4. Delete Account page — mandatory if account creation ships

The page must explain:
- how to initiate deletion in the app: Settings > Account > Delete account;
- any reauthentication required before deletion;
- what account/auth data is deleted;
- what local PDFs remain on the user's device unless the user separately deletes them;
- any legally required retention, if applicable;
- expected deletion timing;
- support/contact path if deletion cannot be completed.

The public deletion URL must remain reachable even when the user cannot sign in.

## 5. Developer website / app-ads.txt

The developer website must:
- identify the developer/app clearly;
- link Privacy, Support and Terms if Terms are used;
- link Delete Account when accounts ship;
- host the root-level `app-ads.txt` required by the final AdMob configuration;
- use HTTPS and remain publicly reachable.

## Release evidence
Before GREEN, record:
1. final live URL for each applicable page;
2. HTTP/HTTPS reachability check;
3. exact production app version/commit checked against the copy;
4. Play Data safety and Contains ads cross-check;
5. auth/account deletion cross-check when auth is enabled;
6. reviewer notes updated with the same URLs.
