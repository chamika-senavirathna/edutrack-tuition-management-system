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
            <h1 class="h5 mt-3 mb-0 fw-bold">Set a New Password</h1>
        </div>
        <div class="card-body p-4">
            <c:if test="${otpIssued}">
                <div class="alert alert-info py-2 small">
                    An OTP has been generated (demo environment shows it here; production delivers it by SMS/email).
                    It is valid for 5 minutes.
                </div>
            </c:if>
            <c:if test="${not empty flashError}"><div class="alert alert-danger py-2 small">${flashError}</div></c:if>
            <form method="post" action="${pageContext.request.contextPath}/reset-password">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <div class="mb-3">
                    <label class="form-label">Username or email</label>
                    <input type="text" class="form-control" name="identity" value="${identity}" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">OTP code</label>
                    <input type="text" class="form-control" name="otp" value="${otp}" required maxlength="6">
                </div>
                <div class="mb-3">
                    <label class="form-label">New password</label>
                    <input type="password" class="form-control" name="newPassword" required minlength="8">
                    <div class="form-text">At least 8 characters with letters and numbers.</div>
                </div>
                <button type="submit" class="btn btn-primary w-100">Change Password</button>
            </form>
        </div>
    </div>
</body>
</html>
