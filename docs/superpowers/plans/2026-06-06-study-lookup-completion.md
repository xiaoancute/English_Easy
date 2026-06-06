# Study Lookup Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Count a new vocabulary word as learning only after a study-task concept-card lookup succeeds.

**Architecture:** Move learning-state mutation out of `ConceptRepository.lookup()` and into `HomeViewModel.lookup()` behind an explicit `markLearningOnSuccess` flag. Pass that flag from `AppRoot` using a typed `PendingLookup` source so only the new-word study task can trigger progress after successful card open.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt ViewModel, kotlinx.coroutines, kotlin.test, GitHub Actions verification.

---

### Task 1: Add failing lookup-progress tests

**Files:**
- Create: `app/src/test/java/io/github/xiaoancute/englisheasy/ui/home/HomeViewModelLookupLearningTest.kt`

- [ ] Write tests for: study lookup success starts learning, study lookup failure does not start learning, normal lookup success does not start learning.
- [ ] Do not run local Gradle; use GitHub Actions for red/green evidence.
- [ ] Commit the tests with `git commit -m "test: cover study lookup learning completion"`.

### Task 2: Remove implicit learning side effect from repository

**Files:**
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/data/llm/ConceptRepository.kt`

- [ ] Remove `WordLearningStateRepository` from `ConceptRepository` constructor and imports.
- [ ] Delete `wordLearningStateRepository.startLearning(normalized)` from cache-hit and generated-card success paths.
- [ ] Commit with `git commit -m "refactor: keep lookup free of learning side effects"`.

### Task 3: Mark learning only from successful study-intent lookup

**Files:**
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/ui/home/HomeViewModel.kt`

- [ ] Inject `WordLearningStateRepository` into `HomeViewModel`.
- [ ] Change lookup signature to `fun lookup(word: String, forceRefresh: Boolean = false, markLearningOnSuccess: Boolean = false)`.
- [ ] In the success branch only, call `wordLearningStateRepository.startLearning(card.word)` when `markLearningOnSuccess` is true.
- [ ] Leave refresh using the default `markLearningOnSuccess = false`.
- [ ] Commit with `git commit -m "feat: complete study learning after lookup success"`.

### Task 4: Pass typed lookup intent through UI navigation

**Files:**
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/ui/AppRoot.kt`
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/ui/home/HomeScreen.kt`
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/ui/study/StudyScreen.kt`

- [ ] Add private `PendingLookup(word, source)` and `LookupSource.Normal / StudyTask` types in `AppRoot.kt`.
- [ ] Replace `pendingWordFromHistory` with `pendingLookup: PendingLookup?`.
- [ ] Pass `markLearningOnSuccess = pendingLookup?.source == LookupSource.StudyTask` into `HomeScreen`.
- [ ] Use `LookupSource.Normal` for history, favorites, and weak-word clicks.
- [ ] Add `onStudyTaskWordClick` to `StudyScreen` and use it only for `TodayStudyTask.NewWord`.
- [ ] Remove the premature `viewModel.startLearning(word)` call from `StudyScreen`.
- [ ] Add `markLearningOnSuccess: Boolean = false` to `HomeScreen` and pass it only to the automatic initial lookup.
- [ ] Commit with `git commit -m "feat: route study lookup completion intent"`.

### Task 5: Verify through GitHub Actions

**Files:**
- All changed files

- [ ] Run `git diff --check` only.
- [ ] Push `rc-layout-build-check`.
- [ ] Inspect GitHub Actions logs and fix CI failures without local Gradle unless explicitly allowed.
