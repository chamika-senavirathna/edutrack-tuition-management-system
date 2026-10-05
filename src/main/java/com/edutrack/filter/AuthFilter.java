package com.edutrack.filter;

import com.edutrack.model.User;
import com.edutrack.util.Csrf;
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

/**
 * Authentication filter (Member 05). All URLs are protected except the
 * explicitly public list. Session fixation is mitigated by regenerating the
 * session at login (LoginServlet).
 */
public class AuthFilter implements Filter {

    private static final String[] PUBLIC_PATHS = {
            "/login", "/logout", "/forgot-password", "/reset-password",
            "/css/", "/js/", "/images/", "/index.jsp", "/error/"
    };

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

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            // Remember the target so the user can be redirected back after login.
            if ("GET".equals(req.getMethod())) {
                session = req.getSession(true);
                session.setAttribute("redirectAfterLogin", req.getRequestURI()
                        + (req.getQueryString() == null ? "" : "?" + req.getQueryString()));
            }
            resp.sendRedirect(req.getContextPath() + "/login?error=auth");
            return;
        }

        // Apply role changes and account deactivation to existing sessions immediately.
        User fresh = new com.edutrack.service.UserService().findUser(user.getId());
        if (fresh == null || !Boolean.TRUE.equals(fresh.getActive())) {
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login?error=auth");
            return;
        }
        session.setAttribute("user", fresh);
        // Refresh session activity timestamp (timeout enforced by container).
        session.setAttribute("lastAccess", System.currentTimeMillis());
        chain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        for (String p : PUBLIC_PATHS) {
            if (path.equals(p) || (p.endsWith("/") && path.startsWith(p))) {
                return true;
            }
        }
        return false;
    }
}
