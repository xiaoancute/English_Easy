# User Example Design

## Goal

Add a small output loop to each concept card: users can write one personal English example sentence for the word or phrase.

## Scope

- Add a separate `userExample` field next to the existing `userNote` field.
- Store it locally in Room and preserve it when an AI card is refreshed.
- Show an editable “我的例句” text field under the existing “我的理解” field.
- Include the example in share/copy/export text.
- Show a history/favorites badge when a saved card has a user example.

## Non-goals

- No AI correction.
- No scoring.
- No new practice page.
- No grammar checker.
- No backend sync.

## Data flow

`ConceptCardEntity.userExample` is the source of truth. `ConceptRepository` exposes `observeExample()` and `setExample()` like the current note flow. `HomeViewModel` keeps the current card, note, favorite state, and example in one `Success` state. `ConceptCardView` renders the example editor only when an `onExampleChange` callback is provided.
