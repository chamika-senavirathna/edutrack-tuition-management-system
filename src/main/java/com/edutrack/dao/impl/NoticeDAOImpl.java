package com.edutrack.dao.impl;

import com.edutrack.dao.NoticeDAO;
import com.edutrack.model.Notice;

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
import static com.edutrack.dao.impl.JdbcHelper.queryOne;
import static com.edutrack.dao.impl.JdbcHelper.setNullableString;

/** JDBC implementation of NoticeDAO (Member 02 - Communication). */
public class NoticeDAOImpl implements NoticeDAO {

    private static final String BASE_SELECT =
            "SELECT n.*, u.full_name AS created_by_name, " +
            "CASE n.audience_type WHEN 'CLASS' THEN (SELECT name FROM classes WHERE id = n.audience_ref_id) " +
            "WHEN 'SUBJECT' THEN (SELECT name FROM subjects WHERE id = n.audience_ref_id) " +
            "WHEN 'ROLE' THEN n.audience_ref_id " +
            "ELSE NULL END AS audience_ref_name " +
            "FROM notices n LEFT JOIN users u ON u.id = n.created_by ";

    private Notice mapRow(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setId(rs.getLong("id"));
        n.setTitle(rs.getString("title"));
        n.setContent(rs.getString("content"));
        n.setCategory(rs.getString("category"));
        n.setAudienceType(rs.getString("audience_type"));
        long ref = rs.getLong("audience_ref_id");
        n.setAudienceRefId(rs.wasNull() ? null : ref);
        n.setChannel(rs.getString("channel"));
        n.setPriority(rs.getString("priority"));
        n.setCreatedBy(rs.getLong("created_by"));
        n.setStatus(rs.getString("status"));
        n.setPublishedAt(getDateTime(rs, "published_at"));
        n.setCreatedAt(getDateTime(rs, "created_at"));
        n.setCreatedByName(rs.getString("created_by_name"));
        n.setAudienceRefName(rs.getString("audience_ref_name"));
        return n;
    }

    @Override
    public Notice insert(Connection conn, Notice n) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO notices (title, content, category, audience_type, audience_ref_id, channel, priority, created_by, status, published_at, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setString(1, n.getTitle());
                    ps.setString(2, n.getContent());
                    ps.setString(3, n.getCategory());
                    ps.setString(4, n.getAudienceType());
                    setNullableLong(ps, 5, n.getAudienceRefId());
                    ps.setString(6, n.getChannel());
                    ps.setString(7, n.getPriority());
                    ps.setLong(8, n.getCreatedBy());
                    ps.setString(9, n.getStatus() == null ? "DRAFT" : n.getStatus());
                    if (n.getPublishedAt() == null) ps.setNull(10, java.sql.Types.TIMESTAMP);
                    else ps.setTimestamp(10, java.sql.Timestamp.valueOf(n.getPublishedAt()));
                });
        n.setId(id);
        return n;
    }

    @Override
    public void update(Connection conn, Notice n) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE notices SET title=?, content=?, category=?, audience_type=?, audience_ref_id=?, channel=?, priority=?, status=?, published_at=? WHERE id=?")) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getContent());
            ps.setString(3, n.getCategory());
            ps.setString(4, n.getAudienceType());
            setNullableLong(ps, 5, n.getAudienceRefId());
            ps.setString(6, n.getChannel());
            ps.setString(7, n.getPriority());
            ps.setString(8, n.getStatus());
            ps.setTimestamp(9, n.getPublishedAt() == null ? null : java.sql.Timestamp.valueOf(n.getPublishedAt()));
            ps.setLong(10, n.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Notice findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE n.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public List<Notice> search(Connection conn, String q, String category, String audienceType,
                               String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (n.title LIKE ? OR n.content LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (category != null && !category.isBlank()) { sql.append("AND n.category = ? "); params.add(category); }
        if (audienceType != null && !audienceType.isBlank()) { sql.append("AND n.audience_type = ? "); params.add(audienceType); }
        if (status != null && !status.isBlank()) { sql.append("AND n.status = ? "); params.add(status); }
        sql.append("ORDER BY COALESCE(n.published_at, n.created_at) DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q, String category, String audienceType, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM notices n WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (n.title LIKE ? OR n.content LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (category != null && !category.isBlank()) { sql.append("AND n.category = ? "); params.add(category); }
        if (audienceType != null && !audienceType.isBlank()) { sql.append("AND n.audience_type = ? "); params.add(audienceType); }
        if (status != null && !status.isBlank()) { sql.append("AND n.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public List<Notice> findVisibleToViewer(Connection conn, Long userId, String role, Long studentId,
                                            Long teacherId, List<Long> classIds, int limit) throws SQLException {
        // Audience visibility: ALL | ROLE(<role>) | CLASS(<classId>) | SUBJECT taught/enrolled.
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE n.status = 'PUBLISHED' AND (n.audience_type = 'ALL' ");
        List<Object> params = new ArrayList<>();
        if (role != null && !role.isBlank()) {
            sql.append("OR (n.audience_type = 'ROLE' AND n.audience_ref_id = (SELECT id FROM roles WHERE name = ?)) ");
            params.add(role);
        }
        if (classIds != null && !classIds.isEmpty()) {
            StringBuilder in = new StringBuilder();
            for (int i = 0; i < classIds.size(); i++) in.append(i == 0 ? "?" : ",?");
            sql.append("OR (n.audience_type = 'CLASS' AND n.audience_ref_id IN (").append(in).append(")) ");
            params.addAll(classIds);
            sql.append("OR (n.audience_type = 'SUBJECT' AND n.audience_ref_id IN (SELECT subject_id FROM classes WHERE id IN (").append(in).append("))) ");
            params.addAll(classIds);
        }
        if (teacherId != null) {
            sql.append("OR (n.audience_type = 'SUBJECT' AND n.audience_ref_id IN (SELECT subject_id FROM classes WHERE teacher_id = ?)) ");
            params.add(teacherId);
        }
        if (studentId != null) {
            sql.append("OR (n.audience_type = 'SUBJECT' AND n.audience_ref_id IN (SELECT c.subject_id FROM classes c JOIN enrolments e ON e.class_id = c.id WHERE e.student_id = ? AND e.status = 'ACTIVE')) ");
            params.add(studentId);
        }
        sql.append(") ORDER BY COALESCE(n.published_at, n.created_at) DESC LIMIT ?");
        params.add(limit);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public void updateStatus(Connection conn, long id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE notices SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public long countAll(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM notices", null, 0);
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.BIGINT);
        else ps.setLong(index, value);
    }
}
