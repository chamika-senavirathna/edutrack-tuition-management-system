package com.edutrack.dao;

import com.edutrack.model.FeeInvoice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for fee invoices (Member 04 - Fee / Payment Management). */
public interface FeeInvoiceDAO {
    FeeInvoice insert(Connection conn, FeeInvoice invoice) throws SQLException;

    void update(Connection conn, FeeInvoice invoice) throws SQLException;

    FeeInvoice findById(Connection conn, long id) throws SQLException;

    /** Active (non-void) invoice for a student+class+term (prevents duplicate billing). */
    FeeInvoice findActive(Connection conn, long studentId, long classId, long termId) throws SQLException;

    List<FeeInvoice> search(Connection conn, String q, Long studentId, Long classId, Long termId,
                            String status, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, Long studentId, Long classId, Long termId,
                     String status) throws SQLException;

    List<FeeInvoice> findByStudent(Connection conn, long studentId) throws SQLException;

    void updateStatus(Connection conn, long id, String status) throws SQLException;

    void updateAmountPaid(Connection conn, long id, java.math.BigDecimal amountPaid) throws SQLException;

    /** Financial summary for dashboards: total invoiced, collected, outstanding. */
    java.math.BigDecimal[] totals(Connection conn, Long termId) throws SQLException;

    long countAll(Connection conn) throws SQLException;

    long nextSequence(Connection conn) throws SQLException;
}
