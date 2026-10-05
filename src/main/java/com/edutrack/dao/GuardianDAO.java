package com.edutrack.dao;

import com.edutrack.model.Guardian;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for guardian/parent profiles (Member 01). */
public interface GuardianDAO {
    Guardian insert(Connection conn, Guardian guardian) throws SQLException;

    void update(Connection conn, Guardian guardian) throws SQLException;

    Guardian findById(Connection conn, long id) throws SQLException;

    Guardian findByNic(Connection conn, String nic) throws SQLException;

    List<Guardian> search(Connection conn, String q, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q) throws SQLException;
}
