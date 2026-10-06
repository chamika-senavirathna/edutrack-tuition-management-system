package com.edutrack.dao;

import com.edutrack.model.Subject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for subjects (Member 02). */
public interface SubjectDAO {
    List<Subject> findAll(Connection conn, boolean includeInactive) throws SQLException;

    Subject findById(Connection conn, long id) throws SQLException;

    Subject insert(Connection conn, Subject subject) throws SQLException;

    void update(Connection conn, Subject subject) throws SQLException;

    boolean codeExists(Connection conn, String code, Long excludeId) throws SQLException;
}
