# Chitram development instructions

- Read [docs/PROJECT_SPEC.md](docs/PROJECT_SPEC.md) and [PROGRESS.md](PROGRESS.md) before changing the app. The specification is the project reference; update it when the owner changes requirements.
- Work in verified milestones. Compile, run relevant tests, and report exactly what was and was not tested. Keep the Android TV experience responsive and operable by D-pad, OK and Back.
- Treat Tamil, Hindi, Telugu, Malayalam, Kannada, Bengali, Marathi and Punjabi as the eight owner-supplied Einthusan catalogue languages. Profile choices and language rows must be data-driven; do not default everyone to Malayalam.
- Demo films are fictional and must be labelled DEMO. Do not present them as a real playable catalogue, or present TMDB metadata as proof of availability.
- Keep catalogue discovery, TMDB metadata and playback source resolution separate. Do not infer access from a filename pattern or implement access-control circumvention.
- Keep profile data, watchlists and playback progress local and isolated by profile. Never use destructive Room migrations on user data.
- Never commit TMDB credentials, signed video URLs, local properties, APK signing keys or other secrets. Configure credentials only through ignored local files when that integration is implemented.
- Do not add a backend, analytics, cloud accounts or AI recommendation service without an explicit new requirement. Keep architecture small and understandable.
