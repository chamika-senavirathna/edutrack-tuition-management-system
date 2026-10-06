<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Mark Register"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <c:choose>
                <c:when test="${clazz == null}">
                    <div class="card"><div class="empty-state"><div class="icon">🏫</div>
                        Select a class: <a href="${pageContext.request.contextPath}/attendance">back to register</a>.
                    </div></div>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${pageContext.request.contextPath}/attendance/mark"
                          data-confirm="Save the register? Duplicate entries for the same session are blocked.">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="classId" value="${clazz.id}">
                        <div class="card mb-3">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <span>Register · ${clazz.name}</span>
                                <input type="date" class="form-control w-auto" name="date" value="${date}">
                            </div>
                            <table class="table mb-0">
                                <thead><tr><th>Student</th><th>Status</th><th>Reason (absent / late / excused)</th></tr></thead>
                                <tbody>
                                <c:forEach items="${enrolments}" var="e">
                                    <c:set var="entry" value="${existing[e.studentId]}"/>
                                    <tr>
                                        <td><strong>${e.studentName}</strong><br><small class="text-muted">${e.studentRegNo}</small></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty entry}">
                                                    <select class="form-select" name="status_${e.studentId}">
                                                        <option value="PRESENT">Present</option>
                                                        <option value="ABSENT">Absent</option>
                                                        <option value="LATE">Late</option>
                                                        <option value="EXCUSED">Excused</option>
                                                    </select>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-${entry.status}">${entry.status}</span>
                                                    <div class="small text-muted">Already recorded (BR-ATT-01)</div>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <input class="form-control" name="reason_${e.studentId}" value="${entry.reason}">
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <button class="btn btn-primary">Save Register</button>
                        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/attendance">Cancel</a>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
