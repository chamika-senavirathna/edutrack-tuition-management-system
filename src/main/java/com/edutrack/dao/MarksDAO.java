package com.edutrack.dao;

import com.edutrack.model.Marks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for marks (Member 06). */
public interface MarksDAO {
    Marks upsert(Connection conn, Marks marks) throws SQLException;

    Marks findById(Connection conn, long id) throws SQLException;

    /** Existing mark row for a student+exam (unique pair). */
    Marks findByExamAndStudent(Connection conn, long examId, long studentId) throws SQLException;

    List<Marks> findByExam(Connection conn, long examId) throws SQLException;

    List<Marks> findByStudent(Connection conn, long studentId) throws SQLException;

    List<Marks> search(Connection conn, Long examId, Long studentId, String status,
                       int offset, int limit) throws SQLException;

    long countSearch(Connection conn, Long examId, Long studentId, String status) throws SQLException;

    void updateStatus(Connection conn, long id, String status) throws SQLException;

    void moderate(Connection conn, long id, String decision, String note, long moderatedBy) throws SQLException;

    void markPublished(Connection conn, long id) throws SQLException;

    long countByStatus(Connection conn, String status) throws SQLException;
}
