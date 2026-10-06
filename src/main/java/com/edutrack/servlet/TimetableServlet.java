package com.edutrack.servlet;

import com.edutrack.model.TimetableSlot;
import com.edutrack.model.User;
import com.edutrack.service.RegistrationService;
import com.edutrack.service.TimetableService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Timetable controller (Member 02). Draft -> Submit -> Approve -> Publish
 * workflow with conflict detection at every step (BR-TTC-01/02).
 */
@WebServlet({"/timetable", "/timetable/add", "/timetable/edit", "/timetable/transition"})
public class TimetableServlet extends BaseServlet {

    private final TimetableService timetableService = new TimetableService();
    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        switch (path) {
            case "/timetable" -> {
                // Teachers see their own slots by default; coordinators see all.
                Long teacherFilter = user.hasRole("TEACHER") && !user.hasRole("ADMIN")
                        && user.getTeacherId() != null && longParam(request, "teacherId", -1) == -1
                        ? user.getTeacherId() : (longParam(request, "teacherId", -1) == -1 ? null : longParam(request, "teacherId", -1));
                Long classId = longParam(request, "classId", -1) == -1 ? null : longParam(request, "classId", -1);
                String day = stringParam(request, "day");
                String status = stringParam(request, "status");
                request.setAttribute("slots", timetableService.searchSlots(classId, teacherFilter, day, status, 0, 200));
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, null, 0, 200));
                request.setAttribute("teachers", registrationService.activeTeachers());
                request.setAttribute("subjects", timetableService.subjects(false));
                request.setAttribute("rooms", timetableService.rooms());
                request.setAttribute("classId", classId);
                request.setAttribute("teacherId", teacherFilter);
                request.setAttribute("day", day);
                request.setAttribute("status", status);
                var ownClasses = scopedClasses(user);
                if (ownClasses != null) {
                    var ids = ownClasses.stream().map(com.edutrack.model.ClassRoom::getId).toList();
                    request.setAttribute("slots", timetableService.publishedTimetable(ids).stream()
                            .filter(s -> classId == null || classId.equals(s.getClassId()))
                            .filter(s -> day == null || day.isBlank() || day.equals(s.getDayOfWeek())).toList());
                    request.setAttribute("classes", ownClasses);
                }
                view(request, response, "timetable/list.jsp");
            }
            case "/timetable/add" -> {
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 200));
                request.setAttribute("teachers", registrationService.activeTeachers());
                request.setAttribute("subjects", timetableService.subjects(false));
                request.setAttribute("rooms", timetableService.rooms());
                view(request, response, "timetable/form.jsp");
            }
            case "/timetable/edit" -> {
                TimetableSlot slot = timetableService.findSlot(longParam(request, "id", -1));
                if (slot == null) {
                    redirect(request, response, "/timetable");
                    return;
                }
                request.setAttribute("slot", slot);
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 200));
                request.setAttribute("teachers", registrationService.activeTeachers());
                request.setAttribute("subjects", timetableService.subjects(false));
                request.setAttribute("rooms", timetableService.rooms());
                view(request, response, "timetable/form.jsp");
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
                case "/timetable/add" -> {
                    TimetableSlot slot = readSlot(request);
                    timetableService.createSlot(user, slot);
                    com.edutrack.util.Flash.success(request.getSession(),
                            "Timetable slot saved as DRAFT. Submit it for approval next.");
                    redirect(request, response, "/timetable");
                }
                case "/timetable/edit" -> {
                    TimetableSlot slot = readSlot(request);
                    slot.setId(longParam(request, "id", -1));
                    timetableService.updateSlot(user, slot);
                    com.edutrack.util.Flash.success(request.getSession(), "Timetable slot updated.");
                    redirect(request, response, "/timetable");
                }
                case "/timetable/transition" -> {
                    long id = longParam(request, "id", -1);
                    timetableService.transitionSlot(user, id, stringParam(request, "action"));
                    com.edutrack.util.Flash.success(request.getSession(), "Timetable slot " + stringParam(request, "action") + "ed.");
                    redirect(request, response, "/timetable");
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, "/timetable", e);
        }
    }

    private TimetableSlot readSlot(HttpServletRequest request) {
        TimetableSlot t = new TimetableSlot();
        t.setClassId(longParam(request, "classId", -1));
        t.setSubjectId(longParam(request, "subjectId", -1) == -1 ? null : longParam(request, "subjectId", -1));
        t.setTeacherId(longParam(request, "teacherId", -1) == -1 ? null : longParam(request, "teacherId", -1));
        t.setRoomId(longParam(request, "roomId", -1) == -1 ? null : longParam(request, "roomId", -1));
        t.setDayOfWeek(stringParam(request, "dayOfWeek"));
        t.setStartTime(stringParam(request, "startTime"));
        t.setEndTime(stringParam(request, "endTime"));
        return t;
    }
}
