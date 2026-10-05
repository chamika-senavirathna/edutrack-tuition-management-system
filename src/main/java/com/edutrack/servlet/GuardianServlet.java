package com.edutrack.servlet;

import com.edutrack.model.Guardian;
import com.edutrack.model.User;
import com.edutrack.service.RegistrationService;
import com.edutrack.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Guardian/parent profile controller (Member 01): list, search, edit. */
@WebServlet({"/guardians", "/guardians/edit", "/guardians/save"})
public class GuardianServlet extends BaseServlet {

    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/guardians" -> {
                String q = stringParam(request, "q");
                request.setAttribute("guardians", registrationService.searchGuardians(q, 0, 50));
                request.setAttribute("q", q);
                view(request, response, "guardians/list.jsp");
            }
            case "/guardians/edit" -> {
                request.setAttribute("guardian", registrationService.findGuardian(longParam(request, "id", -1)));
                view(request, response, "guardians/form.jsp");
            }
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        try {
            Guardian guardian = new Guardian();
            guardian.setId(longParam(request, "id", -1) == -1 ? null : longParam(request, "id", -1));
            guardian.setFullName(stringParam(request, "fullName"));
            guardian.setNic(stringParam(request, "nic"));
            guardian.setPhone(stringParam(request, "phone"));
            guardian.setEmail(stringParam(request, "email"));
            guardian.setOccupation(stringParam(request, "occupation"));
            guardian.setRelationship(stringParam(request, "relationship"));
            guardian.setAddress(stringParam(request, "address"));
            registrationService.saveGuardian(user, guardian);
            Flash.success(request.getSession(), "Guardian saved.");
            redirect(request, response, "/guardians");
        } catch (RuntimeException e) {
            handle(request, response, "/guardians", e);
        }
    }
}
