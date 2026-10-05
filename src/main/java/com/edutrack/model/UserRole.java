package com.edutrack.model;

import java.io.Serializable;

/** Join of user-role with the loaded Role object for convenient JSP rendering. */
public class UserRole implements Serializable {
    private Long userId;
    private Long roleId;
    private Role role;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
