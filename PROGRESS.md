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
- The instrumented Room persistence test is authored but has not yet been run on the emulator.
- A current Android Emulator, Google TV API 34 x86 image, and `Chitram_TV` virtual device are installed on drive E. Earlier boot attempts failed before BIOS virtualization and Windows Hypervisor Platform were enabled. [scripts/start-chitram-tv.cmd](scripts/start-chitram-tv.cmd) provides the emulator launch, APK install and app start commands, though its full flow has not been reverified after the AVD input setting change.

### Emulator launch update — 8 November 2026

- After BIOS virtualization and Windows Hypervisor Platform were enabled, `Chitram_TV` booted on drive E with WHPX acceleration. The AVD needed `hw.keyboard=yes` for D-pad/keyboard input; it currently uses 4 GB RAM and host graphics for testing.
- `adb` reported the emulator as a connected device. `app-debug.apk` installed successfully, `com.chitram.tv/.MainActivity` launched, and an emulator screenshot confirmed the profile picker with **Create profile** focused. This verifies installation and first launch, not the remaining navigation flows or playback.
- Google TV setup and a frozen USB debugging prompt complicated first boot. The test AVD was reset with the owner's approval. A local `-skip-adb-auth` emulator flag was used for this run; it is not an app setting or a production requirement.

### TV navigation and layout update — 8 November 2026

- Main screens now focus their active sidebar item on entry. From filters and film cards, Left explicitly returns to that item, including from a lower Browse card that previously sent focus to Switch profile.
- The app uses a TV-safe outer margin. The sidebar scrolls on short displays so Switch profile can be reached without clipping. The profile picker scrolls, Browse uses one lazy vertical grid for filters and films, and the featured banner grows to fit its title on narrow layouts.
- `assembleDebug testDebugUnitTest` passed. The updated APK was installed on the Google TV emulator. D-pad navigation from Browse to a filter and a lower film card, then Left back to Browse, was visually checked at 1920×1080 and at a temporary 1280×720 display override. Switch profile was fully visible and focusable after sidebar scrolling at 1280×720. The emulator display override was reset to 1920×1080.

### Visual polish and profile selection — 9 November 2026

- Each profile has one focusable selection control on its avatar; its name is a plain label beneath it. Edit profile remains a separate action.
- Refined the dark palette, compact sidebar, focus indicators, spacing, headings, film cards and details typography. The featured DEMO film now uses original generated artwork; fictional titles remain clearly labelled DEMO.
- `assembleDebug testDebugUnitTest --offline` passed after the changes (eight JVM tests). The updated APK installed on the Google TV emulator. At 1920×1080, the profile picker, Home and Browse screens were visually checked. The first avatar received initial focus; Down focused Edit profile, and Up then OK opened Home through the avatar. The emulator had to be restarted after its setup screen froze. A physical Sony TV and smaller display sizes were not tested for this revision.

### Focus-driven Home preview — 9 November 2026

- Home now updates its large preview when D-pad focus moves across film cards. It shows the selected DEMO film's title, year, language, genres and short synopsis; OK on the card opens details. The selected preview survives Back from details.
- Home rows use landscape cards. The preview and cards stay visible together at 1920×1080 and in the emulator's temporary 1280×720 display mode; the 720p layout shortens the preview and cards. The display override was reset to 1920×1080.
- The final `assembleDebug testDebugUnitTest --offline` build passed all eight JVM tests, and its APK installed on the Google TV emulator. D-pad Right changed the preview between the first and second films; OK opened the matching details, and Back returned to Home with the selected preview retained. Physical Sony TV navigation and lower resolutions were not tested.

### One-click emulator launch — 9 November 2026

- Updated `scripts/start-chitram-tv.cmd` to reuse a running emulator and to launch a stopped test AVD with the same 4 GB, host-graphics and local ADB settings used during verification. It installs the current debug APK and brings Chitram to the foreground.
- Ran the script successfully against the already running emulator and confirmed `com.chitram.tv/.MainActivity` was the resumed activity. A cold-start run of the revised script was not tested.

