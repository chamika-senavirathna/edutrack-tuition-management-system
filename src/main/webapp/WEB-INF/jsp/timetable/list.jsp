<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Timetable"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 02 · Draft → Submit → Approve → Publish (BR-TTC-02) · Conflicts blocked (BR-TTC-01)</p>
                <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR')}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/timetable/add">+ New Slot</a>
                </c:if>
            </div>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-3"><label class="form-label small">Class</label>
                    <select class="form-select" name="classId"><option value="-1">All</option>
                        <c:forEach items="${classes}" var="c"><option value="${c.id}" ${classId == c.id ? 'selected' : ''}>${c.name}</option></c:forEach>
                    </select></div>
                <div class="col-md-3"><label class="form-label small">Teacher</label>
                    <select class="form-select" name="teacherId"><option value="-1">All</option>
                        <c:forEach items="${teachers}" var="t"><option value="${t.id}" ${teacherId == t.id ? 'selected' : ''}>${t.fullName}</option></c:forEach>
                    </select></div>
                <div class="col-md-2"><label class="form-label small">Day</label>
                    <select class="form-select" name="day"><option value="">All</option>
                        <option>MONDAY</option><option>TUESDAY</option><option>WEDNESDAY</option>
                        <option>THURSDAY</option><option>FRIDAY</option><option>SATURDAY</option><option>SUNDAY</option>
                    </select></div>
                <div class="col-md-2"><label class="form-label small">Status</label>
                    <select class="form-select" name="status"><option value="">All</option>
                        <option ${status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                        <option ${status == 'SUBMITTED' ? 'selected' : ''}>SUBMITTED</option>
                        <option ${status == 'APPROVED' ? 'selected' : ''}>APPROVED</option>
                        <option ${status == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED</option>
                    </select></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Filter</button></div>
            </form>

            <div class="card">
                <table class="table table-hover mb-0 align-middle">
                    <thead><tr><th>Day / Time</th><th>Class</th><th>Subject</th><th>Teacher</th><th>Room</th><th>Status</th><th class="text-end">Workflow</th></tr></thead>
                    <tbody>
                    <c:forEach items="${slots}" var="slot">
                        <tr>
                            <td class="fw-semibold">${slot.dayOfWeek}<br><small class="text-muted">${slot.startTime}–${slot.endTime}</small></td>
                            <td>${slot.className}</td>
                            <td>${slot.subjectName}</td>
                            <td>${slot.teacherName}</td>
                            <td>${slot.roomName}</td>
                            <td><span class="badge badge-${slot.status}">${slot.status}</span></td>
                            <td class="text-end">
                                <c:if test="${slot.status == 'DRAFT' && (user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR'))}">
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/timetable/transition">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${slot.id}"><input type="hidden" name="action" value="submit">
                                        <button class="btn btn-sm btn-outline-primary">Submit</button>
                                    </form>
                                    <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/timetable/edit?id=${slot.id}">Edit</a>
                                </c:if>
                                <c:if test="${slot.status == 'SUBMITTED' && (user.hasRole('PRINCIPAL') || user.hasRole('ADMIN'))}">
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/timetable/transition">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${slot.id}"><input type="hidden" name="action" value="approve">
                                        <button class="btn btn-sm btn-outline-success">Approve</button>
                                    </form>
                                </c:if>
                                <c:if test="${slot.status == 'APPROVED' && (user.hasRole('PRINCIPAL') || user.hasRole('ADMIN'))}">
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/timetable/transition"
                                          data-confirm="Publish this slot? Affected students and teachers will be notified.">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${slot.id}"><input type="hidden" name="action" value="publish">
                                        <button class="btn btn-sm btn-success">Publish</button>
                                    </form>
                                </c:if>
                                <c:if test="${slot.status != 'PUBLISHED' && slot.status != 'ARCHIVED' && (user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR'))}">
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/timetable/transition"
                                          data-confirm="Archive this slot?">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${slot.id}"><input type="hidden" name="action" value="archive">
                                        <button class="btn btn-sm btn-outline-danger">Archive</button>
                                    </form>
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
