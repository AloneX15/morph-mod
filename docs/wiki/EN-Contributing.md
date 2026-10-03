# Contributing and security

Morph **0.3.0** · [Index](EN-Index.md) · [Español](ES-Contributing.md)

## Reporting bugs

Open a repository issue if you have access. Include environment, versions, minimal steps, expected/actual results and sanitized logs. Share third-party resources only when you have permission.

Repository planning documents and development logs are historical; use this wiki and current code for 0.3.0 behavior.

## Contributing code

1. Propose the change before a large modification.
2. Work on a branch from main and keep the PR focused.
3. Use `com.takumistudios.morphmod` packages, English identifiers/comments and translations in both languages.
4. Preserve TakumiStudios attribution and the MIT license.
5. Prefer Fabric APIs; document mixins and their risks.
6. Validate external input, isolate optional integrations and avoid tick/render IO.
7. Run relevant tests and the matrix for multiversion API changes.
8. Update the changelog and final-behavior documentation.

Use descriptive commits, such as `docs: document character anchors`. Do not commit caches, test worlds, tokens or build artifacts to the mod repository.

## Maintaining this wiki

The complete source is versioned under `docs/wiki` in the mod repository. Update both ES/EN pages for a topic, preserve slugs and check links, examples and navigation with `node scripts/check-wiki.mjs`. Update the home page when versions or compatibility change.

To publish on GitHub Wiki too, run `node scripts/export-wiki.mjs`. Save an initial Home page on GitHub, clone `https://github.com/AloneX15/morph-mod.wiki.git`, copy `build/wiki-export/` into the clone, commit and push its default branch. The exporter adapts links and images; it does not publish by itself or change repository visibility.

Do not document a proposed feature as available. When commands, formats or values change, verify implementation and tests. Translations must have the same technical content without translating IDs or JSON fields.

## Security

Use the repository's private vulnerability report form if enabled. Availability depends on the owner's settings: this wiki does not claim it is active. If unavailable, agree on a private channel with the maintainer; do not publish secrets or exploitation instructions in an issue.

Wiki visibility follows the repository. Publishing it does not change code visibility. Mod code uses MIT; respect independent rights associated with third-party resources.

---

[Home](Home.md) · [Complete index](EN-Index.md) · [Help](EN-Troubleshooting.md)

Created by **TakumiStudios**.
