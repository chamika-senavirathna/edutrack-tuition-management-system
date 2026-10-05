-- ============================================================================
-- EduTrack Sri Lanka - MySQL 8+ Schema
-- Complete six-member application database.
-- Target: MySQL 8.0+ / MySQL 8.4 LTS
-- ============================================================================
DROP DATABASE IF EXISTS edutrack;
CREATE DATABASE edutrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edutrack;

-- ---------------------------------------------------------------- identity --
CREATE TABLE roles (id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(40)  NOT NULL UNIQUE,          -- ADMIN, PRINCIPAL, ACADEMIC_COORDINATOR, TEACHER, FINANCE, STUDENT, PARENT
    description VARCHAR(255));

CREATE TABLE guardians (id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(120) NOT NULL,
    nic          VARCHAR(15)  UNIQUE,
    phone        VARCHAR(15),
    email        VARCHAR(120),
    occupation   VARCHAR(80),
    address      VARCHAR(255),
    relationship VARCHAR(20),
    created_at   DATE NOT NULL DEFAULT (CURRENT_DATE));

CREATE TABLE teachers (id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_no       VARCHAR(20)  NOT NULL UNIQUE,        -- BR-REG-01
    full_name      VARCHAR(120) NOT NULL,
    nic            VARCHAR(15)  UNIQUE,                 -- BR-REG-02
    gender         VARCHAR(10)  NOT NULL,
    email          VARCHAR(120),
    phone          VARCHAR(15),
    address        VARCHAR(255),
    qualification  VARCHAR(255),
    specialization VARCHAR(120),
    joined_date    DATE,
    status         VARCHAR(12)  NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | RESIGNED | ON_LEAVE
    resigned_at    DATE,
    resigned_reason VARCHAR(255),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP);

CREATE TABLE students (id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    reg_no             VARCHAR(20)  NOT NULL UNIQUE,     -- BR-REG-01: unique auto-generated
    first_name         VARCHAR(60)  NOT NULL,
    last_name          VARCHAR(60)  NOT NULL,
    name_with_initials VARCHAR(80),
    nic                VARCHAR(15)  UNIQUE,              -- BR-REG-02: duplicate NIC blocked
    gender             VARCHAR(10)  NOT NULL,
    dob                DATE,
    address            VARCHAR(255),
    phone              VARCHAR(15),
    email              VARCHAR(120),
    guardian_id        BIGINT NULL,
    medical_notes      VARCHAR(500),
    admission_date     DATE,
    status             VARCHAR(12)  NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | WITHDRAWN | SUSPENDED
    withdrawn_at       DATE,
    withdrawn_reason   VARCHAR(255),
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ,
    -- Relational integrity: a guardian can be removed without orphaning the student record.
    CONSTRAINT fk_students_guardian FOREIGN KEY (guardian_id) REFERENCES guardians (id) ON DELETE SET NULL);
CREATE INDEX idx_students_status ON students (status);
CREATE INDEX idx_students_guardian ON students (guardian_id);

CREATE TABLE users (id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(40)  NOT NULL UNIQUE,
    email         VARCHAR(120) UNIQUE,                 -- unique login identity (UC-UAM-01)
    password_hash VARCHAR(255) NOT NULL,               -- PBKDF2 hash, never plaintext
    full_name     VARCHAR(120) NOT NULL,
    phone         VARCHAR(15),
    active        BIT      NOT NULL DEFAULT 1,  -- BR-UAM-03: never hard-deleted
    user_type     VARCHAR(10)  NOT NULL,               -- STUDENT | STAFF | PARENT
    student_id    BIGINT NULL,
    teacher_id    BIGINT NULL,
    parent_id     BIGINT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at DATETIME NULL,
    CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE SET NULL,
    CONSTRAINT fk_users_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE SET NULL,
    CONSTRAINT fk_users_parent  FOREIGN KEY (parent_id)  REFERENCES guardians (id) ON DELETE SET NULL);

CREATE TABLE user_roles (user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE);

CREATE TABLE password_reset_tokens (id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id   BIGINT   NOT NULL,
    otp_code  VARCHAR(255) NOT NULL,        -- stored hashed
    expires_at DATETIME NOT NULL,
    used      BIT  NOT NULL DEFAULT 0,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE);

-- ------------------------------------------------------------- curriculum --
CREATE TABLE subjects (id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(255),
    active      BIT NOT NULL DEFAULT 1);

CREATE TABLE rooms (id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(40) NOT NULL UNIQUE,
    room_type VARCHAR(12) NOT NULL,          -- CLASSROOM | HALL | LAB
    capacity  INT NOT NULL);

