-- ============================================================================
-- EduTrack Sri Lanka - Demo Seed Data (fully fictional, safe for demos)
-- Demo password for every account: EduTrack123
-- ============================================================================
CREATE DATABASE IF NOT EXISTS edutrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edutrack;

-- ------------------------------------------------------------------ roles --

INSERT INTO roles (id, name, description) VALUES
(1, 'ADMIN',                'Full configuration, users, roles, audit logs'),
(2, 'PRINCIPAL',            'Approves timetables, moderates and publishes results'),
(3, 'ACADEMIC_COORDINATOR', 'Classes, timetable drafting, enrolment, subjects, rooms'),
(4, 'TEACHER',              'Attendance marking, assigned marks entry, materials'),
(5, 'FINANCE',              'Invoices, payments, receipts, refunds'),
(6, 'STUDENT',              'Own records only'),
(7, 'PARENT',               'Linked children records only');

-- --------------------------------------------------------- profiles (M01) --

INSERT INTO guardians (id, full_name, nic, phone, email, occupation, address, relationship) VALUES
(1, 'Kumari Jayawardena',   '825641237V', '0771111111', 'kumari.j@example.com',  'Bank clerk',   '12 Temple Rd, Nugegoda',  'Mother'),
(2, 'Sunil Perera',         '791234567V', '0772222222', 'sunil.p@example.com',   'Driver',       '45 Lake Ave, Kandy',      'Father'),
(3, 'Nadeeka Silva',        '839876543V', '0773333333', 'nadeeka.s@example.com', 'Nurse',        '8 Hill St, Galle',        'Mother'),
(4, 'Ruwan Dissanayake',    '774567890V', '0774444444', 'ruwan.d@example.com',   'Engineer',     '23 Church Rd, Negombo',   'Father');

INSERT INTO teachers (id, staff_no, full_name, nic, gender, email, phone, address, qualification, specialization, joined_date, status) VALUES
(1, 'TCH-2026-0001', 'Chamath Wijesinghe', '851234567V', 'MALE',   'chamath.w@example.com', '0712340001', '101 Main St, Colombo 07', 'BSc (Hons) Mathematics',      'Mathematics',     '2024-01-15', 'ACTIVE'),
(2, 'TCH-2026-0002', 'Ishara Gunawardena', '862345678V', 'FEMALE', 'ishara.g@example.com',  '0712340002', '202 Main St, Colombo 05', 'BA (Hons) English',           'English',         '2024-02-01', 'ACTIVE'),
(3, 'TCH-2026-0003', 'Dilshan Fernando',   '873456789V', 'MALE',   'dilshan.f@example.com', '0712340003', '303 Main St, Colombo 04', 'BSc Science',                 'Science',         '2025-06-10', 'ACTIVE'),
(4, 'TCH-2026-0004', 'Menaka Rathnayake',  '884567890V', 'FEMALE', 'menaka.r@example.com',  '0712340004', '404 Main St, Kandy',      'BSc (Hons) Computer Science', 'ICT',             '2025-01-20', 'ACTIVE');

INSERT INTO students (id, reg_no, first_name, last_name, name_with_initials, nic, gender, dob, address, phone, email, guardian_id, medical_notes, admission_date, status) VALUES
(1, 'STU-2026-0001', 'Sanduni', 'Jayawardena', 'S.M.J. Sanduni',   '200745123456', 'FEMALE', '2007-04-12', '12 Temple Rd, Nugegoda', '0775550001', 'sanduni.j@example.com', 1, 'Mild asthma - carries inhaler', '2026-01-10', 'ACTIVE'),
(2, 'STU-2026-0002', 'Kavindu', 'Perera',      'K.P. Kavindu',     '200812345678', 'MALE',   '2008-08-25', '45 Lake Ave, Kandy',     '0775550002', 'kavindu.p@example.com',  2, NULL,                            '2026-01-12', 'ACTIVE'),
(3, 'STU-2026-0003', 'Thimira', 'Silva',       'T.S. Thimira',     '200756789012', 'MALE',   '2007-11-03', '8 Hill St, Galle',       '0775550003', 'thimira.s@example.com',  3, NULL,                            '2026-01-15', 'ACTIVE'),
(4, 'STU-2026-0004', 'Nethmi',  'Dissanayake', 'N.D. Nethmi',      '200867890123', 'FEMALE', '2008-02-18', '23 Church Rd, Negombo',  '0775550004', 'nethmi.d@example.com',   4, 'Allergic to peanuts',           '2026-01-20', 'ACTIVE'),
(5, 'STU-2026-0005', 'Yasiru',  'Bandara',     'Y.B. Yasiru',      NULL,           'MALE',   '2007-06-30', '99 Kandy Rd, Kurunegala','0775550005', 'yasiru.b@example.com',   NULL, NULL,                          '2026-02-01', 'ACTIVE');

