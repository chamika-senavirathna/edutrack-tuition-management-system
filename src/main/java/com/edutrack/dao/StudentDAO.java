package com.edutrack.dao;

import com.edutrack.model.Student;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** DAO for student profiles (Member 01 - Registration & Profiles). */
public interface StudentDAO {
    Student insert(Connection conn, Student student) throws SQLException;

    void update(Connection conn, Student student) throws SQLException;

    Student findById(Connection conn, long id) throws SQLException;

    Student findByRegNo(Connection conn, String regNo) throws SQLException;

    Student findByNic(Connection conn, String nic) throws SQLException;

    /** Search with optional filters; any parameter may be null. */
    List<Student> search(Connection conn, String q, Long guardianId, String status,
                         int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, Long guardianId, String status) throws SQLException;

    void updateStatus(Connection conn, long id, String status, LocalDate withdrawnAt,
                      String reason) throws SQLException;

    long countAll(Connection conn) throws SQLException;

    long countByStatus(Connection conn, String status) throws SQLException;

    long nextSequence(Connection conn) throws SQLException;
}
