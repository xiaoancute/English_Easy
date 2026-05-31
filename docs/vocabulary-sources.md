# Vocabulary Sources

English Easy only bundles vocabulary data from sources with an explicit open-source license.
Full bundled license notices are kept in `docs/third-party-notices.md`.

## Bundled in `student_vocabulary_v1.json`

| Stage | Source | License | Notes |
|---|---|---|---|
| Primary | `guanchunsheng/guanyiyi-english` | MIT | Used as the current primary-school vocabulary source. |
| Gaokao | `pluto0x0/word3500` | MIT | Used as the current gaokao 3500 vocabulary source. |
| Junior | `KyleBing/english-vocabulary` | README permission statement | Headwords only. Definitions, phrases, examples, and translations are not bundled. |
| Senior | `KyleBing/english-vocabulary` | README permission statement | Headwords only. Definitions, phrases, examples, and translations are not bundled. |

## README-Based Source

`KyleBing/english-vocabulary` does not declare an SPDX license, but its README states that the vocabulary lists were shared on GitHub for learning projects. To keep the reuse narrow, English Easy imports only the English headwords for junior and senior packs.

## Policy

- Do not bundle scraped web lists or PDF-derived lists unless reuse rights or README permission are clear.
- Prefer repositories with machine-readable data and SPDX-identifiable licenses.
- Keep source names in each `VocabularyEntry.source` so later cleanup can trace where a word came from.
