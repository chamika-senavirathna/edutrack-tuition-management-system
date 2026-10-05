package com.edutrack.service;

import com.edutrack.dao.EnrolmentDAO;
import com.edutrack.dao.ExaminationDAO;
import com.edutrack.dao.GradeBandDAO;
import com.edutrack.dao.MarksDAO;
import com.edutrack.dao.ReportCardDAO;
import com.edutrack.dao.impl.EnrolmentDAOImpl;
import com.edutrack.dao.impl.ExaminationDAOImpl;
import com.edutrack.dao.impl.GradeBandDAOImpl;
import com.edutrack.dao.impl.MarksDAOImpl;
import com.edutrack.dao.impl.ReportCardDAOImpl;
import com.edutrack.exception.AuthorizationException;
import com.edutrack.exception.BusinessRuleException;
import com.edutrack.model.Enrolment;
import com.edutrack.model.Examination;
import com.edutrack.model.GradeBand;
import com.edutrack.model.Marks;
import com.edutrack.model.ReportCard;
import com.edutrack.util.AuditLogger;
import com.edutrack.util.DB;
import com.edutrack.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Examination & Reporting Management (Member 06 - IT25103998).
 * BR-EXM-01 marks entry restricted to the assigned subject teacher;
 * BR-EXM-02 moderation before publication; BR-EXM-03 published report cards need
 * authorised re-publish. Grades auto-calculated from configurable bands (PBI-22).
 */
public class ExaminationService {

    private final ExaminationDAO examDAO = new ExaminationDAOImpl();
    private final MarksDAO marksDAO = new MarksDAOImpl();
    private final GradeBandDAO gradeBandDAO = new GradeBandDAOImpl();
    private final ReportCardDAO reportCardDAO = new ReportCardDAOImpl();
    private final EnrolmentDAO enrolmentDAO = new EnrolmentDAOImpl();

    // --------------------------------------------------------------- examinations

    public Examination createExam(com.edutrack.model.User actor, Examination exam) {
        requireExamAuthority(actor);
        Validator v = new Validator();
        v.required(exam.getExamName(), "examName", "Exam name")
                .required(exam.getExamDate() == null ? null : "x", "examDate", "Exam date")
                .required(exam.getSubjectId() == null ? null : "x", "subjectId", "Subject")
                .required(exam.getClassId() == null ? null : "x", "classId", "Class");
        v.check();
        try (Connection conn = DB.getConnection()) {
            if (exam.getMaxMarks() == null || exam.getMaxMarks() <= 0) exam.setMaxMarks(100);
            exam.setStatus("SCHEDULED");
            if (exam.getTeacherId() == null && actor.getTeacherId() != null && actor.hasRole("TEACHER")) {
                exam.setTeacherId(actor.getTeacherId());
            }
            Examination saved = examDAO.insert(conn, exam);
            AuditLogger.log(conn, actor, "EXAM_CREATE", "EXAM", saved.getId(), null,
                    saved.getExamName() + " " + saved.getExamDate());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Exam creation failed due to a database error.", e);
        }
    }

