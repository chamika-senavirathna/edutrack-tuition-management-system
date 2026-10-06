package com.edutrack.dao.impl;

import com.edutrack.dao.NotificationDAO;
import com.edutrack.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.getDateTime;
import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryLong;

/** JDBC implementation of NotificationDAO (supporting feature). */
public class NotificationDAOImpl implements NotificationDAO {

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        long uid = rs.getLong("user_id");
        n.setUserId(rs.wasNull() ? null : uid);
        n.setRoleName(rs.getString("role_name"));
        long sid = rs.getLong("student_id");
        n.setStudentId(rs.wasNull() ? null : sid);
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setChannel(rs.getString("channel"));
        n.setDeliveryStatus(rs.getString("delivery_status"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(getDateTime(rs, "created_at"));
        return n;
    }

    @Override
    public Notification insert(Connection conn, Notification n) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO notifications (user_id, role_name, student_id, title, message, channel, delivery_status, is_read, created_at) VALUES (?,?,?,?,?,?,?,0,CURRENT_TIMESTAMP)",
                ps -> {
                    if (n.getUserId() == null) ps.setNull(1, java.sql.Types.BIGINT);
                    else ps.setLong(1, n.getUserId());
                    ps.setString(2, n.getRoleName());
                    if (n.getStudentId() == null) ps.setNull(3, java.sql.Types.BIGINT);
                    else ps.setLong(3, n.getStudentId());
                    ps.setString(4, n.getTitle());
                    ps.setString(5, n.getMessage());
                    ps.setString(6, n.getChannel());
                    ps.setString(7, n.getDeliveryStatus() == null ? "SENT" : n.getDeliveryStatus());
                });
        n.setId(id);
        return n;
    }

    @Override
    public List<Notification> findByUser(Connection conn, long userId, int limit) throws SQLException {
        return queryList(conn,
                "SELECT * FROM notifications WHERE user_id = ? OR role_name IN (SELECT r.name FROM user_roles ur JOIN roles r ON r.id = ur.role_id WHERE ur.user_id = ?) " +
                "ORDER BY created_at DESC LIMIT ?",
                ps -> {
                    ps.setLong(1, userId);
                    ps.setLong(2, userId);
                    ps.setInt(3, limit);
                }, this::mapRow);
    }

    @Override
    public List<Notification> findByStudentIds(Connection conn, List<Long> studentIds, int limit) throws SQLException {
        if (studentIds == null || studentIds.isEmpty()) return new ArrayList<>();
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < studentIds.size(); i++) in.append(i == 0 ? "?" : ",?");
        return queryList(conn,
                "SELECT * FROM notifications WHERE student_id IN (" + in + ") ORDER BY created_at DESC LIMIT ?",
                ps -> {
                    for (int i = 0; i < studentIds.size(); i++) ps.setLong(i + 1, studentIds.get(i));
                    ps.setInt(studentIds.size() + 1, limit);
                }, this::mapRow);
    }

    @Override
    public long countUnread(Connection conn, long userId) throws SQLException {
        return queryLong(conn,
                "SELECT COUNT(*) FROM notifications WHERE (user_id = ? OR role_name IN (SELECT r.name FROM user_roles ur JOIN roles r ON r.id = ur.role_id WHERE ur.user_id = ?)) AND is_read = 0",
                ps -> {
                    ps.setLong(1, userId);
                    ps.setLong(2, userId);
                }, 0);
    }

    @Override
    public void markRead(Connection conn, long id, long userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }
}
