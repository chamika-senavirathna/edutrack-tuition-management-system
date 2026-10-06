package com.edutrack.dao.impl;

import com.edutrack.dao.TimetableDAO;
import com.edutrack.model.TimetableSlot;

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

/** JDBC implementation of TimetableDAO (Member 02). */
public class TimetableDAOImpl implements TimetableDAO {

    private static final String BASE_SELECT =
            "SELECT t.*, c.name AS class_name, sub.name AS subject_name, te.full_name AS teacher_name, r.name AS room_name " +
            "FROM timetable t " +
            "JOIN classes c ON c.id = t.class_id " +
            "LEFT JOIN subjects sub ON sub.id = t.subject_id " +
            "LEFT JOIN teachers te ON te.id = t.teacher_id " +
            "LEFT JOIN rooms r ON r.id = t.room_id ";

    private TimetableSlot mapRow(ResultSet rs) throws SQLException {
        TimetableSlot t = new TimetableSlot();
        t.setId(rs.getLong("id"));
        t.setClassId(rs.getLong("class_id"));
        long sid = rs.getLong("subject_id");
        t.setSubjectId(rs.wasNull() ? null : sid);
        long tid = rs.getLong("teacher_id");
        t.setTeacherId(rs.wasNull() ? null : tid);
        long rid = rs.getLong("room_id");
        t.setRoomId(rs.wasNull() ? null : rid);
        t.setDayOfWeek(rs.getString("day_of_week"));
        t.setStartTime(rs.getString("start_time"));
        t.setEndTime(rs.getString("end_time"));
        t.setStatus(rs.getString("status"));
        t.setPublishedAt(getDateTime(rs, "published_at"));
        t.setClassName(rs.getString("class_name"));
        t.setSubjectName(rs.getString("subject_name"));
        t.setTeacherName(rs.getString("teacher_name"));
        t.setRoomName(rs.getString("room_name"));
        return t;
    }

