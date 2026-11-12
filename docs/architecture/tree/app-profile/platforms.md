# `app-profile/platforms/`

> Part of [`app-profile/`](../app-profile.md)  
> **Measured:** 70 tracked files — 29× `.png`, 19× `.yaml`, 15× `.gitkeep`, 3× `.json`, 3× `.md`

70 tracked files.

<!-- tree-scaffold:end -->
## Significance

Per-platform configuration and, more consequentially, the **store-compliance declarations**: age rating,
content rights, export compliance, data safety, privacy and the content-rating questionnaire.

These are not paperwork. A store checks them before review, and an incorrect answer is a policy
violation rather than a listing error — an undeclared regulated app is removed, not warned. A missing
`ITSAppUsesNonExemptEncryption` stops every iOS upload to ask, which blocks an automated release.

They are authored here rather than clicked through a console so the answers are reviewable in a diff and
reproducible on a re-submission. `media/` alongside them holds the fork's screenshots and promo art,
which `syncForkConfig` projects into each platform's metadata tree.
