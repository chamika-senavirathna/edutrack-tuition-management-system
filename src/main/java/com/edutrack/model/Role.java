package com.edutrack.model;

import java.io.Serializable;

/** Role entity (Member 05): ADMIN, ACADEMIC_COORDINATOR, TEACHER, FINANCE, STUDENT, PARENT, PRINCIPAL. */
public class Role implements Serializable {
    private Long id;
    private String name;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
