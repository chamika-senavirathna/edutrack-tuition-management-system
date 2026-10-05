package com.edutrack.dao;

import com.edutrack.model.GradeBand;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for configurable grade bands (Member 06, PBI-22). */
public interface GradeBandDAO {
    List<GradeBand> findAll(Connection conn) throws SQLException;

    GradeBand findById(Connection conn, long id) throws SQLException;

    GradeBand insert(Connection conn, GradeBand band) throws SQLException;

    void update(Connection conn, GradeBand band) throws SQLException;

    /** BR-EXM: grade lookup by mark. */
    GradeBand findByMark(Connection conn, int mark) throws SQLException;
}
