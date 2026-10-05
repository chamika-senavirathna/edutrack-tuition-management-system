package com.edutrack.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * One-shot flash messages displayed after redirects (success/error feedback).
 */
public final class Flash {
    public static final String SUCCESS = "flashSuccess";
    public static final String ERROR = "flashError";

    private Flash() {
    }

    public static void success(HttpSession session, String message) {
        session.setAttribute(SUCCESS, message);
    }

    public static void error(HttpSession session, String message) {
        session.setAttribute(ERROR, message);
    }

    /** Pops flash attributes from session into request scope for display, then removes them. */
    public static void consume(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object s = session.getAttribute(SUCCESS);
            if (s != null) {
                request.setAttribute(SUCCESS, s);
                session.removeAttribute(SUCCESS);
            }
            Object e = session.getAttribute(ERROR);
            if (e != null) {
                request.setAttribute(ERROR, e);
                session.removeAttribute(ERROR);
            }
        }
    }
}
