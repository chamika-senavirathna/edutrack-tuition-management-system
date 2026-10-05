package com.edutrack.dao;

import com.edutrack.model.Examination;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for examinations (Member 06 - Examination & Reporting). */
public interface ExaminationDAO {
    Examination insert(Connection conn, Examination exam) throws SQLException;

    void update(Connection conn, Examination exam) throws SQLException;

    Examination findById(Connection conn, long id) throws SQLException;

    List<Examination> search(Connection conn, String q, Long termId, Long classId, Long subjectId,
                             String status, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, Long termId, Long classId, Long subjectId,
                     String status) throws SQLException;

    /** Exams relevant to a student (via class enrolment). */
    List<Examination> findByStudent(Connection conn, long studentId) throws SQLException;

    List<Examination> findByTeacher(Connection conn, long teacherId) throws SQLException;

    void updateStatus(Connection conn, long id, String status) throws SQLException;

    long countAll(Connection conn) throws SQLException;
}