### Predictable D-pad focus — 9 November 2026

- Home film rails and Browse filter groups now enter at their first item and restore the previously focused item when revisited. Left moves to the previous item within a row or grid and returns to the active sidebar item only from its left edge. Right from the sidebar targets that screen's content; on Home it returns to the last focused row.
- Browse and My List attach content entry to their scrollable grids, avoiding a crash when a previously targeted first item has scrolled off screen. Empty My List keeps focus on its menu item. Search returns to the menu with Left when its query is empty, including while the TV keyboard is showing.
- The profile picker waits for Room to emit saved profiles before enabling Add profile, removing an early-OK startup race.
- `assembleDebug testDebugUnitTest --offline` passed (eight JVM tests). The APK installed on the Google TV emulator. D-pad entry, vertical row transitions, row restoration, card-to-card Left, Browse filter-to-film transitions, sidebar return, Search return and empty My List were visually checked at 1920×1080. Browse entry and sidebar re-entry after scrolling, plus Home row return, were checked at a temporary 1280×720 override; the display was reset to 1920×1080. No focus crash appeared in the final emulator checks. The physical Sony TV and exhaustive combinations of all screens and text-entry states remain untested.

### Local catalogue and metadata — 9 November 2026

- Added a metadata-only Einthusan catalogue adapter for the eight owner-supplied languages. A recent-page check runs when due after 24 hours; a full alphabetic scan runs when due after about seven days. Full scans save a language, letter and page checkpoint so interrupted scans resume without deleting existing data. Browse also offers manual Check new and Full scan actions.
- The completed emulator scan stored **16,538 distinct Einthusan listings**: Bengali 869, Hindi 4,368, Kannada 1,501, Malayalam 2,675, Marathi 388, Punjabi 351, Tamil 3,454 and Telugu 2,932. These are page listings, not verified playable films. The source pages do not provide a reliable change feed, so the recent check may miss changes until a full scan. The scan checkpoint reset to its start position with no error after completion.
- Room now stores source IDs, language, title, year, synopsis, source page, availability and optional TMDB metadata. Explicit migrations from database versions 1 through 3 preserve profiles, movies and watchlists. All 40 fictional demos stay labelled DEMO; real listings stay labelled LISTED and have no Play control. Home shows real listings in a separate playback-unverified rail, while recommendations remain limited to demos or independently confirmed titles.
- On opening a real listing, exact-title/year TMDB matching adds poster and backdrop URLs, genres and a labelled community rating when a safe match exists. Coil loads artwork on demand with a 250 MB disk cache; an unmatched listing keeps its source text and placeholder. About & credits includes TMDB's approved logo and API notice.
- `assembleDebug testDebugUnitTest --offline` passed **19 JVM tests**. The Android instrumentation test for profile and list persistence passed on the Google TV API 34 emulator. Gradle removed the test app package after this run; the completed catalogue and saved profile were restored from a verified database snapshot, and the current APK was reinstalled. The restored database passed `PRAGMA integrity_check`, and Home, Browse, Search, About and a TMDB-enriched Vikram detail screen were visually checked. The current debug APK is approximately 17.3 MB; app data measured about 7.7 MB before substantial artwork caching. A physical Sony TV, offline sync recovery, full 250 MB cache eviction and playback have not been tested.

### Crash and artwork responsiveness — 9 November 2026

