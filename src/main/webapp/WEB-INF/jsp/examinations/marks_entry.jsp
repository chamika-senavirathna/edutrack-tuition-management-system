<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Enter Marks · ${exam.examName}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <form method="post" action="${pageContext.request.contextPath}/marks/enter">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="examId" value="${exam.id}">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span>Marks Sheet · ${exam.examName} · max ${exam.maxMarks}</span>
                        <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/examinations/view?id=${exam.id}">Exam Details</a>
                    </div>
                    <table class="table mb-0">
                        <thead><tr><th>Student</th><th>Mark (0–${exam.maxMarks})</th><th>Remarks</th></tr></thead>
                        <tbody>
                        <c:forEach items="${students}" var="s">
                            <c:set var="entry" value="${existing[s.id]}"/>
                            <c:if test="${empty entry || entry.status == 'DRAFT'}">
                                <tr>
                                    <td>${s.fullName()} <small class="text-muted">${s.regNo}</small></td>
                                    <td><input class="form-control" type="number" step="0.5" min="0" max="${exam.maxMarks}"
                                               name="mark_${s.id}" value="${entry.marks}"></td>
                                    <td><input class="form-control" name="remarks_${s.id}" value="${entry.remarks}"></td>
                                </tr>
                            </c:if>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="mt-3 d-flex gap-2">
                    <button class="btn btn-primary">Save Marks (Draft)</button>
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/examinations/view?id=${exam.id}">Back</a>
                </div>
            </form>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
