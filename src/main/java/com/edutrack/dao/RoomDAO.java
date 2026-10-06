package com.edutrack.dao;

import com.edutrack.model.Room;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for rooms (Member 02). */
public interface RoomDAO {
    List<Room> findAll(Connection conn) throws SQLException;

    Room findById(Connection conn, long id) throws SQLException;

    Room insert(Connection conn, Room room) throws SQLException;

    void update(Connection conn, Room room) throws SQLException;
}