-- -------------------------------------------- users + roles (M05) ----------
-- All demo passwords: EduTrack123 (PBKDF2-hashed below, never plaintext)

INSERT INTO users (id, username, email, password_hash, full_name, phone, active, user_type, student_id, teacher_id, parent_id) VALUES
(1, 'admin',       'admin@example.com',      'pbkdf2$210000$00000000000000000000000000000001$d1adbbd4ac6e46dedd37ef03d6b727b108b4d05e0cc2aaff6bcf20e756b44b73', 'Nalin Perera (Director)',  '0710000001', 1, 'STAFF',   NULL, NULL, NULL),
(2, 'principal',   'principal@example.com',  'pbkdf2$210000$00000000000000000000000000000002$00e329ab5fac469dfe099441dc9f50714ad5b6e3b09bc1a6d1460cdae6db0a31', 'Malini Senanayake (Principal)', '0710000002', 1, 'STAFF', NULL, NULL, NULL),
(3, 'coordinator', 'coord@example.com',      'pbkdf2$210000$00000000000000000000000000000003$9dc7a4fa13e0beb7db2464829ca29601d29e8f2258ecd0ebb0f3db52955257a0', 'Tharushi Fernando (Coordinator)', '0710000003', 1, 'STAFF', NULL, NULL, NULL),
(4, 'teacher1',    'chamath.w@example.com',  'pbkdf2$210000$00000000000000000000000000000004$86f0d58c9b27c034af08621fa3d24dd5d32016f696429f59c01b0040350ffac7', 'Chamath Wijesinghe',       '0712340001', 1, 'STAFF',   NULL, 1, NULL),
(5, 'teacher2',    'ishara.g@example.com',   'pbkdf2$210000$00000000000000000000000000000005$a6c5694e27f98ee30feb7864818bc96d8884d08b3a5535b5496c4b2680e10eaa', 'Ishara Gunawardena',       '0712340002', 1, 'STAFF',   NULL, 2, NULL),
(6, 'finance',     'finance@example.com',    'pbkdf2$210000$00000000000000000000000000000006$b1202b79808c931a12620b885b176775042bba65664c620caec7df0cc2896061', 'Dinithi Karunaratne (Cashier)', '0710000004', 1, 'STAFF', NULL, NULL, NULL),
(7, 'student1',    'sanduni.j@example.com',  'pbkdf2$210000$00000000000000000000000000000007$181660493f5d697e91106f69c57bbf5308e6cfe8e59d4c856dbc26643c591a25', 'Sanduni Jayawardena',      '0775550001', 1, 'STUDENT', 1, NULL, NULL),
(8, 'student2',    'kavindu.p@example.com',  'pbkdf2$210000$00000000000000000000000000000008$061faaccda6e0803c4c3fa11f4672e2e28e342e698e5560ce6235c5926b92470', 'Kavindu Perera',           '0775550002', 1, 'STUDENT', 2, NULL, NULL),
(9, 'parent1',     'kumari.j@example.com',   'pbkdf2$210000$00000000000000000000000000000009$96fdd3d430a315e5bd509a23474dde058616e25256f789797b00858559d49a86', 'Kumari Jayawardena',       '0771111111', 1, 'PARENT',  NULL, NULL, 1);

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 3),
(4, 4), (5, 4),
(6, 5),
(7, 6), (8, 6),
(9, 7);

-- ---------------------------------------------------- curriculum (M02) ----

INSERT INTO subjects (id, code, name, description, active) VALUES
(1, 'MATH10', 'Mathematics',   'Grade 10 mathematics covering algebra, geometry and statistics', 1),
(2, 'ENG10',  'English',       'English language and literature', 1),
(3, 'SCI10',  'Science',       'Combined science: physics, chemistry, biology basics', 1),
(4, 'ICT10',  'ICT',           'Information and communication technology', 1);

INSERT INTO rooms (id, name, room_type, capacity) VALUES
(1, 'Room 101', 'CLASSROOM', 30),
(2, 'Room 102', 'CLASSROOM', 30),
(3, 'Main Hall', 'HALL',     120),
(4, 'Computer Lab', 'LAB',   25);

