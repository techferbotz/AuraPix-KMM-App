# Firebase: Crashlytics & push notifications

## Why this exists

The Android app is wired for **Firebase Crashlytics** (crash reports) and **Firebase Cloud
Messaging** (push notifications). Both are configured by one file downloaded from the Firebase
console, `androidApp/google-services.json`. Without it the app still builds and runs — with
Firebase off, and a Gradle warning saying so — so a checkout without the file isn't broken.

## Setup

Done: the Firebase project is **`aurapix-4ab8b`**, with an Android app for `com.ferbotz.aurapix`,
and its config is committed at **`androidApp/google-services.json`**. To replace it — a new
project, or after re-registering the app — download it again from the Firebase console (Project
settings → Your apps) and overwrite the file. A SHA-1 isn't needed for Crashlytics or push.

The file holds project identifiers, not credentials: Firebase's own guidance is that its API key
may be checked in, and this repository is public. If you restrict that key in the Google Cloud
console, list every signing certificate that runs the app — debug, upload and Play App Signing —
or Firebase stops working in whichever build you left out.

## Crashlytics

- Collection is **on for release builds and off for debug builds** (`crashlyticsCollectionEnabled`
  in `androidApp/build.gradle.kts`), so the console shows real users' crashes, not development ones.
- To see it work: install a **release** build, make it crash, and open it again. Reports upload on
  the next launch and appear in the console within a few minutes.
- Each report carries the signed-in account's id — an opaque uuid, never the email — so a user's
  report to support can be matched to their crashes. Signed out, it's empty.

## Push notifications

- Send from the Firebase console: **Messaging → New campaign → Notifications**, targeting the
  AuraPix Android app. Nothing registers devices with the AuraPix backend.
- Optional custom data **`link`** = `https://aurapix.ferbotz.com/template/<id or slug>`: tapping the
  notification opens that template.
- Channel: "Updates" (`notification_channel_id` in `androidApp/src/main/res/values/strings.xml`).
- On Android 13+ the app asks for the notification permission **once**, the first time a finished
  portrait is on screen, and again only from **Settings → Push Notifications**.
- Testing on one device: a **debug** build prints its FCM token to logcat under the tag
  `AuraPix-Push`; paste it into the console's "Send test message".

## Not done yet

- **iOS.** It needs the Firebase Apple SDK (Swift Package Manager), `GoogleService-Info.plist`, an
  APNs key uploaded to Firebase, and the Push Notifications capability in Xcode. Until then iOS
  hides the notifications setting.
- **Pushes about a user's own work** ("your portrait is ready"). That needs the backend to store
  each device's FCM token and send through Firebase — a request for the contract when it's wanted.
- **Privacy disclosures.** The Privacy Policy (served by the backend) and the Play Console's Data
  safety form should now name Firebase Crashlytics (crash logs, diagnostics) and Cloud Messaging
  (device identifiers).
