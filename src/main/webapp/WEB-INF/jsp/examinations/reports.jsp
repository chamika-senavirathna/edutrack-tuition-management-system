<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Reports"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <p class="text-muted">Member 06 · Reporting (URF Appendix C catalogue) · access is role-restricted</p>

            <%-- Monthly attendance + revenue filter and CSV export (M03/M04 aggregates). --%>
            <div class="card mb-4">
                <div class="card-body d-flex flex-wrap align-items-center gap-3">
                    <form method="get" action="${pageContext.request.contextPath}/reports" class="d-flex align-items-center gap-2 mb-0">
                        <label class="form-label mb-0">Month</label>
                        <input type="number" name="month" min="1" max="12" value="${month}" class="form-control" style="width:90px">
                        <label class="form-label mb-0">Year</label>
                        <input type="number" name="year" min="2020" max="2100" value="${year}" class="form-control" style="width:110px">
                        <button class="btn btn-primary btn-sm" type="submit">Show Monthly Report</button>
                    </form>
                    <a class="btn btn-outline-success btn-sm"
                       href="${pageContext.request.contextPath}/reports?format=csv&amp;month=${month}&amp;year=${year}">
                        Export CSV ↓</a>
                </div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-header">Monthly Attendance Percentage · M03 (${month}/${year})</div>
                        <table class="table mb-0">
                            <thead><tr><th>Class</th><th>Entries</th><th>Present</th><th>Late</th><th>Absent</th><th>Attendance %</th></tr></thead>
                            <tbody>
                            <c:forEach items="${attendanceMonthly}" var="row">
                                <tr>
                                    <td>${row.className}</td>
                                    <td>${row.entries}</td>
                                    <td>${row.present}</td>
                                    <td>${row.late}</td>
                                    <td>${row.absent}</td>
                                    <td><c:choose><c:when test="${row.attendancePct != null}">${row.attendancePct}%</c:when><c:otherwise>—</c:otherwise></c:choose></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty attendanceMonthly}">
                                <tr><td colspan="6" class="text-muted">No attendance recorded for ${month}/${year}.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-header">Revenue Collections · M04 (${month}/${year})</div>
                        <table class="table mb-0">
                            <thead><tr><th>Method</th><th>Transactions</th><th>Amount (LKR)</th></tr></thead>
                            <tbody>
                            <c:forEach items="${revenueMonthly}" var="row">
                                <tr><td>${row.method}</td><td>${row.txns}</td><td>${row.amount}</td></tr>
                            </c:forEach>
                            <c:if test="${empty revenueMonthly}">
                                <tr><td colspan="3" class="text-muted">No verified collections in ${month}/${year}.</td></tr>
                            </c:if>
                            <tr class="table-light">
                                <td><strong>Total collected</strong></td><td></td>
                                <td><strong>${revenueTotal}</strong></td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-header">Exam Performance by Subject</div>
                        <table class="table mb-0">
                            <thead><tr><th>Subject</th><th>Avg Mark</th><th>Pass Rate</th><th>Entries</th></tr></thead>
                            <tbody>
                            <c:forEach items="${performance}" var="row">
                                <tr><td>${row.subject}</td><td>${row.avgMark}</td><td>${row.passRate}%</td><td>${row.total}</td></tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card h-100 mb-4">
                        <div class="card-header">Fee Collection by Term (LKR)</div>
                        <table class="table mb-0">
                            <thead><tr><th>Term</th><th>Invoiced</th><th>Collected</th><th>Outstanding</th></tr></thead>
                            <tbody>
                            <c:forEach items="${feeCollection}" var="row">
                                <tr><td>${row.term}</td><td>${row.invoiced}</td><td>${row.collected}</td><td>${row.outstanding}</td></tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    <div class="card h-100">
                        <div class="card-header">Timetable Utilisation by Teacher</div>
                        <table class="table mb-0">
                            <thead><tr><th>Teacher</th><th>Weekly Slots</th></tr></thead>
                            <tbody>
                            <c:forEach items="${utilization}" var="row">
                                <tr><td>${row.teacher}</td><td>${row.slots}</td></tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
