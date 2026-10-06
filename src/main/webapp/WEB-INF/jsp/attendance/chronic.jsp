<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Chronic Absence"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <p class="text-muted">Member 03 · Students with ≥ ${attendanceService.CHRONIC_ABSENCE_THRESHOLD} absences in the last month (PBI-11)</p>
            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-6"><label class="form-label small">Class</label>
                    <select class="form-select" name="classId">
                        <c:forEach items="${classes}" var="c"><option value="${c.id}" ${classId == c.id ? 'selected' : ''}>${c.name}</option></c:forEach>
                    </select></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Analyse</button></div>
            </form>

            <div class="card">
                <table class="table mb-0">
                    <thead><tr><th>Student</th><th>Absences (30 days)</th><th>Guardian linked</th></tr></thead>
                    <tbody>
                    <c:if test="${empty flags}">
                        <tr><td colspan="3"><div class="empty-state"><div class="icon">✅</div>No chronic-absence flags for this class.</div></td></tr>
                    </c:if>
                    <c:forEach items="${flags}" var="f">
                        <tr>
                            <td class="fw-semibold">${f.name} <small class="text-muted">${f.regNo}</small></td>
                            <td><span class="badge badge-ABSENT">${f.absences}</span></td>
                            <td>${f.guardianId != null ? 'Yes — parent notified via notice channel' : 'No guardian linked'}</td>
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