INSERT INTO classes (id, name, subject_id, teacher_id, room_id, capacity, start_date, end_date, status, description) VALUES
(1, 'Grade 10 Maths A',  1, 1, 1, 5,  '2026-01-05', '2026-12-18', 'ACTIVE', 'Weekday evening batch'),
(2, 'Grade 10 English A',2, 2, 2, 30, '2026-01-05', '2026-12-18', 'ACTIVE', 'Weekday morning batch'),
(3, 'Grade 10 Science A',3, 3, 1, 30, '2026-01-05', '2026-12-18', 'ACTIVE', 'Weekend batch'),
(4, 'Grade 10 ICT A',    4, 4, 4, 25, '2026-02-01', '2026-12-18', 'ACTIVE', 'Lab-based batch');

INSERT INTO enrolments (student_id, class_id, enrolled_date, status, remarks) VALUES
(1, 1, '2026-01-10', 'ACTIVE', NULL),
(1, 2, '2026-01-10', 'ACTIVE', NULL),
(2, 1, '2026-01-12', 'ACTIVE', NULL),
(2, 4, '2026-02-02', 'ACTIVE', NULL),
(3, 1, '2026-01-15', 'ACTIVE', NULL),
(3, 3, '2026-01-16', 'ACTIVE', NULL),
(4, 2, '2026-01-20', 'ACTIVE', NULL),
(4, 4, '2026-02-03', 'ACTIVE', NULL),
(5, 3, '2026-02-01', 'ACTIVE', NULL);

-- ------------------------------------------- timetable (M02, BR-TTC) -------
INSERT INTO timetable (class_id, subject_id, teacher_id, room_id, day_of_week, start_time, end_time, status, published_at) VALUES
(1, 1, 1, 1, 'MONDAY',    '15:00', '17:00', 'PUBLISHED', CURRENT_TIMESTAMP),
(1, 1, 1, 1, 'WEDNESDAY', '15:00', '17:00', 'PUBLISHED', CURRENT_TIMESTAMP),
(2, 2, 2, 2, 'TUESDAY',   '09:00', '11:00', 'PUBLISHED', CURRENT_TIMESTAMP),
(3, 3, 3, 1, 'SATURDAY',  '09:00', '11:30', 'PUBLISHED', CURRENT_TIMESTAMP),
(4, 4, 4, 4, 'FRIDAY',    '15:00', '17:00', 'APPROVED',  NULL);  -- left in APPROVED to demo the workflow

-- ------------------------------------------------- fees & payments (M04) --

INSERT INTO terms (id, name, start_date, end_date, active) VALUES
(1, '2026 Term 1', '2026-01-05', '2026-04-24', 0),
(2, '2026 Term 2', '2026-05-11', '2026-08-21', 1),
(3, '2026 Term 3', '2026-09-07', '2026-12-18', 0);

INSERT INTO fee_invoices (id, invoice_no, student_id, class_id, term_id, amount, amount_paid, discount, due_date, issued_date, status, remarks, attendance_linked, present_count, last_attendance_date) VALUES
(1, 'INV-2026-000001', 1, 1, 2, 7500.00, 7500.00, 0,    '2026-05-20', '2026-05-11', 'PAID',      NULL,                 1,  2, '2026-09-02'),
(2, 'INV-2026-000002', 1, 2, 2, 6000.00, 3000.00, 0,    '2026-05-20', '2026-05-11', 'PART_PAID', 'Half paid, balance due', 0, 0, NULL),
(3, 'INV-2026-000003', 2, 1, 2, 7500.00, 0,       500,  '2026-05-20', '2026-05-11', 'PENDING',   'Sibling discount applied', 1,  0, '2026-09-02'),
(4, 'INV-2026-000004', 2, 4, 2, 8000.00, 0,       0,    '2026-05-20', '2026-05-11', 'PENDING',   NULL,                 0, 0, NULL),
(5, 'INV-2026-000005', 3, 1, 2, 7500.00, 7500.00, 0,    '2026-05-20', '2026-05-11', 'PAID',      NULL,                 0, 0, NULL),
(6, 'INV-2026-000006', 4, 2, 2, 6000.00, 0,       0,    '2026-05-20', '2026-05-11', 'PENDING',   NULL,                 0, 0, NULL);

-- Monthly billing rates per class (M04 <-> M03 attendance linkage template).
INSERT INTO fee_structures (class_id, monthly_amount, active) VALUES
(1, 7500.00, 1),
(2, 6000.00, 1),
(3, 8000.00, 1),
(4, 8000.00, 1);

