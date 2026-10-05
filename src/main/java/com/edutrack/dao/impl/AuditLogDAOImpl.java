package com.edutrack.dao.impl;

import com.edutrack.dao.AuditLogDAO;
import com.edutrack.model.AuditLog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.getDateTime;
import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryLong;

/** JDBC implementation of AuditLogDAO (Member 05). */
public class AuditLogDAOImpl implements AuditLogDAO {

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setAction(rs.getString("action"));
        a.setEntityType(rs.getString("entity_type"));
        long eid = rs.getLong("entity_id");
        a.setEntityId(rs.wasNull() ? null : eid);
        a.setOldValue(rs.getString("old_value"));
        a.setNewValue(rs.getString("new_value"));
        a.setCreatedAt(getDateTime(rs, "created_at"));
        String userName;
        try {
            userName = rs.getString("user_name");
        } catch (SQLException e) {
            userName = null;
        }
        a.setUserName(userName);
        return a;
    }

    @Override
    public void insert(Connection conn, AuditLog log) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_value, new_value, created_at) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setLong(1, log.getUserId() == null ? 0L : log.getUserId());
                    ps.setString(2, log.getAction());
                    ps.setString(3, log.getEntityType());
                    if (log.getEntityId() == null) ps.setNull(4, java.sql.Types.BIGINT);
                    else ps.setLong(4, log.getEntityId());
                    ps.setString(5, log.getOldValue());
                    ps.setString(6, log.getNewValue());
                });
        log.setId(id);
    }

    @Override
    public List<AuditLog> search(Connection conn, String entityType, Long entityId, Long userId,
                                 int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, u.full_name AS user_name FROM audit_log a " +
                "LEFT JOIN users u ON u.id = a.user_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (entityType != null && !entityType.isBlank()) {
            sql.append("AND a.entity_type = ? ");
            params.add(entityType);
        }
        if (entityId != null) {
            sql.append("AND a.entity_id = ? ");
            params.add(entityId);
        }
        if (userId != null) {
            sql.append("AND a.user_id = ? ");
            params.add(userId);
        }
        sql.append("ORDER BY a.created_at DESC, a.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String entityType, Long entityId, Long userId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_log a WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (entityType != null && !entityType.isBlank()) {
            sql.append("AND a.entity_type = ? ");
            params.add(entityType);
        }
        if (entityId != null) {
            sql.append("AND a.entity_id = ? ");
            params.add(entityId);
        }
        if (userId != null) {
            sql.append("AND a.user_id = ? ");
            params.add(userId);
        }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public List<AuditLog> recent(Connection conn, int limit) throws SQLException {
        return search(conn, null, null, null, 0, limit);
    }
}
