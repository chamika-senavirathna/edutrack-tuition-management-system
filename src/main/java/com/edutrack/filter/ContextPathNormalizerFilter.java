package com.edutrack.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Makes an accidental duplicated context URL (for example
 * /edutrack/edutrack/) redirect safely to the real application root.
 * This is especially useful with IDE-managed Tomcat configurations.
 */
public class ContextPathNormalizerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String context = req.getContextPath();
        String uri = req.getRequestURI();

        if (!context.isEmpty() && uri.startsWith(context + context)) {
            String corrected = uri.substring(context.length());
            // Remove exactly one duplicate context prefix.
            if (corrected.startsWith(context)) {
                corrected = corrected.substring(context.length());
            }
            if (corrected.isEmpty()) corrected = "/";
            String qs = req.getQueryString();
            if (qs != null && !qs.isBlank()) corrected += "?" + qs;
            resp.sendRedirect(req.getScheme() + "://" + req.getServerName()
                    + ":" + req.getServerPort() + context + corrected);
            return;
        }
        chain.doFilter(request, response);
    }
}
