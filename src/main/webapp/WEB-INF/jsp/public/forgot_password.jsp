<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="../common/head.jsp" %>
</head>
<body class="login-page">
    <div class="login-card card shadow">
        <div class="login-header">
            <div class="logo-badge">ET</div>
            <h1 class="h5 mt-3 mb-0 fw-bold">Password Recovery</h1>
            <p class="text-muted small mb-3">Self-service OTP reset (UC-UAM-01)</p>
        </div>
        <div class="card-body p-4">
            <c:if test="${not empty flashError}"><div class="alert alert-danger py-2 small">${flashError}</div></c:if>
            <form method="post" action="${pageContext.request.contextPath}/forgot-password">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <div class="mb-3">
                    <label class="form-label">Username or email</label>
                    <input type="text" class="form-control" name="identity" required>
                </div>
                <button type="submit" class="btn btn-primary w-100">Request OTP</button>
            </form>
            <div class="text-center mt-3"><a href="${pageContext.request.contextPath}/login" class="small">Back to sign in</a></div>
        </div>
    </div>
</body>
</html>
