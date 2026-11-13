# Chitram — current project brief

Chitram is an **existing** Kotlin Android TV app. Continue development in this repository; preserve its profiles, local catalogue, search, filters, recommendations, artwork cache, TV navigation, and Media3 player. The authoritative product requirements are in [docs/PROJECT_SPEC.md](docs/PROJECT_SPEC.md), and verified work is in [PROGRESS.md](PROGRESS.md).

## Playback direction (13 November 2026)

- Arul John's unsigned `D{movieId}.mp4` path is no longer reliable in the owner's testing. Do not construct an unsigned or signed CDN URL from a movie ID and call it playable.
- Research player-provided, short-lived MP4 or HLS sources, as discussed in [docs/PLAYBACK_ARCHITECTURE.md](docs/PLAYBACK_ARCHITECTURE.md). The Stremio addon and EinthuBot are **research references**, not verified Chitram integrations.
- Resolve a source only when Play is pressed, validate its scheme, format and known expiry, and pass it to Media3. Never store signed video URLs in Room, log URLs or cookies, or download movie files.
- Preserve the provider's authentication, region rules, protection and advertising. Do not add a backend or browser automation merely to imitate the reference projects. An automated Einthusan resolver requires a supported mechanism and authorization for playback in Chitram.
- A single explicitly supplied source can be tested with the ignored local `playback.properties` debug configuration. That is a test adapter, not catalogue-wide playback.
- A separate debug-only WebView experiment opens the stored official watch page without inspecting its player requests. It does not replace Media3, resolve native stream URLs, or save WebView progress. Its playability and Sony TV remote behavior are unverified; see [docs/PLAYBACK_ARCHITECTURE.md](docs/PLAYBACK_ARCHITECTURE.md).

## Next playback milestone

Obtain a supported and authorised way to request a fresh source for a film ID, verify it against the current service and on the target Sony Google TV, then implement only that adapter behind `VideoUrlResolver`. Test loading, pause, seek, expiry, retry, Back, and per-profile resume. If access is unavailable, keep listings marked playback unverified.
