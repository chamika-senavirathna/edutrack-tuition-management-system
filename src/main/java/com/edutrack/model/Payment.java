package com.edutrack.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment attempt against a fee invoice (Member 04).
 * States: PENDING -> VERIFYING -> VERIFIED -> RECEIPTED; FAILED / REFUNDED.
 * BR-FEE-01: a receipt follows only a verified payment.
 * BR-FEE-02: duplicate transaction IDs are blocked.
 */
public class Payment implements Serializable {
    private Long id;
    private Long invoiceId;
    private String transactionId;    // gateway/bank reference - UNIQUE
    private BigDecimal amount;
    private String method;           // CASH | BANK_TRANSFER | CARD | ONLINE
    private String status;           // PENDING | VERIFYING | VERIFIED | FAILED | REFUNDED
    private LocalDate paymentDate;
    private LocalDateTime verifiedAt;
    private Long verifiedBy;         // finance user id
    private String failureReason;
    private String refundReason;
    private String notes;
    private LocalDateTime createdAt;

    // joined display fields
    private String invoiceNo;
    private String studentName;
    private String studentRegNo;
    private String verifiedByName;
    private String receiptNo;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public Long getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(Long verifiedBy) { this.verifiedBy = verifiedBy; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getRefundReason() { return refundReason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
    public String getVerifiedByName() { return verifiedByName; }
    public void setVerifiedByName(String verifiedByName) { this.verifiedByName = verifiedByName; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
}
