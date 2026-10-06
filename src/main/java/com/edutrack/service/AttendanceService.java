package com.edutrack.service;

import com.edutrack.dao.AttendanceDAO;
import com.edutrack.dao.EnrolmentDAO;
import com.edutrack.dao.impl.AttendanceDAOImpl;
import com.edutrack.dao.impl.EnrolmentDAOImpl;
import com.edutrack.exception.AuthorizationException;
import com.edutrack.exception.BusinessRuleException;
import com.edutrack.model.Attendance;
import com.edutrack.model.Enrolment;
import com.edutrack.util.AuditLogger;
import com.edutrack.util.DB;
import com.edutrack.util.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attendance Management (Member 03 - IT25103995).
 * BR-ATT-01 one entry per student per session; BR-ATT-02 correction window with reason;
 * BR-ATT-03 no edits after lock without an audited admin override. Also runs the
 * chronic-absence flag and parent notification (PBI-11).
 */
public class AttendanceService {

    /** Correction window in hours (configurable per proposal: "late correction within an allowed window"). */
    public static final int CORRECTION_WINDOW_HOURS = 48;
    /** Chronic-absence threshold: absences within the term meeting/exceeding this trigger a flag. */
    public static final int CHRONIC_ABSENCE_THRESHOLD = 3;

    private final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();
    private final EnrolmentDAO enrolmentDAO = new EnrolmentDAOImpl();
    private final FeeService feeService = new FeeService();  // M04 <-> M03 billing linkage

