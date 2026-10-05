package com.edutrack.servlet;

import com.edutrack.model.User;
import com.edutrack.service.UserService;
import com.edutrack.util.Csrf;
import com.edutrack.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication controller (Member 05). Handles login, logout,
 * OTP request and OTP-based password reset.
 */
@WebServlet({"/login", "/logout", "/forgot-password", "/reset-password"})
public class AuthServlet extends BaseServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/login" -> {
                User user = currentUser(request);
                if (user != null) {
                    redirect(request, response, "/dashboard");
                    return;
                }
                request.setAttribute("authError", request.getParameter("error"));
                request.setAttribute("csrfToken", Csrf.token(request));
                request.getRequestDispatcher("/WEB-INF/jsp/public/login.jsp").forward(request, response);
            }
            case "/logout" -> {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/login?error=loggedout");
            }
            case "/forgot-password" -> {
                request.setAttribute("csrfToken", Csrf.token(request));
                request.getRequestDispatcher("/WEB-INF/jsp/public/forgot_password.jsp").forward(request, response);
            }
            case "/reset-password" -> {
                request.setAttribute("otpIssued", request.getParameter("otp") != null);
                request.setAttribute("identity", request.getParameter("identity"));
                request.setAttribute("csrfToken", Csrf.token(request));
                request.getRequestDispatcher("/WEB-INF/jsp/public/reset_password.jsp").forward(request, response);
            }
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/login" -> doLogin(request, response);
            case "/forgot-password" -> doForgot(request, response);
            case "/reset-password" -> doReset(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = stringParam(request, "username");
        String password = request.getParameter("password");
        try {
            User user = userService.login(username, password);
            // Session fixation protection: preserve the requested target before
            // invalidating the old session, then recreate a clean session.
            HttpSession old = request.getSession(false);
            String target = old == null ? null : (String) old.getAttribute("redirectAfterLogin");
            if (old != null) old.invalidate();
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            Flash.success(session, "Welcome back, " + user.getFullName() + "!");
            response.sendRedirect(request.getContextPath()
                    + (target != null && target.startsWith(request.getContextPath()) ? target : "/dashboard"));
        } catch (RuntimeException e) {
            String message = e instanceof com.edutrack.exception.ValidationException ve
                    ? String.join(" ", ve.getFieldErrors().values())
                    : e.getMessage();
            Flash.error(request.getSession(), message);
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    private void doForgot(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String identity = stringParam(request, "identity");
        try {
            String otp = userService.requestPasswordReset(identity);
            // Demo environment: the OTP is shown to the requester. In production it is
            // delivered by SMS/email (documented channel fallback is an OPEN decision).
            if (otp != null) {
                response.sendRedirect(request.getContextPath()
                        + "/reset-password?identity=" + java.net.URLEncoder.encode(identity, java.nio.charset.StandardCharsets.UTF_8)
                        + "&otp=" + otp);
                return;
            }
            Flash.error(request.getSession(), "If the account exists, an OTP has been issued.");
            response.sendRedirect(request.getContextPath() + "/forgot-password");
        } catch (RuntimeException e) {
            Flash.error(request.getSession(), e.getMessage());
            response.sendRedirect(request.getContextPath() + "/forgot-password");
        }
    }

    private void doReset(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String identity = stringParam(request, "identity");
        String otp = stringParam(request, "otp");
        String newPassword = request.getParameter("newPassword");
        try {
            userService.resetPassword(identity, otp, newPassword);
            Flash.success(request.getSession(), "Password changed. You can now sign in.");
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (RuntimeException e) {
            String message = e instanceof com.edutrack.exception.ValidationException ve
                    ? String.join(" ", ve.getFieldErrors().values())
                    : e.getMessage();
            Flash.error(request.getSession(), message);
            response.sendRedirect(request.getContextPath() + "/reset-password?identity="
                    + java.net.URLEncoder.encode(identity == null ? "" : identity, java.nio.charset.StandardCharsets.UTF_8)
                    + "&otp=" + java.net.URLEncoder.encode(otp == null ? "" : otp, java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
