package com.edutrack.dao;

import com.edutrack.model.AuditLog;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Read-only access to the audit trail (Member 05). */
public interface AuditLogDAO {
    void insert(Connection conn, AuditLog log) throws SQLException;

    List<AuditLog> search(Connection conn, String entityType, Long entityId, Long userId,
                          int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String entityType, Long entityId, Long userId) throws SQLException;

    List<AuditLog> recent(Connection conn, int limit) throws SQLException;
}
