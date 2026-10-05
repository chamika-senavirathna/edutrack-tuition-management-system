<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Students"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 01 · Student &amp; Teacher Registration / Profile Management</p>
                <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR')}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/students/add">+ Register Student</a>
                </c:if>
            </div>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-4">
                    <label class="form-label small">Search name / reg no / NIC</label>
                    <input class="form-control" type="text" name="q" value="${q}">
                </div>
                <div class="col-md-3">
                    <label class="form-label small">Status</label>
                    <select class="form-select" name="status">
                        <option value="">All</option>
                        <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Active</option>
                        <option value="WITHDRAWN" ${status == 'WITHDRAWN' ? 'selected' : ''}>Withdrawn</option>
                        <option value="SUSPENDED" ${status == 'SUSPENDED' ? 'selected' : ''}>Suspended</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button class="btn btn-outline-primary w-100" type="submit">Search</button>
                </div>
            </form>

            <div class="card">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead><tr><th>Reg No</th><th>Name</th><th>NIC</th><th>Guardian</th><th>Status</th><th></th></tr></thead>
                        <tbody>
                        <c:if test="${empty students}">
                            <tr><td colspan="6"><div class="empty-state"><div class="icon">🎓</div>No students match your search.</div></td></tr>
                        </c:if>
                        <c:forEach items="${students}" var="s">
                            <tr>
                                <td class="fw-semibold">${s.regNo}</td>
                                <td>${s.fullName()}</td>
                                <td>${s.nic}</td>
                                <td>${s.guardianName}</td>
                                <td><span class="badge badge-${s.status}">${s.status}</span></td>
                                <td class="text-end">
                                    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/students/view?id=${s.id}">View</a>
                                    <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/students/edit?id=${s.id}">Edit</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <c:set var="pages" value="${(total + pageSize - 1) / pageSize}"/>
            <c:if test="${pages > 1}">
                <nav class="mt-3">
                    <ul class="pagination pagination-sm">
                        <c:forEach begin="1" end="${pages}" var="p">
                            <li class="page-item ${p == page ? 'active' : ''}">
                                <a class="page-link" href="?q=${q}&status=${status}&page=${p}">${p}</a>
                            </li>
                        </c:forEach>
                    </ul>
                </nav>
            </c:if>
            <p class="text-muted small mt-2">${total} record(s)</p>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