    public Examination updateExam(com.edutrack.model.User actor, Examination changes) {
        requireExamAuthority(actor);
        Validator validation = new Validator();
        validation.required(changes.getExamName(), "examName", "Exam name");
        if (changes.getExamDate() == null || changes.getClassId() == null || changes.getSubjectId() == null
                || changes.getMaxMarks() == null || changes.getMaxMarks() <= 0)
            validation.add("exam", "Select the exam date, class, subject and positive maximum marks.");
        validation.check();
        try (Connection conn = DB.getConnection()) {
            Examination before = examDAO.findById(conn, changes.getId());
            if (before == null) throw new BusinessRuleException("Exam not found.");
            if ("ARCHIVED".equals(before.getStatus()) || "CANCELLED".equals(before.getStatus())) {
                throw new BusinessRuleException("Archived or cancelled exams cannot be edited.");
            }
            if (!marksDAO.findByExam(conn, changes.getId()).isEmpty()
                    && (!java.util.Objects.equals(before.getClassId(), changes.getClassId())
                    || !java.util.Objects.equals(before.getSubjectId(), changes.getSubjectId())
                    || !java.util.Objects.equals(before.getMaxMarks(), changes.getMaxMarks())))
                throw new BusinessRuleException("Class, subject and maximum marks are locked once marks exist.");
            changes.setStatus(before.getStatus());
            examDAO.update(conn, changes);
            AuditLogger.log(conn, actor, "EXAM_UPDATE", "EXAM", changes.getId(),
                    before.getExamName() + " " + before.getExamDate(),
                    changes.getExamName() + " " + changes.getExamDate());
            return examDAO.findById(conn, changes.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Exam update failed due to a database error.", e);
        }
    }

    public void transitionExam(com.edutrack.model.User actor, long examId, String action) {
        requireExamAuthority(actor);
        try (Connection conn = DB.getConnection()) {
            Examination before = examDAO.findById(conn, examId);
            if (before == null) throw new BusinessRuleException("Exam not found.");
            switch (action) {
                case "complete" -> examDAO.updateStatus(conn, examId, "COMPLETED");
                case "cancel" -> examDAO.updateStatus(conn, examId, "CANCELLED");
                case "archive" -> examDAO.updateStatus(conn, examId, "ARCHIVED");
                case "schedule" -> examDAO.updateStatus(conn, examId, "SCHEDULED");
                default -> throw new BusinessRuleException("Unknown exam action.");
            }
            AuditLogger.log(conn, actor, "EXAM_" + action.toUpperCase(), "EXAM", examId,
                    before.getStatus(), action.toUpperCase());
        } catch (SQLException e) {
            throw new RuntimeException("Exam transition failed due to a database error.", e);
        }
    }

    public Examination findExam(long id) {
        try (Connection conn = DB.getConnection()) {
            return examDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load exam.", e);
        }
    }

    public List<Examination> searchExams(String q, Long termId, Long classId, Long subjectId,
                                         String status, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return examDAO.search(conn, emptyToNull(q), termId, classId, subjectId, emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search exams.", e);
        }
    }

    public long countExams(String q, Long termId, Long classId, Long subjectId, String status) {
        try (Connection conn = DB.getConnection()) {
            return examDAO.countSearch(conn, emptyToNull(q), termId, classId, subjectId, emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not count exams.", e);
        }
    }

    public List<Examination> examsOfStudent(long studentId) {
        try (Connection conn = DB.getConnection()) {
            return examDAO.findByStudent(conn, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load exams.", e);
        }
    }

    // --------------------------------------------------------------- marks

    /** BR-EXM-01: only the teacher assigned to the exam's subject/class may enter marks. */
    public void enterMarks(com.edutrack.model.User actor, long examId, Map<Long, String> marksByStudent,
                           Map<Long, String> remarksByStudent) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Examination exam = examDAO.findById(conn, examId);
                if (exam == null) throw new BusinessRuleException("Exam not found.");
                if ("CANCELLED".equals(exam.getStatus()) || "ARCHIVED".equals(exam.getStatus())) {
                    throw new BusinessRuleException("Marks cannot be entered for a cancelled or archived exam.");
                }
                requireMarksAccess(actor, exam);

                GradeBand[] bands = gradeBandDAO.findAll(conn).toArray(new GradeBand[0]);
                int saved = 0;
                for (Map.Entry<Long, String> entry : marksByStudent.entrySet()) {
                    long studentId = entry.getKey();
                    String raw = entry.getValue() == null ? "" : entry.getValue().trim();
                    if (raw.isEmpty()) continue; // blank = not entered yet

                    BigDecimal mark;
                    try {
                        mark = new BigDecimal(raw);
                    } catch (NumberFormatException nfe) {
                        throw new BusinessRuleException("Mark for student #" + studentId + " is not a number.");
                    }
                    if (mark.compareTo(BigDecimal.ZERO) < 0
                            || mark.compareTo(BigDecimal.valueOf(exam.getMaxMarks())) > 0) {
                        throw new BusinessRuleException("Mark for student #" + studentId
                                + " must be between 0 and " + exam.getMaxMarks() + ".");
                    }
                    // Integration: marks belong to actively enrolled students.
                    Enrolment enrolment = enrolmentDAO.findActive(conn, studentId, exam.getClassId());
                    if (enrolment == null) {
                        throw new BusinessRuleException("Student #" + studentId + " is not enrolled in the exam's class.");
                    }
                    Marks existing = marksDAO.findByExamAndStudent(conn, examId, studentId);
                    if (existing != null && !"DRAFT".equals(existing.getStatus())) {
                        throw new BusinessRuleException("Marks for student #" + studentId
                                + " were already submitted and locked. Use moderation to change them.");
                    }
                    Marks m = new Marks();
                    m.setExamId(examId);
                    m.setStudentId(studentId);
                    m.setMarks(mark);
                    m.setMaxMarks(BigDecimal.valueOf(exam.getMaxMarks()));
                    m.setGrade(calculateGrade(bands, mark.multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(exam.getMaxMarks()), 2, RoundingMode.HALF_UP)));
                    m.setRemarks(remarksByStudent == null ? null : remarksByStudent.get(studentId));
                    m.setStatus("DRAFT");
                    m.setEnteredBy(actor.getId());
                    marksDAO.upsert(conn, m);
                    saved++;
                }
                AuditLogger.log(conn, actor, "MARKS_ENTER", "EXAM", examId, null, saved + " marks entered");
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Marks entry failed due to a database error.", e);
        }
    }

