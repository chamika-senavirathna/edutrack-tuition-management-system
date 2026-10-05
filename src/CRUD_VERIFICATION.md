# EduTrack role and CRUD verification

Verified on 2026-10-05. Changes apply to the primary project at `EduTrack/src/main`; the nested historical project under `src/EduTrack-Updated-UI-Full-Project` was not modified.

## Result

- All main Java sources compile with Java 21.
- 70 integration/regression assertions pass against a fresh H2 in-memory database in MySQL compatibility mode, loaded from the project's schema and fictional seed data.
- All 55 JSP pages compile with the installed Tomcat 10.1 Jasper compiler: zero errors. Its bundled Eclipse compiler selected Java 19 for generated JSP code; main application compilation used Java 21.
- No production database data or database connection settings were changed.
- The existing layout, stylesheet, colours and page structure are preserved. Functional Submit, Edit, Archive, and subject activation controls reuse existing button styles and forms.

This does **not** establish 100% correctness in every environment. A live MySQL/Tomcat browser acceptance run was not performed. The automated controller checks use servlet request/response proxies; JSP compilation checks syntax, not visual browser rendering. H2 compatibility mode is not identical to MySQL, and concurrency/load behavior has not been exhaustively tested.

## Corrected behavior

| Area | Fixes |
| --- | --- |
| Student/parent reads | Own student/linked-child lists and profiles; own published timetable and results; scoped examination details; published report cards only; any linked child's report card can be opened; missing profile links fail closed. |
| Registration | Preserve status on student/teacher edits; retain selected existing guardian on registration; transactional enrolment; withdrawal closes active enrolments while retaining history. |
| Classes/timetable | Preserve lifecycle status on edits; reject invalid capacity and reductions below enrolment count; fix false teacher/room/class clash matches; retain approval/publication workflow. |
| Subjects/rooms | Wire existing forms for editing; add subject activation/deactivation using the existing save action. |
| Attendance | Enforce class assignment when teachers mark attendance; protect marking/correction forms; correct repeated-row form binding; make corrections, billing, audit and notifications atomic. |
| Examinations | Restrict entry sheet to enrolled students; add missing Submit for Moderation action; compare teacher IDs by value; preserve exam status; validate edits; lock class/subject/max marks once marks exist; calculate grades as a percentage. |
| Notices | Check audience on direct student/parent views; include parent class audiences; resolve role IDs correctly; preserve publication timestamps; prevent teachers changing other authors' notices. |
| Materials | Add update path and Edit/Archive controls; validate links; require teacher class assignment; prevent editing/archiving another uploader's materials; persist updated class/subject/link. |
| Finance | Fix receipt insert column order; preserve invoice status; validate discounts and amounts against verified payments; recheck balance at verification; prevent voiding part-paid invoices; retain attendance billing fields; serialize verify/refund on payment and invoice rows; restore PENDING after a full refund. |
| Accounts | Validate role/profile combinations; transactional account-and-role writes; prevent removal of one's own administrator role; refresh authenticated sessions so deactivation/role changes take effect; fix password-error redirect. |

Archive, withdrawal, deactivation, void and refund remain the appropriate deletion equivalents for records whose history must be retained. Student/parent roles are readers of their own records, not unrestricted CRUD operators.

## Repeat the checks

Use Java 21 and run `src/test/run-regression.ps1` from PowerShell. It uses the Servlet 6.0 and H2 2.2.224 JARs in the local Maven cache by default; optional `-ServletJar` and `-H2Jar` arguments accept alternative local paths. It compiles into a new temporary directory and refuses to run the integration suite unless the database is an H2 in-memory database. The main application's `db.properties` is never used by this test runner.

The regression suite covers all seven role logins, URL permissions, own-record isolation, missing profile links, update persistence, assigned/unassigned teachers, marks submission/moderation/publication, report cards, attendance, duplicate rejection, materials, notice audiences/ownership, finance verification/refund/void, registration/login/withdrawal, account revocation, subject/room edits, timetable conflicts/workflow, CSRF rejection, multiple children, and failed-account-update rollback.

## Run the corrected application

Rebuild the primary project with Maven (`mvn package`) or IntelliJ, then restart/redeploy it in Tomcat. An already-running or previously compiled deployment will not pick up these source changes automatically. Retain the current database; **do not rerun `database/schema.sql` on existing data**, because that original setup script drops the database.

The delivery ZIP contains the corrected primary source project, its original Maven configuration/database scripts, this report, and the repeatable regression suite. It excludes historical nested project copies and temporary verification output.
