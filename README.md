# Chitram

Chitram is a native Android TV / Google TV film-browsing app in development. It keeps a local catalogue of **Einthusan listings** alongside 40 fictional, clearly labelled demonstration films. A listing is not proof that playback works; video playback is not implemented.

The first complete eight-language scan on 9 November 2026 stored **16,538 distinct listings** on the Google TV emulator. This is a count of catalogue entries, not confirmed playable films.

The [project specification](docs/PROJECT_SPEC.md) records product requirements. [Progress](PROGRESS.md) records what has been implemented and verified.

## Current foundation

- TV launcher entry, dark 16:9 interface and D-pad focusable Home, Search, Browse and My List navigation.
- Local Room storage for profiles, Einthusan listings, demo films, watchlists and future playback progress. Existing data is preserved by versioned migrations.
- Profile create/edit/select with primary and secondary language choices and favourite genres. Languages: Tamil, Hindi, Telugu, Malayalam, Kannada, Bengali, Marathi and Punjabi.
- Profile-driven home rows, local title search, browse filters and sorting, per-profile My List, and details with an honest playback placeholder. Home has a separate, clearly labelled rail for Einthusan listings; unverified listings are excluded from recommendations.
- A quick background check of recent listings after 24 hours, a checkpointed full alphabetic scan after about seven days, and manual Check new / Full scan actions in Browse. Existing listings remain available if the network fails.
- Conservative, on-demand TMDB matching when a listing's details are opened. Matched posters, backdrops, genres and community ratings are cached locally; unmatched listings retain their source description and a clean placeholder. Coil's disk cache is capped at about 250 MB. About & credits shows TMDB's approved logo and API notice.
- Deterministic local recommendation ranking is currently limited to demos until real listing playback availability is independently confirmed.

## Build

Use a current Android Studio version or the Gradle wrapper. Install Android SDK Platform 34 and Build Tools 34.0.0, and use JDK 17 or 21. The old Android Studio installation originally present on this machine predates this build configuration; command-line Gradle is the verified path.

```text
gradlew.bat assembleDebug testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Install it on a connected Android TV with `adb install -r app-debug.apk` from that output directory. On first launch, create a profile. Open a demo film's details to add it to My List. The Play button explains that playback is a later milestone.

## Test on this PC

The Google TV API 34 x86 emulator, Android SDK and virtual device named `Chitram_TV` are prepared under `E:\ChitramAndroidSdk` and `E:\ChitramAVD`. First enable CPU virtualization in firmware and Windows Hypervisor Platform, then restart Windows. Double-click [scripts/start-chitram-tv.cmd](scripts/start-chitram-tv.cmd) to open Chitram. The script starts the emulator if needed, installs the latest built debug APK, and opens the app. If the emulator is already running, it reuses it. Press Enter on the focused profile avatar to reach Home. Rebuild the APK first if you have changed the source.

## TMDB setup

Create an ignored `tmdb.properties` file in the project root with `readToken=YOUR_TMDB_API_READ_ACCESS_TOKEN`, then rebuild. The owner's token is configured locally on this development PC and is not tracked by Git. This private APK embeds the token for API requests, so keep the APK private. TMDB supplies metadata and artwork, not playback.

## Limitations

- Einthusan listings are stored from public catalogue pages but playback access has not been verified. A recent-page check is not a complete change feed; a full scan takes longer and resumes from its stored checkpoint if interrupted.
- TMDB matches require an exact normalized title and release year. Many listings will remain unmatched, and artwork is requested only when needed.
- No video playback or physical Sony TV validation yet.
- Release signing and repeatable sideload updates are not configured yet.
