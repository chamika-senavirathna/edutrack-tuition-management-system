package com.edutrack.servlet;

import com.edutrack.model.ClassRoom;
import com.edutrack.model.Room;
import com.edutrack.model.Subject;
import com.edutrack.model.User;
import com.edutrack.service.RegistrationService;
import com.edutrack.service.TimetableService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Class / Subject / Room controller (Member 02).
 * Classes carry capacity (BR-REG-03 enforcement point) and teacher assignment.
 */
@WebServlet({"/classes", "/classes/add", "/classes/edit", "/classes/view", "/classes/archive",
        "/subjects", "/subjects/save", "/rooms", "/rooms/save"})
public class ClassServlet extends BaseServlet {

    private final TimetableService timetableService = new TimetableService();
    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/classes" -> {
                int page = Math.max(1, intParam(request, "page", 1));
                int pageSize = 10;
                String q = stringParam(request, "q");
                Long subjectId = longParam(request, "subjectId", -1) == -1 ? null : longParam(request, "subjectId", -1);
                Long teacherId = longParam(request, "teacherId", -1) == -1 ? null : longParam(request, "teacherId", -1);
                String status = stringParam(request, "status");
                request.setAttribute("classes", timetableService.searchClasses(q, subjectId, teacherId, status,
                        (page - 1) * pageSize, pageSize));
                request.setAttribute("total", timetableService.countClasses(q, subjectId, teacherId, status));
                request.setAttribute("page", page);
                request.setAttribute("pageSize", pageSize);
                request.setAttribute("subjects", timetableService.subjects(true));
                request.setAttribute("teachers", registrationService.activeTeachers());
                request.setAttribute("q", q);
                request.setAttribute("status", status);
                var ownClasses = scopedClasses(currentUser(request));
                if (ownClasses != null) {
                    var filtered = ownClasses.stream()
                        .filter(c -> q == null || q.isBlank() || c.getName().toLowerCase().contains(q.toLowerCase()))
                        .filter(c -> status == null || status.isBlank() || status.equals(c.getStatus()))
                        .filter(c -> subjectId == null || subjectId.equals(c.getSubjectId()))
                        .filter(c -> teacherId == null || teacherId.equals(c.getTeacherId())).toList();
                    request.setAttribute("classes", filtered.stream().skip((long)(page - 1) * pageSize).limit(pageSize).toList());
                    request.setAttribute("total", filtered.size());
                }
                view(request, response, "classes/list.jsp");
            }
            case "/classes/add" -> {
                request.setAttribute("subjects", timetableService.subjects(false));
                request.setAttribute("teachers", registrationService.activeTeachers());
                request.setAttribute("rooms", timetableService.rooms());
                view(request, response, "classes/form.jsp");
            }
            case "/classes/edit" -> {
                ClassRoom clazz = timetableService.findClass(longParam(request, "id", -1));
                if (clazz == null) {
                    redirect(request, response, "/classes");
                    return;
                }
                request.setAttribute("clazz", clazz);
                request.setAttribute("subjects", timetableService.subjects(false));
                request.setAttribute("teachers", registrationService.activeTeachers());
                request.setAttribute("rooms", timetableService.rooms());
                view(request, response, "classes/form.jsp");
            }
            case "/classes/view" -> {
                ClassRoom clazz = timetableService.findClass(longParam(request, "id", -1));
                if (clazz == null) {
                    redirect(request, response, "/classes");
                    return;
                }
                request.setAttribute("clazz", clazz);
                var ownClasses = scopedClasses(currentUser(request));
                if (ownClasses != null && ownClasses.stream().noneMatch(c -> c.getId().equals(clazz.getId())))
                    throw new com.edutrack.exception.AuthorizationException("This class is not linked to your account.");
                request.setAttribute("slots", timetableService.searchSlots(clazz.getId(), null, null,
                        ownClasses == null ? null : "PUBLISHED", 0, 50));
                view(request, response, "classes/view.jsp");
            }
            case "/subjects" -> {
                request.setAttribute("subjects", timetableService.subjects(true));
                long editId = longParam(request, "id", -1);
                request.setAttribute("subject", timetableService.subjects(true).stream().filter(s -> s.getId() == editId).findFirst().orElse(null));
                view(request, response, "classes/subjects.jsp");
            }
            case "/rooms" -> {
                request.setAttribute("rooms", timetableService.rooms());
                long editId = longParam(request, "id", -1);
                request.setAttribute("room", timetableService.rooms().stream().filter(r -> r.getId() == editId).findFirst().orElse(null));
                view(request, response, "classes/rooms.jsp");
            }
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/classes/add" -> {
                    ClassRoom clazz = readClass(request);
                    timetableService.createClass(user, clazz);
                    com.edutrack.util.Flash.success(request.getSession(), "Class created as DRAFT.");
                    redirect(request, response, "/classes");
                }
                case "/classes/edit" -> {
                    ClassRoom clazz = readClass(request);
                    clazz.setId(longParam(request, "id", -1));
                    timetableService.updateClass(user, clazz);
                    com.edutrack.util.Flash.success(request.getSession(), "Class updated.");
                    redirect(request, response, "/classes/view?id=" + clazz.getId());
                }
                case "/classes/archive" -> {
                    long id = longParam(request, "id", -1);
                    boolean activate = "activate".equals(stringParam(request, "action"));
                    timetableService.archiveClass(user, id, activate);
                    com.edutrack.util.Flash.success(request.getSession(),
                            activate ? "Class reactivated." : "Class archived with history.");
                    redirect(request, response, "/classes");
                }
                case "/subjects/save" -> {
                    Subject subject = new Subject();
                    subject.setId(longParam(request, "id", -1) == -1 ? null : longParam(request, "id", -1));
                    subject.setCode(stringParam(request, "code"));
                    subject.setName(stringParam(request, "name"));
                    subject.setDescription(stringParam(request, "description"));
                    subject.setActive(!"off".equals(request.getParameter("active")));
                    timetableService.saveSubject(user, subject);
                    com.edutrack.util.Flash.success(request.getSession(), "Subject saved.");
                    redirect(request, response, "/subjects");
                }
                case "/rooms/save" -> {
                    Room room = new Room();
                    room.setId(longParam(request, "id", -1) == -1 ? null : longParam(request, "id", -1));
                    room.setName(stringParam(request, "name"));
                    room.setRoomType(stringParam(request, "roomType"));
                    room.setCapacity(intParam(request, "capacity", 0));
                    timetableService.saveRoom(user, room);
                    com.edutrack.util.Flash.success(request.getSession(), "Room saved.");
                    redirect(request, response, "/rooms");
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, "/classes", e);
        }
    }

    private ClassRoom readClass(HttpServletRequest request) {
        ClassRoom c = new ClassRoom();
        c.setName(stringParam(request, "name"));
        c.setSubjectId(longParam(request, "subjectId", -1) == -1 ? null : longParam(request, "subjectId", -1));
        c.setTeacherId(longParam(request, "teacherId", -1) == -1 ? null : longParam(request, "teacherId", -1));
        c.setRoomId(longParam(request, "roomId", -1) == -1 ? null : longParam(request, "roomId", -1));
        c.setCapacity(intParam(request, "capacity", 0));
        String start = stringParam(request, "startDate");
        c.setStartDate(start == null || start.isBlank() ? null : LocalDate.parse(start));
        String end = stringParam(request, "endDate");
        c.setEndDate(end == null || end.isBlank() ? null : LocalDate.parse(end));
        c.setDescription(stringParam(request, "description"));
        return c;
    }
}