INSERT INTO payments (id, invoice_id, transaction_id, amount, method, status, payment_date, verified_at, verified_by, failure_reason, refund_reason, notes) VALUES
(1, 1, 'TXN-2026-100001', 7500.00, 'BANK_TRANSFER', 'VERIFIED', '2026-05-12', '2026-05-13 09:30:00', 6, NULL, NULL, 'Full settlement'),
(2, 2, 'TXN-2026-100002', 3000.00, 'CASH',          'VERIFIED', '2026-05-14', '2026-05-14 10:00:00', 6, NULL, NULL, 'First instalment'),
(3, 5, 'TXN-2026-100003', 7500.00, 'ONLINE',        'VERIFIED', '2026-05-15', '2026-05-15 14:20:00', 6, NULL, NULL, NULL),
(4, 2, 'TXN-2026-100004', 3000.00, 'CASH',          'FAILED',   '2026-05-16', NULL, 6, 'Duplicate transaction ID flagged for review (BR-FEE-02)', NULL, 'Retained for audit');

INSERT INTO receipts (id, receipt_no, payment_id, amount, issued_at, issued_by) VALUES
(1, 'RCP-20260906-0001', 1, 7500.00, '2026-05-13 09:30:00', 6),
(2, 'RCP-20260906-0002', 2, 3000.00, '2026-05-14 10:00:00', 6),
(3, 'RCP-20260906-0003', 3, 7500.00, '2026-05-15 14:20:00', 6);

-- ---------------------------------------------- attendance (M03) -----------
INSERT INTO attendance (student_id, class_id, timetable_slot_id, attendance_date, status, reason, marked_by, correction_note, corrected_at, corrected_by, admin_override) VALUES
(1, 1, 1, '2026-09-01', 'PRESENT', NULL, 4, NULL, NULL, NULL, 0),
(2, 1, 1, '2026-09-01', 'ABSENT',  'Fever', 4, NULL, NULL, NULL, 0),
(3, 1, 1, '2026-09-01', 'LATE',    'Bus delay', 4, 'Marked late by mistake, corrected to present within window', '2026-09-01 18:00:00', 4, 0),
(1, 1, 1, '2026-09-02', 'PRESENT', NULL, 4, NULL, NULL, NULL, 0),
(2, 1, 1, '2026-09-02', 'ABSENT',  'Fever (continued)', 4, NULL, NULL, NULL, 0),
(3, 1, 1, '2026-09-02', 'PRESENT', NULL, 4, NULL, NULL, NULL, 0),
(1, 1, 1, '2026-09-03', 'ABSENT',  'Family function', 4, NULL, NULL, NULL, 0),
(2, 1, 1, '2026-09-03', 'ABSENT',  'Fever (continued)', 4, NULL, NULL, NULL, 0),
(3, 1, 1, '2026-09-03', 'PRESENT', NULL, 4, NULL, NULL, NULL, 0),
(1, 2, 3, '2026-09-02', 'PRESENT', NULL, 5, NULL, NULL, NULL, 0),
(4, 2, 3, '2026-09-02', 'PRESENT', NULL, 5, NULL, NULL, NULL, 0);

-- ------------------------------------------ examinations & marks (M06) -----

INSERT INTO grade_bands (id, grade, min_mark, max_mark, description) VALUES
(1, 'A', 75, 100, 'Distinction'),
(2, 'B', 65, 74,  'Very good'),
(3, 'C', 55, 64,  'Good'),
(4, 'S', 35, 54,  'Pass'),
(5, 'W', 0,  34,  'Needs support');

INSERT INTO examinations (id, exam_name, term_id, class_id, subject_id, teacher_id, exam_date, start_time, end_time, room_text, max_marks, status) VALUES
(1, 'Term 2 Maths Mid Test',   2, 1, 1, 1, '2026-06-20', '09:00', '11:00', 'Room 101', 100, 'COMPLETED'),
(2, 'Term 2 English Quiz',     2, 2, 2, 2, '2026-06-22', '09:00', '10:00', 'Room 102', 50,  'COMPLETED'),
(3, 'Term 2 ICT Practical',    2, 4, 4, 4, '2026-07-15', '13:00', '15:00', 'Computer Lab', 100, 'SCHEDULED');

