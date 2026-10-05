package com.edutrack.dao;

import com.edutrack.model.Receipt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for receipts (Member 04). */
public interface ReceiptDAO {
    Receipt insert(Connection conn, Receipt receipt) throws SQLException;

    Receipt findById(Connection conn, long id) throws SQLException;

    Receipt findByPaymentId(Connection conn, long paymentId) throws SQLException;

    List<Receipt> search(Connection conn, String q, Long studentId, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, Long studentId) throws SQLException;

    long nextSequence(Connection conn) throws SQLException;
}
