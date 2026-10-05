<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Guardians"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <p class="text-muted">Member 01 · Guardian / linked parent information (parent accounts see only linked children)</p>
            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-5"><label class="form-label small">Search name / NIC / phone</label>
                    <input class="form-control" name="q" value="${q}"></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Search</button></div>
            </form>
            <div class="card">
                <table class="table table-hover mb-0">
                    <thead><tr><th>Name</th><th>NIC</th><th>Phone</th><th>Email</th><th>Relationship</th><th></th></tr></thead>
                    <tbody>
                    <c:if test="${empty guardians}">
                        <tr><td colspan="6"><div class="empty-state"><div class="icon">👨‍👩‍👧</div>No guardians found.</div></td></tr>
                    </c:if>
                    <c:forEach items="${guardians}" var="g">
                        <tr>
                            <td class="fw-semibold">${g.fullName}</td>
                            <td>${g.nic}</td>
                            <td>${g.phone}</td>
                            <td>${g.email}</td>
                            <td>${g.relationship}</td>
                            <td class="text-end"><a class="btn btn-sm btn-outline-secondary"
                                href="${pageContext.request.contextPath}/guardians/edit?id=${g.id}">Edit</a></td>
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
