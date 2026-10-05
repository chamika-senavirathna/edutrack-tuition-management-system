<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Page Not Found"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <div class="px-4 pt-4">
            <div class="card"><div class="empty-state py-5">
                <div class="icon">🧭</div>
                <h2 class="h5 fw-bold">Page not found (404)</h2>
                <p class="text-muted">The page you requested does not exist in EduTrack.</p>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Back to Dashboard</a>
            </div></div>
        </div>
    </div>
</body>
</html>
