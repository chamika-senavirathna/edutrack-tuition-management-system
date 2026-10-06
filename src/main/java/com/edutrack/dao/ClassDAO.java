package com.edutrack.dao;

import com.edutrack.model.ClassRoom;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for classes (Member 02 - Class, Timetable & Communication). */
public interface ClassDAO {
    ClassRoom insert(Connection conn, ClassRoom clazz) throws SQLException;

    void update(Connection conn, ClassRoom clazz) throws SQLException;

    ClassRoom findById(Connection conn, long id) throws SQLException;

    List<ClassRoom> search(Connection conn, String q, Long subjectId, Long teacherId,
                           String status, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, Long subjectId, Long teacherId, String status) throws SQLException;

    List<ClassRoom> findByTeacher(Connection conn, long teacherId, String status) throws SQLException;

    List<ClassRoom> findByStudent(Connection conn, long studentId, String status) throws SQLException;

    void updateStatus(Connection conn, long id, String status) throws SQLException;

    long countAll(Connection conn) throws SQLException;

    long countActive(Connection conn) throws SQLException;
}
