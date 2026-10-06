package com.edutrack.dao.impl;

import com.edutrack.dao.SubjectDAO;
import com.edutrack.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryLong;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;
import static com.edutrack.dao.impl.JdbcHelper.setNullableString;

/** JDBC implementation of SubjectDAO (Member 02). */
public class SubjectDAOImpl implements SubjectDAO {

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getLong("id"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setActive(rs.getBoolean("active"));
        return s;
    }

    @Override
    public List<Subject> findAll(Connection conn, boolean includeInactive) throws SQLException {
        String sql = "SELECT * FROM subjects" + (includeInactive ? "" : " WHERE active = 1") + " ORDER BY name";
        return queryList(conn, sql, null, this::mapRow);
    }

    @Override
    public Subject findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, "SELECT * FROM subjects WHERE id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Subject insert(Connection conn, Subject s) throws SQLException {
        long id = insertAndGetKey(conn, "INSERT INTO subjects (code, name, description, active) VALUES (?,?,?,?)",
                ps -> {
                    ps.setString(1, s.getCode());
                    ps.setString(2, s.getName());
                    setNullableString(ps, 3, s.getDescription());
                    ps.setBoolean(4, s.getActive() == null || s.getActive());
                });
        s.setId(id);
        return s;
    }

    @Override
    public void update(Connection conn, Subject s) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE subjects SET code=?, name=?, description=?, active=? WHERE id=?")) {
            ps.setString(1, s.getCode());
            ps.setString(2, s.getName());
            setNullableString(ps, 3, s.getDescription());
            ps.setBoolean(4, s.getActive() == null || s.getActive());
            ps.setLong(5, s.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public boolean codeExists(Connection conn, String code, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subjects WHERE code = ?" + (excludeId == null ? "" : " AND id <> ?");
        return queryLong(conn, sql, ps -> {
            ps.setString(1, code);
            if (excludeId != null) ps.setLong(2, excludeId);
        }, 0) > 0;
    }
}
