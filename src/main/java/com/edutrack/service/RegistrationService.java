package com.edutrack.service;

import com.edutrack.dao.EnrolmentDAO;
import com.edutrack.dao.GuardianDAO;
import com.edutrack.dao.StudentDAO;
import com.edutrack.dao.TeacherDAO;
import com.edutrack.dao.impl.EnrolmentDAOImpl;
import com.edutrack.dao.impl.GuardianDAOImpl;
import com.edutrack.dao.impl.StudentDAOImpl;
import com.edutrack.dao.impl.TeacherDAOImpl;
import com.edutrack.dao.RoleDAO;
import com.edutrack.dao.UserDAO;
import com.edutrack.dao.impl.RoleDAOImpl;
import com.edutrack.dao.impl.UserDAOImpl;
import com.edutrack.model.*;
import com.edutrack.util.PasswordUtil;
import com.edutrack.exception.BusinessRuleException;
import com.edutrack.exception.ValidationException;
import com.edutrack.util.AuditLogger;
import com.edutrack.util.CodeGenerator;
import com.edutrack.util.DB;
import com.edutrack.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Student & Teacher Registration / Profile Management (Member 01 - IT25103993).
 * Business rules: BR-REG-01 unique auto reg numbers, BR-REG-02 duplicate NIC blocked,
 * BR-REG-03 class capacity enforced, re-enrolment of withdrawn students needs admin approval.
 */
public class RegistrationService {

    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final TeacherDAO teacherDAO = new TeacherDAOImpl();
    private final GuardianDAO guardianDAO = new GuardianDAOImpl();
    private final EnrolmentDAO enrolmentDAO = new EnrolmentDAOImpl();
    private final TimetableService timetableService = new TimetableService();
    private final UserDAO userDAO = new UserDAOImpl();
    private final RoleDAO roleDAO = new RoleDAOImpl();

    // --------------------------------------------------------------- students

