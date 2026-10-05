package com.edutrack.dao;

import com.edutrack.model.Enrolment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for student-class enrolments (Member 01/02 integration). */
public interface EnrolmentDAO {
    Enrolment insert(Connection conn, Enrolment enrolment) throws SQLException;

    void updateStatus(Connection conn, long id, String status, String remarks) throws SQLException;

    Enrolment findById(Connection conn, long id) throws SQLException;

    /** The active enrolment of a student in a class, if any. */
    Enrolment findActive(Connection conn, long studentId, long classId) throws SQLException;

    List<Enrolment> findByStudent(Connection conn, long studentId) throws SQLException;

    List<Enrolment> findByClass(Connection conn, long classId, String status) throws SQLException;

    long countActive(Connection conn, long classId) throws SQLException;

    List<Enrolment> search(Connection conn, Long studentId, Long classId, String status,
                           int offset, int limit) throws SQLException;

    long countSearch(Connection conn, Long studentId, Long classId, String status) throws SQLException;
}
