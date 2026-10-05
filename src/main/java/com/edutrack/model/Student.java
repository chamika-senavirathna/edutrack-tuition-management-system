package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Student profile (Member 01 - Student & Teacher Registration / Profile Management).
 * Registration number is unique and auto-generated (BR-REG-01); duplicate NIC is
 * blocked (BR-REG-02). Lifecycle: ACTIVE -> WITHDRAWN (archivable) -> re-enrol needs
 * admin approval (documented exception A4 of UC-REG-02).
 */
public class Student implements Serializable {
    private Long id;
    private String regNo;
    private String firstName;
    private String lastName;
    private String nameWithInitials;
    private String nic;
    private String gender;
    private LocalDate dob;
    private String address;
    private String phone;
    private String email;
    private Long guardianId;
    private String medicalNotes;
    private LocalDate admissionDate;
    private String status;        // ACTIVE | WITHDRAWN | SUSPENDED
    private LocalDate withdrawnAt;
    private String withdrawnReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // joined display
    private String guardianName;
    private String guardianPhone;

    public String fullName() {
        if (firstName == null && lastName == null) return nameWithInitials == null ? "" : nameWithInitials;
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getNameWithInitials() { return nameWithInitials; }
    public void setNameWithInitials(String nameWithInitials) { this.nameWithInitials = nameWithInitials; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getGuardianId() { return guardianId; }
    public void setGuardianId(Long guardianId) { this.guardianId = guardianId; }
    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getWithdrawnAt() { return withdrawnAt; }
    public void setWithdrawnAt(LocalDate withdrawnAt) { this.withdrawnAt = withdrawnAt; }
    public String getWithdrawnReason() { return withdrawnReason; }
    public void setWithdrawnReason(String withdrawnReason) { this.withdrawnReason = withdrawnReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
}
