package com.edutrack.dao;

import com.edutrack.model.TeachingMaterial;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for teaching materials (Member 02). */
public interface TeachingMaterialDAO {
    TeachingMaterial insert(Connection conn, TeachingMaterial material) throws SQLException;

    void update(Connection conn, TeachingMaterial material) throws SQLException;

    TeachingMaterial findById(Connection conn, long id) throws SQLException;

    List<TeachingMaterial> search(Connection conn, String q, Long classId, Long subjectId,
                                  String status, int offset, int limit) throws SQLException;

    long countSearch(Connection conn, String q, Long classId, Long subjectId, String status) throws SQLException;

    List<TeachingMaterial> findByClasses(Connection conn, List<Long> classIds) throws SQLException;

    void updateStatus(Connection conn, long id, String status) throws SQLException;
}
