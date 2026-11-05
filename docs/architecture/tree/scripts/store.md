# `scripts/store/`

> Part of [`scripts/`](../scripts.md)  
> **Measured:** 4 tracked files — 3× `.sh`, 1× `.rb`

### Scripts

| Script | Purpose |
|---|---|
| `generate-screenshots.sh` | generate-screenshots.sh |
| `store-listing-preflight.sh` | scripts/store/store-listing-preflight.sh |
| `sync-play-listing.sh` | scripts/store/sync-play-listing.sh |

<!-- tree-scaffold:end -->
## Significance

Store listing and screenshots. `store-listing-preflight.sh` mirrors the release gate locally and fails
on a missing field or an over-length string, so an upload is never rejected by the Play or App Store API
after a full build.

`generate-screenshots.sh` renders HTML to PNG at exact store resolutions — 1080×1920 for Android,
1320×2868 for the 6.9" iPhone. The dimensions are fixed because a scaled image is rejected at upload,
and the iOS set is rendered from the SAME source HTML as Android rather than authored twice.
