package com.edutrack.service;

import com.edutrack.util.DB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reporting & Analytics (Member 06 owns reporting; used by all dashboards).
 * Report catalog (URF appendix C): daily attendance, fee collection, exam
 * performance, timetable utilisation, notice delivery. Access is role-restricted.
 */
public class ReportService {

    /** Daily attendance counts per class over a range. */
    public List<Map<String, Object>> dailyAttendance(LocalDate from, LocalDate to) {
        String sql = "SELECT c.name AS class_name, a.attendance_date, " +
                "SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END) AS present, " +
                "SUM(CASE WHEN a.status='ABSENT' THEN 1 ELSE 0 END) AS absent, " +
                "SUM(CASE WHEN a.status='LATE' THEN 1 ELSE 0 END) AS late " +
                "FROM attendance a JOIN classes c ON c.id = a.class_id " +
                "WHERE a.attendance_date BETWEEN ? AND ? " +
                "GROUP BY c.name, a.attendance_date ORDER BY a.attendance_date DESC, c.name LIMIT 200";
        return fetch(sql, ps -> {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
        });
    }

    /** Fee collection summary per term. */
    public List<Map<String, Object>> feeCollection() {
        String sql = "SELECT t.name AS term, SUM(f.amount - f.discount) AS invoiced, " +
                "SUM(f.amount_paid) AS collected, SUM(f.amount - f.discount - f.amount_paid) AS outstanding " +
                "FROM fee_invoices f LEFT JOIN terms t ON t.id = f.term_id " +
                "WHERE f.status <> 'VOID' GROUP BY t.name ORDER BY t.name";
        return fetch(sql, null);
    }

    /**
     * Monthly attendance percentage per class (M03) for the admin reporting view:
     * entries, sessions attended (PRESENT + LATE) and attendance percentage.
     */
    public List<Map<String, Object>> monthlyAttendanceSummary(int year, int month) {
        String sql = "SELECT c.name AS className, " +
                "COUNT(a.id) AS entries, " +
                "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present, " +
                "SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END) AS late, " +
                "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent, " +
                "SUM(CASE WHEN a.status IN ('PRESENT','LATE') THEN 1 ELSE 0 END) AS attended, " +
                "CASE WHEN COUNT(a.id) = 0 THEN NULL ELSE " +
                "ROUND(100 * SUM(CASE WHEN a.status IN ('PRESENT','LATE') THEN 1 ELSE 0 END) / COUNT(a.id), 1) " +
                "END AS attendancePct " +
                "FROM attendance a JOIN classes c ON c.id = a.class_id " +
                "WHERE YEAR(a.attendance_date) = ? AND MONTH(a.attendance_date) = ? " +
                "GROUP BY c.name ORDER BY c.name";
        return fetch(sql, ps -> {
            ps.setInt(1, year);
            ps.setInt(2, month);
        });
    }

    /** Verified revenue collections in a month, broken down by payment method. */
    public List<Map<String, Object>> revenueSummary(int year, int month) {
        String sql = "SELECT p.method, COUNT(p.id) AS txns, COALESCE(SUM(p.amount), 0) AS amount " +
                "FROM payments p WHERE p.status = 'VERIFIED' " +
                "AND YEAR(p.payment_date) = ? AND MONTH(p.payment_date) = ? " +
                "GROUP BY p.method ORDER BY amount DESC";
        return fetch(sql, ps -> {
            ps.setInt(1, year);
            ps.setInt(2, month);
        });
    }

    /** Total verified collections for a month (M04). */
    public BigDecimal revenueTotal(int year, int month) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments " +
                "WHERE status = 'VERIFIED' AND YEAR(payment_date) = ? AND MONTH(payment_date) = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Revenue total failed due to a database error.", e);
        }
    }

    /** Timetable utilisation: slot count per teacher/room. */
    public List<Map<String, Object>> timetableUtilisation() {
        String sql = "SELECT te.full_name AS teacher, COUNT(t.id) AS slots " +
                "FROM timetable t LEFT JOIN teachers te ON te.id = t.teacher_id " +
                "WHERE t.status <> 'ARCHIVED' GROUP BY te.full_name ORDER BY slots DESC";
        return fetch(sql, null);
    }

    /** Attendance trend for one student (student/parent dashboard). */
    public Map<String, Long> attendanceTrend(long studentId) {
        Map<String, Long> trend = new LinkedHashMap<>();
        trend.put("present", 0L);
        trend.put("absent", 0L);
        trend.put("late", 0L);
        trend.put("excused", 0L);
        String sql = "SELECT status, COUNT(*) AS c FROM attendance WHERE student_id = ? GROUP BY status";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trend.put(rs.getString("status").toLowerCase(), rs.getLong("c"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Attendance trend failed due to a database error.", e);
        }
        return trend;
    }

    /** Institute-wide dashboard counters in a single pass. */
    public Map<String, Long> instituteStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        String[] queries = {
                "students", "SELECT COUNT(*) FROM students WHERE status = 'ACTIVE'",
                "teachers", "SELECT COUNT(*) FROM teachers WHERE status = 'ACTIVE'",
                "classes", "SELECT COUNT(*) FROM classes WHERE status = 'ACTIVE'",
                "users", "SELECT COUNT(*) FROM users WHERE active = 1",
                "invoices", "SELECT COUNT(*) FROM fee_invoices WHERE status <> 'VOID'",
                "outstanding", "SELECT COUNT(*) FROM fee_invoices WHERE status IN ('PENDING','PART_PAID')",
                "exams", "SELECT COUNT(*) FROM examinations WHERE status = 'SCHEDULED'",
                "notices", "SELECT COUNT(*) FROM notices WHERE status = 'PUBLISHED'",
                "marksPending", "SELECT COUNT(*) FROM marks WHERE status = 'SUBMITTED'",
                "paymentsPending", "SELECT COUNT(*) FROM payments WHERE status = 'PENDING'"
        };
        try (Connection conn = DB.getConnection()) {
            for (int i = 0; i < queries.length; i += 2) {
                try (PreparedStatement ps = conn.prepareStatement(queries[i + 1]);
                     ResultSet rs = ps.executeQuery()) {
                    stats.put(queries[i], rs.next() ? rs.getLong(1) : 0L);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Institute stats failed due to a database error.", e);
        }
        return stats;
    }

    /** Finance totals for the finance dashboard. */
    public BigDecimal[] financeTotals() {
        String sql = "SELECT COALESCE(SUM(amount - discount),0) AS invoiced, " +
                "COALESCE(SUM(amount_paid),0) AS collected, " +
                "COALESCE(SUM(amount - discount - amount_paid),0) AS outstanding " +
                "FROM fee_invoices WHERE status <> 'VOID'";
        BigDecimal[] out = new BigDecimal[3];
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                out[0] = rs.getBigDecimal("invoiced");
                out[1] = rs.getBigDecimal("collected");
                out[2] = rs.getBigDecimal("outstanding");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Finance totals failed due to a database error.", e);
        }
        return out;
    }

    private List<Map<String, Object>> fetch(String sql, StatementBinder binder) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Report query failed due to a database error.", e);
        }
        return rows;
    }

    interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
