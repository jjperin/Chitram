# Chitram project specification

This version-controlled specification summarizes the owner's initial project brief and records the later local catalogue and image-caching requirements for future Chitram development. Implementation must proceed by milestone with builds and tests; requirements below do not imply that an integration has been proven or implemented.

## Product and architecture

- Native Kotlin Android TV / Google TV app, initially tested on a Sony Google TV and distributed as a privately sideloaded APK.
- Original, cinematic dark interface designed for D-pad/OK/Back navigation, with Home, Search, Browse and My List plus compact profile access.
- Navigation must be predictable at TV sizes: Right from the active menu item enters that screen's first available control or restores the last focused content; Down into an unvisited film row or filter group starts at its first item; revisiting a row restores its prior card; Left moves through adjacent cards or filters before returning to the active menu item at the left edge. Never let a focus move silently skip to a later item because of screen geometry, and keep the focused item visible while scrolling.
- Wait for locally saved profiles to load before offering Add profile on launch, so an early OK press cannot open profile creation by mistake.
- On Home, moving D-pad focus across film cards updates a larger preview above the rails with that film's title, language, year, genres and short description. OK on a focused card opens its details. Keep the preview readable and the focused card visible at TV sizes; use a scrollable layout on shorter displays.
- The owner wants artwork to appear promptly on app entry and during browsing. Show bundled or cached art immediately, warm a small number of likely first-screen images, keep image requests bounded, and retain a polished placeholder for titles without a verified TMDB image. Measure cold and warm load times on the target TV before claiming a seamless result.
- Local family profiles with one primary language, zero or more distinct secondary languages, optional genres, and separate watch progress, history, My List and recommendations. Home rails derive from preferences and available titles; no language is hardcoded as the default.
- The Einthusan language set supplied by the owner is **Tamil, Hindi, Telugu, Malayalam, Kannada, Bengali, Marathi and Punjabi**. Offer these eight choices in profile preferences and language filters when using that catalogue; preserve the option for a future catalogue source to supply other languages. Do not silently omit any of the eight or default every profile to Malayalam.
- Local-first architecture using Room for profiles, catalogue and viewing state, and DataStore for small app settings. No account, cloud database, dedicated backend, analytics, paid recommendation service or downloaded movie files.
- TMDB supplies metadata and artwork, not playback. Ratings sourced from TMDB must be labelled as community ratings. Store credentials only in ignored local configuration, never in Git.
- Credit TMDB in an in-app About section using its approved logo and required API notice; keep its identity subordinate to Chitram.
- Catalogue discovery, metadata enrichment and playback-source resolution remain separate. A TMDB match or a constructed video URL does not prove that a film is available. The owner reported oral permission from Einthusan contacts for catalogue indexing on 9 November 2026; its scope has not been independently verified. Preserve region restrictions and stop on blocked pages. Do not bypass DRM, authentication, access controls or provider advertising.
- The owner requested playback and supplied two temporary signed MP4 examples on 13 November 2026. A separate downloader site was the source of those links; its public page describes a paste-and-download form, not an authorized integration API. The movie ID in an MP4 path is insufficient to derive its CDN host, expiry and signature. Do not reverse engineer or generate provider signing parameters. Accept only a separately supplied, legitimate playback URL for a specific film, and show a clear error if it expires.
- Recommendations use a deterministic, documented scoring function over confirmed catalogue titles, with primary and secondary language, genre, quality and recency signals. No ML or LLM service.

## Local movie catalogue

- Store the **entire catalogue confirmed available from the configured source** locally in Room. Plan for approximately 4,000 or more Einthusan films if discovery and access prove viable; that figure is a planning estimate, not a verified inventory or a reason to mark unverified films playable.
- Persist stable source/movie IDs, title, language, release date or year, description, genres, rating and its provenance, matched TMDB ID, and poster/backdrop URLs when available. Keep catalogue availability distinct from TMDB metadata and temporary playback URLs.
- Home, search, filters and recommendations should query the local database first for fast, offline-capable browsing. Use indexes and paging/lazy queries appropriate for the measured catalogue size.
- Never download or store movie video files. Preserve locally stored catalogue data when offline or when a refresh fails.
- A public Einthusan listing means only that its catalogue page exposed a title. Label it LISTED and leave playback unverified until a separate source-resolution milestone confirms a legitimate playable path. Keep fictional DEMO films distinct, and keep Home recommendations limited to confirmed titles or demos.

