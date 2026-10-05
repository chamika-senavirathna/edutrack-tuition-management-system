package com.edutrack.dao.impl;

import com.edutrack.dao.PaymentDAO;
import com.edutrack.model.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.getDateTime;
import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryLong;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;
import static com.edutrack.dao.impl.JdbcHelper.setNullableString;

/** JDBC implementation of PaymentDAO (Member 04). */
public class PaymentDAOImpl implements PaymentDAO {

    private static final String BASE_SELECT =
            "SELECT p.*, f.invoice_no, s.first_name, s.last_name, s.reg_no, u.full_name AS verified_by_name, " +
            "(SELECT r.receipt_no FROM receipts r WHERE r.payment_id = p.id ORDER BY r.id LIMIT 1) AS receipt_no " +
            "FROM payments p " +
            "JOIN fee_invoices f ON f.id = p.invoice_id " +
            "JOIN students s ON s.id = f.student_id " +
            "LEFT JOIN users u ON u.id = p.verified_by ";

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setInvoiceId(rs.getLong("invoice_id"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setMethod(rs.getString("method"));
        p.setStatus(rs.getString("status"));
        Date d = rs.getDate("payment_date");
        p.setPaymentDate(d == null ? null : d.toLocalDate());
        p.setVerifiedAt(getDateTime(rs, "verified_at"));
        long vb = rs.getLong("verified_by");
        p.setVerifiedBy(rs.wasNull() ? null : vb);
        p.setFailureReason(rs.getString("failure_reason"));
        p.setRefundReason(rs.getString("refund_reason"));
        p.setNotes(rs.getString("notes"));
        p.setCreatedAt(getDateTime(rs, "created_at"));
        p.setInvoiceNo(rs.getString("invoice_no"));
        p.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        p.setStudentRegNo(rs.getString("reg_no"));
        p.setVerifiedByName(rs.getString("verified_by_name"));
        p.setReceiptNo(rs.getString("receipt_no"));
        return p;
    }

    @Override
    public Payment insert(Connection conn, Payment p) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO payments (invoice_id, transaction_id, amount, method, status, payment_date, notes, created_at) VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setLong(1, p.getInvoiceId());
                    ps.setString(2, p.getTransactionId());
                    ps.setBigDecimal(3, p.getAmount());
                    ps.setString(4, p.getMethod());
                    ps.setString(5, p.getStatus() == null ? "PENDING" : p.getStatus());
                    ps.setDate(6, p.getPaymentDate() == null ? Date.valueOf(LocalDate.now()) : Date.valueOf(p.getPaymentDate()));
                    setNullableString(ps, 7, p.getNotes());
                });
        p.setId(id);
        return p;
    }

    @Override
    public Payment update(Connection conn, Payment p) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE payments SET status=?, verified_at=?, verified_by=?, failure_reason=?, refund_reason=?, notes=? WHERE id=?")) {
            ps.setString(1, p.getStatus());
            if (p.getVerifiedAt() == null) ps.setNull(2, java.sql.Types.TIMESTAMP);
            else ps.setTimestamp(2, java.sql.Timestamp.valueOf(p.getVerifiedAt()));
            setNullableLong(ps, 3, p.getVerifiedBy());
            setNullableString(ps, 4, p.getFailureReason());
            setNullableString(ps, 5, p.getRefundReason());
            setNullableString(ps, 6, p.getNotes());
            ps.setLong(7, p.getId());
            ps.executeUpdate();
        }
        return p;
    }

    @Override
    public Payment findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE p.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Payment findByTransactionId(Connection conn, String transactionId) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE p.transaction_id = ?",
                ps -> ps.setString(1, transactionId), this::mapRow);
    }

    @Override
    public List<Payment> search(Connection conn, String q, Long invoiceId, Long studentId,
                                String status, String method, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND p.transaction_id LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (invoiceId != null) { sql.append("AND p.invoice_id = ? "); params.add(invoiceId); }
        if (studentId != null) { sql.append("AND f.student_id = ? "); params.add(studentId); }
        if (status != null && !status.isBlank()) { sql.append("AND p.status = ? "); params.add(status); }
        if (method != null && !method.isBlank()) { sql.append("AND p.method = ? "); params.add(method); }
        sql.append("ORDER BY p.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q, Long invoiceId, Long studentId,
                            String status, String method) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM payments p JOIN fee_invoices f ON f.id = p.invoice_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND p.transaction_id LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (invoiceId != null) { sql.append("AND p.invoice_id = ? "); params.add(invoiceId); }
        if (studentId != null) { sql.append("AND f.student_id = ? "); params.add(studentId); }
        if (status != null && !status.isBlank()) { sql.append("AND p.status = ? "); params.add(status); }
        if (method != null && !method.isBlank()) { sql.append("AND p.method = ? "); params.add(method); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public List<Payment> findByStudent(Connection conn, long studentId) throws SQLException {
        return queryList(conn, BASE_SELECT + "WHERE f.student_id = ? ORDER BY p.id DESC",
                ps -> ps.setLong(1, studentId), this::mapRow);
    }

    @Override
    public BigDecimal totalVerifiedBetween(Connection conn, LocalDate from, LocalDate to) throws SQLException {
        return queryList(conn,
                "SELECT COALESCE(SUM(amount),0) AS total FROM payments WHERE status IN ('VERIFIED','RECEIPTED') AND payment_date BETWEEN ? AND ?",
                ps -> {
                    ps.setDate(1, Date.valueOf(from));
                    ps.setDate(2, Date.valueOf(to));
                }, rs -> rs.getBigDecimal("total")).get(0);
    }

    @Override
    public long countByStatus(Connection conn, String status) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM payments WHERE status = ?",
                ps -> ps.setString(1, status), 0);
    }

    @Override
    public long countAll(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM payments", null, 0);
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.BIGINT);
        else ps.setLong(index, value);
    }
}
