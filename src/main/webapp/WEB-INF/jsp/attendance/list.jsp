<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Attendance"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 03 · Attendance Management · one entry per student per session (BR-ATT-01)</p>
                <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('TEACHER')}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/attendance/mark?classId=${classId != null ? classId : (classes != null && not empty classes ? classes[0].id : '')}">+ Mark Register</a>
                </c:if>
            </div>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-3"><label class="form-label small">Class</label>
                    <select class="form-select" name="classId"><option value="-1">All</option>
                        <c:forEach items="${classes}" var="c"><option value="${c.id}" ${classId == c.id ? 'selected' : ''}>${c.name}</option></c:forEach>
                    </select></div>
                <div class="col-md-2"><label class="form-label small">Student ID</label>
                    <input class="form-control" name="studentId" value="${studentId}"></div>
                <div class="col-md-2"><label class="form-label small">From</label>
                    <input type="date" class="form-control" name="from" value="${from}"></div>
                <div class="col-md-2"><label class="form-label small">To</label>
                    <input type="date" class="form-control" name="to" value="${to}"></div>
                <div class="col-md-2"><label class="form-label small">Status</label>
                    <select class="form-select" name="status"><option value="">All</option>
                        <option ${status == 'PRESENT' ? 'selected' : ''}>PRESENT</option>
                        <option ${status == 'ABSENT' ? 'selected' : ''}>ABSENT</option>
                        <option ${status == 'LATE' ? 'selected' : ''}>LATE</option>
                        <option ${status == 'EXCUSED' ? 'selected' : ''}>EXCUSED</option>
                    </select></div>
                <div class="col-md-1"><button class="btn btn-outline-primary w-100">Go</button></div>
            </form>

            <c:if test="${not empty summary}">
                <div class="row g-3 mb-3">
                    <div class="col-6 col-md-3"><div class="card stat-card green p-3"><div class="stat-value">${summary.present}</div><div class="stat-label">Present (this month)</div></div></div>
                    <div class="col-6 col-md-3"><div class="card stat-card red p-3"><div class="stat-value">${summary.absent}</div><div class="stat-label">Absent</div></div></div>
                    <div class="col-6 col-md-3"><div class="card stat-card amber p-3"><div class="stat-value">${summary.late}</div><div class="stat-label">Late</div></div></div>
                    <div class="col-6 col-md-3"><div class="card stat-card p-3"><div class="stat-value">${summary.excused}</div><div class="stat-label">Excused</div></div></div>
                </div>
            </c:if>

            <div class="card">
                <table class="table table-hover mb-0">
                    <thead><tr><th>Date</th><th>Student</th><th>Class</th><th>Status</th><th>Reason</th><th>Marked by</th><th></th></tr></thead>
                    <tbody>
                    <c:if test="${empty records}">
                        <tr><td colspan="7"><div class="empty-state"><div class="icon">🗓️</div>No attendance records found.</div></td></tr>
                    </c:if>
                    <c:forEach items="${records}" var="a">
                        <tr>
                            <td>${a.attendanceDate}</td>
                            <td>${a.studentName} <small class="text-muted">${a.studentRegNo}</small></td>
                            <td>${a.className}</td>
                            <td><span class="badge badge-${a.status}">${a.status}</span>
                                <c:if test="${a.adminOverride}"><span class="badge bg-dark" title="Admin override audited">OVR</span></c:if>
                            </td>
                            <td class="wrap-cell">${a.reason}</td>
                            <td>${a.markedByName}</td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/attendance/correct?id=${a.id}">Correct</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <p class="text-muted small mt-2">${total} record(s)</p>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