CREATE TABLE classes (id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80) NOT NULL,
    subject_id  BIGINT NULL,
    teacher_id  BIGINT NULL,
    room_id     BIGINT NULL,
    capacity    INT NOT NULL,
    start_date  DATE,
    end_date    DATE,
    status      VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',  -- DRAFT | ACTIVE | ARCHIVED
    description VARCHAR(255),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Optional references: subject/teacher/room may be detached without orphaning the class.
    CONSTRAINT fk_classes_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE SET NULL,
    CONSTRAINT fk_classes_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE SET NULL,
    CONSTRAINT fk_classes_room    FOREIGN KEY (room_id)    REFERENCES rooms (id)    ON DELETE SET NULL);
CREATE INDEX idx_classes_status ON classes (status);

CREATE TABLE enrolments (id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id    BIGINT NOT NULL,
    class_id      BIGINT NOT NULL,
    enrolled_date DATE NOT NULL,
    status        VARCHAR(12) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | COMPLETED | WITHDRAWN
    remarks       VARCHAR(255),
    -- One student = one active enrolment per class (business rule)
    CONSTRAINT uq_enrol_active UNIQUE (student_id, class_id, status),
    CONSTRAINT fk_enrol_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrol_class   FOREIGN KEY (class_id)   REFERENCES classes (id)  ON DELETE CASCADE);
CREATE INDEX idx_enrol_class ON enrolments (class_id, status);

CREATE TABLE timetable (id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id     BIGINT NOT NULL,
    subject_id   BIGINT NULL,
    teacher_id   BIGINT NULL,
    room_id      BIGINT NULL,
    day_of_week  VARCHAR(10) NOT NULL,      -- MONDAY..SUNDAY
    start_time   VARCHAR(5)  NOT NULL,      -- HH:MM
    end_time     VARCHAR(5)  NOT NULL,
    status       VARCHAR(10) NOT NULL DEFAULT 'DRAFT',  -- DRAFT | SUBMITTED | APPROVED | PUBLISHED | ARCHIVED
    published_at DATETIME NULL,
    -- A class removal cascades to its timetable slots; optional refs detach (no orphans).
    CONSTRAINT fk_tt_class   FOREIGN KEY (class_id)   REFERENCES classes (id)  ON DELETE CASCADE,
    CONSTRAINT fk_tt_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE SET NULL,
    CONSTRAINT fk_tt_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE SET NULL,
    CONSTRAINT fk_tt_room    FOREIGN KEY (room_id)    REFERENCES rooms (id)    ON DELETE SET NULL);
CREATE INDEX idx_tt_day_time ON timetable (day_of_week, start_time, end_time);
CREATE INDEX idx_tt_teacher ON timetable (teacher_id);
CREATE INDEX idx_tt_room ON timetable (room_id);
CREATE INDEX idx_tt_class ON timetable (class_id, status);

-- ------------------------------------------------------------- attendance --
CREATE TABLE attendance (id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id        BIGINT NOT NULL,
    class_id          BIGINT NOT NULL,
    timetable_slot_id BIGINT NULL,
    attendance_date   DATE   NOT NULL,
    status            VARCHAR(10) NOT NULL,   -- PRESENT | ABSENT | LATE | EXCUSED
    reason            VARCHAR(255),
    marked_by         BIGINT NOT NULL,
    correction_note   VARCHAR(255),
    corrected_at      DATETIME,
    corrected_by      BIGINT NULL,
    admin_override    BIT NOT NULL DEFAULT 0,   -- BR-ATT-03
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ,
    -- BR-ATT-01: exactly one entry per student per class per day
    CONSTRAINT uq_attendance_session UNIQUE (student_id, class_id, attendance_date),
    CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_att_class   FOREIGN KEY (class_id)   REFERENCES classes (id)  ON DELETE CASCADE);
CREATE INDEX idx_att_class_date ON attendance (class_id, attendance_date);
CREATE INDEX idx_att_status ON attendance (status);

-- ------------------------------------------------------------ fees (M04) --
CREATE TABLE terms (id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(40) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    active     BIT NOT NULL DEFAULT 0);

