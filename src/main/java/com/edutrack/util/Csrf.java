package com.edutrack.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Per-session CSRF token (double-submit pattern) protecting all POST forms.
 */
public final class Csrf {
    public static final String PARAM = "csrfToken";
    private static final String SESSION_ATTR = "csrfToken";

    private Csrf() {
    }

    /** Returns the session token, creating it lazily. */
    public static String token(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String token = (String) session.getAttribute(SESSION_ATTR);
        if (token == null) {
            token = java.util.UUID.randomUUID().toString().replace("-", "");
            session.setAttribute(SESSION_ATTR, token);
        }
        return token;
    }

    /** Hidden input for JSP forms. */
    public static String hiddenInput(HttpServletRequest request) {
        return "<input type=\"hidden\" name=\"" + PARAM + "\" value=\"" + token(request) + "\">";
    }
}
