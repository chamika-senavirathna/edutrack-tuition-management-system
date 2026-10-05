package com.edutrack.dao.impl;

import com.edutrack.dao.RoleDAO;
import com.edutrack.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryLong;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;

/** JDBC implementation of RoleDAO (Member 05). */
public class RoleDAOImpl implements RoleDAO {

    private Role mapRow(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.setId(rs.getLong("id"));
        r.setName(rs.getString("name"));
        r.setDescription(rs.getString("description"));
        return r;
    }

    @Override
    public List<Role> findAll(Connection conn) throws SQLException {
        return queryList(conn, "SELECT * FROM roles ORDER BY id", null, this::mapRow);
    }

    @Override
    public Role findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, "SELECT * FROM roles WHERE id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Role findByName(Connection conn, String name) throws SQLException {
        return queryOne(conn, "SELECT * FROM roles WHERE name = ?", ps -> ps.setString(1, name), this::mapRow);
    }

    @Override
    public Role insert(Connection conn, Role role) throws SQLException {
        long id = insertAndGetKey(conn, "INSERT INTO roles (name, description) VALUES (?,?)",
                ps -> {
                    ps.setString(1, role.getName());
                    ps.setString(2, role.getDescription());
                });
        role.setId(id);
        return role;
    }

    @Override
    public void update(Connection conn, Role role) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE roles SET name=?, description=? WHERE id=?")) {
            ps.setString(1, role.getName());
            ps.setString(2, role.getDescription());
            ps.setLong(3, role.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public long countUsers(Connection conn, long roleId) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM user_roles WHERE role_id = ?",
                ps -> ps.setLong(1, roleId), 0);
    }
}
