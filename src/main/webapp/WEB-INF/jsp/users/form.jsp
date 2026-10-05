<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${editUser != null ? 'Edit User' : 'New User'}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row g-4">
                <div class="col-lg-7">
                    <form method="post" action="${pageContext.request.contextPath}${editUser != null ? '/users/edit' : '/users/add'}">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <c:if test="${editUser != null}"><input type="hidden" name="id" value="${editUser.id}"></c:if>
                        <div class="card mb-4"><div class="card-header">Account</div>
                            <div class="card-body row g-3">
                                <c:if test="${editUser == null}">
                                    <div class="col-md-6"><label class="form-label required-label">Username</label>
                                        <input class="form-control" name="username" required></div>
                                    <div class="col-md-6"><label class="form-label required-label">Password</label>
                                        <input type="password" class="form-control" name="password" required minlength="8">
                                        <div class="form-text">Min 8 chars with letters and numbers. Stored hashed, never plaintext.</div></div>
                                </c:if>
                                <div class="col-md-6"><label class="form-label required-label">Full name</label>
                                    <input class="form-control" name="fullName" value="${editUser.fullName}" required></div>
                                <div class="col-md-6"><label class="form-label">Email</label>
                                    <input type="email" class="form-control" name="email" value="${editUser.email}"></div>
                                <div class="col-md-6"><label class="form-label">Phone</label>
                                    <input class="form-control" name="phone" value="${editUser.phone}"></div>
                                <div class="col-md-6"><label class="form-label">User type</label>
                                    <select class="form-select" name="userType">
                                        <option value="STAFF" ${editUser.userType == 'STAFF' ? 'selected' : ''}>Staff</option>
                                        <option value="STUDENT" ${editUser.userType == 'STUDENT' ? 'selected' : ''}>Student</option>
                                        <option value="PARENT" ${editUser.userType == 'PARENT' ? 'selected' : ''}>Parent</option>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label">Link student ID</label>
                                    <input type="number" class="form-control" name="studentId" value="${editUser.studentId}"></div>
                                <div class="col-md-4"><label class="form-label">Link teacher ID</label>
                                    <input type="number" class="form-control" name="teacherId" value="${editUser.teacherId}"></div>
                                <div class="col-md-4"><label class="form-label">Link guardian ID</label>
                                    <input type="number" class="form-control" name="parentId" value="${editUser.parentId}"></div>
                            </div>
                        </div>

                        <div class="card mb-4"><div class="card-header">Role Assignment (RBAC, least privilege BR-UAM-01)</div>
                            <div class="card-body">
                                <div class="row">
                                    <c:forEach items="${roles}" var="r">
                                        <c:set var="checked" value="false"/>
                                        <c:forEach items="${editUser.roles}" var="ur">
                                            <c:if test="${ur.roleId == r.id}"><c:set var="checked" value="true"/></c:if>
                                        </c:forEach>
                                        <div class="col-md-6">
                                            <div class="form-check">
                                                <input class="form-check-input" type="checkbox" name="roleIds" value="${r.id}" id="role_${r.id}" ${checked ? 'checked' : ''}>
                                                <label class="form-check-label small" for="role_${r.id}">
                                                    <strong>${r.name}</strong> — ${r.description}
                                                </label>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>

                        <button class="btn btn-primary">${editUser != null ? 'Save Changes' : 'Create Account'}</button>
                    </form>
                </div>
                <div class="col-lg-5">
                    <div class="card"><div class="card-header">Role Reference</div>
                        <div class="card-body small">
                            <ul class="mb-0">
                                <li><strong>ADMIN</strong> — full configuration, users, audit (Member 05)</li>
                                <li><strong>PRINCIPAL</strong> — approves timetables, moderates/publishes results</li>
                                <li><strong>ACADEMIC_COORDINATOR</strong> — classes, timetable drafting, enrolment (Member 02)</li>
                                <li><strong>TEACHER</strong> — attendance (Member 03), assigned marks (Member 06), materials</li>
                                <li><strong>FINANCE</strong> — invoices, payments, receipts, refunds (Member 04)</li>
                                <li><strong>STUDENT</strong> — own records only</li>
                                <li><strong>PARENT</strong> — linked children's records only</li>
                            </ul>
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
