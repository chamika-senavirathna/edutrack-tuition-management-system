package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Guardian/parent profile (Member 01 - Registration & Profiles).
 * PARENT users link here via users.parent_id; parents see only linked children's records.
 */
public class Guardian implements Serializable {
    private Long id;
    private String fullName;
    private String nic;
    private String phone;
    private String email;
    private String occupation;
    private String address;
    private String relationship;   // Father | Mother | Legal Guardian
    private LocalDate createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
