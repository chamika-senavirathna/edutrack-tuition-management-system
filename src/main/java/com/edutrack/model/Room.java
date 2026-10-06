package com.edutrack.model;

import java.io.Serializable;

/** Physical room/hall used by the timetable (Member 02). */
public class Room implements Serializable {
    private Long id;
    private String name;
    private String roomType;     // CLASSROOM | HALL | LAB
    private Integer capacity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
