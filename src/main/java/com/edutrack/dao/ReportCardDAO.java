package com.edutrack.dao;

import com.edutrack.model.ReportCard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for report cards (Member 06). */
public interface ReportCardDAO {
    ReportCard upsert(Connection conn, ReportCard card) throws SQLException;

    ReportCard findById(Connection conn, long id) throws SQLException;

    ReportCard findByStudentAndTerm(Connection conn, long studentId, long termId) throws SQLException;

    List<ReportCard> search(Connection conn, Long termId, Long classId, String status,
                            int offset, int limit) throws SQLException;

    long countSearch(Connection conn, Long termId, Long classId, String status) throws SQLException;

    void publish(Connection conn, long id) throws SQLException;
}
