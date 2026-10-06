package com.edutrack.service;

import com.edutrack.dao.ClassDAO;
import com.edutrack.dao.EnrolmentDAO;
import com.edutrack.dao.NoticeDAO;
import com.edutrack.dao.RoomDAO;
import com.edutrack.dao.SubjectDAO;
import com.edutrack.dao.TeachingMaterialDAO;
import com.edutrack.dao.TimetableDAO;
import com.edutrack.dao.impl.ClassDAOImpl;
import com.edutrack.dao.impl.EnrolmentDAOImpl;
import com.edutrack.dao.impl.NoticeDAOImpl;
import com.edutrack.dao.impl.RoomDAOImpl;
import com.edutrack.dao.impl.SubjectDAOImpl;
import com.edutrack.dao.impl.TeachingMaterialDAOImpl;
import com.edutrack.dao.impl.TimetableDAOImpl;
import com.edutrack.exception.BusinessRuleException;
import com.edutrack.model.ClassRoom;
import com.edutrack.model.Notice;
import com.edutrack.model.Room;
import com.edutrack.model.Subject;
import com.edutrack.model.TeachingMaterial;
import com.edutrack.model.TimetableSlot;
import com.edutrack.util.AuditLogger;
import com.edutrack.util.DB;
import com.edutrack.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Class, Timetable & Communication Management (Member 02 - IT25103994).
 * BR-TTC-01 teacher/room/time conflicts blocked; BR-TTC-02 draft-submit-approve-publish;
 * BR-TTC-03 every change audited. Also owns notices and teaching materials
 * (absorbed from the former seventh module).
 */
public class TimetableService {

    private final ClassDAO classDAO = new ClassDAOImpl();
    private final SubjectDAO subjectDAO = new SubjectDAOImpl();
    private final RoomDAO roomDAO = new RoomDAOImpl();
    private final TimetableDAO timetableDAO = new TimetableDAOImpl();
    private final EnrolmentDAO enrolmentDAO = new EnrolmentDAOImpl();
    private final NoticeDAO noticeDAO = new NoticeDAOImpl();
    private final TeachingMaterialDAO materialDAO = new TeachingMaterialDAOImpl();

    // --------------------------------------------------------------- classes

    public ClassRoom createClass(com.edutrack.model.User actor, ClassRoom clazz) {
        requireCoordinator(actor);
        Validator v = new Validator();
        v.required(clazz.getName(), "name", "Class name");
        if (clazz.getCapacity() == null || clazz.getCapacity() <= 0) {
            v.add("capacity", "Capacity must be a positive number.");
        }
        v.check();
        try (Connection conn = DB.getConnection()) {
            if (clazz.getSubjectId() == null) {
                throw new BusinessRuleException("A subject must be selected for the class.");
            }
            if (clazz.getTeacherId() == null) {
                throw new BusinessRuleException("A teacher must be assigned to the class.");
            }
            clazz.setStatus(clazz.getStatus() == null ? "DRAFT" : clazz.getStatus());
            ClassRoom saved = classDAO.insert(conn, clazz);
            AuditLogger.log(conn, actor, "CLASS_CREATE", "CLASS", saved.getId(), null,
                    saved.getName() + " capacity=" + saved.getCapacity());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Class creation failed due to a database error.", e);
        }
    }

