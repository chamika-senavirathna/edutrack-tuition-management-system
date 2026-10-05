package com.edutrack.servlet;

import com.edutrack.model.User;
import com.edutrack.service.AuditService;
import com.edutrack.service.UserService;
import com.edutrack.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User & Access controller (Member 05). Account CRUD, role assignment,
 * activation/deactivation, audit trail viewer and own-profile password change.
 */
@WebServlet({"/users", "/users/add", "/users/edit", "/users/status",
        "/audit", "/profile", "/profile/password"})
public class UserServlet extends BaseServlet {

    private final UserService userService = new UserService();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        switch (path) {
            case "/users" -> {
                String q = stringParam(request, "q");
                String role = stringParam(request, "role");
                String status = stringParam(request, "status");
                request.setAttribute("users", userService.searchUsers(q, role, status, 0, 50));
                request.setAttribute("roles", userService.allRoles());
                request.setAttribute("stats", userService.userStats());
                request.setAttribute("q", q);
                request.setAttribute("role", role);
                request.setAttribute("status", status);
                view(request, response, "users/list.jsp");
            }
            case "/users/add" -> {
                request.setAttribute("roles", userService.allRoles());
                view(request, response, "users/form.jsp");
            }
            case "/users/edit" -> {
                request.setAttribute("editUser", userService.findUser(longParam(request, "id", -1)));
                request.setAttribute("roles", userService.allRoles());
                view(request, response, "users/form.jsp");
            }
            case "/audit" -> {
                String entityType = stringParam(request, "entityType");
                Long entityId = longParam(request, "entityId", -1) == -1 ? null : longParam(request, "entityId", -1);
                request.setAttribute("logs", auditService.search(entityType, entityId, null, 0, 100));
                request.setAttribute("recent", auditService.recent(10));
                request.setAttribute("entityType", entityType);
                request.setAttribute("entityId", request.getParameter("entityId"));
                view(request, response, "users/audit.jsp");
            }
            case "/profile" -> {
                request.setAttribute("profileUser", userService.findUser(user.getId()));
                view(request, response, "users/profile.jsp");
            }
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User actor = currentUser(request);
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/users/add" -> {
                    User newUser = new User();
                    newUser.setUsername(stringParam(request, "username"));
                    newUser.setEmail(stringParam(request, "email"));
                    newUser.setFullName(stringParam(request, "fullName"));
                    newUser.setPhone(stringParam(request, "phone"));
                    newUser.setUserType(stringParam(request, "userType"));
                    newUser.setStudentId(longParam(request, "studentId", -1) == -1 ? null : longParam(request, "studentId", -1));
                    newUser.setTeacherId(longParam(request, "teacherId", -1) == -1 ? null : longParam(request, "teacherId", -1));
                    newUser.setParentId(longParam(request, "parentId", -1) == -1 ? null : longParam(request, "parentId", -1));
                    List<Long> roleIds = readRoles(request);
                    User created = userService.createUser(actor, newUser, roleIds, stringParam(request, "password"));
                    Flash.success(request.getSession(), "User account created: " + created.getUsername());
                    redirect(request, response, "/users");
                }
                case "/users/edit" -> {
                    User changes = new User();
                    changes.setId(longParam(request, "id", -1));
                    changes.setEmail(stringParam(request, "email"));
                    changes.setFullName(stringParam(request, "fullName"));
                    changes.setPhone(stringParam(request, "phone"));
                    changes.setUserType(stringParam(request, "userType"));
                    changes.setStudentId(longParam(request, "studentId", -1) == -1 ? null : longParam(request, "studentId", -1));
                    changes.setTeacherId(longParam(request, "teacherId", -1) == -1 ? null : longParam(request, "teacherId", -1));
                    changes.setParentId(longParam(request, "parentId", -1) == -1 ? null : longParam(request, "parentId", -1));
                    List<Long> roleIds = readRoles(request);
                    userService.updateUser(actor, changes, roleIds);
                    Flash.success(request.getSession(), "User account updated.");
                    redirect(request, response, "/users");
                }
                case "/users/status" -> {
                    long id = longParam(request, "id", -1);
                    boolean activate = "activate".equals(stringParam(request, "action"));
                    userService.setUserStatus(actor, id, activate, stringParam(request, "reason"));
                    Flash.success(request.getSession(), activate ? "Account activated." : "Account deactivated (BR-UAM-03: not deleted).");
                    redirect(request, response, "/users");
                }
                case "/profile/password" -> {
                    userService.changePassword(actor, stringParam(request, "currentPassword"),
                            stringParam(request, "newPassword"));
                    Flash.success(request.getSession(), "Password changed successfully.");
                    redirect(request, response, "/profile");
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, path.startsWith("/profile") ? "/profile" : "/users", e);
        }
    }

    private List<Long> readRoles(HttpServletRequest request) {
        List<Long> roleIds = new ArrayList<>();
        String[] values = request.getParameterValues("roleIds");
        if (values != null) {
            for (String v : values) {
                try {
                    roleIds.add(Long.parseLong(v));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return roleIds;
    }
}
