# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**英易 (English Easy)** is an Android app that helps Chinese speakers understand English words by revealing their conceptual core through AI-generated "concept cards" rather than dictionary translations.

**Key principle**: Instead of translating "spring" as "春天/弹簧/泉水", the app reveals the unified mental image: "a force suddenly released upward/outward" — showing how all meanings radiate from this core concept.

## Build & Development Commands

### Standard build tasks
```bash
# Build debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Build release APK (requires signing secrets as env vars)
./gradlew assembleRelease

# Run unit tests
./gradlew testDebugUnitTest
# Or run a specific test
./gradlew testDebugUnitTest --tests ConceptCardValidationTest

# Compile Kotlin only (faster feedback during UI work)
./gradlew compileDebugKotlin
```

### Clean builds
```bash
# Clean build outputs
./gradlew clean

# Nuclear option: clean + gradle cache + reimport
rm -rf .gradle build app/build && ./gradlew --refresh-dependencies
```

## Architecture

### Core data flow: Query → Cache → LLM → Card

1. **User queries a word** → `HomeViewModel.lookup(input)`
2. **Check cache** → `ConceptRepository` queries Room with normalized `word` + `promptVersion`
3. **Cache hit (no context sentence)** → Return cached card immediately
4. **Cache miss / force refresh / with context** → Call `OpenAiCompatibleApi` with `SYSTEM_PROMPT_V3`
5. **Parse + validate JSON** → Deserialize into `ConceptCard`, then `validateStructure()`
6. **Save & return** → Write Room cache; contextual lookups preserve an existing general card's JSON

**Prompt versioning**: When `CURRENT_PROMPT_VERSION` changes, all cached cards auto-expire on next query.

### Key architectural layers

**UI (Jetpack Compose + Material 3)**
- `AppRoot.kt` — bottom nav with 5 tabs: Home / History / Favorites / Study / Settings
- `home/HomeScreen.kt` — word / sentence / expression modes + concept card display
- `components/ConceptCardView.kt` — concept card (core concept, scenarios, branches; misconceptions collapsed by default)
- Recent Material You redesign (2026-06): tonal surfaces, pill-shaped buttons, 28dp hero card radius

**Data layer**
- `data/llm/ConceptRepository.kt` — cache-first lookup + LLM fallback + structure validation
- `data/llm/OpenAiCompatibleApi.kt` — Retrofit interface for OpenAI-compatible endpoints
- `data/prompt/SystemPrompt.kt` — `SYSTEM_PROMPT_V3` + `CURRENT_PROMPT_VERSION` constant
- `data/model/ConceptCardValidation.kt` — post-parse structure checks
- `data/util/WordNormalizer.kt` — shared word normalization for cache keys / learning state
- `data/local/ConceptCardEntity.kt` — Room entity with `@PrimaryKey word`
- `data/settings/SettingsRepository.kt` — DataStore for API config (baseUrl, model, apiKey)

**Dependency injection (Hilt)**
- `di/NetworkModule.kt` — provides Retrofit with dynamic baseUrl from settings
- `di/DatabaseModule.kt` — provides Room database + DAOs

### BYOK (Bring Your Own Key) model

- No shared API key, no backend server
- API credentials stored locally via encrypted DataStore
- User configures: `baseUrl`, `model`, `apiKey` in Settings tab
- Retrofit client rebuilds on config change (see `NetworkModule.provideRetrofit`)

### Material You theme system

Recent redesign (June 2026) replaced hardcoded colors with dynamic M3 theming:
- `ui/theme/Theme.kt` — uses MaterialKolor to generate full color schemes from seed color
- `ui/theme/Type.kt` — sans-serif typography scale (38sp hero, 30sp headlines)
- `ui/theme/Shape.kt` — 8dp to 28dp rounded corners
- `ui/components/UiRescueComponents.kt` — shared tonal components (SurfaceCard, TonalIconButton)

8 preset seed colors + "follow wallpaper" dynamic color option.

## Prompt Engineering

**Location**: `app/src/main/java/io/github/xiaoancute/englisheasy/data/prompt/SystemPrompt.kt`

**Upgrading prompts**:
1. Edit `SYSTEM_PROMPT_V3` (or create `SYSTEM_PROMPT_V4`)
2. Increment `CURRENT_PROMPT_VERSION = 4`
3. Update `ConceptRepository` to use new prompt
4. All old cached cards auto-expire on next user query

**JSON structure**: LLM must return strict JSON (no markdown wrapper) matching `ConceptCard` schema:
```kotlin
data class ConceptCard(
    val word: String,
    val entryType: EntryType,  // WORD | FIXED_PHRASE | FREE_COMBINATION
    val coreConcept: CoreConcept?,
    val chineseApproximation: String?,
    val scenarios: List<Scenario>?,
    val misconceptions: List<Misconception>?,
    val branches: List<Branch>?,
    val promptVersion: Int,
)
```

## Testing

Unit tests focus on **data layer** (repositories, prompt versioning logic):
- `ConceptRepositoryTest` — cache hit/miss, version expiry
- `WordLearningStateRepositoryTest` — learning state transitions

**No UI tests** for the Material You redesign — it's a visual-only refactor with zero behavior changes.

## Release Process

GitHub Actions (`.github/workflows/build.yml`) builds on every push and creates releases on `v*` tags.

**To release**:
1. Configure repository secrets: `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`
2. Set repository variable: `VERSION_CODE` (incremented integer)
3. Push tag:
   ```bash
   git tag v0.2.0
   git push origin v0.2.0
   ```
4. CI runs tests → builds debug + release APKs → creates GitHub Release

**Debug builds** are always available as workflow artifacts (no secrets required).

## Important caveats

- **API key security**: Never commit `apiKey` values to git. Keys only exist in device-local DataStore.
- **SDK path**: `local.properties` with `sdk.dir=/home/x/Android/Sdk` is gitignored but required for local builds.
- **Compose previews**: Use `@Preview` annotations liberally for rapid UI iteration.
- **Material Kolor**: Theme.kt uses `rememberDynamicColorScheme(seedColor, isDark, isAmoled=false)` — the `isAmoled` param is required for MaterialKolor 2.0.0 API.

## Navigation flow

```
AppRoot (Scaffold + BottomNavigation)
├── HomeScreen → word / sentence / expression → card views
├── HistoryScreen → click item → Home with that word
├── FavoritesScreen → click item → Home with that word
├── StudyScreen → pack tasks + light spaced review
└── SettingsScreen → API + theme
```

Bottom nav uses `NavigationBar` with default M3 pill indicator (`secondaryContainer`).
