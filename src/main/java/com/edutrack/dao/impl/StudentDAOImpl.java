package com.edutrack.dao.impl;

import com.edutrack.dao.StudentDAO;
import com.edutrack.model.Student;

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

/** JDBC implementation of StudentDAO (Member 01). */
public class StudentDAOImpl implements StudentDAO {

    private static final String BASE_SELECT =
            "SELECT s.*, g.full_name AS guardian_name, g.phone AS guardian_phone FROM students s " +
            "LEFT JOIN guardians g ON g.id = s.guardian_id ";

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getLong("id"));
        s.setRegNo(rs.getString("reg_no"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setNameWithInitials(rs.getString("name_with_initials"));
        s.setNic(rs.getString("nic"));
        s.setGender(rs.getString("gender"));
        Date dob = rs.getDate("dob");
        s.setDob(dob == null ? null : dob.toLocalDate());
        s.setAddress(rs.getString("address"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        long gid = rs.getLong("guardian_id");
        s.setGuardianId(rs.wasNull() ? null : gid);
        s.setMedicalNotes(rs.getString("medical_notes"));
        Date adm = rs.getDate("admission_date");
        s.setAdmissionDate(adm == null ? null : adm.toLocalDate());
        s.setStatus(rs.getString("status"));
        Date wid = rs.getDate("withdrawn_at");
        s.setWithdrawnAt(wid == null ? null : wid.toLocalDate());
        s.setWithdrawnReason(rs.getString("withdrawn_reason"));
        s.setCreatedAt(getDateTime(rs, "created_at"));
        s.setUpdatedAt(getDateTime(rs, "updated_at"));
        s.setGuardianName(rs.getString("guardian_name"));
        s.setGuardianPhone(rs.getString("guardian_phone"));
        return s;
    }

    @Override
    public Student insert(Connection conn, Student s) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO students (reg_no, first_name, last_name, name_with_initials, nic, gender, dob, address, phone, email, guardian_id, medical_notes, admission_date, status, created_at, updated_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setString(1, s.getRegNo());
                    ps.setString(2, s.getFirstName());
                    ps.setString(3, s.getLastName());
                    setNullableString(ps, 4, s.getNameWithInitials());
                    setNullableString(ps, 5, s.getNic());
                    ps.setString(6, s.getGender());
                    setNullableDate(ps, 7, s.getDob());
                    setNullableString(ps, 8, s.getAddress());
                    setNullableString(ps, 9, s.getPhone());
                    setNullableString(ps, 10, s.getEmail());
                    setNullableLong(ps, 11, s.getGuardianId());
                    setNullableString(ps, 12, s.getMedicalNotes());
                    setNullableDate(ps, 13, s.getAdmissionDate());
                    ps.setString(14, s.getStatus() == null ? "ACTIVE" : s.getStatus());
                });
        s.setId(id);
        return s;
    }

    @Override
    public void update(Connection conn, Student s) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE students SET first_name=?, last_name=?, name_with_initials=?, nic=?, gender=?, dob=?, address=?, phone=?, email=?, guardian_id=?, medical_notes=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            setNullableString(ps, 3, s.getNameWithInitials());
            setNullableString(ps, 4, s.getNic());
            ps.setString(5, s.getGender());
            setNullableDate(ps, 6, s.getDob());
            setNullableString(ps, 7, s.getAddress());
            setNullableString(ps, 8, s.getPhone());
            setNullableString(ps, 9, s.getEmail());
            setNullableLong(ps, 10, s.getGuardianId());
            setNullableString(ps, 11, s.getMedicalNotes());
            ps.setString(12, s.getStatus());
            ps.setLong(13, s.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Student findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE s.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Student findByRegNo(Connection conn, String regNo) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE s.reg_no = ?", ps -> ps.setString(1, regNo), this::mapRow);
    }

    @Override
    public Student findByNic(Connection conn, String nic) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE s.nic = ?", ps -> ps.setString(1, nic), this::mapRow);
    }

    @Override
    public List<Student> search(Connection conn, String q, Long guardianId, String status,
                                int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (s.first_name LIKE ? OR s.last_name LIKE ? OR s.reg_no LIKE ? OR s.nic LIKE ?) ");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 4; i++) params.add(like);
        }
        if (guardianId != null) {
            sql.append("AND s.guardian_id = ? ");
            params.add(guardianId);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND s.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY s.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q, Long guardianId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM students s WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (s.first_name LIKE ? OR s.last_name LIKE ? OR s.reg_no LIKE ? OR s.nic LIKE ?) ");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 4; i++) params.add(like);
        }
        if (guardianId != null) {
            sql.append("AND s.guardian_id = ? ");
            params.add(guardianId);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND s.status = ? ");
            params.add(status);
        }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public void updateStatus(Connection conn, long id, String status, LocalDate withdrawnAt, String reason) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE students SET status=?, withdrawn_at=?, withdrawn_reason=?, updated_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setString(1, status);
            setNullableDate(ps, 2, withdrawnAt);
            setNullableString(ps, 3, reason);
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    @Override
    public long countAll(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM students", null, 0);
    }

    @Override
    public long countByStatus(Connection conn, String status) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM students WHERE status = ?",
                ps -> ps.setString(1, status), 0);
    }

    @Override
    public long nextSequence(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COALESCE(MAX(id), 0) + 1 FROM students", null, 1);
    }

    private void setNullableDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }
}
