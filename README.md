<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

VediRaksha is a worker-safety app for room capacity monitoring, shifts, workforce
management, wages, emergency alerts, and compliance reports. Its mobile UI uses
five bottom-navigation destinations; workforce management and reports are grouped
under **More**. Appearance settings support System, Light, and Dark themes with
persistent accent-color selection. Safety reports include a room-capacity bar chart
with per-room colors, a legend, labeled percentage scale, and tap-to-view load values.

The Home dashboard also includes an image-based room safety check. Its replaceable
`RoomSafetyAnalyzer` currently uses a basic on-device color heuristic; it is a
prototype aid, not a certified fire detector or a substitute for human inspection.
Connect a trained vision model before relying on automated decisions in a live plant.
To provide a custom looping emergency sound, add `danger_sos.mp3` under
`app/src/main/res/raw/`; the app plays it for active SOS alerts. Without that asset,
the app uses a configurable synthesized alarm tone.

## Updating the Android app

Play in-app updates check Google Play when the app opens and can also be started
from **Settings → Check for app updates**. The app uses a flexible update: Play
downloads it while the app remains usable, then Android/Play completes installation.
Android requires user consent; apps cannot silently install their own updates.

To enable updates on a phone:

1. In Play Console, create the app using application ID
   `com.aistudio.vediraksha.worker`, configure Play App Signing, and upload a signed
   Android App Bundle (`.aab`) to the Internal testing track.
2. Add the Google account used on the phone as an internal tester. On the phone,
   open the Play testing opt-in link using that account and install the app from
   the Play Store listing. In-app updates work only for Play-installed builds;
   the debug APK produced by `assembleDebug` is for manual testing and will not
   receive Play updates.
3. For every release, build with a never-before-used, higher version code, and
   upload the new signed bundle to the testing or production track. Defaults are
   set in `app/build.gradle.kts`; they can be overridden, for example:
   `gradle :app:bundleRelease -PappVersionCode=3 -PappVersionName=1.2`.
   Use the same Play app and upload-key identity each time, then roll out the
   release. Eligible opted-in devices will be offered the update automatically.
4. To install an APK manually instead, transfer the newer APK to the phone,
   open it from Downloads, and confirm **Install**. Android may ask once for
   permission to install apps from the chosen file/browser app. The APK must use
   the same signing certificate as the installed app and have a higher version
   code. Play delivery is recommended for automatic updates.

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/be99681e-95fd-4c3e-9752-b7ac89a14654

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.
