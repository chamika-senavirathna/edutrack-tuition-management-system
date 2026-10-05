<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Teachers"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 01 · Teacher Registration / Profile Management</p>
                <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR')}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/teachers/add">+ Register Teacher</a>
                </c:if>
            </div>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-5"><label class="form-label small">Search name / staff no / NIC</label>
                    <input class="form-control" name="q" value="${q}"></div>
                <div class="col-md-3"><label class="form-label small">Status</label>
                    <select class="form-select" name="status">
                        <option value="">All</option>
                        <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Active</option>
                        <option value="ON_LEAVE" ${status == 'ON_LEAVE' ? 'selected' : ''}>On leave</option>
                        <option value="RESIGNED" ${status == 'RESIGNED' ? 'selected' : ''}>Resigned</option>
                    </select></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Search</button></div>
            </form>

            <div class="card">
                <table class="table table-hover mb-0">
                    <thead><tr><th>Staff No</th><th>Name</th><th>Specialization</th><th>Phone</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach items="${teachers}" var="t">
                        <tr>
                            <td class="fw-semibold">${t.staffNo}</td>
                            <td>${t.fullName}</td>
                            <td>${t.specialization}</td>
                            <td>${t.phone}</td>
                            <td><span class="badge badge-${t.status}">${t.status}</span></td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/teachers/view?id=${t.id}">View</a>
                                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/teachers/edit?id=${t.id}">Edit</a>
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
