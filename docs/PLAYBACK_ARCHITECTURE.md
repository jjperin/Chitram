# Chitram playback architecture and feasibility

Status: **test player and source boundary implemented; automatic Einthusan resolution unverified** (13 November 2026).

## Existing app path

Room stores source movie IDs and page URLs separately from TMDB metadata and viewing progress. Details shows Play only for the one source ID configured in ignored local `playback.properties`. `VideoUrlResolver` runs at Play time; the current `ConfiguredTestVideoUrlResolver` accepts an explicit test URL, validates HTTPS, MP4/HLS format and an `e` expiry when present, then passes the source to Media3. The player supports loading, retry and error states, D-pad controls and per-profile progress. Signed source URLs are not stored in Room or logged. Debug builds may embed the locally configured test URL; release builds omit it.

The owner reports Arul John's unsigned `D{movieId}.mp4` pattern no longer works. Do not revive it as a fallback. A movie ID and `D` filename cannot produce the issuer's signed `e`/`md5` parameters.

## Research references

| Reference | Observed source handling | Fit for Chitram |
| --- | --- | --- |
| [Einthusan Stremio addon](https://github.com/gsbmk007/Einthusan-Stremio-Addon-) | Its Node implementation signs in, uses Puppeteer to wait for `#download-content`, and reads `#UIVideoPlayer` / `data-mp4-link`. A serverless variant reads page attributes without Puppeteer and includes an HLS fallback. It carries a player-provided expiry/signature; it does not calculate either. | Node/Puppeteer, sessions, and CDN rewriting are not a small native Android TV adapter. The latest repo commit inspected was 21 December 2025. Current selector compatibility and actual playback were not verified. Its code logs source URLs or partial cookie values, which Chitram must not copy. |
| [EinthuBot](https://github.com/aban-waleed/einthubot) | Its Python service uses a Premium session and reads `data-mp4-link` from Premium player pages. It re-requests a source after a saved URL expires. It is designed to download files for Jellyfin; its README states Premium is required. | Requires a server/container and a downloader workflow outside Chitram's local-first, no-video-file design. It is a useful indication that links are temporary, not proof of permission or current Android TV compatibility. Its code logs raw URLs, which Chitram must not copy. |

The current public watch page could not be inspected with the available web tool. No authenticated or Premium browser session was used, no provider player was automated, and neither reference was run against a live film. Their present compatibility is **unknown**. Browser automation on a separate Node server would add operational complexity; Puppeteer cannot simply be placed into the Kotlin TV app. A WebView-based experiment would still need to meet access and player requirements, and has not been validated.

Einthusan's [published terms](https://einthusan.tv/terms/) require written permission for streaming content through another application and restrict player reverse engineering, access controls and advertising interference. The owner's reported oral permission concerned catalogue indexing; its scope for playback is unverified. Do not implement extraction, CDN host rewriting, credential automation or ad bypass from these references without an authorised integration path.

## Resolver contract

1. Details passes the stable movie ID and active profile ID to the player. The player retrieves the existing movie record; the catalogue and TMDB layers do not supply video URLs.
2. The resolver is invoked when playback starts or the user retries. A future authorised adapter may use the existing source ID/page URL and return a fresh provider-issued source. The current adapter only accepts the ignored, explicitly supplied debug test URL.
3. Validate the URL, known expiry and MP4/HLS format. Reject a missing, expired or unsupported source with a clear message. Do not place it in Room or logs.
4. Media3 plays the source and saves progress by profile. A source failure offers retry; a future adapter should re-resolve instead of reusing an expired URL.

## Verification and blocker

`assembleDebug testDebugUnitTest` passed with resolver and format tests. No current Einthusan page extraction, stream playback, seeking, resume or Sony Google TV performance was verified in this milestone. The owner asked not to run the emulator. A supported, authorised source mechanism and target-TV validation remain the blockers to catalogue-wide playback.
