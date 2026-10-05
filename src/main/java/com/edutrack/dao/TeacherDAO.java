package com.edutrack.dao;

import com.edutrack.model.Teacher;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** DAO for teacher profiles (Member 01). */
public interface TeacherDAO {
    Teacher insert(Connection conn, Teacher teacher) throws SQLException;

    void update(Connection conn, Teacher teacher) throws SQLException;

    Teacher findById(Connection conn, long id) throws SQLException;

    Teacher findByNic(Connection conn, String nic) throws SQLException;

    List<Teacher> search(Connection conn, String q, String status, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, String status) throws SQLException;

    void updateStatus(Connection conn, long id, String status, LocalDate resignedAt,
                      String reason) throws SQLException;

    List<Teacher> findAllActive(Connection conn) throws SQLException;

    long countAll(Connection conn) throws SQLException;

    long nextSequence(Connection conn) throws SQLException;
}
