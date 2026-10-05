package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Platform user account (Member 05 - User & Access Management).
 * One shared identity model protects all six modules (Use Case Report, integration rules).
 */
public class User implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private Boolean active = true;
    private String userType;      // STUDENT | STAFF | PARENT
    private Long studentId;       // linked student profile (for STUDENT users)
    private Long teacherId;       // linked teacher profile (for STAFF users)
    private Long parentId;        // linked guardian profile (for PARENT users)
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    private final List<UserRole> roles = new ArrayList<>();

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase(roleName));
    }

    public String primaryRole() {
        return roles.isEmpty() ? "" : roles.get(0).getRole().getName();
    }

    // --- getters/setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public List<UserRole> getRoles() { return roles; }
}
