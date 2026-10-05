<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Generate Report Card"/>
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
                    <form method="post" action="${pageContext.request.contextPath}/reportcards/generate"
                          data-confirm="Generate the report card from published marks of this term?">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <div class="card"><div class="card-header">Generate from Published Marks</div>
                            <div class="card-body">
                                <div class="mb-3"><label class="form-label required-label">Student</label>
                                    <select class="form-select" name="studentId" required>
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${students}" var="s">
                                            <option value="${s.id}">${s.regNo} — ${s.fullName()}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="mb-3"><label class="form-label required-label">Term</label>
                                    <select class="form-select" name="termId" required>
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${terms}" var="t"><option value="${t.id}">${t.name}</option></c:forEach>
                                    </select></div>
                                <button class="btn btn-primary w-100">Generate</button>
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
