package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily attendance (Member 03 - Attendance Management).
 * BR-ATT-01: one entry per student per session (UNIQUE constraint).
 * BR-ATT-02: corrections need a reason within the correction window.
 * BR-ATT-03: no edits after lock without an audited admin override.
 */
public class Attendance implements Serializable {
    public static final String PRESENT = "PRESENT";
    public static final String ABSENT = "ABSENT";
    public static final String LATE = "LATE";
    public static final String EXCUSED = "EXCUSED";

    private Long id;
    private Long studentId;
    private Long classId;
    private Long timetableSlotId;
    private LocalDate attendanceDate;
    private String status;
    private String reason;
    private Long markedBy;
    private String correctionNote;      // reason for a late correction (BR-ATT-02)
    private LocalDateTime correctedAt;
    private Long correctedBy;
    private Boolean adminOverride = false;   // BR-ATT-03
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // joined
    private String studentName;
    private String studentRegNo;
    private String className;
    private String markedByName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getTimetableSlotId() { return timetableSlotId; }
    public void setTimetableSlotId(Long timetableSlotId) { this.timetableSlotId = timetableSlotId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getMarkedBy() { return markedBy; }
    public void setMarkedBy(Long markedBy) { this.markedBy = markedBy; }
    public String getCorrectionNote() { return correctionNote; }
    public void setCorrectionNote(String correctionNote) { this.correctionNote = correctionNote; }
    public LocalDateTime getCorrectedAt() { return correctedAt; }
    public void setCorrectedAt(LocalDateTime correctedAt) { this.correctedAt = correctedAt; }
    public Long getCorrectedBy() { return correctedBy; }
    public void setCorrectedBy(Long correctedBy) { this.correctedBy = correctedBy; }
    public Boolean getAdminOverride() { return adminOverride; }
    public void setAdminOverride(Boolean adminOverride) { this.adminOverride = adminOverride; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getMarkedByName() { return markedByName; }
    public void setMarkedByName(String markedByName) { this.markedByName = markedByName; }
}
