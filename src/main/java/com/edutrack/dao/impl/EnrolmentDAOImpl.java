package com.edutrack.dao.impl;

import com.edutrack.dao.EnrolmentDAO;
import com.edutrack.model.Enrolment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryLong;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;

/** JDBC implementation of EnrolmentDAO. */
public class EnrolmentDAOImpl implements EnrolmentDAO {

    private static final String BASE_SELECT =
            "SELECT e.*, s.first_name, s.last_name, s.reg_no, c.name AS class_name, sub.name AS subject_name " +
            "FROM enrolments e " +
            "JOIN students s ON s.id = e.student_id " +
            "JOIN classes c ON c.id = e.class_id " +
            "LEFT JOIN subjects sub ON sub.id = c.subject_id ";

    private Enrolment mapRow(ResultSet rs) throws SQLException {
        Enrolment e = new Enrolment();
        e.setId(rs.getLong("id"));
        e.setStudentId(rs.getLong("student_id"));
        e.setClassId(rs.getLong("class_id"));
        Date d = rs.getDate("enrolled_date");
        e.setEnrolledDate(d == null ? null : d.toLocalDate());
        e.setStatus(rs.getString("status"));
        e.setRemarks(rs.getString("remarks"));
        e.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        e.setStudentRegNo(rs.getString("reg_no"));
        e.setClassName(rs.getString("class_name"));
        e.setSubjectName(rs.getString("subject_name"));
        return e;
    }

    @Override
    public Enrolment insert(Connection conn, Enrolment e) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO enrolments (student_id, class_id, enrolled_date, status, remarks) VALUES (?,?,?,?,?)",
                ps -> {
                    ps.setLong(1, e.getStudentId());
                    ps.setLong(2, e.getClassId());
                    ps.setDate(3, e.getEnrolledDate() == null ? Date.valueOf(java.time.LocalDate.now()) : Date.valueOf(e.getEnrolledDate()));
                    ps.setString(4, e.getStatus() == null ? "ACTIVE" : e.getStatus());
                    ps.setString(5, e.getRemarks());
                });
        e.setId(id);
        return e;
    }

    @Override
    public void updateStatus(Connection conn, long id, String status, String remarks) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE enrolments SET status=?, remarks=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setString(2, remarks);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Enrolment findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE e.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Enrolment findActive(Connection conn, long studentId, long classId) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE e.student_id = ? AND e.class_id = ? AND e.status = 'ACTIVE'",
                ps -> {
                    ps.setLong(1, studentId);
                    ps.setLong(2, classId);
                }, this::mapRow);
    }

    @Override
    public List<Enrolment> findByStudent(Connection conn, long studentId) throws SQLException {
        return queryList(conn, BASE_SELECT + "WHERE e.student_id = ? ORDER BY e.enrolled_date DESC",
                ps -> ps.setLong(1, studentId), this::mapRow);
    }

    @Override
    public List<Enrolment> findByClass(Connection conn, long classId, String status) throws SQLException {
        String sql = BASE_SELECT + "WHERE e.class_id = ?" + (status == null || status.isBlank() ? "" : " AND e.status = ?") + " ORDER BY s.first_name";
        return queryList(conn, sql, ps -> {
            ps.setLong(1, classId);
            if (status != null && !status.isBlank()) ps.setString(2, status);
        }, this::mapRow);
    }

    @Override
    public long countActive(Connection conn, long classId) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM enrolments WHERE class_id = ? AND status = 'ACTIVE'",
                ps -> ps.setLong(1, classId), 0);
    }

    @Override
    public List<Enrolment> search(Connection conn, Long studentId, Long classId, String status,
                                  int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (studentId != null) { sql.append("AND e.student_id = ? "); params.add(studentId); }
        if (classId != null) { sql.append("AND e.class_id = ? "); params.add(classId); }
        if (status != null && !status.isBlank()) { sql.append("AND e.status = ? "); params.add(status); }
        sql.append("ORDER BY e.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, Long studentId, Long classId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM enrolments e WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (studentId != null) { sql.append("AND e.student_id = ? "); params.add(studentId); }
        if (classId != null) { sql.append("AND e.class_id = ? "); params.add(classId); }
        if (status != null && !status.isBlank()) { sql.append("AND e.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }
}
