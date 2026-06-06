# Study Lookup Completion Design

## Goal

Close the new-word learning loop so a vocabulary item enters learning progress only after its concept card is successfully opened from a study task.

## Current Problem

The app currently starts learning too early and too broadly:

- `StudyScreen` calls `StudyViewModel.startLearning(word)` before opening the concept card.
- `ConceptRepository.lookup()` calls `WordLearningStateRepository.startLearning()` for every successful lookup, including manual search, history, favorites, cache hits, and refreshes.

This makes learning progress unreliable. A user can tap “学习这个词”, fail card generation, and still see the word counted as learning. Manual lookups can also affect study progress even when they were not selected from the study task.

## Product Rule

A word is counted as learning only when all of these are true:

1. The lookup was launched from the new-word study task.
2. The Home lookup completed successfully and produced/opened a `ConceptCard`.
3. The learning repository accepts the state change. Existing safeguards remain: skipped and mastered words are not overwritten.

Lookup failure must not change learning progress. Manual lookup, history lookup, favorites lookup, weak-word lookup, and refresh must not start learning.

## Approach

Use an explicit lookup intent at the app/navigation boundary.

`AppRoot` will hold a `PendingLookup` with:

- `word`: the word or phrase to open.
- `source`: `Normal` or `StudyTask`.

History, favorites, and weak-word clicks create `Normal` intents. The new-word study task creates a `StudyTask` intent.

`HomeScreen` will accept the pending lookup intent and pass `markLearningOnSuccess = true` to `HomeViewModel.lookup()` only for `StudyTask`. Manual queries and refreshes keep the default `false` value.

`HomeViewModel` will become the coordinator for this UI-specific behavior. It will inject `WordLearningStateRepository` and call `startLearning(card.word)` only in the successful lookup branch when `markLearningOnSuccess` is true.

`ConceptRepository` will return cards and preserve card metadata, but it will no longer mutate learning state during lookup. This keeps data fetching independent from study-progress intent.

## Data Flow

1. Study tab shows `TodayStudyTask.NewWord(word)`.
2. User taps “学习这个词”.
3. `StudyScreen` calls `onStudyTaskWordClick(word)` without calling `startLearning()`.
4. `AppRoot` stores `PendingLookup(word, StudyTask)` and switches to Home.
5. `HomeScreen` consumes the pending lookup and calls `HomeViewModel.lookup(word, markLearningOnSuccess = true)`.
6. `HomeViewModel` calls `ConceptRepository.lookup(word)`.
7. On success, Home displays the card and calls `WordLearningStateRepository.startLearning(card.word)`.
8. On failure, Home displays the error and does not touch learning state.

## Error Handling

- Blank lookups remain ignored.
- Repository lookup errors continue to map to `HomeUiState.Error`.
- A failed study-task lookup does not mark the word learning.
- `WordLearningStateRepository.startLearning()` continues to protect skipped and mastered words.

## Testing

Add unit tests around the lookup coordinator behavior:

- Study-intent lookup success starts learning.
- Study-intent lookup failure does not start learning.
- Normal lookup success does not start learning.
- Repository lookup success no longer starts learning implicitly.

Because local Gradle is intentionally not run in this environment, verification will be performed by committing/pushing and reading GitHub Actions results.

## Non-goals

- No review scheduling changes.
- No new UI screens.
- No change to skip, restore, mastered, or weak-word policy.
- No backend sync.
