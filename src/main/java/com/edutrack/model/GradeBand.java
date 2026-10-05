package com.edutrack.model;

import java.io.Serializable;

/**
 * Configurable grade band (Member 06). Grades are auto-calculated from these
 * bands (PBI-22) - never hand-typed.
 */
public class GradeBand implements Serializable {
    private Long id;
    private String grade;
    private Integer minMark;
    private Integer maxMark;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Integer getMinMark() { return minMark; }
    public void setMinMark(Integer minMark) { this.minMark = minMark; }
    public Integer getMaxMark() { return maxMark; }
    public void setMaxMark(Integer maxMark) { this.maxMark = maxMark; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
