package com.edutrack.dao.impl;

import com.edutrack.dao.ReportCardDAO;
import com.edutrack.model.ReportCard;

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

/** JDBC implementation of ReportCardDAO (Member 06). */
public class ReportCardDAOImpl implements ReportCardDAO {

    private static final String BASE_SELECT =
            "SELECT rc.*, s.first_name, s.last_name, s.reg_no, c.name AS class_name, t.name AS term_name " +
            "FROM report_cards rc " +
            "JOIN students s ON s.id = rc.student_id " +
            "LEFT JOIN terms t ON t.id = rc.term_id " +
            "LEFT JOIN enrolments e ON e.student_id = rc.student_id AND e.status = 'ACTIVE' " +
            "LEFT JOIN classes c ON c.id = e.class_id ";

    private ReportCard mapRow(ResultSet rs) throws SQLException {
        ReportCard rc = new ReportCard();
        rc.setId(rs.getLong("id"));
        rc.setStudentId(rs.getLong("student_id"));
        long tid = rs.getLong("term_id");
        rc.setTermId(rs.wasNull() ? null : tid);
        rc.setAverageMarks(rs.getBigDecimal("average_marks"));
        rc.setOverallGrade(rs.getString("overall_grade"));
        int ap = rs.getInt("attendance_percent");
        rc.setAttendancePercent(rs.wasNull() ? null : ap);
        rc.setStatus(rs.getString("status"));
        rc.setPublishedAt(getDateTime(rs, "published_at"));
        rc.setGeneratedAt(getDateTime(rs, "generated_at"));
        rc.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        rc.setStudentRegNo(rs.getString("reg_no"));
        rc.setClassName(rs.getString("class_name"));
        rc.setTermName(rs.getString("term_name"));
        return rc;
    }

    @Override
    public ReportCard upsert(Connection conn, ReportCard rc) throws SQLException {
        ReportCard existing = findByStudentAndTerm(conn, rc.getStudentId(), rc.getTermId());
        if (existing != null) {
            rc.setId(existing.getId());
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE report_cards SET average_marks=?, overall_grade=?, attendance_percent=?, status=?, generated_at=CURRENT_TIMESTAMP WHERE id=?")) {
                ps.setBigDecimal(1, rc.getAverageMarks());
                ps.setString(2, rc.getOverallGrade());
                if (rc.getAttendancePercent() == null) ps.setNull(3, java.sql.Types.INTEGER);
                else ps.setInt(3, rc.getAttendancePercent());
                ps.setString(4, rc.getStatus());
                ps.setLong(5, rc.getId());
                ps.executeUpdate();
            }
            return rc;
        }
        long id = insertAndGetKey(conn,
                "INSERT INTO report_cards (student_id, term_id, average_marks, overall_grade, attendance_percent, status, generated_at) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setLong(1, rc.getStudentId());
                    ps.setLong(2, rc.getTermId());
                    ps.setBigDecimal(3, rc.getAverageMarks());
                    ps.setString(4, rc.getOverallGrade());
                    if (rc.getAttendancePercent() == null) ps.setNull(5, java.sql.Types.INTEGER);
                    else ps.setInt(5, rc.getAttendancePercent());
                    ps.setString(6, rc.getStatus() == null ? "GENERATED" : rc.getStatus());
                });
        rc.setId(id);
        return rc;
    }

    @Override
    public ReportCard findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE rc.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public ReportCard findByStudentAndTerm(Connection conn, long studentId, long termId) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE rc.student_id = ? AND rc.term_id = ?",
                ps -> {
                    ps.setLong(1, studentId);
                    ps.setLong(2, termId);
                }, this::mapRow);
    }

    @Override
    public List<ReportCard> search(Connection conn, Long termId, Long classId, String status,
                                   int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (termId != null) { sql.append("AND rc.term_id = ? "); params.add(termId); }
        if (classId != null) { sql.append("AND e.class_id = ? "); params.add(classId); }
        if (status != null && !status.isBlank()) { sql.append("AND rc.status = ? "); params.add(status); }
        sql.append("GROUP BY rc.id, s.first_name, s.last_name, s.reg_no, c.name, t.name ORDER BY rc.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, Long termId, Long classId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT rc.id) FROM report_cards rc " +
                "LEFT JOIN enrolments e ON e.student_id = rc.student_id AND e.status = 'ACTIVE' WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (termId != null) { sql.append("AND rc.term_id = ? "); params.add(termId); }
        if (classId != null) { sql.append("AND e.class_id = ? "); params.add(classId); }
        if (status != null && !status.isBlank()) { sql.append("AND rc.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public void publish(Connection conn, long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE report_cards SET status='PUBLISHED', published_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
