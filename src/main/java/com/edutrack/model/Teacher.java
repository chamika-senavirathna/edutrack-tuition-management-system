package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Teacher profile (Member 01). Status lifecycle mirrors students: ACTIVE ->
 * RESIGNED (archived, history kept). Teachers are assigned to subjects/classes
 * by the Academic Coordinator (Member 02).
 */
public class Teacher implements Serializable {
    private Long id;
    private String staffNo;
    private String fullName;
    private String nic;
    private String gender;
    private String email;
    private String phone;
    private String address;
    private String qualification;
    private String specialization;
    private LocalDate joinedDate;
    private String status;         // ACTIVE | RESIGNED | ON_LEAVE
    private LocalDate resignedAt;
    private String resignedReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public LocalDate getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDate joinedDate) { this.joinedDate = joinedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getResignedAt() { return resignedAt; }
    public void setResignedAt(LocalDate resignedAt) { this.resignedAt = resignedAt; }
    public String getResignedReason() { return resignedReason; }
    public void setResignedReason(String resignedReason) { this.resignedReason = resignedReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
