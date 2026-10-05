package com.edutrack.servlet;

import com.edutrack.model.Teacher;
import com.edutrack.model.User;
import com.edutrack.service.RegistrationService;
import com.edutrack.service.TimetableService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

/** Teacher registration & profile controller (Member 01). */
@WebServlet({"/teachers", "/teachers/add", "/teachers/edit", "/teachers/view", "/teachers/resign"})
public class TeacherServlet extends BaseServlet {

    private final RegistrationService registrationService = new RegistrationService();
    private final TimetableService timetableService = new TimetableService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/teachers" -> {
                int page = Math.max(1, intParam(request, "page", 1));
                int pageSize = 10;
                String q = stringParam(request, "q");
                String status = stringParam(request, "status");
                request.setAttribute("teachers", registrationService.searchTeachers(q, status, (page - 1) * pageSize, pageSize));
                request.setAttribute("total", registrationService.countTeachers(q, status));
                request.setAttribute("page", page);
                request.setAttribute("pageSize", pageSize);
                request.setAttribute("q", q);
                request.setAttribute("status", status);
                view(request, response, "teachers/list.jsp");
            }
            case "/teachers/add" -> view(request, response, "teachers/form.jsp");
            case "/teachers/edit" -> {
                Teacher teacher = registrationService.findTeacher(longParam(request, "id", -1));
                if (teacher == null) {
                    redirect(request, response, "/teachers");
                    return;
                }
                request.setAttribute("teacher", teacher);
                view(request, response, "teachers/form.jsp");
            }
            case "/teachers/view" -> {
                Teacher teacher = registrationService.findTeacher(longParam(request, "id", -1));
                if (teacher == null) {
                    redirect(request, response, "/teachers");
                    return;
                }
                request.setAttribute("teacher", teacher);
                request.setAttribute("classes", timetableService.classesOfTeacher(teacher.getId(), null));
                view(request, response, "teachers/view.jsp");
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
                case "/teachers/add" -> {
                    Teacher teacher = readTeacher(request);
                    Teacher saved = registrationService.registerTeacher(user, teacher);
                    com.edutrack.util.Flash.success(request.getSession(),
                            "Teacher registered successfully. Staff number: " + saved.getStaffNo());
                    redirect(request, response, "/teachers/view?id=" + saved.getId());
                }
                case "/teachers/edit" -> {
                    Teacher teacher = readTeacher(request);
                    teacher.setId(longParam(request, "id", -1));
                    registrationService.updateTeacher(user, teacher);
                    com.edutrack.util.Flash.success(request.getSession(), "Teacher profile updated.");
                    redirect(request, response, "/teachers/view?id=" + teacher.getId());
                }
                case "/teachers/resign" -> {
                    long id = longParam(request, "id", -1);
                    String status = stringParam(request, "status");
                    if ("ON_LEAVE".equals(status)) {
                        registrationService.resignTeacher(user, id, stringParam(request, "reason") == null
                                ? "On leave" : "ON_LEAVE: " + stringParam(request, "reason"));
                        // reuse update path for ON_LEAVE: set status directly
                        Teacher t = registrationService.findTeacher(id);
                        t.setStatus("ON_LEAVE");
                        registrationService.updateTeacher(user, t);
                        com.edutrack.util.Flash.success(request.getSession(), "Teacher marked on leave.");
                    } else {
                        registrationService.resignTeacher(user, id, stringParam(request, "reason"));
                        com.edutrack.util.Flash.success(request.getSession(), "Teacher archived as RESIGNED. History retained.");
                    }
                    redirect(request, response, "/teachers/view?id=" + id);
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, "/teachers", e);
        }
    }

    private Teacher readTeacher(HttpServletRequest request) {
        Teacher t = new Teacher();
        t.setFullName(stringParam(request, "fullName"));
        t.setNic(stringParam(request, "nic"));
        t.setGender(stringParam(request, "gender"));
        t.setEmail(stringParam(request, "email"));
        t.setPhone(stringParam(request, "phone"));
        t.setAddress(stringParam(request, "address"));
        t.setQualification(stringParam(request, "qualification"));
        t.setSpecialization(stringParam(request, "specialization"));
        String joined = stringParam(request, "joinedDate");
        t.setJoinedDate(joined == null || joined.isBlank() ? null : LocalDate.parse(joined));
        return t;
    }
}
