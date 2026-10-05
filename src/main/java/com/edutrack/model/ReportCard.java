package com.edutrack.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Generated report card (Member 06). Published cards are immutable; corrections
 * need authorised re-publish (BR-EXM-03).
 */
public class ReportCard implements Serializable {
    private Long id;
    private Long studentId;
    private Long termId;
    private BigDecimal averageMarks;
    private String overallGrade;
    private Integer attendancePercent;
    private String status;           // GENERATED | PUBLISHED
    private LocalDateTime publishedAt;
    private LocalDateTime generatedAt;

    // joined
    private String studentName;
    private String studentRegNo;
    private String className;
    private String termName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getTermId() { return termId; }
    public void setTermId(Long termId) { this.termId = termId; }
    public BigDecimal getAverageMarks() { return averageMarks; }
    public void setAverageMarks(BigDecimal averageMarks) { this.averageMarks = averageMarks; }
    public String getOverallGrade() { return overallGrade; }
    public void setOverallGrade(String overallGrade) { this.overallGrade = overallGrade; }
    public Integer getAttendancePercent() { return attendancePercent; }
    public void setAttendancePercent(Integer attendancePercent) { this.attendancePercent = attendancePercent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }
}
