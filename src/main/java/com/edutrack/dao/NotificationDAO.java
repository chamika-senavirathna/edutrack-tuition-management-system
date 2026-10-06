package com.edutrack.dao;

import com.edutrack.model.Notification;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for in-system notifications (supporting feature). */
public interface NotificationDAO {
    Notification insert(Connection conn, Notification notification) throws SQLException;

    List<Notification> findByUser(Connection conn, long userId, int limit) throws SQLException;

    List<Notification> findByStudentIds(Connection conn, List<Long> studentIds, int limit) throws SQLException;

    long countUnread(Connection conn, long userId) throws SQLException;

    void markRead(Connection conn, long id, long userId) throws SQLException;
}
