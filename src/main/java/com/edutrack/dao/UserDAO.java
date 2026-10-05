package com.edutrack.dao;

import com.edutrack.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for user accounts (Member 05 - User & Access Management). */
public interface UserDAO {
    User findByUsername(Connection conn, String username) throws SQLException;

    User findById(Connection conn, long id) throws SQLException;

    User insert(Connection conn, User user, List<Long> roleIds) throws SQLException;

    void update(Connection conn, User user) throws SQLException;

    void updatePassword(Connection conn, long userId, String passwordHash) throws SQLException;

    void updateStatus(Connection conn, long userId, boolean active) throws SQLException;

    void replaceRoles(Connection conn, long userId, List<Long> roleIds) throws SQLException;

    void updateLastLogin(Connection conn, long userId) throws SQLException;

    List<User> search(Connection conn, String q, String role, String status, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, String role, String status) throws SQLException;

    boolean usernameExists(Connection conn, String username, Long excludeId) throws SQLException;

    boolean emailExists(Connection conn, String email, Long excludeId) throws SQLException;

    long countAll(Connection conn) throws SQLException;

    long countActive(Connection conn) throws SQLException;
}
