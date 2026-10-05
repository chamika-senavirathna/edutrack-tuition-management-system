package com.edutrack.dao.impl;

import com.edutrack.dao.MarksDAO;
import com.edutrack.model.Marks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.getDateTime;
import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryLong;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;
import static com.edutrack.dao.impl.JdbcHelper.setNullableString;

/** JDBC implementation of MarksDAO (Member 06). */
public class MarksDAOImpl implements MarksDAO {

    private static final String BASE_SELECT =
            "SELECT m.*, s.first_name, s.last_name, s.reg_no, x.exam_name, sub.name AS subject_name, " +
            "c.name AS class_name, u1.full_name AS entered_by_name, u2.full_name AS moderated_by_name " +
            "FROM marks m " +
            "JOIN students s ON s.id = m.student_id " +
            "JOIN examinations x ON x.id = m.exam_id " +
            "LEFT JOIN subjects sub ON sub.id = x.subject_id " +
            "LEFT JOIN classes c ON c.id = x.class_id " +
            "LEFT JOIN users u1 ON u1.id = m.entered_by " +
            "LEFT JOIN users u2 ON u2.id = m.moderated_by ";

    private Marks mapRow(ResultSet rs) throws SQLException {
        Marks m = new Marks();
        m.setId(rs.getLong("id"));
        m.setExamId(rs.getLong("exam_id"));
        m.setStudentId(rs.getLong("student_id"));
        m.setMarks(rs.getBigDecimal("marks"));
        m.setMaxMarks(rs.getBigDecimal("max_marks"));
        m.setGrade(rs.getString("grade"));
        m.setRemarks(rs.getString("remarks"));
        m.setStatus(rs.getString("status"));
        m.setEnteredBy(rs.getLong("entered_by"));
        m.setEnteredAt(getDateTime(rs, "entered_at"));
        long mb = rs.getLong("moderated_by");
        m.setModeratedBy(rs.wasNull() ? null : mb);
        m.setModeratedAt(getDateTime(rs, "moderated_at"));
        m.setModerationNote(rs.getString("moderation_note"));
        m.setPublishedAt(getDateTime(rs, "published_at"));
        m.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        m.setStudentRegNo(rs.getString("reg_no"));
        m.setExamName(rs.getString("exam_name"));
        m.setSubjectName(rs.getString("subject_name"));
        m.setClassName(rs.getString("class_name"));
        m.setEnteredByName(rs.getString("entered_by_name"));
        m.setModeratedByName(rs.getString("moderated_by_name"));
        return m;
    }

    @Override
    public Marks upsert(Connection conn, Marks m) throws SQLException {
        Marks existing = findByExamAndStudent(conn, m.getExamId(), m.getStudentId());
        if (existing != null) {
            m.setId(existing.getId());
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE marks SET marks=?, max_marks=?, grade=?, remarks=?, status=?, entered_by=?, entered_at=CURRENT_TIMESTAMP WHERE id=?")) {
                ps.setBigDecimal(1, m.getMarks());
                ps.setBigDecimal(2, m.getMaxMarks());
                ps.setString(3, m.getGrade());
                setNullableString(ps, 4, m.getRemarks());
                ps.setString(5, m.getStatus());
                ps.setLong(6, m.getEnteredBy());
                ps.setLong(7, m.getId());
                ps.executeUpdate();
            }
            return m;
        }
        long id = insertAndGetKey(conn,
                "INSERT INTO marks (exam_id, student_id, marks, max_marks, grade, remarks, status, entered_by, entered_at) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setLong(1, m.getExamId());
                    ps.setLong(2, m.getStudentId());
                    ps.setBigDecimal(3, m.getMarks());
                    ps.setBigDecimal(4, m.getMaxMarks());
                    ps.setString(5, m.getGrade());
                    setNullableString(ps, 6, m.getRemarks());
                    ps.setString(7, m.getStatus());
                    ps.setLong(8, m.getEnteredBy());
                });
        m.setId(id);
        return m;
    }

    @Override
    public Marks findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE m.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Marks findByExamAndStudent(Connection conn, long examId, long studentId) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE m.exam_id = ? AND m.student_id = ?",
                ps -> {
                    ps.setLong(1, examId);
                    ps.setLong(2, studentId);
                }, this::mapRow);
    }

    @Override
    public List<Marks> findByExam(Connection conn, long examId) throws SQLException {
        return queryList(conn, BASE_SELECT + "WHERE m.exam_id = ? ORDER BY s.first_name, s.last_name",
                ps -> ps.setLong(1, examId), this::mapRow);
    }

    @Override
    public List<Marks> findByStudent(Connection conn, long studentId) throws SQLException {
        return queryList(conn, BASE_SELECT + "WHERE m.student_id = ? ORDER BY m.id DESC",
                ps -> ps.setLong(1, studentId), this::mapRow);
    }

    @Override
    public List<Marks> search(Connection conn, Long examId, Long studentId, String status,
                              int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (examId != null) { sql.append("AND m.exam_id = ? "); params.add(examId); }
        if (studentId != null) { sql.append("AND m.student_id = ? "); params.add(studentId); }
        if (status != null && !status.isBlank()) { sql.append("AND m.status = ? "); params.add(status); }
        sql.append("ORDER BY m.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, Long examId, Long studentId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM marks m WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (examId != null) { sql.append("AND m.exam_id = ? "); params.add(examId); }
        if (studentId != null) { sql.append("AND m.student_id = ? "); params.add(studentId); }
        if (status != null && !status.isBlank()) { sql.append("AND m.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public void updateStatus(Connection conn, long id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE marks SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void moderate(Connection conn, long id, String decision, String note, long moderatedBy) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE marks SET status=?, moderation_note=?, moderated_by=?, moderated_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setString(1, decision);
            ps.setString(2, note);
            ps.setLong(3, moderatedBy);
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void markPublished(Connection conn, long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE marks SET status='PUBLISHED', published_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public long countByStatus(Connection conn, String status) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM marks WHERE status = ?",
                ps -> ps.setString(1, status), 0);
    }
}
