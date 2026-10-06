package com.edutrack.dao.impl;

import com.edutrack.dao.AttendanceDAO;
import com.edutrack.model.Attendance;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.getDateTime;
import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryLong;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;
import static com.edutrack.dao.impl.JdbcHelper.setNullableString;

/** JDBC implementation of AttendanceDAO (Member 03). */
public class AttendanceDAOImpl implements AttendanceDAO {

    private static final String BASE_SELECT =
            "SELECT a.*, s.first_name, s.last_name, s.reg_no, c.name AS class_name, u.full_name AS marked_by_name " +
            "FROM attendance a " +
            "JOIN students s ON s.id = a.student_id " +
            "JOIN classes c ON c.id = a.class_id " +
            "LEFT JOIN users u ON u.id = a.marked_by ";

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getLong("id"));
        a.setStudentId(rs.getLong("student_id"));
        a.setClassId(rs.getLong("class_id"));
        long slot = rs.getLong("timetable_slot_id");
        a.setTimetableSlotId(rs.wasNull() ? null : slot);
        Date d = rs.getDate("attendance_date");
        a.setAttendanceDate(d == null ? null : d.toLocalDate());
        a.setStatus(rs.getString("status"));
        a.setReason(rs.getString("reason"));
        a.setMarkedBy(rs.getLong("marked_by"));
        a.setCorrectionNote(rs.getString("correction_note"));
        a.setCorrectedAt(getDateTime(rs, "corrected_at"));
        long cid = rs.getLong("corrected_by");
        a.setCorrectedBy(rs.wasNull() ? null : cid);
        a.setAdminOverride(rs.getBoolean("admin_override"));
        a.setCreatedAt(getDateTime(rs, "created_at"));
        a.setUpdatedAt(getDateTime(rs, "updated_at"));
        a.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        a.setStudentRegNo(rs.getString("reg_no"));
        a.setClassName(rs.getString("class_name"));
        a.setMarkedByName(rs.getString("marked_by_name"));
        return a;
    }

    @Override
    public Attendance insert(Connection conn, Attendance a) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO attendance (student_id, class_id, timetable_slot_id, attendance_date, status, reason, marked_by, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?)",
                ps -> {
                    ps.setLong(1, a.getStudentId());
                    ps.setLong(2, a.getClassId());
                    setNullableLong(ps, 3, a.getTimetableSlotId());
                    ps.setDate(4, Date.valueOf(a.getAttendanceDate()));
                    ps.setString(5, a.getStatus());
                    setNullableString(ps, 6, a.getReason());
                    ps.setLong(7, a.getMarkedBy());
                    ps.setTimestamp(8, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                    ps.setTimestamp(9, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                });
        a.setId(id);
        return a;
    }

    @Override
    public Attendance update(Connection conn, Attendance a) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE attendance SET status=?, reason=?, correction_note=?, corrected_at=?, corrected_by=?, admin_override=?, updated_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setString(1, a.getStatus());
            setNullableString(ps, 2, a.getReason());
            setNullableString(ps, 3, a.getCorrectionNote());
            if (a.getCorrectedAt() == null) ps.setNull(4, java.sql.Types.TIMESTAMP);
            else ps.setTimestamp(4, java.sql.Timestamp.valueOf(a.getCorrectedAt()));
            setNullableLong(ps, 5, a.getCorrectedBy());
            ps.setBoolean(6, a.getAdminOverride() != null && a.getAdminOverride());
            ps.setLong(7, a.getId());
            ps.executeUpdate();
        }
        return a;
    }

    @Override
    public Attendance findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE a.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Attendance findBySession(Connection conn, long studentId, long classId, LocalDate date) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE a.student_id = ? AND a.class_id = ? AND a.attendance_date = ?",
                ps -> {
                    ps.setLong(1, studentId);
                    ps.setLong(2, classId);
                    ps.setDate(3, Date.valueOf(date));
                }, this::mapRow);
    }

    @Override
    public List<Attendance> search(Connection conn, Long studentId, Long classId, LocalDate from,
                                   LocalDate to, String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (studentId != null) { sql.append("AND a.student_id = ? "); params.add(studentId); }
        if (classId != null) { sql.append("AND a.class_id = ? "); params.add(classId); }
        if (from != null) { sql.append("AND a.attendance_date >= ? "); params.add(Date.valueOf(from)); }
        if (to != null) { sql.append("AND a.attendance_date <= ? "); params.add(Date.valueOf(to)); }
        if (status != null && !status.isBlank()) { sql.append("AND a.status = ? "); params.add(status); }
        sql.append("ORDER BY a.attendance_date DESC, s.first_name LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, Long studentId, Long classId, LocalDate from,
                            LocalDate to, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM attendance a WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (studentId != null) { sql.append("AND a.student_id = ? "); params.add(studentId); }
        if (classId != null) { sql.append("AND a.class_id = ? "); params.add(classId); }
        if (from != null) { sql.append("AND a.attendance_date >= ? "); params.add(Date.valueOf(from)); }
        if (to != null) { sql.append("AND a.attendance_date <= ? "); params.add(Date.valueOf(to)); }
        if (status != null && !status.isBlank()) { sql.append("AND a.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public List<Attendance> findByStudentAndRange(Connection conn, long studentId, LocalDate from, LocalDate to) throws SQLException {
        return search(conn, studentId, null, from, to, null, 0, 1000);
    }

    @Override
    public long countByStatus(Connection conn, Long classId, LocalDate from, LocalDate to, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM attendance a WHERE a.status = ? ");
        List<Object> params = new ArrayList<>();
        params.add(status);
        if (classId != null) { sql.append("AND a.class_id = ? "); params.add(classId); }
        if (from != null) { sql.append("AND a.attendance_date >= ? "); params.add(Date.valueOf(from)); }
        if (to != null) { sql.append("AND a.attendance_date <= ? "); params.add(Date.valueOf(to)); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public List<LocalDate> absenceDates(Connection conn, long studentId, LocalDate from, LocalDate to) throws SQLException {
        List<Object> params = new ArrayList<>(List.of(studentId));
        StringBuilder sql = new StringBuilder(
                "SELECT attendance_date FROM attendance WHERE student_id = ? AND status IN ('ABSENT','LATE') ");
        if (from != null) { sql.append("AND attendance_date >= ? "); params.add(Date.valueOf(from)); }
        if (to != null) { sql.append("AND attendance_date <= ? "); params.add(Date.valueOf(to)); }
        sql.append("ORDER BY attendance_date");
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, rs -> rs.getDate("attendance_date").toLocalDate());
    }

    @Override
    public List<long[]> chronicAbsences(Connection conn, long classId, LocalDate from, LocalDate to,
                                        int threshold) throws SQLException {
        String sql = "SELECT student_id, COUNT(*) AS absences FROM attendance " +
                "WHERE class_id = ? AND status IN ('ABSENT','LATE') AND attendance_date BETWEEN ? AND ? " +
                "GROUP BY student_id HAVING COUNT(*) >= ? ORDER BY absences DESC";
        return queryList(conn, sql, ps -> {
            ps.setLong(1, classId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ps.setInt(4, threshold);
        }, rs -> new long[]{rs.getLong("student_id"), rs.getLong("absences")});
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.BIGINT);
        else ps.setLong(index, value);
    }
}