    public Student registerStudent(com.edutrack.model.User actor, Student student, Guardian guardian,
                                   Long classId, boolean adminApprovingReenrolment) {
        requireStaff(actor);
        Validator v = new Validator();
        v.required(student.getFirstName(), "firstName", "First name")
                .required(student.getLastName(), "lastName", "Last name")
                .nic(student.getNic(), "nic", "NIC / birth-certificate no.")
                .email(student.getEmail(), "email", "Email")
                .phone(student.getPhone(), "phone", "Phone")
                .required(student.getGender(), "gender", "Gender");
        if (student.getDob() != null && student.getDob().isAfter(LocalDate.now())) {
            v.add("dob", "Date of birth cannot be in the future.");
        }
        v.check();
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // BR-REG-02 duplicate NIC check
                if (student.getNic() != null && !student.getNic().isBlank()) {
                    Student dup = studentDAO.findByNic(conn, student.getNic().trim());
                    if (dup != null) {
                        throw new BusinessRuleException("A student with this NIC / birth-certificate number already exists ("
                                + dup.getRegNo() + "). Duplicate registrations are blocked (BR-REG-02).");
                    }
                }
                // Guardian link: reuse by NIC or create
                if (guardian != null && guardian.getFullName() != null && !guardian.getFullName().isBlank()) {
                    Guardian existing = guardian.getNic() != null && !guardian.getNic().isBlank()
                            ? guardianDAO.findByNic(conn, guardian.getNic().trim()) : null;
                    if (existing != null) {
                        student.setGuardianId(existing.getId());
                    } else {
                        Guardian saved = guardianDAO.insert(conn, guardian);
                        student.setGuardianId(saved.getId());
                    }
                }
                // BR-REG-01 unique auto-generated registration number
                String regNo = CodeGenerator.studentRegNo(studentDAO.nextSequence(conn));
                student.setRegNo(regNo);
                student.setStatus("ACTIVE");
                if (student.getAdmissionDate() == null) student.setAdmissionDate(LocalDate.now());
                Student saved = studentDAO.insert(conn, student);

                // Create the linked STUDENT login account in the same transaction.
                // Username is the immutable registration number so every newly
                // registered student receives a deterministic login identity.
                String username = saved.getRegNo();
                String temporaryPassword = CodeGenerator.temporaryStudentPassword();
                if (userDAO.usernameExists(conn, username, null)) {
                    throw new BusinessRuleException("A login account already exists for registration number "
                            + username + ".");
                }
                String email = saved.getEmail();
                if (email != null && email.isBlank()) email = null;
                if (email != null && userDAO.emailExists(conn, email.trim(), null)) {
                    throw new BusinessRuleException("The student's email is already registered to another user.");
                }

                com.edutrack.model.Role studentRole = roleDAO.findByName(conn, "STUDENT");
                if (studentRole == null) {
                    throw new BusinessRuleException("The STUDENT role is missing from the database. "
                            + "Run the roles seed data before registering students.");
                }

                User studentUser = new User();
                studentUser.setUsername(username);
                studentUser.setEmail(email);
                studentUser.setPasswordHash(PasswordUtil.hash(temporaryPassword));
                studentUser.setFullName(saved.fullName());
                studentUser.setPhone(saved.getPhone());
                studentUser.setActive(true);
                studentUser.setUserType("STUDENT");
                studentUser.setStudentId(saved.getId());
                userDAO.insert(conn, studentUser, java.util.List.of(studentRole.getId()));

                // Optional enrolment at registration (BR-REG-03 capacity)
                if (classId != null) {
                    enrol(conn, actor, saved, classId, adminApprovingReenrolment);
                }
                AuditLogger.log(conn, actor, "STUDENT_CREATE", "STUDENT", saved.getId(), null,
                        saved.getRegNo() + " " + saved.fullName());
                AuditLogger.log(conn, actor, "STUDENT_LOGIN_CREATE", "USER", studentUser.getId(), null,
                        "username=" + username + "; studentId=" + saved.getId());
                conn.commit();

                // Returned to the servlet so the existing flash-message component
                // can show credentials once after the redirect. No JSP/UI change.
                StudentAccountCredentials.set(temporaryPassword);
                return saved;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Student registration failed due to a database error.", e);
        }
    }

    public void enrol(com.edutrack.model.User actor, Student student, Long classId, boolean adminApprovingReenrolment) {
        requireStaff(actor);
        if (student == null) throw new BusinessRuleException("Student not found.");
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                enrol(conn, actor, student, classId, adminApprovingReenrolment);
                conn.commit();
            } catch (Exception e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) {
            throw new RuntimeException("Enrolment failed due to a database error.", e);
        }
    }

    private void enrol(Connection conn, com.edutrack.model.User actor, Student student, Long classId,
                       boolean adminApprovingReenrolment) throws SQLException {
        if ("WITHDRAWN".equals(student.getStatus())) {
            if (!adminApprovingReenrolment) {
                throw new BusinessRuleException("A withdrawn student cannot be re-enrolled without admin approval.");
            }
            studentDAO.updateStatus(conn, student.getId(), "ACTIVE", null, null);
            AuditLogger.log(conn, actor, "STUDENT_REENROL_APPROVED", "STUDENT", student.getId(),
                    "WITHDRAWN", "ACTIVE");
        }
        ClassRoom clazz = timetableService.findClass(conn, classId);
        if (clazz == null || !"ACTIVE".equals(clazz.getStatus())) {
            throw new BusinessRuleException("The selected class is not active.");
        }
        long activeCount = enrolmentDAO.countActive(conn, classId);
        if (clazz.getCapacity() != null && activeCount >= clazz.getCapacity()) {
            throw new BusinessRuleException("Class '" + clazz.getName() + "' is full (capacity "
                    + clazz.getCapacity() + "). Over-enrolment is blocked (BR-REG-03).");
        }
        Enrolment existing = enrolmentDAO.findActive(conn, student.getId(), classId);
        if (existing != null) {
            throw new BusinessRuleException("Student is already actively enrolled in this class "
                    + "(one active enrolment per class).");
        }
        Enrolment e = new Enrolment();
        e.setStudentId(student.getId());
        e.setClassId(classId);
        e.setEnrolledDate(LocalDate.now());
        e.setStatus("ACTIVE");
        enrolmentDAO.insert(conn, e);
        AuditLogger.log(conn, actor, "STUDENT_ENROL", "ENROLMENT", e.getId(), null,
                student.getRegNo() + " -> class " + clazz.getName());
    }

    public Student updateStudent(com.edutrack.model.User actor, Student changes) {
        requireStaff(actor);
        Validator v = new Validator();
        v.required(changes.getFirstName(), "firstName", "First name")
                .required(changes.getLastName(), "lastName", "Last name")
                .nic(changes.getNic(), "nic", "NIC / birth-certificate no.")
                .email(changes.getEmail(), "email", "Email")
                .phone(changes.getPhone(), "phone", "Phone");
        v.check();
        try (Connection conn = DB.getConnection()) {
            Student before = studentDAO.findById(conn, changes.getId());
            if (before == null) throw new BusinessRuleException("Student not found.");
            if (changes.getNic() != null && !changes.getNic().isBlank()) {
                Student dup = studentDAO.findByNic(conn, changes.getNic().trim());
                if (dup != null && !dup.getId().equals(changes.getId())) {
                    throw new BusinessRuleException("Another student already uses this NIC (BR-REG-02).");
                }
            }
            changes.setStatus(before.getStatus());
            studentDAO.update(conn, changes);
            AuditLogger.log(conn, actor, "STUDENT_UPDATE", "STUDENT", changes.getId(),
                    before.getPhone() + "|" + before.getAddress() + "|" + before.getEmail(),
                    changes.getPhone() + "|" + changes.getAddress() + "|" + changes.getEmail());
            return studentDAO.findById(conn, changes.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Student update failed due to a database error.", e);
        }
    }

    /** Withdrawal archives the record; history is retained (safe lifecycle). */
    public void withdrawStudent(com.edutrack.model.User actor, long studentId, String reason) {
        requireStaff(actor);
        Validator v = new Validator();
        v.required(reason, "reason", "Withdrawal reason");
        v.check();
        try (Connection conn = DB.getConnection()) {
            Student before = studentDAO.findById(conn, studentId);
            if (before == null) throw new BusinessRuleException("Student not found.");
            if ("WITHDRAWN".equals(before.getStatus())) {
                throw new BusinessRuleException("Student is already withdrawn.");
            }
            conn.setAutoCommit(false);
            try {
            studentDAO.updateStatus(conn, studentId, "WITHDRAWN", LocalDate.now(), reason);
            try (var ps = conn.prepareStatement("UPDATE enrolments SET status = 'WITHDRAWN' WHERE student_id = ? AND status = 'ACTIVE'")) {
                ps.setLong(1, studentId); ps.executeUpdate();
            }
            AuditLogger.log(conn, actor, "STUDENT_WITHDRAW", "STUDENT", studentId,
                    before.getStatus(), "WITHDRAWN: " + reason);
            conn.commit();
            } catch (Exception e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) {
            throw new RuntimeException("Withdrawal failed due to a database error.", e);
        }
    }

    // --------------------------------------------------------------- teachers

    public Teacher registerTeacher(com.edutrack.model.User actor, Teacher teacher) {
        requireStaff(actor);
        Validator v = new Validator();
        v.required(teacher.getFullName(), "fullName", "Full name")
                .nic(teacher.getNic(), "nic", "NIC")
                .email(teacher.getEmail(), "email", "Email")
                .phone(teacher.getPhone(), "phone", "Phone");
        v.check();
        try (Connection conn = DB.getConnection()) {
            if (teacher.getNic() != null && !teacher.getNic().isBlank()) {
                Teacher dup = teacherDAO.findByNic(conn, teacher.getNic().trim());
                if (dup != null) {
                    throw new BusinessRuleException("A teacher with this NIC already exists ("
                            + dup.getStaffNo() + ") (BR-REG-02).");
                }
            }
            teacher.setStaffNo(CodeGenerator.teacherRegNo(teacherDAO.nextSequence(conn)));
            teacher.setStatus("ACTIVE");
            if (teacher.getJoinedDate() == null) teacher.setJoinedDate(LocalDate.now());
            Teacher saved = teacherDAO.insert(conn, teacher);
            AuditLogger.log(conn, actor, "TEACHER_CREATE", "TEACHER", saved.getId(), null,
                    saved.getStaffNo() + " " + saved.getFullName());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Teacher registration failed due to a database error.", e);
        }
    }

    public Teacher updateTeacher(com.edutrack.model.User actor, Teacher changes) {
        requireStaff(actor);
        Validator v = new Validator();
        v.required(changes.getFullName(), "fullName", "Full name")
                .nic(changes.getNic(), "nic", "NIC")
                .email(changes.getEmail(), "email", "Email")
                .phone(changes.getPhone(), "phone", "Phone");
        v.check();
        try (Connection conn = DB.getConnection()) {
            Teacher before = teacherDAO.findById(conn, changes.getId());
            if (before == null) throw new BusinessRuleException("Teacher not found.");
            changes.setStatus(before.getStatus());
            teacherDAO.update(conn, changes);
            AuditLogger.log(conn, actor, "TEACHER_UPDATE", "TEACHER", changes.getId(),
                    before.getPhone() + "|" + before.getQualification(),
                    changes.getPhone() + "|" + changes.getQualification());
            return teacherDAO.findById(conn, changes.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Teacher update failed due to a database error.", e);
        }
    }

    public void resignTeacher(com.edutrack.model.User actor, long teacherId, String reason) {
        requireStaff(actor);
        Validator v = new Validator();
        v.required(reason, "reason", "Resignation / status reason");
        v.check();
        try (Connection conn = DB.getConnection()) {
            Teacher before = teacherDAO.findById(conn, teacherId);
            if (before == null) throw new BusinessRuleException("Teacher not found.");
            teacherDAO.updateStatus(conn, teacherId, "RESIGNED", LocalDate.now(), reason);
            AuditLogger.log(conn, actor, "TEACHER_RESIGN", "TEACHER", teacherId,
                    before.getStatus(), "RESIGNED: " + reason);
        } catch (SQLException e) {
            throw new RuntimeException("Status change failed due to a database error.", e);
        }
    }

    // --------------------------------------------------------------- guardians

    public Guardian saveGuardian(com.edutrack.model.User actor, Guardian guardian) {
        requireStaff(actor);
        Validator v = new Validator();
        v.required(guardian.getFullName(), "fullName", "Guardian name")
                .phone(guardian.getPhone(), "phone", "Guardian phone");
        v.check();
        try (Connection conn = DB.getConnection()) {
            if (guardian.getId() != null) {
                Guardian before = guardianDAO.findById(conn, guardian.getId());
                guardianDAO.update(conn, guardian);
                AuditLogger.log(conn, actor, "GUARDIAN_UPDATE", "GUARDIAN", guardian.getId(),
                        before == null ? null : before.getPhone(), guardian.getPhone());
                return guardianDAO.findById(conn, guardian.getId());
            }
            Guardian saved = guardianDAO.insert(conn, guardian);
            AuditLogger.log(conn, actor, "GUARDIAN_CREATE", "GUARDIAN", saved.getId(), null, saved.getFullName());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Guardian save failed due to a database error.", e);
        }
    }

    // --------------------------------------------------------------- queries

    public List<Student> searchStudents(String q, Long guardianId, String status, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return studentDAO.search(conn, emptyToNull(q), guardianId, emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search students.", e);
        }
    }

    public long countStudents(String q, Long guardianId, String status) {
        try (Connection conn = DB.getConnection()) {
            return studentDAO.countSearch(conn, emptyToNull(q), guardianId, emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not count students.", e);
        }
    }

    public Student findStudent(long id) {
        try (Connection conn = DB.getConnection()) {
            return studentDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load student.", e);
        }
    }

    public List<Enrolment> enrolmentsOfStudent(long studentId) {
        try (Connection conn = DB.getConnection()) {
            return enrolmentDAO.findByStudent(conn, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load enrolments.", e);
        }
    }

    /** Active enrolments of a class with joined student data (attendance register). */
    public List<Enrolment> enrolmentsOfClass(long classId, String status) {
        try (Connection conn = DB.getConnection()) {
            return enrolmentDAO.findByClass(conn, classId, status);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load class enrolments.", e);
        }
    }

    public List<Teacher> searchTeachers(String q, String status, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return teacherDAO.search(conn, emptyToNull(q), emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search teachers.", e);
        }
    }

    public long countTeachers(String q, String status) {
        try (Connection conn = DB.getConnection()) {
            return teacherDAO.countSearch(conn, emptyToNull(q), emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not count teachers.", e);
        }
    }

    public Teacher findTeacher(long id) {
        try (Connection conn = DB.getConnection()) {
            return teacherDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load teacher.", e);
        }
    }

    public List<Teacher> activeTeachers() {
        try (Connection conn = DB.getConnection()) {
            return teacherDAO.findAllActive(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load teachers.", e);
        }
    }

    public List<Guardian> searchGuardians(String q, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return guardianDAO.search(conn, emptyToNull(q), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search guardians.", e);
        }
    }

    public Guardian findGuardian(long id) {
        try (Connection conn = DB.getConnection()) {
            return guardianDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load guardian.", e);
        }
    }

    public long countStudentsByStatus(String status) {
        try (Connection conn = DB.getConnection()) {
            return status == null || status.isBlank()
                    ? studentDAO.countAll(conn)
                    : studentDAO.countByStatus(conn, status);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load student stats.", e);
        }
    }


    /** One-request handoff for newly created student credentials; never persisted. */
    public static final class StudentAccountCredentials {
        private static final ThreadLocal<String> LAST_TEMP_PASSWORD = new ThreadLocal<>();

        private StudentAccountCredentials() {}

        public static void set(String password) {
            LAST_TEMP_PASSWORD.set(password);
        }

        public static String consume() {
            String value = LAST_TEMP_PASSWORD.get();
            LAST_TEMP_PASSWORD.remove();
            return value;
        }
    }

    // --------------------------------------------------------------- permissions

    private void requireStaff(com.edutrack.model.User actor) {
        if (actor == null) throw new com.edutrack.exception.AuthorizationException("Not authenticated.");
        boolean staff = actor.hasRole("ADMIN") || actor.hasRole("ACADEMIC_COORDINATOR") || actor.hasRole("PRINCIPAL");
        if (!staff) {
            throw new com.edutrack.exception.AuthorizationException(
                    "Only administrative staff can manage registration records.");
        }
    }

    /** Staff may edit any profile; a STUDENT user may edit only their own contact details. */
    private void requireStaffOrSelf(com.edutrack.model.User actor, long studentId) {
        if (actor == null) throw new com.edutrack.exception.AuthorizationException("Not authenticated.");
        if (actor.hasRole("ADMIN") || actor.hasRole("ACADEMIC_COORDINATOR") || actor.hasRole("PRINCIPAL")) {
            return;
        }
        if (actor.getStudentId() != null && actor.getStudentId() == studentId) {
            return;
        }
        throw new com.edutrack.exception.AuthorizationException("You can only update your own profile.");
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
