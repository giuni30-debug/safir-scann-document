# Policy Evidence — Google Play / Android 2026

Checked: 2026-09-08. Re-check before production submission because policies can change.

## Functionality / UX
Google Play requires apps to be stable, responsive and functional; apps that crash, force close, freeze, fail to load or otherwise behave abnormally are not allowed.
Source: https://support.google.com/googleplay/android-developer/answer/9898783

## User data / privacy
Every app must maintain accurate user-data handling disclosures. Play requires a Privacy Policy in Play Console and a Privacy Policy link or text inside the app. Data safety must match the app and third-party SDK behavior.
Source: https://support.google.com/googleplay/android-developer/answer/10144311
Source: https://support.google.com/googleplay/android-developer/answer/10787469

## Metadata
Title, description, icon, screenshots and promotional assets must accurately reflect the real app and must not use deceptive/misleading claims or fake ranking/promotion language.
Source: https://support.google.com/googleplay/android-developer/answer/9898842

## Ads
Unexpected full-screen interstitials that interrupt an action the user just initiated are not allowed. Ads must not cause accidental taps, interfere with normal use or lack a proper dismissal path under the policy.
Source: https://support.google.com/googleplay/android-developer/answer/9857753

## Target API
Starting 2026-08-31, new Android mobile apps and updates submitted to Play must target Android 16 / API 36 or higher (subject to Google's listed form-factor exceptions/extensions).
Source: https://support.google.com/googleplay/android-developer/answer/11926878

## Android App Bundle
New Google Play apps publish using Android App Bundles (AAB).
Source: https://support.google.com/googleplay/android-developer/answer/9844279

## 16 KB native page size
Google documents a Play compatibility requirement for apps targeting Android 15/API 35+ on 64-bit devices and provides testing guidance. This repo keeps an automated native alignment check because OpenCV ships native libraries.
Source: https://developer.android.com/guide/practices/page-sizes

## Content rating
Google's July 2026 policy announcement clarifies that unrated apps are not allowed on Google Play.
Source: https://support.google.com/googleplay/android-developer/answer/17134731

## Reviewer application
These sources are not substitutes for the full Google Play Developer Program Policies. `reviewer-reject-matrix.md` translates them into Safir Scanner-specific controls, and `release-checklist.md` blocks submission until evidence exists.
