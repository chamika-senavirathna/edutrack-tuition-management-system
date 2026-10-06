package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Notice / communication (Member 02 - absorbed the former Module 07).
 * Audience: ALL | CLASS | SUBJECT | ROLE | INDIVIDUAL. Channels: IN_SYSTEM, EMAIL, SMS.
 */
public class Notice implements Serializable {
    private Long id;
    private String title;
    private String content;
    private String category;      // GENERAL | ACADEMIC | FINANCIAL | EMERGENCY | EVENT
    private String audienceType;  // ALL | CLASS | SUBJECT | ROLE | INDIVIDUAL
    private Long audienceRefId;   // class/subject id when applicable
    private String channel;       // IN_SYSTEM | EMAIL | SMS
    private String priority;      // NORMAL | HIGH | EMERGENCY
    private Long createdBy;
    private String status;        // DRAFT | PUBLISHED | ARCHIVED
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    // joined
    private String createdByName;
    private String audienceRefName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAudienceType() { return audienceType; }
    public void setAudienceType(String audienceType) { this.audienceType = audienceType; }
    public Long getAudienceRefId() { return audienceRefId; }
    public void setAudienceRefId(Long audienceRefId) { this.audienceRefId = audienceRefId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public String getAudienceRefName() { return audienceRefName; }
    public void setAudienceRefName(String audienceRefName) { this.audienceRefName = audienceRefName; }
}
