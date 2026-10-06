package com.edutrack.dao.impl;

import com.edutrack.dao.TeachingMaterialDAO;
import com.edutrack.model.TeachingMaterial;

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

/** JDBC implementation of TeachingMaterialDAO (Member 02). */
public class TeachingMaterialDAOImpl implements TeachingMaterialDAO {

    private static final String BASE_SELECT =
            "SELECT m.*, c.name AS class_name, sub.name AS subject_name, u.full_name AS uploaded_by_name " +
            "FROM teaching_materials m " +
            "LEFT JOIN classes c ON c.id = m.class_id " +
            "LEFT JOIN subjects sub ON sub.id = m.subject_id " +
            "LEFT JOIN users u ON u.id = m.uploaded_by ";

    private TeachingMaterial mapRow(ResultSet rs) throws SQLException {
        TeachingMaterial m = new TeachingMaterial();
        m.setId(rs.getLong("id"));
        long cid = rs.getLong("class_id");
        m.setClassId(rs.wasNull() ? null : cid);
        long sid = rs.getLong("subject_id");
        m.setSubjectId(rs.wasNull() ? null : sid);
        m.setUploadedBy(rs.getLong("uploaded_by"));
        m.setTitle(rs.getString("title"));
        m.setDescription(rs.getString("description"));
        m.setFilePath(rs.getString("file_path"));
        m.setFileName(rs.getString("file_name"));
        m.setStatus(rs.getString("status"));
        m.setCreatedAt(getDateTime(rs, "created_at"));
        m.setClassName(rs.getString("class_name"));
        m.setSubjectName(rs.getString("subject_name"));
        m.setUploadedByName(rs.getString("uploaded_by_name"));
        return m;
    }

    @Override
    public TeachingMaterial insert(Connection conn, TeachingMaterial m) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO teaching_materials (class_id, subject_id, uploaded_by, title, description, file_path, file_name, status, created_at) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    setNullableLong(ps, 1, m.getClassId());
                    setNullableLong(ps, 2, m.getSubjectId());
                    ps.setLong(3, m.getUploadedBy());
                    ps.setString(4, m.getTitle());
                    setNullableString(ps, 5, m.getDescription());
                    setNullableString(ps, 6, m.getFilePath());
                    setNullableString(ps, 7, m.getFileName());
                    ps.setString(8, m.getStatus() == null ? "ACTIVE" : m.getStatus());
                });
        m.setId(id);
        return m;
    }

    @Override
    public void update(Connection conn, TeachingMaterial m) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE teaching_materials SET title=?, description=?, status=?, class_id=?, subject_id=?, file_path=?, file_name=? WHERE id=?")) {
            ps.setString(1, m.getTitle());
            setNullableString(ps, 2, m.getDescription());
            ps.setString(3, m.getStatus());
            setNullableLong(ps, 4, m.getClassId());
            setNullableLong(ps, 5, m.getSubjectId());
            ps.setString(6, m.getFilePath());
            ps.setString(7, m.getFileName());
            ps.setLong(8, m.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public TeachingMaterial findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE m.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public List<TeachingMaterial> search(Connection conn, String q, Long classId, Long subjectId,
                                         String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND m.title LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (classId != null) { sql.append("AND m.class_id = ? "); params.add(classId); }
        if (subjectId != null) { sql.append("AND m.subject_id = ? "); params.add(subjectId); }
        if (status != null && !status.isBlank()) { sql.append("AND m.status = ? "); params.add(status); }
        sql.append("ORDER BY m.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q, Long classId, Long subjectId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM teaching_materials m WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND m.title LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (classId != null) { sql.append("AND m.class_id = ? "); params.add(classId); }
        if (subjectId != null) { sql.append("AND m.subject_id = ? "); params.add(subjectId); }
        if (status != null && !status.isBlank()) { sql.append("AND m.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public List<TeachingMaterial> findByClasses(Connection conn, List<Long> classIds) throws SQLException {
        if (classIds == null || classIds.isEmpty()) return new ArrayList<>();
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < classIds.size(); i++) in.append(i == 0 ? "?" : ",?");
        String sql = BASE_SELECT + "WHERE m.class_id IN (" + in + ") AND m.status = 'ACTIVE' ORDER BY m.created_at DESC";
        return queryList(conn, sql, ps -> {
            for (int i = 0; i < classIds.size(); i++) ps.setLong(i + 1, classIds.get(i));
        }, this::mapRow);
    }

    @Override
    public void updateStatus(Connection conn, long id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE teaching_materials SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.BIGINT);
        else ps.setLong(index, value);
    }
}
