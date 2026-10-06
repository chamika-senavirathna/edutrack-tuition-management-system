package com.edutrack.service;

import com.edutrack.dao.EnrolmentDAO;
import com.edutrack.dao.FeeInvoiceDAO;
import com.edutrack.dao.PaymentDAO;
import com.edutrack.dao.ReceiptDAO;
import com.edutrack.dao.impl.EnrolmentDAOImpl;
import com.edutrack.dao.impl.FeeInvoiceDAOImpl;
import com.edutrack.dao.impl.PaymentDAOImpl;
import com.edutrack.dao.impl.ReceiptDAOImpl;
import com.edutrack.exception.AuthorizationException;
import com.edutrack.exception.BusinessRuleException;
import com.edutrack.model.Enrolment;
import com.edutrack.model.FeeInvoice;
import com.edutrack.model.Payment;
import com.edutrack.model.Receipt;
import com.edutrack.util.AuditLogger;
import com.edutrack.util.CodeGenerator;
import com.edutrack.util.DB;
import com.edutrack.util.Validator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fee / Payment Management (Member 04 - IT25103996).
 * BR-FEE-01 receipt only after verified payment; BR-FEE-02 duplicate transaction IDs
 * blocked; BR-FEE-03 refunds require authorised review; financial history retained (void, never delete).
 */
public class FeeService {

    private final FeeInvoiceDAO invoiceDAO = new FeeInvoiceDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final ReceiptDAO receiptDAO = new ReceiptDAOImpl();
    private final EnrolmentDAO enrolmentDAO = new EnrolmentDAOImpl();

    private void validateInvoiceAmounts(FeeInvoice invoice) {
        if (invoice.getAmount() == null || invoice.getAmount().signum() <= 0)
            throw new BusinessRuleException("Invoice amount must be positive.");
        if (invoice.getDiscount() == null) invoice.setDiscount(BigDecimal.ZERO);
        if (invoice.getDiscount().signum() < 0 || invoice.getDiscount().compareTo(invoice.getAmount()) > 0)
            throw new BusinessRuleException("Discount must be between zero and the invoice amount.");
    }

    // --------------------------------------------------------------- invoices

    public FeeInvoice createInvoice(com.edutrack.model.User actor, FeeInvoice invoice) {
        requireFinance(actor);
        Validator v = new Validator();
        v.required(invoice.getStudentId() == null ? null : String.valueOf(invoice.getStudentId()),
                "studentId", "Student")
                .positiveNumber(invoice.getAmount() == null ? null : invoice.getAmount().toPlainString(),
                        "amount", "Invoice amount");
        v.check();
        validateInvoiceAmounts(invoice);
        try (Connection conn = DB.getConnection()) {
            // Integration rule: fee invoice follows the student's confirmed ACTIVE enrolment.
            if (invoice.getClassId() != null) {
                Enrolment enrolment = enrolmentDAO.findActive(conn, invoice.getStudentId(), invoice.getClassId());
                if (enrolment == null) {
                    throw new BusinessRuleException(
                            "Invoice requires a confirmed active enrolment of the student in the class.");
                }
            }
            if (invoice.getTermId() == null) {
                throw new BusinessRuleException("A billing term must be selected.");
            }
            FeeInvoice duplicate = invoiceDAO.findActive(conn, invoice.getStudentId(),
                    invoice.getClassId() == null ? -1 : invoice.getClassId(), invoice.getTermId());
            if (duplicate != null) {
                throw new BusinessRuleException("An active invoice (" + duplicate.getInvoiceNo()
                        + ") already exists for this student, class and term.");
            }
            invoice.setInvoiceNo(CodeGenerator.invoiceNo(invoiceDAO.nextSequence(conn)));
            invoice.setStatus("PENDING");
            invoice.setAmountPaid(BigDecimal.ZERO);
            invoice.setIssuedDate(LocalDate.now());
            FeeInvoice saved = invoiceDAO.insert(conn, invoice);
            AuditLogger.log(conn, actor, "INVOICE_CREATE", "INVOICE", saved.getId(), null,
                    saved.getInvoiceNo() + " amount=" + saved.getAmount());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Invoice creation failed due to a database error.", e);
        }
    }

