<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Notice · ${notice.title}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span class="badge bg-secondary">${notice.category}</span>
                    <span class="badge badge-${notice.status}">${notice.status}</span>
                </div>
                <div class="card-body">
                    <h2 class="h4 fw-bold">${notice.title}</h2>
                    <div class="text-muted small mb-3">
                        By ${notice.createdByName} · Audience ${notice.audienceType} ${notice.audienceRefName}
                        · Channel ${notice.channel} · ${notice.publishedAt != null ? notice.publishedAt : notice.createdAt}
                    </div>
                    <p style="white-space: pre-line;">${notice.content}</p>
                </div>
            </div>
            <a class="btn btn-outline-secondary mt-3" href="${pageContext.request.contextPath}/notices">Back to Notices</a>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
