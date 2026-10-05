<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Users"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 05 · User &amp; Access Management · least privilege (BR-UAM-01), audit old/new (BR-UAM-02)</p>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/users/add">+ New User</a>
            </div>

            <div class="row g-3 mb-3">
                <div class="col-md-4"><div class="card stat-card p-3"><div class="stat-value">${stats.total}</div><div class="stat-label">Total accounts</div></div></div>
                <div class="col-md-4"><div class="card stat-card green p-3"><div class="stat-value">${stats.active}</div><div class="stat-label">Active accounts</div></div></div>
                <div class="col-md-4"><div class="card stat-card red p-3"><div class="stat-value">${stats.total - stats.active}</div><div class="stat-label">Deactivated (not deleted)</div></div></div>
            </div>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-4"><label class="form-label small">Search username / name / email</label>
                    <input class="form-control" name="q" value="${q}"></div>
                <div class="col-md-3"><label class="form-label small">Role</label>
                    <select class="form-select" name="role"><option value="">All</option>
                        <c:forEach items="${roles}" var="r"><option ${role == r.name ? 'selected' : ''}>${r.name}</option></c:forEach>
                    </select></div>
                <div class="col-md-2"><label class="form-label small">Status</label>
                    <select class="form-select" name="status"><option value="">All</option>
                        <option ${status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                        <option ${status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                    </select></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Filter</button></div>
            </form>

            <div class="card">
                <table class="table table-hover mb-0 align-middle">
                    <thead><tr><th>Username</th><th>Name</th><th>Roles</th><th>Type</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
                    <tbody>
                    <c:forEach items="${users}" var="u">
                        <tr>
                            <td class="fw-semibold">${u.username}</td>
                            <td>${u.fullName}<br><small class="text-muted">${u.email}</small></td>
                            <td>
                                <c:forEach items="${u.roles}" var="ur">
                                    <span class="badge bg-secondary">${ur.role.name}</span>
                                </c:forEach>
                            </td>
                            <td>${u.userType}</td>
                            <td><span class="badge ${u.active ? 'badge-ACTIVE' : 'badge-INACTIVE'}">${u.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/users/edit?id=${u.id}">Edit</a>
                                <c:if test="${u.id != user.id}">
                                    <c:choose>
                                        <c:when test="${u.active}">
                                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/users/status"
                                                  data-confirm="Deactivate this account? BR-UAM-03: accounts are never hard-deleted.">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="id" value="${u.id}">
                                                <input type="hidden" name="action" value="deactivate">
                                                <button class="btn btn-sm btn-outline-danger">Deactivate</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/users/status">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="id" value="${u.id}">
                                                <input type="hidden" name="action" value="activate">
                                                <button class="btn btn-sm btn-outline-success">Activate</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
