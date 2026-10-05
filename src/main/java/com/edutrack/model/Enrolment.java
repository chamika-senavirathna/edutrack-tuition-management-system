package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Student-class enrolment (Member 01 registration flow, enforced by capacity BR-REG-03
 * and one-active-enrolment-per-class rule). Status: ACTIVE, COMPLETED, WITHDRAWN.
 */
public class Enrolment implements Serializable {
    private Long id;
    private Long studentId;
    private Long classId;
    private LocalDate enrolledDate;
    private String status;
    private String remarks;

    // joined
    private String studentName;
    private String studentRegNo;
    private String className;
    private String subjectName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public LocalDate getEnrolledDate() { return enrolledDate; }
    public void setEnrolledDate(LocalDate enrolledDate) { this.enrolledDate = enrolledDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
}
