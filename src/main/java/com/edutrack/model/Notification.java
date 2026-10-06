package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * In-system notification (supporting feature - timetable changes, absence alerts,
 * payment receipts). Channels EMAIL/SMS are recorded as delivery-tracked entries
 * (Use Case Report: delivery tracked, not guaranteed).
 */
public class Notification implements Serializable {
    private Long id;
    private Long userId;          // null = role-based/broadcast
    private String roleName;      // used when userId is null
    private Long studentId;       // audience scoped to students of this guardian
    private String title;
    private String message;
    private String channel;       // IN_SYSTEM | EMAIL | SMS
    private String deliveryStatus;  // SENT | DELIVERED | FAILED
    private Long relatedEntityType;
    private String relatedEntityId;
    private Boolean read;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public Long getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(Long relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    public String getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(String relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
