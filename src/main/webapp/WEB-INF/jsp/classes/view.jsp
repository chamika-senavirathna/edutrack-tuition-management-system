<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Class · ${clazz.name}"/>
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
                    <div class="card mb-4">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <span>${clazz.name}</span>
                            <span class="badge badge-${clazz.status}">${clazz.status}</span>
                        </div>
                        <div class="card-body row g-3">
                            <div class="col-md-6"><div class="small text-muted">Subject</div>${clazz.subjectName}</div>
                            <div class="col-md-6"><div class="small text-muted">Teacher</div>${clazz.teacherName}</div>
                            <div class="col-md-6"><div class="small text-muted">Room</div>${clazz.roomName}</div>
                            <div class="col-md-6"><div class="small text-muted">Enrolment</div>${clazz.enrolledCount}/${clazz.capacity}</div>
                            <div class="col-12"><div class="small text-muted">Description</div>${clazz.description}</div>
                        </div>
                    </div>
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <span>Timetable Slots</span>
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/timetable">Manage</a>
                        </div>
                        <table class="table mb-0">
                            <thead><tr><th>Day</th><th>Time</th><th>Teacher</th><th>Room</th><th>Status</th></tr></thead>
                            <tbody>
                            <c:forEach items="${slots}" var="slot">
                                <tr>
                                    <td>${slot.dayOfWeek}</td><td>${slot.startTime}–${slot.endTime}</td>
                                    <td>${slot.teacherName}</td><td>${slot.roomName}</td>
                                    <td><span class="badge badge-${slot.status}">${slot.status}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="col-lg-5">
                    <div class="card">
                        <div class="card-header">Actions</div>
                        <div class="card-body">
                            <a class="btn btn-outline-primary w-100 mb-2" href="${pageContext.request.contextPath}/classes/edit?id=${clazz.id}">Edit Class</a>
                            <a class="btn btn-outline-secondary w-100 mb-2" href="${pageContext.request.contextPath}/attendance?classId=${clazz.id}">Attendance Register (M03)</a>
                            <a class="btn btn-outline-secondary w-100 mb-2" href="${pageContext.request.contextPath}/fees?classId=${clazz.id}">Class Fees (M04)</a>
                            <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR')}">
                                <c:choose>
                                    <c:when test="${clazz.status == 'ACTIVE'}">
                                        <form method="post" action="${pageContext.request.contextPath}/classes/archive"
                                              data-confirm="Archive this class? History is retained.">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="id" value="${clazz.id}">
                                            <input type="hidden" name="action" value="archive">
                                            <button class="btn btn-outline-danger w-100">Archive Class</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form method="post" action="${pageContext.request.contextPath}/classes/archive">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="id" value="${clazz.id}">
                                            <input type="hidden" name="action" value="activate">
                                            <button class="btn btn-outline-success w-100">Reactivate Class</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
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
