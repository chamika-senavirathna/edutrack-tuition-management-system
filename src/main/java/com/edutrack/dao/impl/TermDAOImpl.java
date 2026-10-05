package com.edutrack.dao.impl;

import com.edutrack.dao.TermDAO;
import com.edutrack.model.Term;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;

/** JDBC implementation of TermDAO (shared). */
public class TermDAOImpl implements TermDAO {

    private Term mapRow(ResultSet rs) throws SQLException {
        Term t = new Term();
        t.setId(rs.getLong("id"));
        t.setName(rs.getString("name"));
        Date s = rs.getDate("start_date");
        t.setStartDate(s == null ? null : s.toLocalDate());
        Date e = rs.getDate("end_date");
        t.setEndDate(e == null ? null : e.toLocalDate());
        t.setActive(rs.getBoolean("active"));
        return t;
    }

    @Override
    public List<Term> findAll(Connection conn) throws SQLException {
        return queryList(conn, "SELECT * FROM terms ORDER BY start_date DESC", null, this::mapRow);
    }

    @Override
    public Term findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, "SELECT * FROM terms WHERE id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Term findActive(Connection conn) throws SQLException {
        return queryOne(conn, "SELECT * FROM terms WHERE active = 1 LIMIT 1", null, this::mapRow);
    }

    @Override
    public Term insert(Connection conn, Term t) throws SQLException {
        long id = insertAndGetKey(conn, "INSERT INTO terms (name, start_date, end_date, active) VALUES (?,?,?,?)",
                ps -> {
                    ps.setString(1, t.getName());
                    ps.setDate(2, Date.valueOf(t.getStartDate()));
                    ps.setDate(3, Date.valueOf(t.getEndDate()));
                    ps.setBoolean(4, t.getActive() != null && t.getActive());
                });
        t.setId(id);
        return t;
    }

    @Override
    public void update(Connection conn, Term t) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE terms SET name=?, start_date=?, end_date=?, active=? WHERE id=?")) {
            ps.setString(1, t.getName());
            ps.setDate(2, Date.valueOf(t.getStartDate()));
            ps.setDate(3, Date.valueOf(t.getEndDate()));
            ps.setBoolean(4, t.getActive() != null && t.getActive());
            ps.setLong(5, t.getId());
            ps.executeUpdate();
        }
    }
}
