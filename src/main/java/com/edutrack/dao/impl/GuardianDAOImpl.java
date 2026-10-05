package com.edutrack.dao.impl;

import com.edutrack.dao.GuardianDAO;
import com.edutrack.model.Guardian;

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
import static com.edutrack.dao.impl.JdbcHelper.setNullableString;

/** JDBC implementation of GuardianDAO (Member 01). */
public class GuardianDAOImpl implements GuardianDAO {

    private Guardian mapRow(ResultSet rs) throws SQLException {
        Guardian g = new Guardian();
        g.setId(rs.getLong("id"));
        g.setFullName(rs.getString("full_name"));
        g.setNic(rs.getString("nic"));
        g.setPhone(rs.getString("phone"));
        g.setEmail(rs.getString("email"));
        g.setOccupation(rs.getString("occupation"));
        g.setAddress(rs.getString("address"));
        g.setRelationship(rs.getString("relationship"));
        Date created = rs.getDate("created_at");
        if (created != null) {
            java.time.LocalDateTime ldt = created.toLocalDate().atStartOfDay();
            g.setCreatedAt(created.toLocalDate());
        }
        return g;
    }

    @Override
    public Guardian insert(Connection conn, Guardian g) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO guardians (full_name, nic, phone, email, occupation, address, relationship, created_at) VALUES (?,?,?,?,?,?,?,CURRENT_DATE)",
                ps -> {
                    ps.setString(1, g.getFullName());
                    setNullableString(ps, 2, g.getNic());
                    setNullableString(ps, 3, g.getPhone());
                    setNullableString(ps, 4, g.getEmail());
                    setNullableString(ps, 5, g.getOccupation());
                    setNullableString(ps, 6, g.getAddress());
                    setNullableString(ps, 7, g.getRelationship());
                });
        g.setId(id);
        return g;
    }

    @Override
    public void update(Connection conn, Guardian g) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE guardians SET full_name=?, nic=?, phone=?, email=?, occupation=?, address=?, relationship=? WHERE id=?")) {
            ps.setString(1, g.getFullName());
            setNullableString(ps, 2, g.getNic());
            setNullableString(ps, 3, g.getPhone());
            setNullableString(ps, 4, g.getEmail());
            setNullableString(ps, 5, g.getOccupation());
            setNullableString(ps, 6, g.getAddress());
            setNullableString(ps, 7, g.getRelationship());
            ps.setLong(8, g.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Guardian findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, "SELECT * FROM guardians WHERE id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Guardian findByNic(Connection conn, String nic) throws SQLException {
        return queryOne(conn, "SELECT * FROM guardians WHERE nic = ?", ps -> ps.setString(1, nic), this::mapRow);
    }

    @Override
    public List<Guardian> search(Connection conn, String q, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM guardians WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (full_name LIKE ? OR nic LIKE ? OR phone LIKE ?) ");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 3; i++) params.add(like);
        }
        sql.append("ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM guardians WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (full_name LIKE ? OR nic LIKE ? OR phone LIKE ?) ");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 3; i++) params.add(like);
        }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }
}