    public ClassRoom updateClass(com.edutrack.model.User actor, ClassRoom changes) {
        requireCoordinator(actor);
        Validator v = new Validator();
        if (changes.getCapacity() == null || changes.getCapacity() <= 0)
            v.add("capacity", "Capacity must be positive.");
        if (changes.getSubjectId() == null || changes.getTeacherId() == null)
            v.add("class", "Select a subject and teacher.");
        v.required(changes.getName(), "name", "Class name");
        v.check();
        try (Connection conn = DB.getConnection()) {
            ClassRoom before = classDAO.findById(conn, changes.getId());
            if (before == null) throw new BusinessRuleException("Class not found.");
            if (enrolmentDAO.countActive(conn, changes.getId()) > changes.getCapacity())
                throw new BusinessRuleException("Capacity cannot be below the current active enrolment count.");
            changes.setStatus(before.getStatus());
            classDAO.update(conn, changes);
            AuditLogger.log(conn, actor, "CLASS_UPDATE", "CLASS", changes.getId(),
                    before.getName() + "|" + before.getTeacherId() + "|" + before.getRoomId(),
                    changes.getName() + "|" + changes.getTeacherId() + "|" + changes.getRoomId());
            return classDAO.findById(conn, changes.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Class update failed due to a database error.", e);
        }
    }

    /** Archive/cancel with history instead of delete (safe lifecycle). */
    public void archiveClass(com.edutrack.model.User actor, long classId, boolean activate) {
        requireCoordinator(actor);
        try (Connection conn = DB.getConnection()) {
            ClassRoom before = classDAO.findById(conn, classId);
            if (before == null) throw new BusinessRuleException("Class not found.");
            String newStatus = activate ? "ACTIVE" : "ARCHIVED";
            classDAO.updateStatus(conn, classId, newStatus);
            AuditLogger.log(conn, actor, activate ? "CLASS_ACTIVATE" : "CLASS_ARCHIVE",
                    "CLASS", classId, before.getStatus(), newStatus);
        } catch (SQLException e) {
            throw new RuntimeException("Class status change failed due to a database error.", e);
        }
    }

    public ClassRoom findClass(long id) {
        try (Connection conn = DB.getConnection()) {
            return classDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load class.", e);
        }
    }

    ClassRoom findClass(Connection conn, long id) throws SQLException {
        return classDAO.findById(conn, id);
    }

    public List<ClassRoom> searchClasses(String q, Long subjectId, Long teacherId, String status,
                                         int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return classDAO.search(conn, emptyToNull(q), subjectId, teacherId, emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search classes.", e);
        }
    }

    public long countClasses(String q, Long subjectId, Long teacherId, String status) {
        try (Connection conn = DB.getConnection()) {
            return classDAO.countSearch(conn, emptyToNull(q), subjectId, teacherId, emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not count classes.", e);
        }
    }

    public List<ClassRoom> classesOfTeacher(long teacherId, String status) {
        try (Connection conn = DB.getConnection()) {
            return classDAO.findByTeacher(conn, teacherId, emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not load classes.", e);
        }
    }

    public List<ClassRoom> classesOfStudent(long studentId, String status) {
        try (Connection conn = DB.getConnection()) {
            return classDAO.findByStudent(conn, studentId, emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not load classes.", e);
        }
    }

    // --------------------------------------------------------------- subjects & rooms

    public List<Subject> subjects(boolean includeInactive) {
        try (Connection conn = DB.getConnection()) {
            return subjectDAO.findAll(conn, includeInactive);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load subjects.", e);
        }
    }

    public Subject saveSubject(com.edutrack.model.User actor, Subject subject) {
        requireCoordinator(actor);
        Validator v = new Validator();
        v.required(subject.getCode(), "code", "Subject code").required(subject.getName(), "name", "Subject name");
        v.check();
        try (Connection conn = DB.getConnection()) {
            if (subjectDAO.codeExists(conn, subject.getCode().trim(), subject.getId())) {
                throw new BusinessRuleException("Subject code already exists.");
            }
            if (subject.getId() != null) {
                subjectDAO.update(conn, subject);
                AuditLogger.log(conn, actor, "SUBJECT_UPDATE", "SUBJECT", subject.getId(), null, subject.getName());
                return subject;
            }
            Subject saved = subjectDAO.insert(conn, subject);
            AuditLogger.log(conn, actor, "SUBJECT_CREATE", "SUBJECT", saved.getId(), null,
                    saved.getCode() + " " + saved.getName());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Subject save failed due to a database error.", e);
        }
    }

    public List<Room> rooms() {
        try (Connection conn = DB.getConnection()) {
            return roomDAO.findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load rooms.", e);
        }
    }

    public Room saveRoom(com.edutrack.model.User actor, Room room) {
        requireCoordinator(actor);
        Validator v = new Validator();
        v.required(room.getName(), "name", "Room name");
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            v.add("capacity", "Room capacity must be positive.");
        }
        v.check();
        try (Connection conn = DB.getConnection()) {
            if (room.getId() != null) {
                roomDAO.update(conn, room);
                AuditLogger.log(conn, actor, "ROOM_UPDATE", "ROOM", room.getId(), null, room.getName());
                return room;
            }
            Room saved = roomDAO.insert(conn, room);
            AuditLogger.log(conn, actor, "ROOM_CREATE", "ROOM", saved.getId(), null, saved.getName());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Room save failed due to a database error.", e);
        }
    }

    // --------------------------------------------------------------- timetable

    public TimetableSlot createSlot(com.edutrack.model.User actor, TimetableSlot slot) {
        requireCoordinator(actor);
        Validator v = new Validator();
        v.required(slot.getDayOfWeek(), "dayOfWeek", "Day of week")
                .required(slot.getStartTime(), "startTime", "Start time")
                .required(slot.getEndTime(), "endTime", "End time");
        v.check();
        if (slot.getStartTime() != null && slot.getEndTime() != null
                && slot.getStartTime().compareTo(slot.getEndTime()) >= 0) {
            throw new BusinessRuleException("End time must be after start time.");
        }
        try (Connection conn = DB.getConnection()) {
            List<TimetableSlot> conflicts = timetableDAO.findConflicts(conn, null, slot.getDayOfWeek(),
                    slot.getStartTime(), slot.getEndTime(), slot.getTeacherId(), slot.getRoomId(), slot.getClassId());
            if (!conflicts.isEmpty()) {
                TimetableSlot c = conflicts.get(0);
                throw new BusinessRuleException(
                        "Timetable conflict (BR-TTC-01): overlaps with " + c.getClassName()
                                + " / " + c.getSubjectName() + " (" + c.getStartTime() + "-" + c.getEndTime()
                                + "). Two classes cannot share the same teacher, room or time slot.");
            }
            slot.setStatus("DRAFT");
            TimetableSlot saved = timetableDAO.insert(conn, slot);
            AuditLogger.log(conn, actor, "TIMETABLE_CREATE", "TIMETABLE", saved.getId(), null,
                    saved.getDayOfWeek() + " " + saved.getStartTime() + "-" + saved.getEndTime());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Timetable slot creation failed due to a database error.", e);
        }
    }

    public TimetableSlot updateSlot(com.edutrack.model.User actor, TimetableSlot changes) {
        requireCoordinator(actor);
        if (changes.getDayOfWeek() == null || changes.getStartTime() == null || changes.getEndTime() == null
                || changes.getStartTime().compareTo(changes.getEndTime()) >= 0)
            throw new BusinessRuleException("Select a day and an end time after the start time.");
        try (Connection conn = DB.getConnection()) {
            TimetableSlot before = timetableDAO.findById(conn, changes.getId());
            if (before == null) throw new BusinessRuleException("Timetable slot not found.");
            if ("PUBLISHED".equals(before.getStatus())) {
                throw new BusinessRuleException("Published slots are view-only. Archive and create a new slot instead.");
            }
            List<TimetableSlot> conflicts = timetableDAO.findConflicts(conn, changes.getId(), changes.getDayOfWeek(),
                    changes.getStartTime(), changes.getEndTime(), changes.getTeacherId(), changes.getRoomId(),
                    changes.getClassId());
            if (!conflicts.isEmpty()) {
                throw new BusinessRuleException("Timetable conflict (BR-TTC-01): the new time overlaps another slot.");
            }
            changes.setStatus(before.getStatus());
            timetableDAO.update(conn, changes);
            AuditLogger.log(conn, actor, "TIMETABLE_UPDATE", "TIMETABLE", changes.getId(),
                    before.getDayOfWeek() + " " + before.getStartTime() + "-" + before.getEndTime()
                            + " teacher=" + before.getTeacherId() + " room=" + before.getRoomId(),
                    changes.getDayOfWeek() + " " + changes.getStartTime() + "-" + changes.getEndTime()
                            + " teacher=" + changes.getTeacherId() + " room=" + changes.getRoomId());
            return timetableDAO.findById(conn, changes.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Timetable update failed due to a database error.", e);
        }
    }

    /** BR-TTC-02 workflow transition: DRAFT -> SUBMITTED -> APPROVED -> PUBLISHED. */
    public void transitionSlot(com.edutrack.model.User actor, long slotId, String action) {
        try (Connection conn = DB.getConnection()) {
            TimetableSlot slot = timetableDAO.findById(conn, slotId);
            if (slot == null) throw new BusinessRuleException("Timetable slot not found.");
            switch (action) {
                case "submit" -> {
                    requireCoordinator(actor);
                    if (!"DRAFT".equals(slot.getStatus())) {
                        throw new BusinessRuleException("Only draft slots can be submitted.");
                    }
                    List<TimetableSlot> conflicts = timetableDAO.findConflicts(conn, slotId, slot.getDayOfWeek(),
                            slot.getStartTime(), slot.getEndTime(), slot.getTeacherId(), slot.getRoomId(),
                            slot.getClassId());
                    if (!conflicts.isEmpty()) {
                        throw new BusinessRuleException("Conflict detected before submit (BR-TTC-01). Resolve the clash first.");
                    }
                    timetableDAO.updateStatus(conn, slotId, "SUBMITTED");
                    AuditLogger.log(conn, actor, "TIMETABLE_SUBMIT", "TIMETABLE", slotId,
                            slot.getStatus(), "SUBMITTED");
                }
                case "approve" -> {
                    requireApprover(actor);
                    if (!"SUBMITTED".equals(slot.getStatus())) {
                        throw new BusinessRuleException("Only submitted slots can be approved.");
                    }
                    timetableDAO.updateStatus(conn, slotId, "APPROVED");
                    AuditLogger.log(conn, actor, "TIMETABLE_APPROVE", "TIMETABLE", slotId,
                            "SUBMITTED", "APPROVED");
                }
                case "publish" -> {
                    requireApprover(actor);
                    if (!"APPROVED".equals(slot.getStatus())) {
                        throw new BusinessRuleException("Only approved slots can be published.");
                    }
                    timetableDAO.markPublished(conn, slotId);
                    AuditLogger.log(conn, actor, "TIMETABLE_PUBLISH", "TIMETABLE", slotId,
                            "APPROVED", "PUBLISHED");
                    notifyAffected(conn, slot);
                }
                case "archive" -> {
                    requireCoordinator(actor);
                    timetableDAO.updateStatus(conn, slotId, "ARCHIVED");
                    AuditLogger.log(conn, actor, "TIMETABLE_ARCHIVE", "TIMETABLE", slotId,
                            slot.getStatus(), "ARCHIVED");
                }
                default -> throw new BusinessRuleException("Unknown timetable action.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Timetable transition failed due to a database error.", e);
        }
    }

    /** PBI-07: affected students and teachers are notified when a timetable slot is published/changed. */
    private void notifyAffected(Connection conn, TimetableSlot slot) throws SQLException {
        String message = "Timetable update: " + slot.getClassName() + " - " + slot.getSubjectName()
                + " on " + slot.getDayOfWeek() + " " + slot.getStartTime() + "-" + slot.getEndTime()
                + (slot.getRoomName() != null ? " in " + slot.getRoomName() : "");
        try (var st = conn.prepareStatement(
                "SELECT u.id FROM users u WHERE u.teacher_id = ? AND u.active = 1")) {
            st.setLong(1, slot.getTeacherId() == null ? -1 : slot.getTeacherId());
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    insertNotification(conn, rs.getLong("id"), null, null,
                            "Timetable published", message);
                }
            }
        }
        try (var st = conn.prepareStatement(
                "SELECT u.id, s.id AS sid FROM users u JOIN students s ON s.id = u.student_id " +
                "JOIN enrolments e ON e.student_id = s.id AND e.class_id = ? AND e.status = 'ACTIVE' " +
                "WHERE u.active = 1")) {
            st.setLong(1, slot.getClassId());
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    insertNotification(conn, rs.getLong("id"), null, rs.getLong("sid"),
                            "Timetable published", message);
                }
            }
        }
    }

    private void insertNotification(Connection conn, Long userId, String roleName, Long studentId,
                                    String title, String message) throws SQLException {
        try (var ps = conn.prepareStatement(
                "INSERT INTO notifications (user_id, role_name, student_id, title, message, channel, delivery_status, is_read, created_at) " +
                "VALUES (?,?,?,?,?,'IN_SYSTEM','SENT',0,CURRENT_TIMESTAMP)")) {
            if (userId == null) ps.setNull(1, java.sql.Types.BIGINT); else ps.setLong(1, userId);
            ps.setString(2, roleName);
            if (studentId == null) ps.setNull(3, java.sql.Types.BIGINT); else ps.setLong(3, studentId);
            ps.setString(4, title);
            ps.setString(5, message);
            ps.executeUpdate();
        }
    }

    public TimetableSlot findSlot(long id) {
        try (Connection conn = DB.getConnection()) {
            return timetableDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load timetable slot.", e);
        }
    }

    public List<TimetableSlot> searchSlots(Long classId, Long teacherId, String day, String status,
                                           int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return timetableDAO.search(conn, classId, teacherId, emptyToNull(day), emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search timetable.", e);
        }
    }

    public long countSlots(Long classId, Long teacherId, String day, String status) {
        try (Connection conn = DB.getConnection()) {
            return timetableDAO.countSearch(conn, classId, teacherId, emptyToNull(day), emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not count timetable slots.", e);
        }
    }

    public List<TimetableSlot> publishedTimetable(List<Long> classIds) {
        try (Connection conn = DB.getConnection()) {
            return timetableDAO.findPublishedByClasses(conn, classIds);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load timetable.", e);
        }
    }

    // --------------------------------------------------------------- notices

    private void requireNoticeOwner(com.edutrack.model.User actor, Notice notice) {
        requireStaff(actor);
        if (actor.hasRole("TEACHER") && !actor.hasRole("ADMIN") && !actor.hasRole("PRINCIPAL")
                && !actor.hasRole("ACADEMIC_COORDINATOR") && !java.util.Objects.equals(actor.getId(), notice.getCreatedBy()))
            throw new com.edutrack.exception.AuthorizationException("You can only change your own notices.");
    }

    private void validateNotice(Notice n) {
        Validator v = new Validator();
        v.required(n.getTitle(), "title", "Title").required(n.getContent(), "content", "Content");
        v.check();
        if (!java.util.Set.of("ALL", "CLASS", "SUBJECT", "ROLE").contains(n.getAudienceType() == null ? "" : n.getAudienceType()))
            throw new BusinessRuleException("Invalid notice audience.");
        if (!"ALL".equals(n.getAudienceType()) && n.getAudienceRefId() == null)
            throw new BusinessRuleException("Select the audience reference.");
        if ("ALL".equals(n.getAudienceType())) n.setAudienceRefId(null);
    }

    private void requireMaterialClass(com.edutrack.model.User actor, Long classId) {
        requireStaff(actor);
        if (classId == null || findClass(classId) == null) throw new BusinessRuleException("Select a valid class.");
        if (actor.hasRole("TEACHER") && !actor.hasRole("ADMIN") && !actor.hasRole("PRINCIPAL") && !actor.hasRole("ACADEMIC_COORDINATOR"))
            new AttendanceService().requireTeacher(actor, classId);
    }

    private void requireMaterialOwner(com.edutrack.model.User actor, TeachingMaterial material) {
        requireStaff(actor);
        if (material == null) throw new BusinessRuleException("Material not found.");
        if (actor.hasRole("TEACHER") && !actor.hasRole("ADMIN") && !actor.hasRole("PRINCIPAL")
                && !actor.hasRole("ACADEMIC_COORDINATOR") && !java.util.Objects.equals(actor.getId(), material.getUploadedBy()))
            throw new com.edutrack.exception.AuthorizationException("You can only change your own materials.");
    }

    public TeachingMaterial updateMaterial(com.edutrack.model.User actor, TeachingMaterial changes) {
        TeachingMaterial before = findMaterial(changes.getId());
        requireMaterialOwner(actor, before);
        requireMaterialClass(actor, changes.getClassId());
        if (!"ACTIVE".equals(before.getStatus())) throw new BusinessRuleException("Archived materials cannot be edited.");
        validateMaterial(changes);
        changes.setStatus(before.getStatus());
        try (Connection conn = DB.getConnection()) {
            materialDAO.update(conn, changes);
            AuditLogger.log(conn, actor, "MATERIAL_UPDATE", "MATERIAL", changes.getId(), before.getTitle(), changes.getTitle());
            return materialDAO.findById(conn, changes.getId());
        } catch (SQLException e) { throw new RuntimeException("Could not update material.", e); }
    }

    private void validateMaterial(TeachingMaterial material) {
        Validator v = new Validator();
        v.required(material.getTitle(), "title", "Title").required(material.getFilePath(), "link", "Material link");
        v.check();
        String link = material.getFilePath().trim().toLowerCase(java.util.Locale.ROOT);
        if (link.startsWith("javascript:") || link.startsWith("data:") || link.startsWith("vbscript:"))
            throw new BusinessRuleException("Invalid material link.");
    }

    public Notice createNotice(com.edutrack.model.User actor, Notice notice, boolean publishNow) {
        requireStaff(actor);
        validateNotice(notice);
        notice.setCreatedBy(actor.getId());
        Validator v = new Validator();
        v.required(notice.getTitle(), "title", "Notice title")
                .required(notice.getContent(), "content", "Notice content")
                .required(notice.getAudienceType(), "audienceType", "Audience")
                .required(notice.getCategory(), "category", "Category");
        v.check();
        if ("CLASS".equals(notice.getAudienceType()) && notice.getAudienceRefId() == null) {
            throw new BusinessRuleException("Select a class for this notice audience.");
        }
        try (Connection conn = DB.getConnection()) {
            notice.setStatus(publishNow ? "PUBLISHED" : "DRAFT");
            if (publishNow) notice.setPublishedAt(LocalDateTime.now());
            Notice saved = noticeDAO.insert(conn, notice);
            AuditLogger.log(conn, actor, publishNow ? "NOTICE_PUBLISH" : "NOTICE_CREATE",
                    "NOTICE", saved.getId(), null, saved.getTitle());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Notice creation failed due to a database error.", e);
        }
    }

    public Notice updateNotice(com.edutrack.model.User actor, Notice changes, boolean publishNow) {
        requireStaff(actor);
        validateNotice(changes);
        try (Connection conn = DB.getConnection()) {
            Notice before = noticeDAO.findById(conn, changes.getId());
            if (before == null) throw new BusinessRuleException("Notice not found.");
            requireNoticeOwner(actor, before);
            if ("ARCHIVED".equals(before.getStatus())) {
                throw new BusinessRuleException("Archived notices cannot be edited.");
            }
            changes.setPublishedAt(before.getPublishedAt());
            changes.setStatus(publishNow ? "PUBLISHED" : before.getStatus());
            if (publishNow && before.getPublishedAt() == null) changes.setPublishedAt(LocalDateTime.now());
            noticeDAO.update(conn, changes);
            AuditLogger.log(conn, actor, "NOTICE_UPDATE", "NOTICE", changes.getId(),
                    before.getTitle(), changes.getTitle());
            return noticeDAO.findById(conn, changes.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Notice update failed due to a database error.", e);
        }
    }

    public void archiveNotice(com.edutrack.model.User actor, long noticeId) {
        requireStaff(actor);
        try (Connection conn = DB.getConnection()) {
            Notice before = noticeDAO.findById(conn, noticeId);
            if (before == null) throw new BusinessRuleException("Notice not found.");
            requireNoticeOwner(actor, before);
            noticeDAO.updateStatus(conn, noticeId, "ARCHIVED");
            AuditLogger.log(conn, actor, "NOTICE_ARCHIVE", "NOTICE", noticeId, before.getStatus(), "ARCHIVED");
        } catch (SQLException e) {
            throw new RuntimeException("Notice archive failed due to a database error.", e);
        }
    }

    public List<Notice> searchNotices(String q, String category, String audienceType, String status,
                                      int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return noticeDAO.search(conn, emptyToNull(q), emptyToNull(category),
                    emptyToNull(audienceType), emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search notices.", e);
        }
    }

    public long countNotices(String q, String category, String audienceType, String status) {
        try (Connection conn = DB.getConnection()) {
            return noticeDAO.countSearch(conn, emptyToNull(q), emptyToNull(category),
                    emptyToNull(audienceType), emptyToNull(status));
        } catch (SQLException e) {
            throw new RuntimeException("Could not count notices.", e);
        }
    }

    public Notice findNotice(long id) {
        try (Connection conn = DB.getConnection()) {
            return noticeDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load notice.", e);
        }
    }

    /** Audience-filtered notice list for a viewer (students/parents see only theirs). */
    public List<Notice> noticesForViewer(com.edutrack.model.User viewer, Long studentId, Long teacherId,
                                         List<Long> classIds, int limit) {
        String role = viewer == null ? null : viewer.primaryRole();
        try (Connection conn = DB.getConnection()) {
            boolean staff = viewer != null && (viewer.hasRole("ADMIN") || viewer.hasRole("ACADEMIC_COORDINATOR")
                    || viewer.hasRole("PRINCIPAL") || viewer.hasRole("FINANCE"));
            if (staff) {
                return noticeDAO.search(conn, null, null, null, "PUBLISHED", 0, limit);
            }
            return noticeDAO.findVisibleToViewer(conn, viewer == null ? null : viewer.getId(), role,
                    studentId, teacherId, classIds, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load notices.", e);
        }
    }

    // --------------------------------------------------------------- teaching materials

    public TeachingMaterial createMaterial(com.edutrack.model.User actor, TeachingMaterial material) {
        requireMaterialClass(actor, material.getClassId());
        validateMaterial(material);
        material.setUploadedBy(actor.getId());
        Validator v = new Validator();
        v.required(material.getTitle(), "title", "Material title").required(material.getClassId() == null ? null : "1",
                "classId", "Class");
        v.check();
        try (Connection conn = DB.getConnection()) {
            material.setStatus("ACTIVE");
            TeachingMaterial saved = materialDAO.insert(conn, material);
            AuditLogger.log(conn, actor, "MATERIAL_CREATE", "MATERIAL", saved.getId(), null, saved.getTitle());
            return saved;
        } catch (SQLException e) {
            throw new RuntimeException("Material creation failed due to a database error.", e);
        }
    }

    public void archiveMaterial(com.edutrack.model.User actor, long materialId) {
        requireStaff(actor);
        try (Connection conn = DB.getConnection()) {
            requireMaterialOwner(actor, materialDAO.findById(conn, materialId));
            materialDAO.updateStatus(conn, materialId, "ARCHIVED");
            AuditLogger.log(conn, actor, "MATERIAL_ARCHIVE", "MATERIAL", materialId, "ACTIVE", "ARCHIVED");
        } catch (SQLException e) {
            throw new RuntimeException("Material archive failed due to a database error.", e);
        }
    }

    public List<TeachingMaterial> searchMaterials(String q, Long classId, Long subjectId, String status,
                                                  int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return materialDAO.search(conn, emptyToNull(q), classId, subjectId, emptyToNull(status), offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search materials.", e);
        }
    }

    public List<TeachingMaterial> materialsForClasses(List<Long> classIds) {
        try (Connection conn = DB.getConnection()) {
            return materialDAO.findByClasses(conn, classIds);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load materials.", e);
        }
    }

    public TeachingMaterial findMaterial(long id) {
        try (Connection conn = DB.getConnection()) {
            return materialDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load material.", e);
        }
    }

    // --------------------------------------------------------------- permissions

    private void requireCoordinator(com.edutrack.model.User actor) {
        if (actor == null) throw new com.edutrack.exception.AuthorizationException("Not authenticated.");
        if (!(actor.hasRole("ADMIN") || actor.hasRole("ACADEMIC_COORDINATOR") || actor.hasRole("PRINCIPAL"))) {
            throw new com.edutrack.exception.AuthorizationException(
                    "Only the academic coordinator or administration can manage classes and timetables.");
        }
    }

    private void requireApprover(com.edutrack.model.User actor) {
        if (actor == null) throw new com.edutrack.exception.AuthorizationException("Not authenticated.");
        if (!(actor.hasRole("PRINCIPAL") || actor.hasRole("ADMIN"))) {
            throw new com.edutrack.exception.AuthorizationException(
                    "Only the Principal can approve and publish timetables.");
        }
    }

    private void requireStaff(com.edutrack.model.User actor) {
        if (actor == null) throw new com.edutrack.exception.AuthorizationException("Not authenticated.");
        if (!(actor.hasRole("ADMIN") || actor.hasRole("ACADEMIC_COORDINATOR") || actor.hasRole("PRINCIPAL")
                || actor.hasRole("TEACHER"))) {
            throw new com.edutrack.exception.AuthorizationException("Only staff members can do this.");
        }
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
