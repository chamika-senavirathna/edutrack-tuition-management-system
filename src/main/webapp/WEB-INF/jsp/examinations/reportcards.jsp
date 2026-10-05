<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Report Cards"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 06 · Auto-graded report cards (PBI-22) · published cards change only via re-publish (BR-EXM-03)</p>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/reportcards/generate">+ Generate Report Card</a>
            </div>
            <div class="card">
                <table class="table mb-0 align-middle">
                    <thead><tr><th>Student</th><th>Term</th><th>Average</th><th>Grade</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
                    <tbody>
                    <c:if test="${empty cards}">
                        <tr><td colspan="6"><div class="empty-state"><div class="icon">📄</div>No report cards generated yet.</div></td></tr>
                    </c:if>
                    <c:forEach items="${cards}" var="rc">
                        <tr>
                            <td>${rc.studentName} <small class="text-muted">${rc.studentRegNo}</small></td>
                            <td>${rc.termName}</td>
                            <td>${rc.averageMarks}%</td>
                            <td><span class="badge bg-primary">${rc.overallGrade}</span></td>
                            <td><span class="badge badge-${rc.status}">${rc.status}</span></td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/reportcards/view?id=${rc.id}">View</a>
                                <c:if test="${rc.status == 'GENERATED' && (user.hasRole('PRINCIPAL') || user.hasRole('ADMIN'))}">
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/reportcards/publish"
                                          data-confirm="Publish this report card? Students and parents will see it.">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${rc.id}">
                                        <button class="btn btn-sm btn-success">Publish</button>
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
