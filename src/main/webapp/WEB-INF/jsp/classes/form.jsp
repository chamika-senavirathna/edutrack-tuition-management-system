<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${clazz != null ? 'Edit Class' : 'New Class'}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <form method="post" action="${pageContext.request.contextPath}${clazz != null ? '/classes/edit' : '/classes/add'}">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <c:if test="${clazz != null}"><input type="hidden" name="id" value="${clazz.id}"></c:if>
                <div class="row g-4">
                    <div class="col-lg-8">
                        <div class="card"><div class="card-header">Class Details</div>
                            <div class="card-body row g-3">
                                <div class="col-md-8"><label class="form-label required-label">Class name</label>
                                    <input class="form-control" name="name" value="${clazz.name}" required placeholder="Grade 10 Maths A"></div>
                                <div class="col-md-4"><label class="form-label required-label">Capacity</label>
                                    <input type="number" min="1" class="form-control" name="capacity" value="${clazz.capacity != null ? clazz.capacity : 30}" required></div>
                                <div class="col-md-4"><label class="form-label">Subject</label>
                                    <select class="form-select" name="subjectId">
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${subjects}" var="s"><option value="${s.id}" ${clazz.subjectId == s.id ? 'selected' : ''}>${s.name}</option></c:forEach>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label">Teacher</label>
                                    <select class="form-select" name="teacherId">
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${teachers}" var="t"><option value="${t.id}" ${clazz.teacherId == t.id ? 'selected' : ''}>${t.fullName}</option></c:forEach>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label">Default room</label>
                                    <select class="form-select" name="roomId">
                                        <option value="-1">None</option>
                                        <c:forEach items="${rooms}" var="r"><option value="${r.id}" ${clazz.roomId == r.id ? 'selected' : ''}>${r.name}</option></c:forEach>
                                    </select></div>
                                <div class="col-md-6"><label class="form-label">Start date</label>
                                    <input type="date" class="form-control" name="startDate" value="${clazz.startDate}"></div>
                                <div class="col-md-6"><label class="form-label">End date</label>
                                    <input type="date" class="form-control" name="endDate" value="${clazz.endDate}"></div>
                                <div class="col-12"><label class="form-label">Description</label>
                                    <textarea class="form-control" name="description" rows="2">${clazz.description}</textarea></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card mb-3"><div class="card-header">Rules</div>
                            <div class="card-body small text-muted">
                                Capacity blocks over-enrolment (BR-REG-03). Timetable conflicts on teacher/room/time
                                are blocked before publishing (BR-TTC-01). Archiving keeps history.
                            </div>
                        </div>
                        <button class="btn btn-primary w-100">${clazz != null ? 'Save Changes' : 'Create Class'}</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
