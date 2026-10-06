package com.edutrack.dao;

import com.edutrack.model.Attendance;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** DAO for attendance (Member 03 - Attendance Management). */
public interface AttendanceDAO {
    Attendance insert(Connection conn, Attendance attendance) throws SQLException;

    Attendance update(Connection conn, Attendance attendance) throws SQLException;

    Attendance findById(Connection conn, long id) throws SQLException;

    /** BR-ATT-01 duplicate probe. */
    Attendance findBySession(Connection conn, long studentId, long classId, LocalDate date) throws SQLException;

    List<Attendance> search(Connection conn, Long studentId, Long classId, LocalDate from,
                            LocalDate to, String status, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, Long studentId, Long classId, LocalDate from,
                     LocalDate to, String status) throws SQLException;

    List<Attendance> findByStudentAndRange(Connection conn, long studentId, LocalDate from, LocalDate to) throws SQLException;

    long countByStatus(Connection conn, Long classId, LocalDate from, LocalDate to, String status) throws SQLException;

    /** Distinct absence dates of a student (chronic-absence detection). */
    List<LocalDate> absenceDates(Connection conn, long studentId, LocalDate from, LocalDate to) throws SQLException;

    /** Students of a class whose absence count in range meets/exceeds the threshold. */
    List<long[]> chronicAbsences(Connection conn, long classId, LocalDate from, LocalDate to,
                                 int threshold) throws SQLException;
}
