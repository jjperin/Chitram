# Chitram

Chitram is a planned native Android TV app for browsing and playing films with family profiles and language-aware recommendations. Development will proceed in tested milestones, starting with a remote-first mock-data interface.

## Project status

The Android app has not been implemented yet. The catalogue and video-source integrations described in the project brief remain unverified. TMDB will provide metadata only; it does not provide playback.

## TMDB API setup

1. Create a TMDB account and request an API credential through [TMDB's API settings](https://www.themoviedb.org/settings/api).
2. Keep the credential in local configuration when the Android project is added. Do not put it in source files, commits, issues, or screenshots.
3. The app's eventual TMDB configuration will be documented here once implemented.

## Development plan

The [project specification](docs/PROJECT_SPEC.md) records the product requirements and development milestones for future sessions. The first implementation milestone is an Android TV project with a polished mock-data home screen, profile picker, D-pad focus, and Back navigation. Playback and catalogue integrations follow only after separate verification.
