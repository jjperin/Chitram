# Chitram

Chitram is a native Android TV / Google TV film-browsing app in development. This first foundation release uses **fictional, clearly labelled demonstration films**. It does not connect to Einthusan, retrieve TMDB metadata or play video yet.

The [project specification](docs/PROJECT_SPEC.md) records product requirements. [Progress](PROGRESS.md) records what has been implemented and verified.

## Current foundation

- TV launcher entry, dark 16:9 interface and D-pad focusable Home, Search, Browse and My List navigation.
- Local Room storage for profiles, demo films, watchlists and future playback progress.
- Profile create/edit/select with primary and secondary language choices and favourite genres. Languages: Tamil, Hindi, Telugu, Malayalam, Kannada, Bengali, Marathi and Punjabi.
- Profile-driven home rows, title search, browse filters and sorting, per-profile My List, and details with an honest playback placeholder.
- Deterministic local recommendation ranking. Original gradient artwork placeholders are used for demo films; Coil is wired for future real artwork URLs.

## Build

Use a current Android Studio version or the Gradle wrapper. Install Android SDK Platform 34 and Build Tools 34.0.0, and use JDK 17 or 21. The old Android Studio installation originally present on this machine predates this build configuration; command-line Gradle is the verified path.

```text
gradlew.bat assembleDebug testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Install it on a connected Android TV with `adb install -r app-debug.apk` from that output directory. On first launch, create a profile. Open a demo film's details to add it to My List. The Play button explains that playback is a later milestone.

## TMDB setup

TMDB credentials are **not used by this foundation build**. When metadata integration is implemented, configure them in ignored local configuration. Do not put credentials in Git commits, source files, issues or screenshots. TMDB supplies metadata, not playback.

## Limitations

- All 40 films are fictional demo records, not a verified Einthusan catalogue.
- No video playback, catalogue synchronisation or TMDB API calls yet.
- The image cache target and Sony TV hardware validation belong to later milestones. On this machine, the Google TV emulator did not reach a usable boot state without hardware acceleration.
- Release signing and repeatable sideload updates are not configured yet.
