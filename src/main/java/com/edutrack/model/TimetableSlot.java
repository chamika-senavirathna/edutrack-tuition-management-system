package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Weekly timetable slot (Member 02).
 * BR-TTC-02: slots follow DRAFT -> SUBMITTED -> APPROVED -> PUBLISHED.
 * BR-TTC-01: two slots cannot share the same teacher/room/time (enforced in service).
 */
public class TimetableSlot implements Serializable {
    public static final String[] DAYS = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
    private Long id;
    private Long classId;
    private Long subjectId;
    private Long teacherId;
    private Long roomId;
    private String dayOfWeek;      // MONDAY..SUNDAY
    private String startTime;      // HH:MM
    private String endTime;        // HH:MM
    private String status;         // DRAFT | SUBMITTED | APPROVED | PUBLISHED | ARCHIVED
    private LocalDateTime publishedAt;

    // joined display
    private String className;
    private String subjectName;
    private String teacherName;
    private String roomName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
}
