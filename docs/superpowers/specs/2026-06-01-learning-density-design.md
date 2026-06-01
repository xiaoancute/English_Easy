# Learning Density Design

## Goal

Make English Easy feel less like a one-off lookup tool and more like a daily learning product, without adding patronizing onboarding, noisy gamification, or a broad visual redesign.

The next product pass should turn the existing `学习` tab into the main learning path:

- choose a student vocabulary range
- continue today's learning
- review due words
- see progress at a glance
- keep the existing concept-card workflow as the actual study unit

## Current Context

The code already has most of the foundation:

- `VocabularyCatalog` loads `student_vocabulary_v1.json` with 小学 / 初中 / 高中 / 高考 stages.
- `WordLearningStateRepository` tracks `UNLEARNED`, `LEARNING`, `MASTERED`, and `SKIPPED`.
- `LearningPlanner` picks today's new words and prioritizes due reviews.
- `StudyScreen` already has `今日` and `词库` tabs.
- Review scheduling already exists through `ReviewScheduler`.

So this should be a density and flow pass, not a rewrite.

## Product Shape

### 1. Learning Dashboard

The first screen inside `学习` should show one compact dashboard:

- current vocabulary range
- due review count
- today's new-word count
- learned / total progress for the selected range
- one primary action: continue the next task

This gives the user a reason to open the app even when they do not have a word in mind.

### 2. Vocabulary Progress

The vocabulary pack list should feel like real study material, not settings:

- each pack shows stage name, learned count, total count, and progress percentage
- selected pack is visually clear
- pack cards keep the existing Material3 style
- no large hero sections or decorative redesign

### 3. Daily Task Flow

The `今日` tab should stay simple:

- reviews come first
- otherwise show the next new word from the selected pack
- starting a new word routes to the existing concept-card screen
- skipping stays available but secondary
- done state shows completion and selected-pack progress, not a tutorial

### 4. Status Boundaries

State rules stay conservative:

- `LEARNING` and `MASTERED` block the word from reappearing as new
- `SKIPPED` blocks the word until restored
- mastered words count toward pack progress
- learning words count as started progress

No SRS overhaul, no AI scoring, no exercise page in this pass.

## UI Constraints

- Keep the current Material3 visual language.
- Do not redesign the app's personality.
- Do not add onboarding copy or "how to use this app" panels.
- Improve spacing, grouping, button priority, and information density.
- Keep cards purposeful: dashboard, current task, vocabulary pack, skipped word.

## Data Flow

`StudyViewModel` should expose a richer view state derived from existing sources:

- vocabulary packs from `VocabularyRepository`
- selected pack words
- progress states from `WordLearningStateRepository`
- due cards from `ConceptCardDao`
- task selection from `LearningPlanner`

The UI should render derived counts instead of duplicating learning rules inside composables.

## Testing

Static and unit-level checks should cover:

- pack progress counts started words correctly
- skipped words are excluded from today's queue
- due reviews still outrank new words
- selected pack summary handles empty and completed states

Per project constraint, local Gradle build/test/package should not be run. Verification should use GitHub Actions after implementation.

## Non-Goals

- no new user onboarding
- no community features
- no reading mode
- no large offline dictionary
- no complex spaced-repetition redesign
- no AI correction or scoring for user examples