    /** Locks drafts and routes them to the exam coordinator (PBI-21). */
    public void submitForModeration(com.edutrack.model.User actor, long examId) {
        try (Connection conn = DB.getConnection()) {
            Examination exam = examDAO.findById(conn, examId);
            if (exam == null) throw new BusinessRuleException("Exam not found.");
            requireMarksAccess(actor, exam);
            List<Marks> all = marksDAO.findByExam(conn, examId);
            int n = 0;
            for (Marks m : all) {
                if ("DRAFT".equals(m.getStatus())) {
                    marksDAO.updateStatus(conn, m.getId(), Marks.SUBMITTED);
                    n++;
                }
            }
            if (n == 0) throw new BusinessRuleException("No draft marks to submit.");
            AuditLogger.log(conn, actor, "MARKS_SUBMIT", "EXAM", examId, null, n + " marks submitted");
        } catch (SQLException e) {
            throw new RuntimeException("Marks submission failed due to a database error.", e);
        }
    }

    /** BR-EXM-02: exam coordinator moderates. approve|reject. */
    public void moderate(com.edutrack.model.User actor, long marksId, String decision, String note) {
        if (!(actor.hasRole("PRINCIPAL") || actor.hasRole("ADMIN") || actor.hasRole("ACADEMIC_COORDINATOR"))) {
            throw new AuthorizationException("Only the exam coordinator can moderate marks.");
        }
        if (!"approve".equals(decision) && !"reject".equals(decision))
            throw new BusinessRuleException("Unknown moderation decision.");
        Validator v = new Validator();
        v.required(note, "note", "Moderation note");
        v.check();
        try (Connection conn = DB.getConnection()) {
            Marks m = marksDAO.findById(conn, marksId);
            if (m == null) throw new BusinessRuleException("Marks record not found.");
            if (!Marks.SUBMITTED.equals(m.getStatus())) {
                throw new BusinessRuleException("Only submitted marks can be moderated.");
            }
            String newStatus = "approve".equals(decision) ? Marks.MODERATED : Marks.DRAFT;
            marksDAO.moderate(conn, marksId, newStatus, note, actor.getId());
            AuditLogger.log(conn, actor, "MARKS_MODERATE", "MARKS", marksId,
                    m.getStatus() + " " + m.getMarks(), newStatus + " note=" + note);
        } catch (SQLException e) {
            throw new RuntimeException("Moderation failed due to a database error.", e);
        }
    }

