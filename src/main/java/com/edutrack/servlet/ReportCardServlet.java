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

/** Report card controller (Member 06): generate, list, publish (BR-EXM-03). */
@WebServlet({"/reportcards", "/reportcards/generate", "/reportcards/publish", "/reportcards/view"})
public class ReportCardServlet extends BaseServlet {

    private final ExaminationService examinationService = new ExaminationService();
    private final TermService termService = new TermService();
    private final TimetableService timetableService = new TimetableService();
    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        switch (path) {
            case "/reportcards" -> {
                Long termId = longParam(request, "termId", -1) == -1 ? null : longParam(request, "termId", -1);
                Long classId = longParam(request, "classId", -1) == -1 ? null : longParam(request, "classId", -1);
                var cards = examinationService.searchReportCards(termId, classId, visibleStudentIds(user) == null ? null : "PUBLISHED", 0, Integer.MAX_VALUE);
                Long selfId = resolveSelfOrChildStudentId(user, request);
                if (selfId != null) {
                    cards = cards.stream().filter(c -> selfId.equals(c.getStudentId())).toList();
                }
                request.setAttribute("cards", cards);
                request.setAttribute("terms", termService.allTerms());
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("termId", termId);
                request.setAttribute("classId", classId);
                view(request, response, "examinations/reportcards.jsp");
            }
            case "/reportcards/view" -> {
                var card = examinationService.findReportCard(longParam(request, "id", -1));
                var visible = visibleStudentIds(user);
                if (card == null) { response.sendError(404); return; }
                if (visible != null && (!visible.contains(card.getStudentId()) || !"PUBLISHED".equals(card.getStatus()))) {
                    redirect(request, response, "/dashboard?denied=reportcards");
                    return;
                }
                request.setAttribute("card", card);
                view(request, response, "examinations/reportcard_view.jsp");
            }
            case "/reportcards/generate" -> {
                request.setAttribute("terms", termService.allTerms());
                request.setAttribute("students", registrationService.searchStudents(null, null, "ACTIVE", 0, 500));
                view(request, response, "examinations/reportcard_generate.jsp");
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
                case "/reportcards/generate" -> {
                    long studentId = longParam(request, "studentId", -1);
                    long termId = longParam(request, "termId", -1);
                    var card = examinationService.generateReportCard(user, studentId, termId);
                    Flash.success(request.getSession(),
                            "Report card generated. Average " + card.getAverageMarks() + "% ("
                                    + card.getOverallGrade() + "). Publish it when ready.");
                    redirect(request, response, "/reportcards");
                }
                case "/reportcards/publish" -> {
                    examinationService.publishReportCard(user, longParam(request, "id", -1));
                    Flash.success(request.getSession(), "Report card published (audited).");
                    redirect(request, response, "/reportcards");
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, "/reportcards", e);
        }
    }
}
