<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Exam · ${exam.examName}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="card mb-4">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>${exam.examName}</span>
                    <span class="badge badge-${exam.status}">${exam.status}</span>
                </div>
                <div class="card-body row g-3">
                    <div class="col-md-3"><div class="small text-muted">Class</div>${exam.className}</div>
                    <div class="col-md-3"><div class="small text-muted">Subject</div>${exam.subjectName}</div>
                    <div class="col-md-3"><div class="small text-muted">Teacher</div>${exam.teacherName}</div>
                    <div class="col-md-3"><div class="small text-muted">Date</div>${exam.examDate} ${exam.startTime}–${exam.endTime}</div>
                </div>
            </div>

            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>Marks · Draft → Submitted → Moderated → Published (BR-EXM-02)</span>
                    <div>
                        <c:if test="${user.hasRole('ADMIN') || (user.hasRole('TEACHER') && user.teacherId == exam.teacherId)}">
                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/marks/enter?id=${exam.id}">Enter Marks</a>
                        <form class="d-inline" method="post" action="${pageContext.request.contextPath}/marks/submit">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="examId" value="${exam.id}">
                            <button class="btn btn-sm btn-outline-primary">Submit for Moderation</button>
                        </form>
                        </c:if>
                        <c:if test="${user.hasRole('PRINCIPAL') || user.hasRole('ADMIN')}">
                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/marks/publish"
                                  data-confirm="Publish all moderated marks for this exam?">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <input type="hidden" name="examId" value="${exam.id}">
                                <button class="btn btn-sm btn-success">Publish Moderated</button>
                            </form>
                        </c:if>
                    </div>
                </div>
                <table class="table mb-0 align-middle">
                    <thead><tr><th>Student</th><th>Marks</th><th>Grade</th><th>Status</th><th>Moderation</th><th></th></tr></thead>
                    <tbody>
                    <c:if test="${empty marks}">
                        <tr><td colspan="6"><div class="empty-state"><div class="icon">📝</div>No marks entered yet.</div></td></tr>
                    </c:if>
                    <c:forEach items="${marks}" var="m">
                        <tr>
                            <td>${m.studentName} <small class="text-muted">${m.studentRegNo}</small></td>
                            <td>${m.marks} / ${m.maxMarks}</td>
                            <td><span class="badge bg-primary">${m.grade}</span></td>
                            <td><span class="badge badge-${m.status}">${m.status}</span></td>
                            <td class="small">${m.moderationNote} ${m.moderatedByName}</td>
                            <td class="text-end">
                                <c:if test="${m.status == 'SUBMITTED' && (user.hasRole('PRINCIPAL') || user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR'))}">
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/marks/moderate">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="marksId" value="${m.id}">
                                        <input type="hidden" name="examId" value="${exam.id}">
                                        <input type="hidden" name="decision" value="approve">
                                        <input class="form-control form-control-sm d-inline w-auto" name="note" placeholder="Moderation note" required>
                                        <button class="btn btn-sm btn-outline-success">Approve</button>
                                    </form>
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/marks/moderate">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="marksId" value="${m.id}">
                                        <input type="hidden" name="examId" value="${exam.id}">
                                        <input type="hidden" name="decision" value="reject">
                                        <input class="form-control form-control-sm d-inline w-auto" name="note" placeholder="Rejection reason" required>
                                        <button class="btn btn-sm btn-outline-warning">Return</button>
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
