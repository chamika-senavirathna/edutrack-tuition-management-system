<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Audit Logs"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <p class="text-muted mb-3">Member 05 · Read-only audit trail: user, action, time, old value and new value (BR-UAM-02, PBI-23)</p>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-4"><label class="form-label small">Entity type</label>
                    <input class="form-control" name="entityType" value="${entityType}" placeholder="STUDENT, INVOICE, MARKS…"></div>
                <div class="col-md-2"><label class="form-label small">Entity ID</label>
                    <input class="form-control" name="entityId" value="${entityId}"></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Filter</button></div>
            </form>

            <div class="card mb-4">
                <div class="card-header">Filtered Entries</div>
                <div class="table-responsive">
                    <table class="table table-sm mb-0">
                        <thead><tr><th>Time</th><th>User</th><th>Action</th><th>Entity</th><th>Old</th><th>New</th></tr></thead>
                        <tbody>
                        <c:forEach items="${logs}" var="log">
                            <tr>
                                <td class="small">${log.createdAt}</td>
                                <td>${log.userName != null ? log.userName : log.userId}</td>
                                <td><span class="badge bg-secondary">${log.action}</span></td>
                                <td class="small">${log.entityType}#${log.entityId}</td>
                                <td class="small text-danger text-break">${log.oldValue}</td>
                                <td class="small text-success text-break">${log.newValue}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="card">
                <div class="card-header">Most Recent Activity</div>
                <div class="table-responsive">
                    <table class="table table-sm mb-0">
                        <thead><tr><th>Time</th><th>User</th><th>Action</th><th>Entity</th></tr></thead>
                        <tbody>
                        <c:forEach items="${recent}" var="log">
                            <tr>
                                <td class="small">${log.createdAt}</td>
                                <td>${log.userName != null ? log.userName : log.userId}</td>
                                <td><span class="badge bg-secondary">${log.action}</span></td>
                                <td class="small">${log.entityType}#${log.entityId}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
