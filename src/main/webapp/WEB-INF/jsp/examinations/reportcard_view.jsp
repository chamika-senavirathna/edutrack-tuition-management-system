<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Report Card"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row justify-content-center">
                <div class="col-lg-7">
                    <div class="card">
                        <div class="card-header text-center fw-bold">EduTrack Sri Lanka · Report Card</div>
                        <div class="card-body">
                            <div class="text-center mb-3">
                                <div class="h4 fw-bold">${card.studentName}</div>
                                <div class="text-muted small">${card.studentRegNo} · ${card.className} · ${card.termName}</div>
                                <span class="badge badge-${card.status}">${card.status}</span>
                            </div>
                            <table class="table text-center">
                                <tr><th>Average</th><td class="fw-bold fs-4">${card.averageMarks}%</td></tr>
                                <tr><th>Overall Grade</th><td><span class="badge bg-primary fs-5">${card.overallGrade}</span></td></tr>
                                <tr><th>Generated</th><td>${card.generatedAt}</td></tr>
                                <tr><th>Published</th><td>${card.publishedAt}</td></tr>
                            </table>
                            <p class="small text-muted text-center mb-0">
                                Grades are auto-calculated from configured grade bands. Corrections to a published
                                card require an authorised re-publish (BR-EXM-03).
                            </p>
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
