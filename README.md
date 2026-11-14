# Chitram

Chitram is a native Android TV / Google TV film-browsing app in development. It keeps a local catalogue of **Einthusan listings** alongside 40 fictional, clearly labelled demonstration films. A listing is not proof that playback works. Media3 can play a separately configured test stream for one matching listing; catalogue-wide playback is not integrated.

The first complete eight-language scan on 9 November 2026 stored **16,538 distinct listings** on the Google TV emulator. This is a count of catalogue entries, not confirmed playable films.

The [project specification](docs/PROJECT_SPEC.md) records product requirements. [Progress](PROGRESS.md) records what has been implemented and verified.

## Current foundation

- TV launcher entry, dark 16:9 interface and D-pad focusable Home, Search, Browse and My List navigation.
- Local Room storage for profiles, Einthusan listings, demo films, watchlists and future playback progress. Existing data is preserved by versioned migrations.
- Profile create/edit/select with primary and secondary language choices and favourite genres. Languages: Tamil, Hindi, Telugu, Malayalam, Kannada, Bengali, Marathi and Punjabi.
- Profile-driven home rows, local title search, browse filters and sorting, per-profile My List, and details that mark unverified playback clearly. Home has a separate, clearly labelled rail for Einthusan listings; unverified listings are excluded from recommendations.
- A quick background check of recent listings after 24 hours, a checkpointed full alphabetic scan after about seven days, and manual Check new / Full scan actions in Browse. Existing listings remain available if the network fails.
- Conservative, on-demand TMDB matching for visible listings and detail screens. A bounded queue loads metadata as cards appear, and repeated title/year lookups share their result during the session. On profile selection, a few first-screen candidates are queued and known posters are warmed. Landscape cards use a poster if no backdrop exists. Matched posters, backdrops, genres and community ratings are cached locally; unmatched listings retain their source description and a clean placeholder. Coil's disk cache is capped at about 250 MB. About & credits shows TMDB's approved logo and API notice.
- Deterministic local recommendation ranking is currently limited to demos until real listing playback availability is independently confirmed.

## Updates from GitHub

Open **Check for updates** in Chitram's side menu. It checks the latest public [GitHub Release](https://github.com/jjperin/Chitram/releases), downloads a newer APK, verifies it, and opens Android's installer. Approve the installation on the TV. Android may first ask you to allow Chitram to install apps; return to Chitram and press **Install update** again. Profiles, lists and the local catalogue remain when the installed app and release APK have the same package ID and signing key.

The first signed release was published and its public APK verified on 14 November 2026. The update button's actual download and installation flow still needs testing on the Sony TV. The workflow publishes APKs for app/build changes pushed to `main`; documentation-only commits do not create a new APK.

Code pushed to GitHub is not itself an installable update. The `Publish TV APK` workflow builds a release for each push to `main` using four repository Actions secrets configured on 14 November 2026: `CHITRAM_KEYSTORE_BASE64` (base64 of the ignored `signing/release.jks`), `CHITRAM_STORE_PASSWORD`, `CHITRAM_KEY_ALIAS`, and `CHITRAM_KEY_PASSWORD` (values from ignored `signing.properties`). Keep a private backup of the keystore and passwords: losing them prevents future updates to installs signed with that key. Do not upload `signing.properties`, the keystore or a private debug APK to the public repo.

The first signed release needs a one-time install. It cannot replace an older debug-signed build in place; Android will report a signature mismatch. Do not uninstall an existing app until its local data has been backed up or you accept losing it. Public release APKs omit the private TMDB token and temporary playback URL; new TMDB enrichment is unavailable in those builds until a safe on-device credential option is added. Debug builds remain private for playback experiments.

## Build

Use a current Android Studio version or the Gradle wrapper. Install Android SDK Platform 34 and Build Tools 34.0.0, and use JDK 17 or 21. The old Android Studio installation originally present on this machine predates this build configuration; command-line Gradle is the verified path.

