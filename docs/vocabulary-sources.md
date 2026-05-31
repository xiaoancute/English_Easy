# Vocabulary Sources

English Easy only bundles vocabulary data from sources with an explicit open-source license.
Full bundled license notices are kept in `docs/third-party-notices.md`.

## Bundled in `student_vocabulary_v1.json`

| Stage | Source | License | Notes |
|---|---|---|---|
| Primary | `guanchunsheng/guanyiyi-english` | MIT | Used as the current primary-school vocabulary source. |
| Gaokao | `pluto0x0/word3500` | MIT | Used as the current gaokao 3500 vocabulary source. |
| Junior | `seed-v1` | Project-owned seed data | Kept as a small placeholder until a clearly licensed source is found. |
| Senior | `seed-v1` | Project-owned seed data | Kept as a small placeholder until a clearly licensed source is found. |

## Rejected for bundling

| Source | Reason |
|---|---|
| `KyleBing/english-vocabulary` | Has junior and senior word lists, but no repository license is declared. Do not bundle without permission or a license change. |

## Policy

- Do not bundle scraped web lists or PDF-derived lists unless reuse rights are clear.
- Prefer repositories with machine-readable data and SPDX-identifiable licenses.
- Keep source names in each `VocabularyEntry.source` so later cleanup can trace where a word came from.
