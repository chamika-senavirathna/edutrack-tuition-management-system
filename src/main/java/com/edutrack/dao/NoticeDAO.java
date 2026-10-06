package com.edutrack.dao;

import com.edutrack.model.Notice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for notices (Member 02 - Communication). */
public interface NoticeDAO {
    Notice insert(Connection conn, Notice notice) throws SQLException;

    void update(Connection conn, Notice notice) throws SQLException;

    Notice findById(Connection conn, long id) throws SQLException;

    List<Notice> search(Connection conn, String q, String category, String audienceType,
                        String status, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, String category, String audienceType, String status) throws SQLException;

    /**
     * Notices visible to a given viewer: audience ALL, role-matched, class-matched
     * (via enrolment for students / teacher assignment), subject-matched, or
     * individually targeted (via notifications table link on student).
     */
    List<Notice> findVisibleToViewer(Connection conn, Long userId, String role, Long studentId,
                                     Long teacherId, List<Long> classIds, int limit) throws SQLException;

    void updateStatus(Connection conn, long id, String status) throws SQLException;

    long countAll(Connection conn) throws SQLException;
}
