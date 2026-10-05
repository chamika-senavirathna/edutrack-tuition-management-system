package com.edutrack.service;

import com.edutrack.dao.RoleDAO;
import com.edutrack.dao.UserDAO;
import com.edutrack.dao.impl.RoleDAOImpl;
import com.edutrack.dao.impl.UserDAOImpl;
import com.edutrack.exception.AuthorizationException;
import com.edutrack.exception.BusinessRuleException;
import com.edutrack.exception.ValidationException;
import com.edutrack.model.Role;
import com.edutrack.model.User;
import com.edutrack.util.AuditLogger;
import com.edutrack.util.CodeGenerator;
import com.edutrack.util.DB;
import com.edutrack.util.PasswordUtil;
import com.edutrack.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User & Access Management service (Member 05 - IT25103997).
 * Implements BR-UAM-01 (least privilege), BR-UAM-02 (audit old/new values),
 * BR-UAM-03 (deactivate, never hard-delete) and UC-UAM-01 (OTP recovery).
 */
public class UserService {

    private final UserDAO userDAO = new UserDAOImpl();
    private final RoleDAO roleDAO = new RoleDAOImpl();

    // --------------------------------------------------------------- auth

    public User login(String username, String password) {
        Validator v = new Validator();
        v.required(username, "username", "Username").required(password, "password", "Password");
        v.check();
        try (Connection conn = DB.getConnection()) {
            User user = userDAO.findByUsername(conn, username.trim());
            if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
                throw new BusinessRuleException("Invalid username or password.");
            }
            if (!Boolean.TRUE.equals(user.getActive())) {
                throw new BusinessRuleException("This account is deactivated. Contact an administrator.");
            }
            userDAO.updateLastLogin(conn, user.getId());
            AuditLogger.log(conn, user, "LOGIN", "USER", user.getId(), null, "success");
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Login failed due to a database error.", e);
        }
    }

    /** OTP request: generates a code valid for 5 minutes and stores it in password_reset_tokens. */
    public String requestPasswordReset(String usernameOrEmail) {
        Validator v = new Validator();
        v.required(usernameOrEmail, "usernameOrEmail", "Username or email");
        v.check();
        try (Connection conn = DB.getConnection()) {
            User user = userDAO.findByUsername(conn, usernameOrEmail.trim());
            if (user == null) {
                user = findByEmail(conn, usernameOrEmail.trim());
            }
            // Do not reveal whether the account exists (anti-enumeration).
            if (user == null || !Boolean.TRUE.equals(user.getActive())) {
                return null;
            }
            String otp = CodeGenerator.otp6();
            try (var ps = conn.prepareStatement(
                    "INSERT INTO password_reset_tokens (user_id, otp_code, expires_at, used) VALUES (?,?,?,0)")) {
                ps.setLong(1, user.getId());
                ps.setString(2, PasswordUtil.hash(otp)); // stored hashed, never plaintext
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now().plusMinutes(5)));
                ps.executeUpdate();
            }
            AuditLogger.log(conn, user, "PASSWORD_RESET_REQUEST", "USER", user.getId(), null, "otp issued");
            return otp; // In production this would be emailed/SMSed; demo shows it to the user.
        } catch (SQLException e) {
            throw new RuntimeException("Password reset failed due to a database error.", e);
        }
    }

    public boolean resetPassword(String usernameOrEmail, String otp, String newPassword) {
        Validator v = new Validator();
        v.required(usernameOrEmail, "usernameOrEmail", "Username or email")
                .required(otp, "otp", "OTP code")
                .strongPassword(newPassword, "newPassword", "New password");
        v.check();
        try (Connection conn = DB.getConnection()) {
            User user = userDAO.findByUsername(conn, usernameOrEmail.trim());
            if (user == null) user = findByEmail(conn, usernameOrEmail.trim());
            if (user == null) throw new BusinessRuleException("Invalid reset request.");
            var ps = conn.prepareStatement(
                    "SELECT id, otp_code, expires_at, used FROM password_reset_tokens " +
                    "WHERE user_id = ? AND used = 0 ORDER BY id DESC LIMIT 1");
            ps.setLong(1, user.getId());
            var rs = ps.executeQuery();
            if (!rs.next()) throw new BusinessRuleException("No pending reset request. Request a new OTP.");
            long tokenId = rs.getLong("id");
            String otpHash = rs.getString("otp_code");
            LocalDateTime expires = rs.getTimestamp("expires_at").toLocalDateTime();
            if (LocalDateTime.now().isAfter(expires)) {
                throw new BusinessRuleException("This OTP has expired. Request a new one.");
            }
            if (!PasswordUtil.verify(otp, otpHash)) {
                throw new BusinessRuleException("Incorrect OTP code.");
            }
            userDAO.updatePassword(conn, user.getId(), PasswordUtil.hash(newPassword));
            try (var up = conn.prepareStatement("UPDATE password_reset_tokens SET used = 1 WHERE id = ?")) {
                up.setLong(1, tokenId);
                up.executeUpdate();
            }
            AuditLogger.log(conn, user, "PASSWORD_RESET", "USER", user.getId(), null, "password changed via OTP");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Password reset failed due to a database error.", e);
        }
    }

    public void changePassword(User actor, String currentPassword, String newPassword) {
        Validator v = new Validator();
        v.required(currentPassword, "currentPassword", "Current password")
                .strongPassword(newPassword, "newPassword", "New password");
        v.check();
        try (Connection conn = DB.getConnection()) {
            User fresh = userDAO.findById(conn, actor.getId());
            if (!PasswordUtil.verify(currentPassword, fresh.getPasswordHash())) {
                throw new BusinessRuleException("Current password is incorrect.");
            }
            userDAO.updatePassword(conn, actor.getId(), PasswordUtil.hash(newPassword));
            AuditLogger.log(conn, actor, "PASSWORD_CHANGE", "USER", actor.getId(), null, "changed own password");
        } catch (SQLException e) {
            throw new RuntimeException("Password change failed due to a database error.", e);
        }
    }

    private User findByEmail(Connection conn, String email) throws SQLException {
        List<User> all = userDAO.search(conn, email, null, null, 0, 1);
        return all.stream().filter(u -> email.equalsIgnoreCase(u.getEmail())).findFirst().orElse(null);
    }

    // --------------------------------------------------------------- user CRUD

    public User createUser(User actor, User newUser, List<Long> roleIds, String plainPassword) {
        requirePermission(actor, "USER_CREATE");
        Validator v = new Validator();
        v.required(newUser.getUsername(), "username", "Username").username(newUser.getUsername(), "username", "Username");
        v.required(plainPassword, "password", "Password").strongPassword(plainPassword, "password", "Password");
        v.required(newUser.getFullName(), "fullName", "Full name");
        v.email(newUser.getEmail(), "email", "Email");
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ValidationException("roleIds", "At least one role must be assigned.");
        }
        v.check();
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
            validateAccount(conn, newUser, roleIds);
            if (userDAO.usernameExists(conn, newUser.getUsername().trim(), null)) {
                throw new BusinessRuleException("Username already exists (UC-UAM-01 A1).");
            }
            if (newUser.getEmail() != null && !newUser.getEmail().isBlank()
                    && userDAO.emailExists(conn, newUser.getEmail().trim(), null)) {
                throw new BusinessRuleException("Email is already registered.");
            }
            newUser.setPasswordHash(PasswordUtil.hash(plainPassword));
            User created = userDAO.insert(conn, newUser, roleIds);
            StringBuilder roleNames = new StringBuilder();
            for (Long rid : roleIds) {
                Role r = roleDAO.findById(conn, rid);
                if (r != null) {
                    if (roleNames.length() > 0) roleNames.append(",");
                    roleNames.append(r.getName());
                }
            }
            AuditLogger.log(conn, actor, "USER_CREATE", "USER", created.getId(), null,
                    created.getUsername() + " roles=" + roleNames);
            conn.commit();
            return created;
            } catch (Exception e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) {
            throw new RuntimeException("User creation failed due to a database error.", e);
        }
    }

    public void updateUser(User actor, User changes, List<Long> roleIds) {
        requirePermission(actor, "USER_UPDATE");
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
            User before = userDAO.findById(conn, changes.getId());
            if (before == null) throw new BusinessRuleException("User not found.");
            Validator v = new Validator();
            v.email(changes.getEmail(), "email", "Email").required(changes.getFullName(), "fullName", "Full name");
            v.check();
            if (changes.getEmail() != null && !changes.getEmail().isBlank()
                    && userDAO.emailExists(conn, changes.getEmail().trim(), changes.getId())) {
                throw new BusinessRuleException("Email is already registered to another user.");
            }
            validateAccount(conn, changes, roleIds);
            if (actor.getId().equals(changes.getId()) && before.hasRole("ADMIN")
                    && roleIds.stream().noneMatch(id -> { try { return "ADMIN".equals(roleDAO.findById(conn, id).getName()); }
                        catch (SQLException e) { throw new RuntimeException(e); } }))
                throw new BusinessRuleException("You cannot remove your own administrator role.");
            userDAO.update(conn, changes);
            if (roleIds != null && !roleIds.isEmpty()) {
                List<Long> beforeIds = new ArrayList<>();
                before.getRoles().forEach(ur -> beforeIds.add(ur.getRoleId()));
                userDAO.replaceRoles(conn, changes.getId(), roleIds);
                AuditLogger.log(conn, actor, "USER_ROLE_CHANGE", "USER", changes.getId(),
                        AuditLogger.rolesOf(before), rolesToString(conn, roleIds));
            }
            AuditLogger.log(conn, actor, "USER_UPDATE", "USER", changes.getId(),
                    before.getFullName() + " <" + before.getEmail() + ">",
                    changes.getFullName() + " <" + changes.getEmail() + ">");
            conn.commit();
            } catch (Exception e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) {
            throw new RuntimeException("User update failed due to a database error.", e);
        }
    }

    public void setUserStatus(User actor, long userId, boolean active, String reason) {
        requirePermission(actor, "USER_STATUS");
        try (Connection conn = DB.getConnection()) {
            User target = userDAO.findById(conn, userId);
            if (target == null) throw new BusinessRuleException("User not found.");
            if (target.getId().equals(actor.getId())) {
                throw new BusinessRuleException("You cannot change your own account status.");
            }
            userDAO.updateStatus(conn, userId, active);
            AuditLogger.log(conn, actor, active ? "USER_ACTIVATE" : "USER_DEACTIVATE",
                    "USER", userId, String.valueOf(before_active(target)), String.valueOf(active));
            if (!active && reason != null && !reason.isBlank()) {
                AuditLogger.log(conn, actor, "USER_DEACTIVATE_REASON", "USER", userId, null, reason);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Status change failed due to a database error.", e);
        }
    }

    private boolean before_active(User u) {
        return Boolean.TRUE.equals(u.getActive());
    }

    private void validateAccount(Connection conn, User user, List<Long> roleIds) throws SQLException {
        if (roleIds == null || roleIds.isEmpty()) throw new BusinessRuleException("Assign at least one role.");
        var names = new java.util.HashSet<String>();
        for (Long id : roleIds) {
            Role role = roleDAO.findById(conn, id);
            if (role == null) throw new BusinessRuleException("Unknown role.");
            names.add(role.getName());
        }
        String type = user.getUserType();
        if (!java.util.Set.of("STAFF", "STUDENT", "PARENT").contains(type == null ? "" : type))
            throw new BusinessRuleException("Select a valid user type.");
        if ("STUDENT".equals(type)) {
            if (!names.equals(java.util.Set.of("STUDENT")) || user.getStudentId() == null)
                throw new BusinessRuleException("Student accounts require the STUDENT role and a linked student.");
            user.setTeacherId(null); user.setParentId(null);
        } else if ("PARENT".equals(type)) {
            if (!names.equals(java.util.Set.of("PARENT")) || user.getParentId() == null)
                throw new BusinessRuleException("Parent accounts require the PARENT role and a linked guardian.");
            user.setStudentId(null); user.setTeacherId(null);
        } else {
            if (names.contains("STUDENT") || names.contains("PARENT"))
                throw new BusinessRuleException("Student and parent roles require their matching user type.");
            if (names.contains("TEACHER") && user.getTeacherId() == null)
                throw new BusinessRuleException("Teacher accounts must link to a teacher profile.");
            user.setStudentId(null); user.setParentId(null);
        }
        if (user.getEmail() != null && user.getEmail().isBlank()) user.setEmail(null);
    }

    private String rolesToString(Connection conn, List<Long> roleIds) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (Long rid : roleIds) {
            Role r = roleDAO.findById(conn, rid);
            if (r != null) {
                if (sb.length() > 0) sb.append(",");
                sb.append(r.getName());
            }
        }
        return sb.toString();
    }

    // --------------------------------------------------------------- queries

    public List<Role> allRoles() {
        try (Connection conn = DB.getConnection()) {
            return roleDAO.findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load roles.", e);
        }
    }

    public List<User> searchUsers(String q, String role, String status, int offset, int limit) {
        try (Connection conn = DB.getConnection()) {
            return userDAO.search(conn, q, role, status, offset, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Could not search users.", e);
        }
    }

    public long countUsers(String q, String role, String status) {
        try (Connection conn = DB.getConnection()) {
            return userDAO.countSearch(conn, q, role, status);
        } catch (SQLException e) {
            throw new RuntimeException("Could not count users.", e);
        }
    }

    public User findUser(long id) {
        try (Connection conn = DB.getConnection()) {
            return userDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load user.", e);
        }
    }

    public Map<String, Long> userStats() {
        try (Connection conn = DB.getConnection()) {
            Map<String, Long> stats = new HashMap<>();
            stats.put("total", userDAO.countAll(conn));
            stats.put("active", userDAO.countActive(conn));
            return stats;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load user stats.", e);
        }
    }

    /** Central backend authorisation gate - never trust JSP-only checks. */
    public void requirePermission(User actor, String permission) {
        if (actor == null) {
            throw new AuthorizationException("Not authenticated.");
        }
        boolean admin = actor.hasRole("ADMIN") || actor.hasRole("PRINCIPAL");
        if (!admin) {
            throw new AuthorizationException("You do not have permission to perform this action.");
        }
    }
}
