<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Results"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <c:if test="${user.hasRole('ADMIN') || user.hasRole('PRINCIPAL') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('TEACHER') || user.hasRole('FINANCE')}">
                <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                    <div class="col-md-5"><label class="form-label small">Student</label>
                        <select class="form-select" name="studentId">
                            <option value="-1">Select…</option>
                            <c:forEach items="${students}" var="s">
                                <option value="${s.id}" ${studentId == s.id ? 'selected' : ''}>${s.regNo} — ${s.fullName()}</option>
                            </c:forEach>
                        </select></div>
                    <div class="col-md-2"><button class="btn btn-outline-primary w-100">View Results</button></div>
                </form>
            </c:if>

            <c:choose>
                <c:when test="${empty marksList}">
                    <div class="card"><div class="empty-state"><div class="icon">🎓</div>
                        No results available. Students and parents see results only after publication.</div></div>
                </c:when>
                <c:otherwise>
                    <div class="card">
                        <div class="card-header">Published &amp; Moderated Results</div>
                        <table class="table mb-0">
                            <thead><tr><th>Exam</th><th>Subject</th><th>Marks</th><th>Grade</th><th>Status</th></tr></thead>
                            <tbody>
                            <c:forEach items="${marksList}" var="m">
                                <c:if test="${m.status == 'PUBLISHED' || m.status == 'MODERATED'}">
                                    <tr>
                                        <td>${m.examName}</td>
                                        <td>${m.subjectName}</td>
                                        <td>${m.marks} / ${m.maxMarks}</td>
                                        <td><span class="badge bg-primary">${m.grade}</span></td>
                                        <td><span class="badge badge-${m.status}">${m.status}</span></td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
