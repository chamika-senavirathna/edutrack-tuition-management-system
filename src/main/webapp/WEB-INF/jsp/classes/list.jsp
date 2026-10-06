<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Classes"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 02 · Class, Timetable &amp; Communication Management</p>
                <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR')}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/classes/add">+ New Class</a>
                </c:if>
            </div>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-3"><label class="form-label small">Class name</label><input class="form-control" name="q" value="${q}"></div>
                <div class="col-md-3"><label class="form-label small">Subject</label>
                    <select class="form-select" name="subjectId"><option value="-1">All</option>
                        <c:forEach items="${subjects}" var="s"><option value="${s.id}" ${subjectId == s.id ? 'selected' : ''}>${s.name}</option></c:forEach>
                    </select></div>
                <div class="col-md-2"><label class="form-label small">Status</label>
                    <select class="form-select" name="status">
                        <option value="">All</option>
                        <option value="DRAFT" ${status == 'DRAFT' ? 'selected' : ''}>Draft</option>
                        <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Active</option>
                        <option value="ARCHIVED" ${status == 'ARCHIVED' ? 'selected' : ''}>Archived</option>
                    </select></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Filter</button></div>
            </form>

            <div class="card">
                <table class="table table-hover mb-0">
                    <thead><tr><th>Class</th><th>Subject</th><th>Teacher</th><th>Room</th><th>Capacity</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach items="${classes}" var="c">
                        <tr>
                            <td class="fw-semibold">${c.name}</td>
                            <td>${c.subjectName}</td>
                            <td>${c.teacherName}</td>
                            <td>${c.roomName}</td>
                            <td>${c.enrolledCount}/${c.capacity}</td>
                            <td><span class="badge badge-${c.status}">${c.status}</span></td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/classes/view?id=${c.id}">View</a>
                                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/classes/edit?id=${c.id}">Edit</a>
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
