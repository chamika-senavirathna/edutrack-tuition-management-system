package com.edutrack.dao.impl;

import com.edutrack.dao.TeacherDAO;
import com.edutrack.model.Teacher;

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

/** JDBC implementation of TeacherDAO (Member 01). */
public class TeacherDAOImpl implements TeacherDAO {

    private Teacher mapRow(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getLong("id"));
        t.setStaffNo(rs.getString("staff_no"));
        t.setFullName(rs.getString("full_name"));
        t.setNic(rs.getString("nic"));
        t.setGender(rs.getString("gender"));
        t.setEmail(rs.getString("email"));
        t.setPhone(rs.getString("phone"));
        t.setAddress(rs.getString("address"));
        t.setQualification(rs.getString("qualification"));
        t.setSpecialization(rs.getString("specialization"));
        Date joined = rs.getDate("joined_date");
        t.setJoinedDate(joined == null ? null : joined.toLocalDate());
        t.setStatus(rs.getString("status"));
        Date resigned = rs.getDate("resigned_at");
        t.setResignedAt(resigned == null ? null : resigned.toLocalDate());
        t.setResignedReason(rs.getString("resigned_reason"));
        t.setCreatedAt(getDateTime(rs, "created_at"));
        t.setUpdatedAt(getDateTime(rs, "updated_at"));
        return t;
    }

    @Override
    public Teacher insert(Connection conn, Teacher t) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO teachers (staff_no, full_name, nic, gender, email, phone, address, qualification, specialization, joined_date, status, created_at, updated_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setString(1, t.getStaffNo());
                    ps.setString(2, t.getFullName());
                    setNullableString(ps, 3, t.getNic());
                    ps.setString(4, t.getGender());
                    setNullableString(ps, 5, t.getEmail());
                    setNullableString(ps, 6, t.getPhone());
                    setNullableString(ps, 7, t.getAddress());
                    setNullableString(ps, 8, t.getQualification());
                    setNullableString(ps, 9, t.getSpecialization());
                    setNullableDate(ps, 10, t.getJoinedDate());
                    ps.setString(11, t.getStatus() == null ? "ACTIVE" : t.getStatus());
                });
        t.setId(id);
        return t;
    }

    @Override
    public void update(Connection conn, Teacher t) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE teachers SET full_name=?, nic=?, gender=?, email=?, phone=?, address=?, qualification=?, specialization=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setString(1, t.getFullName());
            setNullableString(ps, 2, t.getNic());
            ps.setString(3, t.getGender());
            setNullableString(ps, 4, t.getEmail());
            setNullableString(ps, 5, t.getPhone());
            setNullableString(ps, 6, t.getAddress());
            setNullableString(ps, 7, t.getQualification());
            setNullableString(ps, 8, t.getSpecialization());
            ps.setString(9, t.getStatus());
            ps.setLong(10, t.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Teacher findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, "SELECT * FROM teachers WHERE id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Teacher findByNic(Connection conn, String nic) throws SQLException {
        return queryOne(conn, "SELECT * FROM teachers WHERE nic = ?", ps -> ps.setString(1, nic), this::mapRow);
    }

    @Override
    public List<Teacher> search(Connection conn, String q, String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM teachers WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (full_name LIKE ? OR staff_no LIKE ? OR nic LIKE ?) ");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 3; i++) params.add(like);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM teachers WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (full_name LIKE ? OR staff_no LIKE ? OR nic LIKE ?) ");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 3; i++) params.add(like);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public void updateStatus(Connection conn, long id, String status, LocalDate resignedAt, String reason) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE teachers SET status=?, resigned_at=?, resigned_reason=?, updated_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setString(1, status);
            setNullableDate(ps, 2, resignedAt);
            setNullableString(ps, 3, reason);
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Teacher> findAllActive(Connection conn) throws SQLException {
        return queryList(conn, "SELECT * FROM teachers WHERE status = 'ACTIVE' ORDER BY full_name",
                null, this::mapRow);
    }

    @Override
    public long countAll(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM teachers", null, 0);
    }

    @Override
    public long nextSequence(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COALESCE(MAX(id), 0) + 1 FROM teachers", null, 1);
    }

    private void setNullableDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }
}
