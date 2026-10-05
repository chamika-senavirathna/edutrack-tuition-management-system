# EduTrack Sri Lanka

A complete school management platform for Sri Lankan institutes, built as a **six-member integrated group project** on Java EE (Jakarta Servlet 6 / Tomcat 10.1), JSP + JSTL, JDBC and MySQL 8+.

---

## 1. Quick start

### Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 21+ | `JAVA_HOME` must point at it |
| Maven | 3.9+ | or use any IDE with Maven support |
| Apache Tomcat | 10.1.x | Jakarta EE 10 (Servlet 6) — Tomcat 9 will **not** work |
| MySQL 8+ | 2019+ / 2022 | with `ONLY_FULL_GROUP_BY` (default) is fine |

### Steps

1. **Create the database and load data:**
   - Open `database/schema.sql` in MySQL Workbench and execute it.
   - Then open `database/seed.sql` and execute it against the `edutrack` database.
   - The schema creates the application tables; the seed loads demo classes, subjects, terms, exams, invoices, payments, receipts, attendance history, fee structures and the accounts below.

2. **Configure the connection** in `src/main/resources/db.properties`. The default URL is `localhost:3306/edutrack`; set `db.username` and `db.password` to your MySQL login.

3. **Build the WAR:**
   ```bash
   mvn clean package
   # -> target/edutrack.war
   ```

4. **Deploy:** copy `target/edutrack.war` into Tomcat's `webapps/` folder and start Tomcat.

5. **Open** `http://localhost:8080/edutrack/` and sign in with a demo account.

> Demo password hashes are generated with PBKDF2 (see `PasswordUtil`); `database/seed.sql` contains pre-computed hashes for the demo password.

---


## MySQL setup (recommended)
1. Install **MySQL 8.0+** and MySQL Workbench.
2. Open `database/schema.sql` and run it.
3. Open `database/seed.sql` and run it.
4. Edit `src/main/resources/db.properties`:
   - `db.username=root`
   - `db.password=YOUR_MYSQL_PASSWORD`
5. Build with `mvn clean package`.
6. Deploy `target/edutrack.war` to Tomcat 10.1+.
7. Open `http://localhost:8080/edutrack/`.

All demo accounts use the password **EduTrack123**.

Newly registered student accounts use **EduTrack123** as the initial password in this demo build. For an existing student `STU-2026-0008`, run `database/repair_student_0008_login.sql` once if the old temporary password is unavailable.

## 2. Demo accounts

Password for **all** demo accounts: `EduTrack123`

| Username | User type | Role(s) | Sees |
|---|---|---|---|
| `admin` | STAFF | ADMIN | Everything, including user management & audit log |
| `principal` | STAFF | PRINCIPAL | Reports, publishing, moderation, audit log |
| `coordinator` | STAFF | ACADEMIC_COORDINATOR | Registration, classes, timetable, exams setup |
| `teacher1` / `teacher2` | STAFF | TEACHER | Attendance marking, marks entry, notices & materials |
| `finance` | STAFF | FINANCE | Invoices, payment verification, refunds |
| `student1` / `student2` | STUDENT | STUDENT | Own timetable, attendance, fees, results, report cards |
| `parent1` | PARENT | PARENT | Own children's records only |

---

## 3. Module ownership (final six-member structure)

Per the final project documentation (LAB02/LAB04 and the Use Case Report), the seven-member allocation in the older Requirement Gathering Report is superseded; communication & teaching-material features belong to **Member 02**.

| # | Member | ID | Module | Key controllers |
|---|---|---|---|---|
| 01 | Hansaka K.L.G.S. | IT25103993 | Student, Teacher & Guardian Registration; enrolment | `StudentServlet`, `TeacherServlet`, `GuardianServlet` |
| 02 | Amarathunge W.R.P.N. | IT25103994 | Class, Timetable & Communication (notices, teaching materials) | `ClassServlet`, `TimetableServlet`, `NoticeServlet` |
| 03 | Samarakoon S.M.D.S. | IT25103995 | Attendance marking, correction window, chronic absence | `AttendanceServlet` |
| 04 | Gunasekara K.J.W | IT25103996 | Fee invoicing, payments, receipts, refunds | `FeeServlet` |
| 05 | Senavirathna K.V.L.C.M | IT25103997 | Users, roles, authentication, profile, audit | `AuthServlet`, `UserServlet` |
| 06 | Dissanayake D.M.A.S.K. | IT25103998 | Examinations, marks, grades, report cards, analytics | `ExamServlet`, `ReportCardServlet` |

