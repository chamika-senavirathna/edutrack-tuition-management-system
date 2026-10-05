<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${exam != null ? 'Edit Exam' : 'Schedule Exam'}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row">
                <div class="col-lg-8">
                    <form method="post" action="${pageContext.request.contextPath}${exam != null ? '/examinations/edit' : '/examinations/add'}">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <c:if test="${exam != null}"><input type="hidden" name="id" value="${exam.id}"></c:if>
                        <div class="card"><div class="card-header">Examination</div>
                            <div class="card-body row g-3">
                                <div class="col-md-8"><label class="form-label required-label">Exam name</label>
                                    <input class="form-control" name="examName" value="${exam.examName}" required placeholder="Term 1 Maths Test"></div>
                                <div class="col-md-4"><label class="form-label">Term</label>
                                    <select class="form-select" name="termId">
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${terms}" var="t">
                                            <option value="${t.id}" ${exam != null && exam.termId == t.id ? 'selected' : ''}>${t.name}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label required-label">Class</label>
                                    <select class="form-select" name="classId" required>
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${classes}" var="c">
                                            <option value="${c.id}" ${exam != null && exam.classId == c.id ? 'selected' : ''}>${c.name}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label required-label">Subject</label>
                                    <select class="form-select" name="subjectId" required>
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${subjects}" var="s">
                                            <option value="${s.id}" ${exam != null && exam.subjectId == s.id ? 'selected' : ''}>${s.name}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label">Assigned teacher</label>
                                    <select class="form-select" name="teacherId">
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${teachers}" var="t">
                                            <option value="${t.id}" ${exam != null && exam.teacherId == t.id ? 'selected' : ''}>${t.fullName}</option>
                                        </c:forEach>
                                    </select>
                                    <div class="form-text">Marks entry is restricted to this teacher (BR-EXM-01).</div></div>
                                <div class="col-md-4"><label class="form-label required-label">Exam date</label>
                                    <input type="date" class="form-control" name="examDate" value="${exam.examDate}" required></div>
                                <div class="col-md-4"><label class="form-label">Start</label>
                                    <input type="time" class="form-control" name="startTime" value="${exam.startTime}"></div>
                                <div class="col-md-4"><label class="form-label">End</label>
                                    <input type="time" class="form-control" name="endTime" value="${exam.endTime}"></div>
                                <div class="col-md-4"><label class="form-label">Room</label>
                                    <input class="form-control" name="roomText" value="${exam.roomText}"></div>
                                <div class="col-md-4"><label class="form-label">Max marks</label>
                                    <input type="number" class="form-control" name="maxMarks" min="1" value="${exam != null ? exam.maxMarks : 100}"></div>
                            </div>
                        </div>
                        <button class="btn btn-primary mt-3">${exam != null ? 'Save Changes' : 'Schedule Exam'}</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
