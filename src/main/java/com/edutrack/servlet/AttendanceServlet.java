package com.edutrack.servlet;

import com.edutrack.model.Attendance;
import com.edutrack.model.Enrolment;
import com.edutrack.model.User;
import com.edutrack.service.AttendanceService;
import com.edutrack.service.RegistrationService;
import com.edutrack.service.TimetableService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Attendance controller (Member 03). Teachers mark the daily register,
 * corrections run through the window/override rules, and chronic-absence
 * flags support early intervention (PBI-09..12).
 */
@WebServlet({"/attendance", "/attendance/mark", "/attendance/correct", "/attendance/chronic"})
public class AttendanceServlet extends BaseServlet {

    private final AttendanceService attendanceService = new AttendanceService();
    private final RegistrationService registrationService = new RegistrationService();
    private final TimetableService timetableService = new TimetableService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        switch (path) {
            case "/attendance" -> {
                Long classId = longParam(request, "classId", -1) == -1 ? null : longParam(request, "classId", -1);
                Long studentId = longParam(request, "studentId", -1) == -1 ? null : longParam(request, "studentId", -1);
                String from = stringParam(request, "from");
                String to = stringParam(request, "to");
                String status = stringParam(request, "status");
                // Record-level scoping: students see only their own history;
                // parents only the attendance of their linked children.
                studentId = resolveSelfOrChildStudentId(user, request);
                if (user.hasRole("PARENT") && user.getParentId() != null) {
                    request.setAttribute("children",
                            registrationService.searchStudents(null, user.getParentId(), null, 0, 50));
                }
                int page = Math.max(1, intParam(request, "page", 1));
                int pageSize = 15;
                LocalDate fromDate = from == null || from.isBlank() ? null : LocalDate.parse(from);
                LocalDate toDate = to == null || to.isBlank() ? null : LocalDate.parse(to);
                request.setAttribute("records", attendanceService.search(studentId, classId, fromDate, toDate,
                        status, (page - 1) * pageSize, pageSize));
                request.setAttribute("total", attendanceService.count(studentId, classId, fromDate, toDate, status));
                request.setAttribute("page", page);
                request.setAttribute("pageSize", pageSize);
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("classId", classId);
                request.setAttribute("studentId", studentId);
                request.setAttribute("from", from);
                request.setAttribute("to", to);
                request.setAttribute("status", status);
                if (classId != null && visibleStudentIds(user) == null) {
                    LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
                    request.setAttribute("summary", attendanceService.summary(classId, monthStart, LocalDate.now()));
                }
                view(request, response, "attendance/list.jsp");
            }
            case "/attendance/mark" -> {
                long classId = longParam(request, "classId", -1);
                String dateStr = stringParam(request, "date");
                LocalDate date = dateStr == null || dateStr.isBlank() ? LocalDate.now() : LocalDate.parse(dateStr);
                attendanceService.requireTeacher(user, classId);
                var enrolments = registrationService.enrolmentsOfClass(classId, "ACTIVE");
                request.setAttribute("clazz", timetableService.findClass(classId));
                request.setAttribute("enrolments", enrolments);
                request.setAttribute("date", date);
                request.setAttribute("existing", attendanceService.search(null, classId, date, date, null, 0, 1000)
                        .stream().collect(java.util.stream.Collectors.toMap(Attendance::getStudentId, a -> a)));
                view(request, response, "attendance/mark.jsp");
            }
            case "/attendance/correct" -> {
                var record = attendanceService.find(longParam(request, "id", -1));
                if (record == null) { response.sendError(404); return; }
                attendanceService.requireTeacher(user, record.getClassId());
                request.setAttribute("record", record);
                view(request, response, "attendance/correct.jsp");
            }
            case "/attendance/chronic" -> {
                long classId = longParam(request, "classId", -1);
                LocalDate from = LocalDate.now().minusMonths(1);
                LocalDate to = LocalDate.now();
                request.setAttribute("flags", attendanceService.chronicAbsences(user, classId, from, to));
                request.setAttribute("clazz", timetableService.findClass(classId));
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("classId", classId);
                view(request, response, "attendance/chronic.jsp");
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
                case "/attendance/mark" -> {
                    long classId = longParam(request, "classId", -1);
                    LocalDate date = LocalDate.parse(stringParam(request, "date"));
                    Map<Long, String> statuses = new HashMap<>();
                    Map<Long, String> reasons = new HashMap<>();
                    request.getParameterMap().forEach((key, values) -> {
                        if (key.startsWith("status_")) {
                            statuses.put(Long.parseLong(key.substring("status_".length())), values[0]);
                        } else if (key.startsWith("reason_")) {
                            if (values[0] != null && !values[0].isBlank()) {
                                reasons.put(Long.parseLong(key.substring("reason_".length())), values[0]);
                            }
                        }
                    });
                    int saved = attendanceService.markRegister(user, classId, date, statuses, reasons);
                    com.edutrack.util.Flash.success(request.getSession(), saved + " attendance entries saved.");
                    redirect(request, response, "/attendance?classId=" + classId);
                }
                case "/attendance/correct" -> {
                    long id = longParam(request, "id", -1);
                    attendanceService.correctEntry(user, id, stringParam(request, "status"),
                            stringParam(request, "reason"), stringParam(request, "correctionNote"));
                    com.edutrack.util.Flash.success(request.getSession(), "Attendance corrected with audit trail.");
                    redirect(request, response, "/attendance");
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, "/attendance", e);
        }
    }
}