CREATE TABLE fee_invoices (id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_no          VARCHAR(24) NOT NULL UNIQUE,
    student_id          BIGINT NOT NULL,
    class_id            BIGINT NULL,
    term_id             BIGINT NULL,
    amount              DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    amount_paid         DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount            DECIMAL(10,2) NOT NULL DEFAULT 0,
    due_date            DATE,
    issued_date         DATE,
    status              VARCHAR(10) NOT NULL DEFAULT 'PENDING',  -- PENDING | PART_PAID | PAID | VOID
    remarks             VARCHAR(255),
    -- M04 <-> M03 linkage: attendance marking keeps billing validated/current.
    attendance_linked   BIT NOT NULL DEFAULT 0,   -- invoice auto-created/validated from attendance
    present_count       INT      NOT NULL DEFAULT 0,      -- attendance sessions counted toward this invoice
    last_attendance_date DATE NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_student FOREIGN KEY (student_id) REFERENCES students (id),
    -- Class can be detached (e.g. archived) without losing the financial record.
    CONSTRAINT fk_inv_class   FOREIGN KEY (class_id)   REFERENCES classes (id) ON DELETE SET NULL,
    CONSTRAINT fk_inv_term    FOREIGN KEY (term_id)    REFERENCES terms (id));
CREATE INDEX idx_inv_student ON fee_invoices (student_id);
CREATE INDEX idx_inv_status ON fee_invoices (status);

-- Monthly billing template used by the M04 <-> M03 linkage: when attendance is
-- marked for a class, the student's active-term invoice is created/validated
-- against this monthly rate, so fee status tracks real attendance.
CREATE TABLE fee_structures (id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id      BIGINT NOT NULL UNIQUE,
    monthly_amount DECIMAL(10,2) NOT NULL CHECK (monthly_amount > 0),
    active        BIT NOT NULL DEFAULT 1,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fs_class FOREIGN KEY (class_id) REFERENCES classes (id) ON DELETE CASCADE);

CREATE TABLE payments (id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id     BIGINT NOT NULL,
    transaction_id VARCHAR(60) NOT NULL,
    amount         DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    method         VARCHAR(15) NOT NULL,      -- CASH | BANK_TRANSFER | CARD | ONLINE
    status         VARCHAR(10) NOT NULL,      -- PENDING | VERIFYING | VERIFIED | FAILED | REFUNDED
    payment_date   DATE NOT NULL,
    verified_at    DATETIME,
    verified_by    BIGINT NULL,
    failure_reason VARCHAR(255),
    refund_reason  VARCHAR(255),
    notes          VARCHAR(255),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pay_invoice FOREIGN KEY (invoice_id)  REFERENCES fee_invoices (id),
    CONSTRAINT fk_pay_verifier FOREIGN KEY (verified_by) REFERENCES users (id));
CREATE INDEX idx_pay_txn ON payments (transaction_id);
CREATE INDEX idx_pay_status ON payments (status);

CREATE TABLE receipts (id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_no VARCHAR(30) NOT NULL UNIQUE,
    payment_id BIGINT NOT NULL UNIQUE,      -- one receipt per verified payment
    amount     DECIMAL(10,2) NOT NULL,
    issued_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    issued_by  BIGINT NOT NULL,
    CONSTRAINT fk_rcp_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_rcp_issuer  FOREIGN KEY (issued_by)  REFERENCES users (id));

-- ------------------------------------------------------- examinations (M06) --
CREATE TABLE grade_bands (id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade       VARCHAR(4) NOT NULL,
    min_mark    INT NOT NULL,
    max_mark    INT NOT NULL,
    description VARCHAR(80),
    CHECK (min_mark <= max_mark));

CREATE TABLE examinations (id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_name  VARCHAR(120) NOT NULL,
    term_id    BIGINT NULL,
    class_id   BIGINT NULL,
    subject_id BIGINT NULL,
    teacher_id BIGINT NULL,               -- BR-EXM-01: marks restricted to this teacher
    exam_date  DATE NOT NULL,
    start_time VARCHAR(5),
    end_time   VARCHAR(5),
    room_text  VARCHAR(40),
    max_marks  INT NOT NULL DEFAULT 100,
    status     VARCHAR(10) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED | COMPLETED | CANCELLED | ARCHIVED
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exam_term    FOREIGN KEY (term_id)    REFERENCES terms (id),
    CONSTRAINT fk_exam_class   FOREIGN KEY (class_id)   REFERENCES classes (id),
    CONSTRAINT fk_exam_subject FOREIGN KEY (subject_id) REFERENCES subjects (id),
    CONSTRAINT fk_exam_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id));
CREATE INDEX idx_exam_class ON examinations (class_id);
CREATE INDEX idx_exam_date ON examinations (exam_date);

