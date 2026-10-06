package com.edutrack.dao;

import com.edutrack.model.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for payments (Member 04). */
public interface PaymentDAO {
    Payment insert(Connection conn, Payment payment) throws SQLException;

    Payment update(Connection conn, Payment payment) throws SQLException;

    Payment findById(Connection conn, long id) throws SQLException;

    /** BR-FEE-02 duplicate probe - any existing payment with this transaction ID. */
    Payment findByTransactionId(Connection conn, String transactionId) throws SQLException;

    List<Payment> search(Connection conn, String q, Long invoiceId, Long studentId,
                         String status, String method, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, Long invoiceId, Long studentId,
                     String status, String method) throws SQLException;

    List<Payment> findByStudent(Connection conn, long studentId) throws SQLException;

    BigDecimal totalVerifiedBetween(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws SQLException;

    long countByStatus(Connection conn, String status) throws SQLException;

    long countAll(Connection conn) throws SQLException;
}
