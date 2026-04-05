# Progress Log

## 2026-04-04

- Audited router, layout shells, shared design system, and major teacher/student/admin views.
- Identified the first implementation slice: global tokens, unified shell layout, and high-traffic dashboard/list pages.

## 2026-04-05

- Rebuilt `design-system.css` and `style.css` into a unified visual foundation.
- Refactored teacher, student, and admin layout shells into one consistent product language.
- Reworked core teacher pages, student pages, admin pages, and major form flows.
- Refreshed the public landing flow by rebuilding the navbar, homepage, and login page.
- Verified the frontend with `npm run build` successfully after each major refactor step.
- Reconnected teacher student management, teacher grade center, and student wrong-book into the new routed workspace.
- Documented the next-phase feature roadmap for homework auto-grading, grade analytics, material enhancement, anti-cheat, notifications, and course archive.
- Implemented homework domain models, APIs, and frontends for teacher publishing, student submission, auto-grading, and teacher review.
- Extended course analytics so teacher grade center now includes homework summaries, pending review counts, and low-accuracy question insights.
- Started the notification slice by auditing message-center entry points and choosing a dynamic, no-new-table notification aggregation strategy.
- Implemented a dynamic notification backend plus a rebuilt message center UI, topbar count badges, and teacher dashboard reminder cards.
- Verified notification-related frontend changes with `npm run build` and backend changes with `mvn -q -DskipTests compile`.
- Added `NotificationIntegrationTest` and verified `/api/notification/list` through H2 + MockMvc for both teacher and student scenarios.
- Added a test-only Mockito configuration override because the local JDK 23 environment cannot attach the default inline mock maker.
- Enhanced the material center with category, tags, version label, version notes, richer teacher/student browsing, and a rebuilt detail page.
- Added `materials_metadata_upgrade.sql` to document the required schema change for material metadata fields.
- Added `MaterialMetadataIntegrationTest` and fixed a real lazy-loading bug in `MaterialService` by moving read paths into `@Transactional(readOnly = true)`.
- Re-verified the material slice with `mvn -q -DskipTests compile`, `npm run build`, and `mvn -q -Dtest=MaterialMetadataIntegrationTest test`.
- Added a first-wave quiz anti-cheat slice: max attempt limits, deterministic shuffled question order, persisted countdown, local draft auto-save, and real quiz result retrieval.
- Patched a serious quiz detail leak by hiding correct answers from student-side `getQuizDetail` responses.
- Added `quiz_anti_cheat_upgrade.sql` plus `QuizAntiCheatIntegrationTest`, and re-verified the quiz slice with backend compile, frontend build, and focused MockMvc tests.
- Added course archive support with lightweight lifecycle fields on `courses`: `status`, `semester_label`, and `archived_at`.
- Extended teacher and student course browsing so both sides can filter active vs archived courses, while teachers can archive or restore courses from list/detail views.
- Blocked new student joins for archived courses and added `course_archive_upgrade.sql` to document the required schema change.
- Added `CourseArchiveIntegrationTest`, which verified archiving, teacher/student status filtering, and join rejection for archived courses.
- Fixed another real lazy-loading issue exposed by the archive test by moving course read paths in `CourseService` under `@Transactional(readOnly = true)`.
- Re-verified the archive slice with `mvn -q -DskipTests compile`, `npm run build`, and `mvn -q -Dtest=CourseArchiveIntegrationTest test`.
