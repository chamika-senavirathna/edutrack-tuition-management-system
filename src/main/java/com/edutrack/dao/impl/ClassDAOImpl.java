package com.edutrack.dao.impl;

import com.edutrack.dao.ClassDAO;
import com.edutrack.model.ClassRoom;

import java.sql.Connection;
import java.sql.Date;
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

/** JDBC implementation of ClassDAO (Member 02). */
public class ClassDAOImpl implements ClassDAO {

    private static final String BASE_SELECT =
            "SELECT c.*, sub.name AS subject_name, t.full_name AS teacher_name, r.name AS room_name, " +
            "(SELECT COUNT(*) FROM enrolments e WHERE e.class_id = c.id AND e.status = 'ACTIVE') AS enrolled_count " +
            "FROM classes c " +
            "LEFT JOIN subjects sub ON sub.id = c.subject_id " +
            "LEFT JOIN teachers t ON t.id = c.teacher_id " +
            "LEFT JOIN rooms r ON r.id = c.room_id ";

    private ClassRoom mapRow(ResultSet rs) throws SQLException {
        ClassRoom c = new ClassRoom();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        long subjectId = rs.getLong("subject_id");
        c.setSubjectId(rs.wasNull() ? null : subjectId);
        long teacherId = rs.getLong("teacher_id");
        c.setTeacherId(rs.wasNull() ? null : teacherId);
        long roomId = rs.getLong("room_id");
        c.setRoomId(rs.wasNull() ? null : roomId);
        c.setCapacity(rs.getInt("capacity"));
        Date start = rs.getDate("start_date");
        c.setStartDate(start == null ? null : start.toLocalDate());
        Date end = rs.getDate("end_date");
        c.setEndDate(end == null ? null : end.toLocalDate());
        c.setStatus(rs.getString("status"));
        c.setDescription(rs.getString("description"));
        c.setCreatedAt(getDateTime(rs, "created_at"));
        c.setSubjectName(rs.getString("subject_name"));
        c.setTeacherName(rs.getString("teacher_name"));
        c.setRoomName(rs.getString("room_name"));
        c.setEnrolledCount(rs.getInt("enrolled_count"));
        return c;
    }

    @Override
    public ClassRoom insert(Connection conn, ClassRoom c) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO classes (name, subject_id, teacher_id, room_id, capacity, start_date, end_date, status, description, created_at) VALUES (?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setString(1, c.getName());
                    setNullableLong(ps, 2, c.getSubjectId());
                    setNullableLong(ps, 3, c.getTeacherId());
                    setNullableLong(ps, 4, c.getRoomId());
                    ps.setInt(5, c.getCapacity());
                    setNullableDate(ps, 6, c.getStartDate());
                    setNullableDate(ps, 7, c.getEndDate());
                    ps.setString(8, c.getStatus() == null ? "ACTIVE" : c.getStatus());
                    setNullableString(ps, 9, c.getDescription());
                });
        c.setId(id);
        return c;
    }

    @Override
    public void update(Connection conn, ClassRoom c) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE classes SET name=?, subject_id=?, teacher_id=?, room_id=?, capacity=?, start_date=?, end_date=?, status=?, description=? WHERE id=?")) {
            ps.setString(1, c.getName());
            setNullableLong(ps, 2, c.getSubjectId());
            setNullableLong(ps, 3, c.getTeacherId());
            setNullableLong(ps, 4, c.getRoomId());
            ps.setInt(5, c.getCapacity());
            setNullableDate(ps, 6, c.getStartDate());
            setNullableDate(ps, 7, c.getEndDate());
            ps.setString(8, c.getStatus());
            setNullableString(ps, 9, c.getDescription());
            ps.setLong(10, c.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public ClassRoom findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE c.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public List<ClassRoom> search(Connection conn, String q, Long subjectId, Long teacherId,
                                  String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND c.name LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (subjectId != null) { sql.append("AND c.subject_id = ? "); params.add(subjectId); }
        if (teacherId != null) { sql.append("AND c.teacher_id = ? "); params.add(teacherId); }
        if (status != null && !status.isBlank()) { sql.append("AND c.status = ? "); params.add(status); }
        sql.append("ORDER BY c.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q, Long subjectId, Long teacherId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM classes c WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND c.name LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (subjectId != null) { sql.append("AND c.subject_id = ? "); params.add(subjectId); }
        if (teacherId != null) { sql.append("AND c.teacher_id = ? "); params.add(teacherId); }
        if (status != null && !status.isBlank()) { sql.append("AND c.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public List<ClassRoom> findByTeacher(Connection conn, long teacherId, String status) throws SQLException {
        String sql = BASE_SELECT + "WHERE c.teacher_id = ?" + (status == null || status.isBlank() ? "" : " AND c.status = ?") + " ORDER BY c.name";
        return queryList(conn, sql, ps -> {
            ps.setLong(1, teacherId);
            if (status != null && !status.isBlank()) ps.setString(2, status);
        }, this::mapRow);
    }

    @Override
    public List<ClassRoom> findByStudent(Connection conn, long studentId, String status) throws SQLException {
        String sql = BASE_SELECT + "JOIN enrolments e ON e.class_id = c.id AND e.status = 'ACTIVE' AND e.student_id = ?" +
                (status == null || status.isBlank() ? "" : " AND c.status = ?") + " ORDER BY c.name";
        return queryList(conn, sql, ps -> {
            ps.setLong(1, studentId);
            if (status != null && !status.isBlank()) ps.setString(2, status);
        }, this::mapRow);
    }

    @Override
    public void updateStatus(Connection conn, long id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE classes SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public long countAll(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM classes", null, 0);
    }

    @Override
    public long countActive(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM classes WHERE status = 'ACTIVE'", null, 0);
    }

    private void setNullableDate(PreparedStatement ps, int index, java.time.LocalDate date) throws SQLException {
        if (date == null) ps.setNull(index, java.sql.Types.DATE);
        else ps.setDate(index, Date.valueOf(date));
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.BIGINT);
        else ps.setLong(index, value);
    }
}
