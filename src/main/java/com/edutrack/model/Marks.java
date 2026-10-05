package com.edutrack.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Marks for one student in one exam (Member 06).
 * BR-EXM-01: entry restricted to the assigned subject teacher.
 * BR-EXM-02: moderation/approval precedes publication.
 * Workflow: DRAFT -> SUBMITTED -> MODERATED -> PUBLISHED.
 */
public class Marks implements Serializable {
    public static final String DRAFT = "DRAFT";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String MODERATED = "MODERATED";
    public static final String PUBLISHED = "PUBLISHED";

    private Long id;
    private Long examId;
    private Long studentId;
    private BigDecimal marks;
    private BigDecimal maxMarks;
    private String grade;            // auto-calculated from grade_bands (PBI-22)
    private String remarks;
    private String status;           // DRAFT | SUBMITTED | MODERATED | PUBLISHED
    private Long enteredBy;
    private LocalDateTime enteredAt;
    private Long moderatedBy;
    private LocalDateTime moderatedAt;
    private String moderationNote;
    private LocalDateTime publishedAt;

    // joined
    private String studentName;
    private String studentRegNo;
    private String examName;
    private String subjectName;
    private String className;
    private String enteredByName;
    private String moderatedByName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public BigDecimal getMarks() { return marks; }
    public void setMarks(BigDecimal marks) { this.marks = marks; }
    public BigDecimal getMaxMarks() { return maxMarks; }
    public void setMaxMarks(BigDecimal maxMarks) { this.maxMarks = maxMarks; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getEnteredBy() { return enteredBy; }
    public void setEnteredBy(Long enteredBy) { this.enteredBy = enteredBy; }
    public LocalDateTime getEnteredAt() { return enteredAt; }
    public void setEnteredAt(LocalDateTime enteredAt) { this.enteredAt = enteredAt; }
    public Long getModeratedBy() { return moderatedBy; }
    public void setModeratedBy(Long moderatedBy) { this.moderatedBy = moderatedBy; }
    public LocalDateTime getModeratedAt() { return moderatedAt; }
    public void setModeratedAt(LocalDateTime moderatedAt) { this.moderatedAt = moderatedAt; }
    public String getModerationNote() { return moderationNote; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getEnteredByName() { return enteredByName; }
    public void setEnteredByName(String enteredByName) { this.enteredByName = enteredByName; }
    public String getModeratedByName() { return moderatedByName; }
    public void setModeratedByName(String moderatedByName) { this.moderatedByName = moderatedByName; }
}