    public FeeInvoice updateInvoice(com.edutrack.model.User actor, FeeInvoice changes) {
        requireFinance(actor);
        validateInvoiceAmounts(changes);
        try (Connection conn = DB.getConnection()) {
            FeeInvoice before = invoiceDAO.findById(conn, changes.getId());
            if (before == null) throw new BusinessRuleException("Invoice not found.");
            if ("PAID".equals(before.getStatus()) || "VOID".equals(before.getStatus())) {
                throw new BusinessRuleException("Paid or void invoices cannot be edited.");
            }
            if (changes.getAmount().subtract(changes.getDiscount()).compareTo(
                    before.getAmountPaid() == null ? BigDecimal.ZERO : before.getAmountPaid()) < 0) {
                throw new BusinessRuleException("Invoice amount cannot be less than the amount already paid.");
            }
            changes.setStatus(before.getStatus());
            invoiceDAO.update(conn, changes);
            AuditLogger.log(conn, actor, "INVOICE_UPDATE", "INVOICE", changes.getId(),
                    before.getAmount() + "|" + before.getStatus(), changes.getAmount() + "|" + before.getStatus());
            return invoiceDAO.findById(conn, changes.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Invoice update failed due to a database error.", e);
        }
    }

    /** VOID keeps history (never delete financial records). */
    public void voidInvoice(com.edutrack.model.User actor, long invoiceId, String reason) {
        requireFinance(actor);
        Validator v = new Validator();
        v.required(reason, "reason", "Void reason");
        v.check();
        try (Connection conn = DB.getConnection()) {
            FeeInvoice before = invoiceDAO.findById(conn, invoiceId);
            if (before == null) throw new BusinessRuleException("Invoice not found.");
            if (before.getAmountPaid() != null && before.getAmountPaid().signum() > 0) {
                throw new BusinessRuleException("Refund all verified payments before voiding this invoice.");
            }
            invoiceDAO.updateStatus(conn, invoiceId, "VOID");
            AuditLogger.log(conn, actor, "INVOICE_VOID", "INVOICE", invoiceId,
                    before.getStatus() + " " + before.getAmount(), "VOID: " + reason);
        } catch (SQLException e) {
            throw new RuntimeException("Invoice void failed due to a database error.", e);
        }
    }

    // ------------------------------------------------- M04 <-> M03 linkage

    /**
     * M04 <-> M03 integration rule: when attendance is marked (delta = +1 per
     * attended session, -1 when a correction removes attendance), the student's
     * monthly invoice for the active term is created or kept validated against
     * the class fee structure. Runs inside the caller's transaction so billing
     * can never diverge from attendance.
     */
    public void syncAttendanceBilling(Connection conn, com.edutrack.model.User actor,
                                      long studentId, long classId, LocalDate date, int delta)
            throws SQLException {
        if (conn == null || date == null || delta == 0) return;
        Long termId = activeTermFor(conn, date);
        if (termId == null) return;                    // no billing term in session: nothing to bind
        BigDecimal monthly = monthlyRateFor(conn, classId);
        if (monthly == null) return;                   // class has no fee structure: nothing to bill

        long invoiceId = -1;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM fee_invoices " +
                "WHERE student_id = ? AND class_id = ? AND term_id = ? AND status <> 'VOID' " +
                "ORDER BY attendance_linked DESC, id DESC LIMIT 1")) {
            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            ps.setLong(3, termId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) invoiceId = rs.getLong("id");
            }
        }

        if (invoiceId == -1) {
            if (delta < 0) return;                     // nothing to decrement
            FeeInvoice invoice = new FeeInvoice();
            invoice.setInvoiceNo(CodeGenerator.invoiceNo(invoiceDAO.nextSequence(conn)));
            invoice.setStudentId(studentId);
            invoice.setClassId(classId);
            invoice.setTermId(termId);
            invoice.setAmount(monthly);
            invoice.setAmountPaid(BigDecimal.ZERO);
            invoice.setDiscount(BigDecimal.ZERO);
            invoice.setDueDate(date);
            invoice.setIssuedDate(date);
            invoice.setStatus("PENDING");
            invoice.setRemarks("Auto-generated from attendance marking (M04<->M03 linkage)");
            invoice.setAttendanceLinked(true);
            invoice.setPresentCount(1);
            invoice.setLastAttendanceDate(date);
            FeeInvoice saved = invoiceDAO.insert(conn, invoice);
            AuditLogger.log(conn, actor, "INVOICE_AUTO_CREATE", "INVOICE", saved.getId(), null,
                    saved.getInvoiceNo() + " created from attendance " + date + " (" + monthly + ")");
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE fee_invoices SET present_count = CASE WHEN present_count + ? < 0 THEN 0 ELSE present_count + ? END, " +
                    "last_attendance_date = ?, attendance_linked = 1 WHERE id = ?")) {
                ps.setInt(1, delta);
                ps.setInt(2, delta);
                ps.setDate(3, java.sql.Date.valueOf(date));
                ps.setLong(4, invoiceId);
                ps.executeUpdate();
            }
            AuditLogger.log(conn, actor,
                    delta > 0 ? "INVOICE_ATTENDANCE_PLUS" : "INVOICE_ATTENDANCE_MINUS",
                    "INVOICE", invoiceId, null, "attendance " + date + " delta=" + delta);
        }
    }

    /** Term covering the date; falls back to the currently active term for cross-term marking. */
    private Long activeTermFor(Connection conn, LocalDate date) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM terms WHERE active = 1 AND start_date <= ? AND end_date >= ? " +
                "ORDER BY start_date DESC LIMIT 1")) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM terms WHERE active = 1 ORDER BY start_date DESC LIMIT 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return null;
    }

    private BigDecimal monthlyRateFor(Connection conn, long classId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT monthly_amount FROM fee_structures WHERE class_id = ? AND active = 1")) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : null;
            }
        }
    }

    // --------------------------------------------------------------- payments

    /** Records a payment attempt linked to the invoice (PBI-13). */
    public Payment recordPayment(com.edutrack.model.User actor, long invoiceId, String transactionId,
                                 BigDecimal amount, String method, String notes) {
        requireFinance(actor);
        Validator v = new Validator();
        v.required(transactionId, "transactionId", "Transaction reference")
                .positiveNumber(amount == null ? null : amount.toPlainString(), "amount", "Payment amount")
                .required(method, "method", "Payment method");
        v.check();
        try (Connection conn = DB.getConnection()) {
            FeeInvoice invoice = invoiceDAO.findById(conn, invoiceId);
            if (invoice == null) throw new BusinessRuleException("Invoice not found.");
            if ("VOID".equals(invoice.getStatus())) {
                throw new BusinessRuleException("Cannot pay a voided invoice.");
            }
            BigDecimal balance = invoice.getBalance();
            if (amount.compareTo(balance) > 0) {
                throw new BusinessRuleException("Payment amount (" + amount + ") exceeds the invoice balance ("
                        + balance + ").");
            }
            // BR-FEE-02 duplicate transaction probe
            Payment dup = paymentDAO.findByTransactionId(conn, transactionId.trim());
            if (dup != null) {
                Payment flagged = new Payment();
                flagged.setInvoiceId(invoiceId);
                flagged.setTransactionId(transactionId.trim() + "-DUP-FLAG-" + System.currentTimeMillis());
                flagged.setAmount(amount);
                flagged.setMethod(method);
                flagged.setStatus("FAILED");
                flagged.setFailureReason("Duplicate transaction ID detected - retained for staff review (BR-FEE-02).");
                flagged.setNotes(notes);
                paymentDAO.insert(conn, flagged);
                AuditLogger.log(conn, actor, "PAYMENT_DUPLICATE_FLAGGED", "PAYMENT", flagged.getId(), null,
                        "duplicate of payment#" + dup.getId() + " txn=" + transactionId);
                throw new BusinessRuleException("Duplicate transaction ID: an existing payment ("
                        + dup.getStatus() + ") already uses this reference. The attempt has been flagged for review.");
            }
            Payment p = new Payment();
            p.setInvoiceId(invoiceId);
            p.setTransactionId(transactionId.trim());
            p.setAmount(amount);
            p.setMethod(method);
            p.setStatus("PENDING");
            p.setNotes(notes);
            Payment saved = paymentDAO.insert(conn, p);
            AuditLogger.log(conn, actor, "PAYMENT_RECORD", "PAYMENT", saved.getId(), null,
                    saved.getTransactionId() + " " + saved.getAmount());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Payment recording failed due to a database error.", e);
        }
    }

    /**
     * BR-FEE-01: verifies the payment (simulated gateway response) and issues the
     * receipt only on success. PBI-14.
     */
    public Payment verifyPayment(com.edutrack.model.User actor, long paymentId, boolean gatewayApproves) {
        requireFinance(actor);
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (var lock = conn.prepareStatement("SELECT id FROM payments WHERE id = ? FOR UPDATE")) {
                    lock.setLong(1, paymentId); try (var ignored = lock.executeQuery()) { while (ignored.next()) {} }
                }
                Payment p = paymentDAO.findById(conn, paymentId);
                if (p == null) throw new BusinessRuleException("Payment not found.");
                try (var lock = conn.prepareStatement("SELECT id FROM fee_invoices WHERE id = ? FOR UPDATE")) {
                    lock.setLong(1, p.getInvoiceId()); try (var ignored = lock.executeQuery()) { while (ignored.next()) {} }
                }
                if (!"PENDING".equals(p.getStatus())) {
                    throw new BusinessRuleException("Only pending payments can be verified (current: " + p.getStatus() + ").");
                }
                p.setVerifiedAt(java.time.LocalDateTime.now());
                p.setVerifiedBy(actor.getId());
                if (!gatewayApproves) {
                    p.setStatus("FAILED");
                    p.setFailureReason("Gateway verification declined. Retry or route to staff review.");
                    paymentDAO.update(conn, p);
                    AuditLogger.log(conn, actor, "PAYMENT_VERIFY_FAILED", "PAYMENT", paymentId,
                            "PENDING", "FAILED");
                    conn.commit();
                    return p;
                }
                FeeInvoice payable = invoiceDAO.findById(conn, p.getInvoiceId());
                if (payable == null || "VOID".equals(payable.getStatus()) || p.getAmount().compareTo(payable.getBalance()) > 0)
                    throw new BusinessRuleException("Payment exceeds the current balance or the invoice is void.");
                p.setStatus("VERIFIED");
                paymentDAO.update(conn, p);

                // Receipt generation only after verification (BR-FEE-01)
                Receipt existing = receiptDAO.findByPaymentId(conn, paymentId);
                if (existing == null) {
                    Receipt receipt = new Receipt();
                    receipt.setReceiptNo(CodeGenerator.receiptNo(receiptDAO.nextSequence(conn)));
                    receipt.setPaymentId(paymentId);
                    receipt.setAmount(p.getAmount());
                    receipt.setIssuedBy(actor.getId());
                    receiptDAO.insert(conn, receipt);
                }

                // Update invoice amount paid / status
                FeeInvoice invoice = invoiceDAO.findById(conn, p.getInvoiceId());
                BigDecimal newPaid = (invoice.getAmountPaid() == null ? BigDecimal.ZERO : invoice.getAmountPaid())
                        .add(p.getAmount());
                invoiceDAO.updateAmountPaid(conn, invoice.getId(), newPaid);
                BigDecimal balance = invoice.getAmount()
                        .subtract(invoice.getDiscount() == null ? BigDecimal.ZERO : invoice.getDiscount())
                        .subtract(newPaid);
                invoiceDAO.updateStatus(conn, invoice.getId(),
                        balance.compareTo(BigDecimal.ZERO) <= 0 ? "PAID" : (newPaid.signum() == 0 ? "PENDING" : "PART_PAID"));

                AuditLogger.log(conn, actor, "PAYMENT_VERIFIED", "PAYMENT", paymentId, "PENDING", "VERIFIED");
                conn.commit();
                return p;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Payment verification failed due to a database error.", e);
        }
    }

    /** BR-FEE-03: reviewed refund with reason; state retained. */
    public Payment refundPayment(com.edutrack.model.User actor, long paymentId, String reason) {
        requireFinance(actor);
        Validator v = new Validator();
        v.required(reason, "reason", "Refund reason");
        v.check();
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (var lock = conn.prepareStatement("SELECT id FROM payments WHERE id = ? FOR UPDATE")) {
                    lock.setLong(1, paymentId); try (var ignored = lock.executeQuery()) { while (ignored.next()) {} }
                }
                Payment p = paymentDAO.findById(conn, paymentId);
                if (p == null) throw new BusinessRuleException("Payment not found.");
                try (var lock = conn.prepareStatement("SELECT id FROM fee_invoices WHERE id = ? FOR UPDATE")) {
                    lock.setLong(1, p.getInvoiceId()); try (var ignored = lock.executeQuery()) { while (ignored.next()) {} }
                }
                if (!"VERIFIED".equals(p.getStatus()) && !"RECEIPTED".equals(p.getStatus())) {
                    throw new BusinessRuleException("Only verified payments can be refunded.");
                }
                p.setStatus("REFUNDED");
                p.setRefundReason(reason);
                paymentDAO.update(conn, p);

                FeeInvoice invoice = invoiceDAO.findById(conn, p.getInvoiceId());
                BigDecimal newPaid = (invoice.getAmountPaid() == null ? BigDecimal.ZERO : invoice.getAmountPaid())
                        .subtract(p.getAmount());
                if (newPaid.compareTo(BigDecimal.ZERO) < 0) newPaid = BigDecimal.ZERO;
                invoiceDAO.updateAmountPaid(conn, invoice.getId(), newPaid);
                BigDecimal balance = invoice.getAmount()
                        .subtract(invoice.getDiscount() == null ? BigDecimal.ZERO : invoice.getDiscount())
                        .subtract(newPaid);
                invoiceDAO.updateStatus(conn, invoice.getId(),
                        balance.compareTo(BigDecimal.ZERO) <= 0 ? "PAID" : (newPaid.signum() == 0 ? "PENDING" : "PART_PAID"));

                AuditLogger.log(conn, actor, "PAYMENT_REFUND", "PAYMENT", paymentId,
                        "VERIFIED " + p.getAmount(), "REFUNDED: " + reason);
                conn.commit();
                return p;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Refund failed due to a database error.", e);
        }
    }

    // --------------------------------------------------------------- queries

    public List<FeeInvoice> searchInvoices(String q, Long studentId, Long classId, Long termId,
                                           String status, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return invoiceDAO.search(conn, emptyToNull(q), studentId, classId, termId, emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search invoices.", e);
        }
    }

    public long countInvoices(String q, Long studentId, Long classId, Long termId, String status) {
        try (Connection conn = DB.getConnection()) {
            return invoiceDAO.countSearch(conn, emptyToNull(q), studentId, classId, termId, emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not count invoices.", e);
        }
    }

    public List<FeeInvoice> invoicesOfStudent(long studentId) {
        try (Connection conn = DB.getConnection()) {
            return invoiceDAO.findByStudent(conn, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load invoices.", e);
        }
    }

    public FeeInvoice findInvoice(long id) {
        try (Connection conn = DB.getConnection()) {
            return invoiceDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load invoice.", e);
        }
    }

    public List<Payment> searchPayments(String q, Long invoiceId, Long studentId, String status,
                                        String method, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return paymentDAO.search(conn, emptyToNull(q), invoiceId, studentId, emptyToNull(status),
                    emptyToNull(method), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search payments.", e);
        }
    }

    public long countPayments(String q, Long invoiceId, Long studentId, String status, String method) {
        try (Connection conn = DB.getConnection()) {
            return paymentDAO.countSearch(conn, emptyToNull(q), invoiceId, studentId, emptyToNull(status),
                    emptyToNull(method));
        } catch (SQLException e) {
            throw new RuntimeException("Could not count payments.", e);
        }
    }

    public List<Payment> paymentsOfStudent(long studentId) {
        try (Connection conn = DB.getConnection()) {
            return paymentDAO.findByStudent(conn, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load payments.", e);
        }
    }

    public Payment findPayment(long id) {
        try (Connection conn = DB.getConnection()) {
            return paymentDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load payment.", e);
        }
    }

    public List<Receipt> searchReceipts(String q, Long studentId, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return receiptDAO.search(conn, emptyToNull(q), studentId, offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search receipts.", e);
        }
    }

    public long countReceipts(String q, Long studentId) {
        try (Connection conn = DB.getConnection()) {
            return receiptDAO.countSearch(conn, emptyToNull(q), studentId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not count receipts.", e);
        }
    }

    public Receipt findReceipt(long id) {
        try (Connection conn = DB.getConnection()) {
            return receiptDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load receipt.", e);
        }
    }

    public Receipt receiptOfPayment(long paymentId) {
        try (Connection conn = DB.getConnection()) {
            return receiptDAO.findByPaymentId(conn, paymentId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load receipt.", e);
        }
    }

    /** Finance dashboard totals: [invoiced, collected, outstanding]. */
    public BigDecimal[] totals(Long termId) {
        try (Connection conn = DB.getConnection()) {
            return invoiceDAO.totals(conn, termId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load finance totals.", e);
        }
    }

    private void requireFinance(com.edutrack.model.User actor) {
        if (actor == null) throw new AuthorizationException("Not authenticated.");
        if (!(actor.hasRole("FINANCE") || actor.hasRole("ADMIN") || actor.hasRole("PRINCIPAL"))) {
            throw new AuthorizationException("Only finance officers can manage fees and payments.");
        }
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
