package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Examination schedule entry (Member 06 - Examination & Reporting).
 * Lifecycle: SCHEDULED -> COMPLETED -> CANCELLED / ARCHIVED.
 */
public class Examination implements Serializable {
    private Long id;
    private String examName;
    private Long termId;
    private Long classId;
    private Long subjectId;
    private Long teacherId;
    private LocalDate examDate;
    private String startTime;
    private String endTime;
    private String roomText;
    private Integer maxMarks = 100;
    private String status;        // SCHEDULED | ONGOING | COMPLETED | CANCELLED | ARCHIVED
    private LocalDateTime createdAt;

    // joined
    private String termName;
    private String className;
    private String subjectName;
    private String teacherName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public Long getTermId() { return termId; }
    public void setTermId(Long termId) { this.termId = termId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getRoomText() { return roomText; }
    public void setRoomText(String roomText) { this.roomText = roomText; }
    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
}
