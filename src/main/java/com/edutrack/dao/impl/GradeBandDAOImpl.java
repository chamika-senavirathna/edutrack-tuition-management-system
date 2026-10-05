package com.edutrack.dao.impl;

import com.edutrack.dao.GradeBandDAO;
import com.edutrack.model.GradeBand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;

/** JDBC implementation of GradeBandDAO (Member 06). */
public class GradeBandDAOImpl implements GradeBandDAO {

    private GradeBand mapRow(ResultSet rs) throws SQLException {
        GradeBand g = new GradeBand();
        g.setId(rs.getLong("id"));
        g.setGrade(rs.getString("grade"));
        g.setMinMark(rs.getInt("min_mark"));
        g.setMaxMark(rs.getInt("max_mark"));
        g.setDescription(rs.getString("description"));
        return g;
    }

    @Override
    public List<GradeBand> findAll(Connection conn) throws SQLException {
        return queryList(conn, "SELECT * FROM grade_bands ORDER BY min_mark DESC", null, this::mapRow);
    }

    @Override
    public GradeBand findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, "SELECT * FROM grade_bands WHERE id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public GradeBand insert(Connection conn, GradeBand g) throws SQLException {
        long id = insertAndGetKey(conn, "INSERT INTO grade_bands (grade, min_mark, max_mark, description) VALUES (?,?,?,?)",
                ps -> {
                    ps.setString(1, g.getGrade());
                    ps.setInt(2, g.getMinMark());
                    ps.setInt(3, g.getMaxMark());
                    ps.setString(4, g.getDescription());
                });
        g.setId(id);
        return g;
    }

    @Override
    public void update(Connection conn, GradeBand g) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE grade_bands SET grade=?, min_mark=?, max_mark=?, description=? WHERE id=?")) {
            ps.setString(1, g.getGrade());
            ps.setInt(2, g.getMinMark());
            ps.setInt(3, g.getMaxMark());
            ps.setString(4, g.getDescription());
            ps.setLong(5, g.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public GradeBand findByMark(Connection conn, int mark) throws SQLException {
        return queryOne(conn, "SELECT * FROM grade_bands WHERE ? BETWEEN min_mark AND max_mark LIMIT 1",
                ps -> ps.setInt(1, mark), this::mapRow);
    }
}