    @Override
    public TimetableSlot insert(Connection conn, TimetableSlot t) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO timetable (class_id, subject_id, teacher_id, room_id, day_of_week, start_time, end_time, status, published_at) VALUES (?,?,?,?,?,?,?,?,?)",
                ps -> {
                    ps.setLong(1, t.getClassId());
                    setNullableLong(ps, 2, t.getSubjectId());
                    setNullableLong(ps, 3, t.getTeacherId());
                    setNullableLong(ps, 4, t.getRoomId());
                    ps.setString(5, t.getDayOfWeek());
                    ps.setString(6, t.getStartTime());
                    ps.setString(7, t.getEndTime());
                    ps.setString(8, t.getStatus() == null ? "DRAFT" : t.getStatus());
                    if (t.getPublishedAt() == null) ps.setNull(9, java.sql.Types.TIMESTAMP);
                    else ps.setTimestamp(9, java.sql.Timestamp.valueOf(t.getPublishedAt()));
                });
        t.setId(id);
        return t;
    }

    @Override
    public void update(Connection conn, TimetableSlot t) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE timetable SET class_id=?, subject_id=?, teacher_id=?, room_id=?, day_of_week=?, start_time=?, end_time=?, status=? WHERE id=?")) {
            ps.setLong(1, t.getClassId());
            setNullableLong(ps, 2, t.getSubjectId());
            setNullableLong(ps, 3, t.getTeacherId());
            setNullableLong(ps, 4, t.getRoomId());
            ps.setString(5, t.getDayOfWeek());
            ps.setString(6, t.getStartTime());
            ps.setString(7, t.getEndTime());
            ps.setString(8, t.getStatus());
            ps.setLong(9, t.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public TimetableSlot findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE t.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public List<TimetableSlot> findByClass(Connection conn, long classId, String status) throws SQLException {
        String sql = BASE_SELECT + "WHERE t.class_id = ?" + (status == null || status.isBlank() ? "" : " AND t.status = ?") +
                " ORDER BY CASE t.day_of_week WHEN 'MONDAY' THEN 1 WHEN 'TUESDAY' THEN 2 WHEN 'WEDNESDAY' THEN 3 WHEN 'THURSDAY' THEN 4 WHEN 'FRIDAY' THEN 5 WHEN 'SATURDAY' THEN 6 ELSE 7 END, t.start_time";
        return queryList(conn, sql, ps -> {
            ps.setLong(1, classId);
            if (status != null && !status.isBlank()) ps.setString(2, status);
        }, this::mapRow);
    }

    @Override
    public List<TimetableSlot> findByTeacher(Connection conn, long teacherId, String status) throws SQLException {
        String sql = BASE_SELECT + "WHERE t.teacher_id = ?" + (status == null || status.isBlank() ? "" : " AND t.status = ?") +
                " ORDER BY CASE t.day_of_week WHEN 'MONDAY' THEN 1 WHEN 'TUESDAY' THEN 2 WHEN 'WEDNESDAY' THEN 3 WHEN 'THURSDAY' THEN 4 WHEN 'FRIDAY' THEN 5 WHEN 'SATURDAY' THEN 6 ELSE 7 END, t.start_time";
        return queryList(conn, sql, ps -> {
            ps.setLong(1, teacherId);
            if (status != null && !status.isBlank()) ps.setString(2, status);
        }, this::mapRow);
    }

    @Override
    public List<TimetableSlot> findPublishedByClasses(Connection conn, List<Long> classIds) throws SQLException {
        if (classIds == null || classIds.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < classIds.size(); i++) {
            in.append(i == 0 ? "?" : ",?");
        }
        String sql = BASE_SELECT + "WHERE t.class_id IN (" + in + ") AND t.status = 'PUBLISHED' " +
                "ORDER BY CASE t.day_of_week WHEN 'MONDAY' THEN 1 WHEN 'TUESDAY' THEN 2 WHEN 'WEDNESDAY' THEN 3 WHEN 'THURSDAY' THEN 4 WHEN 'FRIDAY' THEN 5 WHEN 'SATURDAY' THEN 6 ELSE 7 END, t.start_time";
        return queryList(conn, sql, ps -> {
            for (int i = 0; i < classIds.size(); i++) ps.setLong(i + 1, classIds.get(i));
        }, this::mapRow);
    }

    @Override
    public List<TimetableSlot> findConflicts(Connection conn, Long excludeSlotId, String dayOfWeek,
                                             String startTime, String endTime, Long teacherId,
                                             Long roomId, Long classId) throws SQLException {
        // BR-TTC-01: overlap on time AND match on teacher OR room OR class.
        // Only slots still inside the approval workflow (not ARCHIVED) are considered.
        StringBuilder sql = new StringBuilder(BASE_SELECT +
                "WHERE t.day_of_week = ? AND t.status <> 'ARCHIVED' " +
                "AND t.start_time < ? AND t.end_time > ? ");
        List<Object> params = new ArrayList<>();
        params.add(dayOfWeek);
        params.add(endTime);
        params.add(startTime);
        if (excludeSlotId != null) {
            sql.append("AND t.id <> ? ");
            params.add(excludeSlotId);
        }
        sql.append("AND (t.teacher_id = ? OR t.room_id = ? OR t.class_id = ?) ");
        params.add(teacherId);
        params.add(roomId);
        params.add(classId);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public List<TimetableSlot> search(Connection conn, Long classId, Long teacherId, String day,
                                      String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (classId != null) { sql.append("AND t.class_id = ? "); params.add(classId); }
        if (teacherId != null) { sql.append("AND t.teacher_id = ? "); params.add(teacherId); }
        if (day != null && !day.isBlank()) { sql.append("AND t.day_of_week = ? "); params.add(day); }
        if (status != null && !status.isBlank()) { sql.append("AND t.status = ? "); params.add(status); }
        sql.append("ORDER BY t.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, Long classId, Long teacherId, String day, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM timetable t WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (classId != null) { sql.append("AND t.class_id = ? "); params.add(classId); }
        if (teacherId != null) { sql.append("AND t.teacher_id = ? "); params.add(teacherId); }
        if (day != null && !day.isBlank()) { sql.append("AND t.day_of_week = ? "); params.add(day); }
        if (status != null && !status.isBlank()) { sql.append("AND t.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public void updateStatus(Connection conn, long id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE timetable SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void markPublished(Connection conn, long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE timetable SET status='PUBLISHED', published_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.BIGINT);
        else ps.setLong(index, value);
    }
}
