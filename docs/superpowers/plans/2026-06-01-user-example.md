# User Example Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a local “我的例句” field to concept cards as the smallest output-practice loop.

**Architecture:** Mirror the existing `userNote` path end-to-end. Add `userExample` to Room, DAO, repository, view model state, card UI, export text, and history badges. Keep behavior local-only and preserve it across refresh.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, kotlinx.serialization unit tests.

---

### Task 1: Export Contract

**Files:**
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/data/model/ConceptCardExport.kt`
- Test: `app/src/test/java/io/github/xiaoancute/englisheasy/data/model/ConceptCardSerializationTest.kt`

- [ ] Add a failing test that `toShareText(userNote, userExample)` includes a “我的例句” section.
- [ ] Update `toShareText` to accept `userExample` and append it after “我的理解”.

### Task 2: Local Persistence

**Files:**
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/data/local/AppDatabase.kt`
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/di/DatabaseModule.kt`
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/data/local/ConceptCardEntity.kt`
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/data/local/ConceptCardDao.kt`

- [ ] Add `userExample TEXT NOT NULL DEFAULT ''` migration from version 5 to 6.
- [ ] Add `userExample` to `ConceptCardEntity` and `fromCard()`.
- [ ] Add DAO methods `observeExample()` and `setExample()`.

### Task 3: Repository and Home State

**Files:**
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/data/llm/ConceptRepository.kt`
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/ui/home/HomeViewModel.kt`

- [ ] Preserve cached `userExample` during refresh.
- [ ] Expose `observeExample()` and `setExample()`.
- [ ] Add `userExample` to `HomeUiState.Success` and keep it synchronized.

### Task 4: UI, Share, History

**Files:**
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/ui/components/ConceptCardView.kt`
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/ui/home/HomeScreen.kt`
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/io/github/xiaoancute/englisheasy/ui/history/HistoryViewModel.kt`

- [ ] Add “我的例句” editor below “我的理解”.
- [ ] Pass `userExample` through share/copy.
- [ ] Add a history/favorites badge for cards with examples.

### Task 5: Verification

**Files:**
- All changed files.

- [ ] Run `git diff --check`.
- [ ] Do not run local Gradle.
- [ ] Push branch and verify with GitHub Actions Build.
