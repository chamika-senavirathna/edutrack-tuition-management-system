package com.edutrack.dao.impl;

import com.edutrack.dao.ReceiptDAO;
import com.edutrack.model.Receipt;

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

/** JDBC implementation of ReceiptDAO (Member 04). */
public class ReceiptDAOImpl implements ReceiptDAO {

    private static final String BASE_SELECT =
            "SELECT r.*, u.full_name AS issued_by_name, p.transaction_id, f.invoice_no, s.first_name, s.last_name, s.reg_no " +
            "FROM receipts r " +
            "JOIN users u ON u.id = r.issued_by " +
            "JOIN payments p ON p.id = r.payment_id " +
            "JOIN fee_invoices f ON f.id = p.invoice_id " +
            "JOIN students s ON s.id = f.student_id ";

    private Receipt mapRow(ResultSet rs) throws SQLException {
        Receipt r = new Receipt();
        r.setId(rs.getLong("id"));
        r.setReceiptNo(rs.getString("receipt_no"));
        r.setPaymentId(rs.getLong("payment_id"));
        r.setAmount(rs.getBigDecimal("amount"));
        r.setIssuedAt(getDateTime(rs, "issued_at"));
        r.setIssuedBy(rs.getLong("issued_by"));
        r.setIssuedByName(rs.getString("issued_by_name"));
        r.setTransactionId(rs.getString("transaction_id"));
        r.setInvoiceNo(rs.getString("invoice_no"));
        r.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        r.setStudentRegNo(rs.getString("reg_no"));
        return r;
    }

    @Override
    public Receipt insert(Connection conn, Receipt r) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO receipts (receipt_no, payment_id, amount, issued_at, issued_by) VALUES (?,?,?,CURRENT_TIMESTAMP,?)",
                ps -> {
                    ps.setString(1, r.getReceiptNo());
                    ps.setLong(2, r.getPaymentId());
                    ps.setBigDecimal(3, r.getAmount());
                    ps.setLong(4, r.getIssuedBy());
                });
        r.setId(id);
        return r;
    }

    @Override
    public Receipt findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE r.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Receipt findByPaymentId(Connection conn, long paymentId) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE r.payment_id = ?", ps -> ps.setLong(1, paymentId), this::mapRow);
    }

    @Override
    public List<Receipt> search(Connection conn, String q, Long studentId, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND r.receipt_no LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (studentId != null) {
            sql.append("AND f.student_id = ? ");
            params.add(studentId);
        }
        sql.append("ORDER BY r.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q, Long studentId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM receipts r JOIN payments p ON p.id = r.payment_id " +
                "JOIN fee_invoices f ON f.id = p.invoice_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND r.receipt_no LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (studentId != null) {
            sql.append("AND f.student_id = ? ");
            params.add(studentId);
        }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public long nextSequence(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COALESCE(MAX(id), 0) + 1 FROM receipts", null, 1);
    }
}
