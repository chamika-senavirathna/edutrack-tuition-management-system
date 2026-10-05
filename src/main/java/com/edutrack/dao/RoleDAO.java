package com.edutrack.dao;

import com.edutrack.model.Role;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for roles (Member 05). */
public interface RoleDAO {
    List<Role> findAll(Connection conn) throws SQLException;

    Role findById(Connection conn, long id) throws SQLException;

    Role findByName(Connection conn, String name) throws SQLException;

    Role insert(Connection conn, Role role) throws SQLException;

    void update(Connection conn, Role role) throws SQLException;

    long countUsers(Connection conn, long roleId) throws SQLException;
}