---

## 4. Architecture

Classic MVC with strict layering:

```
Browser ──> Filters (AuthFilter → CsrfFilter → RoleFilter)
              └─> Servlets (controllers: parse params, call services, pick view)
                    └─> Services (business rules, authorization, transactions)
                          └─> DAO interfaces + JDBC impls (com.edutrack.dao.impl)
                                └─> MySQL 8+
Views: JSP + JSTL under WEB-INF/jsp (never directly accessible)
```

* **Filters** — `AuthFilter` (session check), `CsrfFilter` (one-time token on every POST), `RoleFilter` (URL-level RBAC with longest-prefix matching).
* **Services** — every business rule below is enforced server-side in `com.edutrack.service`, never only in the JSP.
* **Record-level authorization** — `BaseServlet.resolveSelfOrChildStudentId` / `visibleStudentIds` pin students to their own records and parents to their children; staff pass freely.
* **Audit log** — privileged actions are recorded via `AuditService` (viewable at `/audit` by ADMIN/PRINCIPAL).

---

## 5. Business rules implemented

| Rule | Behaviour | Where |
|---|---|---|
| BR-REG-01 | Unique auto-generated registration numbers | `CodeGenerator`, registration services |
| BR-REG (enrolment) | One active enrolment per student per class; class capacity enforced | `RegistrationService` |
| BR-TTC (clash) | No teacher/room/class double-booking when placing timetable slots | `TimetableService` |
| BR-TTC (transition) | Academic-year/term transitions require elevated roles | `RoleFilter`, `TimetableService` |
| BR-ATT (window) | Attendance corrections only within the editing window; later edits need override | `AttendanceService` |
| BR-ATT (chronic) | Chronic-absence flags for early intervention (PBI-09..12) | `AttendanceService.chronicAbsences` |
| BR-FEE-01 | Receipts are issued **only** after a payment is VERIFIED | `FeeService.verifyPayment` |
| BR-FEE-02 | Duplicate payment transaction IDs are blocked | `FeeService.recordPayment` |
| BR-FEE (void/refund) | Voids/refunds keep full history (never delete) | `FeeService` |
| BR-UAM-01 | Password recovery via one-time 6-digit OTP code | `CodeGenerator`, `UserService` |
| BR-UAM (password) | PBKDF2 hashing — plaintext is never stored | `PasswordUtil` |
| BR-EXM-01 | Marks entry locked outside the allowed exam window/state | `ExaminationService` |
| BR-EXM (workflow) | Marks workflow: enter → submit → moderate → publish | `ExamServlet`, `ExaminationService` |
| BR-EXM-03 | Report cards generated with auto grade bands; only PRINCIPAL/ADMIN publish | `ReportService`, `GradeBandDAO` |
| M04↔M03 (binding) | Marking attendance keeps the student's active-term monthly invoice created/validated against the class fee structure; corrections adjust it | `FeeService.syncAttendanceBilling`, `AttendanceService` |

---

## 5b. Cross-module integration (added in the enhancement pass)