## Catalogue synchronisation

- Show stored data immediately on launch. If the last successful catalogue check was more than 24 hours ago, check for additions and updates in the background without blocking the interface.
- Prefer incremental discovery and updates, avoiding a full catalogue download when the source supports it. Perform a more comprehensive refresh about every seven days and provide a manual refresh action.
- Record refresh state and failure separately from existing catalogue records. A failed request must not erase cached titles or mark them unavailable without evidence.
- The incremental and seven-day strategies depend on a catalogue source that actually exposes reliable discovery and change information. Prove that capability during the catalogue milestone; document limitations if it is unavailable.
- The current public-page adapter checks the eight language browse pages for recent titles and scans the alphabetic result pages for broader coverage. Full scans store their language/letter/page checkpoint in Room. Because no official change feed has been identified, a recent-page check may miss older additions or removals until a full scan; absence from one page must never delete a stored title.

## Image caching

- Use Coil for automatic on-demand image loading and caching. Store TMDB poster and backdrop **URLs** in Room; store image bytes in Coil's disk cache, not in Room.
- Target an approximately **250 MB** disk cache. Display cached images promptly, fetch and cache missing images as needed, and use the cache's least-recently-used eviction when full.
- Load images lazily as rails and cards become visible. Request appropriately sized images and use bounded memory caching to limit bandwidth, storage and memory use.
- When a visible listing lacks artwork, queue a small number of on-demand TMDB metadata lookups so artwork can appear before the detail screen opens. Reuse results for the same title and year, and never fetch metadata or images for the whole catalogue at once.
- Show a clean placeholder for absent or failed images. Do not pre-download artwork for every catalogue title.
- Preserve the disk cache across app launches. Avoid repeated metadata requests when cached metadata is still valid.
- When a backdrop is absent but a poster is available, use the poster as a cropped landscape fallback in the card and Home preview. Warm only a handful of first-screen candidates when a profile is selected; avoid full-catalogue image prefetch.

## Performance and storage target

- Keep D-pad navigation, scrolling and screen changes responsive while catalogue sync and image requests run in the background.
- Avoid focus restoration paths that target lazy-list items after they detach; keep Home, Browse and Search stable during rapid D-pad movement so the app does not return to the Google TV launcher.
- Aim for roughly **300–400 MB of total app storage**, including APK, metadata database and image cache, subject to measurements on the target Sony Google TV. Adjust cache sizing if measured storage or device limits require it.
- Avoid unnecessary network calls and full metadata re-downloads. Offline or incomplete metadata must produce useful screens, not crashes or blank layouts.

## Development milestones

0. Bootstrap a native Android TV project and build a polished mock-data UI with profile picker, focusable navigation and correct Back behaviour. Mock films must be clearly labelled; do not imply catalogue access or playback.
1. Prove native Media3 playback on an accessible, legitimate test source and test remote controls, errors, seeking and resume on the target TV. Treat the owner-supplied Einthusan URL as an optional manual experiment, not a proven integration.
2. Prove catalogue discovery and availability separately from TMDB matching. Build the Room catalogue, metadata cache and background refresh path when the source's capabilities are established. Implement Coil's on-demand image cache and measure storage. Do not show TMDB-only titles as playable.
3. Build and verify Home, Search, Browse, Details and My List against locally stored, confirmed catalogue titles, with TV-friendly filters, sort, focus restoration and playback navigation.
4. Add editable profiles, dynamic language rails, independent viewing state and deterministic recommendations.
5. Test offline behaviour, data retention, performance, accessibility, actual Sony remote navigation, signing and sideload updates. Report measured storage and verified integrations.

At each milestone: compile, run meaningful tests, state what works, identify blockers and name the next concrete task. Do not implement a later milestone solely because it appears in this specification.
