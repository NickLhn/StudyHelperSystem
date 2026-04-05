# Product Build Plan

## Goal
Complete the remaining high-value product slices on top of the rebuilt UI so the system has stronger teaching workflows, analytics, and notifications.

## Phases
- [completed] Rebuild the frontend UI shells and major pages
- [completed] Add homework auto-grading workflow for teacher and student
- [completed] Extend teacher grade center with homework analytics
- [completed] Add dynamic notifications to message center, shell badges, and dashboard entry points
- [completed] Add richer material-center metadata and search refinement
- [completed] Add quiz anti-cheat helpers
- [completed] Add course archive support

## Decisions
- Keep notifications lightweight in this phase by deriving them from existing homework, quiz, material, and submission data instead of introducing a new message table.
- Surface reminders in three places at once: message center page, topbar badge, and dashboard/home entry context.
- Favor dynamic aggregation now; add read-state persistence only if the user later asks for message history or acknowledgement behavior.
- Material enhancement should use lightweight metadata columns on `materials` (`category`, `tags`, `version_label`, `version_note`) instead of a separate lookup table in the first phase.
- Quiz anti-cheat should stay lightweight in phase one: enforce attempt limits and question-order randomization in the backend, while using browser draft persistence for auto-save and countdown continuity.
- Course lifecycle should stay lightweight in phase one: add `ACTIVE` / `ARCHIVED` status, semester label, and archive timestamp on `courses` instead of introducing separate term/archive tables.

## Errors Encountered
- Quiz accuracy is stored as a 0-1 ratio, so grade center responses must convert it to percent for UI display.
- Extending `CourseService` for richer analytics introduced missing `Objects` / `Optional` imports; backend compile passed again after补齐 import。
- Running backend integration tests under local JDK 23 initially failed because Mockito's default inline mock maker could not self-attach; resolved in test scope by adding `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` with `mock-maker-subclass`.
- Material detail fetching exposed a real `LazyInitializationException` for `Material.user` / `Material.course`; resolved by wrapping read methods in `@Transactional(readOnly = true)`.
- Student quiz detail responses were exposing `Question.answer`; resolved by making `QuestionDTO` answer exposure conditional and suppressing answers for non-teacher access.
- Course archive filtering exposed another `LazyInitializationException` path through enrolled-course reads; resolved by wrapping course read methods in `@Transactional(readOnly = true)`.

## Follow-up
- If dynamic reminders prove useful, the next step is a persisted notification table with read/unread state and optional scheduled pushes.
- If material metadata grows beyond simple filters and labels, the next step would be dedicated `material_tags` or version-history tables. The current phase keeps it intentionally small and deployable.
- If you later want stricter anti-cheat (server-enforced countdown across devices, forced tab-switch logging, or resumable exam sessions), the next step would be a persisted quiz session / in-progress attempt table rather than local draft storage alone.
- If course lifecycle grows beyond simple active/archive filtering, the next step would be a dedicated term model plus archive policies for downstream homework, quiz, and notification visibility.
