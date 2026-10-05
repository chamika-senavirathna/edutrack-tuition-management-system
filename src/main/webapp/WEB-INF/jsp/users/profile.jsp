<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="My Profile"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row g-4">
                <div class="col-lg-6">
                    <div class="card"><div class="card-header">Account</div>
                        <div class="card-body row g-3">
                            <div class="col-md-6"><div class="small text-muted">Username</div>${profileUser.username}</div>
                            <div class="col-md-6"><div class="small text-muted">Full name</div>${profileUser.fullName}</div>
                            <div class="col-md-6"><div class="small text-muted">Email</div>${profileUser.email}</div>
                            <div class="col-md-6"><div class="small text-muted">Phone</div>${profileUser.phone}</div>
                            <div class="col-md-6"><div class="small text-muted">Roles</div>
                                <c:forEach items="${profileUser.roles}" var="ur"><span class="badge bg-secondary me-1">${ur.role.name}</span></c:forEach>
                            </div>
                            <div class="col-md-6"><div class="small text-muted">Last login</div>${profileUser.lastLoginAt}</div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card"><div class="card-header">Change Password</div>
                        <div class="card-body">
                            <form method="post" action="${pageContext.request.contextPath}/profile/password">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <div class="mb-3"><label class="form-label required-label">Current password</label>
                                    <input type="password" class="form-control" name="currentPassword" required></div>
                                <div class="mb-3"><label class="form-label required-label">New password</label>
                                    <input type="password" class="form-control" name="newPassword" required minlength="8">
                                    <div class="form-text">At least 8 characters with letters and numbers.</div></div>
                                <button class="btn btn-primary w-100">Change Password</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
