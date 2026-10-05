<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Examinations"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 06 · Examination &amp; Reporting Management · marks restricted to the assigned teacher (BR-EXM-01)</p>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/examinations/add">+ Schedule Exam</a>
            </div>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-3"><label class="form-label small">Exam name</label><input class="form-control" name="q" value="${q}"></div>
                <div class="col-md-3"><label class="form-label small">Class</label>
                    <select class="form-select" name="classId"><option value="-1">All</option>
                        <c:forEach items="${classes}" var="c"><option value="${c.id}" ${classId == c.id ? 'selected' : ''}>${c.name}</option></c:forEach>
                    </select></div>
                <div class="col-md-2"><label class="form-label small">Status</label>
                    <select class="form-select" name="status"><option value="">All</option>
                        <option ${status == 'SCHEDULED' ? 'selected' : ''}>SCHEDULED</option>
                        <option ${status == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                        <option ${status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                        <option ${status == 'ARCHIVED' ? 'selected' : ''}>ARCHIVED</option>
                    </select></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Filter</button></div>
            </form>

            <div class="card">
                <table class="table table-hover mb-0 align-middle">
                    <thead><tr><th>Exam</th><th>Class / Subject</th><th>Date</th><th>Max</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
                    <tbody>
                    <c:forEach items="${exams}" var="x">
                        <tr>
                            <td class="fw-semibold">${x.examName}<br><small class="text-muted">${x.termName}</small></td>
                            <td>${x.className} · ${x.subjectName}</td>
                            <td>${x.examDate}</td>
                            <td>${x.maxMarks}</td>
                            <td><span class="badge badge-${x.status}">${x.status}</span></td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/examinations/view?id=${x.id}">Details</a>
                                <a class="btn btn-sm btn-outline-success" href="${pageContext.request.contextPath}/marks?id=${x.id}">Marks</a>
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
