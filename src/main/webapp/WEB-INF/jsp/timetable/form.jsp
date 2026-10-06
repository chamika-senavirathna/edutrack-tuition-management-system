<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${slot != null ? 'Edit Timetable Slot' : 'New Timetable Slot'}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <form method="post" action="${pageContext.request.contextPath}${slot != null ? '/timetable/edit' : '/timetable/add'}">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <c:if test="${slot != null}"><input type="hidden" name="id" value="${slot.id}"></c:if>
                <div class="row g-4">
                    <div class="col-lg-8">
                        <div class="card"><div class="card-header">Weekly Slot</div>
                            <div class="card-body row g-3">
                                <div class="col-md-6"><label class="form-label required-label">Class</label>
                                    <select class="form-select" name="classId" required>
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${classes}" var="c">
                                            <option value="${c.id}" ${slot != null && slot.classId == c.id ? 'selected' : ''}>${c.name}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="col-md-6"><label class="form-label">Subject</label>
                                    <select class="form-select" name="subjectId">
                                        <option value="-1">Use class subject</option>
                                        <c:forEach items="${subjects}" var="s">
                                            <option value="${s.id}" ${slot != null && slot.subjectId == s.id ? 'selected' : ''}>${s.name}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="col-md-6"><label class="form-label">Teacher</label>
                                    <select class="form-select" name="teacherId">
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${teachers}" var="t">
                                            <option value="${t.id}" ${slot != null && slot.teacherId == t.id ? 'selected' : ''}>${t.fullName}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="col-md-6"><label class="form-label">Room</label>
                                    <select class="form-select" name="roomId">
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${rooms}" var="r">
                                            <option value="${r.id}" ${slot != null && slot.roomId == r.id ? 'selected' : ''}>${r.name}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label required-label">Day</label>
                                    <select class="form-select" name="dayOfWeek" required>
                                        <c:forEach var="d" items="${['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY']}">
                                            <option ${slot != null && slot.dayOfWeek == d ? 'selected' : ''}>${d}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label required-label">Start</label>
                                    <input type="time" class="form-control" name="startTime" value="${slot.startTime}" required></div>
                                <div class="col-md-4"><label class="form-label required-label">End</label>
                                    <input type="time" class="form-control" name="endTime" value="${slot.endTime}" required></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card mb-3"><div class="card-header">Workflow</div>
                            <div class="card-body small text-muted">
                                New slots are saved as DRAFT. The coordinator submits; the Principal approves and publishes.
                                Publishing notifies affected students and teachers. Overlapping teacher/room/class usage in
                                the same period is rejected (BR-TTC-01).
                            </div>
                        </div>
                        <button class="btn btn-primary w-100">${slot != null ? 'Save Changes' : 'Create Draft Slot'}</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
