package com.edutrack.service;

import com.edutrack.dao.AuditLogDAO;
import com.edutrack.dao.impl.AuditLogDAOImpl;
import com.edutrack.exception.AuthorizationException;
import com.edutrack.model.AuditLog;
import com.edutrack.model.User;
import com.edutrack.util.DB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Audit trail viewer (Member 05). Access restricted to ADMIN and PRINCIPAL. */
public class AuditService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    public List<AuditLog> search(String entityType, Long entityId, Long userId, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return auditLogDAO.search(conn, entityType, entityId, userId, offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search audit logs.", e);
        }
    }

    public List<AuditLog> recent(int limit) {
        try (Connection conn = DB.getConnection()) {
            return auditLogDAO.recent(conn, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load recent audit entries.", e);
        }
    }

    /** Authorisation guard for audit views (PBI-23). */
    public void requireAuditAccess(User actor) {
        if (actor == null || !(actor.hasRole("ADMIN") || actor.hasRole("PRINCIPAL"))) {
            throw new AuthorizationException("Audit access is restricted to administrators and the Principal.");
        }
    }
}
