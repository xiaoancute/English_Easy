# UI Rescue Design

## Goal

Make English Easy feel like a quiet, coherent learning tool instead of a stitched-together Compose demo.

This pass is for `v0.1.0-rc9`. It should improve trust, readability, and learning-flow continuity without adding large new product features.

## Problem

The current app works, but the frontend does not yet feel release-ready:

- Visual hierarchy is inconsistent between Home, concept cards, Study, Vocabulary, and Settings.
- Too many surfaces are framed as separate cards, which makes the app feel assembled rather than designed.
- The learning loop is logically correct, but the UI does not clearly show the user's next step.
- Action feedback is weak: query success, added-to-learning, skip, restore, review, and settings save do not feel like one coherent system.
- Large files make local styling changes risky. `ConceptCardView.kt` and `StudyScreen.kt` are already too large for repeated ad hoc edits.

## Product Direction

English Easy should feel like:

- A focused study notebook.
- A quiet lookup and review tool.
- A product for repeated daily use.

It should not feel like:

- A SaaS dashboard.
- A marketing landing page.
- A pile of Material cards.
- An AI-generated purple-blue gradient app.

## Visual System

### Color

- Use a light, neutral base: near-white background and white surfaces.
- Keep contrast strong for primary text.
- Use one restrained accent color, preferably the current deep blue family or a deep green if the existing theme supports it cleanly.
- Do not introduce purple-blue gradients, beige/cream dominance, decorative blobs, or saturated decorative backgrounds.
- Error, success, skipped, mastered, and learning states should use semantic color gently: text, icon, or small indicator first; full colored panels only when needed.

### Typography

- Keep the existing serif word/title direction for concept content.
- Use sans-serif for controls, labels, settings, list metadata, and navigation.
- Establish four practical text levels:
  - Word/title: large, serif, used sparingly.
  - Section title: clear and compact.
  - Body: readable, stable line height.
  - Metadata/label: small but not washed out.
- Avoid oversized headings inside compact panels.
- Avoid letter-spacing changes except for small section labels already established by the theme.

### Spacing

- Use tighter, more predictable vertical rhythm.
- Prefer page-level spacing over card-in-card padding.
- Repeated rows should align on consistent gutters.
- Mobile layout must avoid tall stacked cards that make the main task disappear below the fold.

### Containers

- Cards are allowed for true content objects: concept card, current study task, vocabulary pack, settings group.
- Page sections should not all become cards.
- Avoid nested cards.
- Prefer rows, dividers, subtle surface bands, and typography hierarchy for secondary information.
- Default radius should stay modest, around `8.dp`, unless Material component defaults make a smaller change impractical.

### Controls

- Primary action is visually obvious but not oversized.
- Secondary actions should be text buttons or outlined buttons with restrained emphasis.
- Icon buttons must use established Material icons and clear content descriptions where user-facing.
- Loading, error, empty, success, and saved states should reuse consistent structure.

## Screen Design

### Home

Home should be the lookup workspace.

Changes:

- Make the lookup input feel like a compact tool entry, not a large promo card.
- Keep examples in the idle state, but make them quiet and useful.
- On lookup success from a study task, show a clear feedback message that the word has joined learning.
- On lookup failure from a study task, show that learning progress was not changed.
- Error state should offer actionable next steps: retry, check settings, or edit query where applicable.

Non-goals:

- No new onboarding flow.
- No marketing hero.
- No fake metrics.

### Concept Card

Concept cards should read like structured study notes.

Information order:

1. Word, type, favorite/share/copy/refresh actions.
2. Core concept.
3. Chinese approximation.
4. Scenarios.
5. Misconceptions.
6. User note and user example.

Changes:

- Reduce visual fragmentation between sections.
- Use headings, dividers, and spacing instead of card stacking.
- Keep branch cards readable, but do not let nested branch content dominate the parent card.
- Keep note and example editors clearly personal and editable.

Non-goals:

- No schema changes.
- No AI correction.
- No new export behavior beyond preserving existing share/copy behavior.

### Study

Study should make the current next step obvious.

Changes:

- Current task gets highest priority.
- Overview becomes secondary and compact.
- Weak words are useful but should not compete with the current task.
- New-word action should make the transition to Home feel intentional.
- Review grading buttons should be clear, stable, and easy to scan.
- Skip/restore feedback should use the same feedback style as Home.

Non-goals:

- No change to scheduling logic.
- No new spaced-repetition algorithm.
- No gamification layer.

### Vocabulary

Vocabulary should feel like a management list.

Changes:

- Make selected pack state visually clear.
- Make progress compact and legible.
- Use rows or compact blocks instead of heavy cards where possible.
- Keep skipped words recoverable but secondary.

Non-goals:

- No search/filter unless already present.
- No multi-pack analytics.

### Settings

Settings should be a serious BYOK form.

Changes:

- Field labels, helper text, and save feedback should be clear.
- API configuration error states should be actionable.
- Theme controls should not compete with provider setup.
- Keep structure restrained and form-like.

Non-goals:

- No provider marketplace.
- No model picker.
- No remote validation call.

## Feedback System

Use one consistent feedback pattern across the app:

- Success: short confirmation.
- Failure: concise message plus action when available.
- Progress not changed: explicit where relevant, especially study-task lookup failure.
- Destructive or state-changing actions: visible result after action.

Required feedback cases:

- Study-task lookup success: word joined learning.
- Study-task lookup failure: word not added to learning.
- Skip word.
- Restore skipped word.
- Settings saved.
- Copy/share result where existing behavior supports it.

## Architecture Scope

This pass may introduce small shared UI helpers if they reduce repetition:

- `SectionHeader`
- `ActionFeedbackHost` or snackbar wiring
- compact row/list primitives
- reusable state panels for idle/error/empty states

This pass should not create a large new design framework. Keep helpers local to existing UI package patterns.

Large files may be split only where it directly supports the rescue work:

- `ConceptCardView.kt` can split internal section composables into a nearby file if needed.
- `StudyScreen.kt` can split today-task and vocabulary composables if needed.

No unrelated data-layer or scheduling refactors.

## Verification

Required verification:

- GitHub Actions Build passes.
- No local Gradle unless explicitly allowed.
- Browser/emulator screenshot review for at least:
  - Home idle.
  - Home success concept card.
  - Study today task.
  - Vocabulary pack list.
  - Settings provider setup.

Screenshot review must check:

- Text hierarchy.
- Spacing consistency.
- Card/container discipline.
- Mobile readability.
- State feedback visibility.
- Absence of obvious AI UI tropes.

## Release Notes Impact

`v0.1.0-rc9` notes should say this is a UI polish and learning-flow clarity release.

The `rc8` claim that every successful lookup adds a word to learning is no longer accurate and must not be repeated in final `v0.1.0` notes.
