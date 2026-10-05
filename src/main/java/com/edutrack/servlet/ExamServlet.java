package com.edutrack.servlet;

import com.edutrack.model.User;
import com.edutrack.service.ExaminationService;
import com.edutrack.service.RegistrationService;
import com.edutrack.service.TermService;
import com.edutrack.service.TimetableService;
import com.edutrack.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Examination & Reporting controller (Member 06). Exam CRUD, marks entry with
 * BR-EXM-01 restriction, submit -> moderate -> publish workflow, report cards
 * with auto grades, and performance analytics.
 */
@WebServlet({"/examinations", "/examinations/add", "/examinations/edit", "/examinations/transition", "/examinations/view",
        "/marks", "/marks/submit", "/marks/enter", "/marks/moderate", "/marks/publish",
        "/results", "/reports"})
public class ExamServlet extends BaseServlet {

    private final ExaminationService examinationService = new ExaminationService();
    private final TimetableService timetableService = new TimetableService();
    private final RegistrationService registrationService = new RegistrationService();
    private final TermService termService = new TermService();
    private final com.edutrack.service.ReportService reportService = new com.edutrack.service.ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        switch (path) {
            case "/examinations" -> {
                String q = stringParam(request, "q");
                String status = stringParam(request, "status");
                Long classId = longParam(request, "classId", -1) == -1 ? null : longParam(request, "classId", -1);
                var exams = examinationService.searchExams(q, null, classId, null, status, 0, Integer.MAX_VALUE);
                var ownClasses = scopedClasses(user);
                if (ownClasses != null) {
                    var ids = ownClasses.stream().map(com.edutrack.model.ClassRoom::getId).toList();
                    exams = exams.stream().filter(e -> ids.contains(e.getClassId())).toList();
                }
                request.setAttribute("exams", exams);
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("terms", termService.allTerms());
                request.setAttribute("q", q);
                request.setAttribute("status", status);
                request.setAttribute("classId", classId);
                view(request, response, "examinations/list.jsp");
            }
            case "/examinations/add" -> {
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("subjects", timetableService.subjects(false));
                request.setAttribute("teachers", registrationService.activeTeachers());
                request.setAttribute("terms", termService.allTerms());
                view(request, response, "examinations/form.jsp");
            }
            case "/examinations/edit" -> {
                request.setAttribute("exam", examinationService.findExam(longParam(request, "id", -1)));
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("subjects", timetableService.subjects(false));
                request.setAttribute("teachers", registrationService.activeTeachers());
                request.setAttribute("terms", termService.allTerms());
                view(request, response, "examinations/form.jsp");
            }
            case "/examinations/view" -> {
                long examId = longParam(request, "id", -1);
                var exam = examinationService.findExam(examId);
                if (exam == null) { response.sendError(404); return; }
                var ownClasses = scopedClasses(user);
                if (ownClasses != null && ownClasses.stream().noneMatch(c -> c.getId().equals(exam.getClassId())))
                    throw new com.edutrack.exception.AuthorizationException("This examination is not linked to your account.");
                var visible = visibleStudentIds(user);
                request.setAttribute("exam", exam);
                request.setAttribute("marks", examinationService.marksOfExam(examId).stream()
                    .filter(m -> visible == null || (visible.contains(m.getStudentId()) && "PUBLISHED".equals(m.getStatus()))).toList());
                view(request, response, "examinations/view.jsp");
            }
            case "/marks" -> {
                long examId = longParam(request, "id", -1);
                if (examId == -1) {
                    redirect(request, response, "/examinations");
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/examinations/view?id=" + examId);
            }
            case "/marks/enter" -> {
                long examId = longParam(request, "id", -1);
                var exam = examinationService.findExam(examId);
                if (exam == null) { response.sendError(404); return; }
                examinationService.requireMarksAccess(user, exam);
                request.setAttribute("exam", exam);
                request.setAttribute("students", registrationService.enrolmentsOfClass(exam.getClassId(), "ACTIVE")
                        .stream().map(e -> registrationService.findStudent(e.getStudentId())).filter(java.util.Objects::nonNull).toList());
                request.setAttribute("existing", examinationService.marksOfExam(examId).stream()
                        .collect(java.util.stream.Collectors.toMap(com.edutrack.model.Marks::getStudentId, m -> m)));
                view(request, response, "examinations/marks_entry.jsp");
            }
            case "/results" -> {
                Long studentId = resolveSelfOrChildStudentId(user, request);
                request.setAttribute("marksList", studentId == null ? java.util.List.of()
                        : examinationService.marksOfStudent(studentId).stream()
                            .filter(m -> visibleStudentIds(user) == null || "PUBLISHED".equals(m.getStatus())).toList());
                var visible = visibleStudentIds(user);
                request.setAttribute("students", visible == null ? registrationService.searchStudents(null, null, null, 0, 500)
                        : visible.stream().map(registrationService::findStudent).filter(java.util.Objects::nonNull).toList());
                request.setAttribute("studentId", studentId);
                view(request, response, "examinations/results.jsp");
            }
            case "/reports" -> {
                int year = intParam(request, "year", LocalDate.now().getYear());
                int month = Math.min(12, Math.max(1, intParam(request, "month", LocalDate.now().getMonthValue())));
                request.setAttribute("performance", examinationService.examPerformance(
                        longParam(request, "examId", -1) == -1 ? null : longParam(request, "examId", -1)));
                request.setAttribute("feeCollection", reportService.feeCollection());
                request.setAttribute("utilization", reportService.timetableUtilisation());
                request.setAttribute("attendanceMonthly", reportService.monthlyAttendanceSummary(year, month));
                request.setAttribute("revenueMonthly", reportService.revenueSummary(year, month));
                request.setAttribute("revenueTotal", reportService.revenueTotal(year, month));
                request.setAttribute("month", month);
                request.setAttribute("year", year);
                if ("csv".equalsIgnoreCase(stringParam(request, "format"))) {
                    exportMonthlyCsv(response, year, month);
                    return;
                }
                view(request, response, "examinations/reports.jsp");
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
                case "/examinations/add" -> {
                    com.edutrack.model.Examination exam = readExam(request);
                    examinationService.createExam(user, exam);
                    Flash.success(request.getSession(), "Examination scheduled.");
                    redirect(request, response, "/examinations");
                }
                case "/examinations/edit" -> {
                    com.edutrack.model.Examination exam = readExam(request);
                    exam.setId(longParam(request, "id", -1));
                    examinationService.updateExam(user, exam);
                    Flash.success(request.getSession(), "Examination updated.");
                    redirect(request, response, "/examinations/view?id=" + exam.getId());
                }
                case "/examinations/transition" -> {
                    examinationService.transitionExam(user, longParam(request, "id", -1), stringParam(request, "action"));
                    Flash.success(request.getSession(), "Examination " + stringParam(request, "action") + "d.");
                    redirect(request, response, "/examinations");
                }
                case "/marks/enter" -> {
                    long examId = longParam(request, "examId", -1);
                    Map<Long, String> marksByStudent = new HashMap<>();
                    Map<Long, String> remarksByStudent = new HashMap<>();
                    request.getParameterMap().forEach((key, values) -> {
                        if (key.startsWith("mark_")) {
                            marksByStudent.put(Long.parseLong(key.substring("mark_".length())), values[0]);
                        } else if (key.startsWith("remarks_")) {
                            if (values[0] != null && !values[0].isBlank()) {
                                remarksByStudent.put(Long.parseLong(key.substring("remarks_".length())), values[0]);
                            }
                        }
                    });
                    examinationService.enterMarks(user, examId, marksByStudent, remarksByStudent);
                    Flash.success(request.getSession(), "Marks saved as DRAFT. Submit for moderation next.");
                    redirect(request, response, "/marks?id=" + examId);
                }
                case "/marks/submit" -> {
                    long examId = longParam(request, "examId", -1);
                    examinationService.submitForModeration(user, examId);
                    Flash.success(request.getSession(), "Marks submitted for moderation.");
                    redirect(request, response, "/marks?id=" + examId);
                }
                case "/marks/moderate" -> {
                    examinationService.moderate(user, longParam(request, "marksId", -1),
                            stringParam(request, "decision"), stringParam(request, "note"));
                    Flash.success(request.getSession(), "Moderation decision recorded.");
                    redirect(request, response, "/marks?id=" + longParam(request, "examId", -1));
                }
                case "/marks/publish" -> {
                    examinationService.publishResults(user, longParam(request, "examId", -1));
                    Flash.success(request.getSession(), "Moderated results published.");
                    redirect(request, response, "/marks?id=" + longParam(request, "examId", -1));
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, "/examinations", e);
        }
    }

    /** Downloads the monthly attendance + revenue report as CSV (M06 export). */
    private void exportMonthlyCsv(HttpServletResponse response, int year, int month) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"edutrack_monthly_report_" + year + "-"
                        + String.format("%02d", month) + ".csv\"");
        java.io.PrintWriter out = response.getWriter();
        out.println("EduTrack Sri Lanka - Monthly Administrative Report, " + year + "-" + String.format("%02d", month));
        out.println();
        out.println("SECTION,Monthly Attendance Summary (M03)");
        out.println("Class,Entries,Attended,Late,Absent,Attendance %");
        for (Map<String, Object> row : reportService.monthlyAttendanceSummary(year, month)) {
            out.println(csv(row.get("className")) + "," + row.get("entries") + "," + row.get("present")
                    + "," + row.get("late") + "," + row.get("absent") + ","
                    + (row.get("attendancePct") == null ? "" : row.get("attendancePct")));
        }
        out.println();
        out.println("SECTION,Revenue Collections (M04)");
        out.println("Method,Transactions,Amount (LKR)");
        for (Map<String, Object> row : reportService.revenueSummary(year, month)) {
            out.println(csv(row.get("method")) + "," + row.get("txns") + "," + row.get("amount"));
        }
        out.println("TOTAL," + reportService.revenueTotal(year, month));
        out.flush();
    }

    private static String csv(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        return s.indexOf(',') >= 0 || s.indexOf('"') >= 0 ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    private com.edutrack.model.Examination readExam(HttpServletRequest request) {
        com.edutrack.model.Examination x = new com.edutrack.model.Examination();
        x.setExamName(stringParam(request, "examName"));
        x.setTermId(longParam(request, "termId", -1) == -1 ? null : longParam(request, "termId", -1));
        x.setClassId(longParam(request, "classId", -1) == -1 ? null : longParam(request, "classId", -1));
        x.setSubjectId(longParam(request, "subjectId", -1) == -1 ? null : longParam(request, "subjectId", -1));
        x.setTeacherId(longParam(request, "teacherId", -1) == -1 ? null : longParam(request, "teacherId", -1));
        String date = stringParam(request, "examDate");
        x.setExamDate(date == null || date.isBlank() ? null : LocalDate.parse(date));
        x.setStartTime(stringParam(request, "startTime"));
        x.setEndTime(stringParam(request, "endTime"));
        x.setRoomText(stringParam(request, "roomText"));
        String max = stringParam(request, "maxMarks");
        x.setMaxMarks(max == null || max.isBlank() ? 100 : Integer.parseInt(max));
        return x;
    }
}
