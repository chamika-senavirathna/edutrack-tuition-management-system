package com.edutrack.servlet;

import com.edutrack.model.Guardian;
import com.edutrack.model.Student;
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
 * Student registration & profile controller (Member 01).
 * GET: list/search, view, add/edit forms. POST: create, update, enrol, withdraw.
 */
@WebServlet({"/students", "/students/add", "/students/edit", "/students/view", "/students/enrol", "/students/withdraw"})
public class StudentServlet extends BaseServlet {

    private final RegistrationService registrationService = new RegistrationService();
    private final TimetableService timetableService = new TimetableService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        switch (path) {
            case "/students" -> {
                int page = Math.max(1, intParam(request, "page", 1));
                int pageSize = 10;
                String q = stringParam(request, "q");
                String status = stringParam(request, "status");
                Long guardianId = user.hasRole("PARENT") && user.getParentId() != null
                        ? user.getParentId() : (longParam(request, "guardianId", -1) == -1 ? null : longParam(request, "guardianId", -1));
                // Parents may only browse their own children.
                if (user.hasRole("PARENT") && guardianId == null) guardianId = user.getParentId();
                java.util.List<Student> students = registrationService.searchStudents(
                        q, guardianId, status, (page - 1) * pageSize, pageSize);
                long total = registrationService.countStudents(q, guardianId, status);
                var visible = visibleStudentIds(user);
                if (visible != null) {
                    students = visible.stream().map(registrationService::findStudent)
                            .filter(java.util.Objects::nonNull)
                            .filter(s -> status == null || status.isBlank() || status.equals(s.getStatus()))
                            .filter(s -> q == null || q.isBlank() || (s.fullName() + " " + s.getRegNo()).toLowerCase().contains(q.toLowerCase()))
                            .toList();
                    total = students.size();
                    students = students.stream().skip((long)(page - 1) * pageSize).limit(pageSize).toList();
                }
                request.setAttribute("students", students);
                request.setAttribute("total", total);
                request.setAttribute("page", page);
                request.setAttribute("pageSize", pageSize);
                request.setAttribute("q", q);
                request.setAttribute("status", status);
                view(request, response, "students/list.jsp");
            }
            case "/students/add" -> {
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("guardians", registrationService.searchGuardians(null, 0, 100));
                view(request, response, "students/form.jsp");
            }
            case "/students/edit" -> {
                Student student = registrationService.findStudent(longParam(request, "id", -1));
                if (student == null) {
                    redirect(request, response, "/students");
                    return;
                }
                request.setAttribute("student", student);
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("guardians", registrationService.searchGuardians(null, 0, 100));
                request.setAttribute("enrolments", registrationService.enrolmentsOfStudent(student.getId()));
                view(request, response, "students/form.jsp");
            }
            case "/students/view" -> {
                Student student = registrationService.findStudent(longParam(request, "id", -1));
                if (student == null) {
                    redirect(request, response, "/students");
                    return;
                }
                // Students may view only their own profile.
                if (visibleStudentIds(user) != null && !visibleStudentIds(user).contains(student.getId())) {
                    redirect(request, response, "/dashboard?denied=students");
                    return;
                }
                request.setAttribute("student", student);
                request.setAttribute("enrolments", registrationService.enrolmentsOfStudent(student.getId()));
                view(request, response, "students/view.jsp");
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
                case "/students/add" -> {
                    Student student = readStudent(request);
                    Guardian guardian = readGuardian(request);
                    Long classId = longParam(request, "classId", -1) == -1 ? null : longParam(request, "classId", -1);
                    boolean approvingReenrolment = "on".equals(request.getParameter("approveReenrolment"));
                    Student saved = registrationService.registerStudent(user, student, guardian, classId, approvingReenrolment);
                    String temporaryPassword = RegistrationService.StudentAccountCredentials.consume();
                    String credentialsMessage = "Student registered successfully. Registration number: "
                            + saved.getRegNo()
                            + ". Login username: " + saved.getRegNo()
                            + ". Temporary password: " + temporaryPassword
                            + ". Give these credentials to the student; the password is shown only once.";
                    com.edutrack.util.Flash.success(request.getSession(), credentialsMessage);
                    redirect(request, response, "/students/view?id=" + saved.getId());
                }
                case "/students/edit" -> {
                    Student student = readStudent(request);
                    student.setId(longParam(request, "id", -1));
                    Long guardianId = longParam(request, "guardianId", -1);
                    student.setGuardianId(guardianId == -1 ? null : guardianId);
                    registrationService.updateStudent(user, student);
                    com.edutrack.util.Flash.success(request.getSession(), "Student profile updated.");
                    redirect(request, response, "/students/view?id=" + student.getId());
                }
                case "/students/enrol" -> {
                    long studentId = longParam(request, "id", -1);
                    Long classId = longParam(request, "classId", -1);
                    if (classId == -1) throw new com.edutrack.exception.BusinessRuleException("Select a class to enrol into.");
                    boolean approvingReenrolment = "on".equals(request.getParameter("approveReenrolment"));
                    Student student = registrationService.findStudent(studentId);
                    registrationService.enrol(user, student, classId, approvingReenrolment);
                    com.edutrack.util.Flash.success(request.getSession(), "Student enrolled into the class.");
                    redirect(request, response, "/students/view?id=" + studentId);
                }
                case "/students/withdraw" -> {
                    long studentId = longParam(request, "id", -1);
                    registrationService.withdrawStudent(user, studentId, stringParam(request, "reason"));
                    com.edutrack.util.Flash.success(request.getSession(), "Student marked as withdrawn. History retained.");
                    redirect(request, response, "/students/view?id=" + studentId);
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, "/students", e);
        }
    }

    private Student readStudent(HttpServletRequest request) {
        Student s = new Student();
        s.setFirstName(stringParam(request, "firstName"));
        s.setLastName(stringParam(request, "lastName"));
        s.setNameWithInitials(stringParam(request, "nameWithInitials"));
        s.setNic(stringParam(request, "nic"));
        s.setGender(stringParam(request, "gender"));
        String dob = stringParam(request, "dob");
        s.setDob(dob == null || dob.isBlank() ? null : LocalDate.parse(dob));
        s.setAddress(stringParam(request, "address"));
        s.setPhone(stringParam(request, "phone"));
        s.setEmail(stringParam(request, "email"));
        s.setGuardianId(longParam(request, "guardianId", -1) == -1 ? null : longParam(request, "guardianId", -1));
        s.setMedicalNotes(stringParam(request, "medicalNotes"));
        return s;
    }

    private Guardian readGuardian(HttpServletRequest request) {
        // A new guardian is created only when the form carries a guardian name.
        String name = stringParam(request, "guardianName");
        if (name == null || name.isBlank()) {
            return null;
        }
        Guardian g = new Guardian();
        g.setFullName(name);
        g.setNic(stringParam(request, "guardianNic"));
        g.setPhone(stringParam(request, "guardianPhone"));
        g.setEmail(stringParam(request, "guardianEmail"));
        g.setOccupation(stringParam(request, "guardianOccupation"));
        g.setRelationship(stringParam(request, "guardianRelationship"));
        g.setAddress(stringParam(request, "guardianAddress"));
        return g;
    }
}
