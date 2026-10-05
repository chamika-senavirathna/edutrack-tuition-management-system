package com.edutrack.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fee invoice (Member 04 - Fee / Payment Management).
 * Lifecycle: PENDING -> PART_PAID -> PAID / VOID. History is retained (never deleted).
 */
public class FeeInvoice implements Serializable {
    private Long id;
    private String invoiceNo;
    private Long studentId;
    private Long classId;
    private Long termId;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private BigDecimal discount = BigDecimal.ZERO;
    private LocalDate dueDate;
    private LocalDate issuedDate;
    private String status;      // PENDING | PART_PAID | PAID | VOID
    private String remarks;
    // M04 <-> M03 linkage: invoice created/validated from attendance marking.
    private boolean attendanceLinked;
    private int presentCount;
    private LocalDate lastAttendanceDate;
    private LocalDateTime createdAt;

    // joined display fields
    private String studentName;
    private String studentRegNo;
    private String className;
    private String termName;

    public BigDecimal getBalance() {
        BigDecimal paid = amountPaid == null ? BigDecimal.ZERO : amountPaid;
        BigDecimal disc = discount == null ? BigDecimal.ZERO : discount;
        BigDecimal total = amount == null ? BigDecimal.ZERO : amount;
        return total.subtract(paid).subtract(disc);
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getTermId() { return termId; }
    public void setTermId(Long termId) { this.termId = termId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public boolean isAttendanceLinked() { return attendanceLinked; }
    public void setAttendanceLinked(boolean attendanceLinked) { this.attendanceLinked = attendanceLinked; }
    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
    public LocalDate getLastAttendanceDate() { return lastAttendanceDate; }
    public void setLastAttendanceDate(LocalDate lastAttendanceDate) { this.lastAttendanceDate = lastAttendanceDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }
}
