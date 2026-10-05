package com.edutrack.dao.impl;

import com.edutrack.dao.FeeInvoiceDAO;
import com.edutrack.model.FeeInvoice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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

/** JDBC implementation of FeeInvoiceDAO (Member 04). */
public class FeeInvoiceDAOImpl implements FeeInvoiceDAO {

    private static final String BASE_SELECT =
            "SELECT f.*, s.first_name, s.last_name, s.reg_no, c.name AS class_name, t.name AS term_name " +
            "FROM fee_invoices f " +
            "JOIN students s ON s.id = f.student_id " +
            "LEFT JOIN classes c ON c.id = f.class_id " +
            "LEFT JOIN terms t ON t.id = f.term_id ";

    private FeeInvoice mapRow(ResultSet rs) throws SQLException {
        FeeInvoice f = new FeeInvoice();
        f.setId(rs.getLong("id"));
        f.setInvoiceNo(rs.getString("invoice_no"));
        f.setStudentId(rs.getLong("student_id"));
        long cid = rs.getLong("class_id");
        f.setClassId(rs.wasNull() ? null : cid);
        long tid = rs.getLong("term_id");
        f.setTermId(rs.wasNull() ? null : tid);
        f.setAmount(rs.getBigDecimal("amount"));
        f.setAmountPaid(rs.getBigDecimal("amount_paid"));
        f.setDiscount(rs.getBigDecimal("discount"));
        Date due = rs.getDate("due_date");
        f.setDueDate(due == null ? null : due.toLocalDate());
        Date issued = rs.getDate("issued_date");
        f.setIssuedDate(issued == null ? null : issued.toLocalDate());
        f.setStatus(rs.getString("status"));
        f.setRemarks(rs.getString("remarks"));
        f.setAttendanceLinked(rs.getBoolean("attendance_linked"));
        f.setPresentCount(rs.getInt("present_count"));
        Date lastAtt = rs.getDate("last_attendance_date");
        f.setLastAttendanceDate(lastAtt == null ? null : lastAtt.toLocalDate());
        f.setCreatedAt(getDateTime(rs, "created_at"));
        f.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        f.setStudentRegNo(rs.getString("reg_no"));
        f.setClassName(rs.getString("class_name"));
        f.setTermName(rs.getString("term_name"));
        return f;
    }

    @Override
    public FeeInvoice insert(Connection conn, FeeInvoice f) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO fee_invoices (invoice_no, student_id, class_id, term_id, amount, amount_paid, discount, due_date, issued_date, status, remarks, attendance_linked, present_count, last_attendance_date, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setString(1, f.getInvoiceNo());
                    ps.setLong(2, f.getStudentId());
                    setNullableLong(ps, 3, f.getClassId());
                    setNullableLong(ps, 4, f.getTermId());
                    ps.setBigDecimal(5, f.getAmount());
                    ps.setBigDecimal(6, f.getAmountPaid() == null ? BigDecimal.ZERO : f.getAmountPaid());
                    ps.setBigDecimal(7, f.getDiscount() == null ? BigDecimal.ZERO : f.getDiscount());
                    setNullableDate(ps, 8, f.getDueDate());
                    setNullableDate(ps, 9, f.getIssuedDate());
                    ps.setString(10, f.getStatus() == null ? "PENDING" : f.getStatus());
                    setNullableString(ps, 11, f.getRemarks());
                    ps.setBoolean(12, f.isAttendanceLinked());
                    ps.setInt(13, f.getPresentCount());
                    setNullableDate(ps, 14, f.getLastAttendanceDate());
                });
        f.setId(id);
        return f;
    }

    @Override
    public void update(Connection conn, FeeInvoice f) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE fee_invoices SET amount=?, discount=?, due_date=?, status=?, remarks=? WHERE id=?")) {
            ps.setBigDecimal(1, f.getAmount());
            ps.setBigDecimal(2, f.getDiscount() == null ? BigDecimal.ZERO : f.getDiscount());
            setNullableDate(ps, 3, f.getDueDate());
            ps.setString(4, f.getStatus());
            setNullableString(ps, 5, f.getRemarks());
            ps.setLong(6, f.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public FeeInvoice findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE f.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public FeeInvoice findActive(Connection conn, long studentId, long classId, long termId) throws SQLException {
        return queryOne(conn, BASE_SELECT +
                        "WHERE f.student_id = ? AND f.class_id = ? AND f.term_id = ? AND f.status <> 'VOID'",
                ps -> {
                    ps.setLong(1, studentId);
                    ps.setLong(2, classId);
                    ps.setLong(3, termId);
                }, this::mapRow);
    }

    @Override
    public List<FeeInvoice> search(Connection conn, String q, Long studentId, Long classId, Long termId,
                                   String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND f.invoice_no LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (studentId != null) { sql.append("AND f.student_id = ? "); params.add(studentId); }
        if (classId != null) { sql.append("AND f.class_id = ? "); params.add(classId); }
        if (termId != null) { sql.append("AND f.term_id = ? "); params.add(termId); }
        if (status != null && !status.isBlank()) { sql.append("AND f.status = ? "); params.add(status); }
        sql.append("ORDER BY f.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q, Long studentId, Long classId, Long termId,
                            String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM fee_invoices f WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND f.invoice_no LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (studentId != null) { sql.append("AND f.student_id = ? "); params.add(studentId); }
        if (classId != null) { sql.append("AND f.class_id = ? "); params.add(classId); }
        if (termId != null) { sql.append("AND f.term_id = ? "); params.add(termId); }
        if (status != null && !status.isBlank()) { sql.append("AND f.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public List<FeeInvoice> findByStudent(Connection conn, long studentId) throws SQLException {
        return queryList(conn, BASE_SELECT + "WHERE f.student_id = ? ORDER BY f.id DESC",
                ps -> ps.setLong(1, studentId), this::mapRow);
    }

    @Override
    public void updateStatus(Connection conn, long id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE fee_invoices SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateAmountPaid(Connection conn, long id, BigDecimal amountPaid) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE fee_invoices SET amount_paid=? WHERE id=?")) {
            ps.setBigDecimal(1, amountPaid);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public BigDecimal[] totals(Connection conn, Long termId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(amount - discount),0) AS invoiced, " +
                "COALESCE(SUM(amount_paid),0) AS collected, " +
                "COALESCE(SUM(amount - discount - amount_paid),0) AS outstanding " +
                "FROM fee_invoices WHERE status <> 'VOID' ");
        List<Object> params = new ArrayList<>();
        if (termId != null) { sql.append("AND term_id = ? "); params.add(termId); }
        final BigDecimal[] result = new BigDecimal[3];
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getBigDecimal("invoiced");
                    result[1] = rs.getBigDecimal("collected");
                    result[2] = rs.getBigDecimal("outstanding");
                }
            }
        }
        return result;
    }

    @Override
    public long countAll(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM fee_invoices", null, 0);
    }

    @Override
    public long nextSequence(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COALESCE(MAX(id), 0) + 1 FROM fee_invoices", null, 1);
    }

    private void setNullableDate(PreparedStatement ps, int index, java.time.LocalDate date) throws SQLException {
        if (date == null) ps.setNull(index, java.sql.Types.DATE);
        else ps.setDate(index, Date.valueOf(date));
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.BIGINT);
        else ps.setLong(index, value);
    }
}
