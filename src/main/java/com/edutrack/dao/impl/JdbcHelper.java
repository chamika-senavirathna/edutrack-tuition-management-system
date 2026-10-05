package com.edutrack.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Shared JDBC helpers for DAO implementations. */
final class JdbcHelper {
    private JdbcHelper() {
    }

    /** Executes an INSERT and returns the generated key. */
    static long insertAndGetKey(Connection conn, String sql, StatementBinder binder) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return -1;
    }

    /** Runs a query and maps the first row; returns null when no row. */
    static <T> T queryOne(Connection conn, String sql, StatementBinder binder, RowMapper<T> mapper) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper.map(rs) : null;
            }
        }
    }

    /** Runs a query and maps every row. */
    static <T> java.util.List<T> queryList(Connection conn, String sql, StatementBinder binder, RowMapper<T> mapper) throws SQLException {
        java.util.List<T> list = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
        }
        return list;
    }

    /** Scalar long query; returns def when no row / NULL. */
    static long queryLong(Connection conn, String sql, StatementBinder binder, long def) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long v = rs.getLong(1);
                    return rs.wasNull() ? def : v;
                }
            }
        }
        return def;
    }

    static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }

    static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
