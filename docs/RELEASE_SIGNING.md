# Release signing & testing in-app purchases

## Why this exists

Google Play will **not** complete a purchase from a binary signed with the Android debug
certificate. The symptom is confusing, because the first half of billing works: the Play Billing
Library returns product details with correct localized prices, the purchase sheet opens, and only
then does Play report *"the item you were attempting to purchase could not be found."*

A debug build installed over adb has `installerPackageName=null`, the `DEBUGGABLE` flag, and is
signed with `CN=Android Debug`. To transact, the installed build must be signed with the **same
key as a build uploaded to a Play track**.

## One-time setup

Put the keystore somewhere outside the repo (it must never be committed) and add these to
`local.properties`, which is gitignored:

```properties
RELEASE_STORE_FILE=/absolute/path/to/aurapix_keystore.jks
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=...
RELEASE_KEY_PASSWORD=...
```

The same four names are read from the environment instead, for CI. With none of them set the
release build is simply left unsigned, so a checkout without the keystore still builds.

## Building a signed build

```bash
./gradlew :androidApp:assembleRelease
```

The APK lands in `androidApp/build/outputs/apk/release/`. For a Play upload use
`:androidApp:bundleRelease` instead, which produces an `.aab`.

## Checklist before a test purchase can work

All of these must be true — the signature is necessary but not sufficient:

1. **A build is uploaded to a Play track** (Internal testing is enough) and **rolled out**, not
   left as a draft.
2. The installed build is signed with the **same key** as that upload, and its `versionCode` is
   one Play recognises. Bump `versionCode` in `androidApp/build.gradle.kts` for each upload.
3. The products are **Active** in Play Console, and each subscription has an **active base plan**.
4. The tester's Google account is on the **License testing** list *and* has **joined the testing
   track** via its opt-in link. License testing alone does not grant track access — this is the
   step most often missed.
5. That same account is the one **currently active in the Play Store app** on the device. A device
   with several Google accounts will happily use the wrong one.

Installing the signed build over adb is fine once 1–5 hold; it does not have to be installed from
Play.

## Verifying what is actually installed

```bash
adb shell dumpsys package com.ferbotz.aurapix | grep -E "versionCode|installerPackageName|pkgFlags"
```

`DEBUGGABLE` in `pkgFlags` means it is still a debug build and purchases will fail.
