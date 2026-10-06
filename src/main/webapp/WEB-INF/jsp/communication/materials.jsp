<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Teaching Materials"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 02 · Teaching Materials (linked to class/subject)</p>
                <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('PRINCIPAL') || user.hasRole('TEACHER')}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/materials/add">+ Share Material</a>
                </c:if>
            </div>
            <div class="card">
                <table class="table mb-0 align-middle">
                    <thead><tr><th>Title</th><th>Class</th><th>Subject</th><th>Shared by</th><th>Link</th><th>Status</th></tr></thead>
                    <tbody>
                    <c:if test="${empty materials}">
                        <tr><td colspan="6"><div class="empty-state"><div class="icon">📚</div>No materials shared yet.</div></td></tr>
                    </c:if>
                    <c:forEach items="${materials}" var="m">
                        <tr>
                            <td class="fw-semibold">${m.title}</td>
                            <td>${m.className}</td>
                            <td>${m.subjectName}</td>
                            <td>${m.uploadedByName}</td>
                            <td><a class="text-break" href="${m.filePath}" target="_blank" rel="noopener">${m.fileName}</a></td>
                            <td><span class="badge badge-${m.status}">${m.status}</span>
                                <c:if test="${m.status == 'ACTIVE' && (user.hasRole('ADMIN') || user.hasRole('PRINCIPAL') || user.hasRole('ACADEMIC_COORDINATOR') || (user.hasRole('TEACHER') && user.id == m.uploadedBy))}">
                                    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/materials/edit?id=${m.id}">Edit</a>
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/materials/archive" data-confirm="Archive this material?">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${m.id}">
                                        <button class="btn btn-sm btn-outline-secondary">Archive</button>
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
