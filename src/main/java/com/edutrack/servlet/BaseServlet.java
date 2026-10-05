package com.edutrack.servlet;

import com.edutrack.exception.AuthorizationException;
import com.edutrack.exception.BusinessRuleException;
import com.edutrack.exception.ValidationException;
import com.edutrack.model.Student;
import com.edutrack.model.User;
import com.edutrack.service.RegistrationService;
import com.edutrack.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/** Shared behaviour for all controller servlets. */
public abstract class BaseServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try { super.service(request, response); }
        catch (AuthorizationException e) { response.sendError(403, e.getMessage()); }
        catch (BusinessRuleException | IllegalArgumentException e) { response.sendError(400, e.getMessage()); }
    }

    protected java.util.List<com.edutrack.model.ClassRoom> scopedClasses(User user) {
        var service = new com.edutrack.service.TimetableService();
        var ids = visibleStudentIds(user);
        if (ids == null) return null;
        var classes = new java.util.LinkedHashMap<Long, com.edutrack.model.ClassRoom>();
        for (Long id : ids) for (var c : service.classesOfStudent(id, "ACTIVE")) classes.put(c.getId(), c);
        return new java.util.ArrayList<>(classes.values());
    }

    protected User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    /**
     * Record-level scoping for read paths (defence in depth alongside the
     * service-layer checks): STUDENT users are pinned to their own student id,
     * PARENT users to one of their linked children, and staff may filter
     * freely or pass a specific studentId. Returns null when no specific
     * student is implied. Throws AuthorizationException when a parent requests
     * a student that is not their child.
     */
    protected Long resolveSelfOrChildStudentId(User user, HttpServletRequest request) {
        if (user == null) throw new AuthorizationException("Not authenticated.");
        if ((user.hasRole("STUDENT") || "STUDENT".equals(user.getUserType())) && user.getStudentId() == null)
            throw new AuthorizationException("Your account is not linked to a student profile.");
        if ((user.hasRole("PARENT") || "PARENT".equals(user.getUserType())) && user.getParentId() == null)
            throw new AuthorizationException("Your account is not linked to a guardian profile.");
        if (user.getStudentId() != null) {
            return user.getStudentId();
        }
        if ((user.hasRole("PARENT") || "PARENT".equals(user.getUserType())) && user.getParentId() != null) {
            List<Student> children = new RegistrationService()
                    .searchStudents(null, user.getParentId(), null, 0, Integer.MAX_VALUE);
            long requested = longParam(request, "studentId", -1);
            if (requested != -1) {
                boolean owns = children.stream()
                        .anyMatch(c -> c.getId() != null && c.getId() == requested);
                if (!owns) {
                    throw new AuthorizationException("You can only view records of your own children.");
                }
                return requested;
            }
            return children.isEmpty() ? -1L : children.get(0).getId();
        }
        long param = longParam(request, "studentId", -1);
        return param == -1 ? null : param;
    }

    /**
     * Full set of student ids this user may see, or null for staff (no
     * restriction). Used for single-record ownership checks where a parent
     * must be allowed to view any of their children's records.
     */
    protected java.util.Set<Long> visibleStudentIds(User user) {
        if (user == null) throw new AuthorizationException("Not authenticated.");
        if ((user.hasRole("STUDENT") || "STUDENT".equals(user.getUserType())) && user.getStudentId() == null)
            throw new AuthorizationException("Your account is not linked to a student profile.");
        if ((user.hasRole("PARENT") || "PARENT".equals(user.getUserType())) && user.getParentId() == null)
            throw new AuthorizationException("Your account is not linked to a guardian profile.");
        if (user.getStudentId() != null) {
            return java.util.Set.of(user.getStudentId());
        }
        if ((user.hasRole("PARENT") || "PARENT".equals(user.getUserType())) && user.getParentId() != null) {
            List<Student> children = new RegistrationService()
                    .searchStudents(null, user.getParentId(), null, 0, Integer.MAX_VALUE);
            return children.stream()
                    .map(Student::getId)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
        }
        return null;
    }

    protected int intParam(HttpServletRequest request, String name, int def) {
        String v = request.getParameter(name);
        if (v == null || v.isBlank()) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected long longParam(HttpServletRequest request, String name, long def) {
        String v = request.getParameter(name);
        if (v == null || v.isBlank()) return def;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected String stringParam(HttpServletRequest request, String name) {
        String v = request.getParameter(name);
        return v == null ? null : v.trim();
    }

    /** Renders a JSP from the secured WEB-INF view folder. */
    protected void view(HttpServletRequest request, HttpServletResponse response, String jspPath)
            throws ServletException, IOException {
        Flash.consume(request);
        request.setAttribute("csrfToken", com.edutrack.util.Csrf.token(request));
        request.getRequestDispatcher("/WEB-INF/jsp/" + jspPath).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    /**
     * Central error handling: converts service exceptions into friendly
     * messages (never a stack trace) and returns to the given page.
     */
    protected void handle(HttpServletRequest request, HttpServletResponse response,
                          String errorRedirectPath, RuntimeException e) throws IOException {
        String message;
        if (e instanceof ValidationException ve) {
            StringBuilder sb = new StringBuilder();
            ve.getFieldErrors().forEach((field, msg) -> sb.append(msg).append(" "));
            message = sb.toString().trim();
        } else if (e instanceof BusinessRuleException || e instanceof AuthorizationException) {
            message = e.getMessage();
        } else {
            message = "An unexpected error occurred. Please try again.";
            e.printStackTrace(); // developer log only; user sees the friendly message
        }
        Flash.error(request.getSession(), message);
        redirect(request, response, errorRedirectPath);
    }
}