* **Sidebar navigation** — every core module has direct links in the left sidebar with per-role visibility. Students/parents see **My Timetable · M02**, **My Attendance · M03**, **My Fees/My Receipts · M04** and Examinations & Reporting · M06 links; staff see the management variants. All links point to role-guarded, record-scoped routes.
* **Fee–attendance binding (M04 ↔ M03)** — when a teacher marks a student PRESENT/LATE, the attendance transaction also validates the student's billing: the active-term invoice for that class is looked up first (finance-created invoices get `attendance_linked`), and if none exists one is auto-generated at the class's `fee_structures.monthly_amount`. Corrections adjust `present_count` in the same direction. Everything runs inside one DB transaction with audit entries (`INVOICE_AUTO_CREATE`, `INVOICE_ATTENDANCE_PLUS/MINUS`).
* **Monthly administrative reporting (M06)** — `/reports` now aggregates **monthly attendance percentages per class** (entries / present / late / absent / %) and **verified revenue collections by payment method** with a total, filterable by month/year, and exportable to **CSV** (`/reports?format=csv&month=5&year=2026`).
* **Relational integrity** — `fee_structures` (per-class monthly rate, cascades with its class) plus explicit FK rules: class removal cascades to timetable slots and enrolments; optional references (subject/teacher/room on classes & timetable, guardian on students, class on invoices) are `ON DELETE SET NULL`, so no record is ever orphaned or blocked from cleanup.

---

## 6. Security summary

* **Authentication** — PBKDF2 password hashing, brute-force friendly error messages (no user enumeration).
* **RBAC** — two layers: URL guards (`RoleFilter`) and per-action role checks in every service, so authorization is never JSP-only.
* **CSRF** — per-session token required on all POSTs (`CsrfFilter`); requests without it get **403**.
* **Validation** — server-side (`Validator`); JavaScript is progressive enhancement only.
* **Record scoping** — students/parents can only read their own (or their children's) attendance, fees, receipts, results and report cards; cross-record access redirects to the dashboard with a `denied` flag.
* **Views** — all JSPs live under `WEB-INF` and are only reachable through controllers.

---

## 7. Verification performed (end-to-end)

* `mvn package` builds clean (JDK 21).
* `schema.sql` + `seed.sql` are prepared for MySQL 8+ 2019+/2022.
* Deployed on Tomcat 10.1 and verified with real HTTP requests:
  * login (admin/teacher/student/parent) with CSRF token — 302 to dashboard;
  * POST without CSRF token — rejected **403**;
  * all GET routes return 200 for ADMIN;
  * student account: every read path 200; `/users`, `/audit`, `/students/add`, `/fees/add`, `/payments/verify`, `/reportcards/generate`, `/marks/enter`, `/notices/add` → redirect `denied`;
  * record ownership: student1 opens own invoice (200) but is redirected on another student's invoice; parent1 likewise scoped to their child;
  * `/fees` list for student1 contains only that student's invoices.
* Enhancement pass re-verified after redeploy:
  * teacher1 marks class 1 on 2026-09-06 (PRESENT/ABSENT/LATE) → invoices update inside the same transaction: student1 `present_count` 2→3, student3 linked & counted 0→1, student2 (ABSENT) unchanged; no duplicate invoices created;
  * correcting student1's entry to ABSENT decrements `present_count` back to 2, with both the correction and the billing delta in `audit_log`;
  * `/reports` shows the monthly attendance % + revenue cards; CSV export downloads (May 2026: 3 methods, TOTAL 18000.00);
  * student1/parent1: all read paths (timetable, attendance, fees, receipts, results, report cards, notices) 200; `/reports`, `/users`, write paths still denied (302) and POST to staff actions blocked (403 without CSRF);
  * sidebar renders role-appropriate links (My Timetable / My Attendance / My Fees / My Receipts / Report Cards) for student and parent accounts.

---

## 8. Project layout

```
EduTrack/
├── database/            schema.sql, seed.sql
├── src/main/java/com/edutrack/
│   ├── model/           20 entity classes
│   ├── dao/             DAO interfaces (+ impl/ JDBC implementations)
│   ├── service/         business rules & authorization
│   ├── filter/          AuthFilter, CsrfFilter, RoleFilter
│   ├── servlet/         controllers
│   ├── exception/       BusinessRule / Validation / Authorization exceptions
│   └── util/            DB, Validator, Flash, Csrf, CodeGenerator, PasswordUtil
├── src/main/webapp/
│   ├── WEB-INF/jsp/     all views (public/, students/, teachers/, classes/,
│   │                    timetable/, communication/, attendance/, fees/,
│   │                    examinations/, users/, guardians/, dashboard, error/)
│   ├── css/ js/         shared styles & behaviour
│   └── index.jsp        -> redirect to /dashboard
└── pom.xml
```
