<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Correct Attendance"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row">
                <div class="col-lg-6">
                    <form method="post" action="${pageContext.request.contextPath}/attendance/correct">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="id" value="${record.id}">
                        <div class="card"><div class="card-header">Correct Entry</div>
                            <div class="card-body">
                                <p class="small text-muted mb-3">
                                    ${record.studentName} · ${record.className} · ${record.attendanceDate} ·
                                    current status <span class="badge badge-${record.status}">${record.status}</span>.
                                    Corrections are allowed for 48h with a reason (BR-ATT-02); afterwards an
                                    administrator override is applied and audited (BR-ATT-03).
                                </p>
                                <div class="mb-3"><label class="form-label required-label">New status</label>
                                    <select class="form-select" name="status">
                                        <option value="PRESENT">Present</option>
                                        <option value="ABSENT">Absent</option>
                                        <option value="LATE">Late</option>
                                        <option value="EXCUSED">Excused</option>
                                    </select></div>
                                <div class="mb-3"><label class="form-label">Reason</label>
                                    <input class="form-control" name="reason" placeholder="e.g. Medical appointment"></div>
                                <div class="mb-3"><label class="form-label required-label">Correction note (audited)</label>
                                    <textarea class="form-control" name="correctionNote" rows="2" required
                                              placeholder="Why is this entry being corrected?"></textarea></div>
                                <button class="btn btn-primary">Save Correction</button>
                                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/attendance">Cancel</a>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
