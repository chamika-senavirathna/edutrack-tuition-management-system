package com.edutrack.dao;

import com.edutrack.model.TimetableSlot;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for timetable slots (Member 02). */
public interface TimetableDAO {
    TimetableSlot insert(Connection conn, TimetableSlot slot) throws SQLException;

    void update(Connection conn, TimetableSlot slot) throws SQLException;

    TimetableSlot findById(Connection conn, long id) throws SQLException;

    /** Slots by class; status filter optional (null = all). */
    List<TimetableSlot> findByClass(Connection conn, long classId, String status) throws SQLException;

    List<TimetableSlot> findByTeacher(Connection conn, long teacherId, String status) throws SQLException;

    /** Published slots for a set of classes (student/parent timetable view). */
    List<TimetableSlot> findPublishedByClasses(Connection conn, List<Long> classIds) throws SQLException;

    /**
     * BR-TTC-01 conflict probe: returns overlapping ACTIVE-workflow slots matching
     * the same teacher OR room OR class at the overlapping time on the same day.
     */
    List<TimetableSlot> findConflicts(Connection conn, Long excludeSlotId, String dayOfWeek,
                                      String startTime, String endTime, Long teacherId,
                                      Long roomId, Long classId) throws SQLException;

    List<TimetableSlot> search(Connection conn, Long classId, Long teacherId, String day,
                               String status, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, Long classId, Long teacherId, String day, String status) throws SQLException;

    void updateStatus(Connection conn, long id, String status) throws SQLException;

    void markPublished(Connection conn, long id) throws SQLException;
}