```text
gradlew.bat assembleDebug testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Install it on a connected Android TV with `adb install -r app-debug.apk` from that output directory. On first launch, create a profile. Open a film's details to add it to My List. A Play test stream button appears only for the one source ID configured below.

## Test the official web player on Sony Google TV

Connect the TV to this PC through ADB, then install the current **debug** APK as an update from PowerShell:

```powershell
& 'E:\ChitramAndroidSdk\platform-tools\adb.exe' install -r 'C:\Users\Jake\Documents\ChatGPT\chitram\app\build\outputs\apk\debug\app-debug.apk'
```

Use the same signing key and package ID so local profiles and catalogue data remain. If ADB reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, stop and report it rather than uninstalling Chitram. Open Chitram → choose your profile → Browse or Search → open an Einthusan listing → **Test Web Playback**. This option appears only if the listing has its official watch-page URL in the local catalogue. No manually copied MP4 URL is needed for this experiment.

The diagnostic header reports whether the page loaded and whether HTML5 fullscreen was requested. With the Sony remote, try starting the website's video, entering fullscreen, OK/Play-Pause, seeking with Left/Right, exiting fullscreen with Back, and returning to Details with Back. Record which actions work, whether login or advertising appears, any error shown, and whether the TV player remains responsive. A loaded page alone is not a playback success. Web viewing does **not** update Chitram's Continue Watching because no supported progress signal is available. The website may not support D-pad input or playback inside WebView; do not assume either until tested. The Android TV emulator is not required for this test.

## Optional playback test

Create an ignored `playback.properties` file in the project root with `sourceId=THE_CATALOGUE_SOURCE_ID` and `url=YOUR_AUTHORIZED_HTTPS_STREAM_URL`, then rebuild the debug APK. Only the matching listing gets a Play test stream button. The isolated resolver accepts HTTPS MP4 or HLS, rejects a known expired link, and passes it to Media3 with loading, retry and error states. The player offers D-pad controls, Back and per-profile resume. The URL is embedded in the **private debug APK**, so do not share that APK while a signed link is valid. Release builds omit the test URL. Temporary signed links stop working after their expiry; this setup does not resolve streams for the rest of the catalogue. See [playback architecture](docs/PLAYBACK_ARCHITECTURE.md).

## Test on this PC

The Google TV API 34 x86 emulator, Android SDK and virtual device named `Chitram_TV` are prepared under `E:\ChitramAndroidSdk` and `E:\ChitramAVD`. First enable CPU virtualization in firmware and Windows Hypervisor Platform, then restart Windows. Double-click [scripts/start-chitram-tv.cmd](scripts/start-chitram-tv.cmd) to open Chitram. The script starts the emulator if needed, installs the latest built debug APK, and opens the app. If the emulator is already running, it reuses it. Press Enter on the focused profile avatar to reach Home. Rebuild the APK first if you have changed the source.

## TMDB setup

Create an ignored `tmdb.properties` file in the project root with `readToken=YOUR_TMDB_API_READ_ACCESS_TOKEN`, then rebuild. The owner's token is configured locally on this development PC and is not tracked by Git. This private APK embeds the token for API requests, so keep the APK private. TMDB supplies metadata and artwork, not playback.

## Limitations

- Einthusan listings are stored from public catalogue pages but playback access has not been verified. A recent-page check is not a complete change feed; a full scan takes longer and resumes from its stored checkpoint if interrupted.
- TMDB matches require an exact normalized title and release year. Many listings will remain unmatched, and artwork is requested only when needed.
- The Media3 player builds, but on-device playback and physical Sony TV behavior have not been validated. The owner's supplied signed URL answered HTTP 200 to a HEAD request on 13 November 2026; that is not a playback test. The owner asked not to use the emulator during this session.
- The TV updater has not been exercised on a physical Sony TV.
