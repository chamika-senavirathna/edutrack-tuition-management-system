package com.edutrack.dao.impl;

import com.edutrack.dao.UserDAO;
import com.edutrack.model.Role;
import com.edutrack.model.User;
import com.edutrack.model.UserRole;

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

/** JDBC implementation of UserDAO (Member 05). */
public class UserDAOImpl implements UserDAO {

    private static final String BASE_SELECT =
            "SELECT u.*, r.id AS role_id, r.name AS role_name, r.description AS role_desc " +
            "FROM users u " +
            "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
            "LEFT JOIN roles r ON r.id = ur.role_id ";

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setActive(rs.getBoolean("active"));
        u.setUserType(rs.getString("user_type"));
        long sid = rs.getLong("student_id");
        u.setStudentId(rs.wasNull() ? null : sid);
        long tid = rs.getLong("teacher_id");
        u.setTeacherId(rs.wasNull() ? null : tid);
        long pid = rs.getLong("parent_id");
        u.setParentId(rs.wasNull() ? null : pid);
        u.setCreatedAt(getDateTime(rs, "created_at"));
        u.setLastLoginAt(getDateTime(rs, "last_login_at"));
        return u;
    }

    private User loadWithRoles(Connection conn, User user) throws SQLException {
        if (user == null) return null;
        List<UserRole> roles = queryList(conn,
                "SELECT ur.user_id, ur.role_id, r.id AS role_id, r.name, r.description " +
                "FROM user_roles ur JOIN roles r ON r.id = ur.role_id WHERE ur.user_id = ?",
                ps -> ps.setLong(1, user.getId()),
                rs -> {
                    Role role = new Role();
                    role.setId(rs.getLong("role_id"));
                    role.setName(rs.getString("name"));
                    role.setDescription(rs.getString("description"));
                    UserRole ur = new UserRole();
                    ur.setUserId(user.getId());
                    ur.setRoleId(role.getId());
                    ur.setRole(role);
                    return ur;
                });
        for (UserRole ur : roles) {
            user.getRoles().add(ur);
        }
        return user;
    }

    @Override
    public User findByUsername(Connection conn, String username) throws SQLException {
        List<User> users = queryList(conn, BASE_SELECT + "WHERE u.username = ?",
                ps -> ps.setString(1, username), this::mapRow);
        if (users.isEmpty()) return null;
        User merged = mergeRows(users);
        return loadWithRoles(conn, merged);
    }

    @Override
    public User findById(Connection conn, long id) throws SQLException {
        List<User> users = queryList(conn, BASE_SELECT + "WHERE u.id = ?",
                ps -> ps.setLong(1, id), this::mapRow);
        if (users.isEmpty()) return null;
        return loadWithRoles(conn, mergeRows(users));
    }

    /** Joins the one-to-many role rows of a BASE_SELECT result into a single User. */
    private User mergeRows(List<User> rows) {
        User first = rows.get(0);
        for (int i = 1; i < rows.size(); i++) {
            User u = rows.get(i);
            // role rows from BASE_SELECT are flattened away; roles load separately
            if (u.getCreatedAt() != null && first.getCreatedAt() == null) first.setCreatedAt(u.getCreatedAt());
            if (u.getLastLoginAt() != null && first.getLastLoginAt() == null) first.setLastLoginAt(u.getLastLoginAt());
        }
        return first;
    }

    @Override
    public User insert(Connection conn, User user, List<Long> roleIds) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO users (username, email, password_hash, full_name, phone, active, user_type, student_id, teacher_id, parent_id, created_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getPasswordHash());
                    ps.setString(4, user.getFullName());
                    JdbcHelper.setNullableString(ps, 5, user.getPhone());
                    ps.setBoolean(6, user.getActive() == null || user.getActive());
                    ps.setString(7, user.getUserType());
                    setNullableLong(ps, 8, user.getStudentId());
                    setNullableLong(ps, 9, user.getTeacherId());
                    setNullableLong(ps, 10, user.getParentId());
                });
        user.setId(id);
        if (roleIds != null && !roleIds.isEmpty()) {
            replaceRoles(conn, id, roleIds);
        }
        return user;
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    @Override
    public void update(Connection conn, User user) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET email=?, full_name=?, phone=?, user_type=?, student_id=?, teacher_id=?, parent_id=? WHERE id=?")) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFullName());
            JdbcHelper.setNullableString(ps, 3, user.getPhone());
            ps.setString(4, user.getUserType());
            setNullableLong(ps, 5, user.getStudentId());
            setNullableLong(ps, 6, user.getTeacherId());
            setNullableLong(ps, 7, user.getParentId());
            ps.setLong(8, user.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void updatePassword(Connection conn, long userId, String passwordHash) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateStatus(Connection conn, long userId, boolean active) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET active=? WHERE id=?")) {
            ps.setBoolean(1, active);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public void replaceRoles(Connection conn, long userId, List<Long> roleIds) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM user_roles WHERE user_id=?")) {
            del.setLong(1, userId);
            del.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO user_roles (user_id, role_id) VALUES (?,?)")) {
            for (Long roleId : roleIds) {
                ps.setLong(1, userId);
                ps.setLong(2, roleId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public void updateLastLogin(Connection conn, long userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET last_login_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<User> search(Connection conn, String q, String role, String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (u.username LIKE ? OR u.full_name LIKE ? OR u.email LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (role != null && !role.isBlank()) {
            sql.append("AND r.name = ? ");
            params.add(role);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND u.active = ? ");
            params.add("ACTIVE".equalsIgnoreCase(status));
        }
        sql.append("ORDER BY u.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        List<User> rows = queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
        }, this::mapRow);
        // merge duplicate rows (one per role) and hydrate roles
        List<User> merged = new ArrayList<>();
        java.util.Map<Long, User> byId = new java.util.LinkedHashMap<>();
        for (User u : rows) {
            byId.merge(u.getId(), u, (a, b) -> a);
        }
        for (User u : byId.values()) {
            merged.add(loadWithRoles(conn, u));
        }
        return merged;
    }

    @Override
    public long countSearch(Connection conn, String q, String role, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT u.id) FROM users u " +
                "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
                "LEFT JOIN roles r ON r.id = ur.role_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (u.username LIKE ? OR u.full_name LIKE ? OR u.email LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (role != null && !role.isBlank()) {
            sql.append("AND r.name = ? ");
            params.add(role);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND u.active = ? ");
            params.add("ACTIVE".equalsIgnoreCase(status));
        }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
        }, 0);
    }

    @Override
    public boolean usernameExists(Connection conn, String username, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?" + (excludeId == null ? "" : " AND id <> ?");
        return queryLong(conn, sql, ps -> {
            ps.setString(1, username);
            if (excludeId != null) ps.setLong(2, excludeId);
        }, 0) > 0;
    }

    @Override
    public boolean emailExists(Connection conn, String email, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?" + (excludeId == null ? "" : " AND id <> ?");
        return queryLong(conn, sql, ps -> {
            ps.setString(1, email);
            if (excludeId != null) ps.setLong(2, excludeId);
        }, 0) > 0;
    }

    @Override
    public long countAll(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM users", null, 0);
    }

    @Override
    public long countActive(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM users WHERE active = 1", null, 0);
    }
}
