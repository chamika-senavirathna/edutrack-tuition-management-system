package com.edutrack.model;

import java.io.Serializable;

/** Subject of the institute curriculum (Member 02 - Class, Timetable & Communication). */
public class Subject implements Serializable {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