    /** Marks attendance for the whole class register in one transaction (PBI-09). */
    public int markRegister(com.edutrack.model.User actor, long classId, LocalDate date,
                            Map<Long, String> statuses, Map<Long, String> reasons) {
        requireTeacher(actor, classId);
        Validator v = new Validator();
        v.required(date == null ? null : "x", "date", "Attendance date");
        v.check();
        if (statuses == null || statuses.isEmpty()) {
            throw new BusinessRuleException("No attendance statuses submitted.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Attendance cannot be marked for a future date.");
        }
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int saved = 0;
                for (Map.Entry<Long, String> entry : statuses.entrySet()) {
                    long studentId = entry.getKey();
                    String status = entry.getValue() == null ? "" : entry.getValue().trim();
                    if (!Attendance.PRESENT.equals(status) && !Attendance.ABSENT.equals(status)
                            && !Attendance.LATE.equals(status) && !Attendance.EXCUSED.equals(status)) {
                        throw new BusinessRuleException("Invalid attendance status: " + status);
                    }
                    // Enrolment check: attendance belongs to enrolled students (integration rule).
                    Enrolment enrolment = enrolmentDAO.findActive(conn, studentId, classId);
                    if (enrolment == null) {
                        throw new BusinessRuleException("Student #" + studentId
                                + " is not actively enrolled in this class.");
                    }
                    // BR-ATT-01: duplicate probe
                    Attendance existing = attendanceDAO.findBySession(conn, studentId, classId, date);
                    String reason = reasons == null ? null : reasons.get(studentId);
                    if (existing != null) {
                        throw new BusinessRuleException("Attendance already recorded for "
                                + existing.getStudentName() + " on " + date + " (BR-ATT-01).");
                    }
                    Attendance a = new Attendance();
                    a.setStudentId(studentId);
                    a.setClassId(classId);
                    a.setAttendanceDate(date);
                    a.setStatus(status);
                    a.setReason(reason);
                    a.setMarkedBy(actor.getId());
                    attendanceDAO.insert(conn, a);
                    saved++;
                    // M04 <-> M03 linkage: attended sessions count toward the monthly invoice.
                    if (Attendance.PRESENT.equals(status) || Attendance.LATE.equals(status)) {
                        feeService.syncAttendanceBilling(conn, actor, studentId, classId, date, +1);
                    }
                }
                AuditLogger.log(conn, actor, "ATTENDANCE_MARK", "ATTENDANCE", classId, null,
                        "class#" + classId + " " + date + " " + saved + " entries");
                conn.commit();
                return saved;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Attendance marking failed due to a database error.", e);
        }
    }

    /** BR-ATT-02/03: correct an entry with reason; after the window an admin override is required and audited. */
    public void correctEntry(com.edutrack.model.User actor, long attendanceId, String newStatus,
                             String reason, String correctionNote) {
        Validator v = new Validator();
        v.required(correctionNote, "correctionNote", "Correction reason");
        v.check();
        if (!Attendance.PRESENT.equals(newStatus) && !Attendance.ABSENT.equals(newStatus)
                && !Attendance.LATE.equals(newStatus) && !Attendance.EXCUSED.equals(newStatus)) {
            throw new BusinessRuleException("Invalid attendance status.");
        }
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
            Attendance existing = attendanceDAO.findById(conn, attendanceId);
            if (existing == null) throw new BusinessRuleException("Attendance record not found.");

            boolean teacherOfThisClass = actor.hasRole("TEACHER") && isClassTeacher(conn, actor, existing.getClassId());
            boolean admin = actor.hasRole("ADMIN") || actor.hasRole("ACADEMIC_COORDINATOR") || actor.hasRole("PRINCIPAL");
            if (!teacherOfThisClass && !admin) {
                throw new AuthorizationException("Only the class teacher or an administrator can correct attendance.");
            }

            LocalDateTime createdAt = existing.getCreatedAt() == null ? LocalDateTime.now() : existing.getCreatedAt();
            boolean withinWindow = createdAt.plusHours(CORRECTION_WINDOW_HOURS).isAfter(LocalDateTime.now());
            boolean adminOverride = false;
            if (!withinWindow) {
                if (!admin) {
                    throw new BusinessRuleException("The correction window (" + CORRECTION_WINDOW_HOURS
                            + "h) has closed. Only an administrator can override this (BR-ATT-03).");
                }
                adminOverride = true;
            }

            String oldStatus = existing.getStatus();
            existing.setStatus(newStatus);
            existing.setReason(reason);
            existing.setCorrectionNote(correctionNote);
            existing.setCorrectedAt(LocalDateTime.now());
            existing.setCorrectedBy(actor.getId());
            existing.setAdminOverride(adminOverride);
            attendanceDAO.update(conn, existing);

            // M04 <-> M03 linkage: keep billing in step with the corrected status.
            boolean wasAttended = Attendance.PRESENT.equals(oldStatus) || Attendance.LATE.equals(oldStatus);
            boolean nowAttended = Attendance.PRESENT.equals(newStatus) || Attendance.LATE.equals(newStatus);
            int delta = (nowAttended ? 1 : 0) - (wasAttended ? 1 : 0);
            feeService.syncAttendanceBilling(conn, actor, existing.getStudentId(),
                    existing.getClassId(), existing.getAttendanceDate(), delta);

            AuditLogger.log(conn, actor, adminOverride ? "ATTENDANCE_OVERRIDE" : "ATTENDANCE_CORRECT",
                    "ATTENDANCE", attendanceId,
                    existing.getStudentName() + " " + oldStatus + " " + existing.getAttendanceDate(),
                    newStatus + " note=" + correctionNote);

            if (Attendance.ABSENT.equals(newStatus)) {
                notifyParentOfAbsence(conn, existing);
            }
            conn.commit();
            } catch (Exception e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) {
            throw new RuntimeException("Attendance correction failed due to a database error.", e);
        }
    }

    /** PBI-11: chronic-absence pattern detection for a class over a date range. */
    public List<Map<String, Object>> chronicAbsences(com.edutrack.model.User actor, long classId,
                                                     LocalDate from, LocalDate to) {
        try (Connection conn = DB.getConnection()) {
            List<long[]> rows = attendanceDAO.chronicAbsences(conn, classId, from, to, CHRONIC_ABSENCE_THRESHOLD);
            List<Map<String, Object>> result = new ArrayList<>();
            for (long[] row : rows) {
                Map<String, Object> m = new HashMap<>();
                m.put("studentId", row[0]);
                m.put("absences", row[1]);
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT reg_no, first_name, last_name, guardian_id FROM students WHERE id = ?")) {
                    ps.setLong(1, row[0]);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            m.put("regNo", rs.getString("reg_no"));
                            m.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                            long guardianId = rs.getLong("guardian_id");
                            m.put("guardianId", rs.wasNull() ? null : guardianId);
                        }
                    }
                }
                result.add(m);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Chronic-absence analysis failed due to a database error.", e);
        }
    }

    /** Sends the parent notification for a chronic-absent student (PBI-11). */
    public void notifyParentOfAbsence(com.edutrack.model.User actor, long studentId, int absenceCount) {
        try (Connection conn = DB.getConnection()) {
            Attendance probe = new Attendance();
            probe.setStudentId(studentId);
            notifyParent(conn, studentId, absenceCount);
            AuditLogger.log(conn, actor, "ABSENCE_ALERT", "STUDENT", studentId, null,
                    "parent notified of " + absenceCount + " absences");
        } catch (SQLException e) {
            throw new RuntimeException("Parent notification failed due to a database error.", e);
        }
    }

    private void notifyParentOfAbsence(Connection conn, Attendance attendance) throws SQLException {
        notifyParent(conn, attendance.getStudentId(), 1);
    }

    private void notifyParent(Connection conn, long studentId, int absenceCount) throws SQLException {
        String message = "Attendance alert: your child was marked absent on " + LocalDate.now()
                + ". Total recent absences: " + absenceCount + ". Please contact the institute if this is unexpected.";
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO notifications (user_id, role_name, student_id, title, message, channel, delivery_status, is_read, created_at) " +
                "VALUES (NULL,'PARENT',?, 'Absence alert', ?, 'IN_SYSTEM', 'SENT', 0, CURRENT_TIMESTAMP)")) {
            ps.setLong(1, studentId);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }

    private boolean isClassTeacher(Connection conn, com.edutrack.model.User actor, long classId) throws SQLException {
        if (actor.getTeacherId() == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(
                // The teacher of the class OR the teacher of any timetable slot of the class may mark it.
                "SELECT COUNT(*) FROM classes c WHERE c.id = ? AND (c.teacher_id = ? " +
                "OR EXISTS (SELECT 1 FROM timetable t WHERE t.class_id = c.id AND t.teacher_id = ?))")) {
            ps.setLong(1, classId);
            ps.setLong(2, actor.getTeacherId());
            ps.setLong(3, actor.getTeacherId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        }
    }

    public List<Attendance> search(Long studentId, Long classId, LocalDate from, LocalDate to,
                                   String status, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return attendanceDAO.search(conn, studentId, classId, from, to, emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search attendance.", e);
        }
    }

    public long count(Long studentId, Long classId, LocalDate from, LocalDate to, String status) {
        try (Connection conn = DB.getConnection()) {
            return attendanceDAO.countSearch(conn, studentId, classId, from, to, emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not count attendance.", e);
        }
    }

    public Attendance find(long id) {
        try (Connection conn = DB.getConnection()) {
            return attendanceDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load attendance.", e);
        }
    }

    public Map<String, Long> summary(Long classId, LocalDate from, LocalDate to) {
        try (Connection conn = DB.getConnection()) {
            Map<String, Long> map = new HashMap<>();
            map.put("present", attendanceDAO.countByStatus(conn, classId, from, to, Attendance.PRESENT));
            map.put("absent", attendanceDAO.countByStatus(conn, classId, from, to, Attendance.ABSENT));
            map.put("late", attendanceDAO.countByStatus(conn, classId, from, to, Attendance.LATE));
            map.put("excused", attendanceDAO.countByStatus(conn, classId, from, to, Attendance.EXCUSED));
            return map;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load attendance summary.", e);
        }
    }

    public void requireTeacher(com.edutrack.model.User actor, long classId) {
        if (actor == null) throw new AuthorizationException("Not authenticated.");
        if (actor.hasRole("ADMIN") || actor.hasRole("ACADEMIC_COORDINATOR")) return;
        if (actor.hasRole("TEACHER")) {
            try (Connection conn = DB.getConnection()) {
                if (isClassTeacher(conn, actor, classId)) return;
            } catch (SQLException e) { throw new RuntimeException("Could not verify class assignment.", e); }
        }
        throw new AuthorizationException("Only teachers can mark attendance.");
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