INSERT INTO marks (exam_id, student_id, marks, max_marks, grade, remarks, status, entered_by, entered_at, moderated_by, moderated_at, moderation_note, published_at) VALUES
(1, 1, 82.00, 100, 'A', 'Excellent work',        'PUBLISHED', 4, '2026-06-21 10:00:00', 2, '2026-06-25 09:00:00', 'Verified against scripts', '2026-06-26 08:00:00'),
(1, 2, 61.50, 100, 'C', 'Good effort',           'PUBLISHED', 4, '2026-06-21 10:05:00', 2, '2026-06-25 09:01:00', 'Verified', '2026-06-26 08:00:00'),
(1, 3, 73.00, 100, 'B', NULL,                    'PUBLISHED', 4, '2026-06-21 10:10:00', 2, '2026-06-25 09:02:00', 'Verified', '2026-06-26 08:00:00'),
(2, 1, 44.00, 50,  'B', 'Strong vocabulary',     'MODERATED', 5, '2026-06-23 11:00:00', 2, '2026-06-27 10:00:00', 'Moderated, awaiting publish', NULL),
(2, 4, 38.00, 50,  'S', NULL,                    'SUBMITTED', 5, '2026-06-23 11:05:00', NULL, NULL, NULL, NULL);

INSERT INTO report_cards (student_id, term_id, average_marks, overall_grade, attendance_percent, status, generated_at, published_at) VALUES
(1, 2, 82.00, 'A', 96, 'GENERATED', CURRENT_TIMESTAMP, NULL);

-- ------------------------------------------ notices & materials (M02) ------

INSERT INTO notices (id, title, content, category, audience_type, audience_ref_id, channel, priority, created_by, status, published_at) VALUES
(1, 'Term 2 Fee Deadline', 'Please settle Term 2 invoices before 20 June to avoid the late-enrolment review. Payments can be verified at the office counter.', 'FINANCIAL', 'ALL', NULL, 'IN_SYSTEM', 'HIGH', 6, 'PUBLISHED', CURRENT_TIMESTAMP),
(2, 'Maths Class Rescheduled', 'Monday Grade 10 Maths A now starts at 15:30 instead of 15:00. Affected students and parents were notified through the system.', 'ACADEMIC', 'CLASS', '1', 'IN_SYSTEM', 'NORMAL', 3, 'PUBLISHED', CURRENT_TIMESTAMP),
(3, 'ICT Lab Safety Briefing', 'All Grade 10 ICT students must attend the lab safety briefing on Friday before the practical session.', 'ACADEMIC', 'CLASS', '4', 'EMAIL', 'NORMAL', 3, 'PUBLISHED', CURRENT_TIMESTAMP),
(4, 'Emergency: Campus Closed Saturday', 'The campus is closed this Saturday for maintenance. Saturday sessions move to next Sunday, same times.', 'EMERGENCY', 'ALL', NULL, 'SMS', 'EMERGENCY', 2, 'PUBLISHED', CURRENT_TIMESTAMP);

INSERT INTO teaching_materials (id, class_id, subject_id, uploaded_by, title, description, file_path, file_name, status) VALUES
(1, 1, 1, 4, 'Algebra Worksheet Set 1', 'Practice problems with answer key', 'https://example.com/materials/algebra-set1.pdf', 'algebra-set1.pdf', 'ACTIVE'),
(2, 2, 2, 5, 'Essay Writing Guide', 'Structure, tone and marking rubric', 'https://example.com/materials/essay-guide.pdf', 'essay-guide.pdf', 'ACTIVE'),
(3, 4, 4, 4, 'Python Basics Slides', 'Slides used in the first lab', 'https://example.com/materials/python-basics.pdf', 'python-basics.pdf', 'ACTIVE');

-- ------------------------------------- notifications & audit seed ----------
INSERT INTO notifications (user_id, role_name, student_id, title, message, channel, delivery_status, is_read) VALUES
(7, NULL, 1, 'Absence alert', 'Attendance alert: your child was marked absent on 2026-09-03. Please contact the institute if this is unexpected.', 'IN_SYSTEM', 'SENT', 0),
(9, NULL, 1, 'Absence alert', 'Attendance alert: your child was marked absent on 2026-09-03. Please contact the institute if this is unexpected.', 'IN_SYSTEM', 'SENT', 0),
(NULL, 'TEACHER', NULL, 'Timetable published', 'Timetable update: Grade 10 Maths A - Mathematics on MONDAY 15:00-17:00 in Room 101.', 'IN_SYSTEM', 'SENT', 0);

INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_value, new_value) VALUES
(1, 'SEED',        'SYSTEM',   NULL, NULL, 'Demo data installed'),
(3, 'TIMETABLE_PUBLISH', 'TIMETABLE', 1, 'APPROVED', 'PUBLISHED'),
(6, 'PAYMENT_VERIFIED',  'PAYMENT',   1, 'PENDING', 'VERIFIED'),
(2, 'MARKS_PUBLISH',     'EXAM',      1, 'MODERATED', 'PUBLISHED'),
(3, 'STUDENT_ENROL',     'ENROLMENT', 1, NULL, 'STU-2026-0001 -> class Grade 10 Maths A');

