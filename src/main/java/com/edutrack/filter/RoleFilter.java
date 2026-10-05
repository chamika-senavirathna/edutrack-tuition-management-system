package com.edutrack.filter;

import com.edutrack.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Role-based URL guard (Member 05, RBAC). First line of defence at the URL
 * level; fine-grained record-level checks stay in the services so that
 * authorisation is never JSP-only.
 */
public class RoleFilter implements Filter {

    /** URL prefix -> roles allowed. ADMIN always passes; unlisted URLs pass for authenticated users. */
    private static final Map<String, Set<String>> GUARDS = buildGuards();

    private static Map<String, Set<String>> buildGuards() {
        Map<String, Set<String>> guards = new java.util.LinkedHashMap<>();
        guards.put("/users", Set.of("ADMIN"));
        guards.put("/audit", Set.of("ADMIN", "PRINCIPAL"));
        // Member 01 - Registration: reads visible to own-record viewers, writes to staff only.
        guards.put("/students/add", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/students/edit", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/students/withdraw", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/students/enrol", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/students", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "FINANCE", "STUDENT", "PARENT"));
        guards.put("/guardians/add", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/guardians/edit", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/guardians/save", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/guardians", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL"));
        guards.put("/teachers/add", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/teachers/edit", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/teachers/resign", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/teachers", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL"));
        // Member 02 - Class, Timetable & Communication: students/parents read published views.
        guards.put("/classes/add", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/classes/edit", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/classes/archive", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/classes", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "STUDENT", "PARENT"));
        guards.put("/subjects/save", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/subjects", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "STUDENT", "PARENT"));
        guards.put("/rooms/save", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/rooms", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER"));
        guards.put("/timetable/add", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/timetable/edit", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/timetable/transition", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL"));
        guards.put("/timetable", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "STUDENT", "PARENT"));
        guards.put("/notices/add", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER"));
        guards.put("/notices/edit", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER"));
        guards.put("/notices/archive", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER"));
        guards.put("/notices", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "FINANCE", "STUDENT", "PARENT"));
        guards.put("/materials/edit", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER"));
        guards.put("/materials/add", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER"));
        guards.put("/materials/archive", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER"));
        guards.put("/materials", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "STUDENT", "PARENT"));
        // Member 03 - Attendance: students/parents see own history; only staff mark/correct.
        guards.put("/attendance/mark", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "TEACHER"));
        guards.put("/attendance/correct", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "TEACHER"));
        guards.put("/attendance/chronic", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER"));
        guards.put("/attendance", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "STUDENT", "PARENT"));
        // Member 04 - Fees: students/parents see own invoices/payments/receipts; finance staff manage.
        guards.put("/fees/add", Set.of("ADMIN", "FINANCE"));
        guards.put("/fees/edit", Set.of("ADMIN", "FINANCE"));
        guards.put("/fees/void", Set.of("ADMIN", "FINANCE", "PRINCIPAL"));
        guards.put("/fees", Set.of("ADMIN", "FINANCE", "PRINCIPAL", "STUDENT", "PARENT"));
        guards.put("/payments/add", Set.of("ADMIN", "FINANCE"));
        guards.put("/payments/verify", Set.of("ADMIN", "FINANCE", "PRINCIPAL"));
        guards.put("/payments/refund", Set.of("ADMIN", "FINANCE", "PRINCIPAL"));
        guards.put("/payments", Set.of("ADMIN", "FINANCE", "PRINCIPAL", "STUDENT", "PARENT"));
        guards.put("/receipts", Set.of("ADMIN", "FINANCE", "PRINCIPAL", "STUDENT", "PARENT"));
        // Member 06 - Examinations & Reporting: students/parents read own results.
        guards.put("/examinations/add", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/examinations/edit", Set.of("ADMIN", "ACADEMIC_COORDINATOR"));
        guards.put("/examinations/transition", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL"));
        guards.put("/examinations", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "STUDENT", "PARENT"));
        guards.put("/marks/submit", Set.of("ADMIN", "TEACHER"));
        guards.put("/marks/enter", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "TEACHER"));
        guards.put("/marks/moderate", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL"));
        guards.put("/marks/publish", Set.of("ADMIN", "PRINCIPAL"));
        guards.put("/marks", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "STUDENT", "PARENT"));
        guards.put("/results", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL", "TEACHER", "FINANCE", "STUDENT", "PARENT"));
        guards.put("/reportcards/generate", Set.of("ADMIN", "ACADEMIC_COORDINATOR", "PRINCIPAL"));
        guards.put("/reportcards/publish", Set.of("ADMIN", "PRINCIPAL"));
        guards.put("/reportcards", Set.of("ADMIN", "PRINCIPAL", "ACADEMIC_COORDINATOR", "TEACHER", "FINANCE", "STUDENT", "PARENT"));
        guards.put("/reports", Set.of("ADMIN", "PRINCIPAL", "ACADEMIC_COORDINATOR", "FINANCE"));
        return guards;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getServletPath();
        if (path.isEmpty()) {
            path = "/";
        }

        String bestMatch = null;
        for (String guard : GUARDS.keySet()) {
            boolean matches = path.equals(guard) || path.startsWith(guard + "/") || path.startsWith(guard + "?");
            if (matches && (bestMatch == null || guard.length() > bestMatch.length())) {
                bestMatch = guard;
            }
        }

        if (bestMatch != null) {
            HttpSession session = req.getSession(false);
            User user = session == null ? null : (User) session.getAttribute("user");
            Set<String> allowed = GUARDS.get(bestMatch);
            boolean ok = user != null && (user.hasRole("ADMIN") || allowed.stream().anyMatch(user::hasRole));
            if (!ok) {
                resp.sendRedirect(req.getContextPath() + "/dashboard?denied=" + bestMatch);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
