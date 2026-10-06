package com.edutrack.dao.impl;

import com.edutrack.dao.RoomDAO;
import com.edutrack.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;
import static com.edutrack.dao.impl.JdbcHelper.setNullableString;

/** JDBC implementation of RoomDAO (Member 02). */
public class RoomDAOImpl implements RoomDAO {

    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getLong("id"));
        r.setName(rs.getString("name"));
        r.setRoomType(rs.getString("room_type"));
        r.setCapacity(rs.getInt("capacity"));
        return r;
    }

    @Override
    public List<Room> findAll(Connection conn) throws SQLException {
        return queryList(conn, "SELECT * FROM rooms ORDER BY name", null, this::mapRow);
    }

    @Override
    public Room findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, "SELECT * FROM rooms WHERE id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public Room insert(Connection conn, Room r) throws SQLException {
        long id = insertAndGetKey(conn, "INSERT INTO rooms (name, room_type, capacity) VALUES (?,?,?)",
                ps -> {
                    ps.setString(1, r.getName());
                    ps.setString(2, r.getRoomType());
                    ps.setInt(3, r.getCapacity());
                });
        r.setId(id);
        return r;
    }

    @Override
    public void update(Connection conn, Room r) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE rooms SET name=?, room_type=?, capacity=? WHERE id=?")) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getRoomType());
            ps.setInt(3, r.getCapacity());
            ps.setLong(4, r.getId());
            ps.executeUpdate();
        }
    }
}