- The owner's report of being sent to Google TV's account chooser was traced to two actual Chitram crashes in emulator logs. Both occurred during Compose D-pad focus movement involving lazy rows or grids. Removed unstable focus restoration targets and attached Home's content-entry requester to its stable scrolling container.
- Visible film cards now enqueue a bounded number of TMDB metadata lookups, so matched artwork appears without opening each detail screen. Search results for the same title and year, plus matched genre lookups, are reused in a small in-memory cache. Coil continues to cache image bytes on disk and now crossfades newly loaded artwork. The app shows demo Home content while the local Room catalogue loads, and title search waits briefly after typing before querying Room.
- `assembleDebug testDebugUnitTest --offline` passed all **19 JVM tests** after the changes. The updated APK installed over the existing emulator app without clearing its 16,538 saved listings. Rapid Browse D-pad traversal, Home row entry and sidebar return were exercised; the app stayed foreground and no new crash appeared in the crash log. Posters appeared on visible Browse cards before details were opened. App data measured about 10 MB with roughly 2 MB in the image cache; a running-app snapshot measured roughly 143 MB PSS. This is an emulator measurement, not a Sony TV benchmark. The entire catalogue is still held in an in-memory Room flow, so a lower-memory TV needs direct measurement.

## Remaining work and known limitations

### Artwork and playback test foundation — 13 November 2026

- Landscape cards and the Home preview now fall back to an available poster when a backdrop is missing. Profile selection queues at most four missing first-screen TMDB matches and warms at most four known posters in Coil. Up to three metadata lookups can run concurrently, with duplicate in-flight movie IDs suppressed, while keeping the 250 MB disk cache and visible-card loading.
- Added Media3 ExoPlayer and TV PlayerView for a single explicitly configured HTTPS test stream. The Play test stream button appears only on the matching catalogue source ID. Back returns to details; player errors are shown in-app; a known duration and position are saved by profile for resume. No video files are stored by Chitram.
- The owner's supplied Bangalore Days signed URL was kept only in ignored `playback.properties` for a private debug build. A HEAD probe returned HTTP 200 and `video/mp4` on 13 November 2026. Its expiry means this is temporary and does not make other catalogue listings playable. The public downloader page exposes no documented playback API.
- `assembleDebug testDebugUnitTest --offline` succeeded after the final artwork change (**20 JVM tests**). Runtime playback and D-pad controls were **not** tested: the emulator reported unavailable hardware acceleration and exited, and the owner then asked not to use the emulator. The physical Sony TV was not tested. Do not claim seamless artwork or playback without device measurement.

### Playback resolver research and boundary — 13 November 2026

- Reviewed the Stremio addon and EinthuBot source. Both rely on player-provided link fields rather than calculating signatures. Stremio uses Node/Puppeteer or a page-attribute fallback; EinthuBot uses a Premium session and a Python/Docker download workflow. Current watch-page compatibility and permissions for Chitram remain unverified. Findings and links are recorded in [docs/PLAYBACK_ARCHITECTURE.md](docs/PLAYBACK_ARCHITECTURE.md).
- Added a Play-time `VideoUrlResolver` interface around the existing explicit debug test source. It validates HTTPS, MP4/HLS format and a known `e` expiry. Media3 now includes HLS support, shows source-loading and playback-error states, and offers retry. The player looks up its movie from Room before resolving; no expiring URL is saved in Room or logged.
- `assembleDebug testDebugUnitTest` passed with **21 JVM tests**. The emulator was not used at the owner's request. No automatic Einthusan extraction or end-to-end playback, seeking, resume or Sony TV test was performed. Catalogue-wide playback remains blocked on a supported, authorised fresh-source mechanism.

- Test the full D-pad, Back and profile editing flows on the physical Sony TV. The emulator checks above cover the reported sidebar return path and short-screen visibility, but not every screen or physical TV overscan setting.
- Verify actual playback access separately from catalogue listing and TMDB matching; do not present a listing as playable until that work is complete.
- Validate the Media3 test player on the target TV and obtain an authorized, durable source-resolution method before enabling Play across the catalogue. Do not infer or generate signed URLs from source IDs.
- Keep adding explicit migrations before future Room schema changes; preserve profile and watchlist data on APK updates.
- Validate offline refresh recovery, full image-cache eviction and storage/performance on the physical Sony TV.
