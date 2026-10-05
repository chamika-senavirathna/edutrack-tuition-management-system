package com.edutrack.util;

import com.edutrack.model.User;
import com.edutrack.model.UserRole;
import com.edutrack.dao.RoleDAO;
import com.edutrack.dao.impl.RoleDAOImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Audit trail (Member 05, BR-UAM-02, Section 25): records user, action,
 * entity type/id, old value, new value and timestamp for every critical action.
 * Audit failures never break the business operation - they are logged and swallowed.
 */
public final class AuditLogger {

    private AuditLogger() {
    }

    /** Record an audit entry with old and new values. */
    public static void log(Connection conn, User actor, String action,
                           String entityType, Long entityId, String oldValue, String newValue) throws SQLException {
        String sql = "INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_value, new_value, created_at) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, actor.getId());
            ps.setString(2, action);
            ps.setString(3, entityType);
            if (entityId == null) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, entityId);
            }
            ps.setString(5, oldValue);
            ps.setString(6, newValue);
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    /** Opens its own connection for callers without one in scope. */
    public static void log(User actor, String action, String entityType, Long entityId,
                           String oldValue, String newValue) {
        try (Connection conn = DB.getConnection()) {
            log(conn, actor, action, entityType, entityId, oldValue, newValue);
        } catch (SQLException e) {
            // Audit must never block business flow; log for developer follow-up.
            System.err.println("[AUDIT-ERROR] " + action + " on " + entityType + "#" + entityId
                    + " by user#" + (actor == null ? "null" : actor.getId()) + ": " + e.getMessage());
        }
    }

    /** Role names for a user, used when building audit payloads. */
    public static String rolesOf(User user) {
        if (user == null || user.getRoles() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (UserRole ur : user.getRoles()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(ur.getRole().getName());
        }
        return sb.toString();
    }

    /** Convenience used by services for 'system' events. */
    public static void system(String action, String entityType, Long entityId, String newValue) {
        User system = new User();
        system.setId(0L);
        log(system, action, entityType, entityId, null, newValue);
    }
}