    /** BR-EXM-02: publication happens only after moderation, by authorised staff. */
    public void publishResults(com.edutrack.model.User actor, long examId) {
        if (!(actor.hasRole("PRINCIPAL") || actor.hasRole("ADMIN"))) {
            throw new AuthorizationException("Only the Principal can publish results.");
        }
        try (Connection conn = DB.getConnection()) {
            List<Marks> all = marksDAO.findByExam(conn, examId);
            int n = 0;
            for (Marks m : all) {
                if (Marks.MODERATED.equals(m.getStatus())) {
                    marksDAO.markPublished(conn, m.getId());
                    n++;
                }
            }
            if (n == 0) throw new BusinessRuleException("No moderated marks ready to publish.");
            AuditLogger.log(conn, actor, "MARKS_PUBLISH", "EXAM", examId, null, n + " marks published");
        } catch (SQLException e) {
            throw new RuntimeException("Publication failed due to a database error.", e);
        }
    }

    public List<Marks> marksOfExam(long examId) {
        try (Connection conn = DB.getConnection()) {
            return marksDAO.findByExam(conn, examId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load marks.", e);
        }
    }

    public List<Marks> marksOfStudent(long studentId) {
        try (Connection conn = DB.getConnection()) {
            return marksDAO.findByStudent(conn, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load marks.", e);
        }
    }

    public Marks findMarks(long id) {
        try (Connection conn = DB.getConnection()) {
            return marksDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load marks.", e);
        }
    }

    // --------------------------------------------------------------- grades & report cards

    public String calculateGrade(GradeBand[] bands, BigDecimal mark) {
        int value = mark.setScale(0, RoundingMode.HALF_UP).intValue();
        for (GradeBand band : bands) {
            if (value >= band.getMinMark() && value <= band.getMaxMark()) {
                return band.getGrade();
            }
        }
        return "N/A";
    }

    /** PBI-22: auto grade + report card generation per student per term. */
    public ReportCard generateReportCard(com.edutrack.model.User actor, long studentId, long termId) {
        if (!(actor.hasRole("PRINCIPAL") || actor.hasRole("ADMIN") || actor.hasRole("ACADEMIC_COORDINATOR"))) {
            throw new AuthorizationException("Only academic staff can generate report cards.");
        }
        try (Connection conn = DB.getConnection()) {
            List<Marks> published = marksDAO.search(conn, null, studentId, Marks.PUBLISHED, 0, 500);
            List<Marks> termMarks = new ArrayList<>();
            for (Marks m : published) {
                Examination exam = examDAO.findById(conn, m.getExamId());
                if (exam != null && exam.getTermId() != null && exam.getTermId() == termId) {
                    termMarks.add(m);
                }
            }
            if (termMarks.isEmpty()) {
                throw new BusinessRuleException("No published marks found for this student in the selected term.");
            }
            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal maxSum = BigDecimal.ZERO;
            for (Marks m : termMarks) {
                sum = sum.add(m.getMarks());
                maxSum = maxSum.add(m.getMaxMarks() == null ? BigDecimal.valueOf(100) : m.getMaxMarks());
            }
            BigDecimal percent = sum.multiply(BigDecimal.valueOf(100)).divide(maxSum, 2, RoundingMode.HALF_UP);
            GradeBand[] bands = gradeBandDAO.findAll(conn).toArray(new GradeBand[0]);

            ReportCard card = new ReportCard();
            card.setStudentId(studentId);
            card.setTermId(termId);
            card.setAverageMarks(percent);
            card.setOverallGrade(calculateGrade(bands, percent));
            card.setStatus("GENERATED");
            ReportCard saved = reportCardDAO.upsert(conn, card);
            AuditLogger.log(conn, actor, "REPORTCARD_GENERATE", "REPORT_CARD", saved.getId(), null,
                    "avg=" + percent + " grade=" + saved.getOverallGrade());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Report card generation failed due to a database error.", e);
        }
    }

    /** BR-EXM-03: publishing is authoritative and audited; published cards change only via re-publish. */
    public void publishReportCard(com.edutrack.model.User actor, long reportCardId) {
        if (!(actor.hasRole("PRINCIPAL") || actor.hasRole("ADMIN"))) {
            throw new AuthorizationException("Only the Principal can publish report cards.");
        }
        try (Connection conn = DB.getConnection()) {
            ReportCard card = reportCardDAO.findById(conn, reportCardId);
            if (card == null) throw new BusinessRuleException("Report card not found.");
            reportCardDAO.publish(conn, reportCardId);
            AuditLogger.log(conn, actor, "REPORTCARD_PUBLISH", "REPORT_CARD", reportCardId,
                    card.getStatus(), "PUBLISHED");
        } catch (SQLException e) {
            throw new RuntimeException("Report card publication failed due to a database error.", e);
        }
    }

    public List<ReportCard> searchReportCards(Long termId, Long classId, String status, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return reportCardDAO.search(conn, termId, classId, emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search report cards.", e);
        }
    }

    public ReportCard findReportCard(long id) {
        try (Connection conn = DB.getConnection()) {
            return reportCardDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load report card.", e);
        }
    }

    public List<GradeBand> gradeBands() {
        try (Connection conn = DB.getConnection()) {
            return gradeBandDAO.findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load grade bands.", e);
        }
    }

    /** Performance analytics by class/subject: avg marks, pass rate, count. */
    public List<Map<String, Object>> examPerformance(Long examId) {
        try (Connection conn = DB.getConnection()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            String sql = "SELECT sub.name AS subject, AVG(m.marks) AS avg_mark, " +
                    "SUM(CASE WHEN m.marks >= 50 THEN 1 ELSE 0 END) AS passes, COUNT(*) AS total " +
                    "FROM marks m JOIN examinations x ON x.id = m.exam_id " +
                    "LEFT JOIN subjects sub ON sub.id = x.subject_id " +
                    "WHERE m.status IN ('MODERATED','PUBLISHED')" +
                    (examId == null ? "" : " AND m.exam_id = ?") +
                    " GROUP BY sub.name";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (examId != null) ps.setLong(1, examId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("subject", rs.getString("subject"));
                        row.put("avgMark", rs.getBigDecimal("avg_mark"));
                        row.put("passes", rs.getLong("passes"));
                        row.put("total", rs.getLong("total"));
                        double passRate = rs.getLong("total") == 0 ? 0
                                : 100.0 * rs.getLong("passes") / rs.getLong("total");
                        row.put("passRate", Math.round(passRate * 10) / 10.0);
                        rows.add(row);
                    }
                }
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("Performance analytics failed due to a database error.", e);
        }
    }

    // --------------------------------------------------------------- helpers

    /** BR-EXM-01 enforcement: assigned subject teacher, or coordinators for setup. */
    public void requireMarksAccess(com.edutrack.model.User actor, Examination exam) {
        if (actor == null) throw new AuthorizationException("Not authenticated.");
        if (actor.hasRole("ADMIN")) return;
        if (actor.hasRole("TEACHER") && actor.getTeacherId() != null
                && java.util.Objects.equals(actor.getTeacherId(), exam.getTeacherId())) {
            return;
        }
        throw new AuthorizationException("Marks entry is restricted to the assigned subject teacher (BR-EXM-01).");
    }

    private void requireExamAuthority(com.edutrack.model.User actor) {
        if (actor == null) throw new AuthorizationException("Not authenticated.");
        if (!(actor.hasRole("ADMIN") || actor.hasRole("ACADEMIC_COORDINATOR")
                || actor.hasRole("PRINCIPAL") || actor.hasRole("TEACHER"))) {
            throw new AuthorizationException("You are not authorised to manage examinations.");
        }
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
