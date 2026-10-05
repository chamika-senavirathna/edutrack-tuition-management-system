package com.edutrack.servlet;

import com.edutrack.model.User;
import com.edutrack.service.AttendanceService;
import com.edutrack.service.ExaminationService;
import com.edutrack.service.FeeService;
import com.edutrack.service.RegistrationService;
import com.edutrack.service.ReportService;
import com.edutrack.service.TimetableService;
import com.edutrack.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Dashboard controller. Content adapts to the logged-in role
 * (Section 18: role-dependent dashboards).
 */
@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {

    private final ReportService reportService = new ReportService();
    private final TimetableService timetableService = new TimetableService();
    private final RegistrationService registrationService = new RegistrationService();
    private final FeeService feeService = new FeeService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final ExaminationService examinationService = new ExaminationService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        boolean staff = user.hasRole("ADMIN") || user.hasRole("ACADEMIC_COORDINATOR")
                || user.hasRole("PRINCIPAL") || user.hasRole("FINANCE");

        if (staff) {
            request.setAttribute("stats", reportService.instituteStats());
            BigDecimal[] finance = reportService.financeTotals();
            request.setAttribute("financeInvoiced", finance[0]);
            request.setAttribute("financeCollected", finance[1]);
            request.setAttribute("financeOutstanding", finance[2]);
            request.setAttribute("userStats", userService.userStats());
            if (user.hasRole("FINANCE") && !user.hasRole("ADMIN") && !user.hasRole("PRINCIPAL")) {
                request.setAttribute("recentInvoices", feeService.searchInvoices(null, null, null, null, null, 0, 6));
                request.setAttribute("recentPayments", feeService.searchPayments(null, null, null, null, null, 0, 6));
            }
        }

        if (user.hasRole("TEACHER") && user.getTeacherId() != null) {
            request.setAttribute("myClasses", timetableService.classesOfTeacher(user.getTeacherId(), "ACTIVE"));
            request.setAttribute("mySlots", timetableService.searchSlots(null, user.getTeacherId(), null,
                    "PUBLISHED", 0, 12));
        }

        if (user.getStudentId() != null) {
            Long sid = user.getStudentId();
            request.setAttribute("myEnrolments", registrationService.enrolmentsOfStudent(sid));
            List<com.edutrack.model.ClassRoom> myClasses = timetableService.classesOfStudent(sid, "ACTIVE");
            request.setAttribute("myClasses", myClasses);
            List<Long> classIds = myClasses.stream().map(com.edutrack.model.ClassRoom::getId).toList();
            request.setAttribute("mySlots", timetableService.publishedTimetable(classIds));
            request.setAttribute("myInvoices", feeService.invoicesOfStudent(sid));
            request.setAttribute("myMarks", examinationService.marksOfStudent(sid).stream().filter(m -> "PUBLISHED".equals(m.getStatus())).toList());
            request.setAttribute("attendanceTrend", reportService.attendanceTrend(sid));
        }

        if (user.hasRole("PARENT") && user.getParentId() != null) {
            request.setAttribute("children", registrationService.searchStudents(null, user.getParentId(), null, 0, 20));
        }

        // Notices visible to this viewer (audience-filtered)
        request.setAttribute("notices", timetableService.noticesForViewer(user, user.getStudentId(),
                user.getTeacherId(),
                user.getStudentId() != null
                        ? timetableService.classesOfStudent(user.getStudentId(), "ACTIVE")
                                .stream().map(com.edutrack.model.ClassRoom::getId).toList()
                        : List.of(),
                8));
        request.setAttribute("denied", request.getParameter("denied"));
        view(request, response, "dashboard.jsp");
    }
}
