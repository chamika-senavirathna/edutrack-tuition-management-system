package com.edutrack.dao.impl;

import com.edutrack.dao.ExaminationDAO;
import com.edutrack.model.Examination;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.edutrack.dao.impl.JdbcHelper.getDateTime;
import static com.edutrack.dao.impl.JdbcHelper.insertAndGetKey;
import static com.edutrack.dao.impl.JdbcHelper.queryList;
import static com.edutrack.dao.impl.JdbcHelper.queryLong;
import static com.edutrack.dao.impl.JdbcHelper.queryOne;
import static com.edutrack.dao.impl.JdbcHelper.setNullableString;

/** JDBC implementation of ExaminationDAO (Member 06). */
public class ExaminationDAOImpl implements ExaminationDAO {

    private static final String BASE_SELECT =
            "SELECT x.*, t.name AS term_name, c.name AS class_name, sub.name AS subject_name, te.full_name AS teacher_name " +
            "FROM examinations x " +
            "LEFT JOIN terms t ON t.id = x.term_id " +
            "LEFT JOIN classes c ON c.id = x.class_id " +
            "LEFT JOIN subjects sub ON sub.id = x.subject_id " +
            "LEFT JOIN teachers te ON te.id = x.teacher_id ";

    private Examination mapRow(ResultSet rs) throws SQLException {
        Examination x = new Examination();
        x.setId(rs.getLong("id"));
        x.setExamName(rs.getString("exam_name"));
        long tid = rs.getLong("term_id");
        x.setTermId(rs.wasNull() ? null : tid);
        long cid = rs.getLong("class_id");
        x.setClassId(rs.wasNull() ? null : cid);
        long sid = rs.getLong("subject_id");
        x.setSubjectId(rs.wasNull() ? null : sid);
        long teid = rs.getLong("teacher_id");
        x.setTeacherId(rs.wasNull() ? null : teid);
        Date d = rs.getDate("exam_date");
        x.setExamDate(d == null ? null : d.toLocalDate());
        x.setStartTime(rs.getString("start_time"));
        x.setEndTime(rs.getString("end_time"));
        x.setRoomText(rs.getString("room_text"));
        x.setMaxMarks(rs.getInt("max_marks"));
        x.setStatus(rs.getString("status"));
        x.setCreatedAt(getDateTime(rs, "created_at"));
        x.setTermName(rs.getString("term_name"));
        x.setClassName(rs.getString("class_name"));
        x.setSubjectName(rs.getString("subject_name"));
        x.setTeacherName(rs.getString("teacher_name"));
        return x;
    }

    @Override
    public Examination insert(Connection conn, Examination x) throws SQLException {
        long id = insertAndGetKey(conn,
                "INSERT INTO examinations (exam_name, term_id, class_id, subject_id, teacher_id, exam_date, start_time, end_time, room_text, max_marks, status, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                ps -> {
                    ps.setString(1, x.getExamName());
                    setNullableLong(ps, 2, x.getTermId());
                    setNullableLong(ps, 3, x.getClassId());
                    setNullableLong(ps, 4, x.getSubjectId());
                    setNullableLong(ps, 5, x.getTeacherId());
                    ps.setDate(6, x.getExamDate() == null ? Date.valueOf(java.time.LocalDate.now()) : Date.valueOf(x.getExamDate()));
                    setNullableString(ps, 7, x.getStartTime());
                    setNullableString(ps, 8, x.getEndTime());
                    setNullableString(ps, 9, x.getRoomText());
                    ps.setInt(10, x.getMaxMarks() == null ? 100 : x.getMaxMarks());
                    ps.setString(11, x.getStatus() == null ? "SCHEDULED" : x.getStatus());
                });
        x.setId(id);
        return x;
    }

    @Override
    public void update(Connection conn, Examination x) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE examinations SET exam_name=?, term_id=?, class_id=?, subject_id=?, teacher_id=?, exam_date=?, start_time=?, end_time=?, room_text=?, max_marks=?, status=? WHERE id=?")) {
            ps.setString(1, x.getExamName());
            setNullableLong(ps, 2, x.getTermId());
            setNullableLong(ps, 3, x.getClassId());
            setNullableLong(ps, 4, x.getSubjectId());
            setNullableLong(ps, 5, x.getTeacherId());
            ps.setDate(6, x.getExamDate() == null ? null : Date.valueOf(x.getExamDate()));
            setNullableString(ps, 7, x.getStartTime());
            setNullableString(ps, 8, x.getEndTime());
            setNullableString(ps, 9, x.getRoomText());
            ps.setInt(10, x.getMaxMarks() == null ? 100 : x.getMaxMarks());
            ps.setString(11, x.getStatus());
            ps.setLong(12, x.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Examination findById(Connection conn, long id) throws SQLException {
        return queryOne(conn, BASE_SELECT + "WHERE x.id = ?", ps -> ps.setLong(1, id), this::mapRow);
    }

    @Override
    public List<Examination> search(Connection conn, String q, Long termId, Long classId, Long subjectId,
                                    String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND x.exam_name LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (termId != null) { sql.append("AND x.term_id = ? "); params.add(termId); }
        if (classId != null) { sql.append("AND x.class_id = ? "); params.add(classId); }
        if (subjectId != null) { sql.append("AND x.subject_id = ? "); params.add(subjectId); }
        if (status != null && !status.isBlank()) { sql.append("AND x.status = ? "); params.add(status); }
        sql.append("ORDER BY x.exam_date DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return queryList(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, this::mapRow);
    }

    @Override
    public long countSearch(Connection conn, String q, Long termId, Long classId, Long subjectId,
                            String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM examinations x WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND x.exam_name LIKE ? ");
            params.add("%" + q.trim() + "%");
        }
        if (termId != null) { sql.append("AND x.term_id = ? "); params.add(termId); }
        if (classId != null) { sql.append("AND x.class_id = ? "); params.add(classId); }
        if (subjectId != null) { sql.append("AND x.subject_id = ? "); params.add(subjectId); }
        if (status != null && !status.isBlank()) { sql.append("AND x.status = ? "); params.add(status); }
        return queryLong(conn, sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
        }, 0);
    }

    @Override
    public List<Examination> findByStudent(Connection conn, long studentId) throws SQLException {
        return queryList(conn, BASE_SELECT +
                        "JOIN enrolments e ON e.class_id = x.class_id AND e.student_id = ? AND e.status = 'ACTIVE' ORDER BY x.exam_date DESC",
                ps -> ps.setLong(1, studentId), this::mapRow);
    }

    @Override
    public List<Examination> findByTeacher(Connection conn, long teacherId) throws SQLException {
        return queryList(conn, BASE_SELECT + "WHERE x.teacher_id = ? ORDER BY x.exam_date DESC",
                ps -> ps.setLong(1, teacherId), this::mapRow);
    }

    @Override
    public void updateStatus(Connection conn, long id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE examinations SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public long countAll(Connection conn) throws SQLException {
        return queryLong(conn, "SELECT COUNT(*) FROM examinations", null, 0);
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.BIGINT);
        else ps.setLong(index, value);
    }
}
