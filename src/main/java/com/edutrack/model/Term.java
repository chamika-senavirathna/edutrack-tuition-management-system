package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDate;

/** Academic term shared across fees, examinations and attendance archiving. */
public class Term implements Serializable {
    private Long id;
    private String name;         // e.g. "2026 Term 1"
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