CREATE TABLE marks (id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id         BIGINT NOT NULL,
    student_id      BIGINT NOT NULL,
    marks           DECIMAL(5,2),
    max_marks       DECIMAL(5,2) NOT NULL,
    grade           VARCHAR(4),          -- auto-calculated from grade_bands
    remarks         VARCHAR(255),
    status          VARCHAR(10) NOT NULL DEFAULT 'DRAFT',  -- DRAFT | SUBMITTED | MODERATED | PUBLISHED
    entered_by      BIGINT NOT NULL,
    entered_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    moderated_by    BIGINT NULL,
    moderated_at    DATETIME,
    moderation_note VARCHAR(255),
    published_at    DATETIME,
    CONSTRAINT uq_marks_exam_student UNIQUE (exam_id, student_id),
    CONSTRAINT fk_marks_exam    FOREIGN KEY (exam_id)    REFERENCES examinations (id) ON DELETE CASCADE,
    CONSTRAINT fk_marks_student FOREIGN KEY (student_id) REFERENCES students (id)    ON DELETE CASCADE,
    CONSTRAINT fk_marks_enterer FOREIGN KEY (entered_by) REFERENCES users (id));
CREATE INDEX idx_marks_status ON marks (status);

CREATE TABLE report_cards (id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id        BIGINT NOT NULL,
    term_id           BIGINT NOT NULL,
    average_marks     DECIMAL(5,2),
    overall_grade     VARCHAR(4),
    attendance_percent INT NULL,
    status            VARCHAR(10) NOT NULL DEFAULT 'GENERATED',  -- GENERATED | PUBLISHED
    generated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at      DATETIME,
    CONSTRAINT uq_reportcard UNIQUE (student_id, term_id),
    CONSTRAINT fk_rc_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_rc_term    FOREIGN KEY (term_id)    REFERENCES terms (id));

-- ------------------------------------------------ communication (M02) --
CREATE TABLE notices (id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(150) NOT NULL,
    content         TEXT NOT NULL,
    category        VARCHAR(12) NOT NULL,   -- GENERAL | ACADEMIC | FINANCIAL | EMERGENCY | EVENT
    audience_type   VARCHAR(12) NOT NULL,   -- ALL | CLASS | SUBJECT | ROLE | INDIVIDUAL
    audience_ref_id VARCHAR(40) NULL,       -- class/subject id or role name
    channel         VARCHAR(10) NOT NULL DEFAULT 'IN_SYSTEM',
    priority        VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    created_by      BIGINT NOT NULL,
    status          VARCHAR(10) NOT NULL DEFAULT 'DRAFT',  -- DRAFT | PUBLISHED | ARCHIVED
    published_at    DATETIME,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notice_user FOREIGN KEY (created_by) REFERENCES users (id));
CREATE INDEX idx_notice_status ON notices (status);
CREATE INDEX idx_notice_audience ON notices (audience_type, audience_ref_id);

CREATE TABLE teaching_materials (id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id    BIGINT NULL,
    subject_id  BIGINT NULL,
    uploaded_by BIGINT NOT NULL,
    title       VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    file_path   VARCHAR(255),
    file_name   VARCHAR(150),
    status      VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | ARCHIVED
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mat_class   FOREIGN KEY (class_id)    REFERENCES classes (id),
    CONSTRAINT fk_mat_subject FOREIGN KEY (subject_id)  REFERENCES subjects (id),
    CONSTRAINT fk_mat_user    FOREIGN KEY (uploaded_by) REFERENCES users (id));
CREATE INDEX idx_mat_class ON teaching_materials (class_id, status);

-- --------------------------------------------------- cross-module support --
CREATE TABLE notifications (id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NULL,
    role_name       VARCHAR(40) NULL,
    student_id      BIGINT NULL,
    title           VARCHAR(150) NOT NULL,
    message         TEXT NOT NULL,
    channel         VARCHAR(10) NOT NULL DEFAULT 'IN_SYSTEM',
    delivery_status VARCHAR(10) NOT NULL DEFAULT 'SENT',   -- SENT | DELIVERED | FAILED
    is_read         BIT NOT NULL DEFAULT 0,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user    FOREIGN KEY (user_id)    REFERENCES users (id)    ON DELETE CASCADE,
    CONSTRAINT fk_notif_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE);
CREATE INDEX idx_notif_user ON notifications (user_id, is_read);

CREATE TABLE audit_log (id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,             -- 0 = system
    action      VARCHAR(40) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id   BIGINT NULL,
    old_value   VARCHAR(255),
    new_value   VARCHAR(255),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE INDEX idx_audit_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_user ON audit_log (user_id);
CREATE INDEX idx_audit_time ON audit_log (created_at);

