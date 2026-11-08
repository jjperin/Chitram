# Chitram progress

## Session 1 foundation

Implemented in source:

- Kotlin Android TV project with Compose, Navigation Compose, Room and Coil dependencies.
- TV launcher manifest, dark theme, focusable sidebar and profile picker/editor.
- Forty fictional demo film records, seeded only when the database is empty.
- Local profile preferences for the eight owner-supplied languages, plus optional genres.
- Dynamic home rows and deterministic recommendation scoring.
- Search by title, Browse language/genre/year filters and sorting, per-profile My List, and film details.
- Honest placeholder message for Play; no playback or real catalogue claims.
- Unit tests for home language rows and recommendations; instrumented Room persistence test authored.

## Verification

- `gradlew.bat assembleDebug testDebugUnitTest` succeeded with JDK 21 and Android SDK 34 on 8 November 2026.
- Eight JVM tests passed: recommendation/language and Continue Watching rows (5), Browse filtering/sorting (2), and Room profile/watchlist persistence after database reopen (1).
- Debug APK generated at `app/build/outputs/apk/debug/app-debug.apk` (about 10.9 MB). `aapt` confirms package `com.chitram.tv`, minimum API 23, target API 34, and a Leanback launcher activity with an app banner.
- An Android Google TV API 34 emulator image was installed and an AVD was created. It did not reach a usable boot state with the machine's old emulator and no x86 acceleration. An emulator update then failed because drive C had insufficient space. The temporary AVD was removed. No physical Android TV was connected.
- The instrumented Room persistence test is authored but could not run without a booted emulator/device.

## Remaining work and known limitations

- Test D-pad focus, Back navigation, profile editing and rendering in an Android TV emulator or on a Sony TV. These behaviours are implemented but not yet device-verified.
- Add a real catalogue only after availability and access are proven. Keep TMDB enrichment and playback resolution separate.
- Add Media3 playback in its own milestone, with legitimate test media and actual TV validation.
- Add migrations before any Room schema change; preserve profile and watchlist data on APK updates.
- Implement metadata/image caching and catalogue sync in their scheduled milestone.
